import {expect, test} from './fixtures/complete-fixtures.js';
import {criarProcessoDiagnosticoComAutoavaliacaoConcluidaFixture} from './fixtures/index.js';
import {login} from './helpers/helpers-auth.js';

const TITULO_CHEFE_ASSESSORIA_12 = '151515';
const TITULO_SERVIDOR_ASSESSORIA_12 = '242426';
const NOME_SERVIDOR_ASSESSORIA_12 = 'João Guilherme de Albuquerque Maranhão';
const UNIDADE = 'ASSESSORIA_12';

test.describe('CDU-42 - Visualizar detalhes de subprocesso de diagnóstico: CHEFE e SERVIDOR', () => {
    test('CHEFE e SERVIDOR visualizam a tela de detalhes com elements e permissões adequadas ao perfil', async ({
        _resetAutomatico,
        page,
        request
    }) => {
        const descricao = `Diagnóstico CDU-42 ${Date.now()}`;
        const processo = await criarProcessoDiagnosticoComAutoavaliacaoConcluidaFixture(request, {
            descricao,
            unidade: UNIDADE,
            iniciar: true,
            servidorTitulo: TITULO_SERVIDOR_ASSESSORIA_12
        });

        // 1. Acesso como CHEFE
        await login(page, TITULO_CHEFE_ASSESSORIA_12, 'senha');
        await page.goto(`/processo/${processo.codigo}/${UNIDADE}`);

        // CHEFE deve ver a grade de servidores (tabela)
        await expect(page.getByTestId('tbl-servidores-diagnostico')).toBeVisible();
        await expect(page.getByText(NOME_SERVIDOR_ASSESSORIA_12)).toBeVisible();
        await expect(page.getByTestId('tbl-movimentacoes')).toBeVisible();

        // CHEFE deve ver o dropdown de ações para os servidores da equipe
        const dropdownAcoes = page.getByTestId(`dropdown-acoes-${TITULO_SERVIDOR_ASSESSORIA_12}`);
        await expect(dropdownAcoes).toBeVisible();
        await dropdownAcoes.getByRole('button', {name: 'Ações'}).click();
        await expect(page.getByTestId(`btn-manter-consenso-${TITULO_SERVIDOR_ASSESSORIA_12}`)).toBeVisible();
        await expect(page.getByTestId(`btn-impossibilitar-${TITULO_SERVIDOR_ASSESSORIA_12}`)).toBeVisible();
        await expect(page.getByTestId(`btn-desfazer-impossibilidade-${TITULO_SERVIDOR_ASSESSORIA_12}`)).toBeDisabled();

        // CHEFE ainda não deve poder abrir Situação de capacitação sem consenso aprovado
        await expect(page.getByTestId('card-subprocesso-situacoes-capacitacao')).toHaveCount(0);
        await expect(page.getByTestId('card-subprocesso-situacoes-capacitacao-desabilitado')).toBeVisible();

        // CHEFE deve ver o botão para Concluir diagnóstico
        await expect(page.getByTestId('btn-concluir-diagnostico-cabecalho')).toBeVisible();

        // 2. Acesso como SERVIDOR
        await login(page, TITULO_SERVIDOR_ASSESSORIA_12, 'senha');
        await page.goto(`/processo/${processo.codigo}/${UNIDADE}`);

        // SERVIDOR deve ver o card de Autoavaliação
        await expect(page.getByTestId('card-subprocesso-diagnostico')).toBeVisible();
        await expect(page.getByTestId('card-subprocesso-consenso')).toHaveClass(/card-disabled/);

        // SERVIDOR NÃO deve ver a grade de servidores (tabela) nem botão de Concluir diagnóstico
        await expect(page.getByTestId('tbl-servidores-diagnostico')).not.toBeVisible();
        await expect(page.getByTestId('btn-concluir-diagnostico-cabecalho')).not.toBeVisible();
        await expect(page.getByTestId(`dropdown-acoes-${TITULO_SERVIDOR_ASSESSORIA_12}`)).not.toBeVisible();
    });
});
