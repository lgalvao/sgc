import {expect, test} from './fixtures/complete-fixtures.js';
import {criarProcesso} from './helpers/helpers-processos.js';
import {navegarParaSubprocesso, verificarPaginaPainel} from './helpers/helpers-navegacao.js';

test.describe.serial('CDU-02 - Alertas no painel (leitura)', () => {
    const UNIDADE_DESTINO = 'ASSESSORIA_22';
    const timestamp = Date.now();
    const descricaoProcesso = `Mapeamento CDU-02 Alertas ${timestamp}`;

    test('Preparacao: ADMIN cria processo e envia lembrete', async ({page, autenticadoComoAdmin, cleanupAutomatico}) => {
        await criarProcesso(page, {
            descricao: descricaoProcesso,
            tipo: 'MAPEAMENTO',
            diasLimite: 5,
            unidade: UNIDADE_DESTINO,
            expandir: ['SECRETARIA_2', 'COORD_22']
        });

        const linhaProcesso = page.getByTestId('tbl-processos').locator('tr', {has: page.getByText(descricaoProcesso)});
        await linhaProcesso.click();

        const processoId = Number.parseInt(new RegExp(/\/processo\/cadastro\/(\d+)/).exec(page.url())?.[1] || '0');
        if (processoId > 0) cleanupAutomatico.registrar(processoId);

        await page.getByTestId('btn-processo-iniciar').click();
        await page.getByTestId('btn-iniciar-processo-confirmar').click();
        await verificarPaginaPainel(page);

        await page.getByTestId('tbl-processos').getByText(descricaoProcesso).first().click();
        await navegarParaSubprocesso(page, UNIDADE_DESTINO);
        await page.getByTestId('btn-enviar-lembrete').click();
        await page.getByTestId('btn-confirmar-enviar-lembrete').click();
        await expect(page.getByTestId('tbl-movimentacoes')).toContainText('Lembrete de prazo enviado');
    });

    test('Deve exibir alerta novo em negrito para usuário de destino', async ({page, autenticadoComoChefeAssessoria22}) => {
        const linhaAlerta = page.getByTestId('tbl-alertas').locator('tr', {has: page.getByText(descricaoProcesso)}).first();
        await expect(linhaAlerta).toBeVisible();
        await expect(linhaAlerta).toHaveClass(/fw-bold/);
    });

    test('Deve marcar alerta como visualizado após primeira visualização', async ({page, autenticadoComoChefeAssessoria22}) => {
        await page.reload();
        const linhaAlerta = page.getByTestId('tbl-alertas').locator('tr', {has: page.getByText(descricaoProcesso)}).first();
        await expect(linhaAlerta).toBeVisible();
        await expect(linhaAlerta).not.toHaveClass(/fw-bold/);
    });
});
