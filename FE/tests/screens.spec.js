import { test, expect } from '@playwright/test';

test.describe('HackCoord Screens Automation', () => {
  test('should load the login page and authenticate', async ({ page }) => {
    await page.goto('/');
    
    // Check if the login page loads
    await expect(page).toHaveTitle(/HackCoord|Vite \+ React/i);
    
    // Login with dummy credentials
    await page.fill('input[type="email"]', 'test@test.com');
    await page.fill('input[type="password"]', 'password');
    await page.click('button:has-text("Sign In")');

    // Verify successful login by checking for dashboard elements
    await expect(page.locator('text=Welcome to HackCoord')).toBeVisible();
    await expect(page.locator('text=Active Tasks')).toBeVisible();
  });

  test('should navigate to Kanban screen', async ({ page }) => {
    await page.goto('/');
    // Login
    await page.fill('input[type="email"]', 'test@test.com');
    await page.fill('input[type="password"]', 'password');
    await page.click('button:has-text("Sign In")');

    // Click Kanban in the navigation
    await page.click('a[href="/kanban"], button:has-text("Kanban"), a:has-text("Kanban")');

    // Verify Kanban board columns are visible
    await expect(page.locator('text=TODO')).toBeVisible();
    await expect(page.locator('text=IN PROGRESS')).toBeVisible();
    await expect(page.locator('text=REVIEW')).toBeVisible();
  });

  test('should navigate to Mesh Share screen', async ({ page }) => {
    await page.goto('/');
    // Login
    await page.fill('input[type="email"]', 'test@test.com');
    await page.fill('input[type="password"]', 'password');
    await page.click('button:has-text("Sign In")');

    // Click Mesh Share in the navigation
    await page.click('a[href="/mesh"], button:has-text("Mesh Share"), a:has-text("Mesh Share")');

    // Verify Mesh Share page
    await expect(page.locator('text=Coming Soon')).toBeVisible();
  });
});
