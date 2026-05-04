import type {Meta, StoryObj} from '@storybook/vue3-vite';
import {expect} from 'vitest';
import {page} from '@vitest/browser/context';
import CadastroAcoesHeader from './CadastroAcoesHeader.vue';
import type {PermissoesSubprocesso, Unidade} from '@/types/tipos';

const meta: Meta<typeof CadastroAcoesHeader> = {
    title: 'Cadastro/CadastroAcoesHeader',
    component: CadastroAcoesHeader,
    tags: ['autodocs'],
    argTypes: {
        mostrarDevolverCadastro: {control: 'boolean'},
        mostrarImportarAtividades: {control: 'boolean'},
        mostrarDisponibilizarCadastro: {control: 'boolean'},
        loadingValidacao: {control: 'boolean'},
        podeVisualizarImpacto: {control: 'boolean'},
    },
};

export default meta;
type Story = StoryObj<typeof CadastroAcoesHeader>;

const unidadeMock: Unidade = {
    codigo: 2,
    nome: 'Pró-Reitoria de Ensino',
    sigla: 'PROEN',
};

const permissoesMock: PermissoesSubprocesso = {
    podeEditarCadastro: true,
    podeDisponibilizarCadastro: true,
    podeDevolverCadastro: true,
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
    habilitarAcessoMapa: false,
    habilitarEditarCadastro: true,
    habilitarDisponibilizarCadastro: true,
    habilitarDevolverCadastro: true,
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

export const SemSubprocesso: Story = {
    args: {
        unidade: unidadeMock,
        codSubprocesso: null,
        permissoes: permissoesMock,
        mostrarDevolverCadastro: false,
        mostrarImportarAtividades: false,
        mostrarDisponibilizarCadastro: false,
        acaoPrincipalCadastro: null,
        loadingValidacao: false,
        podeVisualizarImpacto: false,
    },
    play: async () => {
        await expect.element(page.getByTestId('subprocesso-header__txt-header-unidade')).toBeVisible();
    },
};

export const ComHistorico: Story = {
    args: {
        unidade: unidadeMock,
        codSubprocesso: 101,
        permissoes: permissoesMock,
        mostrarDevolverCadastro: false,
        mostrarImportarAtividades: false,
        mostrarDisponibilizarCadastro: false,
        acaoPrincipalCadastro: null,
        loadingValidacao: false,
        podeVisualizarImpacto: false,
    },
    play: async () => {
        const btnHistorico = page.getByTestId('btn-cad-atividades-historico');
        await expect.element(btnHistorico).toBeVisible();
    },
};

export const ComDisponibilizar: Story = {
    args: {
        unidade: unidadeMock,
        codSubprocesso: 101,
        permissoes: permissoesMock,
        mostrarDevolverCadastro: false,
        mostrarImportarAtividades: false,
        mostrarDisponibilizarCadastro: true,
        acaoPrincipalCadastro: null,
        loadingValidacao: false,
        podeVisualizarImpacto: false,
    },
    play: async () => {
        const btnDisponibilizar = page.getByTestId('btn-cad-atividades-disponibilizar');
        await expect.element(btnDisponibilizar).toBeVisible();
    },
};

export const ComMultiplasAcoes: Story = {
    args: {
        unidade: unidadeMock,
        codSubprocesso: 101,
        permissoes: permissoesMock,
        mostrarDevolverCadastro: true,
        mostrarImportarAtividades: true,
        mostrarDisponibilizarCadastro: true,
        acaoPrincipalCadastro: {
            mostrar: true,
            habilitar: true,
            rotuloBotao: 'Homologar Cadastro',
        },
        loadingValidacao: false,
        podeVisualizarImpacto: true,
    },
    play: async () => {
        const btnAcoes = page.getByTestId('btn-cadastro-acoes');
        await expect.element(btnAcoes).toBeVisible();
    },
};

export const Disponibilizando: Story = {
    args: {
        unidade: unidadeMock,
        codSubprocesso: 101,
        permissoes: permissoesMock,
        mostrarDevolverCadastro: false,
        mostrarImportarAtividades: false,
        mostrarDisponibilizarCadastro: true,
        acaoPrincipalCadastro: null,
        loadingValidacao: true,
        podeVisualizarImpacto: false,
    },
};
