import {vueTest as test} from './support/vue-specific-setup';
import {expect} from '@playwright/test';
import {
    loginComoAdmin,
    criarProcesso,
    limparProcessos,
    abrirProcessoPorDescricao,
} from './helpers';

test.describe('CDU-05: Iniciar processo de revisão (com preparação)', () => {

    test.beforeEach(async ({page}) => {
        await loginComoAdmin(page);
        await limparProcessos(page);
    });

    test.afterEach(async ({page}) => {
        await loginComoAdmin(page);
        await limparProcessos(page);
    });

    test('deve iniciar processo, mudar situação e verificar bloqueio de edição', async ({page}) => {
        const descricao = `Processo Revisão ${Date.now()}`;
        // A unidade 'ADMIN-UNIT' é usada porque já possui um mapa vigente no data.sql,
        // o que é um pré-requisito para processos de REVISAO.
        const processoId = await criarProcesso(page, 'REVISAO', descricao, ['ADMIN-UNIT']);

        await page.goto('/painel');
        await abrirProcessoPorDescricao(page, descricao);

        // Clicar em Iniciar e confirmar no modal
        await page.getByRole('button', {name: /Iniciar processo/i}).click();
        const modal = page.locator('.modal.show');
        await expect(modal).toBeVisible();
        await expect(modal).toContainText(/não será mais possível editá-lo ou removê-lo/);
        await modal.getByRole('button', {name: /Confirmar/i}).click();

        // Verificar se voltou ao painel e a situação mudou
        await page.waitForURL(/\/painel/);
        const processoRow = page.locator('[data-testid="tabela-processos"] tr').filter({hasText: descricao});
        await expect(processoRow).toContainText(/Em andamento/i);

        // Verificar se o processo não é mais editável
        await processoRow.click();
        await page.waitForURL(new RegExp(`/processo/${processoId}`));
        await expect(page.getByRole('button', {name: /Iniciar processo/i})).not.toBeVisible();
        await expect(page.getByRole('button', {name: /Salvar/i})).not.toBeVisible();
        await expect(page.getByRole('button', {name: /Remover/i})).not.toBeVisible();
    });

    test('deve criar subprocessos para as unidades ao iniciar', async ({page}) => {
        const descricao = `Processo Subprocessos ${Date.now()}`;
        const processoId = await criarProcesso(page, 'REVISAO', descricao, ['ADMIN-UNIT']);

        await page.goto('/painel');
        await abrirProcessoPorDescricao(page, descricao);

        // Iniciar o processo
        await page.getByRole('button', {name: /Iniciar processo/i}).click();
        await page.locator('.modal.show').getByRole('button', {name: /Confirmar/i}).click();
        await page.waitForURL(/\/painel/);

        // Verificar via API se os subprocessos foram criados
        const response = await page.request.get(`http://localhost:10000/api/processos/${processoId}/subprocessos`);
        expect(response.ok()).toBeTruthy();
        const subprocessos = await response.json();
        expect(subprocessos.length).toBeGreaterThan(0);
        expect(subprocessos[0].situacao).toBe('NAO_INICIADO');
    });

    test('deve criar alertas para as unidades ao iniciar', async ({page}) => {
        const descricao = `Processo Alertas ${Date.now()}`;
        await criarProcesso(page, 'REVISAO', descricao, ['ADMIN-UNIT']);

        await page.goto('/painel');
        await abrirProcessoPorDescricao(page, descricao);

        // Iniciar o processo
        await page.getByRole('button', {name: /Iniciar processo/i}).click();
        await page.locator('.modal.show').getByRole('button', {name: /Confirmar/i}).click();
        await page.waitForURL(/\/painel/);

        // Verificar no painel se o alerta foi criado
        const tabelaAlertas = page.locator('[data-testid="tabela-alertas"]');
        const alertaInicio = tabelaAlertas.locator('tr').filter({hasText: /Início do processo/i});
        await expect(alertaInicio.first()).toBeVisible({timeout: 10000});
    });
});
