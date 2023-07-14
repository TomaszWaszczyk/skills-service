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
package skills.storage.repos

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.lang.Nullable
import skills.storage.model.SkillAttributesDef

interface SkillAttributesDefRepo extends CrudRepository<SkillAttributesDef, Long> {

    @Nullable
    SkillAttributesDef findBySkillRefIdAndType(Integer skillRefId, SkillAttributesDef.SkillAttributesType type)

    static interface VideoSummaryAttributes {
        String getUrl()
        String getType()
        Boolean getHasCaptions()
        Boolean getHasTranscript()
    }

    @Nullable
    @Query(value = '''select attributes ->> 'videoUrl' as url,
           attributes ->> 'videoType' as type,
           case when attributes -> 'captions' is not null then true else false end   as hasCaptions,
           case when attributes -> 'transcript' is not null then true else false end as hasTranscript
        from skill_attributes_definition
        where type= 'Video' and skill_ref_id = ?1''', nativeQuery = true)
    VideoSummaryAttributes getVideoSummary(Integer skillRefId)

}
