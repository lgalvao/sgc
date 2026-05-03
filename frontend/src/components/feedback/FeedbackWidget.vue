<script lang="ts" setup>
import {ref} from 'vue'
import {useToast} from 'bootstrap-vue-next'
import FeedbackButton from './FeedbackButton.vue'
import FeedbackModal from './FeedbackModal.vue'
import {useFeedback} from '@/composables/useFeedback'
import type {FeedbackTipo} from '@/types/feedback'

type EstadoBotao = 'normal' | 'carregando' | 'sucesso' | 'erro'

const {captura, enviando, capturarTela, enviarFeedback, removerCaptura} = useFeedback()
const {create: criarToast} = useToast()

const modalAberto = ref(false)
const estadoBotao = ref<EstadoBotao>('normal')

async function aoClicarBotao() {
    estadoBotao.value = 'carregando'
    await capturarTela()
    estadoBotao.value = 'normal'
    modalAberto.value = true
}

async function aoEnviar(tipo: FeedbackTipo, nota: string) {
    try {
        await enviarFeedback(tipo, nota)
        modalAberto.value = false
        estadoBotao.value = 'sucesso'
        criarToast({
            props: {
                body: 'Feedback enviado com sucesso. Obrigado!',
                variant: 'success',
                modelValue: 4000,
                pos: 'bottom-end',
                noProgress: true,
            },
        })
        setTimeout(() => {
            estadoBotao.value = 'normal'
        }, 1500)
    } catch {
        estadoBotao.value = 'erro'
        criarToast({
            props: {
                body: 'Não foi possível enviar o feedback. Tente novamente.',
                variant: 'danger',
                modelValue: 5000,
                pos: 'bottom-end',
                noProgress: true,
            },
        })
        setTimeout(() => {
            estadoBotao.value = 'normal'
        }, 2000)
    }
}

function aoFechar() {
    modalAberto.value = false
}
</script>

<template>
    <Teleport to="body">
        <FeedbackButton
            :estado="enviando ? 'carregando' : estadoBotao"
            @click="aoClicarBotao"
        />
        <FeedbackModal
            :captura="captura"
            :enviando="enviando"
            :visivel="modalAberto"
            @enviar="aoEnviar"
            @fechar="aoFechar"
            @remover-captura="removerCaptura"
        />
    </Teleport>
</template>
