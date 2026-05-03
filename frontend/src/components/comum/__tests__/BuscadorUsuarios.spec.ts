import {afterEach, beforeEach, describe, expect, it, vi} from 'vitest'
import {mount} from '@vue/test-utils'
import BuscadorUsuarios from '../BuscadorUsuarios.vue'
import * as usuarioService from '@/services/usuarioService'

// Mocking dependencies
vi.mock('@/services/usuarioService', () => ({
  pesquisarUsuarios: vi.fn()
}))

const mockNotify = vi.fn()
vi.mock('@/composables/useNotification', () => ({
  useNotification: () => ({
    notify: mockNotify
  })
}))

describe('BuscadorUsuarios.vue', () => {
  beforeEach(() => {
    vi.useFakeTimers()
    vi.clearAllMocks()
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  async function aguardarProcessamento(wrapper: any) {
    // Avança o tempo para o debounce de 300ms
    await vi.advanceTimersByTime(305)
    // Deixa as promessas (pesquisarUsuarios) resolverem
    await vi.runAllTicks()
    // Deixa o Vue processar as mudanças de estado
    await wrapper.vm.$nextTick()
    await wrapper.vm.$nextTick()
  }

  it('não deve pesquisar se o termo tiver menos de 2 caracteres', async () => {
    const wrapper = mount(BuscadorUsuarios, {
      props: { termo: '' }
    })
    
    // Simula a alteração do termo
    await wrapper.find('input').setValue('a')
    await vi.advanceTimersByTime(305)
    
    expect(usuarioService.pesquisarUsuarios).not.toHaveBeenCalled()
  })

  it('deve pesquisar e mostrar resultados após debounce se o termo for válido', async () => {
    const usuarios = [
      {nome: 'João Silva', tituloEleitoral: '123456789012'},
      {nome: 'Maria Souza', tituloEleitoral: '987654321098'}
    ]
    vi.mocked(usuarioService.pesquisarUsuarios).mockResolvedValue(usuarios)
    
    const wrapper = mount(BuscadorUsuarios, {
      props: { termo: '' }
    })
    
    await wrapper.find('input').setValue('Jo')
    
    expect(usuarioService.pesquisarUsuarios).not.toHaveBeenCalled()
    
    await aguardarProcessamento(wrapper)
    
    expect(usuarioService.pesquisarUsuarios).toHaveBeenCalledWith('Jo')
    expect(wrapper.find('[data-testid="lista-usuarios-pesquisa"]').exists()).toBe(true)
    expect(wrapper.findAll('.list-group-item')).toHaveLength(2)
  })

  it('deve selecionar um usuário ao clicar na opção', async () => {
    const usuarios = [{nome: 'João Silva', tituloEleitoral: '123456789012'}]
    vi.mocked(usuarioService.pesquisarUsuarios).mockResolvedValue(usuarios)
    
    const wrapper = mount(BuscadorUsuarios, {
      props: {
        termo: '',
        selecionado: null
      }
    })
    
    await wrapper.find('input').setValue('Jo')
    await aguardarProcessamento(wrapper)
    
    const opcao = wrapper.find('[data-testid="opcao-usuario-123456789012"]')
    await opcao.trigger('mousedown')
    
    expect(wrapper.emitted('update:selecionado')![0]).toEqual(['123456789012'])
    // O primeiro emit é do setValue ('Jo'), o segundo é da seleção ('João Silva')
    expect(wrapper.emitted('update:termo')![1]).toEqual(['João Silva'])
  })

  it('deve navegar pelos resultados com as setas do teclado', async () => {
    const usuarios = [
      {nome: 'User 1', tituloEleitoral: '1'},
      {nome: 'User 2', tituloEleitoral: '2'}
    ]
    vi.mocked(usuarioService.pesquisarUsuarios).mockResolvedValue(usuarios)
    
    const wrapper = mount(BuscadorUsuarios, {
      props: { termo: '' }
    })
    
    const input = wrapper.find('input')
    await input.setValue('User')
    await aguardarProcessamento(wrapper)
    
    const itens = wrapper.findAll('.list-group-item')
    // O primeiro deve estar ativo por causa do watch
    expect(itens[0].classes()).toContain('active')
    
    // Seta para baixo
    await input.trigger('keydown', { key: 'ArrowDown' })
    await wrapper.vm.$nextTick()
    expect(itens[1].classes()).toContain('active')
    
    // Seta para cima
    await input.trigger('keydown', { key: 'ArrowUp' })
    await wrapper.vm.$nextTick()
    expect(itens[0].classes()).toContain('active')
  })

  it('deve tratar erro na pesquisa', async () => {
    vi.mocked(usuarioService.pesquisarUsuarios).mockRejectedValue(new Error('Erro'))
    
    const wrapper = mount(BuscadorUsuarios, {
      props: { termo: '' }
    })
    
    await wrapper.find('input').setValue('Jo')
    await aguardarProcessamento(wrapper)
    
    expect(mockNotify).toHaveBeenCalled()
    expect(wrapper.text()).not.toContain('Buscando')
  })

  it('deve ocultar resultados ao perder o foco (blur) com delay', async () => {
    vi.mocked(usuarioService.pesquisarUsuarios).mockResolvedValue([{nome: 'Jo', tituloEleitoral: '1'}])
    const wrapper = mount(BuscadorUsuarios, {
      props: { termo: '' }
    })
    
    const input = wrapper.find('input')
    await input.setValue('Jo')
    await aguardarProcessamento(wrapper)
    
    expect(wrapper.find('[data-testid="lista-usuarios-pesquisa"]').exists()).toBe(true)
    
    await input.trigger('blur')
    // Ocultação tem delay de 150ms no código
    await vi.advanceTimersByTime(160)
    await wrapper.vm.$nextTick()
    
    expect(wrapper.find('[data-testid="lista-usuarios-pesquisa"]').exists()).toBe(false)
  })
})
