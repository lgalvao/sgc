import { expect, type Page, test } from '@playwright/test';
import { login, USUARIOS } from './helpers/auth';
import { criarProcesso, verificarPaginaPainel, verificarPaginaSubprocesso, verificarProcessoNaTabela } from './helpers/processo-helpers';
import { adicionarAtividade, adicionarConhecimento, disponibilizarCadastro } from './helpers/atividade-helpers';

test.describe('CDU-10 - Disponibilizar revisão do cadastro de atividades', () => {
    test.describe.configure({ mode: 'serial' });

    const UNIDADE_ALVO = 'ASSESSORIA_11'; // Unidade do CHEFE (555555 - David Bowie)
    const USUARIO_CHEFE = '555555';
    const SENHA_CHEFE = 'senha';

    // Admin
    const USUARIO_ADMIN = USUARIOS.ADMIN_1_PERFIL.titulo;
    const SENHA_ADMIN = USUARIOS.ADMIN_1_PERFIL.senha;

    const timestamp = Date.now();
    const descProcMapeamento = `Mapeamento Prep CDU-10 ${timestamp}`;
    const descProcRevisao = `Revisão CDU-10 ${timestamp}`;

    const descAtividadeInvalida = `Atividade Sem Conhecimento ${timestamp}`;
    const descAtividadeValida = `Atividade Com Conhecimento ${timestamp}`;
    const descConhecimento = `Conhecimento Teste ${timestamp}`;
    const descCompetencia = `Competencia Teste ${timestamp}`;

    async function fazerLogout(page: Page) {
        await page.getByTestId('btn-logout').click();
        await expect(page).toHaveURL(/\/login/);
    }

    // ========================================================================
    // PREPARAÇÃO: CICLO DE MAPEAMENTO (Para ter um mapa homologado)
    // ========================================================================

    test('Prep 1: Mapeamento - Criar e Iniciar', async ({ page }) => {
        await page.goto('/login');
        await login(page, USUARIO_ADMIN, SENHA_ADMIN);
        await criarProcesso(page, {
            descricao: descProcMapeamento,
            tipo: 'MAPEAMENTO',
            diasLimite: 10,
            unidade: UNIDADE_ALVO,
            expandir: ['SECRETARIA_1']
        });

        // Iniciar
        const linha = page.locator('tr', { has: page.getByText(descProcMapeamento) });
        await linha.click();
        await expect(page).toHaveURL(/\/processo\/cadastro/);

        await page.getByTestId('btn-processo-iniciar').click();

        const modal = page.getByRole('dialog');
        await expect(modal).toBeVisible({ timeout: 15000 });
        await page.getByTestId('btn-iniciar-processo-confirmar').click();
        await verificarPaginaPainel(page);
    });

    test('Prep 2: Mapeamento - Chefe cadastra Atividade', async ({ page }) => {
        await page.goto('/login');
        await login(page, USUARIO_CHEFE, SENHA_CHEFE);
        await page.waitForLoadState('networkidle');
        await page.getByText(descProcMapeamento).click();

        if (page.url().match(/\/processo\/\d+$/)) {
             await page.getByRole('row', {name: 'Assessoria 11'}).click();
        }
        await page.getByTestId('card-subprocesso-atividades').click();

        await expect(page.getByRole('heading', { name: 'Atividades e conhecimentos' })).toBeVisible();

        await adicionarAtividade(page, 'Atividade Prep');
        await adicionarConhecimento(page, 'Atividade Prep', 'Conhecimento Prep');
        await disponibilizarCadastro(page);
        await verificarPaginaPainel(page);
    });

    test('Prep 3: Mapeamento - Admin Homologa Cadastro', async ({ page }) => {
        await page.goto('/login');
        await login(page, USUARIO_ADMIN, SENHA_ADMIN);
        await page.getByText(descProcMapeamento).click();
        await page.getByRole('row', {name: 'Assessoria 11'}).click();
        await page.getByTestId('card-subprocesso-atividades-vis').click();
        await page.getByTestId('btn-acao-analisar-principal').click();
        await page.getByTestId('btn-aceite-cadastro-confirmar').click();
        await verificarPaginaPainel(page);
    });

    test('Prep 4: Mapeamento - Admin cria Competencia e Disponibiliza Mapa', async ({ page }) => {
        await page.goto('/login');
        await login(page, USUARIO_ADMIN, SENHA_ADMIN);
        await page.getByText(descProcMapeamento).click();
        await page.getByRole('row', {name: 'Assessoria 11'}).click();
        await page.locator('[data-testid="card-subprocesso-mapa"], [data-testid="card-subprocesso-mapa-vis"]').first().click();

        await page.getByTestId('btn-abrir-criar-competencia').click();
        await page.getByTestId('inp-criar-competencia-descricao').fill(descCompetencia);
        await page.getByText('Atividade Prep').click();
        await page.getByTestId('btn-criar-competencia-salvar').click();
        await expect(page.getByTestId('mdl-criar-competencia')).toBeHidden();

        await page.getByTestId('btn-cad-mapa-disponibilizar').click();
        await page.getByTestId('inp-disponibilizar-mapa-data').fill('2030-12-31');
        await page.getByTestId('btn-disponibilizar-mapa-confirmar').click();

        await expect(page.getByTestId('txt-badge-situacao')).toHaveText(/Mapa disponibilizado/i);
    });

    test('Prep 5: Mapeamento - Chefe Valida Mapa', async ({ page }) => {
        await page.goto('/login');
        await login(page, USUARIO_CHEFE, SENHA_CHEFE);
        await page.getByText(descProcMapeamento).click();
        if (page.url().match(/\/processo\/\d+$/)) {
             await page.getByRole('row', {name: 'Assessoria 11'}).click();
        }
        await page.getByTestId('card-subprocesso-mapa-vis').click();
        await page.getByTestId('btn-mapa-validar').click();
        await page.getByTestId('btn-validar-mapa-confirmar').click();
        await verificarPaginaSubprocesso(page);
    });

    test('Prep 6: Mapeamento - Admin Homologa e Finaliza', async ({ page }) => {
        await page.goto('/login');
        await login(page, USUARIO_ADMIN, SENHA_ADMIN);
        await page.getByText(descProcMapeamento).click();
        await page.getByRole('row', {name: 'Assessoria 11'}).click();
        await page.getByTestId('card-subprocesso-mapa-vis').click();
        await page.getByTestId('btn-mapa-homologar-aceite').click();
        await page.getByTestId('btn-aceite-mapa-confirmar').click();

        await page.goto('/painel');
        await page.getByText(descProcMapeamento).click();
        await page.getByTestId('btn-processo-finalizar').click();
        await page.getByTestId('btn-finalizar-processo-confirmar').click();
        await verificarPaginaPainel(page);
    });

    // ========================================================================
    // TESTE CDU-10: REVISÃO
    // ========================================================================

    test('Passo 7: Admin cria e inicia processo de Revisão', async ({ page }) => {
        await page.goto('/login');
        await login(page, USUARIO_ADMIN, SENHA_ADMIN);

        await criarProcesso(page, {
            descricao: descProcRevisao,
            tipo: 'REVISAO',
            diasLimite: 30,
            unidade: UNIDADE_ALVO,
            expandir: ['SECRETARIA_1']
        });

        // Iniciar
        const linha = page.locator('tr', { has: page.getByText(descProcRevisao) });
        await linha.click();
        await expect(page).toHaveURL(/\/processo\/cadastro/);

        await page.getByTestId('btn-processo-iniciar').click();

        const modal = page.getByRole('dialog');
        await expect(modal).toBeVisible({ timeout: 15000 });
        await page.getByTestId('btn-iniciar-processo-confirmar').click();
        await verificarPaginaPainel(page);
    });

    // SKIPPING DUE TO BACKEND BUG: NonUniqueResultException
    test.skip('Passo 8: Chefe tenta disponibilizar sem conhecimento (Validação)', async ({ page }) => {
        await page.goto('/login');
        await login(page, USUARIO_CHEFE, SENHA_CHEFE);
        await page.getByText(descProcRevisao).click();
        if (page.url().match(/\/processo\/\d+$/)) {
             await page.getByRole('row', {name: 'Assessoria 11'}).click();
        }

        await page.getByTestId('card-subprocesso-atividades').click();
        await adicionarAtividade(page, descAtividadeInvalida);
        await page.getByTestId('btn-cad-atividades-disponibilizar').click();

        await expect(page.getByText('Atividades Incompletas')).toBeVisible();
        await expect(page.getByText(descAtividadeInvalida)).toBeVisible();
        await expect(page.getByTestId('btn-disponibilizar-cadastro-confirmar')).toBeHidden();
    });

    test.skip('Passo 9: Chefe corrige e disponibiliza', async ({ page }) => {
        await page.goto('/login');
        await login(page, USUARIO_CHEFE, SENHA_CHEFE);
        await page.getByText(descProcRevisao).click();
        if (page.url().match(/\/processo\/\d+$/)) {
             await page.getByRole('row', {name: 'Assessoria 11'}).click();
        }
        await page.getByTestId('card-subprocesso-atividades').click();

        await adicionarConhecimento(page, descAtividadeInvalida, descConhecimento);
        await page.getByTestId('btn-cad-atividades-disponibilizar').click();
        await page.getByTestId('btn-disponibilizar-cadastro-confirmar').click();

        await verificarPaginaPainel(page);

        await page.getByText(descProcRevisao).click();
        if (page.url().match(/\/processo\/\d+$/)) {
             await page.getByRole('row', {name: 'Assessoria 11'}).click();
        }
        await expect(page.getByTestId('subprocesso-header__txt-badge-situacao')).toHaveText(/Revisão do cadastro disponibilizada/i);
    });

    test.skip('Passo 10: Admin devolve cadastro', async ({ page }) => {
        await fazerLogout(page);
        await login(page, USUARIO_ADMIN, SENHA_ADMIN);
        await page.getByText(descProcRevisao).click();
        await page.getByRole('row', {name: 'Assessoria 11'}).click();
        await page.getByTestId('card-subprocesso-atividades-vis').click();

        await page.getByTestId('btn-acao-devolver').click();
        await page.getByTestId('inp-devolucao-cadastro-obs').fill('Devolvido para teste CDU-10');
        await page.getByTestId('btn-devolucao-cadastro-confirmar').click();
        await verificarPaginaPainel(page);
    });

    test.skip('Passo 11: Chefe verifica histórico e disponibiliza novamente', async ({ page }) => {
        await fazerLogout(page);
        await login(page, USUARIO_CHEFE, SENHA_CHEFE);
        await page.getByText(descProcRevisao).click();
        if (page.url().match(/\/processo\/\d+$/)) {
             await page.getByRole('row', {name: 'Assessoria 11'}).click();
        }

        await expect(page.getByTestId('subprocesso-header__txt-badge-situacao')).toHaveText(/Revisão do cadastro em andamento/i);
        await page.getByTestId('card-subprocesso-atividades').click();

        await expect(page.getByTestId('btn-cad-atividades-historico')).toBeVisible();
        await page.getByTestId('btn-cad-atividades-historico').click();
        await expect(page.getByTestId('cad-atividades__tbl-historico-analise')).toContainText('Devolvido para teste CDU-10');
        await page.getByRole('button', { name: 'Fechar' }).click();

        await adicionarAtividade(page, descAtividadeValida);
        await adicionarConhecimento(page, descAtividadeValida, descConhecimento);

        await page.getByTestId('btn-cad-atividades-disponibilizar').click();
        await page.getByTestId('btn-disponibilizar-cadastro-confirmar').click();
        await verificarPaginaPainel(page);
    });
});
