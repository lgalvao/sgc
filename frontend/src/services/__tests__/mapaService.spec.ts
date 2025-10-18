import { describe, it, expect, vi, afterEach } from 'vitest'
import * as service from '../mapaService'
import api from '@/axios-setup'
import * as mappers from '@/mappers/mapas'
import { Mapa } from '@/types/tipos'

vi.mock('@/axios-setup')
vi.mock('@/mappers/mapas', () => ({
  mapMapaDtoToModel: vi.fn((dto) => ({ ...dto, mapped: true })),
}))

describe('mapaService', () => {
  const mockApi = vi.mocked(api)
  const mockMappers = vi.mocked(mappers)

  afterEach(() => {
    vi.clearAllMocks()
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
    const request: Omit<Mapa, 'id'> = { nome: 'Novo Mapa', tipo: 'MAPEAMENTO' }
    const responseDto = { id: 2, ...request }
    mockApi.post.mockResolvedValue({ data: responseDto })

    const result = await service.criarMapa(request)

    expect(mockApi.post).toHaveBeenCalledWith('/mapas', request)
    expect(mockMappers.mapMapaDtoToModel).toHaveBeenCalledWith(responseDto)
    expect(result).toHaveProperty('mapped', true)
  })

  it('atualizarMapa should put and map response', async () => {
    const request: Mapa = { id: 1, nome: 'Mapa Atualizado', tipo: 'MAPEAMENTO' }
    const responseDto = { ...request }
    mockApi.put.mockResolvedValue({ data: responseDto })

    const result = await service.atualizarMapa(1, request)

    expect(mockApi.put).toHaveBeenCalledWith('/mapas/1', request)
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