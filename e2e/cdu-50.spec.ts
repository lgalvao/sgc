import {expect, test} from './fixtures/complete-fixtures.js';
import type {Page, APIRequestContext, Response} from '@playwright/test';
import {
    criarProcesso,
} from './helpers/helpers-processos.js';
import {
    criarProcessoFinalizadoFixture,
    criarProcessoDiagnosticoComConsensoCriadoFixture,
    type ProcessoFixture,
} from './fixtures/index.js';
import {
    USUARIOS,
    login,
    loginComPerfil,
} from './helpers/helpers-auth.js';
import {acessarDetalhesProcesso} from './helpers/helpers-processos.js';
import {navegarParaDiagnosticoUnidade} from './helpers/helpers-navegacao.js';
import {buscarCodSubprocessoDiagnostico} from './helpers/helpers-diagnostico.js';
import {verificarNotificacaoAdmin} from './helpers/helpers-notificacoes-admin.js';
import {resetDatabase} from './hooks/hooks-limpeza.js';
import {TEXTOS} from '../frontend/src/constants/textos.js';

const UNIDADE_GESTOR = 'SECRETARIA_1';
const UNIDADE_SUBORDINADA = 'ASSESSORIA_12';
const UNIDADE_FORA_HIERARQUIA = 'SECAO_211';
const TITULO_CHEFE = '151515';
const NOME_CHEFE = 'Axl Rose';
const TITULO_SERVIDOR = '242426';
const NOME_SERVIDOR = 'Duff McKagan';
const TITULO_SERVIDOR_IMPOSSIBILITADO = '242427';
const NOME_SERVIDOR_IMPOSSIBILITADO = 'Izzy Stradlin';
const TEXTO_BOTAO_ACOES = 'Ações';
const TEXTO_ACEITE = 'Aceite registrado';
const TEXTO_DEVOLUCAO = 'Devolução realizada';
const TEXTO_HOMOLOGACAO = 'Diagnóstico homologado';

async function preencherSituacoesCapacitacaoPendentesPorApi(page: Page, codSubprocesso: number): Promise<void> {
    await page.evaluate(async (codigo: number) => {
        const respostaAtual = await fetch(`/api/subprocessos/${codigo}/diagnostico/unidade`, {credentials: 'include'});
        if (!respostaAtual.ok) {
            throw new Error(`Falha ao carregar situações de capacitação do subprocesso ${codigo}.`);
        }

        const dados = await respostaAtual.json();
        const situacoes = dados.situacoesCapacitacao.map((item: {
            servidorTitulo: string;
            competenciaCodigo: number;
            situacaoCapacitacao: string | null;
        }) => ({
            servidorTitulo: item.servidorTitulo,
            competenciaCodigo: item.competenciaCodigo,
            situacaoCapacitacao: item.situacaoCapacitacao ?? 'EC',
        }));

        const respostaSalvar = await fetch(`/api/subprocessos/${codigo}/diagnostico/situacoes-capacitacao`, {
            method: 'POST',
            credentials: 'include',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({situacoes}),
        });

        if (!respostaSalvar.ok) {
            throw new Error(`Falha ao preencher situações de capacitação do subprocesso ${codigo}.`);
        }
    }, codSubprocesso);
}

async function impossibilitarAvaliacoesPendentes(page: Page, codSubprocesso: number): Promise<void> {
    const tabela = page.getByTestId('tbl-servidores-diagnostico');
    await expect(tabela).toBeVisible();
    await tabela.locator('tbody tr').first().waitFor({state: 'visible'});

    const linhas = tabela.locator('tbody tr');
    const total = await linhas.count();
    console.log(`[DEBUG] total de servidores na tabela: ${total}`);

    for (let i = 0; i < total; i++) {
        const linha = linhas.nth(i);
        const texto = await linha.textContent();
        console.log(`[DEBUG] servidor ${i} texto: ${texto}`);
        if (!texto?.includes(TEXTOS.diagnostico.SITUACAO_NAO_REALIZADA)) {
            console.log(`[DEBUG] pulando servidor ${i} pois não está pendente`);
            continue;
        }

        console.log(`[DEBUG] impossibilitando servidor ${i}...`);
        await linha.getByRole('button', {name: TEXTO_BOTAO_ACOES}).click();
        await page.locator('[data-testid^="btn-impossibilitar-"]:visible').click();
        await page.getByTestId('textarea-justificativa-impossibilidade').fill('Servidor afastado durante a rodada.');
        await Promise.all([
            page.waitForResponse((res: Response) =>
                res.url().includes(`/api/subprocessos/${codSubprocesso}/diagnostico/avaliacoes/`)
                && res.url().includes('/impossibilitar')
                && res.request().method() === 'POST'
                && res.ok(),
            ),
            page.getByTestId('btn-confirmar-impossibilitar').click(),
        ]);
        // Aguarda o fechamento do modal esperando o textarea sumir
        await expect(page.getByTestId('textarea-justificativa-impossibilidade')).toBeHidden();
        console.log(`[DEBUG] impossibilitado com sucesso`);
    }
}

