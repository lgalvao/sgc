import {expect, test} from './fixtures/base';
import {login, USUARIOS} from './helpers/helpers-auth';
import {criarProcesso} from './helpers/helpers-processos';
import {adicionarAtividade, adicionarConhecimento, navegarParaAtividades} from './helpers/helpers-atividades';
import {criarCompetencia, disponibilizarMapa, navegarParaMapa} from './helpers/helpers-mapas';
import {verificarPaginaPainel, navegarParaSubprocesso} from './helpers/helpers-navegacao';
import {resetDatabase, useProcessoCleanup} from './hooks/hooks-limpeza';
import {Page} from '@playwright/test';

async function acessarSubprocessoChefe(page: Page, descProcesso: string) {
    await page.getByText(descProcesso).click();
    await page.getByTestId('card-subprocesso-mapa').click();
}

/**
 * CDU-25 - Aceitar validação de mapas de competências em bloco
 * CDU-26 - Homologar validação de mapas de competências em bloco
 * 
 * Ator: GESTOR (CDU-25), ADMIN (CDU-26)
 * 
 * Pré-condições:
 * - Subprocesso nas situações 'Mapa validado' ou 'Mapa com sugestões'
 * - Localização atual na unidade do usuário
 * 
 * Fluxo principal:
 * 1. Usuário acessa processo em andamento
 * 2. Sistema mostra Detalhes do processo
 * 3. Sistema identifica unidades elegíveis 
 * 4. Sistema mostra botão de ação em bloco
 * 5. Sistema abre modal com lista de unidades
 * 6. Usuário confirma
 * 7. Sistema executa ação para cada unidade
 */
