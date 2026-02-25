import type {Page} from '@playwright/test';
import {expect, test} from './fixtures/complete-fixtures.js';
import {criarProcesso} from './helpers/helpers-processos.js';
import {login, loginComPerfil, USUARIOS} from './helpers/helpers-auth.js';
import {
    adicionarAtividade,
    adicionarConhecimento,
    editarAtividade,
    navegarParaAtividades,
    navegarParaAtividadesVisualizacao,
    removerAtividade
} from './helpers/helpers-atividades.js';
import {criarCompetencia, disponibilizarMapa, navegarParaMapa} from './helpers/helpers-mapas.js';
import {
    aceitarCadastroMapeamento,
    aceitarRevisao,
    acessarSubprocessoAdmin,
    acessarSubprocessoChefeDireto,
    acessarSubprocessoGestor
} from './helpers/helpers-analise.js';
import {verificarPaginaPainel} from './helpers/helpers-navegacao.js';

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

        const linhaProcesso = page.getByTestId('tbl-processos').locator('tr', {has: page.getByText(descProcessoMapeamento)});
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

    test('Preparacao 3: Gestores aceitam cadastro', async ({page, autenticadoComoGestorCoord21}) => {
        console.log('-> Gestores (COORD e SEC) aceitando cadastro...');
        // Gestor COORD_21
        await acessarSubprocessoGestor(page, descProcessoMapeamento, UNIDADE_ALVO);
        await navegarParaAtividadesVisualizacao(page);
        await aceitarCadastroMapeamento(page);

        // Gestor SECRETARIA_2
        await loginComPerfil(page, USUARIOS.CHEFE_SECRETARIA_2.titulo, USUARIOS.CHEFE_SECRETARIA_2.senha, 'GESTOR - SECRETARIA_2');
        await acessarSubprocessoGestor(page, descProcessoMapeamento, UNIDADE_ALVO);
        await navegarParaAtividadesVisualizacao(page);
        await aceitarCadastroMapeamento(page);
    });

    test('Preparacao 4: Admin homologa cadastro, cria competências e disponibiliza mapa', async ({page, autenticadoComoAdmin}) => {
        // Homologação do cadastro
        await acessarSubprocessoAdmin(page, descProcessoMapeamento, UNIDADE_ALVO);
        await navegarParaAtividadesVisualizacao(page);
        await page.getByTestId('btn-acao-analisar-principal').click();
        await page.getByTestId('btn-aceite-cadastro-confirmar').click();
        await expect(page).toHaveURL(/\/processo\/\d+\/\w+$/);

        // Criação do mapa
        await navegarParaMapa(page);
        await criarCompetencia(page, competencia1, [atividadeBase1]);
        await criarCompetencia(page, competencia2, [atividadeBase2]);
        await criarCompetencia(page, competencia3, [atividadeBase3]);

        await disponibilizarMapa(page, '2030-12-31');
        await expect(page.getByText(/Mapa disponibilizado/i)).toBeVisible();
        await verificarPaginaPainel(page);
    });

    test('Preparacao 5: Chefe valida mapa', async ({page, autenticadoComoChefeSecao211}) => {
        await acessarSubprocessoChefeDireto(page, descProcessoMapeamento);
        await navegarParaMapa(page);

        await page.getByTestId('btn-mapa-validar').click();
        await page.getByTestId('btn-validar-mapa-confirmar').click();

        await verificarPaginaPainel(page);
        await expect(page.getByText(/Mapa validado/i).first()).toBeVisible();
    });

    test('Preparacao 6: Gestores aceitam mapa', async ({page, autenticadoComoGestorCoord21}) => {
        // Gestor COORD_21
        await acessarSubprocessoGestor(page, descProcessoMapeamento, UNIDADE_ALVO);
        await navegarParaMapa(page);
        await page.getByTestId('btn-mapa-homologar-aceite').click();
        await page.getByTestId('btn-aceite-mapa-confirmar').click();
        await verificarPaginaPainel(page);

        // Gestor SECRETARIA_2
        await loginComPerfil(page, USUARIOS.CHEFE_SECRETARIA_2.titulo, USUARIOS.CHEFE_SECRETARIA_2.senha, 'GESTOR - SECRETARIA_2');
        await acessarSubprocessoGestor(page, descProcessoMapeamento, UNIDADE_ALVO);
        await navegarParaMapa(page);
        await page.getByTestId('btn-mapa-homologar-aceite').click();
        await page.getByTestId('btn-aceite-mapa-confirmar').click();
        await verificarPaginaPainel(page);
    });

    test('Preparacao 7: Admin homologa mapa, finaliza e inicia revisão', async ({page, autenticadoComoAdmin, cleanupAutomatico}) => {
        // Homologação e finalização do Mapeamento
        await acessarSubprocessoAdmin(page, descProcessoMapeamento, UNIDADE_ALVO);
        await navegarParaMapa(page);
        await page.getByTestId('btn-mapa-homologar-aceite').click();
        await page.getByTestId('btn-aceite-mapa-confirmar').click();
        await verificarPaginaPainel(page);

        await page.getByTestId('tbl-processos').getByText(descProcessoMapeamento).first().click();
        await page.getByTestId('btn-processo-finalizar').click();
        await page.getByTestId('btn-finalizar-processo-confirmar').click();
        await verificarPaginaPainel(page);

        // Criação e início da Revisão
        await criarProcesso(page, {
            descricao: descProcessoRevisao,
            tipo: 'REVISAO',
            diasLimite: 30,
            unidade: UNIDADE_ALVO,
            expandir: ['SECRETARIA_2', 'COORD_21']
        });

        const linhaProcesso = page.getByTestId('tbl-processos').locator('tr', {has: page.getByText(descProcessoRevisao)});
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

        await expect(page.getByText(atividadeBase1)).toBeVisible();
        await expect(page.getByText(atividadeBase2)).toBeVisible();
        await expect(page.getByText(atividadeBase3)).toBeVisible();

        await adicionarAtividade(page, atividadeNovaRevisao);
        await adicionarConhecimento(page, atividadeNovaRevisao, 'Conhecimento Novo');

        await editarAtividade(page, atividadeBase2, `${atividadeBase2} Editada`);

        await removerAtividade(page, atividadeBase3);

        await page.getByTestId('btn-cad-atividades-disponibilizar').click();
        await page.getByTestId('btn-confirmar-disponibilizacao').click();

        await expect(page.getByText(/Revisão do cadastro de atividades disponibilizada/i).first()).toBeVisible();
        await verificarPaginaPainel(page);
    });

    test('Preparacao 9: Gestores e Admin aceitam revisão', async ({page, autenticadoComoGestorCoord21}) => {
        // Gestor COORD_21
        await acessarSubprocessoGestor(page, descProcessoRevisao, UNIDADE_ALVO);
        await navegarParaAtividadesVisualizacao(page);
        await aceitarRevisao(page);

        // Gestor SECRETARIA_2
        await loginComPerfil(page, USUARIOS.CHEFE_SECRETARIA_2.titulo, USUARIOS.CHEFE_SECRETARIA_2.senha, 'GESTOR - SECRETARIA_2');
        await acessarSubprocessoGestor(page, descProcessoRevisao, UNIDADE_ALVO);
        await navegarParaAtividadesVisualizacao(page);
        await aceitarRevisao(page);

        // Admin homologa
        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
        await acessarSubprocessoAdmin(page, descProcessoRevisao, UNIDADE_ALVO);
        await navegarParaAtividadesVisualizacao(page);
        await page.getByTestId('btn-acao-analisar-principal').click();
        await page.getByTestId('btn-aceite-cadastro-confirmar').click();

        await expect(page.getByTestId('subprocesso-header__txt-situacao'))
            .toHaveText(/Revis[aã]o d[oe] cadastro homologada/i);
    });

    // ========================================================================
    // TESTES PRINCIPAIS - CDU-16
    // ========================================================================

    test('Cenários CDU-16: ADMIN ajusta mapa e visualiza impactos', async ({page, autenticadoComoAdmin}) => {
        // Cenario 1: Navegação para tela de edição do mapa
        await test.step('Cenário 1: Navegação para o Mapa', async () => {
            await acessarSubprocessoAdmin(page, descProcessoRevisao, UNIDADE_ALVO);
            await navegarParaMapa(page);

            await expect(page.getByRole('heading', {name: /Mapa de competências/i})).toBeVisible();
            await expect(page.getByTestId('cad-mapa__btn-impactos-mapa')).toBeVisible();
            await expect(page.getByTestId('btn-cad-mapa-disponibilizar')).toBeVisible();
            await expect(page.getByText(competencia1)).toBeVisible();
            await expect(page.getByText(competencia2)).toBeVisible();
        });

        // Cenario 2: Visualização de impactos no mapa
        await test.step('Cenário 2: Visualização de Impactos', async () => {
            await page.getByTestId('cad-mapa__btn-impactos-mapa').click();
            await expect(page.getByTestId('modal-impacto-body')).toBeVisible();

            const modal = page.getByRole('dialog');
            await expect(modal.getByText('Atividades Inseridas')).toBeVisible();
            await expect(modal.getByText(atividadeNovaRevisao)).toBeVisible();
            await expect(modal.getByText('Competências Impactadas')).toBeVisible();
            await expect(modal.getByText(competencia2)).toBeVisible();
            await expect(modal.getByText(competencia3)).toBeVisible();

            await page.getByTestId('btn-fechar-impacto').click();
            await expect(modal).toBeHidden();
        });

        // Cenario 3: Abrir modal para editar competência
        await test.step('Cenário 3: Edição de Competência', async () => {
            const card = page.locator('.competencia-card', {has: page.getByText(competencia1, {exact: true})});
            await card.hover();
            await card.getByTestId('btn-editar-competencia').click();

            const modalCria = page.getByTestId('mdl-criar-competencia');
            await expect(modalCria).toBeVisible();
            await expect(page.getByTestId('inp-criar-competencia-descricao')).toHaveValue(competencia1);

            await page.getByRole('button', {name: 'Cancelar'}).click();
            await expect(modalCria).toBeHidden();
        });

        // Cenario 4: Associar atividade não vinculada a nova competência
        await test.step('Cenário 4: Associação de Nova Competência', async () => {
            const novaCompetencia = `Competência Nova Ajuste ${timestamp}`;
            await criarCompetencia(page, novaCompetencia, [atividadeNovaRevisao]);

            await expect(page.getByText(novaCompetencia)).toBeVisible();
        });
    });
});
