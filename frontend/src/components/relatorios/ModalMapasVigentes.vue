<template>
  <ModalPadrao
      v-model="modelValueComputed"
      :mostrar-botao-acao="false"
      tamanho="xl"
      texto-cancelar="Fechar"
      titulo="Mapas Vigentes"
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
            :key="mapa.codigo"
        >
          <td>{{ mapa.unidade }}</td>
          <td>{{ mapa.competencias?.length || 0 }}</td>
        </tr>
        </tbody>
      </table>
    </div>
  </ModalPadrao>
</template>

<script lang="ts" setup>
import {BButton} from "bootstrap-vue-next";
import {computed} from "vue";
import {downloadCSV, gerarCSV} from "@/utils/csv";
import ModalPadrao from "@/components/comum/ModalPadrao.vue";

interface MapaVigente {
  codigo: number | string;
  unidade: string;
  competencias?: any[];
}

const props = defineProps<{
  modelValue: boolean;
  mapas: MapaVigente[];
}>();

const emit = defineEmits<{
  'update:modelValue': [value: boolean];
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

defineExpose({
  modelValueComputed,
  exportar
});
</script>
