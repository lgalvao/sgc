import type {FluxoLoginResponseDto, PerfilUnidadeDto, PermissoesSessaoDto, SessaoLoginDto} from "@/types/dtos";
import type {FluxoLogin, PerfilUnidade, PermissoesSessao, SessaoLogin} from "@/types/autenticacao";
import {Perfil, type UsuarioPesquisa} from "@/types/tipos";
import {apiGet, apiPost} from "@/utils/apiUtils";

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
        mostrarDiagnosticoOrganizacional: response.mostrarDiagnosticoOrganizacional,
        mostrarMenuConfiguracoes: response.mostrarMenuConfiguracoes,
        mostrarMenuAdministradores: response.mostrarMenuAdministradores,
        mostrarCriarAtribuicaoTemporaria: response.mostrarCriarAtribuicaoTemporaria,
    };
}

function mapearPerfilUnidade(perfilUnidadeDto: PerfilUnidadeDto): PerfilUnidade {
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

function mapearSessaoLogin(response: SessaoLoginDto): SessaoLogin {
    return {
        tituloEleitoral: response.tituloEleitoral,
        nome: response.nome,
        perfil: response.perfil as Perfil,
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
    const response = await apiPost<FluxoLoginResponseDto, AutenticacaoRequest>("/usuarios/login", request);
    return mapearFluxoLogin(response);
}

export async function entrar(request: EntrarRequest): Promise<SessaoLogin> {
    const response = await apiPost<SessaoLoginDto, EntrarRequest>("/usuarios/entrar", request);
    return mapearSessaoLogin(response);
}

export async function logout(): Promise<void> {
    await apiPost("/usuarios/logout");
}

export async function pesquisarUsuarios(termo: string): Promise<UsuarioPesquisa[]> {
    return apiGet<UsuarioPesquisa[]>("/usuarios/pesquisar", {termo});
}
