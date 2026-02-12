import {Page} from '@playwright/test';

/**
 * Credenciais de usuários para testes E2E
 * Baseado nos dados de e2e/setup/seed.sql
 */
export const USUARIOS = {
    ADMIN_2_PERFIS: {titulo: '111111', senha: 'senha', perfil: 'ADMIN - ADMIN'},
    ADMIN_1_PERFIL: {titulo: '191919', senha: 'senha'},
    GESTOR_COORD: {titulo: '222222', senha: 'senha'}, // GESTOR_COORD_11 (COORD_11)
    GESTOR_COORD_22: {titulo: '131313', senha: 'senha'}, // Mick Jagger (COORD_22)
    CHEFE_UNIDADE: {titulo: '777777', senha: 'senha'}, // Janis Joplin (Assessoria 21)
    CHEFE_ASSESSORIA_22: {titulo: '888888', senha: 'senha'}, // Jimi Hendrix (Assessoria 22)
    CHEFE_SECAO_211: {titulo: '101010', senha: 'senha'}, // Debbie Harry (Seção 211)
    CHEFE_SECAO_212: {titulo: '181818', senha: 'senha'}, // Pete Townshend (Seção 212)
    CHEFE_SECAO_221: {titulo: '141414', senha: 'senha'}, // Tina Turner (Seção 221)
    CHEFE_ASSESSORIA_11: {titulo: '555555', senha: 'senha'}, // David Bowie (Assessoria 11)
    CHEFE_ASSESSORIA_12: {titulo: '151515', senha: 'senha'}, // Axl Rose (Assessoria 12)
    CHEFE_SECAO_121: {titulo: '171717', senha: 'senha'}, // Lemmy Kilmister (Seção 121)
    CHEFE_SECAO_111: {titulo: '333333', senha: 'senha'}, // Chefe da Seção 111
    INVALIDO: {titulo: '999999999', senha: 'senhaerrada'}
} as const;

export async function autenticar(page: Page, usuario: string, senha: string) {
    await page.getByTestId('inp-login-usuario').fill(usuario);
    await page.getByTestId('inp-login-senha').fill(senha);
    await page.getByTestId('btn-login-entrar').click();
}

export async function login(page: Page, usuario: string, senha: string) {
    await page.goto('/login');
    await autenticar(page, usuario, senha);
    await page.waitForURL('/painel');
}

export async function loginComPerfil(page: Page, usuario: string, senha: string, perfilUnidade: string) {
    await page.goto('/login');
    await autenticar(page, usuario, senha);
    await page.getByTestId('sel-login-perfil').selectOption({label: perfilUnidade});
    await page.getByTestId('btn-login-entrar').click();
    await page.waitForURL('/painel');
}
