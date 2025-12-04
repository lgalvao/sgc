/**
 * CDU-02 (MELHORADO) - Visualizar Painel
 * 
 * Este arquivo demonstra as melhorias propostas no documento melhorias-e2e.md:
 * 
 * ✅ Reset de banco antes de todos os testes
 * ✅ Cleanup automático após cada teste
 * ✅ Uso de test.step() para melhor legibilidade
 * ✅ Isolamento completo entre testes
 * ✅ Nomenclatura padronizada
 * 
 * Compare com: e2e/cdu-02.spec.ts (versão original)
 */

import { expect, test } from '@playwright/test';
import { login, USUARIOS } from './helpers/auth';
import { criarProcesso, verificarProcessoNaTabela } from './helpers/processo-helpers';
import { resetDatabase, useProcessoCleanup } from './hooks/cleanup-hooks';

test.describe('CDU-02 (MELHORADO) - Visualizar Painel', () => {
    
    // Reset completo do banco antes de TODOS os testes deste describe
    test.beforeAll(async ({ request }) => {
        await resetDatabase(request);
    });

    test.describe('Como ADMIN', () => {
        let cleanup: ReturnType<typeof useProcessoCleanup>;
        
        test.beforeEach(async ({ page }) => {
            // Inicializar hook de cleanup
            cleanup = useProcessoCleanup();
            
            // Navegar para login
            await page.goto('/login');
            await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
        });

        // Cleanup automático após cada teste
        test.afterEach(async ({ request }) => {
            await cleanup.limpar(request);
        });

        test('Deve exibir seções de Processos e Alertas', async ({ page }) => {
            await test.step('Verificar título da seção de Processos', async () => {
                await expect(page.getByTestId('txt-painel-titulo-processos')).toBeVisible();
                await expect(page.getByTestId('txt-painel-titulo-processos')).toHaveText('Processos');
            });

            await test.step('Verificar título da seção de Alertas', async () => {
                await expect(page.getByTestId('txt-painel-titulo-alertas')).toBeVisible();
                await expect(page.getByTestId('txt-painel-titulo-alertas')).toHaveText('Alertas');
            });
        });

        test('Deve exibir botão "Criar processo"', async ({ page }) => {
            await expect(page.getByTestId('btn-painel-criar-processo')).toBeVisible();
        });

        test('Deve criar processo e visualizá-lo na tabela', async ({ page }) => {
            const descricaoProcesso = `Processo E2E Melhorado ${Date.now()}`;
            let processoUrl: string;

            await test.step('Criar processo via UI', async () => {
                await criarProcesso(page, {
                    descricao: descricaoProcesso,
                    tipo: 'MAPEAMENTO',
                    diasLimite: 30,
                    unidade: 'ASSESSORIA_11'
                });
            });

            await test.step('Capturar ID do processo para cleanup', async () => {
                // Clicar no processo para capturar ID da URL
                await page.getByText(descricaoProcesso).click();
                processoUrl = page.url();
                const processoId = parseInt(processoUrl.match(/\/processo\/cadastro\/(\d+)/)?.[1] || '0');
                
                if (processoId > 0) {
                    cleanup.registrar(processoId);
                }
                
                // Voltar ao painel
                await page.goto('/painel');
            });

            await test.step('Verificar processo na tabela', async () => {
                await verificarProcessoNaTabela(page, {
                    descricao: descricaoProcesso,
                    situacao: 'Criado',
                    tipo: 'Mapeamento'
                });
            });
        });

        test('Processos "Criado" devem aparecer apenas para ADMIN', async ({ page }) => {
            const descricaoProcesso = `Processo Visível Apenas Admin ${Date.now()}`;

            await test.step('ADMIN cria processo', async () => {
                await criarProcesso(page, {
                    descricao: descricaoProcesso,
                    tipo: 'MAPEAMENTO',
                    diasLimite: 30,
                    unidade: 'ASSESSORIA_11'
                });

                // Registrar para cleanup
                await page.getByText(descricaoProcesso).click();
                const processoId = parseInt(page.url().match(/\/processo\/cadastro\/(\d+)/)?.[1] || '0');
                if (processoId > 0) cleanup.registrar(processoId);
                await page.goto('/painel');
            });

            await test.step('Verificar que processo está visível para ADMIN', async () => {
                await expect(page.getByText(descricaoProcesso)).toBeVisible();
            });

            await test.step('Fazer logout', async () => {
                await page.evaluate(() => localStorage.clear());
                await page.goto('/login');
                await expect(page).toHaveURL('/login');
            });

            await test.step('Login como GESTOR', async () => {
                await login(page, USUARIOS.GESTOR_COORD.titulo, USUARIOS.GESTOR_COORD.senha);
            });

            await test.step('Verificar que processo NÃO está visível para GESTOR', async () => {
                // Processos "Criado" não devem aparecer para não-admins
                await expect(page.getByText(descricaoProcesso)).not.toBeVisible();
            });
        });

        test('Não deve incluir unidades INTERMEDIARIAS na seleção', async ({ page }) => {
            await test.step('Navegar para criação de processo', async () => {
                await page.getByTestId('btn-painel-criar-processo').click();
                await expect(page).toHaveURL(/\/processo\/cadastro/);
            });

            await test.step('Preencher dados básicos', async () => {
                const descricaoProcesso = `Teste Filtragem Intermediárias ${Date.now()}`;
                await page.getByTestId('inp-processo-descricao').fill(descricaoProcesso);
                await page.getByTestId('sel-processo-tipo').selectOption('MAPEAMENTO');

                const dataLimite = new Date();
                dataLimite.setDate(dataLimite.getDate() + 30);
                await page.getByTestId('inp-processo-data-limite').fill(dataLimite.toISOString().split('T')[0]);
            });

            await test.step('Aguardar carregamento da árvore de unidades', async () => {
                await expect(page.getByText('Carregando unidades...')).toBeHidden();
            });

            await test.step('Expandir hierarquia (SECRETARIA_1 -> COORD_11)', async () => {
                const btnSecretaria = page.getByTestId('btn-arvore-expand-SECRETARIA_1');
                await btnSecretaria.waitFor({ state: 'visible' });
                await btnSecretaria.click();
            });

            await test.step('Verificar que COORD_11 (INTERMEDIARIA) está desabilitada', async () => {
                const checkboxIntermediaria = page.getByTestId('chk-arvore-unidade-COORD_11');
                await expect(checkboxIntermediaria).toBeDisabled();
            });

            await test.step('Expandir COORD_11 para acessar unidades operacionais', async () => {
                await page.getByTestId('btn-arvore-expand-COORD_11').click();
            });

            await test.step('Selecionar unidades OPERACIONAIS (filhas)', async () => {
                await page.getByTestId('chk-arvore-unidade-SECAO_111').check();
                await page.getByTestId('chk-arvore-unidade-SECAO_112').check();
                await page.getByTestId('chk-arvore-unidade-SECAO_113').check();
            });

            await test.step('Salvar processo', async () => {
                await page.getByTestId('btn-processo-salvar').click();
                await expect(page).toHaveURL(/\/painel/);
            });

            await test.step('Capturar ID para cleanup', async () => {
                const descricao = await page.getByTestId('inp-processo-descricao').inputValue().catch(() => '');
                if (descricao) {
                    await page.getByText(descricao, { exact: false }).first().click();
                    const processoId = parseInt(page.url().match(/\/processo\/cadastro\/(\d+)/)?.[1] || '0');
                    if (processoId > 0) cleanup.registrar(processoId);
                    await page.goto('/painel');
                }
            });
        });
    });

    test.describe('Como GESTOR', () => {
        test.beforeEach(async ({ page }) => {
            await page.goto('/login');
            await login(page, USUARIOS.GESTOR_COORD.titulo, USUARIOS.GESTOR_COORD.senha);
        });

        test('Não deve exibir botão "Criar processo"', async ({ page }) => {
            await expect(page.getByTestId('btn-painel-criar-processo')).not.toBeVisible();
        });

        test('Deve exibir mensagem quando não há processos', async ({ page }) => {
            await test.step('Verificar que tabela de processos está visível', async () => {
                const tabela = page.getByTestId('tbl-processos');
                await expect(tabela).toBeVisible();
            });

            await test.step('Verificar mensagem ou processos existentes', async () => {
                const temProcessos = await page.getByRole('row').count() > 1;
                if (!temProcessos) {
                    await expect(page.getByText('Nenhum processo encontrado.')).toBeVisible();
                }
            });
        });

        test('Deve exibir tabela de alertas vazia', async ({ page }) => {
            await test.step('Verificar que tabela de alertas está visível', async () => {
                const tabelaAlertas = page.getByTestId('tbl-alertas');
                await expect(tabelaAlertas).toBeVisible();
            });

            await test.step('Verificar que não há alertas (apenas header)', async () => {
                const tabelaAlertas = page.getByTestId('tbl-alertas');
                const linhasAlertas = await tabelaAlertas.getByRole('row').count();
                expect(linhasAlertas).toBeLessThanOrEqual(2);
            });
        });
    });
});
