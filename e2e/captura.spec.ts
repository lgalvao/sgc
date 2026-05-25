import type {APIRequestContext, Page} from '@playwright/test';
import {expect, test} from './fixtures/base.js';
import {login, loginComPerfil, USUARIOS} from './helpers/helpers-auth.js';
import {
    confirmarInicioProcessoPeloDialogo,
    criarProcesso,
    extrairProcessoCodigo,
    iniciarProcessoPeloCadastro,
    obterAcaoBloco
} from './helpers/helpers-processos.js';
import {
    navegarParaSubprocesso,
    obterAcaoCabecalhoSubprocesso,
    verificarAppAlert,
    verificarPaginaPainel
} from './helpers/helpers-navegacao.js';
import {
    adicionarAtividade,
    adicionarConhecimento,
    disponibilizarCadastro,
    navegarParaCadastro
} from './helpers/helpers-atividades.js';
import {
    abrirAcaoCadastroPrincipal,
    abrirHistoricoAnalise,
    acessarSubprocessoAdmin,
    acessarSubprocessoChefeDireto,
    acessarSubprocessoGestor,
    fecharHistoricoAnalise
} from './helpers/helpers-analise.js';
import {TEXTOS} from '../frontend/src/constants/textos.js';
import * as MapaHelpers from './helpers/helpers-mapas.js';
import {
    abrirDevolucaoMapa,
    abrirModalCriarCompetencia,
    disponibilizarMapa,
    excluirCompetenciaConfirmando,
    navegarParaMapa
} from './helpers/helpers-mapas.js';
import {resetDatabase, useProcessoCleanup} from './hooks/hooks-limpeza.js';
import * as path from 'node:path';
import * as fs from 'node:fs';

/**
 * Suite de testes para a capturar screenshots de todas as telas do sistema
 * para refinamento de UI. Funciona tambem como um bom teste 'smoke'
 */

type ContextoCaptura = Record<string, unknown>;

interface OpcoesCapturaTela {
    fullPage?: boolean;
    extra?: ContextoCaptura;
    tags?: string[];
}

interface CapturaMetadata {
    ordem: number;
    arquivo: string;
    categoria: string;
    nome: string;
    rota: string;
    titulo: string;
    tags?: string[];
    viewport?: { largura: number; altura: number };
    contexto?: ContextoCaptura;
}

interface DocumentoCapturasMetadata {
    versaoEsquema: 1;
    geradoEm: string;
    baseUrl: string;
    viewportPadrao: { largura: number; altura: number };
    capturas: CapturaMetadata[];
}

const capturasMetadata: CapturaMetadata[] = [];
let baseUrlMetadata = '';
let viewportPadraoMetadata: { largura: number; altura: number } | null = null;

// Diretório onde as screenshots serão salvas
const SCREENSHOTS_DIR = path.join(process.cwd(), 'screenshots');
const METADATA_PATH = path.join(SCREENSHOTS_DIR, 'capturas-metadata.json');
type ProcessoCleanup = ReturnType<typeof useProcessoCleanup>;

// Criar diretório se não existir, tratando erro de permissão no Windows
try {
    if (fs.existsSync(SCREENSHOTS_DIR)) {
        // No Windows, rmSync pode falhar se a pasta estiver aberta no Explorer ou sendo indexada
        fs.rmSync(SCREENSHOTS_DIR, {recursive: true, force: true});
    }
} catch {
    console.warn(`Aviso: Não foi possível remover o diretório de screenshots (EPERM). Tentando continuar...`);
}

if (!fs.existsSync(SCREENSHOTS_DIR)) {
    try {
        fs.mkdirSync(SCREENSHOTS_DIR, {recursive: true});
    } catch (error) {
        console.error(`Erro crítico: Não foi possível criar o diretório de screenshots: ${error instanceof Error ? error.message : String(error)}`);
    }
}

/**
 * Helper para capturar screenshot com nome organizado
 */
async function capturarTela(
    page: Page,
    categoria: string,
    nome: string,
    opcoes?: OpcoesCapturaTela
) {
    const url = page.url();
    const titulo = await page.title();
    const isFullPage = opcoes?.fullPage ?? true;
    const viewport = page.viewportSize() || {width: 0, height: 0};
    const rota = extrairRota(url);
    const viewportAtual = {largura: viewport.width, altura: viewport.height};

    if (!baseUrlMetadata) {
        baseUrlMetadata = extrairBaseUrl(url);
    }
    viewportPadraoMetadata ??= viewportAtual;

    const nomeArquivo = `${categoria}--${nome}.png`;
    const caminhoCompleto = path.join(SCREENSHOTS_DIR, nomeArquivo);

    const scrollOriginal = await prepararPaginaParaCaptura(page, isFullPage);
    await aguardarInterfaceEstavelParaCaptura(page);

    try {
        await page.screenshot({
            path: caminhoCompleto,
            fullPage: isFullPage
        });
    } finally {
        await restaurarScrollAposCaptura(page, scrollOriginal);
    }

    const metadata: CapturaMetadata = {
        ordem: capturasMetadata.length + 1,
        arquivo: nomeArquivo,
        categoria,
        nome,
        rota,
        titulo
    };

    if (opcoes?.tags && opcoes.tags.length > 0) {
        metadata.tags = opcoes.tags;
    }
    if (viewportPadraoMetadata.largura !== viewportAtual.largura || viewportPadraoMetadata.altura !== viewportAtual.altura) {
        metadata.viewport = viewportAtual;
    }
    if (opcoes?.extra && Object.keys(opcoes.extra).length > 0) {
        metadata.contexto = opcoes.extra;
    }

    capturasMetadata.push(metadata);

}

function extrairRota(url: string): string {
    try {
        const urlObj = new URL(url);
        return urlObj.pathname + urlObj.search;
    } catch {
        return url;
    }
}

function extrairBaseUrl(url: string): string {
    try {
        return new URL(url).origin;
    } catch {
        return '';
    }
}

function montarDocumentoCapturasMetadata(): DocumentoCapturasMetadata {
    return {
        versaoEsquema: 1,
        geradoEm: new Date().toISOString(),
        baseUrl: baseUrlMetadata,
        viewportPadrao: viewportPadraoMetadata ?? {largura: 0, altura: 0},
        capturas: capturasMetadata
    };
}

async function prepararPaginaParaCaptura(
    page: Page,
    isFullPage: boolean
): Promise<{ x: number; y: number } | null> {
    if (!isFullPage) {
        return null;
    }

    return page.evaluate(() => {
        const estiloId = 'sgc-captura-fullpage-style';
        if (!document.getElementById(estiloId)) {
            const estilo = document.createElement('style');
            estilo.id = estiloId;
            estilo.textContent = '.navbar.sticky-top { position: static !important; top: auto !important; }';
            document.head.appendChild(estilo);
        }

        const scrollOriginal = {x: globalThis.scrollX, y: globalThis.scrollY};
        globalThis.scrollTo(0, 0);
        return scrollOriginal;
    });
}

async function restaurarScrollAposCaptura(
    page: Page,
    scrollOriginal: { x: number; y: number } | null
): Promise<void> {
    if (!scrollOriginal || (scrollOriginal.x === 0 && scrollOriginal.y === 0)) {
        await page.evaluate(() => {
            document.getElementById('sgc-captura-fullpage-style')?.remove();
        });
        return;
    }

    await page.evaluate(({x, y}) => {
        document.getElementById('sgc-captura-fullpage-style')?.remove();
        globalThis.scrollTo(x, y);
    }, scrollOriginal);
}

async function aguardarPinturaEstavel(page: Page, quadros = 2): Promise<void> {
    for (let indice = 0; indice < quadros; indice += 1) {
        await page.evaluate(() => new Promise<void>((resolve) => {
            globalThis.requestAnimationFrame(() => resolve());
        }));
    }
}

async function aguardarInterfaceEstavelParaCaptura(page: Page): Promise<void> {
    await aguardarPinturaEstavel(page);
    await aguardarModaisEstaveis(page);
    await aguardarPinturaEstavel(page);
}

async function aguardarModaisEstaveis(page: Page): Promise<void> {
    const duracaoMs = await page.evaluate(() => {
        const seletores = [
            '.modal.show',
            '.modal.show .modal-dialog',
            '.modal-backdrop.show',
            '[role="dialog"]:not([aria-hidden="true"])',
        ];

        const elementos = Array.from(document.querySelectorAll<HTMLElement>(seletores.join(',')))
            .filter((elemento) => {
                const estilo = globalThis.getComputedStyle(elemento);
                return estilo.display !== 'none' && estilo.visibility !== 'hidden';
            });

        if (elementos.length === 0) {
            return 0;
        }

        function converterTempoParaMs(valor: string): number {
            const valorTratado = valor.trim();
            if (valorTratado.endsWith('ms')) {
                return Number.parseFloat(valorTratado) || 0;
            }
            if (valorTratado.endsWith('s')) {
                return (Number.parseFloat(valorTratado) || 0) * 1000;
            }
            return Number.parseFloat(valorTratado) || 0;
        }

        function maiorTempo(listaCss: string): number {
            return Math.max(
                0,
                ...listaCss.split(',').map((valor) => converterTempoParaMs(valor))
            );
        }

        return Math.max(
            0,
            ...elementos.map((elemento) => {
                const estilo = globalThis.getComputedStyle(elemento);
                return maiorTempo(estilo.transitionDuration)
                    + maiorTempo(estilo.transitionDelay)
                    + maiorTempo(estilo.animationDuration)
                    + maiorTempo(estilo.animationDelay);
            })
        );
    });

    if (duracaoMs <= 0) {
        return;
    }

    await page.waitForTimeout(Math.min(Math.ceil(duracaoMs) + 50, 1000));
}

function registrarProcessoParaCleanup(cleanup: ProcessoCleanup, codigo: number): void {
    expect(codigo, 'Código de processo inválido para cleanup').toBeGreaterThan(0);
    cleanup.registrar(codigo);
}

async function criarProcessoMapeamentoIniciadoPorFixture(
    request: APIRequestContext,
    cleanup: ReturnType<typeof useProcessoCleanup>,
    descricao: string,
    unidadeSigla: string
): Promise<number> {
    const response = await request.post('/e2e/fixtures/processo-mapeamento', {
        data: {
            descricao,
            unidadeSigla,
            iniciar: true,
            diasLimite: 30
        }
    });

    if (!response.ok()) {
        throw new Error(`Falha ao criar fixture de processo iniciado: ${response.status()} ${await response.text()}`);
    }
    const processo = await response.json() as { codigo: number };
    cleanup.registrar(processo.codigo);
    return processo.codigo;
}

async function criarProcessoMapeamentoComMapaDisponibilizadoPorFixture(
    request: APIRequestContext,
    cleanup: ReturnType<typeof useProcessoCleanup>,
    descricao: string,
    unidadeSigla: string
): Promise<number> {
    const response = await request.post('/e2e/fixtures/processo-mapeamento-com-mapa-disponibilizado', {
        data: {
            descricao,
            unidadeSigla,
            iniciar: true,
            diasLimite: 30
        }
    });

    if (!response.ok()) {
        throw new Error(`Falha ao criar fixture de mapa disponibilizado: ${response.status()} ${await response.text()}`);
    }
    const processo = await response.json() as { codigo: number };
    cleanup.registrar(processo.codigo);
    return processo.codigo;
}

