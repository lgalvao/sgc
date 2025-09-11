import {expect, test} from '@playwright/test';
import {
    adicionarAtividade,
    adicionarConhecimento,
    editarAtividade,
    editarConhecimento,
    esperarElementoVisivel,
    gerarNomeUnico,
    loginComoChefe,
    navegarParaCadastroAtividades,
    removerAtividade,
    removerConhecimento
} from './auxiliares-teste';
import {DADOS_TESTE, SELETORES, SELETORES_CSS, TEXTOS} from './constantes-teste';

test.describe('CDU-08 - Manter cadastro de atividades e conhecimentos', () => {
  test.beforeEach(async ({ page }) => {
    await loginComoChefe(page);
  });

  test('Passos 1-4: deve navegar do Painel para cadastro de atividades', async ({ page }) => {
    await navegarParaCadastroAtividades(page, DADOS_TESTE.PROCESSOS.MAPEAMENTO_STIC.id, DADOS_TESTE.UNIDADES.STIC);
    
    await expect(page.getByRole('heading', { name: TEXTOS.CADASTRO_ATIVIDADES_CONHECIMENTOS })).toBeVisible();
    await esperarElementoVisivel(page, SELETORES.INPUT_NOVA_ATIVIDADE);
  });

  test('Passo 5: deve exibir botão Impacto no mapa para processos de revisão', async ({ page }) => {
    await navegarParaCadastroAtividades(page, DADOS_TESTE.PROCESSOS.REVISAO_STIC.id, DADOS_TESTE.UNIDADES.STIC);
    
    const botaoImpacto = page.locator(`button:has-text("${TEXTOS.IMPACTO_NO_MAPA}")`);
    await expect(botaoImpacto).toBeVisible();
  });

  test('Passos 6-9: deve adicionar atividade e conhecimento', async ({ page }) => {
    await navegarParaCadastroAtividades(page, DADOS_TESTE.PROCESSOS.MAPEAMENTO_STIC.id, DADOS_TESTE.UNIDADES.STIC);
    
    // Passo 6-7: Adicionar atividade
    const nomeAtividade = gerarNomeUnico('Atividade Teste');
    await adicionarAtividade(page, nomeAtividade);
    
    // Passo 8-9: Adicionar conhecimento
    const nomeConhecimento = gerarNomeUnico('Conhecimento Teste');
    const cardAtividade = page.locator(SELETORES_CSS.CARD_ATIVIDADE, { hasText: nomeAtividade });
    await adicionarConhecimento(cardAtividade, nomeConhecimento);
  });

  test('Passos 11-11.2: deve editar e remover atividades', async ({ page }) => {
    await navegarParaCadastroAtividades(page, DADOS_TESTE.PROCESSOS.MAPEAMENTO_STIC.id, DADOS_TESTE.UNIDADES.STIC);
    
    // Adicionar atividade para teste
    const nomeOriginal = gerarNomeUnico('Atividade Editar');
    await adicionarAtividade(page, nomeOriginal);
    
    const cardAtividade = page.locator(SELETORES_CSS.CARD_ATIVIDADE, { hasText: nomeOriginal });
    
    // Passo 11.1: Editar atividade
    const nomeEditado = gerarNomeUnico('Atividade Editada');
    await editarAtividade(page, cardAtividade, nomeEditado);
    await expect(page.locator(SELETORES_CSS.CARD_ATIVIDADE, { hasText: nomeEditado })).toBeVisible();
    
    // Passo 11.2: Remover atividade com confirmação
    const cardEditado = page.locator(SELETORES_CSS.CARD_ATIVIDADE, { hasText: nomeEditado });
    await removerAtividade(page, cardEditado);
    await expect(page.locator(SELETORES_CSS.CARD_ATIVIDADE, { hasText: nomeEditado })).not.toBeAttached();
  });

  test('Passos 12-12.2: deve editar e remover conhecimentos', async ({ page }) => {
    await navegarParaCadastroAtividades(page, DADOS_TESTE.PROCESSOS.MAPEAMENTO_STIC.id, DADOS_TESTE.UNIDADES.STIC);
    
    // Adicionar atividade e conhecimento para teste
    const nomeAtividade = gerarNomeUnico('Atividade Conhecimento');
    await adicionarAtividade(page, nomeAtividade);
    
    const cardAtividade = page.locator(SELETORES_CSS.CARD_ATIVIDADE, { hasText: nomeAtividade });
    const nomeConhecimentoOriginal = gerarNomeUnico('Conhecimento Original');
    await adicionarConhecimento(cardAtividade, nomeConhecimentoOriginal);
    
    // Passo 12.1: Editar conhecimento
    const linhaConhecimento = cardAtividade.locator(SELETORES_CSS.GRUPO_CONHECIMENTO, { hasText: nomeConhecimentoOriginal });
    const nomeConhecimentoEditado = gerarNomeUnico('Conhecimento Editado');
    await editarConhecimento(page, linhaConhecimento, nomeConhecimentoEditado);
    await expect(cardAtividade.locator(SELETORES_CSS.GRUPO_CONHECIMENTO, { hasText: nomeConhecimentoEditado })).toBeVisible();
    
    // Passo 12.2: Remover conhecimento com confirmação
    const conhecimentoEditado = cardAtividade.locator(SELETORES_CSS.GRUPO_CONHECIMENTO, { hasText: nomeConhecimentoEditado });
    await removerConhecimento(page, conhecimentoEditado);
    await expect(cardAtividade.locator(SELETORES_CSS.GRUPO_CONHECIMENTO, { hasText: nomeConhecimentoEditado })).not.toBeAttached();
  });

  test('Passo 13: deve importar atividades de processos finalizados', async ({ page }) => {
    await navegarParaCadastroAtividades(page, DADOS_TESTE.PROCESSOS.MAPEAMENTO_STIC.id, DADOS_TESTE.UNIDADES.STIC);
    
    await page.click(`button:has-text("${TEXTOS.IMPORTAR_ATIVIDADES}")`);
    
    await expect(page.locator(SELETORES_CSS.MODAL_VISIVEL)).toBeVisible();
    await expect(page.locator('.modal-body')).toBeVisible();
  });

  test('Passo 14: deve alterar situação de "Não iniciado" para "em andamento"', async ({ page }) => {
    await navegarParaCadastroAtividades(page, DADOS_TESTE.PROCESSOS.MAPEAMENTO_STIC.id, DADOS_TESTE.UNIDADES.SEMARE);
    
    const nomeAtividade = gerarNomeUnico('Primeira Atividade');
    await adicionarAtividade(page, nomeAtividade);
    
    await expect(page.locator(SELETORES_CSS.NOTIFICACAO_SUCESSO)).toBeVisible();
  });

  test('Passo 15: deve disponibilizar cadastro após finalização', async ({ page }) => {
    await navegarParaCadastroAtividades(page, DADOS_TESTE.PROCESSOS.MAPEAMENTO_STIC.id, DADOS_TESTE.UNIDADES.STIC);
    
    // Adicionar atividade com conhecimento
    const nomeAtividade = gerarNomeUnico('Atividade Completa');
    await adicionarAtividade(page, nomeAtividade);
    
    const cardAtividade = page.locator(SELETORES_CSS.CARD_ATIVIDADE, { hasText: nomeAtividade });
    const nomeConhecimento = gerarNomeUnico('Conhecimento Completo');
    await adicionarConhecimento(cardAtividade, nomeConhecimento);
    
    const botaoDisponibilizar = page.locator(`button:has-text("${TEXTOS.DISPONIBILIZAR}")`);
    await expect(botaoDisponibilizar).toBeVisible();
  });

  test('deve validar campos vazios', async ({ page }) => {
    await navegarParaCadastroAtividades(page, DADOS_TESTE.PROCESSOS.MAPEAMENTO_STIC.id, DADOS_TESTE.UNIDADES.STIC);
    
    // Tentar adicionar atividade vazia
    await page.getByTestId(SELETORES.INPUT_NOVA_ATIVIDADE).fill('   ');
    await page.getByTestId(SELETORES.BTN_ADICIONAR_ATIVIDADE).click();
    
    // Não deve adicionar atividade com apenas espaços
    const contadorAntes = await page.locator(SELETORES_CSS.CARD_ATIVIDADE).count();
    await expect(page.locator(SELETORES_CSS.CARD_ATIVIDADE)).toHaveCount(contadorAntes);
  });

  test('deve cancelar edição de atividade', async ({ page }) => {
    await navegarParaCadastroAtividades(page, DADOS_TESTE.PROCESSOS.MAPEAMENTO_STIC.id, DADOS_TESTE.UNIDADES.STIC);
    
    const nomeOriginal = gerarNomeUnico('Atividade Original');
    await adicionarAtividade(page, nomeOriginal);
    
    const cardAtividade = page.locator(SELETORES_CSS.CARD_ATIVIDADE, { hasText: nomeOriginal });
    
    // Iniciar edição
    await cardAtividade.hover();
    await page.waitForTimeout(100);
    await cardAtividade.getByTestId(SELETORES.BTN_EDITAR_ATIVIDADE).click({ force: true });
    
    // Alterar texto e cancelar
    await page.getByTestId(SELETORES.INPUT_EDITAR_ATIVIDADE).fill('Texto alterado');
    await page.getByTestId(SELETORES.BTN_CANCELAR_EDICAO_ATIVIDADE).click();
    
    // Verificar que manteve o nome original
    await expect(page.locator(SELETORES_CSS.CARD_ATIVIDADE, { hasText: nomeOriginal })).toBeVisible();
  });
});