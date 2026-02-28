import {describe, expect, it} from 'vitest'
import {mount} from '@vue/test-utils'
import ConfirmacaoDisponibilizacaoModal from '../mapa/ConfirmacaoDisponibilizacaoModal.vue'

describe('ConfirmacaoDisponibilizacaoModal.vue', () => {
    const defaultProps = {
        mostrar: true,
        isRevisao: false,
    }

    const BModalStub = {
        name: 'BModal',
        template: '<div data-testid="modal-stub"><slot /><slot name="footer" /></div>',
        props: ['title', 'modelValue']
    }

    const globalOptions = {
        stubs: {
            BModal: BModalStub,
            BButton: {template: '<button><slot /></button>'}
        }
    }

    it('renderiza corretamente para cadastro normal (isRevisao=false)', () => {
        const wrapper = mount(ConfirmacaoDisponibilizacaoModal, {
            props: defaultProps,
            global: globalOptions
        })

        expect(wrapper.attributes('title')).toBe('Disponibilização do cadastro')
        expect(wrapper.text()).toContain('Confirma a finalização e a disponibilização do cadastro?')
    })

    it('renderiza corretamente para revisão (isRevisao=true)', () => {
        const wrapper = mount(ConfirmacaoDisponibilizacaoModal, {
            props: {...defaultProps, isRevisao: true},
            global: globalOptions
        })

        expect(wrapper.attributes('title')).toBe('Disponibilização da revisão do cadastro')
        expect(wrapper.text()).toContain('Confirma a finalização da revisão e a disponibilização do cadastro?')
    })

    it('emite evento "confirmar" ao clicar no botão confirmar', async () => {
        const wrapper = mount(ConfirmacaoDisponibilizacaoModal, {
            props: defaultProps,
            global: globalOptions
        })

        const confirmBtn = wrapper.find('[data-testid="btn-confirmar-disponibilizacao"]')
        await confirmBtn.trigger('click')

        expect(wrapper.emitted('confirmar')).toHaveLength(1)
    })

    it('emite evento "fechar" ao clicar em cancelar', async () => {
        const wrapper = mount(ConfirmacaoDisponibilizacaoModal, {
            props: defaultProps,
            global: globalOptions
        })

        const cancelBtn = wrapper.findAll('button').find(b => b.text() === 'Cancelar')
        await cancelBtn?.trigger('click')

        expect(wrapper.emitted('fechar')).toHaveLength(1)
    })
})
