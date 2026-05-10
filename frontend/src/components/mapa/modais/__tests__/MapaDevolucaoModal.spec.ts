import {describe, expect, it} from "vitest";
import {mount} from "@vue/test-utils";
import MapaDevolucaoModal from "../MapaDevolucaoModal.vue";
import {createTestingPinia} from "@pinia/testing";

describe("MapaDevolucaoModal.vue", () => {
    const defaultProps = {
        modelValue: true,
        loading: false,
        observacao: "",
        erro: ""
    };

    const stubs = {
        ModalConfirmacao: {
            template: '<div><slot /></div>',
            props: ['modelValue']
        }
    };

    it("deve renderizar corretamente", () => {
        const wrapper = mount(MapaDevolucaoModal, {
            props: defaultProps,
            global: {
                plugins: [createTestingPinia()],
                stubs
            }
        });
        expect(wrapper.text()).toContain("Confirma a devolução");
        expect(wrapper.find("textarea").exists()).toBe(true);
    });

    it("deve emitir update:observacao ao digitar", async () => {
        const wrapper = mount(MapaDevolucaoModal, {
            props: defaultProps,
            global: {
                plugins: [createTestingPinia()],
                stubs
            }
        });
        const textarea = wrapper.find("textarea");
        await textarea.setValue("Minha justificativa");
        expect(wrapper.emitted("update:observacao")).toBeDefined();
        expect(wrapper.emitted("update:observacao")![0]).toEqual(["Minha justificativa"]);
    });

    it("deve exibir erro se fornecido", () => {
        const wrapper = mount(MapaDevolucaoModal, {
            props: {...defaultProps, erro: "Campo obrigatório"},
            global: {
                plugins: [createTestingPinia()],
                stubs
            }
        });
        expect(wrapper.text()).toContain("Campo obrigatório");
    });
});
