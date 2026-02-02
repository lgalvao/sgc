import type {LoginResponseDto, PerfilUnidadeDto, UsuarioDto} from "@/types/dtos";

export interface AutenticacaoRequest {
    tituloEleitoral: string;
    senha: string;
}

export interface EntrarRequest {
    tituloEleitoral: string;
    perfil: string;
    unidadeCodigo: number;
}

export type Perfil = "ADMIN" | "GESTOR" | "CHEFE" | "SERVIDOR";

export interface Unidade {
    codigo: number;
    nome: string;
    sigla: string;
}

export interface Usuario {
    tituloEleitoral: string;
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

export function mapPerfilUnidadeToFrontend(
    perfilUnidadeDto: PerfilUnidadeDto,
): PerfilUnidade {
    return {
        perfil: perfilUnidadeDto.perfil as Perfil,
        unidade: {
            codigo: perfilUnidadeDto.unidade.codigo,
            nome: perfilUnidadeDto.unidade.nome,
            sigla: perfilUnidadeDto.unidade.sigla,
        },
        siglaUnidade: perfilUnidadeDto.siglaUnidade,
    };
}

export function mapUsuarioToFrontend(usuarioDto: UsuarioDto): Usuario {
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
        perfis: usuarioDto.perfis as Perfil[],
    };
}

export interface LoginResponse {
    tituloEleitoral: string;
    nome: string;
    perfil: Perfil; // Usando o tipo Perfil já definido
    unidadeCodigo: number;
    token: string;
}

export function LoginResponseToFrontend(response: LoginResponseDto): LoginResponse {
    return {
        tituloEleitoral: response.tituloEleitoral,
        nome: response.nome,
        perfil: response.perfil as Perfil,
        unidadeCodigo: response.unidadeCodigo,
        token: response.token,
    };
}

export function perfisUnidadesParaDominio(
    perfisUnidadesBackend: PerfilUnidadeDto[]
): PerfilUnidade[] {
    return perfisUnidadesBackend.map((item) => ({
        perfil: item.perfil as Perfil,
        unidade: {
            codigo: item.unidade.codigo,
            nome: item.unidade.nome,
            sigla: item.unidade.sigla,
        },
        siglaUnidade: item.unidade.sigla,
    }));
}

