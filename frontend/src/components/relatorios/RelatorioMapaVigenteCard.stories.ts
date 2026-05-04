import type {Meta, StoryObj} from '@storybook/vue3-vite';
import RelatorioMapaVigenteCard from './RelatorioMapaVigenteCard.vue';

const meta: Meta<typeof RelatorioMapaVigenteCard> = {
    title: 'Relatorios/RelatorioMapaVigenteCard',
    component: RelatorioMapaVigenteCard,
    tags: ['autodocs'],
};

export default meta;
type Story = StoryObj<typeof RelatorioMapaVigenteCard>;

const mapaCompleto = {
    codigoUnidade: 10,
    siglaUnidade: 'PROEN',
    nomeUnidade: 'Pró-Reitoria de Ensino',
    competencias: [
        {
            codigo: 1,
            descricao: 'Gestão de Políticas Educacionais',
            atividades: [
                {
                    codigo: 1,
                    descricao: 'Elaborar e monitorar o plano de desenvolvimento institucional',
                    conhecimentos: [
                        {codigo: 1, descricao: 'Legislação educacional vigente'},
                        {codigo: 2, descricao: 'Metodologias de avaliação institucional'},
                    ],
                },
                {
                    codigo: 2,
                    descricao: 'Coordenar ações de acompanhamento pedagógico',
                    conhecimentos: [
                        {codigo: 3, descricao: 'Técnicas de supervisão educacional'},
                    ],
                },
            ],
        },
        {
            codigo: 2,
            descricao: 'Gestão de Cursos e Programas',
            atividades: [
                {
                    codigo: 3,
                    descricao: 'Analisar projetos pedagógicos de cursos',
                    conhecimentos: [],
                },
            ],
        },
    ],
};

const mapaSimples = {
    codigoUnidade: 20,
    siglaUnidade: 'REIT',
    nomeUnidade: 'Reitoria',
    competencias: [
        {
            codigo: 3,
            descricao: 'Gestão Institucional',
            atividades: [
                {
                    codigo: 4,
                    descricao: 'Coordenar reuniões de colegiado',
                    conhecimentos: [
                        {codigo: 4, descricao: 'Regimento interno institucional'},
                    ],
                },
            ],
        },
    ],
};

export const Default: Story = {
    args: {
        mapa: mapaCompleto,
    },
};

export const ComUmaCompetencia: Story = {
    args: {
        mapa: mapaSimples,
    },
};

export const SemConhecimentos: Story = {
    args: {
        mapa: {
            codigoUnidade: 30,
            siglaUnidade: 'CF',
            nomeUnidade: 'Campus Fortaleza',
            competencias: [
                {
                    codigo: 4,
                    descricao: 'Gestão Acadêmica',
                    atividades: [
                        {
                            codigo: 5,
                            descricao: 'Coordenar processos seletivos internos',
                            conhecimentos: [],
                        },
                    ],
                },
            ],
        },
    },
};

export const MultiplasCometenciasEAtividades: Story = {
    args: {
        mapa: mapaCompleto,
    },
};
