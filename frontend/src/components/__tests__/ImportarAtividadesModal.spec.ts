import { beforeEach, describe, expect, it, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import ImportarAtividadesModal from '../ImportarAtividadesModal.vue'
import { useProcessosStore } from '@/stores/processos'
import { useSubprocessosStore } from '@/stores/subprocessos'
import { useAtividadesStore } from '@/stores/atividades'
import { Atividade, Processo, SituacaoProcesso, Subprocesso, TipoProcesso } from '@/types/tipos'

// Mocking the stores
vi.mock('@/stores/processos')
vi.mock('@/stores/subprocessos')
vi.mock('@/stores/atividades')

const mockProcessosStore = {
  processos: [] as Processo[]
}
const mockSubprocessosStore = {
  getUnidadesDoProcesso: vi.fn()
}
const mockAtividadesStore = {
  fetchAtividadesPorSubprocesso: vi.fn(),
  getAtividadesPorSubprocesso: vi.fn()
}

describe('ImportarAtividadesModal.vue', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.mocked(useProcessosStore).mockReturnValue(mockProcessosStore as any)
    vi.mocked(useSubprocessosStore).mockReturnValue(mockSubprocessosStore as any)
    vi.mocked(useAtividadesStore).mockReturnValue(mockAtividadesStore as any)
    vi.clearAllMocks()
  })

  const mountComponent = (props = { mostrar: true }) => {
    return mount(ImportarAtividadesModal, { props })
  }

  it('should render correctly when `mostrar` is true', () => {
    const wrapper = mountComponent()
    expect(wrapper.find('.modal').exists()).toBe(true)
    expect(wrapper.find('.modal-title').text()).toBe('Importação de atividades')
  })

  it('should filter available processos correctly', () => {
    mockProcessosStore.processos = [
      { id: 1, tipo: TipoProcesso.MAPEAMENTO, situacao: SituacaoProcesso.FINALIZADO },
      { id: 2, tipo: TipoProcesso.REVISAO, situacao: SituacaoProcesso.FINALIZADO },
      { id: 3, tipo: TipoProcesso.DIAGNOSTICO, situacao: SituacaoProcesso.FINALIZADO },
      { id: 4, tipo: TipoProcesso.MAPEAMENTO, situacao: SituacaoProcesso.EM_ANDAMENTO }
    ] as Processo[]

    const wrapper = mountComponent()
    const vm = wrapper.vm as any
    expect(vm.processosDisponiveis.length).toBe(2)
    expect(vm.processosDisponiveis.map((p: Processo) => p.id)).toEqual([1, 2])
  })

  it('should load subprocessos when a processo is selected', async () => {
    const processo = { id: 1 } as Processo
    const subprocessos = [{ id: 10, unidade: 'U1' }] as Subprocesso[]
    mockSubprocessosStore.getUnidadesDoProcesso.mockReturnValue(subprocessos)

    const wrapper = mountComponent()
    const vm = wrapper.vm as any
    await vm.selecionarProcesso(processo)

    expect(vm.unidadesParticipantes).toEqual(subprocessos)
    expect(mockSubprocessosStore.getUnidadesDoProcesso).toHaveBeenCalledWith(processo.id)
  })

  it('should fetch and load atividades when a unidade is selected', async () => {
    const subprocesso = { id: 10 } as Subprocesso
    const atividades = [{ id: 100, descricao: 'Atividade 1' }] as Atividade[]
    mockAtividadesStore.getAtividadesPorSubprocesso.mockReturnValue(atividades)

    const wrapper = mountComponent()
    const vm = wrapper.vm as any
    await vm.selecionarUnidade(subprocesso)

    expect(mockAtividadesStore.fetchAtividadesPorSubprocesso).toHaveBeenCalledWith(subprocesso.id)
    expect(vm.atividadesParaImportar).toEqual(atividades)
  })

  it('should enable import button only when atividades are selected', async () => {
    const wrapper = mountComponent()
    const vm = wrapper.vm as any

    expect(wrapper.find('[data-testid="btn-importar"]').attributes('disabled')).toBeDefined()

    vm.atividadesSelecionadas = [{ id: 1 } as Atividade]
    await wrapper.vm.$nextTick()

    expect(wrapper.find('[data-testid="btn-importar"]').attributes('disabled')).toBeUndefined()
  })

  it('should emit `importar` with selected atividades', async () => {
    const atividades = [{ id: 1, descricao: 'Teste' }] as Atividade[]
    const wrapper = mountComponent()
    const vm = wrapper.vm as any
    vm.atividadesSelecionadas = atividades
    await wrapper.vm.$nextTick()

    await wrapper.find('[data-testid="btn-importar"]').trigger('click')

    expect(wrapper.emitted().importar).toBeTruthy()
    expect(wrapper.emitted().importar[0]).toEqual([atividades])
  })

  it('should emit `fechar` when close button is clicked', async () => {
    const wrapper = mountComponent()
    await wrapper.find('.btn-close').trigger('click')
    expect(wrapper.emitted().fechar).toBeTruthy()
  })

})
