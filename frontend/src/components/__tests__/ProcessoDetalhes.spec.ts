import {mount} from "@vue/test-utils";
import {BBadge} from "bootstrap-vue-next";
import {describe, expect, it} from "vitest";
import ProcessoDetalhes from "../ProcessoDetalhes.vue";

describe("ProcessoDetalhes.vue", () => {
    it("renders correctly with props", () => {
    const wrapper = mount(ProcessoDetalhes, {
      props: {
          descricao: "Processo de Teste",
          tipo: "REVISAO",
          situacao: "EM_ANDAMENTO",
      },
      global: {
        components: {
            BBadge,
        },
      },
    });

        expect(wrapper.find('[data-testid="processo-info"]').text()).toBe(
            "Processo de Teste",
        );
        expect(wrapper.text()).toContain("Tipo: REVISAO");
        expect(wrapper.text()).toContain("Situação: EM_ANDAMENTO");
    expect(wrapper.findComponent(BBadge).exists()).toBe(true);
  });
});
