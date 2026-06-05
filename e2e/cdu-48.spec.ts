import {expect, test} from './fixtures/complete-fixtures.js';
import {criarProcessoDiagnosticoComAutoavaliacaoConcluidaFixture} from './fixtures/index.js';
import {
    abrirAcaoConsensoDiagnostico,
    buscarCodSubprocessoDiagnostico
} from './helpers/helpers-diagnostico.js';
import {login} from './helpers/helpers-auth.js';
import {verificarNotificacaoAdmin} from './helpers/helpers-notificacoes-admin.js';

const TITULO_SERVIDOR_ASSESSORIA_12 = '242426';
const TITULO_CHEFE_ASSESSORIA_12 = '151515';
const UNIDADE = 'ASSESSORIA_12';
const JUSTIFICATIVA_IMPOSSIBILIDADE = 'Servidor afastado durante o período da avaliação.';

async function impossibilitarAvaliacoesPendentes(page: import('@playwright/test').Page, codSubprocesso: number): Promise<void> {
    const linhas = page.locator('tbody tr');
    const total = await linhas.count();

    for (let i = 0; i < total; i++) {
        const linha = linhas.nth(i);
        const texto = await linha.textContent();
        if (!texto?.includes('Autoavaliação não iniciada')) {
            continue;
        }

        await linha.getByRole('button', {name: 'Ações'}).click();
        await page.locator('[data-testid^="btn-impossibilitar-"]:visible').click();
        await page.getByTestId('textarea-justificativa-impossibilidade').fill(JUSTIFICATIVA_IMPOSSIBILIDADE);
        await Promise.all([
            page.waitForResponse(res =>
                res.url().includes(`/api/diagnosticos/subprocessos/${codSubprocesso}/avaliacoes/`)
                && res.url().includes('/impossibilitar')
                && res.request().method() === 'POST'
                && res.ok()
            ),
            page.getByTestId('btn-confirmar-impossibilitar').click()
        ]);
    }
}

async function preencherOcupacoesPendentesPorApi(page: import('@playwright/test').Page, codSubprocesso: number): Promise<void> {
    await page.evaluate(async (codigo) => {
        const respostaAtual = await fetch(`/api/diagnosticos/subprocessos/${codigo}/unidade`, {credentials: 'include'});
        if (!respostaAtual.ok) {
            throw new Error(`Falha ao carregar ocupações críticas do subprocesso ${codigo}.`);
        }

        const dados = await respostaAtual.json();
        const ocupacoes = dados.ocupacoesCriticas.map((item: {
            servidorTitulo: string;
            competenciaCodigo: number;
            situacaoCapacitacao: string | null;
        }) => ({
            servidorTitulo: item.servidorTitulo,
            competenciaCodigo: item.competenciaCodigo,
            situacaoCapacitacao: item.situacaoCapacitacao ?? 'EC',
        }));

        const respostaSalvar = await fetch(`/api/diagnosticos/subprocessos/${codigo}/ocupacoes-criticas`, {
            method: 'POST',
            credentials: 'include',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({ocupacoes}),
        });

        if (!respostaSalvar.ok) {
            throw new Error(`Falha ao preencher ocupações críticas do subprocesso ${codigo}.`);
        }
    }, codSubprocesso);
}

