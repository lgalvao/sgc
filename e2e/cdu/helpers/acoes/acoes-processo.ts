import {expect, Page} from '@playwright/test';
import {SELETORES, SELETORES_CSS, TEXTOS} from '../dados/constantes-teste';

/**
 * AÇÕES ESPECÍFICAS PARA PROCESSOS
 * Funções para gerenciamento de processos em testes
 */

/**
 * Preencher formulário básico de processo
 */
export async function preencherFormularioProcesso(page: Page, descricao: string, tipo: string, dataLimite?: string, sticChecked: boolean = false): Promise<void> {
    await page.fill(SELETORES_CSS.CAMPO_DESCRICAO, descricao);
    await page.selectOption(SELETORES_CSS.CAMPO_TIPO, tipo);
    
    if (dataLimite) {
        await page.fill(SELETORES_CSS.CAMPO_DATA_LIMITE, dataLimite);
    }
    if (sticChecked) {
        await page.check(SELETORES_CSS.CHECKBOX_STIC);
    }
}

/**
 * Selecionar primeira unidade disponível
 */
export async function selecionarPrimeiraUnidade(page: Page): Promise<void> {
    await page.waitForSelector('input[type="checkbox"]');
    const primeiroCheckbox = page.locator('input[type="checkbox"]').first();
    await primeiroCheckbox.click();
}

/**
 * Clicar no primeiro processo da tabela
 */
export async function clicarPrimeiroProcessoTabela(page: Page): Promise<void> {
    const primeiraLinha = page.locator('table tbody tr').first();
    await primeiraLinha.click();
}

/**
 * Criar processo completo com dados básicos
 */
export async function criarProcessoCompleto(page: Page, descricao: string, tipo: string, dataLimite?: string): Promise<void> {
    await preencherFormularioProcesso(page, descricao, tipo, dataLimite);
    await selecionarPrimeiraUnidade(page);
    await page.getByRole('button', { name: TEXTOS.SALVAR }).click();
}

/**
 * Tentar salvar processo sem preencher dados
 */
export async function tentarSalvarProcessoVazio(page: Page): Promise<void> {
    await page.getByRole('button', { name: TEXTOS.SALVAR }).click();
}

/**
 * Criar processo apenas com descrição e tipo (sem unidades)
 */
export async function criarProcessoSemUnidades(page: Page, descricao: string, tipo: string): Promise<void> {
    await preencherFormularioProcesso(page, descricao, tipo);
    await page.getByRole('button', { name: TEXTOS.SALVAR }).click();
}

/**
 * Navegar para processo específico na tabela
 */
export async function navegarParaProcessoNaTabela(page: Page, descricaoProcesso: string): Promise<void> {
    await page.getByText(descricaoProcesso).click();
}

/**
 * Editar descrição de processo existente
 */
export async function editarDescricaoProcesso(page: Page, novaDescricao: string): Promise<void> {
    await page.getByLabel('Descrição').fill(novaDescricao);
    await page.getByRole('button', { name: TEXTOS.SALVAR }).click();
}

/**
 * Criar processo de mapeamento completo (versão específica para CDU-04)
 */
export async function criarProcessoMapeamentoCompleto(page: Page, descricao: string, dataLimite: string): Promise<void> {
    await page.getByLabel('Descrição').fill(descricao);
    await page.getByLabel('Tipo').selectOption('Mapeamento');
    await page.getByLabel('Data limite').fill(dataLimite);
    
    await page.waitForSelector('input[type="checkbox"]');
    const primeiroCheckbox = page.locator('input[type="checkbox"]').first();
    await primeiroCheckbox.check();
}

/**
 * Finaliza um processo com confirmação
 */
export async function finalizarProcesso(page: Page): Promise<void> {
  await page.click(`button:has-text("${TEXTOS.FINALIZAR_PROCESSO}")`);
  await expect(page.locator(SELETORES_CSS.MODAL_VISIVEL)).toBeVisible();
  await page.click(`button:has-text("${TEXTOS.CONFIRMAR}")`);
}

/**
 * Disponibiliza cadastro com confirmação
 */
