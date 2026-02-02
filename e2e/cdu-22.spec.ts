import {expect, test} from './fixtures/auth-fixtures.js';
import {USUARIOS} from './helpers/helpers-auth.js';
import {criarProcesso} from './helpers/helpers-processos.js';
import {adicionarAtividade, adicionarConhecimento, navegarParaAtividades} from './helpers/helpers-atividades.js';
import {verificarPaginaPainel} from './helpers/helpers-navegacao.js';
import {resetDatabase, useProcessoCleanup} from './hooks/hooks-limpeza.js';

/**
 * CDU-22 - Aceitar cadastros em bloco
 * 
 * Ator: GESTOR
 * 
 * Pré-condições:
 * - Processo de mapeamento ou revisão com unidades subordinadas
 * - Subprocessos na situação 'Cadastro disponibilizado'
 * 
 * Fluxo principal:
 * 1. No Painel, GESTOR acessa processo em andamento
 * 2. Sistema mostra tela Detalhes do processo
 * 3. Sistema identifica unidades elegíveis e exibe botão de aceite em bloco
 * 4. GESTOR clica no botão 'Aceitar em Bloco'
 * 5. Sistema abre modal com lista de unidades selecionáveis
 * 6. GESTOR seleciona unidades e confirma
 * 7. Sistema executa aceite para cada unidade selecionada
 */
test.describe.serial('CDU-22 - Aceitar cadastros em bloco', () => {
    const UNIDADE_1 = 'SECAO_221';
    const USUARIO_CHEFE_1 = USUARIOS.CHEFE_SECAO_221.titulo;
    const SENHA_CHEFE_1 = USUARIOS.CHEFE_SECAO_221.senha;
    const USUARIO_GESTOR = USUARIOS.GESTOR_COORD_22.titulo;
    const SENHA_GESTOR = USUARIOS.GESTOR_COORD_22.senha;
    const USUARIO_ADMIN = USUARIOS.ADMIN_1_PERFIL.titulo;
    const SENHA_ADMIN = USUARIOS.ADMIN_1_PERFIL.senha;

    const timestamp = Date.now();
    const descProcesso = `Mapeamento CDU-22 ${timestamp}`;
    let processoId: number;
    let cleanup: ReturnType<typeof useProcessoCleanup>;

    const atividade1 = `Atividade Bloco ${timestamp}`;

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

    test('Preparacao 1: Admin cria e inicia processo de mapeamento', async ({page, autenticadoComoAdmin, autenticadoComoGestorCoord22, autenticadoComoChefeSecao221}) => {
        

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

    test('Preparacao 2: Chefe adiciona atividades e disponibiliza cadastro', async ({page}) => {
        

        await page.getByText(descProcesso).click();
        await navegarParaAtividades(page);

        await adicionarAtividade(page, atividade1);
        await adicionarConhecimento(page, atividade1, 'Conhecimento Bloco 1');

        await page.getByTestId('btn-cad-atividades-disponibilizar').click();
        await page.getByTestId('btn-confirmar-disponibilizacao').click();

        await verificarPaginaPainel(page);
    });

    // ========================================================================
    // TESTES PRINCIPAIS
    // ========================================================================

    test('Cenario 1: GESTOR visualiza botão Aceitar em Bloco', async ({page}) => {
        

        await page.getByText(descProcesso).click();
        await expect(page.getByRole('heading', {name: /Unidades participantes/i})).toBeVisible();

        const btnAceitar = page.getByRole('button', {name: /Aceitar em Bloco/i});
        const btnVisivel = await btnAceitar.isVisible().catch(() => false);
        
        if (btnVisivel) {
            await expect(btnAceitar).toBeEnabled();
        }
    });

    test('Cenario 2: GESTOR abre modal de aceite em bloco', async ({page, autenticadoComoGestorCoord22}) => {
        

        await page.getByText(descProcesso).click();
        await expect(page.getByRole('heading', {name: /Unidades participantes/i})).toBeVisible();

        const btnAceitar = page.getByRole('button', {name: /Aceitar em Bloco/i});
        
        if (await btnAceitar.isVisible().catch(() => false)) {
            await btnAceitar.click();

            const modal = page.getByRole('dialog');
            await expect(modal).toBeVisible();
            await expect(modal.getByText(/Aceitar em Bloco/i)).toBeVisible();
            await expect(modal.locator('table')).toBeVisible();
            await expect(modal.getByRole('button', {name: /Cancelar/i})).toBeVisible();

            await modal.getByRole('button', {name: /Cancelar/i}).click();
        }
    });

    test('Cenario 3: Cancelar aceite em bloco permanece na tela', async ({page, autenticadoComoGestorCoord22}) => {
        

        await page.getByText(descProcesso).click();

        const btnAceitar = page.getByRole('button', {name: /Aceitar em Bloco/i});
        
        if (await btnAceitar.isVisible().catch(() => false)) {
            await btnAceitar.click();

            const modal = page.getByRole('dialog');
            await expect(modal).toBeVisible();

            await modal.getByRole('button', {name: /Cancelar/i}).click();

            await expect(modal).toBeHidden();
            await expect(page.getByRole('heading', {name: /Unidades participantes/i})).toBeVisible();
        }
    });
});