import {expect, test} from './fixtures/complete-fixtures.js';
import {abrirNotificacoesAdmin} from './helpers/helpers-notificacoes-admin.js';

test.describe('CDU-38 - Acompanhar notificações por e-mail', () => {
    test('ADMIN visualiza detalhes, preview e reenfileira notificação com falha definitiva', async ({
                                                                                                        _resetAutomatico,
                                                                                                        page,
                                                                                                        request,
                                                                                                        _autenticadoComoAdmin
                                                                                                    }) => {
        const respostaFalha = await request.post('/e2e/fixtures/notificacao-email', {
            data: {
                destinatario: 'falha.definitiva@tre-pe.jus.br',
                assunto: 'SGC: Falha definitiva fixture',
                corpoHtml: '<p>Corpo fixture com <strong>preview</strong>.</p>',
                tipoNotificacao: 'LEMBRETE_PRAZO',
                situacao: 'FALHA_DEFINITIVA',
                unidadeDestinoSigla: 'ASSESSORIA_12',
                tentativas: 3,
                ultimoErro: 'SMTP indisponível'
            }
        });
        expect(respostaFalha.ok()).toBeTruthy();
        const notificacaoFalha = await respostaFalha.json();

        await request.post('/e2e/fixtures/notificacao-email', {
            data: {
                destinatario: 'enviado@tre-pe.jus.br',
                assunto: 'SGC: Notificação enviada fixture',
                corpoHtml: '<p>Outra notificação.</p>',
                tipoNotificacao: 'LEMBRETE_PRAZO',
                situacao: 'ENVIADO',
                unidadeDestinoSigla: 'ASSESSORIA_11'
            }
        });

        const tabela = await abrirNotificacoesAdmin(page);
        await expect(tabela).toContainText('ASSESSORIA_12');
        await expect(tabela).toContainText('Falha definitiva');
        await expect(page.getByTestId('btn-notificacoes-atualizar')).toBeVisible();

        await page.getByTestId(`btn-detalhes-${notificacaoFalha.codigo}`).click();
        await expect(page.getByTestId('modal-detalhes-notificacao')).toBeVisible();
        await expect(page.getByText('SMTP indisponível')).toBeVisible();
        await expect(page.getByText('Falhas anteriores')).toBeVisible();
        await page.getByRole('button', {name: /Fechar/i}).click();

        await page.getByTestId(`btn-preview-${notificacaoFalha.codigo}`).click();
        const modalPreview = page.getByTestId('modal-preview-email');
        await expect(modalPreview).toBeVisible();
        await expect(modalPreview).toContainText('falha.definitiva@tre-pe.jus.br');
        await expect(modalPreview.frameLocator('[data-testid="iframe-preview-email"]').locator('body')).toContainText('Corpo fixture com preview.');
        await page.getByTestId('btn-fechar-preview-email').click();

        await page.getByTestId(`btn-notificacoes-reenviar-${notificacaoFalha.codigo}`).click();
        await expect(page.getByTestId('txt-notificacoes-reenviar-confirmacao')).toContainText('falha.definitiva@tre-pe.jus.br');
        await Promise.all([
            page.waitForResponse(res => res.url().includes(`/api/admin/notificacoes/${notificacaoFalha.codigo}/reenviar`) && res.ok()),
            page.getByTestId('btn-notificacoes-reenviar-confirmar').click()
        ]);
        await expect(tabela).toBeVisible();
    });
});
