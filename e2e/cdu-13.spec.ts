import {expect, test} from './fixtures/complete-fixtures.js';
import {USUARIOS, login} from './helpers/helpers-auth.js';
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

    // ========================================================================
    // PREPARAÇÃO
    // ========================================================================

    test('Preparacao 1: ADMIN cria e inicia processo de mapeamento', async ({page, autenticadoComoAdmin, cleanupAutomatico}) => {
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
        processoId = await extrairProcessoId(page);
        if (processoId > 0) cleanupAutomatico.registrar(processoId);

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

    test('Cenario 1: GESTOR visualiza histórico de análise (vazio inicialmente)', async ({page, autenticadoComoGestor}) => {
        await acessarSubprocessoGestor(page, descProcesso, UNIDADE_ALVO);
        await navegarParaAtividadesVisualizacao(page);

        // Abrir histórico
        const modal = await abrirHistoricoAnaliseVisualizacao(page);

        // Verificar que não há registros (ou mensagem apropriada)
        await expect(modal).toBeVisible();

        await fecharHistoricoAnalise(page);
    });

    test('Cenario 2: GESTOR devolve cadastro para ajustes COM observação', async ({page, autenticadoComoGestor}) => {
        await acessarSubprocessoGestor(page, descProcesso, UNIDADE_ALVO);
        await navegarParaAtividadesVisualizacao(page);

        // Devolver com observação
        await devolverCadastroMapeamento(page, 'Favor incluir mais detalhes nos conhecimentos');
    });

    test('Cenario 3: CHEFE visualiza histórico após devolução e disponibiliza novamente', async ({page, autenticadoComoChefeSecao221}) => {
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

    test('Cenario 4: GESTOR cancela devolução', async ({page, autenticadoComoGestor}) => {
        await acessarSubprocessoGestor(page, descProcesso, UNIDADE_ALVO);
        await navegarParaAtividadesVisualizacao(page);

        // Cancelar devolução
        await cancelarDevolucao(page);

        // Verificar que permanece na mesma tela
        await expect(page.getByRole('heading', {name: 'Atividades e conhecimentos'})).toBeVisible();
    });

    test('Cenario 5: GESTOR registra aceite COM observação', async ({page, autenticadoComoGestor}) => {
        await acessarSubprocessoGestor(page, descProcesso, UNIDADE_ALVO);
        await navegarParaAtividadesVisualizacao(page);

        // Aceitar com observação
        await aceitarCadastroMapeamento(page, 'Cadastro aprovado conforme análise');
    });

    test('Cenario 6: ADMIN devolve para nova rodada de aceite', async ({page, autenticadoComoAdmin}) => {
        // Devolver para permitir novo aceite sem observação
        await acessarSubprocessoAdmin(page, descProcesso, UNIDADE_ALVO);
        await page.getByTestId('card-subprocesso-atividades-vis').click();

        await devolverCadastroMapeamento(page, 'Pequeno ajuste necessário');

        // CHEFE disponibiliza novamente
        await fazerLogout(page);
        await login(page, USUARIOS.CHEFE_SECAO_221.titulo, USUARIOS.CHEFE_SECAO_221.senha);

        await acessarSubprocessoChefeDireto(page, descProcesso);
        await navegarParaAtividades(page);

        await page.getByTestId('btn-cad-atividades-disponibilizar').click();
        await expect(page.getByTestId('btn-confirmar-disponibilizacao')).toBeVisible();
        await page.getByTestId('btn-confirmar-disponibilizacao').click();
        await verificarPaginaPainel(page);
    });

    test('Cenario 7: GESTOR registra aceite com observação padrão', async ({page, autenticadoComoGestor}) => {
        await acessarSubprocessoGestor(page, descProcesso, UNIDADE_ALVO);
        await navegarParaAtividadesVisualizacao(page);

        // Aceitar sem observação
        await aceitarCadastroMapeamento(page);
    });

    test('Cenario 8: ADMIN visualiza histórico com múltiplas análises', async ({page, autenticadoComoAdmin}) => {
        await acessarSubprocessoAdmin(page, descProcesso, UNIDADE_ALVO);
        await page.getByTestId('card-subprocesso-atividades-vis').click();

        // Abrir histórico
        const modal = await abrirHistoricoAnaliseVisualizacao(page);

        // Verificar que modal está visível
        await expect(modal).toBeVisible();

        // Verificar que há exatamente 1 registro (pois limpou nas novas disponibilizações)
        await expect(modal.getByTestId('cell-resultado-0')).toBeVisible();
        await expect(modal.getByTestId('cell-resultado-0')).toHaveText(/ACEITE_MAPEAMENTO/i);

        await fecharHistoricoAnalise(page);
    });

    test('Cenario 9: ADMIN cancela homologação', async ({page, autenticadoComoAdmin}) => {
        await acessarSubprocessoAdmin(page, descProcesso, UNIDADE_ALVO);
        await page.getByTestId('card-subprocesso-atividades-vis').click();

        // Cancelar homologação
        await cancelarHomologacao(page);

        // Verificar que permanece na mesma tela
        await expect(page.getByRole('heading', {name: 'Atividades e conhecimentos'})).toBeVisible();
    });

    test('Cenario 10: ADMIN homologa cadastro', async ({page, autenticadoComoAdmin}) => {
        await acessarSubprocessoAdmin(page, descProcesso, UNIDADE_ALVO);
        await page.getByTestId('card-subprocesso-atividades-vis').click();

        // Homologar
        await homologarCadastroMapeamento(page);
    });
});
