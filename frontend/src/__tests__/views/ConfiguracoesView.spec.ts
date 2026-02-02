import {beforeEach, describe, expect, it, vi} from 'vitest'
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

vi.mock('@/services/administradorService', () => ({
  listarAdministradores: vi.fn().mockResolvedValue([]),
  adicionarAdministrador: vi.fn().mockResolvedValue({}),
  removerAdministrador: vi.fn().mockResolvedValue({})
}))

describe('ConfiguracoesView.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('renderiza corretamente e carrega dados', async () => {
    const wrapper = mount(ConfiguracoesView, {
      global: {
        plugins: [createTestingPinia({
          createSpy: vi.fn,
          initialState: {
            configuracoes: {
              parametros: [],
              loading: false,
              error: null
            },
            perfil: {
              isAdmin: false
            }
          }
        })]
      }
    })

    const store = useConfiguracoesStore()
    await wrapper.vm.$nextTick()
    
    // Verifica se a ação foi chamada durante onMounted
    expect(store.carregarConfiguracoes).toHaveBeenCalled()
    expect(wrapper.text()).toContain('Configurações do Sistema')
  })

  it('atualiza valores do formulário da store', async () => {
    const wrapper = mount(ConfiguracoesView, {
      global: {
        plugins: [createTestingPinia({
          createSpy: vi.fn,
          stubActions: false, // Permite que os getters funcionem com os dados reais
          initialState: {
            configuracoes: {
              parametros: [
                  { codigo: 1, chave: 'DIAS_INATIVACAO_PROCESSO', descricao: 'Dias', valor: '45' },
                  { codigo: 2, chave: 'DIAS_ALERTA_NOVO', descricao: 'Dias', valor: '10' }
              ],
              loading: false,
              error: null
            },
            perfil: {
              isAdmin: false
            }
          }
        })]
      }
    })

    // Aguardar que onMounted processe e atualize o formulário
    await wrapper.vm.$nextTick()
    await new Promise(resolve => setTimeout(resolve, 10))

    const input1 = wrapper.find('#diasInativacao').element as HTMLInputElement
    const input2 = wrapper.find('#diasAlertaNovo').element as HTMLInputElement

    expect(input1.value).toBe('45')
    expect(input2.value).toBe('10')
  })
})
