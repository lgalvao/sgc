import {Page} from '@playwright/test';

/**
 * Credenciais de usuários para testes E2E
 * Baseado nos dados de e2e/setup/seed.sql
 */
export const USUARIOS = {
    ADMIN_2_PERFIS: { titulo: '111111', senha: 'senha', perfil: 'ADMIN - SEDOC' },
    ADMIN_1_PERFIL: { titulo: '191919', senha: 'senha' },
    GESTOR_COORD: { titulo: '222222', senha: 'senha' },
    CHEFE_UNIDADE: { titulo: '777777', senha: 'senha' }, // Janis Joplin (Assessoria 21)
    CHEFE_ASSESSORIA_22: { titulo: '888888', senha: 'senha' }, // Jimi Hendrix (Assessoria 22)
    INVALIDO: { titulo: '00000000000', senha: 'senhaerrada' }
} as const;

/**
 * Preenche os campos de login e clica no botão entrar
 */
export async function autenticar(page: Page, usuario: string, senha: string) {
    await page.getByTestId('inp-login-usuario').fill(usuario);
    await page.getByTestId('inp-login-senha').fill(senha);
    await page.getByTestId('btn-login-entrar').click();
}

/**
 * Realiza login com sucesso para usuário com perfil único
 * Verifica que o login foi bem-sucedido navegando para /painel
 */
export async function login(page: Page, usuario: string, senha: string) {
    await autenticar(page, usuario, senha);
    // Aguarda redirecionamento para o painel para garantir que o login foi concluído
    await page.waitForURL('/painel');
}

/**
 * Realiza login para usuários com múltiplos perfis
 * Aguarda a seção de seleção de perfil, escolhe o perfil especificado
 * e verifica que o login foi bem-sucedido navegando para /painel
 */
export async function loginComPerfil(page: Page, usuario: string, senha: string, perfilUnidade: string) {
    await autenticar(page, usuario, senha);
    await page.getByTestId('sel-login-perfil').selectOption({ label: perfilUnidade });
    await page.getByTestId('btn-login-entrar').click();
    // Aguarda redirecionamento para o painel para garantir que o login foi concluído
    await page.waitForURL('/painel');
}
