<template>
  <b-modal
    :model-value="mostrar"
    title="Disponibilizar Mapa"
    header-bg-variant="primary"
    header-text-variant="white"
    centered
    @hidden="fechar"
  >
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

    <template #footer>
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
  </b-modal>
</template>

<script lang="ts" setup>
import {ref, watch} from 'vue'

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
