import { SituacaoProcesso, TipoProcesso } from "@/types/tipos";

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
