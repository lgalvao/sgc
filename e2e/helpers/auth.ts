import { expect, Page } from '@playwright/test';

/**
 * Credenciais de usuários para testes E2E
 * Baseado nos dados de e2e/setup/seed.sql
 */
export const USUARIOS = {
    ADMIN_2_PERFIS: { titulo: '111111', senha: 'senha', perfil: 'ADMIN - SEDOC' },
    ADMIN_1_PERFIL: { titulo: '191919', senha: 'senha' },
    GESTOR_COORD: { titulo: '222222', senha: 'senha' },
    INVALIDO: { titulo: '00000000000', senha: 'senhaerrada' }
} as const;

/**
 * Preenche os campos de login e clica no botão entrar
 */
export async function autenticar(page: Page, usuario: string, senha: string) {
    await page.getByTestId('input-titulo').fill(usuario);
    await page.getByTestId('input-senha').fill(senha);
    await page.getByTestId('botao-entrar').click();
}

/**
 * Realiza login com sucesso para usuário com perfil único
 * Verifica que o login foi bem-sucedido navegando para /painel
 */
export async function login(page: Page, usuario: string, senha: string) {
    await autenticar(page, usuario, senha);
    await expect(page).toHaveURL(/\/painel/);
}

/**
 * Realiza login para usuários com múltiplos perfis
 * Aguarda a seção de seleção de perfil, escolhe o perfil especificado
 * e verifica que o login foi bem-sucedido navegando para /painel
 */
export async function loginComPerfil(page: Page, usuario: string, senha: string, perfilUnidade: string) {
    await autenticar(page, usuario, senha);

    // Se há mais de um perfil deve aparecer a seção de escolha de perfil-unidade
    await expect(page.getByTestId('secao-perfil-unidade')).toBeVisible();

    const selectPerfil = page.getByTestId('select-perfil-unidade');
    await expect(selectPerfil).toBeVisible();

    await selectPerfil.selectOption({ label: perfilUnidade });

    // Aguardar que o botão esteja habilitado antes de clicar
    await expect(page.getByTestId('botao-entrar')).toBeEnabled();
    await page.getByTestId('botao-entrar').click();

    // Verifica que o login foi bem-sucedido
    await expect(page).toHaveURL(/\/painel/);
}
