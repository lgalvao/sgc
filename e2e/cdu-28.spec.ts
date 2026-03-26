import {expect, test} from './fixtures/complete-fixtures.js';
import {loginComPerfil} from './helpers/helpers-auth.js';
import {TEXTOS} from '../frontend/src/constants/textos.js';

test.describe.serial('CDU-28 - Manter atribuição temporária', () => {
    const SIGLA_UNIDADE = 'ASSESSORIA_11';
    const TITULO_USUARIO_ALVO = '232323';
    const NOME_USUARIO_ALVO = 'Bon Jovi';
    const PERFIL_TEMPORARIO = 'CHEFE - ASSESSORIA_11';
    const DESCRICAO_ALERTA = `Atribuição temporária de perfil de CHEFE na unidade ${SIGLA_UNIDADE}`;

    async function acessarUnidadeAlvo(page: import('@playwright/test').Page) {
        await expect(page.getByTestId('link-arvore-unidade-SECRETARIA_1')).toBeVisible();
        await expect(page.getByTestId('link-arvore-unidade-SECRETARIA_2')).toBeVisible();
        await page.getByTestId('btn-arvore-expand-SECRETARIA_1').click();
        await expect(page.getByTestId(`link-arvore-unidade-${SIGLA_UNIDADE}`)).toBeVisible();
        await page.getByTestId(`link-arvore-unidade-${SIGLA_UNIDADE}`).click();
    }

    async function abrirTelaCriacaoAtribuicao(page: import('@playwright/test').Page) {
        await acessarUnidadeAlvo(page);
        await expect(page).toHaveURL(/\/unidade\/\d+$/);
        await expect(page.getByRole('heading', {name: new RegExp(SIGLA_UNIDADE)})).toBeVisible();
        await expect(page.getByTestId('unidade-view__btn-criar-atribuicao')).toBeVisible();
        await page.getByTestId('unidade-view__btn-criar-atribuicao').click();
        await expect(page).toHaveURL(/\/unidade\/\d+\/atribuicao$/);
    }

    async function selecionarUsuarioAlvo(page: import('@playwright/test').Page) {
        await page.getByTestId('input-busca-usuario').fill(TITULO_USUARIO_ALVO);
        const opcaoUsuario = page.getByTestId(/opcao-usuario-/).filter({hasText: NOME_USUARIO_ALVO}).first();
        await expect(opcaoUsuario).toBeVisible();
        await opcaoUsuario.click();
    }

    test.beforeEach(async ({_resetAutomatico, _autenticadoComoAdmin, page}) => {
        await page.getByRole('link', {name: /Unidades/i}).click();
        await expect(page).toHaveURL(/\/unidades/);
        await expect(page.getByRole('heading', {name: TEXTOS.unidades.TITULO})).toBeVisible();
    });

    test('Cenario 1: ADMIN navega pela árvore e acessa detalhes da unidade', async ({_resetAutomatico, _autenticadoComoAdmin, page}) => {
        await acessarUnidadeAlvo(page);

        await expect(page).toHaveURL(/\/unidade\/\d+$/);
        await expect(page.getByRole('heading', {name: new RegExp(SIGLA_UNIDADE)})).toBeVisible();
        await expect(page.getByTestId('unidade-view__btn-criar-atribuicao')).toBeVisible();
    });

    test('Cenario 2: tela de atribuição expõe os campos e ações exigidos pelo requisito', async ({_resetAutomatico, _autenticadoComoAdmin, page}) => {
        await abrirTelaCriacaoAtribuicao(page);

        await expect(page.getByRole('heading', {name: TEXTOS.atribuicaoTemporaria.TITULO})).toBeVisible();
        await expect(page.locator('p.text-muted', {hasText: SIGLA_UNIDADE}).first()).toBeVisible();
        await expect(page.getByText(TEXTOS.atribuicaoTemporaria.AJUDA_PESQUISA_USUARIO)).toBeVisible();

        await expect(page.getByLabel(TEXTOS.atribuicaoTemporaria.LABEL_USUARIO)).toBeVisible();
        await expect(page.getByTestId('input-busca-usuario')).toHaveAttribute('placeholder', TEXTOS.atribuicaoTemporaria.SELECIONE_USUARIO);
        await expect(page.getByLabel(TEXTOS.atribuicaoTemporaria.LABEL_DATA_INICIO)).toBeVisible();
        await expect(page.getByLabel(TEXTOS.atribuicaoTemporaria.LABEL_DATA_TERMINO)).toBeVisible();
        await expect(page.getByLabel(TEXTOS.atribuicaoTemporaria.LABEL_JUSTIFICATIVA)).toBeVisible();

        await expect(page.getByTestId('btn-cancelar-atribuicao')).toBeVisible();
        await expect(page.getByTestId('cad-atribuicao__btn-criar-atribuicao')).toBeVisible();
    });

    test('Cenario 3: todos os campos são obrigatórios para criar a atribuição', async ({_resetAutomatico, _autenticadoComoAdmin, page}) => {
        await abrirTelaCriacaoAtribuicao(page);

        await page.getByTestId('cad-atribuicao__btn-criar-atribuicao').click();

        await expect(page.getByTestId('app-alert')).toContainText(TEXTOS.atribuicaoTemporaria.ERRO_PREENCHIMENTO);
        await expect(page.getByText(TEXTOS.atribuicaoTemporaria.ERRO_SELECIONE_USUARIO)).toBeVisible();
        await expect(page.getByText('Informe a data de início.')).toBeVisible();
        await expect(page.getByText('Informe a data de término.')).toBeVisible();
        await expect(page.getByText('Informe a justificativa.')).toBeVisible();
    });

    test('Cenario 4: ADMIN cancela criação e retorna para detalhes da unidade', async ({_resetAutomatico, _autenticadoComoAdmin, page}) => {
        await abrirTelaCriacaoAtribuicao(page);

        await page.getByTestId('btn-cancelar-atribuicao').click();
        await expect(page).toHaveURL(/\/unidade\/\d+$/);
        await expect(page.getByRole('heading', {name: new RegExp(SIGLA_UNIDADE)})).toBeVisible();
    });

    test('Cenario 5: ADMIN cria atribuição e usuário destino recebe alerta e perfil temporário', async ({_resetAutomatico, _autenticadoComoAdmin, page}) => {
        await abrirTelaCriacaoAtribuicao(page);

        await selecionarUsuarioAlvo(page);
        await page.getByTestId('input-data-inicio').fill('2030-01-01');
        await page.getByTestId('input-data-termino').fill('2030-12-31');
        await page.getByTestId('textarea-justificativa').fill('Cobertura de férias');
        await page.getByTestId('cad-atribuicao__btn-criar-atribuicao').click();

        await expect(page.getByText(TEXTOS.atribuicaoTemporaria.SUCESSO).first()).toBeVisible();

        await loginComPerfil(page, TITULO_USUARIO_ALVO, 'senha', PERFIL_TEMPORARIO);
        await expect(page.locator('.user-info-text')).toContainText(PERFIL_TEMPORARIO);

        const tabelaAlertas = page.getByTestId('tbl-alertas');
        await expect(tabelaAlertas).toBeVisible();

        const linhaAlerta = tabelaAlertas.locator('tr', {hasText: DESCRICAO_ALERTA}).first();
        await expect(linhaAlerta).toBeVisible();
        await expect(linhaAlerta.locator('td').nth(1)).toContainText(DESCRICAO_ALERTA);
        await expect(linhaAlerta.locator('td').nth(2)).toHaveText('');
        await expect(linhaAlerta.locator('td').nth(3)).toHaveText('ADMIN');
    });
});
