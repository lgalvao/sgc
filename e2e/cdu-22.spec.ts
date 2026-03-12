import {expect, test} from './fixtures/complete-fixtures.js';
import {criarProcessoCadastroDisponibilizadoFixture} from './fixtures/fixtures-processos.js';
import {loginComPerfil, USUARIOS} from './helpers/helpers-auth.js';

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
    let processoCodigo: number;

    test('Setup Data', async ({request}) => {
        const processo = await criarProcessoCadastroDisponibilizadoFixture(request, {
            descricao: descProcesso,
            unidade: UNIDADE_1
        });
        processoCodigo = processo.codigo;
        expect(processoCodigo).toBeGreaterThan(0);
    });

    test('Cenario 1: GESTOR abre modal e cancela aceite em bloco', async ({page, autenticadoComoGestorCoord22}) => {
        await page.getByTestId('tbl-processos').getByText(descProcesso).first().click();
        await expect(page.getByRole('heading', {name: /Unidades participantes/i})).toBeVisible();

        const btnAceitar = page.getByTestId('btn-processo-aceitar-bloco');
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

    test('Cenario 3a: Botão desabilitado quando item está com gestor subordinado', async ({page, autenticadoComoGestorSecretaria2}) => {
        // autenticadoComoGestorSecretaria2 já logou como GESTOR SECRETARIA_2
        await page.goto(`/processo/${processoCodigo}`);
        await expect(page.getByRole('heading', {name: /Unidades participantes/i})).toBeVisible();

        const btnAceitar = page.getByTestId('btn-processo-aceitar-bloco');
        await expect(btnAceitar).toBeVisible();
        await expect(btnAceitar).toBeDisabled();
    });

    test('Cenario 3b: Botão habilitado após gestor subordinado aceitar', async ({page, autenticadoComoGestorCoord22}) => {
        // autenticadoComoGestorCoord22 já logou como GESTOR COORD_22
        await page.goto(`/processo/${processoCodigo}`);
        await expect(page.getByRole('heading', {name: /Unidades participantes/i})).toBeVisible();
        await page.getByTestId('btn-processo-aceitar-bloco').click();
        await page.locator('#modal-acao-bloco').getByRole('button', {name: /Registrar aceite/i}).click();
        await expect(page.getByText(/Cadastros aceitos em bloco/i).first()).toBeVisible();

        // GESTOR SECRETARIA_2 deve agora ver o botão habilitado
        await loginComPerfil(page, USUARIOS.CHEFE_SECRETARIA_2.titulo, USUARIOS.CHEFE_SECRETARIA_2.senha, 'GESTOR - SECRETARIA_2');
        await page.goto(`/processo/${processoCodigo}`);
        await expect(page.getByRole('heading', {name: /Unidades participantes/i})).toBeVisible();
        await expect(page.getByTestId('btn-processo-aceitar-bloco')).toBeEnabled();
    });

    test('Cenario 4: Botão desabilitado para gestor superior quando item está com intermediário', async ({request, page}) => {
        const timestamp4 = Date.now();
        const processo4 = await criarProcessoCadastroDisponibilizadoFixture(request, {
            descricao: `CDU-22-C4 ${timestamp4}`,
            unidade: 'SECAO_111'  // Sob COORD_11, que está sob SECRETARIA_1
        });

        // GESTOR SECRETARIA_1 acessa: item está com COORD_11 (intermediário)
        await loginComPerfil(page, USUARIOS.GESTOR_SECRETARIA_1.titulo, USUARIOS.GESTOR_SECRETARIA_1.senha, 'GESTOR - SECRETARIA_1');
        await page.goto(`/processo/${processo4.codigo}`);
        await expect(page.getByRole('heading', {name: /Unidades participantes/i})).toBeVisible();

        const btnAceitar = page.getByTestId('btn-processo-aceitar-bloco');
        await expect(btnAceitar).toBeVisible();
        await expect(btnAceitar).toBeDisabled();
    });
});
