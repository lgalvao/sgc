import type {Meta, StoryObj} from '@storybook/vue3-vite';
import {expect} from 'vitest';
import {page} from 'vitest/browser';
import {ref} from 'vue';
import LoginCredenciaisCampos from './LoginCredenciaisCampos.vue';

const meta: Meta<typeof LoginCredenciaisCampos> = {
    title: 'Login/LoginCredenciaisCampos',
    component: LoginCredenciaisCampos,
    tags: ['autodocs'],
    argTypes: {
        loginBloqueado: {control: 'boolean'},
        isLoading: {control: 'boolean'},
        showPassword: {control: 'boolean'},
        capsLockAtivado: {control: 'boolean'},
    },
};

export default meta;
type Story = StoryObj<typeof LoginCredenciaisCampos>;

export const Default: Story = {
    args: {
        titulo: '',
        senha: '',
        loginBloqueado: false,
        isLoading: false,
        showPassword: false,
        capsLockAtivado: false,
        mensagemErroTitulo: '',
        mensagemErroSenha: '',
    },
    render: (args) => ({
        components: {LoginCredenciaisCampos},
        setup() {
            const titulo = ref(args.titulo);
            const senha = ref(args.senha);
            return {args, titulo, senha};
        },
        template: `
      <div style="max-width: 400px; padding: 20px; border: 1px solid #dee2e6; border-radius: 8px;">
        <LoginCredenciaisCampos
          v-bind="args"
          :titulo="titulo"
          :senha="senha"
          @update:titulo="titulo = $event"
          @update:senha="senha = $event"
        />
      </div>
    `,
    }),
    play: async () => {
        const inputUsuario = page.getByTestId('inp-login-usuario');
        await expect.element(inputUsuario).toBeVisible();
        const inputSenha = page.getByTestId('inp-login-senha');
        await expect.element(inputSenha).toBeVisible();
    },
};

export const ComErros: Story = {
    args: {
        titulo: '12345',
        senha: '123',
        loginBloqueado: false,
        isLoading: false,
        showPassword: false,
        capsLockAtivado: false,
        mensagemErroTitulo: 'Título eleitoral inválido.',
        mensagemErroSenha: 'Senha incorreta.',
    },
    render: (args) => ({
        components: {LoginCredenciaisCampos},
        setup() {
            const titulo = ref(args.titulo);
            const senha = ref(args.senha);
            return {args, titulo, senha};
        },
        template: `
      <div style="max-width: 400px; padding: 20px; border: 1px solid #dee2e6; border-radius: 8px;">
        <LoginCredenciaisCampos
          v-bind="args"
          :titulo="titulo"
          :senha="senha"
          @update:titulo="titulo = $event"
          @update:senha="senha = $event"
        />
      </div>
    `,
    }),
    play: async () => {
        await expect.element(page.getByText('Título eleitoral inválido.')).toBeVisible();
        await expect.element(page.getByText('Senha incorreta.')).toBeVisible();
    },
};

export const Carregando: Story = {
    args: {
        titulo: '012345678',
        senha: 'minhasenha',
        loginBloqueado: false,
        isLoading: true,
        showPassword: false,
        capsLockAtivado: false,
        mensagemErroTitulo: '',
        mensagemErroSenha: '',
    },
    render: (args) => ({
        components: {LoginCredenciaisCampos},
        setup() {
            const titulo = ref(args.titulo);
            const senha = ref(args.senha);
            return {args, titulo, senha};
        },
        template: `
      <div style="max-width: 400px; padding: 20px; border: 1px solid #dee2e6; border-radius: 8px;">
        <LoginCredenciaisCampos
          v-bind="args"
          :titulo="titulo"
          :senha="senha"
          @update:titulo="titulo = $event"
          @update:senha="senha = $event"
        />
      </div>
    `,
    }),
    play: async () => {
        const inputUsuario = page.getByTestId('inp-login-usuario');
        await expect.element(inputUsuario).toBeDisabled();
    },
};

export const ComCapsLock: Story = {
    args: {
        titulo: '012345678',
        senha: '',
        loginBloqueado: false,
        isLoading: false,
        showPassword: false,
        capsLockAtivado: true,
        mensagemErroTitulo: '',
        mensagemErroSenha: '',
    },
    render: (args) => ({
        components: {LoginCredenciaisCampos},
        setup() {
            const titulo = ref(args.titulo);
            const senha = ref(args.senha);
            return {args, titulo, senha};
        },
        template: `
      <div style="max-width: 400px; padding: 20px; border: 1px solid #dee2e6; border-radius: 8px;">
        <LoginCredenciaisCampos
          v-bind="args"
          :titulo="titulo"
          :senha="senha"
          @update:titulo="titulo = $event"
          @update:senha="senha = $event"
        />
      </div>
    `,
    }),
    play: async () => {
        const alertCaps = page.getByTestId('alert-caps-lock');
        await expect.element(alertCaps).toBeVisible();
    },
};

export const SenhaVisivel: Story = {
    args: {
        titulo: '012345678',
        senha: 'minhasenha',
        loginBloqueado: false,
        isLoading: false,
        showPassword: true,
        capsLockAtivado: false,
        mensagemErroTitulo: '',
        mensagemErroSenha: '',
    },
    render: (args) => ({
        components: {LoginCredenciaisCampos},
        setup() {
            const titulo = ref(args.titulo);
            const senha = ref(args.senha);
            return {args, titulo, senha};
        },
        template: `
      <div style="max-width: 400px; padding: 20px; border: 1px solid #dee2e6; border-radius: 8px;">
        <LoginCredenciaisCampos
          v-bind="args"
          :titulo="titulo"
          :senha="senha"
          @update:titulo="titulo = $event"
          @update:senha="senha = $event"
        />
      </div>
    `,
    }),
};
