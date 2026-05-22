<script lang="ts" setup>
import {computed, ref, watch} from 'vue'
import {BFormGroup, BModal} from 'bootstrap-vue-next'
import EditorTextoRico from '@/components/comum/EditorTextoRico.vue'
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
  {titulo: 'Problema', value: 'bug'},
  {titulo: 'Sugestão', value: 'sugestao'},
  {titulo: 'Dúvida', value: 'questao'},
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

const notaTextoPlano = computed(() =>
  nota.value
      .replaceAll(/<br\s*\/?>/gi, '\n')
      .replaceAll(/<\/(p|div|li)>/gi, '\n')
      .replaceAll(/<[^>]+>/g, ' ')
      .replaceAll(/&nbsp;/gi, ' ')
      .replaceAll(/\s+\n/g, '\n')
      .replaceAll(/\n{3,}/g, '\n\n')
      .trim()
)

const notaValida = computed(() => notaTextoPlano.value.length >= 10)

function submeter() {
  if (!notaValida.value) {
    erroNota.value = 'Descreva o problema com pelo menos 10 caracteres.'
    return
  }
  erroNota.value = ''
  emit('enviar', tipo.value, nota.value.trim())
}

</script>

<template>
  <BModal
      :model-value="visivel"
      body-class="p-0"
      data-testid="feedback-modal"
      hide-footer
      no-footer
      size="lg"
      @hide="emit('fechar')"
  >
    <template #title>
      <span data-testid="feedback-modal-title">Enviar feedback</span>
    </template>
    <form @submit.prevent="submeter">
      <div class="feedback-modal__conteudo">
        <section class="feedback-modal__principal">
          <div class="feedback-modal__campo">
            <div class="form-label feedback-modal__label">
              Descreva o problema, a dúvida ou a sugestão
              <span aria-hidden="true" class="text-danger">*</span>
            </div>
            <EditorTextoRico
                v-model="nota"
                data-testid="feedback-nota"
                :desabilitado="enviando"
                class="feedback-modal__textarea"
                rotulo="Descrição do feedback"
            />
            <div v-if="erroNota" class="invalid-feedback d-block">{{ erroNota }}</div>
          </div>
        </section>

        <aside class="feedback-modal__lateral">
          <BFormGroup class="mb-0" label="Classificação">
            <div class="feedback-modal__tipos">
              <div
                  v-for="opcao in opcoesType"
                  :key="opcao.value"
                  class="form-check"
              >
                <input
                    :id="'tipo-' + opcao.value"
                    v-model="tipo"
                    :data-testid="'feedback-tipo-' + opcao.value"
                    :name="'feedback-tipo'"
                    :value="opcao.value"
                    class="form-check-input"
                    type="radio"
                />
                <label
                    :for="'tipo-' + opcao.value"
                    class="form-check-label fw-medium"
                >
                  {{ opcao.titulo }}
                </label>
              </div>
            </div>
          </BFormGroup>

          <section class="feedback-modal__captura">
            <div class="d-flex justify-content-between align-items-center mb-3 gap-3">
              <div>
                <div class="form-label mb-0">Captura da tela</div>
              </div>
              <button
                  v-if="urlCaptura"
                  aria-label="Remover captura"
                  class="btn btn-sm btn-outline-secondary feedback-modal__remover-captura"
                  data-testid="feedback-btn-remover-captura"
                  title="Remover captura"
                  type="button"
                  @click="emit('removerCaptura')"
              >
                <i class="bi bi-trash3" aria-hidden="true"/>
              </button>
            </div>

            <div v-if="urlCaptura" class="feedback-modal__captura-card">
              <img
                  :src="urlCaptura"
                  alt="Prévia da captura de tela"
                  class="feedback-thumbnail"
                  data-testid="feedback-thumbnail"
              />
            </div>
            <div v-else class="feedback-modal__captura-vazia small text-body-secondary">
              Nenhuma captura anexada.
            </div>
          </section>
        </aside>
      </div>

      <div class="feedback-modal__acoes">
        <button
            :disabled="enviando"
            class="btn btn-outline-secondary"
            data-testid="feedback-btn-cancelar"
            type="button"
            @click="emit('fechar')"
        >
          Cancelar
        </button>
        <button
            :disabled="enviando"
            class="btn btn-success"
            data-testid="feedback-btn-enviar"
            type="submit"
        >
          <output v-if="enviando" aria-hidden="true" class="spinner-border spinner-border-sm me-1" />
          Enviar feedback
        </button>
      </div>
    </form>
  </BModal>
