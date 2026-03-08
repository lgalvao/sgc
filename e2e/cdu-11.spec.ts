import {expect, test} from './fixtures/complete-fixtures.js';
import {criarProcessoMapaDisponibilizadoFixture, criarProcessoFinalizadoFixture} from './fixtures/fixtures-processos.js';
import {
    navegarParaAtividadesVisualizacao
} from './helpers/helpers-atividades.js';
import {navegarParaSubprocesso} from './helpers/helpers-navegacao.js';

test.describe.serial('CDU-11 - Visualizar cadastro de atividades e conhecimentos', () => {
    
    test.describe('Em Processo em Andamento', () => {
        const UNIDADE_ALVO = 'SECAO_111';
        let processoId: number;
        let descProcesso: string;

        test('Preparacao: Criar processo disponibilizado', async ({request}) => {
            const processo = await criarProcessoMapaDisponibilizadoFixture(request, {
                unidade: UNIDADE_ALVO
            });
            processoId = processo.codigo;
            descProcesso = processo.descricao;
        });

        test('Fluxo ADMIN/GESTOR: Navega via Detalhes do Processo (Passo 2)', async ({page, autenticadoComoAdmin}) => {
            // 1. No Painel, o usuário clica no processo em andamento
            await page.getByTestId('tbl-processos').getByText(descProcesso).first().click();

            // 2.1. O sistema mostra a tela Detalhes do processo
            await expect(page).toHaveURL(new RegExp(String.raw`/processo/${processoId}$`));

            // 2.2. Usuário clica em uma unidade subordinada
            await navegarParaSubprocesso(page, UNIDADE_ALVO);

            // 2.3. O sistema mostra a tela Detalhes do subprocesso
            await expect(page).toHaveURL(new RegExp(String.raw`/processo/${processoId}/${UNIDADE_ALVO}$`));

            // 4. Na tela de Detalhes do subprocesso, usuário clica no card Atividades e conhecimentos.
            // 5. O sistema apresenta a tela Atividades e conhecimentos
            await navegarParaAtividadesVisualizacao(page);

            // 6. Verificação dos dados
            await expect(page.locator('.unidade-sigla').getByText(UNIDADE_ALVO)).toBeVisible();
            await expect(page.getByText(/Atividade Fixture/)).toBeVisible();
            await expect(page.getByText(/Conhecimento Fixture/)).toBeVisible();
        });

        test('Fluxo CHEFE/SERVIDOR: Navega direto para Detalhes do Subprocesso (Passo 3)', async ({page, autenticadoComoChefeSecao111}) => {
            // 1. No Painel, o usuário clica no processo em andamento
            await page.getByTestId('tbl-processos').getByText(descProcesso).first().click();

            // 3.1. O sistema exibe a tela Detalhes do subprocesso com os dados da unidade do usuário
            await expect(page).toHaveURL(new RegExp(String.raw`/processo/${processoId}/${UNIDADE_ALVO}$`));

            // 4. Na tela de Detalhes do subprocesso, usuário clica no card Atividades e conhecimentos.
            await navegarParaAtividadesVisualizacao(page);

            // 6. Verificação dos dados
            await expect(page.locator('.unidade-sigla').getByText(UNIDADE_ALVO)).toBeVisible();
            await expect(page.getByText(/Atividade Fixture/)).toBeVisible();
        });
    });

    test.describe('Em Processo Finalizado', () => {
        const UNIDADE_ALVO = 'SECAO_112';
        let processoId: number;
        let descProcesso: string;

        test('Preparacao: Criar processo finalizado', async ({request}) => {
            const processo = await criarProcessoFinalizadoFixture(request, {
                unidade: UNIDADE_ALVO
            });
            processoId = processo.codigo;
            descProcesso = processo.descricao;
        });

        test('Fluxo ADMIN: Visualizar em processo finalizado', async ({page, autenticadoComoAdmin}) => {
            // 1. No Painel, o usuário clica no processo finalizado
            await page.getByTestId('tbl-processos').getByText(descProcesso).first().click();

            // 2. Navegação até a visualização
            await navegarParaSubprocesso(page, UNIDADE_ALVO);
            await navegarParaAtividadesVisualizacao(page);

            // 6. Verificação
            await expect(page.locator('.unidade-sigla').getByText(UNIDADE_ALVO)).toBeVisible();
            await expect(page.getByText(/Atividade Origem/).first()).toBeVisible();
        });
    });
});
