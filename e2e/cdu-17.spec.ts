import {expect, test} from './fixtures/complete-fixtures.js';
import {criarProcessoCadastroHomologadoFixture, validarProcessoFixture} from './fixtures/fixtures-processos.js';
import {criarCompetencia, disponibilizarMapa, navegarParaMapa} from './helpers/helpers-mapas.js';
import {navegarParaSubprocesso, verificarPaginaPainel} from './helpers/helpers-navegacao.js';

test.describe.serial('CDU-17 - Disponibilizar mapa de competências', () => {
    const UNIDADE_ALVO = 'SECAO_211';

    const timestamp = Date.now();
    const descProcesso = `Mapeamento CDU-17 ${timestamp}`;

    const atividade1 = 'Atividade fixture 1';
    const atividade2 = 'Atividade fixture 2';
    const atividade3 = 'Atividade fixture 3';
    const competencia1 = `Competência 1 ${timestamp}`;
    const competencia2 = `Competência 2 ${timestamp}`;

    test('Setup data', async ({_resetAutomatico, request}) => {
        const processo = await criarProcessoCadastroHomologadoFixture(request, {
            descricao: descProcesso,
            unidade: UNIDADE_ALVO
        });
        validarProcessoFixture(processo, descProcesso);
    });

    // TESTES PRINCIPAIS - CDU-17

    test('Cenários CDU-17: Fluxo completo de disponibilização do mapa pelo ADMIN', async ({
                                                                                              _resetAutomatico,
                                                                                              page,
                                                                                              _autenticadoComoAdmin
}) => {

        // Cenario 1: Navegação
        await expect(page.getByTestId('tbl-processos').getByText(descProcesso).first()).toBeVisible();
        await page.getByTestId('tbl-processos').getByText(descProcesso).first().click();
        await navegarParaSubprocesso(page, 'SECAO_211');
        await navegarParaMapa(page);

        await expect(page.getByText('Mapa de competências técnicas')).toBeVisible();
        await expect(page.getByTestId('btn-cad-mapa-disponibilizar')).toBeVisible();

        await criarCompetencia(page, competencia1, [atividade1, atividade2]);
        await criarCompetencia(page, competencia2, [atividade3]);

        await expect(page.getByText(competencia1)).toBeVisible();
        await expect(page.getByText(competencia2)).toBeVisible();

        // Cenario 2: Abrir modal
        await page.getByTestId('btn-cad-mapa-disponibilizar').click();
        await expect(page.getByTestId('mdl-disponibilizar-mapa')).toBeVisible();
        await expect(page.getByText('Disponibilização do mapa')).toBeVisible();

        // Cenario 3: Cancelar
        await page.getByTestId('btn-disponibilizar-mapa-cancelar').click();
        await expect(page.getByTestId('mdl-disponibilizar-mapa')).toBeHidden();
        await expect(page.getByText('Mapa de competências técnicas')).toBeVisible();

        // Cenario 4: Disponibilizar com sucesso
        await disponibilizarMapa(page, '2030-12-31');
        await verificarPaginaPainel(page);
        await page.getByTestId('tbl-processos').getByText(descProcesso).first().click();
        await navegarParaSubprocesso(page, 'SECAO_211');
        await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Mapa disponibilizado/i);
    });
});
