<template>
  <div class="arvore-unidades__toolbar">
    <template v-if="modoSelecao">
      <div class="arvore-unidades__acoes">
        <BButton
            aria-label="Selecionar todas as unidades elegíveis"
            class="arvore-unidades__botao"
            size="sm"
            variant="outline-secondary"
            data-testid="btn-arvore-selecionar-todos"
            @click="$emit('selecionar-todos')">
          <i aria-hidden="true" class="bi bi-check-all me-1"/>
          Todos
        </BButton>

        <BButton
            aria-label="Desmarcar todas as unidades"
            class="arvore-unidades__botao arvore-unidades__botao--secundario"
            size="sm"
            variant="outline-secondary"
            data-testid="btn-arvore-limpar-selecao"
            @click="$emit('limpar-selecao')">
          <i aria-hidden="true" class="bi bi-x-lg me-1"/>
          Limpar
        </BButton>
      </div>
    </template>

    <div class="arvore-unidades__busca">
      <BFormInput
          :model-value="termoBusca"
          aria-label="Buscar unidade por sigla"
          class="arvore-unidades__input"
          placeholder="Filtrar..."
          size="sm"
          type="search"
          data-testid="inp-arvore-busca"
          @update:model-value="$emit('update:termoBusca', String($event ?? ''))"
      />
      <i
          v-if="!termoBusca"
          class="bi bi-search position-absolute top-50 end-0 translate-middle-y me-2 text-muted"
          style="pointer-events: none;"
      />
    </div>
  </div>
</template>

<script lang="ts" setup>
import {BButton, BFormInput} from "bootstrap-vue-next";

defineProps<{
  termoBusca: string;
  modoSelecao: boolean;
}>();

defineEmits<{
  'update:termoBusca': [value: string];
  'selecionar-todos': [];
  'limpar-selecao': [];
}>();
</script>

<style scoped>
.arvore-unidades__toolbar {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  margin-bottom: 0.4rem;
}

.arvore-unidades__acoes {
  display: flex;
  align-items: center;
  gap: 0.3rem;
}

.arvore-unidades__botao {
  min-width: 4.5rem;
  height: 1.85rem;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-weight: 500;
  padding: 0.1rem 0.5rem;
  font-size: 0.875rem;
}

.arvore-unidades__botao--secundario {
  min-width: 4.8rem;
}

.arvore-unidades__busca {
  flex: 1;
  position: relative;
}

.arvore-unidades__input {
  height: 1.85rem;
}

@media (max-width: 576px) {
  .arvore-unidades__toolbar {
    flex-direction: column;
    align-items: stretch;
  }

  .arvore-unidades__acoes {
    width: 100%;
  }

  .arvore-unidades__botao {
    flex: 1;
  }
}
</style>
