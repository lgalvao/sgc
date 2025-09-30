import {expect} from '@playwright/test';
import {vueTest as test} from '../support/vue-specific-setup';
import {
    esperarElementoVisivel,
    esperarTextoVisivel,
    irParaProcessoPorTexto,
    loginComoAdmin,
    loginComoChefe,
    loginComoServidor,
    SELETORES,
    TEXTOS,
    verificarUrl
} from './helpers';

test.describe('CDU-18: Visualizar mapa de competências', () => {
    test('ADMIN/GESTOR: navegar pelo processo até visualização do mapa', async ({page}) => {
        await loginComoAdmin(page);
        await irParaProcessoPorTexto(page, 'Revisão de mapeamento STIC/COINF');

        const unidadeStic = page.locator('[data-testid^="tree-table-row-"]').filter({hasText: 'STIC'}).first();
        await unidadeStic.click();

        await verificarUrl(page, '/processo/\\d+/STIC');

        await page.waitForSelector('[data-testid="mapa-card"]');
        await page.getByTestId(SELETORES.MAPA_CARD).click();

        await verificarUrl(page, '/processo/\\d+/STIC/vis-mapa');
        await esperarTextoVisivel(page, TEXTOS.MAPA_COMPETENCIAS_TECNICAS);
    });

    test('CHEFE/SERVIDOR: navegar direto para subprocesso e visualizar mapa', async ({page}) => {
        await loginComoChefe(page);
        await irParaProcessoPorTexto(page, 'Revisão de mapeamento STIC/COINF');
        await verificarUrl(page, '/processo/\\d+/STIC');

        await page.waitForSelector('[data-testid="mapa-card"]');
        await page.getByTestId(SELETORES.MAPA_CARD).click();

        await verificarUrl(page, '/processo/\\d+/STIC/vis-mapa');
        await esperarTextoVisivel(page, TEXTOS.MAPA_COMPETENCIAS_TECNICAS);
    });

    test('deve verificar elementos obrigatórios da visualização do mapa', async ({page}) => {
        await loginComoChefe(page);
        await irParaProcessoPorTexto(page, 'Revisão de mapeamento STIC/COINF');

        await page.waitForSelector('[data-testid="mapa-card"]');
        await page.getByTestId(SELETORES.MAPA_CARD).click();

        await esperarTextoVisivel(page, TEXTOS.MAPA_COMPETENCIAS_TECNICAS);

        await esperarElementoVisivel(page, SELETORES.INFO_UNIDADE);
        await expect(page.getByTestId(SELETORES.INFO_UNIDADE)).toContainText('STIC');

        const competencias = page.getByTestId(SELETORES.BLOCO_COMPETENCIA);
        await expect(competencias.first()).toBeVisible();

        const primeiraCompetencia = competencias.first();
        const descricaoCompetencia = primeiraCompetencia.getByTestId('competencia-descricao');
        await expect(descricaoCompetencia).toBeVisible();

        const atividades = page.getByTestId(SELETORES.ITEM_ATIVIDADE);
        if (await atividades.count() > 0) {
            await expect(atividades.first()).toBeVisible();

            const conhecimentos = page.getByTestId(SELETORES.ITEM_CONHECIMENTO);
            if (await conhecimentos.count() > 0) {
                await expect(conhecimentos.first()).toBeVisible();
            }
        }
    });

    test('SERVIDOR: deve verificar que não tem botões de ação', async ({page}) => {
        await loginComoServidor(page);

        await irParaProcessoPorTexto(page, 'Revisão de mapeamento STIC/COINF');
        await page.waitForSelector('[data-testid="mapa-card"]');
        await page.getByTestId(SELETORES.MAPA_CARD).click();

        await expect(page.getByTestId('validar-btn')).not.toBeVisible();
        await expect(page.getByTestId('apresentar-sugestoes-btn')).not.toBeVisible();
        await expect(page.getByTestId('registrar-aceite-btn')).not.toBeVisible();
    });
});