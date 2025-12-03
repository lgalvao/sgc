import { test, expect } from '@playwright/test';
import { login } from './helpers/auth';
import { criarProcesso } from './helpers/processo-helpers';
import {
    navegarParaAtividades,
    adicionarAtividade,
    adicionarConhecimento
} from './helpers/atividade-helpers';

test.describe.serial('CDU-12 - Verificar impactos no mapa de competências', () => {
    const timestamp = Date.now();
    const nomeProcesso = `Processo CDU-12 ${timestamp}`;
    const unidadeSigla = 'SECAO_111';
    const gestorUsuario = '222222';
    const chefeUsuario = '333333';
    const senhaPadrao = 'senha';

    test.beforeAll(async ({ browser }) => {
        const context = await browser.newContext();
        const page = await context.newPage();
        await page.goto('/');
        await login(page, gestorUsuario, senhaPadrao);
        await criarProcesso(page, {
            descricao: nomeProcesso,
            tipo: 'REVISAO',
            diasLimite: 5,
            unidade: unidadeSigla,
            iniciar: true
        });
        await context.close();
    });

    test('Deve exibir impactos ao adicionar nova atividade', async ({ page }) => {
        await page.goto('/');
        await login(page, chefeUsuario, senhaPadrao);

        await page.getByText(nomeProcesso).click();
        await navegarParaAtividades(page);

        // Adicionar atividade para gerar impacto
        const novaAtividade = `Atividade Nova Impacto ${timestamp}`;
        const novoConhecimento = `Conhecimento Novo ${timestamp}`;
        await adicionarAtividade(page, novaAtividade);
        await adicionarConhecimento(page, novaAtividade, novoConhecimento);

        // Clicar em Impactos no Mapa
        await page.getByTestId('btn-impactos-mapa').click();

        // Verificar Modal
        await expect(page.getByRole('dialog')).toBeVisible();
        await expect(page.getByRole('heading', { name: 'Impacto no Mapa de Competências' })).toBeVisible();

        // Verificar se a nova atividade está listada como inserida
        // CDU-12: "7.1. Atividades inseridas... enumerando... os elementos da lista"
        await expect(page.getByText('Atividades inseridas')).toBeVisible();
        await expect(page.getByText(novaAtividade)).toBeVisible();
        await expect(page.getByText(novoConhecimento)).toBeVisible();

        // Fechar modal
        await page.getByRole('button', { name: 'Fechar' }).click();
        await expect(page.getByRole('dialog')).toBeHidden();
    });
});
