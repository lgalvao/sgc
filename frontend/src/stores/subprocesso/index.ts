import {defineStore} from "pinia";
import {ref} from "vue";
import type {ContextoCadastroAtividadesSubprocesso, ContextoEdicaoSubprocesso} from "@/types/tipos";
import {
    type AtualizacaoStatusLocal,
    atualizarDetalhesContexto,
    dadosValidos,
    registrarContexto
} from "@/stores/subprocessoStoreHelpers";
import type {ErroNormalizado} from "@/utils/apiError";
import {usarOrquestradorContexto} from "./orquestrador";
import {criarConfigs} from "./configs";

const REAPROVEITAR_CONTEXTO = false;
const RECARREGAR_CONTEXTO = true;

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

    const limparErroIntegracao = () => {
        erroIntegracaoContexto.value = null;
    };

    function invalidar(): void {
        carregamentos.clear();
        contextoEdicaoInvalido.value = contextoEdicao.value !== null;
        contextoCadastroInvalido.value = contextoCadastro.value !== null;
        limparErroIntegracao();
    }

    function invalidarContextoEdicao(codigoSubprocesso?: number): void {
        carregamentos.clear();
        if (contextoEdicao.value === null) {
            return;
        }
        if (typeof codigoSubprocesso === "number" && contextoEdicao.value.detalhes.codigo !== codigoSubprocesso) {
            return;
        }
        contextoEdicaoInvalido.value = true;
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
    const registrarContextoEdicao = (c: ContextoEdicaoSubprocesso) => registrarContexto({
        contextoRef: contextoEdicao,
        contextoInvalidoRef: contextoEdicaoInvalido,
        contexto: c,
        limparErroIntegracao,
    });
    const registrarContextoCadastro = (c: ContextoCadastroAtividadesSubprocesso) => registrarContexto({
        contextoRef: contextoCadastro,
        contextoInvalidoRef: contextoCadastroInvalido,
        contexto: c,
        limparErroIntegracao,
    });

    const {configEdicao, configCadastro} = criarConfigs({
        contextoEdicao,
        contextoEdicaoInvalido,
        codigosEdicaoPorProcessoUnidade,
        registrarContextoEdicao,
        contextoCadastro,
        contextoCadastroInvalido,
        codigosCadastroPorProcessoUnidade,
        registrarContextoCadastro,
    });

    function obterContextoEdicao(codigoSubprocesso: number) {
        return orquestrador.garantirContextoPorCodigo(codigoSubprocesso, REAPROVEITAR_CONTEXTO, configEdicao);
    }

    function recarregarContextoEdicao(codigoSubprocesso: number) {
        return orquestrador.garantirContextoPorCodigo(codigoSubprocesso, RECARREGAR_CONTEXTO, configEdicao);
    }

    function obterContextoEdicaoPorProcessoEUnidade(codigoProcesso: number, siglaUnidade: string) {
        return orquestrador.garantirContextoPorProcessoEUnidade(codigoProcesso, siglaUnidade, {
            limparAntes: REAPROVEITAR_CONTEXTO,
            config: configEdicao,
        });
    }

    function recarregarContextoEdicaoPorProcessoEUnidade(codigoProcesso: number, siglaUnidade: string) {
        return orquestrador.garantirContextoPorProcessoEUnidade(codigoProcesso, siglaUnidade, {
            limparAntes: RECARREGAR_CONTEXTO,
            config: configEdicao,
        });
    }

    function obterContextoCadastroAtividades(codigoSubprocesso: number) {
        return orquestrador.garantirContextoPorCodigo(codigoSubprocesso, REAPROVEITAR_CONTEXTO, configCadastro);
    }

    function recarregarContextoCadastroAtividades(codigoSubprocesso: number) {
        return orquestrador.garantirContextoPorCodigo(codigoSubprocesso, RECARREGAR_CONTEXTO, configCadastro);
    }

    async function obterContextoCadastroAtividadesPorProcessoEUnidade(codigoProcesso: number, siglaUnidade: string) {
        return (await orquestrador.garantirContextoPorProcessoEUnidade(codigoProcesso, siglaUnidade, {
            limparAntes: REAPROVEITAR_CONTEXTO,
            config: configCadastro,
        }))?.contexto ?? null;
    }

    async function recarregarContextoCadastroAtividadesPorProcessoEUnidade(codigoProcesso: number, siglaUnidade: string) {
        return (await orquestrador.garantirContextoPorProcessoEUnidade(codigoProcesso, siglaUnidade, {
            limparAntes: RECARREGAR_CONTEXTO,
            config: configCadastro,
        }))?.contexto ?? null;
    }

    return {
        contextoEdicao,
        contextoCadastro,
        erroIntegracaoContexto,
        dadosEdicaoValidos: (codigoSubprocesso: number) => dadosValidos(contextoEdicao, contextoEdicaoInvalido, codigoSubprocesso),
        dadosCadastroValidos: (codigoSubprocesso: number) => dadosValidos(contextoCadastro, contextoCadastroInvalido, codigoSubprocesso),
        invalidar,
        resetar,
        limparContextoAtual,
        limparErroIntegracao,
        obterContextoEdicao,
        recarregarContextoEdicao,
        obterContextoEdicaoPorProcessoEUnidade,
        recarregarContextoEdicaoPorProcessoEUnidade,
        obterContextoCadastroAtividades,
        recarregarContextoCadastroAtividades,
        obterContextoCadastroAtividadesPorProcessoEUnidade,
        recarregarContextoCadastroAtividadesPorProcessoEUnidade,
        atualizarStatusLocal: (s: AtualizacaoStatusLocal) => {
            atualizarDetalhesContexto(contextoEdicao, contextoEdicaoInvalido, s);
            atualizarDetalhesContexto(contextoCadastro, contextoCadastroInvalido, s);
        },
    };
});
