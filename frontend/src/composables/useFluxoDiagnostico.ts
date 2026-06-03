import {useMutation, useQueryCache} from '@pinia/colada';
import {computed} from 'vue';
import {useRouter} from 'vue-router';
import {
    concluirDiagnostico,
    devolverDiagnostico,
    homologarDiagnostico,
    validarDiagnostico,
} from '@/services/diagnosticoService';
import {CHAVE_DIAGNOSTICO} from '@/composables/useDiagnosticoContexto';

/**
 * Composable de fluxo do diagnóstico.
 * Concentra as mutations de concluir, validar, devolver e homologar.
 * Após cada ação bem-sucedida, invalida o contexto e opcionalmente redireciona.
 */
export function useFluxoDiagnostico(codSubprocesso: number) {
    const cache = useQueryCache();
    const router = useRouter();

    function _invalidarTudo() {
        void cache.invalidateQueries({key: [CHAVE_DIAGNOSTICO, 'contexto', codSubprocesso]});
        void cache.invalidateQueries({key: [CHAVE_DIAGNOSTICO, 'equipe', codSubprocesso]});
        void cache.invalidateQueries({key: [CHAVE_DIAGNOSTICO, 'unidade', codSubprocesso]});
    }

    const mutacaoConcluir = useMutation({
        mutation: () => concluirDiagnostico(codSubprocesso),
        onSuccess: () => {
            _invalidarTudo();
        },
    });

    const mutacaoValidar = useMutation({
        mutation: (observacoes?: string) =>
            validarDiagnostico(codSubprocesso, observacoes ? {texto: observacoes} : undefined),
        onSuccess: () => {
            _invalidarTudo();
        },
    });

    const mutacaoDevolver = useMutation({
        mutation: (justificativa: string) =>
            devolverDiagnostico(codSubprocesso, {justificativa}),
        onSuccess: () => {
            _invalidarTudo();
        },
    });

    const mutacaoHomologar = useMutation({
        mutation: (observacoes?: string) =>
            homologarDiagnostico(codSubprocesso, observacoes ? {texto: observacoes} : undefined),
        onSuccess: () => {
            _invalidarTudo();
        },
    });

    /** Volta para a tela do subprocesso após ação de fluxo concluída. */
    function voltarParaSubprocesso(codProcesso: number, siglaUnidade: string) {
        void router.push({
            name: 'Subprocesso',
            params: {codProcesso, siglaUnidade},
        });
    }

    return {
        concluindo: computed(() => mutacaoConcluir.isLoading.value),
        validando: computed(() => mutacaoValidar.isLoading.value),
        devolvendo: computed(() => mutacaoDevolver.isLoading.value),
        homologando: computed(() => mutacaoHomologar.isLoading.value),
        erroConcluir: computed(() => mutacaoConcluir.error.value),
        erroValidar: computed(() => mutacaoValidar.error.value),
        erroDevolver: computed(() => mutacaoDevolver.error.value),
        erroHomologar: computed(() => mutacaoHomologar.error.value),
        concluirDiagnostico: () => mutacaoConcluir.mutateAsync(),
        validarDiagnostico: (observacoes?: string) => mutacaoValidar.mutateAsync(observacoes),
        devolverDiagnostico: (justificativa: string) => mutacaoDevolver.mutateAsync(justificativa),
        homologarDiagnostico: (observacoes?: string) => mutacaoHomologar.mutateAsync(observacoes),
        voltarParaSubprocesso,
    };
}
