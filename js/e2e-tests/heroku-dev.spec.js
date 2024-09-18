import { test, expect } from '@playwright/test';

import fakePlayer from "./fake-player.mjs"

test('login-options', async ({ page }) => {
    await fakePlayer.fullScenarioFakePlayer(page, "https://kumite-dev-d2844865d26b.herokuapp.com/");
});