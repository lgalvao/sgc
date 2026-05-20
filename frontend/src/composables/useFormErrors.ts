import {ref} from 'vue';
import type {ErroNormalizado} from '@/utils/apiError';

export function useFormErrors(initialFields: string[] = []) {
    const erros = ref<Record<string, string>>(
        Object.fromEntries(initialFields.map(f => [f, '']))
    );

    function limparErros() {
        Object.keys(erros.value).forEach(key => {
            erros.value[key] = '';
        });
    }

    function aplicarErroNormalizado(erroNormalizado: ErroNormalizado | null) {
        limparErros();

        if (!erroNormalizado?.erros) return;

        erroNormalizado.erros.forEach(erro => {
            const campo = erro.campo;
            if (campo && campo in erros.value) {
                erros.value[campo] = erro.mensagem || 'Campo inválido';
            }
        });
    }

    function temErros(): boolean {
        return Object.values(erros.value).some(e => e !== '');
    }

    return {
        erros,
        limparErros,
        aplicarErroNormalizado,
        temErros
    };
}
