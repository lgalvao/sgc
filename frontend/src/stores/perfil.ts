import {defineStore} from "pinia";
import {computed, ref} from "vue";
import type {FluxoLogin, PerfilUnidade, PermissoesSessao} from "@/types/autenticacao";
import type {Perfil, Unidade} from "@/types/tipos";
import {useLocalStorage} from "@/composables/useLocalStorage";
import {useSessionStorage} from "@/composables/useSessionStorage";
import {useInvalidacaoNavegacao} from "@/composables/useInvalidacaoNavegacao";
import {concluirLoginPerfil, encerrarLoginPerfil, iniciarLoginPerfil, type DadosSessaoPerfil} from "@/stores/perfilAutenticacao";

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
    const {resetarEstadoSessao} = useInvalidacaoNavegacao();

    const unidadeAtual = computed(() =>
        obterUnidadeAtualSelecionada(perfilSelecionado.value, unidadeSelecionada.value, perfisUnidades.value)
    );

    function avancarVersaoSessao() {
        versaoSessao.value += 1;
    }

    function atualizarIdentificacaoSessao(dados: DadosSessaoPerfil) {
        usuarioNome.value = dados.nome;
        if (dados.tituloEleitoral) {
            usuarioCodigo.value = dados.tituloEleitoral;
        }
    }

    function atualizarAutorizacaoSessao(dados: DadosSessaoPerfil) {
        perfilSelecionado.value = dados.perfil;
        unidadeSelecionada.value = dados.unidadeCodigo;
        unidadeSelecionadaSigla.value = dados.unidadeSigla;
        permissoesSessao.value = dados.permissoes;
    }

    function aplicarSessaoPerfil(dados: DadosSessaoPerfil) {
        avancarVersaoSessao();
        atualizarAutorizacaoSessao(dados);
        atualizarIdentificacaoSessao(dados);
    }

    function limparSessaoLocal() {
        avancarVersaoSessao();
        usuarioCodigo.value = null;
        perfilSelecionado.value = null;
        unidadeSelecionada.value = null;
        unidadeSelecionadaSigla.value = null;
        permissoesSessao.value = null;
        usuarioNome.value = null;
        perfisUnidades.value = [];
        unidadeAtualDetalhes.value = null;
    }

    const autenticacaoPerfil = {
        aplicarSessao: aplicarSessaoPerfil,
        atualizarPerfisUnidades: (novosPerfisUnidades: PerfilUnidade[]) => {
            perfisUnidades.value = novosPerfisUnidades;
        },
        limparSessao: limparSessaoLocal,
        resetarEstadoAplicacao: resetarEstadoSessao,
    };

    async function iniciarLogin(tituloEleitoral: string, senha: string): Promise<FluxoLogin> {
        return iniciarLoginPerfil(autenticacaoPerfil, tituloEleitoral, senha);
    }

    async function concluirLoginComPerfil(perfilUnidade: PerfilUnidade) {
        await concluirLoginPerfil(autenticacaoPerfil, perfilUnidade);
    }

    function cancelarFluxoLogin() {
        perfisUnidades.value = [];
    }

    async function logout() {
        await encerrarLoginPerfil(autenticacaoPerfil);
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
