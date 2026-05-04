import {beforeEach, describe, expect, it, vi} from 'vitest'
import {mount} from '@vue/test-utils'
import FeedbackModal from '../FeedbackModal.vue'
import {createTestingPinia} from '@pinia/testing'

// Mock do URL
global.URL.createObjectURL = vi.fn(() => 'blob:url')
global.URL.revokeObjectURL = vi.fn()

describe('FeedbackModal.vue', () => {
    const defaultProps = {
        visivel: true,
        captura: null,
        enviando: false
    }

    beforeEach(() => {
        vi.clearAllMocks()
    })

    const stubs = {
        'b-modal': {
            props: ['modelValue'],
            template: '<div v-if="modelValue"><slot name="title" /><slot /></div>'
        }
    }

    function definirConteudoEditor(wrapper: ReturnType<typeof mount>, conteudoHtml: string) {
        const editor = wrapper.find('[data-testid="feedback-nota"]')
        ;(editor.element as HTMLDivElement).innerHTML = conteudoHtml
        return editor.trigger('input')
    }

    it('deve renderizar o título correto', () => {
        const wrapper = mount(FeedbackModal, {
            props: defaultProps,
            global: {
                plugins: [createTestingPinia()],
                stubs
            }
        })
        expect(wrapper.find('[data-testid="feedback-modal-title"]').text()).toBe('Enviar feedback')
    })

    it('deve exibir a captura de tela se fornecida', () => {
        const blob = new Blob(['test'], {type: 'image/png'})
        const wrapper = mount(FeedbackModal, {
            props: {...defaultProps, captura: blob},
            global: {
                plugins: [createTestingPinia()],
                stubs
            }
        })
        expect(wrapper.find('[data-testid="feedback-thumbnail"]').exists()).toBe(true)
        expect(URL.createObjectURL).toHaveBeenCalledWith(blob)
    })

    it('deve validar o comprimento da nota ao submeter', async () => {
        const wrapper = mount(FeedbackModal, {
            props: defaultProps,
            global: {
                plugins: [createTestingPinia()],
                stubs
            }
        })

        await definirConteudoEditor(wrapper, '<p>Curto</p>')
        await wrapper.find('form').trigger('submit')

        expect(wrapper.text()).toContain('Descreva o problema com pelo menos 10 caracteres.')
        expect(wrapper.emitted('enviar')).toBeFalsy()
    })

    it('deve emitir enviar com os dados corretos', async () => {
        const wrapper = mount(FeedbackModal, {
            props: defaultProps,
            global: {
                plugins: [createTestingPinia()],
                stubs
            }
        })

        await definirConteudoEditor(wrapper, '<p>Esta é uma descrição longa o suficiente.</p>')
        // Para rádio buttons em Vue, precisamos encontrar o input correto e dar set no valor do v-model manualmente se o trigger não funcionar bem
        // Mas o setValue no rádio deve funcionar se o value bater.
        const radio = wrapper.find('[data-testid="feedback-tipo-sugestao"]')
        await radio.setValue(true)

        await wrapper.find('form').trigger('submit')

        expect(wrapper.emitted('enviar')).toBeTruthy()
        expect(wrapper.emitted('enviar')![0]).toEqual(['sugestao', '<p>Esta é uma descrição longa o suficiente.</p>'])
    })

    it('deve emitir removerCaptura ao clicar no botão correspondente', async () => {
        const blob = new Blob(['test'], {type: 'image/png'})
        const wrapper = mount(FeedbackModal, {
            props: {...defaultProps, captura: blob},
            global: {
                plugins: [createTestingPinia()],
                stubs
            }
        })

        await wrapper.find('[data-testid="feedback-btn-remover-captura"]').trigger('click')
        expect(wrapper.emitted('removerCaptura')).toBeTruthy()
    })

    it('não deve exibir a opção de elogio', () => {
        const wrapper = mount(FeedbackModal, {
            props: defaultProps,
            global: {
                plugins: [createTestingPinia()],
                stubs
            }
        })

        expect(wrapper.find('[data-testid="feedback-tipo-elogio"]').exists()).toBe(false)
    })
})
