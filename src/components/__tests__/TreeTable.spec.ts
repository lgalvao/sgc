import {mount} from '@vue/test-utils';
import {beforeEach, describe, expect, it, vi} from 'vitest';
import TreeTable from '../TreeTable.vue';
import TreeRow from '../TreeRow.vue'; // Importar o componente real

// Mock do componente TreeRow
const mockTreeRow = {
    props: ['item', 'level', 'columns'],
    emits: ['toggle', 'row-click'],
    template: `
      <tr @click="$emit('row-click', item)">
        <td v-for="(column, index) in columns" :key="column.key"
            :style="index === 0 ? { paddingLeft: (level * 20) + 'px' } : {}">
              <span v-if="index === 0 && item.children && item.children.length > 0"
                    @click.stop="$emit('toggle', item.id)"
                    class="toggle-icon">
                <i :class="['bi', item.expanded ? 'bi-chevron-down' : 'bi-chevron-right']"></i>
              </span>
          {{ item[column.key] }}
        </td>
      </tr>
      <template v-if="item.expanded && item.children">
        <TreeRow
            v-for="child in item.children"
            :key="child.id"
            :item="child"
            :level="level + 1"
            :columns="columns"
            @toggle="$emit('toggle', $event)"
            @row-click="$emit('row-click', $event)"
        />
      </template>
    `,
};

