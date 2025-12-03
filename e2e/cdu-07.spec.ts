import {expect, test} from '@playwright/test';
import {login, USUARIOS} from './helpers/auth';
import {criarProcesso, verificarDetalhesSubprocesso, verificarProcessoNaTabela} from './helpers/processo-helpers';

test.describe('CDU-07 - Detalhar subprocesso', () => {
    test.setTimeout(60000);
    const UNIDADE_ALVO = 'ASSESSORIA_21';
    const CHEFE_UNIDADE = USUARIOS.CHEFE_UNIDADE.titulo; // 777777
    const SENHA_CHEFE = USUARIOS.CHEFE_UNIDADE.senha;

    test('Deve exibir detalhes do subprocesso para CHEFE', async ({page}) => {
        const timestamp = Date.now();
        const descricao = `Processo CDU-07 ${timestamp}`;

        // ========================================================================
        // 1. ADMIN cria e inicia processo
        // ========================================================================
        await page.goto('/login');
        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);

        await criarProcesso(page, {
            descricao,
            tipo: 'MAPEAMENTO',
            diasLimite: 30,
            unidade: UNIDADE_ALVO,
            expandir: ['SECRETARIA_2']
        });

        await page.getByRole('row', {name: descricao}).click();
        await page.getByTestId('btn-processo-iniciar').click();

        // Confirmar início
        await page.getByTestId('btn-iniciar-processo-confirmar').click({force: true});

        // Esperar confirmação na tabela antes de sair
        await verificarProcessoNaTabela(page, {
            descricao,
            situacao: 'Em andamento', // Na tabela pode ser Mixed Case ou Upper. cdu-05 checa 'Em andamento'
            tipo: 'Mapeamento'
        });

        await page.getByTestId('btn-logout').click();

        // ========================================================================
        // 2. CHEFE loga e acessa subprocesso
        // ========================================================================
        await login(page, CHEFE_UNIDADE, SENHA_CHEFE);

        // Esperar tabela estabilizar
        await page.waitForLoadState('networkidle');
        await page.waitForTimeout(1000);

        // Navegar para o subprocesso clicando no processo da lista
        await page.getByText(descricao, {exact: true}).click();

        // Verificar URL (deve conter ID e Sigla)
        await expect(page).toHaveURL(/\/processo\/\d+\/ASSESSORIA_21$/);

        // ========================================================================
        // 3. Verificar seções da tela
        // ========================================================================

        // Seção Dados da Unidade
        await verificarDetalhesSubprocesso(page, {
            sigla: 'ASSESSORIA_21',
            situacao: 'AGUARDANDO_CADASTRO', // Detalhes costumam usar UPPER
            prazo: '/'
        });

        // Verificar Cards (Elementos do Processo)
        await expect(page.getByTestId('card-subprocesso-atividades')).toBeVisible();

        // Seção Movimentações
        await expect(page.getByRole('heading', {name: 'Movimentações'})).toBeVisible();
        await expect(page.locator('table tbody tr')).not.toHaveCount(0);
    });
});
