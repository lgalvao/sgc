import {expect, test} from '@playwright/test';
import {
    aguardarProcessoNoPainel,
    cancelarModal,
    criarProcessoCompleto,
    loginComoAdmin,
    loginComoGestor,
    navegarParaProcessoNaTabela,
    TEXTOS,
    URLS
} from "~/helpers";

test.describe('Captura de Telas - Processos', () => {
    const nomeProcesso = 'Processo para Screenshots';

    test.beforeEach(async ({page}) => {
        await loginComoAdmin(page);
        await criarProcessoCompleto(page, nomeProcesso, 'MAPEAMENTO', '2025-12-31', [1, 2]);
        await aguardarProcessoNoPainel(page, nomeProcesso);
    });

    test('09 - Cadastro de Processo - Formulário Vazio (ADMIN)', async ({page}) => {
        await page.goto(URLS.PROCESSO_CADASTRO);
        await page.waitForLoadState('networkidle');
        await page.screenshot({path: 'screenshots/09-cadastro-processo-vazio.png', fullPage: true});
    });

    test('10 - Cadastro de Processo - Erro Descrição Vazia (ADMIN)', async ({page}) => {
        await page.goto(URLS.PROCESSO_CADASTRO);
        await page.getByRole('button', {name: TEXTOS.SALVAR}).click();
        await page.waitForSelector('.notification-error');
        await page.screenshot({path: 'screenshots/10-cadastro-processo-erro-descricao.png', fullPage: true});
    });

    test('11 - Cadastro de Processo - Unidades Selecionadas (ADMIN)', async ({page}) => {
        await page.goto(URLS.PROCESSO_CADASTRO);
        await page.getByLabel('Descrição').fill('Processo com Unidades');
        await page.getByLabel('Tipo').selectOption('Mapeamento');
        await page.waitForSelector('input[type="checkbox"]');
        page.locator('input[type="checkbox"]').first().check();
        await page.screenshot({path: 'screenshots/11-cadastro-processo-unidades-selecionadas.png', fullPage: true});
    });

    test('12 - Edição de Processo (ADMIN)', async ({page}) => {
        await navegarParaProcessoNaTabela(page, nomeProcesso);
        await page.waitForLoadState('networkidle');
        await page.screenshot({path: 'screenshots/12-edicao-processo.png', fullPage: true});
    });

    test('13 - Modal Confirmação Início de Processo (ADMIN)', async ({page}) => {
        await navegarParaProcessoNaTabela(page, nomeProcesso);
        await page.getByTestId('btn-iniciar-processo').click();
        await expect(page.locator('.modal.show', {hasText: TEXTOS.CONFIRMACAO_INICIAR_PROCESSO})).toBeVisible();
        await page.screenshot({path: 'screenshots/13-modal-confirmacao-inicio-processo.png', fullPage: true});
        await cancelarModal(page); // Fecha o modal para não interferir em outros testes
    });

    test('14 - Detalhes de Processo (ADMIN)', async ({page}) => {
        await navegarParaProcessoNaTabela(page, nomeProcesso);
        await page.waitForLoadState('networkidle');
        await page.screenshot({path: 'screenshots/14-detalhes-processo-admin.png', fullPage: true});
    });

    test('15 - Detalhes de Processo (GESTOR)', async ({page}) => {
        await loginComoGestor(page);
        await navegarParaProcessoNaTabela(page, nomeProcesso);
        await page.waitForLoadState('networkidle');
        await page.screenshot({path: 'screenshots/15-detalhes-processo-gestor.png', fullPage: true});
    });

    test('16 - Modal Aceitar em Bloco (GESTOR)', async ({page}) => {
        await loginComoGestor(page);
        await navegarParaProcessoNaTabela(page, nomeProcesso);
        const aceitarBlocoButton = page.getByRole('button', {name: 'Aceitar em bloco'});
        if (await aceitarBlocoButton.isVisible()) {
            await aceitarBlocoButton.click();
            await page.waitForSelector('.modal.show');
            await page.screenshot({path: 'screenshots/16-modal-aceitar-em-bloco.png', fullPage: true});
            await cancelarModal(page);
        }
    });

    test('17 - Modal Homologar em Bloco (ADMIN)', async ({page}) => {
        await navegarParaProcessoNaTabela(page, nomeProcesso);
        const homologarBlocoButton = page.getByRole('button', {name: 'Homologar em bloco'});
        if (await homologarBlocoButton.isVisible()) {
            await homologarBlocoButton.click();
            await page.waitForSelector('.modal.show');
            await page.screenshot({path: 'screenshots/17-modal-homologar-em-bloco.png', fullPage: true});
            await cancelarModal(page);
        }
    });

    test('18 - Modal Finalização de Processo (ADMIN)', async ({page}) => {
        await navegarParaProcessoNaTabela(page, nomeProcesso);
        const finalizarButton = page.getByRole('button', {name: TEXTOS.FINALIZAR_PROCESSO});
        if (await finalizarButton.isVisible()) {
            await finalizarButton.click();
            await page.waitForSelector('.modal.show');
            await page.screenshot({path: 'screenshots/18-modal-finalizacao-processo.png', fullPage: true});
            await cancelarModal(page);
        }
    });
});
