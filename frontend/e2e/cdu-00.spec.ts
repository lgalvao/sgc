import {vueTest as test} from './support/vue-specific-setup';
import {navegarParaLogin, verificarPaginaLogin} from './helpers';

test.describe('CDU-00: Baseline Test', () => {
    test('deve carregar a página de login corretamente', async ({page}) => {
        await navegarParaLogin(page);
        await verificarPaginaLogin(page);
    });
});
