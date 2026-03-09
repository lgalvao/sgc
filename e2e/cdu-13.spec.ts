import {expect, test} from './fixtures/complete-fixtures.js';
import {login, loginComPerfil, USUARIOS} from './helpers/helpers-auth.js';
import {criarProcessoCadastroDisponibilizadoFixture} from './fixtures/fixtures-processos.js';
import {navegarParaAtividades, navegarParaAtividadesVisualizacao} from './helpers/helpers-atividades.js';
import {
    abrirHistoricoAnalise,
    aceitarCadastroMapeamento,
    acessarSubprocessoAdmin,
    acessarSubprocessoChefeDireto,
    acessarSubprocessoGestor,
    devolverCadastroMapeamento,
    fecharHistoricoAnalise,
} from './helpers/helpers-analise.js';
import {navegarParaSubprocesso} from './helpers/helpers-navegacao.js';

test.describe.serial('CDU-13 - Analisar cadastro de atividades e conhecimentos', () => {
    const UNIDADE_ALVO = 'SECAO_211';
    
    let descProcesso: string;

    test('1. Setup: Preparar processo e devoluções iniciais', async ({request, page}) => {
        const processo = await criarProcessoCadastroDisponibilizadoFixture(request, {
            unidade: UNIDADE_ALVO
        });
        descProcesso = processo.descricao;

        await login(page, USUARIOS.GESTOR_COORD_21.titulo, USUARIOS.GESTOR_COORD_21.senha);
        await acessarSubprocessoGestor(page, descProcesso, UNIDADE_ALVO);
        await navegarParaAtividadesVisualizacao(page);
        await aceitarCadastroMapeamento(page, 'Ok pela Coordenação');

        await loginComPerfil(page, USUARIOS.CHEFE_SECRETARIA_2.titulo, USUARIOS.CHEFE_SECRETARIA_2.senha, 'GESTOR - SECRETARIA_2');
        await acessarSubprocessoGestor(page, descProcesso, UNIDADE_ALVO);
        await navegarParaAtividadesVisualizacao(page);
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
        await expect(modal.getByTestId('cell-resultado-0')).toHaveText(/Devolu[cç][aã]o/i);
        await expect(modal.getByTestId('cell-observacao-0')).toHaveText('Corrigir conforme Secretaria');
        await fecharHistoricoAnalise(page);

        await page.getByTestId('btn-cad-atividades-disponibilizar').click();
        await page.getByTestId('btn-confirmar-disponibilizacao').click();
        await expect(page).toHaveURL(/\/painel/);
    });

    test('Cenarios CDU-13: Hierarquia aceita e ADMIN homologa', async ({page}) => {
        await login(page, USUARIOS.GESTOR_COORD_21.titulo, USUARIOS.GESTOR_COORD_21.senha);
        await acessarSubprocessoGestor(page, descProcesso, UNIDADE_ALVO);
        await navegarParaAtividadesVisualizacao(page);
        await aceitarCadastroMapeamento(page, 'Ok final 1');

        await loginComPerfil(page, USUARIOS.CHEFE_SECRETARIA_2.titulo, USUARIOS.CHEFE_SECRETARIA_2.senha, 'GESTOR - SECRETARIA_2');
        await acessarSubprocessoGestor(page, descProcesso, UNIDADE_ALVO);
        await navegarParaAtividadesVisualizacao(page);
        await aceitarCadastroMapeamento(page, 'Ok final 2');

        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
        await acessarSubprocessoAdmin(page, descProcesso, UNIDADE_ALVO);
        await navegarParaSubprocesso(page, UNIDADE_ALVO);
        await navegarParaAtividadesVisualizacao(page);
        await page.getByTestId('btn-acao-analisar-principal').click();
        await page.getByRole('dialog').getByRole('button', {name: 'Confirmar'}).click();

        await expect(page).toHaveURL(new RegExp(String.raw`/processo/\d+/${UNIDADE_ALVO}$`));
        await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Cadastro homologado/i);
    });
});
