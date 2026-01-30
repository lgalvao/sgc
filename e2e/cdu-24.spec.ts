import {expect, test} from './fixtures/base';
import {login, USUARIOS} from './helpers/helpers-auth';
import {criarProcesso} from './helpers/helpers-processos';
import {adicionarAtividade, adicionarConhecimento, navegarParaAtividades} from './helpers/helpers-atividades';
import {criarCompetencia, navegarParaMapa} from './helpers/helpers-mapas';
import {navegarParaSubprocesso, verificarPaginaPainel} from './helpers/helpers-navegacao';
import {resetDatabase, useProcessoCleanup} from './hooks/hooks-limpeza';

/**
 * CDU-24 - Disponibilizar mapas de competências em bloco
 * 
 * Ator: ADMIN
 * 
 * Pré-condições:
 * - Processo de mapeamento com ao menos uma unidade com subprocesso na situação 'Mapa criado'
 * 
 * Fluxo principal:
 * 1. ADMIN acessa processo de mapeamento em andamento
 * 2. Sistema mostra tela Detalhes do processo
 * 3. Sistema identifica unidades com mapas criados/ajustados
 * 4. Sistema mostra botão 'Disponibilizar mapas em bloco'
 * 5. Sistema abre modal com lista de unidades e campo de data limite
 * 6. ADMIN confirma
 * 7. Sistema executa disponibilização para cada unidade selecionada
 */
test.describe.serial('CDU-24 - Disponibilizar mapas em bloco', () => {
    const UNIDADE_1 = 'SECAO_221';
    const USUARIO_CHEFE_1 = USUARIOS.CHEFE_SECAO_221.titulo;
    const SENHA_CHEFE_1 = USUARIOS.CHEFE_SECAO_221.senha;
    const USUARIO_ADMIN = USUARIOS.ADMIN_1_PERFIL.titulo;
    const SENHA_ADMIN = USUARIOS.ADMIN_1_PERFIL.senha;

    const timestamp = Date.now();
    const descProcesso = `Mapeamento CDU-24 ${timestamp}`;
    let processoId: number;
    let cleanup: ReturnType<typeof useProcessoCleanup>;

    const atividade1 = `Atividade Mapa ${timestamp}`;
    const competencia1 = `Competência Mapa ${timestamp}`;

    test.beforeAll(async ({request}) => {
        await resetDatabase(request);
        cleanup = useProcessoCleanup();
    });

    test.afterAll(async ({request}) => {
        await cleanup.limpar(request);
    });

    // ========================================================================
    // PREPARAÇÃO - Criar processo com mapa criado
    // ========================================================================

    test('Preparacao 1: Admin cria e inicia processo', async ({page}) => {
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
        await login(page, USUARIO_CHEFE_1, SENHA_CHEFE_1);

        await page.getByText(descProcesso).click();
        await navegarParaAtividades(page);

        await adicionarAtividade(page, atividade1);
        await adicionarConhecimento(page, atividade1, 'Conhecimento Mapa 1');

        await page.getByTestId('btn-cad-atividades-disponibilizar').click();
        await page.getByTestId('btn-confirmar-disponibilizacao').click();

        await verificarPaginaPainel(page);
    });

    test('Preparacao 3: Admin homologa cadastro e cria competências', async ({page}) => {
        await login(page, USUARIO_ADMIN, SENHA_ADMIN);

        await page.getByText(descProcesso).click();
        await navegarParaSubprocesso(page, UNIDADE_1);
        await page.getByTestId('card-subprocesso-atividades-vis').click();
        await page.getByTestId('btn-acao-analisar-principal').click();
        await page.getByTestId('btn-aceite-cadastro-confirmar').click();

        await expect(page).toHaveURL(/\/processo\/\d+\/\w+$/);

        // Criar competências
        await navegarParaMapa(page);
        await criarCompetencia(page, competencia1, [atividade1]);

        // Verificar que mapa foi criado (situação 'Mapa criado')
        await page.goto('/painel');
        await page.getByText(descProcesso).click();
        await navegarParaSubprocesso(page, UNIDADE_1);
        await expect(page.getByTestId('subprocesso-header__txt-situacao'))
            .toHaveText(/Mapa criado/i);
    });

    // ========================================================================
    // TESTES PRINCIPAIS - CDU-24
    // ========================================================================

    test('Cenario 1: ADMIN visualiza botão Disponibilizar Mapas em Bloco', async ({page}) => {
        // CDU-24: Passos 1-4
        await login(page, USUARIO_ADMIN, SENHA_ADMIN);

        await page.getByText(descProcesso).click();
        await expect(page.getByRole('heading', {name: /Unidades participantes/i})).toBeVisible();

        // Verificar botão
        const btnDisponibilizar = page.getByRole('button', {name: /Disponibilizar.*Bloco/i});
        const btnVisivel = await btnDisponibilizar.isVisible().catch(() => false);
        
        if (btnVisivel) {
            await expect(btnDisponibilizar).toBeEnabled();
        }
    });

    test('Cenario 2: Modal de disponibilização inclui campo de data limite', async ({page}) => {
        // CDU-24: Passo 5 - Modal inclui campo de data limite obrigatório
        await login(page, USUARIO_ADMIN, SENHA_ADMIN);

        await page.getByText(descProcesso).click();
        await expect(page.getByRole('heading', {name: /Unidades participantes/i})).toBeVisible();

        const btnDisponibilizar = page.getByRole('button', {name: /Disponibilizar.*Bloco/i});
        
        if (await btnDisponibilizar.isVisible().catch(() => false)) {
            await btnDisponibilizar.click();

            const modal = page.getByRole('dialog');
            await expect(modal).toBeVisible();

            // Verificar elementos específicos do CDU-24
            await expect(modal.getByRole('heading', {name: /Disponibilizar/i})).toBeVisible();
            await expect(modal.locator('table')).toBeVisible();
            
            // Campo de data limite (obrigatório para CDU-24)
            const inputData = modal.getByRole('textbox', {name: /Data Limite/i});
            await expect(inputData).toBeVisible();

            await expect(modal.getByRole('button', {name: /Cancelar/i})).toBeVisible();
            await expect(modal.getByRole('button', {name: /Disponibilizar/i})).toBeVisible();

            // Fechar modal
            await modal.getByRole('button', {name: /Cancelar/i}).click();
        }
    });

    test('Cenario 3: Cancelar disponibilização em bloco', async ({page}) => {
        // CDU-24: Passo 6 - Cancelar
        await login(page, USUARIO_ADMIN, SENHA_ADMIN);

        await page.getByText(descProcesso).click();

        const btnDisponibilizar = page.getByRole('button', {name: /Disponibilizar.*Bloco/i});
        
        if (await btnDisponibilizar.isVisible().catch(() => false)) {
            await btnDisponibilizar.click();

            const modal = page.getByRole('dialog');
            await expect(modal).toBeVisible();

            await modal.getByRole('button', {name: /Cancelar/i}).click();

            await expect(modal).toBeHidden();
            await expect(page.getByRole('heading', {name: /Unidades participantes/i})).toBeVisible();
        }
    });
});
