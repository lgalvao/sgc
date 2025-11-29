import { expect, test } from '@playwright/test';
import { login, USUARIOS } from './helpers/auth';
import { criarProcesso, verificarProcessoNaTabela } from './helpers/processo-helpers';

test.describe('CDU-02 - Visualizar Painel', () => {
    test.beforeEach(async ({ page }) => await page.goto('/login'));

    test.describe('Como ADMIN', () => {
        test.beforeEach(async ({ page }) => await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha));

        test('Deve exibir seções de Processos e Alertas', async ({ page }) => {
            await expect(page.getByTestId('titulo-processos')).toBeVisible();
            await expect(page.getByTestId('titulo-processos')).toHaveText('Processos');

            await expect(page.getByTestId('titulo-alertas')).toBeVisible();
            await expect(page.getByTestId('titulo-alertas')).toHaveText('Alertas');
        });

        test('Deve exibir botão "Criar processo"', async ({ page }) => {
            await expect(page.getByTestId('btn-criar-processo')).toBeVisible();
        });

        test('Deve criar processo e visualizá-lo na tabela', async ({ page }) => {
            const descricaoProcesso = `Processo E2E - ${Date.now()}`;

            await criarProcesso(page, {
                descricao: descricaoProcesso,
                tipo: 'MAPEAMENTO',
                diasLimite: 30,
                unidade: 'ASSESSORIA_11'
            });

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
                unidade: 'ASSESSORIA_11'
            });

            // Verifica que o processo está visível para ADMIN
            await expect(page.getByText(descricaoProcesso)).toBeVisible();

            // Faz logout e login como GESTOR
            await page.getByTestId('btn-logout').click();
            await login(page,
                USUARIOS.GESTOR_COORD.titulo,
                USUARIOS.GESTOR_COORD.senha
            );

            // Verifica que o processo NÃO está visível para GESTOR
            await expect(page.getByText(descricaoProcesso)).not.toBeVisible();
        });

        test('Não deve incluir unidades INTERMEDIARIAS na seleção', async ({ page }) => {
            await page.getByTestId('btn-criar-processo').click();
            await expect(page).toHaveURL(/\/processo\/cadastro/);

            const descricaoProcesso = `Teste Filtragem - ${Date.now()}`;
            await page.getByTestId('input-descricao').fill(descricaoProcesso);
            await page.getByTestId('select-tipo').selectOption('MAPEAMENTO');

            const dataLimite = new Date();
            dataLimite.setDate(dataLimite.getDate() + 30);
            await page.getByTestId('input-dataLimite').fill(dataLimite.toISOString().split('T')[0]);

            // Expande a hierarquia de forma sequencial e natural
            // SECRETARIA_1 -> COORD_11
            // OBS: SEDOC é oculta na árvore, então SECRETARIA_1 aparece na raiz

            // Aguarda a animação/renderização e expande SECRETARIA_1
            const btnSecretaria = page.getByTestId('btn-expand-SECRETARIA_1');
            await btnSecretaria.waitFor({ state: 'visible' });
            await btnSecretaria.click();

            // Agora COORD_11 deve estar visível
            // Verifica que COORD_11 (INTERMEDIARIA) está desabilitada
            const checkboxIntermediaria = page.getByTestId('chk-COORD_11');
            await expect(checkboxIntermediaria).toBeDisabled();

            // COORD_11 (nível 2) não tem botão de expansão, suas filhas (SECAO_111) são exibidas automaticamente
            // Seleciona as filhas OPERACIONAIS
            await page.getByTestId('chk-SECAO_111').check();
            await page.getByTestId('chk-SECAO_112').check();
            await page.getByTestId('chk-SECAO_113').check();

            // Salva o processo
            await page.getByTestId('btn-salvar').click();

            await expect(page).toHaveURL(/\/painel/);

            // Verifica que o processo foi criado e aparece na tabela
            await expect(page.getByText(descricaoProcesso)).toBeVisible();
        });
    });

    test.describe('Como GESTOR', () => {
        test.beforeEach(async ({ page }) => {
            await login(page, USUARIOS.GESTOR_COORD.titulo, USUARIOS.GESTOR_COORD.senha);
        });

        test('Não deve exibir botão "Criar processo"', async ({ page }) => {
            await expect(page.getByTestId('btn-criar-processo')).not.toBeVisible();
        });

        test('Deve exibir mensagem quando não há processos', async ({ page }) => {
            const tabela = page.getByTestId('tabela-processos');
            await expect(tabela).toBeVisible();

            // Verifica se há a mensagem de "Nenhum processo encontrado" OU se há processos
            const temProcessos = await page.getByRole('row').count() > 1;
            if (!temProcessos) {
                await expect(page.getByText('Nenhum processo encontrado.')).toBeVisible();
            }
        });

        test('Deve exibir tabela de alertas vazia', async ({ page }) => {
            const tabelaAlertas = page.getByTestId('tabela-alertas');
            await expect(tabelaAlertas).toBeVisible();

            // Como não há alertas, a tabela deve estar vazia (apenas header)
            const linhasAlertas = await tabelaAlertas.getByRole('row').count();
            expect(linhasAlertas).toBeLessThanOrEqual(2);
        });
    });
});
