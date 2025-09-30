<template>
  <BaseModal
    :mostrar="mostrar"
    titulo="Disponibilizar Mapa"
    tipo="primary"
    icone="bi bi-share"
    @fechar="fechar"
  >
    <template #conteudo>
      <div class="mb-3">
        <label
          class="form-label"
          for="dataLimite"
        >Data limite para validação</label>
        <input
          id="dataLimite"
          v-model="dataLimiteValidacao"
          class="form-control"
          type="date"
          data-testid="input-data-limite"
        >
      </div>
      <div
        v-if="notificacao"
        class="alert alert-info mt-3"
      >
        {{ notificacao }}
      </div>
    </template>

    <template #acoes>
      <button
        class="btn btn-secondary"
        type="button"
        data-testid="btn-modal-cancelar"
        @click="fechar"
      >
        Cancelar
      </button>
      <button
        :disabled="!dataLimiteValidacao"
        class="btn btn-success"
        type="button"
        data-testid="btn-disponibilizar"
        @click="disponibilizar"
      >
        Disponibilizar
      </button>
    </template>
  </BaseModal>
</template>

<script lang="ts" setup>
import {ref, watch} from 'vue'
import BaseModal from './BaseModal.vue'

const props = defineProps<{
  mostrar: boolean
}>()

const emit = defineEmits<{
  fechar: []
  disponibilizar: [dataLimite: string]
}>()

const dataLimiteValidacao = ref('')
const notificacao = ref('')

watch(() => props.mostrar, (mostrar) => {
  if (mostrar) {
    dataLimiteValidacao.value = ''
    notificacao.value = ''
  }
})

function fechar() {
  emit('fechar')
}

function disponibilizar() {
  emit('disponibilizar', dataLimiteValidacao.value)
}
</script>