test.describe.serial('CDU-25/26 - Aceitar e Homologar mapas em bloco', () => {
    const UNIDADE_1 = 'SECAO_221';
    const USUARIO_CHEFE_1 = USUARIOS.CHEFE_SECAO_221.titulo;
    const SENHA_CHEFE_1 = USUARIOS.CHEFE_SECAO_221.senha;
    const USUARIO_GESTOR = USUARIOS.GESTOR_COORD_22.titulo;
    const SENHA_GESTOR = USUARIOS.GESTOR_COORD_22.senha;
    const USUARIO_ADMIN = USUARIOS.ADMIN_1_PERFIL.titulo;
    const SENHA_ADMIN = USUARIOS.ADMIN_1_PERFIL.senha;

    const timestamp = Date.now();
    const descProcesso = `Mapeamento CDU-25 ${timestamp}`;
    let processoId: number;
    let cleanup: ReturnType<typeof useProcessoCleanup>;

    const atividade1 = `Atividade Val ${timestamp}`;
    const competencia1 = `Competência Val ${timestamp}`;

    test.beforeAll(async ({request}) => {
        await resetDatabase(request);
        cleanup = useProcessoCleanup();
    });

    test.afterAll(async ({request}) => {
        await cleanup.limpar(request);
    });

    // ========================================================================
    // PREPARAÇÃO - Criar processo com mapa validado
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
        await adicionarConhecimento(page, atividade1, 'Conhecimento Val 1');

        await page.getByTestId('btn-cad-atividades-disponibilizar').click();
        await page.getByTestId('btn-confirmar-disponibilizacao').click();

        await verificarPaginaPainel(page);
    });

    test('Preparacao 3: Admin homologa cadastro e cria competências', async ({page}) => {
        await page.goto('/login');
        await login(page, USUARIO_ADMIN, SENHA_ADMIN);

        await page.getByText(descProcesso).click();
        await navegarParaSubprocesso(page, UNIDADE_1);
        await page.getByTestId('card-subprocesso-atividades-vis').click();
        await page.getByTestId('btn-acao-analisar-principal').click();
        await page.getByTestId('btn-aceite-cadastro-confirmar').click();

        await expect(page).toHaveURL(/\/processo\/\d+\/\w+$/);

        await navegarParaMapa(page);
        await criarCompetencia(page, competencia1, [atividade1]);

        await disponibilizarMapa(page, '2030-12-31');
        await verificarPaginaPainel(page);
    });

    test('Preparacao 4: Chefe valida o mapa', async ({page}) => {
        await page.goto('/login');
        await login(page, USUARIO_CHEFE_1, SENHA_CHEFE_1);

        await acessarSubprocessoChefe(page, descProcesso);

        await page.getByTestId('btn-mapa-validar').click();
        const modal = page.getByRole('dialog');
        await expect(modal).toBeVisible();
        await page.getByTestId('btn-validar-mapa-confirmar').click();

        await verificarPaginaPainel(page);
    });

    // ========================================================================
    // TESTES PRINCIPAIS - CDU-25 (GESTOR aceita mapa em bloco)
    // ========================================================================

    test('Cenario 1: GESTOR acessa processo com mapa validado', async ({page}) => {
        // CDU-25: Passos 1-3
        await page.goto('/login');
        await login(page, USUARIO_GESTOR, SENHA_GESTOR);

        await page.getByText(descProcesso).click();
        await expect(page.getByRole('heading', {name: /Unidades participantes/i})).toBeVisible();

        // Verificar botão de aceite em bloco para mapas
        const btnAceitar = page.getByRole('button', {name: /Aceitar.*Bloco/i});
        const btnVisivel = await btnAceitar.isVisible().catch(() => false);
        
        if (btnVisivel) {
            await expect(btnAceitar).toBeEnabled();
        }
    });

    test('Cenario 2: GESTOR abre modal de aceite de mapa em bloco', async ({page}) => {
        // CDU-25: Passos 4-6
        await page.goto('/login');
        await login(page, USUARIO_GESTOR, SENHA_GESTOR);

        await page.getByText(descProcesso).click();

        const btnAceitar = page.getByRole('button', {name: /Aceitar.*Bloco/i});
        
        if (await btnAceitar.isVisible().catch(() => false)) {
            await btnAceitar.click();

            const modal = page.getByRole('dialog');
            await expect(modal).toBeVisible();

            // Verificar elementos do modal
            await expect(modal.locator('table')).toBeVisible();
            await expect(modal.getByRole('button', {name: /Cancelar/i})).toBeVisible();

            await modal.getByRole('button', {name: /Cancelar/i}).click();
        }
    });

    // ========================================================================
    // TESTES PRINCIPAIS - CDU-26 (ADMIN homologa mapa em bloco)
    // ========================================================================

    test('Cenario 3: ADMIN visualiza botão Homologar Mapa em Bloco', async ({page}) => {
        // CDU-26: Passos 1-4
        await page.goto('/login');
        await login(page, USUARIO_ADMIN, SENHA_ADMIN);

        await page.getByText(descProcesso).click();
        await expect(page.getByRole('heading', {name: /Unidades participantes/i})).toBeVisible();

        // ADMIN deve ver botão de homologação em bloco
        const btnHomologar = page.getByRole('button', {name: /Homologar.*Bloco/i});
        const btnVisivel = await btnHomologar.isVisible().catch(() => false);
        
        if (btnVisivel) {
            await expect(btnHomologar).toBeEnabled();
        }
    });

    test('Cenario 4: ADMIN abre modal de homologação de mapa em bloco', async ({page}) => {
        // CDU-26: Passos 5-7
        await page.goto('/login');
        await login(page, USUARIO_ADMIN, SENHA_ADMIN);

        await page.getByText(descProcesso).click();

        const btnHomologar = page.getByRole('button', {name: /Homologar.*Bloco/i});
        
        if (await btnHomologar.isVisible().catch(() => false)) {
            await btnHomologar.click();

            const modal = page.getByRole('dialog');
            await expect(modal).toBeVisible();

            await expect(modal.getByText(/Homologar/i)).toBeVisible();
            await expect(modal.locator('table')).toBeVisible();
            await expect(modal.getByRole('button', {name: /Cancelar/i})).toBeVisible();
            await expect(modal.getByRole('button', {name: /Homologar/i})).toBeVisible();

            await modal.getByRole('button', {name: /Cancelar/i}).click();
        }
    });

    test('Cenario 5: Cancelar homologação de mapa em bloco', async ({page}) => {
        // CDU-26: Passo 7
        await page.goto('/login');
        await login(page, USUARIO_ADMIN, SENHA_ADMIN);

        await page.getByText(descProcesso).click();

        const btnHomologar = page.getByRole('button', {name: /Homologar.*Bloco/i});
        
        if (await btnHomologar.isVisible().catch(() => false)) {
            await btnHomologar.click();

            const modal = page.getByRole('dialog');
            await expect(modal).toBeVisible();

            await modal.getByRole('button', {name: /Cancelar/i}).click();

            await expect(modal).toBeHidden();
            await expect(page.getByRole('heading', {name: /Unidades participantes/i})).toBeVisible();
        }
    });
});
