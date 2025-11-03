import {vueTest as test} from './support/vue-specific-setup';
import { loginComoAdmin, loginComoChefe, loginComoGestor } from './helpers/auth';
import {
    navegarParaProcessoPorId,
} from './helpers/navegacao/navegacao';
import {
    adicionarAtividade,
    adicionarConhecimentoNaAtividade,
} from './helpers/acoes/acoes-atividades';
import {
    verificarAtividadeVisivel,
    verificarConhecimentoNaAtividade,
} from './helpers/verificacoes/verificacoes-atividades';
import { verificarModoSomenteLeitura } from './helpers/verificacoes/verificacoes-ui';
import { criarProcesso, submeterProcesso } from './helpers/acoes/api-helpers';
import { gerarNomeUnico } from './helpers/utils/utils';

test.describe('CDU-11: Visualizar cadastro de atividades (somente leitura)', () => {
    let processo: any;
    const nomeAtividade = gerarNomeUnico('Atividade para Visualizar');
    const nomeConhecimento = gerarNomeUnico('Conhecimento para Visualizar');

    async function setup(page) {
        // Setup: Cria um processo, adiciona dados e disponibiliza o cadastro
        const processoId = await criarProcesso(page, 'MAPEAMENTO', gerarNomeUnico('PROCESSO CDU-11'), ['1']);
        await submeterProcesso(page, processoId);
        processo = { codigo: processoId };

        // Adiciona uma atividade e conhecimento via UI para simular o fluxo real
        await loginComoChefe(page);
        await navegarParaProcessoPorId(page, processo.codigo);
        await page.getByRole('link', { name: 'STIC' }).click();
        await adicionarAtividade(page, nomeAtividade);
        await adicionarConhecimentoNaAtividade(page, nomeAtividade, nomeConhecimento);
        await page.getByRole('button', { name: 'Disponibilizar' }).click();
    }

    test.beforeEach(async ({page}) => {
        await setup(page);
    });

    test('ADMIN deve visualizar cadastro em modo somente leitura', async ({page}) => {
        await loginComoAdmin(page);
        await navegarParaProcessoPorId(page, processo.codigo);
        await page.getByRole('link', { name: 'STIC' }).click();

        await verificarAtividadeVisivel(page, nomeAtividade);
        await verificarConhecimentoNaAtividade(page, nomeAtividade, nomeConhecimento);
        await verificarModoSomenteLeitura(page);
    });

    test('GESTOR da unidade superior deve visualizar cadastro em modo somente leitura', async ({page}) => {
        await loginComoGestor(page); // Gestor da SGP (unidade superior à STIC)
        await navegarParaProcessoPorId(page, processo.codigo);
        await page.getByRole('link', { name: 'STIC' }).click();

        await verificarAtividadeVisivel(page, nomeAtividade);
        await verificarConhecimentoNaAtividade(page, nomeAtividade, nomeConhecimento);
        await verificarModoSomenteLeitura(page);
    });

    test('CHEFE de outra unidade não deve ver os botões de edição', async ({page}) => {
        // Loga como chefe de uma unidade que não é a STIC, mas está no processo
        await criarProcesso(page, 'MAPEAMENTO', gerarNomeUnico('PROCESSO CDU-11 OUTRA UNIDADE'), ['3']); // Adiciona a unidade 3 (SESEL)
        await loginComoChefe(page);
        await navegarParaProcessoPorId(page, processo.codigo);
        await page.getByRole('link', { name: 'STIC' }).click();

        await verificarAtividadeVisivel(page, nomeAtividade);
        await verificarConhecimentoNaAtividade(page, nomeAtividade, nomeConhecimento);
        await verificarModoSomenteLeitura(page);
    });
});
