/**
 * Copyright 2020 SkillTree
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package skills.services.admin

import callStack.profiler.Profile
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import skills.controller.result.model.SettingsResult
import skills.services.RuleSetDefinitionScoreUpdater
import skills.services.UserAchievementsAndPointsManagement
import skills.services.settings.Settings
import skills.services.settings.SettingsService
import skills.storage.model.SkillDef
import skills.storage.repos.SkillDefRepo
import skills.storage.repos.UserAchievedLevelRepo
import skills.storage.repos.UserPointsRepo
import skills.storage.repos.nativeSql.NativeQueriesRepo

import jakarta.transaction.Transactional

@Service
@Slf4j
class BatchOperationsTransactionalAccessor {

    @Autowired
    SkillDefRepo skillDefRepo

    @Autowired
    UserPointsRepo userPointsRepo

    @Autowired
    UserAchievedLevelRepo userAchievedLevelRepo

    @Autowired
    NativeQueriesRepo nativeQueriesRepo

    @Autowired
    UserAchievementsAndPointsManagement userAchievementsAndPointsManagement

    @Autowired
    RuleSetDefinitionScoreUpdater ruleSetDefinitionScoreUpdater

    @Autowired
    SkillsGroupAdminService skillsGroupAdminService

    @Autowired
    SettingsService settingsService

    @Transactional
    @Profile
    void enableSkills(List<SkillDef> disabledImportedSkills) {
        disabledImportedSkills.each {
            it.enabled = Boolean.TRUE.toString()
        }
        skillDefRepo.saveAll(disabledImportedSkills)
    }

    /**
     * Handle subject after there has been user points change in one of its skills
     * (1) create user points for new users
     * (2) update existing
     * (3) calculate achievements
     */
    @Transactional
    void handlePointsAndAchievementsForSubject(SkillDef subject) {
        String projectId = subject.projectId
        String subjectId = subject.skillId

        log.info("Creating UserPoints for the new users for [{}-{}] subject", projectId, subjectId)
        Integer numRows = createSubjectUserPointsForTheNewUsers(projectId, subjectId)
        log.info("Created [{}] UserPoints for the new users for [{}-{}] subject", numRows, projectId, subjectId)

        log.info("Updating UserPoints for the existing users for [{}-{}] subject", projectId, subjectId)
        updateUserPointsForSubject(projectId, subjectId)

        log.info("Identifying subject level achievements for [{}-{}] subject", projectId, subjectId)
        identifyAndAddSubjectLevelAchievements(subject.projectId, subjectId)
        log.info("Completed import for subject. projectIdTo=[{}], subjectIdTo=[{}]", projectId, subjectId)
    }

    /**
     * Handle project after there has been user points change in one of its subjects or skills
     * (1) create user points for new users
     * (2) update existing
     * (3) calculate achievements
     * @param projectId
     */
    @Transactional
    void handlePointsAndAchievementsForProject(String projectId) {
        SettingsResult settingsResult = settingsService.getProjectSetting(projectId, Settings.LEVEL_AS_POINTS.settingName)
        boolean pointsBased = settingsResult ? settingsResult.isEnabled() : false

        log.info("Creating UserPoints for the new users for [{}] project", projectId)
        createProjectUserPointsForTheNewUsers(projectId)
        log.info("Competed creating UserPoints for the new users for [{}] project", projectId)

        log.info("Updating UserPoints for the existing users for [{}] project", projectId)
        updateUserPointsForProject(projectId)
        log.info("Completed updating UserPoints for the existing users for [{}] project", projectId)

        log.info("Identifying and adding project level achievements for [{}] project, pointsBased=[{}]", projectId, pointsBased)
        identifyAndAddProjectLevelAchievements(projectId, pointsBased)
        log.info("Completed identifying and adding project level achievements for [{}] project, pointsBased=[{}]", projectId, pointsBased)
    }

    @Transactional
    @Profile
    void updateSubjectTotalPoints(String projectId, String subjectId) {
        ruleSetDefinitionScoreUpdater.updateSubjectTotalPoints(projectId, subjectId, false)
    }


    @Transactional
    @Profile
    void updateGroupTotalPoints(String projectId, String groupId) {
        ruleSetDefinitionScoreUpdater.updateGroupTotalPoints(projectId, groupId, false)
    }

    @Transactional
    @Profile
    void updateProjectsTotalPoints(String projectId) {
        skillDefRepo.updateProjectsTotalPoints(projectId, false)
    }

    @Transactional
    @Profile
    void copySkillUserPointsToTheImportedProjects(String toProjectId, List<Integer> fromSkillRefIds) {
        userPointsRepo.copySkillUserPointsToTheImportedProjects(toProjectId, fromSkillRefIds)
    }

    @Transactional
    @Profile
    void copySkillAchievementsToTheImportedProjects(List<Integer> fromSkillRefIds) {
        userAchievedLevelRepo.copySkillAchievementsToTheImportedProjects(fromSkillRefIds)
    }

    @Transactional
    @Profile
    Integer createSubjectUserPointsForTheNewUsers(String toProjectId, String toSubjectId) {
        userPointsRepo.createSubjectUserPointsForTheNewUsers(toProjectId, toSubjectId)
    }

    @Transactional
    @Profile
    void createSkillUserPointsFromPassedQuizzes(Integer quizRefId, Integer skillRefId) {
        log.info("Creating UserPoints for users that passed the quiz: quizRefId=[{}], skillId=[{}]", quizRefId, skillRefId)
        userPointsRepo.createSkillUserPointsFromPassedQuizzes(quizRefId, skillRefId)
        log.info("Completed creating UserPoints for users that passed the quiz: quizRefId=[{}], skillId=[{}]", quizRefId, skillRefId)
    }

    @Transactional
    @Profile
    void createUserPerformedEntriesFromPassedQuizzes(Integer quizRefId, Integer skillRefId) {
        log.info("Creating UserPerformedSkills for users that passed the quiz: quizRefId=[{}], skillId=[{}]", quizRefId, skillRefId)
        userPointsRepo.createUserPerformedEntriesFromPassedQuizzes(quizRefId, skillRefId)
        log.info("Completed reating UserPerformedSkills for users that passed the quiz: quizRefId=[{}], skillId=[{}]", quizRefId, skillRefId)
    }

    @Transactional
    @Profile
    void createUserAchievementsFromPassedQuizzes(Integer quizRefId, Integer skillRefId) {
        log.info("Creating UserAchievements for users that passed the quiz: quizRefId=[{}], skillRefId=[{}]", quizRefId, skillRefId)
        userPointsRepo.createUserAchievementsFromPassedQuizzes(quizRefId, skillRefId)
        log.info("Completed creating UserAchievements for users that passed the quiz: quizRefId=[{}], skillRefId=[{}]", quizRefId, skillRefId)
    }

    @Transactional
    @Profile
    void updateUserPointsForSubject(String projectId, String skillId) {
        nativeQueriesRepo.updateUserPointsForSubject(projectId, skillId, false)
    }

    @Transactional
    @Profile
    void identifyAndAddGroupAchievements(List<SkillDef> groups) {
        groups.each { SkillDef skillsGroupSkillDef ->
            int numSkillsRequired = skillsGroupAdminService.getActualNumSkillsRequred(skillsGroupSkillDef.numSkillsRequired, skillsGroupSkillDef.id)
            log.info("Identifying group achievements groupRefId=[{}], groupId=[{}.{}], numSkillsRequired=[{}]",
                    skillsGroupSkillDef.id, skillsGroupSkillDef.projectId, skillsGroupSkillDef.skillId, numSkillsRequired)
            userAchievedLevelRepo.identifyAndAddGroupAchievements(
                    skillsGroupSkillDef.projectId,
                    skillsGroupSkillDef.skillId,
                    skillsGroupSkillDef.id,
                    numSkillsRequired,
                    Boolean.FALSE.toString(),
            )
            log.info("Finished identifying group achievements groupRefId=[{}], groupId=[{}.{}], numSkillsRequired=[{}]",
                    skillsGroupSkillDef.id, skillsGroupSkillDef.projectId, skillsGroupSkillDef.skillId, numSkillsRequired)
        }
    }

    @Transactional
    @Profile
    void identifyAndAddSubjectLevelAchievements(String projectId, String subjectId) {
        SkillDef subject = skillDefRepo.findByProjectIdAndSkillId(projectId, subjectId)
        userAchievementsAndPointsManagement.identifyAndAddSubjectLevelAchievements(subject)
    }

    @Transactional
    @Profile
    void createProjectUserPointsForTheNewUsers(String toProjectId) {
        userPointsRepo.createProjectUserPointsForTheNewUsers(toProjectId)
    }

    @Transactional
    @Profile
    void updateUserPointsForProject(String projectId) {
        nativeQueriesRepo.updateUserPointsForProject(projectId)
    }

    @Transactional
    @Profile
    void identifyAndAddProjectLevelAchievements(String projectId, boolean pointsBasedLevels){
        userAchievementsAndPointsManagement.identifyAndAddProjectLevelAchievements(projectId)
    }

}
