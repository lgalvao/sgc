import {vueTest as test} from '../../tests/vue-specific-setup';
import {esperarElementoVisivel, loginComoChefe, verificarUrl} from './auxiliares-teste';
import {clicarPrimeiroProcesso} from './auxiliares-navegacao';
import {SELETORES} from './constantes-teste';

test.describe('CDU-07: Detalhar subprocesso', () => {
    test.beforeEach(async ({page}) => await loginComoChefe(page));

    test('deve mostrar detalhes do subprocesso para CHEFE', async ({page}) => {
        await clicarPrimeiroProcesso(page);

        await verificarUrl(page, '/processo/\\d+/[^/]+');
        await esperarElementoVisivel(page, SELETORES.SUBPROCESSO_HEADER);
        await esperarElementoVisivel(page, SELETORES.PROCESSO_INFO);
    });
});