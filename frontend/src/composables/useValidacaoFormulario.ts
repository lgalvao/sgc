import {ref} from "vue";

export function useValidacaoFormulario() {
    const validacaoSubmetida = ref(false);

    function registrarTentativaSubmissao() {
        validacaoSubmetida.value = true;
    }

    function resetarValidacao() {
        validacaoSubmetida.value = false;
    }

    function deveExibirErro(condicaoInvalida: boolean): boolean {
        return validacaoSubmetida.value && condicaoInvalida;
    }

    function validarSubmissao(formularioValido: boolean): boolean {
        registrarTentativaSubmissao();
        return formularioValido;
    }

    return {
        validacaoSubmetida,
        registrarTentativaSubmissao,
        resetarValidacao,
        deveExibirErro,
        validarSubmissao
    };
}
