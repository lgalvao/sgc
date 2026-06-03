import type {Meta, StoryObj} from '@storybook/vue3-vite';
import {ref} from 'vue';
import LoginCredenciaisCampos from './LoginCredenciaisCampos.vue';

const meta: Meta<typeof LoginCredenciaisCampos> = {
    title: 'Login/LoginCredenciaisCampos',
    component: LoginCredenciaisCampos,
    tags: ['autodocs'],
    argTypes: {
        loginBloqueado: {control: 'boolean'},
        carregando: {control: 'boolean'},
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
        carregando: false,
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
};

export const ComErros: Story = {
    args: {
        titulo: '12345',
        senha: '123',
        loginBloqueado: false,
        carregando: false,
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
};

export const Carregando: Story = {
    args: {
        titulo: '012345678',
        senha: 'minhasenha',
        loginBloqueado: false,
        carregando: true,
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
};

export const ComCapsLock: Story = {
    args: {
        titulo: '012345678',
        senha: '',
        loginBloqueado: false,
        carregando: false,
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
};

export const SenhaVisivel: Story = {
    args: {
        titulo: '012345678',
        senha: 'minhasenha',
        loginBloqueado: false,
        carregando: false,
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
