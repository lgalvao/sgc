import {defineStore} from "pinia";
import {computed, ref} from "vue";
import {useAsyncAction} from "@/composables/useAsyncAction";
import {
    buscarConfiguracoes as serviceBuscarConfiguracoes,
    type Parametro,
    salvarConfiguracoes as serviceSalvarConfiguracoes,
} from "@/services/configuracaoService";

export type {Parametro};

export const useConfiguracoesStore = defineStore("configuracoes", () => {
    const parametros = ref<Parametro[]>([]);
    const {carregando, erro, executarSilencioso} = useAsyncAction();

    // Map para lookup O(1) de chave -> parametro
    const parametrosMap = computed(() =>
        new Map(parametros.value.map(p => [p.chave, p]))
    );

    async function carregarConfiguracoes() {
        await executarSilencioso(async () => {
            parametros.value = await serviceBuscarConfiguracoes();
        }, "Não foi possível carregar as configurações.");
    }

    async function salvarConfiguracoes(novosParametros: Parametro[]) {
        const result = await executarSilencioso(async () => {
            parametros.value = await serviceSalvarConfiguracoes(novosParametros);
            return true;
        }, "Não foi possível salvar as configurações.");
        return result ?? false;
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
        loading: carregando,
        error: erro,
        carregarConfiguracoes,
        salvarConfiguracoes,
        getValor,
        getDiasInativacaoProcesso,
        getDiasAlertaNovo
    };
});
