import type {
    FluxoLoginResponseDto,
    PerfilUnidadeDto,
    PermissoesSessaoDto,
    SessaoLoginDto,
    UsuarioDto
} from "@/types/dtos";
import {type Usuario, type UsuarioPesquisa, Perfil} from "@/types/tipos";
import apiClient from "../axios-setup";

// Cache de usuários em memória para evitar requisições redundantes
const cacheUsuarios = new Map<string, Usuario>();

export interface AutenticacaoRequest {
    tituloEleitoral: string;
    senha: string;
}

export interface EntrarRequest {
    perfil: string;
    unidadeCodigo: number;
}

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
        matricula: usuarioDto.matricula,
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
    titulo: string;
    matricula: string;
    nome: string;
    email: string;
    ramal: string;
    unidade_lot_codigo: number;
    unidade_sigla?: string;
}

export function mapVWUsuarioToUsuario(vw: VWUsuario): Usuario {
    const codigo = /^\d+$/.test(vw.titulo) ? Number(vw.titulo) : 0;

    return {
        codigo,
        tituloEleitoral: vw.titulo,
        nome: vw.nome,
        matricula: vw.matricula,
        unidade: {
            codigo: vw.unidade_lot_codigo,
            nome: "", // Nome será carregado se necessário ou virá de outra fonte
            sigla: vw.unidade_sigla ?? "",
        },
        email: vw.email,
        ramal: vw.ramal,
        perfis: [],
    };
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

export async function logout(): Promise<void> {
    await apiClient.post("/usuarios/logout");
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
    if (cacheUsuarios.has(titulo)) {
        return cacheUsuarios.get(titulo)!;
    }
    const response = await apiClient.get(`/usuarios/${titulo}`);
    const usuario = response.data;
    cacheUsuarios.set(titulo, usuario);
    return usuario;
}

export async function pesquisarUsuarios(termo: string): Promise<UsuarioPesquisa[]> {
    const response = await apiClient.get<UsuarioPesquisa[]>(`/usuarios/pesquisar`, {
        params: {termo}
    });
    return response.data;
}
