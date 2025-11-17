<template>
  <b-modal
    :model-value="mostrar"
    title="Editar Conhecimento"
    centered
    @hidden="$emit('fechar')"
  >
    <div class="mb-3">
      <label
        for="descricaoConhecimento"
        class="form-label"
      >Descrição do Conhecimento</label>
      <b-form-textarea
        id="descricaoConhecimento"
        v-model="descricaoEditada"
        data-testid="input-conhecimento-modal"
        placeholder="Descreva o conhecimento"
        rows="3"
        @keyup.ctrl.enter="salvar"
      />
    </div>
    <template #footer>
      <button
        type="button"
        class="btn btn-secondary"
        @click="$emit('fechar')"
      >
        Cancelar
      </button>
      <button
        type="button"
        class="btn btn-primary"
        data-testid="btn-salvar-conhecimento-modal"
        :disabled="!descricaoEditada?.trim()"
        @click="salvar"
      >
        Salvar
      </button>
    </template>
  </b-modal>
</template>

<script lang="ts" setup>
import {ref, watch} from 'vue'

interface Props {
  mostrar: boolean
  conhecimento?: {
    id: number
    descricao: string
  } | null
}

interface Emits {
  (e: 'fechar'): void
  (e: 'salvar', conhecimentoId: number, novaDescricao: string): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const descricaoEditada = ref('')

// Atualizar descrição quando o conhecimento mudar
watch(() => props.conhecimento, (novoConhecimento) => {
  if (novoConhecimento) {
    descricaoEditada.value = novoConhecimento.descricao
  }
}, { immediate: true })

// Limpar quando fechar
watch(() => props.mostrar, (mostrar) => {
  if (!mostrar) {
    descricaoEditada.value = ''
  }
})

function salvar() {
  if (props.conhecimento && descricaoEditada.value?.trim()) {
    emit('salvar', props.conhecimento.id, descricaoEditada.value.trim())
  }
}
</script>
