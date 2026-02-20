import {mount} from "@vue/test-utils";
import {describe, expect, it, vi} from "vitest";
import {createMemoryHistory, createRouter} from "vue-router";
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

// Mock services to avoid side effects/heavy init when loading the store
vi.mock("@/services/atividadeService", () => ({}));
vi.mock("@/services/subprocessoService", () => ({}));
vi.mock("@/stores/subprocessos", () => ({ useSubprocessosStore: vi.fn() }));

describe("test-utils/helpers", () => {
    it("getMockAtividadesData deve retornar um array não vazio", () => {
        const data = getMockAtividadesData();
        expect(Array.isArray(data)).toBe(true);
        expect(data.length).toBeGreaterThan(0);
    });

    it("initPinia deve inicializar e retornar uma instância do pinia", () => {
        const pinia = initPinia();
        expect(pinia).toBeDefined();
    });

    it("prepareFreshAtividadesStore deve retornar uma store com dados", async () => {
        const store = await prepareFreshAtividadesStore();
        expect(store).toBeDefined();
        // Since we populate with ID 1
        expect(store.atividadesPorSubprocesso.get(1)).toBeDefined();
        expect(store.atividadesPorSubprocesso.get(1).length).toBeGreaterThan(0);
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

    it("selecionarProcessoEUnidade deve definir valores de seleção", async () => {
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

    it("expectImportButtonDisabled deve verificar atributo disabled", () => {
        const wrapper = mount(TestComponent, {props: {disabled: true}});
        expectImportButtonDisabled(wrapper);
    });

    it("expectImportButtonEnabled deve verificar ausência de atributo disabled", () => {
        const wrapper = mount(TestComponent, {props: {disabled: false}});
        expectImportButtonEnabled(wrapper);
    });

    it("selectFirstCheckbox deve marcar o primeiro checkbox", async () => {
        const wrapper = mount(TestComponent);
        await selectFirstCheckbox(wrapper);
        expect(
            (wrapper.find(".form-check-input").element as HTMLInputElement).checked,
        ).toBe(true);
    });

    it("assertUnidadeOptions deve verificar opções de seleção", () => {
        const wrapper = mount(TestComponent);
        assertUnidadeOptions(wrapper, ["Unidade 1"]);
    });

    it("navigateAndAssertBreadcrumbs deve funcionar corretamente", async () => {
        const routes = [{path: "/", component: TestComponent}];
        const router = createRouter({history: createMemoryHistory(), routes}); // <-- This line
        const mountFn = async () =>
            mount(TestComponent, {global: {plugins: [router]}});
        await navigateAndAssertBreadcrumbs(router, mountFn, "/", ["Page"]);
    });

    it("expectContainsAll deve verificar se o array contém todos os itens", () => {
        const array = ["a", "b", "c"];
        expectContainsAll(array, ["a", "c"]);
    });
});
