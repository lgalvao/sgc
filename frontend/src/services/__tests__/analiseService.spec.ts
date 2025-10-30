import {describe, expect, it, vi} from 'vitest'
import {listarAnalisesCadastro, listarAnalisesValidacao,} from '../analiseService'
import api from '@/axios-setup'
import {AnaliseCadastro, AnaliseValidacao} from '@/types/tipos'

vi.mock('@/axios-setup')

describe('analiseService', () => {
  describe('listarAnalisesCadastro', () => {
    it('should fetch analysis history for registration', async () => {
      const subprocessoId = 1
      const responseData: AnaliseCadastro[] = [{ codigo: 1, dataHora: new Date().toISOString(), observacoes: 'Obs', acao: 'ACEITE', unidadeSigla: 'Unidade', analista: 'Usuario', resultado: 'APROVADO', codSubrocesso: 1 }]
      vi.mocked(api.get).mockResolvedValue({ data: responseData })

      const result = await listarAnalisesCadastro(subprocessoId)

      expect(api.get).toHaveBeenCalledWith(
        `/subprocessos/${subprocessoId}/historico-cadastro`
      )
      expect(result).toEqual(responseData)
    })

    it('should throw an error on failure', async () => {
      const subprocessoId = 1
      const error = new Error('Request failed')
      vi.mocked(api.get).mockRejectedValue(error)

      await expect(listarAnalisesCadastro(subprocessoId)).rejects.toThrow()
    })
  })

  describe('listarAnalisesValidacao', () => {
    it('should fetch analysis history for validation', async () => {
      const subprocessoId = 1
      const responseData: AnaliseValidacao[] = [{ codigo: 1, dataHora: new Date().toISOString(), observacoes: 'Obs', acao: 'DEVOLUCAO', unidade: 'Unidade', analista: 'Usuario', resultado: 'REPROVADO', codSubrocesso: 1 }]
      vi.mocked(api.get).mockResolvedValue({ data: responseData })

      const result = await listarAnalisesValidacao(subprocessoId)

      expect(api.get).toHaveBeenCalledWith(
        `/subprocessos/${subprocessoId}/historico-validacao`
      )
      expect(result).toEqual(responseData)
    })

    it('should throw an error on failure', async () => {
        const subprocessoId = 1
        const error = new Error('Request failed')
        vi.mocked(api.get).mockRejectedValue(error)

        await expect(listarAnalisesValidacao(subprocessoId)).rejects.toThrow()
      })
  })
})