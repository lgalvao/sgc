import {vueTest as test} from './support/vue-specific-setup';
import { loginComoAdmin } from './helpers/auth';
import {
    irParaMapaCompetencias,
    navegarParaCadastroAtividades,
} from './helpers/navegacao/navegacao';
import {
    adicionarAtividade,
    adicionarConhecimentoNaAtividade,
} from './helpers/acoes/acoes-atividades';
import {
    clicarBotaoImpactosMapa,
    fecharModalImpactos,
} from './helpers/acoes/acoes-mapa';
import {
    verificarMensagemNenhumImpacto,
    verificarModalImpactosAberto,
    verificarModalImpactosFechado,
} from './helpers/verificacoes/verificacoes-ui';
import { criarProcesso, submeterProcesso } from './helpers/acoes/api-helpers';
import { gerarNomeUnico } from './helpers/utils/utils';

test.describe('CDU-12: Verificar impactos no mapa de competências', () => {
    const UNIDADE_SESEL = 'SESEL';
    let processo: any;

    test.beforeEach(async ({ page }) => {
        const nomeProcesso = gerarNomeUnico('PROCESSO-CDU-12');
        const processoId = await criarProcesso(page, 'REVISAO', nomeProcesso, ['10']); // Unidade 10 = SESEL
        await submeterProcesso(page, processoId);
        processo = { processo: { codigo: processoId } };
        await loginComoAdmin(page);
    });

    test('deve exibir mensagem de "Nenhum impacto" quando não houver divergências', async ({page}) => {
        await irParaMapaCompetencias(page, processo.processo.codigo, UNIDADE_SESEL);
        await clicarBotaoImpactosMapa(page);
        await verificarMensagemNenhumImpacto(page);
    });

    test('deve exibir modal com impactos quando houver divergências', async ({page}) => {
        // Adiciona um conhecimento para gerar um impacto
        await navegarParaCadastroAtividades(page, processo.processo.codigo, UNIDADE_SESEL);
        const nomeAtividade = gerarNomeUnico('Atividade Impacto');
        await adicionarAtividade(page, nomeAtividade);
        await adicionarConhecimentoNaAtividade(page, nomeAtividade, gerarNomeUnico('Conhecimento Impacto'));

        // Navega para o mapa e abre o modal de impactos
        await irParaMapaCompetencias(page, processo.processo.codigo, UNIDADE_SESEL);
        await clicarBotaoImpactosMapa(page);

        // Confere se o modal de impactos está correto
        await verificarModalImpactosAberto(page);

        // Fecha o modal
        await fecharModalImpactos(page);
        await verificarModalImpactosFechado(page);
    });
});
