import {defineStore} from "pinia";
import {computed, ref} from "vue";
import type {PerfilUnidade} from "@/services/usuarioService";
import type {Perfil} from "@/types/tipos";
import * as usuarioService from "../services/usuarioService";
import {useErrorHandler} from "@/composables/useErrorHandler";
import {useLocalStorage} from "@/composables/useLocalStorage";
import {useSessionStorage} from "@/composables/useSessionStorage";

export const usePerfilStore = defineStore("perfil", () => {
    // Estados sincronizados com localStorage/sessionStorage usando composable
    const usuarioCodigo = useSessionStorage<string | null>("usuarioCodigo", null);
    const perfilSelecionado = useLocalStorage<Perfil | null>("perfilSelecionado", null);
    const unidadeSelecionada = useLocalStorage<number | null>("unidadeSelecionada", null);
    const unidadeSelecionadaSigla = useLocalStorage<string | null>("unidadeSelecionadaSigla", null);
    const usuarioNome = useSessionStorage<string | null>("usuarioNome", null);
    const perfis = useLocalStorage<Perfil[]>("perfis", []);

    // Estados não persistidos
    const perfisUnidades = ref<PerfilUnidade[]>([]);
    const {lastError, clearError, withErrorHandling} = useErrorHandler();

    // Map para lookup O(1) de perfil -> unidade
    const perfilUnidadeMap = computed(() =>
        new Map(perfisUnidades.value.map(pu => [pu.perfil, pu]))
    );

    const unidadeAtual = computed(() => {
        if (!perfilSelecionado.value) return null;
        if (unidadeSelecionada.value) return unidadeSelecionada.value;

        const pu = perfilUnidadeMap.value.get(perfilSelecionado.value);
        return pu ? pu.unidade.codigo : null;
    });

    function definirUsuarioCodigo(novoId: string) {
        usuarioCodigo.value = novoId;
        // localStorage.setItem removido - sincronização automática
    }

    interface DadosSelecaoPerfil {
        perfil: Perfil;
        unidadeCodigo: number;
        unidadeSigla: string;
        nome?: string;
    }

    function definirPerfilUnidade(dados: DadosSelecaoPerfil) {
        perfilSelecionado.value = dados.perfil;
        unidadeSelecionada.value = dados.unidadeCodigo;
        unidadeSelecionadaSigla.value = dados.unidadeSigla;
        if (dados.nome) {
            usuarioNome.value = dados.nome;
        }
    }

    function definirPerfis(novosPerfis: Perfil[]) {
        perfis.value = novosPerfis;
        // localStorage.setItem removido - sincronização automática
    }

    async function loginCompleto(tituloEleitoral: string, senha: string) {
        return withErrorHandling(async () => {
            const autenticado = await usuarioService.autenticar({
                tituloEleitoral,
                senha,
            });
            if (autenticado) {
                const responsePerfisUnidades = await usuarioService.autorizar();
                perfisUnidades.value = responsePerfisUnidades;

                const listaPerfis = [
                    ...new Set(
                        responsePerfisUnidades.map((p) => p.perfil as unknown as Perfil),
                    ),
                ];
                definirPerfis(listaPerfis);

                // Se houver apenas uma opção, seleciona automaticamente
                if (responsePerfisUnidades.length === 1) {
                    const perfilUnidadeSelecionado = responsePerfisUnidades[0];
                    const loginResponse = await usuarioService.entrar({
                        perfil: perfilUnidadeSelecionado.perfil,
                        unidadeCodigo: perfilUnidadeSelecionado.unidade.codigo,
                    });
                    definirPerfilUnidade({
                        perfil: loginResponse.perfil as unknown as Perfil,
                        unidadeCodigo: loginResponse.unidadeCodigo,
                        unidadeSigla: perfilUnidadeSelecionado.unidade.sigla,
                        nome: loginResponse.nome,
                    });
                    definirUsuarioCodigo(loginResponse.tituloEleitoral);
                }
                return true;
            }
            return false;
        }).catch((error: any) => {
            if (error?.response?.status === 404 || error?.response?.status === 401) {
                return false;
            }
            throw error;
        });
    }

    async function selecionarPerfilUnidade(
        tituloEleitoral: string,
        perfilUnidade: PerfilUnidade,
    ) {
        return withErrorHandling(async () => {
            const loginResponse = await usuarioService.entrar({
                perfil: perfilUnidade.perfil,
                unidadeCodigo: perfilUnidade.unidade.codigo,
            });
            definirPerfilUnidade({
                perfil: loginResponse.perfil as unknown as Perfil,
                unidadeCodigo: loginResponse.unidadeCodigo,
                unidadeSigla: perfilUnidade.unidade.sigla,
                nome: loginResponse.nome,
            });
            definirUsuarioCodigo(loginResponse.tituloEleitoral);
        });
    }

    function logout() {
        // Limpa estados - remoção do localStorage é automática
        usuarioCodigo.value = null;
        perfilSelecionado.value = null;
        unidadeSelecionada.value = null;
        unidadeSelecionadaSigla.value = null;
        usuarioNome.value = null;
        perfisUnidades.value = [];
        perfis.value = [];
    }

    return {
        usuarioCodigo,
        perfilSelecionado,
        unidadeSelecionada,
        unidadeSelecionadaSigla,
        usuarioNome,
        perfisUnidades,
        perfis,
        unidadeAtual,
        lastError,
        clearError,
        definirUsuarioCodigo,
        definirPerfilUnidade,
        loginCompleto,
        selecionarPerfilUnidade,
        logout,
    };
});
