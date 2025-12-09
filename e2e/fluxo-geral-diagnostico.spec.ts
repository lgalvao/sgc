import { expect, test } from '@playwright/test';
import { login, USUARIOS } from './helpers/helpers-auth';
import { criarProcesso } from './helpers/helpers-processos';
import { adicionarAtividade, adicionarConhecimento, navegarParaAtividades } from './helpers/helpers-atividades';
import { resetDatabase, useProcessoCleanup } from './hooks/hooks-limpeza';

test.describe.serial('Fluxo Geral Diagnóstico (CDU-02 a CDU-09)', () => {
    const UNIDADE_ALVO = 'UNIT_TEST_DIAG';
    const USUARIO_CHEFE = '123456789012';
    const SENHA_CHEFE = '123'; // Senha padrão
    const USUARIO_ADMIN = USUARIOS.ADMIN_1_PERFIL.titulo;
    const SENHA_ADMIN = USUARIOS.ADMIN_1_PERFIL.senha;

    const timestamp = Date.now();
    const descProcessoMapeamento = `Mapeamento Setup Diagnostico ${timestamp}`;
    const descProcessoDiagnostico = `Diagnostico Teste ${timestamp}`;
    const nomeCompetencia = `Competência Diag A ${timestamp}`;
    
    let cleanup: ReturnType<typeof useProcessoCleanup>;

    test.beforeAll(async ({ request }) => {
        await resetDatabase(request);
        cleanup = useProcessoCleanup();
    });

    test.afterAll(async ({ request }) => {
        await cleanup.limpar(request);
    });

    /**
     * Passo 1: Criar e Homologar Mapa (Pré-requisito para Diagnóstico)
     */
    test('Passo 1: Setup - Criar e Homologar Mapa', async ({ page }) => {
        await page.goto('/login');
        await login(page, USUARIO_ADMIN, SENHA_ADMIN);

        // 1.1 Criar Processo Mapeamento
        await criarProcesso(page, {
            descricao: descProcessoMapeamento,
            tipo: 'MAPEAMENTO',
            diasLimite: 30,
            unidade: UNIDADE_ALVO,
            expandir: ['SECRETARIA_2', 'COORD_21']
        });

        // 1.2 Iniciar Processo
        const linhaProcesso = page.locator('tr', {has: page.getByText(descProcessoMapeamento)});
        await linhaProcesso.click();
        
        const idMap = parseInt(page.url().match(/\/processo\/cadastro\/(\d+)/)?.[1] || '0');
        if (idMap > 0) cleanup.registrar(idMap);

        await page.getByTestId('btn-processo-iniciar').click();
        await page.getByTestId('btn-iniciar-processo-confirmar').click();

        // 1.3 Preencher Mapa
        await expect(page).toHaveURL(/.*\/painel/);
        
        // Login Chefe para preencher mapa
        await page.goto('/login');
        await login(page, USUARIO_CHEFE, SENHA_CHEFE);

        await page.getByText(descProcessoMapeamento).click();
        
        // Adiciona Atividade
        await navegarParaAtividades(page);
        await adicionarAtividade(page, `Atividade Diag 1 ${timestamp}`);
        await adicionarConhecimento(page, `Atividade Diag 1 ${timestamp}`, 'Conhecimento 1');
        
        // Disponibiliza Cadastro
        await page.getByTestId('btn-cad-atividades-disponibilizar').click();
        await page.getByTestId('btn-confirmar-disponibilizacao').click();
        
        // Aceita Cadastro (Admin)
        await page.goto('/login');
        await login(page, USUARIO_ADMIN, SENHA_ADMIN);

        await page.getByText(descProcessoMapeamento).click();
        await page.getByRole('row', {name: /Unit Test Diag/i}).click();
        await page.getByTestId('card-subprocesso-atividades-vis').click();
        await page.getByTestId('btn-acao-analisar-principal').click();
        await page.getByTestId('btn-aceite-cadastro-confirmar').click();
        
        // Cria Competência no Mapa
        await expect(page).toHaveURL(/.*\/painel/);
        await page.getByText(descProcessoMapeamento).click();
        await page.getByRole('row', {name: /Unit Test Diag/i}).click();
        await page.getByTestId('card-subprocesso-mapa').click();
        
        await page.getByTestId('btn-abrir-criar-competencia').click();
        await page.getByTestId('inp-criar-competencia-descricao').fill(nomeCompetencia);
        await page.getByText(`Atividade Diag 1 ${timestamp}`).click();
        await page.getByTestId('btn-criar-competencia-salvar').click();
        
        // Disponibiliza Mapa
        await page.getByTestId('btn-cad-mapa-disponibilizar').click();
        await page.getByTestId('inp-disponibilizar-mapa-data').fill('2030-12-31');
        await page.getByTestId('btn-disponibilizar-mapa-confirmar').click();
        
        // Chefe Valida Mapa
        await page.goto('/login');
        await login(page, USUARIO_CHEFE, SENHA_CHEFE);
        await page.getByText(descProcessoMapeamento).click();
        await page.locator('[data-testid="card-subprocesso-mapa-vis"]').click();
        await page.getByTestId('btn-mapa-validar').click();
        await page.getByTestId('btn-validar-mapa-confirmar').click();
        
        // Admin Homologa Mapa
        await page.goto('/login');
        await login(page, USUARIO_ADMIN, SENHA_ADMIN);
        await page.getByText(descProcessoMapeamento).click();
        await page.getByRole('row', {name: /Unit Test Diag/i}).click();
        await page.locator('[data-testid="card-subprocesso-mapa-vis"]').click();
        await page.getByTestId('btn-mapa-homologar-aceite').click();
        await page.getByTestId('btn-aceite-mapa-confirmar').click();
        
        // Finalizar Processo Mapeamento
        await page.getByRole('link', { name: /Painel/i }).click();
        await page.getByText(descProcessoMapeamento).click();
        await page.getByTestId('btn-processo-finalizar').click();
        await page.getByTestId('btn-finalizar-processo-confirmar').click();
    });

    /**
     * Passo 2: Criar Processo de Diagnóstico
     */
    test('Passo 2: Criar Processo de Diagnóstico', async ({ page }) => {
        await page.goto('/login');
        await login(page, USUARIO_ADMIN, SENHA_ADMIN);

        await criarProcesso(page, {
            descricao: descProcessoDiagnostico,
            tipo: 'DIAGNOSTICO' as any,
            diasLimite: 30,
            unidade: UNIDADE_ALVO,
            expandir: ['SECRETARIA_2', 'COORD_21']
        });

        const linhaProcesso = page.locator('tr', {has: page.getByText(descProcessoDiagnostico)});
        await linhaProcesso.click();
        
        const idDiag = parseInt(page.url().match(/\/processo\/cadastro\/(\d+)/)?.[1] || '0');
        if (idDiag > 0) cleanup.registrar(idDiag);

        await page.getByTestId('btn-processo-iniciar').click();
        await page.getByTestId('btn-iniciar-processo-confirmar').click();
        
        await expect(page).toHaveURL(/.*\/painel/);
    });

    /**
     * Passo 3: Realizar Autoavaliação (Servidor/Chefe)
     */
    test('Passo 3: Realizar Autoavaliação (CDU-02)', async ({ page }) => {
        await page.goto('/login');
        await login(page, USUARIO_CHEFE, SENHA_CHEFE);

        await expect(page.getByText(descProcessoDiagnostico)).toBeVisible();
        await page.getByText(descProcessoDiagnostico).click();

        // Navega para Autoavaliação
        await page.getByTestId('card-subprocesso-diagnostico').click();

        // Verifica tela de Autoavaliação
        await expect(page.getByRole('heading', { name: 'Autoavaliação de Competências' })).toBeVisible();

        // Avalia competência
        const cardCompetencia = page.locator('.card', { hasText: nomeCompetencia });
        await expect(cardCompetencia).toBeVisible();
        
        await cardCompetencia.getByLabel('Importância').selectOption('N5');
        await cardCompetencia.getByLabel('Domínio').selectOption('N3');

        await expect(page.getByText('Salvo')).toBeVisible(); 

        // Concluir Autoavaliação
        await page.getByTestId('btn-concluir-autoavaliacao').click();
        
        await expect(page.getByText('Autoavaliação concluída com sucesso!')).toBeVisible();
        await expect(page).toHaveURL(/\/painel/);
    });

    /**
     * Passo 4: Monitoramento (CDU-03)
     */
    test('Passo 4: Monitoramento do Diagnóstico (CDU-03)', async ({ page }) => {
        await page.goto('/login');
        await login(page, USUARIO_CHEFE, SENHA_CHEFE);

        await page.getByText(descProcessoDiagnostico).click();
        
        // Navega para Monitoramento
        const cardMonitoramento = page.getByTestId('card-subprocesso-monitoramento');
        await expect(cardMonitoramento).toBeVisible();
        await cardMonitoramento.click();
        
        await expect(page.getByRole('heading', { name: 'Monitoramento do Diagnóstico' })).toBeVisible();
        
        // Verifica status da autoavaliação
        const rowServidor = page.getByRole('row', { name: /Usuario Diagnostico Mock/i });
        await expect(rowServidor).toBeVisible();
        await expect(rowServidor).toContainText(/Concluída/i);
    });

    /**
     * Passo 5: Ocupações Críticas (CDU-07)
     */
    test('Passo 5: Definir Ocupações Críticas (CDU-07)', async ({ page }) => {
        await page.goto('/login');
        await login(page, USUARIO_CHEFE, SENHA_CHEFE);
        await page.getByText(descProcessoDiagnostico).click();
        
        // Navega para Ocupações Críticas
        await page.getByTestId('card-subprocesso-ocupacoes').click();
        
        await expect(page.getByRole('heading', { name: 'Ocupações Críticas' })).toBeVisible();
        
        // Verifica competência com gap
        const cardCompetencia = page.locator('.card', { hasText: nomeCompetencia });
        await expect(cardCompetencia).toBeVisible();
        
        const row = cardCompetencia.getByRole('row', { name: nomeCompetencia });
        await expect(row).toBeVisible();
        await expect(row).toContainText('5'); // Importância
        await expect(row).toContainText('3'); // Domínio
        await expect(row).toContainText('2'); // Gap

        // Define Capacitação
        // Define Capacitação
        const selectSituacao = row.getByRole('combobox');
        await selectSituacao.selectOption('EC');
        
        // Verifica ícone de salvo
        await expect(row.locator('.bi-check')).toBeVisible();
    });

    /**
     * Passo 6: Concluir Diagnóstico (CDU-09)
     */
    test('Passo 6: Concluir Diagnóstico (CDU-09)', async ({ page }) => {
        await page.goto('/login');
        await login(page, USUARIO_CHEFE, SENHA_CHEFE);
        await page.getByText(descProcessoDiagnostico).click();
        
        await page.getByTestId('card-subprocesso-monitoramento').click();
        
        await page.getByTestId('btn-concluir-diagnostico').click();
        
        await expect(page.getByRole('heading', { name: 'Conclusão do Diagnóstico' })).toBeVisible();
        
        const btnConfirmar = page.getByTestId('btn-confirmar-conclusao');
        await expect(btnConfirmar).toBeVisible();
        
        // Preenche justificativa se necessário
        const temPendencias = await page.getByText(/Existem.*pendências/).isVisible();
        if (temPendencias) {
            await page.getByLabel('Justificativa').fill('Conclusão de teste E2E');
        }
        
        await btnConfirmar.click();
        
        await expect(page.getByText('Diagnóstico da unidade concluído com sucesso!')).toBeVisible();
        await expect(page).toHaveURL(/\/painel/);
        
        // Verifica status final
        await page.getByText(descProcessoDiagnostico).click();
        await expect(page.getByTestId('subprocesso-header__txt-badge-situacao')).toContainText('Concluído');
    });
});
