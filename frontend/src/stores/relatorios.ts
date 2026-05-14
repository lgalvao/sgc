import {defineStore} from "pinia";
import {ref} from "vue";
import {type RelatorioAndamento, type RelatorioMapa, relatoriosService} from "@/services/relatoriosService";

export const useRelatoriosStore = defineStore("relatorios", () => {
    const relatorioAndamento = ref<RelatorioAndamento[]>([]);
    const relatorioMapas = ref<RelatorioMapa[]>([]);

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

    function limparRelatorio() {
        relatorioAndamento.value = [];
        relatorioMapas.value = [];
    }

    return {
        relatorioAndamento,
        relatorioMapas,
        buscarRelatorioAndamento,
        buscarRelatorioMapas,
        exportarAndamentoPdf,
        exportarMapasPdf,
        limparRelatorio,
    };
});