async function criarProcessoMapeamentoComMapaValidadoPorFixture(
    request: APIRequestContext,
    cleanup: ReturnType<typeof useProcessoCleanup>,
    descricao: string,
    unidadeSigla: string
): Promise<number> {
    const response = await request.post('/e2e/fixtures/processo-mapeamento-com-mapa-validado', {
        data: {
            descricao,
            unidadeSigla,
            iniciar: true,
            diasLimite: 30
        }
    });

    if (!response.ok()) {
        throw new Error(`Falha ao criar fixture de mapa validado: ${response.status()} ${await response.text()}`);
    }
    const processo = await response.json() as { codigo: number };
    cleanup.registrar(processo.codigo);
    return processo.codigo;
}

async function criarProcessoMapeamentoComCadastroDisponibilizadoPorFixture(
    request: APIRequestContext,
    cleanup: ReturnType<typeof useProcessoCleanup>,
    descricao: string,
    unidadeSigla: string
): Promise<number> {
    const response = await request.post('/e2e/fixtures/processo-mapeamento-com-cadastro-disponibilizado', {
        data: {
            descricao,
            unidadeSigla,
            iniciar: true,
            diasLimite: 30
        }
    });

    if (!response.ok()) {
        throw new Error(`Falha ao criar fixture de cadastro disponibilizado: ${response.status()} ${await response.text()}`);
    }
    const processo = await response.json() as { codigo: number };
    cleanup.registrar(processo.codigo);
    return processo.codigo;
}

async function criarProcessoMapeamentoComCadastroHomologadoPorFixture(
    request: APIRequestContext,
    cleanup: ReturnType<typeof useProcessoCleanup>,
    descricao: string,
    unidadeSigla: string
): Promise<number> {
    const response = await request.post('/e2e/fixtures/processo-mapeamento-com-cadastro-homologado', {
        data: {
            descricao,
            unidadeSigla,
            iniciar: true,
            diasLimite: 30
        }
    });

    if (!response.ok()) {
        throw new Error(`Falha ao criar fixture de cadastro homologado: ${response.status()} ${await response.text()}`);
    }
    const processo = await response.json() as { codigo: number };
    cleanup.registrar(processo.codigo);
    return processo.codigo;
}

async function criarProcessoRevisaoComCadastroHomologadoPorFixture(
    request: APIRequestContext,
    cleanup: ReturnType<typeof useProcessoCleanup>,
    descricao: string,
    unidadeSigla: string
): Promise<number> {
    const response = await request.post('/e2e/fixtures/processo-revisao-com-cadastro-homologado', {
        data: {
            descricao,
            unidadeSigla,
            iniciar: true,
            diasLimite: 30
        }
    });

    if (!response.ok()) {
        throw new Error(`Falha ao criar fixture de revisão com cadastro homologado: ${response.status()} ${await response.text()}`);
    }
    const processo = await response.json() as { codigo: number };
    cleanup.registrar(processo.codigo);
    return processo.codigo;
}

async function criarProcessoMapeamentoComMapaHomologadoPorFixture(
    request: APIRequestContext,
    cleanup: ReturnType<typeof useProcessoCleanup>,
    descricao: string,
    unidadeSigla: string
): Promise<number> {
    const response = await request.post('/e2e/fixtures/processo-mapeamento-com-mapa-homologado', {
        data: {
            descricao,
            unidadeSigla,
            iniciar: true,
            diasLimite: 30
        }
    });

    if (!response.ok()) {
        throw new Error(`Falha ao criar fixture de mapa homologado: ${response.status()} ${await response.text()}`);
    }
    const processo = await response.json() as { codigo: number };
    cleanup.registrar(processo.codigo);
    return processo.codigo;
}

async function criarProcessoMapeamentoComMapaComSugestoesPorFixture(
    request: APIRequestContext,
    cleanup: ReturnType<typeof useProcessoCleanup>,
    descricao: string,
    unidadeSigla: string
): Promise<number> {
    const response = await request.post('/e2e/fixtures/processo-mapeamento-com-mapa-com-sugestoes', {
        data: {
            descricao,
            unidadeSigla,
            iniciar: true,
            diasLimite: 30
        }
    });

    if (!response.ok()) {
        throw new Error(`Falha ao criar fixture de mapa com sugestões: ${response.status()} ${await response.text()}`);
    }
    const processo = await response.json() as { codigo: number };
    cleanup.registrar(processo.codigo);
    return processo.codigo;
}

