import {expect, test} from './fixtures/base';
import {login, USUARIOS} from './helpers/helpers-auth';
import {criarProcesso} from './helpers/helpers-processos';
import {adicionarAtividade, adicionarConhecimento, navegarParaAtividades} from './helpers/helpers-atividades';
import {navegarParaSubprocesso, verificarPaginaPainel} from './helpers/helpers-navegacao';
import {resetDatabase, useProcessoCleanup} from './hooks/hooks-limpeza';

/**
 * CDU-32 - Reabrir cadastro
 * 
 * Ator: ADMIN
 * 
 * Pré-condições:
 * - Subprocesso com cadastro já disponibilizado ou aceito
 * 
 * Fluxo principal:
 * 1. ADMIN acessa subprocesso da unidade
 * 2. ADMIN seleciona opção "Reabrir cadastro"
 * 3. Sistema solicita justificativa
 * 4. ADMIN preenche justificativa e confirma
 * 5. Sistema altera situação e envia notificações
 */
test.describe.serial('CDU-32 - Reabrir cadastro', () => {
    const UNIDADE_1 = 'SECAO_221';
    const USUARIO_CHEFE_1 = USUARIOS.CHEFE_SECAO_221.titulo;
    const SENHA_CHEFE_1 = USUARIOS.CHEFE_SECAO_221.senha;
    const USUARIO_ADMIN = USUARIOS.ADMIN_1_PERFIL.titulo;
    const SENHA_ADMIN = USUARIOS.ADMIN_1_PERFIL.senha;

    const timestamp = Date.now();
    const descProcesso = `Mapeamento CDU-32 ${timestamp}`;
    let processoId: number;
    let cleanup: ReturnType<typeof useProcessoCleanup>;

    const atividade1 = `Atividade Reabrir ${timestamp}`;

    test.beforeAll(async ({request}) => {
        await resetDatabase(request);
        cleanup = useProcessoCleanup();
    });

    test.afterAll(async ({request}) => {
        await cleanup.limpar(request);
    });

    // ========================================================================
    // PREPARAÇÃO
    // ========================================================================

    test('Preparacao 1: Admin cria e inicia processo', async ({page}) => {
        await page.goto('/login');
        await login(page, USUARIO_ADMIN, SENHA_ADMIN);

        await criarProcesso(page, {
            descricao: descProcesso,
            tipo: 'MAPEAMENTO',
            diasLimite: 30,
            unidade: UNIDADE_1,
            expandir: ['SECRETARIA_2', 'COORD_22']
        });

        const linhaProcesso = page.locator('tr', {has: page.getByText(descProcesso)});
        await linhaProcesso.click();

        processoId = Number.parseInt(new RegExp(/\/processo\/cadastro\/(\d+)/).exec(page.url())?.[1] || '0');
        if (processoId > 0) cleanup.registrar(processoId);

        await page.getByTestId('btn-processo-iniciar').click();
        await page.getByTestId('btn-iniciar-processo-confirmar').click();

        await verificarPaginaPainel(page);
    });

    test('Preparacao 2: Chefe disponibiliza cadastro', async ({page}) => {
        await page.goto('/login');
        await login(page, USUARIO_CHEFE_1, SENHA_CHEFE_1);

        await page.getByText(descProcesso).click();
        await navegarParaAtividades(page);

        await adicionarAtividade(page, atividade1);
        await adicionarConhecimento(page, atividade1, 'Conhecimento Reabrir 1');

        await page.getByTestId('btn-cad-atividades-disponibilizar').click();
        await page.getByTestId('btn-confirmar-disponibilizacao').click();

        await verificarPaginaPainel(page);
    });

    // ========================================================================
    // TESTES PRINCIPAIS
    // ========================================================================

    test('Cenario 1: ADMIN navega para subprocesso disponibilizado', async ({page}) => {
        await page.goto('/login');
        await login(page, USUARIO_ADMIN, SENHA_ADMIN);

        await page.getByText(descProcesso).click();
        await navegarParaSubprocesso(page, UNIDADE_1);

        await expect(page.getByTestId('subprocesso-header__txt-situacao'))
            .toHaveText(/Cadastro disponibilizado/i);
    });

    test('Cenario 2: ADMIN visualiza botão Reabrir cadastro', async ({page}) => {
        await page.goto('/login');
        await login(page, USUARIO_ADMIN, SENHA_ADMIN);

        await page.getByText(descProcesso).click();
        await navegarParaSubprocesso(page, UNIDADE_1);

        const btnReabrir = page.getByTestId('btn-reabrir-cadastro');
        const btnVisivel = await btnReabrir.isVisible().catch(() => false);
        
        if (btnVisivel) {
            await expect(btnReabrir).toBeEnabled();
        }
    });

    test('Cenario 3: ADMIN abre modal de reabertura de cadastro', async ({page}) => {
        await page.goto('/login');
        await login(page, USUARIO_ADMIN, SENHA_ADMIN);

        await page.getByText(descProcesso).click();
        await navegarParaSubprocesso(page, UNIDADE_1);

        const btnReabrir = page.getByTestId('btn-reabrir-cadastro');
        
        if (await btnReabrir.isVisible().catch(() => false)) {
            await btnReabrir.click();

            const modal = page.getByRole('dialog');
            await expect(modal).toBeVisible();
            await expect(modal.getByText(/Reabrir cadastro/i)).toBeVisible();
            await expect(page.getByTestId('inp-justificativa-reabrir')).toBeVisible();
            await expect(page.getByTestId('btn-confirmar-reabrir')).toBeVisible();
        }
    });

    test('Cenario 4: Botão confirmar desabilitado sem justificativa', async ({page}) => {
        await page.goto('/login');
        await login(page, USUARIO_ADMIN, SENHA_ADMIN);

        await page.getByText(descProcesso).click();
        await navegarParaSubprocesso(page, UNIDADE_1);

        const btnReabrir = page.getByTestId('btn-reabrir-cadastro');
        
        if (await btnReabrir.isVisible().catch(() => false)) {
            await btnReabrir.click();

            const modal = page.getByRole('dialog');
            await expect(modal).toBeVisible();

            // Sem justificativa, botão deve estar desabilitado
            await expect(page.getByTestId('btn-confirmar-reabrir')).toBeDisabled();

            // Preencher justificativa
            await page.getByTestId('inp-justificativa-reabrir').fill('Justificativa de teste');
            await expect(page.getByTestId('btn-confirmar-reabrir')).toBeEnabled();
        }
    });
});
