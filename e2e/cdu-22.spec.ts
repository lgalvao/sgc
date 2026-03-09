import {expect, test} from './fixtures/complete-fixtures.js';
import {criarProcesso} from './helpers/helpers-processos.js';
import {adicionarAtividade, adicionarConhecimento, navegarParaAtividades} from './helpers/helpers-atividades.js';
import {verificarPaginaPainel} from './helpers/helpers-navegacao.js';
import {login, USUARIOS} from './helpers/helpers-auth.js';

/**
 * CDU-22 - Aceitar cadastros em bloco
 *
 * Ator: GESTOR
 *
 * Pré-condições:
 * - Processo de mapeamento ou revisão com unidades subordinadas
 * - Subprocessos na situação 'Cadastro disponibilizado'
 *
 * Fluxo principal:
 * 1. No Painel, GESTOR acessa processo em andamento
 * 2. Sistema mostra tela Detalhes do processo
 * 3. Sistema identifica unidades elegíveis e exibe botão de aceite em bloco
 * 4. GESTOR clica no botão 'Aceitar em Bloco'
 * 5. Sistema abre modal com lista de unidades selecionáveis
 * 6. GESTOR seleciona unidades e confirma
 * 7. Sistema executa aceite para cada unidade selecionada
 */
test.describe.serial('CDU-22 - Aceitar cadastros em bloco', () => {
    const UNIDADE_1 = 'SECAO_221';
    const timestamp = Date.now();
    const descProcesso = `Mapeamento CDU-22 ${timestamp}`;

    const atividade1 = `Atividade Bloco ${timestamp}`;

    test('Setup UI', async ({page}) => {
        // Preparacao 1: Admin cria e inicia processo de mapeamento
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

        await page.getByTestId('btn-processo-iniciar').click();
        await page.getByTestId('btn-iniciar-processo-confirmar').click();

        await verificarPaginaPainel(page);

        // Preparacao 2: Chefe adiciona atividades e disponibiliza cadastro
        await login(page, USUARIOS.CHEFE_SECAO_221.titulo, USUARIOS.CHEFE_SECAO_221.senha);

        await page.getByTestId('tbl-processos').getByText(descProcesso).first().click();
        await navegarParaAtividades(page);

        await adicionarAtividade(page, atividade1);
        await adicionarConhecimento(page, atividade1, 'Conhecimento Bloco 1');

        await page.getByTestId('btn-cad-atividades-disponibilizar').click();
        await page.getByTestId('btn-confirmar-disponibilizacao').click();

        await verificarPaginaPainel(page);
        expect(true).toBeTruthy();
    });

    test('Cenario 1: GESTOR abre modal e cancela aceite em bloco', async ({page, autenticadoComoGestorCoord22}) => {
        await page.getByTestId('tbl-processos').getByText(descProcesso).first().click();
        await expect(page.getByRole('heading', {name: /Unidades participantes/i})).toBeVisible();

        const btnAceitar = page.getByRole('button', {name: /Aceitar cadastro em bloco/i}).first();
        await expect(btnAceitar).toBeVisible();
        await expect(btnAceitar).toBeEnabled();
        await btnAceitar.click();

        const modal = page.locator('#modal-acao-bloco');
        await expect(modal).toHaveClass(/show/);
        await expect(modal.getByText(/Aceite de cadastro em bloco/i)).toBeVisible();
        await expect(modal.getByText(/Selecione as unidades cujos cadastros deverão ser aceitos/i)).toBeVisible();
        await expect(modal.locator('table')).toBeVisible();
        await modal.getByRole('button', {name: /Cancelar/i}).click();

        await expect(modal).not.toHaveClass(/show/);
        await expect(page.getByRole('heading', {name: /Unidades participantes/i})).toBeVisible();
    });

    test('Cenario 2: GESTOR confirma aceite em bloco e retorna ao painel', async ({
                                                                                      page,
                                                                                      autenticadoComoGestorCoord22
                                                                                  }) => {
        await page.getByTestId('tbl-processos').getByText(descProcesso).first().click();
        const btnAceitar = page.getByRole('button', {name: /Aceitar cadastro em bloco/i}).first();
        await expect(btnAceitar).toBeVisible();
        await btnAceitar.click();

        const modal = page.locator('#modal-acao-bloco');
        await expect(modal).toHaveClass(/show/);
        await modal.getByRole('button', {name: /Registrar aceite/i}).click();

        await expect(page.getByText(/Cadastros aceitos em bloco/i).first()).toBeVisible();
        await expect(page).toHaveURL(/\/painel/);
    });
});
