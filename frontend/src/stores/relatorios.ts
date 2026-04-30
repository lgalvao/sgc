import {defineStore} from "pinia";
import {ref} from "vue";
import {type RelatorioAndamento, type RelatorioMapa, relatoriosService} from "@/services/relatoriosService";
import {useErrorHandler} from "@/composables/useErrorHandler";

export const useRelatoriosStore = defineStore("relatorios", () => {
  const {withErrorHandling, lastError, clearError} = useErrorHandler();
  const relatorioAndamento = ref<RelatorioAndamento[]>([]);
  const relatorioMapas = ref<RelatorioMapa[]>([]);

  async function buscarRelatorioAndamento(codProcesso: number) {
    return withErrorHandling(async () => {
      relatorioAndamento.value = await relatoriosService.obterRelatorioAndamento(codProcesso);
    });
  }

  async function exportarAndamentoPdf(codProcesso: number) {
    return withErrorHandling(async () => {
      await relatoriosService.downloadRelatorioAndamentoPdf(codProcesso);
    });
  }

  async function buscarRelatorioMapas(codProcesso: number, unidadeId?: number) {
    return withErrorHandling(async () => {
      relatorioMapas.value = await relatoriosService.obterRelatorioMapas(codProcesso, unidadeId);
    });
  }

  async function exportarMapasPdf(codProcesso: number, unidadeId?: number) {
    return withErrorHandling(async () => {
      await relatoriosService.downloadRelatorioMapasPdf(codProcesso, unidadeId);
    });
  }

  function limparRelatorio() {
    relatorioAndamento.value = [];
    relatorioMapas.value = [];
    clearError();
  }

  return {
    relatorioAndamento,
    relatorioMapas,
    lastError,
    buscarRelatorioAndamento,
    buscarRelatorioMapas,
    exportarAndamentoPdf,
    exportarMapasPdf,
    limparRelatorio,
    clearError
  };
});
