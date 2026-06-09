import {describe, expect, it, vi} from 'vitest'
import {computed, ref} from 'vue'
import {useMapaAnaliseFluxo} from '../mapaAnaliseFluxo'
import {TEXTOS} from "@/constants/textos"
import {TEXTOS_SUCESSO_MAPA} from "@/constants/textos-mapa"
import {TEXTOS_SUCESSO_SUBPROCESSO} from "@/constants/textos-subprocesso"

describe('mapaAnaliseFluxo.ts', () => {
    const criarDependencias = () => ({
        obterCodigoSubprocessoObrigatorio: vi.fn(() => 123),
        acaoPrincipalMapa: computed(() => ({
            codigo: 'ACEITAR' as const,
            mensagemSucesso: 'Sucesso Aceitar'
        })),
        mostrarModalAceitar: ref(false),
        mostrarModalValidar: ref(false),
        mostrarModalDevolucao: ref(false),
        mostrarModalHistorico: ref(false),
        observacaoDevolucao: ref(''),
        analisesCadastro: ref([]),
        resetarValidacao: vi.fn(),
        validarSubmissao: vi.fn(() => true),
        focarPrimeiroErroInvalido: vi.fn().mockResolvedValue(undefined),
        concluirAcaoPainel: vi.fn().mockResolvedValue(undefined),
        notify: vi.fn(),
        listarAnalisesCadastro: vi.fn().mockResolvedValue([{dataHora: '2023', observacoes: 'ok'}]),
        validarMapa: vi.fn().mockResolvedValue(undefined),
        homologarMapa: vi.fn().mockResolvedValue(undefined),
        aceitarMapa: vi.fn().mockResolvedValue(undefined),
        devolverMapa: vi.fn().mockResolvedValue(undefined),
    })

    it('deve abrir e fechar o modal de aceitar', () => {
        const deps = criarDependencias()
        const {abrirModalAceitar, fecharModalAceitar} = useMapaAnaliseFluxo(deps)
        abrirModalAceitar()
        expect(deps.mostrarModalAceitar.value).toBe(true)
        fecharModalAceitar()
        expect(deps.mostrarModalAceitar.value).toBe(false)
    })

    it('deve abrir e fechar o modal de validar', () => {
        const deps = criarDependencias()
        const {abrirModalValidar, fecharModalValidar} = useMapaAnaliseFluxo(deps)
        abrirModalValidar()
        expect(deps.mostrarModalValidar.value).toBe(true)
        fecharModalValidar()
        expect(deps.mostrarModalValidar.value).toBe(false)
    })

    it('deve abrir modal de devolucao e resetar validacao', () => {
        const deps = criarDependencias()
        const {abrirModalDevolucao} = useMapaAnaliseFluxo(deps)
        abrirModalDevolucao()
        expect(deps.resetarValidacao).toHaveBeenCalled()
        expect(deps.mostrarModalDevolucao.value).toBe(true)
    })

    it('deve fechar modal de devolucao e limpar observacao', () => {
        const deps = criarDependencias()
        const {fecharModalDevolucao} = useMapaAnaliseFluxo(deps)
        deps.mostrarModalDevolucao.value = true
        deps.observacaoDevolucao.value = 'teste'
        fecharModalDevolucao()
        expect(deps.mostrarModalDevolucao.value).toBe(false)
        expect(deps.observacaoDevolucao.value).toBe('')
    })

    it('deve confirmar validação com sucesso', async () => {
        const deps = criarDependencias()
        const {confirmarValidacao} = useMapaAnaliseFluxo(deps)
        await confirmarValidacao()
        expect(deps.validarMapa).toHaveBeenCalledWith(123)
        expect(deps.concluirAcaoPainel).toHaveBeenCalledWith(TEXTOS_SUCESSO_MAPA.MAPA_VALIDADO_SUBMETIDO, expect.any(Function))
    })

    it('deve notificar erro ao confirmar validação falha', async () => {
        const deps = criarDependencias()
        deps.validarMapa.mockRejectedValue(new Error('Erro'))
        const {confirmarValidacao} = useMapaAnaliseFluxo(deps)
        await confirmarValidacao()
        expect(deps.notify).toHaveBeenCalledWith(TEXTOS.mapa.ERRO_VALIDAR, "danger")
    })

    describe('confirmarAceitacao', () => {
        it('deve chamar aceitarMapa por padrão', async () => {
            const deps = criarDependencias()
            const {confirmarAceitacao} = useMapaAnaliseFluxo(deps)
            await confirmarAceitacao('obs')
            expect(deps.aceitarMapa).toHaveBeenCalledWith(123, {observacao: 'obs'})
            expect(deps.concluirAcaoPainel).toHaveBeenCalledWith('Sucesso Aceitar', expect.any(Function))
        })

        it('deve chamar homologarMapa quando acao for HOMOLOGAR', async () => {
            const deps = criarDependencias()
            // @ts-expect-error - Mocking computed property
            deps.acaoPrincipalMapa = computed(() => ({
                codigo: 'HOMOLOGAR' as const,
                mensagemSucesso: 'Sucesso Homologar'
            }))
            const {confirmarAceitacao} = useMapaAnaliseFluxo(deps)
            await confirmarAceitacao('obs')
            expect(deps.homologarMapa).toHaveBeenCalledWith(123, {observacao: 'obs'})
            expect(deps.concluirAcaoPainel).toHaveBeenCalledWith('Sucesso Homologar', expect.any(Function))
        })

        it('deve notificar erro se aceitacao falhar', async () => {
            const deps = criarDependencias()
            deps.aceitarMapa.mockRejectedValue(new Error('Erro'))
            const {confirmarAceitacao} = useMapaAnaliseFluxo(deps)
            await confirmarAceitacao()
            expect(deps.notify).toHaveBeenCalledWith(TEXTOS.comum.ERRO_OPERACAO, "danger")
        })
    })

    describe('confirmarDevolucao', () => {
        it('deve falhar se a validação retornar false', async () => {
            const deps = criarDependencias()
            deps.validarSubmissao.mockReturnValue(false)
            const {confirmarDevolucao} = useMapaAnaliseFluxo(deps)
            await confirmarDevolucao()
            expect(deps.focarPrimeiroErroInvalido).toHaveBeenCalled()
            expect(deps.devolverMapa).not.toHaveBeenCalled()
        })

        it('deve confirmar devolução com sucesso', async () => {
            const deps = criarDependencias()
            deps.observacaoDevolucao.value = 'justificativa'
            const {confirmarDevolucao} = useMapaAnaliseFluxo(deps)
            await confirmarDevolucao()
            expect(deps.devolverMapa).toHaveBeenCalledWith(123, {justificativa: 'justificativa'})
            expect(deps.concluirAcaoPainel).toHaveBeenCalledWith(TEXTOS_SUCESSO_SUBPROCESSO.DEVOLUCAO_REALIZADA, expect.any(Function))
        })

        it('deve notificar erro se devolução falhar', async () => {
            const deps = criarDependencias()
            deps.devolverMapa.mockRejectedValue(new Error('Erro'))
            const {confirmarDevolucao} = useMapaAnaliseFluxo(deps)
            deps.observacaoDevolucao.value = 'justificativa'
            await confirmarDevolucao()
            expect(deps.notify).toHaveBeenCalledWith(TEXTOS.mapa.ERRO_DEVOLVER, "danger")
        })
    })

    it('deve carregar histórico de análises', async () => {
        const deps = criarDependencias()
        const {verHistorico} = useMapaAnaliseFluxo(deps)
        await verHistorico()
        expect(deps.listarAnalisesCadastro).toHaveBeenCalledWith(123)
        expect(deps.analisesCadastro.value).toHaveLength(1)
        expect(deps.mostrarModalHistorico.value).toBe(true)
    })
})
