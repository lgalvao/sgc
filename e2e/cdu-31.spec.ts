import {expect, test} from './fixtures/auth-fixtures';
import {login, USUARIOS} from './helpers/helpers-auth';
import {resetDatabase} from './hooks/hooks-limpeza';

/**
 * CDU-31 - Configurar sistema
 * 
 * Ator: ADMIN
 * 
 * Fluxo principal:
 * 1. ADMIN clica no botão de configurações (engrenagem) na navbar
 * 2. Sistema mostra tela Configurações com valores atuais
 * 3. ADMIN altera valores e clica em Salvar
 * 4. Sistema mostra mensagem de confirmação
 */
test.describe.serial('CDU-31 - Configurar sistema', () => {
    const USUARIO_ADMIN = USUARIOS.ADMIN_1_PERFIL.titulo;
    const SENHA_ADMIN = USUARIOS.ADMIN_1_PERFIL.senha;

    test.beforeAll(async ({request}) => {
        await resetDatabase(request);
    });

    // ========================================================================
    // CENÁRIO 1: Navegação para configurações
    // ========================================================================

    test('Cenario 1: ADMIN navega para configurações', async ({page, autenticadoComoAdmin}) => {
        // CDU-31: Passos 1-2
        

        // Passo 1: Clicar no botão de engrenagem
        await page.getByTestId('btn-configuracoes').click();

        // Passo 2: Sistema mostra tela de configurações
        await expect(page).toHaveURL(/\/configuracoes/);
        await expect(page.getByRole('heading', {name: /Configurações/i})).toBeVisible();
    });

    // ========================================================================
    // CENÁRIO 2: Visualizar configurações atuais
    // ========================================================================

    test('Cenario 2: Tela exibe configurações editáveis', async ({page, autenticadoComoAdmin}) => {
        

        await page.getByTestId('btn-configuracoes').click();
        await expect(page).toHaveURL(/\/configuracoes/);

        // Verificar que existem campos editáveis para configurações
        // CDU-31 menciona: DIAS_INATIVACAO_PROCESSO e DIAS_ALERTA_NOVO
        const formInputs = page.locator('input[type="number"], input[type="text"]');
        await expect(formInputs.first()).toBeVisible();

        // Verificar botão Salvar
        await expect(page.getByRole('button', {name: /Salvar/i})).toBeVisible();
    });

    // ========================================================================
    // CENÁRIO 3: Salvar configurações
    // ========================================================================

    test('Cenario 3: ADMIN salva configurações com sucesso', async ({page, autenticadoComoAdmin}) => {
        // CDU-31: Passos 3-4
        

        await page.getByTestId('btn-configuracoes').click();
        await expect(page).toHaveURL(/\/configuracoes/);

        // Passo 3: Alterar valor de uma configuração
        const primeiroInput = page.locator('input[type="number"]').first();
        if (await primeiroInput.count() > 0) {
            await primeiroInput.clear();
            await primeiroInput.fill('30');
        }

        // Clicar em Salvar
        await page.getByRole('button', {name: /Salvar/i}).click();

        // Passo 4: Verificar mensagem de confirmação
        await expect(page.getByRole('heading', { name: /sucesso/i })).toBeVisible({timeout: 5000});
    });
});
