import type {Meta, StoryObj} from '@storybook/vue3-vite';
import {expect} from 'vitest';
import {page} from 'vitest/browser';
import MapaAcoesHeader from './MapaAcoesHeader.vue';
import type {Unidade} from '@/types/tipos';

const meta: Meta<typeof MapaAcoesHeader> = {
    title: 'Mapa/MapaAcoesHeader',
    component: MapaAcoesHeader,
    tags: ['autodocs'],
    argTypes: {
        podeVerSugestoes: {control: 'boolean'},
        loadingSugestoesVisualizacao: {control: 'boolean'},
        podeVisualizarImpacto: {control: 'boolean'},
        loadingImpacto: {control: 'boolean'},
        usarMenuAcoesMapa: {control: 'boolean'},
        mostrarApresentarSugestoes: {control: 'boolean'},
        habilitarApresentarSugestoes: {control: 'boolean'},
        mostrarValidarMapa: {control: 'boolean'},
        habilitarValidarMapa: {control: 'boolean'},
        mostrarDisponibilizarMapa: {control: 'boolean'},
        habilitarDisponibilizarMapa: {control: 'boolean'},
        loadingDisponibilizacao: {control: 'boolean'},
        mostrarDevolverMapa: {control: 'boolean'},
        habilitarDevolverMapa: {control: 'boolean'},
        mostrarAcaoPrincipalMapa: {control: 'boolean'},
        habilitarAcaoPrincipalMapa: {control: 'boolean'},
    },
};

export default meta;
type Story = StoryObj<typeof MapaAcoesHeader>;

const unidadeMock: Unidade = {
    codigo: 2,
    nome: 'Pró-Reitoria de Ensino',
    sigla: 'PROEN',
};

export const Default: Story = {
    args: {
        unidade: unidadeMock,
        codigoSubprocesso: null,
        podeVerSugestoes: false,
        podeVisualizarImpacto: false,
        usarMenuAcoesMapa: false,
    },
    play: async () => {
        await expect.element(page.getByTestId('subprocesso-header__txt-header-unidade')).toBeVisible();
    },
};

export const ComSubprocesso: Story = {
    args: {
        unidade: unidadeMock,
        codigoSubprocesso: 101,
        podeVerSugestoes: false,
        podeVisualizarImpacto: false,
        usarMenuAcoesMapa: false,
    },
    play: async () => {
        const btnHistorico = page.getByTestId('btn-mapa-historico');
        await expect.element(btnHistorico).toBeVisible();
    },
};

export const ComVerSugestoes: Story = {
    args: {
        unidade: unidadeMock,
        codigoSubprocesso: 101,
        podeVerSugestoes: true,
        loadingSugestoesVisualizacao: false,
        podeVisualizarImpacto: false,
        usarMenuAcoesMapa: false,
    },
    play: async () => {
        const btnSugestoes = page.getByTestId('btn-mapa-ver-sugestoes');
        await expect.element(btnSugestoes).toBeVisible();
    },
};

export const ComMenuAcoes: Story = {
    args: {
        unidade: unidadeMock,
        codigoSubprocesso: 101,
        podeVerSugestoes: false,
        podeVisualizarImpacto: false,
        usarMenuAcoesMapa: true,
        mostrarApresentarSugestoes: true,
        habilitarApresentarSugestoes: true,
        mostrarValidarMapa: true,
        habilitarValidarMapa: false,
        mostrarDisponibilizarMapa: true,
        habilitarDisponibilizarMapa: false,
        mostrarDevolverMapa: true,
        habilitarDevolverMapa: true,
        mostrarAcaoPrincipalMapa: false,
        habilitarAcaoPrincipalMapa: false,
    },
    play: async () => {
        const btnAcoes = page.getByTestId('btn-mapa-acoes');
        await expect.element(btnAcoes).toBeVisible();
    },
};

export const ComHomologar: Story = {
    args: {
        unidade: unidadeMock,
        codigoSubprocesso: 101,
        podeVerSugestoes: false,
        podeVisualizarImpacto: false,
        usarMenuAcoesMapa: true,
        mostrarApresentarSugestoes: false,
        habilitarApresentarSugestoes: false,
        mostrarValidarMapa: false,
        habilitarValidarMapa: false,
        mostrarDisponibilizarMapa: false,
        habilitarDisponibilizarMapa: false,
        mostrarDevolverMapa: false,
        habilitarDevolverMapa: false,
        mostrarAcaoPrincipalMapa: true,
        habilitarAcaoPrincipalMapa: true,
        rotuloAcaoPrincipalMapa: 'Homologar Mapa',
    },
    play: async () => {
        const btnAcoes = page.getByTestId('btn-mapa-acoes');
        await expect.element(btnAcoes).toBeVisible();
    },
};

export const SemUnidade: Story = {
    args: {
        unidade: null,
        codigoSubprocesso: 101,
        podeVerSugestoes: false,
        podeVisualizarImpacto: false,
        usarMenuAcoesMapa: false,
    },
};