test.describe('Captura de Telas - Sistema SGC', () => {
    let cleanup: ReturnType<typeof useProcessoCleanup>;

    test.beforeAll(async ({request}) => {
        await resetDatabase(request);
    });

    test.beforeEach(async () => {
        cleanup = useProcessoCleanup();
    });

    test.afterEach(async ({request}) => {
        if (cleanup) {
            await cleanup.limpar(request);
        }
    });

    test.afterAll(async () => {
        if (capturasMetadata.length > 0) {
            fs.writeFileSync(METADATA_PATH, JSON.stringify(montarDocumentoCapturasMetadata(), null, 2));
        }
    });

    test.describe('01 - Autenticação', () => {
        test('Captura telas de login', async ({page}) => {
            await page.goto('/login');
            // Tela de login inicial
            await capturarTela(page, 'seguranca', 'login-inicial', {
                tags: ['login', 'inicial']
            });
            await page.getByTestId('inp-login-usuario').click();
            await capturarTela(page, 'seguranca', 'login-campo-usuario-foco', {
                tags: ['login', 'interacao']
            });

            // Erro de credenciais inválidas
            await page.getByTestId('inp-login-usuario').fill(USUARIOS.INVALIDO.titulo);
            await capturarTela(page, 'seguranca', 'login-usuario-preenchido', {
                tags: ['login', 'preenchimento']
            });
            await page.getByTestId('inp-login-senha').fill(USUARIOS.INVALIDO.senha);
            await page.getByTestId('btn-login-entrar').click();
            await verificarAppAlert(page);
            await capturarTela(page, 'seguranca', 'login-erro-credenciais', {
                tags: ['login', 'erro', 'validacao'],
                extra: {erro: 'Credenciais inválidas'}
            });

            // Limpar e fazer login com múltiplos perfis
            await page.getByTestId('inp-login-usuario').fill(USUARIOS.ADMIN_2_PERFIS.titulo);
            await page.getByTestId('inp-login-senha').fill(USUARIOS.ADMIN_2_PERFIS.senha);
            await page.getByTestId('btn-login-entrar').click();
            await expect(page.getByTestId('sel-login-perfil')).toBeVisible();
            await capturarTela(page, 'seguranca', 'login-selecao-perfil', {
                tags: ['login', 'multi-perfil'],
                extra: {usuario: USUARIOS.ADMIN_2_PERFIS.titulo}
            });

            // Login com perfil selecionado
            await loginComPerfil(page, USUARIOS.ADMIN_2_PERFIS.titulo, USUARIOS.ADMIN_2_PERFIS.senha, USUARIOS.ADMIN_2_PERFIS.perfil);
            await capturarTela(page, 'seguranca', 'painel-apos-login', {
                fullPage: true,
                tags: ['login', 'sucesso', 'dashboard'],
                extra: {perfil: USUARIOS.ADMIN_2_PERFIS.perfil}
            });
        });
    });

    test.describe('02 - Painel principal', () => {
        test('Captura painel ADMIN', async ({page}) => {
            await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);

            // Criar um processo para popular o painel
            const descricaoProcesso = `Processo captura ${Date.now()}`;
            await page.getByTestId('btn-painel-criar-processo').click();
            await expect(page).toHaveURL(/\/processo\/cadastro/);
            await capturarTela(page, 'painel', 'criar-processo-form-vazio', {
                extra: {perfil: 'ADMIN'},
                tags: ['form', 'vazio']
            });

            await page.getByTestId('inp-processo-descricao').fill(descricaoProcesso);
            await page.getByTestId('sel-processo-tipo').selectOption('MAPEAMENTO');
            const dataLimite = new Date();
            dataLimite.setDate(dataLimite.getDate() + 30);
            await page.getByTestId('inp-processo-data-limite').fill(dataLimite.toISOString().split('T')[0]);

            // Expandir árvore de unidades
            await expect(page.getByText('Carregando unidades...')).toBeHidden();
            await page.getByTestId('btn-arvore-expand-SECRETARIA_1').click();
            await expect(page.getByTestId('btn-arvore-expand-COORD_11')).toBeVisible();
            await capturarTela(page, 'painel', 'arvore-unidades-expandida', {
                extra: {perfil: 'ADMIN', acao: 'expandir-arvore'}
            });

            // Expandir COORD_11 para acessar SECAO_111
            await page.getByTestId('btn-arvore-expand-COORD_11').click();
            await expect(page.getByTestId('chk-arvore-unidade-SECAO_111')).toBeVisible();

            // Selecionar múltiplas unidades
            await page.getByTestId('chk-arvore-unidade-ASSESSORIA_11').click();
            await page.getByTestId('chk-arvore-unidade-ASSESSORIA_12').click();
            await page.getByTestId('chk-arvore-unidade-SECAO_111').click();
            await capturarTela(page, 'painel', 'arvore-unidades-selecionada', {
                tags: ['selecao', 'unidades']
            });

            await page.getByTestId('btn-processo-salvar-rodape').click();
            await expect(page).toHaveURL(/\/painel/);
            await expect(page.getByTestId('tbl-processos')).toBeVisible();
            await expect(page.getByTestId('tbl-processos').getByText(descricaoProcesso).first()).toBeVisible();

            // Capturar ID para cleanup
            await page.getByTestId('tbl-processos').getByText(descricaoProcesso).first().click();
            await expect(page).toHaveURL(/codProcesso=\d+/);
            const codProcesso = await extrairProcessoCodigo(page);
            registrarProcessoParaCleanup(cleanup, codProcesso);
            await page.goto('/painel');

            // Painel com processo criado
            await capturarTela(page, 'painel', 'painel-admin-com-processo', {
                fullPage: true,
                extra: {perfil: 'ADMIN'}
            });

            // Hover em linha da tabela
            await page.getByText(descricaoProcesso).hover();
            await capturarTela(page, 'painel', 'painel-hover-processo', {
                tags: ['interacao', 'hover']
            });
        });

        test('Captura painel GESTOR', async ({page}) => {
            // Criar processo para o Gestor primeiro como ADMIN
            await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
            const desc = `Processo gestor ${Date.now()}`;
            await criarProcesso(page, {
                descricao: desc,
                tipo: 'MAPEAMENTO',
                diasLimite: 30,
                unidade: 'COORD_11', // Unidade do Gestor COORD_11
                expandir: ['SECRETARIA_1'],
                iniciar: true
            });

            await login(page, USUARIOS.GESTOR_COORD.titulo, USUARIOS.GESTOR_COORD.senha);
            await expect(page.getByTestId('tbl-processos')).toBeVisible();
            await expect(page.getByTestId('tbl-processos').getByText(desc).first()).toBeVisible();
            await capturarTela(page, 'painel', 'painel-gestor', {
                fullPage: true,
                extra: {perfil: 'GESTOR', unidade: 'COORD_11'}
            });
        });

        test('Captura painel CHEFE', async ({page, request}) => {
            const desc = `Processo chefe ${Date.now()}`;
            await criarProcessoMapeamentoIniciadoPorFixture(request, cleanup, desc, 'SECAO_211');

            await login(page, USUARIOS.CHEFE_SECAO_211.titulo, USUARIOS.CHEFE_SECAO_211.senha);
            await expect(page.getByTestId('tbl-processos')).toBeVisible();
            await expect(page.getByTestId('tbl-processos').getByText(desc).first()).toBeVisible();
            await capturarTela(page, 'painel', 'painel-chefe', {
                fullPage: true,
                extra: {perfil: 'CHEFE', unidade: 'SECAO_211'}
            });
        });
    });

    test.describe('03 - Fluxo de Processo', () => {
        test('Captura criação e detalhamento de processo', async ({page}) => {
            await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);

            const timestamp = Date.now();
            const UNIDADE_ALVO = 'ASSESSORIA_12';
            const descricao = `Processo detalhado ${timestamp}`;

            await criarProcesso(page, {
                descricao,
                tipo: 'MAPEAMENTO',
                diasLimite: 30,
                unidade: UNIDADE_ALVO,
                expandir: ['SECRETARIA_1']
            });

            // Capturar ID para cleanup
            await page.getByTestId('tbl-processos').getByText(descricao).first().click();
            const codProcesso = await extrairProcessoCodigo(page);
            registrarProcessoParaCleanup(cleanup, codProcesso);

            // Tela de edição de processo
            await capturarTela(page, 'processo', 'processo-edicao', {
                extra: {perfil: 'ADMIN', estado: 'CRIADO'}
            });

            // Modal de iniciar processo
            await page.getByTestId('btn-processo-iniciar-rodape').click();
            await expect(page.getByRole('dialog')).toBeVisible();
            await capturarTela(page, 'processo', 'modal-iniciar-processo', {
                tags: ['modal', 'confirmacao'],
                extra: {acao: 'iniciar-processo'}
            });
            await confirmarInicioProcessoPeloDialogo(page, {
                descricao,
                tipo: 'MAPEAMENTO'
            });

            // Acessar detalhes do processo iniciado
            await page.getByTestId('tbl-processos').getByText(descricao).first().click();
            await expect(page).toHaveURL(/\/processo\/\d+/);
            await capturarTela(page, 'processo', 'detalhes-processo-iniciado', {
                fullPage: true,
                extra: {perfil: 'ADMIN', estado: 'EM_ANDAMENTO'}
            });
        });

        test('Captura fluxo de cadastro ate mapa disponibilizado', async ({page, request}) => {
            const timestamp = Date.now();
            const unidadeAlvo = 'ASSESSORIA_12';
            const descricao = `Processo cadastro ${timestamp}`;
            const atividade = `Atividade ${timestamp}`;
            const conhecimento = `Conhecimento ${timestamp}`;
            const competencia = `Competência ${timestamp}`;

            const processoCodigo = await criarProcessoMapeamentoIniciadoPorFixture(request, cleanup, descricao, unidadeAlvo);
            await login(page, USUARIOS.CHEFE_ASSESSORIA_12.titulo, USUARIOS.CHEFE_ASSESSORIA_12.senha);
            await page.goto(`/processo/${processoCodigo}/${unidadeAlvo}`);
            await expect(page).toHaveURL(new RegExp(String.raw`/processo/${processoCodigo}/${unidadeAlvo}(?:\?.*)?$`));
            await navegarParaCadastro(page);
            await adicionarAtividade(page, atividade);
            await adicionarConhecimento(page, atividade, conhecimento);
            await capturarTela(page, 'processo', 'cadastro-chefe', {
                extra: {perfil: 'CHEFE', unidade: unidadeAlvo, acao: 'cadastro-atividades'}
            });
            await disponibilizarCadastro(page);

            await loginComPerfil(page, USUARIOS.GESTOR_SECRETARIA_1.titulo, USUARIOS.GESTOR_SECRETARIA_1.senha, USUARIOS.GESTOR_SECRETARIA_1.perfil);
            await page.goto(`/processo/${processoCodigo}/${unidadeAlvo}`);
            await expect(page).toHaveURL(new RegExp(String.raw`/processo/${processoCodigo}/${unidadeAlvo}(?:\?.*)?$`));
            await navegarParaCadastro(page);
            await capturarTela(page, 'processo', 'analise-gestor', {
                extra: {perfil: 'GESTOR', unidade: unidadeAlvo, acao: 'analise-cadastro'}
            });
            await (await abrirAcaoCadastroPrincipal(page)).click();
            await expect(page.getByRole('dialog')).toBeVisible();
            await capturarTela(page, 'processo', 'modal-aceite-gestor', {
                tags: ['modal', 'aceite']
            });
            await page.getByTestId('inp-aceite-cadastro-obs').fill('Aceite para captura de tela');
            await page.getByTestId('btn-aceite-cadastro-confirmar').click();
            await verificarPaginaPainel(page);

            await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
            await page.goto(`/processo/${processoCodigo}/${unidadeAlvo}`);
            await expect(page).toHaveURL(new RegExp(String.raw`/processo/${processoCodigo}/${unidadeAlvo}(?:\?.*)?$`));
            await navegarParaCadastro(page);
            await (await abrirAcaoCadastroPrincipal(page)).click();
            await expect(page.getByRole('dialog')).toBeVisible();
            await capturarTela(page, 'processo', 'modal-homologacao-admin', {
                tags: ['modal', 'homologacao'],
                extra: {perfil: 'ADMIN'}
            });
            await page.getByTestId('inp-aceite-cadastro-obs').fill('Homologação para captura');
            await page.getByTestId('btn-aceite-cadastro-confirmar').click();
            await expect(page).toHaveURL(new RegExp(String.raw`/processo/${processoCodigo}/${unidadeAlvo}(?:\?.*)?$`));
            await expect(page.getByTestId('header-subprocesso')).toBeVisible();

            await navegarParaMapa(page);
            await abrirModalCriarCompetencia(page);
            await page.getByTestId('inp-criar-competencia-descricao').fill(competencia);
            await page.getByRole('checkbox', {name: atividade}).check();
            await page.getByTestId('btn-criar-competencia-salvar').click();
            await expect(page.locator('.competencia-card', {has: page.getByText(competencia)})).toBeVisible();
            await capturarTela(page, 'processo', 'mapa-criado', {
                extra: {perfil: 'ADMIN', acao: 'edicao-mapa'}
            });
            await disponibilizarMapa(page);
        });

        test('Captura validacao do mapa', async ({page, request}) => {
            const timestamp = Date.now();
            const unidadeAlvo = 'ASSESSORIA_12';
            const descricao = `Processo validacao mapa ${timestamp}`;

            const processoCodigo = await criarProcessoMapeamentoComMapaDisponibilizadoPorFixture(
                request,
                cleanup,
                descricao,
                unidadeAlvo
            );

            await login(page, USUARIOS.CHEFE_ASSESSORIA_12.titulo, USUARIOS.CHEFE_ASSESSORIA_12.senha);
            await page.goto(`/processo/${processoCodigo}/${unidadeAlvo}`);
            await expect(page).toHaveURL(new RegExp(String.raw`/processo/${processoCodigo}/${unidadeAlvo}(?:\?.*)?$`));
            await navegarParaMapa(page);
            await MapaHelpers.abrirValidacaoMapa(page);
            await expect(page.getByRole('dialog')).toBeVisible();
            await capturarTela(page, 'processo', 'modal-validar-mapa', {
                tags: ['modal', 'validacao-mapa'],
                extra: {perfil: 'CHEFE'}
            });
            await page.getByTestId('btn-validar-mapa-confirmar').click();
            await verificarPaginaPainel(page);
        });

        test('Captura processo homologado e finalizacao', async ({page, request}) => {
            const timestamp = Date.now();
            const unidadeAlvo = 'SECAO_121';
            const descricao = `Processo finalizacao ${timestamp}`;

            const processoCodigo = await criarProcessoMapeamentoComMapaHomologadoPorFixture(
                request,
                cleanup,
                descricao,
                unidadeAlvo
            );

            await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
            await page.goto(`/processo/${processoCodigo}`);
            await expect(page).toHaveURL(new RegExp(String.raw`/processo/${processoCodigo}(?:\?.*)?$`));
            await capturarTela(page, 'processo', 'detalhes-processo-finalizavel', {
                fullPage: true,
                extra: {perfil: 'ADMIN', acao: 'finalizacao-processo'}
            });

            // Modal de finalizar processo
            await page.getByTestId('btn-processo-finalizar').click();
            await expect(page.getByRole('dialog')).toBeVisible();
            await capturarTela(page, 'processo', 'modal-finalizar-processo', {
                tags: ['modal', 'finalizar']
            });
            await page.getByRole('button', {name: 'Cancelar'}).click();
            await capturarTela(page, 'processo', 'detalhes-processo-apos-cancelar-finalizacao', {
                fullPage: true
            });
        });
    });

    test.describe('04 - Subprocesso e Atividades', () => {
        test('Captura fluxo completo de atividades (incluindo validações de form)', async ({page}) => {
            const descricao = `Proc atividades ${Date.now()}`;
            const UNIDADE_ALVO = 'SECAO_211';

            await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
            await page.getByTestId('btn-painel-criar-processo').click();
            await expect(page).toHaveURL(/\/processo\/cadastro/);

            // Estado inicial vazio
            await expect(page.getByTestId('btn-processo-salvar-rodape')).toBeDisabled();
            await capturarTela(page, 'processo', 'botoes-desativados-form-vazio', {
                tags: ['validacao', 'form'],
                extra: {motivo: 'falta-campos-obrigatorios'}
            });

            await page.getByTestId('inp-processo-descricao').fill(descricao);
            await expect(page.getByTestId('btn-processo-salvar-rodape')).toBeDisabled();
            await capturarTela(page, 'processo', 'botoes-desativados-falta-campos', {
                extra: {preenchido: 'descricao'}
            });

            await page.getByTestId('sel-processo-tipo').selectOption('MAPEAMENTO');

            const dataLimite = new Date();
            dataLimite.setDate(dataLimite.getDate() + 30);
            await page.getByTestId('inp-processo-data-limite').fill(dataLimite.toISOString().split('T')[0]);
            await expect(page.getByTestId('btn-processo-salvar-rodape')).toBeDisabled();
            await capturarTela(page, 'processo', 'botoes-desativados-falta-unidade', {
                extra: {preenchido: ['descricao', 'tipo', 'data']}
            });

            // Selecionar unidade e validar ativação
            await expect(page.getByText('Carregando unidades...')).toBeHidden();
            await page.getByTestId('btn-arvore-expand-SECRETARIA_2').click();
            await expect(page.getByTestId('btn-arvore-expand-COORD_21')).toBeVisible();
            await page.getByTestId('btn-arvore-expand-COORD_21').click();
            await expect(page.getByTestId('chk-arvore-unidade-SECAO_211')).toBeVisible();
            await page.getByTestId('chk-arvore-unidade-SECAO_211').click();
            await expect(page.getByTestId('btn-processo-salvar-rodape')).toBeEnabled();
            await capturarTela(page, 'processo', 'botoes-ativados-form-completo', {
                tags: ['validacao', 'sucesso']
            });

            await page.getByTestId('btn-processo-iniciar-rodape').click();
            await confirmarInicioProcessoPeloDialogo(page, {
                descricao,
                tipo: 'MAPEAMENTO'
            });

            // Registrar para cleanup
            const linhaProcesso = page.getByTestId('tbl-processos').locator('tr').filter({has: page.getByText(descricao)});
            await linhaProcesso.click();
            await page.waitForURL(/\/processo\/\d+/);
            const codProcesso = await extrairProcessoCodigo(page);
            registrarProcessoParaCleanup(cleanup, codProcesso);

            await login(page, USUARIOS.CHEFE_SECAO_211.titulo, USUARIOS.CHEFE_SECAO_211.senha);
            await page.getByTestId('tbl-processos').getByText(descricao).first().click();
            await navegarParaSubprocesso(page, UNIDADE_ALVO);
            await capturarTela(page, 'subprocesso', 'dashboard-subprocesso', {
                fullPage: true,
                extra: {perfil: 'CHEFE', unidade: UNIDADE_ALVO}
            });

            // Entrar em atividades
            await navegarParaCadastro(page);
            await capturarTela(page, 'subprocesso', 'cadastro-atividades-vazio', {
                fullPage: true,
                tags: ['atividades', 'vazio']
            });

            const atividadeDesc = `Atividade teste ${Date.now()}`;
            await page.getByTestId('inp-nova-atividade').fill(atividadeDesc);
            await capturarTela(page, 'subprocesso', 'cadastro-atividades-preenchendo', {
                tags: ['interacao']
            });
            await page.getByTestId('btn-adicionar-atividade').click();
            await expect(page.getByText(atividadeDesc, {exact: true})).toBeVisible();
            await capturarTela(page, 'subprocesso', 'cadastro-atividades-com-uma', {
                fullPage: true,
                extra: {atividade: atividadeDesc}
            });

            const card = page.locator('.atividade-card', {has: page.getByText(atividadeDesc)});
            await card.getByTestId('inp-novo-conhecimento').fill('Java');
            await capturarTela(page, 'subprocesso', 'cadastro-conhecimento-preenchendo', {
                tags: ['conhecimento', 'edicao']
            });
            await adicionarConhecimento(page, atividadeDesc, 'Java');
            await adicionarConhecimento(page, atividadeDesc, 'Spring Boot');
            await adicionarConhecimento(page, atividadeDesc, 'Oracle');
            await capturarTela(page, 'subprocesso', 'cadastro-atividade-completa', {
                fullPage: true,
                extra: {conhecimentos: ['Java', 'Spring Boot', 'Oracle']}
            });

            // Hover na atividade para ver ações
            await card.locator('.atividade-hover-row').hover();
            await aguardarPinturaEstavel(page);
            await capturarTela(page, 'subprocesso', 'cadastro-atividade-hover', {
                tags: ['interacao', 'hover']
            });

            // Tentar disponibilizar sem data
            await page.getByTestId('btn-cad-atividades-disponibilizar').click();
            await expect(page.getByRole('dialog')).toBeVisible();
            await capturarTela(page, 'subprocesso', 'modal-disponibilizar-atividades', {
                tags: ['modal', 'disponibilizar']
            });
            await page.getByTestId('btn-confirmar-disponibilizacao').click();
            await verificarPaginaPainel(page);
            await capturarTela(page, 'subprocesso', 'cadastro-atividades-disponibilizado', {
                fullPage: true,
                extra: {estado: 'DISPONIBILIZADO'}
            });
        });

        test('Captura estados de validação inline de atividades', async ({page}) => {
            const descricao = `Proc validação ${Date.now()}`;
            const UNIDADE_ALVO = 'SECAO_212';

            await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);

            await criarProcesso(page, {
                descricao,
                tipo: 'MAPEAMENTO',
                diasLimite: 30,
                unidade: UNIDADE_ALVO,
                expandir: ['SECRETARIA_2', 'COORD_21']
            });

            const linhaProcesso = page.getByTestId('tbl-processos').locator('tr').filter({has: page.getByText(descricao)});
            await linhaProcesso.click();
            const codProcesso = await extrairProcessoCodigo(page);
            registrarProcessoParaCleanup(cleanup, codProcesso);

            await iniciarProcessoPeloCadastro(page, {
                descricao,
                tipo: 'MAPEAMENTO'
            });

            await login(page, USUARIOS.CHEFE_SECAO_212.titulo, USUARIOS.CHEFE_SECAO_212.senha);

            await page.getByTestId('tbl-processos').getByText(descricao).first().click();
            await navegarParaSubprocesso(page, UNIDADE_ALVO);
            await navegarParaCadastro(page);

            // Capturar tela inicial vazia com label "Conhecimentos *"
            await capturarTela(page, 'subprocesso', 'cadastro-vazio-com-label-obrigatorio', {
                fullPage: true,
                tags: ['ui-clue', 'obrigatorio']
            });

            // Adicionar primeira atividade SEM conhecimento
            const atividade1 = `Atividade sem conhecimento 1 ${Date.now()}`;
            await adicionarAtividade(page, atividade1);
            await expect(page.locator('.atividade-card', {has: page.getByText(atividade1)})).toBeVisible();
            await capturarTela(page, 'subprocesso', 'atividade-sem-conhecimento', {
                fullPage: true,
                extra: {erro: 'falta-conhecimento'}
            });

            // Adicionar segunda atividade SEM conhecimento
            const atividade2 = `Atividade sem conhecimento 2 ${Date.now()}`;
            await adicionarAtividade(page, atividade2);
            await expect(page.locator('.atividade-card', {has: page.getByText(atividade2)})).toBeVisible();

            // Adicionar terceira atividade COM conhecimento (para contraste)
            const atividade3 = `Atividade com conhecimento ${Date.now()}`;
            await adicionarAtividade(page, atividade3);
            await adicionarConhecimento(page, atividade3, 'Java');
            await expect(page.locator('.atividade-card', {has: page.getByText(atividade3)})).toContainText('Java');
            await capturarTela(page, 'subprocesso', 'mix-atividades-com-sem-conhecimento', {
                fullPage: true,
                tags: ['validacao', 'visual']
            });

            // Sem conhecimento em todas as atividades, o clique deve mostrar erro de validação
            const btnDisponibilizar = page.getByTestId('btn-cad-atividades-disponibilizar');
            await btnDisponibilizar.click();
            await expect(page.getByText(TEXTOS.atividades.ERRO_CADASTRO_INCOMPLETO)).toBeVisible();

            await capturarTela(page, 'subprocesso', 'validacao-ativa-sem-conhecimento', {
                fullPage: true,
                tags: ['validacao', 'notificacao']
            });

            // Scroll para segunda atividade sem conhecimento
            const card2 = page.locator('.atividade-card', {has: page.getByText(atividade2)});
            await card2.scrollIntoViewIfNeeded();
            await aguardarPinturaEstavel(page);
            await capturarTela(page, 'subprocesso', 'segunda-atividade-sem-conhecimento', {
                tags: ['scroll', 'validacao']
            });

            // Corrigir primeira atividade adicionando conhecimento
            await adicionarConhecimento(page, atividade1, 'Python');
            await expect(page.locator('.atividade-card', {has: page.getByText(atividade1)})).toContainText('Python');
            await capturarTela(page, 'subprocesso', 'erro-desaparece-apos-correcao', {
                fullPage: true,
                extra: {atividadeCorrigida: atividade1}
            });

            // Ainda deve mostrar erro de validação porque a atividade 2 continua sem conhecimento
            await btnDisponibilizar.click();
            await expect(page.getByText(TEXTOS.atividades.ERRO_CADASTRO_INCOMPLETO)).toBeVisible();
            await capturarTela(page, 'subprocesso', 'botao-disponibilizar-erro-atividade-restante', {
                extra: {pendente: atividade2}
            });

            // Corrigir segunda atividade
            await adicionarConhecimento(page, atividade2, 'JavaScript');
            await expect(page.locator('.atividade-card', {has: page.getByText(atividade2)})).toContainText('JavaScript');

            // Tentar disponibilizar - agora deve funcionar
            await page.getByTestId('btn-cad-atividades-disponibilizar').click();

            // Modal de confirmação deve aparecer
            const modalConfirmacao = page.locator('.modal-content').filter({hasText: 'Disponibilização do cadastro'});

            // Garantir que a modal está visível antes de capturar
            await expect(modalConfirmacao).toBeVisible();
            await capturarTela(page, 'subprocesso', 'modal-confirmacao-disponibilizacao', {
                tags: ['modal', 'sucesso']
            });
        });
    });

    test.describe('05 - Mapa de Competências', () => {
        test('Captura análise de cadastro antes do mapa', async ({page, request}) => {
            test.setTimeout(30000);
            const descricao = `Proc mapa cadastro ${Date.now()}`;
            const UNIDADE_ALVO = 'SECAO_121';
            const codProcesso = await criarProcessoMapeamentoComCadastroDisponibilizadoPorFixture(
                request,
                cleanup,
                descricao,
                UNIDADE_ALVO
            );

            await login(page, USUARIOS.GESTOR_COORD_12.titulo, USUARIOS.GESTOR_COORD_12.senha);
            await page.goto(`/processo/${codProcesso}/${UNIDADE_ALVO}`);
            await expect(page).toHaveURL(new RegExp(String.raw`/processo/${codProcesso}/${UNIDADE_ALVO}(?:\?.*)?$`));
            await navegarParaCadastro(page);
            await capturarTela(page, 'processo', 'cadastro-analise-gestor-coordenadoria', {
                fullPage: true,
                tags: ['cadastro', 'analise', 'somente-leitura'],
                extra: {perfil: 'GESTOR_COORD', acao: 'aceite-cadastro-1'}
            });
            await (await abrirAcaoCadastroPrincipal(page)).click();
            await page.getByTestId('inp-aceite-cadastro-obs').fill('Cadastro muito bem detalhado. Seguindo para a Secretaria.');
            await page.getByTestId('btn-aceite-cadastro-confirmar').click();
            await verificarPaginaPainel(page);

            await loginComPerfil(page, USUARIOS.GESTOR_SECRETARIA_1.titulo, USUARIOS.GESTOR_SECRETARIA_1.senha, USUARIOS.GESTOR_SECRETARIA_1.perfil);
            await page.goto(`/processo/${codProcesso}/${UNIDADE_ALVO}`);
            await expect(page).toHaveURL(new RegExp(String.raw`/processo/${codProcesso}/${UNIDADE_ALVO}(?:\?.*)?$`));
            await navegarParaCadastro(page);
            await capturarTela(page, 'processo', 'cadastro-analise-gestor-secretaria', {
                fullPage: true,
                tags: ['cadastro', 'analise', 'somente-leitura'],
                extra: {perfil: 'GESTOR_SEC', acao: 'aceite-cadastro-2'}
            });
        });

        test('Captura edição inicial do mapa de competências', async ({page, request}) => {
            test.setTimeout(30000);
            const descricao = `Proc mapa ${Date.now()}`;
            const UNIDADE_ALVO = 'SECAO_121';
            const codProcesso = await criarProcessoMapeamentoComCadastroHomologadoPorFixture(
                request,
                cleanup,
                descricao,
                UNIDADE_ALVO
            );

            await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
            await page.goto(`/processo/${codProcesso}/${UNIDADE_ALVO}`);
            await expect(page).toHaveURL(new RegExp(String.raw`/processo/${codProcesso}/${UNIDADE_ALVO}(?:\?.*)?$`));

            // Agora o Mapa deve estar habilitado para edição pelo Admin
            await navegarParaMapa(page);
            await capturarTela(page, 'mapa', 'mapa-vazio', {
                fullPage: true,
                tags: ['mapa', 'vazio'],
                extra: {perfil: 'ADMIN'}
            });

            await abrirModalCriarCompetencia(page);
            await expect(page.getByTestId('mdl-criar-competencia')).toBeVisible();
            await capturarTela(page, 'mapa', 'modal-criar-competencia', {
                tags: ['modal', 'criacao-competencia']
            });

            const competenciaDesc = 'Desenvolvimento de Software';
            await page.getByTestId('inp-criar-competencia-descricao').fill(competenciaDesc);
            await capturarTela(page, 'mapa', 'modal-criar-competencia-preenchida', {
                extra: {competencia: competenciaDesc}
            });
        });

        test('Captura criação e disponibilização do mapa de competências', async ({page, request}) => {
            test.setTimeout(30000);
            const descricao = `Proc mapa criacao ${Date.now()}`;
            const UNIDADE_ALVO = 'SECAO_121';
            const codProcesso = await criarProcessoMapeamentoComCadastroHomologadoPorFixture(
                request,
                cleanup,
                descricao,
                UNIDADE_ALVO
            );

            await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
            await page.goto(`/processo/${codProcesso}/${UNIDADE_ALVO}`);
            await expect(page).toHaveURL(new RegExp(String.raw`/processo/${codProcesso}/${UNIDADE_ALVO}(?:\?.*)?$`));
            await navegarParaMapa(page);

            await abrirModalCriarCompetencia(page);
            const modal = page.getByTestId('mdl-criar-competencia');
            await expect(modal).toBeVisible();
            const competenciaDesc = 'Desenvolvimento de Software';
            await page.getByTestId('inp-criar-competencia-descricao').fill(competenciaDesc);

            await modal.getByTestId('btn-competencia-selecionar-todas-atividades').click();
            await capturarTela(page, 'mapa', 'modal-criar-competencia-atividades-selecionadas', {
                tags: ['selecao', 'atividades']
            });

            await page.getByTestId('btn-criar-competencia-salvar').click();
            await expect(modal).toBeHidden();
            await capturarTela(page, 'mapa', 'mapa-com-competencia', {
                fullPage: true,
                extra: {competenciaCriada: competenciaDesc}
            });

            // Hover na competência
            const card = page.locator('.competencia-card', {has: page.getByText(competenciaDesc)});
            await card.hover();
            await aguardarPinturaEstavel(page);
            await capturarTela(page, 'mapa', 'mapa-competencia-hover', {
                tags: ['interacao', 'hover']
            });

            await expect(page.getByTestId('btn-mapa-acoes')).toBeVisible();
            await (await MapaHelpers.abrirAcaoMapa(page, 'btn-mapa-acao-disponibilizar')).click();
            await expect(page.getByTestId('mdl-disponibilizar-mapa')).toBeVisible();
            await capturarTela(page, 'mapa', 'modal-disponibilizar-mapa', {
                tags: ['modal', 'disponibilizar-mapa']
            });
        });

        test('Captura mapa disponibilizado e validação do chefe', async ({page, request}) => {
            test.setTimeout(30000);
            const descricao = `Proc mapa disponibilizado ${Date.now()}`;
            const UNIDADE_ALVO = 'SECAO_121';
            const codProcesso = await criarProcessoMapeamentoComMapaDisponibilizadoPorFixture(
                request,
                cleanup,
                descricao,
                UNIDADE_ALVO
            );

            await login(page, USUARIOS.CHEFE_SECAO_121.titulo, USUARIOS.CHEFE_SECAO_121.senha);
            await page.goto(`/processo/${codProcesso}/${UNIDADE_ALVO}`);
            await expect(page).toHaveURL(new RegExp(String.raw`/processo/${codProcesso}/${UNIDADE_ALVO}(?:\?.*)?$`));
            await navegarParaMapa(page);
            await capturarTela(page, 'mapa', 'mapa-disponibilizado', {
                fullPage: true,
                tags: ['mapa', 'analise', 'validacao', 'somente-leitura'],
                extra: {estado: 'MAPA_DISPONIBILIZADO', perfil: 'CHEFE', acao: 'validacao-mapa'}
            });

            await MapaHelpers.abrirValidacaoMapa(page);
            await expect(page.getByRole('dialog')).toContainText('Confirma a validação do mapa de competências?');
            await page.getByTestId('btn-validar-mapa-confirmar').click();
            await verificarPaginaPainel(page);
        });

        test('Captura análise de mapa por gestores', async ({page, request}) => {
            test.setTimeout(30000);
            const descricao = `Proc mapa analise ${Date.now()}`;
            const UNIDADE_ALVO = 'SECAO_121';
            const codProcesso = await criarProcessoMapeamentoComMapaValidadoPorFixture(
                request,
                cleanup,
                descricao,
                UNIDADE_ALVO
            );

            await login(page, USUARIOS.GESTOR_COORD_12.titulo, USUARIOS.GESTOR_COORD_12.senha);
            await page.goto(`/processo/${codProcesso}/${UNIDADE_ALVO}`);
            await expect(page).toHaveURL(new RegExp(String.raw`/processo/${codProcesso}/${UNIDADE_ALVO}(?:\?.*)?$`));
            await navegarParaMapa(page);
            await capturarTela(page, 'mapa', 'analise-gestor-coordenadoria', {
                fullPage: true,
                tags: ['mapa', 'analise', 'somente-leitura'],
                extra: {perfil: 'GESTOR_COORD', acao: 'aceite-mapa-1'}
            });
            await MapaHelpers.abrirAcaoPrincipalMapa(page);
            await expect(page.getByTestId('body-aceite-mapa')).toBeVisible();
            await page.getByTestId('inp-aceite-mapa-observacao').fill('Mapa consistente para seguir à Secretaria.');
            await page.getByTestId('btn-aceite-mapa-confirmar').click();
            await verificarPaginaPainel(page);

            await loginComPerfil(page, USUARIOS.GESTOR_SECRETARIA_1.titulo, USUARIOS.GESTOR_SECRETARIA_1.senha, USUARIOS.GESTOR_SECRETARIA_1.perfil);
            await page.goto(`/processo/${codProcesso}/${UNIDADE_ALVO}`);
            await expect(page).toHaveURL(new RegExp(String.raw`/processo/${codProcesso}/${UNIDADE_ALVO}(?:\?.*)?$`));
            await navegarParaMapa(page);
            await capturarTela(page, 'mapa', 'analise-gestor-secretaria', {
                fullPage: true,
                tags: ['mapa', 'analise', 'somente-leitura'],
                extra: {perfil: 'GESTOR_SEC', acao: 'aceite-mapa-2'}
            });
        });
    });

    test.describe('06 - Navegação e Menus', () => {
        test('Captura elementos de navegação', async ({page}) => {
            await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);

            await capturarTela(page, 'navegacao', 'menu-principal', {tags: ['layout', 'menu']});

            // Configs (se admin)
            await page.getByTestId('btn-configuracoes').click();
            await expect(page).toHaveURL(/\/config/);
            await capturarTela(page, 'navegacao', 'menu-configs', {extra: {secao: 'configuracoes'}});
            await page.goto('/painel');

            // Seção unidades (para ADMIN)
            await page.getByText('Unidades').first().click();
            await expect(page).toHaveURL(/\/unidades/);
            await capturarTela(page, 'navegacao', 'unidades', {fullPage: true});

            await page.getByText('Relatórios').click();
            await expect(page).toHaveURL(/\/relatorios/);
            await capturarTela(page, 'navegacao', 'relatorios', {fullPage: true});

            await page.getByText('Histórico').click();
            await expect(page).toHaveURL(/\/historico/);
            await capturarTela(page, 'navegacao', 'historico', {fullPage: true});

            await page.evaluate(() => window.scrollTo(0, document.body.scrollHeight));
            await aguardarPinturaEstavel(page);
            await capturarTela(page, 'navegacao', 'rodape', {tags: ['layout', 'footer']});
        });
    });

    test.describe('07 - Estados e Situações', () => {
        test('Captura diferentes estados de processo', async ({page}) => {
            await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);

            const processosCriado = `Proc CRIADO ${Date.now()}`;
            await criarProcesso(page, {
                descricao: processosCriado,
                tipo: 'MAPEAMENTO',
                diasLimite: 30,
                unidade: 'ASSESSORIA_11',
                expandir: ['SECRETARIA_1']
            });

            await page.getByText(processosCriado).click();
            await expect(page).toHaveURL(/codProcesso=|processo\/\d+/);
            const codProcesso1 = await extrairProcessoCodigo(page);
            registrarProcessoParaCleanup(cleanup, codProcesso1);
            await page.goto('/painel');

            await capturarTela(page, 'estados', 'processo-criado', {extra: {estado: 'CRIADO'}});

            const processosAndamento = `Proc ANDAMENTO ${Date.now()}`;
            await criarProcesso(page, {
                descricao: processosAndamento,
                tipo: 'REVISAO',
                diasLimite: 30,
                unidade: 'ASSESSORIA_12',
                expandir: ['SECRETARIA_1'],
                iniciar: true
            });


            await capturarTela(page, 'estados', 'processo-em-andamento', {extra: {estado: 'EM_ANDAMENTO'}});
        });
    });

    test.describe('08 - Responsividade (Tamanhos de Tela)', () => {
        test('Captura em diferentes resoluções', async ({page}) => {
            await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);

            // Desktop padrão (1920x1080)
            await page.setViewportSize({width: 1920, height: 1080});
            await capturarTela(page, 'responsividade', 'desktop-1920x1080', {fullPage: true, tags: ['res-high']});

            // Desktop médio (1366x768)
            await page.setViewportSize({width: 1366, height: 768});
            await capturarTela(page, 'responsividade', 'desktop-1366x768', {fullPage: true, tags: ['res-medium']});

            await page.setViewportSize({width: 768, height: 1024});
            await capturarTela(page, 'responsividade', 'tablet-768x1024', {fullPage: true, tags: ['res-tablet']});

            await page.setViewportSize({width: 375, height: 667});
            await capturarTela(page, 'responsividade', 'mobile-375x667', {fullPage: true, tags: ['res-mobile']});
        });
    });

    // SEÇÃO 09 - OPERAÇÕES EM BLOCO (CDUs 22-26)
    test.describe('09 - Operações em Bloco', () => {
        test('Captura fluxo de aceitar cadastros em bloco', async ({page, request}) => {
            test.setTimeout(30000); // Fluxo muito longo (múltiplos logins/logouts)

            const descricao = `Processo bloco ${Date.now()}`;
            // Prepara cenário: criar processo com cadastro disponibilizado 
            // COORD_22 tem SECAO_221 e é gerido por GESTOR_COORD_22
            const codProcesso = await criarProcessoMapeamentoComCadastroDisponibilizadoPorFixture(
                request,
                cleanup,
                descricao,
                'SECAO_221'
            );

            await login(page, USUARIOS.GESTOR_COORD_22.titulo, USUARIOS.GESTOR_COORD_22.senha);

            await page.goto(`/processo/${codProcesso}`);
            await expect(page.getByTestId('processo-info')).toBeVisible();
            await capturarTela(page, 'operacoes-bloco', 'detalhes-processo-gestor', {
                fullPage: true,
                extra: {perfil: 'GESTOR', acao: 'ver-bloco'}
            });

            // Capturar botão de aceitar em bloco (se visível)
            const btnAceitarBloco = await obterAcaoBloco(page, 'btn-processo-aceitar-bloco');
            await expect(btnAceitarBloco).toBeVisible();
            await btnAceitarBloco.click();
            await expect(page.getByRole('dialog')).toBeVisible();
            await capturarTela(page, 'operacoes-bloco', 'modal-aceitar-cadastro-bloco', {tags: ['modal', 'bloco']});
            await page.getByRole('button', {name: /Cancelar/i}).click();

            // Executar aceite real para mover subprocesso para Secretaria 2
            const btnAceitarBlocoConfirmacao = await obterAcaoBloco(page, 'btn-processo-aceitar-bloco');
            await btnAceitarBlocoConfirmacao.click();
            await page.getByRole('button', {name: TEXTOS.acaoBloco.aceitar.BOTAO}).click();
            await expect(page).toHaveURL(/\/painel/);

            // Login como Gestor da SECRETARIA_2 para aceitar e mover para o ADMIN
            await loginComPerfil(
                page,
                USUARIOS.CHEFE_SECRETARIA_2.titulo,
                USUARIOS.CHEFE_SECRETARIA_2.senha,
                'GESTOR - SECRETARIA_2'
            );
            await page.getByTestId('tbl-processos').getByText(descricao).first().click();
            await expect(page.getByTestId('processo-info')).toBeVisible();

            const btnAceitarBlocoSec2 = await obterAcaoBloco(page, 'btn-processo-aceitar-bloco');
            await expect(btnAceitarBlocoSec2).toBeVisible();
            await btnAceitarBlocoSec2.click();
            await page.getByRole('button', {name: TEXTOS.acaoBloco.aceitar.BOTAO}).click();
            await expect(page).toHaveURL(/\/painel/);

            // Login como Admin para homologar em bloco
            await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);

            await page.getByTestId('tbl-processos').getByText(descricao).first().click();
            await expect(page.getByTestId('processo-info')).toBeVisible();

            // Capturar botão de homologar cadastro em bloco (CDU-23)
            const btnHomologarBloco = await obterAcaoBloco(page, 'btn-processo-homologar-bloco');
            await expect(btnHomologarBloco).toBeVisible();
            await btnHomologarBloco.click();
            await expect(page.getByRole('dialog')).toBeVisible();
            await capturarTela(page, 'operacoes-bloco', 'modal-homologar-cadastro-bloco', {extra: {perfil: 'ADMIN'}});
            await page.getByRole('button', {name: /Cancelar/i}).click();

            // Capturar botão de disponibilizar mapas em bloco (CDU-24)
            const btnDisponibilizarMapaBloco = await obterAcaoBloco(page, 'btn-processo-disponibilizar-bloco');
            await expect(btnDisponibilizarMapaBloco).toBeVisible();
            await expect(btnDisponibilizarMapaBloco).toBeDisabled();

            // Capturar botões de aceitar/homologar mapa em bloco (CDU-25 e CDU-26 - se visíveis)
            const btnAceitarMapaBloco = page.getByRole('button', {name: /Aceitar.*mapa.*Bloco/i});
            await expect(btnAceitarMapaBloco).toBeHidden();

            const btnHomologarMapaBloco = await obterAcaoBloco(page, 'btn-processo-homologar-mapas-bloco');
            await expect(btnHomologarMapaBloco).toBeVisible();
            await expect(btnHomologarMapaBloco).toBeDisabled();
        });
    });

    // SEÇÃO 10 - GESTÃO DE SUBPROCESSOS (CDUs 27, 32-34)
    test.describe('10 - Gestão de Subprocessos', () => {
        test('Captura modais de gestão de subprocesso', async ({page}) => {
            await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);

            const descricao = `Processo gestão ${Date.now()}`;
            await criarProcesso(page, {
                descricao,
                tipo: 'MAPEAMENTO',
                diasLimite: 30,
                unidade: 'SECAO_121',
                expandir: ['SECRETARIA_1', 'COORD_12'],
                iniciar: true
            });

            // Navegar para a página de detalhes do processo (estamos em /painel após criar)
            await page.getByTestId('tbl-processos').getByText(descricao).first().click();
            await expect(page).toHaveURL(/\/processo\/\d+/);

            // Registrar para cleanup
            const codProcesso = await extrairProcessoCodigo(page);
            registrarProcessoParaCleanup(cleanup, codProcesso);

            await page.getByRole('row', {name: /SECAO_121/i}).click();
            await expect(page).toHaveURL(/\/processo\/\d+\/SECAO_121/);
            await capturarTela(page, 'gestao-subprocessos', 'detalhes-subprocesso-admin', {
                fullPage: true,
                extra: {perfil: 'ADMIN'}
            });

            // Modal de alterar data limite (CDU-27)
            const btnAlterarData = await obterAcaoCabecalhoSubprocesso(page, 'btn-alterar-data-limite');
            await expect(btnAlterarData).toBeVisible();
            await btnAlterarData.click();
            await expect(page.getByRole('dialog')).toBeVisible();
            await capturarTela(page, 'gestao-subprocessos', 'modal-alterar-data-limite', {tags: ['modal', 'gestao']});
            await page.getByRole('button', {name: /Cancelar/i}).click();

            // Modal de reabrir cadastro (CDU-32)
            const btnReabrirCadastro = await obterAcaoCabecalhoSubprocesso(page, 'btn-reabrir-cadastro');
            await expect(btnReabrirCadastro).toBeVisible();
            await expect(btnReabrirCadastro).toBeDisabled();

            // CDU-34: Botão de enviar lembrete (ação direta, sem modal)
            const btnEnviarLembrete = await obterAcaoCabecalhoSubprocesso(page, 'btn-enviar-lembrete');
            await expect(btnEnviarLembrete).toBeVisible();
            await capturarTela(page, 'gestao-subprocessos', 'botao-enviar-lembrete', {tags: ['acao-direta']});
        });
    });

    // ADMIN - CONSULTAS E CONFIGURAÇÕES
    test.describe('Admin - Consultas e Configurações', () => {
        test('Captura telas administrativas (Unidades, Histórico, Configurações e Relatórios)', async ({page}) => {
            await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);

            const linkUnidades = page.getByRole('link', {name: /Unidades/i});
            await expect(linkUnidades).toBeVisible();
            await linkUnidades.click();
            await expect(page).toHaveURL(/\/unidades/);
            await capturarTela(page, 'unidades', 'arvore-unidades', {fullPage: true});

            await expect(page.getByText('SECRETARIA_1').first()).toBeVisible();
            await expect(page.getByTestId('btn-unidades-expandir-todas')).toBeVisible();
            await page.getByTestId('btn-unidades-expandir-todas').click();
            const unidade = page.getByRole('row', {name: /SECAO_121/i}).first();
            await expect(unidade).toBeVisible();

            await capturarTela(page, 'unidades', 'arvore-unidades-expandida', {fullPage: true});

            await expect(unidade).toBeVisible();
            await unidade.click();
            await expect(page.getByRole('button', {name: /Criar atribuição|Nova atribuição/i})).toBeVisible();
            await capturarTela(page, 'unidades', 'detalhes-unidade', {fullPage: true, extra: {unidade: 'SECAO_12x'}});

            const btnCriarAtribuicao = page.getByRole('button', {name: /Criar atribuição|Nova atribuição/i});
            await expect(btnCriarAtribuicao).toBeVisible();
            await btnCriarAtribuicao.click();
            await expect(page).toHaveURL(/\/unidade\/\d+\/atribuicao(?:\?.*)?$/);
            await expect(page.getByRole('heading', {name: TEXTOS.atribuicaoTemporaria.TITULO})).toBeVisible();
            await expect(page.getByTestId('input-busca-usuario')).toBeVisible();
            await capturarTela(page, 'unidades', 'tela-criar-atribuicao', {tags: ['tela', 'atribuicao']});
            await page.getByTestId('btn-cancelar-atribuicao').click();
            await expect(page).toHaveURL(/\/unidade\/\d+(?:\?.*)?$/);

            const linkHistorico = page.getByRole('link', {name: /Histórico/i});
            await expect(linkHistorico).toBeVisible();
            await linkHistorico.click();
            await expect(page.locator('table').first()).toBeVisible();
            await capturarTela(page, 'historico', 'pagina-historico', {fullPage: true});

            const tabela = page.locator('table');
            await expect(tabela).toBeVisible();
            await capturarTela(page, 'historico', 'tabela-processos-finalizados', {fullPage: true});

            await page.getByTestId('btn-configuracoes').click();
            await expect(page.getByLabel(/Dias para inativação de processos/i)).toBeVisible();
            await capturarTela(page, 'configuracoes', 'pagina-configuracoes', {fullPage: true});

            const inputDiasInativacao = page.getByLabel(/Dias para inativação de processos/i);
            await expect(inputDiasInativacao).toBeVisible();
            await capturarTela(page, 'configuracoes', 'config-sistema', {tags: ['config']});

            await page.getByTestId('btn-administradores').click();
            await expect(page.getByRole('button', {name: /Adicionar|Novo/i})).toBeVisible();
            await capturarTela(page, 'configuracoes', 'lista-administradores', {fullPage: true});

            const btnAdicionar = page.getByRole('button', {name: /Adicionar|Novo/i});
            await expect(btnAdicionar).toBeVisible();
            await btnAdicionar.click();
            await expect(page.getByRole('dialog')).toBeVisible();
            await capturarTela(page, 'configuracoes', 'modal-adicionar-administrador', {tags: ['modal', 'admin']});
            await page.getByRole('button', {name: /Cancelar/i}).click();

            const linkRelatorios = page.getByRole('link', {name: /Relatórios/i});
            await expect(linkRelatorios).toBeVisible();
            await linkRelatorios.click();
            await expect(page.getByTestId('card-relatorio-andamento')).toBeVisible();
            await capturarTela(page, 'relatorios', 'pagina-relatorios', {fullPage: true});

            await page.getByTestId('card-relatorio-andamento').click();
            await expect(page.getByTestId('select-processo-andamento')).toBeVisible();
            await capturarTela(page, 'relatorios', 'relatorio-andamento', {extra: {relatorio: 'andamento'}});

            await expect(page.getByTestId('btn-gerar-andamento')).toBeVisible();
            await capturarTela(page, 'relatorios', 'botao-gerar-relatorio', {tags: ['ui-element']});

            await page.goto('/relatorios');
            await page.getByTestId('card-relatorio-mapas').click();
            await expect(page.getByTestId('container-arvore-unidades-mapas')).toBeVisible();
            await capturarTela(page, 'relatorios', 'relatorio-mapas', {extra: {relatorio: 'mapas'}});

            await expect(page.getByTestId('btn-gerar-mapas')).toBeVisible();
            await capturarTela(page, 'relatorios', 'botao-gerar-pdf', {tags: ['ui-element', 'pdf']});

            await page.goto('/relatorios');
            await page.getByTestId('card-relatorio-unidades-sem-mapas-vigentes').click();
            await expect(page.getByTestId('btn-visualizar-unidades-sem-mapa')).toBeVisible();
            await capturarTela(page, 'relatorios', 'relatorio-unidades-sem-mapas-vigentes', {extra: {relatorio: 'unidades-sem-mapas-vigentes'}});

            await page.getByTestId('btn-visualizar-unidades-sem-mapa').click();
            await expect(page.locator('.arvore-unidades-sem-mapa').first()).toBeVisible();
            await capturarTela(page, 'relatorios', 'relatorio-unidades-sem-mapas-vigentes-arvore', {fullPage: true, tags: ['arvore', 'unidades-sem-mapas']});
        });
    });

    // SEÇÃO 11 - HISTÓRICO DE ANÁLISE E DEVOLUÇÃO DE CADASTRO
    test.describe('11 - Histórico de Análise e Devolução', () => {
        test('Captura modal de histórico e fluxo de devolução de cadastro', async ({page, request}) => {
            const unidadeAlvo = 'SECAO_221';
            const descricao = `Proc devolucao ${Date.now()}`;

            const processo = await criarProcessoMapeamentoComCadastroDisponibilizadoPorFixture(
                request, cleanup, descricao, unidadeAlvo
            );

            // Gestor acessa cadastro disponibilizado para ver o histórico
            await login(
                page,
                USUARIOS.GESTOR_COORD_22.titulo,
                USUARIOS.GESTOR_COORD_22.senha
            );
            await page.goto(`/processo/${processo}/${unidadeAlvo}`);
            await expect(page).toHaveURL(new RegExp(String.raw`/processo/${processo}/${unidadeAlvo}(?:\?.*)?$`));
            await navegarParaCadastro(page);
            await capturarTela(page, 'historico-analise', 'cadastro-disponibilizado-gestor', {
                fullPage: true,
                tags: ['analise', 'historico']
            });

            // Abrir modal de histórico de análise
            const modalHistorico = await abrirHistoricoAnalise(page);
            await expect(modalHistorico).toBeVisible();
            await capturarTela(page, 'historico-analise', 'modal-historico-analise', {
                tags: ['modal', 'historico'],
                extra: {perfil: 'GESTOR'}
            });
            await fecharHistoricoAnalise(page);

            // Iniciar devolução de cadastro - abrir modal
            const dropdown = page.getByTestId('btn-cadastro-acoes');
            if (await dropdown.count() > 0) {
                await dropdown.click();
                await page.getByTestId('btn-cadastro-acao-devolver').click();
            } else {
                await page.getByTestId('btn-acao-devolver').click();
            }
            const modalDevolucao = page.locator('.modal.show');
            await expect(modalDevolucao).toBeVisible();
            await capturarTela(page, 'historico-analise', 'modal-devolucao-cadastro', {
                tags: ['modal', 'devolucao'],
                extra: {perfil: 'GESTOR', acao: 'devolver-cadastro'}
            });

            // Preencher observação e confirmar devolução
            await modalDevolucao.getByTestId('inp-devolucao-cadastro-obs').fill('Dados incompletos para a Secretaria');
            await capturarTela(page, 'historico-analise', 'modal-devolucao-preenchido', {
                tags: ['modal', 'devolucao', 'preenchido']
            });
            await modalDevolucao.getByTestId('btn-devolucao-cadastro-confirmar').click();
            await expect(page).toHaveURL(/\/painel/);

            // Chefe vê o cadastro devolvido com situação "em andamento"
            await login(page, USUARIOS.CHEFE_SECAO_221.titulo, USUARIOS.CHEFE_SECAO_221.senha);
            await page.goto(`/processo/${processo}/${unidadeAlvo}`);
            await expect(page).toHaveURL(new RegExp(String.raw`/processo/${processo}/${unidadeAlvo}(?:\?.*)?$`));
            await capturarTela(page, 'historico-analise', 'subprocesso-apos-devolucao', {
                fullPage: true,
                tags: ['devolucao', 'chefe'],
                extra: {estado: 'CADASTRO_EM_ANDAMENTO'}
            });

            // Chefe acessa cadastro e vê histórico com registro de devolução
            await navegarParaCadastro(page);
            const modalHistoricoChefe = await abrirHistoricoAnalise(page);
            await expect(modalHistoricoChefe).toBeVisible();
            await expect(modalHistoricoChefe.getByTestId('cell-resultado-0')).toHaveText(/Devolu/i);
            await expect(modalHistoricoChefe).toContainText('Dados incompletos para a Secretaria');
            await capturarTela(page, 'historico-analise', 'historico-com-devolucao', {
                tags: ['modal', 'historico', 'devolucao'],
                extra: {perfil: 'CHEFE', mostra: 'registro-devolucao'}
            });
            await fecharHistoricoAnalise(page);
        });
    });

    // SEÇÃO 12 - MODAIS DE GESTÃO DO MAPA
    test.describe('12 - Modais de Gestão do Mapa', () => {
        test('Captura modais de exclusão, edição e sugestões do mapa', async ({page, request}) => {
            const unidadeAlvo = 'SECAO_322';
            const descricao = `Proc mapa modais ${Date.now()}`;
            const competencia1 = 'Competência para excluir';
            const competencia2 = 'Competência para editar';

            const processoCodigo = await criarProcessoMapeamentoComCadastroHomologadoPorFixture(
                request, cleanup, descricao, unidadeAlvo
            );

            await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
            await page.goto(`/processo/${processoCodigo}/${unidadeAlvo}`);
            await expect(page).toHaveURL(new RegExp(String.raw`/processo/${processoCodigo}/${unidadeAlvo}(?:\?.*)?$`));
            await navegarParaMapa(page);

            // Criar duas competências para testar edição e exclusão
            await MapaHelpers.criarCompetencia(page, competencia1, []);
            await MapaHelpers.criarCompetencia(page, competencia2, []);
            await capturarTela(page, 'mapa-modais', 'mapa-com-duas-competencias', {
                fullPage: true,
                tags: ['mapa', 'competencias']
            });

            // Modal de exclusão - abrir e cancelar
            const cardExcluir = page.getByTestId('cad-mapa__card-competencia').filter({has: page.getByText(competencia1, {exact: true})});
            await cardExcluir.hover();
            await cardExcluir.getByTestId('btn-excluir-competencia').click();
            const modalExclusao = page.getByTestId('mdl-excluir-competencia');
            await expect(modalExclusao).toBeVisible();
            await capturarTela(page, 'mapa-modais', 'modal-excluir-competencia', {
                tags: ['modal', 'exclusao'],
                extra: {competencia: competencia1}
            });
            await modalExclusao.getByTestId('btn-modal-confirmacao-cancelar').click();
            await expect(modalExclusao).toBeHidden();
            await expect(page.getByText(competencia1, {exact: true})).toBeVisible();
            await capturarTela(page, 'mapa-modais', 'competencia-mantida-apos-cancelar-exclusao', {
                tags: ['cancelamento']
            });

            // Modal de exclusão - confirmar
            await excluirCompetenciaConfirmando(page, competencia1);
            await capturarTela(page, 'mapa-modais', 'competencia-excluida', {
                tags: ['exclusao', 'confirmado']
            });

            // Modal de edição de competência
            const cardEditar = page.getByTestId('cad-mapa__card-competencia').filter({has: page.getByText(competencia2, {exact: true})});
            await cardEditar.hover();
            await cardEditar.getByTestId('btn-editar-competencia').click();
            const modalEdicao = page.getByTestId('mdl-criar-competencia');
            await expect(modalEdicao).toBeVisible();
            await capturarTela(page, 'mapa-modais', 'modal-editar-competencia', {
                tags: ['modal', 'edicao'],
                extra: {competencia: competencia2}
            });
            await page.getByTestId('inp-criar-competencia-descricao').fill(`${competencia2} Editada`);
            await capturarTela(page, 'mapa-modais', 'modal-editar-competencia-preenchido', {
                tags: ['modal', 'edicao', 'preenchido']
            });
            await page.getByTestId('btn-criar-competencia-salvar').click();
            await expect(modalEdicao).toBeHidden();
            await expect(page.getByText(`${competencia2} Editada`, {exact: true})).toBeVisible();
        });

        test('Captura devolução do mapa e mapa somente leitura', async ({page, request}) => {
            const unidadeAlvo = 'SECAO_221';
            const descricao = `Proc devolucao mapa ${Date.now()}`;

            const processoCodigo = await criarProcessoMapeamentoComMapaComSugestoesPorFixture(
                request, cleanup, descricao, unidadeAlvo
            );

            // CHEFE vê o mapa em modo somente leitura
            await login(page, USUARIOS.CHEFE_SECAO_221.titulo, USUARIOS.CHEFE_SECAO_221.senha);
            await page.goto(`/processo/${processoCodigo}/${unidadeAlvo}`);
            await expect(page).toHaveURL(new RegExp(String.raw`/processo/${processoCodigo}/${unidadeAlvo}(?:\?.*)?$`));
            await navegarParaMapa(page);

            // Abrir ações do mapa para o chefe
            await expect(page.getByTestId('btn-mapa-acoes')).toBeVisible();
            await page.getByTestId('btn-mapa-acoes').click();
            await capturarTela(page, 'mapa-modais', 'menu-acoes-mapa-chefe', {
                tags: ['menu', 'acoes-mapa', 'chefe']
            });
            await page.keyboard.press('Escape');

            // GESTOR da unidade atual do fluxo abre o modal de devolução
            await login(page, USUARIOS.GESTOR_COORD_22.titulo, USUARIOS.GESTOR_COORD_22.senha);
            await page.goto(`/processo/${processoCodigo}/${unidadeAlvo}`);
            await expect(page).toHaveURL(new RegExp(String.raw`/processo/${processoCodigo}/${unidadeAlvo}(?:\?.*)?$`));
            await navegarParaMapa(page);
            await capturarTela(page, 'mapa-modais', 'mapa-disponibilizado-gestor', {
                fullPage: true,
                tags: ['mapa', 'analise'],
                extra: {perfil: 'GESTOR', estado: 'COM_SUGESTOES'}
            });

            // Abrir modal de devolução do mapa
            await abrirDevolucaoMapa(page);
            const modalDevolucaoMapa = page.getByRole('dialog');
            await expect(modalDevolucaoMapa).toBeVisible();
            await capturarTela(page, 'mapa-modais', 'modal-devolver-mapa', {
                tags: ['modal', 'devolucao-mapa'],
                extra: {perfil: 'GESTOR'}
            });
            await page.keyboard.press('Escape');
        });

        test('Captura modal de sugestões do mapa', async ({page, request}) => {
            const unidadeAlvo = 'ASSESSORIA_11';
            const descricao = `Proc sugestoes ${Date.now()}`;

            const processoCodigo = await criarProcessoMapeamentoComMapaDisponibilizadoPorFixture(
                request, cleanup, descricao, unidadeAlvo
            );

            // CHEFE pode ver sugestões no mapa disponibilizado
            await login(page, USUARIOS.CHEFE_ASSESSORIA_11.titulo, USUARIOS.CHEFE_ASSESSORIA_11.senha);
            await page.goto(`/processo/${processoCodigo}/${unidadeAlvo}`);
            await expect(page).toHaveURL(new RegExp(String.raw`/processo/${processoCodigo}/${unidadeAlvo}(?:\?.*)?$`));
            await navegarParaMapa(page);
            await capturarTela(page, 'mapa-modais', 'mapa-chefe-disponibilizado', {
                fullPage: true,
                tags: ['mapa', 'chefe', 'disponibilizado']
            });

            // Abrir dropdown de ações do mapa e sugestões
            await expect(page.getByTestId('btn-mapa-acoes')).toBeVisible();
            await page.getByTestId('btn-mapa-acoes').click();
            const btnSugestoes = page.getByTestId('btn-mapa-acao-sugestoes');
            if (await btnSugestoes.isVisible()) {
                await capturarTela(page, 'mapa-modais', 'menu-acoes-com-sugestoes', {
                    tags: ['menu', 'sugestoes']
                });
                await btnSugestoes.click();
                await expect(page.getByRole('dialog')).toBeVisible();
                await capturarTela(page, 'mapa-modais', 'modal-sugestoes-mapa', {
                    tags: ['modal', 'sugestoes'],
                    extra: {perfil: 'CHEFE'}
                });
                await page.keyboard.press('Escape');
            } else {
                await page.keyboard.press('Escape');
                await capturarTela(page, 'mapa-modais', 'acoes-mapa-sem-sugestoes', {
                    tags: ['menu', 'sem-sugestoes']
                });
            }
        });

        test('Captura mapa somente leitura após homologação', async ({page, request}) => {
            const unidadeAlvo = 'ASSESSORIA_11';
            const descricao = `Proc mapa homologado ${Date.now()}`;

            const processoCodigo = await criarProcessoMapeamentoComMapaHomologadoPorFixture(
                request, cleanup, descricao, unidadeAlvo
            );

            // CHEFE vê mapa homologado (somente leitura, sem botão de editar)
            await login(page, USUARIOS.CHEFE_ASSESSORIA_11.titulo, USUARIOS.CHEFE_ASSESSORIA_11.senha);
            await page.goto(`/processo/${processoCodigo}/${unidadeAlvo}`);
            await expect(page).toHaveURL(new RegExp(String.raw`/processo/${processoCodigo}/${unidadeAlvo}(?:\?.*)?$`));
            await capturarTela(page, 'mapa-modais', 'subprocesso-mapa-homologado-chefe', {
                fullPage: true,
                tags: ['subprocesso', 'homologado', 'chefe']
            });
            await navegarParaMapa(page);
            await expect(page.getByTestId('btn-abrir-criar-competencia')).toBeHidden();
            await capturarTela(page, 'mapa-modais', 'mapa-homologado-somente-leitura-chefe', {
                fullPage: true,
                tags: ['mapa', 'homologado', 'somente-leitura', 'chefe'],
                extra: {estado: 'MAPA_HOMOLOGADO'}
            });

            // GESTOR vê mapa homologado
            await loginComPerfil(
                page,
                USUARIOS.GESTOR_SECRETARIA_1.titulo,
                USUARIOS.GESTOR_SECRETARIA_1.senha,
                USUARIOS.GESTOR_SECRETARIA_1.perfil!
            );
            await page.goto(`/processo/${processoCodigo}/${unidadeAlvo}`);
            await expect(page).toHaveURL(new RegExp(String.raw`/processo/${processoCodigo}/${unidadeAlvo}(?:\?.*)?$`));
            await navegarParaMapa(page);
            await expect(page.getByTestId('btn-abrir-criar-competencia')).toBeHidden();
            await capturarTela(page, 'mapa-modais', 'mapa-homologado-somente-leitura-gestor', {
                fullPage: true,
                tags: ['mapa', 'homologado', 'somente-leitura', 'gestor'],
                extra: {estado: 'MAPA_HOMOLOGADO', perfil: 'GESTOR'}
            });

            // Histórico do mapa (abre modal, não navega para outra página)
            await expect(page.getByTestId('btn-mapa-historico')).toBeVisible();
            await page.getByTestId('btn-mapa-historico').click();
            const modalHistoricoMapa = page.getByRole('dialog');
            await expect(modalHistoricoMapa).toBeVisible();
            await capturarTela(page, 'mapa-modais', 'historico-mapa', {
                fullPage: false,
                tags: ['historico', 'mapa'],
                extra: {perfil: 'GESTOR'}
            });
            await page.keyboard.press('Escape');
        });
    });

    // SEÇÃO 13 - PERFIL SERVIDOR
    test.describe('13 - Perfil Servidor (Somente Leitura)', () => {
        test('Captura painel e acesso restrito do perfil Servidor', async ({page, request}) => {
            const unidadeAlvo = 'SECAO_221';
            const descricao = `Proc servidor ${Date.now()}`;

            const processoCodigo = await criarProcessoMapeamentoIniciadoPorFixture(
                request, cleanup, descricao, unidadeAlvo
            );

            // SERVIDOR faz login e vê o painel
            await login(page, USUARIOS.SERVIDOR_SECAO_221.titulo, USUARIOS.SERVIDOR_SECAO_221.senha);
            await expect(page).toHaveURL(/\/painel/);
            await capturarTela(page, 'perfil-servidor', 'painel-servidor', {
                fullPage: true,
                tags: ['servidor', 'painel'],
                extra: {perfil: 'SERVIDOR'}
            });

            // SERVIDOR não deve ver o botão de criar processo
            await expect(page.getByTestId('btn-painel-criar-processo')).toBeHidden();
            await capturarTela(page, 'perfil-servidor', 'painel-servidor-sem-criar', {
                tags: ['servidor', 'acesso-restrito']
            });

            // SERVIDOR não deve ter acesso ao menu de Unidades, Relatórios, etc.
            await expect(page.getByRole('link', {name: /Unidades/i})).toBeHidden();

            // SERVIDOR acessa subprocesso da sua unidade (somente leitura)
            await page.goto(`/processo/${processoCodigo}/${unidadeAlvo}`);
            await expect(page).toHaveURL(new RegExp(String.raw`/processo/${processoCodigo}/${unidadeAlvo}(?:\?.*)?$`));
            await capturarTela(page, 'perfil-servidor', 'subprocesso-servidor', {
                fullPage: true,
                tags: ['servidor', 'subprocesso', 'somente-leitura'],
                extra: {perfil: 'SERVIDOR'}
            });
        });
    });

    // SEÇÃO 14 - MODAIS DE REMOÇÃO DE ATIVIDADE E CONHECIMENTO
    test.describe('14 - Modais de Remoção de Atividade e Conhecimento', () => {
        test('Captura modais de remoção de atividade e conhecimento', async ({page, request}) => {
            const unidadeAlvo = 'SECAO_323';
            const descricao = `Proc remocao ${Date.now()}`;
            const atividadeDesc = `Atividade para remover ${Date.now()}`;
            const conhecimentoDesc = 'Conhecimento para remover';
            const atividadeComConhecimento = `Atividade com conhecimento ${Date.now()}`;

            const processoCodigo = await criarProcessoMapeamentoIniciadoPorFixture(
                request, cleanup, descricao, unidadeAlvo
            );

            await login(page, '380001', 'senha');
            await page.goto(`/processo/${processoCodigo}/${unidadeAlvo}`);
            await expect(page).toHaveURL(new RegExp(String.raw`/processo/${processoCodigo}/${unidadeAlvo}(?:\?.*)?$`));
            await navegarParaCadastro(page);

            // Adicionar atividade e conhecimento para remoção
            await adicionarAtividade(page, atividadeDesc);
            await adicionarAtividade(page, atividadeComConhecimento);
            await adicionarConhecimento(page, atividadeComConhecimento, conhecimentoDesc);

            // Modal de remoção de conhecimento - abrir
            const cardComConhecimento = page.getByTestId('cad-atividades__card-atividade').filter({has: page.getByText(atividadeComConhecimento)});
            const linhaConhecimento = cardComConhecimento.getByTestId('cad-atividades__item-conhecimento').filter({hasText: conhecimentoDesc});
            await linhaConhecimento.hover();
            await linhaConhecimento.getByTestId('btn-remover-conhecimento').click();
            const modalRemoverConhecimento = page.getByRole('dialog');
            await expect(modalRemoverConhecimento).toBeVisible();
            await capturarTela(page, 'remocao', 'modal-remover-conhecimento', {
                tags: ['modal', 'remocao', 'conhecimento'],
                extra: {conhecimento: conhecimentoDesc}
            });
            // Cancelar remoção
            await page.getByRole('dialog').last().getByTestId('btn-modal-confirmacao-cancelar').click();
            await expect(page.getByRole('dialog')).toBeHidden();
            await expect(cardComConhecimento.getByText(conhecimentoDesc)).toBeVisible();
            await capturarTela(page, 'remocao', 'conhecimento-mantido-apos-cancelar', {
                tags: ['cancelamento', 'conhecimento']
            });

            // Modal de remoção de atividade - hover e abrir
            const cardAtividadeRemover = page.getByTestId('cad-atividades__card-atividade').filter({has: page.getByText(atividadeDesc, {exact: true})});
            const hoverRow = cardAtividadeRemover.getByTestId('cad-atividades__hover-row');
            await hoverRow.hover();
            await aguardarPinturaEstavel(page);
            await capturarTela(page, 'remocao', 'atividade-hover-com-remover', {
                tags: ['hover', 'atividade']
            });
            await cardAtividadeRemover.getByTestId('btn-remover-atividade').click();
            const modalRemoverAtividade = page.getByRole('dialog');
            await expect(modalRemoverAtividade).toBeVisible();
            await capturarTela(page, 'remocao', 'modal-remover-atividade', {
                tags: ['modal', 'remocao', 'atividade'],
                extra: {atividade: atividadeDesc}
            });
            // Confirmar remoção
            await page.getByTestId('btn-modal-confirmacao-confirmar').click();
            await expect(page.getByText(atividadeDesc, {exact: true})).toBeHidden();
            await capturarTela(page, 'remocao', 'atividade-removida', {
                tags: ['remocao', 'confirmado']
            });
        });

        test('Captura modal de impacto no mapa (revisão)', async ({page, request}) => {
            const unidadeAlvo = 'ASSESSORIA_11';
            const descricao = `Proc impacto ${Date.now()}`;

            const processoCodigo = await criarProcessoRevisaoComCadastroHomologadoPorFixture(
                request, cleanup, descricao, unidadeAlvo
            );

            await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
            await page.goto(`/processo/${processoCodigo}/${unidadeAlvo}`);
            await expect(page).toHaveURL(new RegExp(String.raw`/processo/${processoCodigo}/${unidadeAlvo}(?:\?.*)?$`));
            await navegarParaMapa(page);
            await capturarTela(page, 'impacto-mapa', 'mapa-antes-disponibilizar', {
                fullPage: true,
                tags: ['mapa', 'impacto']
            });

            // Botão de impacto no mapa
            const btnImpactoMapa = page.getByTestId('cad-mapa__btn-impactos-mapa');
            if (await btnImpactoMapa.isVisible()) {
                await btnImpactoMapa.click();
                await expect(page.getByRole('dialog')).toBeVisible();
                await capturarTela(page, 'impacto-mapa', 'modal-impacto-mapa', {
                    tags: ['modal', 'impacto', 'mapa']
                });
                await page.keyboard.press('Escape');
            }
        });
    });
});
