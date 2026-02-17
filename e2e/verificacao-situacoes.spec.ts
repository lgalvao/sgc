import {test} from './fixtures/complete-fixtures.js';
import {criarProcesso, verificarDetalhesSubprocesso} from './helpers/helpers-processos.js';

test.describe('Verificação de Situações e Labels', () => {
    const UNIDADE_ALVO = 'SECAO_121';

    test('Deve exibir label "Não iniciado" corretamente no subprocesso', async ({page, autenticadoComoAdmin, cleanupAutomatico}) => {
        const timestamp = Date.now();
        const descricao = `Processo Verificacao Labels ${timestamp}`;

        // 1. ADMIN cria e inicia processo
        await criarProcesso(page, {
            descricao,
            tipo: 'MAPEAMENTO',
            diasLimite: 30,
            unidade: UNIDADE_ALVO,
            expandir: ['SECRETARIA_1', 'COORD_12'],
            iniciar: true
        });

        // 2. Navegar para o subprocesso
        await page.getByTestId('tbl-processos').getByText(descricao, {exact: true}).first().click();
        await page.getByRole('row', {name: /SECAO_121/i}).click();

        // 3. Verificar que a situação está em minúsculo (conforme refatoração)
        await verificarDetalhesSubprocesso(page, {
            sigla: 'SECAO_121',
            situacao: 'Não iniciado',
            prazo: '/'
        });

        // Capturar ID do processo para cleanup
        const processoId = Number.parseInt(page.url().match(/\/processo\/(\d+)/)?.[1] || '0');
        if (processoId > 0) cleanupAutomatico.registrar(processoId);
    });
});
