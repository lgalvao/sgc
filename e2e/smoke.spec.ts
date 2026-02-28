import {expect, test} from './fixtures/base.js';
import logger from '../frontend/src/utils/logger.js';
import {login, loginComPerfil, USUARIOS} from './helpers/helpers-auth.js';
import {criarProcesso, extrairProcessoId} from './helpers/helpers-processos.js';
import {navegarParaSubprocesso} from './helpers/helpers-navegacao.js';
import {
    adicionarAtividade,
    adicionarConhecimento,
    navegarParaAtividades,
    navegarParaAtividadesVisualizacao
} from './helpers/helpers-atividades.js';
import {
    acessarSubprocessoAdmin,
    acessarSubprocessoChefeDireto,
    acessarSubprocessoGestor
} from './helpers/helpers-analise.js';
import {abrirModalCriarCompetencia, navegarParaMapa} from './helpers/helpers-mapas.js';
import {resetDatabase, useProcessoCleanup} from './hooks/hooks-limpeza.js';

/**
 * Suite de Smoke Tests do Sistema SGC
 *
 * Objetivo: Verificar os principais fluxos do sistema para garantir que não há erros bloqueantes.
 */

test.describe('Smoke Test - Sistema SGC', () => {
    test.setTimeout(60000); // Aumentar timeout para cenários longos
    let cleanup: ReturnType<typeof useProcessoCleanup>;

    test.beforeEach(async ({request}) => {
        cleanup = useProcessoCleanup();
        await resetDatabase(request);
    });

    test.afterEach(async ({request}) => {
        if (cleanup) {
            await cleanup.limpar(request);
        }
    });

    test.describe('01 - Autenticação', () => {
        test('Captura telas de login', async ({page}) => {
            await page.goto('/login');
            // Tela de login inicial
            await page.getByTestId('inp-login-usuario').click();

            // Erro de credenciais inválidas
            await page.getByTestId('inp-login-usuario').fill(USUARIOS.INVALIDO.titulo);
            await page.getByTestId('inp-login-senha').fill(USUARIOS.INVALIDO.senha);
            await page.getByTestId('btn-login-entrar').click();
            await page.waitForTimeout(500);

            // Limpar e fazer login com múltiplos perfis
            await page.getByTestId('inp-login-usuario').fill(USUARIOS.ADMIN_2_PERFIS.titulo);
            await page.getByTestId('inp-login-senha').fill(USUARIOS.ADMIN_2_PERFIS.senha);
            await page.getByTestId('btn-login-entrar').click();
            await page.waitForTimeout(500);

            // Login com perfil selecionado
            // Reiniciar a página para garantir estado limpo para a função helper
            await loginComPerfil(page, USUARIOS.ADMIN_2_PERFIS.titulo, USUARIOS.ADMIN_2_PERFIS.senha, USUARIOS.ADMIN_2_PERFIS.perfil);
        });
    });

    test.describe('02 - Painel Principal', () => {
        test('Captura painel ADMIN', async ({page}) => {
            await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);

            // Painel inicial vazio

            // Criar um processo para popular o painel
            const descricaoProcesso = `Processo Captura ${Date.now()}`;
            await page.getByTestId('btn-painel-criar-processo').click();
            await expect(page).toHaveURL(/\/processo\/cadastro/);

            // Preenchendo formulário
            await page.getByTestId('inp-processo-descricao').fill(descricaoProcesso);
            await page.getByTestId('sel-processo-tipo').selectOption('MAPEAMENTO');
            const dataLimite = new Date();
            dataLimite.setDate(dataLimite.getDate() + 30);
            await page.getByTestId('inp-processo-data-limite').fill(dataLimite.toISOString().split('T')[0]);

            // Expandir árvore de unidades
            await expect(page.getByText('Carregando unidades...')).toBeHidden();
            await page.getByTestId('btn-arvore-expand-SECRETARIA_1').click();
            await page.waitForTimeout(300);

            // Expandir COORD_11 para acessar SECAO_111
            await page.waitForTimeout(300);
            await page.getByTestId('btn-arvore-expand-COORD_11').click();
            await page.waitForTimeout(300);

            // Selecionar múltiplas unidades
            await page.getByTestId('chk-arvore-unidade-ASSESSORIA_11').click();
            await page.getByTestId('chk-arvore-unidade-ASSESSORIA_12').click();
            await page.getByTestId('chk-arvore-unidade-SECAO_111').click();

            // Salvar processo
            await page.getByTestId('btn-processo-salvar').click();
            await expect(page).toHaveURL(/\/painel/);
            await expect(page.getByTestId('tbl-processos')).toBeVisible();
            await expect(page.getByTestId('tbl-processos').getByText(descricaoProcesso).first()).toBeVisible();

            // Capturar ID para cleanup
            await page.getByTestId('tbl-processos').getByText(descricaoProcesso).first().click();
            await expect(page).toHaveURL(/codProcesso=\d+/);
            const processoId = await extrairProcessoId(page);
            if (processoId > 0) cleanup.registrar(processoId);
            await page.goto('/painel');

            // Painel com processo criado

            // Hover em linha da tabela
            await page.getByText(descricaoProcesso).hover();
        });

        test('Captura painel GESTOR', async ({page}) => {
            // Criar processo para o Gestor primeiro como ADMIN
            await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
            const desc = `Processo Gestor ${Date.now()}`;
            await criarProcesso(page, {
                descricao: desc,
                tipo: 'MAPEAMENTO',
                diasLimite: 30,
                unidade: 'COORD_11', // Unidade do Gestor COORD_11
                expandir: ['SECRETARIA_1'],
                iniciar: true
            });
            await page.getByTestId('btn-logout').click();

            await login(page, USUARIOS.GESTOR_COORD.titulo, USUARIOS.GESTOR_COORD.senha);
            await expect(page.getByTestId('tbl-processos')).toBeVisible();
            await expect(page.getByTestId('tbl-processos').getByText(desc).first()).toBeVisible();
        });

        test('Captura painel CHEFE', async ({page}) => {
            // Criar processo para o Chefe primeiro como ADMIN
            await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
            const desc = `Processo Chefe ${Date.now()}`;
            await criarProcesso(page, {
                descricao: desc,
                tipo: 'MAPEAMENTO',
                diasLimite: 30,
                unidade: 'SECAO_211', // Unidade do Chefe
                expandir: ['SECRETARIA_2', 'COORD_21'],
                iniciar: true
            });
            await page.getByTestId('btn-logout').click();

            await login(page, USUARIOS.CHEFE_SECAO_211.titulo, USUARIOS.CHEFE_SECAO_211.senha);
            await expect(page.getByTestId('tbl-processos')).toBeVisible();
            await expect(page.getByTestId('tbl-processos').getByText(desc).first()).toBeVisible();
        });
    });

    test.describe('03 - Fluxo de Processo', () => {
        test('Captura criação e detalhamento de processo', async ({page}) => {
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
            await page.getByTestId('tbl-processos').getByText(descricao).first().click();
            const processoId = await extrairProcessoId(page);
            if (processoId > 0) cleanup.registrar(processoId);

            // Tela de edição de processo

            // Modal de iniciar processo
            await page.getByTestId('btn-processo-iniciar').click();
            await page.waitForTimeout(300);
            await page.getByTestId('btn-iniciar-processo-confirmar').click();
            await expect(page).toHaveURL(/\/painel/);

            // Acessar detalhes do processo iniciado
            await page.getByTestId('tbl-processos').getByText(descricao).first().click();
            await expect(page).toHaveURL(/\/processo\/\d+/);

            // Modal de finalizar processo
            await page.getByTestId('btn-processo-finalizar').click();
            await page.waitForTimeout(300);
            await page.getByRole('button', {name: 'Cancelar'}).click();
        });

        test('Captura validações de formulário', async ({page}) => {
            await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);

            await page.getByTestId('btn-painel-criar-processo').click();
            await expect(page).toHaveURL(/\/processo\/cadastro/);

            // Capturar estado inicial com botões desativados (formulário vazio)
            await page.waitForTimeout(300);

            // Preencher apenas descrição (botões ainda desativados)
            await page.getByTestId('inp-processo-descricao').fill('Teste Validação');
            await page.waitForTimeout(300);

            // Selecionar tipo de processo (necessário para carregar árvore de unidades)
            await page.getByTestId('sel-processo-tipo').selectOption('MAPEAMENTO');

            // Preencher data limite (botões ainda desativados - falta unidade)
            const dataLimite = new Date();
            dataLimite.setDate(dataLimite.getDate() + 30);
            await page.getByTestId('inp-processo-data-limite').fill(dataLimite.toISOString().split('T')[0]);
            await page.waitForTimeout(300);

            // Expandir e selecionar unidade (agora botões devem estar ativados)
            await expect(page.getByText('Carregando unidades...')).toBeHidden();
            await page.getByTestId('btn-arvore-expand-SECRETARIA_1').click();
            await page.waitForTimeout(300);
            await page.getByTestId('chk-arvore-unidade-ASSESSORIA_11').click();
            await page.waitForTimeout(300);
        });
    });

    test.describe('04 - Subprocesso e Atividades', () => {
        test('Captura fluxo completo de atividades', async ({page}) => {
            const descricao = `Proc Atividades ${Date.now()}`;
            const UNIDADE_ALVO = 'SECAO_211';

            // Admin cria processo
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
            const processoId = await extrairProcessoId(page);
            if (processoId > 0) cleanup.registrar(processoId);

            await page.getByTestId('btn-processo-iniciar').click();
            await page.getByTestId('btn-iniciar-processo-confirmar').click();
            await expect(page).toHaveURL(/\/painel/);

            // Logout e login como Chefe
            await page.getByTestId('btn-logout').click();
            await login(page, USUARIOS.CHEFE_SECAO_211.titulo, USUARIOS.CHEFE_SECAO_211.senha);

            // Acessar subprocesso
            await page.getByTestId('tbl-processos').getByText(descricao).first().click();
            await navegarParaSubprocesso(page, UNIDADE_ALVO);

            // Entrar em atividades
            await navegarParaAtividades(page);

            // Adicionar atividade
            const atividadeDesc = `Atividade Teste ${Date.now()}`;
            await page.getByTestId('inp-nova-atividade').fill(atividadeDesc);
            await page.getByTestId('btn-adicionar-atividade').click();
            await expect(page.getByText(atividadeDesc, {exact: true})).toBeVisible();

            // Adicionar conhecimentos
            const card = page.locator('.atividade-card', {has: page.getByText(atividadeDesc)});
            await card.getByTestId('inp-novo-conhecimento').fill('Java');
            await adicionarConhecimento(page, atividadeDesc, 'Java');
            await adicionarConhecimento(page, atividadeDesc, 'Spring Boot');
            await adicionarConhecimento(page, atividadeDesc, 'Oracle');

            // Hover na atividade para ver ações
            await card.locator('.atividade-hover-row').hover();
            await page.waitForTimeout(300);

            // Tentar disponibilizar sem data
            await page.getByTestId('btn-cad-atividades-disponibilizar').click();
            await page.waitForTimeout(300);
            await page.getByTestId('btn-confirmar-disponibilizacao').click();
            await page.waitForTimeout(500);
            const cardDisponibilizado = page.locator('.atividade-card').first();
            if (await cardDisponibilizado.isVisible().catch(() => false)) {
            }
        });

        test('Captura estados de validação inline de atividades', async ({page}) => {
            const descricao = `Proc Validação ${Date.now()}`;
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
            const processoId = await extrairProcessoId(page);
            if (processoId > 0) cleanup.registrar(processoId);

            await page.getByTestId('btn-processo-iniciar').click();
            await page.getByTestId('btn-iniciar-processo-confirmar').click();

            await page.getByTestId('btn-logout').click();
            await login(page, USUARIOS.CHEFE_SECAO_212.titulo, USUARIOS.CHEFE_SECAO_212.senha);

            await page.getByTestId('tbl-processos').getByText(descricao).first().click();
            await navegarParaSubprocesso(page, UNIDADE_ALVO);
            await navegarParaAtividades(page);

            // Capturar tela inicial vazia com label "Conhecimentos *"

            // Adicionar primeira atividade SEM conhecimento
            const atividade1 = `Atividade Sem Conhecimento 1 ${Date.now()}`;
            await adicionarAtividade(page, atividade1);
            await page.waitForTimeout(300);

            // Adicionar segunda atividade SEM conhecimento
            const atividade2 = `Atividade Sem Conhecimento 2 ${Date.now()}`;
            await adicionarAtividade(page, atividade2);
            await page.waitForTimeout(300);

            // Adicionar terceira atividade COM conhecimento (para contraste)
            const atividade3 = `Atividade Com Conhecimento ${Date.now()}`;
            await adicionarAtividade(page, atividade3);
            await adicionarConhecimento(page, atividade3, 'Java');
            await page.waitForTimeout(300);

            // Tentar disponibilizar - deve mostrar validação inline
            await page.getByTestId('btn-cad-atividades-disponibilizar').click();
            await page.waitForTimeout(500); // Aguardar scroll automático

            // Capturar primeira atividade com erro inline (scroll automático já levou até ela)

            // Scroll para segunda atividade com erro
            const card2 = page.locator('.atividade-card', {has: page.getByText(atividade2)});
            await card2.scrollIntoViewIfNeeded();
            await page.waitForTimeout(300);

            // Capturar zoom na mensagem de erro inline
            const primeiroCard = page.locator('.atividade-card', {has: page.getByText(atividade1)});
            await primeiroCard.scrollIntoViewIfNeeded();
            await page.waitForTimeout(300);

            // Capturar apenas o card com erro para detalhe

            // Corrigir primeira atividade adicionando conhecimento
            await adicionarConhecimento(page, atividade1, 'Python');
            await page.waitForTimeout(500);

            // Tentar disponibilizar novamente - ainda deve ter erro na atividade 2
            await page.getByTestId('btn-cad-atividades-disponibilizar').click();
            await page.waitForTimeout(500);

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
        });
    });

    test.describe('05 - Mapa de Competências', () => {
        test('Captura fluxo de mapa de competências', async ({page}) => {
            page.on('console', msg => {
                const type = String(msg.type());
                const text = msg.text();
                if (type === 'error' || type === 'warning' || text.includes('NAVEGACAO')) {
                    if (type === 'error') logger.error(`[Browser Console] ${type}: ${text}`);
                    else if (type === 'warning' || type === 'warn') logger.warn(`[Browser Console] ${type}: ${text}`);
                    else logger.info(`[Browser Console] ${type}: ${text}`);
                }
            });

            const descricao = `Proc Mapa ${Date.now()}`;
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
            const processoId = await extrairProcessoId(page);
            if (processoId > 0) cleanup.registrar(processoId);

            await page.getByTestId('btn-processo-iniciar').click();
            await page.getByTestId('btn-iniciar-processo-confirmar').click();

            await page.getByTestId('btn-logout').click();
            await login(page, USUARIOS.CHEFE_SECAO_121.titulo, USUARIOS.CHEFE_SECAO_121.senha);

            await acessarSubprocessoChefeDireto(page, descricao, 'SECAO_121');
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
            await page.waitForTimeout(500);

            // 1. GESTOR COORD_12 - Primeiro Aceite
            await page.getByTestId('btn-logout').click({force: true});
            await login(page, USUARIOS.GESTOR_COORD_12.titulo, USUARIOS.GESTOR_COORD_12.senha);

            await acessarSubprocessoGestor(page, descricao, UNIDADE_ALVO);
            await navegarParaAtividadesVisualizacao(page);
            await page.getByTestId('btn-acao-analisar-principal').click();
            await page.getByTestId('inp-aceite-cadastro-obs').fill('Cadastro muito bem detalhado. Seguindo para a Secretaria.');
            await page.getByTestId('btn-aceite-cadastro-confirmar').click();
            await expect(page.getByText(/Cadastro aceito/i).first()).toBeVisible();

            // 2. GESTOR SECRETARIA_1 - Segundo Aceite
            await page.getByTestId('btn-logout').click({force: true});
            await loginComPerfil(page, USUARIOS.GESTOR_SECRETARIA_1.titulo, USUARIOS.GESTOR_SECRETARIA_1.senha, USUARIOS.GESTOR_SECRETARIA_1.perfil);
            await acessarSubprocessoGestor(page, descricao, UNIDADE_ALVO);
            await navegarParaAtividadesVisualizacao(page);
            await page.getByTestId('btn-acao-analisar-principal').click();
            await page.getByTestId('inp-aceite-cadastro-obs').fill('Ok. Para homologação do ADMIN.');
            await page.getByTestId('btn-aceite-cadastro-confirmar').click();
            await expect(page.getByText(/Cadastro aceito/i).first()).toBeVisible();

            // 3. ADMIN - Homologação Final
            await page.getByTestId('btn-logout').click({force: true});
            await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);

            // Navegar para o subprocesso (admin vê tabela de unidades)
            await acessarSubprocessoAdmin(page, descricao, UNIDADE_ALVO);

            // Entrar no cadastro de atividades (visualização)
            await navegarParaAtividadesVisualizacao(page);

            // Homologar cadastro
            await page.getByTestId('btn-acao-analisar-principal').click();
            await page.getByTestId('btn-aceite-cadastro-confirmar').click();
            await page.waitForTimeout(100);

            // Agora o Mapa deve estar habilitado para edição pelo Admin
            // Navegar para mapa
            await navegarParaMapa(page);

            // Criar competência
            await abrirModalCriarCompetencia(page);
            await page.waitForTimeout(100);

            const competenciaDesc = 'Desenvolvimento de Software';
            await page.getByTestId('inp-criar-competencia-descricao').fill(competenciaDesc);

            // Selecionar atividades
            const modal = page.getByTestId('mdl-criar-competencia');
            await modal.locator('label').filter({hasText: 'Desenvolvimento Web'}).click();
            await modal.locator('label').filter({hasText: 'Desenvolvimento Backend'}).click();

            await page.getByTestId('btn-criar-competencia-salvar').click();
            await expect(modal).toBeHidden();
            await page.waitForTimeout(100);

            // Hover na competência
            const card = page.locator('.competencia-card', {has: page.getByText(competenciaDesc)});
            await card.hover();
            await page.waitForTimeout(100);

            // Disponibilizar mapa
            await page.getByTestId('btn-cad-mapa-disponibilizar').click();
            await page.waitForTimeout(100);

            const dataLimite = new Date();
            dataLimite.setDate(dataLimite.getDate() + 30);
            await page.getByTestId('inp-disponibilizar-mapa-data').fill(dataLimite.toISOString().split('T')[0]);
            await page.getByTestId('btn-disponibilizar-mapa-confirmar').click();
            await page.waitForTimeout(100);
            const cardCompetencia = page.locator('.competencia-card').first();
            if (await cardCompetencia.isVisible().catch(() => false)) {
            }
        });
    });

    test.describe('06 - Navegação e Menus', () => {
        test('Captura elementos de navegação', async ({page}) => {
            await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);

            // Menu principal

            // Configurações (se admin)
            await page.getByTestId('btn-configuracoes').click();
            await page.waitForTimeout(300);
            await page.goto('/painel');

            // Seção Unidades (para ADMIN)
            await page.getByText('Unidades').first().click();
            await page.waitForTimeout(100);

            // Seção Relatórios
            await page.getByText('Relatórios').click();
            await page.waitForTimeout(100);

            // Seção Histórico
            await page.getByText('Histórico').click();
            await page.waitForTimeout(100);

            // Rodapé
            await page.evaluate(() => window.scrollTo(0, document.body.scrollHeight));
            await page.waitForTimeout(100);
        });
    });

    test.describe('07 - Estados e Situações', () => {
        test('Captura diferentes estados de processo', async ({page}) => {
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
            await expect(page).toHaveURL(/codProcesso=|processo\/\d+/);
            const processoId1 = await extrairProcessoId(page);
            if (processoId1 > 0) cleanup.registrar(processoId1);
            await page.goto('/painel');

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

        });
    });

    test.describe('08 - Responsividade (Tamanhos de Tela)', () => {
        test('Captura em diferentes resoluções', async ({page}) => {
            await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);

            // Desktop padrão (1920x1080)
            await page.setViewportSize({width: 1920, height: 1080});

            // Desktop médio (1366x768)
            await page.setViewportSize({width: 1366, height: 768});

            // Tablet (768x1024)
            await page.setViewportSize({width: 768, height: 1024});

            // Mobile (375x667)
            await page.setViewportSize({width: 375, height: 667});
        });
    });

    // ========================================================================
    // SEÇÃO 09 - OPERAÇÕES EM BLOCO (CDUs 22-26)
    // ========================================================================
    test.describe('09 - Operações em Bloco', () => {
        test('Captura fluxo de aceitar cadastros em bloco', async ({page}) => {
            // Prepara cenário: criar processo com unidades subordinadas e disponibilizar cadastros
            await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);

            const descricao = `Processo Bloco ${Date.now()}`;
            // Criar com múltiplas unidades para que GESTOR veja tela de "Unidades participantes"
            // COORD_11 tem SECAO_111, SECAO_112, SECAO_113 e é gerido por GESTOR_COORD
            await criarProcesso(page, {
                descricao,
                tipo: 'MAPEAMENTO',
                diasLimite: 30,
                unidade: ['SECAO_111', 'SECAO_112'],  // Múltiplas unidades subordinadas ao GESTOR_COORD
                expandir: ['SECRETARIA_1', 'COORD_11'],
                iniciar: true
            });

            // Login como Chefe da SECAO_111 para disponibilizar cadastro
            await page.getByTestId('btn-logout').click();
            await login(page, USUARIOS.CHEFE_SECAO_111.titulo, USUARIOS.CHEFE_SECAO_111.senha);

            await page.getByTestId('tbl-processos').getByText(descricao).first().click();
            await navegarParaSubprocesso(page, 'SECAO_111');
            await expect(page).toHaveURL(/processo\/\d+/);

            // Capturar ID para cleanup (após navegar para o subprocesso)
            const processoId = await extrairProcessoId(page);
            if (processoId > 0) cleanup.registrar(processoId);
            await navegarParaAtividades(page);
            await adicionarAtividade(page, 'Atividade Bloco 1');
            await adicionarConhecimento(page, 'Atividade Bloco 1', 'Conhecimento 1');
            await page.getByTestId('btn-cad-atividades-disponibilizar').click();
            await expect(page.getByTestId('btn-confirmar-disponibilizacao')).toBeVisible();
            await page.getByTestId('btn-confirmar-disponibilizacao').click();
            await page.waitForTimeout(500);

            // Login como Gestor da COORD_11 para ver botão de aceitar em bloco
            await page.getByTestId('btn-logout').click();
            await login(page, USUARIOS.GESTOR_COORD.titulo, USUARIOS.GESTOR_COORD.senha);

            await page.getByTestId('tbl-processos').getByText(descricao).first().click();
            await expect(page.getByRole('heading', {name: /Unidades participantes/i})).toBeVisible();

            // Capturar botão de aceitar em bloco (se visível)
            const btnAceitarBloco = page.getByRole('button', {name: /Aceitar.*Bloco/i});
            if (await btnAceitarBloco.isVisible().catch(() => false)) {
                await btnAceitarBloco.click();
                await page.waitForTimeout(300);
                await page.getByRole('button', {name: /Cancelar/i}).click();
            }

            // Login como Admin para homologar em bloco
            await page.getByTestId('btn-logout').click();
            await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);

            await page.getByTestId('tbl-processos').getByText(descricao).first().click();
            await expect(page.getByRole('heading', {name: /Unidades participantes/i})).toBeVisible();

            // Capturar botão de homologar cadastro em bloco (CDU-23)
            const btnHomologarBloco = page.getByRole('button', {name: /Homologar.*Bloco/i});
            if (await btnHomologarBloco.isVisible().catch(() => false)) {
                await btnHomologarBloco.click();
                await page.waitForTimeout(300);
                await page.getByRole('button', {name: /Cancelar/i}).click();
            }

            // Capturar botão de disponibilizar mapas em bloco (CDU-24)
            const btnDisponibilizarMapaBloco = page.getByRole('button', {name: /Disponibilizar.*mapa.*Bloco/i});
            if (await btnDisponibilizarMapaBloco.isVisible().catch(() => false)) {
                await btnDisponibilizarMapaBloco.click();
                await page.waitForTimeout(300);
                await page.getByRole('button', {name: /Cancelar/i}).click();
            }

            // Capturar botões de aceitar/homologar mapa em bloco (CDU-25 e CDU-26 - se visíveis)
            const btnAceitarMapaBloco = page.getByRole('button', {name: /Aceitar.*mapa.*Bloco/i});
            if (await btnAceitarMapaBloco.isVisible().catch(() => false)) {
                await btnAceitarMapaBloco.click();
                await page.waitForTimeout(300);
                await page.getByRole('button', {name: /Cancelar/i}).click();
            }

            const btnHomologarMapaBloco = page.getByRole('button', {name: /Homologar.*mapa.*Bloco/i});
            if (await btnHomologarMapaBloco.isVisible().catch(() => false)) {
                await btnHomologarMapaBloco.click();
                await page.waitForTimeout(300);
                await page.getByRole('button', {name: /Cancelar/i}).click();
            }
        });
    });

    // ========================================================================
    // SEÇÃO 10 - GESTÃO DE SUBPROCESSOS (CDUs 27, 32-34)
    // ========================================================================
    test.describe('10 - Gestão de Subprocessos', () => {
        test('Captura modais de gestão de subprocesso', async ({page}) => {
            await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);

            const descricao = `Processo Gestão ${Date.now()}`;
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
            const processoId = await extrairProcessoId(page);
            if (processoId > 0) cleanup.registrar(processoId);

            // Acessar subprocesso
            await page.getByRole('row', {name: /SECAO_121/i}).click();
            await expect(page).toHaveURL(/\/processo\/\d+\/SECAO_121/);

            // Modal de alterar data limite (CDU-27)
            const btnAlterarData = page.getByRole('button', {name: /Alterar.*data.*limite/i});
            if (await btnAlterarData.isVisible().catch(() => false)) {
                await btnAlterarData.click();
                await page.waitForTimeout(300);
                await page.getByRole('button', {name: /Cancelar/i}).click();
            }

            // Modal de reabrir cadastro (CDU-32)
            const btnReabrirCadastro = page.getByRole('button', {name: /Reabrir.*cadastro/i});
            if (await btnReabrirCadastro.isVisible().catch(() => false)) {
                await btnReabrirCadastro.click();
                await page.waitForTimeout(300);
                await page.getByRole('button', {name: /Cancelar/i}).click();
            }

            // CDU-34: Botão de enviar lembrete (ação direta, sem modal)
            const btnEnviarLembrete = page.getByRole('button', {name: /Enviar.*lembrete/i});
            if (await btnEnviarLembrete.isVisible().catch(() => false)) {
                // Apenas capturar o botão visível, não clicar pois executa ação direta
            }
        });
    });

    // ========================================================================
    // SEÇÃO 11 - GESTÃO DE UNIDADES E ATRIBUIÇÕES (CDU-28)
    // ========================================================================
    test.describe('11 - Gestão de Unidades', () => {
        test('Captura página de unidades e atribuição temporária', async ({page}) => {
            await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);

            // Navegar para página de unidades
            const linkUnidades = page.getByRole('link', {name: /Unidades/i});
            if (await linkUnidades.isVisible().catch(() => false)) {
                await linkUnidades.click();
                await page.waitForTimeout(500);

                // Expandir árvore para ver unidades
                const btnExpand = page.getByTestId('btn-arvore-expand-SECRETARIA_1');
                if (await btnExpand.isVisible().catch(() => false)) {
                    await btnExpand.click();
                    await page.waitForTimeout(300);
                }

                // Clicar em uma unidade para ver detalhes
                const unidade = page.getByText('SECAO_121').first();
                if (await unidade.isVisible().catch(() => false)) {
                    await unidade.click();
                    await page.waitForTimeout(500);

                    // Modal de criar atribuição temporária (CDU-28)
                    const btnCriarAtribuicao = page.getByRole('button', {name: /Criar atribuição|Nova atribuição/i});
                    if (await btnCriarAtribuicao.isVisible().catch(() => false)) {
                        await btnCriarAtribuicao.click();
                        await page.waitForTimeout(300);
                        await page.getByRole('button', {name: /Cancelar/i}).click();
                    }
                }
            }
        });
    });

    // ========================================================================
    // SEÇÃO 12 - HISTÓRICO DE PROCESSOS (CDU-29)
    // ========================================================================
    test.describe('12 - Histórico', () => {
        test('Captura seção de histórico', async ({page}) => {
            await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);

            // Acessar seção de histórico
            const linkHistorico = page.getByRole('link', {name: /Histórico/i});
            if (await linkHistorico.isVisible().catch(() => false)) {
                await linkHistorico.click();
                await page.waitForTimeout(500);

                // Tabela de processos finalizados
                const tabela = page.locator('table');
                if (await tabela.isVisible().catch(() => false)) {
                }
            }
        });
    });

    // ========================================================================
    // SEÇÃO 13 - CONFIGURAÇÕES E ADMINISTRADORES (CDUs 30-31)
    // ========================================================================
    test.describe('13 - Configurações', () => {
        test('Captura página de configurações e administradores', async ({page}) => {
            await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);

            // Acessar configurações
            await page.getByTestId('btn-configuracoes').click();
            await page.waitForTimeout(500);

            // Seção de configurações do sistema (CDU-31)
            const inputDiasInativacao = page.getByTestId('inp-config-dias-inativacao');
            if (await inputDiasInativacao.isVisible().catch(() => false)) {
            }

            // Seção de administradores (CDU-30)
            const secaoAdmins = page.getByText(/Administradores/i);
            if (await secaoAdmins.isVisible().catch(() => false)) {
                await secaoAdmins.click();
                await page.waitForTimeout(300);

                // Botão de adicionar administrador
                const btnAdicionar = page.getByRole('button', {name: /Adicionar|Novo/i});
                if (await btnAdicionar.isVisible().catch(() => false)) {
                    await btnAdicionar.click();
                    await page.waitForTimeout(300);
                    await page.getByRole('button', {name: /Cancelar/i}).click();
                }
            }
        });
    });

    // ========================================================================
    // SEÇÃO 14 - RELATÓRIOS (CDUs 35-36)
    // ========================================================================
    test.describe('14 - Relatórios', () => {
        test('Captura página e modais de relatórios', async ({page}) => {
            await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);

            // Acessar relatórios
            const linkRelatorios = page.getByRole('link', {name: /Relatórios/i});
            if (await linkRelatorios.isVisible().catch(() => false)) {
                await linkRelatorios.click();
                await page.waitForTimeout(500);

                // Card de relatório de andamento (CDU-35)
                const cardAndamento = page.getByTestId('card-relatorio-andamento');
                if (await cardAndamento.isVisible().catch(() => false)) {
                    await cardAndamento.click();
                    await page.waitForTimeout(300);
                    const modalRelatorio = page.locator('.modal-content').first();
                    if (await modalRelatorio.isVisible().catch(() => false)) {
                    }

                    // Verificar filtros
                    const filtroTipo = page.getByTestId('sel-filtro-tipo');
                    if (await filtroTipo.isVisible().catch(() => false)) {
                    }

                    // Verificar botão de exportação
                    const btnExportar = page.getByRole('button', {name: /Exportar|PDF|CSV/i});
                    if (await btnExportar.isVisible().catch(() => false)) {
                    }

                    await page.getByRole('button', {name: /Fechar|Cancelar|Close|Cancel/i}).first().click().catch(() => {
                    });
                    await page.waitForTimeout(300);
                }

                // Card de relatório de mapas (CDU-36)
                const cardMapas = page.getByTestId('card-relatorio-mapas');
                if (await cardMapas.isVisible().catch(() => false)) {
                    await cardMapas.click();
                    await page.waitForTimeout(300);
                    await page.getByRole('button', {name: /Fechar|Cancelar|Close|Cancel/i}).first().click().catch(() => {
                    });
                }
            }
        });
    });
});
