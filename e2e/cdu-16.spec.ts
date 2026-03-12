import {expect, test} from './fixtures/complete-fixtures.js';
import {criarProcessoRevisaoCadastroHomologadoFixture} from './fixtures/fixtures-processos.js';
import {criarCompetencia, navegarParaMapa} from './helpers/helpers-mapas.js';
import {acessarSubprocessoAdmin} from './helpers/helpers-analise.js';

test.describe.serial('CDU-16 - Ajustar mapa de competências', () => {
    const UNIDADE_ALVO = 'SECAO_211';

    const timestamp = Date.now();
    const descProcessoRevisao = `Revisão CDU-16 ${timestamp}`;

    const competencia1 = 'Competência Fixture 1';
    const competencia2 = 'Competência Fixture 2';
    const competencia3 = 'Competência Fixture 3';
    const atividadeNovaRevisao = 'Atividade Nova Revisão Fixture';

    test('Setup Data', async ({request}) => {
        await criarProcessoRevisaoCadastroHomologadoFixture(request, {
            descricao: descProcessoRevisao,
            unidade: UNIDADE_ALVO
        });
        expect(true).toBeTruthy();
    });

    test('Cenários CDU-16: ADMIN ajusta mapa e visualiza impactos', async ({page, autenticadoComoAdmin}) => {
        await test.step('Cenário 1: Navegação para o Mapa', async () => {
            await acessarSubprocessoAdmin(page, descProcessoRevisao, UNIDADE_ALVO);
            await navegarParaMapa(page);

            await expect(page.getByRole('heading', {name: /Mapa de competências/i})).toBeVisible();
            await expect(page.getByTestId('cad-mapa__btn-impactos-mapa')).toBeVisible();
            await expect(page.getByTestId('btn-cad-mapa-disponibilizar')).toBeVisible();
            await expect(page.getByText(competencia1)).toBeVisible();
            await expect(page.getByText(competencia2)).toBeVisible();
        });

        await test.step('Cenário 2: Visualização de Impactos', async () => {
            await page.getByTestId('cad-mapa__btn-impactos-mapa').click();
            await expect(page.getByTestId('modal-impacto-body')).toBeVisible();

            const modal = page.getByRole('dialog');
            await expect(modal.getByText('Atividades Inseridas')).toBeVisible();
            await expect(modal.getByText(atividadeNovaRevisao)).toBeVisible();
            await expect(modal.getByText('Competências Impactadas')).toBeVisible();
            await expect(modal.getByText(competencia2)).toBeVisible();
            await expect(modal.getByText(competencia3)).toBeVisible();

            await page.getByTestId('btn-fechar-impacto').click();
            await expect(modal).toBeHidden();
        });

        await test.step('Cenário 3: Edição de Competência', async () => {
            const card = page.locator('.competencia-card', {has: page.getByText(competencia1, {exact: true})});
            await card.hover();
            await card.getByTestId('btn-editar-competencia').click();

            const modalCria = page.getByTestId('mdl-criar-competencia');
            await expect(modalCria).toBeVisible();
            await expect(page.getByTestId('inp-criar-competencia-descricao')).toHaveValue(competencia1);

            await page.getByRole('button', {name: 'Cancelar'}).click();
            await expect(modalCria).toBeHidden();
        });

        await test.step('Cenário 4: Associação de Nova Competência', async () => {
            const novaCompetencia = `Competência Nova Ajuste ${timestamp}`;
            await criarCompetencia(page, novaCompetencia, [atividadeNovaRevisao]);

            await expect(page.getByText(novaCompetencia)).toBeVisible();
        });
    });
});
