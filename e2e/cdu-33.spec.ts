import {expect, test} from './fixtures/complete-fixtures.js';
import {criarProcesso, extrairProcessoId} from './helpers/helpers-processos.js';
import {
    adicionarAtividade,
    adicionarConhecimento,
    navegarParaAtividades,
    navegarParaAtividadesVisualizacao
} from './helpers/helpers-atividades.js';
import {criarCompetencia, disponibilizarMapa, navegarParaMapa} from './helpers/helpers-mapas.js';
import {verificarPaginaPainel} from './helpers/helpers-navegacao.js';
import {
    aceitarCadastroMapeamento,
    aceitarRevisao,
    acessarSubprocessoChefeDireto,
    acessarSubprocessoGestor,
    homologarCadastroMapeamento,
    homologarCadastroRevisaoComImpacto
} from './helpers/helpers-analise.js';
import {login, loginComPerfil, USUARIOS} from './helpers/helpers-auth.js';

/**
 * CDU-33 - Reabrir revisão de cadastro
 *
 * Ator: ADMIN
 *
 * Pré-condições:
 * - Subprocesso de revisão com cadastro homologado
 */
test.describe.serial('CDU-33 - Reabrir revisão de cadastro', () => {
    const UNIDADE_ALVO = 'SECAO_212';
    const timestamp = Date.now();
    const descMapeamento = `Mapeamento Pre-CDU-33 ${timestamp}`;
    const descRevisao = `Revisão CDU-33 ${timestamp}`;

    // ========================================================================
    // PREPARAÇÃO 0 - CRIAR MAPA VIGENTE
    // ========================================================================

    test('Preparacao 0: Criar e finalizar Mapeamento', async ({page, autenticadoComoAdmin, cleanupAutomatico}) => {
        test.setTimeout(90000);

        await criarProcesso(page, {
            descricao: descMapeamento,
            tipo: 'MAPEAMENTO',
            diasLimite: 30,
            unidade: UNIDADE_ALVO,
            expandir: ['SECRETARIA_2', 'COORD_21']
        });
        const linhaProcesso = page.getByTestId('tbl-processos').locator('tr', {has: page.getByText(descMapeamento)});
        await linhaProcesso.click();
        const pid = await extrairProcessoId(page);
        if (pid > 0) cleanupAutomatico.registrar(pid);

        await page.getByTestId('btn-processo-iniciar').click();
        await page.getByTestId('btn-iniciar-processo-confirmar').click();

        // 2. Chefe (SECAO_212) disponibiliza cadastro
        await login(page, USUARIOS.CHEFE_SECAO_212.titulo, USUARIOS.CHEFE_SECAO_212.senha);
        await acessarSubprocessoChefeDireto(page, descMapeamento);
        await navegarParaAtividades(page);
        await adicionarAtividade(page, `Ativ Map ${timestamp}`);
        await adicionarConhecimento(page, `Ativ Map ${timestamp}`, 'Conhecimento Unico Map');
        await page.getByTestId('btn-cad-atividades-disponibilizar').click();
        await page.getByTestId('btn-confirmar-disponibilizacao').click();

        // 3. Gestores aceitam cadastro
        await login(page, USUARIOS.GESTOR_COORD_21.titulo, USUARIOS.GESTOR_COORD_21.senha);
        await acessarSubprocessoGestor(page, descMapeamento, UNIDADE_ALVO);
        await navegarParaAtividadesVisualizacao(page);
        await aceitarCadastroMapeamento(page);

        await loginComPerfil(page, USUARIOS.CHEFE_SECRETARIA_2.titulo, USUARIOS.CHEFE_SECRETARIA_2.senha, 'GESTOR - SECRETARIA_2');
        await acessarSubprocessoGestor(page, descMapeamento, UNIDADE_ALVO);
        await navegarParaAtividadesVisualizacao(page);
        await aceitarCadastroMapeamento(page);

        // 4. Admin homologa cadastro e disponibiliza mapa
        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
        await acessarSubprocessoChefeDireto(page, descMapeamento, UNIDADE_ALVO);
        await navegarParaAtividadesVisualizacao(page);
        await homologarCadastroMapeamento(page);

        await navegarParaMapa(page);
        await criarCompetencia(page, `Comp Map ${timestamp}`, [`Ativ Map ${timestamp}`]);
        await disponibilizarMapa(page, '2030-12-31');

        // 5. Chefe valida mapa
        await login(page, USUARIOS.CHEFE_SECAO_212.titulo, USUARIOS.CHEFE_SECAO_212.senha);
        await acessarSubprocessoChefeDireto(page, descMapeamento);
        await navegarParaMapa(page);
        await page.getByTestId('btn-mapa-validar').click();
        await page.getByTestId('btn-validar-mapa-confirmar').click();

        // 6. Gestores aceitam mapa
        await login(page, USUARIOS.GESTOR_COORD_21.titulo, USUARIOS.GESTOR_COORD_21.senha);
        await acessarSubprocessoGestor(page, descMapeamento, UNIDADE_ALVO);
        await navegarParaMapa(page);
        await page.getByTestId('btn-mapa-homologar-aceite').click();
        await page.getByTestId('btn-aceite-mapa-confirmar').click();

        await loginComPerfil(page, USUARIOS.CHEFE_SECRETARIA_2.titulo, USUARIOS.CHEFE_SECRETARIA_2.senha, 'GESTOR - SECRETARIA_2');
        await acessarSubprocessoGestor(page, descMapeamento, UNIDADE_ALVO);
        await navegarParaMapa(page);
        await page.getByTestId('btn-mapa-homologar-aceite').click();
        await page.getByTestId('btn-aceite-mapa-confirmar').click();

        // 7. Admin homologa mapa e finaliza processo
        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
        await acessarSubprocessoChefeDireto(page, descMapeamento, UNIDADE_ALVO);
        await navegarParaMapa(page);
        await page.getByTestId('btn-mapa-homologar-aceite').click();
        await page.getByTestId('btn-aceite-mapa-confirmar').click();

        await page.goto('/painel');
        await page.getByTestId('tbl-processos').getByText(descMapeamento).first().click();
        await page.getByTestId('btn-processo-finalizar').click();
        await page.getByTestId('btn-finalizar-processo-confirmar').click();
    });

    // ========================================================================
    // PREPARAÇÃO REVISÃO
    // ========================================================================

    test('Preparacao 1: Admin cria e inicia processo de REVISAO', async ({
                                                                             page,
                                                                             autenticadoComoAdmin,
                                                                             cleanupAutomatico
                                                                         }) => {
        await criarProcesso(page, {
            descricao: descRevisao,
            tipo: 'REVISAO',
            diasLimite: 30,
            unidade: UNIDADE_ALVO,
            expandir: ['SECRETARIA_2', 'COORD_21']
        });

        const linhaProcesso = page.getByTestId('tbl-processos').locator('tr', {has: page.getByText(descRevisao)});
        await linhaProcesso.click();

        const processoId = await extrairProcessoId(page);
        if (processoId > 0) cleanupAutomatico.registrar(processoId);

        await page.getByTestId('btn-processo-iniciar').click();
        await page.getByTestId('btn-iniciar-processo-confirmar').click();

        await verificarPaginaPainel(page);
    });

    test('Preparacao 2: Chefe disponibiliza revisão de cadastro', async ({page, autenticadoComoChefeSecao212}) => {
        await acessarSubprocessoChefeDireto(page, descRevisao);
        await navegarParaAtividades(page);

        await adicionarAtividade(page, `Atividade Rev ${timestamp}`);
        await adicionarConhecimento(page, `Atividade Rev ${timestamp}`, 'Conhecimento Rev');

        await page.getByTestId('btn-cad-atividades-disponibilizar').click();
        await page.getByTestId('btn-confirmar-disponibilizacao').click();

        await verificarPaginaPainel(page);
    });

    test('Preparacao 3: Gestores e ADMIN aceitam revisão', async ({page, autenticadoComoGestorCoord21}) => {
        // Gestor COORD_21
        await acessarSubprocessoGestor(page, descRevisao, UNIDADE_ALVO);
        await navegarParaAtividadesVisualizacao(page);
        await aceitarRevisao(page);

        // Gestor SECRETARIA_2
        await loginComPerfil(page, USUARIOS.CHEFE_SECRETARIA_2.titulo, USUARIOS.CHEFE_SECRETARIA_2.senha, 'GESTOR - SECRETARIA_2');
        await acessarSubprocessoGestor(page, descRevisao, UNIDADE_ALVO);
        await navegarParaAtividadesVisualizacao(page);
        await aceitarRevisao(page);

        // ADMIN homologa
        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
        await acessarSubprocessoChefeDireto(page, descRevisao, UNIDADE_ALVO);
        await navegarParaAtividadesVisualizacao(page);
        await homologarCadastroRevisaoComImpacto(page);

        await expect(page.getByTestId('subprocesso-header__txt-situacao'))
            .toHaveText(/Revisão do cadastro homologada/i);
    });

    // ========================================================================
    // TESTES PRINCIPAIS
    // ========================================================================

    test('Cenários CDU-33: ADMIN reabre revisão de cadastro', async ({page, autenticadoComoAdmin}) => {
        // Cenario 1 & 2: Navegação e visualização do botão
        await acessarSubprocessoChefeDireto(page, descRevisao, UNIDADE_ALVO);

        const btnReabrir = page.getByTestId('btn-reabrir-revisao');
        await expect(btnReabrir).toBeVisible();
        await expect(btnReabrir).toBeEnabled();

        // Cenario 3: Abrir modal
        await btnReabrir.click();

        const modal = page.getByRole('dialog');
        await expect(modal).toBeVisible();
        await expect(modal.getByRole('heading', {name: /Reabrir Revisão/i})).toBeVisible();

        // Cenario 4: Confirmar reabertura
        await page.getByTestId('inp-justificativa-reabrir').fill('Ajuste necessário');
        await page.getByTestId('btn-confirmar-reabrir').click();

        await expect(page.getByText(/Revisão de cadastro reaberta/i).first()).toBeVisible();
        await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Revisão em andamento/i);
    });
});
