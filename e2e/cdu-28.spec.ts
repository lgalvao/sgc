import {expect, test} from './fixtures/complete-fixtures.js';

test.describe.serial('CDU-28 - Manter atribuição temporária', () => {
    const SIGLA_UNIDADE = 'SECRETARIA_2';

    async function acessarUnidadeAlvo(page: import('@playwright/test').Page) {
        await expect(page.getByTestId(`link-arvore-unidade-${SIGLA_UNIDADE}`)).toBeVisible();
        await page.getByTestId(`link-arvore-unidade-${SIGLA_UNIDADE}`).click();
    }

    test.beforeEach(async ({_resetAutomatico, _autenticadoComoAdmin, page}) => {
        await page.getByRole('link', {name: /Unidades/i}).click();
        await expect(page).toHaveURL(/\/unidades/);
    });

    test('Cenario 1: ADMIN acessa detalhes da unidade e opção de criar atribuição', async ({_resetAutomatico, _autenticadoComoAdmin, page}) => {
        await acessarUnidadeAlvo(page);
        await expect(page).toHaveURL(/\/unidade\/\d+$/);
        await expect(page.getByRole('heading', {name: new RegExp(SIGLA_UNIDADE)})).toBeVisible();
        await expect(page.getByTestId('unidade-view__btn-criar-atribuicao')).toBeVisible();
    });

    test('Cenario 2: Campos obrigatórios devem ser validados', async ({_resetAutomatico, _autenticadoComoAdmin, page}) => {
        await acessarUnidadeAlvo(page);
        await page.getByTestId('unidade-view__btn-criar-atribuicao').click();
        await expect(page).toHaveURL(/\/unidade\/\d+\/atribuicao$/);

        // Preenche campos
        await page.getByTestId('input-busca-usuario').fill('Admin');
        // Aguarda e seleciona o primeiro resultado da lista de pesquisa
        const opcaoUsuario = page.getByTestId(/opcao-usuario-/).first();
        await opcaoUsuario.waitFor({state: 'visible'});
        await opcaoUsuario.click();

        await page.getByTestId('textarea-justificativa').fill('Cobertura de férias');
        await page.getByTestId('input-data-termino').fill('2030-12-31');
        await page.getByTestId('cad-atribuicao__btn-criar-atribuicao').click();

        const dataInicioInvalida = await page.getByTestId('input-data-inicio').evaluate((el) => !(el as HTMLInputElement).checkValidity());
        expect(dataInicioInvalida).toBe(true);
    });

    test('Cenario 3: ADMIN cria atribuição temporária com sucesso', async ({_resetAutomatico, _autenticadoComoAdmin, page}) => {
        await acessarUnidadeAlvo(page);
        await page.getByTestId('unidade-view__btn-criar-atribuicao').click();
        await expect(page).toHaveURL(/\/unidade\/\d+\/atribuicao$/);

        // Preenche campos
        await page.getByTestId('input-busca-usuario').fill('Admin');
        const opcaoUsuario = page.getByTestId(/opcao-usuario-/).first();
        await opcaoUsuario.waitFor({state: 'visible'});
        await opcaoUsuario.click();

        await page.getByTestId('input-data-inicio').fill('2030-01-01');
        await page.getByTestId('input-data-termino').fill('2030-12-31');
        await page.getByTestId('textarea-justificativa').fill('Cobertura de férias');
        await page.getByTestId('cad-atribuicao__btn-criar-atribuicao').click();

        await expect(page.getByText(/Atribuição criada/i).first()).toBeVisible();
    });
});
