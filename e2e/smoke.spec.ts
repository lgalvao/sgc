import {expect, test} from './fixtures/base.js';
import {login, loginComPerfil, USUARIOS} from './helpers/helpers-auth.js';
import {criarProcesso, extrairProcessoId, iniciarProcessoPeloCadastro} from './helpers/helpers-processos.js';
import {fazerLogout, navegarParaSubprocesso, verificarPaginaPainel} from './helpers/helpers-navegacao.js';
import {
    adicionarAtividade,
    adicionarConhecimento,
    disponibilizarCadastro,
    navegarParaAtividades,
    navegarParaAtividadesVisualizacao
} from './helpers/helpers-atividades.js';
import {
    aceitarCadastroMapeamento,
    acessarSubprocessoAdmin,
    acessarSubprocessoChefeDireto,
    acessarSubprocessoGestor,
    homologarCadastroMapeamento
} from './helpers/helpers-analise.js';
import {abrirModalCriarCompetencia, disponibilizarMapa, navegarParaMapa} from './helpers/helpers-mapas.js';
import {resetDatabase, useProcessoCleanup} from './hooks/hooks-limpeza.js';

/**
 * Suite de Smoke Tests do Sistema SGC
 *
 * Objetivo: Verificar os principais fluxos do sistema para garantir que não há erros bloqueantes.
 */

