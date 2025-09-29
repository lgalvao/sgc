import {test} from '@playwright/test';
import {
    DADOS_TESTE,
    loginComoAdmin,
    loginComoChefe,
    loginComoGestor,
    loginComoServidor,
    navegarParaCadastroAtividades,
    navegarParaVisualizacaoAtividades,
    verificarCabecalhoUnidade,
    verificarListagemAtividadesEConhecimentos,
    verificarModoSomenteLeitura,
} from './helpers';

test.describe('CDU-11: Visualizar cadastro de atividades e conhecimentos (somente leitura)', () => {
    test('ADMIN: deve visualizar cadastro (somente leitura) de unidade subordinada com cabeçalho', async ({page}) => {
        await loginComoAdmin(page);
        await navegarParaVisualizacaoAtividades(page, DADOS_TESTE.PROCESSOS.REVISAO_STIC.id, DADOS_TESTE.UNIDADES.SESEL);
        await verificarCabecalhoUnidade(page, DADOS_TESTE.UNIDADES.SESEL);
        await verificarListagemAtividadesEConhecimentos(page);
        await verificarModoSomenteLeitura(page);
    });

    test('GESTOR: deve visualizar cadastro (somente leitura) de unidade subordinada com cabeçalho', async ({page}) => {
        await loginComoGestor(page);
        await navegarParaVisualizacaoAtividades(page, DADOS_TESTE.PROCESSOS.REVISAO_STIC.id, DADOS_TESTE.UNIDADES.SESEL);
        await verificarCabecalhoUnidade(page, DADOS_TESTE.UNIDADES.SESEL);
        await verificarListagemAtividadesEConhecimentos(page);
        await verificarModoSomenteLeitura(page);
    });

    test('CHEFE: deve visualizar cadastro de sua unidade (rota de cadastro)', async ({page}) => {
        await loginComoChefe(page);
        await navegarParaCadastroAtividades(page, DADOS_TESTE.PROCESSOS.REVISAO_STIC.id, DADOS_TESTE.UNIDADES.STIC);
        await verificarListagemAtividadesEConhecimentos(page);
    });

    test('SERVIDOR: deve visualizar cadastro de sua unidade (rota de cadastro)', async ({page}) => {
        await loginComoServidor(page);
        await navegarParaCadastroAtividades(page, DADOS_TESTE.PROCESSOS.REVISAO_STIC.id, DADOS_TESTE.UNIDADES.STIC);
        await verificarListagemAtividadesEConhecimentos(page);
    });
});
