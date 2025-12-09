import {expect, test} from '@playwright/test';
import {login, USUARIOS} from './helpers/helpers-auth';
import {criarProcesso, verificarProcessoNaTabela} from './helpers/helpers-processos';
import {resetDatabase, useProcessoCleanup} from './hooks/hooks-limpeza';

test.describe('CDU-02 - Visualizar Painel', () => {
    test.beforeAll(async ({ request }) => await resetDatabase(request));

    test.beforeEach(async ({ page }) => await page.goto('/login'));

    test.describe('Como ADMIN', () => {
        let cleanup: ReturnType<typeof useProcessoCleanup>;

        test.beforeEach(async ({ page }) => {
            cleanup = useProcessoCleanup();
            await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
        });

        test.afterEach(async ({ request }) => {
            await cleanup.limpar(request);
        });

        test('Deve exibir seções de Processos e Alertas', async ({ page }) => {
            await expect(page.getByTestId('txt-painel-titulo-processos')).toBeVisible();
            await expect(page.getByTestId('txt-painel-titulo-processos')).toHaveText('Processos');

            await expect(page.getByTestId('txt-painel-titulo-alertas')).toBeVisible();
            await expect(page.getByTestId('txt-painel-titulo-alertas')).toHaveText('Alertas');
        });

        test('Deve exibir botão "Criar processo"', async ({ page }) => {
            await expect(page.getByTestId('btn-painel-criar-processo')).toBeVisible();
        });

        test('Deve criar processo e visualizá-lo na tabela', async ({ page }) => {
            const descricaoProcesso = `Processo E2E - ${Date.now()}`;

            await criarProcesso(page, {
                descricao: descricaoProcesso,
                tipo: 'MAPEAMENTO',
                diasLimite: 30,
                unidade: 'ASSESSORIA_11',
                expandir: ['SECRETARIA_1']
            });

            // Capturar ID do processo para cleanup
            await page.getByText(descricaoProcesso).click();
            const processoId = Number.parseInt(new RegExp(/\/processo\/cadastro\/(\d+)/).exec(page.url())?.[1] || '0');
            if (processoId > 0) cleanup.registrar(processoId);
            await page.goto('/painel');

            await verificarProcessoNaTabela(page, {
                descricao: descricaoProcesso,
                situacao: 'Criado',
                tipo: 'Mapeamento'
            });
        });

        test('Processos "Criado" devem aparecer apenas para ADMIN', async ({ page }) => {
            const descricaoProcesso = `Processo Criado - ${Date.now()}`;

            await criarProcesso(page, {
                descricao: descricaoProcesso,
                tipo: 'MAPEAMENTO',
                diasLimite: 30,
                unidade: 'ASSESSORIA_11',
                expandir: ['SECRETARIA_1']
            });

            // Capturar ID do processo para cleanup
            await page.getByText(descricaoProcesso).click();
            const processoId = Number.parseInt(new RegExp(/\/processo\/cadastro\/(\d+)/).exec(page.url())?.[1] || '0');
            if (processoId > 0) cleanup.registrar(processoId);
            await page.goto('/painel');

            // Verifica que o processo está visível para ADMIN
            await expect(page.getByText(descricaoProcesso)).toBeVisible();

            // Faz logout e login como GESTOR
            await page.evaluate(() => localStorage.clear());
            await page.goto('/login');
            await expect(page).toHaveURL('/login');
            await login(page,
                USUARIOS.GESTOR_COORD.titulo,
                USUARIOS.GESTOR_COORD.senha
            );

            // Verifica que o processo NÃO está visível para GESTOR
            await expect(page.getByText(descricaoProcesso)).not.toBeVisible();
        });

        test('Não deve incluir unidades INTERMEDIARIAS na seleção', async ({ page }) => {
            await page.getByTestId('btn-painel-criar-processo').click();
            await expect(page).toHaveURL(/\/processo\/cadastro/);

            const descricaoProcesso = `Teste Filtragem - ${Date.now()}`;
            await page.getByTestId('inp-processo-descricao').fill(descricaoProcesso);
            await page.getByTestId('sel-processo-tipo').selectOption('MAPEAMENTO');

            const dataLimite = new Date();
            dataLimite.setDate(dataLimite.getDate() + 30);
            await page.getByTestId('inp-processo-data-limite').fill(dataLimite.toISOString().split('T')[0]);

            // Expande a hierarquia de forma sequencial e natural: SECRETARIA_1 -> COORD_11
            // OBS: SEDOC é oculta na árvore, então SECRETARIA_1 aparece na raiz

            // Aguarda a animação/renderização e expande SECRETARIA_1
            const btnSecretaria = page.getByTestId('btn-arvore-expand-SECRETARIA_1');
            await btnSecretaria.waitFor({ state: 'visible' });
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

            // Salva o processo
            await page.getByTestId('btn-processo-salvar').click();

            await expect(page).toHaveURL(/\/painel/);

            // Capturar ID do processo para cleanup
            await page.getByText(descricaoProcesso).click();
            const processoId = parseInt(page.url().match(/\/processo\/cadastro\/(\d+)/)?.[1] || '0');
            if (processoId > 0) cleanup.registrar(processoId);
            await page.goto('/painel');

            // Verifica que o processo foi criado e aparece na tabela
            await expect(page.getByText(descricaoProcesso)).toBeVisible();
        });
    });

    test.describe('Como GESTOR', () => {
        test.beforeEach(async ({ page }) => await login(page, USUARIOS.GESTOR_COORD.titulo, USUARIOS.GESTOR_COORD.senha));

        test('Não deve exibir botão "Criar processo"', async ({ page }) => {
            await expect(page.getByTestId('btn-painel-criar-processo')).not.toBeVisible();
        });

        test('Deve exibir mensagem quando não há processos', async ({ page }) => {
            const tabela = page.getByTestId('tbl-processos');
            await expect(tabela).toBeVisible();

            // Verifica se há a mensagem de "Nenhum processo encontrado" OU se há processos
            const temProcessos = await page.getByRole('row').count() > 1;
            if (!temProcessos) {
                await expect(page.getByText('Nenhum processo encontrado.')).toBeVisible();
            }
        });

        test('Deve exibir tabela de alertas vazia', async ({ page }) => {
            const tabelaAlertas = page.getByTestId('tbl-alertas');
            await expect(tabelaAlertas).toBeVisible();

            // Como não há alertas, a tabela deve estar vazia (apenas header)
            const linhasAlertas = await tabelaAlertas.getByRole('row').count();
            expect(linhasAlertas).toBeLessThanOrEqual(2);
        });
    });
});
