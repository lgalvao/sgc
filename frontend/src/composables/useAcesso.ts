import {computed, type Ref, unref} from 'vue';
import type {SubprocessoDetalhe} from '@/types/tipos';

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

    const podeAnalisarCadastro = computed(() => podeDevolverCadastro.value || podeAceitarCadastro.value || podeHomologarCadastro.value);

    const podeEditarMapa = computed(() => getSubprocesso()?.permissoes?.podeEditarMapa ?? false);
    const podeDisponibilizarMapa = computed(() => getSubprocesso()?.permissoes?.podeDisponibilizarMapa ?? false);
    const podeValidarMapa = computed(() => getSubprocesso()?.permissoes?.podeValidarMapa ?? false);
    const podeApresentarSugestoes = computed(() => getSubprocesso()?.permissoes?.podeApresentarSugestoes ?? false);
    const podeVerSugestoes = computed(() => getSubprocesso()?.permissoes?.podeVerSugestoes ?? false);
    const podeDevolverMapa = computed(() => getSubprocesso()?.permissoes?.podeDevolverMapa ?? false);
    const podeAceitarMapa = computed(() => getSubprocesso()?.permissoes?.podeAceitarMapa ?? false);
    const podeHomologarMapa = computed(() => getSubprocesso()?.permissoes?.podeHomologarMapa ?? false);

    const podeAnalisarMapa = computed(() => podeDevolverMapa.value || podeAceitarMapa.value || podeHomologarMapa.value);

    const podeVisualizarImpacto = computed(() => getSubprocesso()?.permissoes?.podeVisualizarImpacto ?? false);

    const podeAlterarDataLimite = computed(() => getSubprocesso()?.permissoes?.podeAlterarDataLimite ?? false);
    const podeReabrirCadastro = computed(() => getSubprocesso()?.permissoes?.podeReabrirCadastro ?? false);
    const podeReabrirRevisao = computed(() => getSubprocesso()?.permissoes?.podeReabrirRevisao ?? false);
    const podeEnviarLembrete = computed(() => getSubprocesso()?.permissoes?.podeEnviarLembrete ?? false);

    const mesmaUnidade = computed(() => getSubprocesso()?.permissoes?.mesmaUnidade ?? false);
    const habilitarAcessoCadastro = computed(() => getSubprocesso()?.permissoes?.habilitarAcessoCadastro ?? false);
    const habilitarAcessoMapa = computed(() => getSubprocesso()?.permissoes?.habilitarAcessoMapa ?? false);

    // Helpers to combine permission with location (Visible but Disabled rule)
    const habilitarEditarCadastro = computed(() => podeEditarCadastro.value && mesmaUnidade.value);
    const habilitarDisponibilizarCadastro = computed(() => podeDisponibilizarCadastro.value && mesmaUnidade.value);
    const habilitarDevolverCadastro = computed(() => podeDevolverCadastro.value && mesmaUnidade.value);
    const habilitarAceitarCadastro = computed(() => podeAceitarCadastro.value && mesmaUnidade.value);
    const habilitarHomologarCadastro = computed(() => podeHomologarCadastro.value && mesmaUnidade.value);

    const habilitarEditarMapa = computed(() => podeEditarMapa.value && mesmaUnidade.value);
    const habilitarDisponibilizarMapa = computed(() => podeDisponibilizarMapa.value && mesmaUnidade.value);
    const habilitarValidarMapa = computed(() => podeValidarMapa.value && mesmaUnidade.value);
    const habilitarApresentarSugestoes = computed(() => podeApresentarSugestoes.value && mesmaUnidade.value);
    const habilitarDevolverMapa = computed(() => podeDevolverMapa.value && mesmaUnidade.value);
    const habilitarAceitarMapa = computed(() => podeAceitarMapa.value && mesmaUnidade.value);
    const habilitarHomologarMapa = computed(() => podeHomologarMapa.value && mesmaUnidade.value);

    return {
        mesmaUnidade,
        podeEditarCadastro,
        podeDisponibilizarCadastro,
        podeDevolverCadastro,
        podeAceitarCadastro,
        podeHomologarCadastro,
        podeAnalisarCadastro,
        podeEditarMapa,
        podeDisponibilizarMapa,
        podeValidarMapa,
        podeApresentarSugestoes,
        podeVerSugestoes,
        podeDevolverMapa,
        podeAceitarMapa,
        podeHomologarMapa,
        podeAnalisarMapa,
        podeVisualizarImpacto,
        podeAlterarDataLimite,
        podeReabrirCadastro,
        podeReabrirRevisao,
        podeEnviarLembrete,
        habilitarAcessoCadastro,
        habilitarAcessoMapa,
        // Flags de habilitação (Location check)
        habilitarEditarCadastro,
        habilitarDisponibilizarCadastro,
        habilitarDevolverCadastro,
        habilitarAceitarCadastro,
        habilitarHomologarCadastro,
        habilitarEditarMapa,
        habilitarDisponibilizarMapa,
        habilitarValidarMapa,
        habilitarApresentarSugestoes,
        habilitarDevolverMapa,
        habilitarAceitarMapa,
        habilitarHomologarMapa
    };
}
