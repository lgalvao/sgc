import {expect, test} from './fixtures/complete-fixtures.js';
import {login, loginComPerfil, USUARIOS} from './helpers/helpers-auth.js';
import {criarProcesso} from './helpers/helpers-processos.js';
import {
    abrirModalImpacto,
    adicionarAtividade,
    adicionarConhecimento,
    editarAtividade,
    fecharModalImpacto,
    navegarParaAtividades,
    navegarParaAtividadesVisualizacao,
    removerAtividade,
    verificarBotaoImpactoDireto,
    verificarBotaoImpactoDropdown
} from './helpers/helpers-atividades.js';
import {fazerLogout, limparNotificacoes, navegarParaSubprocesso} from './helpers/helpers-navegacao.js';
import {aceitarCadastroMapeamento, acessarSubprocessoChefeDireto, acessarSubprocessoGestor} from './helpers/helpers-analise.js';
import {criarCompetencia, navegarParaMapa} from './helpers/helpers-mapas.js';

test.describe.serial('CDU-12 - Verificar impactos no mapa de competências', () => {
    const UNIDADE_ALVO = 'SECAO_211';
    const USUARIO_CHEFE = USUARIOS.CHEFE_SECAO_211.titulo;
    const SENHA_CHEFE = USUARIOS.CHEFE_SECAO_211.senha;
    const USUARIO_ADMIN = USUARIOS.ADMIN_1_PERFIL.titulo;
    const SENHA_ADMIN = USUARIOS.ADMIN_1_PERFIL.senha;

    const timestamp = Date.now();
    const descProcessoMapeamento = `AAA Mapeamento CDU-12 ${timestamp}`;
    const descProcessoRevisao = `AAA Revisão CDU-12 ${timestamp}`;
    let codProcessoMapeamento: number;
    let processoRevisaoId: number;

    // ========================================================================
    // PREPARAÇÃO - Criar Mapa Vigente e Iniciar Revisão
    // ========================================================================

    test('Preparacao 1: Setup Mapeamento (Atividades, Competências, Homologação)', async ({page, cleanupAutomatico}) => {
        test.slow();
        // 1. Criar Processo Mapeamento
        await login(page, USUARIO_ADMIN, SENHA_ADMIN);
        await criarProcesso(page, {
            descricao: descProcessoMapeamento,
            tipo: 'MAPEAMENTO',
            diasLimite: 30,
            unidade: UNIDADE_ALVO,
            expandir: ['SECRETARIA_2', 'COORD_21']
        });

        const linhaProcesso = page.getByTestId('tbl-processos').locator('tr', {has: page.getByText(descProcessoMapeamento)});
        await expect(linhaProcesso).toBeVisible();
        await linhaProcesso.click();
        await expect(page).toHaveURL(/\/processo\/cadastro/);
        codProcessoMapeamento = Number.parseInt(new RegExp(/(?:codProcesso=|\/cadastro\/)(\d+)/).exec(page.url())?.[1] || '0');
        if (codProcessoMapeamento > 0) cleanupAutomatico.registrar(codProcessoMapeamento);

        await page.getByTestId('btn-processo-iniciar').click();
        await page.getByTestId('btn-iniciar-processo-confirmar').click();
        await limparNotificacoes(page);

        // 2. Chefe preenche atividades
        await fazerLogout(page);
        await login(page, USUARIO_CHEFE, SENHA_CHEFE);
        await acessarSubprocessoChefeDireto(page, descProcessoMapeamento, UNIDADE_ALVO);
        await navegarParaAtividades(page);

        // Atividade 1
        await adicionarAtividade(page, `Atividade Base 1 ${timestamp}`);
        await adicionarConhecimento(page, `Atividade Base 1 ${timestamp}`, 'Conhecimento Base 1A');

        // Atividade 2 (será modificada na revisão)
        await adicionarAtividade(page, `Atividade Base 2 ${timestamp}`);
        await adicionarConhecimento(page, `Atividade Base 2 ${timestamp}`, 'Conhecimento Base 2A');

        // Atividade 3 (será removida na revisão)
        await adicionarAtividade(page, `Atividade Base 3 ${timestamp}`);
        await adicionarConhecimento(page, `Atividade Base 3 ${timestamp}`, 'Conhecimento Base 3A');

        await page.getByTestId('btn-cad-atividades-disponibilizar').click();
        await page.getByTestId('btn-confirmar-disponibilizacao').click();

        // 3. Aceites intermediários e Homologação do Cadastro
        await login(page, USUARIOS.GESTOR_COORD_21.titulo, USUARIOS.GESTOR_COORD_21.senha);
        await acessarSubprocessoGestor(page, descProcessoMapeamento, UNIDADE_ALVO);
        await navegarParaAtividadesVisualizacao(page);
        await aceitarCadastroMapeamento(page, 'Aceite intermediário COORD_21');

        await loginComPerfil(page, '212121', 'senha', 'GESTOR - SECRETARIA_2');
        await acessarSubprocessoGestor(page, descProcessoMapeamento, UNIDADE_ALVO);
        await navegarParaAtividadesVisualizacao(page);
        await aceitarCadastroMapeamento(page, 'Aceite intermediário SECRETARIA_2');

        await fazerLogout(page);
        await login(page, USUARIO_ADMIN, SENHA_ADMIN);
        await expect(page.getByTestId('tbl-processos').locator('tr', {has: page.getByText(descProcessoMapeamento)})).toBeVisible();
        await page.getByTestId('tbl-processos').locator('tr', {has: page.getByText(descProcessoMapeamento)}).click();
        await expect(page).toHaveURL(/\/processo\/\d+/);
        await navegarParaSubprocesso(page, UNIDADE_ALVO);
        await navegarParaAtividadesVisualizacao(page);
        await page.getByTestId('btn-acao-analisar-principal').click();
        await page.getByTestId('inp-aceite-cadastro-obs').fill('Homologado sem ressalvas');
        await page.getByTestId('btn-aceite-cadastro-confirmar').click();
        await limparNotificacoes(page);

        // Após homologação, redireciona para Detalhes do subprocesso (CDU-13 passo 11.7)
        await expect(page).toHaveURL(/\/processo\/\d+\/\w+$/);

        // 4. Admin cria competências (Mapa)
        // Já está na tela de Detalhes do subprocesso
        // Verificar se o card de mapa EDITAVEL está visível (confirma permissão/status correto)
        await navegarParaMapa(page);

        // Aguardar carregamento da tela do mapa (título da unidade)
        await expect(page.getByTestId('subprocesso-header__txt-header-unidade')).toBeVisible();

        // Competência 1 ligada a Atividade 1
        await criarCompetencia(page, `Competência 1 ${timestamp}`, [`Atividade Base 1 ${timestamp}`]);
        await limparNotificacoes(page);

        // Competência 2 ligada a Atividade 2
        await criarCompetencia(page, `Competência 2 ${timestamp}`, [`Atividade Base 2 ${timestamp}`]);
        await limparNotificacoes(page);

        // Competência 3 ligada a Atividade 3
        await criarCompetencia(page, `Competência 3 ${timestamp}`, [`Atividade Base 3 ${timestamp}`]);
        await limparNotificacoes(page);

        // Disponibilizar Mapa
        await page.getByTestId('btn-cad-mapa-disponibilizar').click();
        await page.getByTestId('inp-disponibilizar-mapa-data').fill('2030-12-31');
        await page.getByTestId('btn-disponibilizar-mapa-confirmar').click();

        // 5. Chefe Valida, Aceites Intermediários e Admin Homologa (Finalizar Mapeamento)
        await fazerLogout(page);
        await login(page, USUARIO_CHEFE, SENHA_CHEFE);
        await acessarSubprocessoChefeDireto(page, descProcessoMapeamento, UNIDADE_ALVO);
        await navegarParaMapa(page);
        await limparNotificacoes(page);
        await page.getByTestId('btn-mapa-validar').click();
        await page.getByTestId('btn-validar-mapa-confirmar').click();

        // Aceite COORD_21 (Mapa)
        await login(page, USUARIOS.GESTOR_COORD_21.titulo, USUARIOS.GESTOR_COORD_21.senha);
        await page.getByTestId('tbl-processos').getByText(descProcessoMapeamento).first().click();
        await navegarParaSubprocesso(page, UNIDADE_ALVO);
        await navegarParaMapa(page);
        await page.getByTestId('btn-mapa-homologar-aceite').click();
        await page.getByTestId('btn-aceite-mapa-confirmar').click();

        // Aceite SECRETARIA_2 (Mapa)
        await loginComPerfil(page, '212121', 'senha', 'GESTOR - SECRETARIA_2');
        await page.getByTestId('tbl-processos').getByText(descProcessoMapeamento).first().click();
        await navegarParaSubprocesso(page, UNIDADE_ALVO);
        await navegarParaMapa(page);
        await page.getByTestId('btn-mapa-homologar-aceite').click();
        await page.getByTestId('btn-aceite-mapa-confirmar').click();

        await fazerLogout(page);
        await login(page, USUARIO_ADMIN, SENHA_ADMIN);
        await expect(page.getByTestId('tbl-processos').locator('tr', {has: page.getByText(descProcessoMapeamento)})).toBeVisible();
        await page.getByTestId('tbl-processos').locator('tr', {has: page.getByText(descProcessoMapeamento)}).click();
        await expect(page).toHaveURL(/\/processo\/\d+/);
        await navegarParaSubprocesso(page, UNIDADE_ALVO);
        await navegarParaMapa(page);
        await limparNotificacoes(page);
        await page.getByTestId('btn-mapa-homologar-aceite').click();
        await expect(page.getByTestId('btn-aceite-mapa-confirmar')).toBeVisible();
        await page.getByTestId('btn-aceite-mapa-confirmar').click();
        await expect(page.getByTestId('btn-aceite-mapa-confirmar')).toBeHidden();

        // Finalizar Processo
        await page.goto('/painel');
        await expect(page.getByTestId('tbl-processos').locator('tr', {has: page.getByText(descProcessoMapeamento)})).toBeVisible();
        await page.getByTestId('tbl-processos').locator('tr', {has: page.getByText(descProcessoMapeamento)}).click();
        await page.getByTestId('btn-processo-finalizar').click();
        await page.getByTestId('btn-finalizar-processo-confirmar').click();
    });

    test('Preparacao 2: Iniciar Processo de Revisão', async ({page, autenticadoComoAdmin, cleanupAutomatico}) => {
        
        await criarProcesso(page, {
            descricao: descProcessoRevisao,
            tipo: 'REVISAO',
            diasLimite: 30,
            unidade: UNIDADE_ALVO,
            expandir: ['SECRETARIA_2', 'COORD_21']
        });

        const linhaProcesso = page.getByTestId('tbl-processos').locator('tr', {has: page.getByText(descProcessoRevisao)});
        await expect(linhaProcesso).toBeVisible();
        await linhaProcesso.click();
        await expect(page).toHaveURL(/\/processo\/cadastro/);
        processoRevisaoId = Number.parseInt(new RegExp(/(?:codProcesso=|\/cadastro\/)(\d+)/).exec(page.url())?.[1] || '0');
        if (processoRevisaoId > 0) cleanupAutomatico.registrar(processoRevisaoId);

        await page.getByTestId('btn-processo-iniciar').click();
        await page.getByTestId('btn-iniciar-processo-confirmar').click();
    });

    // ========================================================================
    // TESTES CDU-12
    // ========================================================================

    test('Cenario 1: Verificar Sem Impactos (Estado Inicial)', async ({page, autenticadoComoChefeSecao211}) => {
        

        await acessarSubprocessoChefeDireto(page, descProcessoRevisao, UNIDADE_ALVO);

        await navegarParaAtividades(page);

        // 1. Verificar presença do botão
        await verificarBotaoImpactoDropdown(page);

        // 2. Abrir modal
        await abrirModalImpacto(page);
        await expect(page.getByText('Nenhum impacto detectado no mapa.')).toBeVisible();
    });

    test('Cenario 2: Verificar Impacto de Inclusão de Atividade', async ({page, autenticadoComoChefeSecao211}) => {
        
        await acessarSubprocessoChefeDireto(page, descProcessoRevisao, UNIDADE_ALVO);
        await navegarParaAtividades(page);

        // Adicionar nova atividade
        const novaAtividade = `Atividade Nova Revisão ${timestamp}`;
        await adicionarAtividade(page, novaAtividade);
        await adicionarConhecimento(page, novaAtividade, 'Conhecimento Novo');

        // Verificar impacto
        await abrirModalImpacto(page);

        // Verificar seção de atividades inseridas
        const modal = page.locator('.modal-content');
        await expect(modal.getByText('Atividades inseridas')).toBeVisible();
        await expect(modal.getByText(novaAtividade)).toBeVisible();
        // A modal não lista conhecimentos, apenas competencias vinculadas

        await fecharModalImpacto(page);
    });

    test('Cenario 3: Verificar Impacto de Alteração em Atividade (Impacta Competência)', async ({page, autenticadoComoChefeSecao211}) => {
        
        await acessarSubprocessoChefeDireto(page, descProcessoRevisao, UNIDADE_ALVO);
        await navegarParaAtividades(page);

        // Editar atividade existente (Atividade Base 2)
        const descOriginal = `Atividade Base 2 ${timestamp}`;
        const descNova = `Atividade Base 2 Editada ${timestamp}`;
        await editarAtividade(page, descOriginal, descNova);

        // Verificar impacto
        await abrirModalImpacto(page);

        // Verificar seção de competências impactadas
        const modal = page.locator('.modal-content');
        await expect(modal.getByText('Competências impactadas')).toBeVisible();

        // Deve mostrar a Competência 2
        await expect(modal.getByText(`Competência 2 ${timestamp}`)).toBeVisible();

        // Deve mostrar o detalhe da alteração. O texto exato pode variar na implementação, mas deve conter a descrição da atividade.
        await expect(modal.getByText(descNova)).toBeVisible();
        await fecharModalImpacto(page);
    });

    test('Cenario 4: Verificar Impacto de Remoção de Atividade (Impacta Competência)', async ({page, autenticadoComoChefeSecao211}) => {
        
        await acessarSubprocessoChefeDireto(page, descProcessoRevisao, UNIDADE_ALVO);
        await navegarParaAtividades(page);

        // Remover atividade (Atividade Base 3)
        const descRemovida = `Atividade Base 3 ${timestamp}`;
        await removerAtividade(page, descRemovida);

        // Verificar impacto
        await abrirModalImpacto(page);

        const modal = page.locator('.modal-content');
        await expect(modal.getByText('Competências impactadas')).toBeVisible();

        // Deve mostrar a Competência 3
        await expect(modal.getByText(`Competência 3 ${timestamp}`)).toBeVisible();

        // Deve indicar que atividade foi removida
        await expect(modal.getByTestId('lista-atividades-removidas').getByText(descRemovida)).toBeVisible();
        // await expect(modal.getByText(/removida/i)).toBeVisible();

        await fecharModalImpacto(page);
    });

    test('Cenario 5: Verificar visualização pelo Admin (Somente Leitura)', async ({page, autenticadoComoChefeSecao211}) => {
        // Chefe disponibiliza a revisão
        
        await acessarSubprocessoChefeDireto(page, descProcessoRevisao, UNIDADE_ALVO);
        await navegarParaAtividades(page);
        await page.getByTestId('btn-cad-atividades-disponibilizar').click();
        await page.getByTestId('btn-confirmar-disponibilizacao').click();

        // Admin acessa
        await fazerLogout(page);
        await login(page, USUARIO_ADMIN, SENHA_ADMIN);
        
        await expect(page.getByTestId('tbl-processos').locator('tr', {has: page.getByText(descProcessoRevisao)})).toBeVisible();
        await page.getByTestId('tbl-processos').locator('tr', {has: page.getByText(descProcessoRevisao)}).click();
        await expect(page).toHaveURL(/\/processo\/\d+/);
        await limparNotificacoes(page); // Limpar possível toast de "Sucesso ao criar/iniciar"
        await navegarParaSubprocesso(page, UNIDADE_ALVO);
        // Acessar visualização
        await navegarParaAtividadesVisualizacao(page);

        // Verificar botão de impacto
        await verificarBotaoImpactoDireto(page);

        // Abrir e verificar conteúdo (deve ter os acumulados dos cenários anteriores)
        await abrirModalImpacto(page);
        const modal = page.locator('.modal-content');

        // Inserida
        await expect(modal.getByText(`Atividade Nova Revisão ${timestamp}`)).toBeVisible();
        // Alterada (Competência 2)
        await expect(modal.getByText(`Competência 2 ${timestamp}`)).toBeVisible();
        // Removida (Competência 3)
        await expect(modal.getByText(`Competência 3 ${timestamp}`)).toBeVisible();

        await fecharModalImpacto(page);
    });
});
