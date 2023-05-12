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
package skills.intTests.dependentSkills


import skills.intTests.utils.DefaultIntSpec
import skills.intTests.utils.SkillsClientException
import skills.intTests.utils.SkillsFactory

import static skills.intTests.utils.SkillsFactory.*

class AdminLearningPathCircularDependencySpecs extends DefaultIntSpec {

    def "skill1 -> skill2 -> skill1 circular dep"() {
        def p1 = createProject(1)
        def p1subj1 = createSubject(1, 1)
        def p1Skills = createSkills(5, 1, 1, 100)
        skillsService.createProjectAndSubjectAndSkills(p1, p1subj1, p1Skills)

        skillsService.addLearningPathPrerequisite(p1.projectId, p1Skills[0].skillId, p1.projectId, p1Skills[2].skillId)
        when:
        skillsService.addLearningPathPrerequisite(p1.projectId, p1Skills[2].skillId, p1.projectId, p1Skills[0].skillId)
        then:
        SkillsClientException e = thrown(SkillsClientException)
        e.getMessage().contains("Discovered circular prerequisite [Skill:${p1Skills[2].skillId} -> Skill:${p1Skills[0].skillId} -> Skill:${p1Skills[2].skillId}]")
    }

    def "skill1 -> skill2 -> skill3 -> skill1 circular dep"() {
        def p1 = createProject(1)
        def p1subj1 = createSubject(1, 1)
        def p1Skills = createSkills(5, 1, 1, 100)
        skillsService.createProjectAndSubjectAndSkills(p1, p1subj1, p1Skills)

        skillsService.addLearningPathPrerequisite(p1.projectId, p1Skills[1].skillId, p1.projectId, p1Skills[2].skillId)
        skillsService.addLearningPathPrerequisite(p1.projectId, p1Skills[0].skillId, p1.projectId, p1Skills[1].skillId)
        when:
        skillsService.addLearningPathPrerequisite(p1.projectId, p1Skills[2].skillId, p1.projectId, p1Skills[0].skillId)
        then:
        SkillsClientException e = thrown(SkillsClientException)
        e.getMessage().contains("Discovered circular prerequisite [Skill:${p1Skills[2].skillId} -> Skill:${p1Skills[0].skillId} -> Skill:${p1Skills[1].skillId} -> Skill:${p1Skills[2].skillId}]")
    }

    def "badge(skill1) -> skill1 learning path: badge cannot contain the skill"() {
        def p1 = createProject(1)
        def p1subj1 = createSubject(1, 1)
        def p1Skills = createSkills(5, 1, 1, 100)
        skillsService.createProjectAndSubjectAndSkills(p1, p1subj1, p1Skills)

        def badge = SkillsFactory.createBadge()
        skillsService.createBadge(badge)
        skillsService.assignSkillToBadge([projectId: p1.projectId, badgeId: badge.badgeId, skillId: p1Skills[0].skillId])
        skillsService.assignSkillToBadge([projectId: p1.projectId, badgeId: badge.badgeId, skillId: p1Skills[1].skillId])
        badge.enabled = true
        skillsService.createBadge(badge)

        when:
        skillsService.addLearningPathPrerequisite(p1.projectId, p1Skills[0].skillId, p1.projectId, badge.badgeId)
        then:
        SkillsClientException e = thrown(SkillsClientException)
        e.getMessage().contains("Discovered circular prerequisite [Skill:${p1Skills[0].skillId} -> Badge:${badge.badgeId}(Skill:${p1Skills[0].skillId})]")
    }

    def "skill1 -> badge(skill2) -> skill1 learning path: badge cannot contain the skill"() {
        def p1 = createProject(1)
        def p1subj1 = createSubject(1, 1)
        def p1Skills = createSkills(5, 1, 1, 100)
        skillsService.createProjectAndSubjectAndSkills(p1, p1subj1, p1Skills)

        def badge = SkillsFactory.createBadge()
        skillsService.createBadge(badge)
        skillsService.assignSkillToBadge([projectId: p1.projectId, badgeId: badge.badgeId, skillId: p1Skills[0].skillId])
        skillsService.assignSkillToBadge([projectId: p1.projectId, badgeId: badge.badgeId, skillId: p1Skills[1].skillId])
        badge.enabled = true
        skillsService.createBadge(badge)

        skillsService.addLearningPathPrerequisite(p1.projectId, badge.badgeId, p1.projectId, p1Skills[2].skillId)
        when:
        skillsService.addLearningPathPrerequisite(p1.projectId, p1Skills[2].skillId, p1.projectId, badge.badgeId)
        then:
        SkillsClientException e = thrown(SkillsClientException)
        e.getMessage().contains("Discovered circular prerequisite [Skill:${p1Skills[2].skillId} -> Badge:${badge.badgeId} -> Skill:${p1Skills[2].skillId}]")
    }

