import {expect, test} from './fixtures/complete-fixtures.js';
import {criarProcesso} from './helpers/helpers-processos.js';
import {
    adicionarAtividade,
    adicionarConhecimento,
    navegarParaAtividades,
    navegarParaAtividadesVisualizacao
} from './helpers/helpers-atividades.js';
import {criarCompetencia, navegarParaMapa} from './helpers/helpers-mapas.js';
import {navegarParaSubprocesso, verificarPaginaPainel} from './helpers/helpers-navegacao.js';
import {aceitarCadastroMapeamento, acessarSubprocessoGestor} from './helpers/helpers-analise.js';
import {login, loginComPerfil, USUARIOS} from './helpers/helpers-auth.js';

/**
 * CDU-24 - Disponibilizar mapas de competências em bloco
 * 
 * Ator: ADMIN
 * 
 * Pré-condições:
 * - Processo de mapeamento com ao menos uma unidade com subprocesso na situação 'Mapa criado'
 * 
 * Fluxo principal:
 * 1. ADMIN acessa processo de mapeamento em andamento
 * 2. Sistema mostra tela Detalhes do processo
 * 3. Sistema identifica unidades com mapas criados/ajustados
 * 4. Sistema mostra botão 'Disponibilizar mapas em bloco'
 * 5. Sistema abre modal com lista de unidades e campo de data limite
 * 6. ADMIN confirma
 * 7. Sistema executa disponibilização para cada unidade selecionada
 */
