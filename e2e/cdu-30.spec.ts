import {expect, test} from './fixtures/complete-fixtures.js';
import {TEXTOS} from '../frontend/src/constants/textos.js';

/**
 * CDU-30 - Manter administradores
 *
 * Ator: ADMIN
 *
 * Pré-condições:
 * - Usuário logado como ADMIN
 */
test.describe.serial('CDU-30 - Manter administradores', () => {

    // Usuário não-admin existente no seed (David Bowie - CHEFE da Assessoria 11)
    const TITULO_NOVO_ADMIN = '555555';
    const NOME_NOVO_ADMIN = 'David Bowie';

    test('Cenário 1: ADMIN navega para página de administradores e visualiza lista', async ({_resetAutomatico, page, _autenticadoComoAdmin}) => {
        await page.getByTestId('btn-administradores').click();
        await expect(page).toHaveURL(/\/administradores/);

        await expect(page.getByRole('heading', {name: TEXTOS.administracao.TITULO, exact: true})).toBeVisible();

        const tabela = page.locator('main table');
        await expect(tabela).toBeVisible();

        // Deve exibir pelo menos o próprio admin logado
        await expect(tabela.locator('tbody tr')).not.toHaveCount(0);

        // Botão de adicionar deve estar visível e habilitado
        await expect(page.getByRole('button', {name: TEXTOS.administracao.BOTAO_ADICIONAR})).toBeVisible();
        await expect(page.getByRole('button', {name: TEXTOS.administracao.BOTAO_ADICIONAR})).toBeEnabled();
    });

    test('Cenário 2: ADMIN adiciona novo administrador', async ({_resetAutomatico, page, _autenticadoComoAdmin}) => {
        await page.getByTestId('btn-administradores').click();
        await expect(page).toHaveURL(/\/administradores/);

        await page.getByRole('button', {name: TEXTOS.administracao.BOTAO_ADICIONAR}).click();

        const modal = page.getByRole('dialog');
        await expect(modal).toBeVisible();
        await expect(modal.getByRole('heading', {name: TEXTOS.administracao.MODAL_ADICIONAR_TITULO})).toBeVisible();
        await expect(modal.getByPlaceholder(TEXTOS.administracao.PLACEHOLDER_TITULO)).toBeVisible();

        await modal.getByPlaceholder(TEXTOS.administracao.PLACEHOLDER_TITULO).fill(TITULO_NOVO_ADMIN);

        const responsePromise = page.waitForResponse(resp => resp.url().includes('/api/') && resp.request().method() === 'POST');
        await modal.getByRole('button', {name: TEXTOS.comum.BOTAO_CRIAR}).click();
        await responsePromise;

        await expect(modal).toBeHidden();
        const tabela = page.locator('main table');
        await expect(tabela.getByText(NOME_NOVO_ADMIN)).toBeVisible();
    });

    test('Cenário 3: ADMIN remove administrador adicionado', async ({_resetAutomatico, page, _autenticadoComoAdmin}) => {
        await page.getByTestId('btn-administradores').click();
        await expect(page).toHaveURL(/\/administradores/);

        const tabela = page.locator('main table');
        const linhaNovoAdmin = tabela.locator('tr', {hasText: NOME_NOVO_ADMIN});
        await expect(linhaNovoAdmin).toBeVisible();

        await linhaNovoAdmin.getByRole('button').click();

        const modal = page.getByRole('dialog');
        await expect(modal).toBeVisible();
        await expect(modal.getByRole('heading', {name: TEXTOS.administracao.MODAL_REMOVER_TITULO})).toBeVisible();
        await expect(modal.getByText(TEXTOS.administracao.MODAL_REMOVER_PERGUNTA(NOME_NOVO_ADMIN))).toBeVisible();

        const responsePromise = page.waitForResponse(resp => resp.url().includes('/api/') && resp.request().method() === 'POST');
        await modal.getByRole('button', {name: /Remover/i}).click();
        await responsePromise;

        await expect(modal).toBeHidden();
        await expect(tabela.getByText(NOME_NOVO_ADMIN)).toBeHidden();
    });
});
