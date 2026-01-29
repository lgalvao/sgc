import type {Alerta} from "@/types/tipos";
import type {AlertaDto} from "@/types/dtos";

export function mapAlertaDtoToFrontend(dto: AlertaDto): Alerta {
    return {
        codigo: dto.codigo,
        codProcesso: dto.codProcesso,
        descricao: dto.descricao,
        dataHora: dto.dataHora,
        unidadeOrigem: dto.unidadeOrigem,
        unidadeDestino: dto.unidadeDestino,
        dataHoraLeitura: dto.dataHoraLeitura,
        mensagem: dto.mensagem,
        dataHoraFormatada: dto.dataHoraFormatada,
        origem: dto.origem,
        processo: dto.processo,
    };
}
