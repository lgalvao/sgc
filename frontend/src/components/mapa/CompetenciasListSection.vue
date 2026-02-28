<template>
  <div v-if="unidade">
    <div v-if="competencias.length === 0" class="mb-4 mt-3">
      <EmptyState
          description="Nenhuma competência cadastrada para esta unidade."
          icon="bi-journal-plus"
          title="Mapa de competências"
      >
        <BButton
            v-if="podeEditar"
            data-testid="btn-abrir-criar-competencia-empty"
            variant="primary"
            @click="$emit('criar')"
        >
          <i aria-hidden="true" class="bi bi-plus-lg me-2"/> Criar primeira competência
        </BButton>
      </EmptyState>
    </div>

    <div v-else class="mb-4 mt-3">
      <BButton
          v-if="podeEditar"
          class="mb-3"
          data-testid="btn-abrir-criar-competencia"
          variant="outline-primary"
          @click="$emit('criar')"
      >
        <i aria-hidden="true" class="bi bi-plus-lg"/> Criar competência
      </BButton>

      <CompetenciaCard
          v-for="comp in competencias"
          :key="comp.codigo"
          :atividades="atividades"
          :competencia="comp"
          :pode-editar="podeEditar"
          @editar="$emit('editar', $event)"
          @excluir="$emit('excluir', $event)"
          @remover-atividade="(competenciaId, atividadeId) => $emit('remover-atividade', competenciaId, atividadeId)"
      />
    </div>
  </div>
  <div v-else>
    <p>Unidade não encontrada.</p>
  </div>
</template>

<script lang="ts" setup>
import {BButton} from 'bootstrap-vue-next';
import EmptyState from '@/components/comum/EmptyState.vue';
import CompetenciaCard from '@/components/mapa/CompetenciaCard.vue';
import type {Atividade, Competencia, Unidade} from '@/types/tipos';

defineProps<{
  unidade?: Unidade | null;
  competencias: Competencia[];
  atividades: Atividade[];
  podeEditar: boolean;
}>();

defineEmits<{
  (e: 'criar'): void;
  (e: 'editar', competencia: Competencia): void;
  (e: 'excluir', codigo: number): void;
  (e: 'remover-atividade', competenciaId: number, atividadeId: number): void;
}>();
</script>
