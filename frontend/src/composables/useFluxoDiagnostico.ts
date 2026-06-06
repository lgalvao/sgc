import {useMutation} from '@pinia/colada';
import {computed} from 'vue';
import {useRouter} from 'vue-router';
import {
    concluirDiagnostico,
    devolverDiagnostico,
    homologarDiagnostico,
    impossibilitarAvaliacao,
    validarDiagnostico,
} from '@/services/diagnosticoService';
import {useCacheDiagnostico} from '@/composables/useDiagnosticoCache';

/**
 * Composable de fluxo do diagnóstico.
 * Concentra as mutations de concluir, validar, devolver e homologar.
 * Após cada ação bem-sucedida, invalida o contexto e opcionalmente redireciona.
 */
export function useFluxoDiagnostico(codSubprocesso: number) {
    const router = useRouter();
    const cacheDiagnostico = useCacheDiagnostico();

    function invalidarTudo() {
        cacheDiagnostico.invalidarFluxoCompleto(codSubprocesso);
    }

    const mutacaoConcluir = useMutation({
        mutation: () => concluirDiagnostico(codSubprocesso),
        onSuccess: () => {
            invalidarTudo();
        },
    });

    const mutacaoValidar = useMutation({
        mutation: (observacoes?: string) =>
            validarDiagnostico(codSubprocesso, observacoes ? {texto: observacoes} : undefined),
        onSuccess: () => {
            invalidarTudo();
        },
    });

    const mutacaoDevolver = useMutation({
        mutation: (justificativa: string) =>
            devolverDiagnostico(codSubprocesso, {justificativa}),
        onSuccess: () => {
            invalidarTudo();
        },
    });

    const mutacaoHomologar = useMutation({
        mutation: (observacoes?: string) =>
            homologarDiagnostico(codSubprocesso, observacoes ? {texto: observacoes} : undefined),
        onSuccess: () => {
            invalidarTudo();
        },
    });

    const mutacaoImpossibilitar = useMutation({
        mutation: ({servidorTitulo, justificativa}: { servidorTitulo: string; justificativa: string }) =>
            impossibilitarAvaliacao(codSubprocesso, servidorTitulo, {justificativa}),
        onSuccess: () => {
            cacheDiagnostico.invalidarEquipe(codSubprocesso);
            cacheDiagnostico.invalidarUnidade(codSubprocesso);
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
        impossibilitando: computed(() => mutacaoImpossibilitar.isLoading.value),
        erroConcluir: computed(() => mutacaoConcluir.error.value),
        erroValidar: computed(() => mutacaoValidar.error.value),
        erroDevolver: computed(() => mutacaoDevolver.error.value),
        erroHomologar: computed(() => mutacaoHomologar.error.value),
        erroImpossibilitar: computed(() => mutacaoImpossibilitar.error.value),
        concluirDiagnostico: () => mutacaoConcluir.mutateAsync(),
        validarDiagnostico: (observacoes?: string) => mutacaoValidar.mutateAsync(observacoes),
        devolverDiagnostico: (justificativa: string) => mutacaoDevolver.mutateAsync(justificativa),
        homologarDiagnostico: (observacoes?: string) => mutacaoHomologar.mutateAsync(observacoes),
        impossibilitarAvaliacao: (servidorTitulo: string, justificativa: string) =>
            mutacaoImpossibilitar.mutateAsync({servidorTitulo, justificativa}),
        voltarParaSubprocesso,
    };
}
