import {expect, test} from './fixtures/complete-fixtures.js';
import type {Page} from '@playwright/test';
import {
    criarProcessoMapaComSugestoesFixture,
    criarProcessoMapaDisponibilizadoFixture,
    criarProcessoMapaValidadoFixture,
    validarProcessoFixture
} from './fixtures/fixtures-processos.js';
import {navegarParaMapa} from './helpers/helpers-mapas.js';
import {login, loginComPerfil, USUARIOS} from './helpers/helpers-auth.js';
import {
    acessarSubprocessoAdmin,
    acessarSubprocessoChefeDireto,
    acessarSubprocessoGestor
} from './helpers/helpers-analise.js';
import {navegarParaSubprocesso, verificarPaginaPainel} from './helpers/helpers-navegacao.js';
import {acessarDetalhesProcesso} from './helpers/helpers-processos.js';
import {TEXTOS} from '../frontend/src/constants/textos.js';
import {resetDatabase} from './hooks/hooks-limpeza.js';

function validarDataHoraBrasileira(dataHoraTexto: string) {
    expect(dataHoraTexto.trim()).toMatch(/^\d{2}\/\d{2}\/\d{4} \d{2}:\d{2}(:\d{2})?$/);
}

async function validarHistoricoAnaliseMapa(page: Page, unidadeEsperada?: string) {
    const modal = page.getByTestId('mdl-historico-analise');

    await expect(modal.getByTestId('header-historico-dataHora')).toBeVisible();
    await expect(modal.getByTestId('header-historico-unidade')).toBeVisible();
    await expect(modal.getByTestId('header-historico-resultado')).toBeVisible();
    await expect(modal.getByTestId('header-historico-analista')).toBeVisible();
    await expect(modal.getByTestId('header-historico-observacao')).toBeVisible();

    const dataHora = await modal.getByTestId('cell-dataHora-0').innerText();
    validarDataHoraBrasileira(dataHora);

    if (unidadeEsperada) {
        await expect(modal.getByTestId('cell-unidade-0')).toHaveText(unidadeEsperada);
    } else {
        await expect(modal.getByTestId('cell-unidade-0')).not.toHaveText('');
    }

    await expect(modal.getByTestId('cell-resultado-0')).not.toHaveText('');
    await expect(modal.getByTestId('cell-observacao-0')).not.toHaveText('');
}

test.describe.serial('CDU-20 - Analisar validação de mapa de competências', () => {
    const UNIDADE_ALVO = 'ASSESSORIA_11';

    const timestamp = Date.now();
    const descProcesso = `Processo CDU-20 ${timestamp}`;

    test('Setup data', async ({_resetAutomatico, request}) => {
        const processo = await criarProcessoMapaValidadoFixture(request, {
            unidade: UNIDADE_ALVO,
            descricao: descProcesso
        });
        validarProcessoFixture(processo, descProcesso);
    });

    test('Cenario 0: modal de aceite exibe campo opcional de observação', async ({_resetAutomatico, page}) => {
        await loginComPerfil(page, USUARIOS.GESTOR_SECRETARIA_1.titulo, USUARIOS.GESTOR_SECRETARIA_1.senha, 'GESTOR - SECRETARIA_1');
        await acessarSubprocessoGestor(page, descProcesso, UNIDADE_ALVO);
        await expect(page.getByTestId('header-subprocesso')).toBeVisible();
        await expect(page.getByTestId('subprocesso-header__txt-header-unidade')).toHaveText(UNIDADE_ALVO);
        await navegarParaMapa(page);

        await page.getByTestId('btn-mapa-homologar-aceite').click();
        await expect(page.getByRole('dialog')).toContainText(TEXTOS.mapa.MODAL_ACEITE_TITULO);
        await expect(page.getByTestId('inp-aceite-mapa-observacao')).toBeVisible();
        await page.getByTestId('inp-aceite-mapa-observacao').fill('Observação opcional do aceite');
        await page.getByTestId('btn-aceite-mapa-cancelar').click();
        await expect(page.getByRole('dialog')).toBeHidden();
    });

    test('Cenario 1: GESTOR SECRETARIA_1 analisa e aceita', async ({_resetAutomatico, page}) => {
        // Superior da ASSESSORIA_11 é John lennon (SECRETARIA_1)
        await loginComPerfil(page, USUARIOS.GESTOR_SECRETARIA_1.titulo, USUARIOS.GESTOR_SECRETARIA_1.senha, 'GESTOR - SECRETARIA_1');
        await acessarSubprocessoGestor(page, descProcesso, UNIDADE_ALVO);
        await expect(page.getByTestId('header-subprocesso')).toBeVisible();
        await expect(page.getByTestId('subprocesso-header__txt-header-unidade')).toHaveText(UNIDADE_ALVO);
        await navegarParaMapa(page);

        await expect(page.getByTestId('btn-mapa-historico-gestor')).toBeVisible();
        await expect(page.getByTestId('btn-mapa-devolver')).toBeVisible();

        // Verifica que o botão "Ver sugestões" NÃO aparece (situação é "Mapa validado", não "Mapa com sugestões")
        await expect(page.getByTestId('btn-mapa-ver-sugestoes')).toBeHidden();

        // Verifica que o botão de confirmar devolução está desabilitado sem observação
        await page.getByTestId('btn-mapa-devolver').click();
        await expect(page.getByTestId('btn-devolucao-mapa-confirmar')).toBeDisabled();

        // Verifica que o botão habilita após preencher a observação
        await page.getByTestId('inp-devolucao-mapa-obs').fill('Observação de devolução');
        await expect(page.getByTestId('btn-devolucao-mapa-confirmar')).toBeEnabled();

        // Cancela a devolução (passo CDU)
        await page.getByTestId('btn-devolucao-mapa-cancelar').click();

        await page.getByTestId('btn-mapa-homologar-aceite').click();
        await page.getByTestId('btn-aceite-mapa-confirmar').click();
        await expect(page).toHaveURL(/\/painel/);
    });

    test('Cenario 2: ADMIN homologa final', async ({_resetAutomatico, page}) => {
        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
        await acessarDetalhesProcesso(page, descProcesso);
        await navegarParaSubprocesso(page, UNIDADE_ALVO);
        await navegarParaMapa(page);

        await page.getByTestId('btn-mapa-homologar-aceite').click();
        await page.getByTestId('btn-aceite-mapa-confirmar').click();
        await expect(page.getByText(TEXTOS.mapa.SUCESSO_HOMOLOGACAO).first()).toBeVisible();
    });

});

