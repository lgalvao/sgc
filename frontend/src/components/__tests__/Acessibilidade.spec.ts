import { describe, it, vi } from "vitest";
import { mount } from "@vue/test-utils";
import { checkA11y } from "../../test-utils/a11yTestHelpers";
import EmptyState from "../EmptyState.vue";
import BarraNavegacao from "../BarraNavegacao.vue";

// Mock das dependências de roteamento para BarraNavegacao
vi.mock("vue-router", () => ({
  useRoute: () => ({ path: "/test" }),
  useRouter: () => ({ back: vi.fn() }),
}));

// Mock do composable de breadcrumbs
vi.mock("@/composables/useBreadcrumbs", () => ({
  useBreadcrumbs: () => ({
    breadcrumbs: [{ label: "Home", to: "/", isHome: true }, { label: "Teste", to: "/test" }]
  })
}));

describe("Verificação de Acessibilidade (Axe)", () => {
  it("EmptyState deve ser acessível", async () => {
    const wrapper = mount(EmptyState, {
      props: {
        title: "Título de Teste",
        description: "Descrição de teste",
        icon: "bi-info-circle",
      },
    });

    await checkA11y(wrapper.element as HTMLElement);
  });

  it("BarraNavegacao deve ser acessível", async () => {
    const wrapper = mount(BarraNavegacao);

    await checkA11y(wrapper.element as HTMLElement);
  });
});
