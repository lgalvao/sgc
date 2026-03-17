import {expect, test} from './fixtures/complete-fixtures.js';
import {criarProcessoCadastroHomologadoFixture} from './fixtures/fixtures-processos.js';
import {criarCompetencia, navegarParaMapa} from './helpers/helpers-mapas.js';
import {navegarParaSubprocesso} from './helpers/helpers-navegacao.js';
import {TEXTOS} from '../frontend/src/constants/textos.js';

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

    test('Setup data', async ({_resetAutomatico, request}) => {
        await criarProcessoCadastroHomologadoFixture(request, {
            descricao: descProcesso,
            unidade: UNIDADE_1
        });
        expect(true).toBeTruthy();
    });

    test('ADMIN mantém botão disponibilizar desabilitado enquanto existir atividade sem competência', async ({_resetAutomatico, page, _autenticadoComoAdmin}) => {
        await page.getByTestId('tbl-processos').getByText(descProcesso).first().click();
        await navegarParaSubprocesso(page, UNIDADE_1);
        await navegarParaMapa(page);

        const btnDisponibilizarMapa = page.getByTestId('btn-cad-mapa-disponibilizar');
        await expect(btnDisponibilizarMapa).toBeDisabled();
        await criarCompetencia(page, competencia1, [atividade1, atividade2]);
        await expect(btnDisponibilizarMapa).toBeDisabled();
    });

    test('ADMIN disponibiliza mapas em bloco após associar todas as atividades', async ({_resetAutomatico, page, _autenticadoComoAdmin}) => {
        await page.getByTestId('tbl-processos').getByText(descProcesso).first().click();
        await navegarParaSubprocesso(page, UNIDADE_1);
        await navegarParaMapa(page);
        await criarCompetencia(page, `${competencia1} complementar`, [atividade3]);

        // Retornar para tela do processo para ação em bloco
        await page.goto('/painel');
        await page.getByTestId('tbl-processos').getByText(descProcesso).first().click();
        
        // Validação da UI da ação em bloco
        const btnDisponibilizar = page.getByRole('button', {name: TEXTOS.acaoBloco.disponibilizar.ROTULO}).first();
        await expect(btnDisponibilizar).toBeEnabled();
        await btnDisponibilizar.click();

        const modal = page.locator('#modal-acao-bloco');
        await expect(modal.getByText(TEXTOS.acaoBloco.disponibilizar.TITULO)).toBeVisible();
        await expect(modal.getByText(TEXTOS.acaoBloco.disponibilizar.TEXTO)).toBeVisible();
        await expect(modal.getByLabel(/Data limite/i)).toBeVisible();

        const data = new Date();
        data.setDate(data.getDate() + 10);
        const yyyy = data.getFullYear();
        const mm = String(data.getMonth() + 1).padStart(2, '0');
        const dd = String(data.getDate()).padStart(2, '0');
        await modal.getByLabel(/Data limite/i).fill(`${yyyy}-${mm}-${dd}`);

        await modal.getByRole('button', {name: TEXTOS.acaoBloco.disponibilizar.BOTAO}).click();
        await expect(page.getByText(TEXTOS.sucesso.MAPAS_DISPONIBILIZADOS_EM_BLOCO).first()).toBeVisible();
        await expect(page).toHaveURL(/\/painel/);
    });
});
