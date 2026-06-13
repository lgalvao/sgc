import {useMutation} from '@pinia/colada';
import {computed} from 'vue';
import {useRouter} from 'vue-router';
import {
    concluirDiagnostico,
    devolverDiagnostico,
    homologarDiagnostico,
    impossibilitarAvaliacao,
    permitirAvaliacao,
    validarAcaoDevolverDiagnostico,
    validarAcaoHomologarDiagnostico,
    validarAcaoValidarDiagnostico,
    validarConclusaoDiagnostico,
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

    const mutacaoValidacaoConcluir = useMutation({
        mutation: () => validarConclusaoDiagnostico(codSubprocesso),
    });

    const mutacaoValidar = useMutation({
        mutation: (observacoes?: string) =>
            validarDiagnostico(codSubprocesso, observacoes ? {texto: observacoes} : undefined),
        onSuccess: () => {
            invalidarTudo();
        },
    });

    const mutacaoValidacaoValidar = useMutation({
        mutation: () => validarAcaoValidarDiagnostico(codSubprocesso),
    });

    const mutacaoDevolver = useMutation({
        mutation: (justificativa: string) =>
            devolverDiagnostico(codSubprocesso, {justificativa}),
        onSuccess: () => {
            invalidarTudo();
        },
    });

    const mutacaoValidacaoDevolver = useMutation({
        mutation: () => validarAcaoDevolverDiagnostico(codSubprocesso),
    });

    const mutacaoHomologar = useMutation({
        mutation: (observacoes?: string) =>
            homologarDiagnostico(codSubprocesso, observacoes ? {texto: observacoes} : undefined),
        onSuccess: () => {
            invalidarTudo();
        },
    });

    const mutacaoValidacaoHomologar = useMutation({
        mutation: () => validarAcaoHomologarDiagnostico(codSubprocesso),
    });

    const mutacaoImpossibilitar = useMutation({
        mutation: ({servidorTitulo, justificativa}: { servidorTitulo: string; justificativa: string }) =>
            impossibilitarAvaliacao(codSubprocesso, servidorTitulo, {justificativa}),
        onSuccess: () => {
            cacheDiagnostico.invalidarEquipe(codSubprocesso);
            cacheDiagnostico.invalidarUnidade(codSubprocesso);
        },
    });

    const mutacaoPermitirAvaliacao = useMutation({
        mutation: (servidorTitulo: string) =>
            permitirAvaliacao(codSubprocesso, servidorTitulo),
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
        permitindo: computed(() => mutacaoPermitirAvaliacao.isLoading.value),
        erroConcluir: computed(() => mutacaoConcluir.error.value),
        erroValidacaoConcluir: computed(() => mutacaoValidacaoConcluir.error.value),
        erroValidar: computed(() => mutacaoValidar.error.value),
        erroValidacaoValidar: computed(() => mutacaoValidacaoValidar.error.value),
        erroDevolver: computed(() => mutacaoDevolver.error.value),
        erroValidacaoDevolver: computed(() => mutacaoValidacaoDevolver.error.value),
        erroHomologar: computed(() => mutacaoHomologar.error.value),
        erroValidacaoHomologar: computed(() => mutacaoValidacaoHomologar.error.value),
        erroImpossibilitar: computed(() => mutacaoImpossibilitar.error.value),
        erroPermitir: computed(() => mutacaoPermitirAvaliacao.error.value),
        validarConclusaoDiagnostico: () => mutacaoValidacaoConcluir.mutateAsync(),
        concluirDiagnostico: () => mutacaoConcluir.mutateAsync(),
        validarAcaoValidarDiagnostico: () => mutacaoValidacaoValidar.mutateAsync(),
        validarDiagnostico: (observacoes?: string) => mutacaoValidar.mutateAsync(observacoes),
        validarAcaoDevolverDiagnostico: () => mutacaoValidacaoDevolver.mutateAsync(),
        devolverDiagnostico: (justificativa: string) => mutacaoDevolver.mutateAsync(justificativa),
        validarAcaoHomologarDiagnostico: () => mutacaoValidacaoHomologar.mutateAsync(),
        homologarDiagnostico: (observacoes?: string) => mutacaoHomologar.mutateAsync(observacoes),
        impossibilitarAvaliacao: (servidorTitulo: string, justificativa: string) =>
            mutacaoImpossibilitar.mutateAsync({servidorTitulo, justificativa}),
        permitirAvaliacao: (servidorTitulo: string) =>
            mutacaoPermitirAvaliacao.mutateAsync(servidorTitulo),
        voltarParaSubprocesso,
    };
}
