import {expect, Page, test} from '@playwright/test';
import {
    esperarElementoVisivel,
    loginComoAdmin,
    loginComoChefe,
    loginComoGestor,
    loginComoServidor,
    verificarAusenciaBotaoCriarProcesso,
    verificarNavegacaoPaginaCadastroProcesso,
    verificarNavegacaoPaginaDetalhesProcesso,
    verificarNavegacaoPaginaSubprocesso,
    verificarVisibilidadeProcesso,
    clicarPrimeiroProcesso,
    clicarProcesso,
    clicarUnidade,
    expandirTodasAsUnidades,
    ordenarTabelaProcessosPorColuna, verificarElementosPainel,
} from './helpers';
import {SELETORES, TEXTOS} from './helpers';

const obterValoresColuna = async (page: Page, indiceColuna: number) => {
    return page.locator(`${SELETORES.TABELA_PROCESSOS} tbody tr`).evaluateAll(linhas =>
        linhas.map(linha => (linha.children[indiceColuna] as HTMLElement).innerText.trim())
    );
};

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
            await verificarVisibilidadeProcesso(page, /Processo teste revisão CDU-05/, true);
        });

        test('não deve exibir processos em situação "Criado" para GESTOR', async ({page}) => {
            await loginComoGestor(page);
            await verificarVisibilidadeProcesso(page, /Processo teste revisão CDU-05/, false);
        });
    });

    test.describe('Tabela de Processos', () => {
        test.beforeEach(async ({page}) => await loginComoAdmin(page));

        test('deve ordenar processos por descrição (asc/desc)', async ({page}) => {
            const indiceColunaDescricao = 0;

            const valoresAntes = await obterValoresColuna(page, indiceColunaDescricao);
            const valoresOrdenadosAsc = [...valoresAntes].sort((a, b) => a.localeCompare(b));
            const valoresOrdenadosDesc = [...valoresOrdenadosAsc].reverse();

            await ordenarTabelaProcessosPorColuna(page, SELETORES.COLUNA_DESCRICAO);
            const valoresAposOrdenacaoAsc = await obterValoresColuna(page, indiceColunaDescricao);
            expect(valoresAposOrdenacaoAsc).toEqual(valoresOrdenadosAsc);

            await ordenarTabelaProcessosPorColuna(page, SELETORES.COLUNA_DESCRICAO);
            const valoresAposOrdenacaoDesc = await obterValoresColuna(page, indiceColunaDescricao);
            expect(valoresAposOrdenacaoDesc).toEqual(valoresOrdenadosDesc);
        });

        test('deve exibir apenas processos da unidade do usuário (e subordinadas)', async ({page}) => {
            await loginComoChefe(page, '5'); // Chefe da STIC (id 5)
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
            test.slow();
            await loginComoGestor(page);
            await clicarProcesso(page, /Mapeamento de competências - 2025/);
            await verificarNavegacaoPaginaDetalhesProcesso(page);
            await expandirTodasAsUnidades(page);
            await clicarUnidade(page, 'COEDE');
            await verificarNavegacaoPaginaDetalhesProcesso(page);
        });
    });

    test.describe('Tabela de Alertas', () => {
        test.beforeEach(async ({page}) => await loginComoAdmin(page));

        test('deve mostrar alertas na tabela com as colunas corretas', async ({page}) => {
            await esperarElementoVisivel(page, SELETORES.TITULO_ALERTAS);
            await esperarElementoVisivel(page, SELETORES.TABELA_ALERTAS);

            const tabelaAlertas = page.getByTestId(SELETORES.TABELA_ALERTAS);
            await expect(tabelaAlertas).toContainText(TEXTOS.COLUNA_DATA_HORA);
            await expect(tabelaAlertas).toContainText(TEXTOS.COLUNA_DESCRICAO);
            await expect(tabelaAlertas).toContainText(TEXTOS.COLUNA_PROCESSO);
            await expect(tabelaAlertas).toContainText(TEXTOS.COLUNA_ORIGEM);
        });

        test('deve exibir alertas ordenados por data/hora decrescente inicialmente', async ({page}) => {
            const indiceColunaData = 0;

            const datas = await page.locator(`${SELETORES.TABELA_ALERTAS} tbody tr`).evaluateAll(linhas =>
                linhas.map(linha => {
                    const texto = (linha.children[indiceColunaData] as HTMLElement).innerText.trim();
                    const [data, hora] = texto.split(' ');
                    const [dia, mes, ano] = data.split('/');
                    return new Date(`${ano}-${mes}-${dia}T${hora}`).getTime();
                })
            );

            const datasOrdenadas = [...datas].sort((a, b) => b - a);
            expect(datas).toEqual(datasOrdenadas);
        });
    });
});