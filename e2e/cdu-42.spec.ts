import {expect, test} from './fixtures/complete-fixtures.js';
import {criarProcessoFixture} from './fixtures/index.js';
import {login} from './helpers/helpers-auth.js';
import {verificarNotificacaoAdmin} from './helpers/helpers-notificacoes-admin.js';
import {verificarToast} from './helpers/helpers-navegacao.js';
import {TEXTOS} from '../frontend/src/constants/textos.js';

const TITULO_SERVIDOR_ASSESSORIA_12 = '242426';
const UNIDADE = 'ASSESSORIA_12';

// CDU-42 testa o fluxo do CDU-43 (Realizar autoavaliação):
// Painel → Detalhes do subprocesso → card Autoavaliação → preenche → conclui
test.describe('CDU-42 - Realizar autoavaliação', () => {
    test('SERVIDOR preenche a autoavaliação, conclui e gera notificação', async ({
        _resetAutomatico,
        page,
        request
    }) => {
        const descricao = `Diagnóstico CDU-42 ${Date.now()}`;
        const processo = await criarProcessoFixture(request, {
            descricao,
            tipo: 'DIAGNOSTICO',
            unidade: UNIDADE,
            iniciar: true,
            diasLimite: 30
        });

        // CDU-43 passo 1: acessa a tela Detalhes do subprocesso
        await login(page, TITULO_SERVIDOR_ASSESSORIA_12, 'senha');
        await page.goto(`/processo/${processo.codigo}/${UNIDADE}`);

        // CDU-43 passo 2: aciona o card Autoavaliação
        await expect(page.getByTestId('card-subprocesso-diagnostico')).toBeVisible();
        await page.getByTestId('card-subprocesso-diagnostico').click();
        await expect(page).toHaveURL(new RegExp(String.raw`/diagnostico/\d+/${UNIDADE}/autoavaliacao`));
        await expect(page.getByRole('heading', {name: TEXTOS.diagnostico.TITULO_AUTOAVALIACAO})).toBeVisible();

        const codSubprocesso = Number(new URL(page.url()).pathname.split('/')[2]);

        // CDU-43 passos 4-5: preenche todos os campos com autosave
        const selectImportancia = page.locator('[data-testid^="autoavaliacao-importancia-"]');
        const selectDominio = page.locator('[data-testid^="autoavaliacao-dominio-"]');
        await expect(selectImportancia.first()).toBeVisible();

        const total = await selectImportancia.count();
        for (let i = 0; i < total; i++) {
            if ((await selectImportancia.nth(i).inputValue()) !== '3') {
                await Promise.all([
                    page.waitForResponse(res => res.url().includes(`/api/diagnosticos/subprocessos/${codSubprocesso}/autoavaliacao`) && res.request().method() === 'POST' && res.ok()),
                    selectImportancia.nth(i).selectOption('3')
                ]);
            }
            if ((await selectDominio.nth(i).inputValue()) !== '4') {
                await Promise.all([
                    page.waitForResponse(res => res.url().includes(`/api/diagnosticos/subprocessos/${codSubprocesso}/autoavaliacao`) && res.request().method() === 'POST' && res.ok()),
                    selectDominio.nth(i).selectOption('4')
                ]);
            }
        }

        // CDU-43 passo 6-7: conclui com confirmação
        await page.getByTestId('btn-concluir-autoavaliacao').click();
        await expect(page.getByRole('dialog')).toContainText(TEXTOS.diagnostico.MODAL_CONCLUIR_MENSAGEM);
        await Promise.all([
            page.waitForResponse(res => res.url().includes(`/api/diagnosticos/subprocessos/${codSubprocesso}/autoavaliacao/concluir`) && res.ok()),
            page.getByTestId('btn-confirmar-concluir').click()
        ]);

        // CDU-43 passo 10: redireciona para Detalhes do subprocesso com mensagem de sucesso
        await expect(page).toHaveURL(new RegExp(String.raw`/processo/${processo.codigo}/${UNIDADE}`));
        await verificarToast(page, TEXTOS.diagnostico.SUCESSO_AUTOAVALIACAO_CONCLUIDA);

        // CDU-43 passos 8-9: notificação gerada para o responsável da unidade
        await login(page, '191919', 'senha');
        await verificarNotificacaoAdmin(page, {
            destinatario: UNIDADE,
            assunto: 'Autoavaliação',
            tipo: 'Autoavaliação concluída'
        });
    });
});
