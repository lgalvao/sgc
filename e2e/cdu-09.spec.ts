import {expect, test} from './fixtures/complete-fixtures.js';
import {login, USUARIOS} from './helpers/helpers-auth.js';
import {criarProcessoFixture} from './fixtures/fixtures-processos.js';
import {adicionarAtividade, adicionarConhecimento, navegarParaAtividades,} from './helpers/helpers-atividades.js';
import {abrirHistoricoAnalise, fecharHistoricoAnalise,} from './helpers/helpers-analise.js';
import {
    fazerLogout,
    verificarAlertaPainel,
    verificarPaginaPainel,
    verificarToast,
} from './helpers/helpers-navegacao.js';
import {TEXTOS} from '../frontend/src/constants/textos.js';

test.describe.serial('CDU-09 - Disponibilizar cadastro de atividades e conhecimentos', () => {
    const UNIDADE_ALVO = 'SECAO_221';
    const USUARIO_CHEFE = USUARIOS.CHEFE_SECAO_221.titulo;
    const SENHA_CHEFE = USUARIOS.CHEFE_SECAO_221.senha;

    let processoCodigo: number;

    test('Setup: Criar processo via fixture', async ({_resetAutomatico, request}) => {
        const processo = await criarProcessoFixture(request, {
            unidade: UNIDADE_ALVO,
            iniciar: true,
            tipo: 'MAPEAMENTO'
        });
        processoCodigo = processo.codigo;
        expect(processoCodigo).toBeGreaterThan(0);
    });

    test('Cenario 1: Validacao - Atividade sem conhecimento', async ({_resetAutomatico, page}) => {
        const timestamp = Date.now();
        await login(page, USUARIO_CHEFE, SENHA_CHEFE);
        await page.goto(`/processo/${processoCodigo}/${UNIDADE_ALVO}`);
        await navegarParaAtividades(page);

        const atividadeDesc = `Atividade incompleta ${timestamp}`;
        await adicionarAtividade(page, atividadeDesc);

        await expect(page.getByTestId('btn-cad-atividades-disponibilizar')).toBeDisabled();

        await adicionarConhecimento(page, atividadeDesc, 'Conhecimento corretivo');
        await page.getByTestId('btn-cad-atividades-disponibilizar').click();
        const modalConfirmacao = page.getByRole('dialog');
        await expect(modalConfirmacao.getByRole('heading', {name: TEXTOS.atividades.MODAL_DISPONIBILIZAR_TITULO})).toBeVisible();
        await expect(modalConfirmacao.getByText(TEXTOS.atividades.MODAL_DISPONIBILIZAR_TEXTO)).toBeVisible();
        await expect(page.getByTestId('btn-confirmar-disponibilizacao')).toBeVisible();
        await page.getByTestId('btn-disponibilizar-revisao-cancelar').click();
        await expect(page.getByRole('heading', {name: TEXTOS.atividades.TITULO, level: 2})).toBeVisible();
    });

    test('Cenario 2: Caminho feliz - Disponibilizar cadastro', async ({_resetAutomatico, page}) => {
        const timestamp = Date.now();
        await login(page, USUARIO_CHEFE, SENHA_CHEFE);
        await page.goto(`/processo/${processoCodigo}/${UNIDADE_ALVO}/cadastro`);

        const atividadeDesc = `Atividade validada ${timestamp}`;
        await adicionarAtividade(page, atividadeDesc);
        await adicionarConhecimento(page, atividadeDesc, 'Conhecimento valido');

        await page.getByTestId('btn-cad-atividades-disponibilizar').click();
        const modalConfirmacao = page.getByRole('dialog');
        await expect(modalConfirmacao.getByRole('heading', {name: TEXTOS.atividades.MODAL_DISPONIBILIZAR_TITULO})).toBeVisible();
        await expect(modalConfirmacao.getByText(TEXTOS.atividades.MODAL_DISPONIBILIZAR_TEXTO)).toBeVisible();
        await page.getByTestId('btn-confirmar-disponibilizacao').click();

        await verificarToast(page, TEXTOS.sucesso.CADASTRO_ATIVIDADES_DISPONIBILIZADO);
        await verificarPaginaPainel(page);

        await login(page, USUARIOS.GESTOR_COORD_22.titulo, USUARIOS.GESTOR_COORD_22.senha);
        await verificarAlertaPainel(page, /Cadastro da unidade SECAO_221 disponibilizado para análise/i);

        await page.goto(`/processo/${processoCodigo}/${UNIDADE_ALVO}`);
        await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Cadastro disponibilizado/i);
        await expect(page.getByTestId('tbl-movimentacoes')).toContainText(TEXTOS.movimentacao.CADASTRO_DISPONIBILIZADO);
    });

    test('Cenario 3: Devolucao e Historico de Analise', async ({_resetAutomatico, page}) => {
        const motivo = 'Faltou detalhar melhor os conhecimentos técnicos.';

        await login(page, USUARIOS.GESTOR_COORD_22.titulo, USUARIOS.GESTOR_COORD_22.senha);
        await page.goto(`/processo/${processoCodigo}/${UNIDADE_ALVO}/vis-cadastro`);

        await page.getByTestId('btn-acao-devolver').click();
        await page.getByTestId('inp-devolucao-cadastro-obs').fill(motivo);
        await page.getByTestId('btn-devolucao-cadastro-confirmar').click();

        await expect(page).toHaveURL(/\/painel/);
        await fazerLogout(page);

        await login(page, USUARIO_CHEFE, SENHA_CHEFE);
        await page.goto(`/processo/${processoCodigo}/${UNIDADE_ALVO}`);
        await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Cadastro em andamento/i);

        await navegarParaAtividades(page);
        const modal = await abrirHistoricoAnalise(page);
        await expect(modal.getByTestId('cell-dataHora-0')).not.toHaveText('');
        await expect(modal.getByTestId('cell-unidade-0')).toHaveText('COORD_22');
        await expect(modal.getByTestId('cell-resultado-0')).toHaveText(/Devolu[cç][aã]o/i);
        await expect(modal.getByTestId('cell-observacao-0')).toHaveText(motivo);
        await fecharHistoricoAnalise(page);

        await page.getByTestId('btn-cad-atividades-disponibilizar').click();
        await page.getByTestId('btn-confirmar-disponibilizacao').click();
        await verificarToast(page, TEXTOS.sucesso.CADASTRO_ATIVIDADES_DISPONIBILIZADO);
        await verificarPaginaPainel(page);
    });
});
