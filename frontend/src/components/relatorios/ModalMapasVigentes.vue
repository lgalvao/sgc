<template>
  <BModal
      v-model="modelValueComputed"
      :fade="false"
      hide-footer
      size="xl"
      title="Mapas Vigentes"
  >
    <div class="mb-3">
      <BButton
          data-testid="export-csv-mapas"
          size="sm"
          variant="outline-primary"
          @click="exportar"
      >
        <i aria-hidden="true" class="bi bi-download"/> Exportar CSV
      </BButton>
    </div>
    <div class="table-responsive">
      <table class="table table-striped">
        <thead>
        <tr>
          <th>Unidade</th>
          <th>Competências</th>
        </tr>
        </thead>
        <tbody>
        <tr
            v-for="mapa in mapas"
            :key="mapa.id"
        >
          <td>{{ mapa.unidade }}</td>
          <td>{{ mapa.competencias?.length || 0 }}</td>
        </tr>
        </tbody>
      </table>
    </div>
  </BModal>
</template>

<script lang="ts" setup>
import {BButton, BModal} from "bootstrap-vue-next";
import {computed} from "vue";
import {downloadCSV, gerarCSV} from "@/utils/csv";

interface MapaVigente {
  id: number | string;
  unidade: string;
  competencias?: any[];
}

const props = defineProps<{
  modelValue: boolean;
  mapas: MapaVigente[];
}>();

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void;
}>();

const modelValueComputed = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val),
});

function exportar() {
  const dados = props.mapas.map((mapa) => ({
    Unidade: mapa.unidade,
    Competencias: mapa.competencias?.length || 0,
  }));

  const csv = gerarCSV(dados);
  downloadCSV(csv, "mapas-vigentes.csv");
}
</script>
