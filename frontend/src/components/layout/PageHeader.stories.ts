import type { Meta, StoryObj } from '@storybook/vue3-vite';
import PageHeader from './PageHeader.vue';

const meta: Meta<typeof PageHeader> = {
  title: 'Layout/PageHeader',
  component: PageHeader,
  tags: ['autodocs'],
  argTypes: {
    title: { control: 'text' },
    subtitle: { control: 'text' },
    etapa: { control: 'text' },
    proximaAcao: { control: 'text' },
  },
};

export default meta;
type Story = StoryObj<typeof PageHeader>;

export const Default: Story = {
  args: {
    title: 'Dashboard de Processos',
    subtitle: 'Visualize o andamento das competências em tempo real.',
  },
};

export const ComContexto: Story = {
  args: {
    title: 'Mapeamento de Competências',
    subtitle: 'Unidade: Diretoria de Tecnologia',
    etapa: 'Em Andamento',
    proximaAcao: 'Aguardando validação do gestor',
  },
};

export const ComAcoes: Story = {
  args: {
    title: 'Detalhes do Processo',
    subtitle: 'Processo: Desenvolvimento de Sistemas',
  },
  render: (args) => ({
    components: { PageHeader },
    setup() {
      return { args };
    },
    template: `
      <PageHeader v-bind="args">
        <template #actions>
          <button class="btn btn-outline-secondary btn-sm">Editar</button>
          <button class="btn btn-primary btn-sm">Salvar Alterações</button>
        </template>
      </PageHeader>
    `,
  }),
};

export const Simples: Story = {
  args: {
    title: 'Configurações',
  },
};
