import {defineStore} from "pinia";
import {computed, ref} from "vue";
import {logger} from "@/utils";
import {useSingleLoading} from "@/composables/useLoadingManager";
import {
    buscarConfiguracoes as serviceBuscarConfiguracoes,
    type Parametro,
    salvarConfiguracoes as serviceSalvarConfiguracoes,
} from "@/services/configuracaoService";

export type { Parametro };

export const useConfiguracoesStore = defineStore("configuracoes", () => {
    const parametros = ref<Parametro[]>([]);
    const loading = useSingleLoading();
    const error = ref<string | null>(null);

    // Map para lookup O(1) de chave -> parametro
    const parametrosMap = computed(() => 
        new Map(parametros.value.map(p => [p.chave, p]))
    );

    async function carregarConfiguracoes() {
        error.value = null;
        await loading.withLoading(async () => {
            try {
                parametros.value = await serviceBuscarConfiguracoes();
            } catch (e: any) {
                logger.error("Erro ao carregar configurações:", e);
                error.value = "Não foi possível carregar as configurações.";
            }
        });
    }

    async function salvarConfiguracoes(novosParametros: Parametro[]) {
        error.value = null;
        return loading.withLoading(async () => {
            try {
                parametros.value = await serviceSalvarConfiguracoes(novosParametros);
                return true;
            } catch (e: any) {
                logger.error("Erro ao salvar configurações:", e);
                error.value = "Não foi possível salvar as configurações.";
                return false;
            }
        });
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
        loading: loading.isLoading,
        error,
        carregarConfiguracoes,
        salvarConfiguracoes,
        getValor,
        getDiasInativacaoProcesso,
        getDiasAlertaNovo
    };
});