async function prepararDiagnosticoConcluido(
    page: Page,
    request: APIRequestContext,
    descricao: string,
): Promise<ProcessoFixture> {
    const processo = await criarProcessoDiagnosticoComConsensoCriadoFixture(request, {
        descricao,
        unidade: UNIDADE_SUBORDINADA,
        iniciar: true,
        servidorTitulo: TITULO_SERVIDOR,
    });

    await login(page, TITULO_CHEFE, 'senha');
    await page.goto(`/processo/${processo.codigo}/${UNIDADE_SUBORDINADA}`);
    const codSubprocesso = await buscarCodSubprocessoDiagnostico(page, processo.codigo, UNIDADE_SUBORDINADA);

    await impossibilitarAvaliacoesPendentes(page, codSubprocesso);

    await login(page, TITULO_SERVIDOR, 'senha');
    await page.goto(`/processo/${processo.codigo}/${UNIDADE_SUBORDINADA}`);
    const cardConsenso = page.getByTestId('card-subprocesso-consenso');
    await expect(cardConsenso).toBeVisible();
    await cardConsenso.click();
    await expect(page.getByTestId('btn-aprovar-consenso')).toBeVisible();
    await Promise.all([
        page.waitForResponse((res: Response) =>
            res.url().includes(`/api/subprocessos/${codSubprocesso}/diagnostico/consenso/aprovar`)
            && res.request().method() === 'POST'
            && res.ok(),
        ),
        page.getByTestId('btn-aprovar-consenso').click(),
    ]);

    await login(page, TITULO_CHEFE, 'senha');
    await page.goto(`/processo/${processo.codigo}/${UNIDADE_SUBORDINADA}`);
    await preencherSituacoesCapacitacaoPendentesPorApi(page, codSubprocesso);
    await page.getByTestId('btn-concluir-diagnostico').click();
    await Promise.all([
        page.waitForResponse((res: Response) =>
            res.url().includes(`/api/subprocessos/${codSubprocesso}/diagnostico/concluir`)
            && res.request().method() === 'POST'
            && res.ok(),
        ),
        page.getByTestId('btn-confirmar-concluir-diagnostico').click(),
    ]);
    await expect(page).toHaveURL(/\/painel/);

    return processo;
}

async function abrirAcoesAnaliseUnidade(page: Page): Promise<void> {
    await page.getByRole('button', {name: TEXTO_BOTAO_ACOES, exact: true}).first().click();
}

