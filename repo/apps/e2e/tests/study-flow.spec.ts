import { test, expect } from '@playwright/test'

const UNIQUE_ID = Date.now()
const TEST_USER = `study_e2e_${UNIQUE_ID}`
const TEST_PASSWORD = 'StudyE2e123!'

test.describe('Study flow', () => {
  test.beforeEach(async ({ page }) => {
    // Sign up and sign in
    await page.goto('/auth/sign-up')
    await page.getByLabel(/username/i).fill(TEST_USER)
    await page.getByLabel(/password/i).first().fill(TEST_PASSWORD)
    await page.getByRole('button', { name: /sign up/i }).click()
    await page.waitForURL(/\/(auth\/sign-in|dashboard)/)

    if (page.url().includes('sign-in')) {
      await page.getByLabel(/username/i).fill(TEST_USER)
      await page.getByLabel(/password/i).fill(TEST_PASSWORD)
      await page.getByRole('button', { name: /sign in/i }).click()
    }
    await page.waitForURL(/\/dashboard/)
  })

  test('study plans page is accessible from dashboard', async ({ page }) => {
    // Navigate to study plans
    const studyLink = page.getByRole('link', { name: /study/i })
      .or(page.getByText(/study plans?/i))
    await studyLink.first().click()

    await expect(page).toHaveURL(/\/study/)
  })

  test('can navigate to study section', async ({ page }) => {
    await page.goto('/study')
    // Page should load (not redirect to sign-in, since we're authenticated)
    await expect(page).not.toHaveURL(/sign-in/)
  })

  test('dashboard loads and shows main navigation', async ({ page }) => {
    await expect(page.locator('nav, [role="navigation"]').first()).toBeVisible()
  })

  test('protected study routes accessible when authenticated', async ({ page }) => {
    await page.goto('/study')
    await expect(page).not.toHaveURL(/sign-in/)
  })
})
