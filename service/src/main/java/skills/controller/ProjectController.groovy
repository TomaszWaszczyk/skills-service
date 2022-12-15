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
package skills.controller

import groovy.util.logging.Slf4j
import org.apache.commons.lang3.StringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*
import skills.PublicProps
import skills.auth.UserInfoService
import skills.controller.exceptions.ErrorCode
import skills.controller.exceptions.SkillException
import skills.controller.exceptions.SkillQuizException
import skills.controller.exceptions.SkillsValidator
import skills.controller.request.model.ProjectExistsRequest
import skills.controller.request.model.ProjectRequest
import skills.controller.request.model.QuizDefRequest
import skills.controller.result.model.CustomIconResult
import skills.controller.result.model.InviteTokenValidationResponse
import skills.controller.result.model.ProjectResult
import skills.controller.result.model.RequestResult
import skills.controller.result.model.QuizDefResult
import skills.dbupgrade.DBUpgradeSafe
import skills.icons.CustomIconFacade
import skills.profile.EnableCallStackProf
import skills.services.IdFormatValidator
import skills.services.admin.InviteOnlyProjectService
import skills.services.admin.ProjAdminService
import skills.services.admin.ShareSkillsService
import skills.services.admin.SkillsAdminService
import skills.services.quiz.QuizDefService
import skills.utils.InputSanitizer

import jakarta.servlet.http.HttpServletResponse

@RestController
@RequestMapping("/app")
@Slf4j
@EnableCallStackProf
class ProjectController {

    @Autowired
    ProjAdminService projAdminService

    @Autowired
    QuizDefService quizDefService

    @Autowired
    CustomIconFacade customIconFacade

    @Autowired
    PasswordEncoder passwordEncoder

    @Autowired
    UserInfoService userInfoService

    @Autowired
    PublicPropsBasedValidator propsBasedValidator

    @Autowired
    SkillsAdminService skillsAdminService

    @Autowired
    InviteOnlyProjectService inviteOnlyProjectService

    static final RESERVERED_PROJECT_ID = ShareSkillsService.ALL_SKILLS_PROJECTS

