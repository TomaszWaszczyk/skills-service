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
package skills.controller.result.model

class DependencyCheckResult {
    static enum FailureType {
        CircularLearningPath, BadgeOverlappingSkills, BadgeSkillIsAlreadyOnPath, AlreadyExist, SkillInCatalog, ReusedSkill, SkillVersion, NotEligible
    }
    boolean possible = true

    FailureType failureType
    String reason

    // if violating skill was found inside of a badge,
    // applies to CircularLearningPath, BadgeOverlappingSkills types
    String violatingSkillInBadgeId
    String violatingSkillInBadgeName

    // applies to BadgeOverlappingSkills, BadgeSkillIsAlreadyOnPath, AlreadyExist types
    String violatingSkillId
    String violatingSkillName
}
