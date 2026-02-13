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
    autenticadoComoAdmin: void;
    autenticadoComoGestor: void;
    autenticadoComoGestorCoord21: void;
    autenticadoComoGestorCoord22: void;
    autenticadoComoChefeSecao111: void;
    autenticadoComoChefeSecao211: void;
    autenticadoComoChefeSecao212: void;
    autenticadoComoChefeSecao221: void;
    autenticadoComoChefeSecao121: void;
    autenticadoComoChefeAssessoria11: void;
    autenticadoComoChefeAssessoria12: void;
    autenticadoComoChefeAssessoria21: void;
    autenticadoComoChefeAssessoria22: void;
    autenticadoComoAdminComPerfil: void;
}>({
    autenticadoComoAdmin: async ({page}, use) => {
        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
        await use();
    },

    autenticadoComoGestor: async ({page}, use) => {
        await login(page, USUARIOS.GESTOR_COORD.titulo, USUARIOS.GESTOR_COORD.senha);
        await use();
    },

    autenticadoComoGestorCoord21: async ({page}, use) => {
        await login(page, USUARIOS.GESTOR_COORD_21.titulo, USUARIOS.GESTOR_COORD_21.senha);
        await use();
    },

    autenticadoComoGestorCoord22: async ({page}, use) => {
        await login(page, USUARIOS.GESTOR_COORD_22.titulo, USUARIOS.GESTOR_COORD_22.senha);
        await use();
    },

    autenticadoComoChefeSecao111: async ({page}, use) => {
        await login(page, USUARIOS.CHEFE_SECAO_111.titulo, USUARIOS.CHEFE_SECAO_111.senha);
        await use();
    },

    autenticadoComoChefeSecao211: async ({page}, use) => {
        await login(page, USUARIOS.CHEFE_SECAO_211.titulo, USUARIOS.CHEFE_SECAO_211.senha);
        await use();
    },

    autenticadoComoChefeSecao212: async ({page}, use) => {
        await login(page, USUARIOS.CHEFE_SECAO_212.titulo, USUARIOS.CHEFE_SECAO_212.senha);
        await use();
    },

    autenticadoComoChefeSecao221: async ({page}, use) => {
        await login(page, USUARIOS.CHEFE_SECAO_221.titulo, USUARIOS.CHEFE_SECAO_221.senha);
        await use();
    },

    autenticadoComoChefeSecao121: async ({page}, use) => {
        await login(page, USUARIOS.CHEFE_SECAO_121.titulo, USUARIOS.CHEFE_SECAO_121.senha);
        await use();
    },

    autenticadoComoChefeAssessoria11: async ({page}, use) => {
        await login(page, USUARIOS.CHEFE_ASSESSORIA_11.titulo, USUARIOS.CHEFE_ASSESSORIA_11.senha);
        await use();
    },

    autenticadoComoChefeAssessoria12: async ({page}, use) => {
        await login(page, USUARIOS.CHEFE_ASSESSORIA_12.titulo, USUARIOS.CHEFE_ASSESSORIA_12.senha);
        await use();
    },

    autenticadoComoChefeAssessoria21: async ({page}, use) => {
        await login(page, USUARIOS.CHEFE_UNIDADE.titulo, USUARIOS.CHEFE_UNIDADE.senha);
        await use();
    },

    autenticadoComoChefeAssessoria22: async ({page}, use) => {
        await login(page, USUARIOS.CHEFE_ASSESSORIA_22.titulo, USUARIOS.CHEFE_ASSESSORIA_22.senha);
        await use();
    },

    autenticadoComoAdminComPerfil: async ({page}, use) => {
        await loginComPerfil(
            page,
            USUARIOS.ADMIN_2_PERFIS.titulo,
            USUARIOS.ADMIN_2_PERFIS.senha,
            USUARIOS.ADMIN_2_PERFIS.perfil
        );
        await use();
    },
});

export {expect} from './base.js';
