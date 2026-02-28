import type {Meta, StoryObj} from '@storybook/vue3-vite';
import BarraNavegacao from './BarraNavegacao.vue';

const meta: Meta<typeof BarraNavegacao> = {
    title: 'Layout/BarraNavegacao',
    component: BarraNavegacao,
    tags: ['autodocs'],
    decorators: [
        (story) => ({
            components: {story},
            template: '<div style="padding: 1rem; background-color: #f8f9fa;"><story /></div>',
        }),
    ],
};

export default meta;
type Story = StoryObj<typeof BarraNavegacao>;

export const Default: Story = {
    render: () => ({
        components: {BarraNavegacao},
        setup() {
            // Mocking route and router
            return {};
        },
        template: '<BarraNavegacao />',
    }),
    parameters: {}
};
