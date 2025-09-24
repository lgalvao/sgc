import {expect, Page} from '@playwright/test';
import {vueTest as test} from '../support/vue-specific-setup';
import {esperarUrl, loginComoAdmin, loginComoGestor} from './auxiliares-verificacoes';
import {devolverParaAjustes} from './auxiliares-acoes';
import {irParaSubprocesso} from './auxiliares-navegacao';
import {DADOS_TESTE, SELETORES_CSS, TEXTOS, URLS} from './constantes-teste';

async function navegarParaAnaliseRevisao(page: Page, loginFn: (page: Page) => Promise<void>) {
  await loginFn(page);
  await irParaSubprocesso(page, DADOS_TESTE.PROCESSOS.REVISAO_STIC.id, 'SESEL');
  await page.goto(`/processo/${DADOS_TESTE.PROCESSOS.REVISAO_STIC.id}/SESEL/vis-cadastro`);
  await page.waitForLoadState('networkidle');
  await expect(page.getByRole('heading', { name: 'Atividades e conhecimentos' })).toBeVisible();
}

async function esperarSucessoERedirecionamento(page: Page, mensagem: string) {
  const notification = page.locator(SELETORES_CSS.NOTIFICACAO_SUCESSO);
  await expect(notification).toBeVisible();
  await expect(notification).toContainText(mensagem);
  await esperarUrl(page, URLS.PAINEL);
}

test.describe('CDU-14: Analisar revisão de cadastro de atividades e conhecimentos', () => {
  test('deve exibir botões corretos por perfil', async ({ page }) => {
    await navegarParaAnaliseRevisao(page, loginComoGestor);
    await expect(page.getByText('Histórico de análise')).toBeVisible();
    await expect(page.getByText('Devolver para ajustes')).toBeVisible();
    await expect(page.getByText('Registrar aceite')).toBeVisible();
    
    await navegarParaAnaliseRevisao(page, loginComoAdmin);
    await expect(page.getByText('Homologar')).toBeVisible();
  });

  test('deve permitir devolução e aceite', async ({ page }) => {
    await navegarParaAnaliseRevisao(page, loginComoGestor);
    
    // Testar devolução
    await devolverParaAjustes(page, 'Ajustes necessários.');
    await esperarSucessoERedirecionamento(page, 'devolvido para ajustes');
    
    // Testar aceite
    await navegarParaAnaliseRevisao(page, loginComoGestor);
    await page.getByText('Registrar aceite').click();
    await expect(page.getByRole('heading', { name: 'Aceite da revisão do cadastro' })).toBeVisible();
    await page.getByLabel('Observação').fill('Aceite após análise.');
    await page.getByRole('button', { name: TEXTOS.CONFIRMAR }).click();
    await esperarSucessoERedirecionamento(page, 'Aceite registrado');
  });

  test('deve exibir histórico de análise', async ({ page }) => {
    await navegarParaAnaliseRevisao(page, loginComoGestor);
    await page.getByText('Histórico de análise').click();
    
    const modal = page.locator(SELETORES_CSS.MODAL_VISIVEL);
    await expect(modal).toBeVisible();
    await expect(page.getByRole('heading', { name: 'Histórico de Análise' })).toBeVisible();
    
    await page.getByRole('button', { name: 'Fechar' }).click();
    await expect(modal).not.toBeVisible();
  });
});