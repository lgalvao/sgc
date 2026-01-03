import { mount } from '@vue/test-utils'
import { describe, it, expect, vi } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import ModalAcaoBloco from '@/components/ModalAcaoBloco.vue'

// Mock Bootstrap Modal globally for this test file
// Use a factory function that returns the class to avoid hoisting issues
vi.mock('bootstrap', () => {
  return {
    Modal: class {
      static instances: any[] = [];
      element: any;
      show = vi.fn();
      hide = vi.fn();
      dispose = vi.fn();

      constructor(element: any) {
        this.element = element;
      }
    }
  }
})

describe('ModalAcaoBloco.vue', () => {
  const defaultProps = {
    id: 'test-modal',
    titulo: 'Teste',
    texto: 'Texto teste',
    rotuloBotao: 'Confirmar',
    unidades: [
      { codigo: 1, sigla: 'U1', nome: 'Unidade 1', situacao: 'Situação 1', selecionada: true },
      { codigo: 2, sigla: 'U2', nome: 'Unidade 2', situacao: 'Situação 2', selecionada: true }
    ],
    unidadesPreSelecionadas: [1, 2]
  }

  it('renders correctly', () => {
    const wrapper = mount(ModalAcaoBloco, {
      props: defaultProps,
      global: {
        plugins: [createTestingPinia({ createSpy: vi.fn })]
      }
    })
    expect(wrapper.find('.modal-title').text()).toBe('Teste')
    // Note: The new component template wraps text in <p class="mb-3">
    expect(wrapper.find('.modal-body p').text()).toBe('Texto teste')
    expect(wrapper.findAll('tbody tr')).toHaveLength(2)
  })

  it('emits confirmar event with selected ids', async () => {
    const wrapper = mount(ModalAcaoBloco, {
      props: defaultProps,
      global: {
        plugins: [createTestingPinia({ createSpy: vi.fn })]
      }
    })

    await wrapper.find('button.btn-primary').trigger('click')

    expect(wrapper.emitted('confirmar')).toBeTruthy()
    expect(wrapper.emitted('confirmar')![0][0]).toEqual({
      ids: [1, 2],
      dataLimite: undefined
    })
  })

  it('validates required date limit when configured', async () => {
    const propsWithDate = { ...defaultProps, mostrarDataLimite: true }
    const wrapper = mount(ModalAcaoBloco, {
      props: propsWithDate,
      global: {
        plugins: [createTestingPinia({ createSpy: vi.fn })]
      }
    })

    // Try to confirm without date
    await wrapper.find('button.btn-primary').trigger('click')

    // In the new component logic, confirm() sets erro.value but doesn't emit 'confirmar' if validation fails
    expect(wrapper.emitted('confirmar')).toBeFalsy()

    // Check if error message is displayed
    expect(wrapper.text()).toContain('A data limite é obrigatória')

    // Set date and confirm
    const dateInput = wrapper.find('input[type="date"]')
    await dateInput.setValue('2024-12-31')
    await wrapper.find('button.btn-primary').trigger('click')

    expect(wrapper.emitted('confirmar')).toBeTruthy()
    expect(wrapper.emitted('confirmar')![0][0]).toEqual({
      ids: [1, 2],
      dataLimite: '2024-12-31'
    })
  })
})
