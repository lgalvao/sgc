import {vueTest as test} from './support/vue-specific-setup';
import {
    clicarPrimeiroProcesso,
    clicarProcesso,
    clicarUnidade,
    esperarElementoVisivel,
    expandirTodasAsUnidades,
    SELETORES,
    verificarAlertasOrdenadosPorDataHora,
    verificarAusenciaBotaoCriarProcesso,
    verificarColunasTabelaAlertas,
    verificarElementosPainel,
    verificarNavegacaoPaginaCadastroProcesso,
    verificarNavegacaoPaginaDetalhesProcesso,
    verificarNavegacaoPaginaSubprocesso,
    verificarVisibilidadeProcesso,
    criarProcessoCompleto,
} from './helpers';
import {
    loginComoAdmin,
    loginComoChefe,
    loginComoGestor,
    loginComoServidor,
} from './helpers/auth';

test.describe('CDU-02: Visualizar Painel', () => {
    test.describe('Visibilidade de Componentes por Perfil', () => {
        const perfisSemAcessoCriacao = [
            {nome: 'GESTOR', funcaoLogin: loginComoGestor},
            {nome: 'CHEFE', funcaoLogin: loginComoChefe},
        ];

        for (const perfil of perfisSemAcessoCriacao) {
            test(`não deve exibir o botão "Criar processo" para ${perfil.nome}`, async ({page}) => {
                await perfil.funcaoLogin(page);
                await verificarElementosPainel(page);
                await verificarAusenciaBotaoCriarProcesso(page);
            });
        }

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
            // Criar o processo antes de verificar sua visibilidade
            await criarProcessoCompleto(page, 'Processo teste revisão CDU-05', 'Revisão', '23/10/2025', [5]);
            await verificarVisibilidadeProcesso(page, /Processo teste revisão CDU-05/, true);
        });

        test('não deve exibir processos em situação "Criado" para GESTOR', async ({page}) => {
            await loginComoGestor(page);
            await verificarVisibilidadeProcesso(page, /Processo teste revisão CDU-05/, false);
        });
    });

    test.describe('Tabela de Processos', () => {
        test.beforeEach(async ({page}) => {
            await loginComoAdmin(page);
        });

        test('deve exibir apenas processos da unidade do usuário (e subordinadas)', async ({page}) => {
            await loginComoChefe(page); // Chefe da STIC (id 5)
            await verificarVisibilidadeProcesso(page, /Revisão de mapa de competências STIC - 2024/, true);
            await verificarVisibilidadeProcesso(page, /Mapeamento inicial COJUR - 2025/, false);
        });
    });

    test.describe('Navegação a partir do Painel', () => {
        test('ADMIN deve navegar para a edição ao clicar em processo "Criado"', async ({page}) => {
            await loginComoAdmin(page);
            await clicarProcesso(page, /Processo teste revisão CDU-05/);
            await verificarNavegacaoPaginaCadastroProcesso(page);
        });

        const perfisNavegamSubprocesso = [
            {nome: 'SERVIDOR', funcaoLogin: loginComoServidor},
            {nome: 'CHEFE', funcaoLogin: loginComoChefe},
        ];

        for (const perfil of perfisNavegamSubprocesso) {
            test(`${perfil.nome} deve navegar para a visualização do subprocesso ao clicar em um processo`, async ({page}) => {
                await perfil.funcaoLogin(page);
                await clicarPrimeiroProcesso(page);
                await verificarNavegacaoPaginaSubprocesso(page);
            });
        }

        test('GESTOR deve navegar para os detalhes do processo e interagir com a árvore de unidades', async ({page}) => {
            await loginComoGestor(page);
            await clicarProcesso(page, /Mapeamento de competências - 2025/);
            await verificarNavegacaoPaginaDetalhesProcesso(page);
            await expandirTodasAsUnidades(page);
            await clicarUnidade(page, 'COEDE');
            await verificarNavegacaoPaginaDetalhesProcesso(page);
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