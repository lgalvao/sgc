import {expect, test} from '@playwright/test';
import {DADOS_TESTE, SELETORES, TEXTOS} from "../cdu/helpers";
import {
    cancelarModal,
    gerarNomeUnico,
    irParaSubprocesso,
    loginComoAdmin,
    loginComoChefe,
    loginComoGestor,
    loginComoServidor,
    navegarParaCadastroAtividades,
    navegarParaVisualizacaoAtividades
} from "../cdu/helpers";

test.describe('Captura de Telas - Atividades', () => {
    test('19 - Detalhes de Subprocesso (CHEFE)', async ({page}) => {
        await loginComoChefe(page);
        await irParaSubprocesso(page, DADOS_TESTE.PROCESSOS.REVISAO_STIC.id, DADOS_TESTE.UNIDADES.STIC);
        await page.waitForLoadState('networkidle');
        await page.screenshot({path: 'screenshots/19-detalhes-subprocesso-chefe.png', fullPage: true});
    });

    test('20 - Detalhes de Subprocesso (SERVIDOR)', async ({page}) => {
        await loginComoServidor(page);
        await irParaSubprocesso(page, DADOS_TESTE.PROCESSOS.REVISAO_STIC.id, DADOS_TESTE.UNIDADES.STIC);
        await page.waitForLoadState('networkidle');
        await page.screenshot({path: 'screenshots/20-detalhes-subprocesso-servidor.png', fullPage: true});
    });

    test('21 - Modal Alterar Data Limite (Subprocesso)', async ({page}) => {
        await loginComoChefe(page);
        await irParaSubprocesso(page, DADOS_TESTE.PROCESSOS.REVISAO_STIC.id, DADOS_TESTE.UNIDADES.STIC);
        await page.waitForLoadState('networkidle');
        const alterarDataButton = page.getByRole('button').filter({hasText: /data limite/i});
        if (await alterarDataButton.isVisible()) {
            await alterarDataButton.click();
            await page.waitForSelector('.modal.show');
            await page.screenshot({path: 'screenshots/21-modal-alterar-data-limite.png', fullPage: true});
            await cancelarModal(page);
        }
    });

    test('22 - Cadastro de Atividades - Vazio (CHEFE)', async ({page}) => {
        await loginComoChefe(page);
        await navegarParaCadastroAtividades(page, DADOS_TESTE.PROCESSOS.MAPEAMENTO_STIC.id, DADOS_TESTE.UNIDADES.STIC);
        await page.waitForLoadState('networkidle');
        await page.screenshot({path: 'screenshots/22-cadastro-atividades-vazio.png', fullPage: true});
    });

    test('23 - Cadastro de Atividades - Com Atividade e Conhecimento (CHEFE)', async ({page}) => {
        await loginComoChefe(page);
        await navegarParaCadastroAtividades(page, DADOS_TESTE.PROCESSOS.MAPEAMENTO_STIC.id, DADOS_TESTE.UNIDADES.STIC);
        const nomeAtividade = gerarNomeUnico('Atividade Visual');
        await page.getByTestId(SELETORES.INPUT_NOVA_ATIVIDADE).fill(nomeAtividade);
        await page.getByTestId(SELETORES.BTN_ADICIONAR_ATIVIDADE).click();
        const cardAtividade = page.locator('.atividade-card', {hasText: nomeAtividade});
        await cardAtividade.locator('[data-testid="input-novo-conhecimento"]').fill('Conhecimento Visual');
        await cardAtividade.locator('[data-testid="btn-adicionar-conhecimento"]').click();
        await page.waitForTimeout(500);
        await page.screenshot({path: 'screenshots/23-cadastro-atividades-com-dados.png', fullPage: true});
    });

    test('24 - Cadastro de Atividades - Botão Impacto no Mapa (CHEFE)', async ({page}) => {
        await loginComoChefe(page);
        await navegarParaCadastroAtividades(page, DADOS_TESTE.PROCESSOS.REVISAO_STIC.id, DADOS_TESTE.UNIDADES.STIC);
        await page.waitForLoadState('networkidle');
        await page.screenshot({path: 'screenshots/24-cadastro-atividades-impacto-mapa.png', fullPage: true});
    });

    test('25 - Modal Importar Atividades (CHEFE)', async ({page}) => {
        await loginComoChefe(page);
        await navegarParaCadastroAtividades(page, DADOS_TESTE.PROCESSOS.MAPEAMENTO_STIC.id, DADOS_TESTE.UNIDADES.STIC);
        await page.getByRole('button', {name: TEXTOS.IMPORTAR_ATIVIDADES}).click();
        await page.waitForSelector('.modal.show');
        await page.screenshot({path: 'screenshots/25-modal-importar-atividades.png', fullPage: true});
        await cancelarModal(page);
    });

    test('26 - Modal Histórico de Análise (Cadastro Atividades - CHEFE)', async ({page}) => {
        await loginComoChefe(page);
        await navegarParaCadastroAtividades(page, DADOS_TESTE.PROCESSOS.REVISAO_STIC.id, DADOS_TESTE.UNIDADES.STIC);
        await page.getByRole('button', { name: 'Histórico de análise' }).waitFor({ state: 'visible' });
    await page.getByRole('button', { name: 'Histórico de análise' }).click();
        await expect(page.locator('.modal.show', { hasText: 'Histórico de Análise' })).toBeVisible();
        await page.screenshot({path: 'screenshots/26-modal-historico-analise-cadastro.png', fullPage: true});
        await cancelarModal(page);
    });

    test('27 - Modal Confirmação Disponibilização (Cadastro Atividades - CHEFE)', async ({page}) => {
        await loginComoChefe(page);
        await navegarParaCadastroAtividades(page, DADOS_TESTE.PROCESSOS.REVISAO_STIC.id, DADOS_TESTE.UNIDADES.STIC);
        const nomeAtividade = gerarNomeUnico('Atividade Disponibilizar');
        await page.getByTestId(SELETORES.INPUT_NOVA_ATIVIDADE).fill(nomeAtividade);
        await page.getByTestId(SELETORES.BTN_ADICIONAR_ATIVIDADE).click();
        const cardAtividade = page.locator('.atividade-card', {hasText: nomeAtividade});
        await cardAtividade.locator('[data-testid="input-novo-conhecimento"]').fill('Conhecimento Disponibilizar');
        await cardAtividade.locator('[data-testid="btn-adicionar-conhecimento"]').click();
        await page.getByRole('button', {name: TEXTOS.DISPONIBILIZAR}).click();
        await page.waitForSelector('.modal.show');
        await page.screenshot({path: 'screenshots/27-modal-confirmacao-disponibilizacao-cadastro.png', fullPage: true});
        await cancelarModal(page);
    });

    test('28 - Visualização de Atividades - ADMIN (Somente Leitura)', async ({page}) => {
        await loginComoAdmin(page);
        await navegarParaVisualizacaoAtividades(page, DADOS_TESTE.PROCESSOS.REVISAO_STIC.id, DADOS_TESTE.UNIDADES.SESEL);
        await page.waitForLoadState('networkidle');
        await page.screenshot({path: 'screenshots/28-visualizacao-atividades-admin.png', fullPage: true});
    });

    test('29 - Visualização de Atividades - GESTOR (Somente Leitura)', async ({page}) => {
        await loginComoGestor(page);
        await navegarParaVisualizacaoAtividades(page, DADOS_TESTE.PROCESSOS.REVISAO_STIC.id, DADOS_TESTE.UNIDADES.SESEL);
        await page.waitForLoadState('networkidle');
        await page.screenshot({path: 'screenshots/29-visualizacao-atividades-gestor.png', fullPage: true});
    });
});
