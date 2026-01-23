<template>
  <BContainer class="mt-4">
    <PageHeader title="Relatórios" />

    <!-- Filtros -->
    <div class="row mb-4">
      <div class="col-md-4">
        <label
            class="form-label"
            for="filtroTipo"
        >Tipo de Processo</label>
        <BFormSelect
            id="filtroTipo"
            v-model="filtroTipo"
            :options="[
            { value: '', text: 'Todos' },
            { value: TipoProcesso.MAPEAMENTO, text: 'Mapeamento' },
            { value: TipoProcesso.REVISAO, text: 'Revisão' },
            { value: TipoProcesso.DIAGNOSTICO, text: 'Diagnóstico' },
          ]"
            data-testid="sel-filtro-tipo"
        />
      </div>
      <div class="col-md-4">
        <label
            class="form-label"
            for="filtroDataInicio"
        >Data Início</label>
        <BFormInput
            id="filtroDataInicio"
            v-model="filtroDataInicio"
            data-testid="inp-filtro-data-inicio"
            type="date"
        />
      </div>
      <div class="col-md-4">
        <label
            class="form-label"
            for="filtroDataFim"
        >Data Fim</label>
        <BFormInput
            id="filtroDataFim"
            v-model="filtroDataFim"
            data-testid="inp-filtro-data-fim"
            type="date"
        />
      </div>
    </div>

    <div class="row">
      <div class="col-md-4 mb-4">
        <BCard
            class="h-100"
            data-testid="card-relatorio-mapas"
            role="button"
            style="cursor: pointer;"
            tabindex="0"
            @click="abrirModalMapasVigentes"
            @keydown.enter.prevent="abrirModalMapasVigentes"
            @keydown.space.prevent="abrirModalMapasVigentes"
        >
          <h3 class="card-title h5">
            Mapas Vigentes
          </h3>
          <p class="card-text">
            Visualize os mapas de competências atualmente vigentes em todas as unidades.
          </p>
          <small class="text-muted">{{ mapasVigentes.length }} mapas encontrados</small>
        </BCard>
      </div>
      <div class="col-md-4 mb-4">
        <BCard
            class="h-100"
            data-testid="card-relatorio-gaps"
            role="button"
            style="cursor: pointer;"
            tabindex="0"
            @click="abrirModalDiagnosticosGaps"
            @keydown.enter.prevent="abrirModalDiagnosticosGaps"
            @keydown.space.prevent="abrirModalDiagnosticosGaps"
        >
          <h3 class="card-title h5">
            Diagnósticos de Gaps
          </h3>
          <p class="card-text">
            Analise os gaps de competências identificados nos processos de diagnóstico.
          </p>
          <small class="text-muted">{{ diagnosticosGaps.length }} diagnósticos encontrados</small>
        </BCard>
      </div>
      <div class="col-md-4 mb-4">
        <BCard
            class="h-100"
            data-testid="card-relatorio-andamento"
            role="button"
            style="cursor: pointer;"
            tabindex="0"
            @click="abrirModalAndamentoGeral"
            @keydown.enter.prevent="abrirModalAndamentoGeral"
            @keydown.space.prevent="abrirModalAndamentoGeral"
        >
          <h3 class="card-title h5">
            Andamento Geral
          </h3>
          <p class="card-text">
            Acompanhe o andamento de todos os processos de mapeamento e revisão.
          </p>
          <small class="text-muted">{{ processosFiltrados.length }} processos encontrados</small>
        </BCard>
      </div>
    </div>

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
    <ModalAndamentoGeral
        v-model="mostrarModalAndamentoGeral"
        :processos="processosFiltrados"
    />
  </BContainer>
</template>

<script lang="ts" setup>
import {BCard, BContainer, BFormInput, BFormSelect} from "bootstrap-vue-next";
import PageHeader from "@/components/layout/PageHeader.vue";
import ModalMapasVigentes from "@/components/relatorios/ModalMapasVigentes.vue";
import ModalDiagnosticosGaps from "@/components/relatorios/ModalDiagnosticosGaps.vue";
import ModalAndamentoGeral from "@/components/relatorios/ModalAndamentoGeral.vue";
import {computed, ref} from "vue";
import {useMapasStore} from "@/stores/mapas";
import {useProcessosStore} from "@/stores/processos";
import {TipoProcesso} from "@/types/tipos";

