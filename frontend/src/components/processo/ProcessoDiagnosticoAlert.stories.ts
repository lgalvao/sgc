import type {Meta, StoryObj} from '@storybook/vue3-vite';
import ProcessoDiagnosticoAlert from './ProcessoDiagnosticoAlert.vue';

const meta: Meta<typeof ProcessoDiagnosticoAlert> = {
    title: 'Processo/ProcessoDiagnosticoAlert',
    component: ProcessoDiagnosticoAlert,
    tags: ['autodocs'],
    argTypes: {
        exibir: {control: 'boolean'},
        carregando: {control: 'boolean'},
    },
};

export default meta;
type Story = StoryObj<typeof ProcessoDiagnosticoAlert>;

export const ComGrupos: Story = {
    args: {
        exibir: true,
        carregando: false,
        resumo: 'Foram encontradas 3 ocorrências de diagnóstico organizacional.',
        grupos: [
            {tipo: 'Unidade sem responsável', quantidadeOcorrencias: 2},
            {tipo: 'Titular ausente', quantidadeOcorrencias: 1},
        ],
        unidadesSemResponsavel: [],
    },
};

export const ComUnidadesSemResponsavel: Story = {
    args: {
        exibir: true,
        carregando: false,
        resumo: 'Unidades sem responsável identificadas.',
        grupos: [],
        unidadesSemResponsavel: [
            {codigo: 2, sigla: 'PROEN'},
            {codigo: 3, sigla: 'CF'},
        ],
    },
};

export const ComUnicaUnidadeSemResponsavel: Story = {
    args: {
        exibir: true,
        carregando: false,
        resumo: 'Unidade sem responsável identificada.',
        grupos: [],
        unidadesSemResponsavel: [
            {codigo: 4, sigla: 'CM'},
        ],
    },
};

export const UnidadeSemCodigo: Story = {
    args: {
        exibir: true,
        carregando: false,
        resumo: 'Unidade externa sem responsável.',
        grupos: [],
        unidadesSemResponsavel: [
            {codigo: null, sigla: 'EXT'},
        ],
    },
};

export const Carregando: Story = {
    args: {
        exibir: true,
        carregando: true,
        resumo: '',
        grupos: [],
        unidadesSemResponsavel: [],
    },
};

export const Oculto: Story = {
    args: {
        exibir: false,
        carregando: false,
        resumo: 'Não exibido.',
        grupos: [],
        unidadesSemResponsavel: [],
    },
    tags: ['visual-only'],
};
