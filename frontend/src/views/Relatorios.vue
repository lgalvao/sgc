<template>
  <LayoutPadrao>
    <PageHeader title="Relatórios" />

    <RelatorioFiltrosSection
        v-model:tipo="filtroTipo"
        v-model:data-inicio="filtroDataInicio"
        v-model:data-fim="filtroDataFim"
    />

    <RelatorioCardsSection
        :mapas-vigentes-count="mapasVigentes.length"
        :diagnosticos-gaps-count="diagnosticosGapsFiltrados.length"
        :processos-filtrados-count="processosFiltrados.length"
        @abrir-mapas-vigentes="abrirModalMapasVigentes"
        @abrir-diagnosticos-gaps="abrirModalDiagnosticosGaps"
        @abrir-andamento-geral="abrirModalAndamentoGeral"
    />

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
import LayoutPadrao from "@/components/layout/LayoutPadrao.vue";
import PageHeader from "@/components/layout/PageHeader.vue";
import ModalMapasVigentes from "@/components/relatorios/ModalMapasVigentes.vue";
import ModalDiagnosticosGaps from "@/components/relatorios/ModalDiagnosticosGaps.vue";
import ModalRelatorioAndamento from "@/components/relatorios/ModalRelatorioAndamento.vue";
import RelatorioFiltrosSection from "@/components/relatorios/RelatorioFiltrosSection.vue";
import RelatorioCardsSection from "@/components/relatorios/RelatorioCardsSection.vue";
import {useRelatorios} from "@/composables/useRelatorios";

const {
  filtroTipo,
  filtroDataInicio,
  filtroDataFim,
  mostrarModalMapasVigentes,
  mostrarModalDiagnosticosGaps,
  mostrarModalAndamentoGeral,
  processosFiltrados,
  mapasVigentes,
  diagnosticosGapsFiltrados,
  abrirModalMapasVigentes,
  abrirModalDiagnosticosGaps,
  abrirModalAndamentoGeral
} = useRelatorios();
</script>
