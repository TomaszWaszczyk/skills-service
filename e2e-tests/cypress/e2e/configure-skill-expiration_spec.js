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

const moment = require("moment-timezone");
describe('Configure Skill Expiration Tests', () => {

    beforeEach(() => {
        cy.intercept('GET', '/admin/projects/proj1/skills/skill1/expiration').as('getExpirationProps')
        cy.intercept('GET', '/admin/projects/proj1/subjects/subj1/skills/skill1').as('getSkillInfo')
        Cypress.Commands.add("visitExpirationConfPage", (projNum) => {
            cy.visit('/administrator/projects/proj1/subjects/subj1/skills/skill1/configExpiration');
            cy.wait('@getExpirationProps')
            cy.wait('@getSkillInfo')
            cy.get('.spinner-border').should('not.exist')
        });
        cy.intercept('POST', '/admin/projects/proj1/skills/skill1/expiration').as('saveExpirationSettings')
        cy.intercept('DELETE', '/admin/projects/proj1/skills/skill1/expiration').as('deleteExpirationSettings')
    });

    it('expiration type of NONE is selected by default', () => {
        cy.createProject(1)
        cy.createSubject(1, 1);
        cy.createSkill(1, 1, 1)
        cy.visitExpirationConfPage();
        cy.get('[data-cy="saveSettingsBtn"]').should('be.disabled')

        cy.get('[data-cy="expirationTypeSelector"] [data-cy="expirationNeverRadio"]').should('be.checked')
        cy.get('[data-cy="yearlyFormGroup"]').should('be.disabled')
        cy.get('[data-cy="monthlyFormGroup"]').should('be.disabled')
    });

    it('expiration type of YEARLY defaults to current date', () => {
        cy.createProject(1)
        cy.createSubject(1, 1);
        cy.createSkill(1, 1, 1)
        cy.visitExpirationConfPage();
        cy.get('[data-cy="saveSettingsBtn"]').should('be.disabled')

        const today = moment.utc();

        cy.get('[data-cy="expirationTypeSelector"] [data-cy="yearlyRadio"]').check({ force: true });

        cy.get('[data-cy="unsavedChangesAlert"]').contains('Unsaved Changes');
        cy.get('[data-cy="settingsSavedAlert"]').should('not.exist');
        cy.get('[data-cy="saveSettingsBtn"]').should('be.enabled');

        cy.get('[data-cy="expirationTypeSelector"] [data-cy="yearlyRadio"]').should('be.checked')
        cy.get('[data-cy="yearlyYears-sb"]').contains('1')
        cy.get('[data-cy="yearlyMonth"]').contains(today.format('MMMM'))
        cy.get('[data-cy="yearlyDayOfMonth"]').contains(today.day())

        cy.get('[data-cy="saveSettingsBtn"]').click()
        cy.wait('@saveExpirationSettings').then((xhr) => {
            expect(xhr.response.statusCode).to.eq(200)
            const requestBody = xhr.request.body
            expect(requestBody.every).to.eq(1)
            expect(requestBody.expirationType).to.eq('YEARLY')
            expect(requestBody.monthlyDay).to.eq(null)
            expect(requestBody.nextExpirationDate).to.eq(today.startOf('day').toISOString())
        })

        cy.get('[data-cy="settingsSavedAlert"]').contains('Settings Updated');
        cy.get('[data-cy="unsavedChangesAlert"]').should('not.exist');
        cy.get('[data-cy="saveSettingsBtn"]').should('be.disabled');
    });

    it('expiration type of MONTHLY defaults to first day of next month', () => {
        cy.createProject(1)
        cy.createSubject(1, 1);
        cy.createSkill(1, 1, 1)
        cy.visitExpirationConfPage();
        cy.get('[data-cy="saveSettingsBtn"]').should('be.disabled')

        const today = moment.utc();
        const startOfNextMonth = today.add(1, 'M').startOf('month');

        cy.get('[data-cy="expirationTypeSelector"] [data-cy="monthlyRadio"]').check({ force: true });

        cy.get('[data-cy="unsavedChangesAlert"]').contains('Unsaved Changes');
        cy.get('[data-cy="settingsSavedAlert"]').should('not.exist');
        cy.get('[data-cy="saveSettingsBtn"]').should('be.enabled');

        cy.get('[data-cy="expirationTypeSelector"] [data-cy="monthlyRadio"]').should('be.checked')
        cy.get('[data-cy="monthlyMonths-sb"]').contains('1')

        cy.get('[data-cy="monthlyDayOption"] [value="FIRST_DAY_OF_MONTH"]').should('be.checked');
        cy.get('[data-cy="monthlyDay"]').should('be.disabled')

        cy.get('[data-cy="saveSettingsBtn"]').click()
        cy.wait('@saveExpirationSettings').then((xhr) => {
            expect(xhr.response.statusCode).to.eq(200)
            const requestBody = xhr.request.body
            expect(requestBody.every).to.eq(1)
            expect(requestBody.expirationType).to.eq('MONTHLY')
            expect(requestBody.monthlyDay).to.eq('FIRST_DAY_OF_MONTH')
            expect(requestBody.nextExpirationDate).to.eq(startOfNextMonth.toISOString())
        })

        cy.get('[data-cy="settingsSavedAlert"]').contains('Settings Updated');
        cy.get('[data-cy="unsavedChangesAlert"]').should('not.exist');
        cy.get('[data-cy="saveSettingsBtn"]').should('be.disabled');
    });

    it('expiration type of MONTHLY, select last day of month', () => {
        cy.createProject(1)
        cy.createSubject(1, 1);
        cy.createSkill(1, 1, 1)
        cy.visitExpirationConfPage();
        cy.get('[data-cy="saveSettingsBtn"]').should('be.disabled')

        const today = moment.utc();
        const endOfThisMonth = today.endOf('month');

        cy.get('[data-cy="expirationTypeSelector"] [data-cy="monthlyRadio"]').check({ force: true });

        cy.get('[data-cy="unsavedChangesAlert"]').contains('Unsaved Changes');
        cy.get('[data-cy="settingsSavedAlert"]').should('not.exist');
        cy.get('[data-cy="saveSettingsBtn"]').should('be.enabled');

        cy.get('[data-cy="expirationTypeSelector"] [data-cy="monthlyRadio"]').should('be.checked')
        cy.get('[data-cy="monthlyMonths-sb"]').contains('1')

        cy.get('[data-cy="monthlyDayOption"] [value="LAST_DAY_OF_MONTH"]').check({ force: true });
        cy.get('[data-cy="monthlyDay"]').should('be.disabled')

        cy.get('[data-cy="saveSettingsBtn"]').click()
        cy.wait('@saveExpirationSettings').then((xhr) => {
            expect(xhr.response.statusCode).to.eq(200)
            const requestBody = xhr.request.body
            expect(requestBody.every).to.eq(1)
            expect(requestBody.expirationType).to.eq('MONTHLY')
            expect(requestBody.monthlyDay).to.eq('LAST_DAY_OF_MONTH')
            expect(requestBody.nextExpirationDate).to.eq(endOfThisMonth.toISOString())
        })

        cy.get('[data-cy="settingsSavedAlert"]').contains('Settings Updated');
        cy.get('[data-cy="unsavedChangesAlert"]').should('not.exist');
        cy.get('[data-cy="saveSettingsBtn"]').should('be.disabled');
    });

    it('expiration type of MONTHLY, select specific day of month', () => {
        cy.createProject(1)
        cy.createSubject(1, 1);
        cy.createSkill(1, 1, 1)
        cy.visitExpirationConfPage();
        cy.get('[data-cy="saveSettingsBtn"]').should('be.disabled')

        const today = moment.utc();
        const theFifteenth = moment.utc().date(15).startOf('day');
        if (today.date() > 15) {
            theFifteenth.add(1, 'M')
        }

        cy.get('[data-cy="expirationTypeSelector"] [data-cy="monthlyRadio"]').check({ force: true });

        cy.get('[data-cy="unsavedChangesAlert"]').contains('Unsaved Changes');
        cy.get('[data-cy="settingsSavedAlert"]').should('not.exist');
        cy.get('[data-cy="saveSettingsBtn"]').should('be.enabled');

        cy.get('[data-cy="expirationTypeSelector"] [data-cy="monthlyRadio"]').should('be.checked')
        cy.get('[data-cy="monthlyMonths-sb"]').contains('1')

        cy.get('[data-cy="monthlyDayOption"] [value="SET_DAY_OF_MONTH"]').check({ force: true });
        cy.get('[data-cy="monthlyDay"]').should('be.enabled')
        cy.get('[data-cy="monthlyDay"]').select('15')

        cy.get('[data-cy="saveSettingsBtn"]').click()
        cy.wait('@saveExpirationSettings').then((xhr) => {
            expect(xhr.response.statusCode).to.eq(200)
            const requestBody = xhr.request.body
            expect(requestBody.every).to.eq(1)
            expect(requestBody.expirationType).to.eq('MONTHLY')
            expect(requestBody.monthlyDay).to.eq(15)
            expect(requestBody.nextExpirationDate).to.eq(theFifteenth.toISOString())
        })

        cy.get('[data-cy="settingsSavedAlert"]').contains('Settings Updated');
        cy.get('[data-cy="unsavedChangesAlert"]').should('not.exist');
        cy.get('[data-cy="saveSettingsBtn"]').should('be.disabled');
    });

    it('expiration type of DAILY defaults to 90 days after achievement', () => {
        cy.createProject(1)
        cy.createSubject(1, 1);
        cy.createSkill(1, 1, 1)
        cy.visitExpirationConfPage();
        cy.get('[data-cy="saveSettingsBtn"]').should('be.disabled')

        cy.get('[data-cy="expirationTypeSelector"] [data-cy="dailyRadio"]').check({ force: true });

        cy.get('[data-cy="unsavedChangesAlert"]').contains('Unsaved Changes');
        cy.get('[data-cy="settingsSavedAlert"]').should('not.exist');
        cy.get('[data-cy="saveSettingsBtn"]').should('be.enabled');

        cy.get('[data-cy="expirationTypeSelector"] [data-cy="dailyRadio"]').should('be.checked')
        cy.get('[data-cy="dailyDays-sb"]').contains('90')

        cy.get('[data-cy="saveSettingsBtn"]').click()
        cy.wait('@saveExpirationSettings').then((xhr) => {
            expect(xhr.response.statusCode).to.eq(200)
            const requestBody = xhr.request.body
            expect(requestBody.every).to.eq(90)
            expect(requestBody.expirationType).to.eq('DAILY')
        })

        cy.get('[data-cy="settingsSavedAlert"]').contains('Settings Updated');
        cy.get('[data-cy="unsavedChangesAlert"]').should('not.exist');
        cy.get('[data-cy="saveSettingsBtn"]').should('be.disabled');
    });

    it('changing expiration type back to NEVER after configuring to something else calls DELETE', () => {
        cy.createProject(1)
        cy.createSubject(1, 1);
        cy.createSkill(1, 1, 1)
        cy.visitExpirationConfPage();
        cy.get('[data-cy="saveSettingsBtn"]').should('be.disabled')

        const today = moment.utc();

        cy.get('[data-cy="expirationTypeSelector"] [data-cy="yearlyRadio"]').check({ force: true });

        cy.get('[data-cy="unsavedChangesAlert"]').contains('Unsaved Changes');
        cy.get('[data-cy="settingsSavedAlert"]').should('not.exist');
        cy.get('[data-cy="saveSettingsBtn"]').should('be.enabled');

        cy.get('[data-cy="expirationTypeSelector"] [data-cy="yearlyRadio"]').should('be.checked')
        cy.get('[data-cy="yearlyYears-sb"]').contains('1')
        cy.get('[data-cy="yearlyMonth"]').contains(today.format('MMMM'))
        cy.get('[data-cy="yearlyDayOfMonth"]').contains(today.day())

        cy.get('[data-cy="saveSettingsBtn"]').click()
        cy.wait('@saveExpirationSettings').then((xhr) => {
            expect(xhr.response.statusCode).to.eq(200)
            const requestBody = xhr.request.body
            expect(requestBody.every).to.eq(1)
            expect(requestBody.expirationType).to.eq('YEARLY')
            expect(requestBody.monthlyDay).to.eq(null)
            expect(requestBody.nextExpirationDate).to.eq(today.startOf('day').toISOString())
        })

        cy.get('[data-cy="settingsSavedAlert"]').contains('Settings Updated');
        cy.get('[data-cy="unsavedChangesAlert"]').should('not.exist');
        cy.get('[data-cy="saveSettingsBtn"]').should('be.disabled');

        cy.get('[data-cy="expirationTypeSelector"] [data-cy="expirationNeverRadio"]').check({ force: true });

        cy.get('[data-cy="unsavedChangesAlert"]').contains('Unsaved Changes');
        cy.get('[data-cy="settingsSavedAlert"]').should('not.exist');
        cy.get('[data-cy="saveSettingsBtn"]').should('be.enabled');

        cy.get('[data-cy="saveSettingsBtn"]').click()
        cy.wait('@deleteExpirationSettings').then((xhr) => {
            expect(xhr.request.method).to.eq('DELETE')
            expect(xhr.response.statusCode).to.eq(200)
        })
    });

});