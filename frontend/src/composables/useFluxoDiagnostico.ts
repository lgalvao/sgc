import {useMutation} from '@pinia/colada';
import type {MaybeRefOrGetter} from 'vue';
import {computed, toValue} from 'vue';
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
import {useSubprocessoStore} from '@/stores/subprocesso';

/**
 * Composable de fluxo do diagnóstico.
 * Concentra as mutations de concluir, validar, devolver e homologar.
 * Após cada ação bem-sucedida, invalida o contexto e opcionalmente redireciona.
 */
export function useFluxoDiagnostico(codSubprocesso: MaybeRefOrGetter<number>) {
    const router = useRouter();
    const cacheDiagnostico = useCacheDiagnostico();
    const subprocessoStore = useSubprocessoStore();
    const obterCodSubprocesso = () => toValue(codSubprocesso);

    function invalidarTudo() {
        cacheDiagnostico.invalidarFluxoCompleto(obterCodSubprocesso());
        subprocessoStore.invalidar();
    }

    const mutacaoConcluir = useMutation({
        mutation: () => concluirDiagnostico(obterCodSubprocesso()),
        onSuccess: () => {
            invalidarTudo();
        },
    });

    const mutacaoValidacaoConcluir = useMutation({
        mutation: () => validarConclusaoDiagnostico(obterCodSubprocesso()),
    });

    const mutacaoValidar = useMutation({
        mutation: (observacoes?: string) =>
            validarDiagnostico(obterCodSubprocesso(), observacoes ? {texto: observacoes} : undefined),
        onSuccess: () => {
            invalidarTudo();
        },
    });

    const mutacaoValidacaoValidar = useMutation({
        mutation: () => validarAcaoValidarDiagnostico(obterCodSubprocesso()),
    });

    const mutacaoDevolver = useMutation({
        mutation: (justificativa: string) =>
            devolverDiagnostico(obterCodSubprocesso(), {justificativa}),
        onSuccess: () => {
            invalidarTudo();
        },
    });

    const mutacaoValidacaoDevolver = useMutation({
        mutation: () => validarAcaoDevolverDiagnostico(obterCodSubprocesso()),
    });

    const mutacaoHomologar = useMutation({
        mutation: (observacoes?: string) =>
            homologarDiagnostico(obterCodSubprocesso(), observacoes ? {texto: observacoes} : undefined),
        onSuccess: () => {
            invalidarTudo();
        },
    });

    const mutacaoValidacaoHomologar = useMutation({
        mutation: () => validarAcaoHomologarDiagnostico(obterCodSubprocesso()),
    });

    const mutacaoImpossibilitar = useMutation({
        mutation: ({servidorTitulo, justificativa}: { servidorTitulo: string; justificativa: string }) =>
            impossibilitarAvaliacao(obterCodSubprocesso(), servidorTitulo, {justificativa}),
        onSuccess: () => {
            cacheDiagnostico.invalidarEquipe(obterCodSubprocesso());
            cacheDiagnostico.invalidarUnidade(obterCodSubprocesso());
        },
    });

    const mutacaoPermitirAvaliacao = useMutation({
        mutation: (servidorTitulo: string) =>
            permitirAvaliacao(obterCodSubprocesso(), servidorTitulo),
        onSuccess: () => {
            cacheDiagnostico.invalidarEquipe(obterCodSubprocesso());
            cacheDiagnostico.invalidarUnidade(obterCodSubprocesso());
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
