import {expect, test} from './fixtures/complete-fixtures.js';

test.describe('CDU-40 - Consultar feedbacks enviados', () => {
    test('ADMIN consulta feedbacks, visualiza detalhes, metadados e captura ampliada', async ({
        _resetAutomatico,
        page,
        request,
        _autenticadoComoAdmin
    }) => {
        const resposta = await request.post('/e2e/fixtures/feedback', {
            data: {
                tipo: 'SUGESTAO',
                nota: '<p><strong>Melhorar</strong> o fluxo do painel.</p>',
                rota: '/painel',
                metadataJson: JSON.stringify({
                    rotaCaminho: '/painel',
                    rotaQuery: '?aba=alertas',
                    perfilAtivo: 'ADMIN',
                    unidadeAtiva: 'SECRETARIA_1',
                    larguraTela: 1440,
                    alturaTela: 900,
                    tituloPagina: 'Painel',
                    idioma: 'pt-BR',
                    userAgent: 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) Chrome/136.0.0.0 Safari/537.36'
                }),
                comScreenshot: true,
                usuarioCodigo: '191919',
                usuarioNome: 'Administrador 1'
            }
        });
        expect(resposta.ok()).toBeTruthy();
        const feedback = await resposta.json();

        await page.goto('/administracao/feedbacks');
        await expect(page.getByRole('heading', {name: /Feedbacks/i})).toBeVisible();
        await expect(page.getByTestId('tbl-feedbacks')).toBeVisible();
        await expect(page.getByTestId('btn-feedbacks-atualizar')).toBeVisible();

        await page.getByTestId(`btn-feedback-detalhes-${feedback.codigo}`).click();
        const modal = page.getByTestId('modal-detalhes-feedback');
        await expect(modal).toBeVisible();
        await expect(modal).toContainText('Administrador 1 (191919)');
        await expect(modal).toContainText('/painel');
        await expect(modal).toContainText('Melhorar o fluxo do painel.');
        await expect(modal).toContainText('Rota');
        await expect(modal).toContainText('/painel');
        await expect(modal).toContainText('Acesso');
        await expect(modal).toContainText('ADMIN - SECRETARIA_1');
        await expect(modal).toContainText('Resolução');
        await expect(modal).toContainText('1440x900');
        await expect(page.getByTestId('img-feedback-captura')).toBeVisible();

        await page.getByTestId('btn-feedback-ampliar-captura').click();
        await expect(page.getByTestId('modal-imagem-ampliada')).toBeVisible();
        await expect(page.getByTestId('modal-imagem-ampliada').locator('img')).toBeVisible();
    });
});
