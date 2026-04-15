import {expect, test} from './fixtures/complete-fixtures.js';
import {criarProcessoCadastroDisponibilizadoFixture, validarProcessoFixture} from './fixtures/index.js';
import {navegarParaAtividadesVisualizacao} from './helpers/helpers-atividades.js';
import {
    aceitarCadastroMapeamento,
    acessarSubprocessoGestor,
    devolverCadastroMapeamento
} from './helpers/helpers-analise.js';
import {fazerLogout, navegarParaSubprocesso} from './helpers/helpers-navegacao.js';
import {acessarDetalhesProcesso, obterAcaoBloco} from './helpers/helpers-processos.js';
import {login, loginComPerfil, USUARIOS} from './helpers/helpers-auth.js';
import {resetDatabase} from './hooks/hooks-limpeza.js';
import {TEXTOS} from '../frontend/src/constants/textos.js';

/**
 * CDU-23 - Homologar cadastros em bloco
 *
 * Ator: ADMIN
 *
 * Pré-condições:
 * - Processo de mapeamento ou revisão com unidades subordinadas
 * - Subprocessos na situação 'Cadastro disponibilizado' ou 'Cadastro aceito'
 *
 * Fluxo principal:
 * 1. No painel, ADMIN acessa processo em andamento
 * 2. Sistema mostra tela Detalhes do processo
 * 3. Sistema identifica unidades elegíveis para homologação
 * 4. ADMIN clica no botão 'Homologar em Bloco'
 * 5. Sistema abre modal com lista de unidades selecionáveis
 * 6. ADMIN seleciona unidades e confirma
 * 7. Sistema executa homologação para cada unidade selecionada
 */
test.describe.serial('CDU-23 - Homologar cadastros em bloco', () => {
    const UNIDADE_1 = 'SECAO_221';

    const timestamp = Date.now();
    const descProcesso = `Mapeamento CDU-23 ${timestamp}`;

    test('Setup data', async ({_resetAutomatico, request}) => {
        await resetDatabase(request);
        const processo = await criarProcessoCadastroDisponibilizadoFixture(request, {
            descricao: descProcesso,
            unidade: UNIDADE_1
        });
        validarProcessoFixture(processo, descProcesso);
    });

    test('Setup aceites', async ({_resetAutomatico, page, _autenticadoComoGestorCoord22}) => {
        await acessarSubprocessoGestor(page, descProcesso, UNIDADE_1);
        await navegarParaAtividadesVisualizacao(page);
        await aceitarCadastroMapeamento(page);

        await loginComPerfil(page, USUARIOS.CHEFE_SECRETARIA_2.titulo, USUARIOS.CHEFE_SECRETARIA_2.senha, 'GESTOR - SECRETARIA_2');
        await acessarSubprocessoGestor(page, descProcesso, UNIDADE_1);
        await navegarParaAtividadesVisualizacao(page);
        await aceitarCadastroMapeamento(page);

        await expect(page.getByTestId('tbl-processos').getByText(descProcesso).first()).toBeVisible();
    });

    test('Cenario 1: ADMIN abre modal e cancela homologação em bloco', async ({_resetAutomatico, page, _autenticadoComoAdmin}) => {
        await acessarDetalhesProcesso(page, descProcesso);
        await expect(page.getByRole('heading', {name: /Unidades participantes/i})).toBeVisible();

        const btnHomologar = await obterAcaoBloco(page, 'btn-processo-homologar-bloco');
        await expect(btnHomologar).toBeVisible();
        await expect(btnHomologar).toBeEnabled();
        await btnHomologar.click();

        const modal = page.locator('#modal-acao-bloco');
        await expect(modal).toHaveClass(/show/);
        await expect(modal.getByText(TEXTOS.acaoBloco.homologar.TITULO_CADASTRO)).toBeVisible();
        await expect(modal.getByText(TEXTOS.acaoBloco.homologar.TEXTO_CADASTRO)).toBeVisible();
        await expect(modal.locator('table')).toBeVisible();
        await expect(modal.getByRole('button', {name: /Cancelar/i})).toBeVisible();
        await expect(modal.getByRole('button', {name: TEXTOS.acaoBloco.homologar.BOTAO})).toBeVisible();
        await modal.getByRole('button', {name: /Cancelar/i}).click();

        await expect(modal).not.toHaveClass(/show/);
        await expect(page.getByRole('heading', {name: /Unidades participantes/i})).toBeVisible();
    });

    test('Cenario 2: ADMIN confirma homologação em bloco e permanece na tela', async ({_resetAutomatico, page, _autenticadoComoAdmin}) => {
        await acessarDetalhesProcesso(page, descProcesso);
        const btnHomologar = await obterAcaoBloco(page, 'btn-processo-homologar-bloco');
        await expect(btnHomologar).toBeVisible();
        await btnHomologar.click();

        const modal = page.locator('#modal-acao-bloco');
        await expect(modal).toHaveClass(/show/);
        await modal.getByRole('button', {name: TEXTOS.acaoBloco.homologar.BOTAO}).click();

        await expect(page.getByText(TEXTOS.sucesso.CADASTROS_HOMOLOGADOS_EM_BLOCO).first()).toBeVisible();

        await expect(page).toHaveURL(/\/processo\/\d+$/);
        await expect(page.getByRole('heading', {name: /Unidades participantes/i})).toBeVisible();
        await expect(page.getByTestId('app-alert')).toContainText(TEXTOS.sucesso.CADASTROS_HOMOLOGADOS_EM_BLOCO);
        await expect(btnHomologar).toBeDisabled();
        await expect(page.getByRole('row', {name: /SECAO_221 - Seção 221 Cadastro homologado/i})).toBeVisible();
    });

    test('Cenario 3: Homologação em bloco registra movimentação e alerta com data/hora', async ({
        _resetAutomatico,
        page,
        _autenticadoComoAdmin
    }) => {
        // Processo da suíte já foi homologado no Cenario 2 — verificar movimentação e alerta
        await acessarDetalhesProcesso(page, descProcesso);
        await navegarParaSubprocesso(page, UNIDADE_1);

        const linhaMovimentacao = page.getByTestId('tbl-movimentacoes')
            .locator('tr', {hasText: /Cadastro homologado/i})
            .first();
        await expect(linhaMovimentacao).toBeVisible();
        await expect(linhaMovimentacao).toContainText(/\d{2}\/\d{2}\/\d{4}/);
        await expect(linhaMovimentacao).toContainText('ADMIN');

        // Verificar alerta para o chefe da unidade do subprocesso (SECAO_221)
        await fazerLogout(page);
        await login(page, USUARIOS.CHEFE_SECAO_221.titulo, USUARIOS.CHEFE_SECAO_221.senha);

        const tabelaAlertas = page.getByTestId('tbl-alertas');
        const linhaAlerta = tabelaAlertas.locator('tr', {hasText: descProcesso})
            .filter({hasText: /homologado/i})
            .first();
        await expect(linhaAlerta).toBeVisible();
        await expect(linhaAlerta).toContainText(/SECAO_221/i);
        await expect(linhaAlerta).toContainText(/homologado/i);
        await expect(linhaAlerta).toContainText(/\d{2}\/\d{2}\/\d{4}/);
    });
});

