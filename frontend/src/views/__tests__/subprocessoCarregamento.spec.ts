import {describe, expect, it, vi, beforeEach, afterEach} from 'vitest'
import {defineComponent, h} from 'vue'
import {flushPromises, mount} from '@vue/test-utils'
import {useSubprocessoCarregamento} from '../subprocessoCarregamento'

const subprocessoStoreMock = {
    erroIntegracaoContexto: null as unknown,
    obterContextoEdicao: vi.fn().mockResolvedValue(null),
    obterContextoEdicaoPorProcessoEUnidade: vi.fn().mockResolvedValue(null),
    dadosEdicaoValidos: vi.fn().mockReturnValue(false),
}

vi.mock('@/stores/subprocesso', () => ({
    useSubprocessoStore: () => subprocessoStoreMock,
}))

describe('subprocessoCarregamento.ts', () => {
    beforeEach(() => {
        subprocessoStoreMock.erroIntegracaoContexto = null
        subprocessoStoreMock.obterContextoEdicao = vi.fn().mockResolvedValue(null)
        subprocessoStoreMock.obterContextoEdicaoPorProcessoEUnidade = vi.fn().mockResolvedValue(null)
        subprocessoStoreMock.dadosEdicaoValidos = vi.fn().mockReturnValue(false)
        vi.useFakeTimers()
    })

    afterEach(() => {
        vi.useRealTimers()
    })

    const criarDependencias = (overrides = {}) => ({
        codProcesso: 1,
        siglaUnidade: 'TEST',
        codSubprocesso: undefined as number | undefined,
        exibirToastPendente: vi.fn(),
        ...overrides
    })

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
        subprocessoStoreMock.obterContextoEdicao = vi.fn().mockResolvedValue({detalhes: {codigo: 456}})
        const deps = criarDependencias({codSubprocesso: 456})

        const wrapper = mount(TestComponent(deps))
        await flushPromises()

        expect(deps.exibirToastPendente).toHaveBeenCalled()
        expect(subprocessoStoreMock.obterContextoEdicao).toHaveBeenCalledWith(456, {forcar: false})
        expect(wrapper.vm.codigoSubprocesso).toBe(456)
        expect(wrapper.vm.erroNaoEncontrado).toBe(false)
    })

    it('deve carregar subprocesso via processo/unidade no onMounted quando direto falha', async () => {
        subprocessoStoreMock.obterContextoEdicaoPorProcessoEUnidade = vi.fn().mockResolvedValue({codigo: 789})
        const deps = criarDependencias()

        const wrapper = mount(TestComponent(deps))
        await flushPromises()

        expect(subprocessoStoreMock.obterContextoEdicaoPorProcessoEUnidade).toHaveBeenCalledWith(1, 'TEST', {forcar: false})
        expect(wrapper.vm.codigoSubprocesso).toBe(789)
    })

    it('deve definir erroNaoEncontrado se nenhum método encontrar o subprocesso', async () => {
        const deps = criarDependencias()

        const wrapper = mount(TestComponent(deps))
        await flushPromises()

        expect(wrapper.vm.codigoSubprocesso).toBe(null)
        expect(wrapper.vm.erroNaoEncontrado).toBe(true)
    })

    it('deve recarregar ao atualizarSubprocessoAtual', async () => {
        subprocessoStoreMock.obterContextoEdicaoPorProcessoEUnidade = vi.fn().mockResolvedValue({codigo: 123})
        const deps = criarDependencias()

        const wrapper = mount(TestComponent(deps))
        await flushPromises()

        await wrapper.vm.atualizarSubprocessoAtual()
        expect(subprocessoStoreMock.obterContextoEdicao).toHaveBeenCalledWith(123, {forcar: true})
    })

    it('deve observar mudanças nas dependências', async () => {
        subprocessoStoreMock.obterContextoEdicaoPorProcessoEUnidade = vi.fn().mockResolvedValue({codigo: 111})
        const deps = criarDependencias()
        const wrapper = mount(TestComponent(deps))
        await flushPromises()

        await wrapper.vm.carregarSubprocesso(true)
        expect(subprocessoStoreMock.obterContextoEdicaoPorProcessoEUnidade).toHaveBeenLastCalledWith(1, 'TEST', {forcar: true})
        expect(wrapper.vm.codigoSubprocesso).toBe(111)
    })

    it('deve respeitar a lógica do onActivated', async () => {
        subprocessoStoreMock.obterContextoEdicaoPorProcessoEUnidade = vi.fn().mockResolvedValue({codigo: 123})
        const deps = criarDependencias()

        const wrapper = mount(TestComponent(deps))
        await flushPromises()

        subprocessoStoreMock.obterContextoEdicaoPorProcessoEUnidade = vi.fn().mockResolvedValue(null)

        // @ts-expect-error - Acessando hook privado do vue para simular ativação
        await wrapper.vm.$.a?.[0]()
        expect(subprocessoStoreMock.obterContextoEdicaoPorProcessoEUnidade).toHaveBeenCalledWith(1, 'TEST', {forcar: false})
    })

    it('não deve recarregar no onActivated quando o contexto atual ainda for válido', async () => {
        subprocessoStoreMock.dadosEdicaoValidos = vi.fn().mockReturnValue(true)
        subprocessoStoreMock.obterContextoEdicaoPorProcessoEUnidade = vi.fn().mockResolvedValue({codigo: 123})
        const deps = criarDependencias()

        const wrapper = mount(TestComponent(deps))
        await flushPromises()
        subprocessoStoreMock.obterContextoEdicaoPorProcessoEUnidade = vi.fn().mockResolvedValue(null)

        // @ts-expect-error - Acessando hook privado do vue para simular ativação
        await wrapper.vm.$.a?.[0]()

        expect(subprocessoStoreMock.obterContextoEdicaoPorProcessoEUnidade).not.toHaveBeenCalled()
    })
})
