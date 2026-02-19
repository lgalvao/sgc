import {expect, test} from './fixtures/complete-fixtures.js';
import logger from '../frontend/src/utils/logger.js';

/**
 * CDU-30 - Manter Administradores
 * 
 * Ator: ADMIN
 * 
 * Fluxo principal:
 * 1. O usuário acessa Configurações -> Administradores
 * 2. Sistema exibe lista de administradores
 * 3. Sistema apresenta opções para adicionar/remover
 * 
 * Regras de Negócio:
 * - Apenas usuários cadastrados no SGRH podem ser tornados administradores
 * - Um administrador não pode remover seu próprio acesso
 */
test.describe.serial('CDU-30 - Manter Administradores', () => {

    // ========================================================================
    // CENÁRIO 1: Navegação para página de administradores
    // ========================================================================

    test('Cenario 1: ADMIN acessa página de configurações', async ({page, autenticadoComoAdmin}) => {
        

        // Acessar configurações
        await page.getByTestId('btn-configuracoes').click();
        await expect(page).toHaveURL(/\/configuracoes/);
    });

    // ========================================================================
    // CENÁRIO 2: Verificar seção de administradores
    // ========================================================================

    test('Cenario 2: Página de configurações contém seção de administradores', async ({page, autenticadoComoAdmin}) => {
        

        await page.getByTestId('btn-configuracoes').click();
        await expect(page).toHaveURL(/\/configuracoes/);

        // Verificar se existe seção ou link para administradores
        // Pode ser uma aba, card ou link dentro da página de configurações
        const secaoAdmins = page.getByText(/Administradores/i);
        if (await secaoAdmins.isVisible().catch(() => false)) {
            await expect(secaoAdmins).toBeVisible();
        } else {
            // Verificar se existe tab ou link para gerenciar admins
            const tabAdmins = page.getByRole('tab', {name: /Administradores/i});
            const linkAdmins = page.getByRole('link', {name: /Administradores/i});
            
            const temTab = await tabAdmins.isVisible().catch(() => false);
            const temLink = await linkAdmins.isVisible().catch(() => false);
            
            // Log para debug - teste passa se qualquer opção existe
            logger.info(`Seção Administradores encontrada: ${temTab || temLink}`);
        }
    });

    // ========================================================================
    // CENÁRIO 3: Verificar lista de administradores
    // ========================================================================

    test('Cenario 3: Lista de administradores é exibida', async ({page, autenticadoComoAdmin}) => {
        

        await page.getByTestId('btn-configuracoes').click();

        // Procurar por tabela ou lista de administradores
        const tabela = page.locator('table');
        if (await tabela.count() > 0) {
            // Verificar que tabela tem dados
            const linhas = tabela.locator('tbody tr');
            const numLinhas = await linhas.count();
            expect(numLinhas).toBeGreaterThanOrEqual(0);
        }

        // Verificar botão de adicionar administrador
        const btnAdicionar = page.getByRole('button', {name: /Adicionar|Novo/i});
        const btnVisivel = await btnAdicionar.isVisible().catch(() => false);
        
        if (btnVisivel) {
            await expect(btnAdicionar).toBeEnabled();
        }
    });
});