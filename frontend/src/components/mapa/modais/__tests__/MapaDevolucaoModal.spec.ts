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

    async function definirObservacao(wrapper: ReturnType<typeof mount>, conteudoHtml: string) {
        const editor = wrapper.find('[data-testid="inp-devolucao-mapa-obs"]');
        (editor.element as HTMLDivElement).innerHTML = conteudoHtml;
        await editor.trigger("input");
    }

    it("deve renderizar corretamente", () => {
        const wrapper = mount(MapaDevolucaoModal, {
            props: defaultProps,
            global: {
                plugins: [createTestingPinia()],
                stubs
            }
        });
        expect(wrapper.text()).toContain("Confirma a devolução");
        expect(wrapper.find('[data-testid="inp-devolucao-mapa-obs"]').exists()).toBe(true);
    });

    it("deve emitir update:observacao ao digitar", async () => {
        const wrapper = mount(MapaDevolucaoModal, {
            props: defaultProps,
            global: {
                plugins: [createTestingPinia()],
                stubs
            }
        });
        await definirObservacao(wrapper, "<p>Minha justificativa</p>");
        expect(wrapper.emitted("update:observacao")).toBeDefined();
        expect(wrapper.emitted("update:observacao")![0]).toEqual(["<p>Minha justificativa</p>"]);
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