test.describe.serial('CDU-20 - Ver sugestões quando situação é "Mapa com sugestões"', () => {
    const UNIDADE_ALVO = 'ASSESSORIA_11';

    const timestamp = Date.now();
    const descProcesso = `Processo CDU-20 Sugestoes ${timestamp}`;

    test('Setup data', async ({_resetAutomatico, request}) => {
        await resetDatabase(request);
        const processo = await criarProcessoMapaComSugestoesFixture(request, {
            unidade: UNIDADE_ALVO,
            descricao: descProcesso
        });
        validarProcessoFixture(processo, descProcesso);
    });

    test('GESTOR visualiza botão "Ver sugestões" e acessa modal com conteúdo', async ({_resetAutomatico, page}) => {
        await loginComPerfil(page, USUARIOS.GESTOR_SECRETARIA_1.titulo, USUARIOS.GESTOR_SECRETARIA_1.senha, 'GESTOR - SECRETARIA_1');
        await acessarSubprocessoGestor(page, descProcesso, UNIDADE_ALVO);
        await navegarParaMapa(page);

        await expect(page.getByTestId('btn-mapa-ver-sugestoes')).toBeVisible();

        // Os demais botões de análise também devem estar presentes
        await expect(page.getByTestId('btn-mapa-historico-gestor')).toBeVisible();
        await expect(page.getByTestId('btn-mapa-devolver')).toBeVisible();

        // Clique no botão abre modal com sugestões registradas
        await page.getByTestId('btn-mapa-ver-sugestoes').click();
        const modal = page.getByRole('dialog');
        await expect(modal).toBeVisible();

        // Conteúdo do modal exibe as sugestões
        const txtSugestoes = page.getByTestId('txt-ver-sugestoes-mapa');
        await expect(txtSugestoes).toBeVisible();
        await expect(txtSugestoes).toHaveValue('Sugestão de ajuste na competência via fixture E2E');

        // Fecha o modal
        await page.getByTestId('btn-ver-sugestoes-mapa-fechar').click();
        await expect(modal).toBeHidden();
    });
});

