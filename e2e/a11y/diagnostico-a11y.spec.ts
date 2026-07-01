import {expect, test} from '../fixtures/base.js';
import {login, USUARIOS} from '../helpers/helpers-auth.js';
import {
    abrirAcaoConsensoDiagnostico,
    aprovarConsensoDiagnostico,
    buscarCodSubprocessoDiagnostico,
    navegarParaConsensoDiagnostico,
    preencherConsensoMinimo,
    preencherPrimeiraSituacaoCapacitacao
} from '../helpers/helpers-diagnostico.js';
import {resetDatabase, useProcessoCleanup} from '../hooks/hooks-limpeza.js';
import {capturarCheckpointA11y, type OpcoesCapturaTela} from './helpers-a11y.js';
import {
    criarProcessoDiagnosticoComAutoavaliacaoConcluidaPorFixture,
    criarProcessoDiagnosticoComConsensoCriadoPorFixture,
    criarProcessoDiagnosticoPorFixture
} from './helpers-diagnostico-fixtures.js';

async function capturarTela(page: import('@playwright/test').Page, _categoria: string, _nome: string, opcoes?: OpcoesCapturaTela) {
    await capturarCheckpointA11y(page, opcoes);
}

test.describe('A11y - Diagnóstico de Competências', () => {
    let cleanup: ReturnType<typeof useProcessoCleanup>;

    test.describe.configure({timeout: 40000});

    test.beforeEach(async ({request}) => {
        test.slow();
        cleanup = useProcessoCleanup();
        await resetDatabase(request);
    });

    test.afterEach(async ({request}) => {
        await cleanup.limpar(request);
    });

    test('Captura dashboard do subprocesso de diagnóstico e autoavaliação do servidor', async ({page, request}) => {
        const unidadeAlvo = 'ASSESSORIA_12';
        const servidorTitulo = '242426';
        const descricao = `Proc diagnostico captura ${Date.now()}`;
        const processoCodigo = await criarProcessoDiagnosticoPorFixture(request, cleanup, descricao, unidadeAlvo);

        await login(page, servidorTitulo, 'senha');
        await page.goto(`/processo/${processoCodigo}/${unidadeAlvo}`);
        await expect(page).toHaveURL(new RegExp(String.raw`/processo/${processoCodigo}/${unidadeAlvo}(?:\?.*)?$`));
        await capturarTela(page, 'diagnostico', 'subprocesso-servidor', {
            fullPage: true,
            tags: ['diagnostico', 'dashboard', 'servidor']
        });

        const codSubprocesso = await buscarCodSubprocessoDiagnostico(page, processoCodigo, unidadeAlvo);
        await page.goto(`/diagnostico/${codSubprocesso}/${unidadeAlvo}/autoavaliacao`);
        await capturarTela(page, 'diagnostico', 'autoavaliacao-servidor', {
            fullPage: true,
            tags: ['diagnostico', 'autoavaliacao', 'servidor']
        });
    });

    test('Captura monitoramento e consenso pela chefia', async ({page, request}) => {
        const unidadeAlvo = 'ASSESSORIA_12';
        const servidorTitulo = '242426';
        const descricao = `Proc diagnostico monitoramento ${Date.now()}`;
        const processoCodigo = await criarProcessoDiagnosticoComAutoavaliacaoConcluidaPorFixture(
            request, cleanup, descricao, unidadeAlvo, servidorTitulo
        );

        await login(page, USUARIOS.CHEFE_ASSESSORIA_12.titulo, USUARIOS.CHEFE_ASSESSORIA_12.senha);
        await page.goto(`/processo/${processoCodigo}/${unidadeAlvo}`);
        const codSubprocesso = await buscarCodSubprocessoDiagnostico(page, processoCodigo, unidadeAlvo);
        await capturarTela(page, 'diagnostico', 'monitoramento-chefia', {
            fullPage: true,
            tags: ['diagnostico', 'monitoramento', 'chefia']
        });

        await abrirAcaoConsensoDiagnostico(page, servidorTitulo);
        await capturarTela(page, 'diagnostico', 'menu-acoes-consenso-chefia', {
            fullPage: true,
            tags: ['diagnostico', 'consenso', 'dropdown']
        });

        await navegarParaConsensoDiagnostico(page, servidorTitulo);
        await capturarTela(page, 'diagnostico', 'consenso-chefia', {
            fullPage: true,
            tags: ['diagnostico', 'consenso', 'chefia']
        });

        await preencherConsensoMinimo(page, codSubprocesso, servidorTitulo);
        await capturarTela(page, 'diagnostico', 'consenso-chefia-preenchido', {
            fullPage: true,
            tags: ['diagnostico', 'consenso', 'autosave']
        });
    });

    test('Captura consenso do servidor e situação de capacitação', async ({page, request}) => {
        const unidadeAlvo = 'ASSESSORIA_12';
        const servidorTitulo = '242426';
        const descricao = `Proc diagnostico consenso ${Date.now()}`;
        const processoCodigo = await criarProcessoDiagnosticoComConsensoCriadoPorFixture(
            request, cleanup, descricao, unidadeAlvo, servidorTitulo
        );

        await login(page, '242426', 'senha');
        const codSubprocessoServidor = await buscarCodSubprocessoDiagnostico(page, processoCodigo, unidadeAlvo);
        await page.goto(`/diagnostico/${codSubprocessoServidor}/${unidadeAlvo}/consenso/${servidorTitulo}`);
        await expect(page).toHaveURL(new RegExp(String.raw`/diagnostico/${codSubprocessoServidor}/${unidadeAlvo}/consenso/${servidorTitulo}`));
        await capturarTela(page, 'diagnostico', 'consenso-servidor', {
            fullPage: true,
            tags: ['diagnostico', 'consenso', 'servidor']
        });

        await login(page, USUARIOS.CHEFE_ASSESSORIA_12.titulo, USUARIOS.CHEFE_ASSESSORIA_12.senha);
        const codSubprocesso = await buscarCodSubprocessoDiagnostico(page, processoCodigo, unidadeAlvo);
        await login(page, servidorTitulo, 'senha');
        await page.goto(`/diagnostico/${codSubprocesso}/${unidadeAlvo}/consenso/${servidorTitulo}`);
        await aprovarConsensoDiagnostico(page, codSubprocesso);

        await login(page, USUARIOS.CHEFE_ASSESSORIA_12.titulo, USUARIOS.CHEFE_ASSESSORIA_12.senha);
        await page.goto(`/diagnostico/${codSubprocesso}/${unidadeAlvo}/situacao-capacitacao`);
        await capturarTela(page, 'diagnostico', 'situacao-capacitacao-chefia', {
            fullPage: true,
            tags: ['diagnostico', 'capacitacao', 'chefia']
        });

        await preencherPrimeiraSituacaoCapacitacao(page, codSubprocesso, 'EC');
        await capturarTela(page, 'diagnostico', 'situacao-capacitacao-preenchida', {
            fullPage: true,
            tags: ['diagnostico', 'capacitacao', 'autosave']
        });
    });
});
