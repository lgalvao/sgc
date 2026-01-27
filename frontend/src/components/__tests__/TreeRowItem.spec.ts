import {mount} from "@vue/test-utils";
import {beforeEach, describe, expect, it, vi} from "vitest";
import TreeRowItem from "../TreeRowItem.vue";
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

    it("deve aplicar paddingLeft com base no level", () => {
        const item = {codigo: 1, nome: "Item 1"};
        const columns = [{key: "nome", label: "Nome"}];
        const wrapper = mount(TreeRowItem, {
            props: {item, columns, level: 2},
        });

        const firstTd = wrapper.find("td");
        expect(firstTd.attributes().style).toContain("padding-left: 2.5rem;");
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

        expect(wrapper.find(".toggle-icon").exists()).toBe(true);
        expect(wrapper.find(".bi-chevron-right").exists()).toBe(true);
    });

    it("não deve exibir o toggle-icon se não houver children", () => {
        const item = {codigo: 1, nome: "Item 1"};
        const columns = [{key: "nome", label: "Nome"}];
        const wrapper = mount(TreeRowItem, {
            props: {item, columns, level: 0},
        });

        expect(wrapper.find(".toggle-icon").exists()).toBe(false);
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

        const toggleIcon = wrapper.find(".toggle-icon");
        expect(toggleIcon.exists()).toBe(true);

        await toggleIcon.trigger("click");

        expect(wrapper.emitted("toggle")).toHaveLength(1);
        expect(wrapper.emitted("toggle")![0]).toEqual([1]);
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
        // Since clickable? is optional, if it's undefined, the check `props.item.clickable === false` is false, so it should emit.
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

        expect(wrapper.find(".toggle-icon").exists()).toBe(true);
        expect(wrapper.find(".bi-chevron-down").exists()).toBe(true);
        expect(wrapper.find(".bi-chevron-right").exists()).toBe(false);
    });

    it("deve emitir o evento toggle ao pressionar Enter no toggle-icon", async () => {
        const item = {
            codigo: 1,
            nome: "Item 1",
            children: [{ codigo: 2, nome: "Child 1" }],
        };
        const columns = [{ key: "nome", label: "Nome" }];
        const wrapper = mount(TreeRowItem, {
            props: { item, columns, level: 0 },
        });

        const toggleIcon = wrapper.find(".toggle-icon");
        expect(toggleIcon.exists()).toBe(true);

        await toggleIcon.trigger("keydown.enter");

        expect(wrapper.emitted("toggle")).toHaveLength(1);
        expect(wrapper.emitted("toggle")![0]).toEqual([1]);
    });

    it("deve emitir o evento toggle ao pressionar Space no toggle-icon", async () => {
        const item = {
            codigo: 1,
            nome: "Item 1",
            children: [{ codigo: 2, nome: "Child 1" }],
        };
        const columns = [{ key: "nome", label: "Nome" }];
        const wrapper = mount(TreeRowItem, {
            props: { item, columns, level: 0 },
        });

        const toggleIcon = wrapper.find(".toggle-icon");
        expect(toggleIcon.exists()).toBe(true);

        await toggleIcon.trigger("keydown.space");

        expect(wrapper.emitted("toggle")).toHaveLength(1);
        expect(wrapper.emitted("toggle")![0]).toEqual([1]);
    });

    it("deve emitir o evento row-click ao pressionar Enter na linha", async () => {
        const item = { codigo: 1, nome: "Item 1", clickable: true };
        const columns = [{ key: "nome", label: "Nome" }];
        const wrapper = mount(TreeRowItem, {
            props: { item, columns, level: 0 },
        });

        await wrapper.find("tr").trigger("keydown.enter");

        expect(wrapper.emitted("row-click")).toHaveLength(1);
        expect(wrapper.emitted("row-click")![0]).toEqual([item]);
    });

    it("deve emitir o evento row-click ao pressionar Space na linha", async () => {
        const item = { codigo: 1, nome: "Item 1", clickable: true };
        const columns = [{ key: "nome", label: "Nome" }];
        const wrapper = mount(TreeRowItem, {
            props: { item, columns, level: 0 },
        });

        await wrapper.find("tr").trigger("keydown.space");

        expect(wrapper.emitted("row-click")).toHaveLength(1);
        expect(wrapper.emitted("row-click")![0]).toEqual([item]);
    });
});
