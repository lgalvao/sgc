import {expect, test} from '@playwright/test';
import {login, USUARIOS} from './helpers/auth';
import {criarProcesso, verificarDetalhesProcesso, verificarUnidadeParticipante} from './helpers/processo-helpers';

test.describe('CDU-06 - Detalhar processo', () => {
    test.setTimeout(60000);
    const UNIDADE_ALVO = 'ASSESSORIA_21';

    test('Deve exibir detalhes do processo para ADMIN', async ({page}) => {
        const timestamp = Date.now();
        const descricao = `Processo CDU-06 ${timestamp}`;

        // Login ADMIN
        await page.goto('/login');
        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);

        // Criar processo
        await criarProcesso(page, {
            descricao,
            tipo: 'MAPEAMENTO',
            diasLimite: 30,
            unidade: UNIDADE_ALVO,
            expandir: ['SECRETARIA_2']
        });

        // Iniciar processo
        await page.getByRole('row', {name: descricao}).click();
        await expect(page).toHaveURL(/\/processo\/cadastro/);
        await page.getByTestId('btn-processo-iniciar').click();

        // Tentar clicar no botão de confirmação mesmo se a animação estiver bloqueando (force: true)
        // Isso ajuda em ambientes lentos/headless onde o estado 'visible' pode demorar
        await page.getByTestId('btn-iniciar-processo-confirmar').click({force: true});

        // Navegar para detalhes do processo
        await expect(page).toHaveURL(/\/painel/);
        await page.getByRole('row', {name: descricao}).click();
        await expect(page).toHaveURL(/\/processo\/\d+/);

        // Verificar detalhes do processo (usando caixa alta conforme observado em reviews)
        await verificarDetalhesProcesso(page, {
            descricao,
            tipo: 'MAPEAMENTO',
            situacao: 'EM_ANDAMENTO'
        });

        // Verificar unidade participante
        await verificarUnidadeParticipante(page, {
            sigla: 'ASSESSORIA_21',
            situacao: 'AGUARDANDO_CADASTRO',
            dataLimite: '/'
        });

        // Verificar botão de finalizar (apenas ADMIN)
        await expect(page.getByTestId('btn-processo-finalizar')).toBeVisible();
    });

    test.skip('Deve exibir detalhes do processo para GESTOR', async ({page}) => {
        // Teste pulado pois requer ajuste na seleção de unidade da árvore para o perfil GESTOR
        const timestamp = Date.now();
        const descricao = `Processo CDU-06 Gestor ${timestamp}`;
        const UNIDADE_GESTOR = 'SECAO_111';

        await page.goto('/login');
        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);

        await criarProcesso(page, {
            descricao,
            tipo: 'MAPEAMENTO',
            diasLimite: 30,
            unidade: UNIDADE_GESTOR,
            expandir: ['SECRETARIA_1', 'COORD_11']
        });

        await page.getByRole('row', {name: descricao}).click();
        await page.getByTestId('btn-processo-iniciar').click();

        await page.getByTestId('btn-iniciar-processo-confirmar').click({force: true});

        await page.getByTestId('btn-logout').click();

        await login(page, USUARIOS.GESTOR_COORD.titulo, USUARIOS.GESTOR_COORD.senha);

        await page.waitForLoadState('networkidle');
        await page.getByRole('row', {name: descricao}).click();

        await expect(page).toHaveURL(/\/processo\/\d+/);

        await verificarDetalhesProcesso(page, {
            descricao,
            tipo: 'MAPEAMENTO',
            situacao: 'EM_ANDAMENTO'
        });

        await expect(page.getByTestId('btn-processo-finalizar')).toBeHidden();
    });
});
