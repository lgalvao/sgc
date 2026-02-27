import type {LoginResponseDto, PerfilUnidadeDto, UsuarioDto} from "@/types/dtos";
import type {Usuario} from "@/types/tipos";
import apiClient from "../axios-setup";

// ------------------------------------------------------------------------------------------------
// Mappers Internos (formerly in /mappers/sgrh.ts & /mappers/usuarios.ts)
// ------------------------------------------------------------------------------------------------

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

// NOTE: 'Usuario' type is imported from "@/types/tipos" to avoid conflict/duplication

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


// ------------------------------------------------------------------------------------------------
// Usuario Services
// ------------------------------------------------------------------------------------------------

export async function autenticar(
    request: AutenticacaoRequest,
): Promise<boolean> {
    const response = await apiClient.post<boolean>(
        "/usuarios/autenticar",
        request,
    );
    return response.data;
}

export async function autorizar(
    tituloEleitoral: string,
): Promise<PerfilUnidade[]> {
    const response = await apiClient.post<any[]>(
        "/usuarios/autorizar",
        { tituloEleitoral },
    );
    return response.data.map(mapPerfilUnidadeToFrontend);
}

export async function entrar(request: EntrarRequest): Promise<LoginResponse> {
    const response = await apiClient.post<LoginResponse>(
        "/usuarios/entrar",
        request,
    );
    return response.data;
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
