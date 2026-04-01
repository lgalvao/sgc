import {expect, test} from './fixtures/complete-fixtures.js';
import {TEXTOS} from '../frontend/src/constants/textos.js';
import {USUARIOS} from './helpers/helpers-auth.js';

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

        await expect(tabela.locator('th', {hasText: TEXTOS.administracao.CAMPO_NOME})).toBeVisible();
        await expect(tabela.locator('th', {hasText: TEXTOS.administracao.CAMPO_TITULO})).toBeVisible();
        await expect(tabela.locator('th', {hasText: TEXTOS.administracao.CAMPO_MATRICULA})).toBeVisible();
        await expect(tabela.locator('th', {hasText: TEXTOS.administracao.CAMPO_UNIDADE})).toBeVisible();
        await expect(tabela.locator('th', {hasText: TEXTOS.administracao.CAMPO_ACOES})).toBeVisible();

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

    test('Cenário 4: ADMIN tenta adicionar sem título e sistema valida campo obrigatório', async ({
                                                                                                      _resetAutomatico,
                                                                                                      page,
                                                                                                      _autenticadoComoAdmin
}) => {
        await page.getByTestId('btn-administradores').click();
        await expect(page).toHaveURL(/\/administradores/);

        await page.getByRole('button', {name: TEXTOS.administracao.BOTAO_ADICIONAR}).click();
        const modal = page.getByRole('dialog');
        await expect(modal).toBeVisible();

        await expect(modal.getByRole('button', {name: /Cancelar/i})).toBeVisible();
        const botaoConfirmar = modal.getByRole('button', {name: /Adicionar|Criar/i});
        await expect(botaoConfirmar).toBeVisible();
        await modal.getByPlaceholder(TEXTOS.administracao.PLACEHOLDER_TITULO).fill('   ');
        await botaoConfirmar.click();
        await expect(page).toHaveURL(/\/administradores/);
        await expect(modal).toBeVisible();
        await expect(page.locator('main table tbody tr')).not.toHaveCount(0);
    });

    test('Cenário 5: ADMIN tenta adicionar usuário que já é administrador e recebe erro de validação', async ({
        _resetAutomatico,
        page,
        _autenticadoComoAdmin
    }) => {
        await page.getByTestId('btn-administradores').click();
        await expect(page).toHaveURL(/\/administradores/);

        await page.getByRole('button', {name: TEXTOS.administracao.BOTAO_ADICIONAR}).click();
        const modal = page.getByRole('dialog');
        await expect(modal).toBeVisible();

        await modal.getByPlaceholder(TEXTOS.administracao.PLACEHOLDER_TITULO).fill('111111');
        const respostaErro = page.waitForResponse(resp =>
            resp.url().includes('/usuarios/administradores') &&
            resp.request().method() === 'POST' &&
            resp.status() >= 400
        );
        await modal.getByRole('button', {name: /Adicionar|Criar/i}).click();
        const resposta = await respostaErro;
        const corpo = await resposta.json();

        await expect(modal).toBeVisible();
        expect(corpo.message).toContain('Usuário já é um administrador do sistema');
    });

    test('Cenário 6: ADMIN não pode remover a si mesmo como administrador', async ({
                                                                                       _resetAutomatico,
                                                                                       page,
                                                                                       _autenticadoComoAdmin
    }) => {
        await page.getByTestId('btn-administradores').click();
        await expect(page).toHaveURL(/\/administradores/);

        const tabela = page.locator('main table');
        // Encontra a linha do próprio usuário logado (ADMIN_1_PERFIL - 191919)
        const linhaProprioAdmin = tabela.locator('tr').filter({hasText: USUARIOS.ADMIN_1_PERFIL.titulo});
        await expect(linhaProprioAdmin).toBeVisible();
        await linhaProprioAdmin.getByRole('button').click();

        const modal = page.getByRole('dialog');
        await expect(modal).toBeVisible();
        await expect(modal.getByRole('heading', {name: TEXTOS.administracao.MODAL_REMOVER_TITULO})).toBeVisible();

        const respostaErro = page.waitForResponse(resp =>
            resp.url().includes('/usuarios/administradores') &&
            resp.request().method() === 'POST' &&
            resp.status() >= 400
        );
        await modal.getByRole('button', {name: /Remover/i}).click();
        const resposta = await respostaErro;
        const corpo = await resposta.json();

        await expect(modal).toBeVisible();
        expect(corpo.message).toContain('Não é permitido remover a si mesmo como administrador');
        await modal.getByRole('button', {name: /Cancelar/i}).click();
        await expect(modal).toBeHidden();
    });

    test('Cenário 7: ADMIN não pode remover o único administrador do sistema', async ({
                                                                                          _resetAutomatico,
                                                                                          page,
                                                                                          _autenticadoComoAdmin
    }) => {
        await page.getByTestId('btn-administradores').click();
        await expect(page).toHaveURL(/\/administradores/);

        const tabela = page.locator('main table');

        // Remove o outro administrador (111111) para deixar apenas o logado (191919)
        const linhaOutroAdmin = tabela.locator('tr').filter({hasText: '111111'});
        await expect(linhaOutroAdmin).toBeVisible();
        await linhaOutroAdmin.getByRole('button').click();

        const modal1 = page.getByRole('dialog');
        await expect(modal1).toBeVisible();
        const respostaRemocaoOk = page.waitForResponse(resp =>
            resp.url().includes('/usuarios/administradores') &&
            resp.request().method() === 'POST' &&
            resp.status() < 400
        );
        await modal1.getByRole('button', {name: /Remover/i}).click();
        await respostaRemocaoOk;
        await expect(modal1).toBeHidden();

        // Tenta remover o único administrador restante (191919)
        const linhaUnicoAdmin = tabela.locator('tr').filter({hasText: USUARIOS.ADMIN_1_PERFIL.titulo});
        await expect(linhaUnicoAdmin).toBeVisible();
        await linhaUnicoAdmin.getByRole('button').click();

        const modal2 = page.getByRole('dialog');
        await expect(modal2).toBeVisible();
        await expect(modal2.getByRole('heading', {name: TEXTOS.administracao.MODAL_REMOVER_TITULO})).toBeVisible();

        const respostaErro = page.waitForResponse(resp =>
            resp.url().includes('/usuarios/administradores') &&
            resp.request().method() === 'POST' &&
            resp.status() >= 400
        );
        await modal2.getByRole('button', {name: /Remover/i}).click();
        const resposta = await respostaErro;
        const corpo = await resposta.json();

        await expect(modal2).toBeVisible();
        expect(corpo.message).toMatch(/Não é permitido remover (o único administrador do sistema|a si mesmo como administrador)/);
    });
});
