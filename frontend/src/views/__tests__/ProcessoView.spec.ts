import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createTestingPinia } from '@pinia/testing'
import ProcessoView from '../ProcessoView.vue'
import { useProcessosStore } from '@/stores/processos'
import { useNotificacoesStore } from '@/stores/notificacoes'
import { createRouter, createWebHistory } from 'vue-router'
import ModalAcaoBloco from '@/components/ModalAcaoBloco.vue'
import ModalFinalizacao from '@/components/ModalFinalizacao.vue'
import ProcessoAcoes from '@/components/ProcessoAcoes.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', name: 'home', component: { template: '<div>Home</div>' } },
    { path: '/painel', name: 'Painel', component: { template: '<div>Painel</div>' } },
    { path: '/processo/:codProcesso', name: 'Processo', component: ProcessoView },
    { path: '/processo/:codProcesso/:siglaUnidade', name: 'Subprocesso', component: { template: '<div>Subprocesso</div>' } },
  ],
})

describe('Processo.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  const mountComponent = (initialState = {}) => {
    return mount(ProcessoView, {
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
                  unidades: [
                      { codUnidade: 10, sigla: 'U1', nome: 'Unidade 1', situacaoSubprocesso: 'EM_ANDAMENTO', dataLimite: '2023-01-01', filhos: [] }
                  ],
                },
                subprocessosElegiveis: [
                  { codSubprocesso: 1, unidadeNome: 'Test Unit', unidadeSigla: 'TU', situacao: 'NAO_INICIADO' },
                ],
                ...initialState['processos']
              },
              perfil: {
                perfilSelecionado: 'ADMIN',
                unidadeSelecionada: 99,
                ...initialState['perfil']
              },
            },
          }),
          router,
        ],
        stubs: {
            TreeTable: true,
            ProcessoDetalhes: true,
            ModalAcaoBloco: true,
            ModalFinalizacao: true
        }
      },
    })
  }

  it('renders process details', async () => {
    const wrapper = mountComponent()
    await router.push('/processo/1')
    await router.isReady()

    const detalhes = wrapper.findComponent({ name: 'ProcessoDetalhes' })
    expect(detalhes.props('descricao')).toBe('Test Process')
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
    const wrapper = mountComponent()
    await router.push('/processo/1')
    await router.isReady()

    expect(wrapper.findComponent(ProcessoAcoes).props('mostrarBotoesBloco')).toBe(true)
  })

  it('deve navegar para detalhes da unidade ao clicar na tabela (ADMIN)', async () => {
      const wrapper = mountComponent({
          perfil: { perfilSelecionado: 'ADMIN' }
      })
      await router.push('/processo/1')
      await router.isReady()

      const treeTable = wrapper.findComponent({ name: 'TreeTable' })

      const pushSpy = vi.spyOn(router, 'push')

      // Emulate row click with item structure used in ProcessoView logic
      const item = { id: 10, unidadeAtual: 'U1', clickable: true };
      treeTable.vm.$emit('row-click', item);

      expect(pushSpy).toHaveBeenCalledWith({
          name: 'Subprocesso',
          params: { codProcesso: '1', siglaUnidade: 'U1' }
      })
  });

  it('deve abrir modal de finalização', async () => {
      const wrapper = mountComponent()
      await router.push('/processo/1')
      await router.isReady()

      const acoes = wrapper.findComponent(ProcessoAcoes)
      acoes.vm.$emit('finalizar')
      await wrapper.vm.$nextTick()

      expect(wrapper.findComponent(ModalFinalizacao).props('mostrar')).toBe(true)
  })

  it('deve confirmar finalização', async () => {
      const wrapper = mountComponent()
      const store = useProcessosStore()
      const notificacoes = useNotificacoesStore()

      await router.push('/processo/1')
      await router.isReady()

      const modal = wrapper.findComponent(ModalFinalizacao)
      await modal.vm.$emit('confirmar')

      expect(store.finalizarProcesso).toHaveBeenCalledWith(1)
      expect(notificacoes.sucesso).toHaveBeenCalled()
  })

  it('deve abrir modal de ação em bloco', async () => {
      const wrapper = mountComponent()
      await router.push('/processo/1')
      await router.isReady()

      const acoes = wrapper.findComponent(ProcessoAcoes)
      acoes.vm.$emit('aceitarBloco')
      await wrapper.vm.$nextTick()

      const modal = wrapper.findComponent(ModalAcaoBloco)
      expect(modal.props('mostrar')).toBe(true)
      expect(modal.props('tipo')).toBe('aceitar')
  })

  it('deve confirmar ação em bloco', async () => {
      const wrapper = mountComponent()
      const store = useProcessosStore()

      await router.push('/processo/1')
      await router.isReady()

      const modal = wrapper.findComponent(ModalAcaoBloco)
      await modal.vm.$emit('confirmar', [{sigla: 'TU', selecionada: true}])

      expect(store.processarCadastroBloco).toHaveBeenCalled()
  })
})