test.describe.serial('CDU-20 - Aceite de mapa com sugestões', () => {
    const UNIDADE_ALVO = 'ASSESSORIA_11';
    const TEXTO_SUGESTAO = 'Sugestão do chefe para ajuste no mapa';

    const timestamp = Date.now();
    const descProcesso = `Processo CDU-20 Aceite Sugestoes ${timestamp}`;

    test('Setup data', async ({_resetAutomatico, request}) => {
        await resetDatabase(request);
        const processo = await criarProcessoMapaDisponibilizadoFixture(request, {
            unidade: UNIDADE_ALVO,
            descricao: descProcesso
        });
        validarProcessoFixture(processo, descProcesso);
    });

    test('CHEFE apresenta sugestões e GESTOR registra aceite', async ({_resetAutomatico, page}) => {
        await login(page, USUARIOS.CHEFE_ASSESSORIA_11.titulo, USUARIOS.CHEFE_ASSESSORIA_11.senha);
        await acessarSubprocessoChefeDireto(page, descProcesso, UNIDADE_ALVO);
        await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Mapa disponibilizado/i);
        await navegarParaMapa(page);

        await expect(page.getByTestId('btn-mapa-sugestoes')).toBeVisible();
        await page.getByTestId('btn-mapa-sugestoes').click();
        await expect(page.getByRole('dialog')).toBeVisible();
        await page.getByTestId('inp-sugestoes-mapa-texto').fill(TEXTO_SUGESTAO);
        await page.getByTestId('btn-sugestoes-mapa-confirmar').click();

        await verificarPaginaPainel(page);

        await loginComPerfil(page, USUARIOS.GESTOR_SECRETARIA_1.titulo, USUARIOS.GESTOR_SECRETARIA_1.senha, 'GESTOR - SECRETARIA_1');
        await acessarSubprocessoGestor(page, descProcesso, UNIDADE_ALVO);
        await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Mapa com sugestões/i);
        await navegarParaMapa(page);

        await expect(page.getByTestId('btn-mapa-ver-sugestoes')).toBeVisible();
        await expect(page.getByTestId('btn-mapa-homologar-aceite')).toBeVisible();
        await expect(page.getByTestId('btn-mapa-homologar-aceite')).toBeEnabled();

        await page.getByTestId('btn-mapa-ver-sugestoes').click();
        await expect(page.getByTestId('txt-ver-sugestoes-mapa')).toHaveValue(TEXTO_SUGESTAO);
        await page.getByTestId('btn-ver-sugestoes-mapa-fechar').click();

        await page.getByTestId('btn-mapa-homologar-aceite').click();
        await page.getByTestId('btn-aceite-mapa-confirmar').click();

        await verificarPaginaPainel(page);
        await expect(page.getByText(TEXTOS.sucesso.ACEITE_REGISTRADO).first()).toBeVisible();
    });

    test('GESTOR devolve validação do mapa e sistema registra efeito visível no subprocesso', async ({_resetAutomatico, request, page}) => {
        await resetDatabase(request);
        const processo = await criarProcessoMapaValidadoFixture(request, {
            unidade: UNIDADE_ALVO,
            descricao: `Processo CDU-20 Devolucao ${Date.now()}`
        });
        validarProcessoFixture(processo, processo.descricao);

        await loginComPerfil(page, USUARIOS.GESTOR_SECRETARIA_1.titulo, USUARIOS.GESTOR_SECRETARIA_1.senha, 'GESTOR - SECRETARIA_1');
        await acessarSubprocessoGestor(page, processo.descricao, UNIDADE_ALVO);
        await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Mapa validado/i);
        await navegarParaMapa(page);

        await page.getByTestId('btn-mapa-devolver').click();
        const modal = page.getByRole('dialog');
        await expect(modal).toContainText('Devolver mapa');
        await expect(modal).toContainText('Confirma a devolução da validação do mapa para ajustes?');
        await expect(page.getByTestId('btn-devolucao-mapa-confirmar')).toBeDisabled();

        await page.getByTestId('inp-devolucao-mapa-obs').fill('Necessário rever competências');
        await expect(page.getByTestId('btn-devolucao-mapa-confirmar')).toBeEnabled();
        await page.getByTestId('btn-devolucao-mapa-confirmar').click();

        await verificarPaginaPainel(page);
        await expect(page.getByText(TEXTOS.sucesso.DEVOLUCAO_REALIZADA).first()).toBeVisible();

        await acessarSubprocessoChefeDireto(page, processo.descricao, UNIDADE_ALVO);
        await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Mapa disponibilizado/i);
        await expect(page.getByTestId('tbl-movimentacoes')).toBeVisible();
        await expect(page.getByRole('columnheader', {name: 'Data/hora'})).toBeVisible();
        await expect(page.getByRole('columnheader', {name: 'Origem'})).toBeVisible();
        await expect(page.getByRole('columnheader', {name: 'Destino'})).toBeVisible();
        await expect(page.getByRole('columnheader', {name: 'Descrição'})).toBeVisible();

        const linhaMovimentacao = page.getByTestId('tbl-movimentacoes')
            .locator('tr', {hasText: 'Devolução da validação do mapa de competências para ajustes'})
            .first();
        await expect(linhaMovimentacao).toBeVisible();

        const dataHora = await linhaMovimentacao.locator('td').nth(0).innerText();
        validarDataHoraBrasileira(dataHora);
        await expect(linhaMovimentacao.locator('td').nth(1)).toHaveText('SECRETARIA_1');
        await expect(linhaMovimentacao.locator('td').nth(2)).toHaveText(UNIDADE_ALVO);

    });
});

