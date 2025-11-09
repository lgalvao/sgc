import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createTestingPinia } from '@pinia/testing'
import VisMapa from '../VisMapa.vue'
import { useMapasStore } from '@/stores/mapas'
import { useRoute } from 'vue-router'

vi.mock('vue-router', () => ({
  useRoute: vi.fn(),
  useRouter: vi.fn(() => ({
    push: vi.fn()
  })),
  createRouter: vi.fn(() => ({
    beforeEach: vi.fn(),
    afterEach: vi.fn(),
  })),
  createWebHistory: vi.fn()
}))

describe('VisMapa.vue', () => {
  beforeEach(() => {
    vi.mocked(useRoute).mockReturnValue({
      params: {
        siglaUnidade: 'TEST',
        codProcesso: '1'
      }
    } as any)
  })

  it('renders correctly with data from store', async () => {
    const wrapper = mount(VisMapa, {
      global: {
        plugins: [
          createTestingPinia({
            createSpy: vi.fn,
            initialState: {
              mapas: {
                mapaVisualizacao: {
                  competencias: [
                    {
                      codigo: 1,
                      descricao: 'Competencia 1',
                      atividades: [
                        {
                          codigo: 1,
                          descricao: 'Atividade 1',
                          conhecimentos: [
                            { codigo: 1, descricao: 'Conhecimento 1' }
                          ]
                        }
                      ]
                    }
                  ]
                }
              },
              unidades: {
                unidades: [
                  { sigla: 'TEST', nome: 'Unidade de Teste' }
                ]
              }
            }
          })
        ]
      }
    })

    const store = useMapasStore()
    store.mapaVisualizacao = {
      sugestoes: '',
      competencias: [
        {
          codigo: 1,
          descricao: 'Competencia 1',
          atividades: [
            {
              codigo: 1,
              descricao: 'Atividade 1',
              conhecimentos: [
                { codigo: 1, descricao: 'Conhecimento 1' }
              ]
            }
          ]
        }
      ]
    }

    await wrapper.vm.$nextTick()

    expect(wrapper.find('[data-testid="competencia-descricao"]').text()).toBe('Competencia 1')
    expect(wrapper.find('.atividade-associada-descricao').text()).toBe('Atividade 1')
    expect(wrapper.find('[data-testid="conhecimento-item"]').text()).toBe('Conhecimento 1')
  })
})
