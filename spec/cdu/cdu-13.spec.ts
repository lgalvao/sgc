import {expect} from '@playwright/test';
import {vueTest as test} from '../../tests/vue-specific-setup';
import {esperarTextoVisivel, esperarUrl, loginComoAdmin, loginComoGestor} from './auxiliares-verificacoes';
import {navegarParaVisualizacaoAtividades} from './auxiliares-navegacao';
import {devolverParaAjustes, homologarItem} from './auxiliares-acoes';
import {DADOS_TESTE, SELETORES_CSS, TEXTOS, URLS} from './constantes-teste';

test.describe('CDU-13: Analisar cadastro de atividades e conhecimentos', () => {
    const idProcessoStic = DADOS_TESTE.PROCESSOS.MAPEAMENTO_STIC.id;
    const siglaStic = DADOS_TESTE.UNIDADES.STIC;

    test('deve exibir modal de Histórico de análise', async ({page}) => {
        await loginComoGestor(page);
        await navegarParaVisualizacaoAtividades(page, idProcessoStic, siglaStic);

        await page.getByRole('button', {name: 'Histórico de análise'}).click();

        await expect(page.locator('div.modal.fade.show')).toBeVisible();
        await expect(page.getByRole('heading', {name: 'Histórico de Análise'})).toBeVisible();
        await esperarTextoVisivel(page, 'Observação de teste para histórico.');

        await page.getByRole('button', {name: 'Fechar'}).click();
        await expect(page.getByRole('heading', {name: 'Histórico de Análise'})).not.toBeVisible();
    });

    test('GESTOR deve conseguir devolver cadastro para ajustes', async ({page}) => {
        await loginComoGestor(page);

        await navegarParaVisualizacaoAtividades(page, idProcessoStic, siglaStic);

        await devolverParaAjustes(page, 'Devolução para correção de detalhes.');

        const notification = page.locator(SELETORES_CSS.NOTIFICACAO_SUCESSO);
        await expect(notification).toBeVisible();
        await expect(notification).toContainText('O cadastro foi devolvido para ajustes!');

        await esperarUrl(page, URLS.PAINEL);
    });

    test('ADMIN deve conseguir devolver cadastro para ajustes', async ({page}) => {
        await loginComoAdmin(page);
        await navegarParaVisualizacaoAtividades(page, idProcessoStic, siglaStic);

        await devolverParaAjustes(page);

        const notification = page.locator(SELETORES_CSS.NOTIFICACAO_SUCESSO);
        await expect(notification).toBeVisible();
        await expect(notification).toContainText('O cadastro foi devolvido para ajustes!');

        await esperarUrl(page, URLS.PAINEL);
    });

    test('GESTOR deve conseguir registrar aceite do cadastro', async ({page}) => {
        await loginComoGestor(page);
        await navegarParaVisualizacaoAtividades(page, idProcessoStic, siglaStic);

        const acceptButton = page.getByRole('button', {name: 'Registrar aceite'});
        const validateButton = page.getByRole('button', {name: 'Validar'});

        if (await acceptButton.isVisible()) {
            await acceptButton.click();
        } else {
            await validateButton.click();
        }

        await expect(page.getByRole('heading', {name: 'Validação do cadastro'})).toBeVisible();
        await esperarTextoVisivel(page, 'Confirma o aceite do cadastro de atividades?');

        await page.getByLabel('Observação').fill('Aceite do cadastro de atividades.');
        await page.getByRole('button', {name: TEXTOS.CONFIRMAR}).click();

        const notification = page.locator(SELETORES_CSS.NOTIFICACAO_SUCESSO);
        await expect(notification).toBeVisible();
        await expect(notification).toContainText('A análise foi registrada com sucesso!');

        await esperarUrl(page, URLS.PAINEL);
    });

    test('ADMIN deve conseguir homologar o cadastro', async ({page}) => {
        await loginComoAdmin(page);
        await navegarParaVisualizacaoAtividades(page, idProcessoStic, siglaStic);

        const homologateButton = page.getByRole('button', {name: 'Homologar'});
        const validateButton = page.getByRole('button', {name: 'Validar'});

        if (await homologateButton.isVisible()) {
            await homologarItem(page);
        } else {
            await validateButton.click();
            await expect(page.getByRole('heading', {name: 'Homologação do cadastro de atividades e conhecimentos'})).toBeVisible();
            await esperarTextoVisivel(page, 'Confirma a homologação do cadastro de atividades e conhecimentos?');
            await page.getByRole('button', {name: TEXTOS.CONFIRMAR}).click();
        }

        const notificacao = page.locator(SELETORES_CSS.NOTIFICACAO_SUCESSO);
        await expect(notificacao).toBeVisible();
        await expect(notificacao).toContainText('O cadastro foi homologado com sucesso!');

        await expect(page).toHaveURL(new RegExp('/processo/1/STIC'));
    });
});