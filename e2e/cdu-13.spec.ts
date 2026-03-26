import {expect, test} from './fixtures/complete-fixtures.js';
import {login, loginComPerfil, USUARIOS} from './helpers/helpers-auth.js';
import {criarProcessoCadastroDisponibilizadoFixture} from './fixtures/fixtures-processos.js';
import {navegarParaAtividades, navegarParaAtividadesVisualizacao} from './helpers/helpers-atividades.js';
import {
    abrirHistoricoAnalise,
    abrirHistoricoAnaliseVisualizacao,
    aceitarCadastroMapeamento,
    acessarSubprocessoAdmin,
    acessarSubprocessoChefeDireto,
    acessarSubprocessoGestor,
    devolverCadastroMapeamento,
    fecharHistoricoAnalise,
    verificarAcoesAnaliseCadastroVisualizacao,
} from './helpers/helpers-analise.js';
import {navegarParaSubprocesso} from './helpers/helpers-navegacao.js';
import {TEXTOS} from '../frontend/src/constants/textos.js';
import {resetDatabase} from './hooks/hooks-limpeza.js';

async function validarCabecalhosHistorico(modal: ReturnType<typeof abrirHistoricoAnalise> extends Promise<infer T> ? T : never) {
    await expect(modal.getByTestId('header-historico-dataHora')).toBeVisible();
    await expect(modal.getByTestId('header-historico-unidade')).toBeVisible();
    await expect(modal.getByTestId('header-historico-resultado')).toBeVisible();
    await expect(modal.getByTestId('header-historico-analista')).toBeVisible();
    await expect(modal.getByTestId('header-historico-observacao')).toBeVisible();
}

