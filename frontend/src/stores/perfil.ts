import {defineStore} from "pinia";
import {computed, ref, type Ref} from "vue";
import type {FluxoLogin, PerfilUnidade, PermissoesSessao} from "@/types/autenticacao";
import type {Perfil, Unidade} from "@/types/tipos";
import * as usuarioService from "../services/usuarioService";
import {useLocalStorage} from "@/composables/useLocalStorage";
import {useSessionStorage} from "@/composables/useSessionStorage";
import {usePainelStore} from "@/stores/painel";
import {useProcessoStore} from "@/stores/processo";
import {useSubprocessoStore} from "@/stores/subprocesso";
import {useUnidadeStore} from "@/stores/unidade";
import {useMapasStore} from "@/stores/mapas";
import {useOrganizacaoStore} from "@/stores/organizacao";
import {cancelarRequisicoesPendentes, finalizarTransicaoSessao, iniciarTransicaoSessao} from "@/axios-setup";
import {logger} from "@/utils";

interface SessaoPerfil {
    tituloEleitoral?: string;
    nome: string;
    perfil: Perfil;
    unidadeCodigo: number;
    unidadeSigla: string;
    permissoes: PermissoesSessao;
}

interface EstadoSessaoRefs {
    usuarioCodigo: Ref<string | null>;
    perfilSelecionado: Ref<Perfil | null>;
    unidadeSelecionada: Ref<number | null>;
    unidadeSelecionadaSigla: Ref<string | null>;
    permissoesSessao: Ref<PermissoesSessao | null>;
    usuarioNome: Ref<string | null>;
    versaoSessao: Ref<number>;
    perfisUnidades: Ref<PerfilUnidade[]>;
    unidadeAtualDetalhes: Ref<Unidade | null>;
}

interface StoresSessao {
    painelStore: ReturnType<typeof usePainelStore>;
    processoStore: ReturnType<typeof useProcessoStore>;
    subprocessoStore: ReturnType<typeof useSubprocessoStore>;
    unidadeStore: ReturnType<typeof useUnidadeStore>;
    mapasStore: ReturnType<typeof useMapasStore>;
    organizacaoStore: ReturnType<typeof useOrganizacaoStore>;
}

function obterUnidadeAtualSelecionada(
    perfilSelecionado: Perfil | null,
    unidadeSelecionada: number | null,
    perfisUnidades: PerfilUnidade[],
): number | null {
    if (!perfilSelecionado) {
        return null;
    }

    if (unidadeSelecionada) {
        return unidadeSelecionada;
    }

    return perfisUnidades.find((perfilUnidade) => perfilUnidade.perfil === perfilSelecionado)?.unidade.codigo ?? null;
}

function invalidarDadosDaSessao(stores: StoresSessao) {
    stores.painelStore.resetar();
    stores.processoStore.resetar();
    stores.subprocessoStore.resetar();
    stores.unidadeStore.resetar();
    stores.mapasStore.resetar();
    stores.organizacaoStore.resetar();
}

function avancarVersaoSessao(estado: EstadoSessaoRefs) {
    estado.versaoSessao.value += 1;
}

function atualizarIdentificacaoSessao(estado: EstadoSessaoRefs, dados: SessaoPerfil) {
    estado.usuarioNome.value = dados.nome;
    if (dados.tituloEleitoral) {
        estado.usuarioCodigo.value = dados.tituloEleitoral;
    }
}

function atualizarAutorizacaoSessao(estado: EstadoSessaoRefs, dados: SessaoPerfil) {
    estado.perfilSelecionado.value = dados.perfil;
    estado.unidadeSelecionada.value = dados.unidadeCodigo;
    estado.unidadeSelecionadaSigla.value = dados.unidadeSigla;
    estado.permissoesSessao.value = dados.permissoes;
}

function aplicarSessaoPerfil(estado: EstadoSessaoRefs, stores: StoresSessao, dados: SessaoPerfil) {
    invalidarDadosDaSessao(stores);
    avancarVersaoSessao(estado);
    atualizarAutorizacaoSessao(estado, dados);
    atualizarIdentificacaoSessao(estado, dados);
}

function limparSessaoLocal(estado: EstadoSessaoRefs) {
    avancarVersaoSessao(estado);
    estado.usuarioCodigo.value = null;
    estado.perfilSelecionado.value = null;
    estado.unidadeSelecionada.value = null;
    estado.unidadeSelecionadaSigla.value = null;
    estado.permissoesSessao.value = null;
    estado.usuarioNome.value = null;
    estado.perfisUnidades.value = [];
    estado.unidadeAtualDetalhes.value = null;
}

function finalizarLoginComSessao(estado: EstadoSessaoRefs, stores: StoresSessao, dados: SessaoPerfil) {
    aplicarSessaoPerfil(estado, stores, dados);
    finalizarTransicaoSessao();
}

