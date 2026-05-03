import {computed, type ComputedRef} from 'vue';
import type {PermissoesSubprocesso} from '@/types/tipos';

const CHAVES_PERMISSOES = [
    'podeEditarCadastro', 'podeDisponibilizarCadastro', 'podeDevolverCadastro', 'podeAceitarCadastro',
    'podeHomologarCadastro', 'podeEditarMapa', 'podeDisponibilizarMapa', 'podeValidarMapa',
    'podeApresentarSugestoes', 'podeVerSugestoes', 'podeDevolverMapa', 'podeAceitarMapa',
    'podeHomologarMapa', 'podeVisualizarImpacto', 'podeAlterarDataLimite', 'podeReabrirCadastro',
    'podeReabrirRevisao', 'podeEnviarLembrete', 'mesmaUnidade', 'habilitarAcessoCadastro',
    'habilitarAcessoMapa', 'habilitarEditarCadastro', 'habilitarDisponibilizarCadastro',
    'habilitarDevolverCadastro', 'habilitarAceitarCadastro', 'habilitarHomologarCadastro',
    'habilitarEditarMapa', 'habilitarDisponibilizarMapa', 'habilitarValidarMapa',
    'habilitarApresentarSugestoes', 'habilitarDevolverMapa', 'habilitarAceitarMapa',
    'habilitarHomologarMapa', 'habilitarAlterarDataLimite', 'habilitarReabrirCadastro',
    'habilitarReabrirRevisao', 'habilitarEnviarLembrete',
] as const satisfies readonly (keyof PermissoesSubprocesso)[];

type ChavePermissao = (typeof CHAVES_PERMISSOES)[number];

export function criarAcessosPermissao(
    permissoes: ComputedRef<PermissoesSubprocesso>,
): Record<ChavePermissao, ComputedRef<boolean>> {
    return CHAVES_PERMISSOES.reduce((acessos, chave) => {
        acessos[chave] = computed(() => permissoes.value[chave]);
        return acessos;
    }, {} as Record<ChavePermissao, ComputedRef<boolean>>);
}