test.describe.serial('CDU-13 - Analisar cadastro de atividades e conhecimentos', () => {
    const UNIDADE_ALVO = 'SECAO_211';
    
    let descProcesso: string;

    test('1. Setup: Preparar processo e devoluções iniciais', async ({_resetAutomatico, request, page}) => {
        const processo = await criarProcessoCadastroDisponibilizadoFixture(request, {
            unidade: UNIDADE_ALVO
        });
        descProcesso = processo.descricao;

        await login(page, USUARIOS.GESTOR_COORD_21.titulo, USUARIOS.GESTOR_COORD_21.senha);
        await acessarSubprocessoGestor(page, descProcesso, UNIDADE_ALVO);
        await navegarParaAtividadesVisualizacao(page);
        await verificarAcoesAnaliseCadastroVisualizacao(page, {
            rotuloPrincipal: 'Registrar aceite',
            principalHabilitado: true,
            devolverHabilitado: true
        });
        await aceitarCadastroMapeamento(page, 'Ok pela Coordenação');

        await loginComPerfil(page, USUARIOS.CHEFE_SECRETARIA_2.titulo, USUARIOS.CHEFE_SECRETARIA_2.senha, 'GESTOR - SECRETARIA_2');
        await acessarSubprocessoGestor(page, descProcesso, UNIDADE_ALVO);
        await navegarParaAtividadesVisualizacao(page);
        await verificarAcoesAnaliseCadastroVisualizacao(page, {
            rotuloPrincipal: 'Registrar aceite',
            principalHabilitado: true,
            devolverHabilitado: true
        });
        const historicoSecretaria = await abrirHistoricoAnaliseVisualizacao(page);
        await expect(historicoSecretaria.getByTestId('cell-unidade-0')).toHaveText('COORD_21');
        await expect(historicoSecretaria.getByTestId('cell-resultado-0')).toHaveText(/ACEITE/i);
        await expect(historicoSecretaria.getByTestId('cell-observacao-0')).toHaveText('Ok pela Coordenação');
        await fecharHistoricoAnalise(page);
        await devolverCadastroMapeamento(page, 'Dados incompletos para a Secretaria');

        await login(page, USUARIOS.GESTOR_COORD_21.titulo, USUARIOS.GESTOR_COORD_21.senha);
        await acessarSubprocessoGestor(page, descProcesso, UNIDADE_ALVO);
        await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Cadastro disponibilizado/i);

        await navegarParaAtividadesVisualizacao(page);
        await devolverCadastroMapeamento(page, 'Corrigir conforme Secretaria');

        await login(page, USUARIOS.CHEFE_SECAO_211.titulo, USUARIOS.CHEFE_SECAO_211.senha);
        await acessarSubprocessoChefeDireto(page, descProcesso, UNIDADE_ALVO);
        await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Cadastro em andamento/i);

        await navegarParaAtividades(page);
        const modal = await abrirHistoricoAnalise(page);
        await validarCabecalhosHistorico(modal);
        await expect(modal.getByTestId('cell-dataHora-0')).not.toHaveText('');
        await expect(modal.getByTestId('cell-resultado-0')).toHaveText(/Devolu[cç][aã]o/i);
        await expect(modal.getByTestId('cell-observacao-0')).toHaveText('Corrigir conforme Secretaria');
        await fecharHistoricoAnalise(page);

        await page.getByTestId('btn-cad-atividades-disponibilizar').click();
        await page.getByTestId('btn-confirmar-disponibilizacao').click();
        await expect(page).toHaveURL(/\/painel/);
    });

    test('Cenarios CDU-13: Hierarquia aceita e ADMIN homologa', async ({_resetAutomatico, page}) => {
        await login(page, USUARIOS.GESTOR_COORD_21.titulo, USUARIOS.GESTOR_COORD_21.senha);
        await acessarSubprocessoGestor(page, descProcesso, UNIDADE_ALVO);
        await navegarParaAtividadesVisualizacao(page);
        await verificarAcoesAnaliseCadastroVisualizacao(page, {
            rotuloPrincipal: 'Registrar aceite',
            principalHabilitado: true,
            devolverHabilitado: true
        });
        await aceitarCadastroMapeamento(page, 'Ok final 1');

        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
        await acessarSubprocessoAdmin(page, descProcesso, UNIDADE_ALVO);
        await navegarParaSubprocesso(page, UNIDADE_ALVO);
        await navegarParaAtividadesVisualizacao(page);
        await verificarAcoesAnaliseCadastroVisualizacao(page, {
            rotuloPrincipal: 'Homologar',
            principalHabilitado: false,
            devolverHabilitado: false
        });

        await loginComPerfil(page, USUARIOS.CHEFE_SECRETARIA_2.titulo, USUARIOS.CHEFE_SECRETARIA_2.senha, 'GESTOR - SECRETARIA_2');
        await acessarSubprocessoGestor(page, descProcesso, UNIDADE_ALVO);
        await navegarParaAtividadesVisualizacao(page);
        await verificarAcoesAnaliseCadastroVisualizacao(page, {
            rotuloPrincipal: 'Registrar aceite',
            principalHabilitado: true,
            devolverHabilitado: true
        });
        await aceitarCadastroMapeamento(page, 'Ok final 2');

        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
        await acessarSubprocessoAdmin(page, descProcesso, UNIDADE_ALVO);
        await navegarParaSubprocesso(page, UNIDADE_ALVO);
        await navegarParaAtividadesVisualizacao(page);
        await verificarAcoesAnaliseCadastroVisualizacao(page, {
            rotuloPrincipal: 'Homologar',
            principalHabilitado: true,
            devolverHabilitado: true
        });
        const historicoAdmin = await abrirHistoricoAnaliseVisualizacao(page);
        await expect(historicoAdmin.getByTestId('cell-unidade-0')).toHaveText('SECRETARIA_2');
        await expect(historicoAdmin.getByTestId('cell-resultado-0')).toHaveText(/ACEITE/i);
        await expect(historicoAdmin.getByTestId('cell-observacao-0')).toHaveText('Ok final 2');
        await expect(historicoAdmin.getByTestId('cell-unidade-1')).toHaveText('COORD_21');
        await expect(historicoAdmin.getByTestId('cell-observacao-1')).toHaveText('Ok final 1');
        await fecharHistoricoAnalise(page);
        await page.getByTestId('btn-acao-analisar-principal').click();
        await page.getByTestId('btn-aceite-cadastro-confirmar').click();

        await expect(page).toHaveURL(new RegExp(String.raw`/processo/\d+/${UNIDADE_ALVO}$`));
        await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Cadastro homologado/i);
        await expect(page.getByText(TEXTOS.sucesso.HOMOLOGACAO_EFETIVADA).first()).toBeVisible();
    });
});

