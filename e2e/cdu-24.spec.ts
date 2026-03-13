import {expect, test} from './fixtures/complete-fixtures.js';
import {criarProcessoCadastroHomologadoFixture} from './fixtures/fixtures-processos.js';
import {criarCompetencia, navegarParaMapa} from './helpers/helpers-mapas.js';
import {navegarParaSubprocesso} from './helpers/helpers-navegacao.js';

/**
 * CDU-24 - Disponibilizar mapas de competências em bloco
 *
 * Ator: ADMIN
 */
test.describe.serial('CDU-24 - Disponibilizar mapas em bloco', () => {
    const UNIDADE_1 = 'SECAO_221';
    const timestamp = Date.now();
    const descProcesso = `Mapeamento CDU-24 ${timestamp}`;
    const atividade1 = 'Atividade fixture 1';
    const atividade2 = 'Atividade fixture 2';
    const atividade3 = 'Atividade fixture 3';
    const competencia1 = `Competência mapa ${timestamp}`;

    test('Setup data', async ({request}) => {
        await criarProcessoCadastroHomologadoFixture(request, {
            descricao: descProcesso,
            unidade: UNIDADE_1
        });
        expect(true).toBeTruthy();
    });

    test('ADMIN disponibiliza mapas em bloco', async ({page, autenticadoComoAdmin}) => {
        await page.getByTestId('tbl-processos').getByText(descProcesso).first().click();
        await navegarParaSubprocesso(page, UNIDADE_1);
        await navegarParaMapa(page);
        await criarCompetencia(page, competencia1, [atividade1, atividade2, atividade3]);

        // Retornar para tela do processo para ação em bloco
        await page.goto('/painel');
        await page.getByTestId('tbl-processos').getByText(descProcesso).first().click();
        
        // Validação da UI da ação em bloco
        const btnDisponibilizar = page.getByRole('button', {name: /Disponibilizar mapas em bloco/i}).first();
        await expect(btnDisponibilizar).toBeEnabled();
        await btnDisponibilizar.click();

        const modal = page.locator('#modal-acao-bloco');
        await expect(modal.getByText(/Disponibilização de mapa em bloco/i)).toBeVisible();
        await expect(modal.getByText(/Selecione abaixo as unidades cujos mapas deverão ser disponibilizados/i)).toBeVisible();
        await expect(modal.getByLabel(/Data limite/i)).toBeVisible();

        const data = new Date();
        data.setDate(data.getDate() + 10);
        const yyyy = data.getFullYear();
        const mm = String(data.getMonth() + 1).padStart(2, '0');
        const dd = String(data.getDate()).padStart(2, '0');
        await modal.getByLabel(/Data limite/i).fill(`${yyyy}-${mm}-${dd}`);

        await modal.getByRole('button', {name: /^Disponibilizar$/i}).click();
        await expect(page.getByText(/Mapas de competências disponibilizados em bloco/i).first()).toBeVisible();
        await expect(page).toHaveURL(/\/painel/);
    });
});
