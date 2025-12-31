import { mount } from '@vue/test-utils'
import { describe, it, expect, vi } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import ConfiguracoesView from '@/views/ConfiguracoesView.vue'
import { useConfiguracoesStore } from '@/stores/configuracoes'

vi.mock('@/stores/feedback', () => ({
  useNotificacoesStore: vi.fn(() => ({
    show: vi.fn()
  }))
}))

describe('ConfiguracoesView.vue', () => {
  it('renders correctly and loads data', async () => {
    const wrapper = mount(ConfiguracoesView, {
      global: {
        plugins: [createTestingPinia({
          createSpy: vi.fn,
          initialState: {
            configuracoes: {
              parametros: [],
              loading: false
            }
          }
        })]
      }
    })

    const store = useConfiguracoesStore()
    expect(store.carregarConfiguracoes).toHaveBeenCalled()
    expect(wrapper.find('h5').text()).toBe('Configurações do Sistema')
  })

  it('updates form values from store', async () => {
    const wrapper = mount(ConfiguracoesView, {
      global: {
        plugins: [createTestingPinia({
          createSpy: vi.fn,
          stubActions: false // Allow store methods to run
        })]
      }
    })

    const store = useConfiguracoesStore()
    // Mock get methods
    store.getDiasInativacaoProcesso = vi.fn().mockReturnValue(45)
    store.getDiasAlertaNovo = vi.fn().mockReturnValue(10)

    // Trigger update (simulate mounted or reload)
    await wrapper.vm.$nextTick()

    // Check if form updated (need to expose or find inputs)
    // Since we can't easily access reactive setup state without exposing, checking inputs
    await wrapper.find('button[class*="btn-light"]').trigger('click') // Recarregar

    // Wait for async operations
    await new Promise(resolve => setTimeout(resolve, 0));

    const input1 = wrapper.find('#diasInativacao').element as HTMLInputElement
    const input2 = wrapper.find('#diasAlertaNovo').element as HTMLInputElement

    expect(input1.value).toBe('45')
    expect(input2.value).toBe('10')
  })
})
