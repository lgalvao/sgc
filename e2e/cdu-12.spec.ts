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
import {
    aceitarCadastroMapeamento,
    acessarSubprocessoChefeDireto,
    acessarSubprocessoGestor
} from './helpers/helpers-analise.js';
import {criarCompetencia, navegarParaMapa} from './helpers/helpers-mapas.js';

test.describe.serial('CDU-12 - Verificar impactos no mapa de competências', () => {
    const UNIDADE_ALVO = 'SECAO_211';
    const USUARIO_CHEFE = USUARIOS.CHEFE_SECAO_211.titulo;
    const SENHA_CHEFE = USUARIOS.CHEFE_SECAO_211.senha;
    const USUARIO_ADMIN = USUARIOS.ADMIN_1_PERFIL.titulo;
    const SENHA_ADMIN = USUARIOS.ADMIN_1_PERFIL.senha;
    const USUARIO_GESTOR_COORD = USUARIOS.GESTOR_COORD_21.titulo;
    const SENHA_GESTOR_COORD = USUARIOS.GESTOR_COORD_21.senha;

    const timestamp = Date.now();
    const descProcessoMapeamento = `AAA Mapeamento CDU-12 ${timestamp}`;
    const descProcessoRevisao = `AAA Revisão CDU-12 ${timestamp}`;
    let codProcessoMapeamento: number;
    let processoRevisaoId: number;

    // ========================================================================
    // PREPARAÇÃO - Criar Mapa Vigente e Iniciar Revisão
    // ========================================================================

    test('Preparacao: Setup Mapeamento e Início da Revisão', async ({page, cleanupAutomatico}) => {
        test.slow();
        // 1. Criar Processo Mapeamento (ADMIN)
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
        codProcessoMapeamento = Number.parseInt(new RegExp(/(?:codProcesso=|\/cadastro\/)(\d+)/).exec(page.url())?.[1] || '0');
        if (codProcessoMapeamento > 0) cleanupAutomatico.registrar(codProcessoMapeamento);

        await page.getByTestId('btn-processo-iniciar').click();
        await page.getByTestId('btn-iniciar-processo-confirmar').click();
        await limparNotificacoes(page);

        // 2. FASE CADASTRO: Chefe preenche atividades e disponibiliza (Localização: SECAO_211 -> COORD_21)
        await fazerLogout(page);
        await login(page, USUARIO_CHEFE, SENHA_CHEFE);
        await acessarSubprocessoChefeDireto(page, descProcessoMapeamento, UNIDADE_ALVO);
        await navegarParaAtividades(page);

        await adicionarAtividade(page, `Atividade Base 1 ${timestamp}`);
        await adicionarConhecimento(page, `Atividade Base 1 ${timestamp}`, 'Conhecimento Base 1A');
        await adicionarAtividade(page, `Atividade Base 2 ${timestamp}`);
        await adicionarConhecimento(page, `Atividade Base 2 ${timestamp}`, 'Conhecimento Base 2A');
        await adicionarAtividade(page, `Atividade Base 3 ${timestamp}`);
        await adicionarConhecimento(page, `Atividade Base 3 ${timestamp}`, 'Conhecimento Base 3A');

        await page.getByTestId('btn-cad-atividades-disponibilizar').click();
        await page.getByTestId('btn-confirmar-disponibilizacao').click();

        // 3. FASE CADASTRO: Aceites e Homologação (Tramitação pela hierarquia)
        // 3.1 GESTOR COORD_21 aceita (Localização: COORD_21 -> SECRETARIA_2)
        await login(page, USUARIO_GESTOR_COORD, SENHA_GESTOR_COORD);
        await acessarSubprocessoGestor(page, descProcessoMapeamento, UNIDADE_ALVO);
        await navegarParaAtividadesVisualizacao(page);
        await aceitarCadastroMapeamento(page, 'Aceite COORD_21');

        // 3.2 GESTOR SECRETARIA_2 aceita (Localização: SECRETARIA_2 -> ADMIN)
        await loginComPerfil(page, '212121', 'senha', 'GESTOR - SECRETARIA_2');
        await acessarSubprocessoGestor(page, descProcessoMapeamento, UNIDADE_ALVO);
        await navegarParaAtividadesVisualizacao(page);
        await aceitarCadastroMapeamento(page, 'Aceite SECRETARIA_2');

        // 3.3 ADMIN homologa (Localização: ADMIN)
        await login(page, USUARIO_ADMIN, SENHA_ADMIN);
        await page.getByTestId('tbl-processos').locator('tr', {has: page.getByText(descProcessoMapeamento)}).click();
        await navegarParaSubprocesso(page, UNIDADE_ALVO);
        await navegarParaAtividadesVisualizacao(page);
        await page.getByTestId('btn-acao-analisar-principal').click();
        await page.getByTestId('btn-aceite-cadastro-confirmar').click();
        
        // 4. FASE MAPA: Admin cria competências e disponibiliza (Localização: ADMIN -> SECAO_211)
        await navegarParaMapa(page);
        await criarCompetencia(page, `Competência 1 ${timestamp}`, [`Atividade Base 1 ${timestamp}`]);
        await criarCompetencia(page, `Competência 2 ${timestamp}`, [`Atividade Base 2 ${timestamp}`]);
        await criarCompetencia(page, `Competência 3 ${timestamp}`, [`Atividade Base 3 ${timestamp}`]);

        await page.getByTestId('btn-cad-mapa-disponibilizar').click();
        await page.getByTestId('inp-disponibilizar-mapa-data').fill('2030-12-31');
        await page.getByTestId('btn-disponibilizar-mapa-confirmar').click();

        // 5. FASE MAPA: Validação e Homologação (Tramitação pela hierarquia)
        // 5.1 CHEFE valida (Localização: SECAO_211 -> COORD_21)
        await fazerLogout(page);
        await login(page, USUARIO_CHEFE, SENHA_CHEFE);
        await acessarSubprocessoChefeDireto(page, descProcessoMapeamento, UNIDADE_ALVO);
        await navegarParaMapa(page);
        await page.getByTestId('btn-mapa-validar').click();
        await page.getByTestId('btn-validar-mapa-confirmar').click();

        // 5.2 GESTOR COORD_21 aceita (Localização: COORD_21 -> SECRETARIA_2)
        await login(page, USUARIO_GESTOR_COORD, SENHA_GESTOR_COORD);
        await page.getByTestId('tbl-processos').locator('tr', {has: page.getByText(descProcessoMapeamento)}).click();
        await navegarParaSubprocesso(page, UNIDADE_ALVO);
        await navegarParaMapa(page);
        await page.getByTestId('btn-mapa-homologar-aceite').click();
        await page.getByTestId('btn-aceite-mapa-confirmar').click();

        // 5.3 GESTOR SECRETARIA_2 aceita (Localização: SECRETARIA_2 -> ADMIN)
        await loginComPerfil(page, '212121', 'senha', 'GESTOR - SECRETARIA_2');
        await page.getByTestId('tbl-processos').locator('tr', {has: page.getByText(descProcessoMapeamento)}).click();
        await navegarParaSubprocesso(page, UNIDADE_ALVO);
        await navegarParaMapa(page);
        await page.getByTestId('btn-mapa-homologar-aceite').click();
        await page.getByTestId('btn-aceite-mapa-confirmar').click();

        // 5.4 ADMIN homologa (Localização: ADMIN)
        await login(page, USUARIO_ADMIN, SENHA_ADMIN);
        await page.getByTestId('tbl-processos').locator('tr', {has: page.getByText(descProcessoMapeamento)}).click();
        await navegarParaSubprocesso(page, UNIDADE_ALVO);
        await navegarParaMapa(page);
        await page.getByTestId('btn-mapa-homologar-aceite').click();
        await page.getByTestId('btn-aceite-mapa-confirmar').click();

        // Finalizar Processo
        await page.goto('/painel');
        await page.getByTestId('tbl-processos').locator('tr', {has: page.getByText(descProcessoMapeamento)}).click();
        await page.getByTestId('btn-processo-finalizar').click();
        await page.getByTestId('btn-finalizar-processo-confirmar').click();

        // 6. Iniciar Processo de Revisão
        await criarProcesso(page, {
            descricao: descProcessoRevisao,
            tipo: 'REVISAO',
            diasLimite: 30,
            unidade: UNIDADE_ALVO,
            expandir: ['SECRETARIA_2', 'COORD_21']
        });
        await page.getByTestId('tbl-processos').locator('tr', {has: page.getByText(descProcessoRevisao)}).click();
        processoRevisaoId = Number.parseInt(new RegExp(/(?:codProcesso=|\/cadastro\/)(\d+)/).exec(page.url())?.[1] || '0');
        if (processoRevisaoId > 0) cleanupAutomatico.registrar(processoRevisaoId);
        await page.getByTestId('btn-processo-iniciar').click();
        await page.getByTestId('btn-iniciar-processo-confirmar').click();
    });

    // ========================================================================
    // FLUXO DE VERIFICAÇÃO DE IMPACTOS (Tramitação)
    // ========================================================================

    test('Fluxo CHEFE: Realizar alterações e verificar impactos', async ({page}) => {
        // Debbie Harry possui apenas 1 perfil
        await login(page, USUARIO_CHEFE, SENHA_CHEFE);
        await acessarSubprocessoChefeDireto(page, descProcessoRevisao, UNIDADE_ALVO);
        await navegarParaAtividades(page);

        // 1. Estado Inicial (Sem impactos)
        await verificarBotaoImpactoDropdown(page);
        await abrirModalImpacto(page);
        await expect(page.getByText('Nenhum impacto no mapa da unidade.')).toBeVisible();
        await fecharModalImpacto(page);

        // 2. Inclusão
        const novaAtividade = `Atividade Nova Revisão ${timestamp}`;
        const novoConhecimento = `Conhecimento Novo ${timestamp}`;
        await adicionarAtividade(page, novaAtividade);
        await adicionarConhecimento(page, novaAtividade, novoConhecimento);
        await abrirModalImpacto(page);
        await expect(page.locator('.modal-content').getByText(novaAtividade)).toBeVisible();
        await expect(page.locator('.modal-content').getByText(novoConhecimento)).toBeVisible();
        await fecharModalImpacto(page);

        // 3. Alteração (Atividade e Conhecimento)
        const descNova = `Atividade Base 2 Editada ${timestamp}`;
        await editarAtividade(page, `Atividade Base 2 ${timestamp}`, descNova);
        await adicionarConhecimento(page, `Atividade Base 1 ${timestamp}`, `Conhecimento Extra ${timestamp}`);
        await abrirModalImpacto(page);
        await expect(page.locator('.modal-content').getByText(`Competência 2 ${timestamp}`)).toBeVisible();
        await expect(page.locator('.modal-content').getByText(`Competência 1 ${timestamp}`)).toBeVisible();
        await fecharModalImpacto(page);

        // 4. Remoção
        await removerAtividade(page, `Atividade Base 3 ${timestamp}`);
        await abrirModalImpacto(page);
        await expect(page.locator('.modal-content').getByText(`Competência 3 ${timestamp}`)).toBeVisible();
        await fecharModalImpacto(page);

        // 5. Disponibilizar (Localização: SECAO_211 -> COORD_21)
        await page.getByTestId('btn-cad-atividades-disponibilizar').click();
        await page.getByTestId('btn-confirmar-disponibilizacao').click();
    });

    test('Fluxo GESTOR e ADMIN: Verificar visualização de impactos', async ({page}) => {
        // 1. GESTOR verifica na visualização (Localização: COORD_21)
        await login(page, USUARIO_GESTOR_COORD, SENHA_GESTOR_COORD);
        await acessarSubprocessoGestor(page, descProcessoRevisao, UNIDADE_ALVO);
        await navegarParaAtividadesVisualizacao(page);
        await verificarBotaoImpactoDireto(page);
        await abrirModalImpacto(page);
        await expect(page.locator('.modal-content').getByText('Competências impactadas')).toBeVisible();
        await fecharModalImpacto(page);
        await aceitarCadastroMapeamento(page, 'Aceite revisão COORD_21');

        // 2. ADMIN verifica na visualização (Após aceite de SECRETARIA_2 para chegar em ADMIN)
        // 2.1 Aceite SECRETARIA_2 (Localização: SECRETARIA_2 -> ADMIN)
        await loginComPerfil(page, '212121', 'senha', 'GESTOR - SECRETARIA_2');
        await acessarSubprocessoGestor(page, descProcessoRevisao, UNIDADE_ALVO);
        await navegarParaAtividadesVisualizacao(page);
        await aceitarCadastroMapeamento(page, 'Aceite revisão SECRETARIA_2');

        // 2.2 ADMIN verifica (Localização: ADMIN)
        await login(page, USUARIO_ADMIN, SENHA_ADMIN);
        await page.getByTestId('tbl-processos').locator('tr', {has: page.getByText(descProcessoRevisao)}).click();
        await navegarParaSubprocesso(page, UNIDADE_ALVO);
        await navegarParaAtividadesVisualizacao(page);
        await abrirModalImpacto(page);
        await expect(page.locator('.modal-content').getByText('Competências impactadas')).toBeVisible();
        await fecharModalImpacto(page);

        // Homologar para liberar edição de mapa (Requisito 3.3)
        await page.getByTestId('btn-acao-analisar-principal').click();
        await page.getByTestId('btn-aceite-cadastro-confirmar').click();

        // 2.3 ADMIN verifica na Edição do Mapa (Requisito 3.3)
        await navegarParaMapa(page);
        await expect(page.getByTestId('cad-mapa__btn-impactos-mapa')).toBeVisible();
        await page.getByTestId('cad-mapa__btn-impactos-mapa').click();
        await expect(page.locator('.modal-content').getByText('Competências impactadas')).toBeVisible();
        await fecharModalImpacto(page);
    });
});
