<template>
  <div class="arvore-unidades__toolbar">
    <div class="arvore-unidades__acoes">
      <template v-if="modoSelecao">
        <BButton
            aria-label="Selecionar todas as unidades elegíveis"
            class="arvore-unidades__botao-icone"
            data-testid="btn-arvore-selecionar-todos"
            size="sm"
            variant="outline-secondary"
            @click="$emit('selecionar-todos')">
          <i aria-hidden="true" class="bi bi-check-all"/>
        </BButton>

        <BButton
            aria-label="Desmarcar todas as unidades"
            class="arvore-unidades__botao-icone"
            data-testid="btn-arvore-limpar-selecao"
            size="sm"
            variant="outline-secondary"
            @click="$emit('limpar-selecao')">
          <i aria-hidden="true" class="bi bi-x-lg"/>
        </BButton>
      </template>

      <template v-if="exibirAcoesExpansao">
        <BButton
            aria-label="Expandir todas as unidades"
            class="arvore-unidades__botao-icone"
            data-testid="btn-arvore-expandir-tudo"
            size="sm"
            variant="outline-secondary"
            @click="$emit('expandir-todos')">
          <i aria-hidden="true" class="bi bi-arrows-expand"/>
        </BButton>

        <BButton
            aria-label="Recolher todas as unidades"
            class="arvore-unidades__botao-icone"
            data-testid="btn-arvore-recolher-tudo"
            size="sm"
            variant="outline-secondary"
            @click="$emit('recolher-todos')">
          <i aria-hidden="true" class="bi bi-arrows-collapse"/>
        </BButton>
      </template>
    </div>

    <div class="arvore-unidades__busca">
      <BFormInput
          :model-value="termoBusca"
          aria-label="Buscar unidade por sigla"
          class="arvore-unidades__input"
          data-testid="inp-arvore-busca"
          placeholder="Filtrar..."
          size="sm"
          type="search"
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

withDefaults(defineProps<{
  termoBusca: string;
  modoSelecao: boolean;
  exibirAcoesExpansao?: boolean;
}>(), {
  exibirAcoesExpansao: true,
});

defineEmits<{
  'update:termoBusca': [value: string];
  'selecionar-todos': [];
  'limpar-selecao': [];
  'expandir-todos': [];
  'recolher-todos': [];
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

.arvore-unidades__botao-icone {
  width: 1.85rem;
  height: 1.85rem;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0;
  border-color: #dee2e6 !important;
  color: #6c757d !important;
}

.arvore-unidades__botao-icone:hover {
  background-color: #f8f9fa !important;
  border-color: #ced4da !important;
  color: #212529 !important;
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

  .arvore-unidades__botao-icone {
    flex: 0 0 1.85rem;
  }
}
</style>
