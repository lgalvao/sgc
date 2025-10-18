import { beforeEach, describe, expect, it, vi } from 'vitest'
import { mount, VueWrapper } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import ImpactoMapaModal from '../ImpactoMapaModal.vue'
import { useMapasStore } from '@/stores/mapas'
import { useUnidadesStore } from '@/stores/unidades'
import { useProcessosStore } from '@/stores/processos'
import { useRevisaoStore } from '@/stores/revisao'
import { mockUnidade, mockProcessoDetalhe } from '@/mocks/processos'
import { ref } from 'vue'

// Mock the stores
vi.mock('@/stores/mapas')
vi.mock('@/stores/unidades')
vi.mock('@/stores/processos')
vi.mock('@/stores/revisao')

describe('ImpactoMapaModal.vue', () => {
    let wrapper: VueWrapper<any>
    let mapasStore: ReturnType<typeof useMapasStore>
    let unidadesStore: ReturnType<typeof useUnidadesStore>
    let processosStore: ReturnType<typeof useProcessosStore>
    let revisaoStore: ReturnType<typeof useRevisaoStore>

    const idProcesso = 1
    const siglaUnidade = 'UNID'

    const mountComponent = (props = { mostrar: true, idProcesso, siglaUnidade }) => {
        return mount(ImpactoMapaModal, {
            props,
            global: {
                plugins: [createPinia()],
            },
        })
    }

    beforeEach(() => {
        setActivePinia(createPinia())

        vi.mocked(useMapasStore).mockReturnValue({
            getMapaByUnidadeId: vi.fn().mockReturnValue({ competencias: [] }),
        } as any)

        vi.mocked(useUnidadesStore).mockReturnValue({
            pesquisarUnidade: vi.fn().mockReturnValue(mockUnidade),
        } as any)

        vi.mocked(useProcessosStore).mockReturnValue({
            fetchProcessoDetalhe: vi.fn().mockResolvedValue(undefined),
            processoDetalhe: ref(mockProcessoDetalhe),
        } as any)

        vi.mocked(useRevisaoStore).mockReturnValue({
            mudancasParaImpacto: ref([]),
        } as any)

        mapasStore = useMapasStore()
        unidadesStore = useUnidadesStore()
        processosStore = useProcessosStore()
        revisaoStore = useRevisaoStore()

        vi.clearAllMocks()
    })

    it('should not render when "mostrar" is false', () => {
        wrapper = mountComponent({ mostrar: false, idProcesso, siglaUnidade })
        expect(wrapper.find('.modal.show').exists()).toBe(false)
    })

    it('should render and fetch impacts when "mostrar" becomes true', async () => {
        wrapper = mountComponent({ mostrar: false, idProcesso, siglaUnidade })
        expect(processosStore.fetchProcessoDetalhe).not.toHaveBeenCalled()

        await wrapper.setProps({ mostrar: true })
        await wrapper.vm.$nextTick()

        expect(wrapper.find('.modal.show').exists()).toBe(true)
        expect(processosStore.fetchProcessoDetalhe).toHaveBeenCalledWith(idProcesso)
    })

    it('should display a loading state initially', async () => {
        vi.mocked(processosStore).processoDetalhe = ref(null)
        wrapper = mountComponent()
        expect(wrapper.find('.spinner-border').exists()).toBe(true)
    })

    it('should display "no impact" message when temImpacto is false', async () => {
        revisaoStore.mudancasParaImpacto = ref([])
        wrapper = mountComponent()
        await wrapper.vm.$nextTick()
        await wrapper.vm.$nextTick()

        expect(wrapper.find('[data-testid="msg-nenhuma-competencia"]').exists()).toBe(true)
    })

    it('should display impacted competencies and their reasons', async () => {
        revisaoStore.mudancasParaImpacto = ref([
            {
                codigo: 1,
                tipo: 'AtividadeAdicionada',
                descricaoAtividade: 'Nova Atividade',
                competenciasImpactadasIds: [1],
            },
        ])
        mapasStore.getMapaByUnidadeId = vi.fn().mockReturnValue({
            competencias: [
                {
                    codigo: 1,
                    descricao: 'Gestão de Projetos',
                    atividadesAssociadas: [],
                },
            ],
        })

        wrapper = mountComponent()
        await wrapper.vm.$nextTick()
        await wrapper.vm.$nextTick()

        const text = wrapper.text()
        expect(text).toContain('Gestão de Projetos')
    })

    it('should emit "fechar" when the close button is clicked', async () => {
        wrapper = mountComponent()
        await wrapper.find('.btn-close').trigger('click')
        expect(wrapper.emitted('fechar')).toBeTruthy()
    })
})