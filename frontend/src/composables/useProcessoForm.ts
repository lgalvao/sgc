import {computed, ref, watch} from 'vue';
import {useFormErrors} from '@/composables/useFormErrors';
import {type AtualizarProcessoRequest, type CriarProcessoRequest, type Processo, TipoProcesso} from '@/types/tipos';

export function useProcessoForm(initialData?: Processo) {
    const descricao = ref(initialData?.descricao ?? '');
    const tipo = ref<TipoProcesso | null>(initialData?.tipo ?? null);

    const initialDate = initialData?.dataLimite ? initialData.dataLimite.split('T')[0] : '';
    const dataLimite = ref(initialDate);

    const unidadesSelecionadas = ref<number[]>(
        initialData?.unidades.map(u => u.codUnidade) ?? []
    );

    const {errors: fieldErrors, setFromNormalizedError, clearErrors, hasErrors} = useFormErrors([
        'descricao',
        'tipo',
        'dataLimite',
        'unidades',
        'dataLimiteEtapa1'
    ]);

    watch(descricao, () => {
        fieldErrors.value.descricao = '';
    });
    watch(tipo, () => {
        fieldErrors.value.tipo = '';
    });
    watch(dataLimite, () => {
        fieldErrors.value.dataLimite = '';
    });
    watch(unidadesSelecionadas, () => {
        fieldErrors.value.unidades = '';
    });

    const isFormInvalid = computed(() => {
        return !descricao.value.trim() ||
            !tipo.value ||
            !dataLimite.value ||
            unidadesSelecionadas.value.length === 0;
    });

    function construirCriarRequest(): CriarProcessoRequest {
        return {
            descricao: descricao.value,
            tipo: tipo.value as TipoProcesso,
            dataLimiteEtapa1: dataLimite.value ? `${dataLimite.value}T00:00:00` : null,
            unidades: unidadesSelecionadas.value,
        };
    }

    function construirAtualizarRequest(codigo: number): AtualizarProcessoRequest {
        return {
            codigo,
            descricao: descricao.value,
            tipo: tipo.value as TipoProcesso,
            dataLimiteEtapa1: dataLimite.value ? `${dataLimite.value}T00:00:00` : null,
            unidades: unidadesSelecionadas.value,
        };
    }

    function limpar() {
        descricao.value = '';
        tipo.value = null;
        dataLimite.value = '';
        unidadesSelecionadas.value = [];
        clearErrors();
    }

    return {
        descricao,
        tipo,
        dataLimite,
        unidadesSelecionadas,
        fieldErrors,

        isFormInvalid,

        setFromNormalizedError,
        clearErrors,
        hasErrors,
        construirCriarRequest,
        construirAtualizarRequest,
        limpar,
    };
}
