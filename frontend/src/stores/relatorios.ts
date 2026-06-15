import {defineStore} from "pinia";
import {ref} from "vue";
import {
    type RelatorioAndamento,
    type RelatorioDiagnosticoGap,
    type RelatorioDiagnosticoSituacaoCapacitacao,
    type RelatorioMapa,
    relatoriosService
} from "@/services/relatoriosService";

export const useRelatoriosStore = defineStore("relatorios", () => {
    const relatorioAndamento = ref<RelatorioAndamento[]>([]);
    const relatorioMapas = ref<RelatorioMapa[]>([]);
    const relatorioGapsDiagnostico = ref<RelatorioDiagnosticoGap[]>([]);
    const relatorioSituacaoCapacitacaoDiagnostico = ref<RelatorioDiagnosticoSituacaoCapacitacao[]>([]);

    async function buscarRelatorioAndamento(codProcesso: number) {
        relatorioAndamento.value = await relatoriosService.obterRelatorioAndamento(codProcesso);
    }

    async function exportarAndamentoPdf(codProcesso: number) {
        await relatoriosService.downloadRelatorioAndamentoPdf(codProcesso);
    }

    async function buscarRelatorioMapas(codigosUnidades: number[]) {
        relatorioMapas.value = await relatoriosService.obterRelatorioMapas(codigosUnidades);
    }

    async function exportarMapasPdf(codigosUnidades: number[]) {
        await relatoriosService.downloadRelatorioMapasPdf(codigosUnidades);
    }

    async function buscarRelatorioGapsDiagnostico(codProcesso: number, codigosUnidades: number[]) {
        relatorioGapsDiagnostico.value = await relatoriosService.obterRelatorioGapsDiagnostico(codProcesso, codigosUnidades);
    }

    async function exportarRelatorioGapsDiagnosticoPdf(codProcesso: number, codigosUnidades: number[]) {
        await relatoriosService.downloadRelatorioGapsDiagnosticoPdf(codProcesso, codigosUnidades);
    }

    async function buscarRelatorioSituacaoCapacitacaoDiagnostico(codProcesso: number, codigosUnidades: number[]) {
        relatorioSituacaoCapacitacaoDiagnostico.value = await relatoriosService.obterRelatorioSituacaoCapacitacaoDiagnostico(codProcesso, codigosUnidades);
    }

    async function exportarRelatorioSituacaoCapacitacaoDiagnosticoPdf(codProcesso: number, codigosUnidades: number[]) {
        await relatoriosService.downloadRelatorioSituacaoCapacitacaoDiagnosticoPdf(codProcesso, codigosUnidades);
    }

    function limparRelatorio() {
        relatorioAndamento.value = [];
        relatorioMapas.value = [];
        relatorioGapsDiagnostico.value = [];
        relatorioSituacaoCapacitacaoDiagnostico.value = [];
    }

    return {
        relatorioAndamento,
        relatorioMapas,
        relatorioGapsDiagnostico,
        relatorioSituacaoCapacitacaoDiagnostico,
        buscarRelatorioAndamento,
        buscarRelatorioMapas,
        buscarRelatorioGapsDiagnostico,
        buscarRelatorioSituacaoCapacitacaoDiagnostico,
        exportarAndamentoPdf,
        exportarMapasPdf,
        exportarRelatorioGapsDiagnosticoPdf,
        exportarRelatorioSituacaoCapacitacaoDiagnosticoPdf,
        limparRelatorio,
    };
});
