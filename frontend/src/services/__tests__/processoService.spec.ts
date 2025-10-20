import { describe, it, expect, vi, afterEach } from 'vitest'
import * as service from '../processoService'
import api from '@/axios-setup'
import * as mappers from '@/mappers/processos'

import { vi } from 'vitest';
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
vi.mock('@/mappers/processos', async (importOriginal) => {
    const original = await importOriginal()
    return {
        ...original,
        mapProcessoDtoToFrontend: vi.fn((dto) => ({ ...dto, mapped: true })),
        mapProcessoDetalheDtoToFrontend: vi.fn((dto) => ({ ...dto, mapped: true })),
    }
})

describe('processoService', () => {
    const mockApi = vi.mocked(api)
    const mockMappers = vi.mocked(mappers)

    afterEach(() => {
        vi.clearAllMocks()
    })

    it('criarProcesso should post and map response', async () => {
        const request = { descricao: 'teste', tipo: 'MAPEAMENTO', dataLimiteEtapa1: '2025-12-31', unidades: [1] }
        const responseDto = { id: 1, ...request }
        mockApi.post.mockResolvedValue({ data: responseDto })

        const result = await service.criarProcesso(request)

        expect(mockApi.post).toHaveBeenCalledWith('/processos', request)
        expect(mockMappers.mapProcessoDtoToFrontend).toHaveBeenCalledWith(responseDto)
        expect(result).toHaveProperty('mapped', true)
    })

    it('fetchProcessosFinalizados should fetch and map response', async () => {
        const dtoList = [{ id: 1, situacao: 'FINALIZADO' }]
        mockApi.get.mockResolvedValue({ data: dtoList })

        const result = await service.fetchProcessosFinalizados()

        expect(mockApi.get).toHaveBeenCalledWith('/processos/finalizados')
        expect(mockMappers.mapProcessoDtoToFrontend).toHaveBeenCalled()
        expect(mockMappers.mapProcessoDtoToFrontend.mock.calls[0][0]).toEqual(dtoList[0])
        expect(result[0]).toHaveProperty('mapped', true)
    })

    it('iniciarProcesso should post with correct params', async () => {
        mockApi.post.mockResolvedValue({})
        await service.iniciarProcesso(1, 'REVISAO', [10, 20])
        expect(mockApi.post).toHaveBeenCalledWith('/processos/1/iniciar?tipo=REVISAO', [10, 20])
    })

    it('finalizarProcesso should post to the correct endpoint', async () => {
        mockApi.post.mockResolvedValue({})
        await service.finalizarProcesso(1)
        expect(mockApi.post).toHaveBeenCalledWith('/processos/1/finalizar')
    })

    it('obterProcessoPorId should fetch and map a processo', async () => {
        const responseDto = { id: 1 }
        mockApi.get.mockResolvedValue({ data: responseDto })

        const result = await service.obterProcessoPorId(1)

        expect(mockApi.get).toHaveBeenCalledWith('/processos/1')
        expect(mockMappers.mapProcessoDtoToFrontend).toHaveBeenCalledWith(responseDto)
        expect(result).toHaveProperty('mapped', true)
    })

    it('atualizarProcesso should put and map response', async () => {
        const request = { nome: 'Processo Atualizado' }
        const responseDto = { id: 1, ...request }
        mockApi.put.mockResolvedValue({ data: responseDto })

        const result = await service.atualizarProcesso(1, request)

        expect(mockApi.put).toHaveBeenCalledWith('/processos/1', request)
        expect(mockMappers.mapProcessoDtoToFrontend).toHaveBeenCalledWith(responseDto)
        expect(result).toHaveProperty('mapped', true)
    })

    it('excluirProcesso should call delete', async () => {
        mockApi.delete.mockResolvedValue({})
        await service.excluirProcesso(1)
        expect(mockApi.delete).toHaveBeenCalledWith('/processos/1')
    })

    it('obterDetalhesProcesso should fetch and map details', async () => {
        const responseDto = { id: 1, subprocessos: [] }
        mockApi.get.mockResolvedValue({ data: responseDto })

        const result = await service.obterDetalhesProcesso(1)

        expect(mockApi.get).toHaveBeenCalledWith('/processos/1/detalhes')
        expect(mockMappers.mapProcessoDetalheDtoToFrontend).toHaveBeenCalledWith(responseDto)
        expect(result).toHaveProperty('mapped', true)
    })

    // Error handling
    it('criarProcesso should throw error on failure', async () => {
        const request = { tipo: 'MAPEAMENTO' }
        mockApi.post.mockRejectedValue(new Error('Failed'))
        await expect(service.criarProcesso(request)).rejects.toThrow()
    })
})