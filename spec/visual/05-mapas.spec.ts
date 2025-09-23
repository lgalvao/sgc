import {expect, test} from '@playwright/test';
import {DADOS_TESTE, TEXTOS} from "~/cdu/constantes-teste";
import {
    loginComoAdmin,
    loginComoChefe,
    loginComoChefeSedia,
    loginComoGestor,
    navegarParaCadastroAtividades
} from "~/cdu/auxiliares-verificacoes";
import {cancelarModal, criarCompetencia} from "~/cdu/auxiliares-acoes";
import {irParaMapaCompetencias, irParaVisualizacaoMapa} from "~/cdu/auxiliares-navegacao";

test.describe('Captura de Telas - Mapas', () => {
    test('30 - Mapa de Competências - Edição (ADMIN)', async ({page}) => {
        await loginComoAdmin(page);
        await irParaMapaCompetencias(page, DADOS_TESTE.PROCESSOS.REVISAO_STIC.id, DADOS_TESTE.UNIDADES.SESEL);
        await page.waitForLoadState('networkidle');
        await page.screenshot({path: 'screenshots/30-mapa-competencias-edicao-admin.png', fullPage: true});
    });

    test('31 - Mapa de Competências - Edição com Competência Criada (ADMIN)', async ({page}) => {
        await loginComoAdmin(page);
        await irParaMapaCompetencias(page, DADOS_TESTE.PROCESSOS.REVISAO_STIC.id, DADOS_TESTE.UNIDADES.SESEL);
        await page.waitForLoadState('networkidle');
        await criarCompetencia(page, 'Competência Visual');
        await page.waitForTimeout(500);
        await page.screenshot({path: 'screenshots/31-mapa-competencias-edicao-com-competencia.png', fullPage: true});
    });

    test('32 - Modal Impactos no Mapa (ADMIN)', async ({page}) => {
        await loginComoAdmin(page);
        // Criar um cenário com impactos
        await navegarParaCadastroAtividades(page, DADOS_TESTE.PROCESSOS.REVISAO_STIC.id, DADOS_TESTE.UNIDADES.SESEL);
        const primeiroCardAtividade = page.locator('.atividade-card').first();
        await primeiroCardAtividade.locator('[data-testid="input-novo-conhecimento"]').fill('Conhecimento para Impacto');
        await primeiroCardAtividade.locator('[data-testid="btn-adicionar-conhecimento"]').click();
        await page.waitForLoadState('networkidle');

        await irParaMapaCompetencias(page, DADOS_TESTE.PROCESSOS.REVISAO_STIC.id, DADOS_TESTE.UNIDADES.SESEL);
        await page.waitForLoadState('networkidle');
            const impactosButton = page.getByTestId('impactos-mapa-button');
            await impactosButton.waitFor({ state: 'visible' });
            await impactosButton.click();
            await expect(page.getByTestId('impacto-mapa-modal')).toBeVisible();
            await page.screenshot({path: 'screenshots/32-modal-impactos-mapa.png', fullPage: true});
            await cancelarModal(page);
    });

    test('33 - Modal Exclusão de Competência (ADMIN)', async ({page}) => {
        await loginComoAdmin(page);
        await irParaMapaCompetencias(page, DADOS_TESTE.PROCESSOS.REVISAO_STIC.id, DADOS_TESTE.UNIDADES.SESEL);
        await page.waitForLoadState('networkidle');
        await criarCompetencia(page, 'Competência para Excluir');
        const competenciaCard = page.locator('.competencia-card').filter({hasText: 'Competência para Excluir'});
        await competenciaCard.hover();
        await competenciaCard.getByTestId('btn-excluir-competencia').click();
        await page.waitForSelector('.modal.show');
        await page.screenshot({path: 'screenshots/33-modal-exclusao-competencia.png', fullPage: true});
        await cancelarModal(page);
    });

    test('34 - Modal Disponibilização do Mapa (ADMIN)', async ({page}) => {
        await loginComoAdmin(page);
        await irParaMapaCompetencias(page, DADOS_TESTE.PROCESSOS.REVISAO_STIC.id, DADOS_TESTE.UNIDADES.SESEL);
        await page.waitForLoadState('networkidle');
        await criarCompetencia(page, 'Competência para Disponibilizar Mapa');
        await page.getByRole('button', {name: TEXTOS.DISPONIBILIZAR}).click();
        await page.waitForSelector('.modal.show');
        await page.screenshot({path: 'screenshots/34-modal-disponibilizacao-mapa.png', fullPage: true});
        await cancelarModal(page);
    });

    test('35 - Visualização de Mapa - ADMIN/GESTOR', async ({page}) => {
        await loginComoAdmin(page);
        await irParaVisualizacaoMapa(page, DADOS_TESTE.PROCESSOS.REVISAO_STIC.id, DADOS_TESTE.UNIDADES.STIC);
        await page.waitForLoadState('networkidle');
        await page.screenshot({path: 'screenshots/35-visualizacao-mapa-admin-gestor.png', fullPage: true});
    });

    test('36 - Visualização de Mapa - CHEFE/SERVIDOR', async ({page}) => {
        await loginComoChefe(page);
        await irParaVisualizacaoMapa(page, DADOS_TESTE.PROCESSOS.REVISAO_STIC.id, DADOS_TESTE.UNIDADES.STIC);
        await page.waitForLoadState('networkidle');
        await page.screenshot({path: 'screenshots/36-visualizacao-mapa-chefe-servidor.png', fullPage: true});
    });

    test('37 - Modal Apresentar Sugestões (Visualização Mapa - CHEFE)', async ({page}) => {
        await loginComoChefeSedia(page); // Usar CHEFE_SEDIA para um cenário com sugestões
        await irParaVisualizacaoMapa(page, 5, 'SEDIA'); // Processo 5, SEDIA tem sugestões
        await page.waitForLoadState('networkidle');
        const sugestoesButton = page.getByTestId('apresentar-sugestoes-btn');
        if (await sugestoesButton.isVisible()) {
            await sugestoesButton.click();
            await page.waitForSelector('.modal.show');
            await page.screenshot({path: 'screenshots/37-modal-apresentar-sugestoes.png', fullPage: true});
            await cancelarModal(page);
        }
    });

    test('38 - Modal Validação do Mapa (Visualização Mapa - CHEFE)', async ({page}) => {
        await loginComoChefeSedia(page);
        await irParaVisualizacaoMapa(page, 5, 'SEDIA');
        await page.waitForLoadState('networkidle');
        const validarButton = page.getByTestId('validar-btn');
        await validarButton.waitFor({ state: 'visible' });
        await validarButton.click();
        await expect(page.getByTestId('modal-validar')).toBeVisible();
            await page.screenshot({path: 'screenshots/38-modal-validacao-mapa.png', fullPage: true});
            await cancelarModal(page);


    });

    test('39 - Modal Devolução (Visualização Mapa - GESTOR)', async ({page}) => {
        await loginComoGestor(page);
        await irParaVisualizacaoMapa(page, 1, 'SEDESENV'); // Processo 1, SEDESENV para devolução
        await page.waitForLoadState('networkidle');
        const devolverButton = page.getByTestId('devolver-ajustes-btn');
        await devolverButton.waitFor({ state: 'visible' });
        await devolverButton.click();
        await expect(page.getByTestId('modal-devolucao')).toBeVisible();
            await page.screenshot({path: 'screenshots/39-modal-devolucao-mapa.png', fullPage: true});
            await cancelarModal(page);
    });

    test('40 - Modal Aceite (Visualização Mapa - GESTOR)', async ({page}) => {
        await loginComoGestor(page);
        await irParaVisualizacaoMapa(page, 1, 'SEDESENV');
        await page.waitForLoadState('networkidle');
        const aceitarButton = page.getByTestId('registrar-aceite-btn');
        if (await aceitarButton.isVisible()) {
            await aceitarButton.click();
            await page.waitForSelector('.modal.show');
            await page.screenshot({path: 'screenshots/40-modal-aceite-mapa.png', fullPage: true});
            await cancelarModal(page);
        }
    });

    test('41 - Modal Homologação (Visualização Mapa - ADMIN)', async ({page}) => {
        await loginComoAdmin(page);
        await irParaVisualizacaoMapa(page, 1, 'SEDESENV');
        await page.waitForLoadState('networkidle');
        const homologarButton = page.getByTestId('registrar-aceite-btn'); // Botão de homologar para ADMIN
        if (await homologarButton.isVisible()) {
            await homologarButton.click();
            await page.waitForSelector('.modal.show');
            await page.screenshot({path: 'screenshots/41-modal-homologacao-mapa.png', fullPage: true});
            await cancelarModal(page);
        }
    });

    test('42 - Modal Ver Sugestões (Visualização Mapa - GESTOR)', async ({page}) => {
        await loginComoGestor(page);
        await irParaVisualizacaoMapa(page, 1, 'SEDESENV'); // Cenário com sugestões
        await page.waitForLoadState('networkidle');
        const verSugestoesButton = page.getByTestId('ver-sugestoes-btn');
        if (await verSugestoesButton.isVisible()) {
            await verSugestoesButton.click();
            await page.waitForSelector('.modal.show');
            await page.screenshot({path: 'screenshots/42-modal-ver-sugestoes.png', fullPage: true});
            await cancelarModal(page);
        }
    });

    test('43 - Modal Histórico de Análise (Visualização Mapa - GESTOR)', async ({page}) => {
        await loginComoGestor(page);
        await irParaVisualizacaoMapa(page, 1, 'SEDESENV');
            const historicoButton = page.getByTestId('historico-analise-btn-gestor');
            await historicoButton.click();
            await expect(page.locator('.modal.show', { hasText: 'Histórico de Análise' })).toBeVisible();
            await page.screenshot({path: 'screenshots/43-modal-historico-analise-mapa.png', fullPage: true});
            await cancelarModal(page);

    });
});
