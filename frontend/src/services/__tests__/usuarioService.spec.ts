import {afterEach, describe, expect, it, vi, type Mocked} from 'vitest'
import * as service from '../usuarioService'
import api from '@/axios-setup'
import * as mappers from '@/mappers/sgrh'

vi.mock('@/axios-setup')
vi.mock('@/mappers/sgrh', () => ({
    mapPerfilUnidadeToFrontend: vi.fn((dto) => ({ ...dto, mapped: true })),
}))

describe('usuarioService', () => {
    const mockApi = api as Mocked<typeof api>
    const mockMappers = vi.mocked(mappers)

    afterEach(() => {
        vi.clearAllMocks()
    })
    it('autenticar should post request and return boolean', async () => {
        const request = { tituloEleitoral: 123, senha: '123' }
        mockApi.post.mockResolvedValueOnce({ data: true })

        const result = await service.autenticar(request)

        expect(mockApi.post).toHaveBeenCalledWith('/usuarios/autenticar', request)
        expect(result).toBe(true)
    })

    it('autorizar should post, map, and return response', async () => {
        const tituloEleitoral = 123
        const responseDto = [{ perfil: 'CHEFE', unidade: 'UNIT' }]
        mockApi.post.mockResolvedValueOnce({ data: responseDto })

        const result = await service.autorizar(tituloEleitoral)

        expect(mockApi.post).toHaveBeenCalledWith('/usuarios/autorizar', tituloEleitoral, {
            headers: { 'Content-Type': 'application/json' },
        })
        expect(mockMappers.mapPerfilUnidadeToFrontend).toHaveBeenCalled()
        expect(mockMappers.mapPerfilUnidadeToFrontend.mock.calls[0][0]).toEqual(responseDto[0])
        expect(result[0]).toHaveProperty('mapped', true)
    })

    it('entrar should post the request', async () => {
        const request = { tituloEleitoral: 123, perfil: 'GESTOR', unidadeCodigo: 1 }
        mockApi.post.mockResolvedValueOnce({})

        await service.entrar(request)

        expect(mockApi.post).toHaveBeenCalledWith('/usuarios/entrar', request)
    })

    // Error handling
    it('autenticar should throw error on failure', async () => {
        const request = { tituloEleitoral: 123, senha: '123' }
        mockApi.post.mockRejectedValueOnce(new Error('Failed'))
        await expect(service.autenticar(request)).rejects.toThrow()
    })
})