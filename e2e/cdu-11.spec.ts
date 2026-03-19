import {expect, test} from './fixtures/complete-fixtures.js';
import {
    criarProcessoCadastroDisponibilizadoFixture,
    criarProcessoFinalizadoFixture
} from './fixtures/fixtures-processos.js';
import {navegarParaAtividadesVisualizacao} from './helpers/helpers-atividades.js';
import {navegarParaSubprocesso} from './helpers/helpers-navegacao.js';

test.describe.serial('CDU-11 - Visualizar cadastro de atividades e conhecimentos', () => {
    
    test.describe('Em processo em Andamento', () => {
        const UNIDADE_ALVO = 'SECAO_111';
        const timestamp = Date.now();
        const descProcesso = `Processo em andamento CDU-11 ${timestamp}`;

        test('Setup data', async ({_resetAutomatico, request}) => {
            await criarProcessoCadastroDisponibilizadoFixture(request, {
                unidade: UNIDADE_ALVO,
                descricao: descProcesso
            });
            expect(true).toBeTruthy();
        });

        test('Fluxo ADMIN/GESTOR: Navega via Detalhes do Processo (Passo 2)', async ({_resetAutomatico, page, _autenticadoComoAdmin}) => {
            await page.getByTestId('tbl-processos').getByText(descProcesso).first().click();

            // 2.1. O sistema mostra a tela Detalhes do processo
            await expect(page).toHaveURL(new RegExp(String.raw`/processo/\d+$`));

            // 2.2. Usuário clica em uma unidade subordinada
            await navegarParaSubprocesso(page, UNIDADE_ALVO);

            // 2.3. O sistema mostra a tela Detalhes do subprocesso
            await expect(page).toHaveURL(new RegExp(String.raw`/processo/\d+/${UNIDADE_ALVO}$`));

            await navegarParaAtividadesVisualizacao(page);

            await expect(page.getByTestId('subprocesso-header__txt-header-unidade')).toHaveText(UNIDADE_ALVO);
            await expect(page.getByText(/\S+/).first()).toBeVisible();
            await expect(page.getByText(/Atividade fixture/)).toBeVisible();
            await expect(page.getByText(/Conhecimento fixture/)).toBeVisible();
        });

        test('Fluxo CHEFE/SERVIDOR: Navega direto para Detalhes do Subprocesso (Passo 3)', async ({_resetAutomatico, page, _autenticadoComoChefeSecao111}) => {
            await page.getByTestId('tbl-processos').getByText(descProcesso).first().click();

            // 3.1. O sistema exibe a tela Detalhes do subprocesso com os dados da unidade do usuário
            await expect(page).toHaveURL(new RegExp(String.raw`/processo/\d+/${UNIDADE_ALVO}$`));

            await navegarParaAtividadesVisualizacao(page);

            await expect(page.getByTestId('subprocesso-header__txt-header-unidade')).toHaveText(UNIDADE_ALVO);
            await expect(page.getByText(/\S+/).first()).toBeVisible();
            await expect(page.getByText(/Atividade fixture/)).toBeVisible();
            await expect(page.getByText(/Conhecimento fixture/)).toBeVisible();
        });
    });

    test.describe('Em processo finalizado', () => {
        const UNIDADE_ALVO = 'SECAO_112';
        const timestamp = Date.now();
        const descProcesso = `Processo mapeamento CDU-11 ${timestamp}`;

        test('Setup data', async ({_resetAutomatico, request}) => {
            await criarProcessoFinalizadoFixture(request, {
                unidade: UNIDADE_ALVO,
                descricao: descProcesso
            });
            expect(true).toBeTruthy();
        });

        test('Fluxo ADMIN: Visualizar em processo finalizado', async ({_resetAutomatico, page, _autenticadoComoAdmin}) => {

            await page.getByTestId('tbl-processos').getByText(descProcesso).first().click();

            await navegarParaSubprocesso(page, UNIDADE_ALVO);
            await navegarParaAtividadesVisualizacao(page);

            await expect(page.getByTestId('subprocesso-header__txt-header-unidade')).toHaveText(UNIDADE_ALVO);
            await expect(page.getByText(/\S+/).first()).toBeVisible();
            await expect(page.getByText(/Atividade origem/).first()).toBeVisible();
            await expect(page.getByText(/Conhecimento [AB]/).first()).toBeVisible();
        });
    });
});
