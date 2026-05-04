<script lang="ts" setup>
import {computed, ref, watch} from 'vue'
import {BFormGroup, BFormTextarea, BModal} from 'bootstrap-vue-next'
import type {FeedbackTipo} from '@/types/feedback'

const props = defineProps<{
  visivel: boolean
  captura: Blob | null
  enviando: boolean
}>()

const emit = defineEmits<{
  fechar: []
  enviar: [tipo: FeedbackTipo, nota: string]
  removerCaptura: []
}>()

const nota = ref('')
const tipo = ref<FeedbackTipo>('bug')
const erroNota = ref('')
const urlCaptura = ref<string | null>(null)

const opcoesType = [
  {text: '🐛 Problema', value: 'bug'},
  {text: '💡 Sugestão', value: 'sugestao'},
  {text: '❓ Dúvida', value: 'questao'},
  {text: '👏 Elogio', value: 'elogio'},
]

watch(() => props.captura, (blob) => {
  if (urlCaptura.value) {
    URL.revokeObjectURL(urlCaptura.value)
    urlCaptura.value = null
  }
  if (blob) {
    urlCaptura.value = URL.createObjectURL(blob)
  }
}, {immediate: true})

watch(() => props.visivel, (aberto) => {
  if (aberto) {
    nota.value = ''
    erroNota.value = ''
    tipo.value = 'bug'
  }
})

const notaValida = computed(() => nota.value.trim().length >= 10)

function submeter() {
  if (!notaValida.value) {
    erroNota.value = 'Descreva o problema com pelo menos 10 caracteres.'
    return
  }
  erroNota.value = ''
  emit('enviar', tipo.value, nota.value.trim())
}

function abrirCapturaEmNovaAba() {
  if (urlCaptura.value) {
    window.open(urlCaptura.value, '_blank')
  }
}
</script>

<template>
  <BModal
      :model-value="visivel"
      data-testid="feedback-modal"
      hide-footer
      @hide="emit('fechar')"
  >
    <template #title>
      <span data-testid="feedback-modal-title">Enviar feedback</span>
    </template>
    <form @submit.prevent="submeter">
      <!-- Prévia da captura de tela -->
      <div v-if="urlCaptura" class="mb-3">
        <p class="form-label mb-1 small text-muted">Captura de tela incluída:</p>
        <div class="d-flex align-items-center gap-2">
          <button
              class="btn p-0 border-0"
              title="Abrir captura em nova aba"
              type="button"
              @click="abrirCapturaEmNovaAba"
          >
            <img
                :src="urlCaptura"
                alt="Prévia da captura de tela"
                class="img-thumbnail feedback-thumbnail"
                data-testid="feedback-thumbnail"
                style="max-width: 200px"
            />
          </button>
          <button
              class="btn btn-sm btn-link text-danger p-0"
              data-testid="feedback-btn-remover-captura"
              type="button"
              @click="emit('removerCaptura')"
          >
            Remover captura
          </button>
        </div>
      </div>

      <!-- Classificação -->
      <BFormGroup class="mb-3" label="Tipo de feedback">
        <div class="d-flex flex-column gap-2">
          <div v-for="opcao in opcoesType" :key="opcao.value" class="form-check">
            <input
                :id="'tipo-' + opcao.value"
                v-model="tipo"
                :data-testid="'feedback-tipo-' + opcao.value"
                :name="'feedback-tipo'"
                :value="opcao.value"
                class="form-check-input"
                type="radio"
            />
            <label :for="'tipo-' + opcao.value" class="form-check-label">
              {{ opcao.text }}
            </label>
          </div>
        </div>
      </BFormGroup>

      <!-- Nota / descrição -->
      <BFormGroup
          :invalid-feedback="erroNota"
          :state="erroNota ? false : null"
          class="mb-3"
      >
        <template #label>
          Descreva o problema ou sugestão
          <span aria-hidden="true" class="text-danger">*</span>
        </template>
        <BFormTextarea
            v-model="nota"
            :rows="4"
            :state="erroNota ? false : null"
            data-testid="feedback-nota"
            maxlength="2000"
            placeholder="Mínimo de 10 caracteres..."
        />
        <template #invalid-feedback>{{ erroNota }}</template>
      </BFormGroup>

      <!-- Ações -->
      <div class="d-flex justify-content-end gap-2">
        <button
            :disabled="enviando"
            class="btn btn-secondary"
            data-testid="feedback-btn-cancelar"
            type="button"
            @click="emit('fechar')"
        >
          Cancelar
        </button>
        <button
            :disabled="enviando"
            class="btn btn-primary"
            data-testid="feedback-btn-enviar"
            type="submit"
        >
          <span v-if="enviando" aria-hidden="true" class="spinner-border spinner-border-sm me-1" role="status"/>
          Enviar
        </button>
      </div>
    </form>
  </BModal>
</template>
