import type {APIRequestContext, Locator, Page} from '@playwright/test';
import {expect, test} from './fixtures/base.js';
import {login, loginComPerfil, USUARIOS} from './helpers/helpers-auth.js';
import {
    confirmarInicioProcessoPeloDialogo,
    criarProcesso,
    extrairProcessoCodigo,
    iniciarProcessoPeloCadastro
} from './helpers/helpers-processos.js';
import {
    fazerLogout,
    navegarParaSubprocesso,
    verificarAppAlert,
    verificarPaginaPainel
} from './helpers/helpers-navegacao.js';
import {
    adicionarAtividade,
    adicionarConhecimento,
    disponibilizarCadastro,
    navegarParaAtividades,
    navegarParaAtividadesVisualizacao
} from './helpers/helpers-atividades.js';
import {
    acessarSubprocessoAdmin,
    acessarSubprocessoChefeDireto,
    acessarSubprocessoGestor
} from './helpers/helpers-analise.js';
import {TEXTOS} from '../frontend/src/constants/textos.js';
import {abrirModalCriarCompetencia, disponibilizarMapa, navegarParaMapa} from './helpers/helpers-mapas.js';
import {resetDatabase, useProcessoCleanup} from './hooks/hooks-limpeza.js';
import * as path from 'node:path';
import * as fs from 'node:fs';

/**
 * Suite de testes para a capturar screenshots de todas as telas do sistema
 * para refinamento de UI. Funciona tambem como um bom teste 'smoke'
 */

interface CaptureMetadata {
    file: string;
    url: string;
    route: string;
    viewport: { width: number; height: number };
    timestamp: string;
    tags: string[];
    categoria: string;
    nome: string;
    titulo: string;
    isFullPage: boolean;
    isComponente: boolean;
    [key: string]: any; // Permite campos extras flexíveis
}

const capturasMetadata: CaptureMetadata[] = [];

// Diretório onde as screenshots serão salvas
const SCREENSHOTS_DIR = path.join(process.cwd(), 'screenshots');
const METADATA_PATH = path.join(SCREENSHOTS_DIR, 'capturas-metadata.json');

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
async function capturarTela(page: Page, categoria: string, nome: string, opcoes?: { fullPage?: boolean; extra?: any; tags?: string[] }) {
    const url = page.url();
    const titulo = await page.title();
    const isFullPage = opcoes?.fullPage ?? true;
    const viewport = page.viewportSize() || { width: 0, height: 0 };
    
    // Extrair rota da URL (ex: /painel)
    let route = '/';
    try {
        const urlObj = new URL(url);
        route = urlObj.pathname + urlObj.search;
    } catch {
        route = url;
    }

    // Injetar banner com a URL no RODAPÉ
    await page.evaluate(({urlText, fullPage}) => {
        const id = 'sgc-url-banner';
        let banner = document.getElementById(id);
        if (!banner) {
            banner = document.createElement('div');
            banner.id = id;
            document.body.appendChild(banner);
        }

        Object.assign(banner.style, {
            position: fullPage ? 'relative' : 'fixed',
            bottom: '0',
            left: '0',
            width: '100%',
            backgroundColor: '#222',
            color: '#0f0', // Verde limão para alto contraste
            fontSize: '11px',
            padding: '4px 10px',
            zIndex: '999999',
            fontFamily: 'monospace',
            borderTop: '1px solid #444',
            opacity: '1',
            textAlign: 'left',
            boxSizing: 'border-box',
            marginTop: fullPage ? '10px' : '0'
        });
        banner.textContent = `URL: ${urlText}`;
        
        if (fullPage) {
            document.body.appendChild(banner); // Move para o final do body
        }
    }, {urlText: url, fullPage: isFullPage});

    const nomeArquivo = `${categoria}--${nome}.png`;
    const caminhoCompleto = path.join(SCREENSHOTS_DIR, nomeArquivo);
    
    // Pequena espera para garantir que o banner e renderização estabilizem
    await page.waitForTimeout(100);
    
    await page.screenshot({
        path: caminhoCompleto,
        fullPage: isFullPage
    });

    // Registrar metadados no formato Captus
    capturasMetadata.push({
        file: nomeArquivo,
        url,
        route,
        viewport,
        timestamp: new Date().toISOString(),
        tags: [...(opcoes?.tags ?? []), categoria],
        categoria,
        nome,
        titulo,
        isFullPage,
        isComponente: false,
        ...opcoes?.extra // Espalha campos extras na raiz do objeto
    });

    // Remover banner após a captura
    await page.evaluate(() => {
        const banner = document.getElementById('sgc-url-banner');
        if (banner) banner.remove();
    });
}

