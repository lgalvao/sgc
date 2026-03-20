import {expect, test} from './fixtures/complete-fixtures.js';
import {criarProcessoCadastroHomologadoFixture} from './fixtures/fixtures-processos.js';
import {acessarSubprocessoAdmin} from './helpers/helpers-analise.js';
import {verificarProcessoNaTabela} from './helpers/helpers-processos.js';
import {
    abrirModalCriarCompetencia,
    criarCompetencia,
    disponibilizarMapa,
    editarCompetencia,
    excluirCompetenciaCancelando,
    excluirCompetenciaConfirmando,
    navegarParaMapa,
    verificarCompetenciaNoMapa
} from './helpers/helpers-mapas.js';
import {TEXTOS} from '../frontend/src/constants/textos.js';

test.describe.serial('CDU-15 - Manter mapa de competências', () => {
    const UNIDADE_ALVO = 'SECAO_211';

    const timestamp = Date.now();
    const descProcesso = `Processo CDU-15 ${timestamp}`;

    const ATIVIDADE_1 = 'Atividade fixture 1';
    const ATIVIDADE_2 = 'Atividade fixture 2';
    const ATIVIDADE_3 = 'Atividade fixture 3';

    test('Setup data', async ({_resetAutomatico, request}) => {
        await criarProcessoCadastroHomologadoFixture(request, {
            descricao: descProcesso,
            unidade: UNIDADE_ALVO
        });
        expect(true).toBeTruthy();
    });

    test('Cenários CDU-15: Fluxo completo de manutenção do mapa pelo ADMIN', async ({_resetAutomatico, page, _autenticadoComoAdmin}) => {
        // CT-00 e CT-01: Acessar edição e verificar elementos
        await acessarSubprocessoAdmin(page, descProcesso, UNIDADE_ALVO);
        await navegarParaMapa(page);

        await expect(page.getByRole('heading', {name: TEXTOS.mapa.TITULO})).toBeVisible();
        await expect(page.getByTestId('btn-abrir-criar-competencia')).toBeVisible();
        await expect(page.getByTestId('btn-cad-mapa-disponibilizar')).toBeDisabled();

        // CT-02: Criar competência
        const compDesc = `Competência 1 ${timestamp}`;
        await criarCompetencia(page, compDesc, [ATIVIDADE_1]);
        await verificarCompetenciaNoMapa(page, compDesc, [ATIVIDADE_1]);
        await expect(page.getByTestId('btn-cad-mapa-disponibilizar')).toBeDisabled();

        // CT-03: Editar competência
        const newDesc = `Competência 1 Editada ${timestamp}`;
        await editarCompetencia(page, compDesc, newDesc, [ATIVIDADE_2]);
        await verificarCompetenciaNoMapa(page, newDesc, [ATIVIDADE_1, ATIVIDADE_2]);

        // CT-05: Validar cancelamento da Exclusão
        await excluirCompetenciaCancelando(page, newDesc);
        await expect(page.getByText(newDesc).first()).toBeVisible();

        // CT-04: Excluir competência com Confirmação
        await excluirCompetenciaConfirmando(page, newDesc);
        await expect(page.getByTestId('btn-cad-mapa-disponibilizar')).toBeDisabled();

        // CT-06: Navegar para Disponibilização
        const compFinal = `Competência final ${timestamp}`;
        await criarCompetencia(page, compFinal, [ATIVIDADE_1, ATIVIDADE_2, ATIVIDADE_3]);
        await disponibilizarMapa(page);

        await expect(page).toHaveURL(/\/painel/);
        await verificarProcessoNaTabela(page, {
            descricao: descProcesso,
            situacao: 'Em andamento',
            tipo: 'Mapeamento'
        });
    });

    test('Badge de conhecimentos no modal de criar competência exibe contagem sem HTML bruto', async ({_resetAutomatico, page, _autenticadoComoAdmin}) => {
        // CT relacionado ao Bug #1391: o badge deve mostrar a contagem de conhecimentos (ex: "1"),
        // e não o HTML bruto que era gerado pela implementação anterior com v-b-tooltip.html.
        await acessarSubprocessoAdmin(page, descProcesso, UNIDADE_ALVO);
        await navegarParaMapa(page);
        await abrirModalCriarCompetencia(page);

        // Atividade fixture 1 tem "Conhecimento fixture 1A" → badge deve mostrar "1"
        const modal = page.getByTestId('mdl-criar-competencia');
        const badgeConhecimentos = modal
            .locator('.atividade-card-item', {hasText: ATIVIDADE_1})
            .getByTestId('cad-mapa__txt-badge-conhecimentos-2');

        await expect(badgeConhecimentos).toBeVisible();
        await expect(badgeConhecimentos).toHaveText('1');
        // O badge não deve conter marcação HTML bruta
        await expect(badgeConhecimentos).not.toContainText('<div');
        await expect(badgeConhecimentos).not.toContainText('<strong>');

        await page.keyboard.press('Escape');
    });
});
