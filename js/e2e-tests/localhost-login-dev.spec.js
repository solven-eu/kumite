import { test, expect, request } from "@playwright/test";

import fakePlayer from "./fake-player.mjs";

const url = "http://localhost:8080";

//test.beforeAll(async ({ request }) => {
//    // Create a new repository
//    const response = await fakePlayer.clear(request, url);
//    expect(response.ok()).toBeTruthy();
//});

// We just check the login page is working OK
test("showLoginOptions", async ({ page }) => {
    await page.goto(url);
    await fakePlayer.showLoginOptions(page);
});

test("login", async ({ page }) => {
    await page.goto(url);
    await fakePlayer.login(page);
});

// We can not login automatically without fakeUser
//test("play-optimization", async ({ page }) => {
//    await page.goto(url);
//    await fakePlayer.playOptimization(page);
//});
