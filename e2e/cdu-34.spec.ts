import {expect, test} from './fixtures/complete-fixtures.js';
import {criarProcesso} from './helpers/helpers-processos.js';
import {navegarParaSubprocesso, verificarPaginaPainel} from './helpers/helpers-navegacao.js';

/**
 * CDU-34 - Enviar lembrete de prazo
 *
 * Ator: Sistema/ADMIN
 *
 * Fluxo principal (Envio Manual):
 * 1. ADMIN acessa tela de Acompanhamento de Processos
 * 2. Sistema exibe processos com indicadores de prazo
 * 3. ADMIN seleciona unidades com pendências
 * 4. ADMIN aciona "Enviar lembrete"
 * 5. Sistema exibe modelo da mensagem
 * 6. ADMIN confirma envio
 * 7. Sistema envia e-mail e registra no histórico
 */
test.describe.serial('CDU-34 - Enviar lembrete de prazo', () => {
    const UNIDADE_1 = 'ASSESSORIA_22';

    const timestamp = Date.now();
    const descProcesso = `Mapeamento CDU-34 ${timestamp}`;
    let processoId: number;

    // ========================================================================
    // PREPARAÇÃO
    // ========================================================================

    test('Preparacao: Admin cria e inicia processo', async ({page, autenticadoComoAdmin, cleanupAutomatico}) => {
        await criarProcesso(page, {
            descricao: descProcesso,
            tipo: 'MAPEAMENTO', diasLimite: 5,
            unidade: UNIDADE_1,
            expandir: ['SECRETARIA_2', 'COORD_22']
        });

        const linhaProcesso = page.getByTestId('tbl-processos').locator('tr', {has: page.getByText(descProcesso)});
        await linhaProcesso.click();

        processoId = Number.parseInt(new RegExp(/\/processo\/cadastro\/(\d+)/).exec(page.url())?.[1] || '0');
        if (processoId > 0) cleanupAutomatico.registrar(processoId);

        await page.getByTestId('btn-processo-iniciar').click();
        await page.getByTestId('btn-iniciar-processo-confirmar').click();

        await verificarPaginaPainel(page);
    });

    // ========================================================================
    // TESTES PRINCIPAIS
    // ========================================================================

    test('Cenario principal: ADMIN envia lembrete e sistema registra histórico/alerta', async ({
                                                                                                   page,
                                                                                                   autenticadoComoAdmin
                                                                                               }) => {
        await page.getByTestId('tbl-processos').getByText(descProcesso).first().click();
        await expect(page.getByRole('heading', {name: /Unidades participantes/i})).toBeVisible();

        await expect(page.getByTestId('tbl-tree')).toBeVisible();
        await navegarParaSubprocesso(page, UNIDADE_1);

        const btnLembrete = page.getByTestId('btn-enviar-lembrete');
        await expect(btnLembrete).toBeVisible();
        await btnLembrete.click();

        await expect(page.getByTestId('txt-modelo-lembrete')).toBeVisible();
        await expect(page.getByTestId('txt-modelo-lembrete')).toContainText('Este lembrete será enviado');
        await page.getByTestId('btn-confirmar-enviar-lembrete').click();

        await expect(page.getByText('Lembrete enviado').first()).toBeVisible();
        await expect(page.getByTestId('tbl-movimentacoes')).toContainText('Lembrete de prazo enviado');
    });

    test('Cenario complementar: unidade de destino visualiza alerta de lembrete no painel', async ({
                                                                                                       page,
                                                                                                       autenticadoComoChefeAssessoria22
                                                                                                   }) => {
        const tabelaAlertas = page.getByTestId('tbl-alertas');
        await expect(tabelaAlertas).toBeVisible();
        await expect(tabelaAlertas).toContainText(descProcesso);
        await expect(tabelaAlertas).toContainText(/Lembrete/i);
    });
});
