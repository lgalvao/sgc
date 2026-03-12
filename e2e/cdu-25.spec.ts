 
import {expect, test} from './fixtures/complete-fixtures.js';
import {criarProcessoMapaValidadoFixture} from './fixtures/fixtures-processos.js';
import {login, USUARIOS} from './helpers/helpers-auth.js';
import {verificarPaginaPainel} from './helpers/helpers-navegacao.js';

/**
 * CDU-25 - Aceitar validação de mapas de competências em bloco
 *
 * Ator: GESTOR
 *
 * Pré-condições:
 * - Subprocesso nas situações 'Mapa validado' ou 'Mapa com sugestões'
 * - Localização atual na unidade do usuário
 *
 * Fluxo principal:
 * 1. GESTOR acessa processo em andamento
 * 2. Sistema mostra Detalhes do processo
 * 3. Sistema identifica unidades elegíveis para aceite
 * 4. GESTOR clica no botão 'Aceitar Mapa em Bloco'
 * 5. Sistema abre modal com lista de unidades
 * 6. GESTOR confirma
 * 7. Sistema executa aceite para cada unidade
 */
test.describe.serial('CDU-25 - Aceitar validação de mapas em bloco', () => {
    const UNIDADE_1 = 'SECAO_211';

    const timestamp = Date.now();
    const descProcesso = `Mapeamento CDU-25 ${timestamp}`;

    test('Setup Data', async ({request}) => {
        await criarProcessoMapaValidadoFixture(request, {
            descricao: descProcesso,
            unidade: UNIDADE_1
        });
        expect(true).toBeTruthy();
    });

    test('Cenario 1: GESTOR acessa processo com mapa validado', async ({page}) => {
        await login(page, USUARIOS.GESTOR_COORD_21.titulo, USUARIOS.GESTOR_COORD_21.senha);

        await page.getByTestId('tbl-processos').getByText(descProcesso).first().click();
        await expect(page.getByRole('heading', {name: /Unidades participantes/i})).toBeVisible();

        const btnAceitar = page.getByRole('button', {name: /Aceitar mapas em bloco/i}).first();
        await expect(btnAceitar).toBeVisible();
        await expect(btnAceitar).toBeEnabled();
    });

    test('Cenario 2: GESTOR abre modal de aceite de mapa em bloco e cancela', async ({page}) => {
        await login(page, USUARIOS.GESTOR_COORD_21.titulo, USUARIOS.GESTOR_COORD_21.senha);

        await page.getByTestId('tbl-processos').getByText(descProcesso).first().click();
        const btnAceitar = page.getByRole('button', {name: /Aceitar mapas em bloco/i}).first();
        await btnAceitar.click();

        const modal = page.getByRole('dialog');
        await expect(modal).toBeVisible();
        await modal.getByRole('button', {name: /Cancelar/i}).click();
        await expect(modal).toBeHidden();
        await expect(page.getByRole('heading', {name: /Unidades participantes/i})).toBeVisible();
    });

    test('Cenario 3: GESTOR realiza aceite em bloco com sucesso', async ({page}) => {
        await login(page, USUARIOS.GESTOR_COORD_21.titulo, USUARIOS.GESTOR_COORD_21.senha);

        await page.getByTestId('tbl-processos').getByText(descProcesso).first().click();
        await page.getByRole('button', {name: /Aceitar mapas em bloco/i}).first().click();

        const modal = page.getByRole('dialog');
        await expect(modal).toBeVisible();
        
        // Verifica título e texto
        await expect(modal.getByText(/Aceite de mapas em bloco/i)).toBeVisible();
        await expect(modal.getByText(/Selecione as unidades para aceite dos mapas correspondentes/i)).toBeVisible();

        const linhaUnidade = modal.locator('tr', { hasText: 'SECAO_211' });
        await expect(linhaUnidade).toBeVisible();
        
        const checkbox = linhaUnidade.locator('input[type="checkbox"]');
        await expect(checkbox).toBeChecked();

        await modal.getByRole('button', {name: /Registrar aceite/i}).click();

        await expect(page.getByText('Mapas aceitos em bloco')).toBeVisible();
        await verificarPaginaPainel(page);
    });
});
