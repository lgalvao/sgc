import { test } from '@playwright/test';
import { ROTAS } from './funcoesGerais/constantes.js';
import { verifPerfil,acessarURL, login, efetuarLogout, verificarTelaLogin, obterUsuariosPorPerfil } from "./funcoesGerais/auxil.js";

test.describe('Homologação - Testes de Acesso por Perfil', () => {
    test.beforeEach(async ({ page }) => {
        await acessarURL(page, ROTAS.LOGIN);
    });

    test.describe('Perfis ADMIN', () => {
        verifPerfil('ADMIN');
    });

    test.describe('Perfis CHEFE', () => {
        verifPerfil('CHEFE - SELOG');
    });
    test.describe('Perfis SERVIDOR', () => {
        verifPerfil('SERVIDOR - SEDOC');
    });
});
