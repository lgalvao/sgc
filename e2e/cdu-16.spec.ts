import type {Page} from '@playwright/test';
import {expect, test} from './fixtures/complete-fixtures.js';
import {criarProcesso} from './helpers/helpers-processos.js';
import {login, USUARIOS} from './helpers/helpers-auth.js';
import {
    adicionarAtividade,
    adicionarConhecimento,
    editarAtividade,
    navegarParaAtividades,
    navegarParaAtividadesVisualizacao,
    removerAtividade
} from './helpers/helpers-atividades.js';
import {criarCompetencia, disponibilizarMapa, navegarParaMapa} from './helpers/helpers-mapas.js';
import {acessarSubprocessoAdmin, acessarSubprocessoChefeDireto} from './helpers/helpers-analise.js';
import {verificarPaginaPainel} from './helpers/helpers-navegacao.js';
import type {useProcessoCleanup} from './hooks/hooks-limpeza.js';

async function verificarPaginaSubprocesso(page: Page, unidade: string) {
    await expect(page).toHaveURL(new RegExp(String.raw`/processo/\d+/${unidade}$`));
}

test.describe.serial('CDU-16 - Ajustar mapa de competências', () => {
    const UNIDADE_ALVO = 'SECAO_211';

    const timestamp = Date.now();
    const descProcessoMapeamento = `Mapeamento CDU-16 ${timestamp}`;
    const descProcessoRevisao = `Revisão CDU-16 ${timestamp}`;
    let processoMapeamentoId: number;
    let processoRevisaoId: number;

    // Atividades e competências para os testes
    const atividadeBase1 = `Atividade Base 1 ${timestamp}`;
    const atividadeBase2 = `Atividade Base 2 ${timestamp}`;
    const atividadeBase3 = `Atividade Base 3 ${timestamp}`;
    const competencia1 = `Competência 1 ${timestamp}`;
    const competencia2 = `Competência 2 ${timestamp}`;
    const competencia3 = `Competência 3 ${timestamp}`;
    const atividadeNovaRevisao = `Atividade Nova Revisão ${timestamp}`;

    // ========================================================================
    // PREPARAÇÃO - Criar mapa vigente (processo de mapeamento completo)
    // ========================================================================

    test('Preparacao 1: Admin cria e inicia processo de mapeamento', async ({page, autenticadoComoAdmin, cleanupAutomatico}) => {
        

        await criarProcesso(page, {
            descricao: descProcessoMapeamento,
            tipo: 'MAPEAMENTO',
            diasLimite: 30,
            unidade: UNIDADE_ALVO,
            expandir: ['SECRETARIA_2', 'COORD_21']
        });

        const linhaProcesso = page.locator('tr', {has: page.getByText(descProcessoMapeamento)});
        await linhaProcesso.click();

        processoMapeamentoId = Number.parseInt(new RegExp(/\/processo\/cadastro\/(\d+)/).exec(page.url())?.[1] || '0');
        if (processoMapeamentoId > 0) cleanupAutomatico.registrar(processoMapeamentoId);

        await page.getByTestId('btn-processo-iniciar').click();
        await page.getByTestId('btn-iniciar-processo-confirmar').click();

        await verificarPaginaPainel(page);
    });

    test('Preparacao 2: Chefe adiciona atividades e disponibiliza cadastro', async ({page, autenticadoComoChefeSecao211}) => {
        

        await acessarSubprocessoChefeDireto(page, descProcessoMapeamento);
        await navegarParaAtividades(page);

        // Três atividades para criar três competências
        await adicionarAtividade(page, atividadeBase1);
        await adicionarConhecimento(page, atividadeBase1, 'Conhecimento 1A');

        await adicionarAtividade(page, atividadeBase2);
        await adicionarConhecimento(page, atividadeBase2, 'Conhecimento 2A');

        await adicionarAtividade(page, atividadeBase3);
        await adicionarConhecimento(page, atividadeBase3, 'Conhecimento 3A');

        await page.getByTestId('btn-cad-atividades-disponibilizar').click();
        await page.getByTestId('btn-confirmar-disponibilizacao').click();

        await expect(page.getByText(/Cadastro de atividades disponibilizado/i).first()).toBeVisible();
        await verificarPaginaPainel(page);
    });

    test('Preparacao 3: Admin homologa cadastro', async ({page, autenticadoComoAdmin}) => {
        

        await acessarSubprocessoAdmin(page, descProcessoMapeamento, UNIDADE_ALVO);
        await navegarParaAtividadesVisualizacao(page);
        await page.getByTestId('btn-acao-analisar-principal').click();
        await page.getByTestId('btn-aceite-cadastro-confirmar').click();

        await expect(page).toHaveURL(/\/processo\/\d+\/\w+$/);
    });

    test('Preparacao 4: Admin cria competências e disponibiliza mapa', async ({page, autenticadoComoAdmin}) => {
        

        await acessarSubprocessoAdmin(page, descProcessoMapeamento, UNIDADE_ALVO);
        await navegarParaMapa(page);

        // Criar três competências, uma para cada atividade
        await criarCompetencia(page, competencia1, [atividadeBase1]);
        await criarCompetencia(page, competencia2, [atividadeBase2]);
        await criarCompetencia(page, competencia3, [atividadeBase3]);

        await disponibilizarMapa(page, '2030-12-31');

        await verificarPaginaPainel(page);
        await expect(page.getByText(/Mapa disponibilizado/i)).toBeVisible();
    });

    test('Preparacao 5: Chefe valida mapa', async ({page, autenticadoComoChefeSecao211}) => {
        

        await acessarSubprocessoChefeDireto(page, descProcessoMapeamento);
        await navegarParaMapa(page);

        await page.getByTestId('btn-mapa-validar').click();
        await page.getByTestId('btn-validar-mapa-confirmar').click();

        // Validação: confirmar redirecionamento para Painel (CDU-19 passo 8)
        await verificarPaginaPainel(page);
        await expect(page.getByText(/Mapa validado/i).first()).toBeVisible();
    });

    test('Preparacao 6: Admin homologa mapa e finaliza processo de mapeamento', async ({page, autenticadoComoAdmin}) => {
        

        await acessarSubprocessoAdmin(page, descProcessoMapeamento, UNIDADE_ALVO);
        await navegarParaMapa(page);
        await page.getByTestId('btn-mapa-homologar-aceite').click();
        await page.getByTestId('btn-aceite-mapa-confirmar').click();

        // Aguardar redirecionamento para o Painel
        await verificarPaginaPainel(page);

        // Navegar de volta para verificar situação atualizada
        await acessarSubprocessoAdmin(page, descProcessoMapeamento, UNIDADE_ALVO);
        await expect(page.getByText(/Mapa homologado/i).first()).toBeVisible();

        // Finalizar processo
        await page.goto('/painel');
        await page.getByText(descProcessoMapeamento).click();
        await page.getByTestId('btn-processo-finalizar').click();
        await page.getByTestId('btn-finalizar-processo-confirmar').click();

        await verificarPaginaPainel(page);
    });

    test('Preparacao 7: Admin cria e inicia processo de revisão', async ({page, autenticadoComoAdmin, cleanupAutomatico}) => {
        

        await criarProcesso(page, {
            descricao: descProcessoRevisao,
            tipo: 'REVISAO',
            diasLimite: 30,
            unidade: UNIDADE_ALVO,
            expandir: ['SECRETARIA_2', 'COORD_21']
        });

        const linhaProcesso = page.locator('tr', {has: page.getByText(descProcessoRevisao)});
        await linhaProcesso.click();

        processoRevisaoId = Number.parseInt(new RegExp(/\/processo\/cadastro\/(\d+)/).exec(page.url())?.[1] || '0');
        if (processoRevisaoId > 0) cleanupAutomatico.registrar(processoRevisaoId);

        await page.getByTestId('btn-processo-iniciar').click();
        await page.getByTestId('btn-iniciar-processo-confirmar').click();

        await verificarPaginaPainel(page);
    });

    test('Preparacao 8: Chefe revisa atividades com alterações', async ({page, autenticadoComoChefeSecao211}) => {
        

        await acessarSubprocessoChefeDireto(page, descProcessoRevisao);
        await navegarParaAtividades(page);

        // Verificar atividades do mapeamento foram copiadas
        await expect(page.getByText(atividadeBase1)).toBeVisible();
        await expect(page.getByText(atividadeBase2)).toBeVisible();
        await expect(page.getByText(atividadeBase3)).toBeVisible();

        // Adicionar nova atividade (causa impacto: atividade inserida)
        await adicionarAtividade(page, atividadeNovaRevisao);
        await adicionarConhecimento(page, atividadeNovaRevisao, 'Conhecimento Novo');

        // Editar atividade existente (causa impacto na competência 2)
        await editarAtividade(page, atividadeBase2, `${atividadeBase2} Editada`);

        // Remover atividade (causa impacto na competência 3)
        await removerAtividade(page, atividadeBase3);

        // Disponibilizar revisão
        await page.getByTestId('btn-cad-atividades-disponibilizar').click();
        await page.getByTestId('btn-confirmar-disponibilizacao').click();

        await expect(page.getByText(/Revisão do cadastro de atividades disponibilizada/i).first()).toBeVisible();
        await verificarPaginaPainel(page);
    });

    test('Preparacao 9: Admin homologa revisão do cadastro', async ({page}) => {
        // Esta homologação leva o subprocesso ao estado "Revisão do cadastro homologada"
        // que é a pré-condição para CDU-16
        await page.goto('/painel');
        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);

        await acessarSubprocessoAdmin(page, descProcessoRevisao, UNIDADE_ALVO);
        await navegarParaAtividadesVisualizacao(page);
        await page.getByTestId('btn-acao-analisar-principal').click();
        await page.getByTestId('btn-aceite-cadastro-confirmar').click();

        // Após aceite, redireciona para Detalhes do subprocesso
        await expect(page).toHaveURL(/\/processo\/\d+\/\w+$/);

        // Verificar situação é "Revisão do cadastro homologada"
        await expect(page.getByTestId('subprocesso-header__txt-situacao'))
            .toHaveText(/Revis[aã]o d[oe] cadastro homologada/i);
    });

    // ========================================================================
    // TESTES PRINCIPAIS - CDU-16
    // ========================================================================

    test('Cenario 1: ADMIN navega para tela de edição do mapa', async ({page, autenticadoComoAdmin}) => {
        // CDU-16: Passo 1-6
        

        // Passo 1: No Painel, ADMIN escolhe o processo de revisão
        await expect(page.getByText(descProcessoRevisao)).toBeVisible();
        
        // Passo 2-4: Sistema mostra Detalhes do processo, ADMIN clica na unidade
        await acessarSubprocessoAdmin(page, descProcessoRevisao, UNIDADE_ALVO);

        // Sistema mostra Detalhes do subprocesso
        await expect(page.getByText(/Revis[aã]o d[oe] cadastro homologada/i).first()).toBeVisible();

        // Passo 5-6: ADMIN clica no card Mapa de Competências
        await navegarParaMapa(page);

        // Verificar tela de Edição de mapa
        await expect(page.getByRole('heading', {name: /Mapa de competências/i})).toBeVisible();

        // Verificar botão Impactos no mapa (CDU-16 passo 6)
        await expect(page.getByTestId('cad-mapa__btn-impactos-mapa')).toBeVisible();

        // Verificar botão Disponibilizar (CDU-16 passo 6)
        await expect(page.getByTestId('btn-cad-mapa-disponibilizar')).toBeVisible();

        // Verificar competências existentes estão visíveis
        await expect(page.getByText(competencia1)).toBeVisible();
        await expect(page.getByText(competencia2)).toBeVisible();
    });

    test('Cenario 2: ADMIN visualiza impactos no mapa', async ({page, autenticadoComoAdmin}) => {
        // CDU-16: Passo 7-8
        

        await acessarSubprocessoAdmin(page, descProcessoRevisao, UNIDADE_ALVO);
        await navegarParaMapa(page);

        // Passo 7: ADMIN clica em Impactos no mapa
        await page.getByTestId('cad-mapa__btn-impactos-mapa').click();

        // Passo 8: Sistema mostra modal Impactos no mapa
        await expect(page.getByTestId('modal-impacto-body')).toBeVisible();

        // Verificar impactos usando getByRole para o dialog
        const modal = page.getByRole('dialog');
        await expect(modal).toBeVisible();

        // - Atividade inserida (atividadeNovaRevisao) - não vinculada a competência
        await expect(modal.getByText('Atividades Inseridas')).toBeVisible();
        await expect(modal.getByText(atividadeNovaRevisao)).toBeVisible();

        // - Competência 2 impactada (atividade editada)
        await expect(modal.getByText('Competências Impactadas')).toBeVisible();
        await expect(modal.getByText(competencia2)).toBeVisible();

        // - Competência 3 impactada (atividade removida)
        await expect(modal.getByText(competencia3)).toBeVisible();

        // Fechar modal
        await page.getByTestId('btn-fechar-impacto').click();
        await expect(modal).toBeHidden();
    });

    test('Cenario 3: ADMIN pode abrir modal para editar competência', async ({page, autenticadoComoAdmin}) => {
        // CDU-16: Passo 9 - ADMIN pode acessar edição de competências
        

        await acessarSubprocessoAdmin(page, descProcessoRevisao, UNIDADE_ALVO);
        await navegarParaMapa(page);

        // Verificar que a competência existe
        await expect(page.getByText(competencia1)).toBeVisible();

        // Clicar para editar competência
        const card = page.locator('.competencia-card', {has: page.getByText(competencia1, {exact: true})});
        await card.hover();
        const editButton = card.getByTestId('btn-editar-competencia');
        await expect(editButton).toBeVisible();
        await editButton.click();

        // Verificar que o modal abre com a descrição correta
        const modal = page.getByTestId('mdl-criar-competencia');
        await expect(modal).toBeVisible();
        await expect(page.getByTestId('inp-criar-competencia-descricao')).toHaveValue(competencia1);

        // Cancelar edição
        await page.getByRole('button', {name: 'Cancelar'}).click();
        await expect(modal).toBeHidden();
    });

    test('Cenario 4: ADMIN associa atividade não vinculada a nova competência', async ({page, autenticadoComoAdmin}) => {
        // CDU-16: Passo 9.1 - ADMIN deve associar todas as atividades não associadas
        

        await acessarSubprocessoAdmin(page, descProcessoRevisao, UNIDADE_ALVO);
        await navegarParaMapa(page);

        // Criar nova competência com a atividade nova da revisão
        const novaCompetencia = `Competência Nova Ajuste ${timestamp}`;
        await criarCompetencia(page, novaCompetencia, [atividadeNovaRevisao]);

        // Verificar que a nova competência foi criada com a atividade vinculada
        await expect(page.getByText(novaCompetencia)).toBeVisible();
    });
});
