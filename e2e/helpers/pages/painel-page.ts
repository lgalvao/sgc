
import { expect, Page } from '@playwright/test';
import { SELETORES, URLS, USUARIOS } from '../dados';

/**
 * Representa a página do Painel (Dashboard)
 * Contém ações e verificações que podem ser realizadas nesta página.
 */
export class PaginaPainel {
    constructor(private readonly page: Page) {}

    /**
     * Realiza o login como administrador.
     */
    async loginComoAdmin() {
        await this.page.goto('/login');
        await this.page.waitForLoadState('domcontentloaded');
        await this.page.getByTestId('input-titulo').fill(USUARIOS.ADMIN.titulo);
        await this.page.getByTestId('input-senha').fill(USUARIOS.ADMIN.senha);
        await this.page.locator(SELETORES.BTN_LOGIN).click();
        await this.verificarUrlDoPainel();
        await this.page.waitForSelector(SELETORES.BTN_CRIAR_PROCESSO, { state: 'visible' });
    }

    /**
     * Navega para a página de criação de processo.
     */
    async irParaCriacaoDeProcesso() {
        await this.page.waitForSelector(SELETORES.BTN_CRIAR_PROCESSO);
        await this.page.click(SELETORES.BTN_CRIAR_PROCESSO);
        await this.page.waitForURL(/\/processo\/cadastro/);
    }
    
    /**
     * Aguarda que um processo apareça na tabela de processos.
     * @param descricaoProcesso A descrição do processo.
     */
    async aguardarProcessoNoPainel(descricaoProcesso: string) {
        await this.verificarUrlDoPainel();
        await expect(this.page.getByText(descricaoProcesso)).toBeVisible();
    }
    
    /**
     * Verifica se a URL atual é a do painel.
     */
    async verificarUrlDoPainel() {
        await this.page.waitForURL(URLS.PAINEL);
        await expect(this.page).toHaveURL(URLS.PAINEL);
        await this.page.waitForLoadState('domcontentloaded');
    }

    /**
     * Clica em um processo na tabela de processos para abrir a página de edição/detalhes.
     * @param nomeProcesso O nome do processo.
     */
    async clicarProcessoNaTabela(nomeProcesso: string) {
        // Aguarda a tabela carregar para evitar cliques prematuros
        await this.page.waitForSelector(`${SELETORES.TABELA_PROCESSOS} tbody tr`);
        const processo = this.page.locator(`${SELETORES.TABELA_PROCESSOS} tbody tr`).filter({ hasText: nomeProcesso });
        await processo.click();
        await this.page.waitForURL(/\/processo\/cadastro\?codProcesso=\d+/);
    }
    
    /**
     * Verifica se um processo não está visível no painel.
     * @param descricao A descrição do processo.
     */
    async verificarProcessoNaoVisivel(descricao: string) {
        const tabela = this.page.locator(SELETORES.TABELA_PROCESSOS);
        await expect(tabela.getByText(descricao)).not.toBeVisible();
    }

    async verificarPainelAdminVisivel() {
        await this.verificarUrlDoPainel();
        await expect(this.page.getByTitle('Configurações do sistema')).toBeVisible();
    }

    async verificarPainelChefeVisivel() {
        await this.verificarUrlDoPainel();
        await expect(this.page.getByTitle('Configurações do sistema')).not.toBeVisible();
    }
}
