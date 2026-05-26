import {useMutation, useQuery, useQueryCache} from "@pinia/colada";
import {computed} from "vue";
import type {Parametro} from "@/services/configuracaoService";
import {
    buscarConfiguracoes as serviceBuscarConfiguracoes,
    salvarConfiguracoes as serviceSalvarConfiguracoes,
} from "@/services/configuracaoService";
import {usePerfilStore} from "@/stores/perfil";

export type {Parametro};

const CHAVE_QUERY_CONFIGURACOES = ["configuracoes"] as const;
const MENSAGEM_ERRO_CARREGAR = "Não foi possível carregar as configurações.";
const MENSAGEM_ERRO_SALVAR = "Não foi possível salvar as configurações.";

function obterMensagemErro(erro: unknown, mensagemPadrao: string): string {
    if (erro instanceof Error && erro.message) {
        return erro.message;
    }

    return mensagemPadrao;
}

function criarChaveConfiguracoes(usuarioCodigo: string | null) {
    return [...CHAVE_QUERY_CONFIGURACOES, usuarioCodigo ?? "anon"] as const;
}

export {criarChaveConfiguracoes};

export function useConfiguracoes() {
    const perfilStore = usePerfilStore();
    const chave = computed(() => criarChaveConfiguracoes(perfilStore.usuarioCodigo));
    const configuracoesQuery = useQuery<Parametro[], Error, Parametro[]>({
        key: () => [...chave.value],
        query: () => serviceBuscarConfiguracoes(),
        enabled: false,
        staleTime: Infinity,
    });
    const salvarConfiguracoesMutation = useMutation<Parametro[], Parametro[], Error>({
        mutation: (novosParametros) => serviceSalvarConfiguracoes(novosParametros),
        onSuccess: (configuracoesAtualizadas) => {
            useQueryCache().setQueryData(chave.value, configuracoesAtualizadas);
        },
    });
    const configuracoes = computed(() => configuracoesQuery.data.value ?? []);
    const carregandoConfiguracoes = computed(() => configuracoesQuery.isPending.value || configuracoesQuery.isLoading.value);
    const salvando = computed(() => salvarConfiguracoesMutation.isLoading.value);
    const erro = computed(() => {
        if (salvarConfiguracoesMutation.error.value) {
            return obterMensagemErro(salvarConfiguracoesMutation.error.value, MENSAGEM_ERRO_SALVAR);
        }
        if (configuracoesQuery.error.value) {
            return obterMensagemErro(configuracoesQuery.error.value, MENSAGEM_ERRO_CARREGAR);
        }
        return null;
    });

    const configuracoesMap = computed(() =>
        new Map(configuracoes.value.map((parametro) => [parametro.chave, parametro]))
    );

    async function carregarConfiguracoes() {
        try {
            if (configuracoesQuery.status.value === "success" || configuracoesQuery.status.value === "error") {
                await configuracoesQuery.refresh(true);
                return;
            }
            await configuracoesQuery.refetch(true);
        } catch {
            // O estado de erro já fica registrado na query.
        }
    }

    async function salvarConfiguracoes(novosParametros: Parametro[]) {
        try {
            await salvarConfiguracoesMutation.mutateAsync(novosParametros);
            return true;
        } catch {
            return false;
        }
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
        salvando,
        erro,
        carregarConfiguracoes,
        salvarConfiguracoes,
        obterValor,
        obterDiasInativacaoProcesso,
        obterDiasAlertaNovo,
    };
}
