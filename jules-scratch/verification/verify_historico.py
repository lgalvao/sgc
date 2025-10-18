import re
from playwright.sync_api import sync_playwright, expect

def run(playwright):
    browser = playwright.chromium.launch(headless=True)
    context = browser.new_context()
    page = context.new_page()

    # Go to login page
    page.goto("http://localhost:5173/login")

    # Fill in login credentials
    page.get_by_label("Título Eleitoral").fill("123456789012")
    page.get_by_label("Senha").fill("senha")

    # Click login button
    page.get_by_role("button", name="Entrar").click()

    page.wait_for_timeout(2000) # wait for 2 seconds
    page.screenshot(path="jules-scratch/verification/debug_after_login.png")

    # Wait for navigation to the panel
    expect(page.get_by_role("heading", name="Painel de Controle")).to_be_visible()

    # Select profile
    page.get_by_role("button", name=re.compile(r"Selecionar Perfil")).click()
    page.get_by_role("link", name=re.compile(r"ADMIN - Administrador")).click()

    # Navigate to Historico
    page.get_by_role("link", name="Histórico").click()

    # Wait for the table to be visible
    expect(page.get_by_role("table")).to_be_visible()

    # Take screenshot
    page.screenshot(path="jules-scratch/verification/verification.png")

    browser.close()

with sync_playwright() as playwright:
    run(playwright)