async function verificarTelaAnaliseDiagnostico(page: Page): Promise<void> {
    await expect(page.getByRole('heading', {name: 'Análise do Diagnóstico da Unidade'})).toBeVisible();
    await expect(page.getByText(UNIDADE_SUBORDINADA, {exact: true})).toBeVisible();
    await expect(page.getByTestId('btn-historico-analise-unidade')).toBeEnabled();
    await expect(page.getByText('Servidores participantes', {exact: true})).toBeVisible();
    await expect(page.getByRole('listitem').filter({hasText: NOME_SERVIDOR})).toBeVisible();
    await expect(page.getByRole('listitem').filter({hasText: NOME_SERVIDOR_IMPOSSIBILITADO})).toBeVisible();
    await expect(page.getByRole('listitem').filter({hasText: NOME_CHEFE})).toHaveCount(0);
    await expect(page.getByText('Competência x Servidor', {exact: true})).toBeVisible();
    await expect(page.getByTestId('matriz-diagnostico-unidade')).toContainText(NOME_SERVIDOR);
    await expect(page.getByTestId('matriz-diagnostico-unidade')).toContainText(NOME_SERVIDOR_IMPOSSIBILITADO);
    await expect(page.getByTestId('matriz-diagnostico-unidade')).toContainText('I');
    await expect(page.getByTestId('matriz-diagnostico-unidade')).toContainText('D');
    await expect(page.getByTestId('matriz-diagnostico-unidade')).toContainText('C');
    await expect(page.getByText('Servidores e Consenso', {exact: true})).toBeVisible();
    await expect(page.getByRole('button', {name: new RegExp(`${NOME_SERVIDOR}${TITULO_SERVIDOR}`)})).toBeVisible();
    await expect(page.getByRole('button', {name: new RegExp(`${NOME_SERVIDOR_IMPOSSIBILITADO}${TITULO_SERVIDOR_IMPOSSIBILITADO}`)})).toBeVisible();
    await expect(page.getByText('Histórico de Movimentações', {exact: true})).toBeVisible();
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
        const modalHistorico = page.getByTestId('mdl-historico-analise');
        await expect(modalHistorico).toBeVisible();
        await expect(modalHistorico.getByTestId('alert-historico-vazio')).toBeVisible();
        await modalHistorico.getByTestId('btn-modal-fechar').click();
        await expect(modalHistorico).toBeHidden();
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
        const processo = await prepararDiagnosticoConcluido(page, request, descricaoAceite);

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
        await expect(page.getByTestId('app-alert')).toContainText(TEXTO_ACEITE);

        await page.getByTestId('btn-historico-analise-unidade').click();
        const modalHistorico = page.getByTestId('mdl-historico-analise');
        await expect(modalHistorico).toBeVisible();
        await expect(modalHistorico.getByTestId('cell-unidade-0')).toHaveText(UNIDADE_GESTOR);
        await expect(modalHistorico.getByTestId('cell-resultado-0')).toContainText('Aceite');
        await modalHistorico.getByTestId('btn-modal-fechar').click();
        await expect(modalHistorico).toBeHidden();

        await expect(page.getByText('Diagnóstico aceito', {exact: true})).toBeVisible();

        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
        await verificarNotificacaoAdmin(page, {
            destinatario: 'ADMIN',
            assunto: `Diagnóstico da unidade ${UNIDADE_SUBORDINADA} submetido para análise`,
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
        const processo = await prepararDiagnosticoConcluido(page, request, descricaoDevolucao);

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
        await expect(page.getByTestId('app-alert')).toContainText(TEXTO_DEVOLUCAO);

        await page.getByTestId('btn-historico-analise-unidade').click();
        const modalHistorico = page.getByTestId('mdl-historico-analise');
        await expect(modalHistorico).toBeVisible();
        await expect(modalHistorico.getByTestId('cell-unidade-0')).toHaveText(UNIDADE_GESTOR);
        await expect(modalHistorico.getByTestId('cell-resultado-0')).toContainText('Devolução');
        await expect(modalHistorico.getByTestId('cell-observacao-0')).toContainText('Ajustar consenso e rever observações.');
        await modalHistorico.getByTestId('btn-modal-fechar').click();
        await expect(modalHistorico).toBeHidden();

        await expect(page.getByText('Diagnóstico devolvido para ajustes', {exact: true})).toBeVisible();

        await login(page, TITULO_CHEFE, 'senha');
        await page.goto(`/processo/${processo.codigo}/${UNIDADE_SUBORDINADA}`);
        await expect(page.getByText('Avaliação de consenso criada').first()).toBeVisible();

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
        const processo = await prepararDiagnosticoConcluido(page, request, descricaoHomologacao);

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
        await expect(page.getByTestId('app-alert')).toContainText(TEXTO_ACEITE);

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
        await expect(page.getByTestId('app-alert')).toContainText(TEXTO_HOMOLOGACAO);

        await page.getByTestId('btn-historico-analise-unidade').click();
        const modalHistorico = page.getByTestId('mdl-historico-analise');
        await expect(modalHistorico).toBeVisible();
        await expect(modalHistorico.getByTestId('cell-unidade-0')).toHaveText(UNIDADE_GESTOR);
        await expect(modalHistorico.getByTestId('cell-resultado-0')).toContainText('Aceite');
        await modalHistorico.getByTestId('btn-modal-fechar').click();
        await expect(modalHistorico).toBeHidden();

        await expect(page.getByText('Diagnóstico homologado', {exact: true}).first()).toBeVisible();

        await verificarNotificacaoAdmin(page, {
            destinatario: UNIDADE_SUBORDINADA,
            assunto: `Diagnóstico da unidade ${UNIDADE_SUBORDINADA} homologado`,
            tipo: 'Diagnóstico homologado',
            trechoCorpo: processo.descricao,
        });
    });
});
