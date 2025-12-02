import {expect, Page, test} from '@playwright/test';
import {login, USUARIOS} from './helpers/auth';
import {criarProcesso, verificarProcessoNaTabela} from './helpers/processo-helpers';

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

test.describe('CDU-05 - Iniciar processo de revisao', () => {
    // Unidade ASSESSORIA_21 (12) - Titular 777777 (Janis Joplin)
    const UNIDADE_ALVO = 'ASSESSORIA_21';
    const USUARIO_CHEFE = '777777';
    const SENHA_CHEFE = 'senha';

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
        await page.getByRole('row', {name: descricao}).click();
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

        console.log('PASSO 1 concluido: Processo de Mapeamento iniciado');
    }

    async function passo2_ChefeAdicionaAtividadesEConhecimentos(page: Page, descricaoProcesso: string, timestamp: number): Promise<void> {
        // Login como chefe
        await fazerLogout(page);
        await login(page, USUARIO_CHEFE, SENHA_CHEFE);

        // Navegar para o subprocesso
        await page.getByText(descricaoProcesso).click();
        await verificarPaginaSubprocesso(page);

        // Validação: Card de atividades está visível
        await expect(page.getByTestId('card-subprocesso-atividades')).toBeVisible();

        // Adicionar Atividade
        await page.getByTestId('card-subprocesso-atividades').click();
        await page.getByTestId('inp-nova-atividade').fill(`Atividade Teste ${timestamp}`);
        await page.getByTestId('btn-adicionar-atividade').click();

        // Validação: Atividade foi criada
        const descAtividade = `Atividade Teste ${timestamp}`;
        await expect(page.getByText(descAtividade)).toBeVisible();

        // Adicionar conhecimento à atividade
        const cardAtividade = page.locator('.atividade-card').filter({hasText: descAtividade});
        await cardAtividade.getByTestId('inp-novo-conhecimento').fill('Conhecimento Teste');
        await cardAtividade.getByTestId('btn-adicionar-conhecimento').click();

        // Validação: Conhecimento foi adicionado
        await expect(cardAtividade.getByText('Conhecimento Teste')).toBeVisible();

        console.log('PASSO 2 concluido: Atividades e Conhecimentos adicionados');
    }

    async function passo2a_ChefeDisponibilizaCadastro(page: Page): Promise<void> {
        // Disponibilizar cadastro
        await page.getByTestId('btn-cad-atividades-disponibilizar').click();
        await page.getByTestId('btn-disponibilizar-cadastro-confirmar').click();

        // Validação: Mensagem de sucesso e redirecionamento para o painel
        await expect(page.getByRole('heading', { name: /Cadastro de atividades disponibilizado/i })).toBeVisible();
        await verificarPaginaPainel(page);

        console.log('PASSO 2a concluido: Cadastro disponibilizado pelo Chefe');
    }

    async function passo2b_AdminHomologaCadastro(page: Page, descricaoProcesso: string): Promise<void> {
        // Login como Admin
        await fazerLogout(page);
        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);

        // Navegar para o subprocesso
        await page.getByText(descricaoProcesso).click();
        await page.getByRole('row', {name: 'Assessoria 21'}).click();
        await verificarPaginaSubprocesso(page);

        // Entrar no cadastro de atividades (visualização)
        await page.getByTestId('card-subprocesso-atividades-vis').click();

        // Homologar cadastro
        await page.getByTestId('btn-acao-analisar-principal').click();
        await page.getByTestId('btn-aceite-cadastro-confirmar').click();

        // Validação: Redirecionado para o painel
        await verificarPaginaPainel(page);

        console.log('PASSO 2b concluido: Cadastro homologado pelo Admin');
    }

    async function passo3_AdminAdicionaCompetenciasEDisponibilizaMapa(page: Page, descProcesso: string, timestamp: number): Promise<void> {
        // Login como Admin
        await fazerLogout(page);
        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);

        // Validação: Redirecionado para o painel
        await verificarPaginaPainel(page);

        // Navegar para o subprocesso
        await page.getByText(descProcesso).click();
        await page.getByRole('row', {name: 'Assessoria 21'}).click();
        await verificarPaginaSubprocesso(page);

        // Entrar no Mapa de Competencias
        // TODO depois corrigir esse gambiarra. Deve aparecer um dos dois cards
        await page.locator('[data-testid="card-subprocesso-mapa"], [data-testid="card-subprocesso-mapa-vis"]').first().click();

        // Adicionar Competência
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

        // Validação: Mapa foi disponibilizado (verificar badge ou estado)
        await expect(page.getByTestId('txt-badge-situacao')).toHaveText(/Mapa disponibilizado/i);

        console.log('PASSO 3 concluido: Competências adicionadas e Mapa disponibilizado');
    }

    async function passo4_ChefeValidaMapa(page: Page, descProcesso: string): Promise<void> {
        // Login como Chefe
        await fazerLogout(page);
        await login(page, USUARIO_CHEFE, SENHA_CHEFE);

        // Navegar para o subprocesso
        await page.getByText(descProcesso).click();
        await verificarPaginaSubprocesso(page);

        // Abrir mapa para validação
        await page.getByTestId('card-subprocesso-mapa-vis').click();

        // Validar o Mapa
        await page.getByTestId('btn-mapa-validar').click();
        await page.getByTestId('btn-validar-mapa-confirmar').click();

        // Validação: confirmar Mapa foi validado
        await expect(page.getByTestId('txt-badge-situacao')).toHaveText(/Mapa validado/i);

        console.log('PASSO 4 concluido: Mapa validado pelo Chefe');
    }

    async function passo5_AdminHomologaEFinalizaProcesso(page: Page, descricaoProcesso: string): Promise<void> {
        // Login como Admin
        await fazerLogout(page);
        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);

        // Navegar para o subprocesso
        await page.getByText(descricaoProcesso).click();
        await page.getByRole('row', {name: 'Assessoria 21'}).click();
        await verificarPaginaSubprocesso(page);

        // Abrir mapa para homologar
        await page.getByTestId('card-subprocesso-mapa-vis').click();

        // Validação: Botão de homologar está visível
        await expect(page.getByTestId('btn-mapa-homologar-aceite')).toBeVisible();

        // Homologar o Mapa
        await page.getByTestId('btn-mapa-homologar-aceite').click();
        await page.getByTestId('btn-aceite-mapa-confirmar').click();

        // Validação: Mapa foi homologado
        await expect(page.getByTestId('txt-badge-situacao')).toHaveText(/Mapa homologado/i);

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

        console.log('PASSO 5 concluido: Processo de Mapeamento finalizado');
    }

    // ========================================================================
    // TESTE PRINCIPAL
    // ========================================================================

    test('Deve realizar o ciclo completo de Mapeamento e iniciar um processo de Revisão', async ({page}) => {
        // Debug listeners
        page.on('console', msg => console.log(`PAGE LOG: ${msg.text()}`));
        page.on('pageerror', err => console.log(`PAGE ERROR: ${err.message}`));
        page.on('response', response => {
            if (response.status() >= 400) console.log(`HTTP ERROR: ${response.status()} ${response.url()}`);
        });

        const timestamp = Date.now();
        const descProcMapeamento = `Mapeamento Setup ${timestamp}`;
        const descProcRevisao = `Revisão Teste ${timestamp}`;

        // ========================================================================
        // FASE 1: PREPARAÇÃO - CRIAR MAPA VIGENTE VIA PROCESSO DE MAPEAMENTO
        // ========================================================================

        console.log('\nINICIANDO PREPARAÇÃO: Criação de Processo, Atividades, Mapa e Finalização do Processo\n');
        await passo1_AdminCriaEIniciaProcessoMapeamento(page, descProcMapeamento);
        await passo2_ChefeAdicionaAtividadesEConhecimentos(page, descProcMapeamento, timestamp);
        await passo2a_ChefeDisponibilizaCadastro(page);
        await passo2b_AdminHomologaCadastro(page, descProcMapeamento);
        await passo3_AdminAdicionaCompetenciasEDisponibilizaMapa(page, descProcMapeamento, timestamp);
        await passo4_ChefeValidaMapa(page, descProcMapeamento);
        await passo5_AdminHomologaEFinalizaProcesso(page, descProcMapeamento);

        console.log('\nPREPARAÇÃO CONCLUÍDA: Processo de mapeamento finalizado e com mapa vigente.\n');

        // ========================================================================
        // FASE 2: TESTE CDU-05 - INICIAR PROCESSO DE REVISÃO
        // ========================================================================

        console.log('\nINICIANDO TESTE CDU-05: Iniciar processo de Revisão\n');

        // Criar processo de REVISÃO
        await criarProcesso(page, {
            descricao: descProcRevisao,
            tipo: 'REVISAO',
            diasLimite: 30,
            unidade: UNIDADE_ALVO,
            expandir: ['SECRETARIA_2']
        });

        // Validação: Processo foi criado
        console.log('Processo de Revisão criado');

        // Iniciar processo
        await page.getByText(descProcRevisao).click();
        await expect(page).toHaveURL(/\/processo\/cadastro/);
        await expect(page.getByTestId('inp-processo-descricao')).toHaveValue(descProcRevisao);
        await page.getByTestId('btn-processo-iniciar').click();

        const modal = page.getByRole('dialog');
        await expect(modal).toBeVisible();
        await expect(modal.getByText('Ao iniciar o processo, não será mais possível editá-lo')).toBeVisible();
        await page.getByTestId('btn-iniciar-processo-confirmar').click();
        console.log('Processo iniciado.');

        // Validação: Redirecionamento e situação do processo iniciado
        await verificarPaginaPainel(page);
        await verificarProcessoNaTabela(page, {
            descricao: descProcRevisao,
            situacao: 'Em andamento',
            tipo: 'Revisão'
        });
    });
});
