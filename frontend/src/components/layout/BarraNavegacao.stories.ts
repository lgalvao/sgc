import type { Meta, StoryObj } from '@storybook/vue3-vite';
import BarraNavegacao from './BarraNavegacao.vue';

const meta: Meta<typeof BarraNavegacao> = {
  title: 'Layout/BarraNavegacao',
  component: BarraNavegacao,
  tags: ['autodocs'],
  decorators: [
    (story) => ({
      components: { story },
      template: '<div style="padding: 1rem; background-color: #f8f9fa;"><story /></div>',
    }),
  ],
};

export default meta;
type Story = StoryObj<typeof BarraNavegacao>;

export const Default: Story = {
  render: () => ({
    components: { BarraNavegacao },
    setup() {
      // Mocking route and router
      return { };
    },
    template: '<BarraNavegacao />',
  }),
  parameters: {
    // We'll need to mock useBreadcrumbs or provide a custom implementation via provide/inject if needed
    // For now, let's assume it picks up the current route or is mocked in the setup
  }
};

// Note: Components heavily dependent on routing and complex composables 
// like useBreadcrumbs might need more elaborate mocking in Storybook.
// In a real scenario, we'd mock useBreadcrumbs to return specific crumbs.
