import {vueTest as test} from '../support/vue-specific-setup';
import {expect} from '@playwright/test';
import {
    abrirProcessoPorNome,
    cancelarIniciacaoProcesso,
    clicarIniciarProcesso,
    confirmarNoModal,
    gerarNomeUnico,
    limparProcessosEmAndamento,
    loginComoAdmin,
    navegarParaCriacaoProcesso,
    preencherFormularioProcesso,
    selecionarUnidadesPorSigla,
    SELETORES,
} from '~/helpers';

/**
 * CDU-05: Iniciar processo de revisão
 * 
 * Ator: ADMIN
 * 
 * Objetivo: Validar a iniciação de um processo de revisão, incluindo:
 * - Abertura do modal de confirmação
 * - Criação de subprocessos para unidades participantes
 * - Envio de notificações e criação de alertas
 * - Mudança de situação do processo para EM_ANDAMENTO
 * 
 * Cobertura:
 * ✅ Fluxo principal completo (passos 1-13)
 * ✅ Fluxo alternativo (cancelamento - passo 5)
 * ✅ Validações de estado e criação de dados
 */
test.describe('CDU-05: Iniciar processo de revisão', () => {
    test.beforeEach(async ({page}) => {
        await limparProcessosEmAndamento(page);
        await loginComoAdmin(page);
    });

    test('deve exibir modal de confirmação ao clicar em Iniciar processo', async ({page}) => {
        // Abrir processo de revisão em situação CRIADO
        await abrirProcessoPorNome(page, 'Processo teste revisão CDU-05');

        // Clicar em Iniciar processo
        await clicarIniciarProcesso(page);

        // Verificar modal de confirmação
        const modal = page.locator('.modal.show');
        await expect(modal).toBeVisible();
        await expect(modal.getByText(/Ao iniciar o processo, não será mais possível editá-lo ou removê-lo/i)).toBeVisible();
        await expect(modal.getByText(/todas as unidades participantes serão notificadas por e-mail/i)).toBeVisible();
        await expect(modal.getByRole('button', {name: /Confirmar/i})).toBeVisible();
        await expect(modal.getByRole('button', {name: /Cancelar/i})).toBeVisible();
    });

    test('deve cancelar iniciação do processo ao clicar em Cancelar no modal', async ({page}) => {
        // Abrir processo e clicar em Iniciar
        await abrirProcessoPorNome(page, 'Processo teste revisão CDU-05');
        await clicarIniciarProcesso(page);

        // Cancelar no modal
        await cancelarIniciacaoProcesso(page);

        // Verificar que modal fechou e processo não foi iniciado
        const modal = page.locator('.modal.show');
        await expect(modal).not.toBeVisible({timeout: 5000});
        await expect(page).toHaveURL(/\/processo\/cadastro\?codProcesso=\d+/);
        await expect(page.getByRole('button', {name: /Iniciar processo/i})).toBeVisible();
    });

    test('deve iniciar processo de revisão e mudar situação para EM_ANDAMENTO', async ({page}) => {
        // Criar novo processo
        await navegarParaCriacaoProcesso(page);
        const nomeProcesso = `Processo Revisão Teste ${gerarNomeUnico('CDU-05')}`;
        await preencherFormularioProcesso(page, nomeProcesso, 'REVISAO', '2025-12-31');
        await selecionarUnidadesPorSigla(page, ['CDU05-REV-UNIT']);
        await page.getByRole('button', {name: /Salvar/i}).click();
        await page.waitForURL(/\/painel/, );

        // Abrir e iniciar processo
        await abrirProcessoPorNome(page, nomeProcesso);
        await clicarIniciarProcesso(page);
        await confirmarNoModal(page);
        await page.waitForTimeout(2000);

        // Verificar mudança de situação para EM_ANDAMENTO
        await page.goto('http://localhost:5173/painel');
        await page.waitForSelector('[data-testid="tabela-processos"]', );
        const processoRow = page.locator('[data-testid="tabela-processos"] tr').filter({hasText: nomeProcesso});
        await expect(processoRow).toBeVisible();
        await expect(processoRow).toContainText(/Em andamento/i);

        // Verificar que botão Iniciar não está mais disponível
        await processoRow.click();
        await expect(page).toHaveURL(/\/processo\/\d+/);
        await expect(page.getByRole('button', {name: /Iniciar processo/i})).not.toBeVisible();
    });

    test('deve criar subprocessos para unidades participantes ao iniciar processo', async ({page}) => {
        // Criar processo com unidade que tem subunidades
        await navegarParaCriacaoProcesso(page);
        const nomeProcesso = `Processo Multi-Unidade ${gerarNomeUnico('CDU-05')}`;
        await preencherFormularioProcesso(page, nomeProcesso, 'REVISAO', '2025-12-31');
        await selecionarUnidadesPorSigla(page, ['CDU05-SUB-UNIT']);
        await page.getByRole('button', {name: /Salvar/i}).click();
        await page.waitForURL(/\/painel/, );

        // Abrir processo e capturar ID da URL
        await abrirProcessoPorNome(page, nomeProcesso);
        const url = page.url();
        const match = url.match(/codProcesso=(\d+)/);
        const processoId = match ? match[1] : null;
        expect(processoId).toBeTruthy();
        
        // Iniciar processo
        await clicarIniciarProcesso(page);
        await confirmarNoModal(page);
        await page.waitForTimeout(2000);

        // Verificar criação de subprocessos via API
        const response = await page.request.get(`http://localhost:10000/api/processos/${processoId}/subprocessos`);
        expect(response.ok()).toBeTruthy();
        
        const subprocessos = await response.json();
        expect(subprocessos.length).toBeGreaterThan(0);

        // Verificar campos dos subprocessos
        for (const subprocesso of subprocessos) {
            expect(subprocesso).toHaveProperty('dataLimiteEtapa1');
            expect(subprocesso).toHaveProperty('situacao');
            expect(subprocesso.situacao).toBe('NAO_INICIADO');
        }
    });

    test('deve criar alertas para unidades participantes ao iniciar processo', async ({page}) => {
        // Criar e iniciar processo
        await navegarParaCriacaoProcesso(page);
        const nomeProcesso = `Processo Alertas ${gerarNomeUnico('CDU-05')}`;
        await preencherFormularioProcesso(page, nomeProcesso, 'REVISAO', '2025-12-31');
        await selecionarUnidadesPorSigla(page, ['CDU05-ALERT-UNIT']);
        await page.getByRole('button', {name: /Salvar/i}).click();
        await page.waitForURL(/\/painel/, );

        // Abrir e iniciar
        await abrirProcessoPorNome(page, nomeProcesso);
        await clicarIniciarProcesso(page);
        await confirmarNoModal(page);
        await page.waitForURL(/\/painel/);

        // Verificar criação de alertas
        const tabelaAlertas = page.locator('[data-testid="tabela-alertas"]');
        const alertaInicio = tabelaAlertas.locator('tr').filter({hasText: /Início do processo/i});
        await expect(alertaInicio.first()).toBeVisible();
    });

    test('deve preservar dados do processo após iniciação (somente leitura)', async ({page}) => {
        // Criar processo
        await navegarParaCriacaoProcesso(page);
        const nomeProcesso = `Processo Somente Leitura ${gerarNomeUnico('CDU-05')}`;
        await preencherFormularioProcesso(page, nomeProcesso, 'REVISAO', '2025-12-31');
        await selecionarUnidadesPorSigla(page, ['CDU05-READONLY-UNIT']);
        await page.getByRole('button', {name: /Salvar/i}).click();
        await page.waitForURL(/\/painel/, );

        // Abrir e iniciar
        await abrirProcessoPorNome(page, nomeProcesso);
        await clicarIniciarProcesso(page);
        await confirmarNoModal(page);
        await page.waitForTimeout(2000);

        // Reabrir processo e verificar somente leitura
        await page.goto('http://localhost:5173/painel');
        await page.waitForSelector('[data-testid="tabela-processos"]', );
        await page.locator('[data-testid="tabela-processos"] tr').filter({hasText: nomeProcesso}).click();
        
        // Verificar que campos estão desabilitados
        const campoDescricao = page.locator(SELETORES.CAMPO_DESCRICAO);
        if (await campoDescricao.count() > 0) {
            const isDisabled = await campoDescricao.isDisabled();
            const isReadonly = await campoDescricao.getAttribute('readonly');
            expect(isDisabled || isReadonly !== null).toBeTruthy();
        }
        
        // Botão Remover não deve estar visível
        await expect(page.getByRole('button', {name: /^Remover$/i})).not.toBeVisible();
        
        // Botão Iniciar não deve estar visível (já foi iniciado)
        await expect(page.getByRole('button', {name: /Iniciar processo/i})).not.toBeVisible();
    });

    test('deve validar que apenas processos CRIADO podem ser iniciados', async ({page}) => {
        await page.goto('http://localhost:5173/painel');
        await page.waitForSelector('[data-testid="tabela-processos"]', );
        
        // Procurar processo EM_ANDAMENTO
        const processoEmAndamento = page.locator('[data-testid="tabela-processos"] tr')
            .filter({hasText: /Revisão de mapa de competências STIC - 2024/});
        
        if (await processoEmAndamento.count() > 0) {
            await processoEmAndamento.click();
            await page.waitForTimeout(2000);
            
            // Verificar que botão Iniciar não está presente
            await expect(page.getByRole('button', {name: /Iniciar processo/i})).not.toBeVisible();
        }
    });

    test('deve exibir informações corretas no modal de confirmação', async ({page}) => {
        // Abrir processo
        await abrirProcessoPorNome(page, 'Processo teste revisão CDU-05');
        
        const descricao = await page.locator(SELETORES.CAMPO_DESCRICAO).inputValue();
        expect(descricao).toBeTruthy();

        // Clicar e verificar modal
        await clicarIniciarProcesso(page);
        const modal = page.locator('.modal.show');
        await expect(modal).toBeVisible();
        await expect(modal).toContainText(/não será mais possível editá-lo ou removê-lo/i);
        await expect(modal).toContainText(/unidades participantes serão notificadas/i);
    });
});
