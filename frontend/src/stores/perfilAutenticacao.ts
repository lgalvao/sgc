import type {FluxoLogin, PerfilUnidade, PermissoesSessao} from "@/types/autenticacao";
import type {Perfil} from "@/types/tipos";
import * as usuarioService from "@/services/usuarioService";
import {cancelarRequisicoesPendentes, finalizarTransicaoSessao, iniciarTransicaoSessao} from "@/axios-setup";
import {logger} from "@/utils";

export interface DadosSessaoPerfil {
    tituloEleitoral?: string;
    nome: string;
    perfil: Perfil;
    unidadeCodigo: number;
    unidadeSigla: string;
    permissoes: PermissoesSessao;
}

type AtualizadorSessao = {
    aplicarSessao: (dados: DadosSessaoPerfil) => void;
    atualizarPerfisUnidades: (perfisUnidades: PerfilUnidade[]) => void;
    limparSessao: () => void;
    resetarEstadoAplicacao: () => void;
};

function montarSessaoPerfilUnico(fluxoLogin: FluxoLogin): DadosSessaoPerfil | null {
    if (!fluxoLogin.sessao || fluxoLogin.perfisUnidades.length !== 1) {
        return null;
    }

    return {
        tituloEleitoral: fluxoLogin.sessao.tituloEleitoral,
        nome: fluxoLogin.sessao.nome,
        perfil: fluxoLogin.sessao.perfil,
        unidadeCodigo: fluxoLogin.sessao.unidadeCodigo,
        unidadeSigla: fluxoLogin.perfisUnidades[0].unidade.sigla,
        permissoes: fluxoLogin.sessao.permissoes,
    };
}

function montarSessaoPerfilSelecionado(
    perfilUnidade: PerfilUnidade,
    respostaLogin: Awaited<ReturnType<typeof usuarioService.entrar>>,
): DadosSessaoPerfil {
    return {
        tituloEleitoral: respostaLogin.tituloEleitoral,
        nome: respostaLogin.nome,
        perfil: respostaLogin.perfil,
        unidadeCodigo: respostaLogin.unidadeCodigo,
        unidadeSigla: perfilUnidade.unidade.sigla,
        permissoes: respostaLogin.permissoes,
    };
}

function concluirTransicaoSessao(atualizador: AtualizadorSessao, dados: DadosSessaoPerfil): void {
    atualizador.resetarEstadoAplicacao();
    atualizador.aplicarSessao(dados);
    finalizarTransicaoSessao();
}

export async function iniciarLoginPerfil(
    atualizador: AtualizadorSessao,
    tituloEleitoral: string,
    senha: string,
): Promise<FluxoLogin> {
    const fluxoLogin = await usuarioService.login({
        tituloEleitoral,
        senha,
    });
    atualizador.atualizarPerfisUnidades(fluxoLogin.perfisUnidades);

    const sessaoPerfilUnico = montarSessaoPerfilUnico(fluxoLogin);
    if (sessaoPerfilUnico) {
        concluirTransicaoSessao(atualizador, sessaoPerfilUnico);
    }

    return fluxoLogin;
}

export async function concluirLoginPerfil(
    atualizador: AtualizadorSessao,
    perfilUnidade: PerfilUnidade,
): Promise<void> {
    const respostaLogin = await usuarioService.entrar({
        perfil: perfilUnidade.perfil,
        unidadeCodigo: perfilUnidade.unidade.codigo,
    });

    concluirTransicaoSessao(atualizador, montarSessaoPerfilSelecionado(perfilUnidade, respostaLogin));
}

export async function encerrarLoginPerfil(atualizador: AtualizadorSessao): Promise<void> {
    iniciarTransicaoSessao();
    cancelarRequisicoesPendentes();

    try {
        await usuarioService.logout();
    } catch (erro) {
        logger.warn("Falha ao encerrar sessao remota; limpando sessao local.", erro);
    }

    atualizador.limparSessao();
    atualizador.resetarEstadoAplicacao();
}
