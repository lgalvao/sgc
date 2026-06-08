import {computed, ref, type Ref} from "vue";
import type {Atividade, ErroValidacao} from "@/types/tipos";
import {normalizarErro} from "@/utils/apiError";

interface UseCadastroErrosParams {
    atividades: Ref<Atividade[]>;
    erroAtualFluxo: Ref<{
        tipo?: string;
        mensagem?: string;
        erros?: Array<{campo?: string | null; mensagem?: string}>;
    } | null>;
}

export function useCadastroErros({atividades, erroAtualFluxo}: UseCadastroErrosParams) {
    const errosValidacao = ref<ErroValidacao[]>([]);
    const erroGlobal = ref<string | null>(null);
    const erroTick = ref(0);

    const mapaErros = computed(() => {
        const mapa = new Map<number, string>();
        for (const erro of errosValidacao.value) {
            if (!erro.atividadeCodigo) continue;
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

    function definirErroGlobalDoErro(error: unknown, mensagemPadrao = "Não foi possível concluir a operação do cadastro.") {
        definirErroGlobal(normalizarErro(error).mensagem || mensagemPadrao);
    }

    function aplicarErrosValidacao(erros: ErroValidacao[]) {
        limparErrosValidacao();
        erroTick.value++;
        errosValidacao.value = erros;
        erroGlobal.value = erros.find((erro) => !erro.atividadeCodigo)?.mensagem ?? null;
    }

    function obterErroParaAtividade(atividadeCodigo: number) {
        return mapaErros.value.get(atividadeCodigo);
    }

    function obterErroCampoFluxo(campos: string[]) {
        return erroAtualFluxo.value?.erros?.find((erro) => erro.campo != null && campos.includes(erro.campo))?.mensagem ?? "";
    }

    const erroFluxoCadastro = computed(() =>
        erroAtualFluxo.value?.tipo === "validacao"
            ? undefined
            : erroAtualFluxo.value?.mensagem,
    );

    return {
        errosValidacao,
        erroGlobal,
        erroTick,
        limparErrosValidacao,
        definirErroGlobal,
        definirErroGlobalDoErro,
        aplicarErrosValidacao,
        obterErroParaAtividade,
        obterErroCampoFluxo,
        erroFluxoCadastro,
    };
}
