import { defineStore } from "pinia";
import { computed, ref } from "vue";
import type { PerfilUnidade } from "@/mappers/sgrh";
import type { Perfil } from "@/types/tipos";
import * as usuarioService from "../services/usuarioService";

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

    const isAdmin = computed(() => perfis.value.includes("ADMIN" as Perfil));

    function setServidorId(novoId: string | number) {
        servidorId.value = Number(novoId);
        localStorage.setItem("idServidor", String(novoId));
    }

    function setPerfilUnidade(perfil: Perfil, unidadeCodigo: number) {
        perfilSelecionado.value = perfil;
        unidadeSelecionada.value = unidadeCodigo;
        localStorage.setItem("perfilSelecionado", perfil);
        localStorage.setItem("unidadeSelecionada", unidadeCodigo.toString());
    }

    function setToken(token: string) {
        localStorage.setItem("jwtToken", token);
    }

    function setPerfis(novosPerfis: Perfil[]) {
        perfis.value = novosPerfis;
        localStorage.setItem("perfis", JSON.stringify(novosPerfis));
    }

    async function loginCompleto(tituloEleitoral: string, senha: string) {
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
            setPerfis(listaPerfis);

            // Se houver apenas uma opção, seleciona automaticamente
            if (responsePerfisUnidades.length === 1) {
                const perfilUnidadeSelecionado = responsePerfisUnidades[0];
                const loginResponse = await usuarioService.entrar({
                    tituloEleitoral: tituloEleitoralNum,
                    perfil: perfilUnidadeSelecionado.perfil,
                    unidadeCodigo: perfilUnidadeSelecionado.unidade.codigo,
                });
                setPerfilUnidade(
                    loginResponse.perfil as unknown as Perfil,
                    loginResponse.unidadeCodigo,
                ); // Usar loginResponse.codUnidade
                setServidorId(loginResponse.tituloEleitoral); // Usar loginResponse.tituloEleitoral
                setToken(loginResponse.token); // Adicionar esta linha
            }
            return true;
        }
        return false;
    }

    async function selecionarPerfilUnidade(
        tituloEleitoral: number,
        perfilUnidade: PerfilUnidade,
    ) {
        const loginResponse = await usuarioService.entrar({
            tituloEleitoral: tituloEleitoral,
            perfil: perfilUnidade.perfil,
            unidadeCodigo: perfilUnidade.unidade.codigo,
        });
        setPerfilUnidade(
            loginResponse.perfil as unknown as Perfil,
            loginResponse.unidadeCodigo,
        );
        setServidorId(loginResponse.tituloEleitoral);
        setToken(loginResponse.token);
    }

    return {
        servidorId,
        perfilSelecionado,
        unidadeSelecionada,
        unidadeSelecionadaSigla,
        perfisUnidades,
        perfis,
        isAdmin,
        setServidorId,
        setPerfilUnidade,
        setToken,
        setPerfis,
        loginCompleto,
        selecionarPerfilUnidade,
    };
});
