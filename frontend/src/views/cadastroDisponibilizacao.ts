import {computed, type ComputedRef, ref, type Ref} from "vue";
import type {Atividade, ErroValidacao} from "@/types/tipos";
import {SituacaoSubprocesso} from "@/types/tipos";
import {TEXTOS} from "@/constants/textos";
import {formatSituacaoSubprocesso} from "@/utils/formatters";
import {normalizarErro} from "@/utils/apiError";
import logger from "@/utils/logger";

type ResultadoValidacaoCadastro = {
    valido: boolean;
    erros: ErroValidacao[];
};

type DependenciasCadastroDisponibilizacao = {
    atividades: Ref<Atividade[]>;
    codigoSubprocesso: Ref<number | null>;
    situacaoAtual: ComputedRef<SituacaoSubprocesso | string | undefined>;
    isRevisao: ComputedRef<boolean>;
    houveAlteracaoCadastro: ComputedRef<boolean>;
    disponibilizacaoSemMudancas: Ref<boolean>;
    mostrarModalConfirmacao: Ref<boolean>;
    nextTick: typeof import("vue").nextTick;
    scrollParaPrimeiroErro: () => void;
    validarCadastro: (codigoSubprocesso: number) => Promise<ResultadoValidacaoCadastro | null | undefined>;
    disponibilizarCadastroFluxo: (codigoSubprocesso: number, isRevisao: boolean) => Promise<unknown>;
};

export function useCadastroDisponibilizacao({
    atividades,
    codigoSubprocesso,
    situacaoAtual,
    isRevisao,
    houveAlteracaoCadastro,
    disponibilizacaoSemMudancas,
    mostrarModalConfirmacao,
    nextTick,
    scrollParaPrimeiroErro,
    validarCadastro,
    disponibilizarCadastroFluxo,
}: DependenciasCadastroDisponibilizacao) {
    const loadingValidacao = ref(false);
    const loadingDisponibilizacao = ref(false);
    const errosValidacao = ref<ErroValidacao[]>([]);
    const erroGlobal = ref<string | null>(null);
    const erroTick = ref(0);

    const mapaErros = computed(() => {
        const mapa = new Map<number, string>();
        errosValidacao.value.forEach((erro) => {
            if (!erro.atividadeCodigo) return;
            const atividade = atividades.value.find((item) => item.codigo === erro.atividadeCodigo);
            if (!atividade || !atividade.conhecimentos || atividade.conhecimentos.length === 0) {
                mapa.set(erro.atividadeCodigo, erro.mensagem);
            }
        });
        return mapa;
    });

    function limparErrosValidacaoCadastro() {
        errosValidacao.value = [];
        erroGlobal.value = null;
    }

    async function registrarErrosValidacaoCadastro(erros: ErroValidacao[]) {
        limparErrosValidacaoCadastro();
        erroTick.value++;

        await nextTick();
        errosValidacao.value = erros;
        erroGlobal.value = erros.find((erro) => !erro.atividadeCodigo)?.mensagem ?? null;
    }

    function obterSituacaoReferenciaDisponibilizacao() {
        return isRevisao.value
            ? SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO
            : SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO;
    }

    function obterErroPreValidacaoDisponibilizacao() {
        const cadastroIncompleto = atividades.value.length === 0
            || atividades.value.some((atividade) => !atividade.conhecimentos || atividade.conhecimentos.length === 0);

        if (cadastroIncompleto) return TEXTOS.atividades.ERRO_CADASTRO_INCOMPLETO;
        if (isRevisao.value && !houveAlteracaoCadastro.value && !disponibilizacaoSemMudancas.value) {
            return TEXTOS.atividades.ERRO_REVISAO_SEM_ALTERACAO;
        }
        return null;
    }

    async function aplicarResultadoValidacaoCadastro(valido: boolean, erros: ErroValidacao[]) {
        if (valido) {
            mostrarModalConfirmacao.value = true;
            return;
        }

        await registrarErrosValidacaoCadastro(erros);
    }

    async function disponibilizarCadastro() {
        if (loadingValidacao.value) return;

        limparErrosValidacaoCadastro();

        const situacaoReferencia = obterSituacaoReferenciaDisponibilizacao();
        const erroPreValidacao = obterErroPreValidacaoDisponibilizacao();
        if (erroPreValidacao) {
            await registrarErrosValidacaoCadastro([{tipo: "PRE_VALIDACAO", mensagem: erroPreValidacao}]);
            return;
        }

        if (!situacaoAtual.value || situacaoAtual.value !== situacaoReferencia) {
            erroGlobal.value = TEXTOS.comum.ACAO_NAO_PERMITIDA_SITUACAO(formatSituacaoSubprocesso(situacaoReferencia));
            return;
        }

        const codigo = codigoSubprocesso.value;
        if (!codigo) {
            erroGlobal.value = "Identificador do subprocesso não encontrado. Recarregue a página.";
            return;
        }

        loadingValidacao.value = true;
        try {
            const resultado = await validarCadastro(codigo);
            if (!resultado) {
                erroGlobal.value = "Não foi possível obter o resultado da validação. Tente novamente.";
                return;
            }

            await aplicarResultadoValidacaoCadastro(resultado.valido, resultado.erros);
            if (resultado.valido) return;

            await nextTick();
            try {
                scrollParaPrimeiroErro();
            } catch (erroDom) {
                logger.warn("Falha ao executar scroll para erro", erroDom);
            }
        } catch (error) {
            erroGlobal.value = normalizarErro(error).mensagem;
        } finally {
            loadingValidacao.value = false;
        }
    }

    async function confirmarDisponibilizacao() {
        const codigo = codigoSubprocesso.value;
        if (!codigo || loadingDisponibilizacao.value) return;

        loadingDisponibilizacao.value = true;
        try {
            await disponibilizarCadastroFluxo(codigo, isRevisao.value);
        } finally {
            loadingDisponibilizacao.value = false;
        }
        mostrarModalConfirmacao.value = false;
    }

    function obterErroParaAtividade(atividadeCodigo: number) {
        return mapaErros.value.get(atividadeCodigo);
    }

    return {
        erroGlobal,
        erroTick,
        errosValidacao,
        loadingValidacao,
        loadingDisponibilizacao,
        limparErrosValidacaoCadastro,
        disponibilizarCadastro,
        confirmarDisponibilizacao,
        obterErroParaAtividade,
    };
}
