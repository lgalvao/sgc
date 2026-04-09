import {computed, type Ref, unref} from 'vue';
import type {SubprocessoDetalhe} from '@/types/tipos';

/**
 * Hook to access permissions calculated by the backend.
 *
 * The backend is the single source of truth for security and workflow rules.
 * This hook simplifies access to those rules in Vue components.
 */
export function useAcesso(subprocessoRef: Ref<SubprocessoDetalhe | null> | SubprocessoDetalhe) {
    const getSubprocesso = () => unref(subprocessoRef);
    const getPermissoes = () => getSubprocesso()?.permissoes;

    const podeEditarCadastro = computed(() => getPermissoes()?.podeEditarCadastro ?? false);
    const podeDisponibilizarCadastro = computed(() => getPermissoes()?.podeDisponibilizarCadastro ?? false);
    const podeDevolverCadastro = computed(() => getPermissoes()?.podeDevolverCadastro ?? false);
    const podeAceitarCadastro = computed(() => getPermissoes()?.podeAceitarCadastro ?? false);
    const podeHomologarCadastro = computed(() => getPermissoes()?.podeHomologarCadastro ?? false);

    const podeAnalisarCadastro = computed(() => podeDevolverCadastro.value || podeAceitarCadastro.value || podeHomologarCadastro.value);

    const podeEditarMapa = computed(() => getPermissoes()?.podeEditarMapa ?? false);
    const podeDisponibilizarMapa = computed(() => getPermissoes()?.podeDisponibilizarMapa ?? false);
    const podeValidarMapa = computed(() => getPermissoes()?.podeValidarMapa ?? false);
    const podeApresentarSugestoes = computed(() => getPermissoes()?.podeApresentarSugestoes ?? false);
    const podeVerSugestoes = computed(() => getPermissoes()?.podeVerSugestoes ?? false);
    const podeDevolverMapa = computed(() => getPermissoes()?.podeDevolverMapa ?? false);
    const podeAceitarMapa = computed(() => getPermissoes()?.podeAceitarMapa ?? false);
    const podeHomologarMapa = computed(() => getPermissoes()?.podeHomologarMapa ?? false);

    const podeAnalisarMapa = computed(() => podeDevolverMapa.value || podeAceitarMapa.value || podeHomologarMapa.value);

    const podeVisualizarImpacto = computed(() => getPermissoes()?.podeVisualizarImpacto ?? false);

    const podeAlterarDataLimite = computed(() => getPermissoes()?.podeAlterarDataLimite ?? false);
    const podeReabrirCadastro = computed(() => getPermissoes()?.podeReabrirCadastro ?? false);
    const podeReabrirRevisao = computed(() => getPermissoes()?.podeReabrirRevisao ?? false);
    const podeEnviarLembrete = computed(() => getPermissoes()?.podeEnviarLembrete ?? false);

    const mesmaUnidade = computed(() => getPermissoes()?.mesmaUnidade ?? false);
    const habilitarAcessoCadastro = computed(() => getPermissoes()?.habilitarAcessoCadastro ?? false);
    const habilitarAcessoMapa = computed(() => getPermissoes()?.habilitarAcessoMapa ?? false);

    const habilitarEditarCadastro = computed(() => getPermissoes()?.habilitarEditarCadastro ?? false);
    const habilitarDisponibilizarCadastro = computed(() => getPermissoes()?.habilitarDisponibilizarCadastro ?? false);
    const habilitarDevolverCadastro = computed(() => getPermissoes()?.habilitarDevolverCadastro ?? false);
    const habilitarAceitarCadastro = computed(() => getPermissoes()?.habilitarAceitarCadastro ?? false);
    const habilitarHomologarCadastro = computed(() => getPermissoes()?.habilitarHomologarCadastro ?? false);

    const habilitarEditarMapa = computed(() => getPermissoes()?.habilitarEditarMapa ?? false);
    const habilitarDisponibilizarMapa = computed(() => getPermissoes()?.habilitarDisponibilizarMapa ?? false);
    const habilitarValidarMapa = computed(() => getPermissoes()?.habilitarValidarMapa ?? false);
    const habilitarApresentarSugestoes = computed(() => getPermissoes()?.habilitarApresentarSugestoes ?? false);
    const habilitarDevolverMapa = computed(() => getPermissoes()?.habilitarDevolverMapa ?? false);
    const habilitarAceitarMapa = computed(() => getPermissoes()?.habilitarAceitarMapa ?? false);
    const habilitarHomologarMapa = computed(() => getPermissoes()?.habilitarHomologarMapa ?? false);

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
