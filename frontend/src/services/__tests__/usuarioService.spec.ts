import { describe, it, expect, vi, afterEach } from 'vitest'
import * as service from '../usuarioService'
import api from '@/axios-setup'
import * as mappers from '@/mappers/sgrh'

vi.mock('@/axios-setup')
vi.mock('@/mappers/sgrh', async (importOriginal) => {
    const original = await importOriginal()
    return {
        ...original,
        mapPerfilUnidadeToFrontend: vi.fn((dto) => ({ ...dto, mapped: true })),
    }
})

describe('usuarioService', () => {
    const mockApi = vi.mocked(api)
    const mockMappers = vi.mocked(mappers)

    afterEach(() => {
        vi.clearAllMocks()
    })

    it('autenticar should post request and return boolean', async () => {
        const request = { tituloEleitoral: '123' }
        mockApi.post.mockResolvedValue({ data: true })

        const result = await service.autenticar(request)

        expect(mockApi.post).toHaveBeenCalledWith('/usuarios/autenticar', request)
        expect(result).toBe(true)
    })

    it('autorizar should post, map, and return response', async () => {
        const tituloEleitoral = 123
        const responseDto = [{ perfil: 'CHEFE', unidade: 'UNIT' }]
        mockApi.post.mockResolvedValue({ data: responseDto })

        const result = await service.autorizar(tituloEleitoral)

        expect(mockApi.post).toHaveBeenCalledWith('/usuarios/autorizar', tituloEleitoral, {
            headers: { 'Content-Type': 'application/json' },
        })
        expect(mockMappers.mapPerfilUnidadeToFrontend).toHaveBeenCalled()
        expect(mockMappers.mapPerfilUnidadeToFrontend.mock.calls[0][0]).toEqual(responseDto[0])
        expect(result[0]).toHaveProperty('mapped', true)
    })

    it('entrar should post the request', async () => {
        const request = { perfil: 'GESTOR', unidadeId: 1 }
        mockApi.post.mockResolvedValue({})

        await service.entrar(request)

        expect(mockApi.post).toHaveBeenCalledWith('/usuarios/entrar', request)
    })

    // Error handling
    it('autenticar should throw error on failure', async () => {
        const request = { tituloEleitoral: '123' }
        mockApi.post.mockRejectedValue(new Error('Failed'))
        await expect(service.autenticar(request)).rejects.toThrow()
    })
})