import {expect, test} from '@playwright/test';
import {loginComoAdmin, loginComoGestor} from './auxiliares-verificacoes';
import {cancelarModal, finalizarProcesso} from './auxiliares-acoes';
import {DADOS_TESTE, SELETORES, SELETORES_CSS, TEXTOS} from './constantes-teste';

test.describe('CDU-21 - Finalizar processo de mapeamento ou de revisão', () => {
    test.beforeEach(async ({page}) => {
        await loginComoAdmin(page);
    });

    test('deve navegar do Painel para processo Em andamento e exibir botão Finalizar', async ({page}) => {
        await expect(page.getByTestId(SELETORES.TITULO_PROCESSOS)).toContainText('Processos');
        const processoEmAndamento = page.locator(`table[data-testid="${SELETORES.TABELA_PROCESSOS}"] tbody tr`).filter({hasText: TEXTOS.EM_ANDAMENTO}).first();
        await processoEmAndamento.click();

        await expect(page.locator('h2.display-6')).toBeVisible();
        await expect(page.locator(`text=${TEXTOS.UNIDADES_PARTICIPANTES}`)).toBeVisible();
        await expect(page.locator(`button:has-text("${TEXTOS.FINALIZAR_PROCESSO}")`)).toBeVisible();
    });

    test('deve impedir finalização quando há unidades não homologadas', async ({page}) => {
        const processoEmAndamento = page.locator(`table[data-testid="${SELETORES.TABELA_PROCESSOS}"] tbody tr`).filter({hasText: TEXTOS.EM_ANDAMENTO}).first();
        await processoEmAndamento.click();

        await page.click(`button:has-text("${TEXTOS.FINALIZAR_PROCESSO}")`);

        await expect(page.locator('.notification')).toContainText('Não é possível encerrar o processo enquanto houver unidades com mapa de competência ainda não homologado');
    });

    test('deve exibir modal de confirmação com título e mensagem corretos', async ({page}) => {
        const processoTeste = page.locator(`table[data-testid="${SELETORES.TABELA_PROCESSOS}"] tbody tr`).filter({hasText: DADOS_TESTE.PROCESSOS.TESTE_FINALIZACAO.nome});

        if (await processoTeste.count() > 0) {
            await processoTeste.click();
            await page.click(`button:has-text("${TEXTOS.FINALIZAR_PROCESSO}")`);

            await expect(page.locator(SELETORES_CSS.MODAL_VISIVEL)).toBeVisible();
            await expect(page.locator(`h5:has-text("${TEXTOS.FINALIZACAO_PROCESSO}")`)).toBeVisible();
            await expect(page.locator(`text=${TEXTOS.CONFIRMA_FINALIZACAO}`)).toBeVisible();
            await expect(page.locator('text=Essa ação tornará vigentes os mapas de competências homologados')).toBeVisible();
            await expect(page.locator(`button:has-text("${TEXTOS.CONFIRMAR}")`)).toBeVisible();
            await expect(page.locator(`button:has-text("${TEXTOS.CANCELAR}")`)).toBeVisible();
        }
    });

    test('deve cancelar finalização e permanecer na mesma tela', async ({page}) => {
        const processoTeste = page.locator(`table[data-testid="${SELETORES.TABELA_PROCESSOS}"] tbody tr`)
            .filter({hasText: DADOS_TESTE.PROCESSOS.TESTE_FINALIZACAO.nome});

        if (await processoTeste.count() > 0) {
            await processoTeste.click();

            await page.click(`button:has-text("${TEXTOS.FINALIZAR_PROCESSO}")`);
            await expect(page.locator(SELETORES_CSS.MODAL_VISIVEL)).toBeVisible();

            await cancelarModal(page);
            await expect(page.locator(SELETORES_CSS.MODAL_VISIVEL)).not.toBeVisible();
            await expect(page.url()).toContain('/processo/99');
        }
    });

    test('Passo 7-10: deve finalizar processo com sucesso', async ({page}) => {
        const processoTeste = page.locator(`table[data-testid="${SELETORES.TABELA_PROCESSOS}"] tbody tr`)
            .filter({hasText: DADOS_TESTE.PROCESSOS.TESTE_FINALIZACAO.nome});

        if (await processoTeste.count() > 0) {
            await processoTeste.click();
            await finalizarProcesso(page);

            await expect(page.locator(SELETORES_CSS.NOTIFICACAO_SUCESSO)).toContainText(TEXTOS.PROCESSO_FINALIZADO);
            await expect(page).toHaveURL('/painel');

            await expect(page.locator(`table[data-testid="${SELETORES.TABELA_PROCESSOS}"] tbody tr`)
                .filter({hasText: DADOS_TESTE.PROCESSOS.TESTE_FINALIZACAO.nome})).toContainText(TEXTOS.FINALIZADO);
        }
    });

    test('Passo 9.1-9.2: deve enviar notificações por email', async ({page}) => {
        const processoTeste = page.locator(`table[data-testid="${SELETORES.TABELA_PROCESSOS}"] tbody tr`)
            .filter({hasText: DADOS_TESTE.PROCESSOS.TESTE_FINALIZACAO.nome});

        if (await processoTeste.count() > 0) {
            await processoTeste.click();

            await page.click(`button:has-text("${TEXTOS.FINALIZAR_PROCESSO}")`);
            await page.click(`button:has-text("${TEXTOS.CONFIRMAR}")`);

            await expect(page.locator(SELETORES_CSS.NOTIFICACAO_EMAIL).first()).toBeVisible();
        }
    });

    test('Pré-condição: não deve exibir botão para perfil não-ADMIN', async ({page}) => {
        await page.goto('/login');
        await page.waitForLoadState('networkidle');
        await loginComoGestor(page);

        const processoEmAndamento = page.locator(`table[data-testid="${SELETORES.TABELA_PROCESSOS}"] tbody tr`)
            .filter({hasText: TEXTOS.EM_ANDAMENTO}).first();
        await processoEmAndamento.click();

        await expect(page.locator(`button:has-text("${TEXTOS.FINALIZAR_PROCESSO}")`)).not.toBeVisible();
    });

    test('Passo 8: deve definir mapas como vigentes após finalização', async ({page}) => {
        await page.goto(`/processo/${DADOS_TESTE.PROCESSOS.TESTE_MAPEAMENTO.id}`);
        await page.waitForLoadState('networkidle');
        await page.waitForSelector(`button:has-text("${TEXTOS.FINALIZAR_PROCESSO}")`);

        await page.click(`button:has-text("${TEXTOS.FINALIZAR_PROCESSO}")`);
        await page.waitForSelector(SELETORES_CSS.MODAL_VISIVEL);
        await page.click(`button:has-text("${TEXTOS.CONFIRMAR}")`);
        await page.waitForSelector(SELETORES_CSS.NOTIFICACAO_SUCESSO);

        await expect(page).toHaveURL('/painel');

        const processoFinalizado = page.locator(`table[data-testid="${SELETORES.TABELA_PROCESSOS}"] tbody tr`)
            .filter({hasText: DADOS_TESTE.PROCESSOS.TESTE_MAPEAMENTO.nome});
        await expect(processoFinalizado).toContainText(TEXTOS.FINALIZADO);

        const notificacaoSucesso = await page.locator(SELETORES_CSS.NOTIFICACAO_SUCESSO).textContent();
        expect(notificacaoSucesso).toContain(TEXTOS.MAPAS_VIGENTES);
    });

    test('Passo 9.1-9.2: deve enviar emails com conteúdo correto', async ({page}) => {
        await page.goto(`/processo/${DADOS_TESTE.PROCESSOS.TESTE_MAPEAMENTO.id}`);
        await page.waitForLoadState('networkidle');
        await page.waitForSelector(`button:has-text("${TEXTOS.FINALIZAR_PROCESSO}")`);

        await page.click(`button:has-text("${TEXTOS.FINALIZAR_PROCESSO}")`);
        await page.waitForSelector(SELETORES_CSS.MODAL_VISIVEL);
        await page.click(`button:has-text("${TEXTOS.CONFIRMAR}")`);
        await page.waitForSelector(SELETORES_CSS.NOTIFICACAO_SUCESSO);

        const emailNotifications = await page.locator(SELETORES_CSS.NOTIFICACAO_EMAIL).count();
        expect(emailNotifications).toBeGreaterThan(0);

        const firstEmailNotification = await page.locator(SELETORES_CSS.NOTIFICACAO_EMAIL).first().textContent();
        expect(firstEmailNotification).toContain(TEXTOS.EMAIL_ENVIADO);
    });

    test('deve funcionar para processos de mapeamento e revisão', async ({page}) => {
        // Testar com processo de mapeamento
        await page.goto(`/processo/${DADOS_TESTE.PROCESSOS.TESTE_MAPEAMENTO.id}`);
        await page.waitForLoadState('networkidle');
        await page.waitForSelector(`button:has-text("${TEXTOS.FINALIZAR_PROCESSO}")`);

        await page.click(`button:has-text("${TEXTOS.FINALIZAR_PROCESSO}")`);
        await page.waitForSelector(SELETORES_CSS.MODAL_VISIVEL);

        const modalTitle = await page.textContent('.modal-title');
        expect(modalTitle).toContain(TEXTOS.FINALIZACAO_PROCESSO);

        await page.click(`button:has-text("${TEXTOS.CANCELAR}")`);
        await page.waitForSelector(SELETORES_CSS.MODAL_VISIVEL, {state: 'hidden'});

        // Testar com processo de revisão
        await page.goto(`/processo/${DADOS_TESTE.PROCESSOS.TESTE_REVISAO.id}`);
        await page.waitForLoadState('networkidle');
        await page.waitForSelector(`button:has-text("${TEXTOS.FINALIZAR_PROCESSO}")`);

        await page.click(`button:has-text("${TEXTOS.FINALIZAR_PROCESSO}")`);
        await page.waitForSelector(SELETORES_CSS.MODAL_VISIVEL);

        const modalTitleRevisao = await page.textContent('.modal-title');
        expect(modalTitleRevisao).toContain(TEXTOS.FINALIZACAO_PROCESSO);

        await page.click(`button:has-text("${TEXTOS.CONFIRMAR}")`);
        await page.waitForSelector(SELETORES_CSS.NOTIFICACAO_SUCESSO);

        const alertText = await page.textContent(SELETORES_CSS.NOTIFICACAO_SUCESSO);
        expect(alertText).toContain(TEXTOS.PROCESSO_FINALIZADO);
    });
});