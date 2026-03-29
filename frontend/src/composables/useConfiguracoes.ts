import {computed, ref} from "vue";
import {useAsyncAction} from "@/composables/useAsyncAction";
import {
    buscarConfiguracoes as serviceBuscarConfiguracoes,
    salvarConfiguracoes as serviceSalvarConfiguracoes,
} from "@/services/configuracaoService";
import type {Parametro} from "@/services/configuracaoService";

export type {Parametro};

export function useConfiguracoes() {
    const configuracoes = ref<Parametro[]>([]);
    const {carregando, erro, executarSilencioso} = useAsyncAction();

    const configuracoesMap = computed(() =>
        new Map(configuracoes.value.map(parametro => [parametro.chave, parametro]))
    );

    async function carregarConfiguracoes() {
        await executarSilencioso(async () => {
            configuracoes.value = await serviceBuscarConfiguracoes();
        }, "Não foi possível carregar as configurações.");
    }

    async function salvarConfiguracoes(novosParametros: Parametro[]) {
        const result = await executarSilencioso(async () => {
            configuracoes.value = await serviceSalvarConfiguracoes(novosParametros);
            return true;
        }, "Não foi possível salvar as configurações.");
        return result ?? false;
    }

    function getValor(chave: string, valorPadrao = ""): string {
        const parametro = configuracoesMap.value.get(chave);
        return parametro ? parametro.valor : valorPadrao;
    }

    function getDiasInativacaoProcesso(): number {
        const valor = getValor("DIAS_INATIVACAO_PROCESSO", "30");
        return parseInt(valor, 10) || 30;
    }

    function getDiasAlertaNovo(): number {
        const valor = getValor("DIAS_ALERTA_NOVO", "3");
        return parseInt(valor, 10) || 3;
    }

    return {
        configuracoes,
        loading: carregando,
        error: erro,
        carregarConfiguracoes,
        salvarConfiguracoes,
        getValor,
        getDiasInativacaoProcesso,
        getDiasAlertaNovo,
    };
}
