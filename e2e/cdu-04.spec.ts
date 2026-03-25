import {expect, test} from './fixtures/complete-fixtures.js';
import {
    acessarDetalhesProcesso,
    criarProcesso,
    extrairProcessoCodigo,
    verificarDetalhesProcesso,
    verificarProcessoNaTabela
} from './helpers/helpers-processos.js';
import {
    esperarPaginaCadastroProcesso,
    esperarPaginaDetalhesProcesso,
    esperarPaginaPainel,
    esperarPaginaSubprocesso,
    navegarParaSubprocesso,
    verificarToast
} from './helpers/helpers-navegacao.js';
import {login, loginComPerfil, USUARIOS} from './helpers/helpers-auth.js';
import {TEXTOS} from '../frontend/src/constants/textos.js';

test.describe('CDU-04 - Iniciar processo', () => {

    test('Deve iniciar um processo e validar criação de subprocessos e alertas', async ({
                                                                                            _resetAutomatico,
                                                                                            page,
                                                                                            browser,
                                                                                            _autenticadoComoAdmin
}) => {
        const descricao = `CDU-04 Iniciar - ${Date.now()}`;
        await criarProcesso(page, {
            descricao: descricao,
            tipo: 'MAPEAMENTO',
            diasLimite: 15,
            unidade: ['SECRETARIA_1', 'ASSESSORIA_11'], // Interoperacional + Operacional
            expandir: ['SECRETARIA_1'],
            iniciar: false
        });

        await acessarDetalhesProcesso(page, descricao);
        await esperarPaginaCadastroProcesso(page);
        const codProcesso = await extrairProcessoCodigo(page);

        const dataLimiteStr = await page.getByTestId('inp-processo-data-limite').inputValue();

        await page.getByTestId('btn-processo-iniciar').click();
        const modal = page.getByRole('dialog');
        await expect(modal.getByText(TEXTOS.processo.cadastro.INICIAR_CONFIRMACAO)).toBeVisible();
        await page.getByTestId('btn-iniciar-processo-confirmar').click();

        // Aguarda toast e redirect
        await esperarPaginaPainel(page);
        await verificarToast(page, TEXTOS.sucesso.PROCESSO_INICIADO);
        await verificarProcessoNaTabela(page, {
            descricao,
            situacao: 'Em andamento',
            tipo: 'Mapeamento'
        });

        await page.getByTestId('tbl-processos').locator('tr', {has: page.getByText(descricao)}).click();
        await esperarPaginaDetalhesProcesso(page, codProcesso);

        await verificarDetalhesProcesso(page, {
            descricao: descricao,
            tipo: 'Mapeamento',
            situacao: 'Em andamento'
        });

        const linhaAss11 = page.locator('tr', {hasText: 'ASSESSORIA_11'}).first();
        await expect(linhaAss11).toContainText('Não iniciado');
        await expect(linhaAss11).toContainText(dataLimiteStr.split('-').reverse().join('/'));

        await navegarParaSubprocesso(page, 'ASSESSORIA_11');
        await esperarPaginaSubprocesso(page, 'ASSESSORIA_11');
        await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText('Não iniciado');

        
        const timeline = page.getByTestId('tbl-movimentacoes');
        await expect(timeline.getByText(TEXTOS.movimentacao.PROCESSO_INICIADO)).toBeVisible();

        const contextoChefeAss11 = await browser.newContext();
        const paginaChefeAss11 = await contextoChefeAss11.newPage();
        await login(paginaChefeAss11, USUARIOS.CHEFE_ASSESSORIA_11.titulo, USUARIOS.CHEFE_ASSESSORIA_11.senha);
        
        const tabelaAlertasAss11 = paginaChefeAss11.getByTestId('tbl-alertas');
        await expect(tabelaAlertasAss11.locator('tr', {hasText: descricao})
            .filter({hasText: 'Início do processo'})
            .filter({hasNotText: 'subordinada'})
        ).toBeVisible();
        await contextoChefeAss11.close();

        // John lennon (SECRETARIA_1) deve receber alertas tanto como Chefe quanto como Gestor

        // 7.1. Como CHEFE (Unidade operacional)
        const contextoChefeSec1 = await browser.newContext();
        const paginaChefeSec1 = await contextoChefeSec1.newPage();
        await loginComPerfil(paginaChefeSec1, USUARIOS.CHEFE_SECRETARIA_1.titulo, USUARIOS.CHEFE_SECRETARIA_1.senha, USUARIOS.CHEFE_SECRETARIA_1.perfil);

        const tabelaAlertasSec1Chefe = paginaChefeSec1.getByTestId('tbl-alertas');
        await expect(tabelaAlertasSec1Chefe.locator('tr', {hasText: descricao})
            .filter({hasText: 'Início do processo'})
            .filter({hasNotText: 'subordinada'})
        ).toBeVisible();
        await contextoChefeSec1.close();

        // 7.2. Como GESTOR (Unidade intermediária) - deve ver alerta sobre subordinada (ASSESSORIA_11)
        const contextoGestorSec1 = await browser.newContext();
        const paginaGestorSec1 = await contextoGestorSec1.newPage();
        await loginComPerfil(paginaGestorSec1, USUARIOS.GESTOR_SECRETARIA_1.titulo, USUARIOS.GESTOR_SECRETARIA_1.senha, USUARIOS.GESTOR_SECRETARIA_1.perfil);

        const tabelaAlertasSec1Gestor = paginaGestorSec1.getByTestId('tbl-alertas');
        await expect(tabelaAlertasSec1Gestor.locator('tr', {hasText: descricao})
            .filter({hasText: 'Início do processo em unidade(s) subordinada(s)'})
        ).toBeVisible();
        await contextoGestorSec1.close();    });
});
