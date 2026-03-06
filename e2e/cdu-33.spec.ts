import {expect, test} from './fixtures/complete-fixtures.js';
import {criarProcessoFixture, criarProcessoMapaHomologadoFixture} from './fixtures/fixtures-processos.js';
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
    let mappingPid = 0;
    let revisaoPid = 0;

    // PREPARAÇÃO 0 - CRIAR MAPA VIGENTE

    test('Preparacao 0: Criar e finalizar Mapeamento', async ({page, request, autenticadoComoAdmin}) => {
        const processo = await criarProcessoMapaHomologadoFixture(request, {
            descricao: descMapeamento,
            diasLimite: 30,
            unidade: UNIDADE_ALVO
        });
        mappingPid = processo.codigo;

        await page.goto('/painel');
        await expect(page.getByTestId('tbl-processos').getByText(descMapeamento).first()).toBeVisible();
        await page.getByTestId('tbl-processos').getByText(descMapeamento).first().click();
        await page.getByTestId('btn-processo-finalizar').click();
        await page.getByTestId('btn-finalizar-processo-confirmar').click();
        await verificarPaginaPainel(page);
    });


    test('Preparacao 1: Admin cria e inicia processo de REVISAO', async ({
                                                                             page,
                                                                             request,
                                                                             autenticadoComoAdmin
                                                                         }) => {
        const processo = await criarProcessoFixture(request, {
            descricao: descRevisao,
            tipo: 'REVISAO',
            diasLimite: 30,
            unidade: UNIDADE_ALVO,
            iniciar: true
        });
        revisaoPid = processo.codigo;

        await page.goto('/painel');
        await expect(page.getByTestId('tbl-processos').getByText(descRevisao).first()).toBeVisible();
        await verificarPaginaPainel(page);
    });

    test('Preparacao 2: Chefe disponibiliza revisão de cadastro', async ({page, autenticadoComoChefeSecao212}) => {
        await acessarSubprocessoChefeDireto(page, descRevisao, UNIDADE_ALVO);
        await navegarParaAtividades(page);

        await adicionarAtividade(page, `Atividade Rev ${timestamp}`);
        await adicionarConhecimento(page, `Atividade Rev ${timestamp}`, 'Conhecimento Rev');

        await page.getByTestId('btn-cad-atividades-disponibilizar').click();
        await page.getByTestId('btn-confirmar-disponibilizacao').click();

        await verificarPaginaPainel(page);
    });

    test('Preparacao 3: Gestores e ADMIN aceitam revisão', async ({page, autenticadoComoGestorCoord21}) => {
        await acessarSubprocessoGestor(page, descRevisao, UNIDADE_ALVO);
        await navegarParaAtividadesVisualizacao(page);
        await aceitarRevisao(page);

        await loginComPerfil(page, USUARIOS.CHEFE_SECRETARIA_2.titulo, USUARIOS.CHEFE_SECRETARIA_2.senha, 'GESTOR - SECRETARIA_2');
        await acessarSubprocessoGestor(page, descRevisao, UNIDADE_ALVO);
        await navegarParaAtividadesVisualizacao(page);
        await aceitarRevisao(page);

        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
        await acessarSubprocessoChefeDireto(page, descRevisao, UNIDADE_ALVO);
        await navegarParaAtividadesVisualizacao(page);
        await homologarCadastroRevisaoComImpacto(page);

        await expect(page.getByTestId('subprocesso-header__txt-situacao'))
            .toHaveText(/Revisão do cadastro homologada/i);
    });

    test('Preparacao 4: ADMIN disponibiliza mapa, chefe valida, gestores aceitam, ADMIN homologa', async ({page, autenticadoComoAdmin}) => {
        // ADMIN cria competência para a atividade adicionada e disponibiliza mapa
        await acessarSubprocessoChefeDireto(page, descRevisao, UNIDADE_ALVO);
        await navegarParaMapa(page);
        await criarCompetencia(page, `Comp Rev ${timestamp}`, [`Atividade Rev ${timestamp}`]);
        await disponibilizarMapa(page, '2030-12-31');
        await fazerLogout(page);

        // Chefe valida mapa
        await login(page, USUARIOS.CHEFE_SECAO_212.titulo, USUARIOS.CHEFE_SECAO_212.senha);
        await acessarSubprocessoChefeDireto(page, descRevisao, UNIDADE_ALVO);
        await navegarParaMapa(page);
        await page.getByTestId('btn-mapa-validar').click();
        await page.getByTestId('btn-validar-mapa-confirmar').click();
        await expect(page.getByText(/Mapa validado/i).first()).toBeVisible();
        await fazerLogout(page);

        // Gestor COORD_21 aceita mapa
        await login(page, USUARIOS.GESTOR_COORD_21.titulo, USUARIOS.GESTOR_COORD_21.senha);
        await acessarSubprocessoGestor(page, descRevisao, UNIDADE_ALVO);
        await navegarParaMapa(page);
        await page.getByTestId('btn-mapa-homologar-aceite').click();
        await page.getByTestId('btn-aceite-mapa-confirmar').click();
        await expect(page.getByText(/Aceite registrado/i).first()).toBeVisible();
        await fazerLogout(page);

        // Gestor SECRETARIA_2 aceita mapa
        await loginComPerfil(page, USUARIOS.CHEFE_SECRETARIA_2.titulo, USUARIOS.CHEFE_SECRETARIA_2.senha, 'GESTOR - SECRETARIA_2');
        await acessarSubprocessoGestor(page, descRevisao, UNIDADE_ALVO);
        await navegarParaMapa(page);
        await page.getByTestId('btn-mapa-homologar-aceite').click();
        await page.getByTestId('btn-aceite-mapa-confirmar').click();
        await expect(page.getByText(/Aceite registrado/i).first()).toBeVisible();
        await fazerLogout(page);

        // ADMIN homologa mapa
        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
        await acessarSubprocessoChefeDireto(page, descRevisao, UNIDADE_ALVO);
        await navegarParaMapa(page);
        await page.getByTestId('btn-mapa-homologar-aceite').click();
        await page.getByTestId('btn-aceite-mapa-confirmar').click();
        await expect(page.getByText(/Homologação efetivada/i).first()).toBeVisible();
    });


    test('Cenários CDU-33: ADMIN reabre revisão de cadastro', async ({page, autenticadoComoAdmin, cleanupAutomatico}) => {
        cleanupAutomatico.registrar(mappingPid);
        cleanupAutomatico.registrar(revisaoPid);

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

        await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Revisão em andamento/i);
        await expect(page.getByTestId('tbl-movimentacoes')).toContainText(/Reabertura de revisão de cadastro/i);
    });
});