    def "badge(skill2) -> skill1 -> badge(skill2) learning path: badge cannot contain the skill"() {
        def p1 = createProject(1)
        def p1subj1 = createSubject(1, 1)
        def p1Skills = createSkills(5, 1, 1, 100)
        skillsService.createProjectAndSubjectAndSkills(p1, p1subj1, p1Skills)

        def badge = SkillsFactory.createBadge()
        skillsService.createBadge(badge)
        skillsService.assignSkillToBadge([projectId: p1.projectId, badgeId: badge.badgeId, skillId: p1Skills[0].skillId])
        skillsService.assignSkillToBadge([projectId: p1.projectId, badgeId: badge.badgeId, skillId: p1Skills[1].skillId])
        badge.enabled = true
        skillsService.createBadge(badge)

        skillsService.addLearningPathPrerequisite(p1.projectId, p1Skills[2].skillId, p1.projectId, badge.badgeId)
        when:
        skillsService.addLearningPathPrerequisite(p1.projectId, badge.badgeId, p1.projectId, p1Skills[2].skillId)

        then:
        SkillsClientException e = thrown(SkillsClientException)
        e.getMessage().contains("Discovered circular prerequisite [Badge:${badge.badgeId} -> Skill:${p1Skills[2].skillId} -> Badge:${badge.badgeId}]")
    }

    def "[badge1, badge2] -> [badge3, badge4] -> skill1 -> badge2) learning path: badge cannot contain the skill"() {
        def p1 = createProject(1)
        def p1subj1 = createSubject(1, 1)
        def p1Skills = createSkills(5, 1, 1, 100)
        skillsService.createProjectAndSubjectAndSkills(p1, p1subj1, p1Skills)

        def badge1 = SkillsFactory.createBadge(1, 1)
        skillsService.createBadge(badge1)
        skillsService.assignSkillToBadge([projectId: p1.projectId, badgeId: badge1.badgeId, skillId: p1Skills[0].skillId])
        badge1.enabled = true
        skillsService.createBadge(badge1)

        def badge2 = SkillsFactory.createBadge(1, 2)
        skillsService.createBadge(badge2)
        skillsService.assignSkillToBadge([projectId: p1.projectId, badgeId: badge2.badgeId, skillId: p1Skills[1].skillId])
        badge2.enabled = true
        skillsService.createBadge(badge2)

        def badge3 = SkillsFactory.createBadge(1, 3)
        skillsService.createBadge(badge3)
        skillsService.assignSkillToBadge([projectId: p1.projectId, badgeId: badge3.badgeId, skillId: p1Skills[2].skillId])
        badge3.enabled = true
        skillsService.createBadge(badge3)

        def badge4 = SkillsFactory.createBadge(1, 4)
        skillsService.createBadge(badge4)
        skillsService.assignSkillToBadge([projectId: p1.projectId, badgeId: badge4.badgeId, skillId: p1Skills[3].skillId])
        badge4.enabled = true
        skillsService.createBadge(badge4)

        skillsService.addLearningPathPrerequisite(p1.projectId, badge3.badgeId, p1.projectId, badge1.badgeId)
        skillsService.addLearningPathPrerequisite(p1.projectId, badge4.badgeId, p1.projectId, badge1.badgeId)
        skillsService.addLearningPathPrerequisite(p1.projectId, badge3.badgeId, p1.projectId, badge2.badgeId)
        skillsService.addLearningPathPrerequisite(p1.projectId, badge4.badgeId, p1.projectId, badge2.badgeId)
        skillsService.addLearningPathPrerequisite(p1.projectId, p1Skills[4].skillId, p1.projectId, badge3.badgeId)
        skillsService.addLearningPathPrerequisite(p1.projectId, p1Skills[4].skillId, p1.projectId, badge4.badgeId)
        when:
        skillsService.addLearningPathPrerequisite(p1.projectId, badge2.badgeId, p1.projectId, p1Skills[4].skillId)

        then:
        SkillsClientException e = thrown(SkillsClientException)
        (e.getMessage().contains("Discovered circular prerequisite [Badge:${badge2.badgeId} -> Skill:${p1Skills[4].skillId} -> Badge:${badge4.badgeId} -> Badge:${badge2.badgeId}]")
                ||
                e.getMessage().contains("Discovered circular prerequisite [Badge:${badge2.badgeId} -> Skill:${p1Skills[4].skillId} -> Badge:${badge3.badgeId} -> Badge:${badge2.badgeId}]"))
    }

