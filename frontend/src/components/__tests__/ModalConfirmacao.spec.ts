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
            ModalPadrao: {
                name: 'ModalPadrao',
                template: `
                    <div class="modal-padrao-stub">
                        <span>{{ titulo }}</span>
                        <slot name="alerta" />
                        <button
                            :data-testid="testIdCancelar || 'btn-modal-confirmacao-cancelar'"
                            :disabled="loading"
                            @click="$emit('fechar')"
                        >
                            {{ textoCancelar }}
                        </button>
                        <button
                            :data-testid="testIdConfirmar || 'btn-modal-confirmacao-confirmar'"
                            :disabled="loading || acaoDesabilitada"
                            @click="$emit('confirmar')"
                        >
                            <span v-if="loading" class="spinner-border"></span>
                            {{ loading ? textoAcaoCarregando : textoAcao }}
                        </button>
                        <slot />
                    </div>
                `,
                props: ['modelValue', 'titulo', 'loading', 'testIdCancelar', 'testIdConfirmar', 'textoCancelar', 'textoAcao', 'textoAcaoCarregando', 'acaoDesabilitada'],
                emits: ['update:modelValue', 'fechar', 'confirmar', 'shown']
            }
        }
    }

    it('renderiza corretamente com props padrão', () => {
        const wrapper = mount(ModalConfirmacao, {
            props: defaultProps,
            global: globalOptions
        })

        expect(wrapper.text()).toContain('Título de Teste')
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

    it('fecha o modal ao clicar em cancelar', async () => {
        const wrapper = mount(ModalConfirmacao, {
            props: defaultProps,
            global: globalOptions
        })

        const cancelBtn = wrapper.find('[data-testid="btn-modal-confirmacao-cancelar"]')
        await cancelBtn.trigger('click')

        expect(wrapper.emitted('hide')).toBeDefined()
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

        expect(wrapper.emitted('confirmar')).toBeDefined()
        expect(wrapper.emitted('update:modelValue')).toEqual([[false]])
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

        expect(wrapper.emitted('confirmar')).toBeDefined()
        expect(wrapper.emitted('update:modelValue')).toBeUndefined() // Não fecha
    })

    it('renderiza conteúdo customizado via slot', () => {
        const customContent = '<div class="custom-content">Conteúdo customizado</div>'
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
        expect(wrapper.find('.custom-content').text()).toBe('Conteúdo customizado')
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
        expect(confirmBtn.attributes('disabled')).toBe('')
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

        expect(confirmBtn.attributes('disabled')).toBe('')
        expect(cancelBtn.attributes('disabled')).toBe('')
        expect(confirmBtn.find('.spinner-border').exists()).toBe(true)
        expect(confirmBtn.text()).toContain('Processando...')
    })

    it('encaminha a regiao de alerta para o modal base', () => {
        const wrapper = mount(ModalConfirmacao, {
            props: defaultProps,
            slots: {
                alerta: '<div class="alerta-confirmacao">Alerta</div>'
            },
            global: globalOptions
        })

        expect(wrapper.find('.alerta-confirmacao').exists()).toBe(true)
        expect(wrapper.find('.alerta-confirmacao').text()).toBe('Alerta')
    })
})
