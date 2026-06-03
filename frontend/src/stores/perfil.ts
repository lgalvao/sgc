import {defineStore} from "pinia";
import {computed, ref} from "vue";
import type {FluxoLogin, PerfilUnidade, PermissoesSessao} from "@/types/autenticacao";
import type {Perfil, Unidade} from "@/types/tipos";
import {useWebStorage} from "@/composables/useWebStorage";
import {useQueryCache} from "@pinia/colada";
import {usePainelStore} from "@/stores/painel";
import {useSubprocessoStore} from "@/stores/subprocesso";
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
    const usuarioCodigo = useWebStorage<string | null>(sessionStorage, "usuarioCodigo", null);
    const perfilSelecionado = useWebStorage<Perfil | null>(localStorage, "perfilSelecionado", null);
    const unidadeSelecionada = useWebStorage<number | null>(localStorage, "unidadeSelecionada", null);
    const unidadeSelecionadaSigla = useWebStorage<string | null>(localStorage, "unidadeSelecionadaSigla", null);
    const permissoesSessao = useWebStorage<PermissoesSessao | null>(localStorage, "permissoesSessao", null);
    const usuarioNome = useWebStorage<string | null>(sessionStorage, "usuarioNome", null);
    const versaoSessao = ref(0);
    const perfisUnidades = ref<PerfilUnidade[]>([]);
    const unidadeAtualDetalhes = ref<Unidade | null>(null);
    const painelStore = usePainelStore();
    const subprocessoStore = useSubprocessoStore();

    function resetarEstadoAplicacao() {
        painelStore.resetar();
        subprocessoStore.resetar();
        useQueryCache().invalidateQueries();
    }

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
        resetarEstadoAplicacao,
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
