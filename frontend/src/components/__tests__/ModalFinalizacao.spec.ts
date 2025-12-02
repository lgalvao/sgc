import {mount} from "@vue/test-utils";
import {describe, expect, it} from "vitest";
import ModalFinalizacao from "../ModalFinalizacao.vue";

describe("ModalFinalizacao", () => {
    const processoDescricao = "Processo de Teste";

    it("não deve renderizar o modal quando mostrar for falso", () => {
    const wrapper = mount(ModalFinalizacao, {
      props: { mostrar: false, processoDescricao },
    });
        expect(wrapper.find(".alert").exists()).toBe(false);
  });

    it("deve renderizar o modal com a descrição do processo", () => {
    const wrapper = mount(ModalFinalizacao, {
      props: { mostrar: true, processoDescricao },
    });
    expect(wrapper.text()).toContain(processoDescricao);
  });

  it('deve emitir "fechar" ao clicar no botão de cancelar', async () => {
    const wrapper = mount(ModalFinalizacao, {
      props: { mostrar: true, processoDescricao },
    });
      await wrapper
          .find('[data-testid="btn-mdl-finalizar-cancelar"]')
          .trigger("click");
      expect(wrapper.emitted("fechar")).toBeTruthy();
  });

  it('deve emitir "confirmar" ao clicar no botão de confirmar', async () => {
    const wrapper = mount(ModalFinalizacao, {
      props: { mostrar: true, processoDescricao },
    });
      await wrapper
          .find('[data-testid="btn-mdl-finalizar-confirmar"]')
          .trigger("click");
      expect(wrapper.emitted("confirmar")).toBeTruthy();
  });
});
