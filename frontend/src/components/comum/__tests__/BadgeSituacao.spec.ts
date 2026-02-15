import {describe, expect, it} from "vitest";
import {mount} from "@vue/test-utils";
import BadgeSituacao from "../BadgeSituacao.vue";
import {SituacaoProcesso} from "@/types/tipos";

describe("BadgeSituacao.vue", () => {
  it("aplica classe success para FINALIZADO", () => {
    const wrapper = mount(BadgeSituacao, {
      props: { situacao: SituacaoProcesso.FINALIZADO, texto: "Finalizado" }
    });
    expect(wrapper.find('[data-testid="badge-situacao"]').classes()).toContain("bg-success");
  });

  it("aplica classe primary para EM_ANDAMENTO", () => {
    const wrapper = mount(BadgeSituacao, {
      props: { situacao: SituacaoProcesso.EM_ANDAMENTO, texto: "Em andamento" }
    });
    expect(wrapper.find('[data-testid="badge-situacao"]').classes()).toContain("bg-primary");
  });
});
