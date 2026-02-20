import type {Meta, StoryObj} from '@storybook/vue3-vite';
import CompetenciaCard from './CompetenciaCard.vue';
import type {Atividade, Competencia} from '@/types/tipos';

const meta: Meta<typeof CompetenciaCard> = {
  title: 'Mapa/CompetenciaCard',
  component: CompetenciaCard,
  tags: ['autodocs'],
  argTypes: {
    podeEditar: { control: 'boolean' },
  },
};

export default meta;
type Story = StoryObj<typeof CompetenciaCard>;

const mockAtividades: Atividade[] = [
  {
    codigo: 1,
    descricao: 'Desenvolver API REST',
    conhecimentos: [
      { codigo: 1, descricao: 'Java' },
      { codigo: 2, descricao: 'Spring Boot' },
    ],
  },
  {
    codigo: 2,
    descricao: 'Criar componentes Vue',
    conhecimentos: [
      { codigo: 3, descricao: 'Vue.js' },
      { codigo: 4, descricao: 'TypeScript' },
    ],
  },
  {
    codigo: 3,
    descricao: 'Configurar CI/CD',
    conhecimentos: [],
  },
];

const mockCompetencia: Competencia = {
  codigo: 10,
  descricao: 'Desenvolvimento Fullstack',
  atividades: [],
  atividadesAssociadas: [1, 2],
};

export const Default: Story = {
  args: {
    competencia: mockCompetencia,
    atividades: mockAtividades,
    podeEditar: true,
  },
};

export const ReadOnly: Story = {
  args: {
    competencia: mockCompetencia,
    atividades: mockAtividades,
    podeEditar: false,
  },
};

export const ComAtividadeSemConhecimento: Story = {
  args: {
    competencia: {
      ...mockCompetencia,
      atividadesAssociadas: [3],
    },
    atividades: mockAtividades,
    podeEditar: true,
  },
};
