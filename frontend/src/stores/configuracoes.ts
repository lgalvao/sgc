import { defineStore } from "pinia";
import { ref } from "vue";
import { apiClient } from "@/axios-setup";

export interface Parametro {
    id?: number;
    chave: string;
    descricao: string;
    valor: string;
}

export const useConfiguracoesStore = defineStore("configuracoes", () => {
    const parametros = ref<Parametro[]>([]);
    const loading = ref(false);
    const error = ref<string | null>(null);

    async function carregarConfiguracoes() {
        loading.value = true;
        error.value = null;
        try {
            const response = await apiClient.get<Parametro[]>("/configuracoes");
            parametros.value = response.data;
        } catch (e: any) {
            console.error("Erro ao carregar configurações:", e);
            error.value = "Não foi possível carregar as configurações.";
        } finally {
            loading.value = false;
        }
    }

    async function salvarConfiguracoes(novosParametros: Parametro[]) {
        loading.value = true;
        error.value = null;
        try {
            const response = await apiClient.post<Parametro[]>("/configuracoes", novosParametros);
            parametros.value = response.data;
            return true;
        } catch (e: any) {
            console.error("Erro ao salvar configurações:", e);
            error.value = "Não foi possível salvar as configurações.";
            return false;
        } finally {
            loading.value = false;
        }
    }

    function getValor(chave: string, valorPadrao: string = ""): string {
        const param = parametros.value.find((p) => p.chave === chave);
        return param ? param.valor : valorPadrao;
    }

    // Helpers para compatibilidade e uso fácil
    function getDiasInativacaoProcesso(): number {
        const val = getValor("DIAS_INATIVACAO_PROCESSO", "30");
        return parseInt(val, 10) || 30;
    }

    function getDiasAlertaNovo(): number {
        const val = getValor("DIAS_ALERTA_NOVO", "3");
        return parseInt(val, 10) || 3;
    }

    return {
        parametros,
        loading,
        error,
        carregarConfiguracoes,
        salvarConfiguracoes,
        getValor,
        getDiasInativacaoProcesso,
        getDiasAlertaNovo
    };
});
