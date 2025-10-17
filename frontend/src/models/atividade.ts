export interface Conhecimento {
  codigo: number;
  descricao: string;
}

export interface Atividade {
  codigo: number;
  descricao: string;
  conhecimentos: Conhecimento[];
}

export interface CriarAtividadeRequest {
  descricao: string;
}

export interface CriarConhecimentoRequest {
    descricao: string;
}