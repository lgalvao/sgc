import {computed, ref} from "vue";
import {useAsyncAction} from "@/composables/useAsyncAction";
import {useLocalStorage} from "@/composables/useLocalStorage";
import type {Parametro} from "@/services/configuracaoService";
import {
    buscarConfiguracoes as serviceBuscarConfiguracoes,
    salvarConfiguracoes as serviceSalvarConfiguracoes,
} from "@/services/configuracaoService";

export type {Parametro};

const configuracoes = ref<Parametro[]>([]);
const temaEscuro = useLocalStorage<boolean>("temaEscuro", false);

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

    function getTemaEscuro(): boolean {
        return temaEscuro.value;
    }

    function setTemaEscuro(novoValor: boolean) {
        temaEscuro.value = novoValor;
    }

    return {
        configuracoes,
        loading: carregandoConfiguracoes,
        saving: carregando,
        error: erro,
        carregarConfiguracoes,
        salvarConfiguracoes,
        getValor,
        getDiasInativacaoProcesso,
        getDiasAlertaNovo,
        getTemaEscuro,
        setTemaEscuro,
    };
}
