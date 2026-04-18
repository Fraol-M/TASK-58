import { test, expect } from '@playwright/test'

const UNIQUE_ID = Date.now()
const TEST_USER = `e2e_user_${UNIQUE_ID}`
const TEST_PASSWORD = 'E2ePassword123!'

test.describe('Authentication flow', () => {
  test('sign-up → sign-in → dashboard → sign-out', async ({ page }) => {
    // 1. Navigate to sign-up page
    await page.goto('/sign-up')
    await expect(page).toHaveTitle(/CampusFit/)

    // 2. Fill in sign-up form
    await page.getByPlaceholder(/choose a username/i).fill(TEST_USER)
    await page.getByPlaceholder(/create a strong password/i).fill(TEST_PASSWORD)
    await page.getByPlaceholder(/confirm your password/i).fill(TEST_PASSWORD)
    await page.getByRole('button', { name: /create account/i }).click()

    // 3. After sign-up, redirected to sign-in or dashboard
    await page.waitForURL(/\/(sign-in|dashboard)/)

    // 4. If redirected to sign-in, fill it in
    if (page.url().includes('sign-in')) {
      await page.getByPlaceholder(/enter your username/i).fill(TEST_USER)
      await page.getByPlaceholder(/enter your password/i).fill(TEST_PASSWORD)
      await page.getByRole('button', { name: /sign in/i }).click()
    }

    // 5. Verify dashboard renders
    await page.waitForURL(/\/dashboard/)
    await expect(page.locator('h1, [data-testid="page-title"]').first()).toBeVisible()

    // 6. Sign out
    const signOutBtn = page.getByRole('button', { name: /sign out/i })
      .or(page.getByText(/sign out/i))
    await signOutBtn.click()

    // 7. After sign-out, redirected to sign-in
    await page.waitForURL(/\/sign-in/)
    await expect(page.getByRole('button', { name: /sign in/i })).toBeVisible()
  })

  test('sign-in with wrong password shows error', async ({ page }) => {
    await page.goto('/sign-in')

    await page.getByPlaceholder(/enter your username/i).fill('nonexistent_user')
    await page.getByPlaceholder(/enter your password/i).fill('wrongpassword')
    await page.getByRole('button', { name: /sign in/i }).click()

    // Error message should appear
    await expect(page.locator('.sign-in-form__error-banner, [data-testid="error"], .error-message, .alert').first()).toBeVisible()
  })

  test('protected routes redirect unauthenticated users to sign-in', async ({ page }) => {
    await page.goto('/dashboard')

    // Should be redirected to sign-in
    await page.waitForURL(/\/sign-in/)
    await expect(page).toHaveURL(/sign-in/)
  })

  test('sign-in page is accessible without authentication', async ({ page }) => {
    await page.goto('/sign-in')

    await expect(page.getByRole('button', { name: /sign in/i })).toBeVisible()
    await expect(page.getByPlaceholder(/enter your username/i)).toBeVisible()
    await expect(page.getByPlaceholder(/enter your password/i)).toBeVisible()
  })
})
