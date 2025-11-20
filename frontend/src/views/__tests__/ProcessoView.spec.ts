import { mount, flushPromises } from '@vue/test-utils'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import ProcessoView from '@/views/ProcessoView.vue'
import { createTestingPinia } from '@pinia/testing'
import { useProcessosStore } from '@/stores/processos'
import { usePerfilStore } from '@/stores/perfil'
import { useNotificacoesStore } from '@/stores/notificacoes'
import { defineComponent } from 'vue'

// Mock services
import * as processoService from '@/services/processoService'

const { pushMock } = vi.hoisted(() => {
  return { pushMock: vi.fn() }
})

vi.mock('vue-router', () => ({
  useRoute: () => ({
    params: {
      codProcesso: '1'
    }
  }),
  useRouter: () => ({
    push: pushMock
  }),
  createRouter: () => ({
    beforeEach: vi.fn(),
    afterEach: vi.fn(),
    push: pushMock
  }),
  createWebHistory: vi.fn(),
  createMemoryHistory: vi.fn()
}))

vi.mock('@/services/processoService', () => ({
    obterDetalhesProcesso: vi.fn(),
    fetchSubprocessosElegiveis: vi.fn(),
    finalizarProcesso: vi.fn(),
    processarAcaoEmBloco: vi.fn(),
    fetchProcessosFinalizados: vi.fn() // used by store initial state or other actions
}))

// Stubs
const ProcessoDetalhesStub = {
    name: 'ProcessoDetalhes',
    props: ['descricao', 'tipo', 'situacao'],
    template: '<div data-testid="processo-detalhes">{{ descricao }}</div>'
}

const ProcessoAcoesStub = {
    name: 'ProcessoAcoes',
    props: ['mostrarBotoesBloco', 'perfil', 'situacaoProcesso'],
    template: '<div data-testid="processo-acoes"></div>',
    emits: ['aceitar-bloco', 'homologar-bloco', 'finalizar']
}

const ModalFinalizacaoStub = {
    name: 'ModalFinalizacao',
    props: ['mostrar', 'processoDescricao'],
    template: '<div v-if="mostrar" data-testid="modal-finalizacao"></div>',
    emits: ['fechar', 'confirmar']
}

const ModalAcaoBlocoStub = {
    name: 'ModalAcaoBloco',
    props: ['mostrar', 'tipo', 'unidades'],
    template: '<div v-if="mostrar" data-testid="modal-acao-bloco"></div>',
    emits: ['fechar', 'confirmar']
}

const TreeTableStub = {
    name: 'TreeTable',
    props: ['columns', 'data', 'title'],
    template: '<div data-testid="tree-table"></div>',
    emits: ['row-click']
}

