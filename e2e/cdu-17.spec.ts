import {vueTest as test} from './support/vue-specific-setup';
import {
    disponibilizarCadastro,
    criarCompetencia,
    esperarTextoVisivel,
    TEXTOS,
    verificarBotaoDisponibilizarHabilitado,
    verificarCampoObservacoesValor,
    verificarModalDisponibilizacaoVisivel,
    verificarModalFechado,
    criarProcessoCompleto,
    iniciarProcesso,
    gerarNomeUnico,
    navegarParaMapaMapeamento,
    confirmarNoModal,
    cancelarNoModal,
} from './helpers';

let processo: any;
const siglaUnidade = 'SESEL';

test.describe('CDU-17: Disponibilizar mapa de competências', () => {
    test.beforeEach(async ({page}) => {
        const nomeProcesso = gerarNomeUnico('PROCESSO-CDU-17');
        processo = await criarProcessoCompleto(page, nomeProcesso, 'MAPEAMENTO', '2025-12-31', [10]); // Unidade 10 = SESEL
        await iniciarProcesso(page);
    });

    test('deve exibir modal com título e campos corretos', async ({page}) => {
        await navegarParaMapaMapeamento(page, processo.processo.codigo, siglaUnidade);
        await criarCompetencia(page, 'Competência Teste', []);

        await disponibilizarCadastro(page);
        await verificarModalDisponibilizacaoVisivel(page);
    });

    test('deve preencher observações no modal', async ({page}) => {
        await navegarParaMapaMapeamento(page, processo.processo.codigo, siglaUnidade);
        await criarCompetencia(page, 'Competência Teste', []);

        await disponibilizarCadastro(page);
        await verificarCampoObservacoesValor(page, 'Observações de teste para CDU-17');
    });

    test('deve validar data obrigatória', async ({page}) => {
        await navegarParaMapaMapeamento(page, processo.processo.codigo, siglaUnidade);
        await criarCompetencia(page, 'Competência Teste', []);

        await disponibilizarCadastro(page);
        await verificarBotaoDisponibilizarHabilitado(page, false);

        await verificarBotaoDisponibilizarHabilitado(page, true);
    });

    test('deve validar campos obrigatórios do modal', async ({page}) => {
        await navegarParaMapaMapeamento(page, processo.processo.codigo, siglaUnidade);
        await criarCompetencia(page, 'Competência para Validação', []);

        await disponibilizarCadastro(page);
        await verificarModalDisponibilizacaoVisivel(page);

        await verificarBotaoDisponibilizarHabilitado(page, false);
        await verificarBotaoDisponibilizarHabilitado(page, true);

        await verificarCampoObservacoesValor(page, 'Teste de observações');
    });

    test('deve processar disponibilização', async ({page}) => {
        await navegarParaMapaMapeamento(page, processo.processo.codigo, siglaUnidade);
        await criarCompetencia(page, 'Competência para Disponibilizar', []);

        await disponibilizarCadastro(page);

        await verificarBotaoDisponibilizarHabilitado(page, true);
        await confirmarNoModal(page);
    });

    test('deve cancelar disponibilização', async ({page}) => {
        await navegarParaMapaMapeamento(page, processo.processo.codigo, siglaUnidade);
        await criarCompetencia(page, 'Competência Teste', []);

        await disponibilizarCadastro(page);
        await cancelarNoModal(page);

        await verificarModalFechado(page);
        await esperarTextoVisivel(page, TEXTOS.MAPA_COMPETENCIAS_TECNICAS);
    });
});
