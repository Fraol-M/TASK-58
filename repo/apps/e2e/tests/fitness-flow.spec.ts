import { test, expect } from '@playwright/test'

const TEST_PASSWORD = 'FitnessE2e123!'

test.describe('Fitness flow', () => {
  let testUser = ''

  test.beforeEach(async ({ page }) => {
    testUser = `fitness_e2e_${Date.now()}_${Math.random().toString(36).slice(2, 8)}`
    await page.goto('/sign-up')
    await page.getByPlaceholder(/choose a username/i).fill(testUser)
    await page.getByPlaceholder(/create a strong password/i).fill(TEST_PASSWORD)
    await page.getByPlaceholder(/confirm your password/i).fill(TEST_PASSWORD)
    await page.getByRole('button', { name: /create account/i }).click()
    await page.waitForURL(/\/(sign-in|dashboard)/)

    if (page.url().includes('sign-in')) {
      await page.getByPlaceholder(/enter your username/i).fill(testUser)
      await page.getByPlaceholder(/enter your password/i).fill(TEST_PASSWORD)
      await page.getByRole('button', { name: /sign in/i }).click()
    }
    await page.waitForURL(/\/dashboard/)
  })

  test('fitness section is accessible when authenticated', async ({ page }) => {
    await page.goto('/fitness/goals')
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
    } else {
      await page.goto('/fitness/goals')
      await expect(page).not.toHaveURL(/sign-in/)
    }
  })

  test('profile page is accessible when authenticated', async ({ page }) => {
    await page.goto('/profile')
    await expect(page).not.toHaveURL(/sign-in/)
  })
})
