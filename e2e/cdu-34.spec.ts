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
 * 5. Sistema exibe modal de confirmação
 * 6. ADMIN confirma envio
 * 7. Sistema envia e-mail e cria alerta para a unidade
 */
test.describe.serial('CDU-34 - Enviar lembrete de prazo', () => {
    const UNIDADE_1 = 'ASSESSORIA_22';

    const timestamp = Date.now();
    const descProcesso = `Mapeamento CDU-34 ${timestamp}`;

    test('Preparacao: Admin cria e inicia processo', async ({_resetAutomatico, page, request, _autenticadoComoAdmin}) => {
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


    test('Cenario principal: ADMIN envia lembrete e sistema cria alerta sem alterar o workflow', async ({
                                                                                                    _resetAutomatico,
                                                                                                    page,
                                                                                                    _autenticadoComoAdmin
}) => {
        await page.getByTestId('tbl-processos').getByText(descProcesso).first().click();
        await expect(page.getByRole('heading', {name: /Unidades participantes/i})).toBeVisible();

        await expect(page.getByTestId('tbl-tree')).toBeVisible();
        await navegarParaSubprocesso(page, UNIDADE_1);

        const situacaoAntes = await page.getByTestId('subprocesso-header__txt-situacao').innerText();
        const localizacaoAntes = await page.getByTestId('subprocesso-header__txt-localizacao').innerText();
        const btnLembrete = page.getByTestId('btn-enviar-lembrete');
        await expect(btnLembrete).toBeVisible();
        await btnLembrete.click();

        const modal = page.getByRole('dialog');
        await expect(modal.getByRole('heading', {name: TEXTOS.subprocesso.LEMBRETE_TITULO})).toBeVisible();
        await expect(page.getByTestId('txt-modelo-lembrete')).toBeVisible();
        await expect(page.getByTestId('txt-modelo-lembrete')).toContainText(TEXTOS.subprocesso.LEMBRETE_MODELO_PREFIXO(UNIDADE_1));
        await modal.getByRole('button', {name: /Cancelar/i}).click();
        await expect(modal).toBeHidden();

        await btnLembrete.click();
        await expect(modal.getByRole('heading', {name: TEXTOS.subprocesso.LEMBRETE_TITULO})).toBeVisible();
        await expect(page.getByTestId('txt-modelo-lembrete')).toContainText(TEXTOS.subprocesso.LEMBRETE_MODELO_PREFIXO(UNIDADE_1));
        await page.getByTestId('btn-confirmar-enviar-lembrete').click();

        await expect(page.getByText(TEXTOS.subprocesso.SUCESSO_LEMBRETE_ENVIADO).first()).toBeVisible();
        await expect(page.getByTestId('tbl-movimentacoes')).not.toContainText(/Lembrete de prazo enviado/i);
        await expect(page.getByTestId('btn-enviar-lembrete')).toBeVisible();
        await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(situacaoAntes);
        await expect(page.getByTestId('subprocesso-header__txt-localizacao')).toHaveText(localizacaoAntes);
    });

    test('Cenario complementar: unidade de destino visualiza alerta de lembrete no painel', async ({
                                                                                                        _resetAutomatico,
                                                                                                        page,
                                                                                                        _autenticadoComoChefeAssessoria22
}) => {
        const tabelaAlertas = page.getByTestId('tbl-alertas');
        await expect(tabelaAlertas).toBeVisible();
        await expect(tabelaAlertas).toContainText(descProcesso);
        await expect(tabelaAlertas).toContainText(new RegExp(`Lembrete: Prazo do processo ${descProcesso} encerra em [0-9]{2}/[0-9]{2}/[0-9]{4}`));
    });
});
