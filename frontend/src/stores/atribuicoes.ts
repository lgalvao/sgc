import {defineStore} from "pinia";
import {computed, ref} from "vue";
import {buscarTodasAtribuicoes} from "@/services/atribuicaoTemporariaService";
import type {AtribuicaoTemporaria} from "@/types/tipos";

export const useAtribuicaoTemporariaStore = defineStore(
    "atribuicaoTemporaria",
    () => {
        const atribuicoes = ref<AtribuicaoTemporaria[]>([]);
        const isLoading = ref(false);
        const error = ref<string | null>(null);

        const obterAtribuicoesPorServidor = computed(
            () =>
                (servidorId: number): AtribuicaoTemporaria[] => {
                    return atribuicoes.value.filter(
                        (a) => a.servidor.codigo === servidorId,
                    );
                },
        );

        const obterAtribuicoesPorUnidade = computed(
            () =>
                (unidadeSigla: string): AtribuicaoTemporaria[] => {
                    return atribuicoes.value.filter(
                        (a) => a.unidade.sigla === unidadeSigla,
                    );
                },
        );

        async function buscarAtribuicoes() {
            isLoading.value = true;
            error.value = null;
            try {
                const response = await buscarTodasAtribuicoes();
                atribuicoes.value = (response as any).data.map((a: any) => ({
                    ...a,
                    dataInicio: new Date(a.dataInicio).toISOString(),
                    dataFim: new Date(a.dataTermino).toISOString(),
                    dataTermino: new Date(a.dataTermino).toISOString(),
                    servidor: a.servidor,
                    unidade: a.unidade,
                })) as AtribuicaoTemporaria[];
            } catch (err: any) {
                error.value = `Falha ao carregar atribuições: ${err.message}`;
            } finally {
                isLoading.value = false;
            }
        }

        function criarAtribuicao(novaAtribuicao: AtribuicaoTemporaria) {
            atribuicoes.value.push(novaAtribuicao);
        }

        return {
            atribuicoes,
            isLoading,
            error,
            obterAtribuicoesPorServidor,
            obterAtribuicoesPorUnidade,
            buscarAtribuicoes,
            criarAtribuicao,
        };
    },
);
