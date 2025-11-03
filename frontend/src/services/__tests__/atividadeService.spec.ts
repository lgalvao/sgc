import {afterEach, beforeEach, describe, expect, it, vi} from 'vitest'
import {createPinia, setActivePinia} from 'pinia';
import * as service from '../atividadeService'
import * as mappers from '@/mappers/atividades'
import {Atividade, Conhecimento} from '@/types/tipos'

import apiClient from '@/axios-setup'

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

const mockApi = apiClient as any;

vi.mock('@/mappers/atividades', () => ({
  mapAtividadeDtoToModel: vi.fn((dto) => ({ ...dto, mapped: true })),
  mapConhecimentoDtoToModel: vi.fn((dto) => ({ ...dto, mapped: true })),
  mapCriarAtividadeRequestToDto: vi.fn((req) => ({ ...req, mapped: true })),
  mapCriarConhecimentoRequestToDto: vi.fn((req) => ({ ...req, mapped: true })),
}))

describe('atividadeService', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
  });

  afterEach(() => {
    vi.clearAllMocks()
    mockApi.get.mockClear()
    mockApi.post.mockClear()
    mockApi.put.mockClear()
    mockApi.delete.mockClear()
  })

  it('listarAtividades should fetch and map atividades', async () => {
    const dtoList = [{ id: 1, descricao: 'Atividade DTO' }]
    mockApi.get.mockResolvedValue({ data: dtoList })

    const result = await service.listarAtividades()

    expect(mockApi.get).toHaveBeenCalledWith('/atividades')
    expect(mappers.mapAtividadeDtoToModel).toHaveBeenCalled()
    expect((mappers.mapAtividadeDtoToModel as any).mock.calls[0][0]).toEqual(dtoList[0])
    expect(result[0]).toHaveProperty('mapped', true)
  })

  it('obterAtividadePorId should fetch and map an atividade', async () => {
    const dto = { id: 1, descricao: 'Atividade DTO' }
    mockApi.get.mockResolvedValue({ data: dto })

    const result = await service.obterAtividadePorId(1)

    expect(mockApi.get).toHaveBeenCalledWith('/atividades/1')
    expect(mappers.mapAtividadeDtoToModel).toHaveBeenCalledWith(dto)
    expect(result).toHaveProperty('mapped', true)
  })

  it('criarAtividade should map request, post, and map response', async () => {
    const request = { descricao: 'Nova Atividade' }
    const requestDto = { ...request, mapped: true }
    const responseDto = { id: 2, ...requestDto }
    mockApi.post.mockResolvedValue({ data: responseDto })

    const result = await service.criarAtividade(request, 123)

    expect(mappers.mapCriarAtividadeRequestToDto).toHaveBeenCalledWith(request, 123)
    expect(mockApi.post).toHaveBeenCalledWith('/atividades', requestDto)
    expect(mappers.mapAtividadeDtoToModel).toHaveBeenCalledWith(responseDto)
    expect(result).toHaveProperty('mapped', true)
  })

  it('atualizarAtividade should post and map response', async () => {
    const request: Atividade = { codigo: 1, descricao: 'Atividade Atualizada', conhecimentos: [] }
    const responseDto = { ...request }
    mockApi.post.mockResolvedValue({ data: responseDto })

    const result = await service.atualizarAtividade(request.codigo, request)

    expect(mockApi.post).toHaveBeenCalledWith(`/atividades/${request.codigo}/atualizar`, request)
    expect(mappers.mapAtividadeDtoToModel).toHaveBeenCalledWith(responseDto)
    expect(result).toHaveProperty('mapped', true)
  })

  it('excluirAtividade should call post', async () => {
    mockApi.post.mockResolvedValue({})
    await service.excluirAtividade(1)
    expect(mockApi.post).toHaveBeenCalledWith('/atividades/1/excluir')
  })

  it('listarConhecimentos should fetch and map conhecimentos', async () => {
    const dtoList = [{ id: 1, descricao: 'Conhecimento DTO' }]
    mockApi.get.mockResolvedValue({ data: dtoList })

    const result = await service.listarConhecimentos(1)

    expect(mockApi.get).toHaveBeenCalledWith('/atividades/1/conhecimentos')
    expect(mappers.mapConhecimentoDtoToModel).toHaveBeenCalled()
    expect((mappers.mapConhecimentoDtoToModel as any).mock.calls[0][0]).toEqual(dtoList[0])
    expect(result[0]).toHaveProperty('mapped', true)
  })

  it('criarConhecimento should map request, post, and map response', async () => {
    const request = { descricao: 'Novo Conhecimento' }
    const requestDto = { ...request, mapped: true }
    const responseDto = { id: 2, ...requestDto }
    mockApi.post.mockResolvedValue({ data: responseDto })

    const result = await service.criarConhecimento(1, request)

    expect(mappers.mapCriarConhecimentoRequestToDto).toHaveBeenCalledWith(request)
    expect(mockApi.post).toHaveBeenCalledWith('/atividades/1/conhecimentos', requestDto)
    expect(mappers.mapConhecimentoDtoToModel).toHaveBeenCalledWith(responseDto)
    expect(result).toHaveProperty('mapped', true)
  })

  it('atualizarConhecimento should post and map response', async () => {
    const request: Conhecimento = { id: 1, descricao: 'Conhecimento Atualizado' }
    const responseDto = { ...request }
    mockApi.post.mockResolvedValue({ data: responseDto })

    const result = await service.atualizarConhecimento(1, request.id, request)

    expect(mockApi.post).toHaveBeenCalledWith(`/atividades/1/conhecimentos/${request.id}/atualizar`, request)
    expect(mappers.mapConhecimentoDtoToModel).toHaveBeenCalledWith(responseDto)
    expect(result).toHaveProperty('mapped', true)
  })

  it('excluirConhecimento should call post', async () => {
    mockApi.post.mockResolvedValue({})
    await service.excluirConhecimento(1, 1)
    expect(mockApi.post).toHaveBeenCalledWith('/atividades/1/conhecimentos/1/excluir')
  })

    // Error handling tests
  it('listarAtividades should throw error on failure', async () => {
    mockApi.get.mockRejectedValue(new Error('Failed'))
    await expect(service.listarAtividades()).rejects.toThrow('Failed')
  })

  it('obterAtividadePorId should throw error on failure', async () => {
    mockApi.get.mockRejectedValue(new Error('Failed'))
    await expect(service.obterAtividadePorId(1)).rejects.toThrow('Failed')
  })

  it('criarAtividade should throw error on failure', async () => {
    const request = { descricao: 'Nova Atividade' }
    mockApi.post.mockRejectedValue(new Error('Failed'))
    await expect(service.criarAtividade(request, 123)).rejects.toThrow('Failed')
  })

  it('atualizarAtividade should throw error on failure', async () => {
    const request: Atividade = { codigo: 1, descricao: 'Atividade Atualizada', conhecimentos: [] }
    mockApi.post.mockRejectedValue(new Error('Failed'))
    await expect(service.atualizarAtividade(1, request)).rejects.toThrow('Failed')
  })

  it('excluirAtividade should throw error on failure', async () => {
    mockApi.post.mockRejectedValue(new Error('Failed'))
    await expect(service.excluirAtividade(1)).rejects.toThrow('Failed')
  })

  it('listarConhecimentos should throw error on failure', async () => {
    mockApi.get.mockRejectedValue(new Error('Failed'))
    await expect(service.listarConhecimentos(1)).rejects.toThrow('Failed')
  })

  it('criarConhecimento should throw error on failure', async () => {
    const request = { descricao: 'Novo Conhecimento' }
    mockApi.post.mockRejectedValue(new Error('Failed'))
    await expect(service.criarConhecimento(1, request)).rejects.toThrow('Failed')
  })

  it('atualizarConhecimento should throw error on failure', async () => {
    const request: Conhecimento = { id: 1, descricao: 'Conhecimento Atualizado' }
    mockApi.post.mockRejectedValue(new Error('Failed'))
    await expect(service.atualizarConhecimento(1, 1, request)).rejects.toThrow('Failed')
  })

  it('excluirConhecimento should throw error on failure', async () => {
    mockApi.post.mockRejectedValue(new Error('Failed'))
    await expect(service.excluirConhecimento(1, 1)).rejects.toThrow('Failed')
  })
})
