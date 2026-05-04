import type {Meta, StoryObj} from '@storybook/vue3-vite';
import {expect} from 'vitest';
import {page} from '@vitest/browser/context';
import ProcessoAcoes from './ProcessoAcoes.vue';
import {SituacaoProcesso, TipoProcesso} from '@/types/tipos';
import type {Processo} from '@/types/tipos';

const meta: Meta<typeof ProcessoAcoes> = {
    title: 'Processo/ProcessoAcoes',
    component: ProcessoAcoes,
    tags: ['autodocs'],
    argTypes: {
        mostrarFinalizarProcesso: {control: 'boolean'},
        podeFinalizar: {control: 'boolean'},
        usarMenuAcoesBloco: {control: 'boolean'},
        processandoAcaoBloco: {control: 'boolean'},
    },
};

export default meta;
type Story = StoryObj<typeof ProcessoAcoes>;

const processoMock: Processo = {
    codigo: 1,
    descricao: 'Mapeamento de Competências 2025',
    tipo: TipoProcesso.MAPEAMENTO,
    situacao: SituacaoProcesso.EM_ANDAMENTO,
    dataLimite: '2025-09-30',
    dataCriacao: '2025-01-15',
    unidades: [],
    resumoSubprocessos: [],
};

const acoesBlocoMock = [
    {
        codigo: 'ACEITAR_CADASTRO',
        acao: 'ACEITAR' as const,
        mostrar: true,
        habilitar: true,
        requerDataLimite: false,
        redirecionarPainel: false,
        rotulo: 'Aceitar Cadastro em Bloco',
        titulo: 'Aceitar Cadastros',
        texto: 'Deseja aceitar os cadastros selecionados?',
        rotuloBotao: 'Aceitar',
        mensagemSucesso: 'Cadastros aceitos com sucesso.',
        unidades: [],
    },
    {
        codigo: 'HOMOLOGAR_CADASTRO',
        acao: 'HOMOLOGAR' as const,
        mostrar: true,
        habilitar: false,
        requerDataLimite: false,
        redirecionarPainel: false,
        rotulo: 'Homologar Cadastro em Bloco',
        titulo: 'Homologar Cadastros',
        texto: 'Deseja homologar os cadastros selecionados?',
        rotuloBotao: 'Homologar',
        mensagemSucesso: 'Cadastros homologados com sucesso.',
        unidades: [],
    },
];

export const Default: Story = {
    args: {
        processo: processoMock,
        mostrarFinalizarProcesso: false,
        podeFinalizar: false,
        usarMenuAcoesBloco: false,
        acoesBlocoVisiveis: [],
        acaoBlocoPrincipal: null,
        processandoAcaoBloco: false,
    },
    play: async () => {
        await expect.element(page.getByTestId('processo-info')).toBeVisible();
    },
};

export const ComBotaoFinalizar: Story = {
    args: {
        processo: processoMock,
        mostrarFinalizarProcesso: true,
        podeFinalizar: true,
        usarMenuAcoesBloco: false,
        acoesBlocoVisiveis: [],
        acaoBlocoPrincipal: null,
        processandoAcaoBloco: false,
    },
    play: async () => {
        const btnFinalizar = page.getByTestId('btn-processo-finalizar');
        await expect.element(btnFinalizar).toBeVisible();
        await expect.element(btnFinalizar).not.toBeDisabled();
    },
};

export const FinalizarDesabilitado: Story = {
    args: {
        processo: processoMock,
        mostrarFinalizarProcesso: true,
        podeFinalizar: false,
        usarMenuAcoesBloco: false,
        acoesBlocoVisiveis: [],
        acaoBlocoPrincipal: null,
        processandoAcaoBloco: false,
    },
    play: async () => {
        const btnFinalizar = page.getByTestId('btn-processo-finalizar');
        await expect.element(btnFinalizar).toBeDisabled();
    },
};

export const ComMenuAcoesBloco: Story = {
    args: {
        processo: processoMock,
        mostrarFinalizarProcesso: false,
        podeFinalizar: false,
        usarMenuAcoesBloco: true,
        acoesBlocoVisiveis: acoesBlocoMock,
        acaoBlocoPrincipal: null,
        processandoAcaoBloco: false,
    },
    play: async () => {
        const btnAcoesBloco = page.getByTestId('btn-processo-acoes-bloco');
        await expect.element(btnAcoesBloco).toBeVisible();
    },
};

export const ComAcaoBlocoPrincipal: Story = {
    args: {
        processo: processoMock,
        mostrarFinalizarProcesso: false,
        podeFinalizar: false,
        usarMenuAcoesBloco: false,
        acoesBlocoVisiveis: [acoesBlocoMock[0]!],
        acaoBlocoPrincipal: acoesBlocoMock[0]!,
        processandoAcaoBloco: false,
    },
};

export const ProcessandoAcaoBloco: Story = {
    args: {
        processo: processoMock,
        mostrarFinalizarProcesso: false,
        podeFinalizar: false,
        usarMenuAcoesBloco: false,
        acoesBlocoVisiveis: [acoesBlocoMock[0]!],
        acaoBlocoPrincipal: acoesBlocoMock[0]!,
        processandoAcaoBloco: true,
    },
};
