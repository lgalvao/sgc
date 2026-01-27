import {defineStore} from "pinia";
import {ref} from "vue";
import type {Page} from "@/services/painelService";
import type {Alerta} from "@/types/tipos";
import * as alertaService from "../services/alertaService";
import * as painelService from "../services/painelService";
import {useErrorHandler} from "@/composables/useErrorHandler";

export const useAlertasStore = defineStore("alertas", () => {
    const alertas = ref<Alerta[]>([]);
    const alertasPage = ref<Page<Alerta>>({} as Page<Alerta>);
    const { lastError, clearError, withErrorHandling } = useErrorHandler();

    async function buscarAlertas(
        usuarioCodigo: string,
        unidade: number,
        page: number,
        size: number,
        sort?: "data" | "processo",
        order?: "asc" | "desc",
    ) {
        return withErrorHandling(async () => {
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
        });
    }

    async function marcarAlertaComoLido(idAlerta: number): Promise<boolean> {
        // 1. Snapshot state for revert
        const originalAlertas = JSON.parse(JSON.stringify(alertas.value));

        // 2. Optimistic Update (UI Otimista)
        const index = alertas.value.findIndex(a => a.codigo === idAlerta);
        if (index !== -1) {
            // Update local state immediately
            alertas.value[index].dataHoraLeitura = new Date().toISOString();
        }

        return withErrorHandling(async () => {
            // 3. Call API
            await alertaService.marcarComoLido(idAlerta);

            // 4. Success: We do NOT re-fetch to preserve current pagination and provide instant feedback.
            // If strict consistency is needed, we could fetch in background, but it might disrupt UX if list shifts.
            return true;
        }).catch(() => {
            // 5. Error: Revert state
            alertas.value = originalAlertas;
            return false;
        });
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
