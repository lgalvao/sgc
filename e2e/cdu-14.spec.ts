import {expect, test} from './fixtures/complete-fixtures.js';
import {login, USUARIOS} from './helpers/helpers-auth.js';
import {criarProcessoFinalizadoFixture, criarProcessoFixture} from './fixtures/index.js';
import {
    abrirModalImpacto,
    adicionarAtividade,
    adicionarConhecimento,
    fecharModalImpacto,
    navegarParaCadastro,
    verificarBotaoImpactoDireto
} from './helpers/helpers-atividades.js';
import {
    abrirHistoricoAnalise,
    aceitarRevisao,
    acessarSubprocessoChefeDireto,
    acessarSubprocessoGestor,
    cancelarDevolucao,
    devolverRevisao,
    fecharHistoricoAnalise
} from './helpers/helpers-analise.js';
import {navegarParaSubprocesso, verificarPaginaPainel, verificarToast} from './helpers/helpers-navegacao.js';
import {acessarDetalhesProcesso} from './helpers/helpers-processos.js';
import {TEXTOS} from '../frontend/src/constants/textos.js';
import {verificarNotificacaoAdmin} from './helpers/helpers-notificacoes-admin.js';

async function validarCabecalhosHistorico(modal: ReturnType<typeof abrirHistoricoAnalise> extends Promise<infer T> ? T : never) {
    await expect(modal.getByTestId('header-historico-dataHora')).toBeVisible();
    await expect(modal.getByTestId('header-historico-unidade')).toBeVisible();
    await expect(modal.getByTestId('header-historico-acao')).toBeVisible();
    await expect(modal.getByTestId('header-historico-usuario')).toBeVisible();
    await expect(modal.getByTestId('header-historico-observacao')).toBeVisible();
}

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
        await navegarParaCadastro(page);

        await adicionarAtividade(page, atividadeRevisao);
        await adicionarConhecimento(page, atividadeRevisao, 'Conhecimento rev');

        await page.getByTestId('btn-cad-atividades-disponibilizar').click();
        await page.getByTestId('btn-confirmar-disponibilizacao').click();
        await verificarToast(page, /disponibilizada?|Disponibilizado/i);
        await verificarPaginaPainel(page);

        // Preparacao 5: GESTOR visualiza histórico, impactos e devolve
        await login(page, USUARIOS.GESTOR_COORD_21.titulo, USUARIOS.GESTOR_COORD_21.senha);
        await acessarSubprocessoGestor(page, descProcesso, UNIDADE_ALVO);
        await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Revisão (do cadastro )?disponibilizada/i);
        await navegarParaCadastro(page);

        const modalVisualizacao = await abrirHistoricoAnalise(page);
        await expect(modalVisualizacao).toBeVisible();
        await fecharHistoricoAnalise(page);

        await verificarBotaoImpactoDireto(page);
        await abrirModalImpacto(page);
        await expect(page.getByTestId('modal-impacto-body')).toBeVisible();
        await expect(page.getByText(TEXTOS.mapa.impacto.ATIVIDADES_INSERIDAS)).toBeVisible();
        await fecharModalImpacto(page);
        await devolverRevisao(page, 'Favor revisar as competências associadas');

        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
        await verificarNotificacaoAdmin(page, {
            destinatario: UNIDADE_ALVO,
            assunto: `Revisão do cadastro de atividades e conhecimentos da ${UNIDADE_ALVO} devolvida para ajustes`,
            tipo: 'Revisão de cadastro devolvida',
            trechoCorpo: new RegExp(
                `A revisão do cadastro de atividades e conhecimentos da\\s*<strong>\\s*${UNIDADE_ALVO}\\s*</strong>\\s*no processo\\s*<strong>\\s*${descProcesso}\\s*</strong>\\s*foi devolvida para ajustes\\.`,
                'i'
            )
        });
        await page.getByTestId('nav-link-painel').click();
        await expect(page).toHaveURL(/\/painel(?:\?.*)?$/);

        // Preparacao 6: CHEFE vê devolução, ajusta e redisponibiliza
        await login(page, USUARIOS.CHEFE_SECAO_212.titulo, USUARIOS.CHEFE_SECAO_212.senha);
        await acessarSubprocessoChefeDireto(page, descProcesso, UNIDADE_ALVO);
        await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Revisão em andamento/i);

        await navegarParaCadastro(page);
        const modalAnalise = await abrirHistoricoAnalise(page);
        await validarCabecalhosHistorico(modalAnalise);
        await expect(modalAnalise.getByTestId('cell-dataHora-0')).not.toHaveText('');
        await expect(modalAnalise.getByTestId('cell-resultado-0')).toHaveText(/Devolu[cç][aã]o/i);
        await expect(modalAnalise.getByTestId('cell-observacao-0')).toHaveText('Favor revisar as competências associadas');
        await fecharHistoricoAnalise(page);

        await expect(page.getByTestId('chk-disponibilizacao-sem-mudancas')).toBeDisabled();
        await expect(page.getByTestId('btn-cad-atividades-disponibilizar')).toBeEnabled();
        await page.getByTestId('btn-cad-atividades-disponibilizar').click();
        await page.getByTestId('btn-confirmar-disponibilizacao').click();
        await verificarToast(page, /disponibilizada?|Disponibilizado/i);
        await verificarPaginaPainel(page);
    });

    test('Cenarios CDU-14: GESTOR cancela devolução, aceita e ADMIN vê histórico final', async ({
                                                                                                    _resetAutomatico,
                                                                                                    page
                                                                                                }) => {
        await login(page, USUARIOS.GESTOR_COORD_21.titulo, USUARIOS.GESTOR_COORD_21.senha);
        await acessarSubprocessoGestor(page, descProcesso, UNIDADE_ALVO);
        await navegarParaCadastro(page);

        await cancelarDevolucao(page);
        await expect(page.getByRole('heading', {name: 'Atividades e conhecimentos'})).toBeVisible();
        await aceitarRevisao(page, 'Revisão aprovada conforme análise');

        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
        await verificarNotificacaoAdmin(page, {
            destinatario: 'SECRETARIA_2',
            assunto: `Revisão do cadastro de atividades e conhecimentos da ${UNIDADE_ALVO} submetido para análise`,
            tipo: 'Revisão de cadastro aceita',
            trechoCorpo: new RegExp(
                `A revisão do cadastro de atividades e conhecimentos da\\s*<strong>\\s*${UNIDADE_ALVO}\\s*</strong>\\s*no processo\\s*<strong>\\s*${descProcesso}\\s*</strong>\\s*foi submetida para análise\\.`,
                'i'
            )
        });
        await page.getByTestId('nav-link-painel').click();
        await expect(page).toHaveURL(/\/painel(?:\?.*)?$/);
        await acessarDetalhesProcesso(page, descProcesso);
        await navegarParaSubprocesso(page, UNIDADE_ALVO);
        await navegarParaCadastro(page);
        const modal = await abrirHistoricoAnalise(page);
        await validarCabecalhosHistorico(modal);
        await expect(modal.getByTestId('cell-dataHora-0')).not.toHaveText('');
        await expect(modal.getByTestId('cell-resultado-0')).toHaveText(/Aceite/i);
        await fecharHistoricoAnalise(page);
    });
});
