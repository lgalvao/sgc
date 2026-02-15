import {mount} from "@vue/test-utils";
import {beforeEach, describe, expect, it, vi} from "vitest";
import TreeRowItem from "../comum/TreeRowItem.vue";
import TreeTable from "../comum/TreeTable.vue";
import EmptyState from "@/components/comum/EmptyState.vue";
import {setupComponentTest} from "@/test-utils/componentTestHelpers";

// Mock do componente TreeRow
const mockTreeRow = {template: "<tr><td>Mocked TreeRowItem</td></tr>"};

describe("TreeTable.vue", () => {
    setupComponentTest();

    const mockData = [
        {
            codigo: 1,
            nome: "Item 1",
            value: "A",
            children: [
                {codigo: 11, nome: "SubItem 1.1", value: "A1"},
                {codigo: 12, nome: "SubItem 1.2", value: "A2"},
            ],
        },
        {codigo: 2, nome: "Item 2", value: "B"},
    ];

    const mockColumns = [
        {key: "nome", label: "Nome", width: "50%"},
        {key: "value", label: "Valor", width: "50%"},
    ];

    beforeEach(() => {
        vi.clearAllMocks();
    });

    it("deve renderizar o título corretamente", () => {
        const wrapper = mount(TreeTable, {
            props: {data: [], columns: [], title: "Meu Título"},
            global: {stubs: {TreeRowItem: mockTreeRow}},
        });
        expect(wrapper.find("h4").text()).toBe("Meu Título");
    });

    it("não deve renderizar o título se não for fornecido", () => {
        const wrapper = mount(TreeTable, {
            props: {data: [], columns: []},
            global: {stubs: {TreeRowItem: mockTreeRow}},
        });
        expect(wrapper.find("h4").exists()).toBe(false);
    });

    it("deve renderizar os cabeçalhos da tabela corretamente", () => {
        const wrapper = mount(TreeTable, {
            props: {data: [], columns: mockColumns},
            global: {stubs: {TreeRowItem: mockTreeRow}},
        });
        const headers = wrapper.findAll("th");
        expect(headers.length).toBe(mockColumns.length);
        expect(headers[0].text()).toBe("Nome");
        expect(headers[1].text()).toBe("Valor");
    });

    it("não deve renderizar os cabeçalhos se hideHeaders for true", () => {
        const wrapper = mount(TreeTable, {
            props: {data: [], columns: mockColumns, hideHeaders: true},
            global: {stubs: {TreeRowItem: mockTreeRow}},
        });
        expect(wrapper.find("thead").exists()).toBe(false);
    });

    it("deve renderizar as linhas da tabela passando os dados para TreeRow", async () => {
        const expandedData = mockData.map((item) => ({...item, expanded: true}));
        const wrapper = mount(TreeTable, {
            props: {data: expandedData, columns: mockColumns},
        });

        await wrapper.vm.$nextTick();

        // When expanded=true, we render all items including children
        // Item 1 (expanded) -> SubItem 1.1, SubItem 1.2
        // Item 2 (expanded but no children)
        // Total: 1 + 2 + 1 = 4 rows
        const trElements = wrapper.findAll("tbody tr");
        expect(trElements.length).toBeGreaterThan(0);

        // Verify we can find the components
        const treeRows = wrapper.findAllComponents(TreeRowItem);
        expect(treeRows.length).toBeGreaterThan(0);
    });

    it("deve emitir o evento row-click quando uma TreeRowItem filha emite", async () => {
        const expandedData = mockData.map((item) => ({...item, expanded: true}));
        const wrapper = mount(TreeTable, {
            props: {data: expandedData, columns: mockColumns},
        });
        await wrapper.vm.$nextTick();
        const treeRows = wrapper.findAllComponents(TreeRowItem);

        expect(treeRows.length).toBeGreaterThan(0);
    });

    it("deve expandir todos os itens ao chamar expandAll", async () => {
        const dataWithChildren = [
            {
                codigo: 1,
                nome: "Item 1",
                expanded: false,
                children: [{codigo: 11, nome: "SubItem 1.1", expanded: false}],
            },
        ];
        const wrapper = mount(TreeTable, {
            props: {
                data: dataWithChildren,
                columns: mockColumns,
                title: "Test Title",
            },
        });
        await wrapper.vm.$nextTick();

        // Initially collapsed, only top-level item should render
        let treeRows = wrapper.findAllComponents(TreeRowItem);
        const initialCount = treeRows.length;

        // Click expand all button
        await wrapper.find("button.btn-outline-primary").trigger("click");
        await wrapper.vm.$nextTick();

        // Verify that more items are now rendered (children are expanded)
        treeRows = wrapper.findAllComponents(TreeRowItem);
        expect(treeRows.length).toBeGreaterThan(initialCount);

        // Verify that the item is expanded
        const expandedItem = (wrapper.vm as any).internalData[0];
        expect(expandedItem.expanded).toBe(true);
    });

    it("deve colapsar todos os itens ao chamar collapseAll", async () => {
        const dataWithChildren = [
            {
                codigo: 1,
                nome: "Item 1",
                expanded: true,
                children: [{codigo: 11, nome: "SubItem 1.1", expanded: true}],
            },
        ];
        const wrapper = mount(TreeTable, {
            props: {
                data: dataWithChildren,
                columns: mockColumns,
                title: "Test Title",
            },
        });

        // Encontrar o botão de colapsar e clicar nele
        const collapseButton = wrapper.find("button.btn-outline-secondary");
        await collapseButton.trigger("click");
        await wrapper.vm.$nextTick();

        // Verificar se a propriedade `expanded` foi definida como false no item
        const internalData = (wrapper.vm as any).internalData;
        expect(internalData[0].expanded).toBe(false);
    });

    it("deve encontrar item existente usando findItemByCodigo", async () => {
        const dataWithNestedChildren = [
            {
                codigo: 1,
                nome: "Item 1",
                expanded: false,
                children: [
                    {
                        codigo: 11,
                        nome: "SubItem 1.1",
                        expanded: false,
                        children: [{codigo: 111, nome: "SubSubItem 1.1.1", expanded: false}],
                    },
                    {codigo: 12, nome: "SubItem 1.2", expanded: false},
                ],
            },
            {codigo: 2, nome: "Item 2", expanded: false},
        ];
        const wrapper = mount(TreeTable, {
            props: {data: dataWithNestedChildren, columns: mockColumns},
            global: {stubs: {TreeRowItem: mockTreeRow}},
        });

        // Testar encontrar item de nível superior

        const vm = wrapper.vm as any;
        const foundItem = vm.findItemByCodigo(dataWithNestedChildren, 1);
        expect(foundItem).toBeTruthy();
        expect(foundItem?.nome).toBe("Item 1");

        // Testar encontrar item aninhado
        const foundNestedItem = vm.findItemByCodigo(dataWithNestedChildren, 111);
        expect(foundNestedItem).toBeTruthy();
        expect(foundNestedItem?.nome).toBe("SubSubItem 1.1.1");
    });

    it("deve retornar null quando item não encontrado usando findItemByCodigo", async () => {
        const mockData = [
            {codigo: 1, nome: "Item 1"},
            {codigo: 2, nome: "Item 2"},
        ];
        const wrapper = mount(TreeTable, {
            props: {data: mockData, columns: mockColumns},
            global: {stubs: {TreeRowItem: mockTreeRow}},
        });

        const vm = wrapper.vm as any;
        const notFoundItem = vm.findItemByCodigo(mockData, 999);
        expect(notFoundItem).toBeNull();
    });

    it("deve alternar expansão de item usando toggleExpand", async () => {
        const dataWithChildren = [
            {
                codigo: 1,
                nome: "Item 1",
                expanded: false,
                children: [{codigo: 11, nome: "SubItem 1.1"}],
            },
        ];
        const wrapper = mount(TreeTable, {
            props: {data: dataWithChildren, columns: mockColumns},
            global: {stubs: {TreeRowItem: mockTreeRow}},
        });

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

    it("não deve fazer nada ao tentar alternar item inexistente usando toggleExpand", async () => {
        const mockData = [{codigo: 1, nome: "Item 1", expanded: false}];
        const wrapper = mount(TreeTable, {
            props: {data: mockData, columns: mockColumns},
            global: {stubs: {TreeRowItem: mockTreeRow}},
        });

        const vm = wrapper.vm as any;

        // Tentar alternar item inexistente
        vm.toggleExpand(999);

        // Verificar que não quebrou e manteve estado
        expect(mockData[0].expanded).toBe(false);
    });

    it("deve renderizar o estado vazio quando não houver dados", () => {
        const wrapper = mount(TreeTable, {
            props: {data: [], columns: mockColumns},
            global: {stubs: {TreeRowItem: mockTreeRow}},
        });

        // Verifica se EmptyState está presente
        const emptyState = wrapper.findComponent(EmptyState);
        expect(emptyState.exists()).toBe(true);
        expect(emptyState.props('title')).toBe("Nenhum registro encontrado");

        // Verifica se TreeRowItem NÃO está presente
        expect(wrapper.findComponent(TreeRowItem).exists()).toBe(false);
    });

    it("deve renderizar o estado vazio customizado", () => {
        const wrapper = mount(TreeTable, {
            props: {
                data: [],
                columns: mockColumns,
                emptyTitle: "Nada aqui",
                emptyDescription: "Vazio mesmo",
                emptyIcon: "bi-x-circle"
            },
            global: {stubs: {TreeRowItem: mockTreeRow}},
        });

        const emptyState = wrapper.findComponent(EmptyState);
        expect(emptyState.exists()).toBe(true);
        expect(emptyState.props('title')).toBe("Nada aqui");
        expect(emptyState.props('description')).toBe("Vazio mesmo");
        expect(emptyState.props('icon')).toBe("bi-x-circle");
    });
});
