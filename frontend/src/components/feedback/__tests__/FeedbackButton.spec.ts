import {describe, it, expect} from 'vitest'
import {mount} from '@vue/test-utils'
import FeedbackButton from '../FeedbackButton.vue'

describe('FeedbackButton.vue', () => {
    it('deve renderizar com estado normal por padrão', () => {
        const wrapper = mount(FeedbackButton, {
            global: {
                directives: {
                    'b-tooltip': {}
                }
            }
        })
        const btn = wrapper.find('[data-testid="feedback-btn"]')
        expect(btn.classes()).toContain('btn-primary')
        expect(wrapper.find('i').classes()).toContain('bi-chat-square-text')
    })

    it('deve mudar ícone e desabilitar em estado de carregamento', () => {
        const wrapper = mount(FeedbackButton, {
            props: {estado: 'carregando'},
            global: {
                directives: {
                    'b-tooltip': {}
                }
            }
        })
        const btn = wrapper.find('[data-testid="feedback-btn"]')
        expect(btn.attributes('disabled')).toBeDefined()
        expect(wrapper.find('i').classes()).toContain('bi-hourglass-split')
    })

    it('deve usar variante success em estado de sucesso', () => {
        const wrapper = mount(FeedbackButton, {
            props: {estado: 'sucesso'},
            global: {
                directives: {
                    'b-tooltip': {}
                }
            }
        })
        expect(wrapper.find('[data-testid="feedback-btn"]').classes()).toContain('btn-success')
        expect(wrapper.find('i').classes()).toContain('bi-check-lg')
    })

    it('deve usar variante danger em estado de erro', () => {
        const wrapper = mount(FeedbackButton, {
            props: {estado: 'erro'},
            global: {
                directives: {
                    'b-tooltip': {}
                }
            }
        })
        expect(wrapper.find('[data-testid="feedback-btn"]').classes()).toContain('btn-danger')
        expect(wrapper.find('i').classes()).toContain('bi-exclamation-triangle')
    })

    it('deve emitir clique quando clicado', async () => {
        const wrapper = mount(FeedbackButton, {
            global: {
                directives: {
                    'b-tooltip': {}
                }
            }
        })
        await wrapper.find('[data-testid="feedback-btn"]').trigger('click')
        expect(wrapper.emitted('click')).toBeTruthy()
    })
})
