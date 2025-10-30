export interface AutenticacaoRequest {
  tituloEleitoral: number;
  senha: string;
}

export interface EntrarRequest {
  tituloEleitoral: number;
  perfil: string;
  unidadeCodigo: number;
}

export type Perfil = 'ADMIN' | 'GESTOR' | 'CHEFE' | 'SERVIDOR';

export interface Unidade {
  codigo: number;
  nome: string;
  sigla: string;
  // Outros campos da Unidade podem ser adicionados conforme necessário
}

export interface Usuario {
  tituloEleitoral: number;
  nome: string;
  email: string;
  ramal: string;
  unidade: Unidade;
  perfis: Perfil[];
}

export interface PerfilUnidade {
  perfil: Perfil;
  unidade: Unidade;
  siglaUnidade: string;
}

export function mapPerfilUnidadeToFrontend(perfilUnidadeDto: any): PerfilUnidade {
  return {
    perfil: perfilUnidadeDto.perfil,
    unidade: {
      codigo: perfilUnidadeDto.unidade.codigo,
      nome: perfilUnidadeDto.unidade.nome,
      sigla: perfilUnidadeDto.unidade.sigla,
    },
    siglaUnidade: perfilUnidadeDto.siglaUnidade,
  };
}

export function mapUsuarioToFrontend(usuarioDto: any): Usuario {
  return {
    tituloEleitoral: usuarioDto.tituloEleitoral,
    nome: usuarioDto.nome,
    email: usuarioDto.email,
    ramal: usuarioDto.ramal,
    unidade: {
      codigo: usuarioDto.unidade.codigo,
      nome: usuarioDto.unidade.nome,
      sigla: usuarioDto.unidade.sigla,
    },
    perfis: usuarioDto.perfis,
  };
}

export interface LoginResponse {
  tituloEleitoral: number;
  perfil: Perfil; // Usando o tipo Perfil já definido
  unidadeCodigo: number;
  token: string;
}