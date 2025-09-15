import {expect} from '@playwright/test';
import {vueTest as test} from '../../tests/vue-specific-setup';
import {
    esperarElementoInvisivel,
    esperarElementoVisivel,
    esperarUrl,
    loginComoAdmin,
    loginComoChefe,
    loginComoGestor,
    loginComoServidor
} from './auxiliares-teste';
import {clicarPrimeiroProcesso} from './auxiliares-navegacao';
import {SELETORES, TEXTOS, URLS} from './constantes-teste';

// CDU-02: Visualizar Painel
test.describe('CDU-02: Visualizar Painel', () => {
    // Testes de visibilidade geral e por perfil
    test.describe('Visibilidade de Componentes por Perfil', () => {
        test('deve exibir painel com seções Processos e Alertas para SERVIDOR', async ({page}) => {
            await loginComoServidor(page);
            await esperarUrl(page, URLS.PAINEL);
            await page.waitForLoadState('networkidle');

            await esperarElementoVisivel(page, SELETORES.TITULO_PROCESSOS);
            await esperarElementoVisivel(page, SELETORES.TABELA_PROCESSOS);
            await esperarElementoInvisivel(page, SELETORES.BTN_CRIAR_PROCESSO);
            await esperarElementoVisivel(page, SELETORES.TABELA_ALERTAS);
        });

        test('deve exibir painel para GESTOR sem botão Criar processo', async ({page}) => {
            await loginComoGestor(page);
            await esperarUrl(page, URLS.PAINEL);
            await page.waitForLoadState('networkidle');

            await esperarElementoVisivel(page, SELETORES.TITULO_PROCESSOS);
            await esperarElementoVisivel(page, SELETORES.TABELA_PROCESSOS);
            await esperarElementoInvisivel(page, SELETORES.BTN_CRIAR_PROCESSO);
        });

        test('deve exibir painel para CHEFE sem botão Criar processo', async ({page}) => {
            await loginComoChefe(page);
            await esperarUrl(page, URLS.PAINEL);
            await page.waitForLoadState('networkidle');

            await esperarElementoVisivel(page, SELETORES.TITULO_PROCESSOS);
            await esperarElementoVisivel(page, SELETORES.TABELA_PROCESSOS);
            await esperarElementoInvisivel(page, SELETORES.BTN_CRIAR_PROCESSO);
        });

        test('deve exibir o botão "Criar processo" e processos "Criado" apenas para ADMIN', async ({page}) => {
            await loginComoAdmin(page);
            await esperarUrl(page, URLS.PAINEL);
            await page.waitForLoadState('networkidle');

            // Requisito 2.3: Botão "Criar processo" visível
            await esperarElementoVisivel(page, SELETORES.BTN_CRIAR_PROCESSO);

            // Requisito 2.2: Processos 'Criado' visíveis
            const processoCriadoRow = page.getByRole('row', {name: /Processo teste revisão CDU-05/});
            await expect(processoCriadoRow).toBeVisible();
            await expect(processoCriadoRow).toContainText('Criado');

            // Requisito 2.2: Processos 'Criado' não visíveis para outros perfis
            await loginComoGestor(page);
            await esperarUrl(page, URLS.PAINEL);
            await page.waitForLoadState('networkidle');
            await expect(processoCriadoRow).toBeHidden();
        });
    });

    // Testes da Tabela de Processos
    test.describe('Tabela de Processos', () => {
        const getColumnValues = async (page, columnIndex: number) => {
            return page.locator(`${SELETORES.TABELA_PROCESSOS} tbody tr`).evaluateAll(rows =>
                rows.map(row => (row.children[columnIndex] as HTMLElement).innerText.trim())
            );
        };

        test('deve ordenar processos por descrição (asc/desc)', async ({page}) => {
            await loginComoAdmin(page);
            await page.waitForLoadState('networkidle');
            const descriptionColumnIndex = 0; // Primeira coluna

            const valuesBefore = await getColumnValues(page, descriptionColumnIndex);
            const sortedAsc = [...valuesBefore].sort((a, b) => a.localeCompare(b));
            const sortedDesc = [...sortedAsc].reverse();

            // Ordena ascendente
            await page.click(`[data-testid="${SELETORES.COLUNA_DESCRICAO}"]`);
            const valuesAfterAsc = await getColumnValues(page, descriptionColumnIndex);
            expect(valuesAfterAsc).toEqual(sortedAsc);

            // Ordena descendente
            await page.click(`[data-testid="${SELETORES.COLUNA_DESCRICAO}"]`);
            const valuesAfterDesc = await getColumnValues(page, descriptionColumnIndex);
            expect(valuesAfterDesc).toEqual(sortedDesc);
        });

        test('deve exibir apenas processos da unidade do usuário (e subordinadas)', async ({page}) => {
            // O Chefe da STIC (id 5) só deve ver processos da STIC
            await loginComoChefe(page, '5'); // Convertido para string
            await esperarUrl(page, URLS.PAINEL);
            await page.waitForLoadState('networkidle');

            const processoStic = page.getByRole('row', {name: /Revisão de mapa de competências STIC - 2024/});
            const processoCojur = page.getByRole('row', {name: /Mapeamento inicial COJUR - 2025/});

            await expect(processoStic).toBeVisible();
            await expect(processoCojur).toBeHidden();
        });
    });

    // Testes de Navegação
    test.describe('Navegação a partir do Painel', () => {
        test('deve navegar para a edição ao clicar em processo "Criado" como ADMIN', async ({page}) => {
            await loginComoAdmin(page);
            await page.getByRole('row', {name: /Processo teste revisão CDU-05/}).click();
            await esperarUrl(page, /.*\/processo\/cadastro\?idProcesso=\d+/);
            await page.waitForLoadState('networkidle');
            await expect(page.getByRole('heading', {name: 'Cadastro de Processo'})).toBeVisible();
        });

        test('deve permitir SERVIDOR navegar para subprocesso', async ({page}) => {
            await loginComoServidor(page);
            await clicarPrimeiroProcesso(page);
            await page.waitForLoadState('networkidle');

            await expect(page).toHaveURL(/.*\/processo\/\d+\/\w+$/);
            await esperarElementoVisivel(page, SELETORES.SUBPROCESSO_HEADER);
            await esperarElementoVisivel(page, SELETORES.PROCESSO_INFO);
        });

        test('deve permitir GESTOR navegar para processo e depois subprocesso', async ({page}) => {
            test.slow();
            await loginComoGestor(page);
            // Clicar explicitamente em um processo que sabemos que envolve SGP e COEDE
            await page.getByTestId(SELETORES.TABELA_PROCESSOS).getByRole('row', {name: /Mapeamento de competências - 2025/}).click();
            await page.waitForLoadState('networkidle');

            await esperarElementoVisivel(page, SELETORES.PROCESSO_INFO);

            await page.locator(SELETORES.TREE_TABLE_ROW).first().waitFor();
            await page.getByTestId('btn-expandir-todas').click();
            await page.waitForLoadState('networkidle');

            const coedeRow = page.getByRole('row', {name: 'COEDE'});
            await expect(coedeRow).toBeVisible();
            await coedeRow.click();

            // GESTOR deve navegar para a página de detalhes do processo, não do subprocesso
            await expect(page).toHaveURL(/.*\/processo\/\d+$/);
            await esperarElementoVisivel(page, SELETORES.PROCESSO_INFO);
        });

        test('deve permitir CHEFE navegar para subprocesso', async ({page}) => {
            await loginComoChefe(page);
            await clicarPrimeiroProcesso(page);
            await page.waitForLoadState('networkidle');

            await expect(page).toHaveURL(/.*\/processo\/\d+\/\w+$/);
            await esperarElementoVisivel(page, SELETORES.SUBPROCESSO_HEADER);
            await esperarElementoVisivel(page, SELETORES.PROCESSO_INFO);
        });
    });

    // Testes da Tabela de Alertas
    test.describe('Tabela de Alertas', () => {
        test('deve mostrar alertas na tabela com as colunas corretas', async ({page}) => {
            await loginComoAdmin(page);
            await page.waitForLoadState('networkidle');

            await esperarElementoVisivel(page, SELETORES.TITULO_ALERTAS);
            await esperarElementoVisivel(page, SELETORES.TABELA_ALERTAS);

            const tabelaAlertas = page.getByTestId(SELETORES.TABELA_ALERTAS);
            await expect(tabelaAlertas).toContainText(TEXTOS.COLUNA_DATA_HORA);
            await expect(tabelaAlertas).toContainText(TEXTOS.COLUNA_DESCRICAO);
            await expect(tabelaAlertas).toContainText(TEXTOS.COLUNA_PROCESSO);
            await expect(tabelaAlertas).toContainText(TEXTOS.COLUNA_ORIGEM);
        });

        test('deve exibir alertas ordenados por data/hora decrescente inicialmente', async ({page}) => {
            await loginComoAdmin(page);
            await page.waitForLoadState('networkidle');
            const dateColumnIndex = 0;

            const dates = await page.locator(`${SELETORES.TABELA_ALERTAS} tbody tr`).evaluateAll(rows =>
                rows.map(row => {
                    const text = (row.children[dateColumnIndex] as HTMLElement).innerText.trim();
                    const [date, time] = text.split(' ');
                    const [day, month, year] = date.split('/');
                    return new Date(`${year}-${month}-${day}T${time}`).getTime();
                })
            );

            const sortedDates = [...dates].sort((a, b) => b - a);
            expect(dates).toEqual(sortedDates);
        });
    });
});
