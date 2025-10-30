import {describe, expect, it, vi} from 'vitest'
import {marcarComoLido} from '../alertaService'
import api from '@/axios-setup'

vi.mock('@/axios-setup')

describe('alertaService', () => {
  it('marcarComoLido should make a POST request', async () => {
    const id = 1
    vi.mocked(api.post).mockResolvedValue({})

    await marcarComoLido(id)

    expect(api.post).toHaveBeenCalledWith(`/alertas/${id}/marcar-como-lido`)
  })

  it('marcarComoLido should throw an error on failure', async () => {
    const id = 1
    const error = new Error('Request failed')
    vi.mocked(api.post).mockRejectedValue(error)

    await expect(marcarComoLido(id)).rejects.toThrow(error)
  })
})