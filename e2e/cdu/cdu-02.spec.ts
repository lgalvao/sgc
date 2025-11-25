import { expect } from '@playwright/test';
import { vueTest as test } from '../support/vue-specific-setup';
import {
    criarEIniciarProcessoBasico,
    criarProcessoBasico,
    verificarNavegacaoPaginaCadastroProcesso,
} from '~/helpers';
import { PaginaLogin } from '~/helpers/pages/login-page';
import { PaginaPainel } from '~/helpers/pages/painel-page';
import { USUARIOS } from '~/helpers/dados/constantes';

test.describe('CDU-02: Visualizar Painel', () => {
    // Clean up of 'processos em andamento' is removed as the fixture now guarantees an isolated DB per test.

    test.describe('Visibilidade de Componentes por Perfil', () => {
        test('deve exibir painel com seções Processos e Alertas para SERVIDOR', async ({ page }) => {
            const loginPage = new PaginaLogin(page);
            const painelPage = new PaginaPainel(page);

            await test.step('Preparação: Criar dados como ADMIN', async () => {
                await loginPage.realizarLogin(USUARIOS.ADMIN.titulo, USUARIOS.ADMIN.senha);
                
                const nomeProcesso = `Processo Servidor SESEL ${Date.now()}`;
                await criarEIniciarProcessoBasico(page, nomeProcesso, 'MAPEAMENTO', ['SESEL'], '2025-12-31');
            });

            await test.step('Ação: Logar como SERVIDOR', async () => {
                await loginPage.realizarLogin(USUARIOS.SERVIDOR.titulo, USUARIOS.SERVIDOR.senha);
            });

            await test.step('Verificação: Elementos do Painel', async () => {
                await painelPage.verificarTituloProcessos();
                await painelPage.verificarTituloAlertas();
                await painelPage.verificarTabelaProcessosVisivel();
                await painelPage.verificarColunasTabelaProcessos();
                await painelPage.verificarBotaoCriarProcesso(false);
                await painelPage.verificarTabelaAlertasVisivel();
            });
        });
    });

    test.describe('Tabela de Processos', () => {
        let nomeProcessoSgp: string;
        let nomeProcessoCojur: string;

        test.beforeEach(async ({ page }) => {
            const loginPage = new PaginaLogin(page);
            await loginPage.realizarLogin(USUARIOS.ADMIN.titulo, USUARIOS.ADMIN.senha);

            nomeProcessoSgp = `Processo da SGP para CDU-02 ${Date.now()}`;
            await criarEIniciarProcessoBasico(page, nomeProcessoSgp, 'MAPEAMENTO', ['SGP'], '2025-12-31');

            nomeProcessoCojur = `Processo COJUR - Fora da SGP ${Date.now()}`;
            await criarEIniciarProcessoBasico(page, nomeProcessoCojur, 'MAPEAMENTO', ['COJUR'], '2025-12-31');
        });

        test('deve exibir apenas processos da unidade do usuário (e subordinadas)', async ({ page }) => {
            const loginPage = new PaginaLogin(page);
            const painelPage = new PaginaPainel(page);

            await loginPage.realizarLogin(USUARIOS.CHEFE_SGP.titulo, USUARIOS.CHEFE_SGP.senha);

            // Chefe SGP deve ver o processo da sua unidade
            await painelPage.aguardarProcessoNoPainel(new RegExp(nomeProcessoSgp));

            // Não deve ver processo da COJUR
            await painelPage.verificarProcessoNaoVisivel(new RegExp(nomeProcessoCojur));

            // A tabela deve conter exatamente 1 processo visível para este usuário
            await painelPage.verificarQuantidadeProcessosNaTabela(1);
        });
    });

    test.describe('Navegação a partir do Painel', () => {
        let nomeProcessoTeste: string;

        test.beforeEach(async ({ page }) => {
            const loginPage = new PaginaLogin(page);
            await loginPage.realizarLogin(USUARIOS.ADMIN.titulo, USUARIOS.ADMIN.senha);
            nomeProcessoTeste = `Processo para Teste de Navegação ${Date.now()}`;
            // Usando SGP pois STIC pode estar bloqueada por processos em andamento (SEDESENV)
            await criarProcessoBasico(page, nomeProcessoTeste, 'MAPEAMENTO', ['SGP']);
        });

        test('ADMIN deve navegar para a edição ao clicar em processo "Criado"', async ({ page }) => {
            const loginPage = new PaginaLogin(page);
            const painelPage = new PaginaPainel(page);

            // Note: creating process already leaves us logged in as admin, but explicitly logging in is safer for test isolation/readability if beforeEach didn't imply state persistence (though Playwright page is persistent per test).
            // Since we are in the same test execution (same page), we are logged in.
            // But to be consistent with the previous test block structure...
            // Actually, we are already logged in from beforeEach.
            
            await painelPage.clicarProcessoNaTabela(new RegExp(nomeProcessoTeste));
            await verificarNavegacaoPaginaCadastroProcesso(page);
        });
    });

    test.describe('Tabela de Alertas', () => {
        test.beforeEach(async ({ page }) => {
            const loginPage = new PaginaLogin(page);
            await loginPage.realizarLogin(USUARIOS.ADMIN.titulo, USUARIOS.ADMIN.senha);
        });

        test('deve mostrar alertas na tabela com as colunas corretas', async ({ page }) => {
            const painelPage = new PaginaPainel(page);
            await painelPage.verificarTituloAlertas();
            await painelPage.verificarTabelaAlertasVisivel();
            await painelPage.verificarColunasTabelaAlertas();
        });

        test('deve exibir alertas ordenados por data/hora decrescente inicialmente', async ({ page }) => {
            const painelPage = new PaginaPainel(page);
            await painelPage.verificarAlertasOrdenadosPorDataHora();
        });
    });
});