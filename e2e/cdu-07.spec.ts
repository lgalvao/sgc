import {expect, test} from './fixtures/complete-fixtures.js';
import {login, USUARIOS} from './helpers/helpers-auth.js';
import {criarProcesso, verificarDetalhesSubprocesso, verificarProcessoNaTabela} from './helpers/helpers-processos.js';

test.describe('CDU-07 - Detalhar subprocesso', () => {
    const UNIDADE_ALVO = 'SECAO_121';
    const CHEFE_UNIDADE = USUARIOS.CHEFE_SECAO_121.titulo;
    const SENHA_CHEFE = USUARIOS.CHEFE_SECAO_121.senha;

    test('Deve exibir detalhes do subprocesso para CHEFE', async ({page, autenticadoComoAdmin, cleanupAutomatico}) => {
        const timestamp = Date.now();
        const descricao = `Processo CDU-07 ${timestamp}`;

        // 1. ADMIN cria e inicia processo
        

        await criarProcesso(page, {
            descricao,
            tipo: 'MAPEAMENTO',
            diasLimite: 30,
            unidade: UNIDADE_ALVO,
            expandir: ['SECRETARIA_1', 'COORD_12'],
            iniciar: true
        });

        // Esperar confirmação na tabela antes de sair
        await verificarProcessoNaTabela(page, {
            descricao,
            situacao: 'Em andamento',
            tipo: 'Mapeamento'
        });
        await page.getByTestId('btn-logout').click();

        // 2. CHEFE loga e acessa subprocesso
        await login(page, CHEFE_UNIDADE, SENHA_CHEFE);

        // Esperar tabela estabilizar
        await page.waitForLoadState('networkidle');

        // Navegar para o subprocesso clicando no processo da lista
        await page.getByText(descricao, {exact: true}).click();

        // Verificar URL (deve conter ID e Sigla)
        await expect(page).toHaveURL(/\/processo\/\d+\/SECAO_121$/);

        // Capturar ID do processo para cleanup
        const processoId = Number.parseInt(page.url().match(/\/processo\/(\d+)/)?.[1] || '0');
        if (processoId > 0) cleanupAutomatico.registrar(processoId);

        // 3. Verificar seções da tela

        // Seção Dados da Unidade
        await verificarDetalhesSubprocesso(page, {
            sigla: 'SECAO_121',
            situacao: 'Não Iniciado', // Ajustado conforme o UI
            prazo: '/'
        });

        // Verificar Cards (Elementos do Processo)
        await expect(page.getByTestId('card-subprocesso-atividades')).toBeVisible();

        // Seção Movimentações
        await expect(page.getByRole('heading', {name: 'Movimentações'})).toBeVisible();
        await expect(page.locator('table tbody tr')).not.toHaveCount(0);
    });
});