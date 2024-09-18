import { expect } from '@playwright/test';

// https://stackoverflow.com/questions/33178843/es6-export-default-with-multiple-functions-referring-to-each-other
export default {
    async fullScenarioFakePlayer(page, url) {
        await page.goto(url);
        await page.getByRole('link', {name: /You need to login/}).click();
    }
};
