import { test, expect } from '@playwright/test';
import { login } from './helpers/auth';
import { criarProcesso } from './helpers/processo-helpers';
import {
    navegarParaAtividades,
    adicionarAtividade,
    disponibilizarCadastro
} from './helpers/atividade-helpers';

test.describe.serial('CDU-09 - Disponibilizar cadastro de atividades', () => {
    const timestamp = Date.now();
    const nomeProcesso = `Processo CDU-09 ${timestamp}`;
    const unidadeSigla = 'SECAO_111'; // Unidade gerida pelo CHEFE
    const gestorUsuario = '222222';
    const usuarioChefe = '333333';
    const senhaPadrao = 'senha';

    test.beforeAll(async ({ browser }) => {
        // Setup: Criar processo
        const context = await browser.newContext();
        const page = await context.newPage();

        await page.goto('/');
        await login(page, gestorUsuario, senhaPadrao);

        await criarProcesso(page, {
            descricao: nomeProcesso,
            tipo: 'MAPEAMENTO',
            diasLimite: 5,
            unidade: unidadeSigla,
            iniciar: true
        });
        await context.close();
    });

    test('Deve disponibilizar o cadastro após inserir atividades', async ({ page }) => {
        // 1. Login Chefe
        await page.goto('/');
        await login(page, usuarioChefe, senhaPadrao);

        // 2. Acessar Subprocesso
        await page.getByText(nomeProcesso).click();

        // 3. Ir para Atividades
        await navegarParaAtividades(page);

        // 4. Inserir uma atividade (pré-requisito)
        await adicionarAtividade(page, `Atividade para Disponibilizar ${timestamp}`);

        // 5. Disponibilizar
        await disponibilizarCadastro(page);

        // 6. Verificar Resultado
        await expect(page).toHaveURL(/\/painel/);

        // Voltar para o subprocesso
        await page.getByText(nomeProcesso).click();

        await expect(page.getByTestId('txt-situacao-subprocesso')).toHaveText(/Cadastro realizado/i);

        // Verificar timeline se possível
        await expect(page.getByTestId('timeline-subprocesso')).toContainText('Cadastro disponibilizado');
    });
});
