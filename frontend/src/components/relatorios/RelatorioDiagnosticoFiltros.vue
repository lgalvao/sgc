<template>
  <BCard class="mb-4">
    <div class="d-flex flex-column gap-3">
      <BFormGroup label-for="select-processo-diagnostico">
        <template #label>
          {{ TEXTOS_RELATORIOS.LABEL_SELECIONE_PROCESSO }}
        </template>
        <BFormSelect
            id="select-processo-diagnostico"
            :model-value="codProcessoSelecionado"
            :options="opcoesProcessos"
            data-testid="select-processo-relatorio-diagnostico"
            @update:model-value="atualizarProcessoSelecionado"
        />
      </BFormGroup>

      <BFormGroup v-if="codProcessoSelecionado" label-for="arvore-unidades-diagnostico">
        <template #label>
          Selecione as unidades
        </template>
        <div class="border rounded p-3 relatorio-diagnostico-filtros__arvore"
             data-testid="container-arvore-unidades-diagnostico">
          <ArvoreUnidades
              id="arvore-unidades-diagnostico"
              :model-value="unidadesSelecionadas"
              :mostrar-superiores-nao-elegiveis-como-indeterminados="true"
              :unidades="unidadesDisponiveis"
              @update:model-value="$emit('update:unidadesSelecionadas', $event)"
          />
        </div>
      </BFormGroup>

      <div class="d-flex flex-wrap gap-2">
        <BButton
            :disabled="carregando || !podeGerar"
            data-testid="btn-visualizar-relatorio-diagnostico"
            variant="success"
            @click="$emit('gerar')"
        >
          <BSpinner v-if="carregando" class="me-1" small/>
          <i v-else class="bi bi-eye me-1"/>
          Visualizar
        </BButton>
        <BButton
            :disabled="carregando || !podeGerar"
            data-testid="btn-exportar-relatorio-diagnostico"
            variant="outline-danger"
            @click="$emit('exportar')"
        >
          <BSpinner v-if="carregando" class="me-1" small/>
          <i v-else class="bi bi-file-earmark-pdf me-1"/>
          PDF
        </BButton>
      </div>
    </div>
  </BCard>
</template>

<script lang="ts" setup>
import {BButton, BCard, BFormGroup, BFormSelect, BSpinner} from "bootstrap-vue-next";
import ArvoreUnidades from "@/components/unidade/ArvoreUnidades.vue";
import {TEXTOS_RELATORIOS} from "@/constants/textos-relatorios";
import type {Unidade} from "@/types/tipos";

interface OpcaoProcesso {
  value: number | null;
  text: string;
}

interface Props {
  codProcessoSelecionado: number | null;
  opcoesProcessos: OpcaoProcesso[];
  unidadesDisponiveis: Unidade[];
  unidadesSelecionadas: number[];
  carregando: boolean;
  podeGerar: boolean;
}

defineProps<Props>();

const emit = defineEmits<{
  (e: "update:codProcessoSelecionado", valor: number | null): void;
  (e: "update:unidadesSelecionadas", valor: number[]): void;
  (e: "gerar"): void;
  (e: "exportar"): void;
}>();

function atualizarProcessoSelecionado(valor: number | (number | null)[] | null) {
  emit("update:codProcessoSelecionado", typeof valor === "number" ? valor : null);
}
</script>

<style scoped>
.relatorio-diagnostico-filtros__arvore {
  overflow-x: hidden;
  max-height: 22rem;
  overflow-y: auto;
  background: var(--bs-body-bg);
  border: 1px solid var(--bs-border-color);
  border-radius: 0.65rem;
}
</style>
