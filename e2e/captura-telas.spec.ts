import type {Locator, Page} from '@playwright/test';
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
import {acessarSubprocessoAdmin, acessarSubprocessoChefeDireto} from './helpers/helpers-analise.js';
import {abrirModalCriarCompetencia, navegarParaMapa} from './helpers/helpers-mapas.js';
import {resetDatabase, useProcessoCleanup} from './hooks/hooks-limpeza.js';
import * as path from 'node:path';
import * as fs from 'node:fs';

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
if (fs.existsSync(SCREENSHOTS_DIR)) {
    fs.rmSync(SCREENSHOTS_DIR, {recursive: true, force: true});
}
if (!fs.existsSync(SCREENSHOTS_DIR)) {
    fs.mkdirSync(SCREENSHOTS_DIR, {recursive: true});
}

/**
 * Helper para capturar screenshot com nome organizado
 */
async function capturarTela(page: Page, categoria: string, nome: string, opcoes?: { fullPage?: boolean }) {
    const nomeArquivo = `${categoria}--${nome}.png`;
    const caminhoCompleto = path.join(SCREENSHOTS_DIR, nomeArquivo);
    await page.screenshot({
        path: caminhoCompleto,
        fullPage: opcoes?.fullPage ?? true // Alterado para true por padrão
    });
}

async function capturarComponente(elemento: Locator, categoria: string, nome: string) {
    const nomeArquivo = `${categoria}--${nome}.png`;
    const caminhoCompleto = path.join(SCREENSHOTS_DIR, nomeArquivo);
    await elemento.screenshot({path: caminhoCompleto});
}

