import {expect, test} from './fixtures/base';
import {login, USUARIOS} from './helpers/helpers-auth';
import {criarProcesso} from './helpers/helpers-processos';
import {
    adicionarAtividade,
    adicionarConhecimento,
    navegarParaAtividades,
    navegarParaAtividadesVisualizacao
} from './helpers/helpers-atividades';
import {resetDatabase, useProcessoCleanup} from './hooks/hooks-limpeza';
import {
    abrirHistoricoAnalise,
    abrirHistoricoAnaliseVisualizacao,
    aceitarCadastroMapeamento,
    aceitarRevisao,
    acessarSubprocessoAdmin,
    acessarSubprocessoChefe,
    acessarSubprocessoGestor,
    cancelarDevolucao,
    cancelarHomologacao,
    devolverRevisao,
    fazerLogout,
    fecharHistoricoAnalise,
    homologarCadastroMapeamento,
    homologarCadastroRevisao,
    verificarPaginaPainel
} from './helpers/helpers-analise';

test.describe.serial('CDU-14 - Analisar revisão de cadastro de atividades e conhecimentos', () => {
    const UNIDADE_ALVO = 'SECAO_221';
    const USUARIO_CHEFE = USUARIOS.CHEFE_SECAO_221.titulo;
    const SENHA_CHEFE = USUARIOS.CHEFE_SECAO_221.senha;
    const USUARIO_GESTOR = USUARIOS.GESTOR_COORD_22.titulo;
    const SENHA_GESTOR = USUARIOS.GESTOR_COORD_22.senha;
    const USUARIO_ADMIN = USUARIOS.ADMIN_1_PERFIL.titulo;
    const SENHA_ADMIN = USUARIOS.ADMIN_1_PERFIL.senha;

    const timestamp = Date.now();
    const descProcesso = `Processo CDU-14 ${timestamp}`;
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

    test('Preparacao 0: Criar mapa vigente através de processo de mapeamento', async ({page}) => {
        const descMapeamento = `Mapeamento para CDU-14 ${timestamp}`;

        // Passo 1: ADMIN cria e inicia processo de mapeamento
        await page.goto('/login');
        await login(page, USUARIO_ADMIN, SENHA_ADMIN);

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

        // Passo 2: CHEFE adiciona atividades e disponibiliza cadastro
        await fazerLogout(page);
        await login(page, USUARIO_CHEFE, SENHA_CHEFE);

        await page.getByText(descMapeamento).click();
        await navegarParaAtividades(page);

        await adicionarAtividade(page, `Atividade Map 1 ${timestamp}`);
        await adicionarConhecimento(page, `Atividade Map 1 ${timestamp}`, 'Conhecimento Map 1A');

        await page.getByTestId('btn-cad-atividades-disponibilizar').click();
        await page.getByTestId('btn-confirmar-disponibilizacao').click();
        await expect(page.getByRole('heading', {name: /Cadastro de atividades disponibilizado/i})).toBeVisible();
        await verificarPaginaPainel(page);

        // Passo 3: GESTOR aceita cadastro
        await fazerLogout(page);
        await login(page, USUARIO_GESTOR, SENHA_GESTOR);

        await acessarSubprocessoGestor(page, descMapeamento, UNIDADE_ALVO);
        await navegarParaAtividadesVisualizacao(page);
        await aceitarCadastroMapeamento(page);

        // Passo 4: ADMIN homologa cadastro
        await fazerLogout(page);
        await login(page, USUARIO_ADMIN, SENHA_ADMIN);

        await acessarSubprocessoAdmin(page, descMapeamento, UNIDADE_ALVO);
        await page.getByTestId('card-subprocesso-atividades-vis').click();
        await homologarCadastroMapeamento(page);

        // Passo 5: ADMIN cria competências e disponibiliza mapa
        // Após homologação, já está na tela de Detalhes do subprocesso
        await page.getByTestId('card-subprocesso-mapa').click();

        await page.getByTestId('btn-abrir-criar-competencia').click();
        await page.getByTestId('inp-criar-competencia-descricao').fill(`Competência Map ${timestamp}`);
        await page.getByText(`Atividade Map 1 ${timestamp}`).click();
        await page.getByTestId('btn-criar-competencia-salvar').click();
        await expect(page.getByTestId('mdl-criar-competencia')).toBeHidden();

        await page.getByTestId('btn-cad-mapa-disponibilizar').click();
        await page.getByTestId('inp-disponibilizar-mapa-data').fill('2030-12-31');
        await page.getByTestId('btn-disponibilizar-mapa-confirmar').click();
        await expect(page.getByTestId('mdl-disponibilizar-mapa')).toBeHidden();



        // Passo 6: CHEFE valida mapa
        await fazerLogout(page);
        await login(page, USUARIO_CHEFE, SENHA_CHEFE);

        await page.getByText(descMapeamento).click();
        await page.getByTestId('card-subprocesso-mapa').click();
        await page.getByTestId('btn-mapa-validar').click();
        await page.getByTestId('btn-validar-mapa-confirmar').click();
        
        // Validação: confirmar redirecionamento para Painel (CDU-19 passo 8)
        await verificarPaginaPainel(page);
        
        // Navegar novamente ao subprocesso para verificar situação
        await page.getByText(descMapeamento).click();
        await expect(page.getByTestId('subprocesso-header__txt-badge-situacao')).toHaveText(/Mapa validado/i);

        // Passo 7: ADMIN homologa mapa
        await fazerLogout(page);
        await login(page, USUARIO_ADMIN, SENHA_ADMIN);


        await acessarSubprocessoAdmin(page, descMapeamento, UNIDADE_ALVO);
        await page.getByTestId('card-subprocesso-mapa').click();
        await page.getByTestId('btn-mapa-homologar-aceite').click();
        await page.getByTestId('btn-aceite-mapa-confirmar').click();
        
        // Validação: confirmar redirecionamento para Painel (CDU-20 passo 10.6)
        await verificarPaginaPainel(page);

        // Passo 8: ADMIN finaliza o processo
        await page.goto('/painel');
        await page.locator('tr').filter({has: page.getByText(descMapeamento)}).click();
        await page.getByTestId('btn-processo-finalizar').click();
        await page.getByTestId('btn-finalizar-processo-confirmar').click();

        // Verificar alert e status na tabela
        await expect(page.getByText('Processo finalizado')).toBeVisible();
        await expect(page.locator('tr', {has: page.getByText(descMapeamento)}).getByText('Finalizado')).toBeVisible();

        // Agora a unidade tem mapa vigente e pode participar de processo de revisão
        await verificarPaginaPainel(page);
    });

    test('Preparacao 1: ADMIN cria e inicia processo de revisão', async ({page}) => {
        await page.goto('/login');
        await login(page, USUARIO_ADMIN, SENHA_ADMIN);

        await criarProcesso(page, {
            descricao: descProcesso,
            tipo: 'REVISAO',
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

    test('Preparacao 2: CHEFE revisa atividades e disponibiliza', async ({page}) => {
        await page.goto('/login');
        await login(page, USUARIO_CHEFE, SENHA_CHEFE);

        await acessarSubprocessoChefe(page, descProcesso);
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
        await expect(page.getByRole('heading', {name: /Revisão disponibilizada/i})).toBeVisible();
        await verificarPaginaPainel(page);
    });

    // ========================================================================
    // CENÁRIOS DE TESTE
    // ========================================================================

    test('Cenario 1: GESTOR visualiza histórico de análise (vazio inicialmente)', async ({page}) => {
        await page.goto('/login');
        await login(page, USUARIO_GESTOR, SENHA_GESTOR);

        await acessarSubprocessoGestor(page, descProcesso, UNIDADE_ALVO);
        await navegarParaAtividadesVisualizacao(page);

        // Abrir histórico
        const modal = await abrirHistoricoAnaliseVisualizacao(page);

        // Verificar que não há registros (ou mensagem apropriada)
        await expect(modal).toBeVisible();

        await fecharHistoricoAnalise(page);
    });

    test('Cenario 2: GESTOR verifica botão "Impactos no mapa" está disponível', async ({page}) => {
        await page.goto('/login');
        await login(page, USUARIO_GESTOR, SENHA_GESTOR);

        await acessarSubprocessoGestor(page, descProcesso, UNIDADE_ALVO);
        await navegarParaAtividadesVisualizacao(page);

        // Verificar que o botão "Impactos no mapa" está visível
        await expect(page.getByTestId('cad-atividades__btn-impactos-mapa')).toBeVisible();
    });

    test('Cenario 3: GESTOR devolve cadastro para ajustes COM observação', async ({page}) => {
        await page.goto('/login');
        await login(page, USUARIO_GESTOR, SENHA_GESTOR);

        await acessarSubprocessoGestor(page, descProcesso, UNIDADE_ALVO);
        await navegarParaAtividadesVisualizacao(page);

        // Devolver com observação
        await devolverRevisao(page, 'Favor revisar as competências associadas');
    });

    test('Cenario 4: CHEFE visualiza histórico após devolução e disponibiliza novamente', async ({page}) => {
        await page.goto('/login');
        await login(page, USUARIO_CHEFE, SENHA_CHEFE);

        await acessarSubprocessoChefe(page, descProcesso);

        // Verificar situação
        await expect(page.getByTestId('subprocesso-header__txt-badge-situacao'))
            .toHaveText(/Revisão de Cadastro em Andamento/i);

        await navegarParaAtividades(page);

        // Abrir histórico e verificar devolução
        const modal = await abrirHistoricoAnalise(page);
        await expect(modal.getByTestId('cell-resultado-0')).toHaveText(/Devolu[cç][aã]o/i);
        await expect(modal.getByTestId('cell-observacao-0')).toHaveText('Favor revisar as competências associadas');
        await fecharHistoricoAnalise(page);

        // Disponibilizar novamente
        await page.getByTestId('btn-cad-atividades-disponibilizar').click();
        await page.getByTestId('btn-confirmar-disponibilizacao').click();

        await expect(page.getByRole('heading', {name: /Revisão disponibilizada/i})).toBeVisible();
        await verificarPaginaPainel(page);
    });

    test('Cenario 5: GESTOR cancela devolução', async ({page}) => {
        await page.goto('/login');
        await login(page, USUARIO_GESTOR, SENHA_GESTOR);

        await acessarSubprocessoGestor(page, descProcesso, UNIDADE_ALVO);
        await navegarParaAtividadesVisualizacao(page);

        // Cancelar devolução
        await cancelarDevolucao(page);

        // Verificar que permanece na mesma tela
        await expect(page.getByRole('heading', {name: 'Atividades e conhecimentos'})).toBeVisible();
    });

    test('Cenario 6: GESTOR registra aceite COM observação', async ({page}) => {
        await page.goto('/login');
        await login(page, USUARIO_GESTOR, SENHA_GESTOR);

        await acessarSubprocessoGestor(page, descProcesso, UNIDADE_ALVO);
        await navegarParaAtividadesVisualizacao(page);

        // Aceitar com observação
        await aceitarRevisao(page, 'Revisão aprovada conforme análise');
    });

    test('Cenario 7: ADMIN devolve para nova rodada de aceite', async ({page}) => {
        // Devolver para permitir novo aceite sem observação
        await page.goto('/login');
        await login(page, USUARIO_ADMIN, SENHA_ADMIN);

        await acessarSubprocessoAdmin(page, descProcesso, UNIDADE_ALVO);
        await page.getByTestId('card-subprocesso-atividades-vis').click();

        await devolverRevisao(page, 'Pequeno ajuste necessário na revisão');

        // CHEFE disponibiliza novamente
        await fazerLogout(page);
        await login(page, USUARIO_CHEFE, SENHA_CHEFE);

        await acessarSubprocessoChefe(page, descProcesso);
        await navegarParaAtividades(page);

        await page.getByTestId('btn-cad-atividades-disponibilizar').click();
        await page.getByTestId('btn-confirmar-disponibilizacao').click();
        await verificarPaginaPainel(page);
    });

    test('Cenario 8: GESTOR registra aceite SEM observação', async ({page}) => {
        await page.goto('/login');
        await login(page, USUARIO_GESTOR, SENHA_GESTOR);

        await acessarSubprocessoGestor(page, descProcesso, UNIDADE_ALVO);
        await navegarParaAtividadesVisualizacao(page);

        // Aceitar sem observação
        await aceitarRevisao(page);
    });

    test('Cenario 9: ADMIN visualiza histórico com múltiplas análises', async ({page}) => {
        await page.goto('/login');
        await login(page, USUARIO_ADMIN, SENHA_ADMIN);

        await acessarSubprocessoAdmin(page, descProcesso, UNIDADE_ALVO);
        await page.getByTestId('card-subprocesso-atividades-vis').click();

        // Abrir histórico
        const modal = await abrirHistoricoAnaliseVisualizacao(page);

        // Verificar que modal está visível
        await expect(modal).toBeVisible();

        // Neste ponto do fluxo serial, devemos ter apenas 1 análise:
        // - Cenário 3: 1 devolução (GESTOR) → REMOVIDA quando CHEFE disponibilizou novamente (Cenário 4)
        // - Cenário 6: 1 aceite (GESTOR) → REMOVIDA quando ADMIN devolveu (Cenário 7)
        // - Cenário 7: 1 devolução (ADMIN) → REMOVIDA quando CHEFE disponibilizou novamente (Cenário 7)
        // - Cenário 8: 1 aceite (GESTOR) → ÚNICA análise desde a última disponibilização
        //
        // Conforme CDU-14 linha 32: "análises prévias registradas para o cadastro de atividades 
        // desde a última disponibilização"

        // Verificar que há exatamente 1 registro
        await expect(modal.getByTestId('cell-resultado-0')).toBeVisible();
        await expect(modal.getByTestId('cell-resultado-0')).toHaveText(/ACEITE_REVISAO/i);

        await fecharHistoricoAnalise(page);
    });

    test('Cenario 10: ADMIN cancela homologação', async ({page}) => {
        await page.goto('/login');
        await login(page, USUARIO_ADMIN, SENHA_ADMIN);

        await acessarSubprocessoAdmin(page, descProcesso, UNIDADE_ALVO);
        await page.getByTestId('card-subprocesso-atividades-vis').click();

        // Cancelar homologação
        await cancelarHomologacao(page);

        // Verificar que permanece na mesma tela
        await expect(page.getByRole('heading', {name: 'Atividades e conhecimentos'})).toBeVisible();
    });

    test('Cenario 11: ADMIN homologa cadastro de revisão', async ({page}) => {
        await page.goto('/login');
        await login(page, USUARIO_ADMIN, SENHA_ADMIN);

        await acessarSubprocessoAdmin(page, descProcesso, UNIDADE_ALVO);
        await page.getByTestId('card-subprocesso-atividades-vis').click();

        // Homologar
        await homologarCadastroRevisao(page);
    });
});
