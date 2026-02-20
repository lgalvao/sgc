import type { Meta, StoryObj } from '@storybook/vue3';
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

const mockAdmins = [
  { nome: 'Admin 1', tituloEleitoral: '111111111111', matricula: 'M1', unidadeSigla: 'PRES' },
  { nome: 'Admin 2', tituloEleitoral: '222222222222', matricula: 'M2', unidadeSigla: 'DITEC' },
];

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
