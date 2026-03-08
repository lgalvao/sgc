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
 */
test.describe('CDU-24 - Disponibilizar mapas em bloco', () => {
    const UNIDADE_1 = 'SECAO_221';

    test('Fluxo completo: De criação de processo à disponibilização em bloco', async ({page}) => {
        const timestamp = Date.now();
        const descProcesso = `Mapeamento CDU-24 ${timestamp}`;
        const atividade1 = `Atividade Mapa ${timestamp}`;
        const competencia1 = `Competência Mapa ${timestamp}`;

        // 1. ADMIN cria e inicia processo
        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
        await criarProcesso(page, {
            descricao: descProcesso,
            tipo: 'MAPEAMENTO',
            diasLimite: 30,
            unidade: UNIDADE_1,
            expandir: ['SECRETARIA_2', 'COORD_22']
        });

        const linhaProcesso = page.getByTestId('tbl-processos').locator('tr', {has: page.getByText(descProcesso)});
        await linhaProcesso.click();
        const processoId = Number.parseInt(new RegExp(/\/processo(?:\/cadastro)?\/(\d+)/).exec(page.url())?.[1] || '0');

        await page.getByTestId('btn-processo-iniciar').click();
        await page.getByTestId('btn-iniciar-processo-confirmar').click();
        await verificarPaginaPainel(page);

        // 2. CHEFE disponibiliza cadastro
        await login(page, USUARIOS.CHEFE_SECAO_221.titulo, USUARIOS.CHEFE_SECAO_221.senha);
        await page.getByTestId('tbl-processos').getByText(descProcesso).first().click();
        await navegarParaAtividades(page);
        await adicionarAtividade(page, atividade1);
        await adicionarConhecimento(page, atividade1, 'Conhecimento Mapa 1');
        await page.getByTestId('btn-cad-atividades-disponibilizar').click();
        await page.getByTestId('btn-confirmar-disponibilizacao').click();
        await verificarPaginaPainel(page);

        // 3. GESTORES aceitam cadastro
        // Gestor COORD_22
        await login(page, USUARIOS.GESTOR_COORD_22.titulo, USUARIOS.GESTOR_COORD_22.senha);
        await acessarSubprocessoGestor(page, descProcesso, UNIDADE_1);
        await navegarParaAtividadesVisualizacao(page);
        await aceitarCadastroMapeamento(page);

        // Gestor SECRETARIA_2
        await loginComPerfil(page, USUARIOS.CHEFE_SECRETARIA_2.titulo, USUARIOS.CHEFE_SECRETARIA_2.senha, 'GESTOR - SECRETARIA_2');
        await acessarSubprocessoGestor(page, descProcesso, UNIDADE_1);
        await navegarParaAtividadesVisualizacao(page);
        await aceitarCadastroMapeamento(page);

        // 4. ADMIN homologa, cria competência e disponibiliza em bloco
        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
        await page.getByTestId('tbl-processos').getByText(descProcesso).first().click();
        await navegarParaSubprocesso(page, UNIDADE_1);
        await page.getByTestId('card-subprocesso-atividades-vis').click();
        await page.getByTestId('btn-acao-analisar-principal').click();
        await page.getByTestId('btn-aceite-cadastro-confirmar').click();

        await navegarParaMapa(page);
        await criarCompetencia(page, competencia1, [atividade1]);

        // Retornar para tela do processo para ação em bloco
        await page.goto('/painel');
        await page.getByTestId('tbl-processos').getByText(descProcesso).first().click();
        
        // Validação da UI da ação em bloco
        const btnDisponibilizar = page.getByRole('button', {name: /Disponibilizar.*Bloco/i}).first();
        await expect(btnDisponibilizar).toBeEnabled();
        await btnDisponibilizar.click();

        const modal = page.locator('#modal-acao-bloco');
        await expect(modal.getByLabel(/Data Limite/i)).toBeVisible();

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
