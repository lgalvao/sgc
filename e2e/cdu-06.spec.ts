import {expect, test} from './fixtures/base';
import {login, USUARIOS} from './helpers/helpers-auth';
import {criarProcesso, verificarDetalhesProcesso, verificarUnidadeParticipante} from './helpers/helpers-processos';
import {resetDatabase, useProcessoCleanup} from './hooks/hooks-limpeza';

test.describe('CDU-06 - Detalhar processo', () => {
    const UNIDADE_ALVO = 'ASSESSORIA_12';
    let cleanup: ReturnType<typeof useProcessoCleanup>;

    test.beforeAll(async ({request}) => await resetDatabase(request));

    test.beforeEach(() => cleanup = useProcessoCleanup());

    test.afterEach(async ({request}) => await cleanup.limpar(request));

    test('Deve exibir detalhes do processo para ADMIN', async ({page}) => {
        const timestamp = Date.now();
        const descricao = `Processo CDU-06 ${timestamp}`;

        // Login ADMIN
        await page.goto('/login');
        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);

        // Criar e iniciar processo
        await criarProcesso(page, {
            descricao,
            tipo: 'MAPEAMENTO',
            diasLimite: 30,
            unidade: UNIDADE_ALVO,
            expandir: ['SECRETARIA_1'],
            iniciar: true
        });

        // Navegar para detalhes do processo
        await page.getByRole('row', {name: descricao}).click();
        await expect(page).toHaveURL(/\/processo\/\d+/);

        // Capturar ID do processo para cleanup
        const processoId = parseInt(page.url().match(/\/processo\/(\d+)/)?.[1] || '0');
        if (processoId > 0) cleanup.registrar(processoId);

        // Verificar detalhes do processo (usando caixa alta conforme observado em reviews)
        await verificarDetalhesProcesso(page, {
            descricao,
            tipo: 'Mapeamento',
            situacao: 'Em andamento'
        });

        // Verificar unidade participante
        await verificarUnidadeParticipante(page, {
            sigla: 'ASSESSORIA_12',
            situacao: 'NAO_INICIADO',
            dataLimite: '/'
        });

        // Verificar botão de finalizar (apenas ADMIN)
        await expect(page.getByTestId('btn-processo-finalizar')).toBeVisible();
    });

    test('Deve exibir detalhes do processo para GESTOR', async ({page}) => {
        const timestamp = Date.now();
        const descricao = `Processo CDU-06 Gestor ${timestamp}`;
        const UNIDADE_PROCESSO = 'SECAO_111';

        await page.goto('/login');
        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);

        await criarProcesso(page, {
            descricao,
            tipo: 'MAPEAMENTO',
            diasLimite: 30,
            unidade: UNIDADE_PROCESSO,
            expandir: ['SECRETARIA_1', 'COORD_11'],
            iniciar: true
        });

        await page.getByTestId('btn-logout').click();

        await login(page, USUARIOS.GESTOR_COORD.titulo, USUARIOS.GESTOR_COORD.senha);

        // Aguardar que o processo apareça no painel
        await expect(page.getByRole('row', {name: descricao})).toBeVisible();
        await page.getByRole('row', {name: descricao}).click();

        await expect(page).toHaveURL(/\/processo\/\d+/);

        // Capturar ID do processo para cleanup
        const processoId = parseInt(page.url().match(/\/processo\/(\d+)/)?.[1] || '0');
        if (processoId > 0) cleanup.registrar(processoId);

        await verificarDetalhesProcesso(page, {
            descricao,
            tipo: 'Mapeamento',
            situacao: 'Em andamento'
        });

        await expect(page.getByTestId('btn-processo-finalizar')).toBeHidden();
    });
});
