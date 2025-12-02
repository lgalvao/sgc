import { expect, test } from '@playwright/test';
import { login, USUARIOS } from './helpers/auth';
import { criarProcesso, verificarProcessoNaTabela } from './helpers/processo-helpers';

test.describe('CDU-05 - Iniciar processo de revisão', () => {
    // Unidade ASSESSORIA_21 (12) - Titular 777777 (Janis Joplin)
    const UNIDADE_ALVO = 'ASSESSORIA_21';
    const USUARIO_CHEFE = '777777';
    const SENHA_CHEFE = 'senha';

    test('Deve realizar o ciclo completo de Mapeamento e então iniciar um processo de Revisão', async ({ page }) => {
        // Debug listeners
        page.on('console', msg => console.log(`PAGE LOG: ${msg.text()}`));
        page.on('pageerror', err => console.log(`PAGE ERROR: ${err.message}`));
        page.on('response', response => {
            if (response.status() >= 400) console.log(`HTTP ERROR: ${response.status()} ${response.url()}`);
        });

        const timestamp = Date.now();
        const descProcMapeamento = `Mapeamento Setup ${timestamp}`;
        const descProcRevisao = `Revisão Teste ${timestamp}`;

        // ------------------------------------------------------------------------
        // FASE 1: PREPARACAO - CRIAR MAPA VIGENTE VIA PROCESSO DE MAPEAMENTO
        // ------------------------------------------------------------------------

        // Admin cria processo de MAPEAMENTO
        await page.goto('/login');
        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);

        await criarProcesso(page, {
            descricao: descProcMapeamento,
            tipo: 'MAPEAMENTO',
            diasLimite: 30,
            unidade: UNIDADE_ALVO,
            expandir: ['SECRETARIA_2']
        });

        // Navegar para processo criado
        await page.getByRole('row', { name: descProcMapeamento }).click();
        await expect(page).toHaveURL(/\/processo\/cadastro/);
        await expect(page.getByTestId('input-descricao')).toHaveValue(descProcMapeamento);

        // Iniciar processo
        await page.getByTestId('btn-iniciar-processo').click();
        await page.getByTestId('btn-modal-confirmar').click();
        await expect(page).toHaveURL(/\/painel/);
        await verificarProcessoNaTabela(page, { descricao: descProcMapeamento, situacao: 'Em andamento', tipo: 'Mapeamento' });

        // Logout Admin
        await page.getByTestId('btn-logout').click();
        await expect(page).toHaveURL(/\/login/);

        // Logar como chefe da Unidade, para criar criar Atividades e Conhecimentos
        await login(page, USUARIO_CHEFE, SENHA_CHEFE);

        // CHEFE vai direto para o SubprocessoView ao clicar no processo
        await page.getByText(descProcMapeamento).click();
        await expect(page).toHaveURL(/\/processo\/\d+\/ASSESSORIA_21$/);

        // Adicionar Atividade
        await expect(page.getByTestId('atividades-card')).toBeVisible();
        await page.getByTestId('atividades-card').click();
        await page.getByTestId('input-nova-atividade').fill(`Atividade Teste ${timestamp}`);
        await page.getByTestId('btn-adicionar-atividade').click({ force: true });

        // Adicionar conhecimento à atividade
        const descAtividade = `Atividade Teste ${timestamp}`;
        await expect(page.getByText(descAtividade)).toBeVisible();

        const activityCard = page.locator('.atividade-card').filter({ hasText: descAtividade });
        await activityCard.getByTestId('input-novo-conhecimento').fill('Conhecimento Teste');
        await activityCard.getByTestId('btn-adicionar-conhecimento').click();

        // Logout do Chefe
        await page.getByTestId('btn-logout').click();

        // Login como Admin, para que possa criar Competências e Disponibilizar Mapa
        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);

        // Navegar para o subprocesso
        await page.getByText(descProcMapeamento).click();

        // Expandir se necessário e clicar na unidade
        await page.getByRole('row', { name: 'Assessoria 21' }).click();
        await expect(page).toHaveURL(/\/processo\/\d+\/ASSESSORIA_21$/);

        // Entrar no Mapa de Competências
        await page.getByTestId('mapa-card').click();

        // Adicionar Competência (Admin)
        await page.getByTestId('btn-criar-competencia').click();
        await page.getByTestId('input-descricao-competencia').fill(`Competência Teste ${timestamp}`);

        // Vincular atividade à competência
        await page.getByText(`Atividade Teste ${timestamp}`).click({ force: true });
        await page.getByTestId('btn-salvar-competencia').click({ force: true });

        // Aguardar modal fechar
        await expect(page.getByTestId('criar-competencia-modal')).toBeHidden();

        // Disponibilizar Mapa
        await page.getByTestId('btn-disponibilizar-mapa').click();

        // Preencher modal de disponibilização de mapa
        await page.getByTestId('input-data-limite').fill('2030-12-31');
        await page.getByTestId('btn-modal-confirmar').click();

        // Logout Admin
        await page.getByTestId('btn-logout').click();
        await expect(page).toHaveURL(/\/login/);

        // Login como Chefe novamente para garantir sessão limpa
        await login(page, USUARIO_CHEFE, SENHA_CHEFE);

        // Chefe clica no processo e navega diretamente para o subprocesso (como sempre acontece para perfil CHEFE)
        await page.getByText(descProcMapeamento).click();
        await expect(page).toHaveURL(/\/processo\/\d+\/ASSESSORIA_21$/);

        // Chefe Valida o Mapa
        await page.getByTestId('mapa-card-vis').click();
        await page.getByTestId('validar-btn').click();
        await page.getByTestId('modal-validar-confirmar').click();
        await expect(page.getByTestId('situacao-badge')).toHaveText(/Mapa validado/i);

        // Logout do Chefe
        await page.getByTestId('btn-logout').click();
        await expect(page).toHaveURL(/\/login/);

        // Admin Homologa e Finaliza
        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);

        // Homologar (Acessar subprocesso)
        await page.getByText(descProcMapeamento).click();
        await page.getByRole('row', { name: 'Assessoria 21' }).click();
        await expect(page).toHaveURL(/\/processo\/\d+\/ASSESSORIA_21$/);

        await page.getByTestId('mapa-card-vis').click();
        await page.getByTestId('btn-registrar-aceite-homologar').click();
        await page.getByTestId('btn-modal-confirmar').click();
        await expect(page.getByTestId('situacao-badge')).toHaveText(/Mapa homologado/i);

        // Voltar ao processo, finalizando prepração
        await page.goto('/painel');
        await page.getByText(descProcMapeamento).click();
        await page.getByTestId('btn-finalizar-processo').click();
        await page.getByTestId('btn-modal-confirmar').click();
        await expect(page).toHaveURL(/\/painel/);

        // Objetivo final da preparação: um processo de mapeamento finalizado
        await verificarProcessoNaTabela(page, { descricao: descProcMapeamento, situacao: 'Finalizado', tipo: 'Mapeamento' });

        // ------------------------------------------------------------------------
        // FASE 2: TESTE CDU-05 - INICIAR PROCESSO DE REVISÃO -- ESTE É o TESTE EM SI
        // ------------------------------------------------------------------------

        // Criar processo de REVISÃO
        await criarProcesso(page, {
            descricao: descProcRevisao,
            tipo: 'REVISAO',
            diasLimite: 30,
            unidade: UNIDADE_ALVO,
            expandir: ['SECRETARIA_2']
        });

        // Iniciar processo
        await page.getByText(descProcRevisao).click();
        await expect(page).toHaveURL(/\/processo\/cadastro/);
        await expect(page.getByTestId('input-descricao')).toHaveValue(descProcRevisao);

        await page.getByTestId('btn-iniciar-processo').click();

        // Verificar Modal e Confirmar
        const modal = page.getByRole('dialog');
        await expect(modal).toBeVisible();
        await expect(modal.getByText('Ao iniciar o processo, não será mais possível editá-lo')).toBeVisible();
        await page.getByTestId('btn-modal-confirmar').click();

        // Verificar redirecionamento e situação
        await expect(page).toHaveURL(/\/painel/);
        await verificarProcessoNaTabela(page, {
            descricao: descProcRevisao,
            situacao: 'Em andamento',
            tipo: 'Revisão'
        });
    });
});