describe('TreeTable.vue', () => {
    const mockData = [
        {
            id: 1, nome: 'Item 1', value: 'A', children: [
                {id: 11, nome: 'SubItem 1.1', value: 'A1'},
                {id: 12, nome: 'SubItem 1.2', value: 'A2'},
            ]
        },
        {id: 2, nome: 'Item 2', value: 'B'},
    ];

    const mockColumns = [
        {key: 'nome', label: 'Nome', width: '50%'},
        {key: 'value', label: 'Valor', width: '50%'},
    ];

    beforeEach(() => {
        vi.clearAllMocks();
    });

    it('deve renderizar o título corretamente', () => {
        const wrapper = mount(TreeTable, {
            props: {data: [], columns: [], title: 'Meu Título'},
            global: {stubs: {TreeRow: mockTreeRow}},
        });
        expect(wrapper.find('h4').text()).toBe('Meu Título');
    });

    it('não deve renderizar o título se não for fornecido', () => {
        const wrapper = mount(TreeTable, {
            props: {data: [], columns: []},
            global: {stubs: {TreeRow: mockTreeRow}},
        });
        expect(wrapper.find('h4').exists()).toBe(false);
    });

    it('deve renderizar os cabeçalhos da tabela corretamente', () => {
        const wrapper = mount(TreeTable, {
            props: {data: [], columns: mockColumns},
            global: {stubs: {TreeRow: mockTreeRow}},
        });
        const headers = wrapper.findAll('th');
        expect(headers.length).toBe(mockColumns.length);
        expect(headers[0].text()).toBe('Nome');
        expect(headers[1].text()).toBe('Valor');
    });

    it('não deve renderizar os cabeçalhos se hideHeaders for true', () => {
        const wrapper = mount(TreeTable, {
            props: {data: [], columns: mockColumns, hideHeaders: true},
            global: {stubs: {TreeRow: mockTreeRow}},
        });
        expect(wrapper.find('thead').exists()).toBe(false);
    });

    it('deve renderizar as linhas da tabela passando os dados para TreeRow', () => {
        const wrapper = mount(TreeTable, {
            props: {data: mockData, columns: mockColumns},
            global: {stubs: {TreeRow: mockTreeRow}},
        });
        const treeRows = wrapper.findAllComponents(TreeRow);
        expect(treeRows.length).toBe(mockData.length); // Apenas os itens de nível superior
        expect(treeRows[0].props().item).toEqual(expect.objectContaining({id: 1, nome: 'Item 1'}));
        expect(treeRows[1].props().item).toEqual(expect.objectContaining({id: 2, nome: 'Item 2'}));
    });

    it('deve emitir o evento row-click quando uma TreeRow filha emite', async () => {
        const wrapper = mount(TreeTable, {
            props: {data: mockData, columns: mockColumns},
            global: {stubs: {TreeRow: mockTreeRow}},
        });
        const treeRows = wrapper.findAllComponents(TreeRow);
        await treeRows[0].vm.$emit('row-click', mockData[0]);
        expect(wrapper.emitted()['row-click']).toBeTruthy();
        expect(wrapper.emitted()['row-click'][0]).toEqual([mockData[0]]);
    });

    it('deve expandir todos os itens ao chamar expandAll', async () => {
        const dataWithChildren = [
            {
                id: 1, nome: 'Item 1', expanded: false, children: [
                    {id: 11, nome: 'SubItem 1.1', expanded: false},
                ]
            },
        ];
        const wrapper = mount(TreeTable, {
            props: {data: dataWithChildren, columns: mockColumns, title: 'Test Title'}, // Adicionar title
            global: {stubs: {TreeRow: mockTreeRow}},
        });

        // Verificar estado inicial do ícone do filho
        const childTreeRow = wrapper.findAllComponents(TreeRow)[0]; // O primeiro filho
        expect(childTreeRow.find('.bi-chevron-right').exists()).toBe(true); // Ícone de colapsado

        await wrapper.find('button.btn-outline-primary').trigger('click'); // Usar seletor de botão
        await wrapper.vm.$nextTick(); // Adicionar esta linha

        // Verificar estado após expandAll. O ícone do filho deve mudar para expandido
        expect(childTreeRow.find('.bi-chevron-down').exists()).toBe(true); // Ícone de expandido
    });

    it('deve colapsar todos os itens ao chamar collapseAll', async () => {
        const dataWithChildren = [
            {
                id: 1, nome: 'Item 1', expanded: true, children: [
                    {id: 11, nome: 'SubItem 1.1', expanded: true},
                ]
            },
        ];
        const wrapper = mount(TreeTable, {
            props: {data: dataWithChildren, columns: mockColumns, title: 'Test Title'},
            global: {stubs: {TreeRow: mockTreeRow}},
        });

        // Encontrar o botão de colapsar e clicar nele
        const collapseButton = wrapper.find('button.btn-outline-secondary');
        await collapseButton.trigger('click');
        await wrapper.vm.$nextTick();

        // Verificar se a propriedade `expanded` foi definida como false no item
        const treeRow = wrapper.findComponent(TreeRow);
        expect(treeRow.props().item.expanded).toBe(false);
    });

    it('deve encontrar item existente usando findItemById', async () => {
        const dataWithNestedChildren = [
            {
                id: 1, nome: 'Item 1', expanded: false, children: [
                    {id: 11, nome: 'SubItem 1.1', expanded: false, children: [
                        {id: 111, nome: 'SubSubItem 1.1.1', expanded: false}
                    ]},
                    {id: 12, nome: 'SubItem 1.2', expanded: false},
                ]
            },
            {id: 2, nome: 'Item 2', expanded: false},
        ];
        const wrapper = mount(TreeTable, {
            props: {data: dataWithNestedChildren, columns: mockColumns},
            global: {stubs: {TreeRow: mockTreeRow}},
        });

        // Testar encontrar item de nível superior
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        const vm = wrapper.vm as any;
        const foundItem = vm.findItemById(dataWithNestedChildren, 1);
        expect(foundItem).toBeTruthy();
        expect(foundItem?.nome).toBe('Item 1');

        // Testar encontrar item aninhado
        const foundNestedItem = vm.findItemById(dataWithNestedChildren, 111);
        expect(foundNestedItem).toBeTruthy();
        expect(foundNestedItem?.nome).toBe('SubSubItem 1.1.1');
    });

    it('deve retornar null quando item não encontrado usando findItemById', async () => {
        const mockData = [
            {id: 1, nome: 'Item 1'},
            {id: 2, nome: 'Item 2'},
        ];
        const wrapper = mount(TreeTable, {
            props: {data: mockData, columns: mockColumns},
            global: {stubs: {TreeRow: mockTreeRow}},
        });

        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        const vm = wrapper.vm as any;
        const notFoundItem = vm.findItemById(mockData, 999);
        expect(notFoundItem).toBeNull();
    });

    it('deve alternar expansão de item usando toggleExpand', async () => {
        const dataWithChildren = [
            {
                id: 1, nome: 'Item 1', expanded: false, children: [
                    {id: 11, nome: 'SubItem 1.1'},
                ]
            },
        ];
        const wrapper = mount(TreeTable, {
            props: {data: dataWithChildren, columns: mockColumns},
            global: {stubs: {TreeRow: mockTreeRow}},
        });

        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        const vm = wrapper.vm as any;

        // Verificar estado inicial do internalData
        expect(vm.internalData[0].expanded).toBe(false);

        // Alternar para expandido
        vm.toggleExpand(1);
        expect(vm.internalData[0].expanded).toBe(true);

        // Alternar para colapsado
        vm.toggleExpand(1);
        expect(vm.internalData[0].expanded).toBe(false);
    });

    it('não deve fazer nada ao tentar alternar item inexistente usando toggleExpand', async () => {
        const mockData = [
            {id: 1, nome: 'Item 1', expanded: false},
        ];
        const wrapper = mount(TreeTable, {
            props: {data: mockData, columns: mockColumns},
            global: {stubs: {TreeRow: mockTreeRow}},
        });

        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        const vm = wrapper.vm as any;

        // Tentar alternar item inexistente
        vm.toggleExpand(999);

        // Verificar que não quebrou e manteve estado
        expect(mockData[0].expanded).toBe(false);
    });
});
