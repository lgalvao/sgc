import {describe, expect, it} from "vitest";
import {mount} from "@vue/test-utils";
import CampoTexto from "../CampoTexto.vue";

describe("CampoTexto.vue", () => {
  it("renderiza erro inline quando informado", () => {
    const wrapper = mount(CampoTexto, {
      props: {
        id: "campo",
        modelValue: "",
        erro: "Campo obrigatório",
      }
    });
    expect(wrapper.text()).toContain("Campo obrigatório");
  });

  it("emite update:modelValue ao alterar valor", async () => {
    const wrapper = mount(CampoTexto, {
      props: {
        id: "campo",
        modelValue: "",
      }
    });
    await wrapper.find("input").setValue("Novo valor");
    expect(wrapper.emitted("update:modelValue")).toBeTruthy();
  });
});
