import {expect, Page, test} from '@playwright/test';
import {login, loginComPerfil, USUARIOS} from './helpers/helpers-auth';
import {criarProcesso} from './helpers/helpers-processos';
import {
    adicionarAtividade,
    adicionarConhecimento,
    navegarParaAtividades
} from './helpers/helpers-atividades';
import {criarCompetencia, disponibilizarMapa, navegarParaMapa} from './helpers/helpers-mapas';
import {resetDatabase, useProcessoCleanup} from './hooks/hooks-limpeza';
import * as path from 'path';
import * as fs from 'fs';

/**
 * Suite de testes dedicada a capturar screenshots de todas as telas do sistema
 * para refinamento de UI.
 * 
 * Objetivo: Capturar o máximo de telas e estados possíveis para análise manual.
 * NÃO é um teste de regressão visual, mas sim uma ferramenta de documentação visual.
 */

// Diretório onde as screenshots serão salvas
const SCREENSHOTS_DIR = path.join(process.cwd(), 'screenshots');

// Criar diretório se não existir
if (!fs.existsSync(SCREENSHOTS_DIR)) {
    fs.mkdirSync(SCREENSHOTS_DIR, {recursive: true});
}

/**
 * Helper para capturar screenshot com nome organizado
 */
async function capturarTela(page: Page, categoria: string, nome: string, opcoes?: {fullPage?: boolean}) {
    const nomeArquivo = `${categoria}--${nome}.png`;
    const caminhoCompleto = path.join(SCREENSHOTS_DIR, nomeArquivo);
    await page.screenshot({
        path: caminhoCompleto,
        fullPage: opcoes?.fullPage ?? false
    });
    console.log(`✓ Screenshot capturada: ${nomeArquivo}`);
}

