import type {Meta, StoryObj} from '@storybook/vue3-vite';
import ProcessoSubprocessosTable from './ProcessoSubprocessosTable.vue';
import {SituacaoSubprocesso} from '@/types/tipos';

const meta: Meta<typeof ProcessoSubprocessosTable> = {
    title: 'Processo/ProcessoSubprocessosTable',
    component: ProcessoSubprocessosTable,
    tags: ['autodocs'],
    argTypes: {
        'onRow-click': {action: 'row-click'},
    },
};

export default meta;
type Story = StoryObj<typeof ProcessoSubprocessosTable>;

const mockParticipantes = [
    {
        codUnidade: 1,
        codSubprocesso: 101,
        nome: 'Presidência',
        sigla: 'PRES',
        situacaoSubprocesso: SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO,
        dataLimite: '2025-12-31',
        filhos: [
            {
                codUnidade: 2,
                codSubprocesso: 102,
                nome: 'Diretoria de Tecnologia',
                sigla: 'DITEC',
                situacaoSubprocesso: SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO,
                dataLimite: '2025-12-31',
                filhos: [
                    {
                        codUnidade: 3,
                        codSubprocesso: 103,
                        nome: 'Coordenação de Desenvolvimento',
                        sigla: 'CODES',
                        situacaoSubprocesso: SituacaoSubprocesso.NAO_INICIADO,
                        dataLimite: '2025-12-31',
                        filhos: []
                    }
                ]
            },
            {
                codUnidade: 4,
                codSubprocesso: 104,
                nome: 'Diretoria Administrativa',
                sigla: 'DIRAD',
                situacaoSubprocesso: SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO,
                dataLimite: '2025-12-31',
                filhos: []
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
                codSubprocesso: 105,
                nome: 'Auditoria Interna',
                sigla: 'AUDIT',
                situacaoSubprocesso: SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO,
                dataLimite: '2025-06-30',
                filhos: []
            }
        ],
    },
};

export const Vazio: Story = {
    args: {
        participantesHierarquia: [],
    },
};
