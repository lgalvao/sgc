from playwright.sync_api import sync_playwright

def run(playwright):
    browser = playwright.chromium.launch(headless=True)
    context = browser.new_context()
    page = context.new_page()

    # Go to the login page
    page.goto("http://localhost:5173/login")

    # Click the login button without filling in the form
    page.get_by_test_id("botao-entrar").click()

    # Take a screenshot of the login page with the inline error message
    page.screenshot(path="jules-scratch/verification/login-error.png")

    browser.close()

with sync_playwright() as playwright:
    try:
        run(playwright)
    except Exception as e:
        print(e)