/* eslint-disable playwright/expect-expect */
import {expect, Page, test} from '@playwright/test';
import * as AuthHelpers from './helpers/helpers-auth.js';
import * as ProcessoHelpers from './helpers/helpers-processos.js';
import * as AtividadeHelpers from './helpers/helpers-atividades.js';
import * as MapaHelpers from './helpers/helpers-mapas.js';
import * as AnaliseHelpers from './helpers/helpers-analise.js';
import {limparNotificacoes, verificarAppAlert, verificarToast} from './helpers/helpers-navegacao.js';
import { TEXTOS } from "../frontend/src/constants/textos.js";

test.describe.serial('Jornada do Ciclo de Vida Completo do SGC', () => {
    test.beforeAll(async ({request}) => {
        // Reset do banco de dados UMA VEZ para iniciar a jornada
        const response = await request.post('/e2e/reset-database');
        expect(response.ok()).toBeTruthy();
    });

    test('Fase 0: Administração de Notificações', async ({page}) => {
        await validarAcessoRestritoNotificacoes(page);
        await validarPainelNotificacoesAdmin(page);
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

    const validarAcessoRestritoNotificacoes = async (page: Page) => {
        await AuthHelpers.executarComo(page, CHEFE, async () => {
            await expect(page.getByTestId('nav-link-notificacoes')).toBeHidden();
            await page.goto('/administracao/notificacoes');
            await expect(page).toHaveURL(/\/painel/);
        });
        await expect(page).toHaveURL(/\/login/);
    };

    const validarPainelNotificacoesAdmin = async (page: Page) => {
        await AuthHelpers.executarComo(page, ADMIN, async () => {
            await expect(page.getByTestId('nav-link-notificacoes')).toBeVisible();
            await page.getByTestId('nav-link-notificacoes').click();
            await expect(page).toHaveURL(/\/administracao\/notificacoes/);

            await expect(page.getByRole('heading', {name: 'Notificações pendentes'})).toBeVisible();
            await expect(page.getByRole('heading', {name: 'Notificações concluídas'})).toBeVisible();

            const tabelaPendentes = page.getByTestId('tbl-notificacoes-pendentes');
            await expect(tabelaPendentes).toBeVisible();
            await expect(tabelaPendentes.getByTestId('notificacao-unidade-SECAO_321')).toBeVisible();
            await expect(tabelaPendentes.getByTestId('notificacao-processo-SECAO_321')).toHaveText('Mapeamento Secão 321');
            await expect(tabelaPendentes.getByTestId('notificacao-status-SECAO_321')).toHaveText('Falha definitiva');
            await expect(tabelaPendentes.getByTestId('notificacao-erro-SECAO_321')).toContainText(/Falha simulada no seed/i);
            await expect(tabelaPendentes.getByTestId('btn-notificacoes-reenviar-SECAO_321')).toBeVisible();

            const tabelaConcluidas = page.getByTestId('tbl-notificacoes-concluidas');
            await expect(tabelaConcluidas).toBeVisible();
            await expect(tabelaConcluidas.getByTestId('notificacao-unidade-SECAO_311')).toBeVisible();
            await expect(tabelaConcluidas.getByTestId('notificacao-processo-SECAO_311')).toHaveText('Mapeamento Secão 311');
            await expect(tabelaConcluidas.getByTestId('notificacao-status-SECAO_311')).toHaveText('Enviada');
            await expect(tabelaConcluidas.getByTestId('btn-notificacoes-reenviar-SECAO_311')).toBeHidden();

            await tabelaPendentes.getByTestId('btn-notificacoes-reenviar-SECAO_321').click();
            await expect(page.getByTestId('txt-notificacoes-reenviar-confirmacao')).toContainText(/Recolocar .* fila .*SECAO_321/i);
            await page.getByTestId('btn-notificacoes-reenviar-confirmar').click();

            await verificarAppAlert(page, /recolocada/i);
            await expect(tabelaPendentes.getByTestId('notificacao-status-SECAO_321')).toHaveText('Pendente');
            await expect(tabelaPendentes.getByTestId('btn-notificacoes-reenviar-SECAO_321')).toBeHidden();
        });
        await expect(page).toHaveURL(/\/login/);
    };

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
            await expect(page.getByTestId('card-subprocesso-atividades')).toContainText('Cadastro de atividades e conhecimentos da unidade');
            await AtividadeHelpers.navegarParaAtividades(page);
            const btnDisponibilizar = page.getByTestId('btn-cad-atividades-disponibilizar');
            await expect(btnDisponibilizar).toBeVisible();
            await btnDisponibilizar.click();
            await expect(page.getByText(TEXTOS.atividades.ERRO_CADASTRO_INCOMPLETO)).toBeVisible();
            await limparNotificacoes(page);

            await AtividadeHelpers.importarAtividadesVazia(page, 'Processo Seed 200', 'SECRETARIA_1', ['Atividade 1']);
            await AtividadeHelpers.disponibilizarCadastro(page);

            await AnaliseHelpers.acessarSubprocessoChefeDireto(page, descricaoMapeamento, siglaUnidade);
            await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Cadastro disponibilizado/i);
            await expect(page.getByTestId('card-subprocesso-atividades')).toBeVisible();
            await expect(page.getByTestId('card-subprocesso-atividades')).toContainText('Cadastro de atividades e conhecimentos da unidade');
        });
        await expect(page).toHaveURL(/\/login/);
    };

    const realizarAceiteGestor = async (page: Page) => {
        await AuthHelpers.executarComo(page, GESTOR, async () => {
            await AnaliseHelpers.acessarSubprocessoGestor(page, descricaoMapeamento, siglaUnidade);
            await expect(page.getByTestId('card-subprocesso-atividades')).toBeVisible();
            await expect(page.getByTestId('card-subprocesso-atividades')).toContainText('Cadastro de atividades e conhecimentos da unidade');
            await AtividadeHelpers.navegarParaAtividades(page);
            await expect(page.getByTestId('btn-cad-atividades-historico')).toBeVisible();
            await expect(page.getByTestId('btn-acao-analisar-principal')).toBeVisible();
            await expect(page.getByTestId('btn-acao-analisar-principal')).toBeEnabled();
            await AnaliseHelpers.aceitarCadastroMapeamento(page, 'Cadastro aceito pelo Gestor.');

            await AnaliseHelpers.acessarSubprocessoGestor(page, descricaoMapeamento, siglaUnidade);
            await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Cadastro disponibilizado/i);
            await expect(page.getByTestId('card-subprocesso-atividades')).toBeVisible();
            await expect(page.getByTestId('card-subprocesso-atividades')).toContainText('Cadastro de atividades e conhecimentos da unidade');
        });
        await expect(page).toHaveURL(/\/login/);
    };

    const homologarMapeamentoAdmin = async (page: Page) => {
        await AuthHelpers.executarComo(page, ADMIN, async () => {
            await AnaliseHelpers.acessarSubprocessoAdmin(page, descricaoMapeamento, siglaUnidade);
            await expect(page.getByTestId('card-subprocesso-atividades')).toBeVisible();
            await expect(page.getByTestId('card-subprocesso-atividades')).toContainText('Cadastro de atividades e conhecimentos da unidade');
            await AtividadeHelpers.navegarParaAtividades(page);
            await expect(page.getByTestId('btn-cad-atividades-historico')).toBeVisible();
            await expect(page.getByTestId('btn-acao-analisar-principal')).toBeVisible();
            await expect(page.getByTestId('btn-acao-analisar-principal')).toBeEnabled();
            await AnaliseHelpers.homologarCadastroMapeamento(page);

            await expect(page.getByTestId('header-subprocesso')).toBeVisible();
            await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Cadastro homologado/i);
            await expect(page.getByTestId('card-subprocesso-mapa')).toBeVisible();
            await expect(page.getByTestId('card-subprocesso-mapa')).toContainText('Mapa de competências técnicas da unidade');
            await expect(page.getByTestId('card-subprocesso-atividades')).toBeVisible();
            await expect(page.getByTestId('card-subprocesso-atividades')).toContainText('Cadastro de atividades e conhecimentos da unidade');
        });
        await expect(page).toHaveURL(/\/login/);
    };

    const criarMapaAdmin = async (page: Page) => {
        await AuthHelpers.executarComo(page, ADMIN, async () => {
            await AnaliseHelpers.acessarSubprocessoAdmin(page, descricaoMapeamento, siglaUnidade);
            await expect(page.getByTestId('card-subprocesso-mapa')).toBeVisible();
            await expect(page.getByTestId('card-subprocesso-mapa')).toContainText('Mapa de competências técnicas da unidade');
            await MapaHelpers.navegarParaMapa(page);
            await expect(page.getByTestId('btn-abrir-criar-competencia')).toBeVisible();
            const btnDisponibilizar = page.getByTestId('btn-cad-mapa-disponibilizar');
            await expect(btnDisponibilizar).toBeVisible();
            await btnDisponibilizar.click();
            await expect(page.getByText(TEXTOS.mapa.ERRO_MAPA_SEM_COMPETENCIAS)).toBeVisible();
            await limparNotificacoes(page);

            await MapaHelpers.criarCompetencia(page, 'Competência Técnica Básica', ['Atividade 1']);
            await MapaHelpers.disponibilizarMapa(page);

            await AnaliseHelpers.acessarSubprocessoAdmin(page, descricaoMapeamento, siglaUnidade);
            await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Mapa disponibilizado/i);
            await expect(page.getByTestId('card-subprocesso-mapa')).toBeVisible();
            await expect(page.getByTestId('card-subprocesso-mapa')).toContainText('Mapa de competências técnicas da unidade');
        });
        await expect(page).toHaveURL(/\/login/);
    };

    const validarMapaChefe = async (page: Page) => {
        await AuthHelpers.executarComo(page, CHEFE, async () => {
            await AnaliseHelpers.acessarSubprocessoChefeDireto(page, descricaoMapeamento, siglaUnidade);
            await expect(page.getByTestId('card-subprocesso-mapa')).toBeVisible();
            await expect(page.getByTestId('card-subprocesso-mapa')).toContainText('Mapa de competências técnicas da unidade');
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
            await expect(page.getByTestId('card-subprocesso-mapa')).toBeVisible();
            await expect(page.getByTestId('card-subprocesso-mapa')).toContainText('Mapa de competências técnicas da unidade');
        });
        await expect(page).toHaveURL(/\/login/);
    };

    const aceitarMapaGestor = async (page: Page) => {
        await AuthHelpers.executarComo(page, GESTOR, async () => {
            await AnaliseHelpers.acessarSubprocessoGestor(page, descricaoMapeamento, siglaUnidade);
            await expect(page.getByTestId('card-subprocesso-mapa')).toBeVisible();
            await expect(page.getByTestId('card-subprocesso-mapa')).toContainText('Mapa de competências técnicas da unidade');
            await page.getByTestId('card-subprocesso-mapa').click();
            await expect(page.getByTestId('btn-mapa-historico')).toBeVisible();
            await expect(page.getByTestId('btn-mapa-devolver')).toBeVisible();
            await expect(page.getByTestId('btn-mapa-homologar-aceite')).toBeVisible();
            await expect(page.getByTestId('btn-mapa-homologar-aceite')).toBeEnabled();
            await page.goBack();
            await expect(page.getByTestId('header-subprocesso')).toBeVisible();
            await MapaHelpers.aceitarOuHomologarMapa(page, 'Mapa aceito pelo Gestor.');

            await AnaliseHelpers.acessarSubprocessoGestor(page, descricaoMapeamento, siglaUnidade);
            await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Mapa validado/i);
            await expect(page.getByTestId('card-subprocesso-mapa')).toBeVisible();
            await expect(page.getByTestId('card-subprocesso-mapa')).toContainText('Mapa de competências técnicas da unidade');
        });
        await expect(page).toHaveURL(/\/login/);
    };

    const homologarMapaEFinalizarAdmin = async (page: Page) => {
        await AuthHelpers.executarComo(page, ADMIN, async () => {
            await AnaliseHelpers.acessarSubprocessoAdmin(page, descricaoMapeamento, siglaUnidade);
            await MapaHelpers.navegarParaMapa(page);
            await expect(page.getByTestId('btn-mapa-homologar-aceite')).toBeVisible();
            await expect(page.getByTestId('btn-mapa-homologar-aceite')).toBeEnabled();
            await page.getByTestId('btn-mapa-homologar-aceite').click();
            const modal = page.getByTestId('body-aceite-mapa');
            await expect(modal).toBeVisible();
            await page.getByTestId('inp-aceite-mapa-observacao').fill('Mapa homologado pelo Admin. Ciclo base concluído.');
            await page.getByTestId('btn-aceite-mapa-confirmar').click();
            await expect(modal).toBeHidden();
            await verificarToast(page, new RegExp(`${TEXTOS.sucesso.ACEITE_REGISTRADO}|${TEXTOS.mapa.SUCESSO_HOMOLOGACAO}`, 'i'));
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
            await expect(page.getByTestId('card-subprocesso-atividades')).toContainText('Cadastro de atividades e conhecimentos da unidade');
            await AtividadeHelpers.navegarParaAtividades(page);
            const btnDisponibilizar = page.getByTestId('btn-cad-atividades-disponibilizar');
            await expect(btnDisponibilizar).toBeVisible();
            await btnDisponibilizar.click();
            await expect(page.getByText(TEXTOS.atividades.ERRO_REVISAO_SEM_ALTERACAO)).toBeVisible();
            await limparNotificacoes(page);

            await expect(page.getByText('Atividade 1')).toBeVisible();
            await AtividadeHelpers.adicionarConhecimento(page, 'Atividade 1', 'Conhecimento Revisado');
            await AtividadeHelpers.disponibilizarCadastro(page);

            await AnaliseHelpers.acessarSubprocessoChefeDireto(page, descricaoRevisao, siglaUnidade);
            await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Revisão do cadastro disponibilizada/i);
            await expect(page.getByTestId('card-subprocesso-atividades')).toBeVisible();
            await expect(page.getByTestId('card-subprocesso-atividades')).toContainText('Cadastro de atividades e conhecimentos da unidade');
        });
        await expect(page).toHaveURL(/\/login/);
    };

    const realizarAceiteRevisaoGestor = async (page: Page) => {
        await AuthHelpers.executarComo(page, GESTOR, async () => {
            await AnaliseHelpers.acessarSubprocessoGestor(page, descricaoRevisao, siglaUnidade);
            await expect(page.getByTestId('card-subprocesso-atividades')).toBeVisible();
            await expect(page.getByTestId('card-subprocesso-atividades')).toContainText('Cadastro de atividades e conhecimentos da unidade');
            await AtividadeHelpers.navegarParaAtividades(page);
            await expect(page.getByTestId('btn-cad-atividades-historico')).toBeVisible();
            await expect(page.getByTestId('btn-acao-analisar-principal')).toBeVisible();
            await expect(page.getByTestId('btn-acao-analisar-principal')).toBeEnabled();
            await AnaliseHelpers.aceitarRevisao(page, 'Revisão aceita.');

            await AnaliseHelpers.acessarSubprocessoGestor(page, descricaoRevisao, siglaUnidade);
            await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Revisão d[oe] cadastro disponibilizada/i);
            await expect(page.getByTestId('card-subprocesso-atividades')).toBeVisible();
            await expect(page.getByTestId('card-subprocesso-atividades')).toContainText('Cadastro de atividades e conhecimentos da unidade');
        });
        await expect(page).toHaveURL(/\/login/);
    };

    const homologarRevisaoAdmin = async (page: Page) => {
        await AuthHelpers.executarComo(page, ADMIN, async () => {
            await AnaliseHelpers.acessarSubprocessoAdmin(page, descricaoRevisao, siglaUnidade);
            await expect(page.getByTestId('card-subprocesso-atividades')).toBeVisible();
            await expect(page.getByTestId('card-subprocesso-atividades')).toContainText('Cadastro de atividades e conhecimentos da unidade');
            await AtividadeHelpers.navegarParaAtividades(page);
            await expect(page.getByTestId('btn-cad-atividades-historico')).toBeVisible();
            await expect(page.getByTestId('btn-acao-analisar-principal')).toBeVisible();
            await expect(page.getByTestId('btn-acao-analisar-principal')).toBeEnabled();
            await AnaliseHelpers.homologarCadastroMapeamento(page, 'Revisão homologada. Ciclo de manutenção completo.');

            await expect(page.getByTestId('header-subprocesso')).toBeVisible();
            await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Revisão do cadastro homologada/i);
            await expect(page.getByTestId('card-subprocesso-mapa')).toBeVisible();
            await expect(page.getByTestId('card-subprocesso-mapa')).toContainText('Mapa de competências técnicas da unidade');
            await expect(page.getByTestId('card-subprocesso-atividades')).toBeVisible();
            await expect(page.getByTestId('card-subprocesso-atividades')).toContainText('Cadastro de atividades e conhecimentos da unidade');
        });
        await expect(page).toHaveURL(/\/login/);
    };

});
