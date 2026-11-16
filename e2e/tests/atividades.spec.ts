import {expect, Page, test} from '@playwright/test';
import {faker} from '@faker-js/faker/locale/pt_BR';

const usuarioSedoc = {usuario: 'sedoc@tre-pe.jus.br', senha: 'admin123'};

const generateTestData = () => ({
    atividade1: `Atividade ${faker.lorem.words(3)}`,
    conhecimento1: `Conhecimento ${faker.lorem.words(2)}`,
    conhecimento2: `Conhecimento ${faker.lorem.words(2)}`,
});

const navegarParaAtividades = async (page: Page) => {
    // Navega para a lista de processos
    await page.goto('/');

    // Aguarda a tabela de processos carregar
    await expect(page.getByRole('heading', {name: 'Processos em andamento'})).toBeVisible();

    // Verifica se há processos na tabela e clica no primeiro
    const primeiroProcesso = page.locator('table tbody tr').first();
    await expect(primeiroProcesso).toBeVisible();

    // Clica no link do primeiro processo (primeira célula da linha)
    await primeiroProcesso.locator('td').first().click();

    // Verifica se a página de detalhes do processo foi carregada
    await expect(page).toHaveURL(/processo\//);

    // Aguarda a tabela de unidades carregar e verifica se há unidades
    await expect(page.locator('table tbody tr.clickable-row').first()).toBeVisible();

    // Clica na primeira unidade da lista
    await page.locator('table tbody tr.clickable-row').first().click();

    // Verifica se a página de atividades foi carregada
    await expect(page).toHaveURL(/processo-unidade\/[^/]+\/atividades/);
};

test.describe('Cadastro de Atividades', () => {
    let testData: ReturnType<typeof generateTestData>;
    let page: Page;

    test.beforeEach(async ({page: testPage}) => {
        page = testPage;
        testData = generateTestData();

        // Faz login
        await page.goto('/login');
        await page.fill('input[name="username"]', usuarioSedoc.usuario);
        await page.fill('input[name="password"]', usuarioSedoc.senha);
        await page.click('button[type="submit"]');

        // Aguarda o redirecionamento após o login e navega para atividades
        await expect(page).toHaveURL('/');
        await navegarParaAtividades(page);
    });

    test('deve cadastrar uma nova atividade', async () => {
        const {atividade1} = testData;

        await test.step('Adicionar nova atividade', async () => {
            // Clica no botão para adicionar nova atividade
            await page.click('button[value="iniciarAdicionarAtividade"]');

            // Preenche a descrição da atividade
            await page.fill('input#novaAtividadeDescricao', atividade1);

            // Clica em Salvar
            await page.click('button[value="salvarNovaAtividade"]');
        });

        await test.step('Verificar se a atividade foi adicionada', async () => {
            // Encontra o input da atividade que acabamos de adicionar
            await expect(page.locator(`input[value="${atividade1}"]`).first()).toBeVisible();
        });
    });

    test('deve adicionar conhecimento a uma atividade', async () => {
        const {atividade1, conhecimento1} = testData;

        // Primeiro adiciona uma atividade
        await test.step('Adicionar nova atividade', async () => {
            await page.click('button[value="iniciarAdicionarAtividade"]');
            await page.fill('input#novaAtividadeDescricao', atividade1);
            await page.click('button[value="salvarNovaAtividade"]');
        });

        await test.step('Adicionar conhecimento à atividade', async () => {
            // Clica em Adicionar Conhecimento
            await page.click('a[data-testid^="btn-adicionar-conhecimento-"]');

            // Preenche o conhecimento
            await page.fill('input[placeholder*="conhecimento"]', conhecimento1);

            // Clica em Salvar para adicionar o conhecimento
            await page.click('button:has-text("Salvar")');

        });

        await test.step('Verificar se o conhecimento foi adicionado', async () => {
            // Verifica se o conhecimento aparece na lista
            await expect(page.getByText(conhecimento1)).toBeVisible();
        });
    });

    test('deve adicionar múltiplos conhecimentos a uma atividade', async () => {
        const {atividade1, conhecimento1, conhecimento2} = testData;

        // Adiciona uma atividade
        await test.step('Adicionar nova atividade', async () => {
            await page.click('button[value="iniciarAdicionarAtividade"]');
            await page.fill('input#novaAtividadeDescricao', atividade1);
            await page.click('button[value="salvarNovaAtividade"]');
        });

        // Adiciona o primeiro conhecimento
        await test.step('Adicionar primeiro conhecimento', async () => {
            await page.click('a[data-testid^="btn-adicionar-conhecimento-"]');
            await page.fill('input[placeholder*="conhecimento"]', conhecimento1);
            await page.click('button:has-text("Salvar")');
        });

        // Adiciona o segundo conhecimento
        await test.step('Adicionar segundo conhecimento', async () => {
            await page.click('a[data-testid^="btn-adicionar-conhecimento-"]');
            await page.fill('input[placeholder*="conhecimento"]', conhecimento2);
            await page.click('button:has-text("Salvar")');
        });

        // Verifica se ambos os conhecimentos foram adicionados
        await test.step('Verificar conhecimentos adicionados', async () => {
            await expect(page.getByText(conhecimento1)).toBeVisible();
            await expect(page.getByText(conhecimento2)).toBeVisible();
        });
    });
});