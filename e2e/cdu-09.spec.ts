import type {Page} from '@playwright/test';
import {expect, test} from './fixtures/complete-fixtures.js';
import {login, USUARIOS} from './helpers/helpers-auth.js';
import {criarProcesso, extrairProcessoId} from './helpers/helpers-processos.js';
import {adicionarAtividade, adicionarConhecimento, navegarParaAtividades, navegarParaAtividadesVisualizacao} from './helpers/helpers-atividades.js';
import {
    abrirHistoricoAnalise,
    acessarSubprocessoAdmin,
    acessarSubprocessoChefeDireto
} from './helpers/helpers-analise.js';
import {fazerLogout, verificarPaginaPainel} from './helpers/helpers-navegacao.js';

async function verificarPaginaSubprocesso(page: Page) {
    await expect(page).toHaveURL(/\/processo\/\d+\/SECAO_221$/);
}

test.describe.serial('CDU-09 - Disponibilizar cadastro de atividades e conhecimentos', () => {
    const UNIDADE_ALVO = 'SECAO_221';
    const USUARIO_CHEFE = USUARIOS.CHEFE_SECAO_221.titulo;
    const SENHA_CHEFE = USUARIOS.CHEFE_SECAO_221.senha;

    const timestamp = Date.now();
    const descProcesso = `Proc 9 ${timestamp}`;

    test('Preparacao: Admin cria e inicia processo', async ({page, autenticadoComoAdmin, cleanupAutomatico}) => {
        await criarProcesso(page, {
            descricao: descProcesso,
            tipo: 'MAPEAMENTO',
            diasLimite: 30,
            unidade: UNIDADE_ALVO,
            expandir: ['SECRETARIA_2', 'COORD_22']
        });

        // Iniciar processo
        const linhaProcesso = page.locator('tr').filter({has: page.getByText(descProcesso)});
        await linhaProcesso.click();

        await expect(page.getByTestId('inp-processo-descricao')).toHaveValue(descProcesso);
        await expect(page.getByText('Carregando unidades...')).toBeHidden();

        await extrairProcessoId(page);

        await page.getByTestId('btn-processo-iniciar').click();
        await page.getByTestId('btn-iniciar-processo-confirmar').click();
        await verificarPaginaPainel(page);
    });

    test('Cenario 1: Validacao - Atividade sem conhecimento', async ({page, autenticadoComoAdmin}) => {
        // Login como Chefe
        await login(page, USUARIO_CHEFE, SENHA_CHEFE);

        // Navegar para o subprocesso (CHEFE vai direto para o subprocesso)
        await acessarSubprocessoChefeDireto(page, descProcesso);

        await verificarPaginaSubprocesso(page);

        // Entrar em Atividades
        await navegarParaAtividades(page);

        // Adicionar Atividade SEM conhecimento
        const atividadeDesc = `Atividade Incompleta ${timestamp}`;
        await adicionarAtividade(page, atividadeDesc);

        // Tentar Disponibilizar
        await page.getByTestId('btn-cad-atividades-disponibilizar').click();

        // Verificar que erro inline aparece na atividade (não há mais modal de pendências)
        const erroInline = page.getByTestId('atividade-erro-validacao');
        await expect(erroInline).toBeVisible();
        await expect(erroInline).toContainText(/conhecimento/i);

        // Adicionar conhecimento para corrigir
        await adicionarConhecimento(page, atividadeDesc, 'Conhecimento Corretivo');

        // Tentar Disponibilizar novamente - Agora deve abrir o modal de confirmação
        await page.getByTestId('btn-cad-atividades-disponibilizar').click();
        await expect(page.getByTestId('btn-confirmar-disponibilizacao')).toBeVisible();

        // Cancelar para continuar o teste no proximo passo
        await page.getByRole('button', {name: 'Cancelar'}).click();
    });

    test('Cenario 2: Caminho feliz - Disponibilizar Cadastro', async ({page}) => {
        await login(page, USUARIO_CHEFE, SENHA_CHEFE);
        await acessarSubprocessoChefeDireto(page, descProcesso);
        await navegarParaAtividades(page);

        // Garantir que temos dados validos
        const atividadeDesc = `Atividade Validada ${timestamp}`;

        await adicionarAtividade(page, atividadeDesc);
        await adicionarConhecimento(page, atividadeDesc, 'Conhecimento Valido');

        // Disponibilizar
        await page.getByTestId('btn-cad-atividades-disponibilizar').click();
        await page.getByTestId('btn-confirmar-disponibilizacao').click();

        // Validar sucesso
        await expect(page.getByText(/Cadastro de atividades disponibilizado/i).first()).toBeVisible();
        await verificarPaginaPainel(page);

        // Verificar status no subprocesso
        await acessarSubprocessoChefeDireto(page, descProcesso);
        await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Cadastro disponibilizado/i);
    });

    test('Cenario 3: Devolucao e Historico de Analise', async ({page, autenticadoComoAdmin}) => {
        // 1. Admin devolve o cadastro
        await acessarSubprocessoAdmin(page, descProcesso, UNIDADE_ALVO);

        // Entrar no cadastro de atividades (visualização)
        await navegarParaAtividadesVisualizacao(page);

        // Devolver cadastro
        await page.getByTestId('btn-acao-devolver').click();

        // Preencher motivo da devolução
        const motivo = 'Faltou detalhar melhor os conhecimentos técnicos.';
        await page.getByTestId('inp-devolucao-cadastro-obs').fill(motivo);
        await page.getByTestId('btn-devolucao-cadastro-confirmar').click();

        await verificarPaginaPainel(page);

        // 2. Chefe verifica historico e corrige
        await fazerLogout(page);
        await login(page, USUARIO_CHEFE, SENHA_CHEFE);

        await acessarSubprocessoChefeDireto(page, descProcesso);

        // Verificar situação 'Cadastro em andamento'
        await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Cadastro em andamento/i);

        await navegarParaAtividades(page);

        // Abrir modal de histórico de análise (via dropdown "Mais ações")
        const modal = await abrirHistoricoAnalise(page);
        await expect(modal).toBeVisible();

        // Assumindo que é a primeira linha ou única
        await expect(modal.getByTestId('cell-resultado-0')).toHaveText(/Devolu[cç][aã]o/i);
        await expect(modal.getByTestId('cell-observacao-0')).toHaveText('Faltou detalhar melhor os conhecimentos técnicos.');

        // Fechar modal
        await page.getByRole('button', {name: 'Fechar'}).click();

        // Disponibilizar novamente
        await page.getByTestId('btn-cad-atividades-disponibilizar').click();
        await page.getByTestId('btn-confirmar-disponibilizacao').click();

        // Validar sucesso
        await expect(page.getByText(/Cadastro de atividades disponibilizado/i).first()).toBeVisible();
    });
});