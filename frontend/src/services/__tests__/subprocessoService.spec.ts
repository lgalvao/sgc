import {describe, expect, it, vi} from 'vitest'
import * as service from '../subprocessoService'
import api from '@/axios-setup'
import * as mappers from '@/mappers/mapas'
import {MapaVisualizacao} from '@/types/tipos'

vi.mock('@/axios-setup', () => {
    return {
        default: {
            get: vi.fn(),
            post: vi.fn(),
            put: vi.fn(),
            delete: vi.fn(),
        },
    };
});
vi.mock('@/mappers/mapas', async (importOriginal) => {
    const original = await importOriginal()
    return {
        ...(original as any),
        mapImpactoMapaDtoToModel: vi.fn((dto) => ({ ...dto, mapped: true })),
        mapMapaCompletoDtoToModel: vi.fn((dto) => ({ ...dto, mapped: true })),
        mapMapaAjusteDtoToModel: vi.fn((dto) => ({ ...dto, mapped: true })),
    }
})

describe('subprocessoService', () => {
    const mockApi = api as any;
    const mockMappers = mappers as any;
    const id = 1

    it('importarAtividades should post to the correct endpoint', async () => {
        const idOrigem = 2
        mockApi.post.mockResolvedValue({})
        await service.importarAtividades(id, idOrigem)
        expect(mockApi.post).toHaveBeenCalledWith(`/subprocessos/${id}/importar-atividades`, {
            subprocessoOrigemId: idOrigem,
        })
    })

    it('obterMapaVisualizacao should fetch data', async () => {
        const responseData: MapaVisualizacao = { codigo: 1, descricao: 'Viz', competencias: [] }
        mockApi.get.mockResolvedValue({ data: responseData })
        const result = await service.obterMapaVisualizacao(id)
        expect(mockApi.get).toHaveBeenCalledWith(`/subprocessos/${id}/mapa-visualizacao`)
        expect(result).toEqual(responseData)
    })

    it('verificarImpactosMapa should fetch and map data', async () => {
        const responseDto = { temImpactos: true }
        mockApi.get.mockResolvedValue({ data: responseDto })
        const result = await service.verificarImpactosMapa(id)
        expect(mockApi.get).toHaveBeenCalledWith(`/subprocessos/${id}/impactos-mapa`)
        expect(mockMappers.mapImpactoMapaDtoToModel).toHaveBeenCalledWith(responseDto)
        expect(result).toHaveProperty('mapped', true)
    })

    it('obterMapaCompleto should fetch and map data', async () => {
        const responseDto = { id: 1, nome: 'Completo' }
        mockApi.get.mockResolvedValue({ data: responseDto })
        const result = await service.obterMapaCompleto(id)
        expect(mockApi.get).toHaveBeenCalledWith(`/subprocessos/${id}/mapa-completo`)
        expect(mockMappers.mapMapaCompletoDtoToModel).toHaveBeenCalledWith(responseDto)
        expect(result).toHaveProperty('mapped', true)
    })

    it('salvarMapaCompleto should put and map data', async () => {
        const data = { nome: 'Mapa Salvo' }
        const responseDto = { id: 1, ...data }
        mockApi.put.mockResolvedValue({ data: responseDto })
        const result = await service.salvarMapaCompleto(id, data)
        expect(mockApi.put).toHaveBeenCalledWith(`/subprocessos/${id}/mapa-completo`, data)
        expect(mockMappers.mapMapaCompletoDtoToModel).toHaveBeenCalledWith(responseDto)
        expect(result).toHaveProperty('mapped', true)
    })

    it('obterMapaAjuste should fetch and map data', async () => {
        const responseDto = { id: 1, nome: 'Ajuste' }
        mockApi.get.mockResolvedValue({ data: responseDto })
        const result = await service.obterMapaAjuste(id)
        expect(mockApi.get).toHaveBeenCalledWith(`/subprocessos/${id}/mapa-ajuste`)
        expect(mockMappers.mapMapaAjusteDtoToModel).toHaveBeenCalledWith(responseDto)
        expect(result).toHaveProperty('mapped', true)
    })

    it('salvarMapaAjuste should put data', async () => {
        const data = { nome: 'Ajuste Salvo' }
        mockApi.put.mockResolvedValue({})
        await service.salvarMapaAjuste(id, data)
        expect(mockApi.put).toHaveBeenCalledWith(`/subprocessos/${id}/mapa-ajuste`, data)
    })

    // Error handling
    it('importarAtividades should throw error on failure', async () => {
        mockApi.post.mockRejectedValue(new Error('Failed'))
        await expect(service.importarAtividades(1, 2)).rejects.toThrow()
    })
})
