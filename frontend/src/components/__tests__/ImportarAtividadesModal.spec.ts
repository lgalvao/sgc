import { beforeEach, describe, expect, it, vi } from 'vitest'
import { mount, VueWrapper } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { ref } from 'vue'
import ImportarAtividadesModal from '../ImportarAtividadesModal.vue'
import { useProcessosStore } from '@/stores/processos'
import { useAtividadesStore } from '@/stores/atividades'
import type { Atividade, ProcessoDetalhe, ProcessoResumo } from '@/types/tipos'
import { SituacaoProcesso, TipoProcesso } from '@/types/tipos'

vi.mock('@/stores/processos')
vi.mock('@/stores/atividades')

const mockProcessosPainel: ProcessoResumo[] = [
    {
        codigo: 1,
        descricao: 'Processo 1',
        situacao: SituacaoProcesso.FINALIZADO,
        tipo: 'MAPEAMENTO',
        dataLimite: '2025-12-31',
        dataCriacao: '2025-01-01',
        unidadeCodigo: 1,
        unidadeNome: 'Unidade 1',
        unidades: [],
    },
]

const mockProcessoDetalhe: ProcessoDetalhe = {
    codigo: 1,
    descricao: 'Processo 1',
    tipo: TipoProcesso.MAPEAMENTO,
    situacao: SituacaoProcesso.FINALIZADO,
    dataLimite: '2025-12-31',
    dataCriacao: '2025-01-01',
    unidades: [
        {
            nome: 'Unidade 1',
            sigla: 'U1',
            codUnidade: 101,
            situacaoSubprocesso: SituacaoSubprocesso.CONCLUIDO,
            dataLimite: '2025-12-31',
            filhos: [],
        },
    ],
    resumoSubprocessos: [],
}

