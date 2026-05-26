import {ref} from 'vue'
import {useRoute} from 'vue-router'
import html2canvas from 'html2canvas'
import apiClient from '@/axios-setup'
import {usePerfilStore} from '@/stores/perfil'
import {logger} from '@/utils'
import type {FeedbackTipo, MetadadosFeedback, PayloadFeedback} from '@/types/feedback'

const TIMEOUT_CAPTURA_MS = 5_000

function criarTimeoutCaptura(): Promise<never> {
    return new Promise((_, rejeitar) =>
        setTimeout(() => rejeitar(new Error('Timeout de captura de tela')), TIMEOUT_CAPTURA_MS)
    )
}

async function capturarCanvasPagina() {
    return Promise.race([
        html2canvas(document.body, {
            useCORS: true,
            allowTaint: false,
            logging: false,
            scale: window.devicePixelRatio,
        }),
        criarTimeoutCaptura(),
    ])
}

async function converterCanvasParaBlob(canvas: HTMLCanvasElement): Promise<Blob | null> {
    return new Promise((resolver) => {
        canvas.toBlob((blob: Blob | null) => resolver(blob), 'image/webp', 0.85)
    })
}

async function capturarBlobTela(): Promise<Blob | null> {
    try {
        const canvas = await capturarCanvasPagina()
        return await converterCanvasParaBlob(canvas)
    } catch (erro) {
        logger.error('[Feedback] Captura de tela falhou; prosseguindo sem screenshot.', erro)
        return null
    }
}

function criarMetadados(route: ReturnType<typeof useRoute>, perfilStore: ReturnType<typeof usePerfilStore>): MetadadosFeedback {
    return {
        usuarioCodigo: perfilStore.usuarioCodigo ? perfilStore.usuarioCodigo : '',
        usuarioNome: perfilStore.usuarioNome ? perfilStore.usuarioNome : '',
        rotaNome: route.name ? String(route.name) : '',
        rotaCaminho: route.fullPath,
        rotaQuery: JSON.stringify(route.query),
        tituloPagina: document.title,
        perfilAtivo: perfilStore.perfilSelecionado ?? null,
        unidadeAtiva: perfilStore.unidadeSelecionadaSigla ?? null,
        dataHora: new Date().toISOString(),
        fusoHorario: new Date().getTimezoneOffset(),
        userAgent: navigator.userAgent,
        larguraTela: window.innerWidth,
        alturaTela: window.innerHeight,
        idioma: navigator.language,
    }
}

function criarFormularioFeedback(payload: PayloadFeedback, captura: Blob | null): FormData {
    const form = new FormData()
    form.append('data', JSON.stringify(payload))
    if (captura) {
        form.append('screenshot', captura, 'screenshot.webp')
    }
    return form
}

/**
 * Composable para captura de tela, montagem de metadados e envio de feedback.
 *
 * Disponível apenas em builds com VITE_FEEDBACK_WIDGET=true.
 */
export function useFeedback() {
    const captura = ref<Blob | null>(null)
    const enviando = ref(false)
    const route = useRoute()
    const perfilStore = usePerfilStore()

    async function capturarTela(): Promise<void> {
        captura.value = await capturarBlobTela()
    }

    async function enviarFeedback(tipo: FeedbackTipo, nota: string): Promise<void> {
        enviando.value = true
        try {
            const payload: PayloadFeedback = {
                tipo,
                nota,
                metadados: criarMetadados(route, perfilStore),
            }
            await apiClient.post('/feedback', criarFormularioFeedback(payload, captura.value), {
                headers: {'Content-Type': 'multipart/form-data'},
            })
        } finally {
            enviando.value = false
        }
    }

    function removerCaptura(): void {
        captura.value = null
    }

    return {
        captura,
        enviando,
        capturarTela,
        enviarFeedback,
        removerCaptura,
    }
}
