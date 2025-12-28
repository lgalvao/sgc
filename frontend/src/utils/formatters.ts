import {SituacaoProcesso, TipoProcesso} from "@/types/tipos";

export function formatarSituacaoProcesso(situacao: SituacaoProcesso | string): string {
    switch (situacao) {
        case SituacaoProcesso.CRIADO:
            return "Criado";
        case SituacaoProcesso.FINALIZADO:
            return "Finalizado";
        case SituacaoProcesso.EM_ANDAMENTO:
            return "Em andamento";
        default:
            return situacao;
    }
}

export function formatarTipoProcesso(tipo: TipoProcesso | string): string {
    switch (tipo) {
        case TipoProcesso.MAPEAMENTO:
            return "Mapeamento";
        case TipoProcesso.REVISAO:
            return "Revisão";
        case TipoProcesso.DIAGNOSTICO:
            return "Diagnóstico";
        default:
            return tipo;
    }
}

/**
 * Remove caracteres não numéricos de uma string de CPF.
 */
export function limparCpf(cpf: string): string {
    return cpf.replace(/\D/g, "");
}

/**
 * Formata uma string para o padrão de CPF (000.000.000-00).
 * Se o CPF não tiver 11 dígitos, retorna a string original limpa ou parcialmente formatada.
 */
export function formatarCpf(cpf: string): string {
    if (!cpf) return "";

    const limpo = limparCpf(cpf);

    if (limpo.length !== 11) {
        return limpo;
    }

    return limpo.replace(/(\d{3})(\d{3})(\d{3})(\d{2})/, "$1.$2.$3-$4");
}
