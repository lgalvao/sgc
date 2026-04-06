import {mount} from "@vue/test-utils";
import {beforeEach, describe, expect, it, vi} from "vitest";
import TreeRowItem from "../comum/TreeRowItem.vue";
import {setupComponentTest} from "@/test-utils/componentTestHelpers";

describe("TreeRowItem.vue", () => {
    setupComponentTest();

    beforeEach(() => {
        vi.clearAllMocks();
    });

    it("deve renderizar o item corretamente", () => {
        const item = {codigo: 1, nome: "Item 1", situacao: "Ativo"};
        const columns = [
            {key: "nome", label: "Nome"},
            {key: "situacao", label: "Situação"},
        ];
        const wrapper = mount(TreeRowItem, {
            props: {item, columns, level: 0},
        });

        expect(wrapper.text()).toContain("Item 1");
        expect(wrapper.text()).toContain("Ativo");
    });

    it("deve indicar visualmente que a linha clicavel pode ser acionada", () => {
        const item = {codigo: 1, nome: "Item 1", clickable: true};
        const columns = [{key: "nome", label: "Nome"}];
        const wrapper = mount(TreeRowItem, {
            props: {item, columns, level: 0},
        });

        expect(wrapper.find("tr").classes()).toContain("tree-row");
        expect(wrapper.find("td").classes()).toContain("tree-table-primeira-coluna");
    });

    it("deve renderizar faixa fixa para alinhamento da primeira coluna", () => {
        const item = {codigo: 1, nome: "Item 1"};
        const columns = [{key: "nome", label: "Nome"}];
        const wrapper = mount(TreeRowItem, {
            props: {item, columns, level: 0},
        });

        expect(wrapper.find(".tree-table-primeira-coluna-conteudo").exists()).toBe(true);
        expect(wrapper.find(".tree-table-toggle-slot").exists()).toBe(true);
        expect(wrapper.find(".tree-table-texto").text()).toBe("Item 1");
    });

    it("deve aplicar recuo adicional em niveis mais profundos", () => {
        const item = {codigo: 1, nome: "Item 1"};
        const columns = [{key: "nome", label: "Nome"}];
        const wrapper = mount(TreeRowItem, {
            props: {item, columns, level: 2},
        });

        const conteudo = wrapper.find(".tree-table-primeira-coluna-conteudo");
        expect(conteudo.attributes().style).toContain("--tree-table-largura-gutter: 1.75rem;");
        expect(conteudo.attributes().style).toContain("--tree-table-recuo-nivel: 2rem;");
    });

    it("deve manter respiro interno na primeira coluna no nivel raiz", () => {
        const item = {codigo: 1, nome: "Item 1"};
        const columns = [{key: "nome", label: "Nome"}];
        const wrapper = mount(TreeRowItem, {
            props: {item, columns, level: 0},
        });

        expect(wrapper.find(".tree-table-primeira-coluna-conteudo").attributes().style)
            .toContain("--tree-table-largura-gutter: 1.75rem;");
        expect(wrapper.find(".tree-table-primeira-coluna-conteudo").attributes().style)
            .toContain("--tree-table-recuo-nivel: 0rem;");
    });

    it("deve exibir o toggle-icon se houver children", () => {
        const item = {
            codigo: 1,
            nome: "Item 1",
            children: [{codigo: 2, nome: "Child 1"}],
        };
        const columns = [{key: "nome", label: "Nome"}];
        const wrapper = mount(TreeRowItem, {
            props: {item, columns, level: 0},
        });

        expect(wrapper.find(".toggle-hit-area").exists()).toBe(true);
        expect(wrapper.find(".bi-chevron-right").exists()).toBe(true);
        expect(wrapper.find(".toggle-icon-indicador-expandido").exists()).toBe(false);
    });

    it("deve indicar cursor de expansao no gutter quando item esta recolhido", () => {
        const item = {
            codigo: 1,
            nome: "Item 1",
            expanded: false,
            children: [{codigo: 2, nome: "Child 1"}],
        };
        const columns = [{key: "nome", label: "Nome"}];
        const wrapper = mount(TreeRowItem, {
            props: {item, columns, level: 0},
        });

        expect(wrapper.find(".toggle-hit-area").classes()).toContain("toggle-hit-area-expandir");
    });

    it("não deve exibir o toggle-icon se não houver children", () => {
        const item = {codigo: 1, nome: "Item 1"};
        const columns = [{key: "nome", label: "Nome"}];
        const wrapper = mount(TreeRowItem, {
            props: {item, columns, level: 0},
        });

        expect(wrapper.find(".toggle-hit-area").exists()).toBe(false);
    });

    it("deve emitir o evento toggle ao clicar no toggle-icon", async () => {
        const item = {
            codigo: 1,
            nome: "Item 1",
            children: [{codigo: 2, nome: "Child 1"}],
        };
        const columns = [{key: "nome", label: "Nome"}];
        const wrapper = mount(TreeRowItem, {
            props: {item, columns, level: 0},
        });

        const toggleIcon = wrapper.find(".toggle-hit-area");
        expect(toggleIcon.exists()).toBe(true);

        await toggleIcon.trigger("click");

        expect(wrapper.emitted("toggle")).toHaveLength(1);
        expect(wrapper.emitted("toggle")![0]).toEqual([1]);
    });

    it("deve permitir expandir ao clicar em toda a margem esquerda do toggle", async () => {
        const item = {
            codigo: 1,
            nome: "Item 1",
            clickable: true,
            children: [{codigo: 2, nome: "Child 1"}],
        };
        const columns = [{key: "nome", label: "Nome"}];
        const wrapper = mount(TreeRowItem, {
            props: {item, columns, level: 0},
        });

        await wrapper.find(".tree-table-toggle-slot").trigger("click");

        expect(wrapper.emitted("toggle")).toHaveLength(1);
        expect(wrapper.emitted("row-click")).toBeUndefined();
    });

    it("deve emitir o evento row-click ao clicar na linha se clickable for true (padrao)", async () => {
        const item = {codigo: 1, nome: "Item 1", clickable: true};
        const columns = [{key: "nome", label: "Nome"}];
        const wrapper = mount(TreeRowItem, {
            props: {item, columns, level: 0},
        });

        await wrapper.find("tr").trigger("click");

        expect(wrapper.emitted("row-click")).toHaveLength(1);
        expect(wrapper.emitted("row-click")![0]).toEqual([item]);
    });

    it("deve emitir o evento row-click ao clicar na linha se clickable for undefined (default true logic check)", async () => {
        const item = {codigo: 1, nome: "Item 1"};
        const columns = [{key: "nome", label: "Nome"}];
        const wrapper = mount(TreeRowItem, {
            props: {item, columns, level: 0},
        });

        await wrapper.find("tr").trigger("click");

        expect(wrapper.emitted("row-click")).toHaveLength(1);
    });

    it("não deve emitir o evento row-click ao clicar na linha se clickable for false", async () => {
        const item = {codigo: 1, nome: "Item 1", clickable: false};
        const columns = [{key: "nome", label: "Nome"}];
        const wrapper = mount(TreeRowItem, {
            props: {item, columns, level: 0},
        });

        await wrapper.find("tr").trigger("click");

        expect(wrapper.emitted("row-click")).toBeUndefined();
    });

    it("deve exibir o ícone chevron-down quando item está expandido", () => {
        const item = {
            codigo: 1,
            nome: "Item 1",
            children: [{codigo: 2, nome: "Child 1"}],
            expanded: true,
        };
        const columns = [{key: "nome", label: "Nome"}];
        const wrapper = mount(TreeRowItem, {
            props: {item, columns, level: 0},
        });

        expect(wrapper.find(".toggle-hit-area").exists()).toBe(true);
        expect(wrapper.find(".toggle-hit-area").classes()).toContain("toggle-hit-area-recolher");
        expect(wrapper.find(".bi-chevron-right").exists()).toBe(true);
        expect(wrapper.find(".toggle-icon-indicador-expandido").exists()).toBe(true);
    });

    it("deve emitir o evento toggle ao pressionar Enter no toggle-icon", async () => {
        const item = {
            codigo: 1,
            nome: "Item 1",
            children: [{codigo: 2, nome: "Child 1"}],
        };
        const columns = [{key: "nome", label: "Nome"}];
        const wrapper = mount(TreeRowItem, {
            props: {item, columns, level: 0},
        });

        const toggleIcon = wrapper.find(".toggle-hit-area");
        expect(toggleIcon.exists()).toBe(true);

        await toggleIcon.trigger("keydown.enter");

        expect(wrapper.emitted("toggle")).toHaveLength(1);
        expect(wrapper.emitted("toggle")![0]).toEqual([1]);
    });

    it("deve emitir o evento toggle ao pressionar Space no toggle-icon", async () => {
        const item = {
            codigo: 1,
            nome: "Item 1",
            children: [{codigo: 2, nome: "Child 1"}],
        };
        const columns = [{key: "nome", label: "Nome"}];
        const wrapper = mount(TreeRowItem, {
            props: {item, columns, level: 0},
        });

        const toggleIcon = wrapper.find(".toggle-hit-area");
        expect(toggleIcon.exists()).toBe(true);

        await toggleIcon.trigger("keydown.space");

        expect(wrapper.emitted("toggle")).toHaveLength(1);
        expect(wrapper.emitted("toggle")![0]).toEqual([1]);
    });

    it("deve emitir o evento row-click ao pressionar Enter na linha", async () => {
        const item = {codigo: 1, nome: "Item 1", clickable: true};
        const columns = [{key: "nome", label: "Nome"}];
        const wrapper = mount(TreeRowItem, {
            props: {item, columns, level: 0},
        });

        await wrapper.find("tr").trigger("keydown.enter");

        expect(wrapper.emitted("row-click")).toHaveLength(1);
        expect(wrapper.emitted("row-click")![0]).toEqual([item]);
    });

    it("deve emitir o evento row-click ao pressionar Space na linha", async () => {
        const item = {codigo: 1, nome: "Item 1", clickable: true};
        const columns = [{key: "nome", label: "Nome"}];
        const wrapper = mount(TreeRowItem, {
            props: {item, columns, level: 0},
        });

        await wrapper.find("tr").trigger("keydown.space");

        expect(wrapper.emitted("row-click")).toHaveLength(1);
        expect(wrapper.emitted("row-click")![0]).toEqual([item]);
    });
});
