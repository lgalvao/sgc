<template>
  <BContainer class="mt-4">
    <PageHeader title="Relatórios" />

    <RelatorioFiltros
        v-model:tipo="filtroTipo"
        v-model:data-inicio="filtroDataInicio"
        v-model:data-fim="filtroDataFim"
    />

    <RelatorioCards
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
    <ModalAndamentoGeral
        v-model="mostrarModalAndamentoGeral"
        :processos="processosFiltrados"
    />
  </BContainer>
</template>

<script lang="ts" setup>
import {BContainer} from "bootstrap-vue-next";
import PageHeader from "@/components/layout/PageHeader.vue";
import ModalMapasVigentes from "@/components/relatorios/ModalMapasVigentes.vue";
import ModalDiagnosticosGaps from "@/components/relatorios/ModalDiagnosticosGaps.vue";
import ModalAndamentoGeral from "@/components/relatorios/ModalAndamentoGeral.vue";
import RelatorioFiltros from "@/components/relatorios/RelatorioFiltros.vue";
import RelatorioCards from "@/components/relatorios/RelatorioCards.vue";
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
  diagnosticosGaps,
  diagnosticosGapsFiltrados,
  abrirModalMapasVigentes,
  abrirModalDiagnosticosGaps,
  abrirModalAndamentoGeral
} = useRelatorios();
</script>
