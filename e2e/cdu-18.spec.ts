import {expect, test} from './fixtures/complete-fixtures.js';
import {navegarParaMapa} from './helpers/helpers-mapas.js';
import {TEXTOS} from '../frontend/src/constants/textos.js';

/**
 * CDU-18: Visualizar mapa de competências
 *
 * Pré-condições do CDU-18:
 * - Usuário logado com qualquer perfil
 * - Processo de mapeamento ou de revisão iniciado ou finalizado
 * - Subprocesso da unidade com mapa de competência já disponibilizado
 *
 * O seed contém:
 * - Processo 99 (FINALIZADO) com mapa homologado para unidade de assessoria
 * - Mapa 99 com competência "Competência técnica seed 99" vinculada às atividades
 */
test.describe('CDU-18: Visualizar mapa de competências', () => {

    test('Cenário 1: ADMIN visualiza mapa via detalhes do processo', async ({_resetAutomatico, page, _autenticadoComoAdmin}) => {
        await test.step('1. Login como ADMIN', async () => {
            // Já autenticado via fixture
        });

        await test.step('2. Navegar para processo finalizado com mapa', async () => {
            // Clicar no processo 99 que tem mapa homologado
            await page.getByTestId('tbl-processos').getByRole('row', {name: 'Processo 99'}).click();
            await expect(page).toHaveURL(/\/processo\/\d+$/);
        });

        await test.step('3. Selecionar unidade de assessoria participante', async () => {
            const linhaUnidade = page.getByRole('row', {name: /ASSESSORIA_/}).first();
            await expect(linhaUnidade).toBeVisible();
            await linhaUnidade.click();

            // Verificar navegação para detalhes do subprocesso
            await expect(page).toHaveURL(/\/processo\/\d+\/ASSESSORIA_\d+$/);
        });


        await test.step('4. Acessar mapa de competências via card', async () => {
            // Verificar que card de mapa está disponível e acessível
            await navegarParaMapa(page);
        });

        await test.step('5. Verificar visualização do mapa (CDU-18)', async () => {
            // 5.1 Título "Mapa de competências técnicas"
            await expect(page.getByRole('heading', {name: TEXTOS.mapa.TITULO_TECNICO})).toBeVisible();

            // 5.2 Identificação da unidade (sigla)
            const headerUnidade = page.getByTestId('subprocesso-header__txt-header-unidade');
            await expect(headerUnidade).toBeVisible();
            await expect(headerUnidade).toContainText(/ASSESSORIA_\d+/);

            // 5.3 Competência do seed
            await expect(page.getByText('Competência técnica seed 99')).toBeVisible();

            // 5.4 Atividades da competência
            await expect(page.getByText('Atividade seed 1')).toBeVisible();
            await expect(page.getByText('Atividade seed 2')).toBeVisible();

            // 5.5 Conhecimentos das atividades
            await expect(page.getByText('Conhecimento seed 1.1')).toBeVisible();
            await expect(page.getByText('Conhecimento seed 2.1')).toBeVisible();
        });
    });

    test('Cenário 2: CHEFE visualiza mapa da própria unidade', async ({_resetAutomatico, page, _autenticadoComoChefeAssessoria12}) => {
        await test.step('1. Login como CHEFE_ASSESSORIA_12', async () => {
            // Já autenticado via fixture
        });

        await test.step('2. Navegar para processo via painel', async () => {
            // CHEFE vê processo no painel e clica
            await page.getByTestId('tbl-processos').getByRole('row', {name: 'Processo 99'}).click();

            // CHEFE vai direto para detalhes do subprocesso da sua unidade
            await expect(page).toHaveURL(/\/processo\/\d+\/ASSESSORIA_12$/);
        });

        await test.step('3. Acessar mapa de competências', async () => {
            await navegarParaMapa(page);
        });

        await test.step('4. Verificar visualização do mapa', async () => {
            await expect(page.getByRole('heading', {name: TEXTOS.mapa.TITULO_TECNICO})).toBeVisible();
            const headerUnidade = page.getByTestId('subprocesso-header__txt-header-unidade');
            await expect(headerUnidade).toBeVisible();
            await expect(headerUnidade).toHaveText('ASSESSORIA_12');
            await expect(page.getByText('Competência técnica seed 99')).toBeVisible();
        });
    });
});
