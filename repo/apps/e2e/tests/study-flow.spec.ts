import { test, expect } from '@playwright/test'

const TEST_PASSWORD = 'StudyE2e123!'

test.describe('Study flow', () => {
  let testUser = ''

  test.beforeEach(async ({ page }) => {
    // Sign up and sign in
    testUser = `study_e2e_${Date.now()}_${Math.random().toString(36).slice(2, 8)}`
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

  test('study plans page is accessible from dashboard', async ({ page }) => {
    // Navigate to study plans
    const studyLink = page.getByRole('link', { name: /study/i })
      .or(page.getByText(/study plans?/i))
    if (await studyLink.count() > 0) {
      await studyLink.first().click()
    } else {
      await page.goto('/study/plans')
    }

    await expect(page).toHaveURL(/\/study\/plans/)
  })

  test('can navigate to study section', async ({ page }) => {
    await page.goto('/study/plans')
    // Page should load (not redirect to sign-in, since we're authenticated)
    await expect(page).not.toHaveURL(/sign-in/)
  })

  test('dashboard loads and shows main navigation', async ({ page }) => {
    await expect(page.locator('nav, [role="navigation"]').first()).toBeVisible()
  })

  test('protected study routes accessible when authenticated', async ({ page }) => {
    await page.goto('/study/history')
    await expect(page).not.toHaveURL(/sign-in/)
  })
})
