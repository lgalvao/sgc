import { mount } from '@vue/test-utils'
import { createRouter, createWebHistory } from 'vue-router'
import { describe, it, expect, beforeEach, vi } from 'vitest'
import BarraNavegacao from '../BarraNavegacao.vue'
import { createPinia, setActivePinia } from 'pinia'
import { usePerfilStore } from '@/stores/perfil'
import { Perfil } from '@/types/tipos'

// Mock das rotas para os testes
const routes = [
  { path: '/login', name: 'Login', component: { template: '<div>Login</div>' } },
  { path: '/painel', name: 'Painel', component: { template: '<div>Painel</div>' } },
  {
    path: '/processo/:idProcesso',
    name: 'Processo',
    component: { template: '<div>Processo</div>' },
    meta: { breadcrumb: 'Processo' }
  },
  {
    path: '/processo/:idProcesso/:siglaUnidade',
    name: 'Subprocesso',
    component: { template: '<div>Subprocesso</div>' },
    meta: {
      breadcrumb: (route: any) => `${route.params.siglaUnidade ?? ''}`
    }
  },
  {
    path: '/processo/:idProcesso/:siglaUnidade/mapa',
    name: 'SubprocessoMapa',
    component: { template: '<div>Mapa</div>' },
    meta: { breadcrumb: 'Mapa' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

describe('BarraNavegacao.vue', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    // Reseta o router para a página inicial antes de cada teste
    router.push('/')
  })

  const mountComponent = async (path: string, perfil: Perfil | null) => {
    const perfilStore = usePerfilStore()
    if (perfil) {
      perfilStore.usuario = {
        nome: 'Teste',
        tituloEleitoral: '1',
        perfil: perfil,
        unidade: 'TJPE',
        token: 'token'
      }
    } else {
      perfilStore.usuario = null
    }

    await router.push(path)
    await router.isReady()

    return mount(BarraNavegacao, {
      global: {
        plugins: [router]
      }
    })
  }

  it('não deve exibir o botão Voltar na página de login', async () => {
    const wrapper = await mountComponent('/login', null)
    expect(wrapper.find('[data-testid="botao-voltar"]').exists()).toBe(false)
  })

  it('deve exibir o botão Voltar em uma página aninhada', async () => {
    const wrapper = await mountComponent('/processo/1', Perfil.ADMIN)
    expect(wrapper.find('[data-testid="botao-voltar"]').exists()).toBe(true)
  })

  it('deve popular a trilha para uma rota de subprocesso para ADMIN', async () => {
    const wrapper = await mountComponent('/processo/1/SGP', Perfil.ADMIN)
    const breadcrumbItems = wrapper.findAll('.breadcrumb-item')
    // Home > Processo > SGP
    expect(breadcrumbItems.length).toBe(3)
    expect(breadcrumbItems[1].text()).toBe('Processo')
    expect(breadcrumbItems[2].text()).toBe('SGP')
  })

  it('deve popular a trilha para uma rota de subprocesso para CHEFE sem o crumb Processo', async () => {
    const wrapper = await mountComponent('/processo/1/SGP', Perfil.CHEFE)
    const breadcrumbItems = wrapper.findAll('.breadcrumb-item')
    // Home > SGP
    expect(breadcrumbItems.length).toBe(2)
    expect(breadcrumbItems[1].text()).toBe('SGP')
  })

  it('deve popular a trilha para uma rota de subprocesso com sub-página (mapa) para GESTOR', async () => {
    const wrapper = await mountComponent('/processo/1/SGP/mapa', Perfil.GESTOR)
    const breadcrumbItems = wrapper.findAll('.breadcrumb-item')
    // Home > Processo > SGP > Mapa
    expect(breadcrumbItems.length).toBe(4)
    expect(breadcrumbItems[2].text()).toBe('SGP')
    expect(breadcrumbItems[3].text()).toBe('Mapa')
  })
})