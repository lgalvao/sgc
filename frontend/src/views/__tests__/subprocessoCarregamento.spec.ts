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
        obterContextoEdicao: vi.fn().mockResolvedValue(null),
        recarregarContextoEdicao: vi.fn().mockResolvedValue(null),
        obterContextoEdicaoPorProcessoEUnidade: vi.fn().mockResolvedValue(null),
        recarregarContextoEdicaoPorProcessoEUnidade: vi.fn().mockResolvedValue(null),
        exibirToastPendente: vi.fn(),
        dadosEdicaoValidos: vi.fn().mockReturnValue(false),
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
        deps.obterContextoEdicao.mockResolvedValue({detalhes: {codigo: 456}})

        const wrapper = mount(TestComponent(deps))
        await flushPromises() // Aguarda promessas do setup/onMounted

        expect(deps.exibirToastPendente).toHaveBeenCalled()
        expect(deps.obterContextoEdicao).toHaveBeenCalledWith(456)
        expect(wrapper.vm.codigoSubprocesso).toBe(456)
        expect(wrapper.vm.erroNaoEncontrado).toBe(false)
    })

    it('deve carregar subprocesso via processo/unidade no onMounted quando direto falha', async () => {
        const deps = criarDependencias()
        deps.obterContextoEdicaoPorProcessoEUnidade.mockResolvedValue({codigo: 789})

        const wrapper = mount(TestComponent(deps))
        await flushPromises()

        expect(deps.obterContextoEdicaoPorProcessoEUnidade).toHaveBeenCalledWith(1, 'TEST')
        expect(wrapper.vm.codigoSubprocesso).toBe(789)
    })

    it('deve definir erroNaoEncontrado se nenhum método encontrar o subprocesso', async () => {
        const deps = criarDependencias()
        deps.obterContextoEdicaoPorProcessoEUnidade.mockResolvedValue(null)

        const wrapper = mount(TestComponent(deps))
        await flushPromises()

        expect(wrapper.vm.codigoSubprocesso).toBe(null)
        expect(wrapper.vm.erroNaoEncontrado).toBe(true)
    })

    it('deve recarregar ao atualizarSubprocessoAtual', async () => {
        const deps = criarDependencias()
        deps.obterContextoEdicaoPorProcessoEUnidade.mockResolvedValue({codigo: 123})

        const wrapper = mount(TestComponent(deps))
        await flushPromises()

        await wrapper.vm.atualizarSubprocessoAtual()
        expect(deps.recarregarContextoEdicao).toHaveBeenCalledWith(123)
    })

    it('deve observar mudanças nas dependências', async () => {
        const deps = criarDependencias()
        const wrapper = mount(TestComponent(deps))
        await flushPromises()

        deps.recarregarContextoEdicaoPorProcessoEUnidade.mockResolvedValue({codigo: 111})
        await wrapper.vm.carregarSubprocesso(true)
        expect(wrapper.vm.codigoSubprocesso).toBe(111)
    })

    it('deve respeitar a lógica do onActivated', async () => {
        const deps = criarDependencias()
        deps.obterContextoEdicaoPorProcessoEUnidade.mockResolvedValue({codigo: 123})

        // Simula a ativação via hook (precisamos do componente montado)
        const wrapper = mount(TestComponent(deps))
        await flushPromises()

        deps.obterContextoEdicaoPorProcessoEUnidade.mockClear()

        // @ts-expect-error - Acessando hook privado do vue para simular ativação
        await wrapper.vm.$.a?.[0]()
        expect(deps.obterContextoEdicaoPorProcessoEUnidade).toHaveBeenCalledWith(1, 'TEST')
    })

    it('não deve recarregar no onActivated quando o contexto atual ainda for válido', async () => {
        const deps = criarDependencias({
            dadosEdicaoValidos: vi.fn().mockReturnValue(true),
        })
        deps.obterContextoEdicaoPorProcessoEUnidade.mockResolvedValue({codigo: 123})

        const wrapper = mount(TestComponent(deps))
        await flushPromises()
        deps.obterContextoEdicaoPorProcessoEUnidade.mockClear()

        // @ts-expect-error - Acessando hook privado do vue para simular ativação
        await wrapper.vm.$.a?.[0]()

        expect(deps.obterContextoEdicaoPorProcessoEUnidade).not.toHaveBeenCalled()
    })
})
