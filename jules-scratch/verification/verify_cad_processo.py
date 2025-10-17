from playwright.sync_api import Page, expect

def test_cad_processo(page: Page):
    """
    This test verifies that a user can navigate to the "Cadastrar Processo" page
    and that the form elements are visible.
    """
    # 1. Arrange: Go to the "Cadastrar Processo" page.
    page.goto("http://localhost:5173/#/processos/cadastrar")

    # 2. Assert: Confirm that the main elements of the form are visible.
    expect(page.get_by_role("heading", name="Cadastro de processo")).to_be_visible()
    expect(page.get_by_label("Descrição")).to_be_visible()
    expect(page.get_by_label("Tipo")).to_be_visible()
    expect(page.get_by_label("Unidades participantes")).to_be_visible()
    expect(page.get_by_label("Data limite")).to_be_visible()

    # 3. Act: Fill in some data.
    page.get_by_label("Descrição").fill("Teste de Processo")
    page.get_by_label("Data limite").fill("2025-12-31")

    # 4. Screenshot: Capture the final result for visual verification.
    page.screenshot(path="jules-scratch/verification/verification.png")