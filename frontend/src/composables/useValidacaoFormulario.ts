import {nextTick, ref} from "vue";

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

    async function focarPrimeiroErroInvalido(): Promise<void> {
        await nextTick();
        const primeiroInvalido = document.querySelector('.is-invalid') as HTMLElement | null;
        primeiroInvalido?.focus();
    }

    return {
        validacaoSubmetida,
        registrarTentativaSubmissao,
        resetarValidacao,
        deveExibirErro,
        validarSubmissao,
        focarPrimeiroErroInvalido
    };
}
