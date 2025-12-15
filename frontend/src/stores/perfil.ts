import {defineStore} from "pinia";
import {computed, ref} from "vue";
import type {PerfilUnidade} from "@/mappers/sgrh";
import type {Perfil} from "@/types/tipos";
import * as usuarioService from "../services/usuarioService";
import {type NormalizedError, normalizeError} from "@/utils/apiError";

export const usePerfilStore = defineStore("perfil", () => {
    const servidorId = ref<number | null>(
        localStorage.getItem("idServidor")
            ? Number(localStorage.getItem("idServidor"))
            : null,
    );
    const perfilSelecionado = ref<Perfil | null>(
        (localStorage.getItem("perfilSelecionado") || null) as Perfil | null,
    );
    const unidadeSelecionada = ref<number | null>(
        localStorage.getItem("unidadeSelecionada")
            ? Number(localStorage.getItem("unidadeSelecionada"))
            : null,
    );
    const unidadeSelecionadaSigla = ref<string | null>(
        (localStorage.getItem("unidadeSelecionadaSigla") || null) as string | null,
    );
    const perfisUnidades = ref<PerfilUnidade[]>([]);
    const perfis = ref<Perfil[]>(
        (JSON.parse(localStorage.getItem("perfis") || "[]")) as Perfil[],
    );
    const lastError = ref<NormalizedError | null>(null);

    const isAdmin = computed(() => perfis.value.includes("ADMIN" as Perfil));
    const isGestor = computed(() => perfis.value.includes("GESTOR" as Perfil));

    function clearError() {
        lastError.value = null;
    }

    function definirServidorId(novoId: string | number) {
        servidorId.value = Number(novoId);
        localStorage.setItem("idServidor", String(novoId));
    }

    function definirPerfilUnidade(perfil: Perfil, unidadeCodigo: number, unidadeSigla: string) {
        perfilSelecionado.value = perfil;
        unidadeSelecionada.value = unidadeCodigo;
        unidadeSelecionadaSigla.value = unidadeSigla;
        localStorage.setItem("perfilSelecionado", perfil);
        localStorage.setItem("unidadeSelecionada", unidadeCodigo.toString());
        localStorage.setItem("unidadeSelecionadaSigla", unidadeSigla);
    }

    function definirToken(token: string) {
        localStorage.setItem("jwtToken", token);
    }

    function definirPerfis(novosPerfis: Perfil[]) {
        perfis.value = novosPerfis;
        localStorage.setItem("perfis", JSON.stringify(novosPerfis));
    }

    async function loginCompleto(tituloEleitoral: string, senha: string) {
        lastError.value = null;
        try {
            const tituloEleitoralNum = Number(tituloEleitoral);
            const autenticado = await usuarioService.autenticar({
                tituloEleitoral: tituloEleitoralNum,
                senha,
            });
            if (autenticado) {
                const responsePerfisUnidades =
                    await usuarioService.autorizar(tituloEleitoralNum);
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
                        tituloEleitoral: tituloEleitoralNum,
                        perfil: perfilUnidadeSelecionado.perfil,
                        unidadeCodigo: perfilUnidadeSelecionado.unidade.codigo,
                    });
                    definirPerfilUnidade(
                        loginResponse.perfil as unknown as Perfil,
                        loginResponse.unidadeCodigo,
                        perfilUnidadeSelecionado.unidade.sigla,
                    );
                    definirServidorId(loginResponse.tituloEleitoral); // Usar loginResponse.tituloEleitoral
                    definirToken(loginResponse.token); // Adicionar esta linha
                }
                return true;
            }
            return false;
        } catch (error) {
            lastError.value = normalizeError(error);
            // It was implicitly rethrowing before, so we rethrow now to let the component handle it (e.g. show error message on login form)
            throw error;
        }
    }

    async function selecionarPerfilUnidade(
        tituloEleitoral: number,
        perfilUnidade: PerfilUnidade,
    ) {
        lastError.value = null;
        try {
            const loginResponse = await usuarioService.entrar({
                tituloEleitoral: tituloEleitoral,
                perfil: perfilUnidade.perfil,
                unidadeCodigo: perfilUnidade.unidade.codigo,
            });
            definirPerfilUnidade(
                loginResponse.perfil as unknown as Perfil,
                loginResponse.unidadeCodigo,
                perfilUnidade.unidade.sigla,
            );
            definirServidorId(loginResponse.tituloEleitoral);
            definirToken(loginResponse.token);
        } catch (error) {
            lastError.value = normalizeError(error);
            throw error;
        }
    }

    function logout() {
        servidorId.value = null;
        perfilSelecionado.value = null;
        unidadeSelecionada.value = null;
        unidadeSelecionadaSigla.value = null;
        perfisUnidades.value = [];
        perfis.value = [];
        localStorage.removeItem("idServidor");
        localStorage.removeItem("perfilSelecionado");
        localStorage.removeItem("unidadeSelecionada");
        localStorage.removeItem("unidadeSelecionadaSigla");
        localStorage.removeItem("jwtToken");
        localStorage.removeItem("perfis");
    }

    return {
        servidorId,
        perfilSelecionado,
        unidadeSelecionada,
        unidadeSelecionadaSigla,
        perfisUnidades,
        perfis,
        isAdmin,
        isGestor,
        lastError,
        clearError,
        definirServidorId,
        definirPerfilUnidade,
        loginCompleto,
        selecionarPerfilUnidade,
        logout,
    };
});
