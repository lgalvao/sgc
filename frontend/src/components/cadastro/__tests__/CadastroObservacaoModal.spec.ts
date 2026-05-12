import {describe, expect, it, vi} from "vitest";
import {mount} from "@vue/test-utils";
import CadastroObservacaoModal from "../CadastroObservacaoModal.vue";
import {createTestingPinia} from "@pinia/testing";

describe("CadastroObservacaoModal.vue", () => {
    const defaultProps = {
        modelValue: true,
        loading: false,
        titulo: "Meu Título",
        okTitle: "Confirmar",
        texto: "Texto de ajuda",
        observacao: "",
        testIdConfirmar: "btn-confirmar",
        inputId: "input-obs",
        inputDataTestid: "inp-obs",
        label: "Observação"
    };

    async function definirObservacao(wrapper: ReturnType<typeof mount>, conteudoHtml: string) {
        const editor = wrapper.find('[data-testid="inp-obs"]');
        (editor.element as HTMLDivElement).innerHTML = conteudoHtml;
        await editor.trigger("input");
    }

    it("deve renderizar corretamente com props básicas", () => {
        const wrapper = mount(CadastroObservacaoModal, {
            props: defaultProps,
            global: {
                plugins: [createTestingPinia({createSpy: vi.fn})],
            }
        });
        // Como BModal é stubbed no vitest.setup.ts e não renderiza o título no template do stub,
        // verificamos se o componente ModalConfirmacao recebeu a prop correta.
        const modal = wrapper.findComponent({name: 'ModalConfirmacao'});
        expect(modal.props('titulo')).toBe("Meu Título");
        expect(wrapper.text()).toContain("Texto de ajuda");
        expect(wrapper.find("#input-obs").exists()).toBe(true);
    });

    it("deve exibir erro quando a prop 'erro' for passada", () => {
        const wrapper = mount(CadastroObservacaoModal, {
            props: {...defaultProps, erro: "Ocorreu um erro"},
            global: {
                plugins: [createTestingPinia({createSpy: vi.fn})],
            }
        });
        expect(wrapper.text()).toContain("Ocorreu um erro");
    });

    it("deve emitir 'update:observacao' quando o textarea muda", async () => {
        const wrapper = mount(CadastroObservacaoModal, {
            props: defaultProps,
            global: {
                plugins: [createTestingPinia({createSpy: vi.fn})],
            }
        });
        await definirObservacao(wrapper, "<p>Nova observação</p>");
        expect(wrapper.emitted("update:observacao")).toBeDefined();
        expect(wrapper.emitted("update:observacao")![0]).toEqual(["<p>Nova observação</p>"]);
    });

    it("deve exibir feedback de validação se houver", () => {
        const wrapper = mount(CadastroObservacaoModal, {
            props: {
                ...defaultProps,
                estadoObservacao: false,
                feedbackObservacao: "Campo obrigatório"
            },
            global: {
                plugins: [createTestingPinia({createSpy: vi.fn})],
            }
        });
        expect(wrapper.text()).toContain("Campo obrigatório");
    });

    it("deve exibir asterisco se labelObrigatoria for true", () => {
        const wrapper = mount(CadastroObservacaoModal, {
            props: {...defaultProps, labelObrigatoria: true},
            global: {
                plugins: [createTestingPinia({createSpy: vi.fn})],
            }
        });
        expect(wrapper.find(".text-danger").text()).toBe("*");
    });
});

