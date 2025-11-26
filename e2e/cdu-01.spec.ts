import { vueTest as test } from './support/vue-specific-setup';
import { PaginaLogin, PaginaPainel } from '~/helpers';
import { USUARIOS } from '~/helpers/dados/constantes';
import { loginComoAdmin, loginComoChefe, loginComMultiPerfilAdmin } from '~/helpers/auth';

test.describe('CDU-01: Fluxo de Login e Seleção de Perfil', () => {
    test.describe('Login Convencional (via UI)', () => {
        test('deve fazer login e ir direto para o painel com perfil único', async ({ page }) => {
            const loginPage = new PaginaLogin(page);
            const painelPage = new PaginaPainel(page);

            await loginPage.navegar();
            await loginPage.preencherCredenciais(USUARIOS.CHEFE_SGP.titulo, USUARIOS.CHEFE_SGP.senha);
            await loginPage.clicarEntrar();

            // Aguarda navegação para o painel
            await painelPage.verificarPainelChefeVisivel();
        });

        test('deve mostrar seleção de perfis para usuário com múltiplos perfis', async ({ page }) => {
            const loginPage = new PaginaLogin(page);
            const painelPage = new PaginaPainel(page);

            await loginPage.navegar();
            await loginPage.preencherCredenciais(USUARIOS.MULTI_PERFIL.titulo, USUARIOS.MULTI_PERFIL.senha);
            await loginPage.clicarEntrar();

            // Aguarda a interface de seleção de perfil aparecer
            await loginPage.verificarSelecaoPerfilVisivel();
            await loginPage.selecionarPerfil('ADMIN - STIC');
            await loginPage.clicarEntrar();

            // Aguarda navegação para o painel
            await painelPage.verificarPainelAdminVisivel();
        });
    });

    test.describe('Login Programático (Helpers de Teste)', () => {
        // Estes testes verificam se os helpers de atalho (usados em outros testes) estão funcionando
        test('deve funcionar para loginComoAdmin', async ({ page }) => {
            await loginComoAdmin(page);
            const painelPage = new PaginaPainel(page);
            await painelPage.verificarPainelAdminVisivel();
        });

        test('deve funcionar para loginComoChefe', async ({ page }) => {
            await loginComoChefe(page);
            const painelPage = new PaginaPainel(page);
            await painelPage.verificarPainelChefeVisivel();
        });

        test('deve funcionar para login com múltiplos perfis', async ({ page }) => {
            await loginComMultiPerfilAdmin(page);
            const painelPage = new PaginaPainel(page);
            await painelPage.verificarPainelAdminVisivel();
        });
    });
});