test.describe.serial('CDU-24 - Disponibilizar mapas em bloco', () => {
    const UNIDADE_1 = 'SECAO_221';

    const timestamp = Date.now();
    const descProcesso = `Mapeamento CDU-24 ${timestamp}`;
    let processoId: number;

    const atividade1 = `Atividade Mapa ${timestamp}`;
    const competencia1 = `Competência Mapa ${timestamp}`;

    // ========================================================================
    // PREPARAÇÃO - Criar processo com mapa criado
    // ========================================================================

    test('Preparacao 1: Admin cria e inicia processo', async ({page, autenticadoComoAdmin, cleanupAutomatico}) => {
        

        await criarProcesso(page, {
            descricao: descProcesso,
            tipo: 'MAPEAMENTO',
            diasLimite: 30,
            unidade: UNIDADE_1,
            expandir: ['SECRETARIA_2', 'COORD_22']
        });

        const linhaProcesso = page.getByTestId('tbl-processos').locator('tr', {has: page.getByText(descProcesso)});
        await linhaProcesso.click();

        processoId = Number.parseInt(new RegExp(/\/processo(?:\/cadastro)?\/(\d+)/).exec(page.url())?.[1] || '0');
        if (processoId > 0) cleanupAutomatico.registrar(processoId);

        await page.getByTestId('btn-processo-iniciar').click();
        await page.getByTestId('btn-iniciar-processo-confirmar').click();

        await verificarPaginaPainel(page);
    });

    test('Preparacao 2: Chefe disponibiliza cadastro', async ({page, autenticadoComoChefeSecao221}) => {
        

        await page.getByTestId('tbl-processos').getByText(descProcesso).first().click();
        await navegarParaAtividades(page);

        await adicionarAtividade(page, atividade1);
        await adicionarConhecimento(page, atividade1, 'Conhecimento Mapa 1');

        await page.getByTestId('btn-cad-atividades-disponibilizar').click();
        await page.getByTestId('btn-confirmar-disponibilizacao').click();

        await verificarPaginaPainel(page);
    });

    test('Preparacao 2a: Gestor COORD_22 aceita cadastro', async ({page}) => {
        await login(page, USUARIOS.GESTOR_COORD_22.titulo, USUARIOS.GESTOR_COORD_22.senha);
        await acessarSubprocessoGestor(page, descProcesso, UNIDADE_1);
        await navegarParaAtividadesVisualizacao(page);
        await aceitarCadastroMapeamento(page);
    });

    test('Preparacao 2b: Gestor SECRETARIA_2 aceita cadastro', async ({page}) => {
        await loginComPerfil(page, USUARIOS.CHEFE_SECRETARIA_2.titulo, USUARIOS.CHEFE_SECRETARIA_2.senha, 'GESTOR - SECRETARIA_2');
        await acessarSubprocessoGestor(page, descProcesso, UNIDADE_1);
        await navegarParaAtividadesVisualizacao(page);
        await aceitarCadastroMapeamento(page);
    });

    test('Preparacao 3: Admin homologa cadastro e cria competências', async ({page, autenticadoComoAdmin}) => {
        

        await page.getByTestId('tbl-processos').getByText(descProcesso).first().click();
        await navegarParaSubprocesso(page, UNIDADE_1);
        await page.getByTestId('card-subprocesso-atividades-vis').click();
        await page.getByTestId('btn-acao-analisar-principal').click();
        await page.getByTestId('btn-aceite-cadastro-confirmar').click();

        await expect(page).toHaveURL(/\/processo\/\d+\/\w+$/);

        // Criar competências
        await navegarParaMapa(page);
        await criarCompetencia(page, competencia1, [atividade1]);

        // Verificar que mapa foi criado (situação 'Mapa criado')
        await page.goto('/painel');
        await page.getByTestId('tbl-processos').getByText(descProcesso).first().click();
        await navegarParaSubprocesso(page, UNIDADE_1);
        await expect(page.getByTestId('subprocesso-header__txt-situacao'))
            .toHaveText(/Mapa criado/i);
    });

    // ========================================================================
    // TESTES PRINCIPAIS - CDU-24
    // ========================================================================

    test('Cenario 1: ADMIN visualiza botão Disponibilizar Mapas em Bloco', async ({page, autenticadoComoAdmin}) => {
        await page.getByTestId('tbl-processos').getByText(descProcesso).first().click();
        await expect(page.getByRole('heading', {name: /Unidades participantes/i})).toBeVisible();
        const btnDisponibilizar = page.getByRole('button', {name: /Disponibilizar.*Bloco/i}).first();
        await expect(btnDisponibilizar).toBeVisible();
        await expect(btnDisponibilizar).toBeEnabled();
    });

    test('Cenario 2: Modal de disponibilização inclui campo de data limite', async ({page, autenticadoComoAdmin}) => {
        await page.getByTestId('tbl-processos').getByText(descProcesso).first().click();
        await expect(page.getByRole('heading', {name: /Unidades participantes/i})).toBeVisible();
        const btnDisponibilizar = page.getByRole('button', {name: /Disponibilizar.*Bloco/i}).first();
        await btnDisponibilizar.click();

        const modal = page.locator('#modal-acao-bloco');
        await expect(modal).toHaveClass(/show/);
        await expect(modal.getByText(/Disponibilizar Mapas em Bloco/i)).toBeVisible();
        await expect(modal.getByText(/Selecione as unidades/i)).toBeVisible();
        await expect(modal.locator('table')).toBeVisible();
        await expect(modal.getByLabel(/Data Limite/i)).toBeVisible();
        await expect(modal.getByRole('button', {name: /Cancelar/i})).toBeVisible();
        await expect(modal.getByRole('button', {name: /Disponibilizar Selecionados/i})).toBeVisible();
        await modal.getByRole('button', {name: /Cancelar/i}).click();
        await expect(modal).not.toHaveClass(/show/);
    });

    test('Cenario 3: ADMIN confirma disponibilização em bloco', async ({page, autenticadoComoAdmin}) => {
        await page.getByTestId('tbl-processos').getByText(descProcesso).first().click();
        const btnDisponibilizar = page.getByRole('button', {name: /Disponibilizar.*Bloco/i}).first();
        await btnDisponibilizar.click();

        const modal = page.locator('#modal-acao-bloco');
        await expect(modal).toHaveClass(/show/);

        const data = new Date();
        data.setDate(data.getDate() + 10);
        const yyyy = data.getFullYear();
        const mm = String(data.getMonth() + 1).padStart(2, '0');
        const dd = String(data.getDate()).padStart(2, '0');
        await modal.getByLabel(/Data Limite/i).fill(`${yyyy}-${mm}-${dd}`);

        await modal.getByRole('button', {name: /Disponibilizar Selecionados/i}).click();
        await expect(page.getByText(/Mapas de competências disponibilizados em bloco/i).first()).toBeVisible();
        await expect(page).toHaveURL(/\/painel/);
    });
});