    def "badge1 -> badge2: two badges cannot contain the same skill in the same learning path"() {
        def p1 = createProject(1)
        def p1subj1 = createSubject(1, 1)
        def p1Skills = createSkills(10, 1, 1, 100)
        skillsService.createProjectAndSubjectAndSkills(p1, p1subj1, p1Skills)

        def badge1 = SkillsFactory.createBadge(1, 1)
        skillsService.createBadge(badge1)
        skillsService.assignSkillToBadge([projectId: p1.projectId, badgeId: badge1.badgeId, skillId: p1Skills[0].skillId])
        skillsService.assignSkillToBadge([projectId: p1.projectId, badgeId: badge1.badgeId, skillId: p1Skills[1].skillId])
        badge1.enabled = true
        skillsService.createBadge(badge1)

        def badge2 = SkillsFactory.createBadge(1, 2)
        skillsService.createBadge(badge2)
        skillsService.assignSkillToBadge([projectId: p1.projectId, badgeId: badge2.badgeId, skillId: p1Skills[2].skillId])
        skillsService.assignSkillToBadge([projectId: p1.projectId, badgeId: badge2.badgeId, skillId: p1Skills[1].skillId]) // same skill
        badge2.enabled = true
        skillsService.createBadge(badge2)

        when:
        skillsService.addLearningPathPrerequisite(p1.projectId, badge2.badgeId, p1.projectId, badge1.badgeId)
        then:
        SkillsClientException e = thrown(SkillsClientException)
        e.message.contains("Multiple badges on the same Learning path cannot have overlapping skills. There is already a badge [Test Badge 1] on this learning path that has the same skill as [Test Badge 2] badge. The skill in conflict is [Test Skill 2].")
    }

    def "skill3 -> skill4 -> badge1 -> skill5 -> skill6 -> badge2: two badges cannot contain the same skill in the same learning path"() {
        def p1 = createProject(1)
        def p1subj1 = createSubject(1, 1)
        def p1Skills = createSkills(10, 1, 1, 100)
        skillsService.createProjectAndSubjectAndSkills(p1, p1subj1, p1Skills)

        def badge1 = SkillsFactory.createBadge(1, 1)
        skillsService.createBadge(badge1)
        skillsService.assignSkillToBadge([projectId: p1.projectId, badgeId: badge1.badgeId, skillId: p1Skills[0].skillId])
        skillsService.assignSkillToBadge([projectId: p1.projectId, badgeId: badge1.badgeId, skillId: p1Skills[1].skillId])
        badge1.enabled = true
        skillsService.createBadge(badge1)

        def badge2 = SkillsFactory.createBadge(1, 2)
        skillsService.createBadge(badge2)
        skillsService.assignSkillToBadge([projectId: p1.projectId, badgeId: badge2.badgeId, skillId: p1Skills[2].skillId])
        skillsService.assignSkillToBadge([projectId: p1.projectId, badgeId: badge2.badgeId, skillId: p1Skills[1].skillId]) // same skill
        badge2.enabled = true
        skillsService.createBadge(badge2)

        // skill3 -> skill4 -> badge1 -> skill5 -> skill6 -> badge2
        skillsService.addLearningPathPrerequisite(p1.projectId, p1Skills[4].skillId, p1.projectId, p1Skills[3].skillId)
        skillsService.addLearningPathPrerequisite(p1.projectId, badge1.badgeId, p1.projectId, p1Skills[4].skillId)
        skillsService.addLearningPathPrerequisite(p1.projectId, p1Skills[5].skillId, p1.projectId, badge1.badgeId)
        skillsService.addLearningPathPrerequisite(p1.projectId, p1Skills[6].skillId, p1.projectId, p1Skills[5].skillId)
        when:
        skillsService.addLearningPathPrerequisite(p1.projectId, badge2.badgeId, p1.projectId, p1Skills[6].skillId)

        then:
        SkillsClientException e = thrown(SkillsClientException)
        e.message.contains("Multiple badges on the same Learning path cannot have overlapping skills. There is already a badge [Test Badge 1] on this learning path that has the same skill as [Test Badge 2] badge. The skill in conflict is [Test Skill 2].")
    }

}
