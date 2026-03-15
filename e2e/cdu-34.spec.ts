import {expect, test} from './fixtures/complete-fixtures.js';
import {criarProcessoFixture} from './fixtures/fixtures-processos.js';
import {navegarParaSubprocesso, verificarPaginaPainel} from './helpers/helpers-navegacao.js';
import {TEXTOS} from '../frontend/src/constants/textos.js';

/**
 * CDU-34 - Enviar lembrete de prazo
 *
 * Ator: Sistema/ADMIN
 *
 * Fluxo principal (Envio manual):
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


    test('Preparacao: Admin cria e inicia processo', async ({page, request}) => {
        await criarProcessoFixture(request, {
            descricao: descProcesso,
            tipo: 'MAPEAMENTO',
            diasLimite: 5,
            unidade: UNIDADE_1,
            iniciar: true
        });

        await page.goto('/painel');
        await expect(page.getByTestId('tbl-processos').getByText(descProcesso).first()).toBeVisible();
        await verificarPaginaPainel(page);
    });


    test('Cenario principal: ADMIN envia lembrete e sistema registra histórico/alerta', async ({
                                                                                                   page
                                                                                               }) => {
        await page.getByTestId('tbl-processos').getByText(descProcesso).first().click();
        await expect(page.getByRole('heading', {name: /Unidades participantes/i})).toBeVisible();

        await expect(page.getByTestId('tbl-tree')).toBeVisible();
        await navegarParaSubprocesso(page, UNIDADE_1);

        const btnLembrete = page.getByTestId('btn-enviar-lembrete');
        await expect(btnLembrete).toBeVisible();
        await btnLembrete.click();

        await expect(page.getByTestId('txt-modelo-lembrete')).toBeVisible();
        await expect(page.getByTestId('txt-modelo-lembrete')).toContainText(TEXTOS.subprocesso.LEMBRETE_MODELO_PREFIXO(UNIDADE_1));
        await page.getByTestId('btn-confirmar-enviar-lembrete').click();

        await expect(page.getByTestId('tbl-movimentacoes')).toContainText(TEXTOS.movimentacao.LEMBRETE_ENVIADO);
    });

    test('Cenario complementar: unidade de destino visualiza alerta de lembrete no painel', async ({
                                                                                                        page
                                                                                                    }) => {
        const tabelaAlertas = page.getByTestId('tbl-alertas');
        await expect(tabelaAlertas).toBeVisible();
        await expect(tabelaAlertas).toContainText(descProcesso);
        await expect(tabelaAlertas).toContainText(/Lembrete/i);
    });
});
