
import { expect, Page } from '@playwright/test';
import { SELETORES, TEXTOS } from '../dados';

/**
 * Representa a página de Criação e Edição de Processo.
 * Contém ações e verificações que podem ser realizadas nesta página.
 */
export class PaginaProcesso {
    constructor(private readonly page: Page) {}

    // --- Ações ---

    async preencherDescricao(descricao: string) {
        await this.page.locator(SELETORES.CAMPO_DESCRICAO).fill(descricao);
    }

    async selecionarTipoProcesso(tipo: 'MAPEAMENTO' | 'REVISAO' | 'DIAGNOSTICO') {
        await this.page.locator(SELETORES.CAMPO_TIPO).selectOption(tipo);
    }

    async preencherDataLimite(data: string) {
        await this.page.locator(SELETORES.CAMPO_DATA_LIMITE).fill(data);
    }

    async selecionarUnidadesPorSigla(siglas: string[]) {
        for (const sigla of siglas) {
            const seletorCheckbox = `#chk-${sigla}`;
            await this.page.waitForSelector(seletorCheckbox, { state: 'visible' });
            await this.page.locator(seletorCheckbox).check();
        }
    }

    async clicarBotaoSalvar() {
        await this.page.getByRole('button', { name: /Salvar/i }).click();
    }
    
    async clicarBotaoRemover() {
        await this.page.getByRole('button', { name: /^Remover$/i }).click();
    }
    
    // --- Ações em Modais ---

    async cancelarNoModal() {
        const modal = this.page.locator('.modal.show');
        await modal.getByRole('button', { name: TEXTOS.CANCELAR }).click();
    }
    
    async confirmarNoModal() {
        const modal = this.page.locator('.modal.show');
        await modal.getByRole('button', { name: TEXTOS.CONFIRMAR }).click();
    }

    async confirmarRemocaoNoModal() {
        const modal = this.page.locator('.modal.show');
        await modal.getByRole('button', { name: TEXTOS.REMOVER }).click();
    }

    // --- Verificações ---

    async verificarPaginaDeCadastro() {
        await expect(this.page).toHaveURL(/.*\/processo\/cadastro/);
        await expect(this.page.getByRole('heading', { name: 'Cadastro de Processo' })).toBeVisible();
    }
    
    async verificarPaginaDeEdicao() {
        await expect(this.page).toHaveURL(/\/processo\/cadastro\?codProcesso=\d+$/);
        await expect(this.page.getByRole('heading', { name: 'Cadastro de Processo' })).toBeVisible();
    }
    
    async verificarPermanenciaNaPaginaDeEdicao(descricaoEsperada: string) {
        await this.verificarPaginaDeEdicao();
        await this.verificarValorDescricao(descricaoEsperada);
    }
    
    async verificarValorDescricao(valor: string) {
        await expect(this.page.locator(SELETORES.CAMPO_DESCRICAO)).toHaveValue(valor);
    }
    
    async verificarBotaoRemoverVisivel(visivel: boolean = true) {
        const expectable = expect(this.page.getByRole('button', { name: /^Remover$/i }));
        if (visivel) {
            await expectable.toBeVisible();
        } else {
            await expectable.not.toBeVisible();
        }
    }
    
    async verificarDialogoConfirmacaoRemocao(descricaoProcesso: string) {
        await expect(this.page.getByText(`Remover o processo '${descricaoProcesso}'? Esta ação não poderá ser desfeita.`)).toBeVisible();
    }
    
    async verificarModalFechado() {
        await expect(this.page.locator(SELETORES.MODAL_VISIVEL)).not.toBeVisible();
    }

    async verificarCheckboxUnidadeMarcado(sigla: string) {
        await expect(this.page.locator(`#chk-${sigla}`)).toBeChecked();
    }
   
    async verificarValorDataLimite(data: string) {
        await expect(this.page.locator(SELETORES.CAMPO_DATA_LIMITE)).toHaveValue(data);
    }

    async verificarTipoProcesso(valor?: 'MAPEAMENTO' | 'REVISAO' | 'DIAGNOSTICO') {
        const selectTipo = this.page.locator(SELETORES.CAMPO_TIPO);
        await expect(selectTipo).toBeVisible();
        if (valor) {
            await expect(selectTipo).toHaveValue(valor);
        }
    }
}
