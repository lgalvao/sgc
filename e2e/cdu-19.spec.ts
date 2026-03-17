import {expect, test} from './fixtures/complete-fixtures.js';
import {criarProcessoMapaDisponibilizadoFixture} from './fixtures/fixtures-processos.js';
import {navegarParaMapa} from './helpers/helpers-mapas.js';
import {login, USUARIOS} from './helpers/helpers-auth.js';
import {acessarSubprocessoGestor} from './helpers/helpers-analise.js';
import {verificarPaginaPainel} from './helpers/helpers-navegacao.js';
import {resetDatabase} from './hooks/hooks-limpeza.js';

test.describe.serial('CDU-19 - Validar mapa de competências', () => {
    const UNIDADE_ALVO = 'SECAO_221';

    const timestamp = Date.now();
    const descProcesso = `Mapeamento CDU-19 ${timestamp}`;

    test('Setup data', async ({_resetAutomatico, request}) => {
        await criarProcessoMapaDisponibilizadoFixture(request, {
            descricao: descProcesso,
            unidade: UNIDADE_ALVO
        });
        expect(true).toBeTruthy();
    });

    // TESTES PRINCIPAIS - CDU-19

    test('Cenários CDU-19: Fluxo completo de validação do mapa pelo CHEFE', async ({
                                                                                       _resetAutomatico,
                                                                                       page,
                                                                                       _autenticadoComoChefeSecao221
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

test.describe.serial('CDU-19 - Apresentar sugestões e pré-preenchimento', () => {
    const UNIDADE_ALVO = 'SECAO_221';
    const GESTOR_SUPERIOR = USUARIOS.GESTOR_COORD_22;

    const timestamp = Date.now();
    const descProcesso = `Mapeamento CDU-19 Sugestoes ${timestamp}`;
    const TEXTO_SUGESTAO = 'Sugestão de ajuste na competência técnica';

    test('Setup data', async ({_resetAutomatico, request}) => {
        await resetDatabase(request);
        await criarProcessoMapaDisponibilizadoFixture(request, {
            descricao: descProcesso,
            unidade: UNIDADE_ALVO
        });
        expect(true).toBeTruthy();
    });

    test('Cenario 1: CHEFE apresenta sugestões com sucesso', async ({_resetAutomatico, page, _autenticadoComoChefeSecao221}) => {
        await expect(page.getByTestId('tbl-processos').getByText(descProcesso).first()).toBeVisible();
        await page.getByTestId('tbl-processos').getByText(descProcesso).first().click();

        await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Mapa disponibilizado/i);
        await navegarParaMapa(page);

        await expect(page.getByTestId('btn-mapa-sugestoes')).toBeVisible();

        // Modal abre sem pré-preenchimento (mapa novo, sem sugestões anteriores)
        await page.getByTestId('btn-mapa-sugestoes').click();
        const modal = page.getByRole('dialog');
        await expect(modal).toBeVisible();
        await expect(page.getByTestId('inp-sugestoes-mapa-texto')).toHaveValue('');

        // Preenche e confirma
        await page.getByTestId('inp-sugestoes-mapa-texto').fill(TEXTO_SUGESTAO);
        await page.getByTestId('btn-sugestoes-mapa-confirmar').click();

        await verificarPaginaPainel(page);
        await page.getByTestId('tbl-processos').getByText(descProcesso).first().click();
        await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Mapa com sugestões/i);
    });

    test('Cenario 2: GESTOR devolve mapa para ajustes', async ({_resetAutomatico, page}) => {
        await login(page, GESTOR_SUPERIOR.titulo, GESTOR_SUPERIOR.senha);
        await acessarSubprocessoGestor(page, descProcesso, UNIDADE_ALVO);
        await navegarParaMapa(page);

        await expect(page.getByTestId('btn-mapa-devolver')).toBeVisible();
        await page.getByTestId('btn-mapa-devolver').click();
        await page.getByTestId('inp-devolucao-mapa-obs').fill('Necessário rever competências');
        await page.getByTestId('btn-devolucao-mapa-confirmar').click();

        await verificarPaginaPainel(page);
    });

    test('Cenario 3: CHEFE reabre modal com pré-preenchimento das sugestões anteriores', async ({
        _resetAutomatico,
        page,
        _autenticadoComoChefeSecao221
    }) => {
        await expect(page.getByTestId('tbl-processos').getByText(descProcesso).first()).toBeVisible();
        await page.getByTestId('tbl-processos').getByText(descProcesso).first().click();

        await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Mapa disponibilizado/i);
        await navegarParaMapa(page);

        await expect(page.getByTestId('btn-mapa-sugestoes')).toBeVisible();

        await page.getByTestId('btn-mapa-sugestoes').click();
        await expect(page.getByTestId('inp-sugestoes-mapa-texto')).toHaveValue(TEXTO_SUGESTAO);

        // Cancela sem alterar o estado
        await page.getByTestId('btn-sugestoes-mapa-cancelar').click();
    });
});
