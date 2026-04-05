import {expect, test} from './fixtures/complete-fixtures.js';
import {criarProcessoMapaValidadoFixture} from './fixtures/index.js';
import {login, loginComPerfil, USUARIOS} from './helpers/helpers-auth.js';
import {acessarDetalhesProcesso} from './helpers/helpers-processos.js';
import {fazerLogout, navegarParaSubprocesso, verificarPaginaPainel} from './helpers/helpers-navegacao.js';
import {resetDatabase} from './hooks/hooks-limpeza.js';
import {TEXTOS} from '../frontend/src/constants/textos.js';

/**
 * CDU-25 - Aceitar validação de mapas de competências em bloco
 *
 * Ator: GESTOR
 */
test.describe.serial('CDU-25 - Aceitar validação de mapas em bloco', () => {
    const UNIDADE_1 = 'SECAO_211';
    const descProcesso = `Mapeamento CDU-25 ${Date.now()}`;

    test('Cenários CDU-25: Aceite em bloco de mapas validados', async ({_resetAutomatico, request, page}) => {
        
        await test.step('Setup: Criar dados e realizar login', async () => {
            const processo = await criarProcessoMapaValidadoFixture(request, {
                unidade: UNIDADE_1,
                descricao: descProcesso
            });
            expect(processo.codigo).toBeGreaterThan(0);

            await login(page, USUARIOS.GESTOR_COORD_21.titulo, USUARIOS.GESTOR_COORD_21.senha);
            await acessarDetalhesProcesso(page, descProcesso);
            await expect(page.getByRole('heading', {name: /Unidades participantes/i})).toBeVisible();
        });

        await test.step('Cenario 1: GESTOR visualiza botões de ação em bloco', async () => {
            const btnAceitar = page.getByRole('button', {name: TEXTOS.acaoBloco.aceitar.ROTULO_VALIDACAO}).first();
            await expect(btnAceitar).toBeVisible();
            await expect(btnAceitar).toBeEnabled();
        });

        await test.step('Cenario 2: GESTOR abre modal e cancela o aceite', async () => {
            const btnAceitar = page.getByRole('button', {name: TEXTOS.acaoBloco.aceitar.ROTULO_VALIDACAO}).first();
            await btnAceitar.click();

            const modal = page.getByRole('dialog');
            await expect(modal).toBeVisible();
            await expect(modal.getByText(TEXTOS.acaoBloco.aceitar.TITULO_VALIDACAO)).toBeVisible();
            await expect(modal.getByRole('button', {name: /Cancelar/i})).toBeVisible();
            await expect(modal.getByRole('button', {name: TEXTOS.acaoBloco.aceitar.BOTAO})).toBeVisible();
            await modal.getByRole('button', {name: /Cancelar/i}).click();
            
            await expect(modal).toBeHidden();
            await expect(page.getByRole('heading', {name: /Unidades participantes/i})).toBeVisible();
        });

        await test.step('Cenario 3: GESTOR realiza aceite em bloco com sucesso', async () => {
            await page.getByRole('button', {name: TEXTOS.acaoBloco.aceitar.ROTULO_VALIDACAO}).first().click();

            const modal = page.getByRole('dialog');
            await expect(modal).toBeVisible();
            
            await expect(modal.getByText(TEXTOS.acaoBloco.aceitar.TITULO_VALIDACAO)).toBeVisible();
            
            const linhaUnidade = modal.locator('tr', { hasText: UNIDADE_1 });
            await expect(linhaUnidade).toBeVisible();
            await expect(linhaUnidade.locator('input[type="checkbox"]')).toBeChecked();

            await modal.getByRole('button', {name: TEXTOS.acaoBloco.aceitar.BOTAO}).click();

            await expect(page.getByText(TEXTOS.sucesso.MAPAS_ACEITOS_EM_BLOCO)).toBeVisible();
            await verificarPaginaPainel(page);
        });
    });

    test('Cenario 4: Aceite em bloco registra movimentação e alerta com data/hora', async ({
        _resetAutomatico,
        request,
        page
    }) => {
        // Reseta o banco para evitar conflito com unidade já em processo ativo dos cenários anteriores
        await resetDatabase(request);
        const descIsolada = `Mapeamento CDU-25 alerta ${Date.now()}`;
        const processoIsolado = await criarProcessoMapaValidadoFixture(request, {
            unidade: UNIDADE_1,
            descricao: descIsolada
        });
        expect(processoIsolado.codigo).toBeGreaterThan(0);

        await login(page, USUARIOS.GESTOR_COORD_21.titulo, USUARIOS.GESTOR_COORD_21.senha);
        await acessarDetalhesProcesso(page, descIsolada);

        const btnAceitar = page.getByRole('button', {name: TEXTOS.acaoBloco.aceitar.ROTULO_VALIDACAO}).first();
        await expect(btnAceitar).toBeVisible();
        await btnAceitar.click();

        const modal = page.getByRole('dialog');
        await expect(modal).toBeVisible();
        await modal.getByRole('button', {name: TEXTOS.acaoBloco.aceitar.BOTAO}).click();
        await expect(page.getByText(TEXTOS.sucesso.MAPAS_ACEITOS_EM_BLOCO)).toBeVisible();

        await fazerLogout(page);
        await loginComPerfil(
            page,
            USUARIOS.GESTOR_SECRETARIA_2.titulo,
            USUARIOS.GESTOR_SECRETARIA_2.senha,
            USUARIOS.GESTOR_SECRETARIA_2.perfil!
        );
        await page.goto(`/processo/${processoIsolado.codigo}`);
        await navegarParaSubprocesso(page, UNIDADE_1);

        const linhaMovimentacao = page.getByTestId('tbl-movimentacoes')
            .locator('tr', {hasText: TEXTOS.movimentacao.MAPA_VALIDACAO_ACEITA})
            .first();
        await expect(linhaMovimentacao).toBeVisible();
        await expect(linhaMovimentacao).toContainText(/\d{2}\/\d{2}\/\d{4}/);

        await page.goto('/painel');
        await verificarPaginaPainel(page);

        const tabelaAlertas = page.getByTestId('tbl-alertas');
        const linhaAlerta = tabelaAlertas.locator('tr', {
            hasText: /Validação do mapa da unidade SECAO_211 submetida para análise/i
        }).first();
        await expect(linhaAlerta).toBeVisible();
        await expect(linhaAlerta).toContainText(/SECAO_211/i);
        await expect(tabelaAlertas).toContainText(descIsolada);
        await expect(linhaAlerta).toContainText(/\d{2}\/\d{2}\/\d{4}/);
    });
});