function montarSessaoPerfilUnico(fluxoLogin: FluxoLogin): SessaoPerfil | null {
    if (!fluxoLogin.sessao || fluxoLogin.perfisUnidades.length !== 1) {
        return null;
    }

    return {
        tituloEleitoral: fluxoLogin.sessao.tituloEleitoral,
        nome: fluxoLogin.sessao.nome,
        perfil: fluxoLogin.sessao.perfil,
        unidadeCodigo: fluxoLogin.sessao.unidadeCodigo,
        unidadeSigla: fluxoLogin.perfisUnidades[0].unidade.sigla,
        permissoes: fluxoLogin.sessao.permissoes,
    };
}

function montarSessaoPerfilSelecionado(
    perfilUnidade: PerfilUnidade,
    respostaLogin: Awaited<ReturnType<typeof usuarioService.entrar>>,
): SessaoPerfil {
    return {
        tituloEleitoral: respostaLogin.tituloEleitoral,
        nome: respostaLogin.nome,
        perfil: respostaLogin.perfil,
        unidadeCodigo: respostaLogin.unidadeCodigo,
        unidadeSigla: perfilUnidade.unidade.sigla,
        permissoes: respostaLogin.permissoes,
    };
}

export const usePerfilStore = defineStore("perfil", () => {
    // Estados sincronizados com localStorage/sessionStorage usando composable
    const usuarioCodigo = useSessionStorage<string | null>("usuarioCodigo", null);
    const perfilSelecionado = useLocalStorage<Perfil | null>("perfilSelecionado", null);
    const unidadeSelecionada = useLocalStorage<number | null>("unidadeSelecionada", null);
    const unidadeSelecionadaSigla = useLocalStorage<string | null>("unidadeSelecionadaSigla", null);
    const permissoesSessao = useLocalStorage<PermissoesSessao | null>("permissoesSessao", null);
    const usuarioNome = useSessionStorage<string | null>("usuarioNome", null);
    const versaoSessao = ref(0);
    const perfisUnidades = ref<PerfilUnidade[]>([]);
    const unidadeAtualDetalhes = ref<Unidade | null>(null);
    const painelStore = usePainelStore();
    const processoStore = useProcessoStore();
    const unidadeStore = useUnidadeStore();
    const subprocessoStore = useSubprocessoStore();
    const mapasStore = useMapasStore();
    const organizacaoStore = useOrganizacaoStore();

    const unidadeAtual = computed(() =>
        obterUnidadeAtualSelecionada(perfilSelecionado.value, unidadeSelecionada.value, perfisUnidades.value)
    );
    const estadoSessao: EstadoSessaoRefs = {
        usuarioCodigo,
        perfilSelecionado,
        unidadeSelecionada,
        unidadeSelecionadaSigla,
        permissoesSessao,
        usuarioNome,
        versaoSessao,
        perfisUnidades,
        unidadeAtualDetalhes,
    };
    const storesSessao: StoresSessao = {
        painelStore,
        processoStore,
        subprocessoStore,
        unidadeStore,
        mapasStore,
        organizacaoStore,
    };

    async function iniciarLogin(tituloEleitoral: string, senha: string): Promise<FluxoLogin> {
        const fluxoLogin = await usuarioService.login({
            tituloEleitoral,
            senha,
        });
        perfisUnidades.value = fluxoLogin.perfisUnidades;

        const sessaoPerfilUnico = montarSessaoPerfilUnico(fluxoLogin);
        if (sessaoPerfilUnico) {
            finalizarLoginComSessao(estadoSessao, storesSessao, sessaoPerfilUnico);
        }

        return fluxoLogin;
    }

    async function concluirLoginComPerfil(perfilUnidade: PerfilUnidade) {
        const respostaLogin = await usuarioService.entrar({
            perfil: perfilUnidade.perfil,
            unidadeCodigo: perfilUnidade.unidade.codigo,
        });

        finalizarLoginComSessao(
            estadoSessao,
            storesSessao,
            montarSessaoPerfilSelecionado(perfilUnidade, respostaLogin),
        );
    }

    function cancelarFluxoLogin() {
        perfisUnidades.value = [];
    }

    async function logout() {
        iniciarTransicaoSessao();
        cancelarRequisicoesPendentes();

        try {
            await usuarioService.logout();
        } catch (erro) {
            logger.warn("Falha ao encerrar sessao remota; limpando sessao local.", erro);
        }

        limparSessaoLocal(estadoSessao);
        invalidarDadosDaSessao(storesSessao);
    }

    return {
        usuarioCodigo,
        versaoSessao,
        perfilSelecionado,
        unidadeSelecionada,
        unidadeSelecionadaSigla,
        permissoesSessao,
        usuarioNome,
        perfisUnidades,
        unidadeAtual,
        unidadeAtualDetalhes,
        iniciarLogin,
        concluirLoginComPerfil,
        cancelarFluxoLogin,
        logout,
    };
});
