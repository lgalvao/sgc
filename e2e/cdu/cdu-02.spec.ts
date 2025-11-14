import {vueTest as test} from '../support/vue-specific-setup';
import {expect} from '@playwright/test';
import {
    clicarProcesso,
    esperarElementoVisivel,
    loginComoAdmin,
    loginComoChefe,
    loginComoChefeStic,
    loginComoGestor,
    loginComoServidor,
    SELETORES,
    verificarAlertasOrdenadosPorDataHora,
    verificarAusenciaBotaoCriarProcesso,
    verificarColunasTabelaAlertas,
    verificarElementosPainel,
    verificarNavegacaoPaginaCadastroProcesso,
    verificarVisibilidadeProcesso,
} from '~/helpers';

test.describe('CDU-02: Visualizar Painel', () => {
    test.describe('Visibilidade de Componentes por Perfil', () => {
        test('não deve exibir o botão "Criar processo" para GESTOR', async ({page}) => {
            await loginComoGestor(page);
            await verificarElementosPainel(page);
            await verificarAusenciaBotaoCriarProcesso(page);
        });

        test('não deve exibir o botão "Criar processo" para CHEFE', async ({page}) => {
            await loginComoChefe(page);
            await verificarElementosPainel(page);
            await verificarAusenciaBotaoCriarProcesso(page);
        });

        test('deve exibir o botão "Criar processo" para ADMIN', async ({page}) => {
            await loginComoAdmin(page);
            await esperarElementoVisivel(page, SELETORES.BTN_CRIAR_PROCESSO);
        });

        test('deve exibir painel com seções Processos e Alertas para SERVIDOR', async ({page}) => {
            await loginComoServidor(page);
            await verificarElementosPainel(page);
            await verificarAusenciaBotaoCriarProcesso(page);
            await esperarElementoVisivel(page, SELETORES.TABELA_ALERTAS);
        });

        test('deve exibir processos em situação "Criado" apenas para ADMIN', async ({page}) => {
            await loginComoAdmin(page);
            await verificarVisibilidadeProcesso(page, /Processo teste revisão CDU-05/, true);
        });

        test('não deve exibir processos em situação "Criado" para GESTOR', async ({page}) => {
            await loginComoGestor(page);
            await verificarVisibilidadeProcesso(page, /Processo teste revisão CDU-05/, false);
        });
    });

     test.describe('Tabela de Processos', () => {
         test('deve exibir apenas processos da unidade do usuário (e subordinadas)', async ({page}) => {
             await loginComoChefeStic(page);

             // Aguardar tabela de processos carregar com pelo menos uma linha
             await page.getByTestId('tabela-processos').locator('tbody tr').first().waitFor({ state: 'visible' });

             // Chefe STIC deve ver processos da sua unidade (verifica quantidade)
             const tabela = page.getByTestId('tabela-processos');
             const linhas = tabela.locator('tbody tr');
             const count = await linhas.count();
             expect(count).toBeGreaterThan(0);

             // Não deve ver processo da ADMIN-UNIT (processo 5)
             await verificarVisibilidadeProcesso(page, /Processo ADMIN-UNIT - Fora da STIC/, false);
         });
     });

    test.describe('Navegação a partir do Painel', () => {
        test('ADMIN deve navegar para a edição ao clicar em processo "Criado"', async ({page}) => {
            await loginComoAdmin(page);
            await clicarProcesso(page, /Processo teste revisão CDU-05/);
            await verificarNavegacaoPaginaCadastroProcesso(page);
        });
    });

    test.describe('Tabela de Alertas', () => {
        test.beforeEach(async ({page}) => await loginComoAdmin(page));

        test('deve mostrar alertas na tabela com as colunas corretas', async ({page}) => {
            await esperarElementoVisivel(page, SELETORES.TITULO_ALERTAS);
            await esperarElementoVisivel(page, SELETORES.TABELA_ALERTAS);
            await verificarColunasTabelaAlertas(page);
        });

        test('deve exibir alertas ordenados por data/hora decrescente inicialmente', async ({page}) => {
            await verificarAlertasOrdenadosPorDataHora(page);
        });
    });
});
