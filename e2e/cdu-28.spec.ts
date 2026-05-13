import {expect, test} from './fixtures/complete-fixtures.js';
import {loginComPerfil} from './helpers/helpers-auth.js';
import {TEXTOS} from '../frontend/src/constants/textos.js';

test.describe.serial('CDU-28 - Manter atribuição temporária', () => {
    const SIGLA_UNIDADE = 'ASSESSORIA_11';
    const TITULO_USUARIO_ALVO = '232323';
    const NOME_USUARIO_ALVO = 'Bon Jovi';
    const PERFIL_TEMPORARIO = 'CHEFE - ASSESSORIA_11';
    const SIGLAS_SUBARVORE_SECRETARIA_1 = [
        'ASSESSORIA_11',
        'ASSESSORIA_12',
        'COORD_11',
        'COORD_12'
    ];
    const SIGLAS_SUBARVORE_SECRETARIA_2 = [
        'ASSESSORIA_21',
        'ASSESSORIA_22',
        'COORD_21',
        'COORD_22'
    ];
    const SIGLAS_SUBARVORE_SECRETARIA_3 = [
        'ASSESSORIA_31',
        'ASSESSORIA_32',
        'COORD_31',
        'COORD_32'
    ];

    function formatarDataInput(data: Date) {
        return data.toISOString().slice(0, 10);
    }

    function obterPeriodoVigente() {
        const inicio = new Date();
        const termino = new Date();
        termino.setDate(termino.getDate() + 30);
        return {
            dataInicio: formatarDataInput(inicio),
            dataTermino: formatarDataInput(termino),
        };
    }

    async function validarRamoUnidade(
        page: import('@playwright/test').Page,
        siglaRamo: string,
        siglasFilhas: string[]
    ) {
        const tabela = page.getByTestId('tbl-tree');
        await expect(tabela.getByText(new RegExp(String.raw`^${siglaRamo}\s+-\s+`)).first()).toBeVisible();
        for (const siglaFilha of siglasFilhas) {
            await expect(tabela.getByText(new RegExp(String.raw`^${siglaFilha}\s+-\s+`)).first()).toBeVisible();
        }
    }

    async function acessarUnidadeAlvo(page: import('@playwright/test').Page) {
        const tabela = page.getByTestId('tbl-tree');
        await expect(tabela.getByText(/^SECRETARIA_1\s+-\s+/).first()).toBeVisible();
        await expect(tabela.getByText(/^SECRETARIA_2\s+-\s+/).first()).toBeVisible();
        const textoUnidade = tabela.getByText(new RegExp(String.raw`^${SIGLA_UNIDADE}\s+-\s+`)).first();
        await expect(textoUnidade).toBeVisible();
        await textoUnidade.click();
    }

    async function garantirSemAtribuicaoVigente(page: import('@playwright/test').Page) {
        await acessarUnidadeAlvo(page);
        await expect(page).toHaveURL(/\/unidade\/\d+(?:\?.*)?$/);
        await expect(page.getByTestId('unidade-view__titulo')).toHaveText(SIGLA_UNIDADE);

        const textoBotaoAtribuicao = page.getByTestId('unidade-view__btn-atribuicao-texto');
        if (await textoBotaoAtribuicao.isVisible().catch(() => false) && await textoBotaoAtribuicao.textContent() === 'Editar atribuição') {
            await page.getByTestId('unidade-view__btn-criar-atribuicao').click();
            await expect(page).toHaveURL(/\/unidade\/\d+\/atribuicao(?:\?.*)?$/);
            await expect(page.getByTestId('btn-remover-atribuicao')).toBeVisible();

            await page.getByTestId('btn-remover-atribuicao').click();
            const modal = page.getByRole('dialog');
            await expect(modal).toBeVisible();
            await modal.getByRole('button', {name: 'Remover'}).click();
            await expect(page.getByText(TEXTOS.atribuicaoTemporaria.SUCESSO_REMOCAO).first()).toBeVisible();

            await page.getByTestId('btn-cancelar-atribuicao').click();
            await expect(page).toHaveURL(/\/unidade\/\d+(?:\?.*)?$/);
        }

        await expect(page.getByTestId('unidade-view__btn-atribuicao-texto')).toHaveText('Criar atribuição');
    }

    async function abrirTelaCriacaoAtribuicao(page: import('@playwright/test').Page) {
        await garantirSemAtribuicaoVigente(page);
        await page.getByTestId('unidade-view__btn-criar-atribuicao').click();
        await expect(page).toHaveURL(/\/unidade\/\d+\/atribuicao(?:\?.*)?$/);
    }

    async function criarAtribuicaoVigente(page: import('@playwright/test').Page, justificativa = 'Cobertura de férias') {
        const {dataInicio, dataTermino} = obterPeriodoVigente();

        await abrirTelaCriacaoAtribuicao(page);
        await selecionarUsuarioAlvo(page);
        await page.getByTestId('input-data-inicio').fill(dataInicio);
        await page.getByTestId('input-data-termino').fill(dataTermino);
        await page.getByTestId('textarea-justificativa').fill(justificativa);
        await page.getByTestId('cad-atribuicao__btn-salvar-atribuicao').click();
        await expect(page.getByText(TEXTOS.atribuicaoTemporaria.SUCESSO).first()).toBeVisible();
    }

    async function selecionarUsuarioAlvo(page: import('@playwright/test').Page) {
        const inputBusca = page.getByTestId('input-busca-usuario');
        await inputBusca.click();
        await inputBusca.pressSequentially(TITULO_USUARIO_ALVO, {delay: 100});

        const listaResultados = page.getByTestId('lista-usuarios-pesquisa');
        await expect(listaResultados).toBeVisible();

        const opcaoUsuario = page.getByTestId(new RegExp(`opcao-usuario-${TITULO_USUARIO_ALVO}`)).filter({hasText: NOME_USUARIO_ALVO}).first();
        await expect(opcaoUsuario).toBeVisible();
        await opcaoUsuario.click();
        await expect(listaResultados).toBeHidden();
    }

    test.beforeEach(async ({_resetAutomatico, _autenticadoComoAdmin, page}) => {
        await page.getByRole('link', {name: /Unidades/i}).click();
        await expect(page).toHaveURL(/\/unidades/);
        await expect(page.getByRole('heading', {name: TEXTOS.unidades.TITULO})).toBeVisible();
        await expect(page.getByTestId('btn-unidades-expandir-todas')).toBeVisible();
        await page.getByTestId('btn-unidades-expandir-todas').click();
        await expect(page.getByTestId('tbl-tree')).toBeVisible();
    });

    test('Cenario 1: ADMIN navega pela árvore e acessa detalhes da unidade', async ({
                                                                                        _resetAutomatico,
                                                                                        _autenticadoComoAdmin,
                                                                                        page
                                                                                    }) => {
        await validarRamoUnidade(page, 'SECRETARIA_1', SIGLAS_SUBARVORE_SECRETARIA_1);
        await validarRamoUnidade(page, 'SECRETARIA_2', SIGLAS_SUBARVORE_SECRETARIA_2);
        await validarRamoUnidade(page, 'SECRETARIA_3', SIGLAS_SUBARVORE_SECRETARIA_3);

        const tabela = page.getByTestId('tbl-tree');
        await expect(tabela.getByText(/^SECAO_111\s+-\s+/).first()).toBeVisible();
        await expect(tabela.getByText(/^SECAO_112\s+-\s+/).first()).toBeVisible();
        await expect(tabela.getByText(/^SECAO_113\s+-\s+/).first()).toBeVisible();

        await garantirSemAtribuicaoVigente(page);

        await expect(page.getByTestId('unidade-view__btn-atribuicao-texto')).toHaveText('Criar atribuição');
    });

    test('Cenario 2: tela de atribuição expõe os campos e ações exigidos pelo requisito', async ({
                                                                                                     _resetAutomatico,
                                                                                                     _autenticadoComoAdmin,
                                                                                                     page
                                                                                                 }) => {
        await abrirTelaCriacaoAtribuicao(page);

        await expect(page.getByTestId('atribuicao-view__titulo')).toHaveText('Atribuição temporária');
        await expect(page.getByTestId('atribuicao-view__sigla')).toHaveText(SIGLA_UNIDADE);
        await expect(page.getByText(TEXTOS.atribuicaoTemporaria.AJUDA_PESQUISA_USUARIO)).toBeVisible();

        await expect(page.getByLabel(TEXTOS.atribuicaoTemporaria.LABEL_USUARIO)).toBeVisible();
        await expect(page.getByTestId('input-busca-usuario')).toHaveAttribute('placeholder', TEXTOS.atribuicaoTemporaria.SELECIONE_USUARIO);
        await expect(page.getByLabel(TEXTOS.atribuicaoTemporaria.LABEL_DATA_INICIO)).toBeVisible();
        await expect(page.getByLabel(TEXTOS.atribuicaoTemporaria.LABEL_DATA_TERMINO)).toBeVisible();
        await expect(page.getByLabel(TEXTOS.atribuicaoTemporaria.LABEL_JUSTIFICATIVA)).toBeVisible();

        await expect(page.getByTestId('btn-cancelar-atribuicao')).toBeVisible();
        await expect(page.getByTestId('cad-atribuicao__btn-salvar-atribuicao')).toBeVisible();
        await expect(page.getByTestId('cad-atribuicao__btn-salvar-atribuicao')).toHaveText('Criar');
        await expect(page.getByTestId('btn-remover-atribuicao')).toBeHidden();
    });

    test('Cenario 3: o formulário exibe erros de validação ao tentar criar incompleto', async ({
                                                                                                   _resetAutomatico,
                                                                                                   _autenticadoComoAdmin,
                                                                                                   page
                                                                                               }) => {
        await abrirTelaCriacaoAtribuicao(page);

        const btnCriar = page.getByTestId('cad-atribuicao__btn-salvar-atribuicao');
        await btnCriar.click();

        await expect(page.getByText(TEXTOS.atribuicaoTemporaria.ERRO_SELECIONE_USUARIO)).toBeVisible();
        await expect(page.getByText('Informe a data de início.')).toBeVisible();
        await expect(page.getByText('Informe a data de término.')).toBeVisible();
        await expect(page.getByText('Informe a justificativa.')).toBeVisible();
    });

    test('Cenario 4: ADMIN cancela criação e retorna para detalhes da unidade', async ({
                                                                                           _resetAutomatico,
                                                                                           _autenticadoComoAdmin,
                                                                                           page
                                                                                       }) => {
        await abrirTelaCriacaoAtribuicao(page);

        await page.getByTestId('btn-cancelar-atribuicao').click();
        await expect(page).toHaveURL(/\/unidade\/\d+(?:\?.*)?$/);
        await expect(page.getByTestId('unidade-view__titulo')).toHaveText(SIGLA_UNIDADE);
    });

    test('Cenario 5: ADMIN cria atribuição e usuário destino recebe perfil temporário', async ({
                                                                                                   _resetAutomatico,
                                                                                                   _autenticadoComoAdmin,
                                                                                                   page
                                                                                               }) => {
        await criarAtribuicaoVigente(page);
        await page.getByTestId('btn-cancelar-atribuicao').click();
        await expect(page.getByTestId('unidade-view__btn-atribuicao-texto')).toHaveText('Editar atribuição');
        await expect(page.getByText(/Atrib\. temporária/)).toBeVisible();

        await loginComPerfil(page, TITULO_USUARIO_ALVO, 'senha', PERFIL_TEMPORARIO);
        await expect(page.locator('.user-info-text')).toContainText(PERFIL_TEMPORARIO);
    });

    test('Cenario 6: ADMIN acessa atribuição vigente para editar', async ({
                                                                              _resetAutomatico,
                                                                              _autenticadoComoAdmin,
                                                                              page
                                                                          }) => {
        await criarAtribuicaoVigente(page);
        await page.getByTestId('btn-cancelar-atribuicao').click();

        await expect(page.getByTestId('unidade-view__btn-atribuicao-texto')).toHaveText('Editar atribuição');
        await page.getByTestId('unidade-view__btn-criar-atribuicao').click();

        await expect(page.getByTestId('atribuicao-view__titulo')).toHaveText('Atribuição temporária');
        await expect(page.getByTestId('cad-atribuicao__btn-salvar-atribuicao')).toHaveText('Salvar');
        await expect(page.getByTestId('btn-remover-atribuicao')).toBeVisible();

        await page.getByTestId('textarea-justificativa').fill('Cobertura de férias atualizada');
        await page.getByTestId('cad-atribuicao__btn-salvar-atribuicao').click();

        await expect(page.getByText(TEXTOS.atribuicaoTemporaria.SUCESSO_ATUALIZACAO).first()).toBeVisible();
    });

    test('Cenario 7: ADMIN remove atribuição vigente', async ({
                                                                  _resetAutomatico,
                                                                  _autenticadoComoAdmin,
                                                                  page
                                                              }) => {
        await criarAtribuicaoVigente(page);
        await page.getByTestId('btn-cancelar-atribuicao').click();
        await page.getByTestId('unidade-view__btn-criar-atribuicao').click();

        await page.getByTestId('btn-remover-atribuicao').click();
        const modal = page.getByRole('dialog');
        await expect(modal).toBeVisible();
        await page.getByTestId('btn-confirmar-remover-atribuicao').click();

        await expect(page.getByText(TEXTOS.atribuicaoTemporaria.SUCESSO_REMOCAO).first()).toBeVisible();

        await page.getByTestId('btn-cancelar-atribuicao').click();
        await expect(page.getByTestId('unidade-view__btn-atribuicao-texto')).toHaveText('Criar atribuição');
    });
});
