import { Conhecimento } from './conhecimento';

export interface Atividade {
  codigo: number;
  descricao: string;
  conhecimentos: Conhecimento[];
}

export interface CriarAtividadeRequest {
  descricao: string;
}