 
import {expect, test} from './fixtures/complete-fixtures.js';
import {criarProcessoCadastroDisponibilizadoFixture} from './fixtures/fixtures-processos.js';
import {navegarParaAtividadesVisualizacao} from './helpers/helpers-atividades.js';
import {aceitarCadastroMapeamento, acessarSubprocessoGestor} from './helpers/helpers-analise.js';
import {loginComPerfil, USUARIOS} from './helpers/helpers-auth.js';

/**
 * CDU-23 - Homologar cadastros em bloco
 *
 * Ator: ADMIN
 *
 * Pré-condições:
 * - Processo de mapeamento ou revisão com unidades subordinadas
 * - Subprocessos na situação 'Cadastro disponibilizado' ou 'Cadastro aceito'
 *
 * Fluxo principal:
 * 1. No Painel, ADMIN acessa processo em andamento
 * 2. Sistema mostra tela Detalhes do processo
 * 3. Sistema identifica unidades elegíveis para homologação
 * 4. ADMIN clica no botão 'Homologar em Bloco'
 * 5. Sistema abre modal com lista de unidades selecionáveis
 * 6. ADMIN seleciona unidades e confirma
 * 7. Sistema executa homologação para cada unidade selecionada
 */
test.describe.serial('CDU-23 - Homologar cadastros em bloco', () => {
    const UNIDADE_1 = 'SECAO_221';

    const timestamp = Date.now();
    const descProcesso = `Mapeamento CDU-23 ${timestamp}`;

    test('Setup Data', async ({request}) => {
        await criarProcessoCadastroDisponibilizadoFixture(request, {
            descricao: descProcesso,
            unidade: UNIDADE_1
        });
        expect(true).toBeTruthy();
    });

    test('Setup Aceites', async ({page, autenticadoComoGestorCoord22}) => {
        await acessarSubprocessoGestor(page, descProcesso, UNIDADE_1);
        await navegarParaAtividadesVisualizacao(page);
        await aceitarCadastroMapeamento(page);

        await loginComPerfil(page, USUARIOS.CHEFE_SECRETARIA_2.titulo, USUARIOS.CHEFE_SECRETARIA_2.senha, 'GESTOR - SECRETARIA_2');
        await acessarSubprocessoGestor(page, descProcesso, UNIDADE_1);
        await navegarParaAtividadesVisualizacao(page);
        await aceitarCadastroMapeamento(page);

        expect(true).toBeTruthy();
    });

    test('Cenario 1: ADMIN abre modal e cancela homologação em bloco', async ({page, autenticadoComoAdmin}) => {
        await page.getByTestId('tbl-processos').getByText(descProcesso).first().click();
        await expect(page.getByRole('heading', {name: /Unidades participantes/i})).toBeVisible();

        const btnHomologar = page.getByRole('button', {name: /Homologar em bloco/i}).first();
        await expect(btnHomologar).toBeVisible();
        await expect(btnHomologar).toBeEnabled();
        await btnHomologar.click();

        const modal = page.locator('#modal-acao-bloco');
        await expect(modal).toHaveClass(/show/);
        await expect(modal.getByText(/Homologação de cadastro em bloco/i)).toBeVisible();
        await expect(modal.getByText(/Selecione abaixo as unidades cujos cadastros deverão ser homologados/i)).toBeVisible();
        await expect(modal.locator('table')).toBeVisible();
        await modal.getByRole('button', {name: /Cancelar/i}).click();

        await expect(modal).not.toHaveClass(/show/);
        await expect(page.getByRole('heading', {name: /Unidades participantes/i})).toBeVisible();
    });

    test('Cenario 2: ADMIN confirma homologação em bloco e permanece na tela', async ({page, autenticadoComoAdmin}) => {
        await page.getByTestId('tbl-processos').getByText(descProcesso).first().click();
        const btnHomologar = page.getByRole('button', {name: /Homologar em bloco/i}).first();
        await expect(btnHomologar).toBeVisible();
        await btnHomologar.click();

        const modal = page.locator('#modal-acao-bloco');
        await expect(modal).toHaveClass(/show/);
        await modal.getByRole('button', {name: /^Homologar$/i}).click();

        await expect(page).toHaveURL(/\/processo\/\d+$/);
        await expect(page.getByRole('heading', {name: /Unidades participantes/i})).toBeVisible();
        await expect(page.getByTestId('app-alert')).toContainText('Cadastros homologados em bloco');
        await expect(btnHomologar).toBeDisabled();
        await expect(page.getByRole('row', {name: /SECAO_221 - Seção 221 Cadastro homologado/i})).toBeVisible();
    });
});
