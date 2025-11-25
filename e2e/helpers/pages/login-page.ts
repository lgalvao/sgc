import { expect, Page } from '@playwright/test';
import { URLS, SELETORES, TEXTOS } from '../dados';

export class PaginaLogin {
    constructor(private readonly page: Page) {}

    async navegar() {
        await this.page.goto(URLS.LOGIN || '/login');
        await this.page.waitForLoadState('domcontentloaded');
    }

    async preencherCredenciais(usuario: string, senha_plana: string) {
        await this.page.getByTestId('input-titulo').fill(usuario);
        await this.page.getByTestId('input-senha').fill(senha_plana);
    }

    async clicarEntrar() {
        await this.page.locator(SELETORES.BTN_LOGIN).click();
    }

    async fazerLogin(usuario: string, senha_plana: string) {
        await this.preencherCredenciais(usuario, senha_plana);
        await this.clicarEntrar();
    }

    /**
     * Realiza o login e aguarda o redirecionamento para o painel.
     * Use este método para fluxos padrão de login (sem seleção de perfil).
     */
    async realizarLogin(usuario: string, senha_plana: string) {
        await this.navegar();
        await this.fazerLogin(usuario, senha_plana);
        await this.page.waitForURL(URLS.PAINEL);
        await this.page.waitForLoadState('domcontentloaded');
    }

    async selecionarPerfil(perfil: string) {
        const seletor = this.page.getByTestId('select-perfil-unidade');
        await seletor.waitFor({ state: 'visible' });
        await seletor.selectOption({ label: perfil });
    }

    async verificarSelecaoPerfilVisivel() {
        await expect(this.page.getByTestId('select-perfil-unidade')).toBeVisible();
    }

    async verificarErroLogin(mensagem: string = TEXTOS.ERRO_LOGIN_INVALIDO) {
        const notificacao = this.page.locator('[data-testid="notificacao-error"]');
        await expect(notificacao).toContainText(mensagem);
        await expect(notificacao).toBeVisible();
    }
}
