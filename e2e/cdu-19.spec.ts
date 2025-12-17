import {expect, Page, test} from '@playwright/test';
import {login, USUARIOS} from './helpers/helpers-auth';
import {criarProcesso} from './helpers/helpers-processos';
import {adicionarAtividade, adicionarConhecimento, navegarParaAtividades} from './helpers/helpers-atividades';
import {criarCompetencia, disponibilizarMapa, navegarParaMapa} from './helpers/helpers-mapas';
import {resetDatabase, useProcessoCleanup} from './hooks/hooks-limpeza';

async function verificarPaginaPainel(page: Page) {
    await expect(page).toHaveURL(/\/painel/);
}

test.describe.serial('CDU-19 - Validar mapa de competências', () => {
    const UNIDADE_ALVO = 'SECAO_221';
    const USUARIO_CHEFE = USUARIOS.CHEFE_SECAO_221.titulo;
    const SENHA_CHEFE = USUARIOS.CHEFE_SECAO_221.senha;
    const USUARIO_ADMIN = USUARIOS.ADMIN_1_PERFIL.titulo;
    const SENHA_ADMIN = USUARIOS.ADMIN_1_PERFIL.senha;

    const timestamp = Date.now();
    const descProcesso = `Mapeamento CDU-19 ${timestamp}`;
    let processoId: number;
    let cleanup: ReturnType<typeof useProcessoCleanup>;

    // Atividades e competências para os testes
    const atividade1 = `Atividade 1 ${timestamp}`;
    const atividade2 = `Atividade 2 ${timestamp}`;
    const competencia1 = `Competência 1 ${timestamp}`;
    const competencia2 = `Competência 2 ${timestamp}`;

    test.beforeAll(async ({request}) => {
        await resetDatabase(request);
        cleanup = useProcessoCleanup();
    });

    test.afterAll(async ({request}) => {
        await cleanup.limpar(request);
    });

    // ========================================================================
    // PREPARAÇÃO - Criar mapa disponibilizado para CHEFE validar
    // ========================================================================

    test('Preparacao 1: Admin cria e inicia processo de mapeamento', async ({page}) => {
        await page.goto('/login');
        await login(page, USUARIO_ADMIN, SENHA_ADMIN);

        await criarProcesso(page, {
            descricao: descProcesso,
            tipo: 'MAPEAMENTO',
            diasLimite: 30,
            unidade: UNIDADE_ALVO,
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
        await page.goto('/login');
        await login(page, USUARIO_CHEFE, SENHA_CHEFE);

        await page.getByText(descProcesso).click();
        await navegarParaAtividades(page);

        await adicionarAtividade(page, atividade1);
        await adicionarConhecimento(page, atividade1, 'Conhecimento 1A');

        await adicionarAtividade(page, atividade2);
        await adicionarConhecimento(page, atividade2, 'Conhecimento 2A');

        await page.getByTestId('btn-cad-atividades-disponibilizar').click();
        await page.getByTestId('btn-confirmar-disponibilizacao').click();

        await expect(page.getByRole('heading', {name: /Cadastro de atividades disponibilizado/i})).toBeVisible();
        await verificarPaginaPainel(page);
    });

    test('Preparacao 3: Admin homologa cadastro', async ({page}) => {
        await page.goto('/login');
        await login(page, USUARIO_ADMIN, SENHA_ADMIN);

        await page.getByText(descProcesso).click();
                    await page.getByRole('row', {name: 'SECAO_221'}).click();        await page.getByTestId('card-subprocesso-atividades-vis').click();
        await page.getByTestId('btn-acao-analisar-principal').click();
        await page.getByTestId('btn-aceite-cadastro-confirmar').click();

        await expect(page).toHaveURL(/\/processo\/\d+\/\w+$/);
    });

    test('Preparacao 4: Admin cria competências e disponibiliza mapa', async ({page}) => {
        await page.goto('/login');
        await login(page, USUARIO_ADMIN, SENHA_ADMIN);

        await page.getByText(descProcesso).click();
                    await page.getByRole('row', {name: 'SECAO_221'}).click();
        await navegarParaMapa(page);

        // Criar competências cobrindo todas as atividades
        await criarCompetencia(page, competencia1, [atividade1]);
        await criarCompetencia(page, competencia2, [atividade2]);

        // Disponibilizar mapa
        await disponibilizarMapa(page, '2030-12-31');

        await verificarPaginaPainel(page);
        await expect(page.getByRole('heading', {name: /Mapa disponibilizado/i})).toBeVisible();
    });

    // ========================================================================
    // TESTES PRINCIPAIS - CDU-19
    // ========================================================================

    test('Cenario 1: CHEFE navega para visualização do mapa', async ({page}) => {
        // CDU-19: Passos 1-2
        await page.goto('/login');
        await login(page, USUARIO_CHEFE, SENHA_CHEFE);

        // Passo 1: CHEFE escolhe o processo
        await expect(page.getByText(descProcesso)).toBeVisible();
        await page.getByText(descProcesso).click();

        // Verificar situação do subprocesso
        await expect(page.getByTestId('subprocesso-header__txt-badge-situacao'))
            .toHaveText(/Mapa disponibilizado/i);

        // Passo 1: Clica no card Mapa de competências
        await page.getByTestId('card-subprocesso-mapa').click();

        // Passo 2: Sistema mostra tela de Visualização de mapa
        await expect(page.getByText('Mapa de competências técnicas')).toBeVisible();

        // Botões disponíveis para CHEFE
        await expect(page.getByTestId('btn-mapa-sugestoes')).toBeVisible();
        await expect(page.getByTestId('btn-mapa-validar')).toBeVisible();
    });

    test('Cenario 2: CHEFE cancela validação - permanece na tela', async ({page}) => {
        // CDU-19: Passo 5.1.1
        await page.goto('/login');
        await login(page, USUARIO_CHEFE, SENHA_CHEFE);

        await page.getByText(descProcesso).click();
        await page.getByTestId('card-subprocesso-mapa').click();

        // Clicar em Validar
        await page.getByTestId('btn-mapa-validar').click();

        // Modal de confirmação (Passo 5.1)
        const modal = page.getByRole('dialog');
        await expect(modal).toBeVisible();
        await expect(modal.getByText(/Confirma a validação/i)).toBeVisible();

        // Cancelar usando testid específico
        await page.getByTestId('btn-validar-mapa-cancelar').click();

        // Permanece na tela de visualização (Passo 5.1.1)
        await expect(page.getByText('Mapa de competências técnicas')).toBeVisible();
        await expect(page.getByTestId('btn-mapa-validar')).toBeVisible();
    });

    test('Cenario 3: CHEFE valida mapa com sucesso', async ({page}) => {
        // CDU-19: Passos 5.2-5.6, 6-8
        await page.goto('/login');
        await login(page, USUARIO_CHEFE, SENHA_CHEFE);

        await page.getByText(descProcesso).click();
        await page.getByTestId('card-subprocesso-mapa').click();

        // Passo 5: Clicar em Validar
        await page.getByTestId('btn-mapa-validar').click();

        // Passo 5.1: Modal de confirmação
        const modal = page.getByRole('dialog');
        await expect(modal).toBeVisible();

        // Passo 5.2: Confirmar
        await page.getByTestId('btn-validar-mapa-confirmar').click();

        // Passo 5.6 e 8: Mensagem de sucesso e redirecionamento
        await verificarPaginaPainel(page);
        await expect(page.getByRole('heading', {name: /Mapa validado/i})).toBeVisible();

        // Verificar mudança de situação (Passo 5.3)
        await page.getByText(descProcesso).click();
        await expect(page.getByTestId('subprocesso-header__txt-badge-situacao'))
            .toHaveText(/Mapa validado/i);
    });
});