test.describe('Captura de Telas - Sistema SGC', () => {
    test.setTimeout(20000); // Aumentar timeout para cenários longos
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
            await capturarTela(page, '01-seguranca', '01-login-inicial');
            await page.getByTestId('inp-login-usuario').click();
            await capturarTela(page, '01-seguranca', '01b-login-campo-usuario-foco');

            // Erro de credenciais inválidas
            await page.getByTestId('inp-login-usuario').fill(USUARIOS.INVALIDO.titulo);
            await capturarTela(page, '01-seguranca', '01c-login-usuario-preenchido');
            await page.getByTestId('inp-login-senha').fill(USUARIOS.INVALIDO.senha);
            await page.getByTestId('btn-login-entrar').click();
            await page.waitForTimeout(500);
            await capturarTela(page, '01-seguranca', '02-login-erro-credenciais');

            // Limpar e fazer login com múltiplos perfis
            await page.getByTestId('inp-login-usuario').fill(USUARIOS.ADMIN_2_PERFIS.titulo);
            await page.getByTestId('inp-login-senha').fill(USUARIOS.ADMIN_2_PERFIS.senha);
            await page.getByTestId('btn-login-entrar').click();
            await page.waitForTimeout(500);
            await capturarTela(page, '01-seguranca', '03-login-selecao-perfil');

            // Login com perfil selecionado
            // Reiniciar a página para garantir estado limpo para a função helper
            await loginComPerfil(page, USUARIOS.ADMIN_2_PERFIS.titulo, USUARIOS.ADMIN_2_PERFIS.senha, USUARIOS.ADMIN_2_PERFIS.perfil);
            await capturarTela(page, '01-seguranca', '04-painel-apos-login', {fullPage: true});
        });
    });

    test.describe('02 - Painel Principal', () => {
        test('Captura painel ADMIN', async ({page}) => {
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

            // Expandir COORD_11 para acessar SECAO_111
            await page.waitForTimeout(300);
            await page.getByTestId('btn-arvore-expand-COORD_11').click();
            await page.waitForTimeout(300);

            // Selecionar múltiplas unidades
            await page.getByTestId('chk-arvore-unidade-ASSESSORIA_11').click();
            await page.getByTestId('chk-arvore-unidade-ASSESSORIA_12').click();
            await page.getByTestId('chk-arvore-unidade-SECAO_111').click();
            await capturarTela(page, '02-painel', '05-arvore-unidades-selecionada');

            // Salvar processo
            await page.getByTestId('btn-processo-salvar').click();
            await expect(page).toHaveURL(/\/painel/);
            await expect(page.getByTestId('tbl-processos')).toBeVisible();
            await expect(page.getByTestId('tbl-processos').getByText(descricaoProcesso).first()).toBeVisible();
            await capturarComponente(page.getByTestId('tbl-processos'), '02-painel', '06a-tabela-processos');

            // Capturar ID para cleanup
            await page.getByTestId('tbl-processos').getByText(descricaoProcesso).first().click();
            await expect(page).toHaveURL(/codProcesso=\d+/);
            const processoId = await extrairProcessoId(page);
            if (processoId > 0) cleanup.registrar(processoId);
            await page.goto('/painel');

            // Painel com processo criado
            await capturarTela(page, '02-painel', '06-painel-admin-com-processo', {fullPage: true});

            // Hover em linha da tabela
            await page.getByText(descricaoProcesso).hover();
            await capturarTela(page, '02-painel', '07-painel-hover-processo');
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
            await capturarTela(page, '02-painel', '10-painel-gestor', {fullPage: true});
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
            await capturarTela(page, '02-painel', '11-painel-chefe', {fullPage: true});
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
            await capturarTela(page, '03-processo', '01-processo-edicao');

            // Modal de iniciar processo
            await page.getByTestId('btn-processo-iniciar').click();
            await page.waitForTimeout(300);
            await capturarTela(page, '03-processo', '02-modal-iniciar-processo');
            await page.getByTestId('btn-iniciar-processo-confirmar').click();
            await expect(page).toHaveURL(/\/painel/);

            // Acessar detalhes do processo iniciado
            await page.getByTestId('tbl-processos').getByText(descricao).first().click();
            await expect(page).toHaveURL(/\/processo\/\d+/);
            await capturarTela(page, '03-processo', '03-detalhes-processo-iniciado', {fullPage: true});

            // Modal de finalizar processo
            await page.getByTestId('btn-processo-finalizar').click();
            await page.waitForTimeout(300);
            await capturarTela(page, '03-processo', '04-modal-finalizar-processo');
            await page.getByRole('button', {name: 'Cancelar'}).click();
            await capturarTela(page, '03-processo', '05-detalhes-processo-apos-cancelar-finalizacao');
        });

        test('Captura validações de formulário', async ({page}) => {
            await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);

            await page.getByTestId('btn-painel-criar-processo').click();
            await expect(page).toHaveURL(/\/processo\/cadastro/);

            // Capturar estado inicial com botões desativados (formulário vazio)
            await page.waitForTimeout(300);
            await capturarTela(page, '03-processo', '10-botoes-desativados-form-vazio');

            // Preencher apenas descrição (botões ainda desativados)
            await page.getByTestId('inp-processo-descricao').fill('Teste Validação');
            await page.waitForTimeout(300);
            await capturarTela(page, '03-processo', '11-botoes-desativados-falta-data-unidade');

            // Selecionar tipo de processo (necessário para carregar árvore de unidades)
            await page.getByTestId('sel-processo-tipo').selectOption('MAPEAMENTO');

            // Preencher data limite (botões ainda desativados - falta unidade)
            const dataLimite = new Date();
            dataLimite.setDate(dataLimite.getDate() + 30);
            await page.getByTestId('inp-processo-data-limite').fill(dataLimite.toISOString().split('T')[0]);
            await page.waitForTimeout(300);
            await capturarTela(page, '03-processo', '12-botoes-desativados-falta-unidade');

            // Expandir e selecionar unidade (agora botões devem estar ativados)
            await expect(page.getByText('Carregando unidades...')).toBeHidden();
            await page.getByTestId('btn-arvore-expand-SECRETARIA_1').click();
            await page.waitForTimeout(300);
            await page.getByTestId('chk-arvore-unidade-ASSESSORIA_11').click();
            await page.waitForTimeout(300);
            await capturarTela(page, '03-processo', '13-botoes-ativados-form-completo');
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
            await adicionarConhecimento(page, atividadeDesc, 'Oracle');
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
            const cardDisponibilizado = page.locator('.atividade-card').first();
            if (await cardDisponibilizado.isVisible().catch(() => false)) {
                await capturarComponente(cardDisponibilizado, '04-subprocesso', '10-card-atividade-disponibilizada');
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
            await capturarTela(page, '04-subprocesso', '20-cadastro-vazio-com-label-obrigatorio', {fullPage: true});

            // Adicionar primeira atividade SEM conhecimento
            const atividade1 = `Atividade Sem Conhecimento 1 ${Date.now()}`;
            await adicionarAtividade(page, atividade1);
            await page.waitForTimeout(300);
            await capturarTela(page, '04-subprocesso', '21-atividade-sem-conhecimento', {fullPage: true});

            // Adicionar segunda atividade SEM conhecimento
            const atividade2 = `Atividade Sem Conhecimento 2 ${Date.now()}`;
            await adicionarAtividade(page, atividade2);
            await page.waitForTimeout(300);

            // Adicionar terceira atividade COM conhecimento (para contraste)
            const atividade3 = `Atividade Com Conhecimento ${Date.now()}`;
            await adicionarAtividade(page, atividade3);
            await adicionarConhecimento(page, atividade3, 'Java');
            await page.waitForTimeout(300);
            await capturarTela(page, '04-subprocesso', '22-mix-atividades-com-sem-conhecimento', {fullPage: true});

            // Tentar disponibilizar - deve mostrar validação inline
            await page.getByTestId('btn-cad-atividades-disponibilizar').click();
            await page.waitForTimeout(500); // Aguardar scroll automático

            // Capturar primeira atividade com erro inline (scroll automático já levou até ela)
            await capturarTela(page, '04-subprocesso', '23-validacao-inline-primeira-atividade', {fullPage: true});

            // Scroll para segunda atividade com erro
            const card2 = page.locator('.atividade-card', {has: page.getByText(atividade2)});
            await card2.scrollIntoViewIfNeeded();
            await page.waitForTimeout(300);
            await capturarTela(page, '04-subprocesso', '24-validacao-inline-segunda-atividade');

            // Capturar zoom na mensagem de erro inline
            const primeiroCard = page.locator('.atividade-card', {has: page.getByText(atividade1)});
            await primeiroCard.scrollIntoViewIfNeeded();
            await page.waitForTimeout(300);

            // Capturar apenas o card com erro para detalhe
            await primeiroCard.screenshot({
                path: path.join(SCREENSHOTS_DIR, '04-subprocesso--25-detalhe-card-com-erro.png')
            });

            // Corrigir primeira atividade adicionando conhecimento
            await adicionarConhecimento(page, atividade1, 'Python');
            await page.waitForTimeout(500);
            await capturarTela(page, '04-subprocesso', '26-erro-desaparece-apos-correcao', {fullPage: true});

            // Tentar disponibilizar novamente - ainda deve ter erro na atividade 2
            await page.getByTestId('btn-cad-atividades-disponibilizar').click();
            await page.waitForTimeout(500);
            await capturarTela(page, '04-subprocesso', '27-validacao-apenas-atividade-restante');

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
            await capturarTela(page, '04-subprocesso', '28-modal-confirmacao-disponibilizacao');
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

            // Setup: criar processo e disponibilizar atividades
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
            await capturarTela(page, '05-mapa', '00a-analise-gestor-coordenadoria', {fullPage: true});
            await page.getByTestId('btn-acao-analisar-principal').click();
            await page.getByTestId('inp-aceite-cadastro-obs').fill('Cadastro muito bem detalhado. Seguindo para a Secretaria.');
            await page.getByTestId('btn-aceite-cadastro-confirmar').click();
            await expect(page.getByText(/Cadastro aceito/i).first()).toBeVisible();

            // 2. CHEFE SECRETARIA_1 - Segundo Aceite
            await page.getByTestId('btn-logout').click({force: true});
            await login(page, USUARIOS.CHEFE_SECRETARIA_1.titulo, USUARIOS.CHEFE_SECRETARIA_1.senha);
            await acessarSubprocessoGestor(page, descricao, UNIDADE_ALVO);
            await navegarParaAtividadesVisualizacao(page);
            await capturarTela(page, '05-mapa', '00b-analise-chefe-secretaria', {fullPage: true});
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
            await capturarTela(page, '05-mapa', '01-mapa-vazio', {fullPage: true});

            // Criar competência
            await abrirModalCriarCompetencia(page);
            await page.waitForTimeout(100);
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
            await page.waitForTimeout(100);
            await capturarTela(page, '05-mapa', '05-mapa-com-competencia', {fullPage: true});

            // Hover na competência
            const card = page.locator('.competencia-card', {has: page.getByText(competenciaDesc)});
            await card.hover();
            await page.waitForTimeout(100);
            await capturarTela(page, '05-mapa', '06-mapa-competencia-hover');

            // Disponibilizar mapa
            await page.getByTestId('btn-cad-mapa-disponibilizar').click();
            await page.waitForTimeout(100);
            await capturarTela(page, '05-mapa', '07-modal-disponibilizar-mapa');

            const dataLimite = new Date();
            dataLimite.setDate(dataLimite.getDate() + 30);
            await page.getByTestId('inp-disponibilizar-mapa-data').fill(dataLimite.toISOString().split('T')[0]);
            await page.getByTestId('btn-disponibilizar-mapa-confirmar').click();
            await page.waitForTimeout(100);
            await capturarTela(page, '05-mapa', '08-mapa-disponibilizado', {fullPage: true});
            const cardCompetencia = page.locator('.competencia-card').first();
            if (await cardCompetencia.isVisible().catch(() => false)) {
                await capturarComponente(cardCompetencia, '05-mapa', '09-card-competencia-detalhe');
            }
        });
    });

    test.describe('06 - Navegação e Menus', () => {
        test('Captura elementos de navegação', async ({page}) => {
            await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);

            // Menu principal
            await capturarTela(page, '06-navegacao', '01-menu-principal');

            // Configurações (se admin)
            await page.getByTestId('btn-configuracoes').click();
            await page.waitForTimeout(300);
            await capturarTela(page, '06-navegacao', '02-menu-configuracoes');
            await page.goto('/painel');

            // Seção Unidades (para ADMIN)
            await page.getByText('Unidades').click();
            await page.waitForTimeout(100);
            await capturarTela(page, '06-navegacao', '03-unidades', {fullPage: true});

            // Seção Relatórios
            await page.getByText('Relatórios').click();
            await page.waitForTimeout(100);
            await capturarTela(page, '06-navegacao', '04-relatorios', {fullPage: true});

            // Seção Histórico
            await page.getByText('Histórico').click();
            await page.waitForTimeout(100);
            await capturarTela(page, '06-navegacao', '05-historico', {fullPage: true});
            await capturarComponente(page.getByRole('navigation').first(), '06-navegacao', '05a-barra-lateral');

            // Rodapé
            await page.evaluate(() => window.scrollTo(0, document.body.scrollHeight));
            await page.waitForTimeout(100);
            await capturarTela(page, '06-navegacao', '06-rodape');
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



            await capturarTela(page, '07-estados', '02-processo-em-andamento');
            await capturarComponente(page.getByTestId('tbl-processos'), '07-estados', '03-tabela-com-multiplos-estados');
        });
    });

    test.describe('08 - Responsividade (Tamanhos de Tela)', () => {
        test('Captura em diferentes resoluções', async ({page}) => {
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
            await capturarTela(page, '09-operacoes-bloco', '01-detalhes-processo-gestor', {fullPage: true});

            // Capturar botão de aceitar em bloco (se visível)
            const btnAceitarBloco = page.getByRole('button', {name: /Aceitar.*Bloco/i});
            if (await btnAceitarBloco.isVisible().catch(() => false)) {
                await btnAceitarBloco.click();
                await page.waitForTimeout(300);
                await capturarTela(page, '09-operacoes-bloco', '02-modal-aceitar-cadastro-bloco');
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
                await capturarTela(page, '09-operacoes-bloco', '03-modal-homologar-cadastro-bloco');
                await page.getByRole('button', {name: /Cancelar/i}).click();
            }

            // Capturar botão de disponibilizar mapas em bloco (CDU-24)
            const btnDisponibilizarMapaBloco = page.getByRole('button', {name: /Disponibilizar.*mapa.*Bloco/i});
            if (await btnDisponibilizarMapaBloco.isVisible().catch(() => false)) {
                await btnDisponibilizarMapaBloco.click();
                await page.waitForTimeout(300);
                await capturarTela(page, '09-operacoes-bloco', '04-modal-disponibilizar-mapa-bloco');
                await page.getByRole('button', {name: /Cancelar/i}).click();
            }

            // Capturar botões de aceitar/homologar mapa em bloco (CDU-25 e CDU-26 - se visíveis)
            const btnAceitarMapaBloco = page.getByRole('button', {name: /Aceitar.*mapa.*Bloco/i});
            if (await btnAceitarMapaBloco.isVisible().catch(() => false)) {
                await btnAceitarMapaBloco.click();
                await page.waitForTimeout(300);
                await capturarTela(page, '09-operacoes-bloco', '05-modal-aceitar-mapa-bloco');
                await page.getByRole('button', {name: /Cancelar/i}).click();
            }

            const btnHomologarMapaBloco = page.getByRole('button', {name: /Homologar.*mapa.*Bloco/i});
            if (await btnHomologarMapaBloco.isVisible().catch(() => false)) {
                await btnHomologarMapaBloco.click();
                await page.waitForTimeout(300);
                await capturarTela(page, '09-operacoes-bloco', '06-modal-homologar-mapa-bloco');
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
            await capturarTela(page, '10-gestao-subprocessos', '01-detalhes-subprocesso-admin', {fullPage: true});

            // Modal de alterar data limite (CDU-27)
            const btnAlterarData = page.getByRole('button', {name: /Alterar.*data.*limite/i});
            if (await btnAlterarData.isVisible().catch(() => false)) {
                await btnAlterarData.click();
                await page.waitForTimeout(300);
                await capturarTela(page, '10-gestao-subprocessos', '02-modal-alterar-data-limite');
                await page.getByRole('button', {name: /Cancelar/i}).click();
            }

            // Modal de reabrir cadastro (CDU-32)
            const btnReabrirCadastro = page.getByRole('button', {name: /Reabrir.*cadastro/i});
            if (await btnReabrirCadastro.isVisible().catch(() => false)) {
                await btnReabrirCadastro.click();
                await page.waitForTimeout(300);
                await capturarTela(page, '10-gestao-subprocessos', '03-modal-reabrir-cadastro');
                await page.getByRole('button', {name: /Cancelar/i}).click();
            }

            // CDU-34: Botão de enviar lembrete (ação direta, sem modal)
            const btnEnviarLembrete = page.getByRole('button', {name: /Enviar.*lembrete/i});
            if (await btnEnviarLembrete.isVisible().catch(() => false)) {
                // Apenas capturar o botão visível, não clicar pois executa ação direta
                await capturarTela(page, '10-gestao-subprocessos', '04-botao-enviar-lembrete');
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
                await capturarTela(page, '11-unidades', '01-arvore-unidades', {fullPage: true});

                // Expandir árvore para ver unidades
                const btnExpand = page.getByTestId('btn-arvore-expand-SECRETARIA_1');
                if (await btnExpand.isVisible().catch(() => false)) {
                    await btnExpand.click();
                    await page.waitForTimeout(300);
                    await capturarTela(page, '11-unidades', '02-arvore-unidades-expandida', {fullPage: true});
                }

                // Clicar em uma unidade para ver detalhes
                const unidade = page.getByText('SECAO_121').first();
                if (await unidade.isVisible().catch(() => false)) {
                    await unidade.click();
                    await page.waitForTimeout(500);
                    await capturarTela(page, '11-unidades', '03-detalhes-unidade', {fullPage: true});

                    // Modal de criar atribuição temporária (CDU-28)
                    const btnCriarAtribuicao = page.getByRole('button', {name: /Criar atribuição|Nova atribuição/i});
                    if (await btnCriarAtribuicao.isVisible().catch(() => false)) {
                        await btnCriarAtribuicao.click();
                        await page.waitForTimeout(300);
                        await capturarTela(page, '11-unidades', '04-modal-criar-atribuicao');
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
                await capturarTela(page, '12-historico', '01-pagina-historico', {fullPage: true});

                // Tabela de processos finalizados
                const tabela = page.locator('table');
                if (await tabela.isVisible().catch(() => false)) {
                    await capturarTela(page, '12-historico', '02-tabela-processos-finalizados', {fullPage: true});
                    await capturarComponente(tabela.first(), '12-historico', '03-tabela-processos-finalizados-detalhe');
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
            await capturarTela(page, '13-configuracoes', '01-pagina-configuracoes', {fullPage: true});

            // Seção de configurações do sistema (CDU-31)
            const inputDiasInativacao = page.getByTestId('inp-config-dias-inativacao');
            if (await inputDiasInativacao.isVisible().catch(() => false)) {
                await capturarTela(page, '13-configuracoes', '02-config-sistema');
            }

            // Seção de administradores (CDU-30)
            const secaoAdmins = page.getByText(/Administradores/i);
            if (await secaoAdmins.isVisible().catch(() => false)) {
                await secaoAdmins.click();
                await page.waitForTimeout(300);
                await capturarTela(page, '13-configuracoes', '03-lista-administradores', {fullPage: true});

                // Botão de adicionar administrador
                const btnAdicionar = page.getByRole('button', {name: /Adicionar|Novo/i});
                if (await btnAdicionar.isVisible().catch(() => false)) {
                    await btnAdicionar.click();
                    await page.waitForTimeout(300);
                    await capturarTela(page, '13-configuracoes', '04-modal-adicionar-administrador');
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
                await capturarTela(page, '14-relatorios', '01-pagina-relatorios', {fullPage: true});

                // Card de relatório de andamento (CDU-35)
                const cardAndamento = page.getByTestId('card-relatorio-andamento');
                if (await cardAndamento.isVisible().catch(() => false)) {
                    await cardAndamento.click();
                    await page.waitForTimeout(300);
                    await capturarTela(page, '14-relatorios', '02-modal-relatorio-andamento');
                    const modalRelatorio = page.locator('.modal-content').first();
                    if (await modalRelatorio.isVisible().catch(() => false)) {
                        await capturarComponente(modalRelatorio, '14-relatorios', '02a-modal-relatorio-andamento-detalhe');
                    }

                    // Verificar filtros
                    const filtroTipo = page.getByTestId('sel-filtro-tipo');
                    if (await filtroTipo.isVisible().catch(() => false)) {
                        await capturarTela(page, '14-relatorios', '03-filtros-relatorio');
                    }

                    // Verificar botão de exportação
                    const btnExportar = page.getByRole('button', {name: /Exportar|PDF|CSV/i});
                    if (await btnExportar.isVisible().catch(() => false)) {
                        await capturarTela(page, '14-relatorios', '04-botao-exportacao');
                    }

                    await page.getByRole('button', {name: /Fechar|Cancelar|Close|Cancel/i}).first().click().catch(() => {});
                    await page.waitForTimeout(300);
                }

                // Card de relatório de mapas (CDU-36)
                const cardMapas = page.getByTestId('card-relatorio-mapas');
                if (await cardMapas.isVisible().catch(() => false)) {
                    await cardMapas.click();
                    await page.waitForTimeout(300);
                    await capturarTela(page, '14-relatorios', '05-modal-relatorio-mapas');
                    await page.getByRole('button', {name: /Fechar|Cancelar|Close|Cancel/i}).first().click().catch(() => {});
                }
            }
        });
    });
});
