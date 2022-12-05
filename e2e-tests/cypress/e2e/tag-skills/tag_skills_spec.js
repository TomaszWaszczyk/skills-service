/*
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
describe('Tag Skills Tests', () => {

    beforeEach(() => {
        cy.createProject(1);
        cy.createSubject(1, 1);
        cy.createSkill(1, 1, 1);
        cy.createSubject(1, 2);
        cy.createSkill(1, 1, 2);
        cy.createSkill(1, 1, 3);
    });

    it('tag skills with new tag', () => {

        cy.visit('/administrator/projects/proj1/subjects/subj1');

        // must exist initially
        cy.get('[data-cy="manageSkillLink_skill1"]');
        cy.get('[data-cy="manageSkillLink_skill2"]');
        cy.get('[data-cy="manageSkillLink_skill3"]');

        cy.get('[data-cy="skillSelect-skill1"]')
          .click({ force: true });
        cy.get('[data-cy="skillSelect-skill3"]')
          .click({ force: true });
        cy.get('[data-cy="skillActionsBtn"]')
          .click();
        cy.get('[data-cy="tagSkillBtn"]')
          .click();

        cy.get('[data-cy="newTagInput"]').type('New Tag 1')
        cy.get('[data-cy="addTagsButton"]').click()

        cy.get('[data-cy="skillTag-skill1-newtag1"]').should('exist')
        cy.get('[data-cy="skillTag-skill2-newtag1"]').should('not.exist')
        cy.get('[data-cy="skillTag-skill3-newtag1"]').should('exist')
    });

    it('tag skills with existing tag', () => {

        cy.visit('/administrator/projects/proj1/subjects/subj1');

        // must exist initially
        cy.get('[data-cy="manageSkillLink_skill1"]');
        cy.get('[data-cy="manageSkillLink_skill2"]');
        cy.get('[data-cy="manageSkillLink_skill3"]');

        cy.get('[data-cy="skillSelect-skill1"]')
          .click({ force: true });
        cy.get('[data-cy="skillSelect-skill3"]')
          .click({ force: true });
        cy.get('[data-cy="skillActionsBtn"]')
          .click();
        cy.get('[data-cy="tagSkillBtn"]')
          .click();

        cy.get('[data-cy="newTagInput"]').type('New Tag 1')
        cy.get('[data-cy="addTagsButton"]').click()

        cy.get('[data-cy="skillTag-skill1-newtag1"]').should('exist')
        cy.get('[data-cy="skillTag-skill2-newtag1"]').should('not.exist')
        cy.get('[data-cy="skillTag-skill3-newtag1"]').should('exist')

        cy.get('[data-cy="clearSelectedSkillsBtn"]')
          .click();
        cy.get('[data-cy="skillSelect-skill2"]')
          .click({ force: true });

        cy.get('[data-cy="skillActionsBtn"]')
          .click();
        cy.get('[data-cy="tagSkillBtn"]')
          .click();

        cy.get('[data-cy="existingTagDropdown"]')
          .click();
        cy.get('[data-cy="existingTagDropdown"] .vs__dropdown-option')
          .eq(0)
          .click()
        cy.get('[data-cy="addTagsButton"]').click()
        cy.get('[data-cy="skillTag-skill1-newtag1"]').should('exist')
        cy.get('[data-cy="skillTag-skill2-newtag1"]').should('exist')
        cy.get('[data-cy="skillTag-skill3-newtag1"]').should('exist')
    });

    it('remove a tag from a skill', () => {

        cy.visit('/administrator/projects/proj1/subjects/subj1');

        // must exist initially
        cy.get('[data-cy="manageSkillLink_skill1"]');
        cy.get('[data-cy="manageSkillLink_skill2"]');
        cy.get('[data-cy="manageSkillLink_skill3"]');

        cy.get('[data-cy="skillSelect-skill1"]')
          .click({ force: true });
        cy.get('[data-cy="skillSelect-skill3"]')
          .click({ force: true });
        cy.get('[data-cy="skillActionsBtn"]')
          .click();
        cy.get('[data-cy="tagSkillBtn"]')
          .click();

        cy.get('[data-cy="newTagInput"]').type('New Tag 1')
        cy.get('[data-cy="addTagsButton"]').click()

        cy.get('[data-cy="skillTag-skill1-newtag1"]').should('exist')
        cy.get('[data-cy="skillTag-skill2-newtag1"]').should('not.exist')
        cy.get('[data-cy="skillTag-skill3-newtag1"]').should('exist')

        cy.get('[data-cy="clearSelectedSkillsBtn"]')
          .click();
        cy.get('[data-cy="skillSelect-skill1"]')
          .click({ force: true });
        cy.get('[data-cy="skillActionsBtn"]')
          .click();
        cy.get('[data-cy="untagSkillBtn"]')
          .click();
        cy.get('[data-cy="existingTagDropdown"]')
          .click();
        cy.get('[data-cy="existingTagDropdown"] .vs__dropdown-option')
          .eq(0)
          .click()
        cy.get('[data-cy="deleteTagsButton"]').click()
        cy.get('[data-cy="skillTag-skill1-newtag1"]').should('not.exist')
    });

    it('remove a tag from a skills where not all selected skills have the tag being removed', () => {

        cy.visit('/administrator/projects/proj1/subjects/subj1');

        // must exist initially
        cy.get('[data-cy="manageSkillLink_skill1"]');
        cy.get('[data-cy="manageSkillLink_skill2"]');
        cy.get('[data-cy="manageSkillLink_skill3"]');

        cy.get('[data-cy="skillSelect-skill1"]')
          .click({ force: true });
        cy.get('[data-cy="skillSelect-skill3"]')
          .click({ force: true });
        cy.get('[data-cy="skillActionsBtn"]')
          .click();
        cy.get('[data-cy="tagSkillBtn"]')
          .click();

        cy.get('[data-cy="newTagInput"]').type('New Tag 1')
        cy.get('[data-cy="addTagsButton"]').click()

        cy.get('[data-cy="skillTag-skill1-newtag1"]').should('exist')
        cy.get('[data-cy="skillTag-skill2-newtag1"]').should('not.exist')
        cy.get('[data-cy="skillTag-skill3-newtag1"]').should('exist')

        cy.get('[data-cy="clearSelectedSkillsBtn"]')
          .click();
        cy.get('[data-cy="skillSelect-skill1"]')
          .click({ force: true });
        cy.get('[data-cy="skillSelect-skill2"]')
          .click({ force: true });
        cy.get('[data-cy="skillActionsBtn"]')
          .click();
        cy.get('[data-cy="untagSkillBtn"]')
          .click();
        cy.get('[data-cy="existingTagDropdown"]')
          .click();
        cy.get('[data-cy="existingTagDropdown"] .vs__dropdown-option')
          .eq(0)
          .click()
        cy.get('[data-cy="deleteTagsButton"]').click()
        cy.get('[data-cy="skillTag-skill1-newtag1"]').should('not.exist')
        cy.get('[data-cy="skillTag-skill2-newtag1"]').should('not.exist')
        cy.get('[data-cy="skillTag-skill3-newtag1"]').should('exist')
    });

    it('tag skills with multiple tags', () => {

        cy.visit('/administrator/projects/proj1/subjects/subj1');

        // must exist initially
        cy.get('[data-cy="manageSkillLink_skill1"]');
        cy.get('[data-cy="manageSkillLink_skill2"]');
        cy.get('[data-cy="manageSkillLink_skill3"]');

        cy.get('[data-cy="skillSelect-skill1"]')
          .click({ force: true });
        cy.get('[data-cy="skillSelect-skill3"]')
          .click({ force: true });
        cy.get('[data-cy="skillActionsBtn"]')
          .click();
        cy.get('[data-cy="tagSkillBtn"]')
          .click();

        cy.get('[data-cy="newTagInput"]').type('New Tag 1')
        cy.get('[data-cy="addTagsButton"]').click()

        cy.get('[data-cy="skillTag-skill1-newtag1"]').should('exist')
        cy.get('[data-cy="skillTag-skill2-newtag1"]').should('not.exist')
        cy.get('[data-cy="skillTag-skill3-newtag1"]').should('exist')

        cy.get('[data-cy="selectAllSkillsBtn"]')
          .click();
        cy.get('[data-cy="skillActionsBtn"]')
          .click();
        cy.get('[data-cy="tagSkillBtn"]')
          .click();
        cy.get('[data-cy="newTagInput"]').type('New Tag 2')
        cy.get('[data-cy="addTagsButton"]').click()

        cy.get('[data-cy="skillTag-skill1-newtag1"]').should('exist')
        cy.get('[data-cy="skillTag-skill2-newtag1"]').should('not.exist')
        cy.get('[data-cy="skillTag-skill3-newtag1"]').should('exist')
        cy.get('[data-cy="skillTag-skill1-newtag2"]').should('exist')
        cy.get('[data-cy="skillTag-skill2-newtag2"]').should('exist')
        cy.get('[data-cy="skillTag-skill3-newtag2"]').should('exist')
    });
});
