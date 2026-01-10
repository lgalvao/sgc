import {beforeEach, describe, expect, it, vi} from "vitest";
import {mount} from "@vue/test-utils";
import ModalAcaoBloco from "../ModalAcaoBloco.vue";

// Mock Bootstrap Modal with hoisted variables
const { mockShow, mockHide } = vi.hoisted(() => ({
    mockShow: vi.fn(),
    mockHide: vi.fn(),
}));

vi.mock("bootstrap", () => {
    return {
        Modal: class {
            show = mockShow;
            hide = mockHide;
            dispose = vi.fn();
        }
    };
});

describe("ModalAcaoBloco.vue", () => {
    const mockUnidades = [
        { codigo: 1, sigla: "U1", nome: "Unidade 1", situacao: "Pendente" },
        { codigo: 2, sigla: "U2", nome: "Unidade 2", situacao: "Pendente" },
    ];

    const defaultProps = {
        id: "modal-test",
        titulo: "Titulo Teste",
        texto: "Texto Teste",
        rotuloBotao: "Confirmar",
        unidades: mockUnidades,
        unidadesPreSelecionadas: [],
    };

    const createWrapper = (props = {}) => {
        return mount(ModalAcaoBloco, {
            props: { ...defaultProps, ...props },
        });
    };

    beforeEach(() => {
        vi.clearAllMocks();
    });

    it("deve inicializar e abrir o modal", async () => {
        const wrapper = createWrapper();
        // Wait for onMounted
        await wrapper.vm.$nextTick();

        (wrapper.vm as any).abrir();
        expect(mockShow).toHaveBeenCalled();
    });

    it("deve selecionar/deselecionar todas as unidades", async () => {
        const wrapper = createWrapper();
        const checkboxTodos = wrapper.find('thead input[type="checkbox"]');

        await checkboxTodos.setValue(true);
        // Checking DOM:
        const checkboxes = wrapper.findAll('tbody input[type="checkbox"]');
        expect((checkboxes[0].element as HTMLInputElement).checked).toBe(true);
        expect((checkboxes[1].element as HTMLInputElement).checked).toBe(true);

        await checkboxTodos.setValue(false);
        expect((checkboxes[0].element as HTMLInputElement).checked).toBe(false);
    });

    it("deve inicializar com unidades pré-selecionadas", () => {
        const wrapper = createWrapper({ unidadesPreSelecionadas: [1] });
        const checkboxes = wrapper.findAll('tbody input[type="checkbox"]');
        expect((checkboxes[0].element as HTMLInputElement).checked).toBe(true); // U1
        expect((checkboxes[1].element as HTMLInputElement).checked).toBe(false); // U2
    });

    it("deve emitir 'confirmar' com os IDs selecionados", async () => {
        const wrapper = createWrapper({ unidadesPreSelecionadas: [2] });

        await wrapper.find('.btn-primary').trigger('click');

        expect(wrapper.emitted('confirmar')).toBeTruthy();
        expect(wrapper.emitted('confirmar')![0][0]).toEqual({
            ids: [2],
            dataLimite: undefined
        });
    });

    it("deve validar data limite se obrigatória", async () => {
        const wrapper = createWrapper({
            unidadesPreSelecionadas: [1],
            mostrarDataLimite: true
        });

        // Try confirming without date
        await wrapper.find('.btn-primary').trigger('click');
        expect(wrapper.emitted('confirmar')).toBeFalsy();
        expect(wrapper.text()).toContain("A data limite é obrigatória");

        // Set date
        await wrapper.find('#dataLimiteBloco').setValue('2024-12-31');
        await wrapper.find('.btn-primary').trigger('click');

        expect(wrapper.emitted('confirmar')).toBeTruthy();
        expect(wrapper.emitted('confirmar')![0][0]).toEqual({
            ids: [1],
            dataLimite: '2024-12-31'
        });
    });

    it("deve fechar o modal e limpar estado", () => {
        const wrapper = createWrapper();
        // Simulate open state with error and processing
        (wrapper.vm as any).setProcessando(true);
        (wrapper.vm as any).setErro("Erro");

        (wrapper.vm as any).fechar();

        expect(mockHide).toHaveBeenCalled();
    });
});
