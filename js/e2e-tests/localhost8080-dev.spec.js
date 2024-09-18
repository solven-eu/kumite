import { test, expect, request } from '@playwright/test';

import fakePlayer from "./fake-player.mjs"

const url = "http://localhost:8080";

test.beforeAll(async ({ request }) => {
  // Create a new repository
  const response = await fakePlayer.clear(request, url);
  expect(response.ok()).toBeTruthy();
});

// FakeUser skips the login phase
//test('login', async ({ page }) => {
//    await page.goto(url);
//    await fakePlayer.login(page);
//});

test('play-optimization', async ({ page }) => {
    await page.goto(url);
    await fakePlayer.playOptimization(page);
});