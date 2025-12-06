import { expect, Page, test } from '@playwright/test';
import { login, USUARIOS } from './helpers/helpers-auth';
import { criarProcesso } from './helpers/helpers-processos';
import { adicionarAtividade, adicionarConhecimento, navegarParaAtividades } from './helpers/helpers-atividades';
import { resetDatabase, useProcessoCleanup } from './hooks/hooks-limpeza';

async function fazerLogout(page: Page) {
    await page.getByTestId('btn-logout').click();
    await expect(page).toHaveURL(/\/login/);
}

async function verificarPaginaPainel(page: Page) {
    await expect(page).toHaveURL(/\/painel/);
}

async function verificarPaginaSubprocesso(page: Page, unidade: string) {
    await expect(page).toHaveURL(new RegExp(String.raw`/processo/\d+/${unidade}$`));
}

test.describe.serial('CDU-11 - Visualizar cadastro de atividades e conhecimentos', () => {
    const UNIDADE_ALVO = 'SECAO_221';
    const USUARIO_CHEFE = USUARIOS.CHEFE_SECAO_221.titulo;
    const SENHA_CHEFE = USUARIOS.CHEFE_SECAO_221.senha;
    const USUARIO_ADMIN = USUARIOS.ADMIN_1_PERFIL.titulo;
    const SENHA_ADMIN = USUARIOS.ADMIN_1_PERFIL.senha;

    const timestamp = Date.now();
    const descProcessoMapeamento = `Mapeamento CDU-11 ${timestamp}`;
    const atividadeA = `Atividade A ${timestamp}`;
    const atividadeB = `Atividade B ${timestamp}`;
    const conhecimento1 = 'Conhecimento 1';
    const conhecimento2 = 'Conhecimento 2';
    const conhecimento3 = 'Conhecimento 3';

    let processoMapeamentoId: number;
    let cleanup: ReturnType<typeof useProcessoCleanup>;

    test.beforeAll(async ({ request }) => {
        await resetDatabase(request);
        cleanup = useProcessoCleanup();
    });

    test.afterAll(async ({ request }) => {
        await cleanup.limpar(request);
    });

    // ========================================================================
    // PREPARAÇÃO - Criar processo de mapeamento com atividades disponibilizadas
    // ========================================================================

    test('Preparacao 1: Admin cria e inicia processo de mapeamento', async ({ page }) => {
        await page.goto('/login');
        await login(page, USUARIO_ADMIN, SENHA_ADMIN);

        await criarProcesso(page, {
            descricao: descProcessoMapeamento,
            tipo: 'MAPEAMENTO',
            diasLimite: 30,
            unidade: UNIDADE_ALVO,
            expandir: ['SECRETARIA_2', 'COORD_22']
        });

        // Selecionar o processo na tabela para obter o ID
        const linhaProcesso = page.locator('tr', { has: page.getByText(descProcessoMapeamento) });
        await linhaProcesso.click();

        await expect(page.getByTestId('inp-processo-descricao')).toHaveValue(descProcessoMapeamento);
        await expect(page.getByText('Carregando unidades...')).toBeHidden();

        // Capturar ID do processo para cleanup
        processoMapeamentoId = Number.parseInt(new RegExp(/\/processo\/cadastro\/(\d+)/).exec(page.url())?.[1] || '0');
        if (processoMapeamentoId > 0) cleanup.registrar(processoMapeamentoId);

        // Iniciar processo
        await page.getByTestId('btn-processo-iniciar').click();
        await page.getByTestId('btn-iniciar-processo-confirmar').click();

        await verificarPaginaPainel(page);
    });

    test('Preparacao 2: Chefe adiciona atividades e conhecimentos, e disponibiliza cadastro', async ({ page }) => {
        await page.goto('/login');
        await login(page, USUARIO_CHEFE, SENHA_CHEFE);

        await page.getByText(descProcessoMapeamento).click();
        if (new RegExp(/\/processo\/\d+$/).exec(page.url())) {
            await page.getByRole('row', { name: 'Seção 221' }).click();
        }

        await verificarPaginaSubprocesso(page, UNIDADE_ALVO);
        await navegarParaAtividades(page);

        // Adicionar primeira atividade com dois conhecimentos
        await adicionarAtividade(page, atividadeA);
        await adicionarConhecimento(page, atividadeA, conhecimento1);
        await adicionarConhecimento(page, atividadeA, conhecimento2);

        // Adicionar segunda atividade com um conhecimento
        await adicionarAtividade(page, atividadeB);
        await adicionarConhecimento(page, atividadeB, conhecimento3);

        // Disponibilizar cadastro
        await page.getByTestId('btn-cad-atividades-disponibilizar').click();
        await page.getByTestId('btn-confirmar-disponibilizacao').click();

        await expect(page.getByRole('heading', { name: /Cadastro de atividades disponibilizado/i })).toBeVisible();
        await verificarPaginaPainel(page);
    });

    // ========================================================================
    // TESTES PRINCIPAIS - CDU-11
    // ========================================================================

    test('Cenario 1: ADMIN visualiza cadastro clicando na unidade subordinada', async ({ page }) => {
        // Fluxo principal passo 2 - ADMIN/GESTOR: 
        // 2.1 Sistema mostra tela Detalhes do processo
        // 2.2 Usuário clica em unidade subordinada operacional/interoperacional
        // 2.3 Sistema mostra tela Detalhes do subprocesso

        await page.goto('/login');
        await login(page, USUARIO_ADMIN, SENHA_ADMIN);

        // Passo 1: Clicar no processo em andamento
        await page.getByText(descProcessoMapeamento).click();

        // Passo 2.2: Clicar na unidade subordinada
        await page.getByRole('row', { name: 'Seção 221' }).click();

        // Passo 4: Clicar no card Atividades e conhecimentos
        await page.getByTestId('card-subprocesso-atividades-vis').click();

        // Passo 5 e 6: Verificar tela de atividades com dados da unidade
        await expect(page.getByRole('heading', { name: 'Atividades e conhecimentos' })).toBeVisible();

        // Verificar sigla da unidade está visível
        await expect(page.getByText('Seção 221')).toBeVisible();

        // Verificar que as atividades estão apresentadas como tabelas
        // Cada atividade aparece com descrição como cabeçalho
        await expect(page.getByText(atividadeA)).toBeVisible();
        await expect(page.getByText(atividadeB)).toBeVisible();

        // Verificar que os conhecimentos estão listados dentro das atividades
        await expect(page.getByText(conhecimento1)).toBeVisible();
        await expect(page.getByText(conhecimento2)).toBeVisible();
        await expect(page.getByText(conhecimento3)).toBeVisible();
    });

    test('Cenario 2: CHEFE visualiza cadastro diretamente (sem navegar por unidades)', async ({ page }) => {
        // Fluxo principal passo 3 - CHEFE/SERVIDOR:
        // 3.1 Sistema exibe diretamente a tela Detalhes do subprocesso

        await page.goto('/login');
        await login(page, USUARIO_CHEFE, SENHA_CHEFE);

        // Passo 1: Clicar no processo em andamento
        await page.getByText(descProcessoMapeamento).click();

        // Verifica que foi diretamente para o subprocesso (sem passar pela lista de unidades)
        // Para Chefe, o sistema vai direto para a tela do subprocesso
        await verificarPaginaSubprocesso(page, UNIDADE_ALVO);

        // Passo 4: Clicar no card Atividades e conhecimentos
        await page.getByTestId('card-subprocesso-atividades-vis').click();

        // Passo 5 e 6: Verificar tela de atividades
        await expect(page.getByRole('heading', { name: 'Atividades e conhecimentos' })).toBeVisible();

        // Verificar sigla da unidade
        await expect(page.getByText('Seção 221')).toBeVisible();

        // Verificar atividades aparecem como tabelas com descrição
        await expect(page.getByText(atividadeA)).toBeVisible();
        await expect(page.getByText(atividadeB)).toBeVisible();

        // Verificar conhecimentos dentro das atividades
        await expect(page.getByText(conhecimento1)).toBeVisible();
        await expect(page.getByText(conhecimento2)).toBeVisible();
        await expect(page.getByText(conhecimento3)).toBeVisible();
    });

    test('Cenario 3: Visualizar processo finalizado', async ({ page }) => {
        // Preparar: Admin homologa o cadastro
        await page.goto('/login');
        await login(page, USUARIO_ADMIN, SENHA_ADMIN);

        await page.getByText(descProcessoMapeamento).click();
        await page.getByRole('row', { name: 'Seção 221' }).click();

        // Aceitar cadastro
        await page.getByTestId('card-subprocesso-atividades-vis').click();
        await page.getByTestId('btn-acao-analisar-principal').click();
        await page.getByTestId('btn-aceite-cadastro-confirmar').click();
        await verificarPaginaPainel(page);

        // Adicionar competências e disponibilizar mapa
        await page.getByText(descProcessoMapeamento).click();
        await page.getByRole('row', { name: 'Seção 221' }).click();
        await page.locator('[data-testid="card-subprocesso-mapa"], [data-testid="card-subprocesso-mapa-vis"]').first().click();

        await page.getByTestId('btn-abrir-criar-competencia').click();
        await page.getByTestId('inp-criar-competencia-descricao').fill(`Competência 1 ${timestamp}`);
        await page.getByText(atividadeA).click();
        await page.getByTestId('btn-criar-competencia-salvar').click();
        await expect(page.getByTestId('mdl-criar-competencia')).toBeHidden();

        // Adicionar competência para a segunda atividade (obrigatório)
        await page.getByTestId('btn-abrir-criar-competencia').click();
        await page.getByTestId('inp-criar-competencia-descricao').fill(`Competência 2 ${timestamp}`);
        await page.getByText(atividadeB).click();
        await page.getByTestId('btn-criar-competencia-salvar').click();
        await expect(page.getByTestId('mdl-criar-competencia')).toBeHidden();

        await page.getByTestId('btn-cad-mapa-disponibilizar').click();
        await page.getByTestId('inp-disponibilizar-mapa-data').fill('2030-12-31');
        await page.getByTestId('btn-disponibilizar-mapa-confirmar').click();
        await expect(page.getByTestId('mdl-disponibilizar-mapa')).toBeHidden();

        // Chefe valida mapa
        await fazerLogout(page);
        await login(page, USUARIO_CHEFE, SENHA_CHEFE);

        await page.getByText(descProcessoMapeamento).click();
        await page.getByTestId('card-subprocesso-mapa-vis').click();
        await page.getByTestId('btn-mapa-validar').click();
        await page.getByTestId('btn-validar-mapa-confirmar').click();

        // Admin homologa mapa e finaliza processo
        await fazerLogout(page);
        await login(page, USUARIO_ADMIN, SENHA_ADMIN);

        await page.getByText(descProcessoMapeamento).click();
        await page.getByRole('row', { name: 'Seção 221' }).click();
        await page.getByTestId('card-subprocesso-mapa-vis').click();
        await page.getByTestId('btn-mapa-homologar-aceite').click();
        await page.getByTestId('btn-aceite-mapa-confirmar').click();

        await page.goto('/painel');
        await page.getByText(descProcessoMapeamento).click();
        await page.getByTestId('btn-processo-finalizar').click();
        await page.getByTestId('btn-finalizar-processo-confirmar').click();
        await verificarPaginaPainel(page);

        // TESTE: Visualizar cadastro de processo finalizado
        await page.getByText(descProcessoMapeamento).click();

        // Verificar que estamos na página de detalhes do processo
        await expect(page).toHaveURL(/\/processo\/\d+$/);

        // Verificar que a tabela de unidades está visível
        await expect(page.getByRole('heading', { name: /Unidades participantes/i })).toBeVisible();

        // DEBUG: verificar quantas linhas existem na tabela  
        const rows = await page.locator('tr').count();
        console.log('Número de linhas na tabela:', rows);

        // Verificar que a linha existe antes de clicar
        const linhaUnidade = page.getByRole('row', { name: 'Seção 221' });
        const linhaVisivel = await linhaUnidade.isVisible();
        console.log('Linha visível:', linhaVisivel);

        if (!linhaVisivel) {
            // DEBUG: mostrar conteúdo da tabela
            const tableContent = await page.locator('table').first().textContent();
            console.log('Conteúdo da tabela:', tableContent);
        }

        await expect(linhaUnidade).toBeVisible({ timeout: 5000 });

        // DEBUG: verificar URL antes do clique
        console.log('URL antes do clique na linha:', page.url());

        // Clicar na linha
        await linhaUnidade.click();

        // Pequena espera para navegação
        await page.waitForTimeout(500);

        // DEBUG: verificar a URL atual (deve ter mudado)
        console.log('URL após clicar na unidade:', page.url());

        // Esperar navegação para o subprocesso
        await verificarPaginaSubprocesso(page, UNIDADE_ALVO);

        // O card deve estar visível para visualização em processo finalizado
        await page.getByTestId('card-subprocesso-atividades-vis').click();

        await expect(page.getByRole('heading', { name: 'Atividades e conhecimentos' })).toBeVisible();
        await expect(page.getByText(atividadeA)).toBeVisible();
        await expect(page.getByText(atividadeB)).toBeVisible();
    });

    test('Cenario 4: CHEFE visualiza cadastro de processo finalizado', async ({ page }) => {
        // Mesmo cenário mas com perfil CHEFE - deve ir direto para subprocesso
        await page.goto('/login');
        await login(page, USUARIO_CHEFE, SENHA_CHEFE);

        // Clicar no processo finalizado
        await page.getByText(descProcessoMapeamento).click();

        // Verificar que foi diretamente para subprocesso (perfil CHEFE)
        await verificarPaginaSubprocesso(page, UNIDADE_ALVO);

        // Visualizar atividades
        await page.getByTestId('card-subprocesso-atividades-vis').click();

        await expect(page.getByRole('heading', { name: 'Atividades e conhecimentos' })).toBeVisible();
        await expect(page.getByText(atividadeA)).toBeVisible();
        await expect(page.getByText(conhecimento1)).toBeVisible();
        await expect(page.getByText(conhecimento2)).toBeVisible();
    });
});
