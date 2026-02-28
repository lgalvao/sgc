import {defineStore} from "pinia";
import {ref} from "vue";
import {
    type AvaliacaoServidorDto,
    type DiagnosticoDto,
    diagnosticoService,
    type OcupacaoCriticaDto
} from "@/services/diagnosticoService";
import {useErrorHandler} from "@/composables/useErrorHandler";
import {useSingleLoading} from "@/composables/useLoadingManager";

export const useDiagnosticosStore = defineStore("diagnosticos", () => {
    const diagnostico = ref<DiagnosticoDto | null>(null);
    const avaliacoes = ref<AvaliacaoServidorDto[]>([]);
    const ocupacoes = ref<OcupacaoCriticaDto[]>([]);
    const loading = useSingleLoading();
    const {lastError, clearError, withErrorHandling} = useErrorHandler();

    async function buscarDiagnostico(subprocessoId: number) {
        await loading.withLoading(async () => {
            await withErrorHandling(async () => {
                diagnostico.value = await diagnosticoService.buscarDiagnostico(subprocessoId);
            }, () => {
                diagnostico.value = null;
            });
        });
    }

    async function buscarMinhasAvaliacoes(subprocessoId: number, servidorTitulo?: string) {
        await withErrorHandling(async () => {
            avaliacoes.value = await diagnosticoService.buscarMinhasAvaliacoes(subprocessoId, servidorTitulo);
            return avaliacoes.value;
        });
    }

    async function salvarAvaliacao(
        subprocessoId: number,
        competenciaCodigo: number,
        importancia: string,
        dominio: string,
        observacoes?: string
    ) {
        return withErrorHandling(async () => {
            const avaliacao = await diagnosticoService.salvarAvaliacao(
                subprocessoId,
                competenciaCodigo,
                importancia,
                dominio,
                observacoes
            );

            const index = avaliacoes.value.findIndex(
                a => a.competenciaCodigo === competenciaCodigo
            );
            if (index >= 0) {
                avaliacoes.value[index] = avaliacao;
            } else {
                avaliacoes.value.push(avaliacao);
            }
            return avaliacao;
        });
    }

    async function concluirAutoavaliacao(subprocessoId: number, justificativaAtraso?: string) {
        return withErrorHandling(async () => {
            await diagnosticoService.concluirAutoavaliacao(subprocessoId, justificativaAtraso);
        });
    }

    async function concluirDiagnostico(subprocessoId: number, justificativa?: string) {
        return withErrorHandling(async () => {
            const resultado = await diagnosticoService.concluirDiagnostico(subprocessoId, justificativa);
            diagnostico.value = resultado;
            return resultado;
        });
    }

    async function buscarOcupacoes(subprocessoId: number) {
        await withErrorHandling(async () => {
            ocupacoes.value = await diagnosticoService.buscarOcupacoes(subprocessoId);
        });
    }

    async function salvarOcupacao(
        subprocessoId: number,
        servidorTitulo: string,
        competenciaCodigo: number,
        situacao: string
    ) {
        return withErrorHandling(async () => {
            const ocupacao = await diagnosticoService.salvarOcupacao(
                subprocessoId,
                servidorTitulo,
                competenciaCodigo,
                situacao
            );

            const index = ocupacoes.value.findIndex(
                o => o.competenciaCodigo === competenciaCodigo
            );
            if (index >= 0) {
                ocupacoes.value[index] = ocupacao;
            } else {
                ocupacoes.value.push(ocupacao);
            }
            return ocupacao;
        });
    }

    return {
        diagnostico,
        avaliacoes,
        ocupacoes,
        isLoading: loading.isLoading,
        lastError,
        clearError,
        buscarDiagnostico,
        buscarMinhasAvaliacoes,
        salvarAvaliacao,
        concluirAutoavaliacao,
        concluirDiagnostico,
        buscarOcupacoes,
        salvarOcupacao,
    };
});
