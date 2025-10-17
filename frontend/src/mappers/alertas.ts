export interface Alerta {
  codigo: number;
  processoCodigo: number;
  descricao: string;
  dataHora: string; // Ou Date, dependendo de como será usado no frontend
  unidadeOrigemCodigo: number;
  unidadeDestinoCodigo: number;
  usuarioDestinoTitulo: string;
}

export function mapAlertaDtoToFrontend(dto: any): Alerta {
  return {
    codigo: dto.codigo,
    processoCodigo: dto.processoCodigo,
    descricao: dto.descricao,
    dataHora: dto.dataHora,
    unidadeOrigemCodigo: dto.unidadeOrigemCodigo,
    unidadeDestinoCodigo: dto.unidadeDestinoCodigo,
    usuarioDestinoTitulo: dto.usuarioDestinoTitulo,
  };
}