test.describe.serial('CDU-13 - Cancelamentos de análise', () => {
    const UNIDADE_ALVO = 'SECAO_211';
    let descProcessoCancelamento: string;

    test('Setup: preparar processo para fluxos de cancelamento', async ({_resetAutomatico, request}) => {
        await resetDatabase(request);
        const processo = await criarProcessoCadastroDisponibilizadoFixture(request, {
            unidade: UNIDADE_ALVO
        });
        descProcessoCancelamento = processo.descricao;
        expect(descProcessoCancelamento).toBeDefined();
    });

    test('GESTOR cancela devolução e permanece na tela de atividades e conhecimentos', async ({_resetAutomatico, page}) => {
        await login(page, USUARIOS.GESTOR_COORD_21.titulo, USUARIOS.GESTOR_COORD_21.senha);
        await acessarSubprocessoGestor(page, descProcessoCancelamento, UNIDADE_ALVO);
        await navegarParaAtividadesVisualizacao(page);

        await page.getByTestId('btn-acao-devolver').click();
        const modal = page.getByRole('dialog');
        await expect(modal.getByRole('heading', {name: TEXTOS.atividades.MODAL_DEVOLVER_TITULO})).toBeVisible();
        await expect(modal.getByText(TEXTOS.atividades.MODAL_DEVOLVER_TEXTO)).toBeVisible();
        await modal.getByTestId('btn-modal-confirmacao-cancelar').click();

        await expect(modal).toBeHidden();
        await expect(page.getByRole('heading', {name: TEXTOS.atividades.TITULO})).toBeVisible();
        await expect(page.getByTestId('btn-acao-devolver')).toBeVisible();
        await expect(page.getByTestId('btn-acao-analisar-principal')).toBeVisible();
    });

    test('ADMIN cancela homologação e permanece na tela de atividades e conhecimentos', async ({_resetAutomatico, page}) => {
        await login(page, USUARIOS.GESTOR_COORD_21.titulo, USUARIOS.GESTOR_COORD_21.senha);
        await acessarSubprocessoGestor(page, descProcessoCancelamento, UNIDADE_ALVO);
        await navegarParaAtividadesVisualizacao(page);
        await aceitarCadastroMapeamento(page, 'Aceite para fluxo de cancelamento 1');

        await loginComPerfil(page, USUARIOS.CHEFE_SECRETARIA_2.titulo, USUARIOS.CHEFE_SECRETARIA_2.senha, 'GESTOR - SECRETARIA_2');
        await acessarSubprocessoGestor(page, descProcessoCancelamento, UNIDADE_ALVO);
        await navegarParaAtividadesVisualizacao(page);
        await aceitarCadastroMapeamento(page, 'Aceite para fluxo de cancelamento 2');

        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
        await acessarSubprocessoAdmin(page, descProcessoCancelamento, UNIDADE_ALVO);
        await navegarParaSubprocesso(page, UNIDADE_ALVO);
        await navegarParaAtividadesVisualizacao(page);
        await verificarAcoesAnaliseCadastroVisualizacao(page, {
            rotuloPrincipal: 'Homologar',
            principalHabilitado: true,
            devolverHabilitado: true
        });

        await page.getByTestId('btn-acao-analisar-principal').click();
        const modal = page.getByRole('dialog');
        await expect(modal.getByRole('heading', {name: TEXTOS.atividades.MODAL_HOMOLOGAR_TITULO})).toBeVisible();
        await expect(modal.getByText(TEXTOS.atividades.MODAL_HOMOLOGAR_TEXTO)).toBeVisible();
        await modal.getByTestId('btn-modal-confirmacao-cancelar').click();

        await expect(modal).toBeHidden();
        await expect(page.getByRole('heading', {name: TEXTOS.atividades.TITULO})).toBeVisible();
        await expect(page.getByTestId('btn-acao-analisar-principal')).toBeVisible();
        await expect(page.getByTestId('btn-acao-devolver')).toBeVisible();
    });
});
