import { expect, test } from '@playwright/test';
import { login, autenticar, USUARIOS } from './helpers/auth';
import { criarProcesso, verificarProcessoNaTabela } from './helpers/processo-helpers';

test.describe('CDU-05 - Iniciar processo de revisão', () => {
    // Unidade ASSESSORIA_21 (12) - Titular 777777 (Janis Joplin)
    const UNIDADE_ALVO = 'ASSESSORIA_21';
    const USUARIO_CHEFE = '777777';
    const SENHA_CHEFE = 'senha';

    test('Deve realizar o ciclo completo de Mapeamento e então iniciar um processo de Revisão', async ({ page }) => {
        test.setTimeout(60000); // Aumenta timeout para fluxo longo

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

        // 3. CHEFE acessa e cadastra atividades
        await login(page, USUARIOS.CHEFE_UNIDADE.titulo, USUARIOS.CHEFE_UNIDADE.senha);
        await page.getByText(descricaoMapeamento).click();
        await expect(page).toHaveURL(/\/processo\/\d+$/);

        // Navegar para o subprocesso da unidade
        await page.getByText('ASSESSORIA_21').click();
        await expect(page).toHaveURL(/\/processo\/\d+\/ASSESSORIA_21$/);

        // Cadastrar Atividades e Conhecimentos
        await page.getByTestId('atividades-card').click();
        await page.getByTestId('input-nova-atividade').fill('Atividade Teste');
        await page.getByTestId('btn-adicionar-atividade').click();
        await page.getByTestId('input-novo-conhecimento').first().fill('Conhecimento Teste');
        await page.getByTestId('btn-adicionar-conhecimento').first().click();

        // Disponibilizar Cadastro (Atividades)
        // Aguardar atualização do status para evitar flakiness
        await expect(page.getByTestId('situacao-badge')).toHaveText('Cadastro em andamento');

        await page.getByTestId('btn-disponibilizar').click();
        await page.getByTestId('btn-confirmar-disponibilizacao').click();
        await page.goto('/painel');

        // 4. ADMIN Homologa Cadastro (Atividades)
        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
        await page.getByText(descricaoMapeamento).click();
        await page.getByText(`${UNIDADE_ALVO} - Assessoria 21`).click();

        // Adicionar Atividade
        await expect(page.getByTestId('atividades-card-vis')).toBeVisible(); // Wait for card to be visible
        const statusBadge = await page.locator('[data-testid="atividades-card-vis"] .badge').textContent();
        console.log(`STATUS BADGE: ${statusBadge}`);
        await page.getByTestId('atividades-card-vis').click();
        await page.getByTestId('btn-acao-principal-analise').click();
        await page.getByTestId('btn-modal-confirmar-aceite').click();
        await expect(page.getByText('Homologação efetivada')).toBeVisible();
        await page.getByTestId('btn-voltar').first().click();

        // 5. ADMIN Cria e Disponibiliza Mapa
        await page.getByTestId('mapa-card').click();

        // Criar Competência (necessário para disponibilizar)
        await page.getByTestId('btn-criar-competencia').click();
        await page.getByTestId('input-descricao-competencia').fill('Competência Teste');
        await page.getByTestId('atividade-checkbox').first().check();
        await page.getByTestId('btn-salvar-competencia').click();

        // Disponibilizar Mapa
        await page.getByTestId('btn-disponibilizar-mapa').click();
        await page.getByTestId('input-data-limite').fill('2025-12-31');
        await page.getByTestId('btn-modal-confirmar').click();
        // Modal fecha, volta para subprocesso ou fica na tela? 
        // CadMapa.vue TODO diz: "Adicionar redirecionamento para o painel", mas atualmente fecha modal.
        // Vamos voltar manualmente se necessário.
        await page.getByTestId('btn-voltar').first().click();

        // 6. ADMIN Homologa Mapa
        // Agora deve aparecer o card de visualização (mapa-card-vis) pois está disponibilizado
        await page.getByTestId('mapa-card-vis').click();
        await page.getByTestId('btn-registrar-aceite-homologar').click();
        await page.getByTestId('btn-modal-confirmar').click();
        await expect(page.getByText('Homologação efetivada')).toBeVisible();

        // Voltar ao painel para iniciar Revisão
        await page.goto('/painel');
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
