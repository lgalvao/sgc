import {expect, test} from './fixtures/complete-fixtures.js';
import {criarProcessoFinalizadoFixture, criarProcessoFixture} from './fixtures/index.js';
import {buscarCodSubprocessoDiagnostico} from './helpers/helpers-diagnostico.js';
import {login} from './helpers/helpers-auth.js';
import {navegarParaDiagnosticoUnidade} from './helpers/helpers-navegacao.js';
import {verificarNotificacaoAdmin} from './helpers/helpers-notificacoes-admin.js';
import {verificarToast} from './helpers/helpers-navegacao.js';
import {TEXTOS} from '../frontend/src/constants/textos.js';

const TITULO_SERVIDOR_ASSESSORIA_12 = '242426';
const UNIDADE = 'ASSESSORIA_12';

test.describe('CDU-44 - Realizar autoavaliação', () => {
    test('SERVIDOR tenta concluir sem preencher todas as competências, recebe erro, depois preenche e conclui com sucesso', async ({
                                                                                                                                       _resetAutomatico,
                                                                                                                                       page,
                                                                                                                                       request
                                                                                                                                   }) => {
        await criarProcessoFinalizadoFixture(request, {
            unidade: UNIDADE,
            iniciar: true
        });

        const descricao = `Diagnóstico CDU-44 ${Date.now()}`;
        const processo = await criarProcessoFixture(request, {
            descricao,
            tipo: 'DIAGNOSTICO',
            unidade: UNIDADE,
            iniciar: true,
            diasLimite: 30
        });

        // 1. Login como Servidor e navegação
        await login(page, TITULO_SERVIDOR_ASSESSORIA_12, 'senha');
        await page.goto(`/processo/${processo.codigo}`);
        await navegarParaDiagnosticoUnidade(page, UNIDADE);
        const codSubprocesso = await buscarCodSubprocessoDiagnostico(page, processo.codigo, UNIDADE);
        await page.goto(`/diagnostico/${codSubprocesso}/${UNIDADE}/autoavaliacao`);
        await expect(page.getByRole('heading', {name: TEXTOS.diagnostico.TITULO_AUTOAVALIACAO})).toBeVisible();
        await page.getByTestId(/^toggle-atividades-/).first().click();
        await expect(page.getByText(/Conhecimento/i).first()).toBeVisible();

        // 2. Tentar concluir sem preencher nenhuma competência
        await page.getByTestId('btn-concluir-autoavaliacao').click();
        await expect(page.getByRole('dialog')).toContainText(TEXTOS.diagnostico.MODAL_CONCLUIR_MENSAGEM);

        await Promise.all([
            page.waitForResponse(res => res.url().includes(`/api/subprocessos/${codSubprocesso}/diagnostico/autoavaliacao/concluir`) && res.status() === 422),
            page.getByTestId('btn-confirmar-concluir').click()
        ]);

        // Validar mensagem de erro no topo
        await expect(page.locator('.alert-danger')).toContainText('Preencha importância e domínio para todas as competências.');

        // 3. Preencher todas as competências (autosave)
        const selectImportancia = page.locator('[data-testid^="autoavaliacao-importancia-"]');
        const selectDominio = page.locator('[data-testid^="autoavaliacao-dominio-"]');
        await expect(selectImportancia.first()).toBeVisible();

        const total = await selectImportancia.count();
        for (let i = 0; i < total; i++) {
            if ((await selectImportancia.nth(i).inputValue()) !== '3') {
                await Promise.all([
                    page.waitForResponse(res => res.url().includes(`/api/subprocessos/${codSubprocesso}/diagnostico/autoavaliacao`) && res.request().method() === 'POST' && res.ok()),
                    selectImportancia.nth(i).selectOption('3')
                ]);
            }
            if ((await selectDominio.nth(i).inputValue()) !== '4') {
                await Promise.all([
                    page.waitForResponse(res => res.url().includes(`/api/subprocessos/${codSubprocesso}/diagnostico/autoavaliacao`) && res.request().method() === 'POST' && res.ok()),
                    selectDominio.nth(i).selectOption('4')
                ]);
            }
        }

        // 4. Concluir com sucesso
        await page.getByTestId('btn-concluir-autoavaliacao').click();
        await expect(page.getByRole('dialog')).toContainText(TEXTOS.diagnostico.MODAL_CONCLUIR_MENSAGEM);
        await Promise.all([
            page.waitForResponse(res => res.url().includes(`/api/subprocessos/${codSubprocesso}/diagnostico/autoavaliacao/concluir`) && res.ok()),
            page.getByTestId('btn-confirmar-concluir').click()
        ]);

        // Redirecionamento e mensagem de sucesso
        await expect(page).toHaveURL(new RegExp(String.raw`/processo/${processo.codigo}/${UNIDADE}`));
        await verificarToast(page, TEXTOS.diagnostico.SUCESSO_AUTOAVALIACAO_CONCLUIDA);

        // 5. Ao reabrir a autoavaliação, os campos ficam somente leitura
        await page.goto(`/diagnostico/${codSubprocesso}/${UNIDADE}/autoavaliacao`);
        await expect(page).toHaveURL(new RegExp(String.raw`/diagnostico/${codSubprocesso}/${UNIDADE}/autoavaliacao`));
        await expect(page.locator('[data-testid^="autoavaliacao-importancia-"]')).toHaveCount(0);
        await expect(page.locator('[data-testid^="autoavaliacao-dominio-"]')).toHaveCount(0);
        await expect(page.getByTestId('btn-concluir-autoavaliacao')).toBeDisabled();

        // 6. Verificar e-mail/notificação no admin
        await login(page, '191919', 'senha');
        await verificarNotificacaoAdmin(page, {
            destinatario: UNIDADE,
            assunto: 'Autoavaliação',
            tipo: 'Autoavaliação concluída'
        });
    });
});
