import type {Meta, StoryObj} from '@storybook/vue3-vite';
import TabelaMovimentacoes from './TabelaMovimentacoes.vue';

const meta: Meta<typeof TabelaMovimentacoes> = {
    title: 'Processo/TabelaMovimentacoes',
    component: TabelaMovimentacoes,
    tags: ['autodocs'],
};

export default meta;
type Story = StoryObj<typeof TabelaMovimentacoes>;

const mockUsuario = {
    codigo: 1,
    nome: 'Usuário Teste',
    tituloEleitoral: '123456789012',
    unidade: {codigo: 1, nome: 'Unidade Teste', sigla: 'TEST'},
    email: 'teste@teste.com',
    ramal: '1234'
};

const mockMovimentacoes = [
    {
        codigo: 1,
        dataHora: '2025-02-20T09:00:00Z',
        unidadeOrigem: {codigo: 1, nome: 'Presidência', sigla: 'PRES'},
        unidadeDestino: {codigo: 2, nome: 'Diretoria de Tecnologia', sigla: 'DITEC'},
        descricao: 'Início do processo de mapeamento.',
        subprocesso: {codigo: 101, situacao: 'EM_ANDAMENTO'} as any,
        usuario: mockUsuario,
    },
    {
        codigo: 2,
        dataHora: '2025-02-21T11:30:00Z',
        unidadeOrigem: {codigo: 2, nome: 'Diretoria de Tecnologia', sigla: 'DITEC'},
        unidadeDestino: {codigo: 1, nome: 'Presidência', sigla: 'PRES'},
        descricao: 'Mapeamento concluído e enviado para homologação.',
        subprocesso: {codigo: 101, situacao: 'CONCLUIDO'} as any,
        usuario: mockUsuario,
    },
    {
        codigo: 3,
        dataHora: '2025-02-22T14:00:00Z',
        unidadeOrigem: {codigo: 1, nome: 'Presidência', sigla: 'PRES'},
        unidadeDestino: {codigo: 1, nome: 'Presidência', sigla: 'PRES'},
        descricao: 'Processo finalizado pelo administrador.',
        subprocesso: {codigo: 101, situacao: 'HOMOLOGADO'} as any,
        usuario: mockUsuario,
    },
];

export const ComMovimentacoes: Story = {
    args: {
        movimentacoes: mockMovimentacoes,
    },
};

export const Vazia: Story = {
    args: {
        movimentacoes: [],
    },
};
