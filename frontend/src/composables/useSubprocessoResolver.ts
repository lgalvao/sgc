import { computed, type Ref } from 'vue';
import { useProcessosStore } from '@/stores/processos';
import { useUnidadesStore } from '@/stores/unidades';
import type { UnidadeParticipante } from '@/types/tipos';

/**
 * Composable para resolver informações de subprocesso e unidade dentro de uma árvore de processo.
 */
export function useSubprocessoResolver(
    codProcesso: Ref<number>,
    siglaUnidadeRef: Ref<string>
) {
    const processosStore = useProcessosStore();
    const unidadesStore = useUnidadesStore();

    /**
     * Busca recursiva de unidade na árvore.
     */
    function buscarUnidadeNaArvore(unidades: UnidadeParticipante[], sigla: string): UnidadeParticipante | null {
        for (const u of unidades) {
            if (u.sigla === sigla) {
                return u;
            }
            if (u.filhos && u.filhos.length > 0) {
                const encontrada = buscarUnidadeNaArvore(u.filhos, sigla);
                if (encontrada) return encontrada;
            }
        }
        return null;
    }

    const unidade = computed(() => unidadesStore.unidade);

    const nomeUnidade = computed(() =>
        unidade.value?.nome ? `${unidade.value.nome}` : "",
    );

    const unidadeEncontrada = computed(() => {
        if (!processosStore.processoDetalhe?.unidades) return null;
        return buscarUnidadeNaArvore(
            processosStore.processoDetalhe.unidades,
            siglaUnidadeRef.value
        );
    });

    const codSubprocesso = computed(() => unidadeEncontrada.value?.codSubprocesso);
    const codMapa = computed(() => unidadeEncontrada.value?.mapaCodigo);
    const subprocesso = computed(() => unidadeEncontrada.value);

    return {
        unidade,
        nomeUnidade,
        codSubprocesso,
        codMapa,
        subprocesso
    };
}
