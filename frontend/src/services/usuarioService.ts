import type {FluxoLogin, PerfilUnidade, PermissoesSessao, SessaoLogin} from "@/types/autenticacao";
import {Perfil, type UsuarioPesquisa} from "@/types/tipos";
import {apiGet, apiPost} from "@/utils/apiUtils";

interface UnidadeDto {
    codigo: number;
    nome: string;
    sigla: string;
}

interface PerfilUnidadeDto {
    perfil: string;
    unidade: UnidadeDto;
    siglaUnidade: string;
}

interface SessaoLoginDto {
    tituloEleitoral: string;
    nome: string;
    perfil: string;
    unidadeCodigo: number;
    permissoes: PermissoesSessaoDto;
}

interface PermissoesSessaoDto {
    mostrarCriarProcesso: boolean;
    mostrarArvoreCompletaUnidades: boolean;
    mostrarCtaPainelVazio: boolean;
    mostrarRelatorios: boolean;
    mostrarDiagnosticoOrganizacional: boolean;
    mostrarMenuConfiguracoes: boolean;
    mostrarMenuAdministradores: boolean;
    mostrarCriarAtribuicaoTemporaria: boolean;
}

interface FluxoLoginResponseDto {
    autenticado: boolean;
    requerSelecaoPerfil: boolean;
    perfisUnidades: PerfilUnidadeDto[];
    sessao: SessaoLoginDto | null;
}

interface AutenticacaoRequest {
    tituloEleitoral: string;
    senha: string;
}

interface EntrarRequest {
    perfil: string;
    unidadeCodigo: number;
}

function mapearPermissoesSessao(response: PermissoesSessaoDto): PermissoesSessao {
    return {
        mostrarCriarProcesso: response.mostrarCriarProcesso,
        mostrarArvoreCompletaUnidades: response.mostrarArvoreCompletaUnidades,
        mostrarCtaPainelVazio: response.mostrarCtaPainelVazio,
        mostrarRelatorios: response.mostrarRelatorios,
        mostrarDiagnosticoOrganizacional: response.mostrarDiagnosticoOrganizacional,
        mostrarMenuConfiguracoes: response.mostrarMenuConfiguracoes,
        mostrarMenuAdministradores: response.mostrarMenuAdministradores,
        mostrarCriarAtribuicaoTemporaria: response.mostrarCriarAtribuicaoTemporaria,
    };
}

function converterParaPerfil(valor: string): Perfil {
    const perfis: Record<string, Perfil> = {
        ADMIN: Perfil.ADMIN,
        GESTOR: Perfil.GESTOR,
        CHEFE: Perfil.CHEFE,
        SERVIDOR: Perfil.SERVIDOR
    };
    return perfis[valor] || Perfil.SERVIDOR;
}

function mapearPerfilUnidade(perfilUnidadeDto: PerfilUnidadeDto): PerfilUnidade {
    return {
        perfil: converterParaPerfil(perfilUnidadeDto.perfil),
        unidade: {
            codigo: perfilUnidadeDto.unidade.codigo,
            nome: perfilUnidadeDto.unidade.nome,
            sigla: perfilUnidadeDto.unidade.sigla,
        },
        siglaUnidade: perfilUnidadeDto.siglaUnidade,
    };
}

function mapearSessaoLogin(response: SessaoLoginDto): SessaoLogin {
    return {
        tituloEleitoral: response.tituloEleitoral,
        nome: response.nome,
        perfil: converterParaPerfil(response.perfil),
        unidadeCodigo: response.unidadeCodigo,
        permissoes: mapearPermissoesSessao(response.permissoes),
    };
}

function mapearFluxoLogin(response: FluxoLoginResponseDto): FluxoLogin {
    return {
        autenticado: response.autenticado,
        requerSelecaoPerfil: response.requerSelecaoPerfil,
        perfisUnidades: response.perfisUnidades.map(mapearPerfilUnidade),
        sessao: response.sessao ? mapearSessaoLogin(response.sessao) : null,
    };
}

export async function login(
    request: AutenticacaoRequest,
): Promise<FluxoLogin> {
    const response = await apiPost<FluxoLoginResponseDto>("/usuarios/login", request);
    return mapearFluxoLogin(response);
}

export async function entrar(request: EntrarRequest): Promise<SessaoLogin> {
    const response = await apiPost<SessaoLoginDto>("/usuarios/entrar", request);
    return mapearSessaoLogin(response);
}

export async function logout(): Promise<void> {
    await apiPost("/usuarios/logout");
}

export async function pesquisarUsuarios(termo: string): Promise<UsuarioPesquisa[]> {
    return apiGet<UsuarioPesquisa[]>("/usuarios/pesquisar", {termo});
}
