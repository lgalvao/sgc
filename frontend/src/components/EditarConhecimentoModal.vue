<template>
  <b-modal
    v-model="show"
    title="Editar Conhecimento"
    centered
    @hidden="fechar"
  >
    <div class="mb-3">
      <label
        for="descricaoConhecimento"
        class="form-label"
      >Descrição do Conhecimento</label>
      <textarea
        id="descricaoConhecimento"
        v-model="descricaoEditada"
        class="form-control"
        data-testid="input-conhecimento-modal"
        placeholder="Descreva o conhecimento"
        rows="3"
        @keyup.ctrl.enter="salvar"
      />
    </div>
    <template #footer>
      <b-button
        variant="secondary"
        @click="fechar"
      >
        Cancelar
      </b-button>
      <b-button
        variant="primary"
        data-testid="btn-salvar-conhecimento-modal"
        :disabled="!descricaoEditada?.trim()"
        @click="salvar"
      >
        Salvar
      </b-button>
    </template>
  </b-modal>
</template>

<script lang="ts" setup>
import {ref, watch, computed} from 'vue'

interface Props {
  mostrar: boolean
  conhecimento?: {
    id: number
    descricao: string
  } | null
}

const props = defineProps<Props>()

const emit = defineEmits<{
  (e: 'update:mostrar', value: boolean): void
  (e: 'salvar', conhecimentoId: number, novaDescricao: string): void
}>()

const descricaoEditada = ref('')

const show = computed({
  get: () => props.mostrar,
  set: (value) => emit('update:mostrar', value)
})

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

function fechar() {
  emit('update:mostrar', false)
}

function salvar() {
  if (props.conhecimento && descricaoEditada.value?.trim()) {
    emit('salvar', props.conhecimento.id, descricaoEditada.value.trim())
    fechar()
  }
}
</script>
