import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createTestingPinia } from '@pinia/testing'
import ProcessoView from '../ProcessoView.vue'
import { useProcessosStore } from '@/stores/processos'
import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', name: 'home', component: { template: '<div>Home</div>' } },
    { path: '/painel', name: 'Painel', component: { template: '<div>Painel</div>' } },
    { path: '/processo/:codProcesso', name: 'Processo', component: ProcessoView },
    { path: '/subprocesso/:codProcesso/:siglaUnidade', name: 'Subprocesso', component: { template: '<div>Subprocesso</div>' } },
  ],
})

describe('Processo.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('renders process details', async () => {
    const wrapper = mount(ProcessoView, {
      global: {
        plugins: [
          createTestingPinia({
            createSpy: vi.fn,
            initialState: {
              processos: {
                processoDetalhe: {
                  codigo: 1,
                  descricao: 'Test Process',
                  tipo: 'MAPEAMENTO',
                  situacao: 'EM_ANDAMENTO',
                  unidades: [],
                },
              },
              perfil: {
                perfilSelecionado: 'ADMIN',
              },
            },
          }),
          router,
        ],
      },
    })

    await router.push('/processo/1')
    await router.isReady()

    expect(wrapper.text()).toContain('Test Process')
  })

  it('fetches process details on mount', async () => {
    const pinia = createTestingPinia({ createSpy: vi.fn })
    const processosStore = useProcessosStore(pinia)
    processosStore.fetchProcessoDetalhe = vi.fn()
    processosStore.fetchSubprocessosElegiveis = vi.fn()

    mount(ProcessoView, {
      global: {
        plugins: [pinia, router],
      },
    })

    await router.push('/processo/1')
    await router.isReady()

    expect(processosStore.fetchProcessoDetalhe).toHaveBeenCalledWith(1)
    expect(processosStore.fetchSubprocessosElegiveis).toHaveBeenCalledWith(1)
  })

  it('shows action buttons when there are eligible subprocesses', async () => {
    const wrapper = mount(ProcessoView, {
      global: {
        plugins: [
          createTestingPinia({
            createSpy: vi.fn,
            initialState: {
              processos: {
                processoDetalhe: {
                  codigo: 1,
                  descricao: 'Test Process',
                  tipo: 'MAPEAMENTO',
                  situacao: 'EM_ANDAMENTO',
                  unidades: [],
                },
                subprocessosElegiveis: [
                  { codSubprocesso: 1, unidadeNome: 'Test Unit', unidadeSigla: 'TU', situacao: 'NAO_INICIADO' },
                ],
              },
              perfil: {
                perfilSelecionado: 'ADMIN',
              },
            },
          }),
          router,
        ],
      },
    })

    await router.push('/processo/1')
    await router.isReady()

    expect(wrapper.findComponent({ name: 'ProcessoAcoes' }).props('mostrarBotoesBloco')).toBe(true)
  })
})
