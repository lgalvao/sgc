import {vueTest as test} from '../support/vue-specific-setup';
import {
    abrirModalFinalizacaoProcesso,
    cancelarModal,
    clicarBotaoFinalizarProcesso,
    DADOS_TESTE,
    finalizarProcesso,
    loginComoAdmin,
    loginComoGestor,
    navegarParaProcessoPorId,
    selecionarPrimeiroProcessoPorSituacao,
    TEXTOS,
    verificarBotaoFinalizarProcessoInvisivel,
    verificarBotaoFinalizarProcessoVisivel,
    verificarEmailFinalizacaoEnviado,
    verificarFinalizacaoBloqueada,
    verificarMapasVigentesNotificacao,
    verificarMensagemFinalizacaoSucesso,
    verificarModalFinalizacaoFechado,
    verificarModalFinalizacaoProcesso,
    verificarPainelBasico,
    verificarProcessoFinalizadoNoPainel
} from './helpers';

const PROCESSO_FINALIZACAO = DADOS_TESTE.PROCESSOS.TESTE_FINALIZACAO;
const PROCESSO_MAPEAMENTO = DADOS_TESTE.PROCESSOS.TESTE_MAPEAMENTO;
const PROCESSO_REVISAO = DADOS_TESTE.PROCESSOS.TESTE_REVISAO;
const ID_PROCESSO = PROCESSO_FINALIZACAO.id;

test.describe('CDU-21: Finalizar processo de mapeamento ou de revisão', () => {
    test.describe('Administrador', () => {
        test.beforeEach(async ({page}) => {
            await loginComoAdmin(page);
            await verificarPainelBasico(page);
        });

        test('deve exibir botão Finalizar para processos em andamento', async ({page}) => {
            await selecionarPrimeiroProcessoPorSituacao(page, TEXTOS.EM_ANDAMENTO);
            await verificarBotaoFinalizarProcessoVisivel(page);
        });

        test('deve bloquear finalização quando existem unidades não homologadas', async ({page}) => {
            await selecionarPrimeiroProcessoPorSituacao(page, TEXTOS.EM_ANDAMENTO);
            await clicarBotaoFinalizarProcesso(page);
            await verificarFinalizacaoBloqueada(page);
        });

        test('deve apresentar modal de confirmação com informações completas', async ({page}) => {
            await navegarParaProcessoPorId(page, ID_PROCESSO);
            await abrirModalFinalizacaoProcesso(page);
            await verificarModalFinalizacaoProcesso(page);
        });

        test('deve cancelar a finalização e permanecer na tela do processo', async ({page}) => {
            await navegarParaProcessoPorId(page, ID_PROCESSO);
            await abrirModalFinalizacaoProcesso(page);
            await cancelarModal(page);

            await verificarModalFinalizacaoFechado(page);
        });

        test('deve finalizar processo com sucesso e marcar situação como Finalizado', async ({page}) => {
            await navegarParaProcessoPorId(page, ID_PROCESSO);
            await finalizarProcesso(page, ID_PROCESSO);

            await verificarMensagemFinalizacaoSucesso(page);
            await verificarProcessoFinalizadoNoPainel(page, PROCESSO_FINALIZACAO.nome);
        });

        test('deve informar vigência dos mapas ao finalizar processo de mapeamento', async ({page}) => {
            await navegarParaProcessoPorId(page, PROCESSO_MAPEAMENTO.id);
            await finalizarProcesso(page, PROCESSO_MAPEAMENTO.id);

            await verificarMapasVigentesNotificacao(page);
            await verificarProcessoFinalizadoNoPainel(page, PROCESSO_MAPEAMENTO.nome);
        });

        test('deve enviar notificações por e-mail ao finalizar processo de revisão', async ({page}) => {
            await navegarParaProcessoPorId(page, PROCESSO_REVISAO.id);
            await finalizarProcesso(page, PROCESSO_REVISAO.id);

            await verificarEmailFinalizacaoEnviado(page);
        });
    });

    test.describe('Restrições de perfil', () => {
        test('não deve exibir botão Finalizar para perfil Gestor', async ({page}) => {
            await loginComoGestor(page);
            await verificarPainelBasico(page);
            await navegarParaProcessoPorId(page, PROCESSO_FINALIZACAO.id);

            await verificarBotaoFinalizarProcessoInvisivel(page);
        });
    });
});