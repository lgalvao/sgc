import {expect, test} from './fixtures/complete-fixtures.js';
import {login, USUARIOS} from './helpers/helpers-auth.js';
import {criarProcessoFinalizadoFixture, criarProcessoFixture} from './fixtures/fixtures-processos.js';
import {
    adicionarAtividade,
    adicionarConhecimento,
    navegarParaAtividades,
    navegarParaAtividadesVisualizacao,
    verificarBotaoImpactoDireto
} from './helpers/helpers-atividades.js';
import {
    abrirHistoricoAnalise,
    abrirHistoricoAnaliseVisualizacao,
    aceitarRevisao,
    acessarSubprocessoChefeDireto,
    acessarSubprocessoGestor,
    cancelarDevolucao,
    devolverRevisao,
    fecharHistoricoAnalise,
} from './helpers/helpers-analise.js';
import {navegarParaSubprocesso} from './helpers/helpers-navegacao.js';

test.describe.serial('CDU-14 - Analisar revisão de cadastro de atividades e conhecimentos', () => {
    const UNIDADE_ALVO = 'SECAO_212';
    const timestamp = Date.now();
    const descProcesso = `Processo CDU-14 ${timestamp}`;
    const atividadeRevisao = `Atividade rev ${timestamp}`;

    test('Setup UI', async ({_resetAutomatico, page, request}) => {

        // Preparacao 1: Base de dados com Mapa vigente e Revisão iniciada
        await criarProcessoFinalizadoFixture(request, {
            unidade: UNIDADE_ALVO,
            descricao: `Base map CDU-14 ${timestamp}`
        });

        await criarProcessoFixture(request, {
            descricao: descProcesso,
            tipo: 'REVISAO',
            unidade: UNIDADE_ALVO,
            iniciar: true
        });

        // Preparacao 4: CHEFE revisa e disponibiliza
        await login(page, USUARIOS.CHEFE_SECAO_212.titulo, USUARIOS.CHEFE_SECAO_212.senha);
        await acessarSubprocessoChefeDireto(page, descProcesso, UNIDADE_ALVO);
        await navegarParaAtividades(page);

        await adicionarAtividade(page, atividadeRevisao);
        await adicionarConhecimento(page, atividadeRevisao, 'Conhecimento rev');

        await page.getByTestId('btn-cad-atividades-disponibilizar').click();
        await page.getByTestId('btn-confirmar-disponibilizacao').click();

        // Preparacao 5: GESTOR visualiza histórico, impactos e devolve
        await login(page, USUARIOS.GESTOR_COORD_21.titulo, USUARIOS.GESTOR_COORD_21.senha);
        await acessarSubprocessoGestor(page, descProcesso, UNIDADE_ALVO);
        await navegarParaAtividadesVisualizacao(page);

        const modalVisualizacao = await abrirHistoricoAnaliseVisualizacao(page);
        await expect(modalVisualizacao).toBeVisible();
        await fecharHistoricoAnalise(page);

        await verificarBotaoImpactoDireto(page);
        await devolverRevisao(page, 'Favor revisar as competências associadas');

        // Preparacao 6: CHEFE vê devolução, ajusta e redisponibiliza
        await login(page, USUARIOS.CHEFE_SECAO_212.titulo, USUARIOS.CHEFE_SECAO_212.senha);
        await acessarSubprocessoChefeDireto(page, descProcesso, UNIDADE_ALVO);
        await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Revisão em andamento/i);

        await navegarParaAtividades(page);
        const modalAnalise = await abrirHistoricoAnalise(page);
        await expect(modalAnalise.getByTestId('cell-resultado-0')).toHaveText(/Devolu[cç][aã]o/i);
        await fecharHistoricoAnalise(page);

        await page.getByTestId('btn-cad-atividades-disponibilizar').click();
        await page.getByTestId('btn-confirmar-disponibilizacao').click();
    });

    test('Cenarios CDU-14: GESTOR cancela devolução, aceita e ADMIN vê histórico final', async ({_resetAutomatico, page}) => {
        await login(page, USUARIOS.GESTOR_COORD_21.titulo, USUARIOS.GESTOR_COORD_21.senha);
        await acessarSubprocessoGestor(page, descProcesso, UNIDADE_ALVO);
        await navegarParaAtividadesVisualizacao(page);

        await cancelarDevolucao(page);
        await expect(page.getByRole('heading', {name: 'Atividades e conhecimentos'})).toBeVisible();
        await aceitarRevisao(page, 'Revisão aprovada conforme análise');

        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
        await page.getByTestId('tbl-processos').getByText(descProcesso).first().click();
        await navegarParaSubprocesso(page, UNIDADE_ALVO);
        await navegarParaAtividadesVisualizacao(page);
        const modal = await abrirHistoricoAnaliseVisualizacao(page);
        await expect(modal.getByTestId('cell-resultado-0')).toHaveText(/ACEITE_REVISAO/i);
        await fecharHistoricoAnalise(page);
    });
});
