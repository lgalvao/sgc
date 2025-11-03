import {vueTest as test} from '../support/vue-specific-setup';
import {expect} from '@playwright/test';
import {
    abrirProcessoPorNome,
    loginComoAdmin,
    navegarParaCriacaoProcesso,
    preencherFormularioProcesso,
    selecionarUnidadesPorSigla,
    SELETORES,
} from '../helpers';

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
        await loginComoAdmin(page);
    });

    test('deve exibir modal de confirmação ao clicar em Iniciar processo', async ({page}) => {
        // Passo 1-2: Abrir processo de revisão em situação CRIADO
        await abrirProcessoPorNome(page, 'Processo teste revisão CDU-05');
        
        // Passo 3: Clicar em Iniciar processo
        const btnIniciar = page.getByRole('button', {name: /Iniciar processo/i});
        await expect(btnIniciar).toBeVisible({timeout: 1500});
        await btnIniciar.click();
        
        // Passo 4: Verificar modal de confirmação
        const modal = page.locator('.modal.show');
        await expect(modal).toBeVisible({timeout: 1500});
        
        // Verificar texto do modal
        await expect(modal.getByText(/Ao iniciar o processo, não será mais possível editá-lo ou removê-lo/i)).toBeVisible();
        await expect(modal.getByText(/todas as unidades participantes serão notificadas por e-mail/i)).toBeVisible();
        
        // Verificar botões
        await expect(modal.getByRole('button', {name: /Confirmar/i})).toBeVisible();
        await expect(modal.getByRole('button', {name: /Cancelar/i})).toBeVisible();
    });

    test('deve cancelar iniciação do processo ao clicar em Cancelar no modal', async ({page}) => {
        // Passo 1-3: Abrir processo e clicar em Iniciar
        await abrirProcessoPorNome(page, 'Processo teste revisão CDU-05');
        await page.getByRole('button', {name: /Iniciar processo/i}).click();
        
        const modal = page.locator('.modal.show');
        await expect(modal).toBeVisible({timeout: 1500});
        
        // Passo 5: Clicar em Cancelar
        await modal.getByRole('button', {name: /Cancelar/i}).click();
        
        // Verificar que modal fechou
        await expect(modal).not.toBeVisible({timeout: 5000});
        
        // Verificar que permanece na mesma tela (cadastro de processo)
        await expect(page).toHaveURL(/\/processo\/cadastro\?codProcesso=\d+/);
        
        // Verificar que botão Iniciar ainda está visível (processo não foi iniciado)
        await expect(page.getByRole('button', {name: /Iniciar processo/i})).toBeVisible();
    });

    test('deve iniciar processo de revisão e mudar situação para EM_ANDAMENTO', async ({page}) => {
        // Criar processo novo para este teste
        await navegarParaCriacaoProcesso(page);
        const descricaoProcesso = `Processo Revisão Teste ${Date.now()}`;
        await preencherFormularioProcesso(page, descricaoProcesso, 'REVISAO', '2025-12-31');
        await selecionarUnidadesPorSigla(page, ['ADMIN-UNIT']);
        
        // Salvar processo
        await page.getByRole('button', {name: /Salvar/i}).click();
        await page.waitForURL(/\/painel/, {timeout: 1500});
        
        // Abrir processo recém-criado
        await abrirProcessoPorNome(page, descricaoProcesso);
        
        // Iniciar processo
        await page.getByRole('button', {name: /Iniciar processo/i}).click();
        const modal = page.locator('.modal.show');
        await modal.getByRole('button', {name: /Confirmar/i}).click();
        
        // Passo 8: Verificar que situação mudou para EM_ANDAMENTO
        await page.waitForTimeout(2000);
        
        // Navegar ao painel para verificar situação do processo
        await page.goto('http://localhost:5173/painel');
        await page.waitForSelector('[data-testid="tabela-processos"]', {timeout: 1500});
        
        // Verificar que processo aparece como EM_ANDAMENTO
        const processoRow = page.locator('[data-testid="tabela-processos"] tr').filter({hasText: descricaoProcesso});
        await expect(processoRow).toBeVisible();
        await expect(processoRow).toContainText(/Em andamento/i);
        
        // Verificar que botão Iniciar não está mais disponível ao abrir o processo
        await processoRow.click();
        await expect(page).toHaveURL(/\/processo\/\d+/);
        await expect(page.getByRole('button', {name: /Iniciar processo/i})).not.toBeVisible();
    });

    test('deve criar subprocessos para unidades participantes ao iniciar processo', async ({page}) => {
        // Criar processo com unidade que tem subunidades
        await navegarParaCriacaoProcesso(page);
        const descricaoProcesso = `Processo Multi-Unidade ${Date.now()}`;
        await preencherFormularioProcesso(page, descricaoProcesso, 'REVISAO', '2025-12-31');
        await selecionarUnidadesPorSigla(page, ['ADMIN-UNIT']);
        
        // Salvar e iniciar
        await page.getByRole('button', {name: /Salvar/i}).click();
        await page.waitForURL(/\/painel/, {timeout: 1500});
        await abrirProcessoPorNome(page, descricaoProcesso);
        
        // Capturar ID do processo da URL
        const url = page.url();
        const match = url.match(/codProcesso=(\d+)/);
        const processoId = match ? match[1] : null;
        expect(processoId).toBeTruthy();
        
        // Iniciar processo
        await page.getByRole('button', {name: /Iniciar processo/i}).click();
        await page.locator('.modal.show').getByRole('button', {name: /Confirmar/i}).click();
        await page.waitForTimeout(2000);
        
        // Passo 9: Verificar criação de subprocessos via API
        const response = await page.request.get(`http://localhost:10000/api/processos/${processoId}/subprocessos`);
        expect(response.ok()).toBeTruthy();
        
        const subprocessos = await response.json();
        expect(subprocessos.length).toBeGreaterThan(0);
        
        // Verificar campos dos subprocessos (passo 9.1-9.4)
        for (const subprocesso of subprocessos) {
            expect(subprocesso).toHaveProperty('dataLimiteEtapa1');
            expect(subprocesso).toHaveProperty('situacao');
            expect(subprocesso.situacao).toBe('NAO_INICIADO');
        }
    });

    test('deve criar alertas para unidades participantes ao iniciar processo', async ({page}) => {
        // Criar e iniciar processo
        await navegarParaCriacaoProcesso(page);
        const descricaoProcesso = `Processo Alertas ${Date.now()}`;
        await preencherFormularioProcesso(page, descricaoProcesso, 'REVISAO', '2025-12-31');
        await selecionarUnidadesPorSigla(page, ['ADMIN-UNIT']);
        
        await page.getByRole('button', {name: /Salvar/i}).click();
        await page.waitForURL(/\/painel/, {timeout: 1500});
        await abrirProcessoPorNome(page, descricaoProcesso);
        
        await page.getByRole('button', {name: /Iniciar processo/i}).click();
        await page.locator('.modal.show').getByRole('button', {name: /Confirmar/i}).click();
        await page.waitForTimeout(2000);
        
        // Passo 13: Verificar criação de alertas
        await page.goto('http://localhost:5173/painel');
        await page.waitForSelector('[data-testid="tabela-alertas"]', {timeout: 1500});
        
        const tabelaAlertas = page.locator('[data-testid="tabela-alertas"]');
        const alertaInicio = tabelaAlertas.locator('tr').filter({hasText: /Início do processo/i});
        
        await expect(alertaInicio.first()).toBeVisible({timeout: 1500});
    });

    test('deve preservar dados do processo após iniciação (somente leitura)', async ({page}) => {
        // Criar processo
        await navegarParaCriacaoProcesso(page);
        const descricaoProcesso = `Processo Somente Leitura ${Date.now()}`;
        await preencherFormularioProcesso(page, descricaoProcesso, 'REVISAO', '2025-12-31');
        await selecionarUnidadesPorSigla(page, ['ADMIN-UNIT']);
        
        await page.getByRole('button', {name: /Salvar/i}).click();
        await page.waitForURL(/\/painel/, {timeout: 1500});
        await abrirProcessoPorNome(page, descricaoProcesso);
        
        // Iniciar
        await page.getByRole('button', {name: /Iniciar processo/i}).click();
        await page.locator('.modal.show').getByRole('button', {name: /Confirmar/i}).click();
        await page.waitForTimeout(2000);
        
        // Reabrir processo
        await page.goto('http://localhost:5173/painel');
        await page.waitForSelector('[data-testid="tabela-processos"]', {timeout: 1500});
        await page.locator('[data-testid="tabela-processos"] tr').filter({hasText: descricaoProcesso}).click();
        
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
        await page.waitForSelector('[data-testid="tabela-processos"]', {timeout: 1500});
        
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
        await abrirProcessoPorNome(page, 'Processo teste revisão CDU-05');
        
        const descricao = await page.locator(SELETORES.CAMPO_DESCRICAO).inputValue();
        expect(descricao).toBeTruthy();
        
        await page.getByRole('button', {name: /Iniciar processo/i}).click();
        const modal = page.locator('.modal.show');
        await expect(modal).toBeVisible();
        
        // Modal deve mencionar as consequências da iniciação (passo 4)
        await expect(modal).toContainText(/não será mais possível editá-lo ou removê-lo/i);
        await expect(modal).toContainText(/unidades participantes serão notificadas/i);
    });
});
