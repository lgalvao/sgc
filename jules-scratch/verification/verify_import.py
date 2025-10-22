from playwright.sync_api import sync_playwright, TimeoutError

def run(playwright):
    browser = playwright.chromium.launch(headless=True)
    page = browser.new_page()

    # Listen for console events and print them
    page.on("console", lambda msg: print(f"Browser console: {msg.text}"))

    try:
        # Navigate to the login page and log in
        page.goto("http://localhost:5173/login")
        page.wait_for_selector('input[aria-label="Título de Eleitor"]')
        page.get_by_label("Título de Eleitor").fill("456")
        page.get_by_label("Senha").fill("456")
        page.get_by_role("button", name="Entrar").click()
        page.wait_for_selector('select[data-testid="select-perfil-unidade"]')
        page.get_by_role("button", name="Entrar").click()
        page.wait_for_url("http://localhost:5173/painel")

        # Navigate to the activities page
        page.goto("http://localhost:5173/processos/1/unidades/1/atividades")
        page.wait_for_selector('button[data-testid="btn-importar"]')


        # Test failed import
        page.get_by_role("button", name="Importar atividades").click()
        page.wait_for_selector('button[data-testid="btn-importar"]')
        page.get_by_role("button", name="Importar").click()
        page.screenshot(path="jules-scratch/verification/failed_import.png")

        # Test successful import
        page.get_by_label("Processo").select_option("1")
        page.get_by_label("Unidade").select_option("2")
        page.get_by_label("Atividade 1").check()
        page.get_by_role("button", name="Importar").click()
        page.screenshot(path="jules-scratch/verification/successful_import.png")

    except TimeoutError:
        print("Timeout error: The page took too long to load or an element was not found.")
        page.screenshot(path="jules-scratch/verification/error.png")
        print(page.content())

    finally:
        browser.close()

with sync_playwright() as playwright:
    run(playwright)
