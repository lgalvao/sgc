import {expect, test} from './fixtures/complete-fixtures.js';
import {
    criarProcessoMapaHomologadoFixture,
    criarProcessoRevisaoMapaHomologadoFixture
} from './fixtures/fixtures-processos.js';
import {acessarSubprocessoAdmin} from './helpers/helpers-analise.js';
import {acessarDetalhesProcesso} from './helpers/helpers-processos.js';
import {verificarAppAlert, verificarPaginaPainel} from './helpers/helpers-navegacao.js';
import {login, USUARIOS} from './helpers/helpers-auth.js';

/**
 * CDU-33 - Reabrir revisão de cadastro
 *
 * Ator: ADMIN
 *
 * Pré-condições:
 * - Processo de mapeamento anterior finalizado para gerar mapa vigente
 * - Subprocesso de revisão com mapa homologado
 */
test.describe.serial('CDU-33 - Reabrir revisão de cadastro', () => {
    const UNIDADE_ALVO = 'SECAO_212';
    const timestamp = Date.now();
    const descMapeamento = `Mapeamento pre-CDU-33 ${timestamp}`;
    const descRevisao = `Revisão CDU-33 ${timestamp}`;

    test('Setup UI', async ({_resetAutomatico, page, request}) => {

        // PREPARAÇÃO 0 - CRIAR MAPA VIGENTE
        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
        await criarProcessoMapaHomologadoFixture(request, {
            descricao: descMapeamento,
            diasLimite: 30,
            unidade: UNIDADE_ALVO
        });

        await page.goto('/painel');
        await acessarDetalhesProcesso(page, descMapeamento);
        await page.getByTestId('btn-processo-finalizar').click();
        await page.getByTestId('btn-finalizar-processo-confirmar').click();
        await verificarPaginaPainel(page);

        // Preparacao 1: Admin cria revisão já homologada
        const processo = await criarProcessoRevisaoMapaHomologadoFixture(request, {
            descricao: descRevisao,
            diasLimite: 30,
            unidade: UNIDADE_ALVO
        });
        expect(processo.codigo).toBeGreaterThan(0);

        await page.goto('/painel');
        await acessarSubprocessoAdmin(page, descRevisao, UNIDADE_ALVO);
        await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Mapa homologado/i);
    });

    test('Cenários CDU-33: ADMIN reabre revisão de cadastro', async ({_resetAutomatico, page, _autenticadoComoAdmin}) => {
        const textoJustificativa = 'Ajuste necessário';

        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
        await page.goto('/painel');
        await acessarSubprocessoAdmin(page, descRevisao, UNIDADE_ALVO);
        await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Mapa homologado/i);

        const btnReabrir = page.getByTestId('btn-reabrir-revisao');
        await expect(btnReabrir).toBeVisible();
        await expect(btnReabrir).toBeEnabled();

        // Cenario 3: Abrir modal
        await btnReabrir.click();

        const modal = page.getByRole('dialog');
        await expect(modal).toBeVisible();
        await expect(modal.getByRole('heading', {name: /Reabrir revisão/i})).toBeVisible();
        await expect(page.getByTestId('inp-justificativa-reabrir')).toBeVisible();
        await expect(modal.getByRole('button', {name: /Cancelar/i})).toBeVisible();
        await expect(page.getByTestId('btn-confirmar-reabrir')).toHaveText(/Reabrir/i);

        await expect(page.getByTestId('btn-confirmar-reabrir')).toBeDisabled();
        await modal.getByRole('button', {name: /Cancelar/i}).click();
        await expect(modal).toBeHidden();
        await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Mapa homologado/i);

        await btnReabrir.click();
        await expect(page.getByTestId('btn-confirmar-reabrir')).toBeDisabled();

        // Cenario 4: Confirmar reabertura
        await page.getByTestId('inp-justificativa-reabrir').fill(textoJustificativa);
        await expect(page.getByTestId('btn-confirmar-reabrir')).toBeEnabled();
        await page.getByTestId('btn-confirmar-reabrir').click();

        await verificarAppAlert(page, /Revisão reaberta/i);
        await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Revisão em andamento/i);
        await expect(page.getByTestId('tbl-movimentacoes')).toContainText(/Reabertura de revisão de cadastro/i);
    });
});
