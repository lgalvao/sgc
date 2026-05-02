import type {Unidade} from "./organizacao";

export interface Conhecimento {
  codigo: number;
  descricao: string;
}

export interface Atividade {
  codigo: number;
  descricao: string;
  conhecimentos: Conhecimento[];
  mapaCodigo?: number;
}

export interface Competencia {
  codigo: number;
  descricao: string;
  atividades: Atividade[];
}

export interface SalvarCompetenciaRequest {
  descricao: string;
  atividadesCodigos: number[];
}

export interface CriarAtividadeRequest {
  descricao: string;
}

export interface CriarConhecimentoRequest {
  descricao: string;
}

export interface Mapa {
  codigo: number;
  descricao: string;
  unidade: Unidade;
  codProcesso: number;
  competencias: Competencia[];
  situacao: string;
  dataCriacao: string;
  dataDisponibilizacao?: string;
  dataFinalizacao?: string;
}

export interface MapaResumo {
  codigo: number;
  subprocessoCodigo: number;
  dataHoraDisponibilizado?: string | null;
  observacoesDisponibilizacao?: string | null;
  sugestoes?: string | null;
  dataHoraHomologado?: string | null;
}

export interface MapaVisualizacao {
  codigo: number;
  descricao: string;
  competencias: Competencia[];
  atividadesSemCompetencia?: Atividade[];
  sugestoes?: string;
}

export interface SalvarMapaRequest {
  competencias: {
    codigo?: number;
    descricao: string;
    atividades: {
      codigo?: number;
      descricao: string;
      conhecimentos: {
        codigo?: number;
        descricao: string;
      }[];
    }[];
  }[];
}

export interface SalvarAjustesRequest {
  competencias: {
    codigo: number;
    descricao: string;
  }[];
  atividades: {
    codigo: number;
    descricao: string;
  }[];
  sugestoes: string;
}

export interface MapaCompleto {
  codigo: number;
  subprocessoCodigo: number;
  observacoes: string;
  competencias: Competencia[];
  atividades: Atividade[];
  situacao: string;
}

export interface MapaAjuste {
  codigo: number;
  descricao: string;
  competencias: Competencia[];
}

export interface DisponibilizarMapaRequest {
  dataLimite: string;
  observacoes: string;
}

export enum TipoImpactoAtividade {
  INSERIDA = "INSERIDA",
  REMOVIDA = "REMOVIDA",
  ALTERADA = "ALTERADA",
}

export enum TipoImpactoCompetencia {
  ATIVIDADE_REMOVIDA = "ATIVIDADE_REMOVIDA",
  ATIVIDADE_ALTERADA = "ATIVIDADE_ALTERADA",
}

export interface AtividadeImpactada {
  codigo: number;
  descricao: string;
  tipoImpacto: TipoImpactoAtividade;
  descricaoAnterior?: string;
  conhecimentos?: string[];
  conhecimentosAdicionados?: string[];
  conhecimentosRemovidos?: string[];
  competenciasVinculadas: string[];
}

export interface CompetenciaImpactada {
  codigo: number;
  descricao: string;
  atividadesAfetadas: string[];
  tiposImpacto: TipoImpactoCompetencia[];
}

export interface ImpactoMapa {
  temImpactos: boolean;
  totalAtividadesInseridas: number;
  totalAtividadesRemovidas: number;
  totalAtividadesAlteradas: number;
  totalCompetenciasImpactadas: number;
  atividadesInseridas: AtividadeImpactada[];
  atividadesRemovidas: AtividadeImpactada[];
  atividadesAlteradas: AtividadeImpactada[];
  competenciasImpactadas: CompetenciaImpactada[];
}
