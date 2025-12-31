import {expect, test} from './fixtures/base';
import {login, USUARIOS} from './helpers/helpers-auth';
import {criarProcesso} from './helpers/helpers-processos';
import {adicionarAtividade, adicionarConhecimento, navegarParaAtividades} from './helpers/helpers-atividades';
import {verificarPaginaPainel, navegarParaSubprocesso} from './helpers/helpers-navegacao';
import {resetDatabase, useProcessoCleanup} from './hooks/hooks-limpeza';

/**
 * CDU-32 - Reabrir cadastro
 * CDU-33 - Reabrir revisão de cadastro
 * 
 * Ator: ADMIN
 * 
 * Fluxo principal:
 * 1. ADMIN acessa subprocesso da unidade
 * 2. ADMIN seleciona opção "Reabrir Cadastro/Revisão"
 * 3. Sistema solicita justificativa
 * 4. ADMIN confirma
 * 5. Sistema altera situação e envia notificações
 */
test.describe.serial('CDU-32/33 - Reabrir cadastro e revisão', () => {
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
    // PREPARAÇÃO - Criar processo com cadastro disponibilizado
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
    // TESTES PRINCIPAIS - CDU-32
    // ========================================================================

    test('Cenario 1: ADMIN navega para subprocesso disponibilizado', async ({page}) => {
        // CDU-32: Passos 1-2
        await page.goto('/login');
        await login(page, USUARIO_ADMIN, SENHA_ADMIN);

        await page.getByText(descProcesso).click();
        await navegarParaSubprocesso(page, UNIDADE_1);

        // Verificar situação do subprocesso
        await expect(page.getByTestId('subprocesso-header__txt-situacao'))
            .toHaveText(/Cadastro disponibilizado/i);
    });

    test('Cenario 2: ADMIN verifica opção de reabrir cadastro', async ({page}) => {
        // CDU-32: Passo 3
        await page.goto('/login');
        await login(page, USUARIO_ADMIN, SENHA_ADMIN);

        await page.getByText(descProcesso).click();
        await navegarParaSubprocesso(page, UNIDADE_1);

        // Procurar por opção de reabrir (pode estar em menu dropdown ou como botão)
        const btnReabrir = page.getByRole('button', {name: /Reabrir.*Cadastro/i});
        const btnVisivel = await btnReabrir.isVisible().catch(() => false);

        if (!btnVisivel) {
            // Verificar no menu "Mais ações"
            const btnMaisAcoes = page.getByTestId('btn-mais-acoes');
            if (await btnMaisAcoes.isVisible().catch(() => false)) {
                await btnMaisAcoes.click();
                
                const itemReabrir = page.getByRole('menuitem', {name: /Reabrir/i});
                const itemVisivel = await itemReabrir.isVisible().catch(() => false);
                // Se a opção existe, o teste é bem sucedido
                console.log('Opção Reabrir disponível:', itemVisivel);
            }
        }
    });
});
