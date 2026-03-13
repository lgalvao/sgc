import {expect, test} from './fixtures/complete-fixtures.js';
import {criarProcessoMapaDisponibilizadoFixture} from './fixtures/fixtures-processos.js';
import {navegarParaMapa} from './helpers/helpers-mapas.js';
import {verificarPaginaPainel} from './helpers/helpers-navegacao.js';

test.describe.serial('CDU-19 - Validar mapa de competências', () => {
    const UNIDADE_ALVO = 'SECAO_221';

    const timestamp = Date.now();
    const descProcesso = `Mapeamento CDU-19 ${timestamp}`;

    test('Setup data', async ({request}) => {
        await criarProcessoMapaDisponibilizadoFixture(request, {
            descricao: descProcesso,
            unidade: UNIDADE_ALVO
        });
        expect(true).toBeTruthy();
    });

    // TESTES PRINCIPAIS - CDU-19

    test('Cenários CDU-19: Fluxo completo de validação do mapa pelo CHEFE', async ({
                                                                                       page,
                                                                                       autenticadoComoChefeSecao221
                                                                                   }) => {
        // Cenario 1: Navegação para visualização do mapa
        await expect(page.getByTestId('tbl-processos').getByText(descProcesso).first()).toBeVisible();
        await page.getByTestId('tbl-processos').getByText(descProcesso).first().click();

        await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Mapa disponibilizado/i);

        await navegarParaMapa(page);
        await expect(page.getByText('Mapa de competências técnicas')).toBeVisible();
        await expect(page.getByTestId('btn-mapa-sugestoes')).toBeVisible();
        await expect(page.getByTestId('btn-mapa-validar')).toBeVisible();

        // Cenario 2: Cancelar validação
        await page.getByTestId('btn-mapa-validar').click();
        const modal = page.getByRole('dialog');
        await expect(modal).toBeVisible();
        await expect(modal.getByText(/Confirma a validação/i)).toBeVisible();

        await page.getByTestId('btn-validar-mapa-cancelar').click();
        await expect(page.getByText('Mapa de competências técnicas')).toBeVisible();
        await expect(page.getByTestId('btn-mapa-validar')).toBeVisible();

        // Cenario 3: Validar com sucesso
        await page.getByTestId('btn-mapa-validar').click();
        await expect(modal).toBeVisible();
        await page.getByTestId('btn-validar-mapa-confirmar').click();

        await verificarPaginaPainel(page);
        await page.getByTestId('tbl-processos').getByText(descProcesso).first().click();
        await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Mapa validado/i);
    });
});
