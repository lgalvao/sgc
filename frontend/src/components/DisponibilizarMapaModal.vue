<template>
  <b-modal
    v-model="show"
    title="Disponibilizar Mapa"
    header-class="bg-primary text-white"
    size="lg"
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
      <b-button
        variant="secondary"
        data-testid="btn-modal-cancelar"
        @click="fechar"
      >
        Cancelar
      </b-button>
      <b-button
        :disabled="!dataLimiteValidacao"
        variant="success"
        data-testid="btn-disponibilizar"
        @click="disponibilizar"
      >
        Disponibilizar
      </b-button>
    </template>
  </b-modal>
</template>

<script lang="ts" setup>
import {ref, watch, computed} from 'vue'

const props = defineProps<{
  mostrar: boolean
}>()

const emit = defineEmits<{
  (e: 'update:mostrar', value: boolean): void
  (e: 'disponibilizar', dataLimite: string): void
}>()

const dataLimiteValidacao = ref('')
const notificacao = ref('')

const show = computed({
  get: () => props.mostrar,
  set: (value) => emit('update:mostrar', value)
})

watch(() => props.mostrar, (mostrar) => {
  if (mostrar) {
    dataLimiteValidacao.value = ''
    notificacao.value = ''
  }
})

function fechar() {
  emit('update:mostrar', false)
}

function disponibilizar() {
  emit('disponibilizar', dataLimiteValidacao.value)
}
</script>
