import {expect, test} from './fixtures/complete-fixtures.js';
import {criarProcessoCadastroHomologadoFixture, validarProcessoFixture} from './fixtures/fixtures-processos.js';
import {criarCompetencia, navegarParaMapa} from './helpers/helpers-mapas.js';
import {acessarDetalhesProcesso} from './helpers/helpers-processos.js';
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

    function obterDataPosterior(dataIso: string): string {
        const data = new Date(`${dataIso}T00:00:00`);
        data.setDate(data.getDate() + 1);
        const ano = data.getFullYear();
        const mes = String(data.getMonth() + 1).padStart(2, '0');
        const dia = String(data.getDate()).padStart(2, '0');
        return `${ano}-${mes}-${dia}`;
    }

    test('Setup data', async ({_resetAutomatico, request}) => {
        const processo = await criarProcessoCadastroHomologadoFixture(request, {
            descricao: descProcesso,
            unidade: UNIDADE_1
        });
        validarProcessoFixture(processo, descProcesso);
    });

    test('ADMIN mantém botão disponibilizar desabilitado enquanto existir atividade sem competência', async ({_resetAutomatico, page, _autenticadoComoAdmin}) => {
        await acessarDetalhesProcesso(page, descProcesso);
        await navegarParaSubprocesso(page, UNIDADE_1);
        await navegarParaMapa(page);

        const btnDisponibilizarMapa = page.getByTestId('btn-cad-mapa-disponibilizar');
        await expect(btnDisponibilizarMapa).toBeDisabled();
        await criarCompetencia(page, competencia1, [atividade1, atividade2]);
        await expect(btnDisponibilizarMapa).toBeDisabled();
    });

    test('ADMIN abre modal de disponibilização em bloco e cancela operação', async ({_resetAutomatico, page, _autenticadoComoAdmin}) => {
        await acessarDetalhesProcesso(page, descProcesso);

        const btnDisponibilizar = page.getByRole('button', {name: TEXTOS.acaoBloco.disponibilizar.ROTULO}).first();
        await expect(btnDisponibilizar).toBeEnabled();
        await btnDisponibilizar.click();

        const modal = page.locator('#modal-acao-bloco');
        await expect(modal.getByText(TEXTOS.acaoBloco.disponibilizar.TITULO)).toBeVisible();
        await expect(modal.getByText(TEXTOS.acaoBloco.disponibilizar.TEXTO)).toBeVisible();
        await expect(modal.getByRole('button', {name: /Cancelar/i})).toBeVisible();
        await expect(modal.getByRole('button', {name: TEXTOS.acaoBloco.disponibilizar.BOTAO})).toBeVisible();

        await modal.getByRole('button', {name: /Cancelar/i}).click();
        await expect(modal).not.toHaveClass(/show/);
        await expect(page.getByRole('heading', {name: /Unidades participantes/i})).toBeVisible();
    });


    test('ADMIN disponibiliza mapas em bloco após associar todas as atividades', async ({_resetAutomatico, page, _autenticadoComoAdmin}) => {
        await acessarDetalhesProcesso(page, descProcesso);
        await navegarParaSubprocesso(page, UNIDADE_1);
        await navegarParaMapa(page);
        await criarCompetencia(page, `${competencia1} complementar`, [atividade3]);

        // Retornar para tela do processo para ação em bloco
        await page.goto('/painel');
        await acessarDetalhesProcesso(page, descProcesso);
        
        // Validação da UI da ação em bloco
        const btnDisponibilizar = page.getByRole('button', {name: TEXTOS.acaoBloco.disponibilizar.ROTULO}).first();
        await expect(btnDisponibilizar).toBeEnabled();
        await btnDisponibilizar.click();

        const modal = page.locator('#modal-acao-bloco');
        await expect(modal.getByText(TEXTOS.acaoBloco.disponibilizar.TITULO)).toBeVisible();
        await expect(modal.getByText(TEXTOS.acaoBloco.disponibilizar.TEXTO)).toBeVisible();
        await expect(modal.getByLabel(/Data limite/i)).toBeVisible();

        const campoData = modal.getByLabel(/Data limite/i);
        const dataMinima = await campoData.getAttribute('min');
        expect(dataMinima).toBeTruthy();
        await campoData.fill(obterDataPosterior(dataMinima!));

        await modal.getByRole('button', {name: TEXTOS.acaoBloco.disponibilizar.BOTAO}).click();
        await expect(page.getByText(TEXTOS.sucesso.MAPAS_DISPONIBILIZADOS_EM_BLOCO).first()).toBeVisible();
        await expect(page).toHaveURL(/\/painel/);
    });
});
