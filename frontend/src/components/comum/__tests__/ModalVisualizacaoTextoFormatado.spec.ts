import {describe, expect, it} from "vitest";
import {mount} from "@vue/test-utils";
import ModalVisualizacaoTextoFormatado from "../ModalVisualizacaoTextoFormatado.vue";

describe("ModalVisualizacaoTextoFormatado", () => {
  it("renderiza o modal e o conteúdo formatado", () => {
    const wrapper = mount(ModalVisualizacaoTextoFormatado, {
      props: {
        modelValue: true,
        titulo: "Titulo Teste",
        conteudo: "Conteudo Teste",
        testIdConteudo: "test-content"
      },
      global: {
        stubs: {
          ModalPadrao: {
            template: "<div><header>{{ titulo }}</header><slot/></div>",
            props: ["titulo"]
          },
          ConteudoTextoFormatado: {
            template: "<div :data-testid='testId'>{{ conteudo }}</div>",
            props: ["conteudo", "testId"]
          }
        }
      }
    });

    expect(wrapper.find("header").text()).toBe("Titulo Teste");
    expect(wrapper.find("[data-testid='test-content']").text()).toBe("Conteudo Teste");
  });

  it("emite fechar e update:modelValue", async () => {
      const wrapper = mount(ModalVisualizacaoTextoFormatado, {
          props: {modelValue: true, titulo: "T", conteudo: "C"},
          global: {
              stubs: {
                  ModalPadrao: {
                      template: "<button @click=\"$emit('fechar'); $emit('update:modelValue', false)\"></button>"
                  },
                  ConteudoTextoFormatado: true
              }
          }
      });

      await wrapper.find("button").trigger("click");
      expect(wrapper.emitted("fechar")).toBeTruthy();
      expect(wrapper.emitted("update:modelValue")?.[0]).toEqual([false]);
  });
});
