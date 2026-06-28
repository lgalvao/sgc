import {expect, test} from './fixtures/complete-fixtures.js';
import type {APIRequestContext, Page} from '@playwright/test';
import {acessarDetalhesProcesso, criarProcesso,} from './helpers/helpers-processos.js';
import {
    criarProcessoDiagnosticoConcluidoFixture,
    criarProcessoFinalizadoFixture,
    type ProcessoFixture,
} from './fixtures/index.js';
import {login, loginComPerfil, USUARIOS,} from './helpers/helpers-auth.js';
import {navegarParaDiagnosticoUnidade, verificarToast} from './helpers/helpers-navegacao.js';
import {verificarNotificacaoAdmin} from './helpers/helpers-notificacoes-admin.js';
import {resetDatabase} from './hooks/hooks-limpeza.js';
import {fecharHistoricoAnalise} from './helpers/helpers-analise.js';

const UNIDADE_GESTOR = 'SECRETARIA_1';
const UNIDADE_SUBORDINADA = 'ASSESSORIA_12';
const UNIDADE_FORA_HIERARQUIA = 'SECAO_211';
const TITULO_CHEFE = '151515';
const NOME_CHEFE = 'Ana Beatriz de Albuquerque e Souza';
const NOME_SERVIDOR = 'João Guilherme de Albuquerque Maranhão';
const NOME_SERVIDOR_IMPOSSIBILITADO = 'Maria Eduarda Cavalcanti de Alencar';
const TEXTO_BOTAO_ACOES = 'Ações';
const TEXTO_ACEITE = 'Aceite registrado';
const TEXTO_DEVOLUCAO = 'Devolução realizada';
const TEXTO_HOMOLOGACAO = 'Diagnóstico homologado';

async function prepararDiagnosticoConcluido(
    request: APIRequestContext,
    descricao: string,
): Promise<ProcessoFixture> {
    return criarProcessoDiagnosticoConcluidoFixture(request, {
        descricao,
        unidade: UNIDADE_SUBORDINADA,
        iniciar: true,
    });
}

async function abrirAcoesAnaliseUnidade(page: Page): Promise<void> {
    await page.getByRole('button', {name: TEXTO_BOTAO_ACOES, exact: true}).first().click();
}

async function verificarTelaAnaliseDiagnostico(page: Page): Promise<void> {
    await expect(page.getByTestId('subprocesso-header__txt-header-unidade')).toHaveText(UNIDADE_SUBORDINADA);
    await expect(page.getByTestId('btn-historico-analise-unidade')).toBeEnabled();
    await expect(page.getByText('Processo:', {exact: true})).toBeVisible();
    const listaServidores = page.getByTestId('lista-servidores-diagnostico-unidade');
    await expect(listaServidores).toBeVisible();
    await expect(listaServidores).toContainText(NOME_SERVIDOR);
    await expect(listaServidores).toContainText(NOME_SERVIDOR_IMPOSSIBILITADO);
    await expect(page.getByText(NOME_CHEFE, {exact: true})).toHaveCount(0);
    await expect(page.getByText('Avaliações de competências', {exact: true})).toBeVisible();
    await expect(page.getByTestId('tbl-movimentacoes')).toBeVisible();
}

