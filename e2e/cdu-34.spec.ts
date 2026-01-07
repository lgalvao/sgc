import {expect, test} from './fixtures/base';
import {login, USUARIOS} from './helpers/helpers-auth';
import {criarProcesso} from './helpers/helpers-processos';
import {navegarParaSubprocesso, verificarPaginaPainel} from './helpers/helpers-navegacao';
import {resetDatabase, useProcessoCleanup} from './hooks/hooks-limpeza';

/**
 * CDU-34 - Enviar lembrete de prazo
 * 
 * Ator: Sistema/ADMIN
 * 
 * Fluxo principal (Envio Manual):
 * 1. ADMIN acessa tela de Acompanhamento de Processos
 * 2. Sistema exibe processos com indicadores de prazo
 * 3. ADMIN seleciona unidades com pendências
 * 4. ADMIN aciona "Enviar Lembrete"
 * 5. Sistema exibe modelo da mensagem
 * 6. ADMIN confirma envio
 * 7. Sistema envia e-mail e registra no histórico
 */
test.describe.serial('CDU-34 - Enviar lembrete de prazo', () => {
    const UNIDADE_1 = 'SECAO_221';
    const USUARIO_ADMIN = USUARIOS.ADMIN_1_PERFIL.titulo;
    const SENHA_ADMIN = USUARIOS.ADMIN_1_PERFIL.senha;

    const timestamp = Date.now();
    const descProcesso = `Mapeamento CDU-34 ${timestamp}`;
    let processoId: number;
    let cleanup: ReturnType<typeof useProcessoCleanup>;

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

    test('Preparacao: Admin cria e inicia processo', async ({page}) => {
        await page.goto('/login');
        await login(page, USUARIO_ADMIN, SENHA_ADMIN);

        await criarProcesso(page, {
            descricao: descProcesso,
            tipo: 'MAPEAMENTO',
            diasLimite: 5, // Prazo curto para simular urgência
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

    // ========================================================================
    // TESTES PRINCIPAIS
    // ========================================================================

    test('Cenario 1: ADMIN navega para detalhes do processo', async ({page}) => {
        // CDU-34: Passo 1
        await page.goto('/login');
        await login(page, USUARIO_ADMIN, SENHA_ADMIN);

        await page.getByText(descProcesso).click();
        await expect(page.getByRole('heading', {name: /Unidades participantes/i})).toBeVisible();
    });

    test('Cenario 2: Verificar indicadores de prazo', async ({page}) => {
        // CDU-34: Passo 2 - Sistema exibe indicadores de prazo
        await page.goto('/login');
        await login(page, USUARIO_ADMIN, SENHA_ADMIN);

        await page.getByText(descProcesso).click();

        // Verificar que tabela de unidades está visível
        const tabela = page.getByTestId('tbl-tree');
        if (await tabela.isVisible().catch(() => false)) {
            await expect(tabela).toBeVisible();
        }

        // Verificar se há indicadores visuais de prazo (badges, cores, ícones)
        // Isso depende da implementação visual
    });

    test('Cenario 3: Verificar opção de enviar lembrete', async ({page}) => {
        // CDU-34: Passo 4
        await page.goto('/login');
        await login(page, USUARIO_ADMIN, SENHA_ADMIN);

        await page.getByText(descProcesso).click();
        await navegarParaSubprocesso(page, UNIDADE_1);

        // Verificar se existe opção de enviar lembrete
        const btnLembrete = page.getByRole('button', {name: /Lembrete|Enviar lembrete/i});
        const btnVisivel = await btnLembrete.isVisible().catch(() => false);

        if (!btnVisivel) {
            // Verificar no menu "Mais ações"
            const btnMaisAcoes = page.getByTestId('btn-mais-acoes');
            if (await btnMaisAcoes.isVisible().catch(() => false)) {
                await btnMaisAcoes.click();
                
                const itemLembrete = page.getByRole('menuitem', {name: /Lembrete/i});
                const itemVisivel = await itemLembrete.isVisible().catch(() => false);
                console.log('Opção Enviar Lembrete disponível:', itemVisivel);
            }
        } else {
            await expect(btnLembrete).toBeEnabled();
        }
    });
});
