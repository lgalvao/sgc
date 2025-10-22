import {afterEach, describe, expect, it, vi, beforeEach} from 'vitest'
import { createPinia, setActivePinia } from 'pinia';
import * as service from '../mapaService'
import api from '@/axios-setup'
import * as mappers from '@/mappers/mapas'
import {Mapa, SalvarMapaRequest} from '@/types/tipos'

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
vi.mock('@/mappers/mapas', () => ({
  mapMapaDtoToModel: vi.fn((dto) => ({ ...dto, mapped: true })),
}))

describe('mapaService', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
  });
  const mockApi = api as any;
  const mockMappers = mappers as any;

  afterEach(() => {
    vi.clearAllMocks()
    mockApi.get.mockClear()
    mockApi.post.mockClear()
    mockApi.put.mockClear()
    mockApi.delete.mockClear()
  })

  it('listarMapas should fetch and map mapas', async () => {
    const dtoList = [{ id: 1, nome: 'Mapa DTO' }]
    mockApi.get.mockResolvedValue({ data: dtoList })

    const result = await service.listarMapas()

    expect(mockApi.get).toHaveBeenCalledWith('/mapas')
    expect(mockMappers.mapMapaDtoToModel).toHaveBeenCalled()
    expect(mockMappers.mapMapaDtoToModel.mock.calls[0][0]).toEqual(dtoList[0])
    expect(result[0]).toHaveProperty('mapped', true)
  })

  it('obterMapa should fetch and map a mapa', async () => {
    const dto = { id: 1, nome: 'Mapa DTO' }
    mockApi.get.mockResolvedValue({ data: dto })

    const result = await service.obterMapa(1)

    expect(mockApi.get).toHaveBeenCalledWith('/mapas/1')
    expect(mockMappers.mapMapaDtoToModel).toHaveBeenCalledWith(dto)
    expect(result).toHaveProperty('mapped', true)
  })

  it('criarMapa should post and map response', async () => {
    const request: SalvarMapaRequest = { competencias: [] }
    const responseDto = { id: 2, ...request }
    mockApi.post.mockResolvedValue({ data: responseDto })

    const result = await service.criarMapa(request)

    expect(mockApi.post).toHaveBeenCalledWith('/mapas', request)
    expect(mockMappers.mapMapaDtoToModel).toHaveBeenCalledWith(responseDto)
    expect(result).toHaveProperty('mapped', true)
  })

  it('atualizarMapa should put and map response', async () => {
    const request: Mapa = { codigo: 1, descricao: 'Mapa Atualizado', competencias: [], dataCriacao: '2025-01-01', idProcesso: 1, situacao: 'EM_ELABORACAO', unidade: null as any }
    const responseDto = { ...request }
    mockApi.put.mockResolvedValue({ data: responseDto })

    const result = await service.atualizarMapa(request.codigo, request)

    expect(mockApi.put).toHaveBeenCalledWith(`/mapas/${request.codigo}`, request)
    expect(mockMappers.mapMapaDtoToModel).toHaveBeenCalledWith(responseDto)
    expect(result).toHaveProperty('mapped', true)
  })

  it('excluirMapa should call delete', async () => {
    mockApi.delete.mockResolvedValue({})
    await service.excluirMapa(1)
    expect(mockApi.delete).toHaveBeenCalledWith('/mapas/1')
  })

  // Error handling
  it('listarMapas should throw error on failure', async () => {
    mockApi.get.mockRejectedValue(new Error('Failed'))
    await expect(service.listarMapas()).rejects.toThrow()
  })
})
