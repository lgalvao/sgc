import {defineStore} from "pinia";
import {computed, ref, type Ref} from "vue";
import type {FluxoLogin, PerfilUnidade, PermissoesSessao} from "@/types/autenticacao";
import type {Perfil, Unidade} from "@/types/tipos";
import {useLocalStorage} from "@/composables/useLocalStorage";
import {useSessionStorage} from "@/composables/useSessionStorage";
import {useInvalidacaoEstadoNavegacao} from "@/composables/useInvalidacaoEstadoNavegacao";
import {concluirLoginPerfil, encerrarLoginPerfil, iniciarLoginPerfil, type DadosSessaoPerfil} from "@/stores/perfilAutenticacao";

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
function avancarVersaoSessao(estado: EstadoSessaoRefs) {
    estado.versaoSessao.value += 1;
}

function atualizarIdentificacaoSessao(estado: EstadoSessaoRefs, dados: DadosSessaoPerfil) {
    estado.usuarioNome.value = dados.nome;
    if (dados.tituloEleitoral) {
        estado.usuarioCodigo.value = dados.tituloEleitoral;
    }
}

function atualizarAutorizacaoSessao(estado: EstadoSessaoRefs, dados: DadosSessaoPerfil) {
    estado.perfilSelecionado.value = dados.perfil;
    estado.unidadeSelecionada.value = dados.unidadeCodigo;
    estado.unidadeSelecionadaSigla.value = dados.unidadeSigla;
    estado.permissoesSessao.value = dados.permissoes;
}

function aplicarSessaoPerfil(estado: EstadoSessaoRefs, dados: DadosSessaoPerfil) {
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
    const {resetarEstadoSessao} = useInvalidacaoEstadoNavegacao();

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
    const autenticacaoPerfil = {
        aplicarSessao: (dados: DadosSessaoPerfil) => aplicarSessaoPerfil(estadoSessao, dados),
        atualizarPerfisUnidades: (novosPerfisUnidades: PerfilUnidade[]) => {
            perfisUnidades.value = novosPerfisUnidades;
        },
        limparSessao: () => limparSessaoLocal(estadoSessao),
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
