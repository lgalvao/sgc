import {defineStore} from "pinia";
import {ref} from "vue";
import type {ContextoCadastroAtividadesSubprocesso, ContextoEdicaoSubprocesso} from "@/types/tipos";
import {
    type AtualizacaoStatusLocal,
    atualizarDetalhesContexto,
    registrarContexto
} from "@/stores/subprocessoStoreHelpers";
import type {ErroNormalizado} from "@/utils/apiError";
import {usarOrquestradorContexto} from "./orquestrador";
import {criarConfigs} from "./configs";

export const useSubprocessoStore = defineStore("subprocesso", () => {
    const contextoEdicao = ref<ContextoEdicaoSubprocesso | null>(null);
    const contextoCadastro = ref<ContextoCadastroAtividadesSubprocesso | null>(null);
    const contextoEdicaoInvalido = ref(false);
    const contextoCadastroInvalido = ref(false);
    const erroIntegracaoContexto = ref<ErroNormalizado | null>(null);
    const carregamentos = new Map<string, Promise<unknown>>();
    const codigosEdicaoPorProcessoUnidade = new Map<string, number>();
    const codigosCadastroPorProcessoUnidade = new Map<string, number>();

    function limparContextoAtual(): void {
        contextoEdicao.value = null;
        contextoCadastro.value = null;
        contextoEdicaoInvalido.value = false;
        contextoCadastroInvalido.value = false;
    }

    const limparErroIntegracao = () => { erroIntegracaoContexto.value = null; };

    function invalidar(): void {
        carregamentos.clear();
        contextoEdicaoInvalido.value = contextoEdicao.value !== null;
        contextoCadastroInvalido.value = contextoCadastro.value !== null;
        limparErroIntegracao();
    }

    function resetar(): void {
        carregamentos.clear();
        codigosEdicaoPorProcessoUnidade.clear();
        codigosCadastroPorProcessoUnidade.clear();
        limparErroIntegracao();
        limparContextoAtual();
    }

    const orquestrador = usarOrquestradorContexto(carregamentos, erroIntegracaoContexto, limparContextoAtual);
    const registrarContextoEdicao = (c: ContextoEdicaoSubprocesso) => registrarContexto(contextoEdicao, contextoEdicaoInvalido, c, limparErroIntegracao);
    const registrarContextoCadastro = (c: ContextoCadastroAtividadesSubprocesso) => registrarContexto(contextoCadastro, contextoCadastroInvalido, c, limparErroIntegracao);

    const {configEdicao, configCadastro} = criarConfigs(
        contextoEdicao, contextoEdicaoInvalido, codigosEdicaoPorProcessoUnidade, registrarContextoEdicao,
        contextoCadastro, contextoCadastroInvalido, codigosCadastroPorProcessoUnidade, registrarContextoCadastro
    );

    return {
        contextoEdicao, contextoCadastro, erroIntegracaoContexto, invalidar, resetar, limparContextoAtual, limparErroIntegracao,
        garantirContextoEdicao: (c: number, l = false) => orquestrador.garantirContextoPorCodigo(c, l, configEdicao),
        garantirContextoEdicaoPorProcessoEUnidade: (p: number, s: string, l = false) => orquestrador.garantirContextoPorProcessoEUnidade(p, s, l, configEdicao),
        garantirContextoCadastroAtividades: (c: number, l = false) => orquestrador.garantirContextoPorCodigo(c, l, configCadastro),
        garantirContextoCadastroAtividadesPorProcessoEUnidade: async (p: number, s: string, l = false) => (await orquestrador.garantirContextoPorProcessoEUnidade(p, s, l, configCadastro))?.contexto ?? null,
        atualizarStatusLocal: (s: AtualizacaoStatusLocal) => {
            atualizarDetalhesContexto(contextoEdicao, contextoEdicaoInvalido, s);
            atualizarDetalhesContexto(contextoCadastro, contextoCadastroInvalido, s);
        },
    };
});
