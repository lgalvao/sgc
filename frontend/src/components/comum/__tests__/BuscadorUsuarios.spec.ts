import {describe, expect, it, vi, beforeEach, afterEach} from 'vitest'
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

  async function aguardarProcessamento(wrapper?: any) {
    await vi.runAllTicks()
    // Avança o tempo para o debounce
    await vi.advanceTimersByTime(300)
    await vi.runAllTicks()
    // Múltiplos ticks para garantir que watches e renderização terminaram
    for (let i = 0; i < 3; i++) {
      await Promise.resolve()
      if (wrapper) await wrapper.vm.$nextTick()
    }
  }

  it('não deve pesquisar se o termo tiver menos de 2 caracteres', async () => {
    const wrapper = mount(BuscadorUsuarios, {
      props: {
        termo: 'a'
      }
    })
    
    const input = wrapper.find('input')
    await input.setValue('a')
    
    vi.advanceTimersByTime(300)
    
    expect(usuarioService.pesquisarUsuarios).not.toHaveBeenCalled()
  })

  it('deve pesquisar e mostrar resultados após debounce se o termo for válido', async () => {
    const usuarios = [
      {nome: 'João Silva', tituloEleitoral: '123456789012'},
      {nome: 'Maria Souza', tituloEleitoral: '987654321098'}
    ]
    vi.mocked(usuarioService.pesquisarUsuarios).mockResolvedValue(usuarios)
    
    const wrapper = mount(BuscadorUsuarios, {
      props: {
        termo: ''
      }
    })
    
    const input = wrapper.find('input')
    await input.setValue('Jo')
    
    // Verifica se não chamou imediatamente
    expect(usuarioService.pesquisarUsuarios).not.toHaveBeenCalled()
    
    // Avança o tempo do debounce (300ms)
    await vi.advanceTimersByTime(300)
    
    expect(usuarioService.pesquisarUsuarios).toHaveBeenCalledWith('Jo')
    
    // Aguarda promessas
    await vi.runAllTicks()
    await wrapper.vm.$nextTick()
    
    expect(wrapper.find('[data-testid="lista-usuarios-pesquisa"]').exists()).toBe(true)
    const itens = wrapper.findAll('.list-group-item')
    expect(itens).toHaveLength(2)
    expect(itens[0].text()).toContain('João Silva')
  })

  it('deve selecionar um usuário ao clicar na opção', async () => {
    const usuarios = [{nome: 'João Silva', tituloEleitoral: '123456789012'}]
    vi.mocked(usuarioService.pesquisarUsuarios).mockResolvedValue(usuarios)
    
    const wrapper = mount(BuscadorUsuarios, {
      props: {
        termo: '',
        'onUpdate:termo': (val: string) => wrapper.setProps({termo: val}),
        'onUpdate:selecionado': (val: string | null) => wrapper.setProps({selecionado: val})
      }
    })
    
    await wrapper.find('input').setValue('Jo')
    await aguardarProcessamento(wrapper)
    
    const lista = wrapper.find('[data-testid="lista-usuarios-pesquisa"]')
    expect(lista.exists()).toBe(true)
    
    const opcao = wrapper.find('[data-testid="opcao-usuario-123456789012"]')
    // Usamos mousedown.prevent no componente
    await opcao.trigger('mousedown')
    
    expect(wrapper.props('selecionado')).toBe('123456789012')
    expect(wrapper.props('termo')).toBe('João Silva')
    expect(wrapper.find('[data-testid="lista-usuarios-pesquisa"]').exists()).toBe(false)
  })

  it('deve navegar pelos resultados com as setas do teclado e selecionar com Enter', async () => {
    const usuarios = [
      {nome: 'João Silva', tituloEleitoral: '1'},
      {nome: 'Maria Souza', tituloEleitoral: '2'}
    ]
    vi.mocked(usuarioService.pesquisarUsuarios).mockResolvedValue(usuarios)
    
    const wrapper = mount(BuscadorUsuarios, {
      props: {
        termo: '',
        'onUpdate:termo': (val: string) => wrapper.setProps({termo: val}),
        'onUpdate:selecionado': (val: string | null) => wrapper.setProps({selecionado: val})
      }
    })
    
    const input = wrapper.find('input')
    await wrapper.find('input').setValue('Jo')
    await aguardarProcessamento(wrapper)
    
    // O primeiro item deve estar destacado por padrão (watch em usuariosEncontrados)
    const itens = wrapper.findAll('.list-group-item')
    expect(itens.length).toBeGreaterThan(0)
    expect(itens[0].classes()).toContain('active')
    
    // Pressiona Seta para Baixo
    await input.trigger('keydown', {key: 'ArrowDown'})
    expect(itens[1].classes()).toContain('active')
    
    // Pressiona Enter
    await input.trigger('keydown', {key: 'Enter'})
    expect(wrapper.props('selecionado')).toBe('2')
    expect(wrapper.props('termo')).toBe('Maria Souza')
  })

  it('deve ocultar resultados ao pressionar Escape', async () => {
    vi.mocked(usuarioService.pesquisarUsuarios).mockResolvedValue([{nome: 'Jo', tituloEleitoral: '1'}])
    
    const wrapper = mount(BuscadorUsuarios, {
      props: {termo: ''}
    })
    
    const input = wrapper.find('input')
    await input.setValue('Jo')
    await vi.advanceTimersByTime(300)
    await vi.runAllTicks()
    
    expect(wrapper.find('[data-testid="lista-usuarios-pesquisa"]').exists()).toBe(true)
    
    await input.trigger('keydown', {key: 'Escape'})
    expect(wrapper.find('[data-testid="lista-usuarios-pesquisa"]').exists()).toBe(false)
  })

  it('deve tratar erro na pesquisa e notificar o usuário', async () => {
    vi.mocked(usuarioService.pesquisarUsuarios).mockRejectedValue(new Error('Falha na API'))
    
    const wrapper = mount(BuscadorUsuarios, {
      props: {termo: ''}
    })
    
    await wrapper.find('input').setValue('Jo')
    await vi.advanceTimersByTime(300)
    await vi.runAllTicks()
    
    expect(mockNotify).toHaveBeenCalledWith(expect.stringContaining('Erro ao pesquisar'), 'danger')
    expect(wrapper.text()).not.toContain('Buscando usuários...')
  })
  
  it('deve mostrar mensagem quando nenhum usuário for encontrado', async () => {
    vi.mocked(usuarioService.pesquisarUsuarios).mockResolvedValue([])
    
    const wrapper = mount(BuscadorUsuarios, {
      props: {termo: ''}
    })
    
    await wrapper.find('input').setValue('Jo')
    await vi.advanceTimersByTime(300)
    await vi.runAllTicks()
    await wrapper.vm.$nextTick()
    
    expect(wrapper.text()).toContain('Nenhum usuário encontrado')
  })
})
