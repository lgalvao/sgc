import {vueTest as test} from '../../tests/vue-specific-setup';
import {esperarElementoInvisivel, esperarTextoVisivel, loginComoAdmin} from './auxiliares-verificacoes';
import {irParaMapaCompetencias} from './auxiliares-navegacao';
import {DADOS_TESTE} from './constantes-teste';

test.describe('CDU-12: Verificar impactos no mapa de competências', () => {
    test('deve exibir mensagem de "Nenhum impacto" quando não houver divergências', async ({page}) => {
        await loginComoAdmin(page);

        await irParaMapaCompetencias(page, DADOS_TESTE.PROCESSOS.REVISAO_STIC.id, 'SESEL');
        await page.waitForLoadState('networkidle');

        await page.getByTestId('impactos-mapa-button').waitFor({state: 'visible'});
        await page.getByTestId('impactos-mapa-button').click();

        await esperarElementoInvisivel(page, 'impacto-mapa-modal');
        await esperarTextoVisivel(page, 'Nenhum impacto no mapa da unidade.');
    });
});