import {vueTest as test} from '../support/vue-specific-setup';
import {
    aguardarTabelaProcessosCarregada,
    clicarProcesso,
    criarProcessoBasico,
    esperarElementoVisivel,
    loginComoAdmin,
    loginComoChefeStic,
    loginComoServidor,
    SELETORES,
    verificarAlertasOrdenadosPorDataHora,
    verificarAusenciaBotaoCriarProcesso,
    verificarColunasTabelaAlertas,
    verificarElementosPainel,
    verificarNavegacaoPaginaCadastroProcesso,
    verificarQuantidadeProcessosNaTabela,
    verificarVisibilidadeProcesso,
} from '~/helpers';

test.describe('CDU-02: Visualizar Painel', () => {
    test.describe('Visibilidade de Componentes por Perfil', () => {
        const NOME_PROCESSO_CRIADO = 'Processo teste revisão CDU-05';

        test('deve exibir painel com seções Processos e Alertas para SERVIDOR', async ({page}) => {
            // Cria um processo em estado "EM_ANDAMENTO" para o SERVIDOR (Ana Paula Souza, SESEL)
            await loginComoAdmin(page);
            await criarProcessoBasico(page, 'Processo Servidor SESEL', 'MAPEAMENTO', ['SESEL'], '2025-12-31', 'EM_ANDAMENTO');

            await loginComoServidor(page);
            await verificarElementosPainel(page);
            await verificarAusenciaBotaoCriarProcesso(page);
            await esperarElementoVisivel(page, SELETORES.TABELA_ALERTAS);
        });
    });

    test.describe('Tabela de Processos', () => {
        test.beforeEach(async ({ page }) => {
            await loginComoAdmin(page);
            // Cria um processo visível para o Chefe da STIC (unidade 2)
            await criarProcessoBasico(page, 'Processo da STIC para CDU-02', 'MAPEAMENTO', ['STIC'], '2025-12-31', 'EM_ANDAMENTO');
            // Cria um processo fora da hierarquia da STIC
            await criarProcessoBasico(page, 'Processo ADMIN-UNIT - Fora da STIC', 'MAPEAMENTO', ['ADMIN-UNIT'], '2025-12-31', 'EM_ANDAMENTO');
        });

        test('deve exibir apenas processos da unidade do usuário (e subordinadas)', async ({page}) => {
            await loginComoChefeStic(page);

            // Aguardar tabela de processos carregar
            await aguardarTabelaProcessosCarregada(page);

            // Chefe STIC deve ver o processo da sua unidade
            await verificarVisibilidadeProcesso(page, /Processo da STIC para CDU-02/, true);

            // Não deve ver processo da ADMIN-UNIT
            await verificarVisibilidadeProcesso(page, /Processo ADMIN-UNIT - Fora da STIC/, false);

            // A tabela deve conter exatamente 1 processo visível para este usuário
            await verificarQuantidadeProcessosNaTabela(page, 1);
        });
    });

    test.describe('Navegação a partir do Painel', () => {
        const NOME_PROCESSO_CRIADO = 'Processo teste revisão CDU-05';

        test.beforeEach(async ({ page }) => {
            // Cria um processo em estado "CRIADO" para o teste de navegação
            await loginComoAdmin(page);
            await criarProcessoBasico(page, NOME_PROCESSO_CRIADO, 'REVISAO', ['CDU05-REV-UNIT']);
        });

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
