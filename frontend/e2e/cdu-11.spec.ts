import {test} from '@playwright/test';
import {
    adicionarAtividade,
    adicionarConhecimento,
    clicarUnidadeNaTabelaDetalhes,
    criarProcessoCompleto,
    disponibilizarCadastro,
    gerarNomeUnico,
    loginComo,
    loginComoAdmin,
    loginComoChefe,
    navegarParaProcessoPorId,
    SELETORES_CSS,
    verificarAtividadeVisivel,
    verificarConhecimentoVisivel,
    verificarModoSomenteLeitura,
} from './helpers';
import {Perfil} from '@/types/tipos';

test.describe('CDU-11: Visualizar cadastro de atividades (somente leitura)', () => {
    let processo: any;
    const nomeAtividade = gerarNomeUnico('Atividade para Visualizar');
    const nomeConhecimento = gerarNomeUnico('Conhecimento para Visualizar');

    test.beforeAll(async ({browser}) => {
        const page = await browser.newPage();
        // Setup: Cria um processo, adiciona dados e disponibiliza o cadastro
        const context = await criarProcessoCompleto(page, gerarNomeUnico('PROCESSO CDU-11'), 'MAPEAMENTO', '2025-12-31', [1]);
        processo = context.processo;

        // Adiciona uma atividade e conhecimento via UI para simular o fluxo real
        await loginComoChefe(page);
        await navegarParaProcessoPorId(page, processo.codigo);
        await clicarUnidadeNaTabelaDetalhes(page, 'STIC');
        await adicionarAtividade(page, nomeAtividade);
        const cardAtividade = page.locator(SELETORES_CSS.CARD_ATIVIDADE, {hasText: nomeAtividade});
        await adicionarConhecimento(cardAtividade, nomeConhecimento);
        await disponibilizarCadastro(page);
        await page.close();
    });

    test('ADMIN deve visualizar cadastro em modo somente leitura', async ({page}) => {
        await loginComoAdmin(page);
        await navegarParaProcessoPorId(page, processo.codigo);
        await clicarUnidadeNaTabelaDetalhes(page, 'STIC');

        await verificarAtividadeVisivel(page, nomeAtividade);
        const cardAtividade = page.locator(SELETORES_CSS.CARD_ATIVIDADE, {hasText: nomeAtividade});
        await verificarConhecimentoVisivel(cardAtividade, nomeConhecimento);
        await verificarModoSomenteLeitura(page);
    });

    test('GESTOR da unidade superior deve visualizar cadastro em modo somente leitura', async ({page}) => {
        await loginComo(page, Perfil.GESTOR, '1'); // Gestor da SGP (unidade superior à STIC)
        await navegarParaProcessoPorId(page, processo.codigo);
        await clicarUnidadeNaTabelaDetalhes(page, 'STIC');

        await verificarAtividadeVisivel(page, nomeAtividade);
        const cardAtividade = page.locator(SELETORES_CSS.CARD_ATIVIDADE, {hasText: nomeAtividade});
        await verificarConhecimentoVisivel(cardAtividade, nomeConhecimento);
        await verificarModoSomenteLeitura(page);
    });

    test('CHEFE de outra unidade não deve ver os botões de edição', async ({page}) => {
        // Loga como chefe de uma unidade que não é a STIC, mas está no processo
        await criarProcessoCompleto(page, gerarNomeUnico('PROCESSO CDU-11 OUTRA UNIDADE'), 'MAPEAMENTO', '2025-12-31', [3]); // Adiciona a unidade 3 (SESEL)
        await loginComo(page, Perfil.CHEFE, '111111111111'); // Chefe da SESEL
        await navegarParaProcessoPorId(page, processo.codigo);
        await clicarUnidadeNaTabelaDetalhes(page, 'STIC');

        await verificarAtividadeVisivel(page, nomeAtividade);
        const cardAtividade = page.locator(SELETORES_CSS.CARD_ATIVIDADE, {hasText: nomeAtividade});
        await verificarConhecimentoVisivel(cardAtividade, nomeConhecimento);
        await verificarModoSomenteLeitura(page);
    });
});