export async function disponibilizarCadastro(page: Page): Promise<void> {
  await page.click(`button:has-text("${TEXTOS.DISPONIBILIZAR}")`);
  await expect(page.locator(SELETORES_CSS.MODAL_VISIVEL)).toBeVisible();
  await page.click(`button:has-text("${TEXTOS.CONFIRMAR}")`);
}

/**
 * Homologa um cadastro/mapa
 */
export async function homologarItem(page: Page): Promise<void> {
  await page.click(`button:has-text("${TEXTOS.HOMOLOGAR}")`);
  await expect(page.locator(SELETORES_CSS.MODAL_VISIVEL)).toBeVisible();
  const confirmarBtn = page.locator(`button:has-text("${TEXTOS.CONFIRMAR}")`).last();
  await confirmarBtn.click();
}

/**
 * Devolve para ajustes
 */
export async function devolverParaAjustes(page: Page, observacao?: string): Promise<void> {
  await page.click(`button:has-text("${TEXTOS.DEVOLVER}")`);
  await expect(page.locator(SELETORES_CSS.MODAL_VISIVEL)).toBeVisible();
  
  if (observacao) {
    const textarea = page.locator('textarea').first();
    await textarea.waitFor({ state: 'visible' });
    await textarea.fill(observacao);
  }
  
  await page.click(`button:has-text("${TEXTOS.CONFIRMAR}")`);
}

/**
 * Clica no botão de iniciar processo
 */
export async function clicarBotaoIniciarProcesso(page: Page): Promise<void> {
    await page.getByRole('button', { name: TEXTOS.INICIAR_PROCESSO }).click();
}

/**
 * Clica em um processo específico na tabela do painel
 */
export async function clicarProcessoNaTabela(page: Page, nomeProcesso: string): Promise<void> {
    await page.waitForSelector(`table[data-testid="${SELETORES.TABELA_PROCESSOS}"]`); // Espera a tabela carregar
    const processo = page.locator(`table[data-testid="${SELETORES.TABELA_PROCESSOS}"] tbody tr`).filter({hasText: nomeProcesso});
    await processo.click();
}

/**
 * Inicia um processo com confirmação
 */
export async function iniciarProcesso(page: Page): Promise<void> {
  await page.getByText(TEXTOS.INICIAR_PROCESSO).click();
  await page.waitForSelector(SELETORES_CSS.MODAL_VISIVEL);
  await page.getByText(TEXTOS.CONFIRMAR).click();
}

/**
 * Remover processo com confirmação
 */
export async function removerProcessoComConfirmacao(page: Page): Promise<void> {
  await page.getByRole('button', { name: TEXTOS.REMOVER }).click();
  await page.getByRole('button', { name: TEXTOS.CONFIRMAR }).click();
}

/**
 * Cancelar remoção de processo
 */
export async function cancelarRemocaoProcesso(page: Page): Promise<void> {
  await page.getByRole('button', { name: TEXTOS.REMOVER }).click();
  await page.getByRole('button', { name: TEXTOS.CANCELAR }).click();
}

/**
 * Confirmar inicialização de processo
 */
export async function confirmarInicializacaoProcesso(page: Page): Promise<void> {
  await page.getByRole('button', { name: TEXTOS.INICIAR_PROCESSO }).click();
  await page.getByRole('button', { name: TEXTOS.CONFIRMAR }).click();
}

/**
 * Remover processo com confirmação usando botão de danger no modal
 */
export async function removerProcessoConfirmandoNoModal(page: Page): Promise<void> {
  await page.getByRole('button', { name: TEXTOS.REMOVER }).click();
  await page.locator('.modal.show .btn-danger').click();
}

/**
 * Clica em uma unidade específica na tabela de detalhes do processo.
 */
export async function clicarUnidadeNaTabelaDetalhes(page: Page, nomeUnidade: string): Promise<void> {
    const unidadeRow = page.locator(SELETORES.LINHA_TABELA_ARVORE).filter({hasText: nomeUnidade}).first();
    await unidadeRow.click();
}
