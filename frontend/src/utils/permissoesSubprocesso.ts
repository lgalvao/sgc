import type {PermissoesSubprocesso} from "@/types/tipos";

export const PERMISSOES_SUBPROCESSO_VAZIAS: PermissoesSubprocesso = {
    podeEditarCadastro: false,
    podeDisponibilizarCadastro: false,
    podeDevolverCadastro: false,
    podeAceitarCadastro: false,
    podeHomologarCadastro: false,
    podeEditarMapa: false,
    podeDisponibilizarMapa: false,
    podeValidarMapa: false,
    podeApresentarSugestoes: false,
    podeVerSugestoes: false,
    podeDevolverMapa: false,
    podeAceitarMapa: false,
    podeHomologarMapa: false,
    podeVisualizarImpacto: false,
    podeAlterarDataLimite: false,
    podeReabrirCadastro: false,
    podeReabrirRevisao: false,
    podeEnviarLembrete: false,
    mesmaUnidade: false,
    habilitarAcessoCadastro: false,
    habilitarAcessoMapa: false,
    habilitarEditarCadastro: false,
    habilitarDisponibilizarCadastro: false,
    habilitarDevolverCadastro: false,
    habilitarAceitarCadastro: false,
    habilitarHomologarCadastro: false,
    habilitarEditarMapa: false,
    habilitarDisponibilizarMapa: false,
    habilitarValidarMapa: false,
    habilitarApresentarSugestoes: false,
    habilitarDevolverMapa: false,
    habilitarAceitarMapa: false,
    habilitarHomologarMapa: false,
};

export function normalizarPermissoesSubprocesso(
    permissoes?: Partial<PermissoesSubprocesso> | null,
): PermissoesSubprocesso {
    return {
        ...PERMISSOES_SUBPROCESSO_VAZIAS,
        ...permissoes,
    };
}
