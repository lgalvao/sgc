import {describe, expect, it, vi} from 'vitest'
import {mount} from '@vue/test-utils'
import {createTestingPinia} from '@pinia/testing'
import ConfiguracoesView from '@/views/ConfiguracoesView.vue'
import {useConfiguracoesStore} from '@/stores/configuracoes'

// Mock dependencies
const { mockApiClient } = vi.hoisted(() => {
    return {
        mockApiClient: {
            get: vi.fn().mockResolvedValue({ data: [] }),
            post: vi.fn().mockResolvedValue({ data: {} }),
        }
    }
});

vi.mock("@/axios-setup", () => ({
    apiClient: mockApiClient,
    default: mockApiClient,
}));

vi.mock('@/stores/feedback', () => ({
  useFeedbackStore: vi.fn(() => ({
    show: vi.fn()
  })),
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
              loading: false,
              error: null
            }
          }
        })]
      }
    })

    const store = useConfiguracoesStore()
    expect(store.carregarConfiguracoes).toHaveBeenCalled()
    expect(wrapper.text()).toContain('Configurações do Sistema')
  })

  it('updates form values from store', async () => {
    const wrapper = mount(ConfiguracoesView, {
      global: {
        plugins: [createTestingPinia({
          createSpy: vi.fn,
          initialState: {
            configuracoes: {
              parametros: [
                  { id: 1, chave: 'DIAS_INATIVACAO_PROCESSO', valor: '45' },
                  { id: 2, chave: 'DIAS_ALERTA_NOVO', valor: '10' }
              ],
              loading: false,
              error: null
            }
          },
          stubActions: false
        })],
        stubs: {
            // Fix: bind $attrs to ensure ID is passed to the input element
            BFormInput: {
                template: '<input v-bind="$attrs" :value="modelValue" @input="$emit(\'update:modelValue\', Number($event.target.value))" />',
                props: ['modelValue']
            }
        }
      }
    })

    await new Promise(resolve => setTimeout(resolve, 0));

    // Now find by ID should work because $attrs passes the ID to the root element of the stub
    const input1 = wrapper.find('#diasInativacao').element as HTMLInputElement
    const input2 = wrapper.find('#diasAlertaNovo').element as HTMLInputElement

    expect(input1.value).toBe('45')
    expect(input2.value).toBe('10')
  })
})
