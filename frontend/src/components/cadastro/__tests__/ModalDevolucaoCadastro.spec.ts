import {describe, expect, it} from "vitest";
import {mount} from "@vue/test-utils";
import ModalDevolucaoCadastro from "../ModalDevolucaoCadastro.vue";
import CadastroObservacaoModal from "../CadastroObservacaoModal.vue";

describe("ModalDevolucaoCadastro", () => {
  const defaultProps = {
    modelValue: true,
    loading: false,
    isRevisao: false,
    observacao: "",
  };

  it("calcula estadoObservacao corretamente", async () => {
    const wrapper = mount(ModalDevolucaoCadastro, {
      props: defaultProps,
      global: {
        stubs: {
          CadastroObservacaoModal: {
            template: "<div></div>",
            props: ["estadoObservacao", "feedbackObservacao"]
          }
        }
      }
    });

    const stub = wrapper.findComponent(CadastroObservacaoModal);
    
    // Vazio
    expect(stub.props("estadoObservacao")).toBe(null);

    // Preenchido
    await wrapper.setProps({observacao: "Ok"});
    expect(stub.props("estadoObservacao")).toBe(true);

    // Com erro
    await wrapper.setProps({erroObservacao: "Erro"});
    expect(stub.props("estadoObservacao")).toBe(false);
    expect(stub.props("feedbackObservacao")).toBe("Erro");
  });

  it("exibe textos de revisão quando isRevisao é true", () => {
    const wrapper = mount(ModalDevolucaoCadastro, {
      props: {...defaultProps, isRevisao: true},
      global: {
        stubs: {
          CadastroObservacaoModal: {
            template: "<div></div>",
            props: ["titulo", "texto"]
          }
        }
      }
    });

    const stub = wrapper.findComponent(CadastroObservacaoModal);
    expect(stub.props("titulo")).toContain("revisão");
    expect(stub.props("texto")).toContain("revisão");
  });
});
