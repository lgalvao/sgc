import type { Meta, StoryObj } from '@storybook/vue3';
import AtividadeItem from './AtividadeItem.vue';
import { fn } from '@storybook/test';

const meta: Meta<typeof AtividadeItem> = {
  title: 'Atividades/AtividadeItem',
  component: AtividadeItem,
  tags: ['autodocs'],
  argTypes: {
    onAtualizarAtividade: { action: 'atualizar-atividade' },
    onRemoverAtividade: { action: 'remover-atividade' },
    onAdicionarConhecimento: { action: 'adicionar-conhecimento' },
    onAtualizarConhecimento: { action: 'atualizar-conhecimento' },
    onRemoverConhecimento: { action: 'remover-conhecimento' },
  },
};

export default meta;
type Story = StoryObj<typeof AtividadeItem>;

const mockAtividade = {
  codigo: 101,
  descricao: 'Desenvolver novas funcionalidades no sistema de gestão',
  conhecimentos: [
    { codigo: 1, descricao: 'Vue.js 3' },
    { codigo: 2, descricao: 'TypeScript' },
    { codigo: 3, descricao: 'Bootstrap 5' },
  ],
};

export const Default: Story = {
  args: {
    atividade: mockAtividade,
    podeEditar: true,
  },
};

export const SomenteLeitura: Story = {
  args: {
    atividade: mockAtividade,
    podeEditar: false,
  },
};

export const ComErro: Story = {
  args: {
    atividade: {
      ...mockAtividade,
      conhecimentos: [], // Atividade sem conhecimentos deve mostrar erro
    },
    podeEditar: true,
    erroValidacao: 'A atividade deve possuir pelo menos um conhecimento associado.',
  },
};

export const AtividadeLonga: Story = {
  args: {
    atividade: {
      codigo: 102,
      descricao: 'Esta é uma descrição de atividade extremamente longa para testar como o componente se comporta com textos extensos que podem quebrar o layout se não forem tratados corretamente com word-break ou overflow.',
      conhecimentos: [
        { codigo: 4, descricao: 'Conhecimento com nome também muito longo que ultrapassa os limites normais' },
      ],
    },
    podeEditar: true,
  },
};
