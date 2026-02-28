import type {Meta, StoryObj} from '@storybook/vue3-vite';
import TreeRowItem from './TreeRowItem.vue';

const meta: Meta<typeof TreeRowItem> = {
    title: 'Comum/TreeRowItem',
    component: TreeRowItem,
    tags: ['autodocs'],
    argTypes: {
        onToggle: {action: 'toggle'},
        'onRow-click': {action: 'row-click'},
    },
    decorators: [
        () => ({
            template: '<table class="table"><tbody><story /></tbody></table>',
        }),
    ],
};

export default meta;
type Story = StoryObj<typeof TreeRowItem>;

const mockColumns = [{key: 'nome'}, {key: 'valor'}];

export const Raiz: Story = {
    args: {
        item: {codigo: 1, nome: 'Item Raiz', valor: '100', children: [{codigo: 1.1}], expanded: false},
        level: 0,
        columns: mockColumns,
    },
};

export const Nivel1: Story = {
    args: {
        item: {codigo: 2, nome: 'Item Nível 1', valor: '50'},
        level: 1,
        columns: mockColumns,
    },
};

export const Nivel2: Story = {
    args: {
        item: {codigo: 3, nome: 'Item Nível 2', valor: '25'},
        level: 2,
        columns: mockColumns,
    },
};

export const Expandido: Story = {
    args: {
        item: {codigo: 4, nome: 'Item Expandido', valor: '200', children: [{codigo: 4.1}], expanded: true},
        level: 0,
        columns: mockColumns,
    },
};
