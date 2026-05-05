import type {Meta, StoryObj} from '@storybook/vue3-vite';
import {expect} from 'vitest';
import {page} from 'vitest/browser';
import {ref} from 'vue';
import ProcessoBasicFields from './ProcessoBasicFields.vue';
import {TipoProcesso} from '@/types/tipos';

const meta: Meta<typeof ProcessoBasicFields> = {
    title: 'Processo/ProcessoBasicFields',
    component: ProcessoBasicFields,
    tags: ['autodocs'],
    argTypes: {
        isEdit: {control: 'boolean'},
    },
};

export default meta;
type Story = StoryObj<typeof ProcessoBasicFields>;

export const Default: Story = {
    render: () => ({
        components: {ProcessoBasicFields},
        setup() {
            const descricao = ref('');
            const tipo = ref<TipoProcesso | null>(null);
            return {descricao, tipo};
        },
        template: `
      <div style="max-width: 500px; padding: 20px;">
        <ProcessoBasicFields
          :descricao="descricao"
          :tipo="tipo"
          @update:descricao="descricao = $event"
          @update:tipo="tipo = $event"
        />
      </div>
    `,
    }),
    play: async () => {
        const inputDescricao = page.getByTestId('inp-processo-descricao');
        await expect.element(inputDescricao).toBeVisible();
        const selectTipo = page.getByTestId('sel-processo-tipo');
        await expect.element(selectTipo).toBeVisible();
    },
};

export const Preenchido: Story = {
    render: () => ({
        components: {ProcessoBasicFields},
        setup() {
            const descricao = ref('Mapeamento de Competências 2025');
            const tipo = ref<TipoProcesso | null>(TipoProcesso.MAPEAMENTO);
            return {descricao, tipo};
        },
        template: `
      <div style="max-width: 500px; padding: 20px;">
        <ProcessoBasicFields
          :descricao="descricao"
          :tipo="tipo"
          @update:descricao="descricao = $event"
          @update:tipo="tipo = $event"
        />
      </div>
    `,
    }),
    play: async () => {
        const inputDescricao = page.getByTestId('inp-processo-descricao');
        await expect.element(inputDescricao).toHaveValue('Mapeamento de Competências 2025');
    },
};

export const EmEdicao: Story = {
    render: () => ({
        components: {ProcessoBasicFields},
        setup() {
            const descricao = ref('Revisão de Atividades 2024');
            const tipo = ref<TipoProcesso | null>(TipoProcesso.REVISAO);
            return {descricao, tipo};
        },
        template: `
      <div style="max-width: 500px; padding: 20px;">
        <ProcessoBasicFields
          :descricao="descricao"
          :tipo="tipo"
          :is-edit="true"
          @update:descricao="descricao = $event"
          @update:tipo="tipo = $event"
        />
      </div>
    `,
    }),
    play: async () => {
        const selectTipo = page.getByTestId('sel-processo-tipo');
        await expect.element(selectTipo).toBeDisabled();
    },
};

export const ComErros: Story = {
    render: () => ({
        components: {ProcessoBasicFields},
        setup() {
            const descricao = ref('');
            const tipo = ref<TipoProcesso | null>(null);
            return {descricao, tipo};
        },
        template: `
      <div style="max-width: 500px; padding: 20px;">
        <ProcessoBasicFields
          :descricao="descricao"
          :tipo="tipo"
          erro-descricao="A descrição é obrigatória."
          erro-tipo="Selecione um tipo de processo."
          @update:descricao="descricao = $event"
          @update:tipo="tipo = $event"
        />
      </div>
    `,
    }),
    play: async () => {
        await expect.element(page.getByText('A descrição é obrigatória.')).toBeVisible();
        await expect.element(page.getByText('Selecione um tipo de processo.')).toBeVisible();
    },
};
