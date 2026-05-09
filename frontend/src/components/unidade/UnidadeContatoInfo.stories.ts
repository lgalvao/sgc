import type {Meta, StoryObj} from '@storybook/vue3-vite';
import UnidadeContatoInfo from './UnidadeContatoInfo.vue';
import type {Usuario} from '@/types/tipos';

const meta: Meta<typeof UnidadeContatoInfo> = {
    title: 'Unidade/UnidadeContatoInfo',
    component: UnidadeContatoInfo,
    tags: ['autodocs'],
    argTypes: {
        label: {control: 'text'},
        nomeFallback: {control: 'text'},
        detalhesClass: {control: 'text'},
    },
};

export default meta;
type Story = StoryObj<typeof UnidadeContatoInfo>;

const usuarioCompleto: Usuario = {
    codigo: 10,
    nome: 'Maria Aparecida de Souza',
    matricula: '1234567',
    tituloEleitoral: '234567890',
    unidade: {codigo: 2, nome: 'Pró-Reitoria de Ensino', sigla: 'PROEN'},
    email: 'maria.souza@ifce.edu.br',
    ramal: '3232',
};

const usuarioSemContato: Usuario = {
    codigo: 20,
    nome: 'João Carlos Pereira',
    matricula: '9876543',
    tituloEleitoral: '123456789',
    unidade: {codigo: 4, nome: 'Campus Fortaleza', sigla: 'CF'},
    email: '',
    ramal: '',
};

const usuarioComRamal: Usuario = {
    codigo: 30,
    nome: 'Carlos Eduardo Lima',
    matricula: '4455667',
    tituloEleitoral: '987654321',
    unidade: {codigo: 3, nome: 'Pró-Reitoria de Extensão', sigla: 'PROEX'},
    email: '',
    ramal: '4455',
};

const usuarioComEmail: Usuario = {
    codigo: 40,
    nome: 'Ana Paula Rodrigues',
    matricula: '1122334',
    tituloEleitoral: '111222333',
    unidade: {codigo: 2, nome: 'Pró-Reitoria de Ensino', sigla: 'PROEN'},
    email: 'ana.rodrigues@ifce.edu.br',
    ramal: '',
};

export const ComResponsavelCompleto: Story = {
    args: {
        label: 'Titular:',
        contato: usuarioCompleto,
    },
};

export const SomenteName: Story = {
    args: {
        label: 'Responsável:',
        contato: usuarioSemContato,
    },
};

export const SemContato: Story = {
    args: {
        label: 'Titular:',
        contato: null,
        nomeFallback: 'Não definido',
    },
};

export const SemContatoSemFallback: Story = {
    args: {
        label: 'Responsável:',
        contato: null,
        nomeFallback: '',
    },
};

export const ComRamalSemEmail: Story = {
    args: {
        label: 'Titular:',
        contato: usuarioComRamal,
    },
};

export const ComEmailSemRamal: Story = {
    args: {
        label: 'Responsável:',
        contato: usuarioComEmail,
    },
};
