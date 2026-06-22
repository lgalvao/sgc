import {computed, type ComputedRef} from 'vue';
import type {PermissoesSubprocesso} from '@/types/tipos';

const CHAVES_PERMISSOES = [
    'podeEditarCadastro', 'podeDisponibilizarCadastro', 'podeDevolverCadastro', 'podeAceitarCadastro',
    'podeHomologarCadastro', 'podeEditarMapa', 'podeDisponibilizarMapa', 'podeValidarMapa',
    'podeApresentarSugestoes', 'podeVerSugestoes', 'podeDevolverMapa', 'podeAceitarMapa',
    'podeHomologarMapa', 'podeVisualizarImpacto', 'podeAlterarDataLimite', 'podeReabrirCadastro',
    'podeReabrirRevisao', 'podeEnviarLembrete', 'mostrarExportacaoMapa', 'mostrarHistoricoAnaliseDiagnostico',
    'podePreencherAutoavaliacao', 'podeCriarConsenso',
    'podeConcluirDiagnostico', 'podeValidarDiagnostico', 'podeDevolverDiagnostico', 'podeHomologarDiagnostico',
    'mesmaUnidade', 'habilitarAcessoCadastro', 'habilitarAcessoMapa', 'habilitarAcessoDiagnostico',
    'habilitarEditarCadastro', 'habilitarDisponibilizarCadastro',
    'habilitarDevolverCadastro', 'habilitarAceitarCadastro', 'habilitarHomologarCadastro',
    'habilitarEditarMapa', 'habilitarDisponibilizarMapa', 'habilitarValidarMapa',
    'habilitarApresentarSugestoes', 'habilitarDevolverMapa', 'habilitarAceitarMapa',
    'habilitarHomologarMapa', 'habilitarAlterarDataLimite', 'habilitarReabrirCadastro',
    'habilitarReabrirRevisao', 'habilitarEnviarLembrete', 'habilitarPreencherAutoavaliacao',
    'habilitarCardConsenso', 'habilitarCardSituacaoCapacitacao',
    'habilitarCriarConsenso', 'habilitarConcluirDiagnostico', 'habilitarValidarDiagnostico',
    'habilitarDevolverDiagnostico', 'habilitarHomologarDiagnostico',
] as const satisfies readonly (keyof PermissoesSubprocesso)[];

type ChavePermissao = (typeof CHAVES_PERMISSOES)[number];

const toAny = (val: unknown) => {
    if (typeof val === 'string') {
        try {
            return JSON.parse(val);
        } catch {
            return val;
        }
    }
    return val;
};

export function criarAcessosPermissao(
    permissoes: ComputedRef<PermissoesSubprocesso>,
): Record<ChavePermissao, ComputedRef<boolean>> {
    const acessos: Partial<Record<ChavePermissao, ComputedRef<boolean>>> = {};
    CHAVES_PERMISSOES.forEach((chave) => {
        acessos[chave] = computed(() => Boolean(permissoes.value[chave]));
    });
    return toAny(acessos);
}
