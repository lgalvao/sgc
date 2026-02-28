import {expect, test} from './fixtures/complete-fixtures.js';
import {login, loginComPerfil, USUARIOS} from './helpers/helpers-auth.js';
import {criarProcesso} from './helpers/helpers-processos.js';
import {
    adicionarAtividade,
    adicionarConhecimento,
    navegarParaAtividades,
    navegarParaAtividadesVisualizacao
} from './helpers/helpers-atividades.js';
import {
    aceitarCadastroMapeamento,
    acessarSubprocessoGestor,
    homologarCadastroMapeamento
} from './helpers/helpers-analise.js';
import {navegarParaSubprocesso, verificarPaginaPainel} from './helpers/helpers-navegacao.js';

/**
 * CDU-32 - Reabrir cadastro
 *
 * Ator: ADMIN
 *
 * Pré-condições:
 * - Subprocesso com cadastro já disponibilizado ou aceito
 */
test.describe.serial('CDU-32 - Reabrir cadastro', () => {
    const UNIDADE_1 = 'SECAO_221';

    const timestamp = Date.now();
    const descProcesso = `Mapeamento CDU-32 ${timestamp}`;
    let processoId: number;

    const atividade1 = `Atividade Reabrir ${timestamp}`;


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
        await adicionarConhecimento(page, atividade1, 'Conhecimento Reabrir 1');

        await page.getByTestId('btn-cad-atividades-disponibilizar').click();
        await page.getByTestId('btn-confirmar-disponibilizacao').click();

        await verificarPaginaPainel(page);
    });

    test('Preparacao 3: Gestores e ADMIN aceitam e homologam cadastro', async ({
                                                                                   page,
                                                                                   autenticadoComoGestorCoord22
                                                                               }) => {
        await acessarSubprocessoGestor(page, descProcesso, UNIDADE_1);
        await navegarParaAtividadesVisualizacao(page);
        await aceitarCadastroMapeamento(page, 'Aceite intermediário COORD_22');

        await loginComPerfil(page, USUARIOS.CHEFE_SECRETARIA_2.titulo, USUARIOS.CHEFE_SECRETARIA_2.senha, 'GESTOR - SECRETARIA_2');
        await acessarSubprocessoGestor(page, descProcesso, UNIDADE_1);
        await navegarParaAtividadesVisualizacao(page);
        await aceitarCadastroMapeamento(page, 'Aceite intermediário SECRETARIA_2');

        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
        await page.getByTestId('tbl-processos').getByText(descProcesso).first().click();
        await navegarParaSubprocesso(page, UNIDADE_1);
        await page.getByTestId('card-subprocesso-atividades-vis').click();
        await homologarCadastroMapeamento(page);
    });


    test('Cenários CDU-32: ADMIN reabre cadastro', async ({page, autenticadoComoAdmin}) => {
        // Cenario 1 & 2: Navegação e visualização do botão
        await page.getByTestId('tbl-processos').getByText(descProcesso).first().click();
        await navegarParaSubprocesso(page, UNIDADE_1);

        await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Cadastro homologado/i);

        const btnReabrir = page.getByTestId('btn-reabrir-cadastro');
        await expect(btnReabrir).toBeVisible();
        await expect(btnReabrir).toBeEnabled();

        // Cenario 3: Abrir modal e cancelar
        await btnReabrir.click();
        const modal = page.getByRole('dialog');
        await expect(modal).toBeVisible();
        await expect(modal.getByText(/Reabrir cadastro/i)).toBeVisible();
        await modal.getByRole('button', {name: /Cancelar/i}).click();
        await expect(modal).toBeHidden();

        // Cenario 4: Botão confirmar desabilitado sem justificativa
        await btnReabrir.click();
        await expect(page.getByTestId('btn-confirmar-reabrir')).toBeDisabled();
        await page.getByTestId('inp-justificativa-reabrir').fill('Justificativa de teste');
        await expect(page.getByTestId('btn-confirmar-reabrir')).toBeEnabled();

        // Cenario 5: Confirmar reabertura
        await page.getByTestId('btn-confirmar-reabrir').click();

        await expect(page.getByText(/Cadastro reaberto/i).first()).toBeVisible();
        await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Em andamento/i);
        await expect(page.getByTestId('tbl-movimentacoes')).toContainText(/Reabertura de cadastro/i);
    });
});
