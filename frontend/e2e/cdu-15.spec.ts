import {vueTest as test} from './support/vue-specific-setup';
import {
    criarProcessoCompleto,
    gerarNomeUnico,
    loginComoAdmin,
    navegarParaEdicaoMapa,
    criarCompetencia,
    editarCompetencia,
    excluirCompetencia,
    verificarCompetenciaExiste,
    verificarCompetenciaNaoExiste,
    verificarAtividadesAssociadas,
    verificarDescricaoCompetencia,
    clicarBotaoDisponibilizar,
    preencherDataModal,
    confirmarNoModal,
    esperarUrl
} from './helpers';

test.describe('CDU-15: Manter Mapa de Competências', () => {
    let processo: any;
    const nomeProcesso = gerarNomeUnico('PROCESSO CDU-15');
    const siglaUnidade = 'STIC'; // Assumindo uma unidade padrão para o teste

    test.beforeEach(async ({page}) => {
        await loginComoAdmin(page);
        processo = await criarProcessoCompleto(page, nomeProcesso, 'MAPEAMENTO', '2025-12-31', [1]);
        await navegarParaEdicaoMapa(page, processo.processo.codigo, siglaUnidade);
    });

    test('deve permitir criar uma nova competência', async ({page}) => {
        const descricaoCompetencia = 'Nova Competência de Teste';
        const atividadesParaAssociar = ['Atividade 1', 'Atividade 2']; // Assumindo que estas atividades existem

        await criarCompetencia(page, descricaoCompetencia, atividadesParaAssociar);
        await verificarCompetenciaExiste(page, descricaoCompetencia);
        await verificarAtividadesAssociadas(page, descricaoCompetencia, atividadesParaAssociar);
    });

    test('deve permitir editar uma competência existente', async ({page}) => {
        const descricaoOriginal = 'Competência a Ser Editada';
        const descricaoEditada = 'Competência Editada';
        const atividadesOriginais = ['Atividade 1'];
        const atividadesEditadas = ['Atividade 2', 'Atividade 3'];

        // Criar uma competência para editar
        await criarCompetencia(page, descricaoOriginal, atividadesOriginais);
        await verificarCompetenciaExiste(page, descricaoOriginal);

        await editarCompetencia(page, descricaoOriginal, descricaoEditada, atividadesEditadas);
        await verificarCompetenciaNaoExiste(page, descricaoOriginal); // A descrição original não deve mais existir
        await verificarCompetenciaExiste(page, descricaoEditada);
        await verificarAtividadesAssociadas(page, descricaoEditada, atividadesEditadas);
    });

    test('deve permitir excluir uma competência', async ({page}) => {
        const descricaoCompetencia = 'Competência a Ser Excluída';
        const atividadesParaAssociar = ['Atividade 1'];

        // Criar uma competência para excluir
        await criarCompetencia(page, descricaoCompetencia, atividadesParaAssociar);
        await verificarCompetenciaExiste(page, descricaoCompetencia);

        await excluirCompetencia(page, descricaoCompetencia);
        await verificarCompetenciaNaoExiste(page, descricaoCompetencia);
    });

    test('deve permitir disponibilizar o mapa após edições', async ({page}) => {
        const descricaoCompetencia = 'Competência para Disponibilizar';
        const atividadesParaAssociar = ['Atividade 1'];

        await criarCompetencia(page, descricaoCompetencia, atividadesParaAssociar);
        await verificarCompetenciaExiste(page, descricaoCompetencia);

        await clicarBotaoDisponibilizar(page);
        await preencherDataModal(page, '2025-12-31');
        await confirmarNoModal(page);

        await esperarUrl(page, new RegExp(`/processo/${processo.processo.codigo}/${siglaUnidade}$`));
    });
});
