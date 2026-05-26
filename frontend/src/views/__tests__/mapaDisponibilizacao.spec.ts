import {describe, expect, it, vi} from 'vitest'
import {ref} from 'vue'
import {useMapaDisponibilizacao} from '../mapaDisponibilizacao'
import {TEXTOS} from "@/constants/textos"
import {TEXTOS_SUCESSO_MAPA} from "@/constants/textos-mapa"

describe('mapaDisponibilizacao.ts', () => {
    const criarDependencias = () => ({
        competencias: ref([{codigo: 1}]),
        existeCompetenciaSemAtividade: ref(false),
        atividadesSemCompetencia: ref<Array<{ codigo: number }>>([]),
        mostrarModalDisponibilizar: ref(false),
        limparErros: vi.fn(),
        obterCodigoSubprocessoObrigatorio: vi.fn(() => 123),
        disponibilizarMapaFluxo: vi.fn().mockResolvedValue(undefined),
        concluirAcaoPainel: vi.fn().mockResolvedValue(undefined),
        aplicarErroNormalizado: vi.fn(),
    })

    describe('podeConfirmarDisponibilizacao', () => {
        it('deve retornar true quando tudo está válido', () => {
            const deps = criarDependencias()
            const {podeConfirmarDisponibilizacao} = useMapaDisponibilizacao(deps as any)
            expect(podeConfirmarDisponibilizacao.value).toBe(true)
        })

        it('deve retornar false se não houver competências', () => {
            const deps = criarDependencias()
            deps.competencias.value = []
            const {podeConfirmarDisponibilizacao} = useMapaDisponibilizacao(deps as any)
            expect(podeConfirmarDisponibilizacao.value).toBe(false)
        })

        it('deve retornar false se houver competência sem atividade', () => {
            const deps = criarDependencias()
            deps.existeCompetenciaSemAtividade.value = true
            const {podeConfirmarDisponibilizacao} = useMapaDisponibilizacao(deps as any)
            expect(podeConfirmarDisponibilizacao.value).toBe(false)
        })

        it('deve retornar false se houver atividades sem competência', () => {
            const deps = criarDependencias()
            deps.atividadesSemCompetencia.value = [{codigo: 1}]
            const {podeConfirmarDisponibilizacao} = useMapaDisponibilizacao(deps as any)
            expect(podeConfirmarDisponibilizacao.value).toBe(false)
        })
    })

    describe('abrirModalDisponibilizar', () => {
        it('deve mostrar erro se não puder confirmar', () => {
            const deps = criarDependencias()
            deps.competencias.value = []
            const {abrirModalDisponibilizar, erroValidacaoMapa, erroValidacaoMapaTick} = useMapaDisponibilizacao(deps as any)
            abrirModalDisponibilizar()
            expect(erroValidacaoMapa.value).toBe(TEXTOS.mapa.ERRO_MAPA_SEM_COMPETENCIAS)
            expect(erroValidacaoMapaTick.value).toBe(1)
            expect(deps.mostrarModalDisponibilizar.value).toBe(false)
        })

        it('deve abrir o modal se estiver válido', () => {
            const deps = criarDependencias()
            const {abrirModalDisponibilizar} = useMapaDisponibilizacao(deps as any)
            abrirModalDisponibilizar()
            expect(deps.mostrarModalDisponibilizar.value).toBe(true)
            expect(deps.limparErros).toHaveBeenCalled()
        })
    })

    it('deve disponibilizar o mapa com sucesso', async () => {
        const deps = criarDependencias()
        const {disponibilizarMapa, loadingDisponibilizacao} = useMapaDisponibilizacao(deps as any)
        const request = {dataLimite: '2026-12-31', observacoes: 'teste'}

        const promise = disponibilizarMapa(request)
        expect(loadingDisponibilizacao.value).toBe(true)
        await promise

        expect(deps.disponibilizarMapaFluxo).toHaveBeenCalledWith(123, request)
        expect(deps.concluirAcaoPainel).toHaveBeenCalledWith(TEXTOS_SUCESSO_MAPA.MAPA_DISPONIBILIZADO, expect.any(Function))
        expect(loadingDisponibilizacao.value).toBe(false)
    })

    it('deve tratar erro na disponibilização', async () => {
        const deps = criarDependencias()
        deps.disponibilizarMapaFluxo.mockRejectedValue(new Error('Erro'))
        const {disponibilizarMapa} = useMapaDisponibilizacao(deps as any)

        await disponibilizarMapa({dataLimite: '2026-12-31', observacoes: ''})

        expect(deps.aplicarErroNormalizado).toHaveBeenCalled()
    })

    it('deve sincronizar o mapa no contexto', () => {
        const deps = criarDependencias()
        const {sincronizarMapaContexto} = useMapaDisponibilizacao(deps as any)
        const definirMapaCompleto = vi.fn()
        const mapaContextoAtual = ref({detalhes: {codigo: 123}, mapa: {} as any})
        const novoMapa = {codigo: 1, competencias: []} as any

        sincronizarMapaContexto({
            mapaAtualizado: novoMapa,
            codigoSubprocesso: 123,
            definirMapaCompleto,
            mapaContextoAtual,
        })

        expect(definirMapaCompleto).toHaveBeenCalledWith(123, novoMapa)
        expect(mapaContextoAtual.value.mapa).toEqual(novoMapa)
    })

    it('deve limpar erros do mapa', () => {
        const deps = criarDependencias()
        const {limparErroMapa, erroValidacaoMapa, erroValidacaoMapaTick} = useMapaDisponibilizacao(deps as any)
        const erroMapa = ref<string | null>('erro')
        erroValidacaoMapa.value = 'erro local'

        limparErroMapa(erroMapa)

        expect(erroValidacaoMapa.value).toBe('')
        expect(erroValidacaoMapaTick.value).toBe(1)
        expect(erroMapa.value).toBe(null)
    })
})
