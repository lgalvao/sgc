import {expect} from '@playwright/test';
import {vueTest as test} from '../support/vue-specific-setup';
import {
    DADOS_TESTE,
    loginComoAdmin,
    loginComoChefe,
    loginComoGestor,
    loginComoServidor,
    navegarParaCadastroAtividades,
    navegarParaVisualizacaoAtividades
} from './helpers';

async function verificarListagemAtividadesEConhecimentos(page) {
    // Renderização básica da lista
    await expect(page.getByTestId('atividade-descricao').first()).toBeVisible();
    await expect(page.getByTestId('conhecimento-descricao').first()).toBeVisible();
}

async function verificarSomenteLeitura(page) {
    // Garantir ausência de controles de edição/remoção/adição na tela de visualização
    await expect(page.locator('[data-testid="btn-editar-atividade"]')).toHaveCount(0);
    await expect(page.locator('[data-testid="btn-remover-atividade"]')).toHaveCount(0);
    await expect(page.locator('[data-testid="btn-editar-conhecimento"]')).toHaveCount(0);
    await expect(page.locator('[data-testid="btn-remover-conhecimento"]')).toHaveCount(0);
    await expect(page.locator('[data-testid="btn-adicionar-conhecimento"]')).toHaveCount(0);
    await expect(page.locator('[data-testid="btn-adicionar-atividade"]')).toHaveCount(0);
}

async function verificarCabecalhoUnidade(page, siglaEsperada: string) {
    const sigla = page.locator('.unidade-cabecalho .unidade-sigla');
    const nome = page.locator('.unidade-cabecalho .unidade-nome');
    await expect(sigla).toBeVisible();
    await expect(sigla).toContainText(siglaEsperada);
    await expect(nome).toBeVisible();
    await expect(nome).toHaveText(/\S+/);
}

test.describe('CDU-11: Visualizar cadastro de atividades e conhecimentos (somente leitura)', () => {
    test('ADMIN: deve visualizar cadastro (somente leitura) de unidade subordinada com cabeçalho', async ({page}) => {
        await loginComoAdmin(page);
        await navegarParaVisualizacaoAtividades(page, DADOS_TESTE.PROCESSOS.REVISAO_STIC.id, DADOS_TESTE.UNIDADES.SESEL);
        await verificarCabecalhoUnidade(page, DADOS_TESTE.UNIDADES.SESEL);
        await verificarListagemAtividadesEConhecimentos(page);
        await verificarSomenteLeitura(page);
    });

    test('GESTOR: deve visualizar cadastro (somente leitura) de unidade subordinada com cabeçalho', async ({page}) => {
        await loginComoGestor(page);
        await navegarParaVisualizacaoAtividades(page, DADOS_TESTE.PROCESSOS.REVISAO_STIC.id, DADOS_TESTE.UNIDADES.SESEL);
        await verificarCabecalhoUnidade(page, DADOS_TESTE.UNIDADES.SESEL);
        await verificarListagemAtividadesEConhecimentos(page);
        await verificarSomenteLeitura(page);
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