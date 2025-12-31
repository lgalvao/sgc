import { mount } from '@vue/test-utils'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import ModalAcaoBloco from '@/components/ModalAcaoBloco.vue'
import { Modal } from 'bootstrap'

// Mock Bootstrap Modal
vi.mock('bootstrap', () => ({
  Modal: vi.fn(() => ({
    show: vi.fn(),
    hide: vi.fn(),
  }))
}))

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
    expect(wrapper.emitted('confirmar')).toBeFalsy()
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
