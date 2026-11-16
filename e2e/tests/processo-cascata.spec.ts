import {expect, Page, test} from '@playwright/test';

test.setTimeout(3000);

const usuarioSedoc = {usuario: 'sedoc@tre-pe.jus.br', senha: 'admin123'};
let page: Page;

test.describe('Seleção em cascata de unidades no novo processo (isolado)', () => {
    test.beforeEach(async ({page: testPage}) => {
        page = testPage;

        // Captura e exibe logs do console do navegador
        page.on('console', msg => {
            if (msg.type() === 'log') {
                console.log('[browser]', msg.text());
            }
        });

        // Login
        await page.goto('/login');
        await page.fill('input[name="username"]', usuarioSedoc.usuario);
        await page.fill('input[name="password"]', usuarioSedoc.senha);
        await page.click('button[type="submit"]');
        await expect(page).toHaveURL('/');
    });

    test('deve selecionar e desselecionar subordinadas ao marcar/desmarcar superior', async () => {
        await page.goto('/processo/novo');
        await expect(page.getByText('Novo processo de mapeamento')).toBeVisible();

        // Seleciona o primeiro checkbox de unidade superior
        const superior = page.locator('.unidade-checkbox').first();
        await superior.click(); // Usar click para disparar o evento JS
        await page.waitForTimeout(200); // Pequeno delay para o JS rodar

        // Seleciona todas as subordinadas diretas desse superior
        const idSuperior = await superior.getAttribute('data-id');
        const subordinadas = page.locator(`.unidade-checkbox[data-parent="${idSuperior}"]`);
        const count = await subordinadas.count();
        for (let i = 0; i < count; i++) {
            await expect(subordinadas.nth(i)).toBeChecked();
        }

        // Desmarca o superior
        await superior.click();
        await page.waitForTimeout(200);
        for (let i = 0; i < count; i++) {
            await expect(subordinadas.nth(i)).not.toBeChecked();
        }
    });

    test('deve exibir feedback visual ao tentar submeter sem nenhuma unidade marcada', async () => {
        await page.goto('/processo/novo');
        await expect(page.getByText('Novo processo de mapeamento')).toBeVisible();
        // Garante que nenhum checkbox está marcado
        const checkboxes = page.locator('.unidade-checkbox');
        const total = await checkboxes.count();
        for (let i = 0; i < total; i++) {
            if (await checkboxes.nth(i).isChecked()) {
                await checkboxes.nth(i).click();
            }
        }
        // Tenta submeter
        await page.getByTestId('iniciar-processo-btn').click();
        await page.waitForTimeout(200);
        // Espera feedback visual (classe is-invalid em pelo menos um checkbox)
        await expect(checkboxes.first()).toHaveClass(/is-invalid/);
    });
}); 