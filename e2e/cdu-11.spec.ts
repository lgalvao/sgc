import { test, expect } from '@playwright/test';
import { login } from './helpers/auth';
import { criarProcesso } from './helpers/processo-helpers';
import {
    navegarParaAtividades,
    adicionarAtividade,
    adicionarConhecimento,
    disponibilizarCadastro
} from './helpers/atividade-helpers';

test.describe('CDU-11 - Visualizar cadastro de atividades e conhecimentos', () => {
    const timestamp = Date.now();
    const nomeProcesso = `Processo CDU-11 ${timestamp}`;
    const siglaUnidade = 'SECAO_111';
    const gestorUsuario = '222222';
    const chefeUsuario = '333333';
    const senhaPadrao = 'senha';
    const descAtividade = `Atividade CDU-11 ${timestamp}`;
    const descConhecimento = `Conhecimento CDU-11 ${timestamp}`;

    test.beforeAll(async ({ browser }) => {
        let context = await browser.newContext();
        let page = await context.newPage();

        // 1. Criar Processo (Gestor)
        await page.goto('/');
        await login(page, gestorUsuario, senhaPadrao);
        await criarProcesso(page, {
            descricao: nomeProcesso,
            tipo: 'REVISAO',
            diasLimite: 5,
            unidade: siglaUnidade,
            iniciar: true
        });
        await context.close();

        // 2. Disponibilizar Cadastro (Chefe)
        context = await browser.newContext();
        page = await context.newPage();
        await page.goto('/');
        await login(page, chefeUsuario, senhaPadrao);
        await page.getByText(nomeProcesso).click();
        await navegarParaAtividades(page);
        await adicionarAtividade(page, descAtividade);
        await adicionarConhecimento(page, descAtividade, descConhecimento);
        await disponibilizarCadastro(page);
        await context.close();
    });

    test('Gestor deve visualizar atividades da unidade subordinada em modo somente leitura', async ({ page }) => {
        await page.goto('/');
        await login(page, gestorUsuario, senhaPadrao);

        // 1. Acessar processo
        await page.getByText(nomeProcesso).click();

        // 2. Clicar na unidade subordinada na lista de participantes
        await page.getByRole('link', { name: siglaUnidade }).click();

        // 3. Ir para Atividades
        await navegarParaAtividades(page, { visualizacao: true });

        // 4. Verificar conteúdo
        await expect(page.getByText(descAtividade)).toBeVisible();
        await expect(page.getByText(descConhecimento)).toBeVisible();

        // 5. Verificar modo somente leitura (ausência de botões de edição)
        await expect(page.getByTestId('btn-adicionar-atividade')).toBeHidden();

        const card = page.locator('.atividade-card', { has: page.getByText(descAtividade) });
        await expect(card.getByTestId('btn-editar-atividade')).toBeHidden();
        await expect(card.getByTestId('btn-remover-atividade')).toBeHidden();
    });

    test('Chefe deve visualizar suas atividades (após disponibilizar) em modo somente leitura', async ({ page }) => {
        await page.goto('/');
        await login(page, chefeUsuario, senhaPadrao);

        await page.getByText(nomeProcesso).click();

        // Já deve cair nos detalhes do subprocesso
        await navegarParaAtividades(page, { visualizacao: true });

        await expect(page.getByText(descAtividade)).toBeVisible();
        await expect(page.getByTestId('btn-adicionar-atividade')).toBeHidden();
    });
});
