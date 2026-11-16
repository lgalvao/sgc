import {expect, Page, test} from '@playwright/test';
import {login} from './helpers';

const usuarioSedoc = {usuario: 'sedoc@tre-pe.jus.br', senha: 'admin123'};

test.describe('Processos', () => {
    test.setTimeout(3000)

    test.beforeEach(async ({page}) => {
        await login(page, usuarioSedoc.usuario, usuarioSedoc.senha);
    });

    test('deve listar processos em andamento', async ({page}) => {
        await page.goto('/');
        await expect(page.getByText('Processos em andamento')).toBeVisible();

        // Verifica se há pelo menos uma linha na tabela (exceto cabeçalho)
        const linhas = await page.locator('table tbody tr').count();
        expect(linhas).toBeGreaterThan(0);
    });

    test('deve visualizar detalhes de um processo', async ({page}) => {
        await page.goto('/');

        // Clica na primeira linha da tabela para visualizar o processo
        await page.locator('table tbody tr').first().click();
        await expect(page).toHaveURL(/processo\//);
        await expect(page.getByTestId('detalhes-processo-title')).toBeVisible();
        await expect(page.getByTestId('info-processo-title')).toBeVisible();
        await expect(page.getByTestId('estatisticas-processo-title')).toBeVisible();
        await expect(page.getByTestId('unidades-processo-title')).toBeVisible();
    });

    test('deve exibir formulário de novo processo (SEDOC)', async ({page}) => {
        await page.goto('http://localhost:8080/');
        await page.getByTestId('novo-processo-btn').click();
        await expect(page).toHaveURL(/processo\/novo/);
        await expect(page.getByTestId('novo-processo-title')).toBeVisible();
    });

    test('deve validar campos obrigatórios ao criar processo', async ({page}) => {
        await page.goto('http://localhost:8080/processo/novo');

        // Tenta submeter sem preencher campos
        await page.getByRole('button', {name: /iniciar processo/i}).click();

        // Espera mensagens de validação dos campos obrigatórios
        await expect(page.locator('.invalid-feedback')).toContainText([
            'Nome do processo é obrigatório',
            'Data limite é obrigatória'
        ]);
    });

    test('deve criar um novo processo de mapeamento', async ({page}) => {
        await page.goto('http://localhost:8080/');
        await page.getByTestId('novo-processo-btn').click();
        await expect(page).toHaveURL(/processo\/novo/);
        await expect(page.getByTestId('novo-processo-title')).toBeVisible();

        // Preenche o nome do processo
        const nomeProcesso = `Processo E2E ${Date.now()}`;
        await page.fill('input#nome', nomeProcesso);

        // Preenche a data limite (adiciona 7 dias ao dia atual)
        const data = new Date();
        data.setDate(data.getDate() + 7);
        const dataStr = data.toISOString().slice(0, 10); // yyyy-MM-ddTHH:mm
        await page.fill('input#dataLimite', dataStr);

        // Seleciona a primeira unidade participante, se houver
        const checkboxes = page.locator('input[type="checkbox"]');
        if (await checkboxes.count() > 0) {
            await checkboxes.first().check();
        }

        // Submete o formulário
        await page.getByRole('button', {name: /iniciar processo/i}).click();

        // Valida que foi redirecionado para a lista e o novo processo aparece na tabela
        await expect(page).toHaveURL('http://localhost:8080/');
        await expect(page.locator('table')).toContainText(nomeProcesso);
    });

    test('deve acessar e validar a página de detalhes do processo', async ({page}) => {
        await login(page, usuarioSedoc.usuario, usuarioSedoc.senha);
        await page.goto('http://localhost:8080/');

        // Pega o nome do processo mais recente criado pelo teste anterior
        const processoRow = page.locator('table tbody tr').first();
        const nomeProcesso = await processoRow.locator('td').first().textContent();

        // Clica na linha para ver os detalhes
        await processoRow.click();
        await expect(page).toHaveURL(/processo\//);
        await expect(page.getByTestId('detalhes-processo-title')).toBeVisible();
        await expect(page.getByTestId('info-processo-title')).toBeVisible();
        await expect(page.getByTestId('estatisticas-processo-title')).toBeVisible();
        await expect(page.getByTestId('unidades-processo-title')).toBeVisible();
        // Valida que o nome do processo aparece na página de detalhes
        await expect(page.locator('dd').first()).toContainText(nomeProcesso?.trim() || '');
    });
});

test.describe('Seleção em cascata de unidades no novo processo', () => {
    const usuarioSedoc = {usuario: 'sedoc@tre-pe.jus.br', senha: 'admin123'};
    let page: Page;

    test.beforeEach(async ({page: testPage}) => {
        page = testPage;

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