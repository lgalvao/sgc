import type {Meta, StoryObj} from '@storybook/vue3-vite';
import SubprocessoResumoHeader from './SubprocessoResumoHeader.vue';
import {SituacaoSubprocesso, TipoProcesso} from '@/types/tipos';
import type {SubprocessoDetalhe, ResponsavelDto} from '@/types/tipos';

const meta: Meta<typeof SubprocessoResumoHeader> = {
    title: 'Processo/SubprocessoResumoHeader',
    component: SubprocessoResumoHeader,
    tags: ['autodocs'],
    argTypes: {
        mostrarAcoesCabecalho: {control: 'boolean'},
        mostrarAlterarDataLimite: {control: 'boolean'},
        habilitarAlterarDataLimite: {control: 'boolean'},
        mostrarReabrirCadastro: {control: 'boolean'},
        habilitarReabrirCadastro: {control: 'boolean'},
        mostrarReabrirRevisao: {control: 'boolean'},
        habilitarReabrirRevisao: {control: 'boolean'},
        mostrarEnviarLembrete: {control: 'boolean'},
        habilitarEnviarLembrete: {control: 'boolean'},
    },
};

export default meta;
type Story = StoryObj<typeof SubprocessoResumoHeader>;

const permissoesMock = {
    podeEditarCadastro: false,
    podeDisponibilizarCadastro: false,
    podeDevolverCadastro: false,
    podeAceitarCadastro: false,
    podeHomologarCadastro: false,
    podeEditarMapa: false,
    podeDisponibilizarMapa: false,
    podeValidarMapa: false,
    podeApresentarSugestoes: false,
    podeVerSugestoes: false,
    podeDevolverMapa: false,
    podeAceitarMapa: false,
    podeHomologarMapa: false,
    podeVisualizarImpacto: false,
    podeAlterarDataLimite: false,
    podeReabrirCadastro: false,
    podeReabrirRevisao: false,
    podeEnviarLembrete: false,
    mesmaUnidade: true,
    habilitarAcessoCadastro: true,
    habilitarAcessoMapa: true,
    habilitarEditarCadastro: false,
    habilitarDisponibilizarCadastro: false,
    habilitarDevolverCadastro: false,
    habilitarAceitarCadastro: false,
    habilitarHomologarCadastro: false,
    habilitarEditarMapa: false,
    habilitarDisponibilizarMapa: false,
    habilitarValidarMapa: false,
    habilitarApresentarSugestoes: false,
    habilitarDevolverMapa: false,
    habilitarAceitarMapa: false,
    habilitarHomologarMapa: false,
    habilitarAlterarDataLimite: false,
    habilitarReabrirCadastro: false,
    habilitarReabrirRevisao: false,
    habilitarEnviarLembrete: false,
};

const titularMock = {
    codigo: 10,
    nome: 'Maria Aparecida de Souza',
    matricula: '1234567',
    tituloEleitoral: '012345678',
    unidade: {codigo: 2, nome: 'Pró-Reitoria de Ensino', sigla: 'PROEN'},
    email: 'maria.souza@ifce.edu.br',
    ramal: '3232',
};

const subprocessoMock: SubprocessoDetalhe = {
    codigo: 101,
    unidade: {codigo: 2, nome: 'Pró-Reitoria de Ensino', sigla: 'PROEN'},
    titular: titularMock,
    responsavel: null,
    situacao: SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO,
    localizacaoAtual: 'PROEN',
    processoDescricao: 'Mapeamento de Competências 2025',
    dataCriacaoProcesso: '2025-01-15',
    ultimaDataLimiteSubprocesso: '2025-09-30',
    tipoProcesso: TipoProcesso.MAPEAMENTO,
    prazoEtapaAtual: '2025-03-31',
    isEmAndamento: true,
    etapaAtual: 1,
    movimentacoes: [],
    elementosProcesso: [],
    permissoes: permissoesMock,
};

const formatSituacaoSubprocesso = (situacao: string) =>
    situacao.replace(/_/g, ' ').toLowerCase().replace(/\b\w/g, c => c.toUpperCase());

const formatDataSimples = (data: string | null) => data ? new Date(data).toLocaleDateString('pt-BR') : '-';

const formatTipoResponsabilidade = (_responsavel: ResponsavelDto | null) => '';

export const Default: Story = {
    args: {
        subprocesso: subprocessoMock,
        siglaUnidadeFallback: 'N/A',
        mostrarAcoesCabecalho: false,
        formatSituacaoSubprocesso,
        formatDataSimples,
        formatTipoResponsabilidade,
    },
};

export const ComAcoesCabecalho: Story = {
    args: {
        subprocesso: subprocessoMock,
        siglaUnidadeFallback: 'N/A',
        mostrarAcoesCabecalho: true,
        mostrarAlterarDataLimite: true,
        habilitarAlterarDataLimite: true,
        mostrarReabrirCadastro: true,
        habilitarReabrirCadastro: false,
        mostrarEnviarLembrete: true,
        habilitarEnviarLembrete: true,
        formatSituacaoSubprocesso,
        formatDataSimples,
        formatTipoResponsabilidade,
    },
};

export const ComResponsavelDiferente: Story = {
    args: {
        subprocesso: {
            ...subprocessoMock,
            responsavel: {
                usuario: {
                    codigo: 20,
                    nome: 'Ana Paula Rodrigues',
                    matricula: '9876543',
                    tituloEleitoral: '987654321',
                    unidade: {codigo: 2, nome: 'Pró-Reitoria de Ensino', sigla: 'PROEN'},
                    email: 'ana.rodrigues@ifce.edu.br',
                    ramal: '5566',
                },
                tipo: 'TEMPORARIA',
                dataInicio: '2025-02-01',
                dataFim: null,
            } as ResponsavelDto,
        },
        siglaUnidadeFallback: 'N/A',
        mostrarAcoesCabecalho: false,
        formatSituacaoSubprocesso,
        formatDataSimples,
        formatTipoResponsabilidade: (responsavel: ResponsavelDto | null) =>
            responsavel?.tipo === 'TEMPORARIA' ? 'Temporária' : '',
    },
};

export const SemPrazoEtapa: Story = {
    args: {
        subprocesso: {
            ...subprocessoMock,
            prazoEtapaAtual: '',
        },
        siglaUnidadeFallback: 'N/A',
        mostrarAcoesCabecalho: false,
        formatSituacaoSubprocesso,
        formatDataSimples,
        formatTipoResponsabilidade,
    },
};
