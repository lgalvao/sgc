import {computed, ref} from "vue";
import {useAsyncAction} from "@/composables/useAsyncAction";
import type {Parametro} from "@/services/configuracaoService";
import {
    buscarConfiguracoes as serviceBuscarConfiguracoes,
    salvarConfiguracoes as serviceSalvarConfiguracoes,
} from "@/services/configuracaoService";

export type {Parametro};

const configuracoes = ref<Parametro[]>([]);
const CHAVE_TEMA_ESCURO = "temaEscuro";
const temaEscuro = ref(false);
const codigoUsuarioTema = ref<string | null>(null);

function montarChaveTemaEscuro(codigoUsuario: string): string {
    return `${CHAVE_TEMA_ESCURO}:${codigoUsuario}`;
}

function lerTemaEscuroPorUsuario(codigoUsuario: string): boolean {
    const valor = localStorage.getItem(montarChaveTemaEscuro(codigoUsuario));
    return valor === "true";
}

function salvarTemaEscuroPorUsuario(codigoUsuario: string, novoValor: boolean) {
    localStorage.setItem(montarChaveTemaEscuro(codigoUsuario), String(novoValor));
}

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

    function setContextoUsuarioTemaEscuro(codigoUsuario: string | null | undefined) {
        codigoUsuarioTema.value = codigoUsuario ? String(codigoUsuario) : null;
        if (!codigoUsuarioTema.value) {
            temaEscuro.value = false;
            return;
        }

        temaEscuro.value = lerTemaEscuroPorUsuario(codigoUsuarioTema.value);
    }

    function getTemaEscuro(): boolean {
        return temaEscuro.value;
    }

    function setTemaEscuro(novoValor: boolean) {
        temaEscuro.value = novoValor;
        if (codigoUsuarioTema.value) {
            salvarTemaEscuroPorUsuario(codigoUsuarioTema.value, novoValor);
        }
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
        setContextoUsuarioTemaEscuro,
        getTemaEscuro,
        setTemaEscuro,
    };
}
