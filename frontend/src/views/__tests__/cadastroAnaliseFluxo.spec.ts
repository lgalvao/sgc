import {describe, expect, it, vi} from 'vitest'
import {computed, ref} from 'vue'
import {useCadastroAnaliseFluxo} from '../cadastroAnaliseFluxo'

describe('cadastroAnaliseFluxo.ts', () => {
  const criarDependencias = () => ({
    codigoSubprocesso: ref<number | null>(123),
    codProcesso: 1,
    sigla: 'TEST',
    isRevisao: computed(() => false),
    acaoPrincipalCadastro: computed(() => ({
      codigo: 'ACEITAR' as const,
      mensagemSucesso: 'Sucesso',
      redirecionarParaPainel: true
    })),
    mostrarModalHistorico: ref(false),
    mostrarModalValidarAnalise: ref(false),
    mostrarModalDevolverAnalise: ref(false),
    resetarValidacao: vi.fn(),
    validarSubmissao: vi.fn(() => true),
    focarPrimeiroErroInvalido: vi.fn().mockResolvedValue(undefined),
    listarAnalisesCadastro: vi.fn().mockResolvedValue([{dataHora: '2023-01-01', observacoes: 'teste'}]),
    homologarCadastro: vi.fn().mockResolvedValue(true),
    aceitarCadastro: vi.fn().mockResolvedValue(true),
    devolverCadastro: vi.fn().mockResolvedValue(true),
  })

  it('deve abrir o histórico e carregar as análises', async () => {
    const deps = criarDependencias()
    const {abrirModalHistorico, historicoAnalises} = useCadastroAnaliseFluxo(deps)

    await abrirModalHistorico()

    expect(deps.listarAnalisesCadastro).toHaveBeenCalledWith(123)
    expect(historicoAnalises.value).toHaveLength(1)
    expect(deps.mostrarModalHistorico.value).toBe(true)
  })

  it('não deve carregar análises se não houver código de subprocesso ao abrir histórico', async () => {
    const deps = criarDependencias()
    deps.codigoSubprocesso.value = null
    const {abrirModalHistorico} = useCadastroAnaliseFluxo(deps)

    await abrirModalHistorico()

    expect(deps.listarAnalisesCadastro).not.toHaveBeenCalled()
    expect(deps.mostrarModalHistorico.value).toBe(true)
  })

  describe('confirmarValidacaoAnalise', () => {
    it('deve chamar aceitarCadastro quando o código da ação for ACEITAR', async () => {
      const deps = criarDependencias()
      const {confirmarValidacaoAnalise, observacaoValidacao} = useCadastroAnaliseFluxo(deps)
      
      observacaoValidacao.value = 'Obs Teste'
      await confirmarValidacaoAnalise()

      expect(deps.aceitarCadastro).toHaveBeenCalledWith(123, {observacoes: 'Obs Teste'}, false, {
        mensagemSucesso: 'Sucesso'
      })
      expect(deps.mostrarModalValidarAnalise.value).toBe(false)
    })

    it('deve chamar homologarCadastro quando o código da ação for HOMOLOGAR', async () => {
      const deps = criarDependencias()
      // @ts-expect-error - Mocking computed
      deps.acaoPrincipalCadastro = computed(() => ({
        codigo: 'HOMOLOGAR' as const,
        mensagemSucesso: 'Homologado',
        redirecionarParaPainel: false
      }))
      
      const {confirmarValidacaoAnalise} = useCadastroAnaliseFluxo(deps)
      await confirmarValidacaoAnalise()

      expect(deps.homologarCadastro).toHaveBeenCalledWith(123, {observacoes: ''}, false, {
        mensagemSucesso: 'Homologado',
        redirecionarParaPainel: false,
        redirecionarPara: {name: 'Subprocesso', params: {codProcesso: 1, siglaUnidade: 'TEST'}}
      })
    })

    it('deve chamar homologarCadastro sem redirecionarPara quando redirecionarParaPainel for true', async () => {
      const deps = criarDependencias()
      // @ts-expect-error - Mocking computed property
      deps.acaoPrincipalCadastro = computed(() => ({
        codigo: 'HOMOLOGAR' as const,
        mensagemSucesso: 'Homologado',
        redirecionarParaPainel: true
      }))
      
      const {confirmarValidacaoAnalise} = useCadastroAnaliseFluxo(deps)
      await confirmarValidacaoAnalise()

      expect(deps.homologarCadastro).toHaveBeenCalledWith(123, {observacoes: ''}, false, {
        mensagemSucesso: 'Homologado',
        redirecionarParaPainel: true,
        redirecionarPara: undefined
      })
    })
  })

  describe('confirmarDevolucaoAnalise', () => {
    it('deve falhar se a validação de submissão retornar false', async () => {
      const deps = criarDependencias()
      deps.validarSubmissao.mockReturnValue(false)
      const {confirmarDevolucaoAnalise, observacaoDevolucao} = useCadastroAnaliseFluxo(deps)
      
      observacaoDevolucao.value = ' '
      await confirmarDevolucaoAnalise()

      expect(deps.validarSubmissao).toHaveBeenCalledWith(false)
      expect(deps.focarPrimeiroErroInvalido).toHaveBeenCalled()
      expect(deps.devolverCadastro).not.toHaveBeenCalled()
    })

    it('deve chamar devolverCadastro se a validação for bem sucedida', async () => {
      const deps = criarDependencias()
      const {confirmarDevolucaoAnalise, observacaoDevolucao} = useCadastroAnaliseFluxo(deps)
      
      observacaoDevolucao.value = 'Obs Devolução'
      await confirmarDevolucaoAnalise()

      expect(deps.devolverCadastro).toHaveBeenCalledWith(123, {observacoes: 'Obs Devolução'}, false)
      expect(deps.mostrarModalDevolverAnalise.value).toBe(false)
      expect(observacaoDevolucao.value).toBe('')
    })
  })

  it('deve limpar observações ao fechar modais', () => {
    const deps = criarDependencias()
    const {
      fecharModalValidarAnalise,
      fecharModalDevolverAnalise,
      observacaoValidacao,
      observacaoDevolucao
    } = useCadastroAnaliseFluxo(deps)

    observacaoValidacao.value = 'abc'
    fecharModalValidarAnalise()
    expect(observacaoValidacao.value).toBe('')
    expect(deps.mostrarModalValidarAnalise.value).toBe(false)

    observacaoDevolucao.value = 'def'
    fecharModalDevolverAnalise()
    expect(observacaoDevolucao.value).toBe('')
    expect(deps.mostrarModalDevolverAnalise.value).toBe(false)
    expect(deps.resetarValidacao).toHaveBeenCalled()
  })
})
