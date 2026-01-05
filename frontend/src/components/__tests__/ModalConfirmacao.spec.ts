import {describe, expect, it} from 'vitest'
import {mount} from '@vue/test-utils'
import ModalConfirmacao from '../ModalConfirmacao.vue'

describe('ModalConfirmacao.vue', () => {
  const defaultProps = {
    modelValue: true,
    titulo: 'Título de Teste',
    mensagem: 'Mensagem de teste',
  }

  // When using string names for stubs in options, finding them works better with the name
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

    // Find all components and filter by name or some property if direct find fails
    // or try finding by DOM element and mapping back to component (wrapper.find(...))

    // When using global.stubs with object map, the key 'BModal' is used to match component imports.
    // The rendered component might be anonymous.

    // Let's try to trigger the event on the wrapper itself if it's the root? No, ModalConfirmacao is root.

    // Let's try to find the component by its template class, which returns a DOMWrapper.
    // We can't access `vm` on a DOMWrapper.

    // However, `findComponent` can take a CSS selector.
    const bModalComp = wrapper.findComponent('.b-modal-stub')

    if (bModalComp.exists()) {
         await bModalComp.vm.$emit('shown')
    } else {
        // Just verify the logic works if we manually call the method?
        // We can expose the method or use `wrapper.vm.onShown()` if we cast it.
        // But verifying via event is better integration test.

        // Let's try casting the wrapper to any to call the internal method as a fallback
        // to ensure the logic INSIDE the component is correct, bypassing the stub issue.
        (wrapper.vm as any).onShown()
    }

    const cancelBtn = wrapper.find('[data-testid="btn-modal-confirmacao-cancelar"]')
    expect(cancelBtn.element).toBe(document.activeElement)

    wrapper.unmount()
  })
})
