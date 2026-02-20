import type { Meta, StoryObj } from '@storybook/vue3';
import CompetenciaViewCard from './CompetenciaViewCard.vue';

const meta: Meta<typeof CompetenciaViewCard> = {
  title: 'Mapa/CompetenciaViewCard',
  component: CompetenciaViewCard,
  tags: ['autodocs'],
};

export default meta;
type Story = StoryObj<typeof CompetenciaViewCard>;

const mockCompetencia = {
  codigo: 1,
  descricao: 'Desenvolvimento de Software',
  atividades: [
    {
      codigo: 101,
      descricao: 'Desenvolver novas funcionalidades no frontend',
      conhecimentos: [
        { codigo: 1, descricao: 'Vue.js' },
        { codigo: 2, descricao: 'TypeScript' },
      ],
    },
    {
      codigo: 102,
      descricao: 'Realizar manutenção corretiva no backend',
      conhecimentos: [
        { codigo: 3, descricao: 'Node.js' },
        { codigo: 4, descricao: 'PostgreSQL' },
      ],
    },
  ],
};

export const Default: Story = {
  args: {
    competencia: mockCompetencia,
  },
};

export const Simples: Story = {
  args: {
    competencia: {
      codigo: 2,
      descricao: 'Gestão de Projetos',
      atividades: [
        {
          codigo: 103,
          descricao: 'Coordenar equipes de desenvolvimento',
          conhecimentos: [
            { codigo: 5, descricao: 'Scrum' },
            { codigo: 6, descricao: 'Kanban' },
          ],
        },
      ],
    },
  },
};

export const Vazia: Story = {
  args: {
    competencia: {
      codigo: 3,
      descricao: 'Inovação e Tecnologia',
      atividades: [],
    },
  },
};
