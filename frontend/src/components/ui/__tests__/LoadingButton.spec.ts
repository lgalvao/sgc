import {describe, expect, it} from 'vitest'
import {mount} from '@vue/test-utils'
import LoadingButton from '../LoadingButton.vue'

describe('LoadingButton.vue', () => {
  const globalOptions = {
    stubs: {
      BButton: {
        template: '<button class="b-button-stub" :disabled="disabled"><slot /></button>',
        props: ['disabled', 'variant', 'type'],
      },
      BSpinner: {
        template: '<div class="b-spinner-stub" :class="{ small: small }"></div>',
        props: ['small']
      }
    }
  }

  it('renders correctly with default props', () => {
    const wrapper = mount(LoadingButton, {
      global: globalOptions,
      slots: {
        default: 'Click Me'
      }
    })

    const button = wrapper.find('.b-button-stub')
    expect(button.exists()).toBe(true)
    expect(button.text()).toBe('Click Me')
    expect(wrapper.find('.b-spinner-stub').exists()).toBe(false)
  })

  it('renders correctly when loading is true', () => {
    const wrapper = mount(LoadingButton, {
      props: {
        loading: true
      },
      global: globalOptions,
      slots: {
        default: 'Click Me'
      }
    })

    const button = wrapper.find('.b-button-stub')
    expect(button.attributes('disabled')).toBeDefined()
    expect(wrapper.find('.b-spinner-stub').exists()).toBe(true)
  })

  it('renders icon when provided and not loading', () => {
    const wrapper = mount(LoadingButton, {
      props: {
        icon: 'save'
      },
      global: globalOptions
    })

    expect(wrapper.find('.bi-save').exists()).toBe(true)
    expect(wrapper.find('.b-spinner-stub').exists()).toBe(false)
  })

  it('does not render icon when loading', () => {
    const wrapper = mount(LoadingButton, {
      props: {
        icon: 'save',
        loading: true
      },
      global: globalOptions
    })

    expect(wrapper.find('.bi-save').exists()).toBe(false)
    expect(wrapper.find('.b-spinner-stub').exists()).toBe(true)
  })

  it('renders loading text when loading and loadingText provided', () => {
    const wrapper = mount(LoadingButton, {
      props: {
        loading: true,
        loadingText: 'Saving...',
        text: 'Save'
      },
      global: globalOptions
    })

    expect(wrapper.text()).toContain('Saving...')
    expect(wrapper.text()).not.toContain('Save')
  })

  it('renders normal text when not loading even if loadingText provided', () => {
    const wrapper = mount(LoadingButton, {
      props: {
        loading: false,
        loadingText: 'Saving...',
        text: 'Save'
      },
      global: globalOptions
    })

    expect(wrapper.text()).toContain('Save')
    expect(wrapper.text()).not.toContain('Saving...')
  })

  it('disables button when disabled prop is true', () => {
    const wrapper = mount(LoadingButton, {
      props: {
        disabled: true
      },
      global: globalOptions
    })

    expect(wrapper.find('.b-button-stub').attributes('disabled')).toBeDefined()
  })
})
