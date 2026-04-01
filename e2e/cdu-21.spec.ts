import {expect, test} from './fixtures/complete-fixtures.js';
import {
    criarProcessoMapaHomologadoFixture,
    criarProcessoMapaValidadoFixture,
    criarProcessoRevisaoMapaHomologadoFixture,
    validarProcessoFixture
} from './fixtures/fixtures-processos.js';
import {acessarDetalhesProcesso} from './helpers/helpers-processos.js';
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
        validarProcessoFixture(processo, descProcesso);
    });

    // TESTES PRINCIPAIS - CDU-21

    test('Cenario 1: ADMIN navega para detalhes do processo', async ({_resetAutomatico, page, _autenticadoComoAdmin}) => {
        // CDU-21: Passos 1-2

        await acessarDetalhesProcesso(page, descProcesso);

        await expect(page.getByRole('heading', {name: /Unidades participantes/i})).toBeVisible();

        // Botão finalizar visível
        await expect(page.getByTestId('btn-processo-finalizar')).toBeVisible();
    });

    test('Cenario 2: ADMIN cancela finalização - permanece na tela', async ({_resetAutomatico, page, _autenticadoComoAdmin}) => {


        await acessarDetalhesProcesso(page, descProcesso);

        await page.getByTestId('btn-processo-finalizar').click();

        const modal = page.getByRole('dialog');
        await expect(modal).toBeVisible();

        // CDU-21 Passo 6: verificar título, mensagem completa e botões do modal
        await expect(modal.getByRole('heading', {name: TEXTOS.processo.FINALIZACAO_TITULO})).toBeVisible();
        await expect(modal.getByText(TEXTOS.processo.FINALIZACAO_CONFIRMACAO_PREFIXO)).toBeVisible();
        await expect(modal.getByText(descProcesso)).toBeVisible();
        await expect(modal.getByText(TEXTOS.processo.FINALIZACAO_CONFIRMACAO_COMPLEMENTO)).toBeVisible();
        await expect(page.getByTestId('btn-finalizar-processo-cancelar')).toBeVisible();
        await expect(page.getByTestId('btn-finalizar-processo-confirmar')).toBeVisible();

        await page.getByTestId('btn-finalizar-processo-cancelar').click();

        // Permanece na tela de detalhes do processo
        await expect(page.getByRole('heading', {name: /Unidades participantes/i})).toBeVisible();
        await expect(page.getByTestId('btn-processo-finalizar')).toBeVisible();
    });

    test('Cenario 3: ADMIN finaliza processo com sucesso', async ({_resetAutomatico, page, _autenticadoComoAdmin}) => {
        // CDU-21: Passos 7-10


        await acessarDetalhesProcesso(page, descProcesso);

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

test.describe.serial('CDU-21 - Processo com mapas não homologados', () => {
    const UNIDADE_ALVO = 'SECAO_221';
    const timestamp = Date.now();
    const descProcessoErro = `Mapeamento CDU-21 Erro ${timestamp}`;

    test('Setup data: processo com mapa validado mas não homologado', async ({_resetAutomatico, request}) => {
        const processo = await criarProcessoMapaValidadoFixture(request, {
            descricao: descProcessoErro,
            unidade: UNIDADE_ALVO
        });
        validarProcessoFixture(processo, descProcessoErro);
    });

    test('Cenario 5: ADMIN não vê botão Finalizar quando mapas não estão todos homologados', async ({_resetAutomatico, page, _autenticadoComoAdmin}) => {
        // CDU-21 Passos 4-5: sistema verifica situação dos subprocessos e bloqueia finalização
        await acessarDetalhesProcesso(page, descProcessoErro);
        await expect(page.getByRole('heading', {name: /Unidades participantes/i})).toBeVisible();

        // Processo com mapa validado (não homologado): botão Finalizar não deve estar disponível
        await expect(page.getByTestId('btn-processo-finalizar')).toBeHidden();
    });
});

test.describe.serial('CDU-21 - Finalizar processo de REVISÃO', () => {
    const UNIDADE_ALVO = 'SECAO_212';

    const timestamp = Date.now();
    const descProcessoRevisao = `Revisão CDU-21 ${timestamp}`;
    let codProcessoRevisao: number;

    test('Setup: Criar processo de revisão com mapa homologado', async ({_resetAutomatico, request}) => {
        const processo = await criarProcessoRevisaoMapaHomologadoFixture(request, {
            descricao: descProcessoRevisao,
            unidade: UNIDADE_ALVO
        });
        codProcessoRevisao = processo.codigo;
        validarProcessoFixture(processo, descProcessoRevisao);
    });

    test('Cenario 1: ADMIN finaliza processo de revisão com sucesso', async ({_resetAutomatico, page, _autenticadoComoAdmin}) => {
        await acessarDetalhesProcesso(page, descProcessoRevisao);

        // Verificar que é processo de revisão
        await expect(page.getByText(/Revisão/i).first()).toBeVisible();
        await expect(page.getByTestId('btn-processo-finalizar')).toBeVisible();

        await page.getByTestId('btn-processo-finalizar').click();

        const modal = page.getByRole('dialog');
        await expect(modal).toBeVisible();
        await expect(modal.getByText(/Confirma a finalização/i)).toBeVisible();

        await page.getByTestId('btn-finalizar-processo-confirmar').click();

        await verificarPaginaPainel(page);
        await expect(page.getByText(TEXTOS.sucesso.PROCESSO_FINALIZADO)).toBeVisible();
    });

    test('Cenario 2: Verificar ausência de botões em processo de revisão finalizado', async ({_resetAutomatico, page, _autenticadoComoAdmin}) => {
        await page.goto(`/processo/${codProcessoRevisao}`);
        await expect(page.getByRole('heading', {name: /Unidades participantes/i})).toBeVisible();

        await expect(page.getByText(/Situação:\s*Finalizado/i)).toBeVisible();

        await expect(page.getByTestId('btn-processo-finalizar')).toBeHidden();

        await navegarParaSubprocesso(page, UNIDADE_ALVO);
        await expect(page.getByTestId('btn-enviar-lembrete')).toBeHidden();
        await expect(page.getByTestId('btn-reabrir-revisao')).toBeHidden();
    });
});
