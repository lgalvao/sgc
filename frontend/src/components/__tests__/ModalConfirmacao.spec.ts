import {describe, expect, it} from 'vitest'
import {mount} from '@vue/test-utils'
import ModalConfirmacao from '../ModalConfirmacao.vue'

describe('ModalConfirmacao.vue', () => {
    const defaultProps = {
        modelValue: true,
        titulo: 'Título de Teste',
        mensagem: 'Mensagem de teste',
    }

    const globalOptions = {
        stubs: {
            BModal: {
                template: '<div class="b-modal-stub"><slot /><slot name="footer" /></div>',
                emits: ['shown']
            },
            BButton: {
                template: '<button class="b-button-stub"><slot /></button>'
            }
        }
    }

    it('renderiza corretamente com props padrão', () => {
        const wrapper = mount(ModalConfirmacao, {
            props: defaultProps,
            global: globalOptions
        })

        expect(wrapper.attributes('title')).toBe('Título de Teste')
        expect(wrapper.text()).toContain('Mensagem de teste')
        const confirmBtn = wrapper.find('[data-testid="btn-modal-confirmacao-confirmar"]')
        expect(confirmBtn.exists()).toBe(true)
    })

    it('aplica variant correta e ícone quando fornecida', () => {
        const wrapper = mount(ModalConfirmacao, {
            props: {
                ...defaultProps,
                variant: 'danger'
            },
            global: globalOptions
        })

        const confirmBtn = wrapper.find('[data-testid="btn-modal-confirmacao-confirmar"]')
        expect(confirmBtn.exists()).toBe(true)
        expect(wrapper.find('.bi-exclamation-triangle-fill').exists()).toBe(true)
    })

    it('foca no botão cancelar ao abrir se variant for danger', async () => {
        const wrapper = mount(ModalConfirmacao, {
            props: {
                ...defaultProps,
                variant: 'danger',
                modelValue: true
            },
            global: globalOptions,
            attachTo: document.body
        })

        const bModalComp = wrapper.findComponent('.b-modal-stub')

        if (bModalComp.exists()) {
            await (bModalComp as any).vm.$emit('shown')
        } else {
            (wrapper.vm as any).onShown()
        }

        const cancelBtn = wrapper.find('[data-testid="btn-modal-confirmacao-cancelar"]')
        expect(cancelBtn.element).toBe(document.activeElement)

        wrapper.unmount()
    })
})
