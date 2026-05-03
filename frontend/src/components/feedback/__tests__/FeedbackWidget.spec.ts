import {describe, it, expect, vi, beforeEach} from 'vitest'
import {mount} from '@vue/test-utils'
import FeedbackWidget from '../FeedbackWidget.vue'
import {createTestingPinia} from '@pinia/testing'
import * as useFeedbackModule from '@/composables/useFeedback'
import * as bootstrapVueNext from 'bootstrap-vue-next'

// Mock do composable useFeedback
const mockUseFeedback = {
    captura: {value: null},
    enviando: {value: false},
    capturarTela: vi.fn(),
    enviarFeedback: vi.fn(),
    removerCaptura: vi.fn(),
}

vi.spyOn(useFeedbackModule, 'useFeedback').mockReturnValue(mockUseFeedback as any)

// Mock do useToast e outros componentes do bootstrap-vue-next
const mockCriarToast = vi.fn()
vi.mock('bootstrap-vue-next', async () => {
    const actual = await vi.importActual('bootstrap-vue-next')
    return {
        ...(actual as any),
        useToast: vi.fn(() => ({create: mockCriarToast}))
    }
})

describe('FeedbackWidget.vue', () => {
    beforeEach(() => {
        vi.clearAllMocks()
        mockUseFeedback.captura.value = null
        mockUseFeedback.enviando.value = false
    })

    it('deve renderizar o botão de feedback', () => {
        const wrapper = mount(FeedbackWidget, {
            global: {
                plugins: [createTestingPinia()],
                stubs: {
                    Teleport: true,
                    BOrchestrator: true,
                    FeedbackButton: true,
                    FeedbackModal: true
                }
            }
        })
        expect(wrapper.findComponent({name: 'FeedbackButton'}).exists()).toBe(true)
    })

    it('deve abrir o modal ao clicar no botão', async () => {
        const wrapper = mount(FeedbackWidget, {
            global: {
                plugins: [createTestingPinia()],
                stubs: {
                    Teleport: true,
                    BOrchestrator: true,
                    FeedbackButton: {
                        template: '<button @click="$emit(\'click\')">Feedback</button>'
                    },
                    FeedbackModal: true
                }
            }
        })
        
        await wrapper.find('button').trigger('click')
        
        expect(mockUseFeedback.capturarTela).toHaveBeenCalled()
        // O modal deve estar visível (verificando a prop do componente stub)
        const modal = wrapper.findComponent({name: 'FeedbackModal'})
        expect(modal.props('visivel')).toBe(true)
    })

    it('deve fechar o modal e mostrar toast ao enviar com sucesso', async () => {
        mockUseFeedback.enviarFeedback.mockResolvedValueOnce(undefined)
        
        const wrapper = mount(FeedbackWidget, {
            global: {
                plugins: [createTestingPinia()],
                stubs: {
                    Teleport: true,
                    BOrchestrator: true,
                    FeedbackButton: true,
                    FeedbackModal: {
                        template: '<div />',
                        props: ['visivel']
                    }
                }
            }
        })
        
        // Simular o evento 'enviar' do modal
        const modal = wrapper.findComponent({name: 'FeedbackModal'})
        await modal.vm.$emit('enviar', 'ELOGIO', '5')
        
        expect(mockUseFeedback.enviarFeedback).toHaveBeenCalledWith('ELOGIO', '5')
        expect(mockCriarToast).toHaveBeenCalledWith(expect.objectContaining({
            props: expect.objectContaining({variant: 'success'})
        }))
    })

    it('deve mostrar toast de erro se o envio falhar', async () => {
        mockUseFeedback.enviarFeedback.mockRejectedValueOnce(new Error('Falha'))
        
        const wrapper = mount(FeedbackWidget, {
            global: {
                plugins: [createTestingPinia()],
                stubs: {
                    Teleport: true,
                    BOrchestrator: true,
                    FeedbackButton: true,
                    FeedbackModal: {
                        template: '<div />',
                        props: ['visivel']
                    }
                }
            }
        })
        
        const modal = wrapper.findComponent({name: 'FeedbackModal'})
        await modal.vm.$emit('enviar', 'BUG', '1')
        
        expect(mockCriarToast).toHaveBeenCalledWith(expect.objectContaining({
            props: expect.objectContaining({variant: 'danger'})
        }))
    })
})
