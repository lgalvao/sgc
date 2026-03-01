<template>
  <LayoutPadrao>
    <PageHeader title="Relatórios"/>

    <BRow class="mb-4">
      <BCol md="4">
        <BFormGroup label="Filtrar por Tipo" label-for="filtro-tipo">
          <BFormSelect
              id="filtro-tipo"
              v-model="filtroTipo"
              :options="opcoesTipo"
              aria-label="Filtrar por Tipo"
              data-testid="filtro-tipo"
          />
        </BFormGroup>
      </BCol>
      <BCol md="4">
        <BFormGroup label="Data Início" label-for="filtro-data-inicio">
          <BFormInput
              id="filtro-data-inicio"
              v-model="filtroDataInicio"
              aria-label="Data Início"
              data-testid="filtro-data-inicio"
              type="date"
          />
        </BFormGroup>
      </BCol>
      <BCol md="4">
        <BFormGroup label="Data Fim" label-for="filtro-data-fim">
          <BFormInput
              id="filtro-data-fim"
              v-model="filtroDataFim"
              aria-label="Data Fim"
              data-testid="filtro-data-fim"
              type="date"
          />
        </BFormGroup>
      </BCol>
    </BRow>

    <BRow>
      <BCol md="4">
        <BCard
            class="mb-3 text-center h-100 cursor-pointer shadow-sm hover-shadow"
            data-testid="card-relatorio-mapas"
            @click="abrirModalMapasVigentes"
        >
          <div class="display-4 mb-2 text-primary">
            <i class="bi bi-map"/>
          </div>
          <h3>Mapas Vigentes</h3>
          <div class="h2 mb-0">{{ mapasVigentes.length }}</div>
          <div class="text-muted">Unidades com mapas</div>
        </BCard>
      </BCol>

      <BCol md="4">
        <BCard
            class="mb-3 text-center h-100 cursor-pointer shadow-sm hover-shadow"
            data-testid="card-relatorio-gaps"
            @click="abrirModalDiagnosticosGaps"
        >
          <div class="display-4 mb-2 text-danger">
            <i class="bi bi-exclamation-triangle"/>
          </div>
          <h3>Diagnósticos de Gaps</h3>
          <div class="h2 mb-0">{{ diagnosticosGapsFiltrados.length }}</div>
          <div class="text-muted">Gaps identificados</div>
        </BCard>
      </BCol>

      <BCol md="4">
        <BCard
            class="mb-3 text-center h-100 cursor-pointer shadow-sm hover-shadow"
            data-testid="card-relatorio-andamento"
            @click="abrirModalAndamentoGeral"
        >
          <div class="display-4 mb-2 text-success">
            <i class="bi bi-graph-up"/>
          </div>
          <h3>Andamento Geral</h3>
          <div class="h2 mb-0">{{ processosFiltrados.length }}</div>
          <div class="text-muted">Processos em andamento</div>
        </BCard>
      </BCol>
    </BRow>

    <!-- Modal Mapas Vigentes -->
    <ModalMapasVigentes
        v-model="mostrarModalMapasVigentes"
        :mapas="mapasVigentes"
    />

    <!-- Modal Diagnósticos de Gaps -->
    <ModalDiagnosticosGaps
        v-model="mostrarModalDiagnosticosGaps"
        :diagnosticos="diagnosticosGapsFiltrados"
    />

    <!-- Modal Andamento Geral -->
    <ModalRelatorioAndamento
        v-model="mostrarModalAndamentoGeral"
        :processos="processosFiltrados"
    />
  </LayoutPadrao>
</template>

