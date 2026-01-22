import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import PageHeader from '../PageHeader.vue'

describe('PageHeader.vue', () => {
  it('renders title correctly', () => {
    const title = 'Test Title'
    const wrapper = mount(PageHeader, {
      props: { title }
    })
    expect(wrapper.find('h2').text()).toBe(title)
  })

  it('renders subtitle from prop', () => {
    const subtitle = 'Test Subtitle'
    const wrapper = mount(PageHeader, {
      props: {
        title: 'Title',
        subtitle
      }
    })
    expect(wrapper.text()).toContain(subtitle)
    expect(wrapper.find('p.text-muted').exists()).toBe(true)
  })

  it('renders subtitle from slot', () => {
    const slotContent = 'Slot Subtitle'
    const wrapper = mount(PageHeader, {
      props: { title: 'Title' },
      slots: {
        default: slotContent
      }
    })
    expect(wrapper.text()).toContain(slotContent)
  })

  it('renders actions slot', () => {
    const wrapper = mount(PageHeader, {
      props: { title: 'Title' },
      slots: {
        actions: '<button>Action Button</button>'
      }
    })
    expect(wrapper.find('button').text()).toBe('Action Button')
    expect(wrapper.find('.d-flex.gap-2').exists()).toBe(true)
  })

  it('does not render subtitle element if no prop or slot provided', () => {
    const wrapper = mount(PageHeader, {
      props: { title: 'Title' }
    })
    expect(wrapper.find('p').exists()).toBe(false)
  })
})
