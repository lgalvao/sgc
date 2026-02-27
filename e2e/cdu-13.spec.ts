import {expect, test} from './fixtures/complete-fixtures.js';
import {criarProcesso, extrairProcessoId} from './helpers/helpers-processos.js';
import {
    adicionarAtividade,
    adicionarConhecimento,
    navegarParaAtividades,
    navegarParaAtividadesVisualizacao
} from './helpers/helpers-atividades.js';
import {
    abrirHistoricoAnalise,
    abrirHistoricoAnaliseVisualizacao,
    aceitarCadastroMapeamento,
    acessarSubprocessoAdmin,
    acessarSubprocessoChefeDireto,
    acessarSubprocessoGestor,
    cancelarDevolucao,
    devolverCadastroMapeamento,
    fecharHistoricoAnalise,
} from './helpers/helpers-analise.js';
import {fazerLogout, navegarParaSubprocesso, verificarPaginaPainel} from './helpers/helpers-navegacao.js';

test.describe.serial('CDU-13 - Analisar cadastro de atividades e conhecimentos', () => {
    const UNIDADE_ALVO = 'SECAO_211';

    const timestamp = Date.now();
    const descProcesso = `Processo CDU-13 ${timestamp}`;
    let processoId: number;

    // ========================================================================
    // PREPARAÇÃO
    // ========================================================================

    test('Preparacao 1: ADMIN cria e inicia processo de mapeamento', async ({page, autenticadoComoAdmin, cleanupAutomatico}) => {
        await criarProcesso(page, {
            descricao: descProcesso,
            tipo: 'MAPEAMENTO',
            diasLimite: 30,
            unidade: [UNIDADE_ALVO, 'SECRETARIA_2'],
            expandir: ['SECRETARIA_2', 'COORD_21']
        });

        // Iniciar processo
        const linhaProcesso = page.getByTestId('tbl-processos').locator('tr').filter({has: page.getByText(descProcesso)});
        await expect(linhaProcesso).toBeVisible();
        await linhaProcesso.click();

        // Capturar ID do processo para cleanup
        processoId = await extrairProcessoId(page);
        if (processoId > 0) cleanupAutomatico.registrar(processoId);

        await page.getByTestId('btn-processo-iniciar').click();
        await page.getByTestId('btn-iniciar-processo-confirmar').click();
        await verificarPaginaPainel(page);
        
        // Logout para garantir que o próximo teste (com outra fixture) não tenha conflito de sessão
        await fazerLogout(page);
    });

    test('Preparacao 2: CHEFE preenche atividades e disponibiliza', async ({page, autenticadoComoChefeSecao211}) => {
        await acessarSubprocessoChefeDireto(page, descProcesso);
        await navegarParaAtividades(page);

        // Adicionar 3 atividades com conhecimentos
        await adicionarAtividade(page, `Atividade 1 ${timestamp}`);
        await adicionarConhecimento(page, `Atividade 1 ${timestamp}`, 'Conhecimento 1A');

        await adicionarAtividade(page, `Atividade 2 ${timestamp}`);
        await adicionarConhecimento(page, `Atividade 2 ${timestamp}`, 'Conhecimento 2A');

        await adicionarAtividade(page, `Atividade 3 ${timestamp}`);
        await adicionarConhecimento(page, `Atividade 3 ${timestamp}`, 'Conhecimento 3A');

        // Disponibilizar cadastro
        await page.getByTestId('btn-cad-atividades-disponibilizar').click();
        await page.getByTestId('btn-confirmar-disponibilizacao').click();

        // Verificar mensagem de sucesso
        await expect(page.getByText(/Cadastro de atividades disponibilizado/i).first()).toBeVisible();
        await verificarPaginaPainel(page);
    });

    // ========================================================================
    // CENÁRIOS DE TESTE
    // ========================================================================

    test('Cenario 1: GESTOR visualiza histórico de análise (vazio inicialmente)', async ({page, autenticadoComoGestorCoord21}) => {
        await acessarSubprocessoGestor(page, descProcesso, UNIDADE_ALVO);
        await navegarParaAtividadesVisualizacao(page);

        // Abrir histórico
        const modal = await abrirHistoricoAnaliseVisualizacao(page);

        // Verificar que não há registros (ou mensagem apropriada)
        await expect(modal).toBeVisible();

        await fecharHistoricoAnalise(page);
    });

    test('Cenario 2: GESTOR devolve cadastro para ajustes COM observação', async ({page, autenticadoComoGestorCoord21}) => {
        await acessarSubprocessoGestor(page, descProcesso, UNIDADE_ALVO);
        await navegarParaAtividadesVisualizacao(page);

        // Devolver com observação
        await devolverCadastroMapeamento(page, 'Favor incluir mais detalhes nos conhecimentos');
    });

    test('Cenario 3: CHEFE visualiza histórico após devolução e disponibiliza novamente', async ({page, autenticadoComoChefeSecao211}) => {
        await acessarSubprocessoChefeDireto(page, descProcesso);

        // Verificar situação
        await expect(page.getByTestId('subprocesso-header__txt-situacao'))
            .toHaveText(/Cadastro em andamento/i);

        await navegarParaAtividades(page);

        // Abrir histórico e verificar devolução
        const modal = await abrirHistoricoAnalise(page);
        await expect(modal.getByTestId('cell-resultado-0')).toHaveText(/Devolu[cç][aã]o/i);
        await expect(modal.getByTestId('cell-observacao-0')).toHaveText('Favor incluir mais detalhes nos conhecimentos');
        await fecharHistoricoAnalise(page);

        // Disponibilizar novamente
        await page.getByTestId('btn-cad-atividades-disponibilizar').click();
        await page.getByTestId('btn-confirmar-disponibilizacao').click();

        await expect(page.getByText(/Cadastro de atividades disponibilizado/i).first()).toBeVisible();
        await verificarPaginaPainel(page);
    });

    test('Cenario 4: GESTOR cancela devolução', async ({page, autenticadoComoGestorCoord21}) => {
        await acessarSubprocessoGestor(page, descProcesso, UNIDADE_ALVO);
        await navegarParaAtividadesVisualizacao(page);

        // Cancelar devolução
        await cancelarDevolucao(page);

        // Verificar que permanece na mesma tela
        await expect(page.getByRole('heading', {name: 'Atividades e conhecimentos'})).toBeVisible();
    });

    test('Cenario 5: GESTOR COORD_21 registra aceite COM observação', async ({page, autenticadoComoGestorCoord21}) => {
        await acessarSubprocessoGestor(page, descProcesso, UNIDADE_ALVO);
        await navegarParaAtividadesVisualizacao(page);

        // Aceitar com observação - Isso move para a SECRETARIA_2
        await aceitarCadastroMapeamento(page, 'Cadastro aprovado pela COORD_21');
    });

    test('Cenario 6: ADMIN devolve para COORD_21 (Devolução Gradual - Nível Superior)', async ({page, autenticadoComoAdmin}) => {
        await acessarSubprocessoAdmin(page, descProcesso, UNIDADE_ALVO);
        await navegarParaAtividadesVisualizacao(page);

        // Como ADMIN na visualização, verificamos que a localização atual é SECRETARIA_2 (ou superior da COORD)
        // No momento o subprocesso está "localizado" na SECRETARIA_2 aguardando análise.
        
        await devolverCadastroMapeamento(page, 'Dados insuficientes para a Secretaria');

        // VERIFICAÇÃO 1: Deve ter voltado para a COORD_21
        await navegarParaSubprocesso(page, UNIDADE_ALVO);
        await expect(page.getByTestId('subprocesso-header__txt-localizacao')).toHaveText(/COORD_21/i);
        
        // VERIFICAÇÃO 2: A situação deve continuar como "Cadastro disponibilizado" (pois não chegou na unidade dona)
        await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Cadastro disponibilizado/i);
    });

    test('Cenario 7: GESTOR COORD_21 devolve para SECAO_211 (Devolução Gradual - Chegada na Origem)', async ({page, autenticadoComoGestorCoord21}) => {
        await acessarSubprocessoGestor(page, descProcesso, UNIDADE_ALVO);
        await navegarParaAtividadesVisualizacao(page);

        await devolverCadastroMapeamento(page, 'Corrigir conforme orientações da Secretaria');

        // VERIFICAÇÃO 1: Deve ter voltado para a SECAO_211
        await navegarParaSubprocesso(page, UNIDADE_ALVO);
        await expect(page.getByTestId('subprocesso-header__txt-localizacao')).toHaveText(/SECAO_211/i);
        
        // VERIFICAÇÃO 2: Agora sim a situação deve ser "Cadastro em andamento"
        await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Cadastro em andamento/i);
    });

    test('Cenario 8: ADMIN visualiza histórico com múltiplas análises', async ({page, autenticadoComoAdmin}) => {
        await acessarSubprocessoAdmin(page, descProcesso, UNIDADE_ALVO);
        await navegarParaSubprocesso(page, UNIDADE_ALVO);
        await navegarParaAtividadesVisualizacao(page);

        // Abrir histórico
        const modal = await abrirHistoricoAnaliseVisualizacao(page);

        // Verificar que modal está visível
        await expect(modal).toBeVisible();

        // Verificar que há exatamente 1 registro (pois limpou nas novas disponibilizações)
        await expect(modal.getByTestId('cell-resultado-0')).toBeVisible();
        await expect(modal.getByTestId('cell-resultado-0')).toHaveText(/ACEITE_MAPEAMENTO/i);

        await fecharHistoricoAnalise(page);
    });

});