</template>

<style scoped>
.feedback-modal__conteudo {
  display: grid;
  grid-template-columns: minmax(0, 1.45fr) minmax(16rem, 0.82fr);
  gap: 1rem;
  align-items: stretch;
  padding: 1rem 1rem 0;
  min-height: 31rem;
}

.feedback-modal__principal,
.feedback-modal__lateral {
  min-width: 0;
}

.feedback-modal__principal {
  display: flex;
}

.feedback-modal__campo {
  display: flex;
  flex: 1 1 auto;
  flex-direction: column;
  min-height: 0;
}

.feedback-modal__label {
  margin-bottom: 0.55rem;
}

.feedback-modal__textarea {
  flex: 1 1 auto;
  min-height: 0;
  min-width: 0;
}

.feedback-modal__principal :deep(.form-label),
.feedback-modal__lateral :deep(.form-label) {
  margin-bottom: 0.5rem;
  font-size: 0.98rem;
  font-weight: 600;
  letter-spacing: normal;
  text-transform: none;
  color: var(--bs-body-color);
}

.feedback-modal__lateral {
  display: flex;
  flex-direction: column;
  gap: 0.85rem;
  padding-top: 0.1rem;
}

.feedback-modal__tipos {
  display: grid;
  gap: 0.4rem;
}

.feedback-modal__tipos .form-check {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  min-height: 1.75rem;
  padding-left: 0;
}

.feedback-modal__tipos .form-check-input {
  float: none;
  margin: 0;
}

.feedback-modal__tipos .form-check-label {
  line-height: 1.2;
  font-size: 0.98rem;
}

.feedback-modal__captura {
  display: flex;
  flex: 1 1 auto;
  flex-direction: column;
  min-height: 0;
  padding: 0.85rem;
  border: 1px solid var(--bs-border-color-translucent);
  border-radius: 0.75rem;
  background: var(--bs-tertiary-bg);
}

.feedback-modal__captura-card {
  position: relative;
  flex: 1 1 auto;
  border-radius: 0.75rem;
  overflow: hidden;
  background: var(--bs-secondary-bg);
  border: 1px solid var(--bs-border-color);
  min-height: 13.5rem;
}

.feedback-thumbnail {
  position: absolute;
  inset: 0;
  display: block;
  width: 100%;
  height: 100%;
  object-fit: cover;
  object-position: center 0;
  background: transparent;
}

.feedback-modal__captura-vazia {
  flex: 1 1 auto;
  padding: 0.9rem;
  border: 1px dashed var(--bs-border-color);
  border-radius: 0.75rem;
  background: var(--bs-body-bg);
  min-height: 13.5rem;
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
}

.feedback-modal__remover-captura {
  width: 2rem;
  height: 2rem;
  padding: 0;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.feedback-modal__acoes {
  display: flex;
  justify-content: flex-end;
  gap: 0.65rem;
  padding: 0.9rem 1rem 1rem;
  margin-top: 0.75rem;
  border-top: 1px solid var(--bs-border-color-translucent);
  background: transparent;
}

.feedback-modal__acoes .btn {
  min-width: 9.25rem;
  min-height: 2.6rem;
  border-radius: 0.6rem;
  font-weight: 600;
}

.feedback-modal__acoes .btn-success {
  box-shadow: none;
}

.feedback-modal__acoes .btn-outline-secondary {
  background: var(--bs-body-bg);
}

@media (max-width: 991px) {
  .feedback-modal__conteudo {
    grid-template-columns: 1fr;
    gap: 1.25rem;
    min-height: auto;
  }
}

@media (max-width: 576px) {
  .feedback-modal__conteudo,
  .feedback-modal__acoes {
    padding-left: 1rem;
    padding-right: 1rem;
  }

  .feedback-modal__acoes {
    flex-direction: column-reverse;
  }

  .feedback-modal__acoes .btn {
    width: 100%;
  }

  .feedback-modal__captura-card,
  .feedback-modal__captura-vazia {
    min-height: 13rem;
  }
}
</style>
