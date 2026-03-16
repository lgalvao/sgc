import {expect, test} from './fixtures/complete-fixtures.js';
import {criarProcessoMapaValidadoFixture, criarProcessoMapaComSugestoesFixture} from './fixtures/fixtures-processos.js';
import {navegarParaMapa} from './helpers/helpers-mapas.js';
import {login, loginComPerfil, USUARIOS} from './helpers/helpers-auth.js';
import {acessarSubprocessoGestor} from './helpers/helpers-analise.js';
import {navegarParaSubprocesso} from './helpers/helpers-navegacao.js';
import {TEXTOS} from '../frontend/src/constants/textos.js';
import {resetDatabase} from './hooks/hooks-limpeza.js';

test.describe.serial('CDU-20 - Analisar validação de mapa de competências', () => {
    const UNIDADE_ALVO = 'ASSESSORIA_11';

    const timestamp = Date.now();
    const descProcesso = `Processo CDU-20 ${timestamp}`;

    test('Setup data', async ({_resetAutomatico, request}) => {
        await criarProcessoMapaValidadoFixture(request, {
            unidade: UNIDADE_ALVO,
            descricao: descProcesso
        });
        expect(true).toBeTruthy();
    });

    test('Cenario 1: GESTOR SECRETARIA_1 analisa e aceita', async ({_resetAutomatico, page}) => {
        // Superior da ASSESSORIA_11 é John lennon (SECRETARIA_1)
        await loginComPerfil(page, USUARIOS.GESTOR_SECRETARIA_1.titulo, USUARIOS.GESTOR_SECRETARIA_1.senha, 'GESTOR - SECRETARIA_1');
        await acessarSubprocessoGestor(page, descProcesso, UNIDADE_ALVO);
        await navegarParaMapa(page);

        await expect(page.getByTestId('btn-mapa-historico-gestor')).toBeVisible();
        await expect(page.getByTestId('btn-mapa-devolver')).toBeVisible();

        // Verifica que o botão "Ver sugestões" NÃO aparece (situação é "Mapa validado", não "Mapa com sugestões")
        await expect(page.getByTestId('btn-mapa-ver-sugestoes')).not.toBeVisible();

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
        await page.getByTestId('tbl-processos').getByText(descProcesso).first().click();
        await navegarParaSubprocesso(page, UNIDADE_ALVO);
        await navegarParaMapa(page);

        await page.getByTestId('btn-mapa-homologar-aceite').click();
        await page.getByTestId('btn-aceite-mapa-confirmar').click();
        await expect(page.getByText(TEXTOS.sucesso.HOMOLOGACAO_EFETIVADA).first()).toBeVisible();
    });
});

test.describe.serial('CDU-20 - Ver sugestões quando situação é "Mapa com sugestões"', () => {
    const UNIDADE_ALVO = 'ASSESSORIA_11';

    const timestamp = Date.now();
    const descProcesso = `Processo CDU-20 Sugestoes ${timestamp}`;

    test('Setup data', async ({_resetAutomatico, request}) => {
        await resetDatabase(request);
        await criarProcessoMapaComSugestoesFixture(request, {
            unidade: UNIDADE_ALVO,
            descricao: descProcesso
        });
        expect(true).toBeTruthy();
    });

    test('GESTOR visualiza botão "Ver sugestões" e acessa modal com conteúdo', async ({_resetAutomatico, page}) => {
        await loginComPerfil(page, USUARIOS.GESTOR_SECRETARIA_1.titulo, USUARIOS.GESTOR_SECRETARIA_1.senha, 'GESTOR - SECRETARIA_1');
        await acessarSubprocessoGestor(page, descProcesso, UNIDADE_ALVO);
        await navegarParaMapa(page);

        // O botão "Ver sugestões" deve aparecer quando situação é "Mapa com sugestões" (CDU-20, passo 5)
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
        await expect(txtSugestoes).not.toHaveValue('');

        // Fecha o modal
        await page.getByTestId('btn-ver-sugestoes-mapa-fechar').click();
        await expect(modal).toBeHidden();
    });
});
