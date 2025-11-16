import { Alerta } from '@/types/tipos';

export function mapAlertaDtoToFrontend(dto: any): Alerta {
  return {
    codigo: dto.codigo,
    codProcesso: dto.codProcesso,
    descricao: dto.descricao,
    dataHora: dto.dataHora,
    unidadeOrigem: dto.unidadeOrigem,
    unidadeDestino: dto.unidadeDestino,
    dataHoraLeitura: dto.dataHoraLeitura,
    linkDestino: dto.linkDestino,
    mensagem: dto.mensagem,
    dataHoraFormatada: dto.dataHoraFormatada,
    origem: dto.origem,
    processo: dto.processo,
  };
}
