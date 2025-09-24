<template>
  <div
    v-if="mostrar"
    class="modal fade show"
    style="display: block;"
    tabindex="-1"
    :class="{ 'modal-lg': tamanho === 'large', 'modal-xl': tamanho === 'extra-large' }"
  >
    <div
      class="modal-dialog"
      :class="{ 'modal-dialog-centered': centralizado }"
    >
      <div class="modal-content">
        <div
          class="modal-header"
          :class="{
            'bg-primary text-white': tipo === 'primary',
            'bg-success text-white': tipo === 'success',
            'bg-warning text-dark': tipo === 'warning',
            'bg-danger text-white': tipo === 'danger',
            'bg-info text-white': tipo === 'info',
            'bg-secondary text-white': tipo === 'secondary'
          }"
        >
          <h5 class="modal-title">
            <i
              v-if="icone"
              :class="icone"
            />
            {{ titulo }}
          </h5>
          <button
            type="button"
            class="btn-close"
            :class="{ 'btn-close-white': tipo !== 'warning' }"
            @click="$emit('fechar')"
          />
        </div>
        <div class="modal-body">
          <slot name="conteudo" />
        </div>
        <div
          v-if="$slots.acoes"
          class="modal-footer"
        >
          <slot name="acoes" />
        </div>
      </div>
    </div>
  </div>
  <div
    v-if="mostrar"
    class="modal-backdrop fade show"
  />
</template>

<script lang="ts" setup>
interface Props {
  mostrar: boolean;
  titulo: string;
  tipo?: 'primary' | 'success' | 'warning' | 'danger' | 'info' | 'secondary';
  tamanho?: 'default' | 'large' | 'extra-large';
  centralizado?: boolean;
  icone?: string;
}

withDefaults(defineProps<Props>(), {
  tipo: 'primary',
  tamanho: 'default',
  centralizado: true,
  icone: ''
});

defineEmits<{
  fechar: [];
}>();
</script>

<style scoped>
.modal-content {
  border: none;
  border-radius: 0.5rem;
  box-shadow: 0 0.5rem 1rem rgba(0, 0, 0, 0.15);
}

.modal-header {
  border: none;
  border-radius: 0.5rem 0.5rem 0 0;
  padding: 1rem 1.5rem;
}

.modal-title {
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.modal-body {
  padding: 1.5rem;
}

.modal-footer {
  border: none;
  border-radius: 0 0 0.5rem 0.5rem;
  padding: 1rem 1.5rem;
}

.btn-close-white {
  filter: invert(1);
}
</style>