<script lang="ts" setup>
import {computed, onMounted, ref} from "vue";
import {BCard, BCol, BFormGroup, BFormInput, BFormSelect, BRow} from "bootstrap-vue-next";
import LayoutPadrao from "@/components/layout/LayoutPadrao.vue";
import PageHeader from "@/components/layout/PageHeader.vue";
import ModalMapasVigentes from "@/components/relatorios/ModalMapasVigentes.vue";
import ModalDiagnosticosGaps from "@/components/relatorios/ModalDiagnosticosGaps.vue";
import ModalRelatorioAndamento from "@/components/relatorios/ModalRelatorioAndamento.vue";
import {useProcessosStore} from "@/stores/processos";
import {useMapasStore} from "@/stores/mapas";
import {usePerfilStore} from "@/stores/perfil";
import {endOfDay, isWithinInterval, parseISO, startOfDay} from "date-fns";
import {TipoProcesso} from "@/types/tipos";

const opcoesTipo = [
  {value: "", text: "Todos os Tipos"},
  {value: TipoProcesso.MAPEAMENTO, text: "Mapeamento"},
  {value: TipoProcesso.REVISAO, text: "Revisão"},
  {value: TipoProcesso.DIAGNOSTICO, text: "Diagnóstico"},
];

const processosStore = useProcessosStore();
const mapasStore = useMapasStore();
const perfilStore = usePerfilStore();

const filtroTipo = ref("");
const filtroDataInicio = ref("");
const filtroDataFim = ref("");

const mostrarModalMapasVigentes = ref(false);
const mostrarModalDiagnosticosGaps = ref(false);
const mostrarModalAndamentoGeral = ref(false);

const diagnosticosGaps = ref([
  {
    codigo: 1,
    processo: "Processo A",
    unidade: "Unidade 1",
    gaps: 5,
    importanciaMedia: 4.5,
    dominioMedio: 2.1,
    competenciasCriticas: ["Java", "SQL"],
    data: new Date("2024-08-15"),
    status: "Finalizado"
  },
  {
    codigo: 2,
    processo: "Processo B",
    unidade: "Unidade 2",
    gaps: 3,
    importanciaMedia: 4,
    dominioMedio: 3.5,
    competenciasCriticas: ["Vue"],
    data: new Date("2024-08-20"),
    status: "Em análise"
  },
  {
    codigo: 3,
    processo: "Processo C",
    unidade: "Unidade 3",
    gaps: 8,
    importanciaMedia: 4.8,
    dominioMedio: 1.5,
    competenciasCriticas: ["Spring"],
    data: new Date("2024-09-05"),
    status: "Pendente"
  },
  {
    codigo: 4,
    processo: "Processo D",
    unidade: "Unidade 4",
    gaps: 0,
    importanciaMedia: 3,
    dominioMedio: 4.5,
    competenciasCriticas: [],
    data: new Date("2024-09-10"),
    status: "Finalizado"
  },
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
      return isWithinInterval(date, {start, end});
    });
  }

  return list;
});

const mapasVigentes = computed(() => {
  const mapa = mapasStore.mapaCompleto as any;
  if (mapa?.unidade) {
    return [{
      codigo: mapa.codigo || 1,
      unidade: mapa.unidade.sigla,
      competencias: mapa.competencias || []
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
      return isWithinInterval(date, {start, end});
    });
  }

  return list;
});

const abrirModalMapasVigentes = () => {
  mostrarModalMapasVigentes.value = true;
};
const abrirModalDiagnosticosGaps = () => {
  mostrarModalDiagnosticosGaps.value = true;
};
const abrirModalAndamentoGeral = () => {
  mostrarModalAndamentoGeral.value = true;
};

onMounted(async () => {
  if (!processosStore.processosPainel || processosStore.processosPainel.length === 0) {
    if (perfilStore.perfilSelecionado && perfilStore.unidadeSelecionada) {
      await processosStore.buscarProcessosPainel(
          perfilStore.perfilSelecionado,
          perfilStore.unidadeSelecionada,
          0,
          100
      );
    }
  }
});
</script>

<style scoped>
.cursor-pointer {
  cursor: pointer;
}

.hover-shadow:hover {
  box-shadow: 0 .5rem 1rem rgba(0, 0, 0, .15) !important;
}
</style>
