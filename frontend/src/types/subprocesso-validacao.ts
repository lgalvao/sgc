import type {Atividade} from "./mapa";
import type {PermissoesSubprocesso, SubprocessoStatus} from "./subprocesso-modelos";

export interface ErroValidacao {
  tipo: string;
  atividadeCodigo?: number;
  descricaoAtividade?: string;
  mensagem: string;
}

export interface ValidacaoCadastro {
  valido: boolean;
  erros: ErroValidacao[];
}

export interface AtividadeOperacaoResponse {
  atividade: Atividade | null;
  subprocesso: SubprocessoStatus;
  atividadesAtualizadas: Atividade[];
  permissoes: PermissoesSubprocesso;
  message?: string | null;
  aviso?: string | null;
}

export interface RespostaLocalCadastro {
  subprocesso: SubprocessoStatus;
  permissoes: PermissoesSubprocesso;
  atividadesAtualizadas: Atividade[];
}
