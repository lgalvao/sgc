import { computed, type Ref, unref } from 'vue';
import type { SubprocessoDetalhe } from '@/types/tipos';

/**
 * Hook to access permissions calculated by the backend.
 * 
 * The backend is the single source of truth for security and workflow rules.
 * This hook simplifies access to those rules in Vue components.
 */
export function useAcesso(subprocessoRef: Ref<SubprocessoDetalhe | null> | SubprocessoDetalhe) {
    // Helper to unwrap refs
    const getSubprocesso = () => unref(subprocessoRef);

    // Direct mapping of permissions from the backend
    const podeEditarCadastro = computed(() => getSubprocesso()?.permissoes?.podeEditarCadastro ?? false);
    const podeDisponibilizarCadastro = computed(() => getSubprocesso()?.permissoes?.podeDisponibilizarCadastro ?? false);
    const podeDevolverCadastro = computed(() => getSubprocesso()?.permissoes?.podeDevolverCadastro ?? false);
    const podeAceitarCadastro = computed(() => getSubprocesso()?.permissoes?.podeAceitarCadastro ?? false);
    const podeHomologarCadastro = computed(() => getSubprocesso()?.permissoes?.podeHomologarCadastro ?? false);

    const podeEditarMapa = computed(() => getSubprocesso()?.permissoes?.podeEditarMapa ?? false);
    const podeDisponibilizarMapa = computed(() => getSubprocesso()?.permissoes?.podeDisponibilizarMapa ?? false);
    const podeValidarMapa = computed(() => getSubprocesso()?.permissoes?.podeValidarMapa ?? false);
    const podeApresentarSugestoes = computed(() => getSubprocesso()?.permissoes?.podeApresentarSugestoes ?? false);
    const podeDevolverMapa = computed(() => getSubprocesso()?.permissoes?.podeDevolverMapa ?? false);
    const podeAceitarMapa = computed(() => getSubprocesso()?.permissoes?.podeAceitarMapa ?? false);
    const podeHomologarMapa = computed(() => getSubprocesso()?.permissoes?.podeHomologarMapa ?? false);

    const podeVisualizarImpacto = computed(() => getSubprocesso()?.permissoes?.podeVisualizarImpacto ?? false);

    const podeAlterarDataLimite = computed(() => getSubprocesso()?.permissoes?.podeAlterarDataLimite ?? false);
    const podeReabrirCadastro = computed(() => getSubprocesso()?.permissoes?.podeReabrirCadastro ?? false);
    const podeReabrirRevisao = computed(() => getSubprocesso()?.permissoes?.podeReabrirRevisao ?? false);
    const podeEnviarLembrete = computed(() => getSubprocesso()?.permissoes?.podeEnviarLembrete ?? false);

    return {
        podeEditarCadastro,
        podeDisponibilizarCadastro,
        podeDevolverCadastro,
        podeAceitarCadastro,
        podeHomologarCadastro,
        podeEditarMapa,
        podeDisponibilizarMapa,
        podeValidarMapa,
        podeApresentarSugestoes,
        podeDevolverMapa,
        podeAceitarMapa,
        podeHomologarMapa,
        podeVisualizarImpacto,
        podeAlterarDataLimite,
        podeReabrirCadastro,
        podeReabrirRevisao,
        podeEnviarLembrete
    };
}
