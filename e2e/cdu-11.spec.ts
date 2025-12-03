import { test, expect } from '@playwright/test';
import { login } from './helpers/auth';
import { criarProcesso } from './helpers/processo-helpers';
import {
    navegarParaAtividades,
    adicionarAtividade,
    adicionarConhecimento,
    disponibilizarCadastro
} from './helpers/atividade-helpers';

test.describe.serial('CDU-11 - Visualizar cadastro de atividades e conhecimentos', () => {
    const timestamp = Date.now();
    const nomeProcesso = `Processo CDU-11 ${timestamp}`;
    const unidadeSigla = 'SECAO_111';
    const gestorUsuario = '222222';
    const chefeUsuario = '333333';
    const senhaPadrao = 'senha';
    const atvDesc = `Atividade CDU-11 ${timestamp}`;
    const conhDesc = `Conhecimento CDU-11 ${timestamp}`;

    test.beforeAll(async ({ browser }) => {
        // 1. Criar Processo (Gestor)
        let context = await browser.newContext();
        let page = await context.newPage();
        await page.goto('/');
        await login(page, gestorUsuario, senhaPadrao);
        await criarProcesso(page, {
            descricao: nomeProcesso,
            tipo: 'REVISAO',
            diasLimite: 5,
            unidade: unidadeSigla,
            expandir: ['COORD_11']
        });
        await context.close();

        // 2. Disponibilizar Cadastro (Chefe)
        context = await browser.newContext();
        page = await context.newPage();
        await page.goto('/');
        await login(page, chefeUsuario, senhaPadrao);
        await page.getByText(nomeProcesso).click();
        await navegarParaAtividades(page);
        await adicionarAtividade(page, atvDesc);
        await adicionarConhecimento(page, atvDesc, conhDesc);
        await disponibilizarCadastro(page);
        await context.close();
    });

    test('Gestor deve visualizar atividades da unidade subordinada em modo somente leitura', async ({ page }) => {
        await page.goto('/');
        await login(page, gestorUsuario, senhaPadrao);

        // 1. Acessar processo
        await page.getByText(nomeProcesso).click();

        // 2. Clicar na unidade subordinada na lista de participantes
        // Assume que o nome da unidade é um link para os detalhes do subprocesso
        await page.getByRole('link', { name: unidadeSigla }).click();

        // 3. Ir para Atividades
        await navegarParaAtividades(page);

        // 4. Verificar conteúdo
        await expect(page.getByText(atvDesc)).toBeVisible();
        await expect(page.getByText(conhDesc)).toBeVisible();

        // 5. Verificar modo somente leitura (ausência de botões de edição)
        await expect(page.getByTestId('btn-adicionar-atividade')).toBeHidden();

        const card = page.locator('.atividade-card', { has: page.getByText(atvDesc) });
        await expect(card.getByTestId('btn-editar-atividade')).toBeHidden();
        await expect(card.getByTestId('btn-remover-atividade')).toBeHidden();
    });

    test('Chefe deve visualizar suas atividades (após disponibilizar) em modo somente leitura', async ({ page }) => {
        await page.goto('/');
        await login(page, chefeUsuario, senhaPadrao);

        await page.getByText(nomeProcesso).click();

        // Já deve cair nos detalhes do subprocesso
        await navegarParaAtividades(page);

        await expect(page.getByText(atvDesc)).toBeVisible();
        await expect(page.getByTestId('btn-adicionar-atividade')).toBeHidden();
    });
});