test.describe.serial('CDU-20 - ADMIN não deve ver botões de edição com mapa com sugestões (Bug #1376)', () => {
    const UNIDADE_ALVO = 'ASSESSORIA_11';

    const timestamp = Date.now();
    const descProcesso = `Processo CDU-20 Bug1376 EditarMapa ${timestamp}`;

    test('Setup data', async ({_resetAutomatico, request}) => {
        await resetDatabase(request);
        const processo = await criarProcessoMapaComSugestoesFixture(request, {
            unidade: UNIDADE_ALVO,
            descricao: descProcesso
        });
        validarProcessoFixture(processo, descProcesso);
    });

    test('ADMIN não vê card de edição de mapa quando situação é Mapa com sugestões', async ({_resetAutomatico, page}) => {
        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
        await acessarSubprocessoAdmin(page, descProcesso, UNIDADE_ALVO);

        await expect(page.getByTestId('card-subprocesso-mapa-edicao')).toBeHidden();
        await expect(page.getByTestId('card-subprocesso-mapa-visualizacao')).toBeVisible();
    });
});

test.describe.serial('CDU-20 - ADMIN homologa mapa após GESTOR aceitar com sugestões (Bug #1376)', () => {
    const UNIDADE_ALVO = 'ASSESSORIA_11';
    const TEXTO_SUGESTAO = 'Sugestão para ajuste no mapa - Bug 1376';

    const timestamp = Date.now();
    const descProcesso = `Processo CDU-20 Bug1376 Homologar ${timestamp}`;

    test('Setup data', async ({_resetAutomatico, request}) => {
        await resetDatabase(request);
        const processo = await criarProcessoMapaDisponibilizadoFixture(request, {
            unidade: UNIDADE_ALVO,
            descricao: descProcesso
        });
        validarProcessoFixture(processo, descProcesso);
    });

    test('CHEFE apresenta sugestões e GESTOR registra aceite', async ({_resetAutomatico, page}) => {
        await login(page, USUARIOS.CHEFE_ASSESSORIA_11.titulo, USUARIOS.CHEFE_ASSESSORIA_11.senha);
        await acessarSubprocessoChefeDireto(page, descProcesso, UNIDADE_ALVO);
        await navegarParaMapa(page);

        await page.getByTestId('btn-mapa-sugestoes').click();
        await expect(page.getByRole('dialog')).toBeVisible();
        await page.getByTestId('inp-sugestoes-mapa-texto').fill(TEXTO_SUGESTAO);
        await page.getByTestId('btn-sugestoes-mapa-confirmar').click();
        await verificarPaginaPainel(page);

        await loginComPerfil(page, USUARIOS.GESTOR_SECRETARIA_1.titulo, USUARIOS.GESTOR_SECRETARIA_1.senha, 'GESTOR - SECRETARIA_1');
        await acessarSubprocessoGestor(page, descProcesso, UNIDADE_ALVO);
        await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Mapa com sugestões/i);
        await navegarParaMapa(page);

        await page.getByTestId('btn-mapa-homologar-aceite').click();
        await page.getByTestId('btn-aceite-mapa-confirmar').click();
        await verificarPaginaPainel(page);
        await expect(page.getByText(TEXTOS.sucesso.ACEITE_REGISTRADO).first()).toBeVisible();
    });

    test('ADMIN homologa o mapa após aceite do GESTOR com sugestões', async ({_resetAutomatico, page}) => {
        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
        await acessarSubprocessoAdmin(page, descProcesso, UNIDADE_ALVO);
        await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Mapa com sugestões/i);
        await navegarParaMapa(page);

        await page.getByTestId('btn-mapa-homologar-aceite').click();
        await page.getByTestId('btn-aceite-mapa-confirmar').click();
        await expect(page.getByText(TEXTOS.mapa.SUCESSO_HOMOLOGACAO).first()).toBeVisible();
    });
});
