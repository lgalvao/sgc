import {test} from '@playwright/test';
import {SELETORES, URLS} from "~/cdu/constantes-teste";
import {loginComoAdmin, loginComoChefe, loginComoGestor, loginComoServidor} from "~/cdu/auxiliares-verificacoes";

test.describe('Captura de Telas - Painel', () => {
    test('03 - Painel - ADMIN', async ({page}) => {
        await loginComoAdmin(page);
        await page.waitForURL(URLS.PAINEL);
        await page.screenshot({path: 'screenshots/03-painel-admin.png', fullPage: true});
    });

    test('04 - Painel - GESTOR', async ({page}) => {
        await loginComoGestor(page);
        await page.waitForURL(URLS.PAINEL);
        await page.screenshot({path: 'screenshots/04-painel-gestor.png', fullPage: true});
    });

    test('05 - Painel - CHEFE', async ({page}) => {
        await loginComoChefe(page);
        await page.waitForURL(URLS.PAINEL);
        await page.screenshot({path: 'screenshots/05-painel-chefe.png', fullPage: true});
    });

    test('06 - Painel - SERVIDOR', async ({page}) => {
        await loginComoServidor(page);
        await page.waitForURL(URLS.PAINEL);
        await page.screenshot({path: 'screenshots/06-painel-servidor.png', fullPage: true});
    });

    test('07 - Painel - Tabela de Processos Ordenada (ADMIN)', async ({page}) => {
        await loginComoAdmin(page);
        await page.waitForURL(URLS.PAINEL);
        await page.getByTestId(SELETORES.COLUNA_DESCRICAO).click(); // Ordena por descrição
        await page.waitForTimeout(500); // Espera a ordenação visual

        await page.screenshot({path: 'screenshots/07-painel-admin-processos-ordenados.png', fullPage: true});
    });

    test('08 - Painel - Alertas Visíveis (ADMIN)', async ({page}) => {
        await loginComoAdmin(page);
        await page.waitForURL(URLS.PAINEL);
        // Clicar em um alerta para marcar como lido e ver a mudança visual
        const alertasRows = page.locator('[data-testid="tabela-alertas"] tbody tr');
        if (await alertasRows.count() > 0) {
            await alertasRows.first().click();
            await page.waitForTimeout(500);
        }
        await page.screenshot({path: 'screenshots/08-painel-admin-alertas.png', fullPage: true});
    });
});
