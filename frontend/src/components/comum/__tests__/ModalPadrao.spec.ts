import {describe, expect, it} from "vitest";
import {mount} from "@vue/test-utils";
import ModalPadrao from "../ModalPadrao.vue";

describe("ModalPadrao.vue", () => {
  const mountOptions = {
    props: {
      modelValue: true,
      titulo: "Modal de teste",
    },
    slots: {
      default: "<div>Conteúdo</div>",
    },
    global: {
      stubs: {
        BModal: {
          template: '<div><slot /><slot name="footer" /></div>',
          props: ["modelValue", "title", "size", "fade", "centered"],
          emits: ["update:modelValue", "hide", "shown"],
        },
        BButton: {
          template: '<button :data-testid="dataTestid" :disabled="disabled" @click="$emit(\'click\')"><slot /></button>',
          props: ["disabled", "variant", "dataTestid"],
          emits: ["click"],
        },
      },
    },
  };

  it("renderiza cancelar à esquerda e confirmar à direita", () => {
    const wrapper = mount(ModalPadrao, mountOptions);
    const botoes = wrapper.findAll("button");
    expect(botoes).toHaveLength(2);
    expect(botoes[0].text()).toContain("Cancelar");
    expect(botoes[1].text()).toContain("Confirmar");
  });

  it("emite confirmar ao clicar no botão de ação", async () => {
    const wrapper = mount(ModalPadrao, mountOptions);
    await wrapper.find('[data-testid="btn-modal-padrao-confirmar"]').trigger("click");
    expect(wrapper.emitted("confirmar")).toBeTruthy();
  });

  it("fecha e emite update:modelValue ao cancelar", async () => {
    const wrapper = mount(ModalPadrao, mountOptions);
    await wrapper.find('[data-testid="btn-modal-padrao-cancelar"]').trigger("click");
    expect(wrapper.emitted("fechar")).toBeTruthy();
    expect(wrapper.emitted("update:modelValue")?.[0]).toEqual([false]);
  });
});
