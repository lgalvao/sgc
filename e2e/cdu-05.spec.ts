import {expect, test} from './fixtures/base';
import {login, USUARIOS} from './helpers/helpers-auth';
import {criarProcesso, verificarProcessoNaTabela} from './helpers/helpers-processos';
import {resetDatabase, useProcessoCleanup} from './hooks/hooks-limpeza';
import { Page, Response } from '@playwright/test';

async function fazerLogout(page: Page) {
    await page.getByTestId('btn-logout').click();
    await expect(page).toHaveURL(/\/login/);
}

async function verificarPaginaPainel(page: Page) {
    await expect(page).toHaveURL(/\/painel/);
}

async function verificarPaginaSubprocesso(page: Page) {
    await expect(page).toHaveURL(/\/processo\/\d+\/ASSESSORIA_21$/);
}

test.describe.serial('CDU-05 - Iniciar processo de revisao', () => {
    // Unidade ASSESSORIA_21 (12) - Titular 777777 (Janis Joplin)
    const UNIDADE_ALVO = 'ASSESSORIA_21';
    const USUARIO_CHEFE = '777777';
    const SENHA_CHEFE = 'senha';

    const timestamp = Date.now();
    const descProcMapeamento = `Mapeamento Setup ${timestamp}`;
    const descProcRevisao = `Revisão Teste ${timestamp}`;
    let processoMapeamentoId: number;
    let processoRevisaoId: number;
    let cleanup: ReturnType<typeof useProcessoCleanup>;

    // Reset completo do banco antes de todos os testes
    test.beforeAll(async ({request}) => {
        await resetDatabase(request);
        cleanup = useProcessoCleanup();
    });

    // Limpar processos criados após todos os testes
    test.afterAll(async ({request}) => {
        await cleanup.limpar(request);
    });

    // ========================================================================
    // PASSOS DE PREPARAÇÃO - PROCESSO DE MAPEAMENTO
    // ========================================================================

    async function passo1_AdminCriaEIniciaProcessoMapeamento(page: Page, descricao: string): Promise<void> {
        await page.goto('/login');
        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);

        await criarProcesso(page, {
            descricao,
            tipo: 'MAPEAMENTO',
            diasLimite: 30,
            unidade: UNIDADE_ALVO,
            expandir: ['SECRETARIA_2']
        });

        // Validação: Processo foi criado e está na tela de cadastro
        await verificarProcessoNaTabela(page, {
            descricao,
            situacao: 'Criado',
            tipo: 'Mapeamento'
        });

        const linhaProcesso = page.locator('tr', {has: page.getByText(descricao)});
        await linhaProcesso.click();
        await expect(page).toHaveURL(/\/processo\/cadastro/);
        await expect(page.getByTestId('inp-processo-descricao')).toHaveValue(descricao);

        // Iniciar processo
        await page.getByTestId('btn-processo-iniciar').click();
        await page.getByTestId('btn-iniciar-processo-confirmar').click();

        // Validação: Processo iniciado com sucesso
        await verificarPaginaPainel(page);
        await verificarProcessoNaTabela(page, {
            descricao,
            situacao: 'Em andamento',
            tipo: 'Mapeamento'
        });
    }

    async function passo2_ChefeAdicionaAtividadesEConhecimentos(page: Page, descricaoProcesso: string, timestamp: number): Promise<void> {
        // Login como chefe
        await fazerLogout(page);
        await login(page, USUARIO_CHEFE, SENHA_CHEFE);

        await page.waitForLoadState('networkidle');

        // Navegar para o subprocesso
        await page.getByText(descricaoProcesso).click();

        // Para CHEFE, o clique no processo no painel já redireciona para o subprocesso
        // se ele for da unidade participante.
        await verificarPaginaSubprocesso(page);

        // Validação: Card de atividades está visível
        await expect(page.getByTestId('card-subprocesso-atividades')).toBeVisible();

        // Adicionar Atividade
        await page.getByTestId('card-subprocesso-atividades').click();
        await page.getByTestId('inp-nova-atividade').fill(`Atividade Teste ${timestamp}`);

        const promessaAtividade = page.waitForResponse((resp: Response) => resp.url().includes('/atividades') && resp.status() === 201);
        await page.getByTestId('btn-adicionar-atividade').click();
        await promessaAtividade;

        // Validação: Atividade foi criada
        const descAtividade = `Atividade Teste ${timestamp}`;
        await expect(page.getByText(descAtividade)).toBeVisible();

        // Adicionar conhecimento à atividade
        const cardAtividade = page.locator('.atividade-card').filter({hasText: descAtividade});
        await cardAtividade.getByTestId('inp-novo-conhecimento').fill('Conhecimento Teste');

        const promessaConhecimento = page.waitForResponse((resp: Response) => resp.url().includes('/conhecimentos') && resp.status() === 201);
        await cardAtividade.getByTestId('btn-adicionar-conhecimento').click();
        await promessaConhecimento;

        // Validação: Conhecimento foi adicionado
        await expect(cardAtividade.getByText('Conhecimento Teste')).toBeVisible();
    }

    async function passo2a_ChefeDisponibilizaCadastro(page: Page): Promise<void> {
        // Disponibilizar cadastro
        await page.getByTestId('btn-cad-atividades-disponibilizar').click();
        await page.getByTestId('btn-confirmar-disponibilizacao').click();

        // Validação: Mensagem de sucesso e redirecionamento para o painel
        await expect(page.getByRole('heading', {name: /Cadastro de atividades disponibilizado/i})).toBeVisible();
        await verificarPaginaPainel(page);
    }

    async function passo2b_AdminHomologaCadastro(page: Page, descricaoProcesso: string): Promise<void> {
        // Login como Admin
        await fazerLogout(page);
        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);

        // Navegar para o subprocesso
        await page.getByText(descricaoProcesso).click();
                    await page.getByRole('row', {name: 'ASSESSORIA_21'}).click();        await verificarPaginaSubprocesso(page);

        // Entrar no cadastro de atividades (visualização)
        await page.getByTestId('card-subprocesso-atividades-vis').click();

        // Homologar cadastro
        await page.getByTestId('btn-acao-analisar-principal').click();
        await page.getByTestId('btn-aceite-cadastro-confirmar').click();

        // Validação: Redirecionado para Detalhes do subprocesso (CDU-13 passo 11.7)
        await verificarPaginaSubprocesso(page);
    }

    async function passo3_AdminAdicionaCompetenciasEDisponibilizaMapa(page: Page, descProcesso: string, timestamp: number): Promise<void> {
        // Login como Admin
        await fazerLogout(page);
        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);

        // Após homologação, já está na tela de Detalhes do subprocesso
        // Navegar para o subprocesso (caso não esteja mais lá)
        await page.getByText(descProcesso).click();
                    await page.getByRole('row', {name: 'ASSESSORIA_21'}).click();        await verificarPaginaSubprocesso(page);

        // Entrar no Mapa de Competencias
        await page.locator('[data-testid="card-subprocesso-mapa"], [data-testid="card-subprocesso-mapa"]').first().click();

        // Adicionar Competência
        await expect(page.getByTestId('btn-abrir-criar-competencia')).toBeVisible();
        await page.getByTestId('btn-abrir-criar-competencia').click();
        await page.getByTestId('inp-criar-competencia-descricao').fill(`Competência Teste ${timestamp}`);

        // Vincular atividade à competência
        await page.getByText(`Atividade Teste ${timestamp}`).click();
        await page.getByTestId('btn-criar-competencia-salvar').click();

        // Validação: Modal fechou e competência foi criada
        await expect(page.getByTestId('mdl-criar-competencia')).toBeHidden();
        await expect(page.getByText(`Competência Teste ${timestamp}`)).toBeVisible();

        // Disponibilizar Mapa
        await page.getByTestId('btn-cad-mapa-disponibilizar').click();
        await page.getByTestId('inp-disponibilizar-mapa-data').fill('2030-12-31');
        await page.getByTestId('btn-disponibilizar-mapa-confirmar').click();

        // Aguardar modal fechar
        await expect(page.getByTestId('mdl-disponibilizar-mapa')).toBeHidden();

        // Validação: Redirecionado para o painel após disponibilizar
        await verificarPaginaPainel(page);
        await verificarProcessoNaTabela(page, {
            descricao: descProcesso,
            tipo: 'Mapeamento',
            situacao: 'Em andamento'
        });
    }

    async function passo4_ChefeValidaMapa(page: Page, descProcesso: string): Promise<void> {
        // Login como Chefe
        await fazerLogout(page);
        await login(page, USUARIO_CHEFE, SENHA_CHEFE);

        // Navegar para o subprocesso (CHEFE vai direto)
        await page.getByText(descProcesso).click();
        await verificarPaginaSubprocesso(page);

        // Abrir mapa para validação
        await page.getByTestId('card-subprocesso-mapa').click();

        // Validar o Mapa
        await page.getByTestId('btn-mapa-validar').click();
        await page.getByTestId('btn-validar-mapa-confirmar').click();

        // Validação: confirmar redirecionamento para Painel (CDU-19 passo 8)
        await verificarPaginaPainel(page);
        
        // Verificar que o processo ainda está visível no painel
        await verificarProcessoNaTabela(page, {
            descricao: descProcesso,
            tipo: 'Mapeamento',
            situacao: 'Em andamento'
        });
    }

    async function passo5_AdminHomologaEFinalizaProcesso(page: Page, descricaoProcesso: string): Promise<void> {
        // Login como Admin
        await fazerLogout(page);
        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);

        // Navegar para o subprocesso
        await page.getByText(descricaoProcesso).click();
                    await page.getByRole('row', {name: 'ASSESSORIA_21'}).click();        await verificarPaginaSubprocesso(page);

        // Abrir mapa para homologar
        await page.getByTestId('card-subprocesso-mapa').click();

        // Validação: Botão de homologar está visível
        await expect(page.getByTestId('btn-mapa-homologar-aceite')).toBeVisible();

        // Homologar o Mapa
        await page.getByTestId('btn-mapa-homologar-aceite').click();
        await page.getByTestId('btn-aceite-mapa-confirmar').click();

        // Validação: confirmar redirecionamento para Painel (CDU-20 passo 10.6)
        await verificarPaginaPainel(page);

        // Voltar ao painel e finalizar processo
        await page.goto('/painel');
        await page.getByText(descricaoProcesso).click();

        // Finalizar processo
        await expect(page.getByTestId('btn-processo-finalizar')).toBeVisible();
        await page.getByTestId('btn-processo-finalizar').click();
        await page.getByTestId('btn-finalizar-processo-confirmar').click();

        // Validação: Processo finalizado com sucesso
        await verificarPaginaPainel(page);
        await verificarProcessoNaTabela(page, {
            descricao: descricaoProcesso,
            tipo: 'Mapeamento',
            situacao: 'Finalizado'
        });
    }

    // ========================================================================
    // TESTE PRINCIPAL
    // ========================================================================

    test('Fase 1: Ciclo completo de Mapeamento', async ({page}) => {
        await passo1_AdminCriaEIniciaProcessoMapeamento(page, descProcMapeamento);

        // Capturar ID do processo para cleanup
        await page.getByText(descProcMapeamento).click();
        processoMapeamentoId = parseInt(page.url().match(/\/processo\/(\d+)/)?.[1] || '0');
        if (processoMapeamentoId > 0) cleanup.registrar(processoMapeamentoId);
        await page.goto('/painel');

        await passo2_ChefeAdicionaAtividadesEConhecimentos(page, descProcMapeamento, timestamp);
        await passo2a_ChefeDisponibilizaCadastro(page);
        await passo2b_AdminHomologaCadastro(page, descProcMapeamento);
        await passo3_AdminAdicionaCompetenciasEDisponibilizaMapa(page, descProcMapeamento, timestamp);
        await passo4_ChefeValidaMapa(page, descProcMapeamento);
        await passo5_AdminHomologaEFinalizaProcesso(page, descProcMapeamento);
    });

    test('Fase 2: Iniciar processo de Revisão', async ({page}) => {
        // Login as Admin
        await page.goto('/login');
        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);

        // Criar processo de REVISÃO
        await criarProcesso(page, {
            descricao: descProcRevisao,
            tipo: 'REVISAO',
            diasLimite: 30,
            unidade: UNIDADE_ALVO,
            expandir: ['SECRETARIA_2']
        });

        // Capturar ID do processo para cleanup
        await page.getByText(descProcRevisao).click();
        await expect(page).toHaveURL(/\/processo\/cadastro/);
        processoRevisaoId = parseInt(page.url().match(/\/processo\/cadastro\/(\d+)/)?.[1] || '0');
        if (processoRevisaoId > 0) cleanup.registrar(processoRevisaoId);

        await expect(page.getByTestId('inp-processo-descricao')).toHaveValue(descProcRevisao);
        await page.getByTestId('btn-processo-iniciar').click();

        const modal = page.getByRole('dialog');
        await expect(modal).toBeVisible();
        await expect(modal.getByText('Ao iniciar o processo, não será mais possível editá-lo')).toBeVisible();
        await page.getByTestId('btn-iniciar-processo-confirmar').click();

        // Validação: Redirecionamento e situação do processo iniciado
        await verificarPaginaPainel(page);
        await verificarProcessoNaTabela(page, {
            descricao: descProcRevisao,
            situacao: 'Em andamento',
            tipo: 'Revisão'
        });
    });
});
