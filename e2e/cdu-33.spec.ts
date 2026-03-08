import {expect, test} from './fixtures/complete-fixtures.js';
import {
    criarProcessoMapaHomologadoFixture,
    criarProcessoRevisaoMapaHomologadoFixture
} from './fixtures/fixtures-processos.js';
import {verificarPaginaPainel} from './helpers/helpers-navegacao.js';
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
    const descMapeamento = `Mapeamento Pre-CDU-33 ${timestamp}`;
    const descRevisao = `Revisão CDU-33 ${timestamp}`;
    let mappingPid = 0;
    let revisaoPid = 0;

    // PREPARAÇÃO 0 - CRIAR MAPA VIGENTE

    test('Preparacao 0: Criar e finalizar Mapeamento', async ({page, request, autenticadoComoAdmin}) => {
        const processo = await criarProcessoMapaHomologadoFixture(request, {
            descricao: descMapeamento,
            diasLimite: 30,
            unidade: UNIDADE_ALVO
        });
        mappingPid = processo.codigo;

        await page.goto('/painel');
        await expect(page.getByTestId('tbl-processos').getByText(descMapeamento).first()).toBeVisible();
        await page.getByTestId('tbl-processos').getByText(descMapeamento).first().click();
        await page.getByTestId('btn-processo-finalizar').click();
        await page.getByTestId('btn-finalizar-processo-confirmar').click();
        await verificarPaginaPainel(page);
    });


    test('Preparacao 1: Admin cria revisão já homologada', async ({page, request, autenticadoComoAdmin}) => {
        const processo = await criarProcessoRevisaoMapaHomologadoFixture(request, {
            descricao: descRevisao,
            diasLimite: 30,
            unidade: UNIDADE_ALVO
        });
        revisaoPid = processo.codigo;

        await page.goto(`/processo/${revisaoPid}/${UNIDADE_ALVO}`);
        await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Mapa homologado/i);
    });


    test('Cenários CDU-33: ADMIN reabre revisão de cadastro', async ({page, autenticadoComoAdmin}) => {
        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
        await page.goto(`/processo/${revisaoPid}/${UNIDADE_ALVO}`);
        await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Mapa homologado/i);

        const btnReabrir = page.getByTestId('btn-reabrir-revisao');
        await expect(btnReabrir).toBeVisible();
        await expect(btnReabrir).toBeEnabled();

        // Cenario 3: Abrir modal
        await btnReabrir.click();

        const modal = page.getByRole('dialog');
        await expect(modal).toBeVisible();
        await expect(modal.getByRole('heading', {name: /Reabrir Revisão/i})).toBeVisible();

        // Cenario 4: Confirmar reabertura
        await page.getByTestId('inp-justificativa-reabrir').fill('Ajuste necessário');
        await page.getByTestId('btn-confirmar-reabrir').click();

        await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Revisão em andamento/i);
        await expect(page.getByTestId('tbl-movimentacoes')).toContainText(/Reabertura de revisão de cadastro/i);
    });
});
