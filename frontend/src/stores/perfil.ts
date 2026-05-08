import {defineStore} from "pinia";
import {computed, ref} from "vue";
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

export const usePerfilStore = defineStore("perfil", () => {
    // Estados sincronizados com localStorage/sessionStorage usando composable
    const usuarioCodigo = useSessionStorage<string | null>("usuarioCodigo", null);
    const perfilSelecionado = useLocalStorage<Perfil | null>("perfilSelecionado", null);
    const unidadeSelecionada = useLocalStorage<number | null>("unidadeSelecionada", null);
    const unidadeSelecionadaSigla = useLocalStorage<string | null>("unidadeSelecionadaSigla", null);
    const permissoesSessao = useLocalStorage<PermissoesSessao | null>("permissoesSessao", null);
    const usuarioNome = useSessionStorage<string | null>("usuarioNome", null);
    const perfisUnidades = ref<PerfilUnidade[]>([]);
    const unidadeAtualDetalhes = ref<Unidade | null>(null);
    const painelStore = usePainelStore();
    const processoStore = useProcessoStore();
    const unidadeStore = useUnidadeStore();
    const subprocessoStore = useSubprocessoStore();
    const mapasStore = useMapasStore();
    const organizacaoStore = useOrganizacaoStore();

    const unidadeAtual = computed(() => {
        if (!perfilSelecionado.value) {
            return null;
        }
        if (unidadeSelecionada.value) {
            return unidadeSelecionada.value;
        }

        return perfisUnidades.value.find((perfilUnidade) => perfilUnidade.perfil === perfilSelecionado.value)?.unidade.codigo ?? null;
    });

    interface SessaoPerfil {
        tituloEleitoral?: string;
        nome: string;
        perfil: Perfil;
        unidadeCodigo: number;
        unidadeSigla: string;
        permissoes: PermissoesSessao;
    }

    function invalidarDadosDaSessao() {
        painelStore.resetar();
        processoStore.resetar();
        subprocessoStore.resetar();
        unidadeStore.invalidar();
        mapasStore.resetar();
        organizacaoStore.resetar();
    }

    function aplicarSessaoPerfil(dados: SessaoPerfil) {
        invalidarDadosDaSessao();
        perfilSelecionado.value = dados.perfil;
        unidadeSelecionada.value = dados.unidadeCodigo;
        unidadeSelecionadaSigla.value = dados.unidadeSigla;
        permissoesSessao.value = dados.permissoes;
        usuarioNome.value = dados.nome;
        if (dados.tituloEleitoral) {
            usuarioCodigo.value = dados.tituloEleitoral;
        }
    }

    function limparSessaoLocal() {
        usuarioCodigo.value = null;
        perfilSelecionado.value = null;
        unidadeSelecionada.value = null;
        unidadeSelecionadaSigla.value = null;
        permissoesSessao.value = null;
        usuarioNome.value = null;
        perfisUnidades.value = [];
        unidadeAtualDetalhes.value = null;
    }

    async function iniciarLogin(tituloEleitoral: string, senha: string): Promise<FluxoLogin> {
        const fluxoLogin = await usuarioService.login({
            tituloEleitoral,
            senha,
        });
        perfisUnidades.value = fluxoLogin.perfisUnidades;

        if (fluxoLogin.sessao && fluxoLogin.perfisUnidades.length === 1) {
            aplicarSessaoPerfil({
                tituloEleitoral: fluxoLogin.sessao.tituloEleitoral,
                nome: fluxoLogin.sessao.nome,
                perfil: fluxoLogin.sessao.perfil,
                unidadeCodigo: fluxoLogin.sessao.unidadeCodigo,
                unidadeSigla: fluxoLogin.perfisUnidades[0].unidade.sigla,
                permissoes: fluxoLogin.sessao.permissoes,
            });
            finalizarTransicaoSessao();
        }

        return fluxoLogin;
    }

    async function concluirLoginComPerfil(
        perfilUnidade: PerfilUnidade,
    ) {
        const loginResponse = await usuarioService.entrar({
            perfil: perfilUnidade.perfil,
            unidadeCodigo: perfilUnidade.unidade.codigo,
        });
        aplicarSessaoPerfil({
            tituloEleitoral: loginResponse.tituloEleitoral,
            nome: loginResponse.nome,
            perfil: loginResponse.perfil,
            unidadeCodigo: loginResponse.unidadeCodigo,
            unidadeSigla: perfilUnidade.unidade.sigla,
            permissoes: loginResponse.permissoes,
        });
        finalizarTransicaoSessao();
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

        limparSessaoLocal();
        invalidarDadosDaSessao();
    }

    return {
        usuarioCodigo,
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
