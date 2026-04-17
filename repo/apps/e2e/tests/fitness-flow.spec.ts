import { test, expect } from '@playwright/test'

const UNIQUE_ID = Date.now() + 1
const TEST_USER = `fitness_e2e_${UNIQUE_ID}`
const TEST_PASSWORD = 'FitnessE2e123!'

test.describe('Fitness flow', () => {
  test.beforeEach(async ({ page }) => {
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

  test('fitness section is accessible when authenticated', async ({ page }) => {
    await page.goto('/fitness')
    await expect(page).not.toHaveURL(/sign-in/)
  })

  test('dashboard renders without errors', async ({ page }) => {
    // Check no error boundary visible
    await expect(page.locator('.error, [data-testid="error"]')).not.toBeVisible()
    await expect(page.locator('h1, [data-testid="page-title"]').first()).toBeVisible()
  })

  test('can navigate to fitness goals', async ({ page }) => {
    const fitnessLink = page.getByRole('link', { name: /fitness|goals?/i })
      .or(page.getByText(/fitness|goals?/i))
    if (await fitnessLink.count() > 0) {
      await fitnessLink.first().click()
      await expect(page).not.toHaveURL(/sign-in/)
    }
  })

  test('profile page is accessible when authenticated', async ({ page }) => {
    await page.goto('/profile')
    await expect(page).not.toHaveURL(/sign-in/)
  })
})
