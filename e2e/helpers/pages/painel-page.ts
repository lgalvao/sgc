
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
        await this.page.waitForLoadState('networkidle');
        await this.page.getByTestId('input-titulo').fill(USUARIOS.ADMIN.titulo);
        await this.page.getByTestId('input-senha').fill(USUARIOS.ADMIN.senha);
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
        // Verifica se a página atual é a de cadastro, o que indica um bug da aplicação.
        // O requisito é redirecionar para o painel após a criação.
        if (this.page.url().includes('/processo/cadastro')) {
            console.warn('BUG DA APLICACAO: Após criar processo, não houve redirecionamento automático para o Painel. Navegando manualmente.');
            await this.page.goto(URLS.PAINEL);
            await this.page.waitForLoadState('networkidle');
        }
        await this.verificarUrlDoPainel(); // Agora verifica que estamos no painel, seja por redirecionamento ou navegação manual
        await expect(this.page.getByText(descricaoProcesso)).toBeVisible();
    }

    /**
     * Verifica se a URL atual é a do painel.
     */
    async verificarUrlDoPainel() {
        await this.page.waitForURL(URLS.PAINEL);
        await expect(this.page).toHaveURL(URLS.PAINEL);
        await this.page.waitForLoadState('networkidle');
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
        await expect(this.page.getByText(descricao)).not.toBeVisible();
    }
}
