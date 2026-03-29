import type {FluxoLoginResponseDto, PerfilUnidadeDto, SessaoLoginDto, UsuarioDto} from "@/types/dtos";
import type {Usuario} from "@/types/tipos";
import apiClient from "../axios-setup";

// Mappers internos (formerly in /mappers/sgrh.ts & /mappers/usuarios.ts)

export interface AutenticacaoRequest {
    tituloEleitoral: string;
    senha: string;
}

export interface EntrarRequest {
    perfil: string;
    unidadeCodigo: number;
}

export type Perfil = "ADMIN" | "GESTOR" | "CHEFE" | "SERVIDOR";

export interface Unidade {
    codigo: number;
    nome: string;
    sigla: string;
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
    const codigo = /^\d+$/.test(usuarioDto.tituloEleitoral) ? Number(usuarioDto.tituloEleitoral) : 0;
    return {
        codigo,
        tituloEleitoral: usuarioDto.tituloEleitoral,
        nome: usuarioDto.nome,
        email: usuarioDto.email,
        ramal: usuarioDto.ramal,
        unidade: {
            codigo: usuarioDto.unidade.codigo,
            nome: usuarioDto.unidade.nome,
            sigla: usuarioDto.unidade.sigla,
        },
        perfis: usuarioDto.perfis as any[], // Casting to match generic array if needed, or specific
    } as Usuario; // Force cast to match potentially looser frontend type
}

export interface SessaoLogin {
    tituloEleitoral: string;
    nome: string;
    perfil: Perfil;
    unidadeCodigo: number;
}

export interface FluxoLogin {
    autenticado: boolean;
    requerSelecaoPerfil: boolean;
    perfisUnidades: PerfilUnidade[];
    sessao: SessaoLogin | null;
}

export function mapSessaoLoginToFrontend(response: SessaoLoginDto): SessaoLogin {
    return {
        tituloEleitoral: response.tituloEleitoral,
        nome: response.nome,
        perfil: response.perfil as Perfil,
        unidadeCodigo: response.unidadeCodigo,
    };
}

export function mapFluxoLoginToFrontend(response: FluxoLoginResponseDto): FluxoLogin {
    return {
        autenticado: response.autenticado,
        requerSelecaoPerfil: response.requerSelecaoPerfil,
        perfisUnidades: perfisUnidadesParaDominio(response.perfisUnidades),
        sessao: response.sessao ? mapSessaoLoginToFrontend(response.sessao) : null,
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

export function mapVWUsuarioToUsuario(vw: any): Usuario {
    const candidateId =
        vw?.codigo ??
        (typeof vw?.titulo === "string" && /^\d+$/.test(vw.titulo)
            ? Number(vw.titulo)
            : undefined) ??
        undefined;
    const codigo = Number(candidateId ?? 0);

    return {
        codigo,
        nome: vw?.nome ?? vw?.nome_completo ?? vw?.nome_usuario ?? "",
        unidade: vw?.unidade ?? vw?.unidade_sigla ?? vw?.unidade_codigo ?? "",
        email: vw?.email ?? null,
        ramal: vw?.ramal ?? vw?.ramal_telefone ?? null,
        tituloEleitoral: vw?.titulo_eleitoral ?? vw?.titulo ?? "",
    } as Usuario;
}

export function mapVWUsuariosArray(arr: any[] = []): Usuario[] {
    return arr.map(mapVWUsuarioToUsuario);
}

export async function login(
    request: AutenticacaoRequest,
): Promise<FluxoLogin> {
    const response = await apiClient.post<FluxoLoginResponseDto>(
        "/usuarios/login",
        request,
    );
    return mapFluxoLoginToFrontend(response.data);
}

export async function entrar(request: EntrarRequest): Promise<SessaoLogin> {
    const response = await apiClient.post<SessaoLoginDto>(
        "/usuarios/entrar",
        request,
    );
    return mapSessaoLoginToFrontend(response.data);
}

export async function buscarTodosUsuarios() {
    const response = await apiClient.get("/usuarios");
    return response.data;
}

export async function buscarUsuariosPorUnidade(
    codigoUnidade: number,
): Promise<Usuario[]> {
    const response = await apiClient.get(`/unidades/${codigoUnidade}/usuarios`);
    return response.data;
}

export async function buscarUsuarioPorTitulo(
    titulo: string,
): Promise<Usuario> {
    const response = await apiClient.get(`/usuarios/${titulo}`);
    return response.data;
}

export async function pesquisarUsuarios(termo: string): Promise<Usuario[]> {
    const response = await apiClient.get(`/usuarios/pesquisar`, {
        params: {termo}
    });
    return response.data;
}
