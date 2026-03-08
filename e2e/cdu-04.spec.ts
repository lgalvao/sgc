import {expect, test} from './fixtures/complete-fixtures.js';
import {
    criarProcesso,
    extrairProcessoId,
    verificarDetalhesProcesso,
    verificarProcessoNaTabela
} from './helpers/helpers-processos.js';
import {
    esperarPaginaCadastroProcesso,
    esperarPaginaDetalhesProcesso,
    esperarPaginaPainel,
    esperarPaginaSubprocesso,
    verificarToast
} from './helpers/helpers-navegacao.js';
import {login, loginComPerfil, USUARIOS} from './helpers/helpers-auth.js';
import type {Page} from '@playwright/test';

test.describe('CDU-04 - Iniciar Processo', () => {

    test('Deve iniciar um processo e validar criação de subprocessos e alertas', async ({
                                                                                            page,
                                                                                            browser,
                                                                                            autenticadoComoAdmin,
                                                                                            cleanupAutomatico
                                                                                        }: {
        page: Page,
        browser: any,
        autenticadoComoAdmin: void,
        cleanupAutomatico: any
    }) => {
        const descricao = `CDU-04 Iniciar - ${Date.now()}`;
        // 1. Criar processo como ADMIN
        await criarProcesso(page, {
            descricao: descricao,
            tipo: 'MAPEAMENTO',
            diasLimite: 15,
            unidade: ['SECRETARIA_1', 'ASSESSORIA_11'], // Interoperacional + Operacional
            expandir: ['SECRETARIA_1'],
            iniciar: false
        });

        // Capturar ID
        await page.getByTestId('tbl-processos').getByText(descricao).first().click();
        await esperarPaginaCadastroProcesso(page);
        const processoId = await extrairProcessoId(page);
        cleanupAutomatico.registrar(processoId);
        const dataLimiteStr = await page.getByTestId('inp-processo-data-limite').inputValue();

        // 2. Iniciar processo
        await page.getByTestId('btn-processo-iniciar').click();
        const modal = page.getByRole('dialog');
        await expect(modal.getByText(/Ao iniciar o processo, não será mais possível editá-lo ou removê-lo/i)).toBeVisible();
        await page.getByTestId('btn-iniciar-processo-confirmar').click();

        // Aguarda toast e redirect
        await esperarPaginaPainel(page);
        await verificarToast(page, /iniciado com sucesso/i);
        await verificarProcessoNaTabela(page, {
            descricao,
            situacao: 'Em andamento',
            tipo: 'Mapeamento'
        });

        // 3. Verificar Detalhes do Processo (Snapshot e Subprocessos)
        await page.getByTestId('tbl-processos').locator('tr', {has: page.getByText(descricao)}).click();
        await esperarPaginaDetalhesProcesso(page, processoId);

        await verificarDetalhesProcesso(page, {
            descricao: descricao,
            tipo: 'Mapeamento',
            situacao: 'Em andamento'
        });

        // 4. Validar dados iniciais dos subprocessos (Step 9)
        const linhaAss11 = page.locator('tr', {hasText: 'ASSESSORIA_11'}).first();
        await expect(linhaAss11).toContainText('Não iniciado');
        // Verifica se a data limite foi copiada (Step 9)
        await expect(linhaAss11).toContainText(dataLimiteStr.split('-').reverse().join('/'));

        // 5. Validar Movimentações (Step 11)
        await linhaAss11.click();
        await esperarPaginaSubprocesso(page, 'ASSESSORIA_11');
        await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText('Não iniciado');
        
        const timeline = page.getByTestId('tbl-movimentacoes');
        await expect(timeline.getByText(/Processo iniciado/i)).toBeVisible();

        // 6. Validar Alertas (Step 14) para Unidade Operacional
        const contextoChefeAss11 = await browser.newContext();
        const paginaChefeAss11 = await contextoChefeAss11.newPage();
        await login(paginaChefeAss11, USUARIOS.CHEFE_ASSESSORIA_11.titulo, USUARIOS.CHEFE_ASSESSORIA_11.senha);
        
        const tabelaAlertasAss11 = paginaChefeAss11.getByTestId('tbl-alertas');
        await expect(tabelaAlertasAss11.locator('tr', {hasText: descricao})
            .filter({hasText: 'Início do processo'})
            .filter({hasNotText: 'subordinada'})
        ).toBeVisible();
        await contextoChefeAss11.close();

        // 7. Validar Alertas (Step 14) para Unidade Interoperacional (SECRETARIA_1)
        // John Lennon (SECRETARIA_1) deve receber alertas tanto como Chefe quanto como Gestor

        // 7.1. Como CHEFE (Unidade Operacional)
        const contextoChefeSec1 = await browser.newContext();
        const paginaChefeSec1 = await contextoChefeSec1.newPage();
        await loginComPerfil(paginaChefeSec1, USUARIOS.CHEFE_SECRETARIA_1.titulo, USUARIOS.CHEFE_SECRETARIA_1.senha, USUARIOS.CHEFE_SECRETARIA_1.perfil);

        const tabelaAlertasSec1Chefe = paginaChefeSec1.getByTestId('tbl-alertas');
        await expect(tabelaAlertasSec1Chefe.locator('tr', {hasText: descricao})
            .filter({hasText: 'Início do processo'})
            .filter({hasNotText: 'subordinada'})
        ).toBeVisible();
        await contextoChefeSec1.close();

        // 7.2. Como GESTOR (Unidade Intermediária) - deve ver alerta sobre subordinada (ASSESSORIA_11)
        const contextoGestorSec1 = await browser.newContext();
        const paginaGestorSec1 = await contextoGestorSec1.newPage();
        await loginComPerfil(paginaGestorSec1, USUARIOS.GESTOR_SECRETARIA_1.titulo, USUARIOS.GESTOR_SECRETARIA_1.senha, USUARIOS.GESTOR_SECRETARIA_1.perfil);

        const tabelaAlertasSec1Gestor = paginaGestorSec1.getByTestId('tbl-alertas');
        await expect(tabelaAlertasSec1Gestor.locator('tr', {hasText: descricao})
            .filter({hasText: 'Início do processo em unidade(s) subordinada(s)'})
        ).toBeVisible();
        await contextoGestorSec1.close();    });
});
