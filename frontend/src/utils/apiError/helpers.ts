import type {ErroNormalizado} from "./types";

/**
 * Extrai mensagens de erro que não estão vinculadas a um campo específico (erros globais/genéricos).
 */
export function extrairErrosGenericos(error: ErroNormalizado): string[] {
    if (!error.erros) return [];
    return error.erros
        .filter(e => !e.campo && !!e.mensagem)
        .map(e => e.mensagem as string);
}