test.describe('Captura de Telas - Sistema SGC', () => {
    let cleanup: ReturnType<typeof useProcessoCleanup>;

    test.beforeEach(async ({request}) => {
        await resetDatabase(request);
        cleanup = useProcessoCleanup();
    });

    test.afterEach(async ({request}) => {
        await cleanup.limpar(request);
    });

    test.describe('01 - Autenticação', () => {
        test('Captura telas de login', async ({page}) => {
            // Tela de login inicial
            await page.goto('/login');
            await capturarTela(page, '01-autenticacao', '01-login-inicial');

            // Erro de credenciais inválidas
            await page.getByTestId('inp-login-usuario').fill(USUARIOS.INVALIDO.titulo);
            await page.getByTestId('inp-login-senha').fill(USUARIOS.INVALIDO.senha);
            await page.getByTestId('btn-login-entrar').click();
            await page.waitForTimeout(500);
            await capturarTela(page, '01-autenticacao', '02-login-erro-credenciais');

            // Limpar e fazer login com múltiplos perfis
            await page.goto('/login');
            await page.getByTestId('inp-login-usuario').fill(USUARIOS.ADMIN_2_PERFIS.titulo);
            await page.getByTestId('inp-login-senha').fill(USUARIOS.ADMIN_2_PERFIS.senha);
            await page.getByTestId('btn-login-entrar').click();
            await page.waitForTimeout(500);
            await capturarTela(page, '01-autenticacao', '03-login-selecao-perfil');

            // Login com perfil selecionado
            // Reiniciar a página para garantir estado limpo para a função helper
            await page.goto('/login');
            await loginComPerfil(page, USUARIOS.ADMIN_2_PERFIS.titulo, USUARIOS.ADMIN_2_PERFIS.senha, USUARIOS.ADMIN_2_PERFIS.perfil);
            await capturarTela(page, '01-autenticacao', '04-painel-apos-login', {fullPage: true});
        });
    });

    test.describe('02 - Painel Principal', () => {
        test('Captura painel ADMIN', async ({page}) => {
            await page.goto('/login');
            await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);

            // Painel inicial vazio
            await capturarTela(page, '02-painel', '01-painel-admin-vazio', {fullPage: true});

            // Criar um processo para popular o painel
            const descricaoProcesso = `Processo Captura ${Date.now()}`;
            await page.getByTestId('btn-painel-criar-processo').click();
            await expect(page).toHaveURL(/\/processo\/cadastro/);
            await capturarTela(page, '02-painel', '02-criar-processo-form-vazio');

            // Preenchendo formulário
            await page.getByTestId('inp-processo-descricao').fill(descricaoProcesso);
            await page.getByTestId('sel-processo-tipo').selectOption('MAPEAMENTO');
            const dataLimite = new Date();
            dataLimite.setDate(dataLimite.getDate() + 30);
            await page.getByTestId('inp-processo-data-limite').fill(dataLimite.toISOString().split('T')[0]);
            await capturarTela(page, '02-painel', '03-criar-processo-form-preenchido');

            // Expandir árvore de unidades
            await expect(page.getByText('Carregando unidades...')).toBeHidden();
            await page.getByTestId('btn-arvore-expand-SECRETARIA_1').click();
            await page.waitForTimeout(300);
            await capturarTela(page, '02-painel', '04-arvore-unidades-expandida');

            // Selecionar unidade
            await page.getByTestId('chk-arvore-unidade-ASSESSORIA_11').click();
            await capturarTela(page, '02-painel', '05-arvore-unidades-selecionada');

            // Salvar processo
            await page.getByTestId('btn-processo-salvar').click();
            await expect(page).toHaveURL(/\/painel/);

            // Capturar ID para cleanup
            await page.getByText(descricaoProcesso).click();
            const processoId = Number.parseInt(new RegExp(/codProcesso=(\d+)/).exec(page.url())?.[1] || '0');
            if (processoId > 0) cleanup.registrar(processoId);
            await page.goto('/painel');

            // Painel com processo criado
            await capturarTela(page, '02-painel', '06-painel-admin-com-processo', {fullPage: true});

            // Hover em linha da tabela
            await page.getByText(descricaoProcesso).hover();
            await capturarTela(page, '02-painel', '07-painel-hover-processo');
        });

        test('Captura painel GESTOR', async ({page}) => {
            await page.goto('/login');
            await login(page, USUARIOS.GESTOR_COORD.titulo, USUARIOS.GESTOR_COORD.senha);
            await capturarTela(page, '02-painel', '10-painel-gestor', {fullPage: true});
        });

        test('Captura painel CHEFE', async ({page}) => {
            await page.goto('/login');
            await login(page, USUARIOS.CHEFE_SECAO_211.titulo, USUARIOS.CHEFE_SECAO_211.senha);
            await capturarTela(page, '02-painel', '11-painel-chefe', {fullPage: true});
        });
    });

    test.describe('03 - Fluxo de Processo', () => {
        test('Captura criação e detalhamento de processo', async ({page}) => {
            await page.goto('/login');
            await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);

            const descricao = `Processo Detalhado ${Date.now()}`;
            await criarProcesso(page, {
                descricao,
                tipo: 'MAPEAMENTO',
                diasLimite: 30,
                unidade: 'ASSESSORIA_12',
                expandir: ['SECRETARIA_1']
            });

            // Capturar ID para cleanup
            await page.getByText(descricao).click();
            const processoId = Number.parseInt(new RegExp(/codProcesso=(\d+)/).exec(page.url())?.[1] || '0');
            if (processoId > 0) cleanup.registrar(processoId);

            // Tela de edição de processo
            await capturarTela(page, '03-processo', '01-processo-edicao');

            // Modal de iniciar processo
            await page.getByTestId('btn-processo-iniciar').click();
            await page.waitForTimeout(300);
            await capturarTela(page, '03-processo', '02-modal-iniciar-processo');
            await page.getByTestId('btn-iniciar-processo-confirmar').click();
            await expect(page).toHaveURL(/\/painel/);

            // Acessar detalhes do processo iniciado
            await page.getByText(descricao).click();
            await expect(page).toHaveURL(/\/processo\/\d+/);
            await capturarTela(page, '03-processo', '03-detalhes-processo-iniciado', {fullPage: true});

            // Modal de finalizar processo
            await page.getByTestId('btn-processo-finalizar').click();
            await page.waitForTimeout(300);
            await capturarTela(page, '03-processo', '04-modal-finalizar-processo');
            await page.getByRole('button', {name: 'Cancelar'}).click();
        });

        test('Captura validações de formulário', async ({page}) => {
            await page.goto('/login');
            await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);

            await page.getByTestId('btn-painel-criar-processo').click();
            await expect(page).toHaveURL(/\/processo\/cadastro/);

            // Tentar salvar sem preencher
            await page.getByTestId('btn-processo-salvar').click();
            await page.waitForTimeout(300);
            await capturarTela(page, '03-processo', '10-validacao-descricao-obrigatoria');

            // Preencher apenas descrição
            await page.getByTestId('inp-processo-descricao').fill('Teste Validação');
            await page.getByTestId('btn-processo-salvar').click();
            await page.waitForTimeout(300);
            await capturarTela(page, '03-processo', '11-validacao-unidade-obrigatoria');
        });
    });

    test.describe('04 - Subprocesso e Atividades', () => {
        test('Captura fluxo completo de atividades', async ({page}) => {
            const descricao = `Proc Atividades ${Date.now()}`;
            const UNIDADE_ALVO = 'SECAO_211';

            // Admin cria processo
            await page.goto('/login');
            await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);

            await criarProcesso(page, {
                descricao,
                tipo: 'MAPEAMENTO',
                diasLimite: 30,
                unidade: UNIDADE_ALVO,
                expandir: ['SECRETARIA_2', 'COORD_21']
            });

            const linhaProcesso = page.locator('tr').filter({has: page.getByText(descricao)});
            await linhaProcesso.click();
            const processoId = Number.parseInt(new RegExp(/\/processo\/cadastro\/(\d+)/).exec(page.url())?.[1] || '0');
            if (processoId > 0) cleanup.registrar(processoId);

            await page.getByTestId('btn-processo-iniciar').click();
            await page.getByTestId('btn-iniciar-processo-confirmar').click();
            await expect(page).toHaveURL(/\/painel/);

            // Logout e login como Chefe
            await page.getByTestId('btn-logout').click();
            await login(page, USUARIOS.CHEFE_SECAO_211.titulo, USUARIOS.CHEFE_SECAO_211.senha);

            // Acessar subprocesso
            await page.getByText(descricao).click();
            await capturarTela(page, '04-subprocesso', '01-dashboard-subprocesso', {fullPage: true});

            // Entrar em atividades
            await navegarParaAtividades(page);
            await capturarTela(page, '04-subprocesso', '02-cadastro-atividades-vazio', {fullPage: true});

            // Adicionar atividade
            const atividadeDesc = `Atividade Teste ${Date.now()}`;
            await page.getByTestId('inp-nova-atividade').fill(atividadeDesc);
            await capturarTela(page, '04-subprocesso', '03-cadastro-atividades-preenchendo');
            await page.getByTestId('btn-adicionar-atividade').click();
            await expect(page.getByText(atividadeDesc, {exact: true})).toBeVisible();
            await capturarTela(page, '04-subprocesso', '04-cadastro-atividades-com-uma', {fullPage: true});

            // Adicionar conhecimentos
            const card = page.locator('.atividade-card', {has: page.getByText(atividadeDesc)});
            await card.getByTestId('inp-novo-conhecimento').fill('Java');
            await capturarTela(page, '04-subprocesso', '05-cadastro-conhecimento-preenchendo');
            await adicionarConhecimento(page, atividadeDesc, 'Java');
            await adicionarConhecimento(page, atividadeDesc, 'Spring Boot');
            await adicionarConhecimento(page, atividadeDesc, 'PostgreSQL');
            await capturarTela(page, '04-subprocesso', '06-cadastro-atividade-completa', {fullPage: true});

            // Hover na atividade para ver ações
            await card.locator('.atividade-hover-row').hover();
            await page.waitForTimeout(300);
            await capturarTela(page, '04-subprocesso', '07-cadastro-atividade-hover');

            // Tentar disponibilizar sem data
            await page.getByTestId('btn-cad-atividades-disponibilizar').click();
            await page.waitForTimeout(300);
            await capturarTela(page, '04-subprocesso', '08-modal-disponibilizar-atividades');
            await page.getByTestId('btn-confirmar-disponibilizacao').click();
            await page.waitForTimeout(500);
            await capturarTela(page, '04-subprocesso', '09-cadastro-atividades-disponibilizado', {fullPage: true});
        });

        test('Captura estados de validação de atividades', async ({page}) => {
            const descricao = `Proc Validação ${Date.now()}`;
            const UNIDADE_ALVO = 'SECAO_212';

            await page.goto('/login');
            await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);

            await criarProcesso(page, {
                descricao,
                tipo: 'MAPEAMENTO',
                diasLimite: 30,
                unidade: UNIDADE_ALVO,
                expandir: ['SECRETARIA_2', 'COORD_21']
            });

            const linhaProcesso = page.locator('tr').filter({has: page.getByText(descricao)});
            await linhaProcesso.click();
            const processoId = Number.parseInt(new RegExp(/\/processo\/cadastro\/(\d+)/).exec(page.url())?.[1] || '0');
            if (processoId > 0) cleanup.registrar(processoId);

            await page.getByTestId('btn-processo-iniciar').click();
            await page.getByTestId('btn-iniciar-processo-confirmar').click();

            await page.getByTestId('btn-logout').click();
            await login(page, USUARIOS.CHEFE_SECAO_212.titulo, USUARIOS.CHEFE_SECAO_212.senha);

            await page.getByText(descricao).click();
            await navegarParaAtividades(page);

            // Adicionar atividade SEM conhecimento
            const atividadeIncompleta = `Atividade Incompleta ${Date.now()}`;
            await adicionarAtividade(page, atividadeIncompleta);

            // Tentar disponibilizar
            await page.getByTestId('btn-cad-atividades-disponibilizar').click();
            await page.waitForTimeout(500);
            
            // Modal de pendências
            const modalPendencias = page.locator('.modal-content').filter({hasText: 'Pendências para disponibilização'});
            if (await modalPendencias.isVisible()) {
                await capturarTela(page, '04-subprocesso', '20-validacao-atividade-sem-conhecimento');
            }
        });
    });

    test.describe('05 - Mapa de Competências', () => {
        test('Captura fluxo de mapa de competências', async ({page}) => {
            const descricao = `Proc Mapa ${Date.now()}`;
            const UNIDADE_ALVO = 'SECAO_121';

            // Setup: criar processo e disponibilizar atividades
            await page.goto('/login');
            await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);

            await criarProcesso(page, {
                descricao,
                tipo: 'MAPEAMENTO',
                diasLimite: 30,
                unidade: UNIDADE_ALVO,
                expandir: ['SECRETARIA_1', 'COORD_12']
            });

            const linhaProcesso = page.locator('tr').filter({has: page.getByText(descricao)});
            await linhaProcesso.click();
            const processoId = Number.parseInt(new RegExp(/\/processo\/cadastro\/(\d+)/).exec(page.url())?.[1] || '0');
            if (processoId > 0) cleanup.registrar(processoId);

            await page.getByTestId('btn-processo-iniciar').click();
            await page.getByTestId('btn-iniciar-processo-confirmar').click();

            await page.getByTestId('btn-logout').click();
            await login(page, USUARIOS.CHEFE_SECAO_121.titulo, USUARIOS.CHEFE_SECAO_121.senha);

            await page.getByText(descricao).click();
            await navegarParaAtividades(page);

            // Adicionar atividades
            await adicionarAtividade(page, 'Desenvolvimento Web');
            await adicionarConhecimento(page, 'Desenvolvimento Web', 'Vue.js');
            await adicionarConhecimento(page, 'Desenvolvimento Web', 'TypeScript');

            await adicionarAtividade(page, 'Desenvolvimento Backend');
            await adicionarConhecimento(page, 'Desenvolvimento Backend', 'Java');
            await adicionarConhecimento(page, 'Desenvolvimento Backend', 'Spring Boot');

            // Disponibilizar (como chefe)
            await page.getByTestId('btn-cad-atividades-disponibilizar').click();
            await page.getByTestId('btn-confirmar-disponibilizacao').click();
            await page.waitForTimeout(1000);

            // Logout e login como ADMIN para Homologar
            await page.getByTestId('btn-logout').click();
            await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);

            // Navegar para o subprocesso
            await page.getByText(descricao).click();
            // Clicar na unidade (como admin vê tabela)
            await page.getByRole('row', {name: 'Seção 121'}).click();

            // Entrar no cadastro de atividades (visualização)
            await page.getByTestId('card-subprocesso-atividades-vis').click();

            // Homologar cadastro
            await page.getByTestId('btn-acao-analisar-principal').click();
            await page.getByTestId('btn-aceite-cadastro-confirmar').click();
            await page.waitForTimeout(1000);

            // Agora o Mapa deve estar habilitado para edição pelo Admin
            // Navegar para mapa
            await navegarParaMapa(page);
            await capturarTela(page, '05-mapa', '01-mapa-vazio', {fullPage: true});

            // Criar competência
            await page.getByTestId('btn-abrir-criar-competencia').click();
            await page.waitForTimeout(300);
            await capturarTela(page, '05-mapa', '02-modal-criar-competencia');

            const competenciaDesc = 'Desenvolvimento de Software';
            await page.getByTestId('inp-criar-competencia-descricao').fill(competenciaDesc);
            await capturarTela(page, '05-mapa', '03-modal-criar-competencia-preenchida');

            // Selecionar atividades
            const modal = page.getByTestId('mdl-criar-competencia');
            await modal.locator('label').filter({hasText: 'Desenvolvimento Web'}).click();
            await modal.locator('label').filter({hasText: 'Desenvolvimento Backend'}).click();
            await capturarTela(page, '05-mapa', '04-modal-criar-competencia-atividades-selecionadas');

            await page.getByTestId('btn-criar-competencia-salvar').click();
            await expect(modal).toBeHidden();
            await page.waitForTimeout(500);
            await capturarTela(page, '05-mapa', '05-mapa-com-competencia', {fullPage: true});

            // Hover na competência
            const card = page.locator('.competencia-card', {has: page.getByText(competenciaDesc)});
            await card.locator('.competencia-hover-row').hover();
            await page.waitForTimeout(300);
            await capturarTela(page, '05-mapa', '06-mapa-competencia-hover');

            // Disponibilizar mapa
            await page.getByTestId('btn-cad-mapa-disponibilizar').click();
            await page.waitForTimeout(300);
            await capturarTela(page, '05-mapa', '07-modal-disponibilizar-mapa');

            const dataLimite = new Date();
            dataLimite.setDate(dataLimite.getDate() + 30);
            await page.getByTestId('inp-disponibilizar-mapa-data').fill(dataLimite.toISOString().split('T')[0]);
            await page.getByTestId('btn-disponibilizar-mapa-confirmar').click();
            await page.waitForTimeout(500);
            await capturarTela(page, '05-mapa', '08-mapa-disponibilizado', {fullPage: true});
        });
    });

    test.describe('06 - Navegação e Menus', () => {
        test('Captura elementos de navegação', async ({page}) => {
            await page.goto('/login');
            await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);

            // Menu principal
            await capturarTela(page, '06-navegacao', '01-menu-principal');

            // Configurações (se admin)
            await page.getByTestId('btn-configuracoes').click();
            await page.waitForTimeout(300);
            await capturarTela(page, '06-navegacao', '02-menu-configuracoes');
            await page.goto('/painel');

            // Seção Minha Unidade
            await page.getByText('Minha unidade').click();
            await page.waitForTimeout(500);
            await capturarTela(page, '06-navegacao', '03-minha-unidade', {fullPage: true});

            // Seção Relatórios
            await page.getByText('Relatórios').click();
            await page.waitForTimeout(500);
            await capturarTela(page, '06-navegacao', '04-relatorios', {fullPage: true});

            // Seção Histórico
            await page.getByText('Histórico').click();
            await page.waitForTimeout(500);
            await capturarTela(page, '06-navegacao', '05-historico', {fullPage: true});

            // Rodapé
            await page.evaluate(() => window.scrollTo(0, document.body.scrollHeight));
            await page.waitForTimeout(300);
            await capturarTela(page, '06-navegacao', '06-rodape');
        });
    });

    test.describe('07 - Estados e Situações', () => {
        test('Captura diferentes estados de processo', async ({page}) => {
            await page.goto('/login');
            await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);

            // Processo CRIADO
            const processosCriado = `Proc CRIADO ${Date.now()}`;
            await criarProcesso(page, {
                descricao: processosCriado,
                tipo: 'MAPEAMENTO',
                diasLimite: 30,
                unidade: 'ASSESSORIA_11',
                expandir: ['SECRETARIA_1']
            });

            await page.getByText(processosCriado).click();
            const processoId1 = Number.parseInt(new RegExp(/codProcesso=(\d+)/).exec(page.url())?.[1] || '0');
            if (processoId1 > 0) cleanup.registrar(processoId1);
            await page.goto('/painel');

            await capturarTela(page, '07-estados', '01-processo-criado');

            // Processo EM_ANDAMENTO
            const processosAndamento = `Proc ANDAMENTO ${Date.now()}`;
            await criarProcesso(page, {
                descricao: processosAndamento,
                tipo: 'REVISAO',
                diasLimite: 30,
                unidade: 'ASSESSORIA_12',
                expandir: ['SECRETARIA_1'],
                iniciar: true
            });

            const processoId2 = Number.parseInt(new RegExp(/\/processo\/(\d+)/).exec(page.url())?.[1] || '0');
            if (processoId2 > 0) cleanup.registrar(processoId2);
            
            await capturarTela(page, '07-estados', '02-processo-em-andamento');
        });
    });

    test.describe('08 - Responsividade (Tamanhos de Tela)', () => {
        test('Captura em diferentes resoluções', async ({page}) => {
            await page.goto('/login');
            await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);

            // Desktop padrão (1920x1080)
            await page.setViewportSize({width: 1920, height: 1080});
            await capturarTela(page, '08-responsividade', '01-desktop-1920x1080', {fullPage: true});

            // Desktop médio (1366x768)
            await page.setViewportSize({width: 1366, height: 768});
            await capturarTela(page, '08-responsividade', '02-desktop-1366x768', {fullPage: true});

            // Tablet (768x1024)
            await page.setViewportSize({width: 768, height: 1024});
            await capturarTela(page, '08-responsividade', '03-tablet-768x1024', {fullPage: true});

            // Mobile (375x667)
            await page.setViewportSize({width: 375, height: 667});
            await capturarTela(page, '08-responsividade', '04-mobile-375x667', {fullPage: true});
        });
    });
});
