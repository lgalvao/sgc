 
import {expect, test} from './fixtures/complete-fixtures.js';
import {criarProcessoFixture} from './fixtures/fixtures-processos.js';
import {login, loginComPerfil, USUARIOS} from './helpers/helpers-auth.js';
import {
    adicionarAtividade,
    adicionarConhecimento,
    navegarParaAtividades,
    navegarParaAtividadesVisualizacao
} from './helpers/helpers-atividades.js';
import {criarCompetencia, disponibilizarMapa, navegarParaMapa} from './helpers/helpers-mapas.js';
import {
    aceitarCadastroMapeamento,
    acessarSubprocessoChefeDireto,
    acessarSubprocessoGestor,
    homologarCadastroMapeamento
} from './helpers/helpers-analise.js';
import {fazerLogout, navegarParaSubprocesso, verificarPaginaPainel} from './helpers/helpers-navegacao.js';

/**
 * CDU-32 - Reabrir cadastro
 */
test.describe.serial('CDU-32 - Reabrir cadastro', () => {
    const UNIDADE_1 = 'SECAO_221';

    const timestamp = Date.now();
    const descProcesso = `Mapeamento CDU-32 ${timestamp}`;

    const atividade1 = `Atividade Reabrir ${timestamp}`;

    test('Setup UI', async ({page, request}) => {

        // Preparacao 1: Admin cria e inicia processo
        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
        await criarProcessoFixture(request, {
            descricao: descProcesso,
            tipo: 'MAPEAMENTO',
            diasLimite: 30,
            unidade: UNIDADE_1,
            iniciar: true
        });

        await page.goto('/painel');
        await expect(page.getByTestId('tbl-processos').getByText(descProcesso).first()).toBeVisible();
        await verificarPaginaPainel(page);
        await fazerLogout(page);

        // Preparacao 2: Chefe disponibiliza cadastro
        await login(page, USUARIOS.CHEFE_SECAO_221.titulo, USUARIOS.CHEFE_SECAO_221.senha);
        await page.getByTestId('tbl-processos').getByText(descProcesso).first().click();
        await navegarParaAtividades(page);

        await adicionarAtividade(page, atividade1);
        await adicionarConhecimento(page, atividade1, 'Conhecimento Reabrir 1');

        await page.getByTestId('btn-cad-atividades-disponibilizar').click();
        await page.getByTestId('btn-confirmar-disponibilizacao').click();

        await verificarPaginaPainel(page);
        await fazerLogout(page);

        // Preparacao 3: Gestores e ADMIN aceitam e homologam cadastro
        await login(page, USUARIOS.GESTOR_COORD_22.titulo, USUARIOS.GESTOR_COORD_22.senha);
        await acessarSubprocessoGestor(page, descProcesso, UNIDADE_1);
        await navegarParaAtividadesVisualizacao(page);
        await aceitarCadastroMapeamento(page, 'Aceite intermediário COORD_22');
        await fazerLogout(page);

        await loginComPerfil(page, USUARIOS.CHEFE_SECRETARIA_2.titulo, USUARIOS.CHEFE_SECRETARIA_2.senha, 'GESTOR - SECRETARIA_2');
        await acessarSubprocessoGestor(page, descProcesso, UNIDADE_1);
        await navegarParaAtividadesVisualizacao(page);
        await aceitarCadastroMapeamento(page, 'Aceite intermediário SECRETARIA_2');
        await fazerLogout(page);

        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
        await page.getByTestId('tbl-processos').getByText(descProcesso).first().click();
        await navegarParaSubprocesso(page, UNIDADE_1);
        await page.getByTestId('card-subprocesso-atividades-vis').click();
        await homologarCadastroMapeamento(page);

        // Preparacao 4: ADMIN cria mapa, disponibiliza, chefe valida, gestores aceitam, ADMIN homologa
        await page.getByTestId('tbl-processos').getByText(descProcesso).first().click();
        await navegarParaSubprocesso(page, UNIDADE_1);
        await navegarParaMapa(page);
        await criarCompetencia(page, `Competência Reabrir ${timestamp}`, [atividade1]);
        await disponibilizarMapa(page, '2030-12-31');
        await fazerLogout(page);

        // Chefe valida mapa
        await login(page, USUARIOS.CHEFE_SECAO_221.titulo, USUARIOS.CHEFE_SECAO_221.senha);
        await acessarSubprocessoChefeDireto(page, descProcesso, UNIDADE_1);
        await navegarParaMapa(page);
        await page.getByTestId('btn-mapa-validar').click();
        await page.getByTestId('btn-validar-mapa-confirmar').click();
        await fazerLogout(page);

        // Gestor COORD_22 aceita mapa
        await login(page, USUARIOS.GESTOR_COORD_22.titulo, USUARIOS.GESTOR_COORD_22.senha);
        await acessarSubprocessoGestor(page, descProcesso, UNIDADE_1);
        await navegarParaMapa(page);
        await page.getByTestId('btn-mapa-homologar-aceite').click();
        await page.getByTestId('btn-aceite-mapa-confirmar').click();
        await fazerLogout(page);

        // Gestor SECRETARIA_2 aceita mapa
        await loginComPerfil(page, USUARIOS.CHEFE_SECRETARIA_2.titulo, USUARIOS.CHEFE_SECRETARIA_2.senha, 'GESTOR - SECRETARIA_2');
        await acessarSubprocessoGestor(page, descProcesso, UNIDADE_1);
        await navegarParaMapa(page);
        await page.getByTestId('btn-mapa-homologar-aceite').click();
        await page.getByTestId('btn-aceite-mapa-confirmar').click();
        await fazerLogout(page);

        // ADMIN homologa mapa
        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
        await acessarSubprocessoChefeDireto(page, descProcesso, UNIDADE_1);
        await navegarParaMapa(page);
        await page.getByTestId('btn-mapa-homologar-aceite').click();
        await page.getByTestId('btn-aceite-mapa-confirmar').click();
        await expect(page.getByText(/Homologação efetivada/i).first()).toBeVisible();
    });

    test('Cenários CDU-32: ADMIN reabre cadastro', async ({page, autenticadoComoAdmin}) => {

        // Cenario 1 & 2: Navegação e visualização do botão
        await page.getByTestId('tbl-processos').getByText(descProcesso).first().click();
        await navegarParaSubprocesso(page, UNIDADE_1);

        await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Mapa homologado/i);

        const btnReabrir = page.getByTestId('btn-reabrir-cadastro');
        await expect(btnReabrir).toBeVisible();
        await expect(btnReabrir).toBeEnabled();

        // Cenario 3: Abrir modal e cancelar
        await btnReabrir.click();
        const modal = page.getByRole('dialog');
        await expect(modal).toBeVisible();
        await expect(modal.getByText(/Reabrir cadastro/i)).toBeVisible();
        await modal.getByRole('button', {name: /Cancelar/i}).click();
        await expect(modal).toBeHidden();

        // Cenario 4: Botão confirmar desabilitado sem justificativa
        await btnReabrir.click();
        await expect(page.getByTestId('btn-confirmar-reabrir')).toBeDisabled();
        await page.getByTestId('inp-justificativa-reabrir').fill('Justificativa de teste');
        await expect(page.getByTestId('btn-confirmar-reabrir')).toBeEnabled();

        // Cenario 5: Confirmar reabertura
        await page.getByTestId('btn-confirmar-reabrir').click();

        await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Cadastro em andamento/i);
        await expect(page.getByTestId('tbl-movimentacoes')).toContainText(/Reabertura de cadastro/i);
    });
});
