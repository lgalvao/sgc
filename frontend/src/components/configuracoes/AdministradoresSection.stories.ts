import type {Meta, StoryObj} from '@storybook/vue3-vite';
import AdministradoresSection from './AdministradoresSection.vue';
import {createTestingPinia} from '@pinia/testing';
import {vi} from 'vitest';

const meta: Meta<typeof AdministradoresSection> = {
    title: 'Configuracoes/AdministradoresSection',
    component: AdministradoresSection,
    tags: ['autodocs'],
};

export default meta;
type Story = StoryObj<typeof AdministradoresSection>;

export const Default: Story = {
    render: () => ({
        components: {AdministradoresSection},
        setup() {
            const pinia = createTestingPinia({createSpy: vi.fn});
            return {pinia};
        },
        template: '<AdministradoresSection />',
    }),
};
