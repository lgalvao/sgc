import {expect, test} from './fixtures/complete-fixtures.js';
import {criarProcesso, extrairProcessoId} from './helpers/helpers-processos.js';
import {
    adicionarAtividade,
    adicionarConhecimento,
    navegarParaAtividades,
    navegarParaAtividadesVisualizacao
} from './helpers/helpers-atividades.js';
import {criarCompetencia, disponibilizarMapa, navegarParaMapa} from './helpers/helpers-mapas.js';
import {fazerLogout, verificarPaginaPainel} from './helpers/helpers-navegacao.js';
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
        // 1. Criar Processo
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

        // 2. Chefe (SECAO_212) disponibiliza
        await login(page, USUARIOS.CHEFE_SECAO_212.titulo, USUARIOS.CHEFE_SECAO_212.senha);
        await acessarSubprocessoChefeDireto(page, descMapeamento);
        await navegarParaAtividades(page);
        await adicionarAtividade(page, `Ativ Map ${timestamp}`);
        await adicionarConhecimento(page, `Ativ Map ${timestamp}`, 'Conhecimento Unico Map');
        await page.getByTestId('btn-cad-atividades-disponibilizar').click();
        await page.getByTestId('btn-confirmar-disponibilizacao').click();

        // 3. Gestor COORD_21 aceita
        await login(page, USUARIOS.GESTOR_COORD_21.titulo, USUARIOS.GESTOR_COORD_21.senha);
        await acessarSubprocessoGestor(page, descMapeamento, UNIDADE_ALVO);
        await navegarParaAtividadesVisualizacao(page);
        await aceitarCadastroMapeamento(page);

        // 4. Gestor SECRETARIA_2 aceita
        await loginComPerfil(page, USUARIOS.CHEFE_SECRETARIA_2.titulo, USUARIOS.CHEFE_SECRETARIA_2.senha, 'GESTOR - SECRETARIA_2');
        await acessarSubprocessoGestor(page, descMapeamento, UNIDADE_ALVO);
        await navegarParaAtividadesVisualizacao(page);
        await aceitarCadastroMapeamento(page);

        // 5. Admin homologa cadastro
        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
        await acessarSubprocessoChefeDireto(page, descMapeamento, UNIDADE_ALVO);
        await navegarParaAtividadesVisualizacao(page);
        await homologarCadastroMapeamento(page);
        await fazerLogout(page);

        // 6. Admin cria mapa e disponibiliza
        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
        await acessarSubprocessoChefeDireto(page, descMapeamento, UNIDADE_ALVO);
        await navegarParaMapa(page);
        await criarCompetencia(page, `Comp Map ${timestamp}`, [`Ativ Map ${timestamp}`]);
        await disponibilizarMapa(page, '2030-12-31');
        await fazerLogout(page);

        // 7. Chefe valida mapa
        await login(page, USUARIOS.CHEFE_SECAO_212.titulo, USUARIOS.CHEFE_SECAO_212.senha);
        await acessarSubprocessoChefeDireto(page, descMapeamento);
        await navegarParaMapa(page);
        await page.getByTestId('btn-mapa-validar').click();
        await page.getByTestId('btn-validar-mapa-confirmar').click();
        await fazerLogout(page);

        // 8. Gestor COORD_21 aceita mapa
        await login(page, USUARIOS.GESTOR_COORD_21.titulo, USUARIOS.GESTOR_COORD_21.senha);
        await acessarSubprocessoGestor(page, descMapeamento, UNIDADE_ALVO);
        await navegarParaMapa(page);
        await page.getByTestId('btn-mapa-homologar-aceite').click();
        await page.getByTestId('btn-aceite-mapa-confirmar').click();

        // 9. Gestor SECRETARIA_2 aceita mapa
        await loginComPerfil(page, USUARIOS.CHEFE_SECRETARIA_2.titulo, USUARIOS.CHEFE_SECRETARIA_2.senha, 'GESTOR - SECRETARIA_2');
        await acessarSubprocessoGestor(page, descMapeamento, UNIDADE_ALVO);
        await navegarParaMapa(page);
        await page.getByTestId('btn-mapa-homologar-aceite').click();
        await page.getByTestId('btn-aceite-mapa-confirmar').click();

        // 10. Admin homologa mapa e finaliza processo
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

    test('Preparacao 1: Admin cria e inicia processo de REVISAO', async ({page, autenticadoComoAdmin, cleanupAutomatico}) => {
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

        // Adicionar uma atividade para ter algo na revisão
        await adicionarAtividade(page, `Atividade Rev ${timestamp}`);
        await adicionarConhecimento(page, `Atividade Rev ${timestamp}`, 'Conhecimento Rev');

        await page.getByTestId('btn-cad-atividades-disponibilizar').click();
        await page.getByTestId('btn-confirmar-disponibilizacao').click();

        await verificarPaginaPainel(page);
    });

    test('Preparacao 3a: Gestor COORD_21 aceita revisão', async ({page, autenticadoComoGestorCoord21}) => {
        await acessarSubprocessoGestor(page, descRevisao, UNIDADE_ALVO);
        await navegarParaAtividadesVisualizacao(page);
        await aceitarRevisao(page);
    });

    test('Preparacao 3b: Gestor SECRETARIA_2 aceita revisão', async ({page}) => {
        await loginComPerfil(page, USUARIOS.CHEFE_SECRETARIA_2.titulo, USUARIOS.CHEFE_SECRETARIA_2.senha, 'GESTOR - SECRETARIA_2');
        await acessarSubprocessoGestor(page, descRevisao, UNIDADE_ALVO);
        await navegarParaAtividadesVisualizacao(page);
        await aceitarRevisao(page);
    });

    test('Preparacao 3: ADMIN homologa revisão de cadastro', async ({page, autenticadoComoAdmin}) => {
        await acessarSubprocessoChefeDireto(page, descRevisao, UNIDADE_ALVO);
        await navegarParaAtividadesVisualizacao(page);
        await homologarCadastroRevisaoComImpacto(page);

        await expect(page.getByTestId('subprocesso-header__txt-situacao'))
            .toHaveText(/Revisão do cadastro homologada/i);
    });

    // ========================================================================
    // TESTES PRINCIPAIS
    // ========================================================================

    test('Cenario 1: ADMIN navega para subprocesso de revisão', async ({page, autenticadoComoAdmin}) => {
        await acessarSubprocessoChefeDireto(page, descRevisao, UNIDADE_ALVO);
    });

    test('Cenario 2: ADMIN visualiza botão Reabrir Revisão', async ({page, autenticadoComoAdmin}) => {
        await acessarSubprocessoChefeDireto(page, descRevisao, UNIDADE_ALVO);

        const btnReabrir = page.getByTestId('btn-reabrir-revisao');
        await expect(btnReabrir).toBeVisible();
        await expect(btnReabrir).toBeEnabled();
    });

    test('Cenario 3: ADMIN abre modal de reabertura de revisão', async ({page, autenticadoComoAdmin}) => {
        await acessarSubprocessoChefeDireto(page, descRevisao, UNIDADE_ALVO);

        await page.getByTestId('btn-reabrir-revisao').click();

        const modal = page.getByRole('dialog');
        await expect(modal).toBeVisible();
        await expect(modal.getByRole('heading', {name: /Reabrir Revisão/i})).toBeVisible();
    });

    test('Cenario 4: ADMIN confirma reabertura da revisão', async ({page, autenticadoComoAdmin}) => {
        await acessarSubprocessoChefeDireto(page, descRevisao, UNIDADE_ALVO);
        await page.getByTestId('btn-reabrir-revisao').click();

        await page.getByTestId('inp-justificativa-reabrir').fill('Ajuste necessário');
        await page.getByTestId('btn-confirmar-reabrir').click();

        await expect(page.getByText(/Revisão de cadastro reaberta/i).first()).toBeVisible();
        await expect(page.getByTestId('subprocesso-header__txt-situacao'))
            .toHaveText(/Revisão em andamento/i);
    });
});
