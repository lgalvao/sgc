<template>
  <div
    v-if="mostrar"
    aria-labelledby="disponibilizarModalLabel"
    aria-modal="true"
    class="modal fade show"
    role="dialog"
    style="display: block;"
    tabindex="-1"
  >
    <div class="modal-dialog modal-dialog-centered">
      <div class="modal-content">
        <div class="modal-header">
          <h5
            id="disponibilizarModalLabel"
            class="modal-title"
          >
            Disponibilizar Mapa
          </h5>
          <button
            aria-label="Close"
            class="btn-close"
            type="button"
            @click="fechar"
          />
        </div>
        <div class="modal-body">
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
            >
          </div>
          <div
            v-if="notificacao"
            class="alert alert-info mt-3"
          >
            {{ notificacao }}
          </div>
        </div>

        <div class="modal-footer">
          <button
            class="btn btn-secondary"
            type="button"
            @click="fechar"
          >
            Cancelar
          </button>
          <button
            :disabled="!dataLimiteValidacao"
            class="btn btn-success"
            type="button"
            @click="disponibilizar"
          >
            Disponibilizar
          </button>
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