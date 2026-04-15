import {defineStore} from "pinia";
import {ref} from "vue";
import {type RelatorioAndamento, relatoriosService} from "@/services/relatoriosService";
import {useErrorHandler} from "@/composables/useErrorHandler";

export const useRelatoriosStore = defineStore("relatorios", () => {
  const {withErrorHandling, lastError, clearError} = useErrorHandler();
  const relatorioAndamento = ref<RelatorioAndamento[]>([]);

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

  async function exportarMapasPdf(codProcesso: number, unidadeId?: number) {
    return withErrorHandling(async () => {
      await relatoriosService.downloadRelatorioMapasPdf(codProcesso, unidadeId);
    });
  }

  function limparRelatorio() {
    relatorioAndamento.value = [];
    clearError();
  }

  return {
    relatorioAndamento,
    lastError,
    buscarRelatorioAndamento,
    exportarAndamentoPdf,
    exportarMapasPdf,
    limparRelatorio,
    clearError
  };
});
