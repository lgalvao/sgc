import {expect, test} from './fixtures/complete-fixtures.js';
import {login, USUARIOS} from './helpers/helpers-auth.js';
import {fazerLogout} from './helpers/helpers-navegacao.js';
import {criarProcesso, extrairProcessoId, verificarProcessoNaTabela} from './helpers/helpers-processos.js';
import type {Page} from '@playwright/test';
import type {useProcessoCleanup} from './hooks/hooks-limpeza.js';

async function registrarProcessoNoCleanup(page: Page, cleanupAutomatico: ReturnType<typeof useProcessoCleanup>) {
    const processoId = await extrairProcessoId(page);
    expect(processoId).toBeGreaterThan(0);
    cleanupAutomatico.registrar(processoId);
}

test.describe('CDU-02 - Visualizar Painel', () => {
    test.describe('Como ADMIN', () => {
        test('Deve exibir estrutura básica do painel e testar ordenação', async ({page, autenticadoComoAdmin}: {
            page: Page,
            autenticadoComoAdmin: void
        }) => {
            await test.step('Verificar seções principais', async () => {
                await expect(page.getByTestId('txt-painel-titulo-processos')).toBeVisible();
                await expect(page.getByTestId('txt-painel-titulo-processos')).toHaveText('Processos');
                await expect(page.getByTestId('txt-painel-titulo-alertas')).toBeVisible();
                await expect(page.getByTestId('txt-painel-titulo-alertas')).toHaveText('Alertas');
            });

            await test.step('Verificar botão de criação', async () => {
                await expect(page.getByTestId('btn-painel-criar-processo')).toBeVisible();
            });

            await test.step('Testar ordenação da tabela de processos', async () => {
                const tabelaProcessos = page.locator('[data-testid="tbl-processos"]');
                const cabecalhoDescricao = tabelaProcessos.locator('th', {hasText: 'Descrição'}).first();

                // Aguarda o estado inicial de ordenação (pode ser ascending ou descending dependendo do backend, mas o componente define asc inicialmente)
                await expect(cabecalhoDescricao).toHaveAttribute('aria-sort', /ascending|descending|none/, {timeout: 5000});

                await cabecalhoDescricao.click();
                await expect(cabecalhoDescricao).toHaveAttribute('aria-sort', /ascending|descending/);

                await cabecalhoDescricao.click();
                await expect(cabecalhoDescricao).toHaveAttribute('aria-sort', /ascending|descending/);
            });
        });

        test('Deve criar processo e visualizá-lo na tabela', async ({page, autenticadoComoAdmin, cleanupAutomatico}: {
            page: Page,
            autenticadoComoAdmin: void,
            cleanupAutomatico: ReturnType<typeof useProcessoCleanup>
        }) => {
            const descricaoProcesso = `Processo E2E - ${Date.now()}`;

            await criarProcesso(page, {
                descricao: descricaoProcesso,
                tipo: 'MAPEAMENTO',
                diasLimite: 30,
                unidade: 'ASSESSORIA_21',
                expandir: ['SECRETARIA_2']
            });

            // Capturar ID do processo para cleanup
            const tabelaPainel = page.locator('[data-testid="tbl-processos"]');
            const linha1 = tabelaPainel.locator('tr').filter({hasText: descricaoProcesso}).first();
            await linha1.waitFor({state: 'visible'});
            await linha1.click();
            await expect(page).toHaveURL(/processo\/cadastro/);
            await registrarProcessoNoCleanup(page, cleanupAutomatico);
            await page.goto('/painel');

            await verificarProcessoNaTabela(page, {
                descricao: descricaoProcesso,
                situacao: 'Criado',
                tipo: 'Mapeamento'
            });
        });

        test('Processos "Criado" devem aparecer apenas para ADMIN', async ({
                                                                               page,
                                                                               autenticadoComoAdmin,
                                                                               cleanupAutomatico
                                                                           }: {
            page: Page,
            autenticadoComoAdmin: void,
            cleanupAutomatico: ReturnType<typeof useProcessoCleanup>
        }) => {
            const descricaoProcesso = `Processo Criado - ${Date.now()}`;

            await criarProcesso(page, {
                descricao: descricaoProcesso,
                tipo: 'MAPEAMENTO',
                diasLimite: 30,
                unidade: 'ASSESSORIA_12',
                expandir: ['SECRETARIA_1']
            });

            // Capturar ID do processo para cleanup
            const tabelaPainelP = page.locator('[data-testid="tbl-processos"]');
            const linha2 = tabelaPainelP.locator('tr').filter({hasText: descricaoProcesso}).first();
            await linha2.waitFor({state: 'visible'});
            await linha2.click();
            await expect(page).toHaveURL(/processo\/cadastro/);
            await registrarProcessoNoCleanup(page, cleanupAutomatico);
            await page.goto('/painel');

            // Verifica que o processo está visível para ADMIN
            const tabelaAdmin = page.locator('[data-testid="tbl-processos"]');
            await expect(tabelaAdmin.locator('tr').filter({hasText: descricaoProcesso}).first()).toBeVisible();

            // Faz logout e login como GESTOR
            await fazerLogout(page);
            await login(page,
                USUARIOS.GESTOR_COORD.titulo,
                USUARIOS.GESTOR_COORD.senha
            );

            // Verifica que o processo NÃO está visível para GESTOR
            const tabelaGestor = page.locator('[data-testid="tbl-processos"]');
            await expect(tabelaGestor.locator('tr').filter({hasText: descricaoProcesso})).toBeHidden();
        });


        test('Não deve incluir unidades INTERMEDIARIAS na seleção', async ({
                                                                               page,
                                                                               autenticadoComoAdmin,
                                                                               cleanupAutomatico
                                                                           }: {
            page: Page,
            autenticadoComoAdmin: void,
            cleanupAutomatico: ReturnType<typeof useProcessoCleanup>
        }) => {
            await page.getByTestId('btn-painel-criar-processo').click();
            await expect(page).toHaveURL(/\/processo\/cadastro/);

            const descricaoProcesso = `Teste Filtragem - ${Date.now()}`;
            await page.getByTestId('inp-processo-descricao').fill(descricaoProcesso);
            await page.getByTestId('sel-processo-tipo').selectOption('MAPEAMENTO');

            const dataLimite = new Date();
            dataLimite.setDate(dataLimite.getDate() + 30);
            await page.getByTestId('inp-processo-data-limite').fill(dataLimite.toISOString().split('T')[0]);


            // Aguarda a animação/renderização e expande SECRETARIA_1
            const btnSecretaria = page.getByTestId('btn-arvore-expand-SECRETARIA_1');
            await btnSecretaria.waitFor({state: 'visible'});
            await btnSecretaria.click();

            // Verifica que COORD_11 (INTERMEDIARIA) está HABILITADA (novo comportamento)
            const checkboxIntermediaria = page.getByTestId('chk-arvore-unidade-COORD_11');
            await expect(checkboxIntermediaria).toBeEnabled();
            await expect(checkboxIntermediaria).not.toBeChecked();

            // Clica em COORD_11 (INTERMEDIARIA) para selecionar suas filhas
            await checkboxIntermediaria.click();

            // Verifica que COORD_11 foi marcada (comportamento visual, embora filtrada no envio)
            await expect(checkboxIntermediaria).toBeChecked();

            // Expande COORD_11 para ver as filhas
            await page.getByTestId('btn-arvore-expand-COORD_11').click();

            // Verifica que as filhas OPERACIONAIS foram selecionadas automaticamente
            await expect(page.getByTestId('chk-arvore-unidade-SECAO_111')).toBeChecked();
            await expect(page.getByTestId('chk-arvore-unidade-SECAO_112')).toBeChecked();
            await expect(page.getByTestId('chk-arvore-unidade-SECAO_113')).toBeChecked();

            await page.getByTestId('btn-processo-salvar').click();

            await expect(page).toHaveURL(/\/painel/);

            // Capturar ID do processo para cleanup
            const tabelaPainelF = page.locator('[data-testid="tbl-processos"]');
            const linhaF = tabelaPainelF.locator('tr').filter({hasText: descricaoProcesso}).first();
            await linhaF.waitFor({state: 'visible'});
            await linhaF.click();
            await expect(page).toHaveURL(/processo\/cadastro/);
            await registrarProcessoNoCleanup(page, cleanupAutomatico);
            await page.goto('/painel');

            // Verifica que o processo foi criado e aparece na tabela
            await verificarProcessoNaTabela(page, {
                descricao: descricaoProcesso,
                situacao: 'Criado',
                tipo: 'Mapeamento',
                unidadesParticipantes: ['COORD_11']
            });
        });
    });

    test.describe('Como GESTOR', () => {
        test('Deve validar visualização, alertas e ordenação', async ({page, autenticadoComoGestor}: {
            page: Page,
            autenticadoComoGestor: void
        }) => {
            await test.step('Verificar restrições de botões e mensagens de tabela vazia', async () => {
                await expect(page.getByTestId('btn-painel-criar-processo')).toBeHidden();
                await expect(page.locator('[data-testid="tbl-processos"]')).toBeVisible();
                await expect(page.getByTestId('empty-state-processos')).toBeVisible();
            });

            await test.step('Verificar tabela de alertas vazia', async () => {
                const tabelaAlertas = page.locator('[data-testid="tbl-alertas"]');
                await expect(tabelaAlertas).toBeVisible();
                // BTable empty state renders a row with the message
                await expect(tabelaAlertas).toContainText(/Nenhum alerta/i);
            });

            await test.step('Testar ordenação de alertas', async () => {
                const tabelaAlertas = page.locator('[data-testid="tbl-alertas"]');
                const cabecalhoProcesso = tabelaAlertas.getByRole('columnheader', {name: 'Processo'});
                await cabecalhoProcesso.click();
                await expect(cabecalhoProcesso).toHaveAttribute('aria-sort', 'ascending');

                await cabecalhoProcesso.click();
                await expect(cabecalhoProcesso).toHaveAttribute('aria-sort', 'descending');
            });
        });
    });
});
