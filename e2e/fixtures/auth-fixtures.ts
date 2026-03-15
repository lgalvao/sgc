import {test as base} from './base.js';
import {login, loginComPerfil, USUARIOS} from '../helpers/helpers-auth.js';

/**
 * Fixtures de autenticação para testes E2E.
 *
 * Elimina a duplicação de código de login em testes.
 *
 * @example
 * ```typescript
 * import {test, expect} from './fixtures/auth-fixtures.js';
 *
 * test('Deve criar processo', async ({page, autenticadoComoAdmin}) => {
 *   // Já está logado como ADMIN!
 *   await page.getByTestId('btn-painel-criar-processo').click();
 *   // ...
 * });
 * ```
 */
export const test = base.extend<{
    _autenticadoComoAdmin: void;
    _autenticadoComoGestor: void;
    _autenticadoComoGestorCoord21: void;
    _autenticadoComoGestorCoord22: void;
    _autenticadoComoChefeSecao111: void;
    _autenticadoComoChefeSecao112: void;
    _autenticadoComoChefeSecao211: void;
    _autenticadoComoChefeSecao212: void;
    _autenticadoComoChefeSecao221: void;
    _autenticadoComoChefeSecao121: void;
    _autenticadoComoChefeAssessoria11: void;
    _autenticadoComoChefeAssessoria12: void;
    _autenticadoComoChefeAssessoria21: void;
    _autenticadoComoChefeAssessoria22: void;
    _autenticadoComoGestorSecretaria2: void;
    _autenticadoComoGestorSecretaria1: void;
    _autenticadoComoAdminComPerfil: void;
    _autenticadoComoServidor: void;
}>({
    _autenticadoComoAdmin: async ({page}, use) => {
        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
        await use();
    },

    _autenticadoComoGestor: async ({page}, use) => {
        await login(page, USUARIOS.GESTOR_COORD.titulo, USUARIOS.GESTOR_COORD.senha);
        await use();
    },

    _autenticadoComoGestorCoord21: async ({page}, use) => {
        await login(page, USUARIOS.GESTOR_COORD_21.titulo, USUARIOS.GESTOR_COORD_21.senha);
        await use();
    },

    _autenticadoComoGestorCoord22: async ({page}, use) => {
        await login(page, USUARIOS.GESTOR_COORD_22.titulo, USUARIOS.GESTOR_COORD_22.senha);
        await use();
    },

    _autenticadoComoGestorSecretaria2: async ({page}, use) => {
        await loginComPerfil(page, USUARIOS.CHEFE_SECRETARIA_2.titulo, USUARIOS.CHEFE_SECRETARIA_2.senha, 'GESTOR - SECRETARIA_2');
        await use();
    },

    _autenticadoComoGestorSecretaria1: async ({page}, use) => {
        await loginComPerfil(page, USUARIOS.GESTOR_SECRETARIA_1.titulo, USUARIOS.GESTOR_SECRETARIA_1.senha, 'GESTOR - SECRETARIA_1');
        await use();
    },

    _autenticadoComoChefeSecao111: async ({page}, use) => {
        await login(page, USUARIOS.CHEFE_SECAO_111.titulo, USUARIOS.CHEFE_SECAO_111.senha);
        await use();
    },

    _autenticadoComoChefeSecao112: async ({page}, use) => {
        await login(page, USUARIOS.CHEFE_SECAO_112.titulo, USUARIOS.CHEFE_SECAO_112.senha);
        await use();
    },

    _autenticadoComoChefeSecao211: async ({page}, use) => {
        await login(page, USUARIOS.CHEFE_SECAO_211.titulo, USUARIOS.CHEFE_SECAO_211.senha);
        await use();
    },

    _autenticadoComoChefeSecao212: async ({page}, use) => {
        await login(page, USUARIOS.CHEFE_SECAO_212.titulo, USUARIOS.CHEFE_SECAO_212.senha);
        await use();
    },

    _autenticadoComoChefeSecao221: async ({page}, use) => {
        await login(page, USUARIOS.CHEFE_SECAO_221.titulo, USUARIOS.CHEFE_SECAO_221.senha);
        await use();
    },

    _autenticadoComoChefeSecao121: async ({page}, use) => {
        await login(page, USUARIOS.CHEFE_SECAO_121.titulo, USUARIOS.CHEFE_SECAO_121.senha);
        await use();
    },

    _autenticadoComoChefeAssessoria11: async ({page}, use) => {
        await login(page, USUARIOS.CHEFE_ASSESSORIA_11.titulo, USUARIOS.CHEFE_ASSESSORIA_11.senha);
        await use();
    },

    _autenticadoComoChefeAssessoria12: async ({page}, use) => {
        await login(page, USUARIOS.CHEFE_ASSESSORIA_12.titulo, USUARIOS.CHEFE_ASSESSORIA_12.senha);
        await use();
    },

    _autenticadoComoChefeAssessoria21: async ({page}, use) => {
        await login(page, USUARIOS.CHEFE_UNIDADE.titulo, USUARIOS.CHEFE_UNIDADE.senha);
        await use();
    },

    _autenticadoComoChefeAssessoria22: async ({page}, use) => {
        await login(page, USUARIOS.CHEFE_ASSESSORIA_22.titulo, USUARIOS.CHEFE_ASSESSORIA_22.senha);
        await use();
    },

    _autenticadoComoAdminComPerfil: async ({page}, use) => {
        await loginComPerfil(
            page,
            USUARIOS.ADMIN_2_PERFIS.titulo,
            USUARIOS.ADMIN_2_PERFIS.senha,
            USUARIOS.ADMIN_2_PERFIS.perfil
        );
        await use();
    },

    _autenticadoComoServidor: async ({page}, use) => {
        await login(page, USUARIOS.SERVIDOR.titulo, USUARIOS.SERVIDOR.senha);
        await use();
    },
});

export {expect} from './base.js';
