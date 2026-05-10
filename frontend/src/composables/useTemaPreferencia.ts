import {ref} from "vue";

const CHAVE_TEMA_ESCURO = "temaEscuro";
const temaEscuro = ref(false);
const codigoUsuarioTema = ref<string | null>(null);

function montarChaveTemaEscuro(codigoUsuario: string): string {
    return `${CHAVE_TEMA_ESCURO}:${codigoUsuario}`;
}

function lerTemaEscuroPorUsuario(codigoUsuario: string): boolean {
    const valor = localStorage.getItem(montarChaveTemaEscuro(codigoUsuario));
    return valor === "true";
}

function salvarTemaEscuroPorUsuario(codigoUsuario: string, novoValor: boolean) {
    localStorage.setItem(montarChaveTemaEscuro(codigoUsuario), String(novoValor));
}

export function useTemaPreferencia() {
    function setContextoUsuarioTemaEscuro(codigoUsuario: string | null | undefined) {
        codigoUsuarioTema.value = codigoUsuario ? String(codigoUsuario) : null;
        if (!codigoUsuarioTema.value) {
            temaEscuro.value = false;
            return;
        }

        temaEscuro.value = lerTemaEscuroPorUsuario(codigoUsuarioTema.value);
    }

    function getTemaEscuro(): boolean {
        return temaEscuro.value;
    }

    function setTemaEscuro(novoValor: boolean) {
        temaEscuro.value = novoValor;
        if (codigoUsuarioTema.value) {
            salvarTemaEscuroPorUsuario(codigoUsuarioTema.value, novoValor);
        }
    }

    return {
        setContextoUsuarioTemaEscuro,
        getTemaEscuro,
        setTemaEscuro,
    };
}
