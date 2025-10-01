import {expect, Page} from '@playwright/test';
import {vueTest as test} from '../support/vue-specific-setup';
import {
    confirmarNoModal,
    criarCompetencia,
    esperarBotaoVisivel,
    esperarElementoVisivel,
    esperarTextoVisivel,
    gerarNomeUnico,
    irParaMapaCompetencias,
    loginComoAdmin,
    SELETORES,
    TEXTOS,
    verificarModalVisivel
} from './helpers';

async function navegarParaMapa(page: Page) {
    await loginComoAdmin(page);
    await irParaMapaCompetencias(page, 4, 'SESEL');
    await esperarTextoVisivel(page, TEXTOS.MAPA_COMPETENCIAS_TECNICAS);
}

test.describe('CDU-15: Manter mapa de competências', () => {
    test.beforeEach(async ({page}) => {
        await navegarParaMapa(page);
    });

    test('deve exibir tela de edição de mapa com elementos corretos', async ({page}) => {
        await esperarElementoVisivel(page, 'btn-abrir-criar-competencia');
        await esperarBotaoVisivel(page, TEXTOS.DISPONIBILIZAR);
    });

    test('deve criar competência e alterar situação do subprocesso', async ({page}) => {
        const competenciaDescricao = `Competência Teste ${Date.now()}`;
        await criarCompetencia(page, competenciaDescricao);

        // Verifica que o novo card aparece pelo seu texto e que as ações estão disponíveis
        await esperarTextoVisivel(page, competenciaDescricao);
        await esperarElementoVisivel(page, SELETORES.EDITAR_COMPETENCIA);
        await esperarElementoVisivel(page, SELETORES.EXCLUIR_COMPETENCIA);
    });

    test('deve editar competência existente', async ({page}) => {
        const competenciaOriginal = gerarNomeUnico("Competência Original");
        await criarCompetencia(page, competenciaOriginal);

        const competenciaCard = page.locator('.competencia-card').filter({hasText: competenciaOriginal});
        await competenciaCard.getByTestId('btn-editar-competencia').click();

        const competenciaEditada = gerarNomeUnico("Competência Editada");
        await page.getByTestId('input-nova-competencia').fill(competenciaEditada);
        await page.getByTestId('btn-criar-competencia').click();

        // Verifica pelo texto da competência editada e que o original não existe mais
        await esperarTextoVisivel(page, competenciaEditada);
        await expect(page.getByText(competenciaOriginal)).not.toBeVisible();
    });

    test('deve excluir competência com confirmação', async ({page}) => {
        const competenciaParaExcluir = gerarNomeUnico("Competência para Excluir");
        await criarCompetencia(page, competenciaParaExcluir);

        const competenciaCard = page.locator('.competencia-card').filter({hasText: competenciaParaExcluir});
        await competenciaCard.getByTestId('btn-excluir-competencia').click();

        // Modal de exclusão pode ter título fixo; usamos helper semântico para aguardar modal e confirmar
        await verificarModalVisivel(page);
        await confirmarNoModal(page);

        // Verifica que o texto da competência não está mais presente
        await expect(page.getByText(competenciaParaExcluir)).not.toBeVisible();
    });
});