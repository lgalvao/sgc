import type { Meta, StoryObj } from '@storybook/vue3-vite';
import AdministradoresSection from './AdministradoresSection.vue';
import { createTestingPinia } from '@pinia/testing';
import { fn } from '@storybook/test';

const meta: Meta<typeof AdministradoresSection> = {
  title: 'Configuracoes/AdministradoresSection',
  component: AdministradoresSection,
  tags: ['autodocs'],
};

export default meta;
type Story = StoryObj<typeof AdministradoresSection>;

export const Default: Story = {
  render: () => ({
    components: { AdministradoresSection },
    setup() {
      const pinia = createTestingPinia({ createSpy: fn });
      // In a real Storybook, we would mock the service calls. 
      // For this demo, we're showing the component structure.
      return { pinia };
    },
    template: '<AdministradoresSection />',
  }),
};
