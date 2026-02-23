import type {Page} from '@playwright/test';
import {expect, test} from './fixtures/complete-fixtures.js';
import {login, loginComPerfil, USUARIOS} from './helpers/helpers-auth.js';
import {criarProcesso, extrairProcessoId} from './helpers/helpers-processos.js';
import {
    adicionarAtividade,
    adicionarConhecimento,
    navegarParaAtividades,
    navegarParaAtividadesVisualizacao
} from './helpers/helpers-atividades.js';
import {acessarSubprocessoChefeDireto, acessarSubprocessoGestor} from './helpers/helpers-analise.js';
import {abrirModalCriarCompetencia, navegarParaMapa} from './helpers/helpers-mapas.js';
import {fazerLogout, navegarParaSubprocesso, verificarPaginaPainel} from './helpers/helpers-navegacao.js';

async function verificarPaginaSubprocesso(page: Page, unidade: string) {
    await expect(page).toHaveURL(new RegExp(String.raw`/processo/\d+/${unidade}$`));
}

test.describe.serial('CDU-11 - Visualizar cadastro de atividades e conhecimentos', () => {
    const UNIDADE_ALVO = 'SECAO_211';
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

    // ========================================================================
    // PREPARAÇÃO - Criar processo de mapeamento com atividades disponibilizadas
    // ========================================================================

    test('Preparacao 1: Admin cria e inicia processo de mapeamento', async ({page, autenticadoComoAdmin, cleanupAutomatico}) => {
        

        await criarProcesso(page, {
            descricao: descProcessoMapeamento,
            tipo: 'MAPEAMENTO',
            diasLimite: 30,
            unidade: UNIDADE_ALVO,
            expandir: ['SECRETARIA_2', 'COORD_21']
        });

        // Selecionar o processo na tabela para obter o ID

        const linhaProcesso = page.getByTestId('tbl-processos').locator('tr').filter({has: page.getByText(descProcessoMapeamento)});

        await linhaProcesso.click();
        await expect(page.getByTestId('inp-processo-descricao')).toHaveValue(descProcessoMapeamento);
        await expect(page.getByText('Carregando unidades...')).toBeHidden();
        processoMapeamentoId = await extrairProcessoId(page);
        if (processoMapeamentoId > 0) cleanupAutomatico.registrar(processoMapeamentoId);

        // Iniciar processo
        await page.getByTestId('btn-processo-iniciar').click();
        await page.getByTestId('btn-iniciar-processo-confirmar').click();

        await verificarPaginaPainel(page);
    });

    test('Preparacao 2: Chefe adiciona atividades e conhecimentos, e disponibiliza cadastro', async ({page, autenticadoComoChefeSecao211}) => {
        

        await acessarSubprocessoChefeDireto(page, descProcessoMapeamento, UNIDADE_ALVO);

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

        await expect(page.getByText(/Cadastro de atividades disponibilizado/i).first()).toBeVisible();
        await verificarPaginaPainel(page);
    });

    // ========================================================================
    // TESTES PRINCIPAIS - CDU-11
    // ========================================================================

    test('Cenario 1: ADMIN visualiza cadastro clicando na unidade subordinada', async ({page, autenticadoComoAdmin}) => {
        // Fluxo principal passo 2 - ADMIN/GESTOR: 
        // 2.1 Sistema mostra tela Detalhes do processo
        // 2.2 Usuário clica em unidade subordinada operacional/interoperacional
        // 2.3 Sistema mostra tela Detalhes do subprocesso

        

        await expect(page.getByTestId('tbl-processos').getByText(descProcessoMapeamento).first()).toBeVisible();
        await page.getByTestId('tbl-processos').getByText(descProcessoMapeamento).first().click();

        // Aguardar página de Detalhes do processo carregar
        await expect(page).toHaveURL(/\/processo\/\d+$/);
        await expect(page.getByRole('heading', {name: /Unidades participantes/i})).toBeVisible();
        
        // Passo 2.2: Clicar na unidade subordinada - esperar a tabela de dados carregar
        const tabela = page.getByTestId('tbl-tree');
        await expect(tabela).toBeVisible();
        
        // Clicar na célula SECAO_211 dentro da tabela de unidades
        const celula = tabela.getByRole('cell', {name: 'SECAO_211'}).first();
        await expect(celula).toBeVisible();
        await celula.click();
        
        // Aguardar navegação para Detalhes do subprocesso
        await expect(page).toHaveURL(/\/processo\/\d+\/\w+$/);
        
        // Passo 2.3: Verificar visualização
        await navegarParaAtividadesVisualizacao(page);
        await expect(page.getByRole('heading', {name: 'Atividades e conhecimentos', exact: true})).toBeVisible();

        // Passo 5 e 6: Verificar tela de atividades com dados da unidade
        await expect(page.getByRole('heading', {name: 'Atividades e conhecimentos'})).toBeVisible();

        // Verificar que continua no subprocesso da unidade alvo
        await expect(page).toHaveURL(new RegExp(String.raw`/processo/\d+/${UNIDADE_ALVO}/`));

        // Verificar que as atividades estão apresentadas como tabelas
        // Cada atividade aparece com descrição como cabeçalho
        await expect(page.getByText(atividadeA)).toBeVisible();
        await expect(page.getByText(atividadeB)).toBeVisible();

        // Verificar que os conhecimentos estão listados dentro das atividades
        await expect(page.getByText(conhecimento1)).toBeVisible();
        await expect(page.getByText(conhecimento2)).toBeVisible();
        await expect(page.getByText(conhecimento3)).toBeVisible();
    });

    test('Cenario 2: CHEFE visualiza cadastro diretamente (sem navegar por unidades)', async ({page, autenticadoComoChefeSecao211}) => {
        // Fluxo principal passo 3 - CHEFE/SERVIDOR:
        // 3.1 Sistema exibe diretamente a tela Detalhes do subprocesso
        

        // Passo 1: Clicar no processo em andamento
        await page.getByTestId('tbl-processos').getByText(descProcessoMapeamento).first().click();

        // Verifica que foi diretamente para o subprocesso (sem passar pela lista de unidades)
        await verificarPaginaSubprocesso(page, UNIDADE_ALVO);

        // Passo 4: Clicar no card Atividades e conhecimentos
        await navegarParaAtividadesVisualizacao(page);

        // Passo 5 e 6: Verificar tela de atividades
        await expect(page.getByRole('heading', {name: 'Atividades e conhecimentos'})).toBeVisible();

        // Verificar que continua no subprocesso da unidade alvo
        await expect(page).toHaveURL(new RegExp(String.raw`/processo/\d+/${UNIDADE_ALVO}/`));

        // Verificar atividades aparecem como tabelas com descrição
        await expect(page.getByText(atividadeA)).toBeVisible();
        await expect(page.getByText(atividadeB)).toBeVisible();

        // Verificar conhecimentos dentro das atividades
        await expect(page.getByText(conhecimento1)).toBeVisible();
        await expect(page.getByText(conhecimento2)).toBeVisible();
        await expect(page.getByText(conhecimento3)).toBeVisible();
    });

    test('Cenario 3: Visualizar processo finalizado', async ({page, autenticadoComoGestorCoord21}) => {
        test.setTimeout(90000);
        // Preparar: Admin homologa o cadastro
        

        await acessarSubprocessoGestor(page, descProcessoMapeamento, UNIDADE_ALVO);
        // Aceitar cadastro
        await navegarParaAtividadesVisualizacao(page);
        await page.getByTestId('btn-acao-analisar-principal').click();
        await page.getByTestId('inp-aceite-cadastro-obs').fill('Aceite para finalização do cenário');
        await page.getByTestId('btn-aceite-cadastro-confirmar').click();

        await verificarPaginaPainel(page);

        // Aceite SECRETARIA_2 (Cadastro)
        await loginComPerfil(page, '212121', 'senha', 'GESTOR - SECRETARIA_2');
        await acessarSubprocessoGestor(page, descProcessoMapeamento, UNIDADE_ALVO);
        await navegarParaAtividadesVisualizacao(page);
        await page.getByTestId('btn-acao-analisar-principal').click();
        await page.getByTestId('inp-aceite-cadastro-obs').fill('Aceite Secretaria 2');
        await page.getByTestId('btn-aceite-cadastro-confirmar').click();

        await verificarPaginaPainel(page);
        await fazerLogout(page);
        await login(page, USUARIO_ADMIN, SENHA_ADMIN);
        await page.getByTestId('tbl-processos').getByText(descProcessoMapeamento).first().click();
        await navegarParaSubprocesso(page, UNIDADE_ALVO);
        await navegarParaAtividadesVisualizacao(page);
        await page.getByTestId('btn-acao-analisar-principal').click();
        await page.getByTestId('inp-aceite-cadastro-obs').fill('Homologado para finalização do cenário');
        await page.getByTestId('btn-aceite-cadastro-confirmar').click();
        await page.goto('/painel');
        await page.getByTestId('tbl-processos').getByText(descProcessoMapeamento).first().click();
        await navegarParaSubprocesso(page, UNIDADE_ALVO);

        // Adicionar competências e disponibilizar mapa
        await navegarParaMapa(page);

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

        // Chefe valida mapa
        await fazerLogout(page);
        await login(page, USUARIOS.CHEFE_SECAO_211.titulo, USUARIOS.CHEFE_SECAO_211.senha);

        await page.getByTestId('tbl-processos').getByText(descProcessoMapeamento).first().click();
        await navegarParaMapa(page);
        await page.getByTestId('btn-mapa-validar').click();
        await page.getByTestId('btn-validar-mapa-confirmar').click();
        await expect(page.getByText(/Mapa validado/i).first()).toBeVisible();

        // Aceite COORD_21 (Mapa)
        await login(page, USUARIOS.GESTOR_COORD_21.titulo, USUARIOS.GESTOR_COORD_21.senha);
        await page.getByTestId('tbl-processos').getByText(descProcessoMapeamento).first().click();
        await navegarParaSubprocesso(page, 'SECAO_211');
        await navegarParaMapa(page);
        await page.getByTestId('btn-mapa-homologar-aceite').click();
        await page.getByTestId('btn-aceite-mapa-confirmar').click();
        await verificarPaginaPainel(page);

        // Aceite SECRETARIA_2 (Mapa)
        await loginComPerfil(page, '212121', 'senha', 'GESTOR - SECRETARIA_2');
        await page.getByTestId('tbl-processos').getByText(descProcessoMapeamento).first().click();
        await navegarParaSubprocesso(page, 'SECAO_211');
        await navegarParaMapa(page);
        await page.getByTestId('btn-mapa-homologar-aceite').click();
        await page.getByTestId('btn-aceite-mapa-confirmar').click();
        await verificarPaginaPainel(page);

        // Admin homologa mapa e finaliza processo
        await fazerLogout(page);
        await login(page, USUARIO_ADMIN, SENHA_ADMIN);

        await page.getByTestId('tbl-processos').getByText(descProcessoMapeamento).first().click();
        await navegarParaSubprocesso(page, 'SECAO_211');
        await navegarParaMapa(page);
        await page.getByTestId('btn-mapa-homologar-aceite').click();
        await page.getByTestId('btn-aceite-mapa-confirmar').click();

        await page.goto('/painel');
        await page.getByTestId('tbl-processos').getByText(descProcessoMapeamento).first().click();
        await page.getByTestId('btn-processo-finalizar').click();
        await page.getByTestId('btn-finalizar-processo-confirmar').click();
        await verificarPaginaPainel(page);
        await page.getByTestId('tbl-processos').getByText(descProcessoMapeamento).first().click();

        // Verificar que estamos na página de detalhes do processo
        await expect(page).toHaveURL(/\/processo\/\d+$/);

        // Verificar que a tabela de unidades está visível
        await expect(page.getByRole('heading', {name: /Unidades participantes/i})).toBeVisible();

        // Clicar na linha da unidade
        await navegarParaSubprocesso(page, 'SECAO_211');

        // O card deve estar visível para visualização em processo finalizado
        await navegarParaAtividadesVisualizacao(page);

        await expect(page.getByRole('heading', {name: 'Atividades e conhecimentos'})).toBeVisible();
        await expect(page.getByText(atividadeA)).toBeVisible();
        await expect(page.getByText(atividadeB)).toBeVisible();
    });

    test('Cenario 4: CHEFE visualiza cadastro de processo finalizado', async ({page, autenticadoComoChefeSecao211}) => {
        // Mesmo cenário mas com perfil CHEFE - deve ir direto para subprocesso
        

        // Clicar no processo finalizado
        await page.getByTestId('tbl-processos').getByText(descProcessoMapeamento).first().click();

        // Verificar que foi diretamente para subprocesso (perfil CHEFE)
        await verificarPaginaSubprocesso(page, UNIDADE_ALVO);

        // Visualizar atividades
        await navegarParaAtividadesVisualizacao(page);

        await expect(page.getByRole('heading', {name: 'Atividades e conhecimentos'})).toBeVisible();
        await expect(page.getByText(atividadeA)).toBeVisible();
        await expect(page.getByText(conhecimento1)).toBeVisible();
        await expect(page.getByText(conhecimento2)).toBeVisible();
    });
});
