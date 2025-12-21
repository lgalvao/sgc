import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import ModalConfirmacao from '../ModalConfirmacao.vue'
import { BButton } from 'bootstrap-vue-next'

describe('ModalConfirmacao.vue', () => {
  const defaultProps = {
    modelValue: true,
    titulo: 'Título de Teste',
    mensagem: 'Mensagem de teste',
  }

  const BModalStub = {
    name: 'BModal',
    template: '<div data-testid="modal-stub"><slot /><slot name="footer" /></div>',
    props: ['title', 'modelValue']
  }

  const globalOptions = {
    stubs: {
      BModal: BModalStub,
      BButton: BButton
    }
  }

  it('renderiza corretamente com props padrão', () => {
    const wrapper = mount(ModalConfirmacao, {
      props: defaultProps,
      global: globalOptions
    })

    // Como BModal é o elemento raiz e está stubbed (automático ou manual),
    // verificamos os atributos no próprio wrapper ou no elemento raiz
    expect(wrapper.attributes('title')).toBe('Título de Teste')
    expect(wrapper.text()).toContain('Mensagem de teste')
    const confirmBtn = wrapper.find('[data-testid="btn-modal-confirmacao-confirmar"]')
    expect(confirmBtn.classes()).toContain('btn-primary') // Default variant
  })

  it('aplica variant correta quando fornecida', () => {
    const wrapper = mount(ModalConfirmacao, {
      props: {
        ...defaultProps,
        variant: 'danger'
      },
      global: globalOptions
    })

    const confirmBtn = wrapper.find('[data-testid="btn-modal-confirmacao-confirmar"]')
    expect(confirmBtn.classes()).toContain('btn-danger')
  })

  it('emite evento "confirmar" e fecha ao clicar em confirmar', async () => {
    const wrapper = mount(ModalConfirmacao, {
      props: defaultProps,
      global: globalOptions
    })

    const confirmBtn = wrapper.find('[data-testid="btn-modal-confirmacao-confirmar"]')
    await confirmBtn.trigger('click')

    expect(wrapper.emitted('confirmar')).toHaveLength(1)
    expect(wrapper.emitted('update:modelValue')).toHaveLength(1)
    expect(wrapper.emitted('update:modelValue')![0]).toEqual([false])
  })

  it('emite update:modelValue false ao clicar em cancelar', async () => {
    const wrapper = mount(ModalConfirmacao, {
      props: defaultProps,
      global: globalOptions
    })

    // Botão cancelar é o primeiro (secondary)
    const cancelBtn = wrapper.findAll('button').filter(b => b.text() === 'Cancelar')[0]
    await cancelBtn.trigger('click')

    expect(wrapper.emitted('update:modelValue')).toHaveLength(1)
    expect(wrapper.emitted('update:modelValue')![0]).toEqual([false])
    expect(wrapper.emitted('confirmar')).toBeUndefined()
  })
})

// Debugging
// console.log(wrapper.html())
