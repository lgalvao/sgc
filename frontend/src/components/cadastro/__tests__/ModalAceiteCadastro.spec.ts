import {describe, expect, it} from "vitest";
import {mount} from "@vue/test-utils";
import ModalAceiteCadastro from "../ModalAceiteCadastro.vue";
import CadastroObservacaoModal from "../CadastroObservacaoModal.vue";

describe("ModalAceiteCadastro", () => {
    const defaultProps = {
        modelValue: true,
        loading: false,
        observacao: "Teste",
        acao: {
            tituloModal: "Titulo Teste",
            rotuloConfirmacao: "Confirmar Teste",
            textoModal: "Texto Teste"
        }
    };

    it("renderiza o componente base com as props corretas", () => {
        const wrapper = mount(ModalAceiteCadastro, {
            props: defaultProps,
            global: {
                stubs: {
                    CadastroObservacaoModal: {
                        template: "<div class='stub-obs-modal'><slot/></div>",
                        props: ["titulo", "okTitle", "texto"]
                    }
                }
            }
        });

        const stub = wrapper.findComponent(CadastroObservacaoModal);
        expect(stub.exists()).toBe(true);
        expect(stub.props("titulo")).toBe("Titulo Teste");
        expect(stub.props("okTitle")).toBe("Confirmar Teste");
        expect(stub.props("texto")).toBe("Texto Teste");
    });

    it("emite confirmar quando o modal base emite", async () => {
        const wrapper = mount(ModalAceiteCadastro, {
            props: defaultProps,
            global: {
                stubs: {
                    CadastroObservacaoModal: {
                        template: "<button id='btn-confirmar' @click=\"$emit('confirmar')\"></button>"
                    }
                }
            }
        });

        await wrapper.find("#btn-confirmar").trigger("click");
        expect(wrapper.emitted("confirmar")).toBeTruthy();
    });

    it("atualiza model e observacao através do modal base", async () => {
        const wrapper = mount(ModalAceiteCadastro, {
            props: defaultProps,
            global: {
                stubs: {
                    CadastroObservacaoModal: {
                        template: `
              <div>
                <button id="btn-close" @click="$emit('update:modelValue', false)"></button>
                <button id="btn-obs" @click="$emit('update:observacao', 'Nova Obs')"></button>
              </div>
            `,
                        props: ["modelValue", "observacao"]
                    }
                }
            }
        });

        await wrapper.find("#btn-close").trigger("click");
        expect(wrapper.emitted("update:modelValue")?.[0]).toEqual([false]);

        await wrapper.find("#btn-obs").trigger("click");
        expect(wrapper.emitted("update:observacao")?.[0]).toEqual(["Nova Obs"]);
    });
});
