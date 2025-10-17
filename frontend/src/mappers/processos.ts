export type SituacaoProcesso = 'CRIADO' | 'EM_ANDAMENTO' | 'FINALIZADO';

export type SituacaoSubprocesso = 'NAO_INICIADO' | 'CADASTRO_EM_ANDAMENTO' | 'CADASTRO_DISPONIBILIZADO' | 'CADASTRO_HOMOLOGADO' | 'MAPA_CRIADO' | 'MAPA_DISPONIBILIZADO' | 'MAPA_COM_SUGESTOES' | 'MAPA_VALIDADO' | 'MAPA_HOMOLOGADO' | 'REVISAO_CADASTRO_EM_ANDAMENTO' | 'REVISAO_CADASTRO_DISPONIBILIZADA' | 'REVISAO_CADASTRO_HOMOLOGADA' | 'MAPA_AJUSTADO';

export interface ProcessoResumo {
  codigo: number;
  descricao: string;
  situacao: SituacaoProcesso;
  tipo: string;
  dataLimite: string; // Ou Date, dependendo de como será usado no frontend
  dataCriacao: string; // Ou Date
  unidadeCodigo: number;
  unidadeNome: string;
}

export interface CriarProcessoRequest {
  descricao: string;
  tipo: string;
  dataLimiteEtapa1: string;
  unidades: number[];
}

export interface AtualizarProcessoRequest {
  codigo: number;
  descricao: string;
  tipo: string;
  dataLimiteEtapa1: string;
  unidades: number[];
}

export interface Processo {
  codigo: number;
  dataCriacao: string;
  dataFinalizacao: string;
  dataLimite: string;
  descricao: string;
  situacao: SituacaoProcesso;
  tipo: string;
}

export interface UnidadeParticipante {
  nome: string;
  sigla: string;
  codUnidade: number;
  codUnidadeSuperior: number;
  situacaoSubprocesso: SituacaoSubprocesso;
  dataLimite: string;
  filhos: UnidadeParticipante[];
}

export interface ProcessoDetalhe {
  codigo: number;
  descricao: string;
  tipo: string;
  situacao: SituacaoProcesso;
  dataLimite: string;
  dataCriacao: string;
  dataFinalizacao: string;
  unidades: UnidadeParticipante[];
  resumoSubprocessos: ProcessoResumo[];
}

export function mapProcessoResumoDtoToFrontend(dto: any): ProcessoResumo {
  return {
    codigo: dto.codigo,
    descricao: dto.descricao,
    situacao: dto.situacao,
    tipo: dto.tipo,
    dataLimite: dto.dataLimite,
    dataCriacao: dto.dataCriacao,
    unidadeCodigo: dto.unidadeCodigo,
    unidadeNome: dto.unidadeNome,
  };
}

export function mapProcessoDtoToFrontend(dto: any): Processo {
  return {
    codigo: dto.codigo,
    dataCriacao: dto.dataCriacao,
    dataFinalizacao: dto.dataFinalizacao,
    dataLimite: dto.dataLimite,
    descricao: dto.descricao,
    situacao: dto.situacao,
    tipo: dto.tipo,
  };
}

export function mapUnidadeParticipanteDtoToFrontend(dto: any): UnidadeParticipante {
  return {
    nome: dto.nome,
    sigla: dto.sigla,
    codUnidade: dto.codUnidade,
    codUnidadeSuperior: dto.codUnidadeSuperior,
    situacaoSubprocesso: dto.situacaoSubprocesso,
    dataLimite: dto.dataLimite,
    filhos: dto.filhos ? dto.filhos.map(mapUnidadeParticipanteDtoToFrontend) : [],
  };
}

export function mapProcessoDetalheDtoToFrontend(dto: any): ProcessoDetalhe {
  return {
    codigo: dto.codigo,
    descricao: dto.descricao,
    tipo: dto.tipo,
    situacao: dto.situacao,
    dataLimite: dto.dataLimite,
    dataCriacao: dto.dataCriacao,
    dataFinalizacao: dto.dataFinalizacao,
    unidades: dto.unidades ? dto.unidades.map(mapUnidadeParticipanteDtoToFrontend) : [],
    resumoSubprocessos: dto.resumoSubprocessos ? dto.resumoSubprocessos.map(mapProcessoResumoDtoToFrontend) : [],
  };
}
