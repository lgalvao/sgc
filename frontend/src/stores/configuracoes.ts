import {defineStore} from "pinia";
import {ref, computed} from "vue";
import {logger} from "@/utils";
import {
    buscarConfiguracoes as serviceBuscarConfiguracoes,
    salvarConfiguracoes as serviceSalvarConfiguracoes,
    type Parametro,
} from "@/services/configuracaoService";

export type { Parametro };

export const useConfiguracoesStore = defineStore("configuracoes", () => {
    const parametros = ref<Parametro[]>([]);
    const loading = ref(false);
    const error = ref<string | null>(null);

    // Map para lookup O(1) de chave -> parametro
    const parametrosMap = computed(() => 
        new Map(parametros.value.map(p => [p.chave, p]))
    );

    async function carregarConfiguracoes() {
        loading.value = true;
        error.value = null;
        try {
            parametros.value = await serviceBuscarConfiguracoes();
        } catch (e: any) {
            logger.error("Erro ao carregar configurações:", e);
            error.value = "Não foi possível carregar as configurações.";
        } finally {
            loading.value = false;
        }
    }

    async function salvarConfiguracoes(novosParametros: Parametro[]) {
        loading.value = true;
        error.value = null;
        try {
            parametros.value = await serviceSalvarConfiguracoes(novosParametros);
            return true;
        } catch (e: any) {
            logger.error("Erro ao salvar configurações:", e);
            error.value = "Não foi possível salvar as configurações.";
            return false;
        } finally {
            loading.value = false;
        }
    }

    function getValor(chave: string, valorPadrao: string = ""): string {
        const param = parametrosMap.value.get(chave);
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
