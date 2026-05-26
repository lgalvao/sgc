import {computed, nextTick, ref, type ComputedRef, type Ref} from "vue";
import type {Atividade, ErroValidacao} from "@/types/tipos";
import {SituacaoSubprocesso} from "@/types/tipos";
import {TEXTOS} from "@/constants/textos";
import {formatSituacaoSubprocesso} from "@/utils/formatters";
import {normalizarErro} from "@/utils/apiError";

const {atividades: TEXTOS_ATIVIDADES, comum: TEXTOS_COMUM} = TEXTOS;

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
    scrollParaPrimeiroErro: () => void;
    validarCadastro: (codigoSubprocesso: number) => Promise<ResultadoValidacaoCadastro>;
    disponibilizarCadastroFluxo: (codigoSubprocesso: number) => Promise<unknown>;
    disponibilizarRevisaoCadastroFluxo: (codigoSubprocesso: number) => Promise<unknown>;
};

type ResultadoValidacaoLocal =
    | {tipo: "pode-validar"}
    | {tipo: "erro-validacao"; erros: ErroValidacao[]}
    | {tipo: "acao-nao-permitida"; mensagem: string};

export function useCadastroDisponibilizacao(dependencias: DependenciasCadastroDisponibilizacao) {
    const estado = criarEstado(dependencias.atividades);

    async function disponibilizarCadastro() {
        if (estado.loadingValidacao.value) {
            return;
        }

        estado.limparErrosValidacao();
        const validacaoLocal = validarLocalmente(dependencias);

        if (validacaoLocal.tipo === "erro-validacao") {
            estado.aplicarErrosValidacao(validacaoLocal.erros);
            await nextTick();
            return;
        }
        if (validacaoLocal.tipo === "acao-nao-permitida") {
            estado.definirErroGlobal(validacaoLocal.mensagem);
            return;
        }

        estado.loadingValidacao.value = true;
        try {
            const resultado = await dependencias.validarCadastro(dependencias.codigoSubprocesso.value!);
            if (resultado.valido) {
                dependencias.mostrarModalConfirmacao.value = true;
                return;
            }

            estado.aplicarErrosValidacao(resultado.erros);
            await nextTick();
            dependencias.scrollParaPrimeiroErro();
        } catch (error) {
            estado.definirErroGlobal(normalizarErro(error).mensagem);
        } finally {
            estado.loadingValidacao.value = false;
        }
    }

    async function confirmarDisponibilizacao() {
        if (estado.loadingDisponibilizacao.value) {
            return;
        }

        estado.loadingDisponibilizacao.value = true;
        try {
            if (dependencias.isRevisao.value) {
                await dependencias.disponibilizarRevisaoCadastroFluxo(dependencias.codigoSubprocesso.value!);
            } else {
                await dependencias.disponibilizarCadastroFluxo(dependencias.codigoSubprocesso.value!);
            }
        } finally {
            estado.loadingDisponibilizacao.value = false;
        }
        dependencias.mostrarModalConfirmacao.value = false;
    }

    return {
        erroGlobal: estado.erroGlobal,
        erroTick: estado.erroTick,
        errosValidacao: estado.errosValidacao,
        loadingValidacao: estado.loadingValidacao,
        loadingDisponibilizacao: estado.loadingDisponibilizacao,
        limparErrosValidacao: estado.limparErrosValidacao,
        disponibilizarCadastro,
        confirmarDisponibilizacao,
        obterErroParaAtividade: (atividadeCodigo: number) => estado.mapaErros.value.get(atividadeCodigo),
    };
}

function validarLocalmente(dependencias: DependenciasCadastroDisponibilizacao): ResultadoValidacaoLocal {
    const erroPreValidacao = obterErroPreValidacao(dependencias);
    if (erroPreValidacao) {
        return {
            tipo: "erro-validacao",
            erros: [{tipo: "PRE_VALIDACAO", mensagem: erroPreValidacao}]
        };
    }

    const situacaoReferencia = obterSituacaoReferencia(dependencias.isRevisao.value);
    if (dependencias.situacaoAtual.value !== situacaoReferencia) {
        return {
            tipo: "acao-nao-permitida",
            mensagem: TEXTOS_COMUM.ACAO_NAO_PERMITIDA_SITUACAO(formatSituacaoSubprocesso(situacaoReferencia))
        };
    }

    return {tipo: "pode-validar"};
}

function obterSituacaoReferencia(isRevisao: boolean) {
    return isRevisao
        ? SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO
        : SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO;
}

function obterErroPreValidacao({
    atividades,
    isRevisao,
    houveAlteracaoCadastro,
    disponibilizacaoSemMudancas,
}: Pick<DependenciasCadastroDisponibilizacao, "atividades" | "isRevisao" | "houveAlteracaoCadastro" | "disponibilizacaoSemMudancas">) {
    const cadastroIncompleto = atividades.value.length === 0
        || atividades.value.some((atividade) => !atividade.conhecimentos || atividade.conhecimentos.length === 0);

    if (cadastroIncompleto) {
        return TEXTOS_ATIVIDADES.ERRO_CADASTRO_INCOMPLETO;
    }
    if (isRevisao.value && !houveAlteracaoCadastro.value && !disponibilizacaoSemMudancas.value) {
        return TEXTOS_ATIVIDADES.ERRO_REVISAO_SEM_ALTERACAO;
    }
    return null;
}

function criarEstado(atividades: Ref<Atividade[]>) {
    const loadingValidacao = ref(false);
    const loadingDisponibilizacao = ref(false);
    const errosValidacao = ref<ErroValidacao[]>([]);
    const erroGlobal = ref<string | null>(null);
    const erroTick = ref(0);

    const mapaErros = computed(() => {
        const mapa = new Map<number, string>();
        for (const erro of errosValidacao.value) {
            if (!erro.atividadeCodigo) {
                continue;
            }
            const atividade = atividades.value.find((item) => item.codigo === erro.atividadeCodigo);
            if (!atividade?.conhecimentos || atividade.conhecimentos.length === 0) {
                mapa.set(erro.atividadeCodigo, erro.mensagem);
            }
        }
        return mapa;
    });

    function limparErrosValidacao() {
        errosValidacao.value = [];
        erroGlobal.value = null;
    }

    function definirErroGlobal(mensagem: string) {
        limparErrosValidacao();
        erroGlobal.value = mensagem;
    }

    function aplicarErrosValidacao(erros: ErroValidacao[]) {
        limparErrosValidacao();
        erroTick.value++;
        errosValidacao.value = erros;
        erroGlobal.value = erros.find((erro) => !erro.atividadeCodigo)?.mensagem ?? null;
    }

    return {
        loadingValidacao,
        loadingDisponibilizacao,
        errosValidacao,
        erroGlobal,
        erroTick,
        mapaErros,
        limparErrosValidacao,
        definirErroGlobal,
        aplicarErrosValidacao,
    };
}
