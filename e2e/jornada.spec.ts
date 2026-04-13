/* eslint-disable playwright/expect-expect */
import {expect, Page, test} from '@playwright/test';
import * as AuthHelpers from './helpers/helpers-auth.js';
import * as ProcessoHelpers from './helpers/helpers-processos.js';
import * as AtividadeHelpers from './helpers/helpers-atividades.js';
import * as MapaHelpers from './helpers/helpers-mapas.js';
import * as AnaliseHelpers from './helpers/helpers-analise.js';

test.describe.serial('Jornada do Ciclo de Vida Completo do SGC', () => {
    test.beforeAll(async ({request}) => {
        // Reset do banco de dados UMA VEZ para iniciar a jornada
        const response = await request.post('/e2e/reset-database');
        expect(response.ok()).toBeTruthy();
    });

    test('Fase 1: Mapeamento Inicial - Cadastro de Atividades', async ({page}) => {
        await criarProcessoMapeamento(page);
        await preencherAtividadesChefe(page);
        await realizarAceiteGestor(page);
        await homologarMapeamentoAdmin(page);
    });

    test('Fase 2: Elaboração e Homologação do Mapa', async ({page}) => {
        await criarMapaAdmin(page);
        await validarMapaChefe(page);
        await aceitarMapaGestor(page);
        await homologarMapaEFinalizarAdmin(page);
    });

    test('Fase 3: Ciclo de Revisão e Manutenção', async ({page}) => {
        await criarProcessoRevisaoAdmin(page);
        await realizarRevisaoChefe(page);
        await realizarAceiteRevisaoGestor(page);
        await homologarRevisaoAdmin(page);
    });

    const timestamp = Date.now();
    const descricaoMapeamento = `Mapeamento Ciclo Completo ${timestamp}`;
    const descricaoRevisao = `Revisão Ciclo Completo ${timestamp}`;
    const siglaUnidade = 'ASSESSORIA_11'; // Unidade alvo

    const ADMIN = AuthHelpers.USUARIOS.ADMIN_1_PERFIL;
    const CHEFE = AuthHelpers.USUARIOS.CHEFE_ASSESSORIA_11;
    const GESTOR = AuthHelpers.USUARIOS.GESTOR_SECRETARIA_1;


    const criarProcessoMapeamento = async (page: Page) => {
        await AuthHelpers.executarComo(page, ADMIN, async () => {
            await ProcessoHelpers.criarProcesso(page, {
                descricao: descricaoMapeamento,
                tipo: 'MAPEAMENTO',
                unidade: [siglaUnidade],
                expandir: ['SECRETARIA_1'],
                iniciar: true
            });
        });
        await expect(page).toHaveURL(/\/login/);
    };

    const preencherAtividadesChefe = async (page: Page) => {
        await AuthHelpers.executarComo(page, CHEFE, async () => {
            await AnaliseHelpers.acessarSubprocessoChefeDireto(page, descricaoMapeamento, siglaUnidade);
            await expect(page.getByTestId('card-subprocesso-atividades')).toBeVisible();
            await expect(page.getByTestId('card-subprocesso-atividades-vis')).toBeHidden();
            await AtividadeHelpers.navegarParaAtividades(page);
            await expect(page.getByTestId('btn-cad-atividades-disponibilizar')).toBeVisible();
            await expect(page.getByTestId('btn-cad-atividades-disponibilizar')).toBeDisabled();
            await AtividadeHelpers.importarAtividadesVazia(page, 'Processo Seed 200', 'SECRETARIA_1', ['Atividade 1']);
            await expect(page.getByTestId('btn-cad-atividades-disponibilizar')).toBeEnabled();
            await AtividadeHelpers.disponibilizarCadastro(page);

            await AnaliseHelpers.acessarSubprocessoChefeDireto(page, descricaoMapeamento, siglaUnidade);
            await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Cadastro disponibilizado/i);
            await expect(page.getByTestId('card-subprocesso-atividades-vis')).toBeVisible();
            await expect(page.getByTestId('card-subprocesso-atividades')).toBeHidden();
        });
        await expect(page).toHaveURL(/\/login/);
    };

    const realizarAceiteGestor = async (page: Page) => {
        await AuthHelpers.executarComo(page, GESTOR, async () => {
            await AnaliseHelpers.acessarSubprocessoGestor(page, descricaoMapeamento, siglaUnidade);
            await expect(page.getByTestId('card-subprocesso-atividades-vis')).toBeVisible();
            await expect(page.getByTestId('card-subprocesso-atividades')).toBeHidden();
            await AtividadeHelpers.navegarParaAtividadesVisualizacao(page);
            await expect(page.getByTestId('btn-vis-atividades-historico')).toBeVisible();
            await expect(page.getByTestId('btn-acao-analisar-principal')).toBeVisible();
            await expect(page.getByTestId('btn-acao-analisar-principal')).toBeEnabled();
            await AnaliseHelpers.aceitarCadastroMapeamento(page, 'Cadastro aceito pelo Gestor.');

            await AnaliseHelpers.acessarSubprocessoGestor(page, descricaoMapeamento, siglaUnidade);
            await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Cadastro disponibilizado/i);
            await expect(page.getByTestId('card-subprocesso-atividades-vis')).toBeVisible();
            await expect(page.getByTestId('card-subprocesso-atividades')).toBeHidden();
        });
        await expect(page).toHaveURL(/\/login/);
    };

    const homologarMapeamentoAdmin = async (page: Page) => {
        await AuthHelpers.executarComo(page, ADMIN, async () => {
            await AnaliseHelpers.acessarSubprocessoAdmin(page, descricaoMapeamento, siglaUnidade);
            await expect(page.getByTestId('card-subprocesso-atividades-vis')).toBeVisible();
            await expect(page.getByTestId('card-subprocesso-atividades')).toBeHidden();
            await AtividadeHelpers.navegarParaAtividadesVisualizacao(page);
            await expect(page.getByTestId('btn-vis-atividades-historico')).toBeVisible();
            await expect(page.getByTestId('btn-acao-analisar-principal')).toBeVisible();
            await expect(page.getByTestId('btn-acao-analisar-principal')).toBeEnabled();
            await AnaliseHelpers.homologarCadastroMapeamento(page);

            await expect(page.getByTestId('header-subprocesso')).toBeVisible();
            await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Cadastro homologado/i);
            await expect(page.getByTestId('card-subprocesso-mapa-edicao')).toBeVisible();
            await expect(page.getByTestId('card-subprocesso-atividades-vis')).toBeVisible();
            await expect(page.getByTestId('card-subprocesso-atividades')).toBeHidden();
        });
        await expect(page).toHaveURL(/\/login/);
    };

    const criarMapaAdmin = async (page: Page) => {
        await AuthHelpers.executarComo(page, ADMIN, async () => {
            await AnaliseHelpers.acessarSubprocessoAdmin(page, descricaoMapeamento, siglaUnidade);
            await expect(page.getByTestId('card-subprocesso-mapa-edicao')).toBeVisible();
            await expect(page.getByTestId('card-subprocesso-mapa-visualizacao')).toBeHidden();
            await MapaHelpers.navegarParaMapa(page);
            await expect(page.getByTestId('btn-abrir-criar-competencia')).toBeVisible();
            await expect(page.getByTestId('btn-cad-mapa-disponibilizar')).toBeVisible();
            await expect(page.getByTestId('btn-cad-mapa-disponibilizar')).toBeDisabled();
            await MapaHelpers.criarCompetencia(page, 'Competência Técnica Básica', ['Atividade 1']);
            await expect(page.getByTestId('btn-cad-mapa-disponibilizar')).toBeEnabled();
            await MapaHelpers.disponibilizarMapa(page);

            await AnaliseHelpers.acessarSubprocessoAdmin(page, descricaoMapeamento, siglaUnidade);
            await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Mapa disponibilizado/i);
            await expect(page.getByTestId('card-subprocesso-mapa-visualizacao')).toBeVisible();
            await expect(page.getByTestId('card-subprocesso-mapa-edicao')).toBeHidden();
        });
        await expect(page).toHaveURL(/\/login/);
    };

    const validarMapaChefe = async (page: Page) => {
        await AuthHelpers.executarComo(page, CHEFE, async () => {
            await AnaliseHelpers.acessarSubprocessoChefeDireto(page, descricaoMapeamento, siglaUnidade);
            await expect(page.getByTestId('card-subprocesso-mapa-visualizacao')).toBeVisible();
            await expect(page.getByTestId('card-subprocesso-mapa-edicao')).toBeHidden();
            await MapaHelpers.navegarParaMapa(page);
            await expect(page.getByTestId('btn-mapa-validar')).toBeVisible();
            await expect(page.getByTestId('btn-mapa-validar')).toBeEnabled();
            await expect(page.getByTestId('btn-mapa-sugestoes')).toBeVisible();
            await expect(page.getByTestId('btn-mapa-sugestoes')).toBeEnabled();
            await page.getByTestId('btn-mapa-validar').click();
            await page.getByTestId('btn-validar-mapa-confirmar').click();
            await page.waitForURL(/\/painel$/);

            await AnaliseHelpers.acessarSubprocessoChefeDireto(page, descricaoMapeamento, siglaUnidade);
            await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Mapa validado/i);
            await expect(page.getByTestId('card-subprocesso-mapa-visualizacao')).toBeVisible();
        });
        await expect(page).toHaveURL(/\/login/);
    };

    const aceitarMapaGestor = async (page: Page) => {
        await AuthHelpers.executarComo(page, GESTOR, async () => {
            await AnaliseHelpers.acessarSubprocessoGestor(page, descricaoMapeamento, siglaUnidade);
            await expect(page.getByTestId('card-subprocesso-mapa-visualizacao')).toBeVisible();
            await expect(page.getByTestId('card-subprocesso-mapa-edicao')).toBeHidden();
            await page.getByTestId('card-subprocesso-mapa-visualizacao').click();
            await expect(page.getByTestId('btn-mapa-historico')).toBeVisible();
            await expect(page.getByTestId('btn-mapa-devolver')).toBeVisible();
            await expect(page.getByTestId('btn-mapa-homologar-aceite')).toBeVisible();
            await expect(page.getByTestId('btn-mapa-homologar-aceite')).toBeEnabled();
            await page.goBack();
            await expect(page.getByTestId('header-subprocesso')).toBeVisible();
            await MapaHelpers.aceitarOuHomologarMapa(page, 'Mapa aceito pelo Gestor.');

            await AnaliseHelpers.acessarSubprocessoGestor(page, descricaoMapeamento, siglaUnidade);
            await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Mapa validado/i);
            await expect(page.getByTestId('card-subprocesso-mapa-visualizacao')).toBeVisible();
            await expect(page.getByTestId('card-subprocesso-mapa-edicao')).toBeHidden();
        });
        await expect(page).toHaveURL(/\/login/);
    };

    const homologarMapaEFinalizarAdmin = async (page: Page) => {
        await AuthHelpers.executarComo(page, ADMIN, async () => {
            await AnaliseHelpers.acessarSubprocessoAdmin(page, descricaoMapeamento, siglaUnidade);
            await expect(page.getByTestId('card-subprocesso-mapa-visualizacao')).toBeVisible();
            await expect(page.getByTestId('card-subprocesso-mapa-edicao')).toBeHidden();
            await page.getByTestId('card-subprocesso-mapa-visualizacao').click();
            await expect(page.getByTestId('btn-mapa-historico')).toBeVisible();
            await expect(page.getByTestId('btn-mapa-homologar-aceite')).toBeVisible();
            await expect(page.getByTestId('btn-mapa-homologar-aceite')).toBeEnabled();
            await page.goBack();
            await expect(page.getByTestId('header-subprocesso')).toBeVisible();
            await MapaHelpers.aceitarOuHomologarMapa(page, 'Mapa homologado pelo Admin. Ciclo base concluído.');
            await ProcessoHelpers.acessarDetalhesProcesso(page, descricaoMapeamento);
            await ProcessoHelpers.finalizarProcesso(page);
            await ProcessoHelpers.verificarProcessoNaTabela(page, {
                descricao: descricaoMapeamento,
                situacao: 'Finalizado',
                tipo: 'Mapeamento'
            });
        });
        await expect(page).toHaveURL(/\/login/);
    };

    const criarProcessoRevisaoAdmin = async (page: Page) => {
        await AuthHelpers.executarComo(page, ADMIN, async () => {
            await ProcessoHelpers.criarProcesso(page, {
                descricao: descricaoRevisao,
                tipo: 'REVISAO',
                unidade: [siglaUnidade],
                expandir: ['SECRETARIA_1'],
                iniciar: true
            });
        });
        await expect(page).toHaveURL(/\/login/);
    };

    const realizarRevisaoChefe = async (page: Page) => {
        await AuthHelpers.executarComo(page, CHEFE, async () => {
            await AnaliseHelpers.acessarSubprocessoChefeDireto(page, descricaoRevisao, siglaUnidade);
            await expect(page.getByTestId('card-subprocesso-atividades')).toBeVisible();
            await expect(page.getByTestId('card-subprocesso-atividades-vis')).toBeHidden();
            await AtividadeHelpers.navegarParaAtividades(page);
            await expect(page.getByTestId('btn-cad-atividades-disponibilizar')).toBeVisible();
            await expect(page.getByTestId('btn-cad-atividades-disponibilizar')).toBeDisabled();
            await expect(page.getByText('Atividade 1')).toBeVisible();
            await AtividadeHelpers.adicionarConhecimento(page, 'Atividade 1', 'Conhecimento Revisado');
            await expect(page.getByTestId('btn-cad-atividades-disponibilizar')).toBeEnabled();
            await AtividadeHelpers.disponibilizarCadastro(page);

            await AnaliseHelpers.acessarSubprocessoChefeDireto(page, descricaoRevisao, siglaUnidade);
            await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Revisão do cadastro disponibilizada/i);
            await expect(page.getByTestId('card-subprocesso-atividades-vis')).toBeVisible();
            await expect(page.getByTestId('card-subprocesso-atividades')).toBeHidden();
        });
        await expect(page).toHaveURL(/\/login/);
    };

    const realizarAceiteRevisaoGestor = async (page: Page) => {
        await AuthHelpers.executarComo(page, GESTOR, async () => {
            await AnaliseHelpers.acessarSubprocessoGestor(page, descricaoRevisao, siglaUnidade);
            await expect(page.getByTestId('card-subprocesso-atividades-vis')).toBeVisible();
            await expect(page.getByTestId('card-subprocesso-atividades')).toBeHidden();
            await AtividadeHelpers.navegarParaAtividadesVisualizacao(page);
            await expect(page.getByTestId('btn-vis-atividades-historico')).toBeVisible();
            await expect(page.getByTestId('btn-acao-analisar-principal')).toBeVisible();
            await expect(page.getByTestId('btn-acao-analisar-principal')).toBeEnabled();
            await AnaliseHelpers.aceitarRevisao(page, 'Revisão aceita.');

            await AnaliseHelpers.acessarSubprocessoGestor(page, descricaoRevisao, siglaUnidade);
            await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Revisão d[oe] cadastro disponibilizada/i);
            await expect(page.getByTestId('card-subprocesso-atividades-vis')).toBeVisible();
            await expect(page.getByTestId('card-subprocesso-atividades')).toBeHidden();
        });
        await expect(page).toHaveURL(/\/login/);
    };

    const homologarRevisaoAdmin = async (page: Page) => {
        await AuthHelpers.executarComo(page, ADMIN, async () => {
            await AnaliseHelpers.acessarSubprocessoAdmin(page, descricaoRevisao, siglaUnidade);
            await expect(page.getByTestId('card-subprocesso-atividades-vis')).toBeVisible();
            await expect(page.getByTestId('card-subprocesso-atividades')).toBeHidden();
            await AtividadeHelpers.navegarParaAtividadesVisualizacao(page);
            await expect(page.getByTestId('btn-vis-atividades-historico')).toBeVisible();
            await expect(page.getByTestId('btn-acao-analisar-principal')).toBeVisible();
            await expect(page.getByTestId('btn-acao-analisar-principal')).toBeEnabled();
            await AnaliseHelpers.homologarCadastroMapeamento(page, 'Revisão homologada. Ciclo de manutenção completo.');

            await expect(page.getByTestId('header-subprocesso')).toBeVisible();
            await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Revisão do cadastro homologada/i);
            await expect(page.getByTestId('card-subprocesso-mapa-edicao')).toBeVisible();
            await expect(page.getByTestId('card-subprocesso-atividades-vis')).toBeVisible();
            await expect(page.getByTestId('card-subprocesso-atividades')).toBeHidden();
        });
        await expect(page).toHaveURL(/\/login/);
    };

});
