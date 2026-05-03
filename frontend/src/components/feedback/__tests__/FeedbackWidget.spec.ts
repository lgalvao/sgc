import {describe, expect, it} from 'vitest'
import {mount} from '@vue/test-utils'
import FeedbackButton from '../FeedbackButton.vue'
import FeedbackModal from '../FeedbackModal.vue'

describe('FeedbackButton.vue', () => {
    it('deve renderizar o botão com ícone padrão', () => {
        const wrapper = mount(FeedbackButton)
        expect(wrapper.find('[data-testid="feedback-btn"]').exists()).toBe(true)
        expect(wrapper.find('.bi-chat-square-text').exists()).toBe(true)
    })

    it('deve emitir evento click ao clicar', async () => {
        const wrapper = mount(FeedbackButton)
        await wrapper.find('[data-testid="feedback-btn"]').trigger('click')
        expect(wrapper.emitted('click')).toBeTruthy()
    })

    it('deve desabilitar botão no estado carregando', () => {
        const wrapper = mount(FeedbackButton, {props: {estado: 'carregando'}})
        expect(wrapper.find('button').attributes('disabled')).toBeDefined()
    })

    it('deve exibir ícone de sucesso no estado sucesso', () => {
        const wrapper = mount(FeedbackButton, {props: {estado: 'sucesso'}})
        expect(wrapper.find('.bi-check-lg').exists()).toBe(true)
    })

    it('deve exibir ícone de erro no estado erro', () => {
        const wrapper = mount(FeedbackButton, {props: {estado: 'erro'}})
        expect(wrapper.find('.bi-exclamation-triangle').exists()).toBe(true)
    })
})

describe('FeedbackModal.vue', () => {
    const stubs = {
        BModal: {
            template: '<div v-if="modelValue" data-testid="feedback-modal"><slot /></div>',
            props: ['modelValue', 'title', 'hideFooter'],
        },
        BFormGroup: {
            template: '<div><slot /><slot name="invalid-feedback" /><slot name="label" /></div>',
        },
        BFormRadioGroup: {
            template: '<div data-testid="feedback-tipo"></div>',
            props: ['modelValue', 'options'],
        },
        BFormTextarea: {
            template: '<textarea data-testid="feedback-nota" @input="$emit(\'update:modelValue\', $event.target.value)"></textarea>',
            props: ['modelValue', 'rows', 'state', 'maxlength', 'placeholder'],
            emits: ['update:modelValue'],
        },
    }

    it('deve exibir modal quando visivel=true', () => {
        const wrapper = mount(FeedbackModal, {
            props: {visivel: true, captura: null, enviando: false},
            global: {stubs},
        })
        expect(wrapper.find('[data-testid="feedback-modal"]').exists()).toBe(true)
    })

    it('não deve exibir modal quando visivel=false', () => {
        const wrapper = mount(FeedbackModal, {
            props: {visivel: false, captura: null, enviando: false},
            global: {stubs},
        })
        expect(wrapper.find('[data-testid="feedback-modal"]').exists()).toBe(false)
    })

    it('deve emitir evento enviar com tipo e nota ao submeter formulário válido', async () => {
        const wrapper = mount(FeedbackModal, {
            props: {visivel: true, captura: null, enviando: false},
            global: {stubs},
        })

        const textarea = wrapper.find('textarea[data-testid="feedback-nota"]')
        await textarea.setValue('Nota com pelo menos dez caracteres')
        await wrapper.find('form').trigger('submit')

        expect(wrapper.emitted('enviar')).toBeTruthy()
        const args = wrapper.emitted('enviar')![0] as [string, string]
        expect(args[0]).toBe('bug')
        expect(args[1]).toBe('Nota com pelo menos dez caracteres')
    })

    it('não deve emitir enviar quando nota for muito curta', async () => {
        const wrapper = mount(FeedbackModal, {
            props: {visivel: true, captura: null, enviando: false},
            global: {stubs},
        })

        const textarea = wrapper.find('textarea[data-testid="feedback-nota"]')
        await textarea.setValue('curta')
        await wrapper.find('form').trigger('submit')

        expect(wrapper.emitted('enviar')).toBeFalsy()
        expect(wrapper.text()).toContain('pelo menos 10 caracteres')
    })

    it('deve emitir fechar ao clicar no botão cancelar', async () => {
        const wrapper = mount(FeedbackModal, {
            props: {visivel: true, captura: null, enviando: false},
            global: {stubs},
        })
        await wrapper.find('button[type="button"]').trigger('click')
        expect(wrapper.emitted('fechar')).toBeTruthy()
    })

    it('deve exibir prévia quando captura está disponível', () => {
        const captura = new Blob(['fake'], {type: 'image/webp'})
        const wrapper = mount(FeedbackModal, {
            props: {visivel: true, captura, enviando: false},
            global: {stubs},
        })
        expect(wrapper.find('img').exists()).toBe(true)
    })
})