describe('ProcessoView.vue', () => {
  let wrapper: any

  const mockProcesso = {
      codigo: 1,
      descricao: 'Test Process',
      tipo: 'MAPEAMENTO',
      situacao: 'EM_ANDAMENTO',
      unidades: [
          { codUnidade: 10, sigla: 'U1', nome: 'Unidade 1', situacaoSubprocesso: 'EM_ANDAMENTO', dataLimite: '2023-01-01', filhos: [] }
      ],
      resumoSubprocessos: []
  }

  const mockSubprocessosElegiveis = [
      { codSubprocesso: 1, unidadeNome: 'Test Unit', unidadeSigla: 'TU', situacao: 'NAO_INICIADO' }
  ]

  function createWrapper(customState = {}) {
    const wrapper = mount(ProcessoView, {
      global: {
        plugins: [
          createTestingPinia({
            stubActions: false,
            initialState: {
              perfil: {
                perfilSelecionado: 'ADMIN',
                unidadeSelecionada: 99,
              },
              ...customState
            },
          }),
        ],
        stubs: {
            ProcessoDetalhes: ProcessoDetalhesStub,
            ProcessoAcoes: ProcessoAcoesStub,
            ModalFinalizacao: ModalFinalizacaoStub,
            ModalAcaoBloco: ModalAcaoBlocoStub,
            TreeTable: TreeTableStub,
            BContainer: { template: '<div><slot /></div>' },
            BAlert: { template: '<div><slot /></div>' }
        }
      },
    })

    const processosStore = useProcessosStore()
    const perfilStore = usePerfilStore()
    const notificacoesStore = useNotificacoesStore()

    return { wrapper, processosStore, perfilStore, notificacoesStore }
  }

  beforeEach(() => {
    vi.clearAllMocks()
    vi.mocked(processoService.obterDetalhesProcesso).mockResolvedValue(mockProcesso as any)
    vi.mocked(processoService.fetchSubprocessosElegiveis).mockResolvedValue(mockSubprocessosElegiveis as any)
  })

  afterEach(() => {
      wrapper?.unmount()
  })

  it('deve renderizar detalhes do processo e buscar dados no mount', async () => {
    const { wrapper: w } = createWrapper()
    wrapper = w
    await flushPromises()

    expect(processoService.obterDetalhesProcesso).toHaveBeenCalledWith(1)
    expect(processoService.fetchSubprocessosElegiveis).toHaveBeenCalledWith(1)

    const detalhes = wrapper.findComponent(ProcessoDetalhesStub)
    expect(detalhes.props('descricao')).toBe('Test Process')
  })

  it('deve mostrar botões de ação quando houver subprocessos elegíveis', async () => {
    const { wrapper: w } = createWrapper()
    wrapper = w
    await flushPromises()

    const acoes = wrapper.findComponent(ProcessoAcoesStub)
    expect(acoes.props('mostrarBotoesBloco')).toBe(true)
  })

  it('deve navegar para detalhes da unidade ao clicar na tabela (ADMIN)', async () => {
      const { wrapper: w } = createWrapper({
          perfil: { perfilSelecionado: 'ADMIN', unidadeSelecionada: 99 }
      })
      wrapper = w
      await flushPromises()

      const treeTable = wrapper.findComponent(TreeTableStub)

      // Emulate row click
      const item = { id: 10, unidadeAtual: 'U1', clickable: true };
      await treeTable.vm.$emit('row-click', item);

      expect(pushMock).toHaveBeenCalledWith({
          name: 'Subprocesso',
          params: { codProcesso: '1', siglaUnidade: 'U1' }
      })
  });

  it('deve abrir modal de finalização', async () => {
      const { wrapper: w } = createWrapper()
      wrapper = w
      await flushPromises()

      const acoes = wrapper.findComponent(ProcessoAcoesStub)
      await acoes.vm.$emit('finalizar')

      const modal = wrapper.findComponent(ModalFinalizacaoStub)
      expect(modal.props('mostrar')).toBe(true)
  })

  it('deve confirmar finalização', async () => {
      const { wrapper: w, notificacoesStore } = createWrapper()
      wrapper = w
      await flushPromises()

      const modal = wrapper.findComponent(ModalFinalizacaoStub)
      await modal.vm.$emit('confirmar')
      await flushPromises()

      expect(processoService.finalizarProcesso).toHaveBeenCalledWith(1)
      expect(notificacoesStore.sucesso).toHaveBeenCalled()
      expect(pushMock).toHaveBeenCalledWith('/painel')
  })

  it('deve abrir modal de ação em bloco', async () => {
      const { wrapper: w } = createWrapper()
      wrapper = w
      await flushPromises()

      const acoes = wrapper.findComponent(ProcessoAcoesStub)
      await acoes.vm.$emit('aceitarBloco')

      const modal = wrapper.findComponent(ModalAcaoBlocoStub)
      expect(modal.props('mostrar')).toBe(true)
      expect(modal.props('tipo')).toBe('aceitar')
  })

  it('deve confirmar ação em bloco', async () => {
      const { wrapper: w } = createWrapper()
      wrapper = w
      await flushPromises()

      const modal = wrapper.findComponent(ModalAcaoBlocoStub)
      await modal.vm.$emit('confirmar', [{sigla: 'TU', selecionada: true}])

      expect(processoService.processarAcaoEmBloco).toHaveBeenCalledWith(expect.objectContaining({
          codProcesso: 1,
          unidades: ['TU'],
          tipoAcao: 'aceitar'
      }))

      // Verify re-fetch happens
      // It's called inside processarCadastroBloco which is awaited.
      expect(processoService.obterDetalhesProcesso).toHaveBeenCalledTimes(2) // One initial, one after action
  })
})
