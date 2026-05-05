import type {Meta, StoryObj} from '@storybook/vue3-vite';
import {expect} from 'vitest';
import {page} from 'vitest/browser';
import {ref} from 'vue';
import LoginPerfilSelect from './LoginPerfilSelect.vue';
import type {PerfilUnidade} from '@/types/autenticacao';
import {Perfil} from '@/types/tipos';

const meta: Meta<typeof LoginPerfilSelect> = {
    title: 'Login/LoginPerfilSelect',
    component: LoginPerfilSelect,
    tags: ['autodocs'],
    argTypes: {
        mostrar: {control: 'boolean'},
    },
};

export default meta;
type Story = StoryObj<typeof LoginPerfilSelect>;

const perfisOptions = [
    {
        value: {
            perfil: Perfil.GESTOR,
            unidade: {codigo: 1, nome: 'Reitoria', sigla: 'REIT'},
            siglaUnidade: 'REIT',
        } as PerfilUnidade,
        text: 'GESTOR - REIT',
    },
    {
        value: {
            perfil: Perfil.CHEFE,
            unidade: {codigo: 2, nome: 'Pró-Reitoria de Ensino', sigla: 'PROEN'},
            siglaUnidade: 'PROEN',
        } as PerfilUnidade,
        text: 'CHEFE - PROEN',
    },
    {
        value: {
            perfil: Perfil.SERVIDOR,
            unidade: {codigo: 3, nome: 'Campus Fortaleza', sigla: 'CF'},
            siglaUnidade: 'CF',
        } as PerfilUnidade,
        text: 'SERVIDOR - CF',
    },
];

export const Visivel: Story = {
    args: {
        mostrar: true,
        parSelecionado: null,
        perfisUnidadesOptions: perfisOptions,
        mensagemErroPerfil: '',
    },
    render: (args) => ({
        components: {LoginPerfilSelect},
        setup() {
            const selecionado = ref(args.parSelecionado);
            return {args, selecionado};
        },
        template: `
      <div style="max-width: 400px; padding: 20px; border: 1px solid #dee2e6; border-radius: 8px;">
        <LoginPerfilSelect
          v-bind="args"
          :par-selecionado="selecionado"
          @update:par-selecionado="selecionado = $event"
        />
      </div>
    `,
    }),
    play: async () => {
        const secao = page.getByTestId('sec-login-perfil');
        await expect.element(secao).toBeVisible();
        const select = page.getByTestId('sel-login-perfil');
        await expect.element(select).toBeVisible();
    },
};

export const ComErro: Story = {
    args: {
        mostrar: true,
        parSelecionado: null,
        perfisUnidadesOptions: perfisOptions,
        mensagemErroPerfil: 'Selecione um perfil de acesso.',
    },
    render: (args) => ({
        components: {LoginPerfilSelect},
        setup() {
            const selecionado = ref(args.parSelecionado);
            return {args, selecionado};
        },
        template: `
      <div style="max-width: 400px; padding: 20px; border: 1px solid #dee2e6; border-radius: 8px;">
        <LoginPerfilSelect
          v-bind="args"
          :par-selecionado="selecionado"
          @update:par-selecionado="selecionado = $event"
        />
      </div>
    `,
    }),
    play: async () => {
        await expect.element(page.getByText('Selecione um perfil de acesso.')).toBeVisible();
    },
};

export const Oculto: Story = {
    args: {
        mostrar: false,
        parSelecionado: null,
        perfisUnidadesOptions: perfisOptions,
        mensagemErroPerfil: '',
    },
    tags: ['visual-only'],
};
