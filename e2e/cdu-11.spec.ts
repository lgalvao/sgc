import {expect, test} from './fixtures/base';
import {login, USUARIOS} from './helpers/helpers-auth';
import {criarProcesso} from './helpers/helpers-processos';
import {adicionarAtividade, adicionarConhecimento, navegarParaAtividades} from './helpers/helpers-atividades';
import {abrirModalCriarCompetencia} from './helpers/helpers-mapas';
import {fazerLogout, navegarParaSubprocesso, verificarPaginaPainel} from './helpers/helpers-navegacao';
import {resetDatabase, useProcessoCleanup} from './hooks/hooks-limpeza';
import type {Page} from '@playwright/test';

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
    const descProcessoMapeamento = `Map 11 ${timestamp}`;
    const atividadeA = `Atividade A ${timestamp}`;
    const atividadeB = `Atividade B ${timestamp}`;
    const conhecimento1 = 'Conhecimento 1';
    const conhecimento2 = 'Conhecimento 2';
    const conhecimento3 = 'Conhecimento 3';

    let processoMapeamentoId: number;
    let cleanup: ReturnType<typeof useProcessoCleanup>;

    test.beforeAll(async ({request}) => {
        await resetDatabase(request);
        cleanup = useProcessoCleanup();
    });

    test.afterAll(async ({request}) => {
        await cleanup.limpar(request);
    });

    // ========================================================================
    // PREPARAÇÃO - Criar processo de mapeamento com atividades disponibilizadas
    // ========================================================================

    test('Preparacao 1: Admin cria e inicia processo de mapeamento', async ({page}) => {
        await login(page, USUARIO_ADMIN, SENHA_ADMIN);

        await criarProcesso(page, {
            descricao: descProcessoMapeamento,
            tipo: 'MAPEAMENTO',
            diasLimite: 30,
            unidade: UNIDADE_ALVO,
            expandir: ['SECRETARIA_2', 'COORD_22']
        });

        // Selecionar o processo na tabela para obter o ID

        const linhaProcesso = page.locator('tr').filter({has: page.getByText(descProcessoMapeamento)});

        await linhaProcesso.click();
        await expect(page.getByTestId('inp-processo-descricao')).toHaveValue(descProcessoMapeamento);
        await expect(page.getByText('Carregando unidades...')).toBeHidden();
        processoMapeamentoId = Number.parseInt(new RegExp(/\/processo\/cadastro\/(\d+)/).exec(page.url())?.[1] || '0');
        if (processoMapeamentoId > 0) cleanup.registrar(processoMapeamentoId);

        // Iniciar processo
        await page.getByTestId('btn-processo-iniciar').click();
        await page.getByTestId('btn-iniciar-processo-confirmar').click();

        await verificarPaginaPainel(page);
    });

    test('Preparacao 2: Chefe adiciona atividades e conhecimentos, e disponibiliza cadastro', async ({page}) => {
        await login(page, USUARIO_CHEFE, SENHA_CHEFE);

        await page.getByText(descProcessoMapeamento).click();
        await page.getByText(descProcessoMapeamento).click();

        // Chefe vai direto para subprocesso
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

        await expect(page.getByRole('heading', {name: /Cadastro de atividades disponibilizado/i})).toBeVisible();
        await verificarPaginaPainel(page);
    });

    // ========================================================================
    // TESTES PRINCIPAIS - CDU-11
    // ========================================================================

    test('Cenario 1: ADMIN visualiza cadastro clicando na unidade subordinada', async ({page}) => {
        // Fluxo principal passo 2 - ADMIN/GESTOR: 
        // 2.1 Sistema mostra tela Detalhes do processo
        // 2.2 Usuário clica em unidade subordinada operacional/interoperacional
        // 2.3 Sistema mostra tela Detalhes do subprocesso

        await login(page, USUARIO_ADMIN, SENHA_ADMIN);

        await expect(page.getByText(descProcessoMapeamento)).toBeVisible();
        await page.getByText(descProcessoMapeamento).click();

        // Aguardar página de Detalhes do processo carregar
        await expect(page).toHaveURL(/\/processo\/\d+$/);
        await expect(page.getByRole('heading', {name: /Unidades participantes/i})).toBeVisible();
        
        // Passo 2.2: Clicar na unidade subordinada - esperar a tabela de dados carregar
        const tabela = page.getByTestId('tbl-tree');
        await expect(tabela).toBeVisible();
        
        // Clicar na célula SECAO_221 dentro da tabela de unidades
        const celula = tabela.getByRole('cell', {name: 'SECAO_221'}).first();
        await expect(celula).toBeVisible();
        await celula.click();
        
        // Aguardar navegação para Detalhes do subprocesso
        await expect(page).toHaveURL(/\/processo\/\d+\/\w+$/);
        
        // Passo 2.3: Verificar visualização
        await page.getByTestId('card-subprocesso-atividades-vis').click();
        await expect(page.getByRole('heading', {name: 'Atividades e conhecimentos', exact: true})).toBeVisible();

        // Passo 5 e 6: Verificar tela de atividades com dados da unidade
        await expect(page.getByRole('heading', {name: 'Atividades e conhecimentos'})).toBeVisible();

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

    test('Cenario 2: CHEFE visualiza cadastro diretamente (sem navegar por unidades)', async ({page}) => {
        // Fluxo principal passo 3 - CHEFE/SERVIDOR:
        // 3.1 Sistema exibe diretamente a tela Detalhes do subprocesso
        await login(page, USUARIO_CHEFE, SENHA_CHEFE);

        // Passo 1: Clicar no processo em andamento
        await page.getByText(descProcessoMapeamento).click();

        // Verifica que foi diretamente para o subprocesso (sem passar pela lista de unidades)
        await verificarPaginaSubprocesso(page, UNIDADE_ALVO);

        // Passo 4: Clicar no card Atividades e conhecimentos
        await page.getByTestId('card-subprocesso-atividades-vis').click();

        // Passo 5 e 6: Verificar tela de atividades
        await expect(page.getByRole('heading', {name: 'Atividades e conhecimentos'})).toBeVisible();

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

    test('Cenario 3: Visualizar processo finalizado', async ({page}) => {
        // Preparar: Admin homologa o cadastro
        await login(page, USUARIO_ADMIN, SENHA_ADMIN);

        await expect(page.getByText(descProcessoMapeamento)).toBeVisible();
        await page.getByText(descProcessoMapeamento).click();
        await navegarParaSubprocesso(page, 'SECAO_221');
        // Aceitar cadastro
        await page.getByTestId('card-subprocesso-atividades-vis').click();
        await page.getByTestId('btn-acao-analisar-principal').click();
        await page.getByTestId('btn-aceite-cadastro-confirmar').click();

        // Após homologação, redireciona para Detalhes do subprocesso (CDU-13 passo 11.7)
        await expect(page).toHaveURL(/\/processo\/\d+\/\w+$/);

        // Adicionar competências e disponibilizar mapa
        // Já está na tela de Detalhes do subprocesso
        await page.locator('[data-testid="card-subprocesso-mapa"], [data-testid="card-subprocesso-mapa"]').first().click();

        await abrirModalCriarCompetencia(page);
        await page.getByTestId('inp-criar-competencia-descricao').fill(`Competência 1 ${timestamp}`);
        await page.getByText(atividadeA).click();
        await page.getByTestId('btn-criar-competencia-salvar').click();
        await expect(page.getByTestId('mdl-criar-competencia')).toBeHidden();

        // Adicionar competência para a segunda atividade (obrigatório)
        await abrirModalCriarCompetencia(page);
        await page.getByTestId('inp-criar-competencia-descricao').fill(`Competência 2 ${timestamp}`);
        await page.getByText(atividadeB).click();
        await page.getByTestId('btn-criar-competencia-salvar').click();
        await expect(page.getByTestId('mdl-criar-competencia')).toBeHidden();

        await page.getByTestId('btn-cad-mapa-disponibilizar').click();
        await page.getByTestId('inp-disponibilizar-mapa-data').fill('2030-12-31');
        await page.getByTestId('btn-disponibilizar-mapa-confirmar').click();
        await expect(page.getByTestId('mdl-disponibilizar-mapa')).toBeHidden();

        // Aguardar redirecionamento para o painel e verificar mensagem de sucesso
        await verificarPaginaPainel(page);
        await expect(page.getByRole('heading', {name: /Mapa disponibilizado/i})).toBeVisible();

        // Chefe valida mapa
        await fazerLogout(page);
        await login(page, USUARIO_CHEFE, SENHA_CHEFE);

        await page.getByText(descProcessoMapeamento).click();
        await page.getByTestId('card-subprocesso-mapa').click();
        await page.getByTestId('btn-mapa-validar').click();
        await page.getByTestId('btn-validar-mapa-confirmar').click();
        await expect(page.getByRole('heading', {name: /Mapa validado/i})).toBeVisible();

        // Admin homologa mapa e finaliza processo
        await fazerLogout(page);
        await login(page, USUARIO_ADMIN, SENHA_ADMIN);

        await page.getByText(descProcessoMapeamento).click();
        await navegarParaSubprocesso(page, 'SECAO_221');
        await page.getByTestId('card-subprocesso-mapa').click();
        await page.getByTestId('btn-mapa-homologar-aceite').click();
        await page.getByTestId('btn-aceite-mapa-confirmar').click();

        await page.goto('/painel');
        await page.getByText(descProcessoMapeamento).click();
        await page.getByTestId('btn-processo-finalizar').click();
        await page.getByTestId('btn-finalizar-processo-confirmar').click();
        await verificarPaginaPainel(page);
        await page.getByText(descProcessoMapeamento).click();

        // Verificar que estamos na página de detalhes do processo
        await expect(page).toHaveURL(/\/processo\/\d+$/);

        // Verificar que a tabela de unidades está visível
        await expect(page.getByRole('heading', {name: /Unidades participantes/i})).toBeVisible();

        // Clicar na linha da unidade
        await navegarParaSubprocesso(page, 'SECAO_221');

        // O card deve estar visível para visualização em processo finalizado
        await page.getByTestId('card-subprocesso-atividades-vis').click();

        await expect(page.getByRole('heading', {name: 'Atividades e conhecimentos'})).toBeVisible();
        await expect(page.getByText(atividadeA)).toBeVisible();
        await expect(page.getByText(atividadeB)).toBeVisible();
    });

    test('Cenario 4: CHEFE visualiza cadastro de processo finalizado', async ({page}) => {
        // Mesmo cenário mas com perfil CHEFE - deve ir direto para subprocesso
        await login(page, USUARIO_CHEFE, SENHA_CHEFE);

        // Clicar no processo finalizado
        await page.getByText(descProcessoMapeamento).click();

        // Verificar que foi diretamente para subprocesso (perfil CHEFE)
        await verificarPaginaSubprocesso(page, UNIDADE_ALVO);

        // Visualizar atividades
        await page.getByTestId('card-subprocesso-atividades-vis').click();

        await expect(page.getByRole('heading', {name: 'Atividades e conhecimentos'})).toBeVisible();
        await expect(page.getByText(atividadeA)).toBeVisible();
        await expect(page.getByText(conhecimento1)).toBeVisible();
        await expect(page.getByText(conhecimento2)).toBeVisible();
    });
});
