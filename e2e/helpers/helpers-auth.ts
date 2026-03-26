import {expect, Page} from '@playwright/test';
import {fazerLogout, limparNotificacoes} from './helpers-navegacao.js';
import {TEXTOS} from '../../frontend/src/constants/textos.js';

export interface Usuario {
    titulo: string;
    senha: string;
    perfil?: string;
}

/**
 * Credenciais de usuários para testes E2E
 * Baseado nos dados de e2e/setup/seed.sql
 */
export const USUARIOS = {
    ADMIN_2_PERFIS: {titulo: '111111', senha: 'senha', perfil: 'ADMIN'},
    ADMIN_1_PERFIL: {titulo: '191919', senha: 'senha'},
    GESTOR_COORD: {titulo: '222222', senha: 'senha'}, // GESTOR_COORD_11 (COORD_11)
    GESTOR_COORD_21: {titulo: '999999', senha: 'senha'}, // Roger waters (COORD_21)
    GESTOR_COORD_22: {titulo: '131313', senha: 'senha'}, // Mick jagger (COORD_22)
    CHEFE_UNIDADE: {titulo: '777777', senha: 'senha'}, // Janis joplin (Assessoria 21)
    CHEFE_ASSESSORIA_22: {titulo: '888888', senha: 'senha'}, // Jimi hendrix (Assessoria 22)
    CHEFE_SECAO_211: {titulo: '101010', senha: 'senha'}, // Debbie harry (Seção 211)
    CHEFE_SECAO_212: {titulo: '181818', senha: 'senha'}, // Pete townshend (Seção 212)
    CHEFE_SECAO_221: {titulo: '141414', senha: 'senha'}, // Tina turner (Seção 221)
    CHEFE_ASSESSORIA_11: {titulo: '555555', senha: 'senha'}, // David bowie (Assessoria 11)
    CHEFE_ASSESSORIA_12: {titulo: '151515', senha: 'senha'}, // Axl rose (Assessoria 12)
    CHEFE_SECAO_121: {titulo: '171717', senha: 'senha'}, // Lemmy kilmister (Seção 121)
    CHEFE_SECAO_111: {titulo: '333333', senha: 'senha'}, // Chefe da Seção 111
    CHEFE_SECAO_112: {titulo: '444444', senha: 'senha'}, // Chefe da Seção 112
    GESTOR_COORD_12: {titulo: '222223', senha: 'senha'}, // Ringo starr (COORD_12)
    CHEFE_SECRETARIA_1: {titulo: '202020', senha: 'senha', perfil: 'CHEFE - SECRETARIA_1'}, // John lennon (SECRETARIA_1)
    GESTOR_SECRETARIA_1: {titulo: '202020', senha: 'senha', perfil: 'GESTOR - SECRETARIA_1'}, // John lennon (SECRETARIA_1)
    CHEFE_SECRETARIA_2: {titulo: '212121', senha: 'senha'}, // George harrison (Secretaria 2)
    SERVIDOR: {titulo: '121212', senha: 'senha'}, // Servidor (SECAO_113)
    SERVIDOR_SECAO_211: {titulo: '282828', senha: 'senha'}, // Eric clapton (Seção 211)
    SERVIDOR_SECAO_221: {titulo: '292929', senha: 'senha'}, // Flea (Seção 221)
    INVALIDO: {titulo: '999999999', senha: 'senhaerrada'}
} as const;


export async function verificarTelaLogin(page: Page) {
    await expect(page.getByTestId('txt-login-titulo')).toHaveText(TEXTOS.login.TITULO);
    await expect(page.getByTestId('txt-login-subtitulo')).toHaveText(TEXTOS.login.SUBTITULO);

    await expect(page.getByTestId('form-login')).toBeVisible();
    await expect(page.getByTestId('inp-login-usuario')).toBeVisible();
    await expect(page.getByTestId('inp-login-usuario')).toHaveAttribute('placeholder', TEXTOS.login.PLACEHOLDER_USUARIO);

    await expect(page.getByTestId('inp-login-senha')).toBeVisible();
    await expect(page.getByTestId('inp-login-senha')).toHaveAttribute('placeholder', TEXTOS.login.PLACEHOLDER_SENHA);

    await expect(page.getByTestId('btn-login-entrar')).toBeVisible();
    await expect(page.getByTestId('btn-login-entrar')).toContainText(TEXTOS.comum.BOTAO_ENTRAR);
}

export async function autenticar(page: Page, usuario: string, senha: string) {
    await page.getByTestId('inp-login-usuario').fill(usuario);
    await page.getByTestId('inp-login-senha').fill(senha);
    await page.getByTestId('btn-login-entrar').click();
}

export async function login(page: Page, usuario: string, senha: string) {
    await page.goto('/login');
    // Se ainda não estiver na página de login (redirecionado para /painel por sessão ativa), força logout
    if (page.url().includes('/painel')) {
        await page.getByTestId('btn-logout').click();
        await page.waitForURL(/\/login/);
    }

    await autenticar(page, usuario, senha);
    await page.waitForURL(/\/painel(?:\?|$)/);
    await limparNotificacoes(page);
}

export async function loginComPerfil(page: Page, usuario: string, senha: string, perfilUnidade: string) {
    await page.goto('/login');
    // Se ainda não estiver na página de login (redirecionado para /painel por sessão ativa), força logout
    if (page.url().includes('/painel')) {
        await page.getByTestId('btn-logout').click();
        await page.waitForURL(/\/login/);
    }

    await autenticar(page, usuario, senha);
    await page.getByTestId('sel-login-perfil').selectOption({label: perfilUnidade});
    await page.getByTestId('btn-login-entrar').click();
    await page.waitForURL(/\/painel(?:\?|$)/);
    await limparNotificacoes(page);
}

/**
 * Encapsula o ciclo de login, execução de uma ação e logout.
 */
export async function executarComo(page: Page, usuario: Usuario, acao: (page: Page) => Promise<void>) {
    if (usuario.perfil) {
        await loginComPerfil(page, usuario.titulo, usuario.senha, usuario.perfil);
    } else {
        await login(page, usuario.titulo, usuario.senha);
    }

    try {
        await acao(page);
    } finally {
        await fazerLogout(page);
    }
}
