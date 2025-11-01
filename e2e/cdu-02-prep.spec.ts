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
    verificarVisibilidadeProcesso,
    criarProcesso,
    submeterProcesso,
    limparProcessos,
} from './helpers';
import {
    loginComoAdmin,
    loginComoChefe,
    loginComoChefeStic,
    loginComoGestor,
    loginComoServidor,
} from './helpers';

test.describe('CDU-02: Visualizar Painel', () => {
    let processoId;

    test.beforeAll(async ({page}) => {
        await loginComoAdmin(page);
        processoId = await criarProcesso(page, 'MAPEAMENTO', 'Processo para CDU-02', ['SGP']);
        await submeterProcesso(page, processoId);
    });

    test.afterAll(async ({page}) => {
        await loginComoAdmin(page);
        await limparProcessos(page);
    });

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

        test('deve exibir processos em situação "Em Andamento" para GESTOR', async ({page}) => {
            await loginComoGestor(page);
            await verificarVisibilidadeProcesso(page, /Processo para CDU-02/, true);
        });

    });

     test.describe('Tabela de Processos', () => {
         test('deve exibir apenas processos da unidade do usuário (e subordinadas)', async ({page}) => {
             await loginComoChefeStic(page);
             // Chefe STIC não deve ver processo da SGP
             await verificarVisibilidadeProcesso(page, /Processo para CDU-02/, false);
         });
     });

    test.describe('Navegação a partir do Painel', () => {
        test('ADMIN deve navegar para a edição ao clicar em processo "Criado"', async ({page}) => {
            await loginComoAdmin(page);
            const id = await criarProcesso(page, 'MAPEAMENTO', 'Processo Criado CDU-02', ['SGP']);
            await clicarProcesso(page, /Processo Criado CDU-02/);
            await verificarNavegacaoPaginaCadastroProcesso(page);
            await limparProcessos(page);
        });
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
