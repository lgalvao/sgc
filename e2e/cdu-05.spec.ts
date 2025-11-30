import { expect, test } from '@playwright/test';
import { login, autenticar, USUARIOS } from './helpers/auth';
import { criarProcesso, verificarProcessoNaTabela } from './helpers/processo-helpers';

test.describe('CDU-05 - Iniciar processo de revisão', () => {
    // Unidade ASSESSORIA_21 (12) - Titular 777777 (Janis Joplin)
    const UNIDADE_ALVO = 'ASSESSORIA_21';
    const USUARIO_CHEFE = '777777';
    const SENHA_CHEFE = 'senha';

    test('Deve realizar o ciclo completo de Mapeamento e então iniciar um processo de Revisão', async ({ page }) => {
        test.setTimeout(15000); // Aumenta timeout para fluxo longo

        // Debug listeners
        page.on('console', msg => console.log(`PAGE LOG: ${msg.text()}`));
        page.on('pageerror', err => console.log(`PAGE ERROR: ${err.message}`));
        page.on('response', response => {
            if (response.status() >= 400) {
                console.log(`HTTP ERROR: ${response.status()} ${response.url()}`);
            }
        });

        const timestamp = Date.now();
        const descricaoMapeamento = `Mapeamento Setup ${timestamp}`;
        const descricaoRevisao = `Revisão Teste ${timestamp}`;

        // ------------------------------------------------------------------------
        // FASE 1: PREPARACAO - CRIAR MAPA VIGENTE VIA PROCESSO DE MAPEAMENTO
        // ------------------------------------------------------------------------

        // Admin cria e inicia processo de MAPEAMENTO
        await page.goto('/login');
        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);

        await criarProcesso(page, {
            descricao: descricaoMapeamento,
            tipo: 'MAPEAMENTO',
            diasLimite: 30,
            unidade: UNIDADE_ALVO,
            expandir: ['SECRETARIA_2']
        });

        // Iniciar processo
        await page.getByRole('row', { name: descricaoMapeamento }).click();
        await expect(page).toHaveURL(/\/processo\/cadastro/);
        await expect(page.getByTestId('input-descricao')).toHaveValue(descricaoMapeamento);
        await page.getByTestId('btn-iniciar-processo').click();
        await page.getByTestId('btn-modal-confirmar').click();

        await expect(page).toHaveURL(/\/painel/);
        await verificarProcessoNaTabela(page, { descricao: descricaoMapeamento, situacao: 'Em andamento', tipo: 'Mapeamento' });

        // Logout Admin
        await page.getByTestId('btn-logout').click();

        // Chefe da Unidade cria Atividade e Competência
        await autenticar(page, USUARIO_CHEFE, SENHA_CHEFE);
        await expect(page).toHaveURL(/\/painel/);

        // Acessar subprocesso
        await page.getByText(descricaoMapeamento).click();
        await page.getByRole('cell', { name: UNIDADE_ALVO }).click(); // Clica na unidade na árvore ou lista

        // Adicionar Atividade
        await page.getByTestId('atividades-card').click();
        await page.getByTestId('input-nova-atividade').fill(`Atividade Teste ${timestamp}`);
        await page.getByTestId('btn-adicionar-atividade').click({ force: true });

        // Adicionar conhecimento à atividade (necessário para validação)
        const descricaoAtividade = `Atividade Teste ${timestamp}`;
        await expect(page.getByText(descricaoAtividade)).toBeVisible();

        const activityCard = page.locator('.atividade-card').filter({ hasText: descricaoAtividade });
        await activityCard.getByTestId('input-novo-conhecimento').fill('Conhecimento Teste');
        await activityCard.getByTestId('btn-adicionar-conhecimento').click();

        // Voltar para detalhes do subprocesso
        await page.getByTestId('btn-voltar').click();

        // Adicionar Competência
        await page.getByRole('heading', { name: 'Mapa de competências' }).click();
        await page.getByTestId('btn-criar-competencia').click();
        await page.getByTestId('input-descricao-competencia').fill(`Competência Teste ${timestamp}`);

        // Vincular atividade
        await page.getByText(`Atividade Teste ${timestamp}`).click();
        await page.getByTestId('btn-salvar-competencia').click();

        // Disponibilizar Mapa (Chefe)
        await page.getByTestId('btn-disponibilizar-mapa').click();

        // Preencher modal
        await page.getByTestId('input-data-limite').fill('2030-12-31');
        await page.getByTestId('btn-modal-confirmar').click();

        // Verificar status e logout
        await expect(page.getByText('Mapa disponibilizado')).toBeVisible();
        await page.getByTestId('btn-logout').click();

        // Admin Homologa e Finaliza
        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);

        // Homologar (Acessar subprocesso)
        await page.getByText(descricaoMapeamento).click();
        await page.getByText(UNIDADE_ALVO).click();
        await page.getByText('Mapa de competências').click();
        await page.getByTestId('btn-homologar').click();
        await page.getByTestId('btn-modal-confirmar').click();
        await expect(page.getByText('Mapa homologado')).toBeVisible();

        // Voltar ao processo e Finalizar
        await page.goto('/painel');
        await page.getByText(descricaoMapeamento).click();
        await page.getByTestId('btn-finalizar-processo').click();
        await page.getByTestId('btn-modal-confirmar').click();
        await expect(page).toHaveURL(/\/painel/);
        await verificarProcessoNaTabela(page, { descricao: descricaoMapeamento, situacao: 'Finalizado', tipo: 'Mapeamento' });

        // ------------------------------------------------------------------------
        // FASE 2: TESTE CDU-05 - INICIAR PROCESSO DE REVISÃO
        // ------------------------------------------------------------------------

        // 2.1. Criar processo de REVISÃO
        await criarProcesso(page, {
            descricao: descricaoRevisao,
            tipo: 'REVISAO',
            diasLimite: 30,
            unidade: UNIDADE_ALVO,
            expandir: ['SECRETARIA_2']
        });

        // 2.2. Iniciar processo
        await page.getByText(descricaoRevisao).click();
        await expect(page).toHaveURL(/\/processo\/cadastro/);
        await expect(page.getByTestId('input-descricao')).toHaveValue(descricaoRevisao);

        await page.getByTestId('btn-iniciar-processo').click();

        // 2.3. Verificar Modal e Confirmar
        const modal = page.getByRole('dialog');
        await expect(modal).toBeVisible();
        await expect(modal.getByText('Ao iniciar o processo, não será mais possível editá-lo')).toBeVisible();
        await page.getByTestId('btn-modal-confirmar').click();

        // 2.4. Verificar redirecionamento e Status
        await expect(page).toHaveURL(/\/painel/);
        await verificarProcessoNaTabela(page, {
            descricao: descricaoRevisao,
            situacao: 'Em andamento',
            tipo: 'Revisão'
        });
    });
});
