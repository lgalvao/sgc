import sys
from playwright.sync_api import sync_playwright, Page, expect

def login(page: Page, base_url: str):
    """Logs in as a chefe user."""
    page.goto(f"{base_url}/login")
    page.get_by_label("Título eleitoral").fill("2")
    page.get_by_label("Senha").fill("123456")
    page.get_by_role("button", name="Entrar").click()

    # Handle profile selection if it appears
    profile_selector = page.get_by_test_id('select-perfil-unidade')
    if profile_selector.is_visible():
        profile_selector.select_option(label='CHEFE - SGP')
        page.get_by_role("button", name="Entrar").click()

    expect(page).to_have_url(f"{base_url}/painel")

def create_process(page: Page, name: str, base_url: str):
    """Creates a new process."""
    page.goto(f"{base_url}/processo/cadastro")
    page.get_by_label("Descrição").fill(name)
    page.get_by_label("Tipo").select_option("MAPEAMENTO")
    page.get_by_label("Data limite").fill("2025-12-31")
    page.locator('input[type="checkbox"]').first().check()
    page.get_by_role("button", name="Salvar").click()
    expect(page.get_by_text(name)).to_be_visible()

def verify_process_creation(page: Page, base_url: str):
    """
    This test verifies that a user can create a new process and view its details.
    """
    process_name = "Meu Processo de Teste Visual"

    # 1. Arrange: Log in and create a new process.
    login(page, base_url)
    create_process(page, process_name, base_url)

    # 2. Act: Navigate to the details page of the new process.
    page.get_by_text(process_name).click()

    # 3. Assert: Confirm the navigation was successful.
    expect(page.get_by_test_id("processo-info")).to_be_visible()
    expect(page.get_by_text(process_name)).to_be_visible()

    # 4. Screenshot: Capture the final result for visual verification.
    page.screenshot(path="jules-scratch/verification/process-details.png")

def main():
    base_url = "http://localhost:5173"
    if len(sys.argv) > 1:
        base_url = sys.argv[1]

    with sync_playwright() as p:
        browser = p.chromium.launch()
        page = browser.new_page()
        verify_process_creation(page, base_url)
        browser.close()

if __name__ == "__main__":
    main()