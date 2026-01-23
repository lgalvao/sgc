import {defineStore} from "pinia";
import {ref} from "vue";
import type {Page} from "@/services/painelService";
import type {Alerta} from "@/types/tipos";
import * as alertaService from "../services/alertaService";
import * as painelService from "../services/painelService";
import {type NormalizedError, normalizeError} from "@/utils/apiError";

export const useAlertasStore = defineStore("alertas", () => {
    const alertas = ref<Alerta[]>([]);
    const alertasPage = ref<Page<Alerta>>({} as Page<Alerta>);
    const lastError = ref<NormalizedError | null>(null);

    function clearError() {
        lastError.value = null;
    }

    async function buscarAlertas(
        usuarioCodigo: string,
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

        // 1. Snapshot state for revert
        const originalAlertas = JSON.parse(JSON.stringify(alertas.value));

        // 2. Optimistic Update (UI Otimista)
        const index = alertas.value.findIndex(a => a.codigo === idAlerta);
        if (index !== -1) {
            // Update local state immediately
            alertas.value[index].dataHoraLeitura = new Date().toISOString();
        }

        try {
            // 3. Call API
            await alertaService.marcarComoLido(idAlerta);

            // 4. Success: We do NOT re-fetch to preserve current pagination and provide instant feedback.
            // If strict consistency is needed, we could fetch in background, but it might disrupt UX if list shifts.
            return true;
        } catch (error) {
            // 5. Error: Revert state
            alertas.value = originalAlertas;
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
