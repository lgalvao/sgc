import type { Meta, StoryObj } from '@storybook/vue3';
import SubprocessoHeader from './SubprocessoHeader.vue';

const meta: Meta<typeof SubprocessoHeader> = {
  title: 'Processo/SubprocessoHeader',
  component: SubprocessoHeader,
  tags: ['autodocs'],
  argTypes: {
    onAlterarDataLimite: { action: 'alterarDataLimite' },
    onReabrirCadastro: { action: 'reabrirCadastro' },
    onReabrirRevisao: { action: 'reabrirRevisao' },
    onEnviarLembrete: { action: 'enviarLembrete' },
  },
};

export default meta;
type Story = StoryObj<typeof SubprocessoHeader>;

const mockData = {
  processoDescricao: 'Mapeamento de Competências 2025',
  unidadeSigla: 'DITEC',
  unidadeNome: 'Diretoria de Tecnologia',
  situacao: 'EM_ANDAMENTO',
  titularNome: 'João Titular',
  titularRamal: '1234',
  titularEmail: 'joao.titular@empresa.com.br',
  responsavelNome: 'Maria Responsável',
  responsavelRamal: '5678',
  responsavelEmail: 'maria.responsavel@empresa.com.br',
};

export const Default: Story = {
  args: {
    ...mockData,
    podeAlterarDataLimite: true,
    podeReabrirCadastro: true,
    podeReabrirRevisao: false,
    podeEnviarLembrete: true,
  },
};

export const SomenteLeitura: Story = {
  args: {
    ...mockData,
    podeAlterarDataLimite: false,
    podeReabrirCadastro: false,
    podeReabrirRevisao: false,
    podeEnviarLembrete: false,
  },
};

export const SemResponsavel: Story = {
  args: {
    ...mockData,
    responsavelNome: '',
    podeAlterarDataLimite: true,
  },
};
