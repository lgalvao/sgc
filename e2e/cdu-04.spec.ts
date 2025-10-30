import {vueTest as test} from './support/vue-specific-setup';
import { expect } from '@playwright/test';
import {
    loginComoAdmin,
    navegarParaCriacaoProcesso,
    SELETORES
} from './helpers';

/**
 * CDU-04: Iniciar processo de mapeamento
 * 
 * ✅ O QUE ESTÁ COBERTO:
 * - Fluxo completo de iniciação: modal → confirmação → mudança de situação
 * - Cancelar iniciação
 * - Processo bloqueado após iniciado (não pode editar/remover)
 * 
 * ❌ LACUNAS (testadas no backend):
 * - Copiar árvore de unidades (passo 7)
 * - Criar subprocessos para cada unidade (passo 9)
 * - Criar mapas vazios (passo 10)  
 * - Registrar movimentações (passo 11)
 * - Enviar e-mails (passo 12)
 * - Criar alertas (passo 13)
 */
test.describe('CDU-04: Iniciar processo', () => {
    test.beforeEach(async ({page}) => await loginComoAdmin(page));

    test('deve abrir modal de confirmação e iniciar processo', async ({page}) => {
        // 1. Criar processo
        const descricao = `Processo Iniciar ${Date.now()}`;
        await navegarParaCriacaoProcesso(page);
        await page.fill(SELETORES.CAMPO_DESCRICAO, descricao);
        await page.selectOption(SELETORES.CAMPO_TIPO, 'MAPEAMENTO');
        await page.fill(SELETORES.CAMPO_DATA_LIMITE, '2025-12-31');
        await page.check(SELETORES.CHECKBOX_STIC);
        await expect(page.locator(SELETORES.CHECKBOX_STIC)).toBeChecked(); // Verify checkbox
        await page.getByRole('button', {name: /salvar/i}).click();
        await page.waitForURL(/\/painel/);
        
        // 2. Abrir processo e aguardar carregamento completo
        await page.getByText(descricao).first().click();
        await page.waitForURL(/\/processo\/cadastro/);
        await expect(page.locator(SELETORES.CAMPO_DESCRICAO)).toHaveValue(descricao);
        
        // DEBUG: Verificar estado do Vue após carregamento
        const debugInfo = await page.evaluate(() => {
            const checkboxes = Array.from(document.querySelectorAll('input[type="checkbox"]'));
            return {
                totalCheckboxes: checkboxes.length,
                checkedCheckboxes: checkboxes.filter((cb: any) => cb.checked).length,
                sticCheckbox: {
                    exists: !!document.querySelector('#chk-STIC'),
                    checked: document.querySelector<HTMLInputElement>('#chk-STIC')?.checked || false
                }
            };
        });
        console.log('[DEBUG TEST] Estado após carregar:', JSON.stringify(debugInfo, null, 2));
        
        // Aguardar checkbox estar marcado (Vue reactivity)
        await page.waitForFunction(() => {
            const checkbox = document.querySelector<HTMLInputElement>('#chk-STIC');
            return checkbox?.checked === true;
        }, {timeout: 5000});
        
        // 3. Clicar em Iniciar Processo → Abre modal
        await page.getByTestId(SELETORES.BTN_INICIAR_PROCESSO).click();
        const modal = page.locator('.modal.show');
        await expect(modal).toBeVisible();
        await expect(modal.getByText(/Iniciar processo/i)).toBeVisible();
        await expect(modal.getByText(/não será mais possível/i)).toBeVisible();
        
        // 4. Confirmar → Processo iniciado
        await modal.getByRole('button', {name: /confirmar/i}).click();
        await page.waitForURL(/\/painel/);
        await expect(page.getByText(descricao).first()).toBeVisible();
    });

    test('deve cancelar iniciação e permanecer na tela', async ({page}) => {
        // 1. Criar processo
        const descricao = `Processo Cancelar ${Date.now()}`;
        await navegarParaCriacaoProcesso(page);
        await page.fill(SELETORES.CAMPO_DESCRICAO, descricao);
        await page.selectOption(SELETORES.CAMPO_TIPO, 'MAPEAMENTO');
        await page.fill(SELETORES.CAMPO_DATA_LIMITE, '2025-12-31');
        await page.check(SELETORES.CHECKBOX_STIC);
        await page.getByRole('button', {name: /salvar/i}).click();
        await page.waitForURL(/\/painel/);
        
        // 2. Abrir e clicar em Iniciar
        await page.getByText(descricao).first().click();
        await expect(page.locator(SELETORES.CAMPO_DESCRICAO)).toHaveValue(descricao);
        await page.waitForTimeout(500); // Aguardar Vue carregar unidades
        await page.getByTestId(SELETORES.BTN_INICIAR_PROCESSO).click();
        const modal = page.locator('.modal.show');
        await expect(modal).toBeVisible();
        
        // 3. Cancelar → Modal fecha e permanece na tela
        await modal.getByRole('button', {name: /cancelar/i}).click();
        await expect(modal).not.toBeVisible();
        await expect(page.locator(SELETORES.CAMPO_DESCRICAO)).toHaveValue(descricao);
        await expect(page.getByTestId(SELETORES.BTN_INICIAR_PROCESSO)).toBeVisible();
    });

    test('não deve permitir editar processo após iniciado', async ({page}) => {
        // 1. Criar e iniciar processo
        const descricao = `Processo Bloqueio ${Date.now()}`;
        await navegarParaCriacaoProcesso(page);
        await page.fill(SELETORES.CAMPO_DESCRICAO, descricao);
        await page.selectOption(SELETORES.CAMPO_TIPO, 'MAPEAMENTO');
        await page.fill(SELETORES.CAMPO_DATA_LIMITE, '2025-12-31');
        await page.check(SELETORES.CHECKBOX_STIC);
        await page.getByRole('button', {name: /salvar/i}).click();
        await page.waitForURL(/\/painel/);
        
        await page.getByText(descricao).first().click();
        await expect(page.locator(SELETORES.CAMPO_DESCRICAO)).toHaveValue(descricao);
        await page.waitForTimeout(500); // Aguardar Vue carregar unidades
        await page.getByTestId(SELETORES.BTN_INICIAR_PROCESSO).click();
        await page.locator('.modal.show').getByRole('button', {name: /confirmar/i}).click();
        await page.waitForURL(/\/painel/);
        
        // 2. Abrir processo iniciado → botões Editar/Remover/Iniciar não aparecem
        await page.getByText(descricao).first().click();
        await expect(page.getByRole('button', {name: /salvar/i})).not.toBeVisible();
        await expect(page.getByRole('button', {name: /remover/i})).not.toBeVisible();
        await expect(page.getByTestId(SELETORES.BTN_INICIAR_PROCESSO)).not.toBeVisible();
    });
});
