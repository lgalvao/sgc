import {describe, expect, it} from 'vitest'
import {mount} from '@vue/test-utils'
import EmptyState from '../comum/EmptyState.vue'

describe('EmptyState.vue', () => {
  it('renderiza corretamente sem props', () => {
    const wrapper = mount(EmptyState)
    expect(wrapper.find('[data-testid="empty-state"]').exists()).toBe(true)
    expect(wrapper.find('[data-testid="empty-state-title"]').exists()).toBe(false)
    expect(wrapper.find('[data-testid="empty-state-description"]').exists()).toBe(false)
    expect(wrapper.find('i').exists()).toBe(false)
  })

  it('renderiza título quando fornecido', () => {
    const title = 'Título de Teste'
    const wrapper = mount(EmptyState, {
      props: { title }
    })
    const titleEl = wrapper.find('[data-testid="empty-state-title"]')
    expect(titleEl.exists()).toBe(true)
    expect(titleEl.text()).toBe(title)
  })

  it('renderiza descrição quando fornecida', () => {
    const description = 'Descrição de teste'
    const wrapper = mount(EmptyState, {
      props: { description }
    })
    const descEl = wrapper.find('[data-testid="empty-state-description"]')
    expect(descEl.exists()).toBe(true)
    expect(descEl.text()).toBe(description)
  })

  it('renderiza ícone quando fornecido', () => {
    const icon = 'bi-check'
    const wrapper = mount(EmptyState, {
      props: { icon }
    })
    const iconEl = wrapper.find('i')
    expect(iconEl.exists()).toBe(true)
    expect(iconEl.classes()).toContain(icon)
  })

  it('renderiza conteúdo do slot', () => {
    const slotContent = '<button>Ação</button>'
    const wrapper = mount(EmptyState, {
      slots: {
        default: slotContent
      }
    })
    expect(wrapper.html()).toContain(slotContent)
  })

  it('não renderiza elementos se as props forem strings vazias', () => {
    const wrapper = mount(EmptyState, {
      props: { title: '', description: '', icon: '' }
    })
    expect(wrapper.find('[data-testid="empty-state-title"]').exists()).toBe(false)
    expect(wrapper.find('[data-testid="empty-state-description"]').exists()).toBe(false)
    expect(wrapper.find('i').exists()).toBe(false)
  })
})
