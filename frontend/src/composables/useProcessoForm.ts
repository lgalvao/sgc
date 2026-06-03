import {computed, ref, watch} from 'vue';
import {useFormErrors} from '@/composables/useFormErrors';
import {type AtualizarProcessoRequest, type CriarProcessoRequest, type Processo, TipoProcesso} from '@/types/tipos';
import {ehDataEstritamenteFutura} from "@/utils/date";

export function useProcessoForm(initialData?: Processo) {
    const descricao = ref(initialData?.descricao ?? '');
    const tipo = ref<TipoProcesso | null>(initialData?.tipo ?? null);

    const initialDate = initialData?.dataLimite ? initialData.dataLimite.split('T')[0] : '';
    const dataLimite = ref(initialDate);

    const unidadesSelecionadas = ref<number[]>(
        initialData?.unidades.map(u => u.codUnidade) ?? []
    );

    const {
        erros: fieldErrors,
        aplicarErroNormalizado: baseAplicarErroNormalizado,
        limparErros,
        temErros
    } = useFormErrors([
        'descricao',
        'tipo',
        'dataLimite',
        'unidades',
        'dataLimiteEtapa1'
    ]);

    function aplicarErroNormalizado(erroNormalizado: import('@/utils/apiError').ErroNormalizado | null) {
        baseAplicarErroNormalizado(erroNormalizado);
        // Mapeamento de erro legado/backend para o campo da UI
        if (fieldErrors.value.dataLimiteEtapa1 && !fieldErrors.value.dataLimite) {
            fieldErrors.value.dataLimite = fieldErrors.value.dataLimiteEtapa1;
        }
    }

    watch(descricao, () => {
        fieldErrors.value.descricao = '';
    });

    watch(tipo, () => {
        fieldErrors.value.tipo = '';
    });

    watch(dataLimite, (novaData) => {
        fieldErrors.value.dataLimite = '';
        // Só valida se a data parecer completa (10 caracteres no formato yyyy-mm-dd)
        if (novaData?.length === 10 && !ehDataEstritamenteFutura(novaData)) {
            fieldErrors.value.dataLimite = 'A data limite deve ser uma data futura.';
        }
    });

    watch(unidadesSelecionadas, () => {
        fieldErrors.value.unidades = '';
    });

    const isFormInvalid = computed(() => {
        return !descricao.value.trim() ||
            !tipo.value ||
            dataLimite.value?.length !== 10 ||
            !!fieldErrors.value.dataLimite ||
            unidadesSelecionadas.value.length === 0;
    });

    function construirCriarRequest(): CriarProcessoRequest {
        if (!tipo.value) {
            throw new Error("Tipo de processo é obrigatório");
        }
        return {
            descricao: descricao.value,
            tipo: tipo.value,
            dataLimiteEtapa1: dataLimite.value ? `${dataLimite.value}T00:00:00` : null,
            unidades: unidadesSelecionadas.value,
        };
    }

    function construirAtualizarRequest(codigo: number): AtualizarProcessoRequest {
        if (!tipo.value) {
            throw new Error("Tipo de processo é obrigatório");
        }
        return {
            codigo,
            descricao: descricao.value,
            tipo: tipo.value,
            dataLimiteEtapa1: dataLimite.value ? `${dataLimite.value}T00:00:00` : null,
            unidades: unidadesSelecionadas.value,
        };
    }

    function limpar() {
        descricao.value = '';
        tipo.value = null;
        dataLimite.value = '';
        unidadesSelecionadas.value = [];
        limparErros();
    }

    return {
        descricao,
        tipo,
        dataLimite,
        unidadesSelecionadas,
        fieldErrors,
        isFormInvalid,
        aplicarErroNormalizado,
        limparErros,
        temErros,
        construirCriarRequest,
        construirAtualizarRequest,
        limpar,
    };
}
