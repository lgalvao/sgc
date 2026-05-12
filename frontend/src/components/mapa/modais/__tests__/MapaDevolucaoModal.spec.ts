import {describe, expect, it} from "vitest";
import {mount} from "@vue/test-utils";
import MapaDevolucaoModal from "../MapaDevolucaoModal.vue";
import {createTestingPinia} from "@pinia/testing";

describe("MapaDevolucaoModal.vue", () => {
    const defaultProps = {
        modelValue: true,
        loading: false,
        observacao: "",
        erro: "",
    };

    const stubs = {
        ModalConfirmacao: {
            template: "<div><slot /></div>",
            props: ["modelValue"],
        },
        EditorTextoRico: {
            props: ["modelValue"],
            emits: ["update:modelValue"],
            template: '<textarea :data-testid="$attrs[\'data-testid\']" :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)"></textarea>',
        },
    };

    function montar(props = defaultProps) {
        return mount(MapaDevolucaoModal, {
            props,
            global: {
                plugins: [createTestingPinia()],
                stubs,
            },
        });
    }

    it("deve renderizar corretamente", () => {
        const wrapper = montar();
        expect(wrapper.text()).toContain("Confirma a devolução");
        expect(wrapper.find('[data-testid="inp-devolucao-mapa-obs"]').exists()).toBe(true);
    });

    it("deve emitir update:observacao ao digitar", async () => {
        const wrapper = montar();
        await wrapper.find('[data-testid="inp-devolucao-mapa-obs"]').setValue("Minha justificativa");
        expect(wrapper.emitted("update:observacao")).toBeDefined();
        expect(wrapper.emitted("update:observacao")![0]).toEqual(["Minha justificativa"]);
    });

    it("deve exibir erro se fornecido", () => {
        const wrapper = montar({...defaultProps, erro: "Campo obrigatório"});
        expect(wrapper.text()).toContain("Campo obrigatório");
    });
});
