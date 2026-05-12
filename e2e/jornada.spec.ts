/* eslint-disable playwright/expect-expect */
import {expect, Page, test} from '@playwright/test';
import * as AuthHelpers from './helpers/helpers-auth.js';
import * as ProcessoHelpers from './helpers/helpers-processos.js';
import * as AtividadeHelpers from './helpers/helpers-atividades.js';
import * as MapaHelpers from './helpers/helpers-mapas.js';
import * as AnaliseHelpers from './helpers/helpers-analise.js';
import {limparNotificacoes, verificarAppAlert, verificarToast} from './helpers/helpers-navegacao.js';
import {TEXTOS} from "../frontend/src/constants/textos.js";

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

        // SERVIDOR também não tem acesso ao painel de notificações nem ao menu de Unidades
        const SERVIDOR = AuthHelpers.USUARIOS.SERVIDOR;
        await AuthHelpers.executarComo(page, SERVIDOR, async () => {
            await expect(page.getByTestId('nav-link-notificacoes')).toBeHidden();
            await expect(page.getByRole('link', {name: /Unidades/i})).toBeHidden();
            await expect(page.getByTestId('btn-painel-criar-processo')).toBeHidden();
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

            const secaoNotificacoes = page.getByTestId('sec-notificacoes');
            await expect(secaoNotificacoes).toBeVisible();

            const tabelaNotificacoes = page.getByTestId('tbl-notificacoes');
            await expect(tabelaNotificacoes).toBeVisible();
            await expect(tabelaNotificacoes).toContainText('SECAO_321');
            await expect(tabelaNotificacoes).toContainText('Mapeamento Secão 321');
            await expect(tabelaNotificacoes).toContainText('Falha Definitiva');
            await expect(tabelaNotificacoes).toContainText('SECAO_311');
            await expect(tabelaNotificacoes).toContainText('Mapeamento Secão 311');
            await expect(tabelaNotificacoes).toContainText('Enviado');
            const btnDetalhesFalha = tabelaNotificacoes.locator('[data-testid^="btn-detalhes-"]').first();
            await expect(btnDetalhesFalha).toBeVisible();
            await btnDetalhesFalha.click();
            const modalDetalhes = page.getByTestId('modal-detalhes-notificacao');
            await expect(modalDetalhes).toBeVisible();
            await expect(modalDetalhes).toContainText(/Falha simulada no seed/i);
            await page.getByRole('button', {name: /Fechar/i}).click();
            await expect(modalDetalhes).toBeHidden();
            const btnReenviarPendente = tabelaNotificacoes.locator('[data-testid^="btn-notificacoes-reenviar-"]').first();
            await expect(btnReenviarPendente).toBeVisible();

            await btnReenviarPendente.click();
            await expect(page.getByTestId('txt-notificacoes-reenviar-confirmacao')).toContainText(/Confirma o reenvio/i);
            await page.getByTestId('btn-notificacoes-reenviar-confirmar').click();

            await verificarAppAlert(page, /recolocad[oa] na fila/i);
            await expect(tabelaNotificacoes).toContainText('Pendente');
            await expect(tabelaNotificacoes.locator('[data-testid^="btn-notificacoes-reenviar-"]')).toHaveCount(0);
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
            await AtividadeHelpers.navegarParaCadastro(page);
            const btnDisponibilizar = page.getByTestId('btn-cad-atividades-disponibilizar');
            await expect(btnDisponibilizar).toBeVisible();
            await expect(btnDisponibilizar).toBeDisabled();

            // Importar atividade do Seed e adicionar um conhecimento extra antes de disponibilizar
            await AtividadeHelpers.importarAtividades(page, 'Processo Seed 200', 'SECRETARIA_1', ['Atividade 1']);
            await AtividadeHelpers.adicionarConhecimento(page, 'Atividade 1', 'Conhecimento Adicional');
            await AtividadeHelpers.disponibilizarCadastro(page);

            await AnaliseHelpers.acessarSubprocessoChefeDireto(page, descricaoMapeamento, siglaUnidade);
            await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Cadastro disponibilizado/i);
            await expect(page.getByTestId('card-subprocesso-atividades')).toBeVisible();
            await expect(page.getByTestId('card-subprocesso-atividades')).toContainText('Cadastro de atividades e conhecimentos da unidade');
        });
        await expect(page).toHaveURL(/\/login/);
    };

    const realizarAceiteGestor = async (page: Page) => {
        // Primeiro: GESTOR devolve o cadastro para o CHEFE corrigir
        await AuthHelpers.executarComo(page, GESTOR, async () => {
            await AnaliseHelpers.acessarSubprocessoGestor(page, descricaoMapeamento, siglaUnidade);
            await expect(page.getByTestId('card-subprocesso-atividades')).toBeVisible();
            await AtividadeHelpers.navegarParaCadastro(page);
            await AnaliseHelpers.verificarAcoesAnaliseCadastro(page, {
                rotuloPrincipal: /Registrar aceite/i,
                principalHabilitado: true,
                devolverHabilitado: true
            });

            // Abrir histórico de análise antes de devolver
            const modalHistorico = await AnaliseHelpers.abrirHistoricoAnalise(page);
            await expect(modalHistorico).toBeVisible();
            await AnaliseHelpers.fecharHistoricoAnalise(page);

            // Devolver com observação
            await AnaliseHelpers.devolverCadastroMapeamento(page, 'Detalhes insuficientes. Por favor, revise as atividades.');
        });
        await expect(page).toHaveURL(/\/login/);

        // CHEFE recebe a devolução, abre o histórico e re-disponibiliza
        await AuthHelpers.executarComo(page, CHEFE, async () => {
            await AnaliseHelpers.acessarSubprocessoChefeDireto(page, descricaoMapeamento, siglaUnidade);
            await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Cadastro em andamento/i);
            await AtividadeHelpers.navegarParaCadastro(page);

            // Verificar no histórico que há uma devolução registrada
            const modalHistorico = await AnaliseHelpers.abrirHistoricoAnalise(page);
            await expect(modalHistorico).toBeVisible();
            await expect(modalHistorico.getByTestId('cell-resultado-0')).toHaveText(/Devolu/i);
            await AnaliseHelpers.fecharHistoricoAnalise(page);

            await AtividadeHelpers.disponibilizarCadastro(page);
        });
        await expect(page).toHaveURL(/\/login/);

        // Segundo: GESTOR aceita após correção
        await AuthHelpers.executarComo(page, GESTOR, async () => {
            await AnaliseHelpers.acessarSubprocessoGestor(page, descricaoMapeamento, siglaUnidade);
            await expect(page.getByTestId('card-subprocesso-atividades')).toBeVisible();
            await expect(page.getByTestId('card-subprocesso-atividades')).toContainText('Cadastro de atividades e conhecimentos da unidade');
            await AtividadeHelpers.navegarParaCadastro(page);
            await AnaliseHelpers.verificarAcoesAnaliseCadastro(page, {
                rotuloPrincipal: /Registrar aceite/i,
                principalHabilitado: true,
                devolverHabilitado: true
            });
            await AnaliseHelpers.aceitarCadastroMapeamento(page, 'Cadastro aceito pelo Gestor após correção.');

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
            await AtividadeHelpers.navegarParaCadastro(page);
            await AnaliseHelpers.verificarAcoesAnaliseCadastro(page, {
                rotuloPrincipal: /Homologar/i,
                principalHabilitado: true,
                devolverHabilitado: true
            });

            // Verificar histórico completo antes de homologar (deve ter devolução e aceite)
            const modalHistorico = await AnaliseHelpers.abrirHistoricoAnalise(page);
            await expect(modalHistorico).toBeVisible();
            await expect(modalHistorico.locator('[data-testid^="cell-resultado-"]').first()).toBeVisible();
            await AnaliseHelpers.fecharHistoricoAnalise(page);

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
            await expect(page.getByTestId('btn-mapa-acoes')).toBeVisible();
            await (await MapaHelpers.abrirAcaoMapa(page, 'btn-mapa-acao-disponibilizar')).click();
            await expect(page.getByText(TEXTOS.mapa.ERRO_MAPA_SEM_COMPETENCIAS)).toBeVisible();
            await limparNotificacoes(page);

            // Criar competência, editar e depois confirmar
            await MapaHelpers.criarCompetencia(page, 'Competência Técnica Rascunho', ['Atividade 1']);
            // Editar descrição da competência criada
            await MapaHelpers.editarCompetencia(page, 'Competência Técnica Rascunho', 'Competência Técnica Básica');
            await expect(page.getByText('Competência Técnica Básica', {exact: true})).toBeVisible();
            await expect(page.getByText('Competência Técnica Rascunho', {exact: true})).toBeHidden();

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
            await expect(page.getByTestId('btn-mapa-acoes')).toBeVisible();
            await MapaHelpers.abrirValidacaoMapa(page);
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
            await expect(page.getByTestId('btn-mapa-acoes')).toBeVisible();
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
            await expect(page.getByTestId('btn-mapa-acoes')).toBeVisible();
            await MapaHelpers.abrirAcaoPrincipalMapa(page);
            const modal = page.getByTestId('body-aceite-mapa');
            await expect(modal).toBeVisible();
            await page.getByTestId('inp-aceite-mapa-observacao').fill('Mapa homologado pelo Admin. Ciclo base concluído.');
            await page.getByTestId('btn-aceite-mapa-confirmar').click();
            await expect(modal).toBeHidden();
            await verificarToast(page, new RegExp(`${TEXTOS.sucesso.ACEITE_REGISTRADO}|${TEXTOS.mapa.SUCESSO_HOMOLOGACAO}`, 'i'));

            await page.goto('/painel');
            await ProcessoHelpers.acessarDetalhesProcesso(page, descricaoMapeamento);
            await ProcessoHelpers.finalizarProcesso(page);
            await ProcessoHelpers.verificarProcessoTabela(page, {
                descricao: descricaoMapeamento,
                situacao: 'Finalizado',
                tipo: 'Mapeamento'
            });
        });
        await expect(page).toHaveURL(/\/login/);

        // CHEFE confere mapa homologado (somente leitura)
        await AuthHelpers.executarComo(page, CHEFE, async () => {
            await AnaliseHelpers.acessarSubprocessoChefeDireto(page, descricaoMapeamento, siglaUnidade);
            await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Mapa homologado/i);
            await MapaHelpers.navegarParaMapa(page);
            await expect(page.getByTestId('btn-abrir-criar-competencia')).toBeHidden();
            await expect(page.getByText('Competência Técnica Básica')).toBeVisible();
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
            await AtividadeHelpers.navegarParaCadastro(page);
            const btnDisponibilizar = page.getByTestId('btn-cad-atividades-disponibilizar');
            await expect(btnDisponibilizar).toBeVisible();
            await expect(btnDisponibilizar).toBeDisabled();

            await expect(page.getByText('Atividade 1')).toBeVisible();
            // Verificar botão de impacto (mostra diferenças em relação ao mapa vigente)
            const btnImpacto = page.getByTestId('cad-atividades__btn-impactos-mapa-edicao');
            if (await btnImpacto.isVisible()) {
                await btnImpacto.click();
                const modalImpacto = page.getByRole('dialog');
                await expect(modalImpacto).toBeVisible();
                await page.getByTestId('btn-fechar-impacto').click();
                await expect(modalImpacto).toBeHidden();
            }

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
            await AtividadeHelpers.navegarParaCadastro(page);
            await AnaliseHelpers.verificarAcoesAnaliseCadastro(page, {
                rotuloPrincipal: /Registrar aceite/i,
                principalHabilitado: true,
                devolverHabilitado: true
            });
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
            await AtividadeHelpers.navegarParaCadastro(page);
            await AnaliseHelpers.verificarAcoesAnaliseCadastro(page, {
                rotuloPrincipal: /Homologar/i,
                principalHabilitado: true,
                devolverHabilitado: true
            });
            await AnaliseHelpers.homologarCadastroMapeamento(page, 'Revisão homologada. Ciclo de manutenção completo.');

            await expect(page.getByTestId('header-subprocesso')).toBeVisible();
            await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Revisão do cadastro homologada/i);
            await expect(page.getByTestId('card-subprocesso-mapa')).toBeVisible();
            await expect(page.getByTestId('card-subprocesso-mapa')).toContainText('Mapa de competências técnicas da unidade');
            await expect(page.getByTestId('card-subprocesso-atividades')).toBeVisible();
            await expect(page.getByTestId('card-subprocesso-atividades')).toContainText('Cadastro de atividades e conhecimentos da unidade');
        });
        await expect(page).toHaveURL(/\/login/);

        // GESTOR confere que o mapa da revisão permanece somente leitura
        await AuthHelpers.executarComo(page, GESTOR, async () => {
            await AnaliseHelpers.acessarSubprocessoGestor(page, descricaoRevisao, siglaUnidade);
            await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Revisão do cadastro homologada/i);
            await expect(page.getByTestId('card-subprocesso-mapa')).toBeVisible();
        });
        await expect(page).toHaveURL(/\/login/);
    };

});