test.describe.serial('CDU-50 - Analisar diagnóstico', () => {
    const descricaoArvore = `Diagnóstico CDU-50 árvore ${Date.now()}`;
    const descricaoAceite = `Diagnóstico CDU-50 aceite ${Date.now()}`;
    const descricaoDevolucao = `Diagnóstico CDU-50 devolução ${Date.now()}`;
    const descricaoHomologacao = `Diagnóstico CDU-50 homologação ${Date.now()}`;

    test('Setup: ADMIN cria processo de diagnóstico com unidades em hierarquias distintas', async ({
        _resetAutomatico,
        page,
        request,
    }) => {
        await criarProcessoFinalizadoFixture(request, {
            unidade: UNIDADE_FORA_HIERARQUIA,
            iniciar: true,
            descricao: `Mapa vigente base ${UNIDADE_FORA_HIERARQUIA} ${Date.now()}`,
        });
        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
        await criarProcesso(page, {
            descricao: descricaoArvore,
            tipo: 'DIAGNOSTICO',
            unidade: [UNIDADE_SUBORDINADA, UNIDADE_FORA_HIERARQUIA],
            expandir: ['SECRETARIA_1', 'SECRETARIA_2', 'COORD_21'],
            iniciar: true,
        });
        await expect(page.getByTestId('tbl-processos').getByText(descricaoArvore).first()).toBeVisible();
    });

    test('GESTOR vê apenas sua hierarquia e a tela de análise completa da unidade subordinada', async ({
        _resetAutomatico,
        page,
    }) => {
        await loginComPerfil(
            page,
            USUARIOS.GESTOR_SECRETARIA_1.titulo,
            USUARIOS.GESTOR_SECRETARIA_1.senha,
            USUARIOS.GESTOR_SECRETARIA_1.perfil,
        );

        await acessarDetalhesProcesso(page, descricaoArvore);

        const tabelaArvore = page.getByTestId('tbl-tree');
        await expect(tabelaArvore).toBeVisible();
        await expect(tabelaArvore.getByText(`${UNIDADE_GESTOR} - Secretaria 1`).first()).toBeVisible();
        await expect(tabelaArvore.getByRole('row', {name: /^ASSESSORIA_12\b/i}).first()).toBeVisible();
        await expect(tabelaArvore.getByText('SECRETARIA_2')).toHaveCount(0);
        await expect(tabelaArvore.getByText('ASSESSORIA_21')).toHaveCount(0);

        await navegarParaDiagnosticoUnidade(page, UNIDADE_SUBORDINADA);
        await verificarTelaAnaliseDiagnostico(page);

        await page.getByTestId('btn-historico-analise-unidade').click();
        const modalHistorico = page.getByRole('dialog', {name: 'Histórico de análise'});
        await expect(modalHistorico).toBeVisible();
        await expect(modalHistorico.getByTestId('alert-historico-vazio')).toBeVisible();
        await fecharHistoricoAnalise(page);
    });

    test('ADMIN vê todas as unidades participantes na árvore do processo', async ({
        _resetAutomatico,
        page,
    }) => {
        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);

        await acessarDetalhesProcesso(page, descricaoArvore);

        const tabelaArvore = page.getByTestId('tbl-tree');
        await expect(tabelaArvore).toBeVisible();
        await expect(tabelaArvore.getByRole('row', {name: /^ASSESSORIA_12\b/i}).first()).toBeVisible();
        await expect(tabelaArvore.getByRole('row', {name: /^SECAO_211\b/i}).first()).toBeVisible();
    });

    test('GESTOR registra aceite, gera histórico e notificação para a unidade superior', async ({
        _resetAutomatico,
        page,
        request,
    }) => {
        await resetDatabase(request);
        const processo = await prepararDiagnosticoConcluido(request, descricaoAceite);

        await loginComPerfil(
            page,
            USUARIOS.GESTOR_SECRETARIA_1.titulo,
            USUARIOS.GESTOR_SECRETARIA_1.senha,
            USUARIOS.GESTOR_SECRETARIA_1.perfil,
        );
        await acessarDetalhesProcesso(page, processo.descricao);
        await navegarParaDiagnosticoUnidade(page, UNIDADE_SUBORDINADA);
        await verificarTelaAnaliseDiagnostico(page);

        await abrirAcoesAnaliseUnidade(page);
        await expect(page.getByTestId('btn-validar-diagnostico-unidade')).toBeVisible();
        await expect(page.getByTestId('btn-devolver-diagnostico-unidade')).toBeVisible();
        await expect(page.getByTestId('btn-homologar-diagnostico-unidade')).toHaveCount(0);

        await page.getByTestId('btn-validar-diagnostico-unidade').click();
        const modal = page.getByRole('dialog');
        await expect(modal).toContainText('Aceitar diagnóstico');
        await modal.getByRole('button', {name: 'Aceitar'}).click();
        await verificarToast(page, TEXTO_ACEITE);

        await page.getByTestId('btn-historico-analise-unidade').click();
        const modalHistorico = page.getByRole('dialog', {name: 'Histórico de análise'});
        await expect(modalHistorico).toBeVisible();
        await expect(modalHistorico.getByTestId('cell-unidade-0')).toHaveText(UNIDADE_GESTOR);
        await expect(modalHistorico.getByTestId('cell-resultado-0')).toContainText('Aceite');
        await fecharHistoricoAnalise(page);

        await expect(page.getByText('Diagnóstico aceito', {exact: true})).toBeVisible();

        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
        await verificarNotificacaoAdmin(page, {
            destinatario: 'ADMIN',
            assunto: `Diagnóstico da unidade ${UNIDADE_SUBORDINADA} aceito`,
            tipo: 'Diagnóstico aceito',
            trechoCorpo: processo.descricao,
        });
    });

    test('GESTOR devolve para ajustes, exige justificativa e reabre consenso para a unidade', async ({
        _resetAutomatico,
        page,
        request,
    }) => {
        await resetDatabase(request);
        const processo = await prepararDiagnosticoConcluido(request, descricaoDevolucao);

        await loginComPerfil(
            page,
            USUARIOS.GESTOR_SECRETARIA_1.titulo,
            USUARIOS.GESTOR_SECRETARIA_1.senha,
            USUARIOS.GESTOR_SECRETARIA_1.perfil,
        );
        await acessarDetalhesProcesso(page, processo.descricao);
        await navegarParaDiagnosticoUnidade(page, UNIDADE_SUBORDINADA);

        await abrirAcoesAnaliseUnidade(page);
        await page.getByTestId('btn-devolver-diagnostico-unidade').click();
        const modal = page.getByRole('dialog');
        await expect(modal).toContainText('Devolver diagnóstico');
        await modal.getByTestId('btn-confirmar-devolver-unidade').click();
        await expect(modal).toContainText('A justificativa é obrigatória.');

        await modal.getByRole('textbox').fill('Ajustar consenso e rever observações.');
        await modal.getByTestId('btn-confirmar-devolver-unidade').click();
        await verificarToast(page, TEXTO_DEVOLUCAO);

        await page.getByTestId('btn-historico-analise-unidade').click();
        const modalHistorico = page.getByRole('dialog', {name: 'Histórico de análise'});
        await expect(modalHistorico).toBeVisible();
        await expect(modalHistorico.getByTestId('cell-unidade-0')).toHaveText(UNIDADE_GESTOR);
        await expect(modalHistorico.getByTestId('cell-resultado-0')).toContainText('Devolução');
        await expect(modalHistorico.getByTestId('cell-observacao-0')).toContainText('Ajustar consenso e rever observações.');
        await fecharHistoricoAnalise(page);

        await expect(page.getByText('Diagnóstico devolvido para ajustes', {exact: true})).toBeVisible();

        await login(page, TITULO_CHEFE, 'senha');
        await page.goto(`/processo/${processo.codigo}/${UNIDADE_SUBORDINADA}`);
        await expect(page.getByText('Situação de capacitação', {exact: true})).toBeVisible();
        await expect(page.getByTestId('tbl-servidores-diagnostico')).toContainText('Autoavaliação não iniciada');

        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
        await verificarNotificacaoAdmin(page, {
            destinatario: UNIDADE_SUBORDINADA,
            assunto: `Diagnóstico da unidade ${UNIDADE_SUBORDINADA} devolvido para ajustes`,
            tipo: 'Diagnóstico devolvido',
            trechoCorpo: processo.descricao,
        });
    });

    test('ADMIN homologa após o aceite do gestor e registra histórico/notificação', async ({
        _resetAutomatico,
        page,
        request,
    }) => {
        await resetDatabase(request);
        const processo = await prepararDiagnosticoConcluido(request, descricaoHomologacao);

        await loginComPerfil(
            page,
            USUARIOS.GESTOR_SECRETARIA_1.titulo,
            USUARIOS.GESTOR_SECRETARIA_1.senha,
            USUARIOS.GESTOR_SECRETARIA_1.perfil,
        );
        await acessarDetalhesProcesso(page, processo.descricao);
        await navegarParaDiagnosticoUnidade(page, UNIDADE_SUBORDINADA);
        await abrirAcoesAnaliseUnidade(page);
        await page.getByTestId('btn-validar-diagnostico-unidade').click();
        await page.getByRole('dialog').getByRole('button', {name: 'Aceitar'}).click();
        await verificarToast(page, TEXTO_ACEITE);

        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
        await acessarDetalhesProcesso(page, processo.descricao);
        await navegarParaDiagnosticoUnidade(page, UNIDADE_SUBORDINADA);

        await abrirAcoesAnaliseUnidade(page);
        await expect(page.getByTestId('btn-validar-diagnostico-unidade')).toHaveCount(0);
        await expect(page.getByTestId('btn-devolver-diagnostico-unidade')).toBeVisible();
        await expect(page.getByTestId('btn-homologar-diagnostico-unidade')).toBeVisible();

        await page.getByTestId('btn-homologar-diagnostico-unidade').click();
        const modal = page.getByRole('dialog');
        await expect(modal).toContainText('Homologar diagnóstico');
        await modal.getByRole('button', {name: 'Homologar'}).click();
        await verificarToast(page, TEXTO_HOMOLOGACAO);

        await page.getByTestId('btn-historico-analise-unidade').click();
        const modalHistorico = page.getByRole('dialog', {name: 'Histórico de análise'});
        await expect(modalHistorico).toBeVisible();
        await expect(modalHistorico.getByTestId('cell-unidade-0')).toHaveText('ADMIN');
        await expect(modalHistorico.getByTestId('cell-resultado-0')).toContainText('Homologação');
        await expect(modalHistorico.getByTestId('cell-unidade-1')).toHaveText(UNIDADE_GESTOR);
        await expect(modalHistorico.getByTestId('cell-resultado-1')).toContainText('Aceite');
        await fecharHistoricoAnalise(page);

        await expect(page.getByText('Diagnóstico homologado', {exact: true}).first()).toBeVisible();

        await verificarNotificacaoAdmin(page, {
            destinatario: UNIDADE_SUBORDINADA,
            assunto: `Diagnóstico da unidade ${UNIDADE_SUBORDINADA} homologado`,
            tipo: 'Diagnóstico homologado',
            trechoCorpo: processo.descricao,
        });
    });
});
