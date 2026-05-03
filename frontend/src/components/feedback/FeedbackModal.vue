<script lang="ts" setup>
import {computed, ref, watch} from 'vue'
import {BFormGroup, BFormRadioGroup, BFormTextarea, BModal} from 'bootstrap-vue-next'
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
        title="Enviar feedback"
        hide-footer
        data-testid="feedback-modal"
        @hide="emit('fechar')"
    >
        <form @submit.prevent="submeter">
            <!-- Prévia da captura de tela -->
            <div v-if="urlCaptura" class="mb-3">
                <p class="form-label mb-1 small text-muted">Captura de tela incluída:</p>
                <div class="d-flex align-items-center gap-2">
                    <img
                        :src="urlCaptura"
                        alt="Prévia da captura de tela"
                        class="img-thumbnail feedback-thumbnail"
                        style="max-width: 200px; cursor: pointer"
                        @click="abrirCapturaEmNovaAba"
                    />
                    <button
                        class="btn btn-sm btn-link text-danger p-0"
                        type="button"
                        @click="emit('removerCaptura')"
                    >
                        Remover captura
                    </button>
                </div>
            </div>

            <!-- Classificação -->
            <BFormGroup class="mb-3" label="Tipo de feedback">
                <BFormRadioGroup
                    v-model="tipo"
                    :options="opcoesType"
                    data-testid="feedback-tipo"
                    stacked
                />
            </BFormGroup>

            <!-- Nota / descrição -->
            <BFormGroup
                class="mb-3"
                :invalid-feedback="erroNota"
                :state="erroNota ? false : null"
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
                    class="btn btn-secondary"
                    type="button"
                    :disabled="enviando"
                    @click="emit('fechar')"
                >
                    Cancelar
                </button>
                <button
                    class="btn btn-primary"
                    type="submit"
                    :disabled="enviando"
                    data-testid="feedback-btn-enviar"
                >
                    <span v-if="enviando" class="spinner-border spinner-border-sm me-1" role="status" aria-hidden="true"/>
                    Enviar
                </button>
            </div>
        </form>
    </BModal>
</template>