test.describe('Smoke Test - Sistema SGC', () => {
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
            await expect(page.getByTestId('app-alert')).toBeVisible();

            // Limpar e fazer login com múltiplos perfis
            await page.getByTestId('inp-login-usuario').fill(USUARIOS.ADMIN_2_PERFIS.titulo);
            await page.getByTestId('inp-login-senha').fill(USUARIOS.ADMIN_2_PERFIS.senha);
            await page.getByTestId('btn-login-entrar').click();
            await expect(page.getByTestId('sel-login-perfil')).toBeVisible();

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

            await page.getByTestId('inp-processo-descricao').fill(descricaoProcesso);
            await page.getByTestId('sel-processo-tipo').selectOption('MAPEAMENTO');
            const dataLimite = new Date();
            dataLimite.setDate(dataLimite.getDate() + 30);
            await page.getByTestId('inp-processo-data-limite').fill(dataLimite.toISOString().split('T')[0]);

            // Expandir árvore de unidades
            await expect(page.getByText('Carregando unidades...')).toBeHidden();
            await page.getByTestId('btn-arvore-expand-SECRETARIA_1').click();
            await expect(page.getByTestId('btn-arvore-expand-COORD_11')).toBeVisible();

            // Expandir COORD_11 para acessar SECAO_111
            await page.getByTestId('btn-arvore-expand-COORD_11').click();
            await expect(page.getByTestId('chk-arvore-unidade-SECAO_111')).toBeVisible();

            // Selecionar múltiplas unidades
            await page.getByTestId('chk-arvore-unidade-ASSESSORIA_11').click();
            await page.getByTestId('chk-arvore-unidade-ASSESSORIA_12').click();
            await page.getByTestId('chk-arvore-unidade-SECAO_111').click();

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
            await fazerLogout(page);

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
            await fazerLogout(page);

            await login(page, USUARIOS.CHEFE_SECAO_211.titulo, USUARIOS.CHEFE_SECAO_211.senha);
            await expect(page.getByTestId('tbl-processos')).toBeVisible();
            await expect(page.getByTestId('tbl-processos').getByText(desc).first()).toBeVisible();
        });
    });

    test.describe('03 - Fluxo de Processo', () => {
        test('Captura criação e detalhamento de processo', async ({page}) => {
            const timestamp = Date.now();
            const UNIDADE_ALVO = 'ASSESSORIA_12';
            const descricao = `Processo Detalhado ${timestamp}`;

            await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);

            await criarProcesso(page, {
                descricao,
                tipo: 'MAPEAMENTO',
                diasLimite: 30,
                unidade: UNIDADE_ALVO,
                expandir: ['SECRETARIA_1']
            });

            // Capturar ID para cleanup
            await page.getByTestId('tbl-processos').getByText(descricao).first().click();
            const processoId = await extrairProcessoId(page);
            if (processoId > 0) cleanup.registrar(processoId);

            // Modal de iniciar processo
            await iniciarProcessoPeloCadastro(page, {
                descricao,
                tipo: 'MAPEAMENTO'
            });


            // 1. Chefe cadastra e disponibiliza
            await fazerLogout(page);
            await login(page, USUARIOS.CHEFE_ASSESSORIA_12.titulo, USUARIOS.CHEFE_ASSESSORIA_12.senha);
            await acessarSubprocessoChefeDireto(page, descricao, UNIDADE_ALVO);
            await navegarParaAtividades(page);
            await adicionarAtividade(page, `Atividade ${timestamp}`);
            await adicionarConhecimento(page, `Atividade ${timestamp}`, `Conhecimento ${timestamp}`);
            await disponibilizarCadastro(page);
            await fazerLogout(page);

            // 2. Gestor aceita
            await loginComPerfil(page, USUARIOS.GESTOR_SECRETARIA_1.titulo, USUARIOS.GESTOR_SECRETARIA_1.senha, USUARIOS.GESTOR_SECRETARIA_1.perfil);
            await acessarSubprocessoGestor(page, descricao, UNIDADE_ALVO);
            await navegarParaAtividadesVisualizacao(page);
            await aceitarCadastroMapeamento(page);
            await fazerLogout(page);

            // 3. Admin homologa cadastro, cria competência e disponibiliza mapa
            await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
            await acessarSubprocessoAdmin(page, descricao, UNIDADE_ALVO);
            await navegarParaAtividadesVisualizacao(page);
            await homologarCadastroMapeamento(page);
            await navegarParaMapa(page);
            await abrirModalCriarCompetencia(page);
            await page.getByTestId('inp-criar-competencia-descricao').fill(`Competência ${timestamp}`);
            await page.locator('label').filter({hasText: `Atividade ${timestamp}`}).click();
            await page.getByTestId('btn-criar-competencia-salvar').click();
            await disponibilizarMapa(page);
            await fazerLogout(page);

            // 4. Chefe valida mapa
            await login(page, USUARIOS.CHEFE_ASSESSORIA_12.titulo, USUARIOS.CHEFE_ASSESSORIA_12.senha);
            await acessarSubprocessoChefeDireto(page, descricao, UNIDADE_ALVO);
            await navegarParaMapa(page);
            await page.getByTestId('btn-mapa-validar').click();
            await page.getByTestId('btn-validar-mapa-confirmar').click();
            await fazerLogout(page);

            // 5. Gestor aceita mapa
            await loginComPerfil(page, USUARIOS.GESTOR_SECRETARIA_1.titulo, USUARIOS.GESTOR_SECRETARIA_1.senha, USUARIOS.GESTOR_SECRETARIA_1.perfil);
            await acessarSubprocessoGestor(page, descricao, UNIDADE_ALVO);
            await navegarParaMapa(page);
            await page.getByTestId('btn-mapa-homologar-aceite').click();
            await page.getByTestId('btn-aceite-mapa-confirmar').click();
            await fazerLogout(page);

            // 6. Admin homologa mapa final e testa botão finalizar
            await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
            await acessarSubprocessoAdmin(page, descricao, UNIDADE_ALVO);
            await navegarParaMapa(page);
            await page.getByTestId('btn-mapa-homologar-aceite').click();
            await page.getByTestId('btn-aceite-mapa-confirmar').click();

            await page.goto('/painel');
            await page.getByTestId('tbl-processos').getByText(descricao).first().click();
            await expect(page).toHaveURL(/\/processo\/\d+$/);

            // Modal de finalizar processo (Agora o botão deve estar visível)
            await page.getByTestId('btn-processo-finalizar').click();
            await expect(page.getByRole('dialog')).toBeVisible();
            await page.getByRole('button', {name: 'Cancelar'}).click();
        });

        test('Captura validações de formulário', async ({page}) => {
            await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);

            await page.getByTestId('btn-painel-criar-processo').click();
            await expect(page).toHaveURL(/\/processo\/cadastro/);

            // Capturar estado inicial com botões desativados (formulário vazio)
            await expect(page.getByTestId('btn-processo-salvar')).toBeDisabled();

            // Preencher apenas descrição (botões ainda desativados)
            await page.getByTestId('inp-processo-descricao').fill('Teste Validação');
            await expect(page.getByTestId('btn-processo-salvar')).toBeDisabled();

            // Selecionar tipo de processo (necessário para carregar árvore de unidades)
            await page.getByTestId('sel-processo-tipo').selectOption('MAPEAMENTO');

            // Preencher data limite (botões ainda desativados - falta unidade)
            const dataLimite = new Date();
            dataLimite.setDate(dataLimite.getDate() + 30);
            await page.getByTestId('inp-processo-data-limite').fill(dataLimite.toISOString().split('T')[0]);
            await expect(page.getByTestId('btn-processo-salvar')).toBeDisabled();

            // Expandir e selecionar unidade (agora botões devem estar ativados)
            await expect(page.getByText('Carregando unidades...')).toBeHidden();
            await page.getByTestId('btn-arvore-expand-SECRETARIA_1').click();
            await expect(page.getByTestId('chk-arvore-unidade-ASSESSORIA_11')).toBeVisible();
            await page.getByTestId('chk-arvore-unidade-ASSESSORIA_11').click();
            await expect(page.getByTestId('btn-processo-salvar')).toBeEnabled();
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

            await iniciarProcessoPeloCadastro(page, {
                descricao,
                tipo: 'MAPEAMENTO'
            });

            // Logout e login como Chefe
            await fazerLogout(page);
            await login(page, USUARIOS.CHEFE_SECAO_211.titulo, USUARIOS.CHEFE_SECAO_211.senha);

            await page.getByTestId('tbl-processos').getByText(descricao).first().click();
            await navegarParaSubprocesso(page, UNIDADE_ALVO);

            // Entrar em atividades
            await navegarParaAtividades(page);

            const atividadeDesc = `Atividade Teste ${Date.now()}`;
            await page.getByTestId('inp-nova-atividade').fill(atividadeDesc);
            await page.getByTestId('btn-adicionar-atividade').click();
            await expect(page.getByText(atividadeDesc, {exact: true})).toBeVisible();

            const card = page.locator('.atividade-card', {has: page.getByText(atividadeDesc)});
            await card.getByTestId('inp-novo-conhecimento').fill('Java');
            await adicionarConhecimento(page, atividadeDesc, 'Java');
            await adicionarConhecimento(page, atividadeDesc, 'Spring Boot');
            await adicionarConhecimento(page, atividadeDesc, 'Oracle');

            // Hover na atividade para ver ações
            await card.locator('.atividade-hover-row').hover();

            // Tentar disponibilizar sem data
            await page.getByTestId('btn-cad-atividades-disponibilizar').click();
            await expect(page.getByRole('dialog')).toBeVisible();
            await page.getByTestId('btn-confirmar-disponibilizacao').click();
            await verificarPaginaPainel(page);
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

            await iniciarProcessoPeloCadastro(page, {
                descricao,
                tipo: 'MAPEAMENTO'
            });

            await fazerLogout(page);
            await login(page, USUARIOS.CHEFE_SECAO_212.titulo, USUARIOS.CHEFE_SECAO_212.senha);

            await page.getByTestId('tbl-processos').getByText(descricao).first().click();
            await navegarParaSubprocesso(page, UNIDADE_ALVO);
            await navegarParaAtividades(page);

            // Capturar tela inicial vazia com label "Conhecimentos *"

            // Adicionar primeira atividade SEM conhecimento
            const atividade1 = `Atividade Sem Conhecimento 1 ${Date.now()}`;
            await adicionarAtividade(page, atividade1);

            // Adicionar segunda atividade SEM conhecimento
            const atividade2 = `Atividade Sem Conhecimento 2 ${Date.now()}`;
            await adicionarAtividade(page, atividade2);

            // Adicionar terceira atividade COM conhecimento (para contraste)
            const atividade3 = `Atividade Com Conhecimento ${Date.now()}`;
            await adicionarAtividade(page, atividade3);
            await adicionarConhecimento(page, atividade3, 'Java');

            // Tentar disponibilizar - deve mostrar validação inline
            await page.getByTestId('btn-cad-atividades-disponibilizar').click();
            await expect(page.getByText(/conhecimento/i).first()).toBeVisible();

            // Capturar primeira atividade com erro inline (scroll automático já levou até ela)

            // Scroll para segunda atividade com erro
            const card2 = page.locator('.atividade-card', {has: page.getByText(atividade2)});
            await card2.scrollIntoViewIfNeeded();

            // Capturar zoom na mensagem de erro inline
            const primeiroCard = page.locator('.atividade-card', {has: page.getByText(atividade1)});
            await primeiroCard.scrollIntoViewIfNeeded();

            // Capturar apenas o card com erro para detalhe

            // Corrigir primeira atividade adicionando conhecimento
            await adicionarConhecimento(page, atividade1, 'Python');

            // Tentar disponibilizar novamente - ainda deve ter erro na atividade 2
            await page.getByTestId('btn-cad-atividades-disponibilizar').click();

            // Corrigir segunda atividade
            await adicionarConhecimento(page, atividade2, 'JavaScript');

            // Tentar disponibilizar - agora deve funcionar
            await page.getByTestId('btn-cad-atividades-disponibilizar').click();
            // Modal de confirmação deve aparecer
            const modalConfirmacao = page.locator('.modal-content').filter({hasText: 'Disponibilização do cadastro'});

            // Garantir que a modal está visível antes de capturar
            await expect(modalConfirmacao).toBeVisible();
        });
    });

    test.describe('05 - Mapa de Competências', () => {
        test('Captura fluxo de mapa de competências', async ({page}) => {
            const descricao = `Proc Mapa ${Date.now()}`;
            const UNIDADE_ALVO = 'ASSESSORIA_12';

            await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);

            await criarProcesso(page, {
                descricao,
                tipo: 'MAPEAMENTO',
                diasLimite: 30,
                unidade: UNIDADE_ALVO,
                expandir: ['SECRETARIA_1']
            });

            const linhaProcesso = page.getByTestId('tbl-processos').locator('tr').filter({has: page.getByText(descricao)});
            await linhaProcesso.click();
            const processoId = await extrairProcessoId(page);
            if (processoId > 0) cleanup.registrar(processoId);

            await iniciarProcessoPeloCadastro(page, {
                descricao,
                tipo: 'MAPEAMENTO'
            });

            await fazerLogout(page);
            await login(page, USUARIOS.CHEFE_ASSESSORIA_12.titulo, USUARIOS.CHEFE_ASSESSORIA_12.senha);

            await acessarSubprocessoChefeDireto(page, descricao, UNIDADE_ALVO);
            await navegarParaAtividades(page);

            await adicionarAtividade(page, 'Desenvolvimento Web');
            await adicionarConhecimento(page, 'Desenvolvimento Web', 'Vue.js');
            await adicionarConhecimento(page, 'Desenvolvimento Web', 'TypeScript');

            await adicionarAtividade(page, 'Desenvolvimento Backend');
            await adicionarConhecimento(page, 'Desenvolvimento Backend', 'Java');
            await adicionarConhecimento(page, 'Desenvolvimento Backend', 'Spring Boot');

            // Disponibilizar (como chefe)
            await page.getByTestId('btn-cad-atividades-disponibilizar').click();
            await expect(page.getByRole('dialog')).toBeVisible();
            await page.getByTestId('btn-confirmar-disponibilizacao').click();
            await verificarPaginaPainel(page);

            // 1. GESTOR SECRETARIA_1 - Aceite
            await fazerLogout(page);
            await loginComPerfil(page, USUARIOS.GESTOR_SECRETARIA_1.titulo, USUARIOS.GESTOR_SECRETARIA_1.senha, USUARIOS.GESTOR_SECRETARIA_1.perfil);

            await acessarSubprocessoGestor(page, descricao, UNIDADE_ALVO);
            await navegarParaAtividadesVisualizacao(page);
            await page.getByTestId('btn-acao-analisar-principal').click();
            await page.getByTestId('inp-aceite-cadastro-obs').fill('Cadastro muito bem detalhado. Seguindo para homologação.');
            await page.getByTestId('btn-aceite-cadastro-confirmar').click();
            await verificarPaginaPainel(page);

            // 2. ADMIN - Homologação Final
            await fazerLogout(page);
            await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);

            // Navegar para o subprocesso (admin vê tabela de unidades)
            await acessarSubprocessoAdmin(page, descricao, UNIDADE_ALVO);

            // Entrar no cadastro de atividades (visualização)
            await navegarParaAtividadesVisualizacao(page);

            await page.getByTestId('btn-acao-analisar-principal').click();
            await page.getByTestId('inp-aceite-cadastro-obs').fill('Homologado sem ressalvas');
            await page.getByTestId('btn-aceite-cadastro-confirmar').click();
            await expect(page.getByTestId('card-subprocesso-mapa-edicao')).toBeVisible();

            // Agora o Mapa deve estar habilitado para edição pelo Admin
            // Navegar para mapa
            await navegarParaMapa(page);

            await abrirModalCriarCompetencia(page);

            const competenciaDesc = 'Desenvolvimento de Software';
            await page.getByTestId('inp-criar-competencia-descricao').fill(competenciaDesc);

            const modal = page.getByTestId('mdl-criar-competencia');
            await modal.locator('label').filter({hasText: 'Desenvolvimento Web'}).click();
            await modal.locator('label').filter({hasText: 'Desenvolvimento Backend'}).click();

            await page.getByTestId('btn-criar-competencia-salvar').click();
            await expect(modal).toBeHidden();
            await expect(page.getByText(competenciaDesc)).toBeVisible();

            // Hover na competência
            const card = page.locator('.competencia-card', {has: page.getByText(competenciaDesc)});
            await card.hover();

            await page.getByTestId('btn-cad-mapa-disponibilizar').click();
            await expect(page.getByTestId('mdl-disponibilizar-mapa')).toBeVisible();

            const dataLimite = new Date();
            dataLimite.setDate(dataLimite.getDate() + 30);
            await page.getByTestId('inp-disponibilizar-mapa-data').fill(dataLimite.toISOString().split('T')[0]);
            await page.getByTestId('btn-disponibilizar-mapa-confirmar').click();
            await verificarPaginaPainel(page);
        });
    });

    test.describe('06 - Navegação e Menus', () => {
        test('Captura elementos de navegação', async ({page}) => {
            await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);


            // Parâmetros (se admin)
            await page.getByTestId('btn-parametros').click();
            await expect(page.getByLabel(/Dias para inativação de processos/i)).toBeVisible();
            await page.goto('/painel');

            // Seção Unidades (para ADMIN)
            await page.getByText('Unidades').first().click();
            await expect(page.getByTestId('btn-arvore-expand-SECRETARIA_1')).toBeVisible();

            await page.getByText('Relatórios').click();
            await expect(page.getByRole('heading', {name: /Relatórios/i})).toBeVisible();

            await page.getByText('Histórico').click();
            await expect(page.locator('table')).toBeVisible();

            await page.evaluate(() => window.scrollTo(0, document.body.scrollHeight));
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
            const processoId1 = await extrairProcessoId(page);
            if (processoId1 > 0) cleanup.registrar(processoId1);
            await page.goto('/painel');

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

            await page.setViewportSize({width: 768, height: 1024});

            await page.setViewportSize({width: 375, height: 667});
        });
    });

    // SEÇÃO 09 - OPERAÇÕES EM BLOCO (CDUs 22-26)
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
            await fazerLogout(page);
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
            await verificarPaginaPainel(page);

            // Login como Gestor da COORD_11 para ver botão de aceitar em bloco
            await fazerLogout(page);
            await login(page, USUARIOS.GESTOR_COORD.titulo, USUARIOS.GESTOR_COORD.senha);

            await page.getByTestId('tbl-processos').getByText(descricao).first().click();
            await expect(page.getByRole('heading', {name: /Unidades participantes/i})).toBeVisible();

            // Capturar botão de aceitar em bloco (se visível)
            const btnAceitarBloco = page.getByRole('button', {name: /Aceitar.*Bloco/i});
            await expect(btnAceitarBloco).toBeVisible();
            await btnAceitarBloco.click();
            await expect(page.getByRole('dialog')).toBeVisible();
            await page.getByRole('button', {name: /Cancelar/i}).click();

            // Login como Admin para homologar em bloco
            await fazerLogout(page);
            await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);

            await page.getByTestId('tbl-processos').getByText(descricao).first().click();
            await expect(page.getByRole('heading', {name: /Unidades participantes/i})).toBeVisible();

            // Capturar botão de homologar cadastro em bloco (CDU-23)
            const btnHomologarBloco = page.getByRole('button', {name: /Homologar.*Bloco/i});
            await expect(btnHomologarBloco).toBeVisible();
            await btnHomologarBloco.click();
            await expect(page.getByRole('dialog')).toBeVisible();
            await page.getByRole('button', {name: /Cancelar/i}).click();

            // Capturar botão de disponibilizar mapas em bloco (CDU-24)
            const btnDisponibilizarMapaBloco = page.getByRole('button', {name: /Disponibilizar.*mapa.*Bloco/i});
            await expect(btnDisponibilizarMapaBloco).toBeHidden();

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

            await page.getByRole('row', {name: /SECAO_121/i}).click();
            await expect(page).toHaveURL(/\/processo\/\d+\/SECAO_121/);

            // Modal de alterar data limite (CDU-27)
            const btnAlterarData = page.getByRole('button', {name: /Alterar.*data.*limite/i});
            await expect(btnAlterarData).toBeVisible();
            await btnAlterarData.click();
            await expect(page.getByRole('dialog')).toBeVisible();
            await page.getByRole('button', {name: /Cancelar/i}).click();

            // Modal de reabrir cadastro (CDU-32)
            const btnReabrirCadastro = page.getByRole('button', {name: /Reabrir.*cadastro/i});
            await expect(btnReabrirCadastro).toBeHidden();

            // CDU-34: Botão de enviar lembrete (ação direta, sem modal)
            const btnEnviarLembrete = page.getByRole('button', {name: /Enviar.*lembrete/i});
            await expect(btnEnviarLembrete).toBeVisible();
        });
    });

    // SEÇÃO 11 - GESTÃO DE UNIDADES E ATRIBUIÇÕES (CDU-28)
    test.describe('11 - Gestão de Unidades', () => {
        test('Captura página de unidades e atribuição temporária', async ({page}) => {
            await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);

            // Navegar para página de unidades
            const linkUnidades = page.getByRole('link', {name: /Unidades/i});
            await expect(linkUnidades).toBeVisible();
            await linkUnidades.click();
            await expect(page.getByTestId('btn-arvore-expand-SECRETARIA_1')).toBeVisible();

            const btnExpand = page.getByTestId('btn-arvore-expand-SECRETARIA_1');
            await btnExpand.click();
            await expect(page.getByTestId('btn-arvore-expand-COORD_12')).toBeVisible();

            const btnExpandCoord12 = page.getByTestId('btn-arvore-expand-COORD_12');
            await btnExpandCoord12.click();
            await expect(page.getByText('SECAO_121').first()).toBeVisible();

            const unidade = page.getByText('SECAO_121').first();
            await unidade.click();
            await expect(page.getByRole('button', {name: /Criar atribuição|Nova atribuição/i})).toBeVisible();

            const btnCriarAtribuicao = page.getByRole('button', {name: /Criar atribuição|Nova atribuição/i});
            await btnCriarAtribuicao.click();
            await expect(page.getByRole('heading', {name: /Criar atribuição temporária/i})).toBeVisible();
            await page.getByTestId('btn-cancelar-atribuicao').click();
            await expect(btnCriarAtribuicao).toBeVisible();
        });
    });

    // SEÇÃO 12 - HISTÓRICO DE PROCESSOS (CDU-29)
    test.describe('12 - Histórico', () => {
        test('Captura seção de histórico', async ({page}) => {
            await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);

            // Acessar seção de histórico
            const linkHistorico = page.getByRole('link', {name: /Histórico/i});
            await expect(linkHistorico).toBeVisible();
            await linkHistorico.click();
            await expect(page.getByTestId('tbl-processos')).toBeVisible();
        });
    });

    // SEÇÃO 13 - PARÂMETROS E ADMINISTRADORES (CDUs 30-31)
    test.describe('13 - Configurações', () => {
        test('Captura página de configurações e administradores', async ({page}) => {
            await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);

            // Página de parâmetros (CDU-31)
            await page.getByTestId('btn-parametros').click();

            // Seção de configurações do sistema (CDU-31)
            const inputDiasInativacao = page.getByLabel(/Dias para inativação de processos/i);
            await expect(inputDiasInativacao).toBeVisible();

            // Página de administradores (CDU-30)
            await page.getByTestId('btn-administradores').click();
            await expect(page.getByRole('button', {name: /Adicionar|Novo/i})).toBeVisible();

            // Botão de adicionar administrador
            const btnAdicionar = page.getByRole('button', {name: /Adicionar|Novo/i});
            await btnAdicionar.click();
            await expect(page.getByRole('dialog')).toBeVisible();
            await page.getByRole('button', {name: /Cancelar/i}).click();
        });
    });

    // SEÇÃO 14 - RELATÓRIOS (CDUs 35-36)
    test.describe('14 - Relatórios', () => {
        test('Captura página e modais de relatórios', async ({page}) => {
            await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);

            const linkRelatorios = page.getByRole('link', {name: /Relatórios/i});
            await expect(linkRelatorios).toBeVisible();
            await linkRelatorios.click();

            const painelAndamento = page.getByRole('tabpanel', {name: /Andamento de processo/i});
            await expect(page.getByRole('heading', {name: /Relatórios/i})).toBeVisible();
            await expect(page.getByRole('tab', {name: /Andamento de processo/i})).toBeVisible();
            await expect(painelAndamento.getByLabel(/Selecione o Processo/i)).toBeVisible();
            await expect(painelAndamento.getByRole('button', {name: /Gerar Relatório/i})).toBeVisible();

            await page.getByRole('tab', {name: /^Mapas$/i}).click();
            const painelMapas = page.getByRole('tabpanel', {name: /^Mapas$/i});
            await expect(painelMapas.getByLabel(/Selecione o Processo/i)).toBeVisible();
            await expect(painelMapas.getByLabel(/Selecione a unidade/i)).toBeVisible();
            await expect(painelMapas.getByRole('button', {name: /Gerar PDF/i})).toBeVisible();
        });
    });
});
