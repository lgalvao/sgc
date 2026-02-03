import { computed, ref, onMounted } from "vue";
import { useProcessosStore } from "@/stores/processos";
import { useMapasStore } from "@/stores/mapas";
import { isWithinInterval, parseISO, startOfDay, endOfDay } from "date-fns";
import { TipoProcesso } from "@/types/tipos";

export function useRelatorios() {
  const processosStore = useProcessosStore();
  const mapasStore = useMapasStore();

  const filtroTipo = ref("");
  const filtroDataInicio = ref("");
  const filtroDataFim = ref("");

  const mostrarModalMapasVigentes = ref(false);
  const mostrarModalDiagnosticosGaps = ref(false);
  const mostrarModalAndamentoGeral = ref(false);

  // TODO: Implement proper diagnostic fetching via store.
  // Temporary mock data for UI development and testing.
  const diagnosticosGaps = ref([
    { id: 1, processo: "Processo A", unidade: "Unidade 1", gaps: 5, importanciaMedia: 4.5, dominioMedio: 2.1, competenciasCriticas: ["Java", "SQL"], data: new Date("2024-08-15"), status: "Finalizado" },
    { id: 2, processo: "Processo B", unidade: "Unidade 2", gaps: 3, importanciaMedia: 4.0, dominioMedio: 3.5, competenciasCriticas: ["Vue"], data: new Date("2024-08-20"), status: "Em análise" },
    { id: 3, processo: "Processo C", unidade: "Unidade 3", gaps: 8, importanciaMedia: 4.8, dominioMedio: 1.5, competenciasCriticas: ["Spring"], data: new Date("2024-09-05"), status: "Pendente" },
    { id: 4, processo: "Processo D", unidade: "Unidade 4", gaps: 0, importanciaMedia: 3.0, dominioMedio: 4.5, competenciasCriticas: [], data: new Date("2024-09-10"), status: "Finalizado" },
  ]);

  const processosFiltrados = computed(() => {
    let list = processosStore.processosPainel || [];

    if (filtroTipo.value) {
      list = list.filter(p => p.tipo === filtroTipo.value);
    }

    if (filtroDataInicio.value || filtroDataFim.value) {
      list = list.filter(p => {
        const date = parseISO(p.dataCriacao);
        const start = filtroDataInicio.value ? startOfDay(parseISO(filtroDataInicio.value)) : new Date(0);
        const end = filtroDataFim.value ? endOfDay(parseISO(filtroDataFim.value)) : new Date(8640000000000000);
        return isWithinInterval(date, { start, end });
      });
    }

    return list;
  });

  const mapasVigentes = computed(() => {
    const mapa = mapasStore.mapaCompleto;
    if (mapa && mapa.unidade) {
        return [{
            id: mapa.codigo || 1,
            unidade: mapa.unidade.sigla,
            competencias: (mapa as any).competencias || []
        }];
    }
    return [];
  });

  const diagnosticosGapsFiltrados = computed(() => {
    let list = diagnosticosGaps.value;

    if (filtroTipo.value) {
        if (filtroTipo.value !== TipoProcesso.DIAGNOSTICO) {
            return [];
        }
    }

    if (filtroDataInicio.value || filtroDataFim.value) {
        list = list.filter(d => {
            const date = d.data;
            const start = filtroDataInicio.value ? startOfDay(parseISO(filtroDataInicio.value)) : new Date(0);
            const end = filtroDataFim.value ? endOfDay(parseISO(filtroDataFim.value)) : new Date(8640000000000000);
            return isWithinInterval(date, { start, end });
        });
    }

    return list;
  });

  const abrirModalMapasVigentes = () => { mostrarModalMapasVigentes.value = true; };
  const abrirModalDiagnosticosGaps = () => { mostrarModalDiagnosticosGaps.value = true; };
  const abrirModalAndamentoGeral = () => { mostrarModalAndamentoGeral.value = true; };

  onMounted(async () => {
    if (!processosStore.processosPainel || processosStore.processosPainel.length === 0) {
        await processosStore.buscarProcessosPainel();
    }
  });

  return {
    filtroTipo,
    filtroDataInicio,
    filtroDataFim,
    mostrarModalMapasVigentes,
    mostrarModalDiagnosticosGaps,
    mostrarModalAndamentoGeral,
    processosFiltrados,
    mapasVigentes,
    diagnosticosGaps,
    diagnosticosGapsFiltrados,
    abrirModalMapasVigentes,
    abrirModalDiagnosticosGaps,
    abrirModalAndamentoGeral
  };
}
