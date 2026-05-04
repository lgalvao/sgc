import type {Meta, StoryObj} from '@storybook/vue3-vite';
import {expect} from 'vitest';
import {page} from '@vitest/browser/context';
import {ref} from 'vue';
import ProcessoUnidadesField from './ProcessoUnidadesField.vue';
import type {Unidade} from '@/types/tipos';

const meta: Meta<typeof ProcessoUnidadesField> = {
    title: 'Processo/ProcessoUnidadesField',
    component: ProcessoUnidadesField,
    tags: ['autodocs'],
    argTypes: {
        isLoading: {control: 'boolean'},
    },
};

export default meta;
type Story = StoryObj<typeof ProcessoUnidadesField>;

const unidadesMock: Unidade[] = [
    {
        codigo: 1,
        nome: 'Reitoria',
        sigla: 'REIT',
        filhas: [
            {
                codigo: 2,
                nome: 'Pró-Reitoria de Ensino',
                sigla: 'PROEN',
                filhas: [],
            },
            {
                codigo: 3,
                nome: 'Pró-Reitoria de Extensão',
                sigla: 'PROEX',
                filhas: [],
            },
        ],
    },
    {
        codigo: 4,
        nome: 'Campus Fortaleza',
        sigla: 'CF',
        filhas: [],
    },
    {
        codigo: 5,
        nome: 'Campus Maracanaú',
        sigla: 'CM',
        filhas: [],
    },
];

export const Default: Story = {
    render: () => ({
        components: {ProcessoUnidadesField},
        setup() {
            const selecionadas = ref<number[]>([]);
            return {selecionadas, unidades: unidadesMock};
        },
        template: `
      <div style="max-width: 500px; padding: 20px;">
        <ProcessoUnidadesField
          :model-value="selecionadas"
          :unidades="unidades"
          :is-loading="false"
          @update:model-value="selecionadas = $event"
        />
      </div>
    `,
    }),
    play: async () => {
        const container = page.getByTestId('container-processo-unidades');
        await expect.element(container).toBeVisible();
    },
};

export const ComUnidadesSelecionadas: Story = {
    render: () => ({
        components: {ProcessoUnidadesField},
        setup() {
            const selecionadas = ref<number[]>([1, 2]);
            return {selecionadas, unidades: unidadesMock};
        },
        template: `
      <div style="max-width: 500px; padding: 20px;">
        <ProcessoUnidadesField
          :model-value="selecionadas"
          :unidades="unidades"
          :is-loading="false"
          @update:model-value="selecionadas = $event"
        />
      </div>
    `,
    }),
};

export const Carregando: Story = {
    args: {
        modelValue: [],
        unidades: [],
        isLoading: true,
    },
    render: (args) => ({
        components: {ProcessoUnidadesField},
        setup() {
            const selecionadas = ref<number[]>([]);
            return {args, selecionadas};
        },
        template: `
      <div style="max-width: 500px; padding: 20px;">
        <ProcessoUnidadesField
          :model-value="selecionadas"
          :unidades="[]"
          :is-loading="true"
          @update:model-value="selecionadas = $event"
        />
      </div>
    `,
    }),
};

export const ComErro: Story = {
    render: () => ({
        components: {ProcessoUnidadesField},
        setup() {
            const selecionadas = ref<number[]>([]);
            return {selecionadas, unidades: unidadesMock};
        },
        template: `
      <div style="max-width: 500px; padding: 20px;">
        <ProcessoUnidadesField
          :model-value="selecionadas"
          :unidades="unidades"
          :is-loading="false"
          erro="Selecione ao menos uma unidade participante."
          @update:model-value="selecionadas = $event"
        />
      </div>
    `,
    }),
    play: async () => {
        await expect.element(page.getByText('Selecione ao menos uma unidade participante.')).toBeVisible();
    },
};
