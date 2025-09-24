import {expect} from '@playwright/test';
import {vueTest as test} from '../support/vue-specific-setup';
import {
    adicionarConhecimento,
    esperarElementoInvisivel,
    esperarTextoVisivel,
    loginComoAdmin,
    navegarParaCadastroAtividades
} from './auxiliares-verificacoes';
import {irParaMapaCompetencias} from './auxiliares-navegacao';
import {DADOS_TESTE, SELETORES_CSS} from './constantes-teste';

test.describe('CDU-12: Verificar impactos no mapa de competências', () => {
  test('deve exibir mensagem de "Nenhum impacto" quando não houver divergências', async ({page}) => {
    await loginComoAdmin(page);

    await irParaMapaCompetencias(page, DADOS_TESTE.PROCESSOS.REVISAO_STIC.id, 'SESEL');
    //await page.waitForLoadState('networkidle');

    await page.getByTestId('impactos-mapa-button').waitFor({state: 'visible'});
    await page.getByTestId('impactos-mapa-button').click();

    await esperarElementoInvisivel(page, 'impacto-mapa-modal');
    await esperarTextoVisivel(page, 'Nenhum impacto no mapa da unidade.');
  });

  test('deve exibir seções de impactos quando houver divergências (com impactos)', async ({page}) => {
    // 1) Criar mudanças no cadastro da unidade SESEL no processo de Revisão (id 2)
    await loginComoAdmin(page);
    await navegarParaCadastroAtividades(page, DADOS_TESTE.PROCESSOS.REVISAO_STIC.id, 'SESEL');

    // Adiciona um conhecimento na primeira atividade para registrar impacto
    const primeiroCardAtividade = page.locator(SELETORES_CSS.CARD_ATIVIDADE).first();
    await adicionarConhecimento(primeiroCardAtividade, `Conhecimento Impacto ${Date.now()}`);

    // 2) Navegar para o mapa de competências e abrir o modal de impactos
    await irParaMapaCompetencias(page, DADOS_TESTE.PROCESSOS.REVISAO_STIC.id, 'SESEL');
    await page.getByTestId('impactos-mapa-button').waitFor({ state: 'visible' });
    await page.getByTestId('impactos-mapa-button').click();

    // 3) Verificações no modal
    const modal = page.getByTestId('impacto-mapa-modal');
    await expect(modal).toBeVisible();

    // Deve exibir o título de "Competências impactadas" e não deve mostrar a mensagem de "Nenhuma competência foi impactada."
    await expect(page.getByTestId('titulo-competencias-impactadas')).toBeVisible();
    await expect(page.getByTestId('msg-nenhuma-competencia')).not.toBeVisible();

    // 4) Fechar o modal
    await page.getByTestId('fechar-impactos-mapa-button').click();
    await expect(modal).not.toBeVisible();
  });
});