import {defineStore} from "pinia";
import {computed, ref} from "vue";
import type {PerfilUnidade} from "@/mappers/sgrh";
import type {Perfil} from "@/types/tipos";
import * as usuarioService from "../services/usuarioService";
import {useErrorHandler} from "@/composables/useErrorHandler";
import {useLocalStorage} from "@/composables/useLocalStorage";

export const usePerfilStore = defineStore("perfil", () => {
    // Estados sincronizados com localStorage usando composable
    const usuarioCodigo = useLocalStorage<string | null>("usuarioCodigo", null);
    const perfilSelecionado = useLocalStorage<Perfil | null>("perfilSelecionado", null);
    const unidadeSelecionada = useLocalStorage<number | null>("unidadeSelecionada", null);
    const unidadeSelecionadaSigla = useLocalStorage<string | null>("unidadeSelecionadaSigla", null);
    const usuarioNome = useLocalStorage<string | null>("usuarioNome", null);
    const perfis = useLocalStorage<Perfil[]>("perfis", []);
    
    // Estados não persistidos
    const perfisUnidades = ref<PerfilUnidade[]>([]);
    const { lastError, clearError, withErrorHandling } = useErrorHandler();

    const isAdmin = computed(() => perfis.value.includes("ADMIN" as Perfil));
    const isGestor = computed(() => perfis.value.includes("GESTOR" as Perfil));

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
    }

    function definirPerfilUnidade(perfil: Perfil, unidadeCodigo: number, unidadeSigla: string, nome?: string) {
        perfilSelecionado.value = perfil;
        unidadeSelecionada.value = unidadeCodigo;
        unidadeSelecionadaSigla.value = unidadeSigla;
        if (nome) {
            usuarioNome.value = nome;
        }
    }

    function definirToken(token: string) {
        localStorage.setItem("jwtToken", token);
    }

    function definirPerfis(novosPerfis: Perfil[]) {
        perfis.value = novosPerfis;
    }

    async function loginCompleto(tituloEleitoral: string, senha: string) {
        return withErrorHandling(async () => {
            const autenticado = await usuarioService.autenticar({
                tituloEleitoral,
                senha,
            });
            if (autenticado) {
                const responsePerfisUnidades =
                    await usuarioService.autorizar(tituloEleitoral);
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
                        tituloEleitoral,
                        perfil: perfilUnidadeSelecionado.perfil,
                        unidadeCodigo: perfilUnidadeSelecionado.unidade.codigo,
                    });
                    definirPerfilUnidade(
                        loginResponse.perfil as unknown as Perfil,
                        loginResponse.unidadeCodigo,
                        perfilUnidadeSelecionado.unidade.sigla,
                        loginResponse.nome,
                    );
                    definirUsuarioCodigo(loginResponse.tituloEleitoral);
                    definirToken(loginResponse.token); 
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
                tituloEleitoral,
                perfil: perfilUnidade.perfil,
                unidadeCodigo: perfilUnidade.unidade.codigo,
            });
            definirPerfilUnidade(
                loginResponse.perfil as unknown as Perfil,
                loginResponse.unidadeCodigo,
                perfilUnidade.unidade.sigla,
                loginResponse.nome,
            );
            definirUsuarioCodigo(loginResponse.tituloEleitoral);
            definirToken(loginResponse.token);
        });
    }

    function logout() {
        usuarioCodigo.value = null;
        perfilSelecionado.value = null;
        unidadeSelecionada.value = null;
        unidadeSelecionadaSigla.value = null;
        usuarioNome.value = null;
        perfisUnidades.value = [];
        perfis.value = [];

        localStorage.removeItem("jwtToken");
    }

    return {
        usuarioCodigo,
        perfilSelecionado,
        unidadeSelecionada,
        unidadeSelecionadaSigla,
        usuarioNome,
        perfisUnidades,
        perfis,
        isAdmin,
        isGestor,
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
