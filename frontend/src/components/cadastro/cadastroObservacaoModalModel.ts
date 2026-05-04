import {computed} from 'vue';

interface PropsCadastroObservacaoModal {
    modelValue: boolean;
    observacao: string;
}

interface EmitCadastroObservacaoModal {
    (evento: 'update:modelValue', valor: boolean): void;

    (evento: 'update:observacao', valor: string): void;
}

export function useCadastroObservacaoModalModel(
    props: PropsCadastroObservacaoModal,
    emit: EmitCadastroObservacaoModal
) {
    const model = computed({
        get: () => props.modelValue,
        set: (valor) => emit('update:modelValue', valor)
    });

    const observacaoModel = computed({
        get: () => props.observacao,
        set: (valor) => emit('update:observacao', valor)
    });

    return {
        model,
        observacaoModel
    };
}
