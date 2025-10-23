from playwright.sync_api import sync_playwright

def run(playwright):
    browser = playwright.chromium.launch(headless=True)
    context = browser.new_context()
    page = context.new_page()

    # Login
    page.goto("http://localhost:5173/login")
    page.get_by_label("Título de eleitor").fill("111111111111")
    page.get_by_label("Senha").fill("123")
    page.get_by_role("button", name="Entrar").click()

    # Navigate to CDU-15
    page.goto("http://localhost:5173/processo/1/unidade/100/mapa")

    # Add a new competency
    page.get_by_test_id("btn-abrir-criar-competencia").click()
    page.get_by_test_id("input-nova-competencia").fill("Nova Competência de Teste")
    page.locator('[data-testid="atividade-nao-associada"] label').first().click()
    page.get_by_test_id("btn-criar-competencia").click()

    # Take a screenshot
    page.screenshot(path="jules-scratch/verification/verification.png")

    browser.close()

with sync_playwright() as playwright:
    run(playwright)
