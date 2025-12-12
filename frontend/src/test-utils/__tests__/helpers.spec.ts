import {mount} from "@vue/test-utils";
import {describe, expect, it} from "vitest";
import {createRouter, createMemoryHistory} from "vue-router";
import {getMockAtividadesData, initPinia, prepareFreshAtividadesStore,} from "../helpers";
import {
    assertUnidadeOptions,
    expectContainsAll,
    expectImportButtonDisabled,
    expectImportButtonEnabled,
    navigateAndAssertBreadcrumbs,
    selecionarProcessoEUnidade,
    selectFirstCheckbox,
} from "../uiHelpers";

describe("test-utils/helpers", () => {
    it("getMockAtividadesData should return a non-empty array", () => {
        const data = getMockAtividadesData();
        expect(Array.isArray(data)).toBe(true);
        expect(data.length).toBeGreaterThan(0);
    });

    it("initPinia should initialize and return a pinia instance", () => {
        const pinia = initPinia();
        expect(pinia).toBeDefined();
    });

    it("prepareFreshAtividadesStore should return a store with data", async () => {
        const store = await prepareFreshAtividadesStore();
        expect(store).toBeDefined();
        expect(store.atividades.length).toBeGreaterThan(0);
    });
});

describe("test-utils/uiHelpers", () => {
    const TestComponent = {
        template: `
      <div>
        <select id="processo-select">
            <option value="1">Processo 1</option>
        </select>
        <select id="unidade-select">
          <option disabled value="">Selecione</option>
          <option value="1">Unidade 1</option>
        </select>
        <button class="btn-outline-primary" :disabled="disabled">Importar</button>
        <input type="checkbox" class="form-check-input" />
        <div data-testid="breadcrumb-item">Home</div>
        <div data-testid="breadcrumb-item">Page</div>
      </div>
    `,
        props: {
            disabled: Boolean,
        },
    };

    it("selecionarProcessoEUnidade should set select values", async () => {
        const wrapper = mount(TestComponent);
        await selecionarProcessoEUnidade(wrapper, 1, 1);
        expect(
            (wrapper.find("select#processo-select").element as HTMLSelectElement)
                .value,
        ).toBe("1");
        expect(
            (wrapper.find("select#unidade-select").element as HTMLSelectElement)
                .value,
        ).toBe("1");
    });

    it("expectImportButtonDisabled should assert disabled attribute", () => {
        const wrapper = mount(TestComponent, {props: {disabled: true}});
        expectImportButtonDisabled(wrapper);
    });

    it("expectImportButtonEnabled should assert no disabled attribute", () => {
        const wrapper = mount(TestComponent, {props: {disabled: false}});
        expectImportButtonEnabled(wrapper);
    });

    it("selectFirstCheckbox should check the first checkbox", async () => {
        const wrapper = mount(TestComponent);
        await selectFirstCheckbox(wrapper);
        expect(
            (wrapper.find(".form-check-input").element as HTMLInputElement).checked,
        ).toBe(true);
    });

    it("assertUnidadeOptions should check select options", () => {
        const wrapper = mount(TestComponent);
        assertUnidadeOptions(wrapper, ["Unidade 1"]);
    });

    it("navigateAndAssertBreadcrumbs should work correctly", async () => {
        const routes = [{path: "/", component: TestComponent}];
        const router = createRouter({history: createMemoryHistory(), routes}); // <-- This line
        const mountFn = async () =>
            mount(TestComponent, {global: {plugins: [router]}});
        await navigateAndAssertBreadcrumbs(router, mountFn, "/", ["Page"]);
    });

    it("expectContainsAll should check if array contains all items", () => {
        const array = ["a", "b", "c"];
        expectContainsAll(array, ["a", "c"]);
    });
});
