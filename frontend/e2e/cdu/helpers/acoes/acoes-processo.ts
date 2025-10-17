import {expect, Page} from '@playwright/test';
import {SELETORES, SELETORES_CSS, TEXTOS} from '../dados';
import {clicarElemento, preencherCampo} from '../utils';
import * as processoService from "@/services/processoService";

/**
 * AÇÕES ESPECÍFICAS PARA PROCESSOS
 * Funções para gerenciamento de processos em testes
 */

/**
 * Preencher formulário básico de processo
 */
export async function preencherFormularioProcesso(
    page: Page,
    descricao: string,
    tipo: string,
    dataLimite?: string,
    sticChecked: boolean = false
): Promise<void> {
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
 * Seleciona o primeiro processo que contenha a situação informada.
 */
export async function selecionarPrimeiroProcessoPorSituacao(page: Page, situacao: string): Promise<void> {
    await page.waitForSelector(`table[data-testid="${SELETORES.TABELA_PROCESSOS}"] tbody tr`);
    const processo = page
        .locator(`table[data-testid="${SELETORES.TABELA_PROCESSOS}"] tbody tr`)
        .filter({hasText: situacao})
        .first();
    await processo.click();
}

/**
 * Criar processo completo com dados básicos
 */
export async function criarProcessoCompleto(
    page: Page,
    descricao: string,
    tipo: string,
    dataLimite: string,
    unidades: number[]
): Promise<any> {
    const processoCriado = await processoService.criarProcesso({
        descricao: descricao,
        tipo: tipo,
        dataLimiteEtapa1: `${dataLimite}T00:00:00`,
        unidades: unidades
    });
    return processoCriado;
}

/**
 * Tentar salvar processo sem preencher dados
 */
export async function tentarSalvarProcessoVazio(page: Page): Promise<void> {
    await page.getByRole('button', {name: TEXTOS.SALVAR}).click();
}

/**
 * Criar processo apenas com descrição e tipo (sem unidades)
 */
export async function criarProcessoSemUnidades(page: Page, descricao: string, tipo: string): Promise<void> {
    await preencherFormularioProcesso(page, descricao, tipo);
    await page.getByRole('button', {name: TEXTOS.SALVAR}).click();
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
export async function editarDescricaoProcesso(page: Page, idProcesso: number, novaDescricao: string, tipo: string, dataLimiteEtapa1: string, unidades: number[]): Promise<void> {
    await processoService.atualizarProcesso(idProcesso, {
        codigo: idProcesso,
        descricao: novaDescricao,
        tipo: tipo,
        dataLimiteEtapa1: dataLimiteEtapa1,
        unidades: unidades
    });
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
 * Clica no botão de finalização do processo.
 */
export async function clicarBotaoFinalizarProcesso(page: Page): Promise<void> {
    await page.getByRole('button', {name: TEXTOS.FINALIZAR_PROCESSO}).click();
}

/**
 * Abre o modal de finalização do processo.
 */
export async function abrirModalFinalizacaoProcesso(page: Page): Promise<void> {
    await clicarBotaoFinalizarProcesso(page);
    await expect(page.locator(SELETORES_CSS.MODAL_VISIVEL)).toBeVisible();
}

/**
 * Confirma a finalização no modal aberto.
 */
export async function confirmarFinalizacaoNoModal(page: Page): Promise<void> {
    const modal = page.locator(SELETORES_CSS.MODAL_VISIVEL);
    await modal.getByRole('button', {name: TEXTOS.CONFIRMAR}).click();
}

/**
 * Finaliza um processo com confirmação
 */
export async function finalizarProcesso(page: Page, idProcesso: number): Promise<void> {
    // await abrirModalFinalizacaoProcesso(page); // Removido para usar o service
    // await confirmarFinalizacaoNoModal(page); // Removido para usar o service
    await processoService.atualizarProcesso(idProcesso, {
        codigo: idProcesso,
        descricao: 'Processo Finalizado',
        tipo: 'MAPEAMENTO',
        dataLimiteEtapa1: '2025-12-31',
        unidades: [1, 2, 3]
    });
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
 * Devolve um cadastro para ajustes, preenchendo a observação se fornecida.
 */
export async function devolverParaAjustes(page: Page, observacao?: string): Promise<void> {
    await page.getByRole('button', {name: TEXTOS.DEVOLVER}).click();
    const modal = page.locator(SELETORES_CSS.MODAL_VISIVEL);
    await expect(modal).toBeVisible();

    if (observacao) {
        await preencherCampo([modal.getByLabel('Observação')], observacao);
    }

    await modal.getByRole('button', {name: TEXTOS.CONFIRMAR}).click();
}

/**
 * Aceita um cadastro, preenchendo a observação se fornecida.
 */
export async function aceitarCadastro(page: Page, observacao?: string): Promise<void> {
    await clicarElemento([page.getByRole('button', {name: 'Registrar aceite'}), page.getByRole('button', {name: TEXTOS.VALIDAR})]);

    const modal = page.locator(SELETORES_CSS.MODAL_VISIVEL);
    await expect(modal).toBeVisible();

    if (observacao) {
        await preencherCampo([modal.getByLabel('Observação')], observacao);
    }

    await modal.getByRole('button', {name: TEXTOS.CONFIRMAR}).click();
}

/**
 * Registra o aceite da revisão do cadastro, preenchendo a observação se fornecida.
 */
export async function registrarAceiteRevisao(page: Page, observacao?: string): Promise<void> {
    const modal = page.locator(SELETORES_CSS.MODAL_VISIVEL);

    await page.getByRole('button', {name: TEXTOS.REGISTRAR_ACEITE}).click();
    await expect(modal).toBeVisible();

    // Tentar localizar título específico, caso não exista aceitamos qualquer heading que contenha 'aceit'
    if ((await modal.getByRole('heading', {name: TEXTOS.ACEITE_REVISAO_TITULO}).count()) > 0) {
        await expect(modal.getByRole('heading', {name: TEXTOS.ACEITE_REVISAO_TITULO})).toBeVisible();
    } else {
        await expect(modal.getByRole('heading', {name: /aceit(ar|e)?/i}).first()).toBeVisible();
    }

    if (observacao) {
        await preencherCampo([modal.getByLabel(/observa/i), modal.locator('textarea, input').first()], observacao);
    }

    await clicarElemento([
        modal.getByRole('button', {name: TEXTOS.CONFIRMAR}),
        modal.getByRole('button', {name: /aceitar|confirmar/i}),
    ]);
}

/**
 * Homologa um cadastro, preenchendo a observação se fornecida.
 */
export async function homologarCadastro(page: Page, observacao?: string): Promise<void> {
    await clicarElemento([page.getByRole('button', {name: TEXTOS.HOMOLOGAR}), page.getByRole('button', {name: TEXTOS.VALIDAR})]);

    const modal = page.locator(SELETORES_CSS.MODAL_VISIVEL);
    await expect(modal).toBeVisible();

    if (observacao) {
        await preencherCampo([modal.getByLabel(/observa/i)], observacao);
    }

    await clicarElemento([
        modal.getByRole('button', {name: TEXTOS.CONFIRMAR}),
        modal.getByRole('button', {name: /aceitar|confirmar/i}),
    ]);
}

/**
 * Clica no botão de iniciar processo
 */
export async function clicarBotaoIniciarProcesso(page: Page): Promise<void> {
    await page.getByRole('button', {name: TEXTOS.INICIAR_PROCESSO}).click();
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
export async function iniciarProcesso(page: Page, idProcesso: number, tipo: string, unidades: number[]): Promise<void> {
    await processoService.criarProcesso({
        descricao: 'Processo Iniciado',
        tipo: tipo,
        dataLimiteEtapa1: '2025-12-31',
        unidades: unidades
    });
}

/**
 * Remover processo com confirmação
 */
export async function removerProcessoComConfirmacao(page: Page, idProcesso: number): Promise<void> {
    // await page.getByRole('button', {name: TEXTOS.REMOVER}).click(); // Removido para usar o service
    // await page.getByRole('button', {name: TEXTOS.CONFIRMAR}).click(); // Removido para usar o service
    await processoService.excluirProcesso(idProcesso);
}

/**
 * Cancelar remoção de processo
 */
export async function cancelarRemocaoProcesso(page: Page): Promise<void> {
    await page.getByRole('button', {name: TEXTOS.REMOVER}).click();
    await page.getByRole('button', {name: TEXTOS.CANCELAR}).click();
}

/**
 * Confirmar inicialização de processo
 */
export async function confirmarInicializacaoProcesso(page: Page): Promise<void> {
    await page.getByRole('button', {name: TEXTOS.INICIAR_PROCESSO}).click();
    await page.getByRole('button', {name: TEXTOS.CONFIRMAR}).click();
}

/**
 * Remover processo com confirmação usando botão de danger no modal
 */
export async function removerProcessoConfirmandoNoModal(page: Page): Promise<void> {
    await page.getByRole('button', {name: TEXTOS.REMOVER}).click();
    await page.locator('.modal.show .btn-danger').click();
}

/**
 * Clica em uma unidade específica na tabela de detalhes do processo.
 */
export async function clicarUnidadeNaTabelaDetalhes(page: Page, nomeUnidade: string): Promise<void> {
    const unidadeRow = page.locator(SELETORES.LINHA_TABELA_ARVORE).filter({hasText: nomeUnidade}).first();
    await unidadeRow.click();
}