import {test} from '@playwright/test';
import {
    clicarBotaoEntrar,
    clicarBotaoSair,
    esperarNotificacaoLoginInvalido,
    login,
    loginComoAdmin,
    loginComoServidor,
    navegarParaHome,
    verificarElementosPainel,
    verificarEstruturaAdmin,
    verificarEstruturaServidor,
    verificarPaginaLogin,
} from './helpers';

test.describe('CDU-01: Realizar login e exibir estrutura das telas', () => {
    test('deve carregar a página de login corretamente', async ({page}) => {
        await navegarParaHome(page);
        await verificarPaginaLogin(page);
    });

    test('deve exibir erro para usuário não encontrado', async ({page}) => {
        await login(page, '0000000000'); // ID de usuário que não existe
        await clicarBotaoEntrar(page);
        await esperarNotificacaoLoginInvalido(page);
    });

    test('deve exibir estrutura da aplicação para SERVIDOR', async ({page}) => {
        await loginComoServidor(page);
        await verificarElementosPainel(page);
        await verificarEstruturaServidor(page);
    });

    test('deve exibir estrutura da aplicação para ADMIN com acesso às configurações', async ({page}) => {
        await loginComoAdmin(page);
        await verificarElementosPainel(page);
        await verificarEstruturaAdmin(page);
    });

    test('deve fazer logout e retornar para a tela de login', async ({page}) => {
        await loginComoServidor(page);
        await verificarElementosPainel(page);
        await clicarBotaoSair(page);

        await verificarPaginaLogin(page);
    });
});
