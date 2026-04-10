import type {
    FluxoLoginResponseDto,
    PerfilUnidadeDto,
    PermissoesSessaoDto,
    SessaoLoginDto,
    UsuarioDto
} from "@/types/dtos";
import type {Usuario, UsuarioPesquisa} from "@/types/tipos";
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
        perfis: usuarioDto.perfis as unknown[], // Casting to match generic array if needed, or specific
    } as Usuario; // Force cast to match potentially looser frontend type
}

export interface SessaoLogin {
    tituloEleitoral: string;
    nome: string;
    perfil: Perfil;
    unidadeCodigo: number;
    permissoes: PermissoesSessao;
}

export interface PermissoesSessao {
    mostrarCriarProcesso: boolean;
    mostrarArvoreCompletaUnidades: boolean;
    mostrarCtaPainelVazio: boolean;
    mostrarDiagnosticoOrganizacional: boolean;
    mostrarMenuConfiguracoes: boolean;
    mostrarMenuAdministradores: boolean;
    mostrarCriarAtribuicaoTemporaria: boolean;
}

export interface FluxoLogin {
    autenticado: boolean;
    requerSelecaoPerfil: boolean;
    perfisUnidades: PerfilUnidade[];
    sessao: SessaoLogin | null;
}

export function mapPermissoesSessaoToFrontend(response: PermissoesSessaoDto): PermissoesSessao {
    return {
        mostrarCriarProcesso: response.mostrarCriarProcesso,
        mostrarArvoreCompletaUnidades: response.mostrarArvoreCompletaUnidades,
        mostrarCtaPainelVazio: response.mostrarCtaPainelVazio,
        mostrarDiagnosticoOrganizacional: response.mostrarDiagnosticoOrganizacional,
        mostrarMenuConfiguracoes: response.mostrarMenuConfiguracoes,
        mostrarMenuAdministradores: response.mostrarMenuAdministradores,
        mostrarCriarAtribuicaoTemporaria: response.mostrarCriarAtribuicaoTemporaria,
    };
}

export function mapSessaoLoginToFrontend(response: SessaoLoginDto): SessaoLogin {
    return {
        tituloEleitoral: response.tituloEleitoral,
        nome: response.nome,
        perfil: response.perfil as Perfil,
        unidadeCodigo: response.unidadeCodigo,
        permissoes: mapPermissoesSessaoToFrontend(response.permissoes),
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

export interface VWUsuario {
    codigo?: number;
    titulo?: string;
    nome?: string;
    nome_completo?: string;
    nome_usuario?: string;
    unidade?: unknown;
    unidade_sigla?: string;
    unidade_codigo?: number;
    email?: string | null;
    ramal?: string | null;
    ramal_telefone?: string | null;
    titulo_eleitoral?: string;
}

export function mapVWUsuarioToUsuario(vw: VWUsuario): Usuario {
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
        unidade: (vw?.unidade ?? vw?.unidade_sigla ?? vw?.unidade_codigo ?? "") as unknown,
        email: vw?.email ?? null,
        ramal: vw?.ramal ?? vw?.ramal_telefone ?? null,
        tituloEleitoral: vw?.titulo_eleitoral ?? vw?.titulo ?? "",
    } as Usuario;
}

export function mapVWUsuariosArray(arr: VWUsuario[] = []): Usuario[] {
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

export async function pesquisarUsuarios(termo: string): Promise<UsuarioPesquisa[]> {
    const response = await apiClient.get<UsuarioPesquisa[]>(`/usuarios/pesquisar`, {
        params: {termo}
    });
    return response.data;
}
