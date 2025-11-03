import {vueTest as test} from './support/vue-specific-setup';
import { loginComoAdmin } from './helpers/auth';
import { navegarParaEdicaoMapa } from './helpers/navegacao/navegacao';
import {
    criarCompetencia,
    editarCompetencia,
    excluirCompetencia,
} from './helpers/acoes/acoes-mapa';
import {
    verificarCompetenciaVisivel,
    verificarCompetenciaNaoVisivel,
    verificarAtividadesAssociadas,
} from './helpers/verificacoes/verificacoes-ui';
import { esperarUrl } from './helpers/verificacoes/verificacoes-basicas';
import { criarProcesso, submeterProcesso } from './helpers/acoes/api-helpers';
import { gerarNomeUnico } from './helpers/utils/utils';

test.describe('CDU-15: Manter Mapa de Competências', () => {
    let processo: any;
    const nomeProcesso = gerarNomeUnico('PROCESSO CDU-15');
    const siglaUnidade = 'STIC'; // Assumindo uma unidade padrão para o teste

    test.beforeEach(async ({page}) => {
        await loginComoAdmin(page);
        const processoId = await criarProcesso(page, 'MAPEAMENTO', nomeProcesso, ['1']);
        await submeterProcesso(page, processoId);
        processo = { processo: { codigo: processoId } };
        await navegarParaEdicaoMapa(page, processo.processo.codigo, siglaUnidade);
    });

    test('deve permitir criar uma nova competência', async ({page}) => {
        const descricaoCompetencia = 'Nova Competência de Teste';
        const atividadesParaAssociar = ['Atividade 1', 'Atividade 2']; // Assumindo que estas atividades existem

        await criarCompetencia(page, descricaoCompetencia, atividadesParaAssociar);
        await verificarCompetenciaVisivel(page, descricaoCompetencia);
        await verificarAtividadesAssociadas(page, descricaoCompetencia, atividadesParaAssociar);
    });

    test('deve permitir editar uma competência existente', async ({page}) => {
        const descricaoOriginal = 'Competência a Ser Editada';
        const descricaoEditada = 'Competência Editada';
        const atividadesOriginais = ['Atividade 1'];
        const atividadesEditadas = ['Atividade 2', 'Atividade 3'];

        // Criar uma competência para editar
        await criarCompetencia(page, descricaoOriginal, atividadesOriginais);
        await verificarCompetenciaVisivel(page, descricaoOriginal);

        await editarCompetencia(page, descricaoOriginal, descricaoEditada, atividadesEditadas);
        await verificarCompetenciaNaoVisivel(page, descricaoOriginal); // A descrição original não deve mais existir
        await verificarCompetenciaVisivel(page, descricaoEditada);
        await verificarAtividadesAssociadas(page, descricaoEditada, atividadesEditadas);
    });

    test('deve permitir excluir uma competência', async ({page}) => {
        const descricaoCompetencia = 'Competência a Ser Excluída';

        // Criar uma competência para excluir
        await criarCompetencia(page, descricaoCompetencia, ['Atividade 1']);
        await verificarCompetenciaVisivel(page, descricaoCompetencia);

        await excluirCompetencia(page, descricaoCompetencia);
        await verificarCompetenciaNaoVisivel(page, descricaoCompetencia);
    });

    test('deve permitir disponibilizar o mapa após edições', async ({page}) => {
        const descricaoCompetencia = 'Competência para Disponibilizar';

        await criarCompetencia(page, descricaoCompetencia, ['Atividade 1']);
        await verificarCompetenciaVisivel(page, descricaoCompetencia);

        await page.getByRole('button', { name: 'Disponibilizar' }).click();

        await esperarUrl(page, new RegExp(`/processo/${processo.processo.codigo}/${siglaUnidade}$`));
    });
});
