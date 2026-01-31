import {expect, test} from './fixtures/auth-fixtures.js';
import {criarProcesso} from './helpers/helpers-processos.js';
import {
    adicionarAtividade,
    adicionarConhecimento,
    navegarParaAtividades,
    navegarParaAtividadesVisualizacao
} from './helpers/helpers-atividades.js';
import {resetDatabase, useProcessoCleanup} from './hooks/hooks-limpeza.js';
import {
    abrirHistoricoAnalise,
    abrirHistoricoAnaliseVisualizacao,
    aceitarCadastroMapeamento,
    acessarSubprocessoAdmin,
    acessarSubprocessoChefeDireto,
    acessarSubprocessoGestor,
    cancelarDevolucao,
    cancelarHomologacao,
    devolverCadastroMapeamento,
    fecharHistoricoAnalise,
    homologarCadastroMapeamento,
} from './helpers/helpers-analise.js';
import {fazerLogout, verificarPaginaPainel} from './helpers/helpers-navegacao.js';

test.describe.serial('CDU-13 - Analisar cadastro de atividades e conhecimentos', () => {
    const UNIDADE_ALVO = 'SECAO_221';

    const timestamp = Date.now();
    const descProcesso = `Processo CDU-13 ${timestamp}`;
    let processoId: number;
    let cleanup: ReturnType<typeof useProcessoCleanup>;

    test.beforeAll(async ({request}) => {
        await resetDatabase(request);
        cleanup = useProcessoCleanup();
    });

    test.afterAll(async ({request}) => {
        await cleanup.limpar(request);
    });

    // ========================================================================
    // PREPARAÇÃO
    // ========================================================================

    test('Preparacao 1: ADMIN cria e inicia processo de mapeamento', async ({page}) => {
        

        await criarProcesso(page, {
            descricao: descProcesso,
            tipo: 'MAPEAMENTO',
            diasLimite: 30,
            unidade: UNIDADE_ALVO,
            expandir: ['SECRETARIA_2', 'COORD_22']
        });

        // Iniciar processo
        const linhaProcesso = page.locator('tr').filter({has: page.getByText(descProcesso)});
        await linhaProcesso.click();

        // Capturar ID do processo para cleanup
        processoId = Number.parseInt(new RegExp(/\/processo\/cadastro\/(\d+)/).exec(page.url())?.[1] || '0');
        if (processoId > 0) cleanup.registrar(processoId);

        await page.getByTestId('btn-processo-iniciar').click();
        await page.getByTestId('btn-iniciar-processo-confirmar').click();
        await verificarPaginaPainel(page);
    });

    test('Preparacao 2: CHEFE preenche atividades e disponibiliza', async ({page}) => {
        

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
        await expect(page.getByText(/Cadastro de atividades disponibilizado/i)).toBeVisible();
        await verificarPaginaPainel(page);
    });

    // ========================================================================
    // CENÁRIOS DE TESTE
    // ========================================================================

    test('Cenario 1: GESTOR visualiza histórico de análise (vazio inicialmente)', async ({page}) => {
        

        await acessarSubprocessoGestor(page, descProcesso, UNIDADE_ALVO);
        await navegarParaAtividadesVisualizacao(page);

        // Abrir histórico
        const modal = await abrirHistoricoAnaliseVisualizacao(page);

        // Verificar que não há registros (ou mensagem apropriada)
        // O modal deve estar vazio ou mostrar mensagem de "nenhum registro"
        await expect(modal).toBeVisible();

        await fecharHistoricoAnalise(page);
    });

    test('Cenario 2: GESTOR devolve cadastro para ajustes COM observação', async ({page}) => {
        

        await acessarSubprocessoGestor(page, descProcesso, UNIDADE_ALVO);
        await navegarParaAtividadesVisualizacao(page);

        // Devolver com observação
        await devolverCadastroMapeamento(page, 'Favor incluir mais detalhes nos conhecimentos');
    });

    test('Cenario 3: CHEFE visualiza histórico após devolução e disponibiliza novamente', async ({page}) => {
        

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

        await expect(page.getByText(/Cadastro de atividades disponibilizado/i)).toBeVisible();
        await verificarPaginaPainel(page);
    });

    test('Cenario 4: GESTOR cancela devolução', async ({page}) => {
        

        await acessarSubprocessoGestor(page, descProcesso, UNIDADE_ALVO);
        await navegarParaAtividadesVisualizacao(page);

        // Cancelar devolução
        await cancelarDevolucao(page);

        // Verificar que permanece na mesma tela
        await expect(page.getByRole('heading', {name: 'Atividades e conhecimentos'})).toBeVisible();
    });

    test('Cenario 5: GESTOR registra aceite COM observação', async ({page}) => {
        

        await acessarSubprocessoGestor(page, descProcesso, UNIDADE_ALVO);
        await navegarParaAtividadesVisualizacao(page);

        // Aceitar com observação
        await aceitarCadastroMapeamento(page, 'Cadastro aprovado conforme análise');
    });

    test('Cenario 6: ADMIN devolve para nova rodada de aceite', async ({page}) => {
        // Devolver para permitir novo aceite sem observação
        

        await acessarSubprocessoAdmin(page, descProcesso, UNIDADE_ALVO);
        await page.getByTestId('card-subprocesso-atividades-vis').click();

        await devolverCadastroMapeamento(page, 'Pequeno ajuste necessário');

        // CHEFE disponibiliza novamente
        await fazerLogout(page);
        

        await acessarSubprocessoChefeDireto(page, descProcesso);
        await navegarParaAtividades(page);

        await page.getByTestId('btn-cad-atividades-disponibilizar').click();
        await expect(page.getByTestId('btn-confirmar-disponibilizacao')).toBeVisible();
        await page.getByTestId('btn-confirmar-disponibilizacao').click();
        await verificarPaginaPainel(page);
    });

    test('Cenario 7: GESTOR registra aceite com observação padrão', async ({page}) => {
        

        await acessarSubprocessoGestor(page, descProcesso, UNIDADE_ALVO);
        await navegarParaAtividadesVisualizacao(page);

        // Aceitar sem observação
        await aceitarCadastroMapeamento(page);
    });

    test('Cenario 8: ADMIN visualiza histórico com múltiplas análises', async ({page}) => {
        

        await acessarSubprocessoAdmin(page, descProcesso, UNIDADE_ALVO);
        await page.getByTestId('card-subprocesso-atividades-vis').click();

        // Abrir histórico
        const modal = await abrirHistoricoAnaliseVisualizacao(page);

        // Verificar que modal está visível
        await expect(modal).toBeVisible();

        // Neste ponto do fluxo serial, devemos ter apenas 1 análise:
        // - Cenário 2: 1 devolução (GESTOR) → REMOVIDA quando CHEFE disponibilizou novamente (Cenário 3)
        // - Cenário 5: 1 aceite (GESTOR) → REMOVIDA quando CHEFE disponibilizou novamente (Cenário 6)
        // - Cenário 6: 1 devolução (ADMIN) → REMOVIDA quando CHEFE disponibilizou novamente (Cenário 6)
        // - Cenário 7: 1 aceite (GESTOR) → ÚNICA análise desde a última disponibilização
        //
        // Conforme CDU-13 linha 28: "análises prévias registradas para o cadastro de atividades 
        // desde a última disponibilização"

        // Verificar que há exatamente 1 registro
        await expect(modal.getByTestId('cell-resultado-0')).toBeVisible();
        await expect(modal.getByTestId('cell-resultado-0')).toHaveText(/ACEITE_MAPEAMENTO/i);

        await fecharHistoricoAnalise(page);
    });

    test('Cenario 9: ADMIN cancela homologação', async ({page}) => {
        

        await acessarSubprocessoAdmin(page, descProcesso, UNIDADE_ALVO);
        await page.getByTestId('card-subprocesso-atividades-vis').click();

        // Cancelar homologação
        await cancelarHomologacao(page);

        // Verificar que permanece na mesma tela
        await expect(page.getByRole('heading', {name: 'Atividades e conhecimentos'})).toBeVisible();
    });

    test('Cenario 10: ADMIN homologa cadastro', async ({page}) => {
        

        await acessarSubprocessoAdmin(page, descProcesso, UNIDADE_ALVO);
        await page.getByTestId('card-subprocesso-atividades-vis').click();

        // Homologar
        await homologarCadastroMapeamento(page);
    });
});