async function capturarComponente(elemento: Locator, categoria: string, nome: string, extra?: any, tags?: string[]) {
    const page = elemento.page();
    const url = page.url();
    const titulo = await page.title();
    const viewport = page.viewportSize() || { width: 0, height: 0 };
    
    // Extrair rota
    let route = '/';
    try {
        const urlObj = new URL(url);
        route = urlObj.pathname + urlObj.search;
    } catch {
        route = url;
    }

    // Para componentes, adicionamos um selo discreto no final
    await elemento.evaluate((el, urlText) => {
        const banner = document.createElement('div');
        banner.id = 'sgc-url-banner-comp';
        Object.assign(banner.style, {
            backgroundColor: '#222',
            color: '#0f0',
            fontSize: '10px',
            padding: '2px 6px',
            fontFamily: 'monospace',
            marginTop: '5px',
            display: 'block',
            width: '100%',
            boxSizing: 'border-box',
            borderTop: '1px solid #444'
        });
        banner.textContent = `URL: ${urlText}`;
        el.appendChild(banner);
    }, url);

    const nomeArquivo = `${categoria}--${nome}.png`;
    const caminhoCompleto = path.join(SCREENSHOTS_DIR, nomeArquivo);
    await elemento.screenshot({path: caminhoCompleto});

    // Registrar metadados no formato Captus
    capturasMetadata.push({
        file: nomeArquivo,
        url,
        route,
        viewport,
        timestamp: new Date().toISOString(),
        tags: [...(tags ?? []), categoria, 'componente'],
        categoria,
        nome,
        titulo,
        isFullPage: false,
        isComponente: true,
        ...extra // Espalha campos extras na raiz do objeto
    });

    await elemento.evaluate((el) => {
        const banner = el.querySelector('#sgc-url-banner-comp');
        if (banner) banner.remove();
    });
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
            fs.writeFileSync(METADATA_PATH, JSON.stringify(capturasMetadata, null, 2));
        }
    });

    test.describe('01 - Autenticação', () => {
        test('Captura telas de login', async ({page}) => {
            await page.goto('/login');
            // Tela de login inicial
            await capturarTela(page, '01-seguranca', '01-login-inicial', {
                tags: ['login', 'inicial']
            });
            await page.getByTestId('inp-login-usuario').click();
            await capturarTela(page, '01-seguranca', '01b-login-campo-usuario-foco', {
                tags: ['login', 'interacao']
            });

            // Erro de credenciais inválidas
            await page.getByTestId('inp-login-usuario').fill(USUARIOS.INVALIDO.titulo);
            await capturarTela(page, '01-seguranca', '01c-login-usuario-preenchido', {
                tags: ['login', 'preenchimento']
            });
            await page.getByTestId('inp-login-senha').fill(USUARIOS.INVALIDO.senha);
            await page.getByTestId('btn-login-entrar').click();
            await verificarAppAlert(page);
            await capturarTela(page, '01-seguranca', '02-login-erro-credenciais', {
                tags: ['login', 'erro', 'validacao'],
                extra: { erro: 'Credenciais inválidas' }
            });

            // Limpar e fazer login com múltiplos perfis
            await page.getByTestId('inp-login-usuario').fill(USUARIOS.ADMIN_2_PERFIS.titulo);
            await page.getByTestId('inp-login-senha').fill(USUARIOS.ADMIN_2_PERFIS.senha);
            await page.getByTestId('btn-login-entrar').click();
            await expect(page.getByTestId('sel-login-perfil')).toBeVisible();
            await capturarTela(page, '01-seguranca', '03-login-selecao-perfil', {
                tags: ['login', 'multi-perfil'],
                extra: { usuario: USUARIOS.ADMIN_2_PERFIS.titulo }
            });

            // Login com perfil selecionado
            await loginComPerfil(page, USUARIOS.ADMIN_2_PERFIS.titulo, USUARIOS.ADMIN_2_PERFIS.senha, USUARIOS.ADMIN_2_PERFIS.perfil);
            await capturarTela(page, '01-seguranca', '04-painel-apos-login', {
                fullPage: true,
                tags: ['login', 'sucesso', 'dashboard'],
                extra: { perfil: USUARIOS.ADMIN_2_PERFIS.perfil }
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
            await capturarTela(page, '02-painel', '02-criar-processo-form-vazio', {
                extra: { perfil: 'ADMIN' },
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
            await capturarTela(page, '02-painel', '04-arvore-unidades-expandida', {
                extra: { perfil: 'ADMIN', acao: 'expandir-arvore' }
            });

            // Expandir COORD_11 para acessar SECAO_111
            await page.getByTestId('btn-arvore-expand-COORD_11').click();
            await expect(page.getByTestId('chk-arvore-unidade-SECAO_111')).toBeVisible();

            // Selecionar múltiplas unidades
            await page.getByTestId('chk-arvore-unidade-ASSESSORIA_11').click();
            await page.getByTestId('chk-arvore-unidade-ASSESSORIA_12').click();
            await page.getByTestId('chk-arvore-unidade-SECAO_111').click();
            await capturarTela(page, '02-painel', '05-arvore-unidades-selecionada', {
                tags: ['selecao', 'unidades']
            });

            await page.getByTestId('btn-processo-salvar').click();
            await expect(page).toHaveURL(/\/painel/);
            await expect(page.getByTestId('tbl-processos')).toBeVisible();
            await expect(page.getByTestId('tbl-processos').getByText(descricaoProcesso).first()).toBeVisible();
            await capturarComponente(page.getByTestId('tbl-processos'), '02-painel', '06a-tabela-processos', { perfil: 'ADMIN' });

            // Capturar ID para cleanup
            await page.getByTestId('tbl-processos').getByText(descricaoProcesso).first().click();
            await expect(page).toHaveURL(/codProcesso=\d+/);
            const codProcesso = await extrairProcessoCodigo(page);
            if (codProcesso > 0) cleanup.registrar(codProcesso);
            await page.goto('/painel');

            // Painel com processo criado
            await capturarTela(page, '02-painel', '06-painel-admin-com-processo', {
                fullPage: true,
                extra: { perfil: 'ADMIN' }
            });

            // Hover em linha da tabela
            await page.getByText(descricaoProcesso).hover();
            await capturarTela(page, '02-painel', '07-painel-hover-processo', {
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
            await capturarTela(page, '02-painel', '10-painel-gestor', {
                fullPage: true,
                extra: { perfil: 'GESTOR', unidade: 'COORD_11' }
            });
        });

        test('Captura painel CHEFE', async ({page, request}) => {
            const desc = `Processo chefe ${Date.now()}`;
            await criarProcessoMapeamentoIniciadoPorFixture(request, cleanup, desc, 'SECAO_211');

            await login(page, USUARIOS.CHEFE_SECAO_211.titulo, USUARIOS.CHEFE_SECAO_211.senha);
            await expect(page.getByTestId('tbl-processos')).toBeVisible();
            await expect(page.getByTestId('tbl-processos').getByText(desc).first()).toBeVisible();
            await capturarTela(page, '02-painel', '11-painel-chefe', {
                fullPage: true,
                extra: { perfil: 'CHEFE', unidade: 'SECAO_211' }
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
            if (codProcesso > 0) cleanup.registrar(codProcesso);

            // Tela de edição de processo
            await capturarTela(page, '03-processo', '01-processo-edicao', {
                extra: { perfil: 'ADMIN', estado: 'CRIADO' }
            });

            // Modal de iniciar processo
            await page.getByTestId('btn-processo-iniciar').click();
            await expect(page.getByRole('dialog')).toBeVisible();
            await capturarTela(page, '03-processo', '02-modal-iniciar-processo', {
                tags: ['modal', 'confirmacao'],
                extra: { acao: 'iniciar-processo' }
            });
            await confirmarInicioProcessoPeloDialogo(page, {
                descricao,
                tipo: 'MAPEAMENTO'
            });

            // Acessar detalhes do processo iniciado
            await page.getByTestId('tbl-processos').getByText(descricao).first().click();
            await expect(page).toHaveURL(/\/processo\/\d+/);
            await capturarTela(page, '03-processo', '03-detalhes-processo-iniciado', {
                fullPage: true,
                extra: { perfil: 'ADMIN', estado: 'EM_ANDAMENTO' }
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
            await expect(page).toHaveURL(new RegExp(String.raw`/processo/${processoCodigo}/${unidadeAlvo}$`));
            await navegarParaAtividades(page);
            await adicionarAtividade(page, atividade);
            await adicionarConhecimento(page, atividade, conhecimento);
            await capturarTela(page, '03-processo', '03a-cadastro-chefe', {
                extra: { perfil: 'CHEFE', unidade: unidadeAlvo, acao: 'cadastro-atividades' }
            });
            await disponibilizarCadastro(page);

            await loginComPerfil(page, USUARIOS.GESTOR_SECRETARIA_1.titulo, USUARIOS.GESTOR_SECRETARIA_1.senha, USUARIOS.GESTOR_SECRETARIA_1.perfil);
            await page.goto(`/processo/${processoCodigo}/${unidadeAlvo}`);
            await expect(page).toHaveURL(new RegExp(String.raw`/processo/${processoCodigo}/${unidadeAlvo}$`));
            await navegarParaAtividadesVisualizacao(page);
            await capturarTela(page, '03-processo', '03b-analise-gestor', {
                extra: { perfil: 'GESTOR', unidade: unidadeAlvo, acao: 'analise-cadastro' }
            });
            await page.getByTestId('btn-acao-analisar-principal').click();
            await expect(page.getByRole('dialog')).toBeVisible();
            await capturarTela(page, '03-processo', '03c-modal-aceite-gestor', {
                tags: ['modal', 'aceite']
            });
            await page.getByTestId('inp-aceite-cadastro-obs').fill('Aceite para captura de tela');
            await page.getByTestId('btn-aceite-cadastro-confirmar').click();
            await verificarPaginaPainel(page);

            await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
            await page.goto(`/processo/${processoCodigo}/${unidadeAlvo}`);
            await expect(page).toHaveURL(new RegExp(String.raw`/processo/${processoCodigo}/${unidadeAlvo}$`));
            await navegarParaAtividadesVisualizacao(page);
            await page.getByTestId('btn-acao-analisar-principal').click();
            await expect(page.getByRole('dialog')).toBeVisible();
            await capturarTela(page, '03-processo', '03d-modal-homologacao-admin', {
                tags: ['modal', 'homologacao'],
                extra: { perfil: 'ADMIN' }
            });
            await page.getByTestId('inp-aceite-cadastro-obs').fill('Homologação para captura');
            await page.getByTestId('btn-aceite-cadastro-confirmar').click();

            await navegarParaMapa(page);
            await abrirModalCriarCompetencia(page);
            await page.getByTestId('inp-criar-competencia-descricao').fill(competencia);
            await page.locator('label').filter({hasText: atividade}).click();
            await page.getByTestId('btn-criar-competencia-salvar').click();
            await capturarTela(page, '03-processo', '03e-mapa-criado', {
                extra: { perfil: 'ADMIN', acao: 'edicao-mapa' }
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
            await expect(page).toHaveURL(new RegExp(String.raw`/processo/${processoCodigo}/${unidadeAlvo}$`));
            await navegarParaMapa(page);
            await page.getByTestId('btn-mapa-validar').click();
            await expect(page.getByRole('dialog')).toBeVisible();
            await capturarTela(page, '03-processo', '03f-modal-validar-mapa', {
                tags: ['modal', 'validacao-mapa'],
                extra: { perfil: 'CHEFE' }
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
            await expect(page).toHaveURL(new RegExp(String.raw`/processo/${processoCodigo}$`));
            await capturarTela(page, '03-processo', '03i-detalhes-processo-finalizavel', {
                fullPage: true,
                extra: { perfil: 'ADMIN', acao: 'finalizacao-processo' }
            });

            // Modal de finalizar processo
            await page.getByTestId('btn-processo-finalizar').click();
            await expect(page.getByRole('dialog')).toBeVisible();
            await capturarTela(page, '03-processo', '04-modal-finalizar-processo', {
                tags: ['modal', 'finalizar']
            });
            await page.getByRole('button', {name: 'Cancelar'}).click();
            await capturarTela(page, '03-processo', '05-detalhes-processo-apos-cancelar-finalizacao', {
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
            await expect(page.getByTestId('btn-processo-salvar')).toBeDisabled();
            await capturarTela(page, '03-processo', '10-botoes-desativados-form-vazio', {
                tags: ['validacao', 'form'],
                extra: { motivo: 'falta-campos-obrigatorios' }
            });

            await page.getByTestId('inp-processo-descricao').fill(descricao);
            await expect(page.getByTestId('btn-processo-salvar')).toBeDisabled();
            await capturarTela(page, '03-processo', '11-botoes-desativados-falta-data-unidade', {
                extra: { preenchido: 'descricao' }
            });

            await page.getByTestId('sel-processo-tipo').selectOption('MAPEAMENTO');

            const dataLimite = new Date();
            dataLimite.setDate(dataLimite.getDate() + 30);
            await page.getByTestId('inp-processo-data-limite').fill(dataLimite.toISOString().split('T')[0]);
            await expect(page.getByTestId('btn-processo-salvar')).toBeDisabled();
            await capturarTela(page, '03-processo', '12-botoes-desativados-falta-unidade', {
                extra: { preenchido: ['descricao', 'tipo', 'data'] }
            });

            // Selecionar unidade e validar ativação
            await expect(page.getByText('Carregando unidades...')).toBeHidden();
            await page.getByTestId('btn-arvore-expand-SECRETARIA_2').click();
            await expect(page.getByTestId('btn-arvore-expand-COORD_21')).toBeVisible();
            await page.getByTestId('btn-arvore-expand-COORD_21').click();
            await expect(page.getByTestId('chk-arvore-unidade-SECAO_211')).toBeVisible();
            await page.getByTestId('chk-arvore-unidade-SECAO_211').click();
            await expect(page.getByTestId('btn-processo-salvar')).toBeEnabled();
            await capturarTela(page, '03-processo', '13-botoes-ativados-form-completo', {
                tags: ['validacao', 'sucesso']
            });

            await page.getByTestId('btn-processo-iniciar').click();
            await confirmarInicioProcessoPeloDialogo(page, {
                descricao,
                tipo: 'MAPEAMENTO'
            });

            // Registrar para cleanup
            const linhaProcesso = page.getByTestId('tbl-processos').locator('tr').filter({has: page.getByText(descricao)});
            await linhaProcesso.click();
            await page.waitForURL(/\/processo\/\d+/);
            const codProcesso = await extrairProcessoCodigo(page);
            if (codProcesso > 0) cleanup.registrar(codProcesso);

            await login(page, USUARIOS.CHEFE_SECAO_211.titulo, USUARIOS.CHEFE_SECAO_211.senha);
            await page.getByTestId('tbl-processos').getByText(descricao).first().click();
            await navegarParaSubprocesso(page, UNIDADE_ALVO);
            await capturarTela(page, '04-subprocesso', '01-dashboard-subprocesso', {
                fullPage: true,
                extra: { perfil: 'CHEFE', unidade: UNIDADE_ALVO }
            });

            // Entrar em atividades
            await navegarParaAtividades(page);
            await capturarTela(page, '04-subprocesso', '02-cadastro-atividades-vazio', {
                fullPage: true,
                tags: ['atividades', 'vazio']
            });

            const atividadeDesc = `Atividade teste ${Date.now()}`;
            await page.getByTestId('inp-nova-atividade').fill(atividadeDesc);
            await capturarTela(page, '04-subprocesso', '03-cadastro-atividades-preenchendo', {
                tags: ['interacao']
            });
            await page.getByTestId('btn-adicionar-atividade').click();
            await expect(page.getByText(atividadeDesc, {exact: true})).toBeVisible();
            await capturarTela(page, '04-subprocesso', '04-cadastro-atividades-com-uma', {
                fullPage: true,
                extra: { atividade: atividadeDesc }
            });

            const card = page.locator('.atividade-card', {has: page.getByText(atividadeDesc)});
            await card.getByTestId('inp-novo-conhecimento').fill('Java');
            await capturarTela(page, '04-subprocesso', '05-cadastro-conhecimento-preenchendo', {
                tags: ['conhecimento', 'edicao']
            });
            await adicionarConhecimento(page, atividadeDesc, 'Java');
            await adicionarConhecimento(page, atividadeDesc, 'Spring Boot');
            await adicionarConhecimento(page, atividadeDesc, 'Oracle');
            await capturarTela(page, '04-subprocesso', '06-cadastro-atividade-completa', {
                fullPage: true,
                extra: { conhecimentos: ['Java', 'Spring Boot', 'Oracle'] }
            });

            // Hover na atividade para ver ações
            await card.locator('.atividade-hover-row').hover();
            await page.waitForTimeout(300);
            await capturarTela(page, '04-subprocesso', '07-cadastro-atividade-hover', {
                tags: ['interacao', 'hover']
            });

            // Tentar disponibilizar sem data
            await page.getByTestId('btn-cad-atividades-disponibilizar').click();
            await expect(page.getByRole('dialog')).toBeVisible();
            await capturarTela(page, '04-subprocesso', '08-modal-disponibilizar-atividades', {
                tags: ['modal', 'disponibilizar']
            });
            await page.getByTestId('btn-confirmar-disponibilizacao').click();
            await verificarPaginaPainel(page);
            await capturarTela(page, '04-subprocesso', '09-cadastro-atividades-disponibilizado', {
                fullPage: true,
                extra: { estado: 'DISPONIBILIZADO' }
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
            if (codProcesso > 0) cleanup.registrar(codProcesso);

            await iniciarProcessoPeloCadastro(page, {
                descricao,
                tipo: 'MAPEAMENTO'
            });

            await login(page, USUARIOS.CHEFE_SECAO_212.titulo, USUARIOS.CHEFE_SECAO_212.senha);

            await page.getByTestId('tbl-processos').getByText(descricao).first().click();
            await navegarParaSubprocesso(page, UNIDADE_ALVO);
            await navegarParaAtividades(page);

            // Capturar tela inicial vazia com label "Conhecimentos *"
            await capturarTela(page, '04-subprocesso', '20-cadastro-vazio-com-label-obrigatorio', {
                fullPage: true,
                tags: ['ui-clue', 'obrigatorio']
            });

            // Adicionar primeira atividade SEM conhecimento
            const atividade1 = `Atividade sem conhecimento 1 ${Date.now()}`;
            await adicionarAtividade(page, atividade1);
            await page.waitForTimeout(300);
            await capturarTela(page, '04-subprocesso', '21-atividade-sem-conhecimento', {
                fullPage: true,
                extra: { erro: 'falta-conhecimento' }
            });

            // Adicionar segunda atividade SEM conhecimento
            const atividade2 = `Atividade sem conhecimento 2 ${Date.now()}`;
            await adicionarAtividade(page, atividade2);
            await page.waitForTimeout(300);

            // Adicionar terceira atividade COM conhecimento (para contraste)
            const atividade3 = `Atividade com conhecimento ${Date.now()}`;
            await adicionarAtividade(page, atividade3);
            await adicionarConhecimento(page, atividade3, 'Java');
            await page.waitForTimeout(300);
            await capturarTela(page, '04-subprocesso', '22-mix-atividades-com-sem-conhecimento', {
                fullPage: true,
                tags: ['validacao', 'visual']
            });

            // Sem conhecimento em todas as atividades, o botão deve permanecer desabilitado
            await expect(page.getByTestId('btn-cad-atividades-disponibilizar')).toBeDisabled();
            await capturarTela(page, '04-subprocesso', '23-botao-disponibilizar-desabilitado-sem-conhecimento', {
                fullPage: true,
                tags: ['validacao', 'bloqueio']
            });

            // Scroll para segunda atividade sem conhecimento
            const card2 = page.locator('.atividade-card', {has: page.getByText(atividade2)});
            await card2.scrollIntoViewIfNeeded();
            await page.waitForTimeout(300);
            await capturarTela(page, '04-subprocesso', '24-segunda-atividade-sem-conhecimento', {
                tags: ['scroll', 'validacao']
            });

            // Capturar detalhe do card sem conhecimento
            const primeiroCard = page.locator('.atividade-card', {has: page.getByText(atividade1)});
            await primeiroCard.scrollIntoViewIfNeeded();
            await page.waitForTimeout(300);

            // Capturar apenas o card para detalhe
            await primeiroCard.screenshot({
                path: path.join(SCREENSHOTS_DIR, '04-subprocesso--25-detalhe-card-sem-conhecimento.png')
            });
            // Registrar manualmente no array pois não usamos capturarTela
            capturasMetadata.push({
                file: '04-subprocesso--25-detalhe-card-sem-conhecimento.png',
                url: page.url(),
                route: new URL(page.url()).pathname,
                viewport: page.viewportSize() || { width: 0, height: 0 },
                timestamp: new Date().toISOString(),
                tags: ['04-subprocesso', 'detalhe', 'erro'],
                categoria: '04-subprocesso',
                nome: '25-detalhe-card-sem-conhecimento',
                titulo: await page.title(),
                isFullPage: false,
                isComponente: true,
                motivo: 'visualizar erro no card'
            });

            // Corrigir primeira atividade adicionando conhecimento
            await adicionarConhecimento(page, atividade1, 'Python');
            await page.waitForTimeout(500);
            await capturarTela(page, '04-subprocesso', '26-erro-desaparece-apos-correcao', {
                fullPage: true,
                extra: { atividadeCorrigida: atividade1 }
            });

            // Ainda deve permanecer bloqueado porque a atividade 2 continua sem conhecimento
            await expect(page.getByTestId('btn-cad-atividades-disponibilizar')).toBeDisabled();
            await capturarTela(page, '04-subprocesso', '27-botao-ainda-desabilitado-com-atividade-restante', {
                extra: { pendente: atividade2 }
            });

            // Corrigir segunda atividade
            await adicionarConhecimento(page, atividade2, 'JavaScript');
            await page.waitForTimeout(500);

            // Tentar disponibilizar - agora deve funcionar
            await page.getByTestId('btn-cad-atividades-disponibilizar').click();
            await page.waitForTimeout(500);

            // Modal de confirmação deve aparecer
            const modalConfirmacao = page.locator('.modal-content').filter({hasText: 'Disponibilização do cadastro'});

            // Garantir que a modal está visível antes de capturar
            await expect(modalConfirmacao).toBeVisible();
            await capturarTela(page, '04-subprocesso', '28-modal-confirmacao-disponibilizacao', {
                tags: ['modal', 'sucesso']
            });
        });
    });

    test.describe('05 - Mapa de Competências', () => {
        test('Captura fluxo de mapa de competências', async ({page}) => {
            const descricao = `Proc mapa ${Date.now()}`;
            const UNIDADE_ALVO = 'SECAO_121';

            await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);

            await criarProcesso(page, {
                descricao,
                tipo: 'MAPEAMENTO',
                diasLimite: 30,
                unidade: UNIDADE_ALVO,
                expandir: ['SECRETARIA_1', 'COORD_12']
            });

            const linhaProcesso = page.getByTestId('tbl-processos').locator('tr').filter({has: page.getByText(descricao)});
            await linhaProcesso.click();
            const codProcesso = await extrairProcessoCodigo(page);
            if (codProcesso > 0) cleanup.registrar(codProcesso);

            await iniciarProcessoPeloCadastro(page, {
                descricao,
                tipo: 'MAPEAMENTO'
            });

            await login(page,
                USUARIOS.CHEFE_SECAO_121.titulo, USUARIOS.CHEFE_SECAO_121.senha);

            await acessarSubprocessoChefeDireto(page, descricao, 'SECAO_121');
            await navegarParaAtividades(page);

            await adicionarAtividade(page, 'Desenvolvimento web');
            await adicionarConhecimento(page, 'Desenvolvimento web', 'Vue.js');
            await adicionarConhecimento(page, 'Desenvolvimento web', 'TypeScript');

            await adicionarAtividade(page, 'Desenvolvimento backend');
            await adicionarConhecimento(page, 'Desenvolvimento backend', 'Java');
            await adicionarConhecimento(page, 'Desenvolvimento backend', 'Spring Boot');

            // Disponibilizar (como chefe)
            await page.getByTestId('btn-cad-atividades-disponibilizar').click();
            await page.getByTestId('btn-confirmar-disponibilizacao').click();
            await page.waitForTimeout(500);

            await login(page, USUARIOS.GESTOR_COORD_12.titulo, USUARIOS.GESTOR_COORD_12.senha);

            await acessarSubprocessoGestor(page, descricao, UNIDADE_ALVO);
            await navegarParaAtividadesVisualizacao(page);
            await capturarTela(page, '05-mapa', '00a-analise-gestor-coordenadoria', {
                fullPage: true,
                extra: { perfil: 'GESTOR_COORD', acao: 'aceite-1' }
            });
            await page.getByTestId('btn-acao-analisar-principal').click();
            await page.getByTestId('inp-aceite-cadastro-obs').fill('Cadastro muito bem detalhado. Seguindo para a Secretaria.');
            await page.getByTestId('btn-aceite-cadastro-confirmar').click();
            await verificarPaginaPainel(page);

            await loginComPerfil(page, USUARIOS.GESTOR_SECRETARIA_1.titulo, USUARIOS.GESTOR_SECRETARIA_1.senha, USUARIOS.GESTOR_SECRETARIA_1.perfil);
            await acessarSubprocessoGestor(page, descricao, UNIDADE_ALVO);
            await navegarParaAtividadesVisualizacao(page);
            await capturarTela(page, '05-mapa', '00b-analise-chefe-secretaria', {
                fullPage: true,
                extra: { perfil: 'GESTOR_SEC', acao: 'aceite-2' }
            });
            await page.getByTestId('btn-acao-analisar-principal').click();
            await page.getByTestId('inp-aceite-cadastro-obs').fill('Ok. Para homologação do ADMIN.');
            await page.getByTestId('btn-aceite-cadastro-confirmar').click();
            await verificarPaginaPainel(page);

            await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);

            // Navegar para o subprocesso (admin vê tabela de unidades)
            await acessarSubprocessoAdmin(page, descricao, UNIDADE_ALVO);

            // Entrar no cadastro de atividades (visualização)
            await navegarParaAtividadesVisualizacao(page);

            await page.getByTestId('btn-acao-analisar-principal').click();
            await page.getByTestId('btn-aceite-cadastro-confirmar').click();

            // Agora o Mapa deve estar habilitado para edição pelo Admin
            // Navegar para mapa
            await navegarParaMapa(page);
            await capturarTela(page, '05-mapa', '01-mapa-vazio', {
                fullPage: true,
                tags: ['mapa', 'vazio'],
                extra: { perfil: 'ADMIN' }
            });

            await abrirModalCriarCompetencia(page);
            await expect(page.getByTestId('mdl-criar-competencia')).toBeVisible();
            await capturarTela(page, '05-mapa', '02-modal-criar-competencia', {
                tags: ['modal', 'criacao-competencia']
            });

            const competenciaDesc = 'Desenvolvimento de Software';
            await page.getByTestId('inp-criar-competencia-descricao').fill(competenciaDesc);
            await capturarTela(page, '05-mapa', '03-modal-criar-competencia-preenchida', {
                extra: { competencia: competenciaDesc }
            });

            const modal = page.getByTestId('mdl-criar-competencia');
            await modal.locator('label').filter({hasText: 'Desenvolvimento web'}).click();
            await modal.locator('label').filter({hasText: 'Desenvolvimento backend'}).click();
            await capturarTela(page, '05-mapa', '04-modal-criar-competencia-atividades-selecionadas', {
                tags: ['selecao', 'atividades']
            });

            await page.getByTestId('btn-criar-competencia-salvar').click();
            await expect(modal).toBeHidden();
            await capturarTela(page, '05-mapa', '05-mapa-com-competencia', {
                fullPage: true,
                extra: { competenciaCriada: competenciaDesc }
            });

            // Hover na competência
            const card = page.locator('.competencia-card', {has: page.getByText(competenciaDesc)});
            await card.hover();
            await page.waitForTimeout(100);
            await capturarTela(page, '05-mapa', '06-mapa-competencia-hover', {
                tags: ['interacao', 'hover']
            });

            await expect(page.getByTestId('btn-cad-mapa-disponibilizar')).toBeVisible();
            await page.getByTestId('btn-cad-mapa-disponibilizar').click();
            await expect(page.getByRole('dialog')).toBeVisible();
            await capturarTela(page, '05-mapa', '07-modal-disponibilizar-mapa', {
                tags: ['modal', 'disponibilizar-mapa']
            });

            const dataLimite = new Date();
            dataLimite.setDate(dataLimite.getDate() + 30);
            await page.getByTestId('inp-disponibilizar-mapa-data').fill(dataLimite.toISOString().split('T')[0]);
            await page.getByTestId('btn-disponibilizar-mapa-confirmar').click();
            await verificarPaginaPainel(page);
            await capturarTela(page, '05-mapa', '08-mapa-disponibilizado', {
                fullPage: true,
                extra: { estado: 'MAPA_DISPONIBILIZADO' }
            });
        });
    });

    test.describe('06 - Navegação e Menus', () => {
        test('Captura elementos de navegação', async ({page}) => {
            await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);

            await capturarTela(page, '06-navegacao', '01-menu-principal', { tags: ['layout', 'menu'] });

            // Configs (se admin)
            await page.getByTestId('btn-configuracoes').click();
            await expect(page).toHaveURL(/\/config/);
            await capturarTela(page, '06-navegacao', '02-menu-configs', { extra: { secao: 'configuracoes' } });
            await page.goto('/painel');

            // Seção unidades (para ADMIN)
            await page.getByText('Unidades').first().click();
            await expect(page).toHaveURL(/\/unidades/);
            await capturarTela(page, '06-navegacao', '03-unidades', { fullPage: true });

            await page.getByText('Relatórios').click();
            await expect(page).toHaveURL(/\/relatorios/);
            await capturarTela(page, '06-navegacao', '04-relatorios', { fullPage: true });

            await page.getByText('Histórico').click();
            await expect(page).toHaveURL(/\/historico/);
            await capturarTela(page, '06-navegacao', '05-historico', { fullPage: true });
            await capturarComponente(page.getByRole('navigation').first(), '06-navegacao', '05a-barra-lateral', { parte: 'sidebar' });

            await page.evaluate(() => window.scrollTo(0, document.body.scrollHeight));
            await page.waitForTimeout(100);
            await capturarTela(page, '06-navegacao', '06-rodape', { tags: ['layout', 'footer'] });
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
            if (codProcesso1 > 0) cleanup.registrar(codProcesso1);
            await page.goto('/painel');

            await capturarTela(page, '07-estados', '01-processo-criado', { extra: { estado: 'CRIADO' } });

            const processosAndamento = `Proc ANDAMENTO ${Date.now()}`;
            await criarProcesso(page, {
                descricao: processosAndamento,
                tipo: 'REVISAO',
                diasLimite: 30,
                unidade: 'ASSESSORIA_12',
                expandir: ['SECRETARIA_1'],
                iniciar: true
            });


            await capturarTela(page, '07-estados', '02-processo-em-andamento', { extra: { estado: 'EM_ANDAMENTO' } });
            await capturarComponente(page.getByTestId('tbl-processos'), '07-estados', '03-tabela-com-multiplos-estados', { contexto: 'painel-admin' });
        });
    });

    test.describe('08 - Responsividade (Tamanhos de Tela)', () => {
        test('Captura em diferentes resoluções', async ({page}) => {
            await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);

            // Desktop padrão (1920x1080)
            await page.setViewportSize({width: 1920, height: 1080});
            await capturarTela(page, '08-responsividade', '01-desktop-1920x1080', { fullPage: true, tags: ['res-high'] });

            // Desktop médio (1366x768)
            await page.setViewportSize({width: 1366, height: 768});
            await capturarTela(page, '08-responsividade', '02-desktop-1366x768', { fullPage: true, tags: ['res-medium'] });

            await page.setViewportSize({width: 768, height: 1024});
            await capturarTela(page, '08-responsividade', '03-tablet-768x1024', { fullPage: true, tags: ['res-tablet'] });

            await page.setViewportSize({width: 375, height: 667});
            await capturarTela(page, '08-responsividade', '04-mobile-375x667', { fullPage: true, tags: ['res-mobile'] });
        });
    });

    // SEÇÃO 09 - OPERAÇÕES EM BLOCO (CDUs 22-26)
    test.describe('09 - Operações em Bloco', () => {
        test('Captura fluxo de aceitar cadastros em bloco', async ({page}) => {
            // Prepara cenário: criar processo com unidades subordinadas e disponibilizar cadastros
            await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);

            const descricao = `Processo bloco ${Date.now()}`;
            // Criar com múltiplas unidades para que GESTOR veja tela de "Unidades participantes"
            // COORD_22 tem SECAO_221 e é gerido por GESTOR_COORD_22
            await criarProcesso(page, {
                descricao,
                tipo: 'MAPEAMENTO',
                diasLimite: 30,
                unidade: ['SECAO_221'],  // Usar unidade livre
                expandir: ['SECRETARIA_2', 'COORD_22'],
                iniciar: true
            });

            // Login como Chefe da SECAO_221 para disponibilizar cadastro
            await fazerLogout(page);
            await login(page, USUARIOS.CHEFE_SECAO_221.titulo, USUARIOS.CHEFE_SECAO_221.senha);

            await page.getByTestId('tbl-processos').getByText(descricao).first().click();
            await navegarParaSubprocesso(page, 'SECAO_221');
            await expect(page).toHaveURL(/processo\/\d+/);

            // Capturar ID para cleanup (após navegar para o subprocesso)
            const codProcesso = await extrairProcessoCodigo(page);
            if (codProcesso > 0) cleanup.registrar(codProcesso);
            await navegarParaAtividades(page);
            await adicionarAtividade(page, 'Atividade bloco 1');
            await adicionarConhecimento(page, 'Atividade bloco 1', 'Conhecimento 1');
            await page.getByTestId('btn-cad-atividades-disponibilizar').click();
            await expect(page.getByTestId('btn-confirmar-disponibilizacao')).toBeVisible();
            await page.getByTestId('btn-confirmar-disponibilizacao').click();
            await page.waitForTimeout(500);

            // Login como Gestor da COORD_22 para ver botão de aceitar em bloco
            await fazerLogout(page);
            await login(page, USUARIOS.GESTOR_COORD_22.titulo, USUARIOS.GESTOR_COORD_22.senha);

            await page.getByTestId('tbl-processos').getByText(descricao).first().click();
            await expect(page.getByRole('heading', {name: /Unidades participantes/i})).toBeVisible();
            await capturarTela(page, '09-operacoes-bloco', '01-detalhes-processo-gestor', { 
                fullPage: true, 
                extra: { perfil: 'GESTOR', acao: 'ver-bloco' } 
            });

            // Capturar botão de aceitar em bloco (se visível)
            const btnAceitarBloco = page.getByRole('button', {name: /Aceitar.*Bloco/i});
            await expect(btnAceitarBloco).toBeVisible();
            await btnAceitarBloco.click();
            await page.waitForTimeout(300);
            await capturarTela(page, '09-operacoes-bloco', '02-modal-aceitar-cadastro-bloco', { tags: ['modal', 'bloco'] });
            await page.getByRole('button', {name: /Cancelar/i}).click();

            // Executar aceite real para mover subprocesso para Secretaria 2
            await btnAceitarBloco.click();
            await page.getByRole('button', {name: TEXTOS.acaoBloco.aceitar.BOTAO}).click();
            await page.waitForTimeout(500);

            // Login como Gestor da SECRETARIA_2 para aceitar e mover para o ADMIN
            await fazerLogout(page);
            await loginComPerfil(
                page,
                USUARIOS.CHEFE_SECRETARIA_2.titulo,
                USUARIOS.CHEFE_SECRETARIA_2.senha,
                'GESTOR - SECRETARIA_2'
            );
            await page.getByTestId('tbl-processos').getByText(descricao).first().click();
            await expect(page.getByRole('heading', {name: /Unidades participantes/i})).toBeVisible();
            
            const btnAceitarBlocoSec2 = page.getByRole('button', {name: /Aceitar.*Bloco/i});
            await expect(btnAceitarBlocoSec2).toBeVisible();
            await btnAceitarBlocoSec2.click();
            await page.getByRole('button', {name: TEXTOS.acaoBloco.aceitar.BOTAO}).click();
            await page.waitForTimeout(500);

            // Login como Admin para homologar em bloco
            await fazerLogout(page);
            await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);

            await page.getByTestId('tbl-processos').getByText(descricao).first().click();
            await expect(page.getByRole('heading', {name: /Unidades participantes/i})).toBeVisible();

            // Capturar botão de homologar cadastro em bloco (CDU-23)
            const btnHomologarBloco = page.getByRole('button', {name: /Homologar.*Bloco/i});
            await expect(btnHomologarBloco).toBeVisible();
            await btnHomologarBloco.click();
            await page.waitForTimeout(300);
            await capturarTela(page, '09-operacoes-bloco', '03-modal-homologar-cadastro-bloco', { extra: { perfil: 'ADMIN' } });
            await page.getByRole('button', {name: /Cancelar/i}).click();

            // Capturar botão de disponibilizar mapas em bloco (CDU-24)
            const btnDisponibilizarMapaBloco = page.getByRole('button', {name: /Disponibilizar.*mapa.*Bloco/i});
            await expect(btnDisponibilizarMapaBloco).toBeVisible();
            await expect(btnDisponibilizarMapaBloco).toBeDisabled();

            // Capturar botões de aceitar/homologar mapa em bloco (CDU-25 e CDU-26 - se visíveis)
            const btnAceitarMapaBloco = page.getByRole('button', {name: /Aceitar.*mapa.*Bloco/i});
            await expect(btnAceitarMapaBloco).toBeHidden();

            const btnHomologarMapaBloco = page.getByRole('button', {name: /Homologar.*mapa.*Bloco/i});
            await expect(btnHomologarMapaBloco).toBeHidden();
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
            if (codProcesso > 0) cleanup.registrar(codProcesso);

            await page.getByRole('row', {name: /SECAO_121/i}).click();
            await expect(page).toHaveURL(/\/processo\/\d+\/SECAO_121/);
            await capturarTela(page, '10-gestao-subprocessos', '01-detalhes-subprocesso-admin', { fullPage: true, extra: { perfil: 'ADMIN' } });

            // Modal de alterar data limite (CDU-27)
            const btnAlterarData = page.getByRole('button', {name: /Alterar.*data.*limite/i});
            await expect(btnAlterarData).toBeVisible();
            await btnAlterarData.click();
            await page.waitForTimeout(300);
            await capturarTela(page, '10-gestao-subprocessos', '02-modal-alterar-data-limite', { tags: ['modal', 'gestao'] });
            await page.getByRole('button', {name: /Cancelar/i}).click();

            // Modal de reabrir cadastro (CDU-32)
            const btnReabrirCadastro = page.getByRole('button', {name: /Reabrir.*cadastro/i});
            await expect(btnReabrirCadastro).toBeHidden();

            // CDU-34: Botão de enviar lembrete (ação direta, sem modal)
            const btnEnviarLembrete = page.getByRole('button', {name: /Enviar.*lembrete/i});
            await expect(btnEnviarLembrete).toBeVisible();
            await capturarTela(page, '10-gestao-subprocessos', '04-botao-enviar-lembrete', { tags: ['acao-direta'] });
        });
    });

    // ADMIN - CONSULTAS E CONFIGURAÇÕES
    test.describe('Admin - Consultas e Configurações', () => {
        test('Captura telas administrativas (Unidades, Histórico, Configurações e Relatórios)', async ({page}) => {
            await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);

            const linkUnidades = page.getByRole('link', {name: /Unidades/i});
            await expect(linkUnidades).toBeVisible();
            await linkUnidades.click();
            await page.waitForTimeout(500);
            await capturarTela(page, '11-unidades', '01-arvore-unidades', { fullPage: true });

            const btnExpand = page.getByTestId('btn-arvore-expand-SECRETARIA_1');
            await expect(btnExpand).toBeVisible();
            await btnExpand.click();
            await page.waitForTimeout(300);

            const btnExpandCoord12 = page.getByTestId('btn-arvore-expand-COORD_12');
            await expect(btnExpandCoord12).toBeVisible();
            await btnExpandCoord12.click();
            await page.waitForTimeout(300);
            await capturarTela(page, '11-unidades', '02-arvore-unidades-expandida', { fullPage: true });

            const unidade = page.getByText('SECAO_121').first();
            await expect(unidade).toBeVisible();
            await unidade.click();
            await page.waitForTimeout(500);
            await capturarTela(page, '11-unidades', '03-detalhes-unidade', { fullPage: true, extra: { unidade: 'SECAO_121' } });

            const btnCriarAtribuicao = page.getByRole('button', {name: /Criar atribuição|Nova atribuição/i});
            await expect(btnCriarAtribuicao).toBeVisible();
            await btnCriarAtribuicao.click();
            await page.waitForTimeout(300);
            await capturarTela(page, '11-unidades', '04-modal-criar-atribuicao', { tags: ['modal', 'atribuicao'] });
            await page.getByRole('button', {name: /Cancelar/i}).click();

            const linkHistorico = page.getByRole('link', {name: /Histórico/i});
            await expect(linkHistorico).toBeVisible();
            await linkHistorico.click();
            await page.waitForTimeout(500);
            await capturarTela(page, '12-historico', '01-pagina-historico', { fullPage: true });

            const tabela = page.locator('table');
            await expect(tabela).toBeVisible();
            await capturarTela(page, '12-historico', '02-tabela-processos-finalizados', { fullPage: true });
            await capturarComponente(tabela.first(), '12-historico', '03-tabela-processos-finalizados-detalhe', { contexto: 'historico' });

            await page.getByTestId('btn-configuracoes').click();
            await page.waitForTimeout(500);
            await capturarTela(page, '13-configuracoes', '01-pagina-configuracoes', { fullPage: true });

            const inputDiasInativacao = page.getByLabel(/Dias para inativação de processos/i);
            await expect(inputDiasInativacao).toBeVisible();
            await capturarTela(page, '13-configuracoes', '02-config-sistema', { tags: ['config'] });

            await page.getByTestId('btn-administradores').click();
            await page.waitForTimeout(500);
            await capturarTela(page, '13-configuracoes', '03-lista-administradores', { fullPage: true });

            const btnAdicionar = page.getByRole('button', {name: /Adicionar|Novo/i});
            await expect(btnAdicionar).toBeVisible();
            await btnAdicionar.click();
            await page.waitForTimeout(300);
            await capturarTela(page, '13-configuracoes', '04-modal-adicionar-administrador', { tags: ['modal', 'admin'] });
            await page.getByRole('button', {name: /Cancelar/i}).click();

            const linkRelatorios = page.getByRole('link', {name: /Relatórios/i});
            await expect(linkRelatorios).toBeVisible();
            await linkRelatorios.click();
            await page.waitForTimeout(500);
            await capturarTela(page, '14-relatorios', '01-pagina-relatorios', { fullPage: true });

            const painelAndamento = page.getByRole('tabpanel', {name: /Andamento de processo/i});
            await expect(page.getByRole('tab', {name: /Andamento de processo/i})).toBeVisible();
            await expect(painelAndamento.getByLabel(/Selecione o Processo/i)).toBeVisible();
            await capturarTela(page, '14-relatorios', '02-relatorio-andamento', { extra: { relatorio: 'andamento' } });

            await expect(painelAndamento.getByRole('button', {name: /Gerar relatório/i})).toBeVisible();
            await capturarTela(page, '14-relatorios', '03-botao-gerar-relatorio', { tags: ['ui-element'] });

            await page.getByRole('tab', {name: /^Mapas$/i}).click();
            await page.waitForTimeout(300);
            const painelMapas = page.getByRole('tabpanel', {name: /^Mapas$/i});
            await expect(painelMapas.getByLabel(/Selecione o Processo/i)).toBeVisible();
            await expect(painelMapas.getByLabel(/Selecione a unidade/i)).toBeVisible();
            await capturarTela(page, '14-relatorios', '04-relatorio-mapas', { extra: { relatorio: 'mapas' } });

            await expect(painelMapas.getByRole('button', {name: /Gerar PDF/i})).toBeVisible();
            await capturarTela(page, '14-relatorios', '05-botao-gerar-pdf', { tags: ['ui-element', 'pdf'] });
        });
    });
});
