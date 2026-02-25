import { vi } from "vitest";
import type { Meta, StoryObj } from '@storybook/vue3-vite';
import MainNavbar from './MainNavbar.vue';
import { createTestingPinia } from '@pinia/testing';

const meta: Meta<typeof MainNavbar> = {
  title: 'Layout/MainNavbar',
  component: MainNavbar,
  tags: ['autodocs'],
  decorators: [
    (story) => ({
      components: { story },
      template: '<div style="min-height: 100px; background-color: #f8f9fa;"><story /></div>',
    }),
  ],
};

export default meta;
type Story = StoryObj<typeof MainNavbar>;

export const Admin: Story = {
  render: () => ({
    components: { MainNavbar },
    setup() {
      // Mocking Pinia and Router
      const pinia = createTestingPinia({
        createSpy: vi.fn,
        initialState: {
          perfil: {
            perfilSelecionado: 'ADMIN',
            unidadeSelecionada: 'PRES',
            usuarioNome: 'Administrador do Sistema',
          },
        },
      });
      return { pinia };
    },
    template: '<MainNavbar />',
  }),
};

export const Gestor: Story = {
  render: () => ({
    components: { MainNavbar },
    setup() {
      const pinia = createTestingPinia({
        createSpy: vi.fn,
        initialState: {
          perfil: {
            perfilSelecionado: 'GESTOR',
            unidadeSelecionada: 'DITEC',
            usuarioNome: 'João Gestor',
          },
        },
      });
      return { pinia };
    },
    template: '<MainNavbar />',
  }),
};

export const Mobile: Story = {
  parameters: {
    viewport: {
      defaultViewport: 'mobile1',
    },
  },
  render: () => ({
    components: { MainNavbar },
    setup() {
      const pinia = createTestingPinia({
        createSpy: vi.fn,
        initialState: {
          perfil: {
            perfilSelecionado: 'ADMIN',
            unidadeSelecionada: 'PRES',
            usuarioNome: 'Admin Mobile',
          },
        },
      });
      return { pinia };
    },
    template: '<MainNavbar />',
  }),
};
