import type {Meta, StoryObj} from '@storybook/vue3-vite';
import RelatorioAndamentoCard from './RelatorioAndamentoCard.vue';

const meta: Meta<typeof RelatorioAndamentoCard> = {
    title: 'Relatorios/RelatorioAndamentoCard',
    component: RelatorioAndamentoCard,
    tags: ['autodocs'],
};

export default meta;
type Story = StoryObj<typeof RelatorioAndamentoCard>;

const itemMapeamentoConcluido = {
    siglaUnidade: 'PROEN',
    nomeUnidade: 'Pró-Reitoria de Ensino',
    situacaoAtual: 'Mapa disponibilizado',
    localizacao: 'REIT',
    dataLimiteEtapa1: '31/03/2025',
    dataLimiteEtapa2: '30/06/2025',
    dataFimEtapa1: '28/03/2025',
    dataFimEtapa2: '',
    dataUltimaMovimentacao: '28/03/2025',
    titular: 'Maria Aparecida de Souza',
    responsavel: 'Maria Aparecida de Souza',
    mostraPrazoAjustado: false,
};

const itemEmAndamento = {
    siglaUnidade: 'CF',
    nomeUnidade: 'Campus Fortaleza',
    situacaoAtual: 'Cadastro em andamento',
    localizacao: 'CF',
    dataLimiteEtapa1: '31/03/2025',
    dataLimiteEtapa2: '30/06/2025',
    dataFimEtapa1: '',
    dataFimEtapa2: '',
    dataUltimaMovimentacao: '10/01/2025',
    titular: 'João Carlos Pereira',
    responsavel: 'Ana Paula Rodrigues',
    mostraPrazoAjustado: false,
};

const itemComPrazoAjustado = {
    siglaUnidade: 'REIT',
    nomeUnidade: 'Reitoria',
    situacaoAtual: 'Mapa validado',
    localizacao: 'PROEX',
    dataLimiteEtapa1: '31/03/2025',
    dataLimiteEtapa2: '15/07/2025',
    dataFimEtapa1: '25/03/2025',
    dataFimEtapa2: '',
    dataUltimaMovimentacao: '01/05/2025',
    titular: 'Carlos Eduardo Lima',
    responsavel: 'Carlos Eduardo Lima',
    mostraPrazoAjustado: true,
};

export const Default: Story = {
    args: {
        item: itemMapeamentoConcluido,
    },
};

export const EmAndamento: Story = {
    args: {
        item: itemEmAndamento,
    },
};

export const ComResponsavelDiferente: Story = {
    args: {
        item: itemEmAndamento,
    },
};

export const ComPrazoAjustado: Story = {
    args: {
        item: itemComPrazoAjustado,
    },
};

export const EtapasCompletas: Story = {
    args: {
        item: {
            ...itemMapeamentoConcluido,
            dataFimEtapa1: '28/03/2025',
            dataFimEtapa2: '20/06/2025',
            situacaoAtual: 'Mapa homologado',
        },
    },
};
