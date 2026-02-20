import type { Meta, StoryObj } from '@storybook/vue3';
import ProcessoSubprocessosTable from './ProcessoSubprocessosTable.vue';

const meta: Meta<typeof ProcessoSubprocessosTable> = {
  title: 'Processo/ProcessoSubprocessosTable',
  component: ProcessoSubprocessosTable,
  tags: ['autodocs'],
  argTypes: {
    onRowClick: { action: 'row-click' },
  },
};

export default meta;
type Story = StoryObj<typeof ProcessoSubprocessosTable>;

const mockParticipantes = [
  {
    codUnidade: 1,
    nome: 'Presidência',
    sigla: 'PRES',
    situacaoSubprocesso: 'EM_ANDAMENTO',
    dataLimite: '2025-12-31',
    filhos: [
      {
        codUnidade: 2,
        nome: 'Diretoria de Tecnologia',
        sigla: 'DITEC',
        situacaoSubprocesso: 'CONCLUIDO',
        dataLimite: '2025-12-31',
        filhos: [
          {
            codUnidade: 3,
            nome: 'Coordenação de Desenvolvimento',
            sigla: 'CODES',
            situacaoSubprocesso: 'PENDENTE',
            dataLimite: '2025-12-31',
          }
        ]
      },
      {
        codUnidade: 4,
        nome: 'Diretoria Administrativa',
        sigla: 'DIRAD',
        situacaoSubprocesso: 'EM_ANDAMENTO',
        dataLimite: '2025-12-31',
      }
    ]
  }
];

export const Default: Story = {
  args: {
    participantesHierarquia: mockParticipantes,
  },
};

export const UnidadeUnica: Story = {
  args: {
    participantesHierarquia: [
      {
        codUnidade: 5,
        nome: 'Auditoria Interna',
        sigla: 'AUDIT',
        situacaoSubprocesso: 'EM_ANDAMENTO',
        dataLimite: '2025-06-30',
      }
    ],
  },
};

export const Vazio: Story = {
  args: {
    participantesHierarquia: [],
  },
};
