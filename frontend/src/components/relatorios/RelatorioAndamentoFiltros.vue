<template>
  <BCard class="mb-4">
    <div class="d-flex flex-column gap-3">
      <BFormGroup label-for="select-processo">
        <template #label>
          {{ TEXTOS_RELATORIOS.LABEL_SELECIONE_PROCESSO }}
        </template>
        <BFormSelect
            id="select-processo"
            :model-value="codProcessoSelecionado"
            :options="opcoesProcessos"
            data-testid="select-processo-andamento"
            @update:model-value="atualizarProcessoSelecionado"
        />
      </BFormGroup>

      <div class="d-flex flex-wrap gap-2">
        <BButton
            :disabled="carregando || !codProcessoSelecionado"
            data-testid="btn-gerar-andamento"
            variant="success"
            @click="$emit('gerar')"
        >
          <BSpinner v-if="carregando" class="me-1" small/>
          <i v-else class="bi bi-eye me-1"/>
          Visualizar
        </BButton>
        <BButton
            :disabled="carregando || !codProcessoSelecionado"
            data-testid="btn-exportar-andamento"
            variant="outline-danger"
            @click="$emit('exportar')"
        >
          <i class="bi bi-file-earmark-pdf me-1"/>
          PDF
        </BButton>
      </div>
    </div>
  </BCard>
</template>

<script lang="ts" setup>
import {BButton, BCard, BFormGroup, BFormSelect, BSpinner} from "bootstrap-vue-next";
import {TEXTOS_RELATORIOS} from "@/constants/textos-relatorios";

interface OpcaoProcesso {
  value: number | null;
  text: string;
}

interface Props {
  codProcessoSelecionado: number | null;
  opcoesProcessos: OpcaoProcesso[];
  carregando: boolean;
}

defineProps<Props>();

const emit = defineEmits<{
  (e: "update:codProcessoSelecionado", valor: number | null): void;
  (e: "gerar"): void;
  (e: "exportar"): void;
}>();

function atualizarProcessoSelecionado(valor: number | (number | null)[] | null) {
  emit("update:codProcessoSelecionado", typeof valor === "number" ? valor : null);
}
</script>
