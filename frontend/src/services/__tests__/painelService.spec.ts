import {afterEach, describe, expect, it, vi, type Mocked} from 'vitest'
import * as service from '../painelService'
import api from '@/axios-setup'
import * as processoMappers from '@/mappers/processos'
import * as alertaMappers from '@/mappers/alertas'

vi.mock('@/axios-setup')
vi.mock('@/mappers/processos', () => ({
  mapProcessoResumoDtoToFrontend: vi.fn((dto) => ({ ...dto, mapped: true })),
}))
vi.mock('@/mappers/alertas', () => ({
  mapAlertaDtoToFrontend: vi.fn((dto) => ({ ...dto, mapped: true })),
}))

describe('painelService', () => {
  const mockApi = api as Mocked<typeof api>
  const mockProcessoMappers = vi.mocked(processoMappers)
  const mockAlertaMappers = vi.mocked(alertaMappers)

  afterEach(() => {
    vi.clearAllMocks()
  })

  describe('listarProcessos', () => {
    it('should fetch and map processos', async () => {
      const dtoList = [{ id: 1, tipo: 'MAPEAMENTO' }]
      const responseData = {
        content: dtoList,
        totalPages: 1,
        totalElements: 1,
        number: 0,
        size: 20,
        first: true,
        last: true,
        empty: false,
      }
      mockApi.get.mockResolvedValueOnce({ data: responseData })

      const result = await service.listarProcessos('CHEFE', 1)

      expect(mockApi.get).toHaveBeenCalledWith('/painel/processos', {
        params: { perfil: 'CHEFE', unidade: 1, page: 0, size: 20 },
      })
      expect(mockProcessoMappers.mapProcessoResumoDtoToFrontend).toHaveBeenCalled()
      expect(mockProcessoMappers.mapProcessoResumoDtoToFrontend.mock.calls[0][0]).toEqual(dtoList[0])
      expect(result.content[0]).toHaveProperty('mapped', true)
      expect(result.totalPages).toBe(1)
    })

    it('should handle different pagination', async () => {
        mockApi.get.mockResolvedValueOnce({ data: { content: [] } })
        await service.listarProcessos('GESTOR', undefined, 2, 10)
        expect(mockApi.get).toHaveBeenCalledWith('/painel/processos', {
            params: { perfil: 'GESTOR', unidade: undefined, page: 2, size: 10 },
        })
    })

    it('should throw an error on failure', async () => {
      mockApi.get.mockRejectedValueOnce(new Error('Failed'))
      await expect(service.listarProcessos('CHEFE')).rejects.toThrow()
    })
  })

  describe('listarAlertas', () => {
    it('should fetch and map alertas', async () => {
        const dtoList = [{ id: 1, mensagem: 'Alerta DTO' }]
        const responseData = {
            content: dtoList,
            totalPages: 1,
            totalElements: 1,
            number: 0,
            size: 20,
            first: true,
            last: true,
            empty: false,
          }
        mockApi.get.mockResolvedValueOnce({ data: responseData })

        const result = await service.listarAlertas('123', 1)

        expect(mockApi.get).toHaveBeenCalledWith('/painel/alertas', {
          params: { usuarioTitulo: '123', unidade: 1, page: 0, size: 20 },
        })
        expect(mockAlertaMappers.mapAlertaDtoToFrontend).toHaveBeenCalled()
        expect(mockAlertaMappers.mapAlertaDtoToFrontend.mock.calls[0][0]).toEqual(dtoList[0])
        expect(result.content[0]).toHaveProperty('mapped', true)
        expect(result.totalElements).toBe(1)
      })

      it('should throw an error on failure', async () => {
        mockApi.get.mockRejectedValueOnce(new Error('Failed'))
        await expect(service.listarAlertas('123')).rejects.toThrow()
      })
  })
})