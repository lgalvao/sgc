import {beforeEach, describe, expect, it, vi} from 'vitest'
import {createPinia, setActivePinia} from 'pinia'
import {useFeedback} from '../useFeedback'
import logger from '@/utils/logger'

vi.mock('html2canvas', () => ({
    default: vi.fn().mockResolvedValue({
        toBlob: vi.fn((cb: (blob: Blob | null) => void) => cb(new Blob(['fake'], {type: 'image/webp'}))),
    }),
}))

const {mockPerfilStore, mockPost, mockRoute} = vi.hoisted(() => ({
    mockPerfilStore: {
        usuarioCodigo: '12345' as string | null,
        usuarioNome: 'João Testador' as string | null,
        perfilSelecionado: 'CHEFE' as string | null,
        unidadeSelecionadaSigla: 'SENIC' as string | null,
    },
    mockRoute: {
        name: 'painel' as string | null,
        fullPath: '/painel',
        query: {},
    },
    mockPost: vi.fn().mockResolvedValue({data: {id: 'uuid-123'}})
}))

vi.mock('vue-router', () => ({
    useRoute: () => mockRoute,
}))

vi.mock('@/stores/perfil', () => ({
    usePerfilStore: () => mockPerfilStore,
}))

vi.mock('@/axios-setup', () => ({
    default: {post: mockPost},
}))

describe('useFeedback', () => {
    beforeEach(() => {
        setActivePinia(createPinia())
        vi.clearAllMocks()
        mockPost.mockResolvedValue({data: {id: 'uuid-123'}})

        mockPerfilStore.usuarioCodigo = '12345';
        mockPerfilStore.usuarioNome = 'João Testador';
        mockPerfilStore.perfilSelecionado = 'CHEFE';
        mockPerfilStore.unidadeSelecionadaSigla = 'SENIC';

        mockRoute.name = 'painel';
        mockRoute.fullPath = '/painel';
        mockRoute.query = {};
    })

    it('deve montar metadados com dados do perfil e da rota', async () => {
        const {enviarFeedback} = useFeedback()

        await enviarFeedback('bug', 'Problema encontrado no sistema')

        expect(mockPost).toHaveBeenCalled()
        const formData: FormData = mockPost.mock.calls[0][1] as FormData
        const dataJson = formData.get('data') as string
        const payload = JSON.parse(dataJson)

        expect(payload.tipo).toBe('bug')
        expect(payload.nota).toBe('Problema encontrado no sistema')
        expect(payload.metadados.usuarioCodigo).toBe('12345')
        expect(payload.metadados.rotaCaminho).toBe('/painel')
        expect(payload.metadados.perfilAtivo).toBe('CHEFE')
    })

    it('deve montar metadados com valores default quando perfil e rota não têm as informações', async () => {
        mockPerfilStore.usuarioCodigo = null;
        mockPerfilStore.usuarioNome = null;
        mockPerfilStore.perfilSelecionado = null;
        mockPerfilStore.unidadeSelecionadaSigla = null;
        mockRoute.name = null;

        const {enviarFeedback} = useFeedback()

        await enviarFeedback('sugestao', 'Sugestao anonima')

        expect(mockPost).toHaveBeenCalled()
        const formData: FormData = mockPost.mock.calls[0][1] as FormData
        const dataJson = formData.get('data') as string
        const payload = JSON.parse(dataJson)

        expect(payload.metadados.usuarioCodigo).toBe('')
        expect(payload.metadados.usuarioNome).toBe('')
        expect(payload.metadados.rotaNome).toBe('')
        expect(payload.metadados.perfilAtivo).toBeNull()
        expect(payload.metadados.unidadeAtiva).toBeNull()
    })

    it('deve incluir screenshot no FormData quando captura estiver disponível', async () => {
        const fb = useFeedback()

        await fb.capturarTela()
        expect(fb.captura.value).not.toBeNull()

        await fb.enviarFeedback('sugestao', 'Sugestão de melhoria para a tela')

        expect(mockPost).toHaveBeenCalled()
        const formData: FormData = mockPost.mock.calls[0][1] as FormData
        expect(formData.has('screenshot')).toBe(true)
    })

    it('deve enviar sem screenshot quando captura estiver nula', async () => {
        const fb = useFeedback()

        await fb.enviarFeedback('questao', 'Dúvida sobre o funcionamento do sistema')

        expect(mockPost).toHaveBeenCalled()
        const formData: FormData = mockPost.mock.calls[0][1] as FormData
        expect(formData.has('screenshot')).toBe(false)
    })

    it('deve limpar captura ao chamar removerCaptura', async () => {
        const fb = useFeedback()

        await fb.capturarTela()
        expect(fb.captura.value).not.toBeNull()

        fb.removerCaptura()
        expect(fb.captura.value).toBeNull()
    })

    it('deve continuar sem screenshot quando html2canvas falha', async () => {
        const spyError = vi.spyOn(logger, 'error').mockImplementation(() => {
        })
        const html2canvas = (await import('html2canvas')).default
        vi.mocked(html2canvas).mockRejectedValueOnce(new Error('canvas error'))

        const fb = useFeedback()

        await fb.capturarTela()
        expect(fb.captura.value).toBeNull()
        expect(spyError).toHaveBeenCalledWith(
            expect.stringContaining('[Feedback] Captura de tela falhou'),
            expect.any(Error)
        )
        spyError.mockRestore()
    })

    it('deve definir enviando como true durante o envio e false ao finalizar', async () => {
        let resolverPost: (v: any) => void
        mockPost.mockReturnValue(new Promise((resolve) => {
            resolverPost = resolve
        }))

        const fb = useFeedback()

        const promessa = fb.enviarFeedback('sugestao', 'Sistema muito bom, parabéns ao time')
        expect(fb.enviando.value).toBe(true)

        resolverPost!({data: {id: '1'}})
        await promessa
        expect(fb.enviando.value).toBe(false)
    })
})

