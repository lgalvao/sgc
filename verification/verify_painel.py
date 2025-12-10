from playwright.sync_api import sync_playwright

def verify_painel(page):
    print("Navigating to http://localhost:5173")
    page.goto("http://localhost:5173")

    # Login
    print("Logging in...")
    page.get_by_test_id("inp-login-usuario").fill("191919")
    page.get_by_test_id("inp-login-senha").fill("senha")
    page.get_by_test_id("btn-login-entrar").click()

    # Wait for Painel
    print("Waiting for /painel...")
    page.wait_for_url("**/painel")

    # Take screenshot
    print("Taking screenshot...")
    page.screenshot(path="verification/painel_before.png")

if __name__ == "__main__":
    with sync_playwright() as p:
        browser = p.chromium.launch()
        page = browser.new_page()
        try:
            verify_painel(page)
        except Exception as e:
            print(f"Error: {e}")
            page.screenshot(path="verification/error.png")
        finally:
            browser.close()
