import { expect, Page, test } from '@playwright/test';
import { login, USUARIOS } from './helpers/auth';
import { criarProcesso, verificarProcessoNaTabela } from './helpers/processo-helpers';
import { adicionarAtividade, adicionarConhecimento, navegarParaAtividades } from './helpers/atividade-helpers';

async function fazerLogout(page: Page) {
    await page.getByTestId('btn-logout').click();
    await expect(page).toHaveURL(/\/login/);
}

async function verificarPaginaPainel(page: Page) {
    await expect(page).toHaveURL(/\/painel/);
}

async function verificarPaginaSubprocesso(page: Page) {
    await expect(page).toHaveURL(/\/processo\/\d+\/SECAO_221$/);
}

test.describe('CDU-09 - Disponibilizar cadastro de atividades e conhecimentos', () => {
    test.describe.configure({ mode: 'serial' });

    const UNIDADE_ALVO = 'SECAO_221';
    const USUARIO_CHEFE = USUARIOS.CHEFE_SECAO_221.titulo;
    const SENHA_CHEFE = USUARIOS.CHEFE_SECAO_221.senha;

    const timestamp = Date.now();
    const descProcesso = `Processo CDU-09 ${timestamp}`;

    test('Preparacao: Admin cria e inicia processo', async ({ page }) => {
        await page.goto('/login');
        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);

        await criarProcesso(page, {
            descricao: descProcesso,
            tipo: 'MAPEAMENTO',
            diasLimite: 30,
            unidade: UNIDADE_ALVO,
            expandir: ['SECRETARIA_2', 'COORD_22']
        });

        // Iniciar processo
        const linhaProcesso = page.locator('tr', { has: page.getByText(descProcesso) });
        await linhaProcesso.click();
        await page.getByTestId('btn-processo-iniciar').click();
        await page.getByTestId('btn-iniciar-processo-confirmar').click();
    });

    test('Cenario 1: Validacao - Atividade sem conhecimento', async ({ page }) => {
        // Login como Chefe
        await page.goto('/login');
        await login(page, USUARIO_CHEFE, SENHA_CHEFE);

        // Navegar para o subprocesso
        await page.getByText(descProcesso).click();

        // Se cair na tela de processo (lista de unidades), clicar na unidade
        if (page.url().match(/\/processo\/\d+$/)) {
             await page.getByRole('row', {name: 'Seção 221'}).click();
        }

        await verificarPaginaSubprocesso(page);

        // Entrar em Atividades
        await navegarParaAtividades(page);

        // Adicionar Atividade SEM conhecimento
        const atividadeDesc = `Atividade Incompleta ${timestamp}`;
        await adicionarAtividade(page, atividadeDesc);

        // Tentar Disponibilizar
        await page.getByTestId('btn-cad-atividades-disponibilizar').click();

        // Verificar que modal de confirmação NÃO abre
        await expect(page.getByTestId('btn-disponibilizar-cadastro-confirmar')).toBeHidden();

        // Verificar indicador de erro (Toast/Alert)
        await expect(page.getByText('Atividades Incompletas')).toBeVisible();

        // Adicionar conhecimento para corrigir
        await adicionarConhecimento(page, atividadeDesc, 'Conhecimento Corretivo');

        // Tentar Disponibilizar novamente - Agora deve abrir o modal
        await page.getByTestId('btn-cad-atividades-disponibilizar').click();
        await expect(page.getByTestId('btn-disponibilizar-cadastro-confirmar')).toBeVisible();

        // Cancelar para continuar o teste no proximo passo
        await page.getByRole('button', { name: 'Cancelar' }).click();
    });

    test('Cenario 2: Fluxo Feliz - Disponibilizar Cadastro', async ({ page }) => {
        await page.goto('/login');
        await login(page, USUARIO_CHEFE, SENHA_CHEFE);
        await page.getByText(descProcesso).click();
        if (page.url().match(/\/processo\/\d+$/)) {
             await page.getByRole('row', {name: 'Seção 221'}).click();
        }
        await navegarParaAtividades(page);

        // Garantir que temos dados validos
        const atividadeDesc = `Atividade Validada ${timestamp}`;

        // Check if exists
        const count = await page.getByText(atividadeDesc).count();
        if (count === 0) {
            await adicionarAtividade(page, atividadeDesc);
            await adicionarConhecimento(page, atividadeDesc, 'Conhecimento Valido');
        }

        // Disponibilizar
        await page.getByTestId('btn-cad-atividades-disponibilizar').click();
        await page.getByTestId('btn-disponibilizar-cadastro-confirmar').click();

        // Validar sucesso
        await expect(page.getByRole('heading', { name: /Cadastro de atividades disponibilizado/i })).toBeVisible();
        await verificarPaginaPainel(page);

        // Verificar status no subprocesso
        await page.getByText(descProcesso).click();
        if (page.url().match(/\/processo\/\d+$/)) {
             await page.getByRole('row', {name: 'Seção 221'}).click();
        }
        await expect(page.getByTestId('subprocesso-header__txt-badge-situacao')).toHaveText('Cadastro disponibilizado');
    });

    test('Cenario 3: Devolucao e Historico de Analise', async ({ page }) => {
        // 1. Admin devolve o cadastro
        await page.goto('/login');
        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);

        await page.getByText(descProcesso).click();
        await page.getByRole('row', {name: 'Seção 221'}).click();

        // Entrar na visualizacao de atividades
        await page.getByTestId('card-subprocesso-atividades-vis').click();

        // Clicar em Analisar (Devolver)
        await page.getByTestId('btn-acao-analisar-principal').click();

        // Preencher motivo da devolução
        const motivo = 'Faltou detalhar melhor os conhecimentos técnicos.';
        await page.getByTestId('inp-analise-observacoes').fill(motivo);
        await page.getByTestId('btn-devolucao-cadastro-confirmar').click();

        await verificarPaginaPainel(page);

        // 2. Chefe verifica historico e corrige
        await fazerLogout(page);
        await login(page, USUARIO_CHEFE, SENHA_CHEFE);

        await page.getByText(descProcesso).click();
        if (page.url().match(/\/processo\/\d+$/)) {
             await page.getByRole('row', {name: 'Seção 221'}).click();
        }

        // Verificar status 'Cadastro em andamento'
        await expect(page.getByTestId('subprocesso-header__txt-badge-situacao')).toHaveText('Cadastro em andamento');

        await navegarParaAtividades(page);

        // Verificar botão Histórico de Análise
        await expect(page.getByTestId('btn-cad-atividades-historico')).toBeVisible();
        await page.getByTestId('btn-cad-atividades-historico').click();

        // Verificar conteúdo do modal
        const modal = page.locator('.modal-content');
        await expect(modal).toBeVisible();
        await expect(modal.getByText('Devolução')).toBeVisible();
        await expect(modal.getByText('Faltou detalhar melhor os conhecimentos técnicos.')).toBeVisible();

        // Fechar modal
        await page.getByRole('button', { name: 'Fechar' }).click();

        // Disponibilizar novamente
        await page.getByTestId('btn-cad-atividades-disponibilizar').click();
        await page.getByTestId('btn-disponibilizar-cadastro-confirmar').click();

        // Validar sucesso
        await expect(page.getByRole('heading', { name: /Cadastro de atividades disponibilizado/i })).toBeVisible();
    });
});
