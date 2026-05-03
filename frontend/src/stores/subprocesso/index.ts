import {defineStore} from "pinia";
import {ref} from "vue";
import type {ContextoCadastroAtividadesSubprocesso, ContextoEdicaoSubprocesso, TipoProcesso} from "@/types/tipos";
import {
    buscarContextoCadastroAtividades as serviceBuscarContextoCadastroAtividades,
    buscarContextoCadastroAtividadesPorProcessoEUnidade as serviceBuscarContextoCadastroAtividadesPorProcessoEUnidade,
    buscarContextoEdicao as serviceBuscarContextoEdicao,
    buscarContextoEdicaoPorProcessoEUnidade as serviceBuscarContextoEdicaoPorProcessoEUnidade,
} from "@/services/subprocessoService";
import {
    type AtualizacaoStatusLocal,
    atualizarDetalhesContexto,
    registrarContexto
} from "@/stores/subprocessoStoreHelpers";
import type {ErroNormalizado} from "@/utils/apiError";
import type {ConfiguracaoContexto} from "./tipos";
import {usarOrquestradorContexto} from "./orquestrador";

export const useSubprocessoStore = defineStore("subprocesso", () => {
    // Estado
    const contextoEdicao = ref<ContextoEdicaoSubprocesso | null>(null);
    const contextoCadastro = ref<ContextoCadastroAtividadesSubprocesso | null>(null);
    const contextoEdicaoInvalido = ref(false);
    const contextoCadastroInvalido = ref(false);
    const erroIntegracaoContexto = ref<ErroNormalizado | null>(null);
    
    // Cache e Controle
    const carregamentos = new Map<string, Promise<unknown>>();
    const codigosEdicaoPorProcessoUnidade = new Map<string, number>();
    const codigosCadastroPorProcessoUnidade = new Map<string, number>();

    // Ações de Limpeza
    function limparContextoAtual(): void {
        contextoEdicao.value = null;
        contextoCadastro.value = null;
        contextoEdicaoInvalido.value = false;
        contextoCadastroInvalido.value = false;
    }

    function limparErroIntegracao(): void {
        erroIntegracaoContexto.value = null;
    }

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

    // Orquestração
    const orquestrador = usarOrquestradorContexto(carregamentos, erroIntegracaoContexto, limparContextoAtual);

    function registrarContextoEdicao(contexto: ContextoEdicaoSubprocesso): void {
        registrarContexto(contextoEdicao, contextoEdicaoInvalido, contexto, limparErroIntegracao);
    }

    function registrarContextoCadastro(contexto: ContextoCadastroAtividadesSubprocesso): void {
        registrarContexto(contextoCadastro, contextoCadastroInvalido, contexto, limparErroIntegracao);
    }

    // Configurações de domínio
    const configEdicao: ConfiguracaoContexto<ContextoEdicaoSubprocesso> = {
        tipoCodigo: "EDICAO_CODIGO",
        tipoProcessoUnidade: "EDICAO_PROCESSO_UNIDADE",
        contextoRef: contextoEdicao,
        contextoInvalidoRef: contextoEdicaoInvalido,
        codigosPorProcessoUnidade: codigosEdicaoPorProcessoUnidade,
        buscarPorCodigo: serviceBuscarContextoEdicao,
        buscarPorProcessoEUnidade: serviceBuscarContextoEdicaoPorProcessoEUnidade,
        registrar: registrarContextoEdicao,
        mensagemCodigo: (codigo) => `Falha grave ao localizar o subprocesso ${codigo}.`,
        mensagemProcessoUnidade: (codProcesso, sigla) =>
            `Falha grave ao resolver subprocesso do processo ${codProcesso} para a unidade ${sigla}.`,
    };

    const configCadastro: ConfiguracaoContexto<ContextoCadastroAtividadesSubprocesso> = {
        tipoCodigo: "CADASTRO_CODIGO",
        tipoProcessoUnidade: "CADASTRO_PROCESSO_UNIDADE",
        contextoRef: contextoCadastro,
        contextoInvalidoRef: contextoCadastroInvalido,
        codigosPorProcessoUnidade: codigosCadastroPorProcessoUnidade,
        buscarPorCodigo: serviceBuscarContextoCadastroAtividades,
        buscarPorProcessoEUnidade: serviceBuscarContextoCadastroAtividadesPorProcessoEUnidade,
        registrar: registrarContextoCadastro,
        mensagemCodigo: (codigo) => `Falha grave ao carregar o cadastro do subprocesso ${codigo}.`,
        mensagemProcessoUnidade: (codProcesso, sigla) =>
            `Falha grave ao resolver o cadastro do processo ${codProcesso} para a unidade ${sigla}.`,
    };

    // APIs Públicas do Store
    async function garantirContextoEdicao(codigoSubprocesso: number, limparAntes = false) {
        return orquestrador.garantirContextoPorCodigo(codigoSubprocesso, limparAntes, configEdicao);
    }

    async function garantirContextoEdicaoPorProcessoEUnidade(codProcesso: number, siglaUnidade: string, limparAntes = false) {
        return orquestrador.garantirContextoPorProcessoEUnidade(codProcesso, siglaUnidade, limparAntes, configEdicao);
    }

    async function garantirContextoCadastroAtividades(codigoSubprocesso: number, limparAntes = false) {
        return orquestrador.garantirContextoPorCodigo(codigoSubprocesso, limparAntes, configCadastro);
    }

    async function garantirContextoCadastroAtividadesPorProcessoEUnidade(codProcesso: number, siglaUnidade: string, limparAntes = false) {
        const resultado = await orquestrador.garantirContextoPorProcessoEUnidade(codProcesso, siglaUnidade, limparAntes, configCadastro);
        return resultado?.contexto ?? null;
    }

    function atualizarStatusLocal(status: AtualizacaoStatusLocal) {
        atualizarDetalhesContexto(contextoEdicao, contextoEdicaoInvalido, status);
        atualizarDetalhesContexto(contextoCadastro, contextoCadastroInvalido, status);
    }

    return {
        contextoEdicao,
        contextoCadastro,
        erroIntegracaoContexto,
        invalidar,
        resetar,
        limparContextoAtual,
        limparErroIntegracao,
        garantirContextoEdicao,
        garantirContextoEdicaoPorProcessoEUnidade,
        garantirContextoCadastroAtividades,
        garantirContextoCadastroAtividadesPorProcessoEUnidade,
        atualizarStatusLocal,
    };
});
