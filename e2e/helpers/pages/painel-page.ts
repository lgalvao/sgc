import { expect, Page } from '@playwright/test';
import { SELETORES, URLS, USUARIOS, TEXTOS } from '../dados';

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
    async aguardarProcessoNoPainel(descricaoProcesso: string | RegExp) {
        await this.verificarUrlDoPainel();
        await expect(this.page.locator(SELETORES.TABELA_PROCESSOS)).toBeVisible();
        await expect(this.page.locator(SELETORES.TABELA_PROCESSOS).locator('tr').filter({ hasText: descricaoProcesso })).toBeVisible();
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
    async clicarProcessoNaTabela(nomeProcesso: string | RegExp) {
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
    async verificarProcessoNaoVisivel(descricao: string | RegExp) {
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

    async verificarTituloProcessos() {
        await expect(this.page.getByTestId('titulo-processos')).toBeVisible();
    }

    async verificarTituloAlertas() {
        await expect(this.page.getByTestId('titulo-alertas')).toBeVisible();
    }

    async verificarTabelaProcessosVisivel() {
        await expect(this.page.locator(SELETORES.TABELA_PROCESSOS)).toBeVisible();
    }

    async verificarTabelaAlertasVisivel() {
        await expect(this.page.locator(SELETORES.TABELA_ALERTAS)).toBeVisible();
    }

    async verificarColunasTabelaProcessos() {
        const tabela = this.page.locator(SELETORES.TABELA_PROCESSOS);
        await expect(tabela.getByRole('columnheader', { name: 'Descrição' })).toBeVisible();
        await expect(tabela.getByRole('columnheader', { name: 'Tipo' })).toBeVisible();
        await expect(tabela.getByRole('columnheader', { name: 'Situação' })).toBeVisible();
    }

    async verificarColunasTabelaAlertas() {
        const tabelaAlertas = this.page.locator(SELETORES.TABELA_ALERTAS);
        await expect(tabelaAlertas).toContainText(TEXTOS.COLUNA_DATA_HORA);
        await expect(tabelaAlertas).toContainText(TEXTOS.COLUNA_PROCESSO);
        await expect(tabelaAlertas).toContainText(TEXTOS.COLUNA_ORIGEM);
    }

    async verificarBotaoCriarProcesso(visivel: boolean) {
        const botao = this.page.locator(SELETORES.BTN_CRIAR_PROCESSO).first();
        if (visivel) {
            await expect(botao).toBeVisible();
        } else {
            await expect(botao).not.toBeVisible();
        }
    }

    async verificarQuantidadeProcessosNaTabela(quantidade: number) {
        const tabela = this.page.getByTestId('tabela-processos');
        const linhas = tabela.locator('tbody tr');
        await expect(linhas).toHaveCount(quantidade);
    }

    async verificarAlertasOrdenadosPorDataHora() {
        const linhasAlertas = this.page.locator(`${SELETORES.TABELA_ALERTAS} tbody tr`);
        const valoresDatas = await linhasAlertas.evaluateAll(linhas =>
            linhas.map(linha => {
                const textoData = (linha.children[0] as HTMLElement).innerText.trim();
                const [data, hora] = textoData.split(' ');
                const [dia, mes, ano] = data.split('/');
                return new Date(`${ano}-${mes}-${dia}T${hora}`).getTime();
            })
        );
        const valoresOrdenados = [...valoresDatas].sort((a, b) => b - a);
        expect(valoresDatas).toEqual(valoresOrdenados);
    }
}