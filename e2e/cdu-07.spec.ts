import {expect, test} from './fixtures/complete-fixtures.js';
import {login, USUARIOS} from './helpers/helpers-auth.js';
import {criarProcesso, verificarDetalhesSubprocesso, verificarProcessoNaTabela} from './helpers/helpers-processos.js';
import {navegarParaSubprocesso} from './helpers/helpers-navegacao.js';

test.describe('CDU-07 - Detalhar subprocesso', () => {
    const UNIDADE_ALVO = 'SECAO_211';
    const CHEFE_UNIDADE = USUARIOS.CHEFE_SECAO_211.titulo;
    const SENHA_CHEFE = USUARIOS.CHEFE_SECAO_211.senha;
    const GESTOR_UNIDADE = USUARIOS.GESTOR_COORD_21.titulo;
    const SENHA_GESTOR = USUARIOS.GESTOR_COORD_21.senha;

    test('Deve exibir detalhes do subprocesso para ADMIN, GESTOR e CHEFE', async ({page, autenticadoComoAdmin, cleanupAutomatico}) => {
        const timestamp = Date.now();
        const descricao = `Processo CDU-07 ${timestamp}`;

        await criarProcesso(page, {
            descricao,
            tipo: 'MAPEAMENTO',
            diasLimite: 30,
            unidade: UNIDADE_ALVO,
            expandir: ['SECRETARIA_2', 'COORD_21'],
            iniciar: true
        });

        await verificarProcessoNaTabela(page, {
            descricao,
            situacao: 'Em andamento',
            tipo: 'Mapeamento'
        });
        await page.getByText(descricao, {exact: true}).click();
        const processoId = Number.parseInt(page.url().match(/\/processo\/(\d+)/)?.[1] || '0');
        if (processoId > 0) cleanupAutomatico.registrar(processoId);
        await navegarParaSubprocesso(page, UNIDADE_ALVO);

        await verificarDetalhesSubprocesso(page, {
            sigla: UNIDADE_ALVO,
            situacao: 'Não iniciado',
            prazo: '/'
        });
        await expect(page.locator('[data-testid="card-subprocesso-atividades"], [data-testid="card-subprocesso-atividades-vis"]').first()).toBeVisible();
        await expect(page.getByRole('heading', {name: 'Movimentações'})).toBeVisible();
        await expect(page.locator('table tbody tr')).not.toHaveCount(0);

        await page.getByTestId('btn-logout').click();
        await login(page, GESTOR_UNIDADE, SENHA_GESTOR);
        await page.getByText(descricao, {exact: true}).click();
        await expect(page).toHaveURL(/\/processo\/\d+$/);
        await expect(page.getByRole('row').filter({has: page.getByRole('cell', {name: UNIDADE_ALVO})})).toBeVisible();

        await page.getByTestId('btn-logout').click();
        await login(page, CHEFE_UNIDADE, SENHA_CHEFE);
        await page.getByText(descricao, {exact: true}).click();
        await expect(page).toHaveURL(new RegExp(`/processo/\\d+/${UNIDADE_ALVO}$`));
        await verificarDetalhesSubprocesso(page, {
            sigla: UNIDADE_ALVO,
            situacao: 'Não iniciado',
            prazo: '/'
        });
    });
});
