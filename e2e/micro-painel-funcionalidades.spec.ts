import {expect, test} from './fixtures/complete-fixtures.js';
import {login, USUARIOS} from './helpers/helpers-auth.js';
import {criarProcesso} from './helpers/helpers-processos.js';
import {navegarParaSubprocesso} from './helpers/helpers-navegacao.js';
import type {Page} from '@playwright/test';

test.describe('Micro - Painel e navegação por perfil', () => {
    test('ADMIN deve exibir link "Unidades" e ocultar "Minha unidade"', async ({page, autenticadoComoAdmin}) => {
        await expect(page.getByRole('link', {name: /Unidades/i})).toBeVisible();
        await expect(page.getByRole('link', {name: /Minha unidade/i})).not.toBeVisible();
    });

    test('GESTOR deve exibir link "Minha unidade" e ocultar "Unidades"', async ({page, autenticadoComoGestor}) => {
        await expect(page.getByRole('link', {name: /Minha unidade/i})).toBeVisible();
        await expect(page.getByRole('link', {name: /Unidades/i})).not.toBeVisible();
    });

    test('ADMIN ao clicar processo "Criado" deve abrir cadastro do processo', async ({page, autenticadoComoAdmin, cleanupAutomatico}) => {
        const descricao = `Micro Painel Criado ${Date.now()}`;
        await criarProcesso(page, {
            descricao,
            tipo: 'MAPEAMENTO',
            diasLimite: 20,
            unidade: 'ASSESSORIA_12',
            expandir: ['SECRETARIA_1']
        });

        await page.getByTestId('tbl-processos').getByText(descricao).first().click();
        await expect(page).toHaveURL(/\/processo\/cadastro(?:\?codProcesso=\d+|\/\d+)/);

        const codigo = Number.parseInt(page.url().match(/codProcesso=(\d+)|\/cadastro\/(\d+)/)?.slice(1).find(Boolean) || '0');
        if (codigo > 0) cleanupAutomatico.registrar(codigo);
    });
});

test.describe.serial('Micro - Ordenação de alertas no painel', () => {
    const descricaoProcesso = `Micro Alertas ${Date.now()}`;
    const unidadeDestino = 'ASSESSORIA_22';
    
    async function criarProcessoIniciado(page: Page, descricao: string, unidade: string) {
        await criarProcesso(page, {
            descricao,
            tipo: 'MAPEAMENTO',
            diasLimite: 5,
            unidade,
            expandir: ['SECRETARIA_2', 'COORD_22'],
            iniciar: true
        });
    }

    test('Preparacao: criar processo iniciado e enviar lembrete para gerar alertas', async ({page, autenticadoComoAdmin}) => {
        await criarProcessoIniciado(page, descricaoProcesso, unidadeDestino);
        await page.getByTestId('tbl-processos').getByText(descricaoProcesso).first().click();
        await navegarParaSubprocesso(page, unidadeDestino);
        await page.getByTestId('btn-enviar-lembrete').click();
        await page.getByTestId('btn-confirmar-enviar-lembrete').click();
        await expect(page.getByTestId('tbl-movimentacoes')).toContainText('Lembrete de prazo enviado');
    });

    test('Ordem padrão deve ser Data/Hora desc (alerta mais novo primeiro)', async ({page, autenticadoComoChefeAssessoria22}) => {
        const primeiraLinha = page.getByTestId('tbl-alertas').locator('tbody tr').first();
        await expect(primeiraLinha).toContainText(/Lembrete/i);
        await expect(page.getByTestId('tbl-alertas')).toContainText(/Início do processo/i);
    });

    test('Ordenação por Processo deve alternar entre asc e desc', async ({page}) => {
        await login(page, USUARIOS.CHEFE_ASSESSORIA_22.titulo, USUARIOS.CHEFE_ASSESSORIA_22.senha);
        const cabecalhoProcesso = page.getByTestId('tbl-alertas').getByRole('columnheader', {name: 'Processo'});

        await cabecalhoProcesso.click();
        await expect(cabecalhoProcesso).toHaveAttribute('aria-sort', 'ascending');

        await cabecalhoProcesso.click();
        await expect(cabecalhoProcesso).toHaveAttribute('aria-sort', 'descending');
    });
});
