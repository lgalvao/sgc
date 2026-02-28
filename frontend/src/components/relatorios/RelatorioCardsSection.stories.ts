import type {Meta, StoryObj} from '@storybook/vue3-vite';
import RelatorioCardsSection from './RelatorioCardsSection.vue';

const meta: Meta<typeof RelatorioCardsSection> = {
    title: 'Relatorios/RelatorioCardsSection',
    component: RelatorioCardsSection,
    tags: ['autodocs'],
    argTypes: {
        'onAbrir-mapas-vigentes': {action: 'abrir-mapas-vigentes'},
        'onAbrir-diagnosticos-gaps': {action: 'abrir-diagnosticos-gaps'},
        'onAbrir-andamento-geral': {action: 'abrir-andamento-geral'},
    },
};

export default meta;
type Story = StoryObj<typeof RelatorioCardsSection>;

export const Default: Story = {
    args: {
        mapasVigentesCount: 24,
        diagnosticosGapsCount: 15,
        processosFiltradosCount: 8,
    },
};

export const Vazio: Story = {
    args: {
        mapasVigentesCount: 0,
        diagnosticosGapsCount: 0,
        processosFiltradosCount: 0,
    },
};

export const ValoresAltos: Story = {
    args: {
        mapasVigentesCount: 150,
        diagnosticosGapsCount: 890,
        processosFiltradosCount: 45,
    },
};