const processosStore = useProcessosStore();
const mapasStore = useMapasStore();

const filtroTipo = ref("");
const filtroDataInicio = ref("");
const filtroDataFim = ref("");

const mostrarModalMapasVigentes = ref(false);
const mostrarModalDiagnosticosGaps = ref(false);
const mostrarModalAndamentoGeral = ref(false);

// Dados computados
const processosFiltrados = computed(() => {
  let processos = processosStore.processosPainel;

  if (filtroTipo.value) {
    processos = processos.filter((p) => p.tipo === filtroTipo.value);
  }

  if (filtroDataInicio.value) {
    const dataInicio = new Date(filtroDataInicio.value);
    processos = processos.filter((p) => new Date(p.dataCriacao) >= dataInicio);
  }

  if (filtroDataFim.value) {
    const dataFim = new Date(filtroDataFim.value);
    processos = processos.filter((p) => new Date(p.dataCriacao) <= dataFim);
  }

  return processos;
});

const mapasVigentes = computed(() => {
  // Filtrar mapas vigentes (aqueles com processos finalizados)
  const mapa = mapasStore.mapaCompleto as any;
  if (
      mapa &&
      mapa.competencias &&
      mapa.competencias.length > 0 &&
      mapa.unidade
  ) {
    return [
      {
        ...mapa,
        unidade: mapa.unidade.sigla || 'N/A',
        id: mapa.codigo,
      },
    ];
  }
  return [];
});

const diagnosticosGaps = computed(() => {
  // Simulação de dados de diagnóstico mais completos
  return [
    {
      id: 1,
      processo: "Diagnóstico Anual 2024",
      unidade: "SESEL",
      gaps: 3,
      importanciaMedia: 4.2,
      dominioMedio: 2.8,
      competenciasCriticas: [
        "Gestão de Processos",
        "Análise de Dados",
        "Comunicação",
      ],
      data: new Date("2024-08-15"),
      status: "Em análise",
    },
    {
      id: 2,
      processo: "Diagnóstico Anual 2024",
      unidade: "COJUR",
      gaps: 5,
      importanciaMedia: 3.9,
      dominioMedio: 3.1,
      competenciasCriticas: [
        "Gestão Jurídica",
        "Análise de Riscos",
        "Ética Profissional",
        "Gestão de Equipes",
      ],
      data: new Date("2024-08-20"),
      status: "Finalizado",
    },
    {
      id: 3,
      processo: "Diagnóstico Semestral 2024",
      unidade: "COSIS",
      gaps: 2,
      importanciaMedia: 4.5,
      dominioMedio: 3.2,
      competenciasCriticas: ["Segurança da Informação", "Gestão de Projetos"],
      data: new Date("2024-07-10"),
      status: "Em análise",
    },
    {
      id: 4,
      processo: "Diagnóstico Anual 2024",
      unidade: "STIC",
      gaps: 4,
      importanciaMedia: 4.1,
      dominioMedio: 2.9,
      competenciasCriticas: [
        "Infraestrutura de TI",
        "Suporte Técnico",
        "Análise de Sistemas",
      ],
      data: new Date("2024-09-01"),
      status: "Em análise",
    },
  ];
});

const diagnosticosGapsFiltrados = computed(() => {
  let diagnosticos = diagnosticosGaps.value;

  if (filtroTipo.value && filtroTipo.value !== TipoProcesso.DIAGNOSTICO) {
    // Se filtro não for diagnóstico, mostrar apenas diagnósticos relacionados ao tipo
    return [];
  }

  if (filtroDataInicio.value) {
    const dataInicio = new Date(filtroDataInicio.value);
    diagnosticos = diagnosticos.filter((d) => d.data >= dataInicio);
  }

  if (filtroDataFim.value) {
    const dataFim = new Date(filtroDataFim.value);
    diagnosticos = diagnosticos.filter((d) => d.data <= dataFim);
  }

  return diagnosticos;
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
</script>

<style scoped>
.card:hover {
  background-color: var(--bs-light);
  border-color: var(--bs-primary);
}
</style>
