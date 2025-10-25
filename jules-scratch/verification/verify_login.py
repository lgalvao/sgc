
from playwright.sync_api import sync_playwright, Page, expect

def run(playwright):
    browser = playwright.chromium.launch(headless=True)
    context = browser.new_context()
    page = context.new_page()

    # Test login with a single profile user
    page.goto("http://localhost:5173/login")
    page.get_by_test_id("input-titulo").fill("2")
    page.get_by_test_id("input-senha").fill("123")
    page.get_by_test_id("botao-entrar").click()
    expect(page).to_have_url("http://localhost:5173/painel")
    page.screenshot(path="jules-scratch/verification/single_profile_login.png")

    # Test login with a multi-profile user
    page.goto("http://localhost:5173/login")
    page.get_by_test_id("input-titulo").fill("999999999999")
    page.get_by_test_id("input-senha").fill("123")
    page.get_by_test_id("botao-entrar").click()
    expect(page.get_by_text("Selecione o perfil e a unidade")).to_be_visible()
    seletor = page.get_by_test_id("select-perfil-unidade")
    expect(seletor).to_be_visible()
    seletor.select_option(label="ADMIN - STIC")
    page.get_by_test_id("botao-entrar").click()
    expect(page).to_have_url("http://localhost:5173/painel")
    page.screenshot(path="jules-scratch/verification/multi_profile_login.png")

    context.close()
    browser.close()

with sync_playwright() as playwright:
    run(playwright)
