import {expect, Page, test} from '@playwright/test';
import {login, USUARIOS} from './helpers/auth';
import {criarProcesso, verificarProcessoNaTabela} from './helpers/processo-helpers';

test.describe('CDU-05 - Iniciar processo de revisão', () => {
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
        await expect(page.getByTestId('input-descricao')).toHaveValue(descricao);

        // Iniciar processo
        await page.getByTestId('btn-iniciar-processo').click();
        await page.getByTestId('btn-modal-confirmar').click();

        // Validação: Processo iniciado com sucesso
        await verificarPaginaPainel(page);
        await verificarProcessoNaTabela(page, {
            descricao,
            situacao: 'Em andamento',
            tipo: 'Mapeamento'
        });

        console.log('PASSO 1 concluído: Processo de Mapeamento iniciado');
    }

    async function passo2_ChefeAdicionaAtividadesEConhecimentos(page: Page, descricaoProcesso: string, timestamp: number): Promise<void> {
        // Login como chefe
        await fazerLogout(page);
        await login(page, USUARIO_CHEFE, SENHA_CHEFE);

        // Navegar para o subprocesso
        await page.getByText(descricaoProcesso).click();
        await verificarPaginaSubprocesso(page);

        // Validação: Card de atividades está visível
        await expect(page.getByTestId('atividades-card')).toBeVisible();

        // Adicionar Atividade
        await page.getByTestId('atividades-card').click();
        await page.getByTestId('input-nova-atividade').fill(`Atividade Teste ${timestamp}`);
        await page.getByTestId('btn-adicionar-atividade').click();

        // Validação: Atividade foi criada
        const descAtividade = `Atividade Teste ${timestamp}`;
        await expect(page.getByText(descAtividade)).toBeVisible();

        // Adicionar conhecimento à atividade
        const cardAtividade = page.locator('.atividade-card').filter({hasText: descAtividade});
        await cardAtividade.getByTestId('input-novo-conhecimento').fill('Conhecimento Teste');
        await cardAtividade.getByTestId('btn-adicionar-conhecimento').click();

        // Validação: Conhecimento foi adicionado
        await expect(cardAtividade.getByText('Conhecimento Teste')).toBeVisible();
        
        // Aguardar um pouco para garantir que o conhecimento foi persistido
        await page.waitForTimeout(500);
        
        console.log('PASSO 2 concluído: Atividades e Conhecimentos adicionados');
    }

    async function passo2a_ChefeDisponibilizaCadastro(page: Page, descricaoProcesso: string): Promise<void> {
        // Já estamos na página de cadastro de atividades após o Passo 2

        // Validação: Botão de disponibilizar está visível
        await expect(page.getByTestId('btn-disponibilizar')).toBeVisible();

        // Disponibilizar cadastro
        await page.getByTestId('btn-disponibilizar').click();
        await page.getByTestId('btn-confirmar-disponibilizacao').click();

        // Validação: Mensagem de sucesso e redirecionamento para o painel
        await expect(page.getByText(/Cadastro de atividades disponibilizado/i)).toBeVisible();
        await verificarPaginaPainel(page);

        console.log('PASSO 2a concluído: Cadastro disponibilizado pelo Chefe');
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
        await page.getByTestId('atividades-card-vis').click();

        // Validação: Botão de homologar está visível
        await expect(page.getByTestId('btn-acao-principal-analise')).toBeVisible();

        // Homologar cadastro
        await page.getByTestId('btn-acao-principal-analise').click();
        await page.getByTestId('btn-modal-confirmar-aceite').click();

        // Validação: Redirecionado para o painel
        await verificarPaginaPainel(page);

        console.log('PASSO 2b concluído: Cadastro homologado pelo Admin');
    }

    async function passo3_AdminAdicionaCompetenciasEDisponibilizaMapa(
        page: Page,
        descricaoProcesso: string,
        timestamp: number
    ): Promise<void> {
        // Login como Admin
        await fazerLogout(page);
        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);

        // Navegar para o subprocesso
        await page.getByText(descricaoProcesso).click();
        await page.getByRole('row', {name: 'Assessoria 21'}).click();
        await verificarPaginaSubprocesso(page);

        // Entrar no Mapa de Competências
        await page.getByTestId('mapa-card').click();

        // Validação: Botão de criar competência está visível
        await expect(page.getByTestId('btn-criar-competencia')).toBeVisible();

        // Adicionar Competência
        await page.getByTestId('btn-criar-competencia').click();
        await page.getByTestId('input-descricao-competencia').fill(`Competência Teste ${timestamp}`);

        // Vincular atividade à competência
        await page.getByText(`Atividade Teste ${timestamp}`).click();
        await page.getByTestId('btn-salvar-competencia').click();

        // Validação: Modal fechou e competência foi criada
        await expect(page.getByTestId('criar-competencia-modal')).toBeHidden();
        await expect(page.getByText(`Competência Teste ${timestamp}`)).toBeVisible();

        // Disponibilizar Mapa
        await page.getByTestId('btn-disponibilizar-mapa').click();
        await page.getByTestId('input-data-limite').fill('2030-12-31');
        await page.getByTestId('btn-modal-confirmar').click();

        // Validação: Mapa foi disponibilizado (verificar badge ou estado)
        await expect(page.getByTestId('situacao-badge')).toHaveText(/Mapa disponibilizado/i);

        console.log('PASSO 3 concluído: Competências adicionadas e Mapa disponibilizado');
    }

    async function passo4_ChefeValidaMapa(
        page: Page,
        descricaoProcesso: string
    ): Promise<void> {
        // Login como Chefe
        await fazerLogout(page);
        await login(page, USUARIO_CHEFE, SENHA_CHEFE);

        // Navegar para o subprocesso
        await page.getByText(descricaoProcesso).click();
        await verificarPaginaSubprocesso(page);

        // Abrir mapa para validação
        await page.getByTestId('mapa-card-vis').click();

        // Validação: Botão de validar está visível
        await expect(page.getByTestId('validar-btn')).toBeVisible();

        // Validar o Mapa
        await page.getByTestId('validar-btn').click();
        await page.getByTestId('modal-validar-confirmar').click();

        // Validação: Mapa foi validado
        await expect(page.getByTestId('situacao-badge')).toHaveText(/Mapa validado/i);

        console.log('PASSO 4 concluído: Mapa validado pelo Chefe');
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
        await page.getByTestId('mapa-card-vis').click();

        // Validação: Botão de homologar está visível
        await expect(page.getByTestId('btn-registrar-aceite-homologar')).toBeVisible();

        // Homologar o Mapa
        await page.getByTestId('btn-registrar-aceite-homologar').click();
        await page.getByTestId('btn-modal-confirmar').click();

        // Validação: Mapa foi homologado
        await expect(page.getByTestId('situacao-badge')).toHaveText(/Mapa homologado/i);

        // Voltar ao painel e finalizar processo
        await page.goto('/painel');
        await page.getByText(descricaoProcesso).click();

        // Validação: Botão de finalizar está visível
        await expect(page.getByTestId('btn-finalizar-processo')).toBeVisible();

        // Finalizar processo
        await page.getByTestId('btn-finalizar-processo').click();
        await page.getByTestId('btn-modal-confirmar').click();

        // Validação: Processo finalizado com sucesso
        await verificarPaginaPainel(page);
        await verificarProcessoNaTabela(page, {
            descricao: descricaoProcesso,
            tipo: 'Mapeamento',
            situacao: 'Finalizado'
        });

        console.log('PASSO 5 concluído: Processo de Mapeamento finalizado');
    }

    // ========================================================================
    // TESTE PRINCIPAL
    // ========================================================================

    test('Deve realizar o ciclo completo de Mapeamento e então iniciar um processo de Revisão', async ({page}) => {
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
        await passo2a_ChefeDisponibilizaCadastro(page, descProcMapeamento);
        await passo2b_AdminHomologaCadastro(page, descProcMapeamento);
        await passo3_AdminAdicionaCompetenciasEDisponibilizaMapa(page, descProcMapeamento, timestamp);
        await passo4_ChefeValidaMapa(page, descProcMapeamento);
        await passo5_AdminHomologaEFinalizaProcesso(page, descProcMapeamento);

        console.log('\nPREPARAÇÃO CONCLUÍDA: Mapa vigente criado com sucesso\n');

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
        await expect(page.getByTestId('input-descricao')).toHaveValue(descProcRevisao);
        await page.getByTestId('btn-iniciar-processo').click();

        const modal = page.getByRole('dialog');
        await expect(modal).toBeVisible();
        await expect(modal.getByText('Ao iniciar o processo, não será mais possível editá-lo')).toBeVisible();
        await page.getByTestId('btn-modal-confirmar').click();
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
