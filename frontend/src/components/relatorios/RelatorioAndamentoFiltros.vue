<template>
  <BCard class="mb-4">
    <div class="d-flex flex-column gap-3">
      <BFormGroup label-for="select-processo">
        <template #label>
          {{ TEXTOS.relatorios.LABEL_SELECIONE_PROCESSO }} <span aria-hidden="true" class="text-danger">*</span>
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
            variant="success"
            data-testid="btn-gerar-andamento"
            @click="$emit('gerar')"
        >
          <BSpinner v-if="carregando" small class="me-1"/>
          <i v-else class="bi bi-search me-1"/>
          {{ TEXTOS.relatorios.BOTAO_GERAR }}
        </BButton>
        <BButton
            :disabled="carregando || !codProcessoSelecionado"
            variant="outline-danger"
            data-testid="btn-exportar-andamento"
            @click="$emit('exportar')"
        >
          <i class="bi bi-file-earmark-pdf me-1"/>
          PDF
        </BButton>
      </div>
    </div>
  </BCard>
</template>

<script setup lang="ts">
import {BButton, BCard, BFormGroup, BFormSelect, BSpinner} from "bootstrap-vue-next";
import {TEXTOS} from "@/constants/textos";

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
