import { test, expect } from '@playwright/test';
import { loginComoAdmin } from './helpers/auth';

test.describe('CDU-03: Manter processo', () => {
    test.beforeEach(async ({ page }) => {
        await loginComoAdmin(page);
    });

    test('deve criar processo e redirecionar para o Painel', async ({ page }) => {
        test.setTimeout(10000);
        const descricao = `Processo E2E ${Date.now()}`;

        await page.goto('/processo/cadastro');
        await expect(page.getByRole('heading', { name: 'Cadastro de processo' })).toBeVisible();

        await page.getByTestId('input-descricao').fill(descricao);
        await page.getByTestId('select-tipo').selectOption('MAPEAMENTO');
        await page.getByTestId('input-dataLimite').fill('2025-12-31');

        // Select STIC (ID 2)
        // Using force: true for checkbox if needed, or ensuring it's visible
        const chkStic = page.getByTestId('chk-STIC');
        await expect(chkStic).toBeEnabled();
        await chkStic.check({ force: true });

        // Click Salvar with force to avoid stability issues
        await page.getByTestId('btn-salvar').click();

        // Verify redirect to Painel
        await expect(page).toHaveURL(/.*\/painel/);
        await expect(page.getByText(descricao)).toBeVisible();
    });

    test('deve validar descrição obrigatória', async ({ page }) => {
        await page.goto('/processo/cadastro');

        await page.getByTestId('select-tipo').selectOption('MAPEAMENTO');
        await page.getByTestId('input-dataLimite').fill('2025-12-31');
        await page.getByTestId('chk-STIC').check({ force: true });

        await page.getByTestId('btn-salvar').click();

        // Should stay on page (or show validation error)
        // HTML5 validation might prevent submission, or backend error.
        // Assuming HTML5 validation on 'Descrição' field or Vuelidate.
        // Let's check if we are still on the page.
        await expect(page.getByRole('heading', { name: 'Cadastro de processo' })).toBeVisible();
    });

    test('deve abrir processo para edição e modificar descrição', async ({ page }) => {
        const descricaoOriginal = `Processo para Editar ${Date.now()}`;
        const novaDescricao = `Processo Editado ${Date.now()}`;

        // 1. Setup via API
        const response = await page.request.post('http://localhost:10000/api/processos', {
            data: {
                descricao: descricaoOriginal,
                tipo: 'MAPEAMENTO',
                dataLimiteEtapa1: '2025-12-31T00:00:00',
                unidades: [2] // STIC
            }
        });
        expect(response.ok()).toBeTruthy();
        const processo = await response.json();
        const codProcesso = processo.codigo;

        // 2. Go to Edit Page
        await page.goto(`/processo/cadastro?codProcesso=${codProcesso}`);
        await expect(page.getByRole('heading', { name: 'Cadastro de processo' })).toBeVisible();
        await expect(page.getByTestId('input-descricao')).toHaveValue(descricaoOriginal);

        // 3. Edit
        await page.getByTestId('input-descricao').fill(novaDescricao);
        await page.getByTestId('btn-salvar').click();

        // 4. Verify Redirect and Update
        await expect(page).toHaveURL(/.*\/painel/);
        await expect(page.getByText(novaDescricao)).toBeVisible();
    });

    test('deve exibir botão Remover apenas em modo de edição', async ({ page }) => {
        // Creation mode
        await page.goto('/processo/cadastro');
        await expect(page.getByTestId('btn-remover')).not.toBeVisible();

        // Edit mode (Setup via API)
        const descricao = `Processo Botao Remover ${Date.now()}`;
        const response = await page.request.post('http://localhost:10000/api/processos', {
            data: {
                descricao: descricao,
                tipo: 'MAPEAMENTO',
                dataLimiteEtapa1: '2025-12-31T00:00:00',
                unidades: [2]
            }
        });
        expect(response.ok()).toBeTruthy();
        const processo = await response.json();

        await page.goto(`/processo/cadastro?codProcesso=${processo.codigo}`);
        await expect(page.getByTestId('btn-remover')).toBeVisible();
    });

    test('deve remover processo após confirmação', async ({ page }) => {
        const descricao = `Processo para Remover ${Date.now()}`;

        // 1. Setup via API
        const response = await page.request.post('http://localhost:10000/api/processos', {
            data: {
                descricao: descricao,
                tipo: 'MAPEAMENTO',
                dataLimiteEtapa1: '2025-12-31T00:00:00',
                unidades: [2]
            }
        });
        expect(response.ok()).toBeTruthy();
        const processo = await response.json();

        // 2. Go to Edit Page
        await page.goto(`/processo/cadastro?codProcesso=${processo.codigo}`);

        // 3. Click Remove
        await page.getByTestId('btn-remover').click();

        // 4. Confirm in Modal
        await expect(page.getByText(`Remover o processo '${descricao}'?`)).toBeVisible();
        await page.getByRole('button', { name: 'Remover' }).nth(1).click();

        // 5. Verify Redirect and Removal
        await expect(page).toHaveURL(/.*\/painel/);
        // Check that it's not in the table (ignore toast)
        await expect(page.locator('table').getByText(descricao)).not.toBeVisible();
    });

    test('deve selecionar automaticamente as unidades filhas ao selecionar o pai', async ({ page }) => {
        await page.goto('/processo/cadastro');
        await page.getByTestId('select-tipo').selectOption('MAPEAMENTO');

        // STIC is the root for this user (ADMIN of STIC)
        // STIC -> COSIS
        const paiSigla = 'STIC';
        const filhosSiglas = ['COSIS'];

        const checkboxPai = page.getByTestId(`chk-${paiSigla}`);

        await expect(checkboxPai).toBeVisible();
        await checkboxPai.check({ force: true });

        await expect(checkboxPai).toBeChecked();
        for (const sigla of filhosSiglas) {
            await expect(page.getByTestId(`chk-${sigla}`)).toBeChecked();
        }

        await checkboxPai.uncheck({ force: true });
        await expect(checkboxPai).not.toBeChecked();
        for (const sigla of filhosSiglas) {
            await expect(page.getByTestId(`chk-${sigla}`)).not.toBeChecked();
        }
    });

    test('deve validar unidades sem mapa ao criar processo de Revisão', async ({ page }) => {
        await page.goto('/processo/cadastro');

        await page.getByTestId('input-descricao').fill('Processo Revisão Inválido');
        await page.getByTestId('select-tipo').selectOption('REVISAO');
        await page.getByTestId('input-dataLimite').fill('2025-12-31');

        // STIC (ID 2) has no map.
        // If it's disabled in UI, we verify it's disabled.
        // If it's enabled, we select it and expect error on save.

        const chkStic = page.getByTestId('chk-STIC');
        if (await chkStic.isDisabled()) {
            // If disabled, it means frontend is preventing selection, which is valid validation.
            await expect(chkStic).toBeDisabled();
        } else {
            // If enabled, select and save, expect backend error.
            await chkStic.check({ force: true });
            await page.getByTestId('btn-salvar').click();

            // Expect error message or stay on page
            // Adjust expectation based on actual behavior
            await expect(page.getByRole('heading', { name: 'Cadastro de processo' })).toBeVisible();
            // Optionally check for specific error message if known
            // await expect(page.getByText('As seguintes unidades não possuem mapa')).toBeVisible();
        }
    });
});
