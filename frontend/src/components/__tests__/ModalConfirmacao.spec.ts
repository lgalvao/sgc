import {describe, expect, it} from 'vitest'
import {mount} from '@vue/test-utils'
import ModalConfirmacao from '../comum/ModalConfirmacao.vue'

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
                template: '<button class="b-button-stub" @click="$emit(\'click\')" :disabled="disabled"><slot /></button>',
                props: ['disabled', 'variant'],
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
        expect(confirmBtn.text()).toBe('Confirmar')
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

    it('emite confirmar e fecha o modal ao clicar em confirmar (se autoClose=true)', async () => {
        const wrapper = mount(ModalConfirmacao, {
            props: {
                ...defaultProps,
                autoClose: true // Default
            },
            global: globalOptions
        })

        const confirmBtn = wrapper.find('[data-testid="btn-modal-confirmacao-confirmar"]')
        await confirmBtn.trigger('click')

        expect(wrapper.emitted('confirmar')).toBeTruthy()
        expect(wrapper.emitted('update:modelValue')).toBeTruthy() // Fecha
    })

    it('emite confirmar mas NAO fecha o modal se autoClose=false', async () => {
        const wrapper = mount(ModalConfirmacao, {
            props: {
                ...defaultProps,
                autoClose: false
            },
            global: globalOptions
        })

        const confirmBtn = wrapper.find('[data-testid="btn-modal-confirmacao-confirmar"]')
        await confirmBtn.trigger('click')

        expect(wrapper.emitted('confirmar')).toBeTruthy()
        expect(wrapper.emitted('update:modelValue')).toBeFalsy() // Não fecha
    })

    it('renderiza conteúdo customizado via slot', () => {
        const customContent = '<div class="custom-content">Conteúdo Customizado</div>'
        const wrapper = mount(ModalConfirmacao, {
            props: {
                ...defaultProps,
                mensagem: undefined
            },
            slots: {
                default: customContent
            },
            global: globalOptions
        })

        expect(wrapper.find('.custom-content').exists()).toBe(true)
        expect(wrapper.find('.custom-content').text()).toBe('Conteúdo Customizado')
    })

    it('usa titulos customizados para os botoes', () => {
        const wrapper = mount(ModalConfirmacao, {
            props: {
                ...defaultProps,
                okTitle: 'Sim, eu quero',
                cancelTitle: 'Não, obrigado'
            },
            global: globalOptions
        })

        const confirmBtn = wrapper.find('[data-testid="btn-modal-confirmacao-confirmar"]')
        const cancelBtn = wrapper.find('[data-testid="btn-modal-confirmacao-cancelar"]')

        expect(confirmBtn.text()).toBe('Sim, eu quero')
        expect(cancelBtn.text()).toBe('Não, obrigado')
    })

    it('desabilita o botao confirmar se okDisabled=true', () => {
        const wrapper = mount(ModalConfirmacao, {
            props: {
                ...defaultProps,
                okDisabled: true
            },
            global: globalOptions
        })

        const confirmBtn = wrapper.find('[data-testid="btn-modal-confirmacao-confirmar"]')
        expect(confirmBtn.attributes('disabled')).toBeDefined()
    })

    it('exibe estado de carregamento corretamente', () => {
        const wrapper = mount(ModalConfirmacao, {
            props: {
                ...defaultProps,
                loading: true
            },
            global: globalOptions
        })

        const confirmBtn = wrapper.find('[data-testid="btn-modal-confirmacao-confirmar"]')
        const cancelBtn = wrapper.find('[data-testid="btn-modal-confirmacao-cancelar"]')

        expect(confirmBtn.attributes('disabled')).toBeDefined()
        expect(cancelBtn.attributes('disabled')).toBeDefined()
        expect(confirmBtn.find('.spinner-border').exists()).toBe(true)
        expect(confirmBtn.text()).toContain('Processando...')
    })
})
