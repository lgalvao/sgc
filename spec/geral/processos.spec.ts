import {expect} from '@playwright/test';
import {vueTest as test} from '../../tests/vue-specific-setup';
import {login, loginAsGestor} from "~/utils/auth";

test.describe('Detalhes do Processo - Unidades', () => {
    test.beforeEach(async ({page}) => {
        await login(page);
        await page.goto(`/processo/1`);
        await page.waitForLoadState('networkidle'); // Added this line
    });

    test('deve exibir os detalhes do processo e a tabela de unidades participantes', async ({page}) => {
        await expect(page.getByRole('heading', {name: 'Mapeamento de competências - 2025'})).toBeVisible();
        await expect(page.getByText('Tipo: Mapeamento')).toBeVisible();
        await expect(page.getByText('Situação: Em andamento')).toBeVisible();
        await expect(page.getByRole('heading', {name: 'Unidades participantes'})).toBeVisible();
        await expect(page.getByRole('table')).toBeVisible();
        await expect(page.getByRole('columnheader', {name: 'Unidade', exact: true})).toBeVisible();
        await expect(page.getByRole('columnheader', {name: 'Situação', exact: true})).toBeVisible();
        await expect(page.getByRole('columnheader', {name: 'Data limite', exact: true})).toBeVisible();
        await expect(page.getByRole('columnheader', {name: 'Unidade Atual', exact: true})).toBeVisible();
    });

    test('deve exibir informações completas do processo', async ({page}) => {
        // Verificar badge de detalhes do processo
        await expect(page.getByText('Detalhes do processo')).toBeVisible();
        
        // Verificar título do processo
        await expect(page.getByTestId('processo-info')).toBeVisible();
        
        // Verificar informações básicas
        await expect(page.getByText('Tipo:')).toBeVisible();
        await expect(page.getByText('Situação:')).toBeVisible();
        
        // Verificar tabela de unidades participantes
        await expect(page.getByRole('heading', {name: 'Unidades participantes'})).toBeVisible();
        await expect(page.getByRole('table')).toBeVisible();
    });

    test('deve permitir navegação para detalhes de unidade', async ({page}) => {
        // Aguardar carregamento da tabela
        await page.waitForSelector('table tbody tr');
        
        // Aguardar sincronização completa (Lição 8)
        await page.waitForLoadState('networkidle');
        
        // Procurar especificamente por uma unidade OPERACIONAL que deveria ser clicável
        // Baseado nos dados mockados: SESEL, SEDESENV, SEMARE, SEDIA, STIC são OPERACIONAL/INTEROPERACIONAL
        const linhaSESEL = page.locator('table tbody tr').filter({ hasText: 'SESEL' });
        const linhaSTIC = page.locator('table tbody tr').filter({ hasText: 'STIC' });
        
        // Clicar na linha SESEL (OPERACIONAL)
        await expect(linhaSESEL).toBeVisible();
        await linhaSESEL.click();
        
        // Aguardar navegação completa
        await page.waitForLoadState('networkidle');
        
        // Verificar navegação para subprocesso
        await expect(page).toHaveURL(/\/processo\/1\/[A-Z]+/);
    });

    test.describe('Testes para perfil GESTOR', () => {
        test.beforeEach(async ({page}) => {
            await loginAsGestor(page);
            await page.goto(`/processo/1`);
            await page.waitForLoadState('networkidle'); // Added this line
        });

        test('deve exibir botões de ação em bloco para GESTOR', async ({page}) => {
            // Verificar se o botão "Aceitar em bloco" aparece quando há unidades elegíveis
            const aceitarBlocoButton = page.getByRole('button', {name: 'Aceitar em bloco'});
            
            // O botão pode ou não aparecer dependendo do estado dos dados
            // Vamos verificar se existe pelo menos a estrutura
            const botoesBloco = page.locator('.d-flex.gap-2');
            await expect(botoesBloco).toBeVisible();
            await expect(aceitarBlocoButton).toBeVisible();
        });

                test('deve abrir modal de aceitar em bloco', async ({page}) => {
                    const aceitarBlocoButton = page.getByRole('button', {name: 'Aceitar em bloco'});
                    
                    await expect(aceitarBlocoButton).toBeVisible();
                    await aceitarBlocoButton.click();
                    
                    // Verificar modal
                    await expect(page.getByText('Aceitar cadastros em bloco')).toBeVisible();
                    await expect(page.getByText('Selecione as unidades que terão seus cadastros aceitos:')).toBeVisible();
                    
                    // Verificar tabela no modal
                    await expect(page.getByRole('cell', {name: 'Selecionar'})).toBeVisible();
                    await expect(page.getByRole('cell', {name: 'Sigla'})).toBeVisible();
                    await expect(page.getByRole('cell', {name: 'Nome'})).toBeVisible();
                    await expect(page.getByRole('cell', {name: 'Situação Atual'})).toBeVisible();
                    
                    // Verificar botões do modal
                    await expect(page.getByRole('button', {name: 'Cancelar'})).toBeVisible();
                    await expect(page.getByRole('button', {name: 'Aceitar'}).and(page.locator('.btn-primary'))).toBeVisible();
                });
        
                test('deve fechar modal de ação em bloco', async ({page}) => {
                    const aceitarBlocoButton = page.getByRole('button', {name: 'Aceitar em bloco'});
                    
                    await expect(aceitarBlocoButton).toBeVisible();
                    await aceitarBlocoButton.click();
                    
                    // Fechar modal pelo botão X
                    const fecharButton = page.locator('.modal-header .btn-close');
                    await fecharButton.click();
                    
                    // Verificar que o modal foi fechado
                    await expect(page.getByText('Aceitar cadastros em bloco')).not.toBeVisible();
                });    });

    test('deve exibir botões de ação em bloco para ADMIN', async ({page}) => {
        // Verificar se o botão "Homologar em bloco" aparece quando há unidades elegíveis
        const homologarBlocoButton = page.getByRole('button', {name: 'Homologar em bloco'});
        
        // O botão pode ou não aparecer dependendo do estado dos dados
        const botoesBloco = page.locator('.d-flex.gap-2');
        await expect(botoesBloco).toBeVisible();
        await page.pause(); // Added for inspection
        await expect(homologarBlocoButton).toBeVisible();
    });

    test('deve exibir botão de finalizar processo para ADMIN', async ({page}) => {
        // Verificar se o botão "Finalizar processo" aparece para processos em andamento
        const finalizarButton = page.getByRole('button', {name: 'Finalizar processo'});
        
        // O botão deve aparecer apenas para processos em andamento
        await expect(finalizarButton).toBeVisible();
        await expect(finalizarButton).toHaveClass(/btn-danger/);
    });


        




    test('deve abrir modal de finalização de processo', async ({page}) => {
        // Este cenário exige todas as unidades operacionais/interoperacionais homologadas.
        // Usar processo 99 (mock) que atende a pré-condição para exibir o modal.
        await page.goto('/processo/99');
    
        const finalizarButton = page.getByRole('button', {name: 'Finalizar processo'});
        await expect(finalizarButton).toBeVisible();
    
        await finalizarButton.click();
    
        // Modal deve abrir imediatamente quando a pré-condição é satisfeita
        await expect(page.locator('.modal.show')).toBeVisible({ timeout: 3000 });
        await expect(page.getByText('Finalização de processo')).toBeVisible({ timeout: 3000 });
        await expect(page.getByText(/Confirma a finalização do processo/)).toBeVisible({ timeout: 3000 });
    
        await expect(page.getByTestId('btn-cancelar-finalizacao')).toBeVisible();
        await expect(page.getByTestId('btn-confirmar-finalizacao')).toBeVisible();
    });

    test('deve cancelar finalização de processo', async ({page}) => {
        // Este cenário exige todas as unidades operacionais/interoperacionais homologadas.
        // Usar processo 99 (mock) que atende a pré-condição para exibir o modal.
        await page.goto('/processo/99');

        const finalizarButton = page.getByRole('button', {name: 'Finalizar processo'});
        await expect(finalizarButton).toBeVisible();

        await finalizarButton.click();

        // Wait for modal - usar seletor mais específico para evitar conflitos com outros modais
        await expect(page.locator('.modal.show')).toBeVisible();

        // Clicar em cancelar - usa texto "Cancelar" não data-testid
        const cancelarButton = page.getByRole('button', {name: 'Cancelar'});
        await expect(cancelarButton).toBeVisible();
        await cancelarButton.click();

        // Verificar que o modal foi fechado
        await expect(page.getByText('Finalização de processo')).not.toBeVisible();
    });





    test('deve exibir estrutura hierárquica das unidades', async ({page}) => {
        // Wait for load
        await page.waitForLoadState('networkidle');
        await page.waitForSelector('table', { state: 'visible'});

        // Verificar se a tabela mostra unidades em estrutura hierárquica
        const tabela = page.getByRole('table');
        await expect(tabela).toBeVisible();
        
        // Verificar se há linhas na tabela
        const linhas = page.locator('table tbody tr');
        const countLinhas = await linhas.count();
        expect(countLinhas).toBeGreaterThan(0);
        
        // Verificar se as colunas estão presentes
        await expect(page.getByRole('columnheader', {name: 'Unidade', exact: true})).toBeVisible();
        await expect(page.getByRole('columnheader', {name: 'Situação', exact: true})).toBeVisible();
        await expect(page.getByRole('columnheader', {name: 'Data limite', exact: true})).toBeVisible();
        await expect(page.getByRole('columnheader', {name: 'Unidade Atual', exact: true})).toBeVisible();
    });

    test('deve navegar corretamente para diferentes processos', async ({page}) => {
        // Testar navegação para processo diferente
        await page.goto('/processo/2');
        await expect(page).toHaveURL(/\/processo\/2/);
        
        // Verificar que a página carrega
        await expect(page.getByRole('heading', { level: 2 })).toBeVisible();
    });
});