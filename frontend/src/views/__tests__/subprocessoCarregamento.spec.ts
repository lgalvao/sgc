import {describe, expect, it, vi} from 'vitest'
import {computed, defineComponent, h} from 'vue'
import {flushPromises, mount} from '@vue/test-utils'
import {useSubprocessoCarregamento} from '../subprocessoCarregamento'

describe('subprocessoCarregamento.ts', () => {
    beforeEach(() => {
        vi.useFakeTimers()
    })

    afterEach(() => {
        vi.useRealTimers()
    })

    const criarDependencias = (overrides = {}) => ({
        codProcesso: 1,
        siglaUnidade: 'TEST',
        codSubprocesso: undefined as number | undefined,
        erroIntegracaoContexto: computed(() => null),
        garantirContextoEdicao: vi.fn().mockResolvedValue(null),
        garantirContextoEdicaoPorProcessoEUnidade: vi.fn().mockResolvedValue(null),
        invalidarMapa: vi.fn(),
        exibirToastPendente: vi.fn(),
        ...overrides
    })

    // Componente auxiliar para testar o composable e seus hooks de ciclo de vida
    const TestComponent = (deps: any) => defineComponent({
        setup() {
            const result = useSubprocessoCarregamento(deps)
            return {...result}
        },
        render() {
            return h('div')
        }
    })

    it('deve carregar subprocesso via codSubprocesso no onMounted', async () => {
        const deps = criarDependencias({codSubprocesso: 456})
        deps.garantirContextoEdicao.mockResolvedValue({detalhes: {codigo: 456}})

        const wrapper = mount(TestComponent(deps))
        await flushPromises() // Aguarda promessas do setup/onMounted

        expect(deps.exibirToastPendente).toHaveBeenCalled()
        expect(deps.garantirContextoEdicao).toHaveBeenCalledWith(456, true)
        expect(wrapper.vm.codigoSubprocesso).toBe(456)
        expect(wrapper.vm.erroNaoEncontrado).toBe(false)
    })

    it('deve carregar subprocesso via processo/unidade no onMounted quando direto falha', async () => {
        const deps = criarDependencias()
        deps.garantirContextoEdicaoPorProcessoEUnidade.mockResolvedValue({codigo: 789})

        const wrapper = mount(TestComponent(deps))
        await flushPromises()

        expect(deps.garantirContextoEdicaoPorProcessoEUnidade).toHaveBeenCalledWith(1, 'TEST', true)
        expect(wrapper.vm.codigoSubprocesso).toBe(789)
    })

    it('deve definir erroNaoEncontrado se nenhum método encontrar o subprocesso', async () => {
        const deps = criarDependencias()
        deps.garantirContextoEdicaoPorProcessoEUnidade.mockResolvedValue(null)

        const wrapper = mount(TestComponent(deps))
        await flushPromises()

        expect(wrapper.vm.codigoSubprocesso).toBe(null)
        expect(wrapper.vm.erroNaoEncontrado).toBe(true)
    })

    it('deve recarregar ao atualizarSubprocessoAtual', async () => {
        const deps = criarDependencias()
        deps.garantirContextoEdicaoPorProcessoEUnidade.mockResolvedValue({codigo: 123})

        const wrapper = mount(TestComponent(deps))
        await flushPromises()

        await wrapper.vm.atualizarSubprocessoAtual()
        expect(deps.invalidarMapa).toHaveBeenCalled()
        expect(deps.garantirContextoEdicao).toHaveBeenCalledWith(123, true)
    })

    it('deve observar mudanças nas dependências', async () => {
        const deps = criarDependencias()
        const wrapper = mount(TestComponent(deps))
        await flushPromises()

        deps.garantirContextoEdicaoPorProcessoEUnidade.mockResolvedValue({codigo: 111})
        await wrapper.vm.carregarSubprocesso(true)
        expect(wrapper.vm.codigoSubprocesso).toBe(111)
    })

    it('deve respeitar a lógica do onActivated', async () => {
        const deps = criarDependencias()
        deps.garantirContextoEdicaoPorProcessoEUnidade.mockResolvedValue({codigo: 123})

        // Simula a ativação via hook (precisamos do componente montado)
        const wrapper = mount(TestComponent(deps))
        await flushPromises()

        deps.garantirContextoEdicaoPorProcessoEUnidade.mockClear()

        // @ts-expect-error - Acessando hook privado do vue para simular ativação
        await wrapper.vm.$.a?.[0]()
        expect(deps.garantirContextoEdicaoPorProcessoEUnidade).toHaveBeenCalledWith(1, 'TEST', false)
    })
})
