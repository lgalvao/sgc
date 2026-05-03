export interface Unidade {
  codigo: number;
  nome: string;
  sigla: string;
  unidadeSuperiorCodigo?: number;
  filhas?: Unidade[];
  tipo?: string;
  usuarioCodigo?: number;
  tituloTitular?: string;
  isElegivel?: boolean;
  tipoResponsabilidade?: string;
  titular?: Usuario | null;
  responsavel?: Responsavel | Usuario | null;
}

export interface MapaVigenteReferencia {
  codProcesso: number;
  codSubprocesso: number;
}

export interface Responsavel {
  codigo: number;
  nome: string;
  tituloEleitoral: string;
  unidade: Unidade;
  email: string;
  ramal: string;
  usuarioTitulo: string;
  unidadeCodigo: number;
  usuarioCodigo: number;
  tipo: string;
  dataInicio: string;
  dataFim: string | null;
}

export interface UsuarioPesquisa {
  tituloEleitoral: string;
  nome: string;
}

export interface Usuario {
  codigo: number;
  nome: string;
  matricula: string;
  tituloEleitoral: string;
  unidade: Unidade;
  email: string;
  ramal: string;
  perfis?: string[];
}

export interface AtribuicaoTemporaria {
  codigo: number;
  usuario: Usuario;
  unidade: Unidade;
  dataInicio: string;
  dataFim: string;
  dataTermino: string;
  justificativa: string;
}

export interface UnidadeSnapshot {
  codigo: number;
  nome: string;
  sigla: string;
  filhas?: UnidadeSnapshot[];
}

