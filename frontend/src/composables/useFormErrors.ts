import {ref} from 'vue';
import type {NormalizedError} from '@/utils/apiError';

export function useFormErrors(initialFields: string[] = []) {
    const errors = ref<Record<string, string>>(
        Object.fromEntries(initialFields.map(f => [f, '']))
    );

    function clearErrors() {
        Object.keys(errors.value).forEach(key => {
            errors.value[key] = '';
        });
    }

    function setFromNormalizedError(normalizedError: NormalizedError | null) {
        clearErrors();

        if (!normalizedError?.erros) return;

        normalizedError.erros.forEach(erro => {
            const campo = erro.campo;
            if (campo && campo in errors.value) {
                errors.value[campo] = erro.mensagem || 'Campo inválido';
            }
        });
    }

    function hasErrors(): boolean {
        return Object.values(errors.value).some(e => e !== '');
    }

    return {
        errors,
        clearErrors,
        setFromNormalizedError,
        hasErrors
    };
}
