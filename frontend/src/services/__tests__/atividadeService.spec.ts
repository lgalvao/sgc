import {afterEach, describe, expect, it, vi} from 'vitest'
import * as service from '../atividadeService'
import * as mappers from '@/mappers/atividades'
import {Atividade, Conhecimento} from '@/types/tipos'

import apiClient from '@/axios-setup' // Importar o apiClient real

// Criar spies para os métodos do apiClient
const getSpy = vi.spyOn(apiClient, 'get')
const postSpy = vi.spyOn(apiClient, 'post')
const putSpy = vi.spyOn(apiClient, 'put')
const deleteSpy = vi.spyOn(apiClient, 'delete')

vi.mock('@/mappers/atividades', () => ({
  mapAtividadeDtoToModel: vi.fn((dto) => ({ ...dto, mapped: true })),
  mapConhecimentoDtoToModel: vi.fn((dto) => ({ ...dto, mapped: true })),
  mapCriarAtividadeRequestToDto: vi.fn((req) => ({ ...req, mapped: true })),
  mapCriarConhecimentoRequestToDto: vi.fn((req) => ({ ...req, mapped: true })),
}))

describe('atividadeService', () => {
  afterEach(() => {
    vi.clearAllMocks()
    // Resetar os mocks dos spies após cada teste
    getSpy.mockReset()
    postSpy.mockReset()
    putSpy.mockReset()
    deleteSpy.mockReset()
  })

  it('listarAtividades should fetch and map atividades', async () => {
    const dtoList = [{ id: 1, descricao: 'Atividade DTO' }]
    getSpy.mockResolvedValue({ data: dtoList })

    const result = await service.listarAtividades()

    expect(getSpy).toHaveBeenCalledWith('/atividades')
    expect(mappers.mapAtividadeDtoToModel).toHaveBeenCalled()
    expect((mappers.mapAtividadeDtoToModel as any).mock.calls[0][0]).toEqual(dtoList[0])
    expect(result[0]).toHaveProperty('mapped', true)
  })

  it('obterAtividadePorId should fetch and map an atividade', async () => {
    const dto = { id: 1, descricao: 'Atividade DTO' }
    getSpy.mockResolvedValue({ data: dto })

    const result = await service.obterAtividadePorId(1)

    expect(getSpy).toHaveBeenCalledWith('/atividades/1')
    expect(mappers.mapAtividadeDtoToModel).toHaveBeenCalledWith(dto)
    expect(result).toHaveProperty('mapped', true)
  })

  it('criarAtividade should map request, post, and map response', async () => {
    const request = { descricao: 'Nova Atividade' }
    const requestDto = { ...request, mapped: true }
    const responseDto = { id: 2, ...requestDto }
    postSpy.mockResolvedValue({ data: responseDto })

    const result = await service.criarAtividade(request, 123)

    expect(mappers.mapCriarAtividadeRequestToDto).toHaveBeenCalledWith(request, 123)
    expect(postSpy).toHaveBeenCalledWith('/atividades', requestDto)
    expect(mappers.mapAtividadeDtoToModel).toHaveBeenCalledWith(responseDto)
    expect(result).toHaveProperty('mapped', true)
  })

  it('atualizarAtividade should put and map response', async () => {
    const request: Atividade = { codigo: 1, descricao: 'Atividade Atualizada', conhecimentos: [] }
    const responseDto = { ...request }
    putSpy.mockResolvedValue({ data: responseDto })

    const result = await service.atualizarAtividade(1, request)

    expect(putSpy).toHaveBeenCalledWith('/atividades/1', request)
    expect(mappers.mapAtividadeDtoToModel).toHaveBeenCalledWith(responseDto)
    expect(result).toHaveProperty('mapped', true)
  })

  it('excluirAtividade should call delete', async () => {
    deleteSpy.mockResolvedValue({})
    await service.excluirAtividade(1)
    expect(deleteSpy).toHaveBeenCalledWith('/atividades/1')
  })

  it('listarConhecimentos should fetch and map conhecimentos', async () => {
    const dtoList = [{ id: 1, descricao: 'Conhecimento DTO' }]
    getSpy.mockResolvedValue({ data: dtoList })

    const result = await service.listarConhecimentos(1)

    expect(getSpy).toHaveBeenCalledWith('/atividades/1/conhecimentos')
    expect(mappers.mapConhecimentoDtoToModel).toHaveBeenCalled()
    expect((mappers.mapConhecimentoDtoToModel as any).mock.calls[0][0]).toEqual(dtoList[0])
    expect(result[0]).toHaveProperty('mapped', true)
  })

  it('criarConhecimento should map request, post, and map response', async () => {
    const request = { descricao: 'Novo Conhecimento' }
    const requestDto = { ...request, mapped: true }
    const responseDto = { id: 2, ...requestDto }
    postSpy.mockResolvedValue({ data: responseDto })

    const result = await service.criarConhecimento(1, request)

    expect(mappers.mapCriarConhecimentoRequestToDto).toHaveBeenCalledWith(request)
    expect(postSpy).toHaveBeenCalledWith('/atividades/1/conhecimentos', requestDto)
    expect(mappers.mapConhecimentoDtoToModel).toHaveBeenCalledWith(responseDto)
    expect(result).toHaveProperty('mapped', true)
  })

  it('atualizarConhecimento should put and map response', async () => {
    const request: Conhecimento = { id: 1, descricao: 'Conhecimento Atualizado' }
    const responseDto = { ...request }
    putSpy.mockResolvedValue({ data: responseDto })

    const result = await service.atualizarConhecimento(1, 1, request)

    expect(putSpy).toHaveBeenCalledWith('/atividades/1/conhecimentos/1', request)
    expect(mappers.mapConhecimentoDtoToModel).toHaveBeenCalledWith(responseDto)
    expect(result).toHaveProperty('mapped', true)
  })

  it('excluirConhecimento should call delete', async () => {
    deleteSpy.mockResolvedValue({})
    await service.excluirConhecimento(1, 1)
    expect(deleteSpy).toHaveBeenCalledWith('/atividades/1/conhecimentos/1')
  })

    // Error handling tests
  it('listarAtividades should throw error on failure', async () => {
    getSpy.mockRejectedValue(new Error('Failed'))
    await expect(service.listarAtividades()).rejects.toThrow('Failed')
  })

  it('obterAtividadePorId should throw error on failure', async () => {
    getSpy.mockRejectedValue(new Error('Failed'))
    await expect(service.obterAtividadePorId(1)).rejects.toThrow('Failed')
  })

  it('criarAtividade should throw error on failure', async () => {
    const request = { descricao: 'Nova Atividade' }
    postSpy.mockRejectedValue(new Error('Failed'))
    await expect(service.criarAtividade(request, 123)).rejects.toThrow('Failed')
  })

  it('atualizarAtividade should throw error on failure', async () => {
    const request: Atividade = { codigo: 1, descricao: 'Atividade Atualizada', conhecimentos: [] }
    putSpy.mockRejectedValue(new Error('Failed'))
    await expect(service.atualizarAtividade(1, request)).rejects.toThrow('Failed')
  })

  it('excluirAtividade should throw error on failure', async () => {
    deleteSpy.mockRejectedValue(new Error('Failed'))
    await expect(service.excluirAtividade(1)).rejects.toThrow('Failed')
  })

  it('listarConhecimentos should throw error on failure', async () => {
    getSpy.mockRejectedValue(new Error('Failed'))
    await expect(service.listarConhecimentos(1)).rejects.toThrow('Failed')
  })

  it('criarConhecimento should throw error on failure', async () => {
    const request = { descricao: 'Novo Conhecimento' }
    postSpy.mockRejectedValue(new Error('Failed'))
    await expect(service.criarConhecimento(1, request)).rejects.toThrow('Failed')
  })

  it('atualizarConhecimento should throw error on failure', async () => {
    const request: Conhecimento = { id: 1, descricao: 'Conhecimento Atualizado' }
    putSpy.mockRejectedValue(new Error('Failed'))
    await expect(service.atualizarConhecimento(1, 1, request)).rejects.toThrow('Failed')
  })

  it('excluirConhecimento should throw error on failure', async () => {
    deleteSpy.mockRejectedValue(new Error('Failed'))
    await expect(service.excluirConhecimento(1, 1)).rejects.toThrow('Failed')
  })
})
