import {defineStore} from "pinia";
import {ref} from "vue";
import type {Page} from "@/services/painelService";
import type {Alerta} from "@/types/tipos";
import * as alertaService from "../services/alertaService";
import * as painelService from "../services/painelService";
import {usePerfilStore} from "./perfil";

export const useAlertasStore = defineStore("alertas", () => {
    const alertas = ref<Alerta[]>([]);
    const alertasPage = ref<Page<Alerta>>({} as Page<Alerta>);

    async function fetchAlertas(
        usuarioTitulo: number,
        unidade: number,
        page: number,
        size: number,
        sort?: "data" | "processo",
        order?: "asc" | "desc",
    ) {
        const response = await painelService.listarAlertas(
            usuarioTitulo,
            unidade,
            page,
            size,
            sort,
            order,
        );
        alertas.value = response.content;
        alertasPage.value = response;
    }

    async function marcarAlertaComoLido(idAlerta: number): Promise<boolean> {
        try {
            await alertaService.marcarComoLido(idAlerta);
            const perfilStore = usePerfilStore();
            if (perfilStore.servidorId && perfilStore.unidadeSelecionada) {
                await fetchAlertas(
                    Number(perfilStore.servidorId),
                    Number(perfilStore.unidadeSelecionada),
                    0,
                    20,
                    undefined,
                    undefined,
                );
            }
            return true;
        } catch {
            return false;
        }
    }

    return {
        alertas,
        alertasPage,
        fetchAlertas,
        marcarAlertaComoLido,
    };
});