describe('ImportarAtividadesModal.vue', () => {
    let wrapper: VueWrapper<any>
    let processosStore: ReturnType<typeof useProcessosStore>
    let atividadesStore: ReturnType<typeof useAtividadesStore>

    const mountComponent = (props = { mostrar: true }) => {
        return mount(ImportarAtividadesModal, {
            props,
            global: {
                plugins: [createPinia()],
            },
        })
    }

    beforeEach(() => {
        setActivePinia(createPinia())

        const processosPainel = ref(mockProcessosPainel)
        const processoDetalhe = ref<ProcessoDetalhe | null>(null)
        const fetchProcessosPainel = vi.fn().mockResolvedValue(void 0)
        const fetchProcessoDetalhe = vi.fn().mockImplementation((id) => {
            if (id === mockProcessoDetalhe.codigo) {
                processoDetalhe.value = mockProcessoDetalhe
            }
            return Promise.resolve()
        })

        vi.mocked(useProcessosStore).mockReturnValue({
            processosPainel,
            processoDetalhe,
            fetchProcessosPainel,
            fetchProcessoDetalhe,
        } as any)

        const atividadesPorSubprocesso = ref(new Map())
        const getAtividadesPorSubprocesso = vi.fn((id) => atividadesPorSubprocesso.value.get(id) || [])
        const fetchAtividadesParaSubprocesso = vi.fn().mockResolvedValue(void 0)

        vi.mocked(useAtividadesStore).mockReturnValue({
            atividadesPorSubprocesso,
            getAtividadesPorSubprocesso,
            fetchAtividadesParaSubprocesso,
        } as any)

        processosStore = useProcessosStore()
        atividadesStore = useAtividadesStore()

        vi.clearAllMocks()
    })

    it('deve renderizar o modal quando "mostrar" for verdadeiro', () => {
        wrapper = mountComponent()
        expect(wrapper.find('.modal.show').exists()).toBe(true)
        expect(wrapper.find('.modal-title').text()).toBe('Importação de atividades')
    })

    it('não deve renderizar o modal quando "mostrar" for falso', () => {
        wrapper = mountComponent({ mostrar: false })
        expect(wrapper.find('.modal.show').exists()).toBe(false)
    })

    it('deve buscar processos disponíveis ao ser montado', () => {
        wrapper = mountComponent()
        expect(processosStore.fetchProcessosPainel).toHaveBeenCalled()
    })

    it('deve buscar detalhes do processo quando um processo for selecionado', async () => {
        wrapper = mountComponent()
        const select = wrapper.find<HTMLSelectElement>('[data-testid="select-processo"]')
        await select.setValue(String(mockProcessoDetalhe.codigo))
        await wrapper.vm.$nextTick()
        expect(processosStore.fetchProcessoDetalhe).toHaveBeenCalledWith(mockProcessoDetalhe.codigo)
    })

    it('deve buscar atividades quando uma unidade for selecionada', async () => {
        wrapper = mountComponent()

        const selectProcesso = wrapper.find<HTMLSelectElement>('[data-testid="select-processo"]')
        await selectProcesso.setValue(String(mockProcessoDetalhe.codigo))
        await wrapper.vm.$nextTick()
        await wrapper.vm.$nextTick()

        const idUnidade = mockProcessoDetalhe.unidades[0].codUnidade
        const selectUnidade = wrapper.find<HTMLSelectElement>('[data-testid="select-unidade"]')
        await selectUnidade.setValue(String(idUnidade))
        await wrapper.vm.$nextTick()

        expect(atividadesStore.fetchAtividadesParaSubprocesso).toHaveBeenCalledWith(idUnidade)
    })

    it('deve habilitar o botão de importação somente quando as atividades forem selecionadas', async () => {
        wrapper = mountComponent()
        const mockAtividades: Atividade[] = [{ codigo: 1, descricao: 'Atividade 1', conhecimentos: [] }]
        const idUnidade = mockProcessoDetalhe.unidades[0].codUnidade

        vi.mocked(atividadesStore.getAtividadesPorSubprocesso).mockReturnValue(mockAtividades)

        const importButton = wrapper.find<HTMLButtonElement>('[data-testid="btn-importar"]')
        expect(importButton.element.disabled).toBe(true)

        await wrapper.find('[data-testid="select-processo"]').setValue(String(mockProcessoDetalhe.codigo))
        await wrapper.vm.$nextTick()
        await wrapper.vm.$nextTick()

        await wrapper.find('[data-testid="select-unidade"]').setValue(String(idUnidade))
        await wrapper.vm.$nextTick()
        await wrapper.vm.$nextTick()

        const checkbox = wrapper.find<HTMLInputElement>('input[type="checkbox"]')
        await checkbox.setValue(true)
        await wrapper.vm.$nextTick()

        expect(importButton.element.disabled).toBe(false)
    })

    it('deve emitir "importar" com as atividades selecionadas ao clicar em importar', async () => {
        wrapper = mountComponent()
        const mockAtividades: Atividade[] = [{ codigo: 1, descricao: 'Atividade 1', conhecimentos: [] }]
        const idUnidade = mockProcessoDetalhe.unidades[0].codUnidade

        vi.mocked(atividadesStore.getAtividadesPorSubprocesso).mockReturnValue(mockAtividades)

        await wrapper.find('[data-testid="select-processo"]').setValue(String(mockProcessoDetalhe.codigo))
        await wrapper.vm.$nextTick()
        await wrapper.vm.$nextTick()

        await wrapper.find('[data-testid="select-unidade"]').setValue(String(idUnidade))
        await wrapper.vm.$nextTick()
        await wrapper.vm.$nextTick()

        await wrapper.find('input[type="checkbox"]').setValue(true)
        await wrapper.vm.$nextTick()

        const importButton = wrapper.find('[data-testid="btn-importar"]')
        await importButton.trigger('click')

        expect(wrapper.emitted('importar')).toBeTruthy()
        expect(wrapper.emitted('importar')?.[0]).toEqual([mockAtividades])
    })

    it('deve emitir "fechar" ao clicar em cancelar', async () => {
        wrapper = mountComponent()
        await wrapper.find('[data-testid="btn-modal-cancelar"]').trigger('click')
        expect(wrapper.emitted('fechar')).toBeTruthy()
    })
})