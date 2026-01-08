import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
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
                props: ['modelValue', 'title'],
                emits: ['update:modelValue', 'hide', 'shown']
            },
            BButton: {
                template: '<button class="b-button-stub" @click="$emit(\'click\')"><slot /></button>',
                emits: ['click']
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
             await (wrapper.vm as any).onShown()
        }

        const cancelBtn = wrapper.find('[data-testid="btn-modal-confirmacao-cancelar"]')
        expect(cancelBtn.element).toBe(document.activeElement)

        wrapper.unmount()
    })

    it('não foca no botão cancelar se variant não for danger', async () => {
        const wrapper = mount(ModalConfirmacao, {
            props: {
                ...defaultProps,
                variant: 'primary',
                modelValue: true
            },
            global: globalOptions,
            attachTo: document.body
        })

        const bModalComp = wrapper.findComponent('.b-modal-stub')
        if (bModalComp.exists()) {
             await (bModalComp as any).vm.$emit('shown')
        } else {
             await (wrapper.vm as any).onShown()
        }

        const cancelBtn = wrapper.find('[data-testid="btn-modal-confirmacao-cancelar"]')
        expect(cancelBtn.element).not.toBe(document.activeElement)

        wrapper.unmount()
    })

    it('fecha o modal ao clicar em cancelar', async () => {
        const wrapper = mount(ModalConfirmacao, {
            props: defaultProps,
            global: globalOptions
        })

        const cancelBtn = wrapper.find('[data-testid="btn-modal-confirmacao-cancelar"]')
        await cancelBtn.trigger('click')

        expect(wrapper.emitted('update:modelValue')).toBeTruthy()
        expect(wrapper.emitted('update:modelValue')![0]).toEqual([false])
    })

    it('emite confirmar e fecha o modal ao clicar em confirmar', async () => {
        const wrapper = mount(ModalConfirmacao, {
            props: defaultProps,
            global: globalOptions
        })

        const confirmBtn = wrapper.find('[data-testid="btn-modal-confirmacao-confirmar"]')
        await confirmBtn.trigger('click')

        expect(wrapper.emitted('confirmar')).toBeTruthy()
        expect(wrapper.emitted('update:modelValue')).toBeTruthy()
        expect(wrapper.emitted('update:modelValue')![0]).toEqual([false])
    })

    it('renderiza conteúdo customizado via slot', () => {
        const customContent = '<div class="custom-content">Conteúdo Customizado</div>'
        const wrapper = mount(ModalConfirmacao, {
            props: {
                ...defaultProps,
                mensagem: undefined // Para garantir que não renderiza a mensagem padrão
            },
            slots: {
                default: customContent
            },
            global: globalOptions
        })

        expect(wrapper.find('.custom-content').exists()).toBe(true)
        expect(wrapper.find('.custom-content').text()).toBe('Conteúdo Customizado')
    })
})
