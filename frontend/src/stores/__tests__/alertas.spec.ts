import { setActivePinia, createPinia } from 'pinia'
import { useAlertasStore } from '../alertas'
import { usePerfilStore } from '../perfil'
import { vi } from 'vitest'

describe('useAlertasStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('should initialize with mock alerts and parsed dates', () => {
    const alertasStore = useAlertasStore()
    expect(alertasStore.alertas.length).toBeGreaterThan(0)
    expect(alertasStore.alertas[0].dataHora).toBeInstanceOf(Date)
  })

  // ... outros testes que não dependem do perfil ...

  describe('Sistema de Alertas por Servidor', () => {
    beforeEach(() => {
      // Mock do estado do perfilStore para simular um usuário logado
      const perfilStore = usePerfilStore()
      perfilStore.usuario = {
        nome: 'Teste',
        tituloEleitoral: '1', // Corresponde ao servidorId 1 dos mocks
        perfil: 'CHEFE',
        unidade: 'TJPE',
        token: 'token'
      }
    })

    it('getAlertasDoServidor > should return alerts with read status', () => {
      const alertasStore = useAlertasStore()
      const alertas = alertasStore.getAlertasDoServidor
      const alerta1 = alertas.find((a) => a.id === 1) // Lido pelo servidor 1
      const alerta2 = alertas.find((a) => a.id === 2) // Não lido pelo servidor 1

      expect(alerta1?.lido).toBe(true)
      expect(alerta2?.lido).toBe(false)
    })

    it('getAlertasNaoLidos > should return only unread alerts', () => {
      const alertasStore = useAlertasStore()
      const alertasNaoLidos = alertasStore.getAlertasNaoLidos
      expect(alertasNaoLidos.length).toBe(1)
      expect(alertasNaoLidos[0].id).toBe(2)
    })

    it('marcarAlertaComoLido > should mark an alert as read', () => {
      const alertasStore = useAlertasStore()
      let alertas = alertasStore.getAlertasDoServidor
      let alerta2 = alertas.find((a) => a.id === 2)
      expect(alerta2?.lido).toBe(false)

      alertasStore.marcarAlertaComoLido(2)

      // Re-busca os alertas para verificar a atualização
      alertas = alertasStore.getAlertasDoServidor
      alerta2 = alertas.find((a) => a.id === 2)
      expect(alerta2?.lido).toBe(true)
    })

    it('marcarTodosAlertasComoLidos > should mark all alerts as read', () => {
      const alertasStore = useAlertasStore()
      alertasStore.marcarTodosAlertasComoLidos()
      const alertasNaoLidos = alertasStore.getAlertasNaoLidos
      expect(alertasNaoLidos.length).toBe(0)
    })
  })
})