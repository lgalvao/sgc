import {vueTest as test} from './support/vue-specific-setup';
import {
    clicarProcesso,
    esperarElementoVisivel,
    SELETORES,
    verificarAlertasOrdenadosPorDataHora,
    verificarAusenciaBotaoCriarProcesso,
    verificarColunasTabelaAlertas,
    verificarElementosPainel,
    verificarNavegacaoPaginaCadastroProcesso,
    verificarNavegacaoPaginaDetalhesProcesso,
    verificarVisibilidadeProcesso,
} from './helpers';
import {
    loginComoAdmin,
    loginComoChefe,
    loginComoChefeStic,
    loginComoGestor,
    loginComoServidor,
} from 'e2e/helpers';

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

        test('deve exibir painel com seções Processos e Alertas para SERVIDOR', async ({page}) => {
            await loginComoServidor(page);
            await verificarElementosPainel(page);
            await verificarAusenciaBotaoCriarProcesso(page);
            await esperarElementoVisivel(page, SELETORES.TABELA_ALERTAS);
        });

        test('deve exibir o botão "Criar processo" para ADMIN', async ({page}) => {
            await loginComoAdmin(page);
            await esperarElementoVisivel(page, SELETORES.BTN_CRIAR_PROCESSO);
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
            await verificarVisibilidadeProcesso(page, /Revisão de mapa de competências STIC - 2024/, true);
            await verificarVisibilidadeProcesso(page, /Mapeamento inicial SEDOCAP - 2025/, false);
        });
    });

    test.describe('Navegação a partir do Painel', () => {
        test('ADMIN deve navegar para a edição ao clicar em processo "Criado"', async ({page}) => {
            await loginComoAdmin(page);
            await clicarProcesso(page, /Processo teste revisão CDU-05/);
            await verificarNavegacaoPaginaCadastroProcesso(page);
        });
        
        // NOTA: Navegação para processos EM_ANDAMENTO (GESTOR->Detalhes, CHEFE/SERVIDOR->Subprocesso)
        // será testada nos CDUs específicos (CDU-07, CDU-08) pois requer processos iniciados
        // com subprocessos já criados.
    });

    test.describe('Tabela de Alertas', () => {
        test.beforeEach(async ({page}) => {
            await loginComoAdmin(page);
        });

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