test.describe.serial('CDU-23 - Homologar cadastros em bloco após devolução', () => {
    const UNIDADE_1 = 'SECAO_221';
    const timestamp = Date.now();
    const descProcesso = `Mapeamento CDU-23 Devolucao ${timestamp}`;

    test('Setup data', async ({_resetAutomatico, request}) => {
        await resetDatabase(request);
        const processo = await criarProcessoCadastroDisponibilizadoFixture(request, {
            descricao: descProcesso,
            unidade: UNIDADE_1
        });
        validarProcessoFixture(processo, descProcesso);
    });

    test('Setup aceites', async ({_resetAutomatico, page, _autenticadoComoGestorCoord22}) => {
        await acessarSubprocessoGestor(page, descProcesso, UNIDADE_1);
        await navegarParaAtividadesVisualizacao(page);
        await aceitarCadastroMapeamento(page);

        await loginComPerfil(page, USUARIOS.CHEFE_SECRETARIA_2.titulo, USUARIOS.CHEFE_SECRETARIA_2.senha, 'GESTOR - SECRETARIA_2');
        await acessarSubprocessoGestor(page, descProcesso, UNIDADE_1);
        await navegarParaAtividadesVisualizacao(page);
        await aceitarCadastroMapeamento(page);

        await expect(page.getByTestId('tbl-processos').getByText(descProcesso).first()).toBeVisible();
    });

    test('Cenario 1: ADMIN não pode homologar em bloco após devolver para ajustes', async ({_resetAutomatico, page, _autenticadoComoAdmin}) => {
        await acessarDetalhesProcesso(page, descProcesso);
        await navegarParaSubprocesso(page, UNIDADE_1);
        await navegarParaAtividadesVisualizacao(page);
        await devolverCadastroMapeamento(page, 'Ajustar cadastro antes da homologação');

        await acessarDetalhesProcesso(page, descProcesso);
        await expect(page.getByRole('heading', {name: /Unidades participantes/i})).toBeVisible();
        const btnHomologar = await obterAcaoBloco(page, 'btn-processo-homologar-bloco');
        await expect(btnHomologar).toBeDisabled();
    });
});
