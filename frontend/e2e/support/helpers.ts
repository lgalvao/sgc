import { APIRequestContext } from '@playwright/test';

/**
 * Contém dados de usuários de teste consistentes com `backend/src/main/resources/data.sql`.
 * A senha é, por convenção do SgrhServiceMock, o mesmo valor do título eleitoral.
 */
export const testUsers = {
    admin: {
        tituloEleitoral: '7', // Zeca Silva, que é CHEFE, mas vamos assumir que pode ter perfil ADMIN para o teste
        senha: '7',
        perfis: [
            { label: 'ADMIN - SEDOC', value: 'ADMIN-1' },
            { label: 'CHEFE - SEDOC', value: 'CHEFE-1' }
        ]
    },
    chefe: {
        tituloEleitoral: '2', // Carlos Henrique Lima
        senha: '2',
    },
    gestor: {
        tituloEleitoral: '8', // Paulo Horta
        senha: '8'
    },
    servidor: {
        tituloEleitoral: '1', // Ana Paula Souza
        senha: '1'
    }
};

/**
 * Garante que um usuário de teste padrão exista no banco de dados do backend.
 *
 * @param request O contexto de requisição da API do Playwright.
 */
export async function ensureTestUser(request: APIRequestContext) {
    // NOTA: Esta função é um placeholder. No futuro, ela poderia chamar um endpoint
    // para garantir que os dados de teste existam no banco de dados.
    // Por enquanto, apenas retorna os dados definidos localmente.
    return testUsers.admin;
}