    @RequestMapping(value = "/projects", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    List<ProjectResult> getProjects() {
        return projAdminService.getProjects()
    }

    @RequestMapping(value = "/quiz-definitions", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    List<QuizDefResult> getQuizDefs() {
        return quizDefService.getCurrentUsersTestDefs()
    }

    @RequestMapping(value = "/projects/{id}", method = [RequestMethod.PUT, RequestMethod.POST], produces = "application/json")
    @ResponseBody
    RequestResult saveProject(@PathVariable("id") String projectId, @RequestBody ProjectRequest projectRequest) {
        // project id is optional
        if (projectRequest.projectId && projectId != projectRequest.projectId) {
            throw new SkillException("Project id in the request doesn't equal to project id in the URL [${projectRequest?.projectId}]<>[${projectId}]. Cannot edit project id using /app/projects/{id} endpoint. Please use /admin/projects/{id}", null, null, ErrorCode.AccessDenied)
        }
        IdFormatValidator.validate(projectId)

        propsBasedValidator.validateMaxStrLength(PublicProps.UiProp.maxIdLength, "Project Id", projectId)
        propsBasedValidator.validateMinStrLength(PublicProps.UiProp.minIdLength, "Project Id", projectId)

        if (!projectRequest.projectId) {
            projectRequest.projectId = projectId
        } else {
            IdFormatValidator.validate(projectRequest.projectId)
            propsBasedValidator.validateMaxStrLength(PublicProps.UiProp.maxIdLength, "Project Id", projectRequest.projectId)
            propsBasedValidator.validateMinStrLength(PublicProps.UiProp.minIdLength, "Project Id", projectRequest.projectId)
        }

        propsBasedValidator.validateMaxStrLength(PublicProps.UiProp.maxProjectNameLength, "Project Name", projectRequest.name)
        propsBasedValidator.validateMinStrLength(PublicProps.UiProp.minNameLength, "Project Name", projectRequest.name)

        if (projectRequest.projectId == RESERVERED_PROJECT_ID) {
            throw new SkillException("Project id uses a reserved id, please choose a different project id.", projectId, null, ErrorCode.BadParam)
        }
        if (!projectRequest?.name) {
            throw new SkillException("Project name was not provided.", projectId, null, ErrorCode.BadParam)
        }

        projectRequest.projectId = InputSanitizer.sanitize(projectRequest.projectId)
        projectRequest.name = InputSanitizer.sanitize(projectRequest.name)?.trim()
        projectRequest.description = StringUtils.trimToNull(InputSanitizer.sanitize(projectRequest.description))

        projAdminService.saveProject(null, projectRequest)
        return new RequestResult(success: true)
    }

    @RequestMapping(value = "/quiz-definitions/{id}", method = [RequestMethod.PUT, RequestMethod.POST], produces = "application/json")
    @ResponseBody
    QuizDefResult saveQuizDef(@PathVariable("id") String quizId, @RequestBody QuizDefRequest quizDefRequest) {
        // project id is optional
        if (quizDefRequest.quizId && quizId != quizDefRequest.quizId) {
            throw new SkillQuizException("Quiz id in the request doesn't equal to quiz id in the URL [${quizDefRequest?.quizId}]<>[${quizId}]. Cannot edit quiz id using /app/quiz-definitions/{id} endpoint. Please use /admin/quiz-definitions/{id}", quizId, ErrorCode.AccessDenied)
        }
        IdFormatValidator.validate(quizId)

        propsBasedValidator.validateMaxStrLength(PublicProps.UiProp.maxIdLength, "QuizId Id", quizId)
        propsBasedValidator.validateMinStrLength(PublicProps.UiProp.minIdLength, "QuizId Id", quizId)

        if (!quizDefRequest.quizId) {
            quizDefRequest.quizId = quizId
        } else {
            IdFormatValidator.validate(quizDefRequest.quizId)
            propsBasedValidator.validateMaxStrLength(PublicProps.UiProp.maxIdLength, "Quiz Id", quizDefRequest.quizId)
            propsBasedValidator.validateMinStrLength(PublicProps.UiProp.minIdLength, "Quiz Id", quizDefRequest.quizId)
        }

        propsBasedValidator.validateMaxStrLength(PublicProps.UiProp.maxQuizNameLength, "Quiz Name", quizDefRequest.name)
        propsBasedValidator.validateMinStrLength(PublicProps.UiProp.minNameLength, "Quiz Name", quizDefRequest.name)

        if (quizDefRequest.quizId == RESERVERED_PROJECT_ID) {
            throw new SkillQuizException("Quiz Id uses a reserved id, please choose a different Quiz Id.", quizDefRequest.quizId, ErrorCode.BadParam)
        }
        if (!quizDefRequest?.name) {
            throw new SkillQuizException("Quiz name was not provided.", quizId, ErrorCode.BadParam)
        }

        quizDefRequest.quizId = InputSanitizer.sanitize(quizDefRequest.quizId)
        quizDefRequest.name = InputSanitizer.sanitize(quizDefRequest.name)?.trim()
        quizDefRequest.description = StringUtils.trimToNull(InputSanitizer.sanitize(quizDefRequest.description))

        return quizDefService.saveQuizDef(null, quizDefRequest)
    }

    @RequestMapping(value = "/projects/{id}/join/{invite_code}", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    RequestResult joinProject(@PathVariable("id") String projectId, @PathVariable("invite_code") String inviteCode) {
        SkillsValidator.isNotBlank(projectId, "projectId")
        SkillsValidator.isNotBlank(inviteCode, "invite_code", projectId)
        inviteOnlyProjectService.joinProject(inviteCode, projectId)
        return RequestResult.success()
    }

    @RequestMapping(value = "/projects/{id}/validateInvite/{invite_code}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    InviteTokenValidationResponse validateProjectInvite(@PathVariable("id") String projectId, @PathVariable("invite_code") String inviteCode) {
        SkillsValidator.isNotBlank(projectId, "projectId")
        SkillsValidator.isNotBlank(inviteCode, "inviteCode")
        return inviteOnlyProjectService.validateInvite(inviteCode, projectId)
    }

    @DBUpgradeSafe
    @RequestMapping(value = "/projectExist", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    boolean doesProjectExist(@RequestBody ProjectExistsRequest existsRequest) {
        String projectId = existsRequest.projectId
        String projectName = existsRequest.name?.trim()

        SkillsValidator.isTrue((projectId || projectName), "One of Project Id or Project Name must be provided.")
        SkillsValidator.isTrue(!(projectId && projectName), "Only Project Id or Project Name may be provided, not both.")

        if (projectId) {
            return projAdminService.existsByProjectId(InputSanitizer.sanitize(projectId))
        }

        return projAdminService.existsByProjectName(InputSanitizer.sanitize(projectName))
    }

    @RequestMapping(value = "/projects/{id}/customIcons", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    List<CustomIconResult> getCustomIcons(@PathVariable("id") String projectId) {
        return projAdminService.getCustomIcons(projectId)
    }

    @RequestMapping(value = "/projects/{id}/versions", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    List<Integer> listVersions(@PathVariable("id") String projectId) {
        return skillsAdminService.getUniqueSkillVersionList(projectId)
    }

}
