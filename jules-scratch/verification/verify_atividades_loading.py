import re
from playwright.sync_api import sync_playwright, Page, expect

def run(playwright):
    browser = playwright.chromium.launch(headless=True)
    context = browser.new_context()
    page = context.new_page()

    # 1. Login
    page.goto("http://localhost:5173/")
    page.get_by_label("Usuário").select_option("1")
    page.get_by_label("Unidade").select_option("1")
    page.get_by_role("button", name="Entrar").click()
    expect(page).to_have_url("http://localhost:5173/painel")

    # 2. Navigate to a process
    page.get_by_role("link", name="PR-TESTE-1").click()
    expect(page).to_have_url(re.compile(r".*/processos/1/detalhes"))

    # 3. Navigate to the activities page (CadAtividades)
    page.get_by_role("link", name="Atividades").first.click()
    expect(page).to_have_url(re.compile(r".*/processos/1/unidade/INMETRO/atividades"))

    # 4. Verify that activities are loaded
    # The new store should be loading data from the API.
    # We'll wait for the first activity description to be visible.
    expect(page.get_by_text("Atividades e conhecimentos")).to_be_visible()

    # Check for a specific activity that comes from the backend
    expect(page.get_by_text("Elaborar minutas de atos normativos")).to_be_visible()
    expect(page.get_by_text("Realizar auditoria interna")).to_be_visible()

    # 5. Screenshot
    page.screenshot(path="jules-scratch/verification/verification.png")

    browser.close()

with sync_playwright() as playwright:
    run(playwright)