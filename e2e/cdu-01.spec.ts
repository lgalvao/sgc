import {vueTest as test} from './support/vue-specific-setup';
import {
    clicarBotaoEntrar,
    loginComMultiPerfilAdmin,
    loginComoAdmin,
    loginComoChefe,
    navegarParaLogin,
    preencherFormularioLogin,
    selecionarPerfil,
    verificarPainelAdminVisivel,
    verificarPainelChefeVisivel,
    verificarSelecaoPerfilVisivel,
} from './helpers';
import {USUARIOS} from './helpers/dados/constantes-teste';

test.describe('CDU-01: Fluxo de Login e Seleção de Perfil', () => {
    test.describe('Login Convencional (via UI)', () => {
        test('deve fazer login e ir direto para o painel com perfil único', async ({page}) => {
            await navegarParaLogin(page);
            await preencherFormularioLogin(page, USUARIOS.CHEFE_SGP.titulo, USUARIOS.CHEFE_SGP.senha);
            await clicarBotaoEntrar(page);

            // Aguarda navegação para o painel
            await page.waitForURL('/painel', {timeout: 15000});
            await verificarPainelChefeVisivel(page);
        });

        test('deve mostrar seleção de perfis para usuário com múltiplos perfis', async ({page}) => {
            await navegarParaLogin(page);
            await preencherFormularioLogin(page, USUARIOS.MULTI_PERFIL.titulo, USUARIOS.MULTI_PERFIL.senha);
            await clicarBotaoEntrar(page);

            // Aguarda a interface de seleção de perfil aparecer
            await verificarSelecaoPerfilVisivel(page);
            await selecionarPerfil(page, 'ADMIN - STIC');
            await clicarBotaoEntrar(page);

            // Aguarda navegação para o painel
            await page.waitForURL('/painel', {timeout: 15000});
            await verificarPainelAdminVisivel(page);
        });
    });

    test.describe('Login Programático (Helpers de Teste)', () => {
        test('deve funcionar para loginComoAdmin', async ({page}) => {
            await loginComoAdmin(page);
            await verificarPainelAdminVisivel(page);
        });

        test('deve funcionar para loginComoChefe', async ({page}) => {
            await loginComoChefe(page);
            await verificarPainelChefeVisivel(page);
        });

        test('deve funcionar para login com múltiplos perfis', async ({page}) => {
            await loginComMultiPerfilAdmin(page);
            await verificarPainelAdminVisivel(page);
        });
    });
});
