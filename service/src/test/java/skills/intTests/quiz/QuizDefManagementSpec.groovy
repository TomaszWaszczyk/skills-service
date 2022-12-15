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
package skills.intTests.quiz

import groovy.util.logging.Slf4j
import skills.intTests.utils.DefaultIntSpec
import skills.intTests.utils.QuizDefFactory

@Slf4j
class QuizDefManagementSpec extends DefaultIntSpec {

    def "no quiz definitions"() {
        when:
        def quizDefs = skillsService.getQuizDefs()

        then:
        !quizDefs
    }

    def "create quiz definition"() {
        def quiz = QuizDefFactory.createQuiz(1)

        when:
        def newQuiz = skillsService.createQuizDef(quiz)

        def quizDefs = skillsService.getQuizDefs()

        then:
        newQuiz.body.quizId == quiz.quizId
        newQuiz.body.name == quiz.name

        quizDefs.quizId == [quiz.quizId]
        quizDefs.name == [quiz.name]
    }

}

