import {ref} from 'vue'
import {useRoute} from 'vue-router'
import html2canvas from 'html2canvas'
import apiClient from '@/axios-setup'
import {usePerfilStore} from '@/stores/perfil'
import {logger} from '@/utils'
import type {FeedbackTipo, MetadadosFeedback, PayloadFeedback} from '@/types/feedback'

const TIMEOUT_CAPTURA_MS = 5_000

/**
 * Composable para captura de tela, montagem de metadados e envio de feedback UAT.
 *
 * Disponível apenas em builds com VITE_FEEDBACK_WIDGET=true.
 */
export function useFeedback() {
    const captura = ref<Blob | null>(null)
    const enviando = ref(false)
    const route = useRoute()
    const perfilStore = usePerfilStore()

    async function capturarTela(): Promise<void> {
        try {
            const canvas = await Promise.race([
                html2canvas(document.body, {
                    useCORS: true,
                    allowTaint: false,
                    logging: false,
                    scale: window.devicePixelRatio,
                }),
                new Promise<never>((_, rejeitar) =>
                    setTimeout(() => rejeitar(new Error('Timeout de captura de tela')), TIMEOUT_CAPTURA_MS)
                ),
            ])

            captura.value = await new Promise<Blob | null>((resolver) => {
                canvas.toBlob((blob: Blob | null) => resolver(blob), 'image/webp', 0.85)
            })
        } catch (erro) {
            logger.error('[Feedback] Captura de tela falhou; prosseguindo sem screenshot.', erro)
            captura.value = null
        }
    }

    function montarMetadados(): MetadadosFeedback {
        return {
            usuarioCodigo: perfilStore.usuarioCodigo ?? '',
            usuarioNome: perfilStore.usuarioNome ?? '',
            rotaNome: String(route.name ?? ''),
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

    async function enviarFeedback(tipo: FeedbackTipo, nota: string): Promise<void> {
        enviando.value = true
        try {
            const payload: PayloadFeedback = {
                tipo,
                nota,
                metadados: montarMetadados(),
            }

            const form = new FormData()
            form.append('data', JSON.stringify(payload))
            if (captura.value) {
                form.append('screenshot', captura.value, 'screenshot.webp')
            }

            await apiClient.post('/feedback', form, {
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
