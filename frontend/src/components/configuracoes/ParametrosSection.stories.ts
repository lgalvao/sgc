import {vi} from "vitest";
import type {Meta, StoryObj} from '@storybook/vue3-vite';
import ParametrosSection from './ParametrosSection.vue';
import {createTestingPinia} from '@pinia/testing';

const meta: Meta<typeof ParametrosSection> = {
    title: 'Configuracoes/ParametrosSection',
    component: ParametrosSection,
    tags: ['autodocs'],
};

export default meta;
type Story = StoryObj<typeof ParametrosSection>;

export const Default: Story = {
    render: () => ({
        components: {ParametrosSection},
        setup() {
            const pinia = createTestingPinia({
                createSpy: vi.fn,
                initialState: {
                    configuracoes: {
                        parametros: [
                            {codigo: 1, chave: 'DIAS_INATIVACAO_PROCESSO', valor: '30'},
                            {codigo: 2, chave: 'DIAS_ALERTA_NOVO', valor: '3'},
                        ],
                        loading: false,
                    },
                },
            });
            return {pinia};
        },
        template: '<ParametrosSection />',
    }),
};

export const Carregando: Story = {
    render: () => ({
        components: {ParametrosSection},
        setup() {
            const pinia = createTestingPinia({
                createSpy: vi.fn,
                initialState: {
                    configuracoes: {loading: true},
                },
            });
            return {pinia};
        },
        template: '<ParametrosSection />',
    }),
};
