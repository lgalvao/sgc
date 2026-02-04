<template>
  <BModal
      v-model="modelValueComputed"
      :fade="false"
      hide-footer
      size="xl"
      title="Diagnósticos de Gaps"
  >
    <div class="mb-3">
      <BButton
          data-testid="export-csv-diagnosticos"
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
          <th>Processo</th>
          <th>Unidade</th>
          <th>Gaps Identificados</th>
          <th>Importância Média</th>
          <th>Dominio Médio</th>
          <th>Competências Críticas</th>
          <th>Status</th>
          <th>Data Diagnóstico</th>
        </tr>
        </thead>
        <tbody>
        <tr
            v-for="diagnostico in diagnosticos"
            :key="diagnostico.id"
        >
          <td>{{ diagnostico.processo }}</td>
          <td>{{ diagnostico.unidade }}</td>
          <td>{{ diagnostico.gaps }}</td>
          <td>{{ diagnostico.importanciaMedia }}/5</td>
          <td>{{ diagnostico.dominioMedio }}/5</td>
          <td>
            <small class="text-muted">
              {{ diagnostico.competenciasCriticas.join(', ') }}
            </small>
          </td>
          <td>
                <span :class="getClasseStatus(diagnostico.status)">
                  {{ diagnostico.status }}
                </span>
          </td>
          <td>{{ formatDateBR(diagnostico.data) }}</td>
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
import {formatDateBR} from "@/utils";

interface Diagnostico {
  id: number;
  processo: string;
  unidade: string;
  gaps: number;
  importanciaMedia: number;
  dominioMedio: number;
  competenciasCriticas: string[];
  data: Date;
  status: string;
}

const props = defineProps<{
  modelValue: boolean;
  diagnosticos: Diagnostico[];
}>();

const emit = defineEmits<{
  'update:modelValue': [value: boolean];
}>();

const modelValueComputed = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val),
});

const getClasseStatus = (status: string) => {
  switch (status) {
    case "Finalizado":
      return "badge bg-success";
    case "Em análise":
      return "badge bg-warning text-dark";
    case "Pendente":
      return "badge bg-danger";
    default:
      return "badge bg-secondary";
  }
};

function exportar() {
  const dados = props.diagnosticos.map((diag) => ({
    Processo: diag.processo,
    Unidade: diag.unidade,
    "Gaps Identificados": diag.gaps,
    "Importancia Media": diag.importanciaMedia,
    "Dominio Medio": diag.dominioMedio,
    "Competencias Criticas": diag.competenciasCriticas.join("; "),
    Status: diag.status,
    "Data Diagnostico": formatDateBR(diag.data),
  }));

  const csv = gerarCSV(dados);
  downloadCSV(csv, "diagnosticos-gaps.csv");
}
</script>
