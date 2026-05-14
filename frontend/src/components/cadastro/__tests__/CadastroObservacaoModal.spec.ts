import {describe, expect, it, vi} from "vitest";
import {mount} from "@vue/test-utils";
import CadastroObservacaoModal from "../CadastroObservacaoModal.vue";
import {createTestingPinia} from "@pinia/testing";

describe("CadastroObservacaoModal.vue", () => {
    type PropsCadastroObservacaoModal = {
        modelValue: boolean;
        loading: boolean;
        titulo: string;
        okTitle: string;
        texto: string;
        observacao: string;
        testIdConfirmar: string;
        inputId: string;
        inputDataTestid: string;
        label: string;
        erro?: string | null;
        labelObrigatoria?: boolean;
        estadoObservacao?: boolean | null;
        feedbackObservacao?: string | null;
    };

    const defaultProps: PropsCadastroObservacaoModal = {
        modelValue: true,
        loading: false,
        titulo: "Meu Título",
        okTitle: "Confirmar",
        texto: "Texto de ajuda",
        observacao: "",
        testIdConfirmar: "btn-confirmar",
        inputId: "input-obs",
        inputDataTestid: "inp-obs",
        label: "Observação",
    };

    const editorStub = {
        props: ["modelValue"],
        emits: ["update:modelValue", "update:invalido"],
        template: `
            <div>
                <textarea :data-testid="$attrs['data-testid']" :value="modelValue" @input="$emit('update:modelValue', $event.target.value)"></textarea>
                <button data-testid="editor-invalido" type="button" @click="$emit('update:invalido', true)">inválido</button>
                <button data-testid="editor-valido" type="button" @click="$emit('update:invalido', false)">válido</button>
            </div>
        `,
    };

    function montar(props: PropsCadastroObservacaoModal = defaultProps) {
        return mount(CadastroObservacaoModal, {
            props,
            global: {
                plugins: [createTestingPinia({createSpy: vi.fn})],
                stubs: {
                    EditorTextoRico: editorStub,
                },
            },
        });
    }

    it("deve renderizar corretamente com props básicas", () => {
        const wrapper = montar();
        const modal = wrapper.findComponent({name: "ModalConfirmacao"});
        expect(modal.props("titulo")).toBe("Meu Título");
        expect(wrapper.text()).toContain("Texto de ajuda");
        expect(wrapper.find("#input-obs").exists()).toBe(true);
    });

    it("deve exibir erro quando a prop 'erro' for passada", () => {
        const wrapper = montar({...defaultProps, erro: "Ocorreu um erro"});
        expect(wrapper.text()).toContain("Ocorreu um erro");
    });

    it("deve emitir 'update:observacao' quando o textarea muda", async () => {
        const wrapper = montar();
        await wrapper.find('textarea[data-testid="inp-obs"]').setValue("Nova observação");
        expect(wrapper.emitted("update:observacao")).toBeDefined();
        expect(wrapper.emitted("update:observacao")![0]).toEqual(["Nova observação"]);
    });

    it("deve exibir feedback de validação se houver", () => {
        const wrapper = montar({
            ...defaultProps,
            estadoObservacao: false,
            feedbackObservacao: "Campo obrigatório",
        });
        expect(wrapper.text()).toContain("Campo obrigatório");
    });

    it("deve exibir asterisco se labelObrigatoria for true", () => {
        const wrapper = montar({...defaultProps, labelObrigatoria: true});
        expect(wrapper.find(".text-danger").text()).toBe("*");
    });

    it("deve desabilitar a confirmação quando a observação estiver inválida", async () => {
        const wrapper = montar();

        await wrapper.find('[data-testid="editor-invalido"]').trigger("click");

        const modal = wrapper.findComponent({name: "ModalConfirmacao"});
        expect(modal.props("okDisabled")).toBe(true);
    });

    it("não deve emitir confirmar enquanto a observação estiver inválida", async () => {
        const wrapper = montar();
        const modal = wrapper.findComponent({name: "ModalConfirmacao"});

        await wrapper.find('[data-testid="editor-invalido"]').trigger("click");
        await modal.vm.$emit("confirmar");

        expect(wrapper.emitted("confirmar")).toBeUndefined();

        await wrapper.find('[data-testid="editor-valido"]').trigger("click");
        await modal.vm.$emit("confirmar");

        expect(wrapper.emitted("confirmar")).toHaveLength(1);
    });
});
