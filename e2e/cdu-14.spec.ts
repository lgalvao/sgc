import {expect, test} from './fixtures/complete-fixtures.js';
import {USUARIOS, login} from './helpers/helpers-auth.js';
import {criarProcesso, extrairProcessoId} from './helpers/helpers-processos.js';
import {
    adicionarAtividade,
    adicionarConhecimento,
    navegarParaAtividades,
    navegarParaAtividadesVisualizacao,
    verificarBotaoImpactoDireto
} from './helpers/helpers-atividades.js';
import {
    abrirHistoricoAnalise,
    abrirHistoricoAnaliseVisualizacao,
    aceitarCadastroMapeamento,
    aceitarRevisao,
    acessarSubprocessoAdmin,
    acessarSubprocessoChefeDireto,
    acessarSubprocessoGestor,
    cancelarDevolucao,
    cancelarHomologacao,
    devolverRevisao,
    fecharHistoricoAnalise,
    homologarCadastroMapeamento,
    homologarCadastroRevisaoComImpacto,
} from './helpers/helpers-analise.js';
import {fazerLogout, verificarPaginaPainel} from './helpers/helpers-navegacao.js';
import {criarCompetencia, disponibilizarMapa, navegarParaMapa} from './helpers/helpers-mapas.js';

test.describe.serial('CDU-14 - Analisar revisão de cadastro de atividades e conhecimentos', () => {
    const UNIDADE_ALVO = 'SECAO_221';

    const timestamp = Date.now();
    const descProcesso = `Processo CDU-14 ${timestamp}`;
    const descMapeamento = `Mapeamento para CDU-14 ${timestamp}`;
    let processoId: number;

    // ========================================================================
    // PREPARAÇÃO
    // ========================================================================

    test('Preparacao 0.1: ADMIN cria e inicia processo de mapeamento', async ({page, autenticadoComoAdmin, cleanupAutomatico}) => {
        await criarProcesso(page, {
            descricao: descMapeamento,
            tipo: 'MAPEAMENTO',
            diasLimite: 30,
            unidade: UNIDADE_ALVO,
            expandir: ['SECRETARIA_2', 'COORD_22']
        });

        const linhaProcesso = page.locator('tr').filter({has: page.getByText(descMapeamento)});
        await linhaProcesso.click();
        await page.getByTestId('btn-processo-iniciar').click();
        await page.getByTestId('btn-iniciar-processo-confirmar').waitFor({state: 'visible'});
        await page.getByTestId('btn-iniciar-processo-confirmar').click();
        await verificarPaginaPainel(page);
    });

    test('Preparacao 0.2: CHEFE adiciona atividades e disponibiliza cadastro', async ({page, autenticadoComoChefeSecao221}) => {
        await acessarSubprocessoChefeDireto(page, descMapeamento, UNIDADE_ALVO);
        await navegarParaAtividades(page);

        await adicionarAtividade(page, `Atividade Map 1 ${timestamp}`);
        await adicionarConhecimento(page, `Atividade Map 1 ${timestamp}`, 'Conhecimento Map 1A');

        await page.getByTestId('btn-cad-atividades-disponibilizar').click();
        await page.getByTestId('btn-confirmar-disponibilizacao').click();
        await expect(page.getByText(/Cadastro de atividades disponibilizado/i).first()).toBeVisible();
        await verificarPaginaPainel(page);
    });

    test('Preparacao 0.3: GESTOR aceita cadastro', async ({page, autenticadoComoGestorCoord22}) => {
        await acessarSubprocessoGestor(page, descMapeamento, UNIDADE_ALVO);
        await navegarParaAtividadesVisualizacao(page);
        await aceitarCadastroMapeamento(page);
    });

    test('Preparacao 0.4: ADMIN homologa cadastro', async ({page, autenticadoComoAdmin}) => {
        await acessarSubprocessoAdmin(page, descMapeamento, UNIDADE_ALVO);
        await page.getByTestId('card-subprocesso-atividades-vis').click();
        await homologarCadastroMapeamento(page);
    });

    test('Preparacao 0.5: ADMIN cria competências e disponibiliza mapa', async ({page, autenticadoComoAdmin}) => {
        await acessarSubprocessoAdmin(page, descMapeamento, UNIDADE_ALVO);
        await navegarParaMapa(page);
        await criarCompetencia(page, `Competência Map ${timestamp}`, [`Atividade Map 1 ${timestamp}`]);
        await disponibilizarMapa(page, '2030-12-31');
    });

    test('Preparacao 0.6: CHEFE valida mapa', async ({page, autenticadoComoChefeSecao221}) => {
        await acessarSubprocessoChefeDireto(page, descMapeamento, UNIDADE_ALVO);
        await navegarParaMapa(page);
        await page.getByTestId('btn-mapa-validar').click();
        await page.getByTestId('btn-validar-mapa-confirmar').click();
        await verificarPaginaPainel(page);
    });

    test('Preparacao 0.7: ADMIN homologa mapa', async ({page, autenticadoComoAdmin}) => {
        await acessarSubprocessoAdmin(page, descMapeamento, UNIDADE_ALVO);
        await navegarParaMapa(page);
        await page.getByTestId('btn-mapa-homologar-aceite').click();
        await page.getByTestId('btn-aceite-mapa-confirmar').click();
        await verificarPaginaPainel(page);
    });

    test('Preparacao 0.8: ADMIN finaliza o processo', async ({page, autenticadoComoAdmin}) => {
        await page.locator('tr').filter({has: page.getByText(descMapeamento)}).click();
        await page.getByTestId('btn-processo-finalizar').click();
        await page.getByTestId('btn-finalizar-processo-confirmar').click();
        await expect(page.getByText('Processo finalizado')).toBeVisible();
        await verificarPaginaPainel(page);
    });

    test('Preparacao 1: ADMIN cria e inicia processo de revisão', async ({page, autenticadoComoAdmin, cleanupAutomatico}) => {
        await criarProcesso(page, {
            descricao: descProcesso,
            tipo: 'REVISAO',
            diasLimite: 30,
            unidade: UNIDADE_ALVO,
            expandir: ['SECRETARIA_2', 'COORD_22']
        });

        const linhaProcesso = page.locator('tr').filter({has: page.getByText(descProcesso)});
        await linhaProcesso.click();

        // Capturar ID do processo para cleanup
        processoId = await extrairProcessoId(page);
        if (processoId > 0) cleanupAutomatico.registrar(processoId);

        await page.getByTestId('btn-processo-iniciar').click();
        await page.getByTestId('btn-iniciar-processo-confirmar').click();
        await verificarPaginaPainel(page);
    });

    test('Preparacao 2: CHEFE revisa atividades e disponibiliza', async ({page, autenticadoComoChefeSecao221}) => {
        await page.goto(`/processo/${processoId}/${UNIDADE_ALVO}`);
        await navegarParaAtividades(page);

        // Adicionar 3 atividades com conhecimentos
        await adicionarAtividade(page, `Atividade Rev 1 ${timestamp}`);
        await adicionarConhecimento(page, `Atividade Rev 1 ${timestamp}`, 'Conhecimento Rev 1A');

        await adicionarAtividade(page, `Atividade Rev 2 ${timestamp}`);
        await adicionarConhecimento(page, `Atividade Rev 2 ${timestamp}`, 'Conhecimento Rev 2A');

        await adicionarAtividade(page, `Atividade Rev 3 ${timestamp}`);
        await adicionarConhecimento(page, `Atividade Rev 3 ${timestamp}`, 'Conhecimento Rev 3A');

        // Disponibilizar cadastro
        await page.getByTestId('btn-cad-atividades-disponibilizar').click();
        await page.getByTestId('btn-confirmar-disponibilizacao').click();

        // Verificar mensagem de sucesso
        await expect(page.getByText(/Revisão disponibilizada/i)).toBeVisible();
        await verificarPaginaPainel(page);
    });

    // ========================================================================
    // CENÁRIOS DE TESTE
    // ========================================================================

    test('Cenario 1: GESTOR visualiza histórico de análise (vazio inicialmente)', async ({page, autenticadoComoGestorCoord22}) => {
        await acessarSubprocessoGestor(page, descProcesso, UNIDADE_ALVO);
        await navegarParaAtividadesVisualizacao(page);

        // Abrir histórico
        const modal = await abrirHistoricoAnaliseVisualizacao(page);
        await expect(modal).toBeVisible();
        await fecharHistoricoAnalise(page);
    });

    test('Cenario 2: GESTOR verifica botão "Impactos no mapa" está disponível', async ({page, autenticadoComoGestorCoord22}) => {
        await acessarSubprocessoGestor(page, descProcesso, UNIDADE_ALVO);
        await navegarParaAtividadesVisualizacao(page);
        await verificarBotaoImpactoDireto(page);
    });

    test('Cenario 3: GESTOR devolve cadastro para ajustes COM observação', async ({page, autenticadoComoGestorCoord22}) => {
        await acessarSubprocessoGestor(page, descProcesso, UNIDADE_ALVO);
        await navegarParaAtividadesVisualizacao(page);
        await devolverRevisao(page, 'Favor revisar as competências associadas');
    });

    test('Cenario 4: CHEFE visualiza histórico após devolução e disponibiliza novamente', async ({page, autenticadoComoChefeSecao221}) => {
        await acessarSubprocessoChefeDireto(page, descProcesso);
        await navegarParaAtividades(page);

        const modal = await abrirHistoricoAnalise(page);
        await expect(modal.getByTestId('cell-resultado-0')).toHaveText(/Devolu[cç][aã]o/i);
        await expect(modal.getByTestId('cell-observacao-0')).toHaveText('Favor revisar as competências associadas');
        await fecharHistoricoAnalise(page);

        await page.getByTestId('btn-cad-atividades-disponibilizar').click();
        await page.getByTestId('btn-confirmar-disponibilizacao').click();
        await expect(page.getByText(/Revisão disponibilizada/i)).toBeVisible();
        await verificarPaginaPainel(page);
    });

    test('Cenario 5: GESTOR cancela devolução', async ({page, autenticadoComoGestorCoord22}) => {
        await acessarSubprocessoGestor(page, descProcesso, UNIDADE_ALVO);
        await navegarParaAtividadesVisualizacao(page);
        await cancelarDevolucao(page);
        await expect(page.getByRole('heading', {name: 'Atividades e conhecimentos'})).toBeVisible();
    });

    test('Cenario 6: GESTOR registra aceite COM observação', async ({page, autenticadoComoGestorCoord22}) => {
        await acessarSubprocessoGestor(page, descProcesso, UNIDADE_ALVO);
        await navegarParaAtividadesVisualizacao(page);
        await aceitarRevisao(page, 'Revisão aprovada conforme análise');
    });

    test('Cenario 7: ADMIN devolve para nova rodada de aceite', async ({page, autenticadoComoAdmin}) => {
        await acessarSubprocessoAdmin(page, descProcesso, UNIDADE_ALVO);
        await page.getByTestId('card-subprocesso-atividades-vis').click();
        await devolverRevisao(page, 'Pequeno ajuste necessário na revisão');

        await fazerLogout(page);
        await login(page, USUARIOS.CHEFE_SECAO_221.titulo, USUARIOS.CHEFE_SECAO_221.senha);

        await acessarSubprocessoChefeDireto(page, descProcesso);
        await navegarParaAtividades(page);
        await page.getByTestId('btn-cad-atividades-disponibilizar').click();
        await page.getByTestId('btn-confirmar-disponibilizacao').click();
        await verificarPaginaPainel(page);
    });

    test('Cenario 8: GESTOR registra aceite com observação padrão', async ({page, autenticadoComoGestorCoord22}) => {
        await acessarSubprocessoGestor(page, descProcesso, UNIDADE_ALVO);
        await navegarParaAtividadesVisualizacao(page);
        await aceitarRevisao(page);
    });

    test('Cenario 9: ADMIN visualiza histórico com múltiplas análises', async ({page, autenticadoComoAdmin}) => {
        await acessarSubprocessoAdmin(page, descProcesso, UNIDADE_ALVO);
        await page.getByTestId('card-subprocesso-atividades-vis').click();
        const modal = await abrirHistoricoAnaliseVisualizacao(page);
        await expect(modal).toBeVisible();
        await expect(modal.getByTestId('cell-resultado-0')).toHaveText(/ACEITE_REVISAO/i);
        await fecharHistoricoAnalise(page);
    });

    test('Cenario 10: ADMIN cancela homologação', async ({page, autenticadoComoAdmin}) => {
        await acessarSubprocessoAdmin(page, descProcesso, UNIDADE_ALVO);
        await page.getByTestId('card-subprocesso-atividades-vis').click();
        await cancelarHomologacao(page);
        await expect(page.getByRole('heading', {name: 'Atividades e conhecimentos'})).toBeVisible();
    });

    test('Cenario 11: ADMIN homologa cadastro de revisão', async ({page, autenticadoComoAdmin}) => {
        await acessarSubprocessoAdmin(page, descProcesso, UNIDADE_ALVO);
        await page.getByTestId('card-subprocesso-atividades-vis').click();
        await homologarCadastroRevisaoComImpacto(page);
    });
});
