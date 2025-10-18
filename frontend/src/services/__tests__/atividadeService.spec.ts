import { describe, it, expect, vi, afterEach } from 'vitest'
import * as service from '../atividadeService'
import api from '@/axios-setup'
import * as mappers from '@/mappers/atividades'
import { Atividade, Conhecimento } from '@/types/tipos'

vi.mock('@/axios-setup')
vi.mock('@/mappers/atividades', () => ({
  mapAtividadeDtoToModel: vi.fn((dto) => ({ ...dto, mapped: true })),
  mapConhecimentoDtoToModel: vi.fn((dto) => ({ ...dto, mapped: true })),
  mapCriarAtividadeRequestToDto: vi.fn((req) => ({ ...req, mapped: true })),
  mapCriarConhecimentoRequestToDto: vi.fn((req) => ({ ...req, mapped: true })),
}))

describe('atividadeService', () => {
  const mockApi = vi.mocked(api)
  const mockMappers = vi.mocked(mappers)

  afterEach(() => {
    vi.clearAllMocks()
  })

  it('listarAtividades should fetch and map atividades', async () => {
    const dtoList = [{ id: 1, descricao: 'Atividade DTO' }]
    mockApi.get.mockResolvedValue({ data: dtoList })

    const result = await service.listarAtividades()

    expect(mockApi.get).toHaveBeenCalledWith('/atividades')
    expect(mockMappers.mapAtividadeDtoToModel).toHaveBeenCalled()
    expect(mockMappers.mapAtividadeDtoToModel.mock.calls[0][0]).toEqual(dtoList[0])
    expect(result[0]).toHaveProperty('mapped', true)
  })

  it('obterAtividadePorId should fetch and map an atividade', async () => {
    const dto = { id: 1, descricao: 'Atividade DTO' }
    mockApi.get.mockResolvedValue({ data: dto })

    const result = await service.obterAtividadePorId(1)

    expect(mockApi.get).toHaveBeenCalledWith('/atividades/1')
    expect(mockMappers.mapAtividadeDtoToModel).toHaveBeenCalledWith(dto)
    expect(result).toHaveProperty('mapped', true)
  })

  it('criarAtividade should map request, post, and map response', async () => {
    const request = { descricao: 'Nova Atividade' }
    const requestDto = { ...request, mapped: true }
    const responseDto = { id: 2, ...requestDto }
    mockApi.post.mockResolvedValue({ data: responseDto })

    const result = await service.criarAtividade(request, 123)

    expect(mockMappers.mapCriarAtividadeRequestToDto).toHaveBeenCalledWith(request, 123)
    expect(mockApi.post).toHaveBeenCalledWith('/atividades', requestDto)
    expect(mockMappers.mapAtividadeDtoToModel).toHaveBeenCalledWith(responseDto)
    expect(result).toHaveProperty('mapped', true)
  })

  it('atualizarAtividade should put and map response', async () => {
    const request: Atividade = { id: 1, descricao: 'Atividade Atualizada' }
    const responseDto = { ...request }
    mockApi.put.mockResolvedValue({ data: responseDto })

    const result = await service.atualizarAtividade(1, request)

    expect(mockApi.put).toHaveBeenCalledWith('/atividades/1', request)
    expect(mockMappers.mapAtividadeDtoToModel).toHaveBeenCalledWith(responseDto)
    expect(result).toHaveProperty('mapped', true)
  })

  it('excluirAtividade should call delete', async () => {
    mockApi.delete.mockResolvedValue({})
    await service.excluirAtividade(1)
    expect(mockApi.delete).toHaveBeenCalledWith('/atividades/1')
  })

  it('listarConhecimentos should fetch and map conhecimentos', async () => {
    const dtoList = [{ id: 1, descricao: 'Conhecimento DTO' }]
    mockApi.get.mockResolvedValue({ data: dtoList })

    const result = await service.listarConhecimentos(1)

    expect(mockApi.get).toHaveBeenCalledWith('/atividades/1/conhecimentos')
    expect(mockMappers.mapConhecimentoDtoToModel).toHaveBeenCalled()
    expect(mockMappers.mapConhecimentoDtoToModel.mock.calls[0][0]).toEqual(dtoList[0])
    expect(result[0]).toHaveProperty('mapped', true)
  })

  it('criarConhecimento should map request, post, and map response', async () => {
    const request = { descricao: 'Novo Conhecimento' }
    const requestDto = { ...request, mapped: true }
    const responseDto = { id: 2, ...requestDto }
    mockApi.post.mockResolvedValue({ data: responseDto })

    const result = await service.criarConhecimento(1, request)

    expect(mockMappers.mapCriarConhecimentoRequestToDto).toHaveBeenCalledWith(request)
    expect(mockApi.post).toHaveBeenCalledWith('/atividades/1/conhecimentos', requestDto)
    expect(mockMappers.mapConhecimentoDtoToModel).toHaveBeenCalledWith(responseDto)
    expect(result).toHaveProperty('mapped', true)
  })

  it('atualizarConhecimento should put and map response', async () => {
    const request: Conhecimento = { id: 1, descricao: 'Conhecimento Atualizado' }
    const responseDto = { ...request }
    mockApi.put.mockResolvedValue({ data: responseDto })

    const result = await service.atualizarConhecimento(1, 1, request)

    expect(mockApi.put).toHaveBeenCalledWith('/atividades/1/conhecimentos/1', request)
    expect(mockMappers.mapConhecimentoDtoToModel).toHaveBeenCalledWith(responseDto)
    expect(result).toHaveProperty('mapped', true)
  })

  it('excluirConhecimento should call delete', async () => {
    mockApi.delete.mockResolvedValue({})
    await service.excluirConhecimento(1, 1)
    expect(mockApi.delete).toHaveBeenCalledWith('/atividades/1/conhecimentos/1')
  })

    // Error handling tests
  it('listarAtividades should throw error on failure', async () => {
    mockApi.get.mockRejectedValue(new Error('Failed'))
    await expect(service.listarAtividades()).rejects.toThrow()
  })

  it('criarConhecimento should throw error on failure', async () => {
    const request = { descricao: 'Novo Conhecimento' }
    mockApi.post.mockRejectedValue(new Error('Failed'))
    await expect(service.criarConhecimento(1, request)).rejects.toThrow()
  })

  it('excluirConhecimento should throw error on failure', async () => {
    mockApi.delete.mockRejectedValue(new Error('Failed'))
    await expect(service.excluirConhecimento(1, 1)).rejects.toThrow()
  })
})