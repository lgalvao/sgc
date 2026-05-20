import {computed, ref} from "vue";
import {useAsyncAction} from "@/composables/useAsyncAction";
import type {Parametro} from "@/services/configuracaoService";
import {
    buscarConfiguracoes as serviceBuscarConfiguracoes,
    salvarConfiguracoes as serviceSalvarConfiguracoes,
} from "@/services/configuracaoService";

export type {Parametro};

const configuracoes = ref<Parametro[]>([]);

export function useConfiguracoes() {
    const {carregando, erro, executar} = useAsyncAction();
    const carregandoConfiguracoes = ref(false);

    const configuracoesMap = computed(() =>
        new Map(configuracoes.value.map(parametro => [parametro.chave, parametro]))
    );

    async function carregarConfiguracoes() {
        configuracoes.value = [];
        carregandoConfiguracoes.value = true;
        try {
            await executar(async () => {
                configuracoes.value = await serviceBuscarConfiguracoes();
            }, "Não foi possível carregar as configurações.", {relancarErro: false});
        } finally {
            carregandoConfiguracoes.value = false;
        }
    }

    async function salvarConfiguracoes(novosParametros: Parametro[]) {
        const resultado = await executar(async () => {
            configuracoes.value = await serviceSalvarConfiguracoes(novosParametros);
            return true;
        }, "Não foi possível salvar as configurações.", {relancarErro: false});
        return resultado === true;
    }

    function obterValor(chave: string, valorPadrao = ""): string {
        const parametro = configuracoesMap.value.get(chave);
        return parametro ? parametro.valor : valorPadrao;
    }

    function obterDiasInativacaoProcesso(): number {
        const valor = obterValor("DIAS_INATIVACAO_PROCESSO", "30");
        return parseInt(valor, 10) || 30;
    }

    function obterDiasAlertaNovo(): number {
        const valor = obterValor("DIAS_ALERTA_NOVO", "3");
        return parseInt(valor, 10) || 3;
    }

    return {
        configuracoes,
        carregandoConfiguracoes,
        salvando: carregando,
        erro,
        carregarConfiguracoes,
        salvarConfiguracoes,
        obterValor,
        obterDiasInativacaoProcesso,
        obterDiasAlertaNovo,
    };
}
