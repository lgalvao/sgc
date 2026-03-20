import {expect, test} from './fixtures/complete-fixtures.js';
import {criarProcessoMapaHomologadoFixture} from './fixtures/fixtures-processos.js';
import {navegarParaSubprocesso, verificarPaginaPainel} from './helpers/helpers-navegacao.js';
import {TEXTOS} from '../frontend/src/constants/textos.js';

test.describe.serial('CDU-21 - Finalizar processo de mapeamento ou de revisão', () => {
    const UNIDADE_ALVO = 'SECAO_221';

    const timestamp = Date.now();
    const descProcesso = `Mapeamento CDU-21 ${timestamp}`;
    let codProcesso: number;

    test('Setup data', async ({_resetAutomatico, request}) => {
        const processo = await criarProcessoMapaHomologadoFixture(request, {
            descricao: descProcesso,
            unidade: UNIDADE_ALVO
        });
        codProcesso = processo.codigo;
        expect(true).toBeTruthy();
    });

    // TESTES PRINCIPAIS - CDU-21

    test('Cenario 1: ADMIN navega para detalhes do processo', async ({_resetAutomatico, page, _autenticadoComoAdmin}) => {
        // CDU-21: Passos 1-2


        await expect(page.getByTestId('tbl-processos').getByText(descProcesso).first()).toBeVisible();
        await page.getByTestId('tbl-processos').getByText(descProcesso).first().click();

        await expect(page.getByRole('heading', {name: /Unidades participantes/i})).toBeVisible();

        // Botão finalizar visível
        await expect(page.getByTestId('btn-processo-finalizar')).toBeVisible();
    });

    test('Cenario 2: ADMIN cancela finalização - permanece na tela', async ({_resetAutomatico, page, _autenticadoComoAdmin}) => {


        await page.getByTestId('tbl-processos').getByText(descProcesso).first().click();

        await page.getByTestId('btn-processo-finalizar').click();

        const modal = page.getByRole('dialog');
        await expect(modal).toBeVisible();
        await expect(modal.getByText(/Confirma a finalização/i)).toBeVisible();

        await page.getByTestId('btn-finalizar-processo-cancelar').click();

        // Permanece na tela de detalhes do processo
        await expect(page.getByRole('heading', {name: /Unidades participantes/i})).toBeVisible();
        await expect(page.getByTestId('btn-processo-finalizar')).toBeVisible();
    });

    test('Cenario 3: ADMIN finaliza processo com sucesso', async ({_resetAutomatico, page, _autenticadoComoAdmin}) => {
        // CDU-21: Passos 7-10


        await page.getByTestId('tbl-processos').getByText(descProcesso).first().click();

        await page.getByTestId('btn-processo-finalizar').click();

        const modal = page.getByRole('dialog');
        await expect(modal).toBeVisible();

        await page.getByTestId('btn-finalizar-processo-confirmar').click();

        await verificarPaginaPainel(page);
        await expect(page.getByText(TEXTOS.sucesso.PROCESSO_FINALIZADO)).toBeVisible();

        // Verificar que processo não aparece mais no painel ativo (foi finalizado)
        // (Processo finalizado não aparece na lista de processos ativos)
    });

    test('Cenario 4: Verificar ausência de botões em processo finalizado', async ({_resetAutomatico, page, _autenticadoComoAdmin}) => {
        // Garantir que botões de ação não aparecem para processos finalizados

        await page.goto(`/processo/${codProcesso}`);
        await expect(page.getByRole('heading', {name: /Unidades participantes/i})).toBeVisible();

        await expect(page.getByText(/Situação:\s*Finalizado/i)).toBeVisible();


        await expect(page.getByTestId('btn-processo-finalizar')).toBeHidden();


        await expect(page.getByTestId('btn-acao-bloco-aceitar')).toBeHidden();
        await expect(page.getByTestId('btn-acao-bloco-homologar')).toBeHidden();

        await navegarParaSubprocesso(page, 'SECAO_221');
        await expect(page.getByTestId('btn-enviar-lembrete')).toBeHidden();
        await expect(page.getByTestId('btn-reabrir-cadastro')).toBeHidden();
        await expect(page.getByTestId('btn-reabrir-revisao')).toBeHidden();
        await expect(page.getByTestId('btn-alterar-data-limite')).toBeHidden();


        await expect(page.getByTestId('card-subprocesso-atividades')).toBeHidden();
        // Deve aparecer o de visualização
        await expect(page.getByTestId('card-subprocesso-atividades-vis')).toBeVisible();
    });
});
