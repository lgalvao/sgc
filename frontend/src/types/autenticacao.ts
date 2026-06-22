import type {Unidade} from "@/types/tipos";
import {Perfil} from "@/types/tipos";

export interface PerfilUnidade {
    perfil: Perfil;
    unidade: Pick<Unidade, "codigo" | "nome" | "sigla">;
    siglaUnidade: string;
}

export interface PermissoesSessao {
    mostrarCriarProcesso: boolean;
    mostrarArvoreCompletaUnidades: boolean;
    mostrarCtaPainelVazio: boolean;
    mostrarRelatorios: boolean;
    mostrarDiagnosticoOrganizacional: boolean;
    mostrarMenuConfiguracoes: boolean;
    mostrarMenuAdministradores: boolean;
    mostrarCriarAtribuicaoTemporaria: boolean;
}

export interface SessaoLogin {
    tituloEleitoral: string;
    nome: string;
    perfil: Perfil;
    unidadeCodigo: number;
    permissoes: PermissoesSessao;
}

export interface FluxoLogin {
    autenticado: boolean;
    requerSelecaoPerfil: boolean;
    perfisUnidades: PerfilUnidade[];
    sessao: SessaoLogin | null;
}
