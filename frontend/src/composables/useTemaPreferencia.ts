import {ref} from "vue";

const CHAVE_TEMA_ESCURO = "temaEscuro";
const temaEscuro = ref(false);
const codigoUsuarioTema = ref<string | null>(null);
const storage: Pick<Storage, "getItem" | "setItem"> = localStorage;

function montarChaveTemaEscuro(codigoUsuario: string): string {
    return `${CHAVE_TEMA_ESCURO}:${codigoUsuario}`;
}

function lerTemaEscuroPorUsuario(codigoUsuario: string): boolean {
    const valor = storage.getItem(montarChaveTemaEscuro(codigoUsuario));
    return valor === "true";
}

function salvarTemaEscuroPorUsuario(codigoUsuario: string, novoValor: boolean) {
    storage.setItem(montarChaveTemaEscuro(codigoUsuario), String(novoValor));
}

export function useTemaPreferencia() {
    function definirContextoUsuarioTemaEscuro(codigoUsuario: string | null | undefined) {
        codigoUsuarioTema.value = codigoUsuario ? String(codigoUsuario) : null;
        if (!codigoUsuarioTema.value) {
            temaEscuro.value = false;
            return;
        }

        temaEscuro.value = lerTemaEscuroPorUsuario(codigoUsuarioTema.value);
    }

    function obterTemaEscuro(): boolean {
        return temaEscuro.value;
    }

    function definirTemaEscuro(novoValor: boolean) {
        temaEscuro.value = novoValor;
        if (codigoUsuarioTema.value) {
            salvarTemaEscuroPorUsuario(codigoUsuarioTema.value, novoValor);
        }
    }

    return {
        definirContextoUsuarioTemaEscuro,
        obterTemaEscuro,
        definirTemaEscuro,
    };
}