test.describe('CDU-48 - Concluir diagnóstico da unidade', () => {
    test('CHEFE conclui o diagnóstico após eliminar pendências e submete para análise', async ({
        _resetAutomatico,
        page,
        request
    }) => {
        const descricao = `Diagnóstico CDU-48 ${Date.now()}`;
        const processo = await criarProcessoDiagnosticoComAutoavaliacaoConcluidaFixture(request, {
            descricao,
            unidade: UNIDADE,
            iniciar: true,
            servidorTitulo: TITULO_SERVIDOR_ASSESSORIA_12
        });

        await login(page, TITULO_CHEFE_ASSESSORIA_12, 'senha');
        await page.goto(`/processo/${processo.codigo}/${UNIDADE}`);
        const codSubprocesso = await buscarCodSubprocessoDiagnostico(page, processo.codigo, UNIDADE);

        await abrirAcaoConsensoDiagnostico(page, TITULO_SERVIDOR_ASSESSORIA_12);
        const seletorConsensoImportancia = page.locator('[data-testid^="consenso-final-importancia-"]').first();
        await Promise.all([
            page.waitForResponse(res =>
                res.url().includes(`/api/diagnosticos/subprocessos/${codSubprocesso}/consenso/${TITULO_SERVIDOR_ASSESSORIA_12}`)
                && res.request().method() === 'POST'
                && res.ok()
            ),
            seletorConsensoImportancia.selectOption('4')
        ]);

        await login(page, TITULO_SERVIDOR_ASSESSORIA_12, 'senha');
        await page.goto(`/diagnostico/${codSubprocesso}/${UNIDADE}/consenso/${TITULO_SERVIDOR_ASSESSORIA_12}`);
        await Promise.all([
            page.waitForResponse(res => res.url().includes(`/api/diagnosticos/subprocessos/${codSubprocesso}/consenso/aprovar`) && res.ok()),
            page.getByTestId('btn-aprovar-consenso').click()
        ]);

        await login(page, TITULO_CHEFE_ASSESSORIA_12, 'senha');
        await page.goto(`/processo/${processo.codigo}/${UNIDADE}`);
        await expect(page.getByTestId('btn-concluir-diagnostico')).toBeVisible();
        await page.getByTestId('btn-concluir-diagnostico').click();
        const modalConcluir = page.getByRole('dialog');
        await expect(modalConcluir).toContainText('Confirma a conclusão do diagnóstico desta unidade?');
        await Promise.all([
            page.waitForResponse(res =>
                res.url().includes(`/api/diagnosticos/subprocessos/${codSubprocesso}/concluir`)
                && res.request().method() === 'POST'
                && res.status() >= 400
            ),
            page.getByTestId('btn-confirmar-concluir-diagnostico').click()
        ]);
        await expect(page.getByTestId('app-alert')).toContainText('Ainda existem avaliações ou ocupações críticas pendentes.');
        await page.getByRole('dialog').getByRole('button', {name: 'Cancelar'}).click();

        await impossibilitarAvaliacoesPendentes(page, codSubprocesso);
        await expect(page.getByText('Autoavaliação não iniciada')).toHaveCount(0);

        await preencherOcupacoesPendentesPorApi(page, codSubprocesso);
        await expect.poll(async () => await page.evaluate(async (codigo) => {
            const resposta = await fetch(`/api/diagnosticos/subprocessos/${codigo}/unidade`, {credentials: 'include'});
            if (!resposta.ok) {
                return false;
            }
            const dados = await resposta.json();
            return dados.ocupacoesCriticas.every((item: {situacaoCapacitacao: string | null}) => item.situacaoCapacitacao !== null);
        }, codSubprocesso)).toBe(true);

        await page.goto(`/processo/${processo.codigo}/${UNIDADE}`);
        await page.getByTestId('btn-concluir-diagnostico').click();
        await expect(page.getByRole('dialog')).toContainText('Confirma a conclusão do diagnóstico desta unidade?');
        await Promise.all([
            page.waitForResponse(res =>
                res.url().includes(`/api/diagnosticos/subprocessos/${codSubprocesso}/concluir`)
                && res.request().method() === 'POST'
                && res.ok()
            ),
            page.getByTestId('btn-confirmar-concluir-diagnostico').click()
        ]);

        await expect(page).toHaveURL(/\/painel/);

        await login(page, '191919', 'senha');
        await verificarNotificacaoAdmin(page, {
            destinatario: 'SECRETARIA_1',
            assunto: 'Diagnóstico da unidade ASSESSORIA_12 concluído',
            tipo: 'Diagnóstico concluído',
            trechoCorpo: descricao
        });
    });
});
