import {defineStore} from "pinia";
import {ref} from "vue";
import type {Page} from "@/services/painelService";
import type {Alerta} from "@/types/tipos";
import * as alertaService from "../services/alertaService";
import * as painelService from "../services/painelService";
import {usePerfilStore} from "./perfil";
import {type NormalizedError, normalizeError} from "@/utils/apiError";

export const useAlertasStore = defineStore("alertas", () => {
    const alertas = ref<Alerta[]>([]);
    const alertasPage = ref<Page<Alerta>>({} as Page<Alerta>);
    const lastError = ref<NormalizedError | null>(null);

    function clearError() {
        lastError.value = null;
    }

    async function buscarAlertas(
        usuarioCodigo: number,
        unidade: number,
        page: number,
        size: number,
        sort?: "data" | "processo",
        order?: "asc" | "desc",
    ) {
        lastError.value = null;
        try {
            const response = await painelService.listarAlertas(
                usuarioCodigo,
                unidade,
                page,
                size,
                sort,
                order,
            );
            alertas.value = response.content;
            alertasPage.value = response;
        } catch (error) {
            lastError.value = normalizeError(error);
            throw error;
        }
    }

    async function marcarAlertaComoLido(idAlerta: number): Promise<boolean> {
        lastError.value = null;
        try {
            await alertaService.marcarComoLido(idAlerta);
            const perfilStore = usePerfilStore();
            if (perfilStore.usuarioCodigo && perfilStore.unidadeSelecionada) {
                await buscarAlertas(
                    Number(perfilStore.usuarioCodigo),
                    Number(perfilStore.unidadeSelecionada),
                    0,
                    20,
                    undefined,
                    undefined,
                );
            }
            return true;
        } catch (error) {
            lastError.value = normalizeError(error);
            return false;
        }
    }

    return {
        alertas,
        alertasPage,
        lastError,
        buscarAlertas,
        marcarAlertaComoLido,
        clearError
    };
});
