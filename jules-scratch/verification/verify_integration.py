from playwright.sync_api import sync_playwright, expect
import re

def run(playwright):
    browser = playwright.chromium.launch(headless=True)
    context = browser.new_context()
    page = context.new_page()

    # 1. Login com usuário de perfil único
    page.goto("http://localhost:5173/")
    page.get_by_label('Título eleitoral').fill('2') # Carlos Henrique Lima
    page.get_by_label('Senha').fill('2')
    page.get_by_role('button', name='Entrar').click()

    # 2. Verificar se o login foi bem-sucedido e navegou para o painel
    expect(page).to_have_url(re.compile(r'.*\/painel'))
    expect(page.get_by_role('heading', name=re.compile('Processos de Mapeamento', re.IGNORECASE))).to_be_visible()

    # 3. Navegar para uma página que use os dados integrados
    page.goto("http://localhost:5173/processos/1/detalhes")

    # 4. Verificar se os dados foram carregados
    expect(page.get_by_role('heading', name=re.compile('Detalhes do Processo', re.IGNORECASE))).to_be_visible()
    expect(page.get_by_text('Unidades Participantes')).to_be_visible()

    # Tira um screenshot da página de detalhes do processo
    page.screenshot(path="jules-scratch/verification/verification.png")

    browser.close()

with sync_playwright() as playwright:
    run(playwright)