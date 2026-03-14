import { beforeEach, describe, expect, it, vi } from "vitest";
import { mount } from "@vue/test-utils";
import ModalAcaoBloco from "../processo/ModalAcaoBloco.vue";
import { BButton } from "bootstrap-vue-next";

describe("ModalAcaoBloco.vue", () => {
    const mockUnidades = [
        { codigo: 1, sigla: "U1", nome: "Unidade 1", situacao: "Pendente" },
        { codigo: 2, sigla: "U2", nome: "Unidade 2", situacao: "Pendente" },
    ];

    const defaultProps = {
        id: "modal-test",
        titulo: "Titulo teste",
        texto: "Texto teste",
        rotuloBotao: "Confirmar",
        unidades: mockUnidades,
        unidadesPreSelecionadas: [],
    };

    const createWrapper = (props = {}) => {
        const wrapper = mount(ModalAcaoBloco, {
            props: { ...defaultProps, ...props },
            global: {
                stubs: {
                    BModal: {
                        template: '<div><slot /><slot name="footer" /></div>'
                    },
                    BTable: false,
                    BFormCheckbox: false,
                    BButton: false,
                    BSpinner: true,
                    BAlert: {
                        template: '<div><slot /></div>'
                    },
                    BFormGroup: {
                        template: '<div><slot /></div>'
                    },
                    InputData: true
                }
            }
        });
        return wrapper;
    };

    beforeEach(() => {
        vi.clearAllMocks();
    });

    it("deve inicializar e abrir o modal", async () => {
        const wrapper = createWrapper();
        await wrapper.vm.$nextTick();

        (wrapper.vm as any).abrir();
        expect((wrapper.vm as any).mostrar).toBe(true);
    });

    it("deve selecionar/deselecionar todas as unidades", async () => {
        const wrapper = createWrapper();
        (wrapper.vm as any).abrir();
        await wrapper.vm.$nextTick();

        const checkboxTodos = wrapper.find('thead input[type="checkbox"]');
        expect(checkboxTodos.exists()).toBe(true);

        await checkboxTodos.setValue(true);
        const checkboxes = wrapper.findAll('tbody input[type="checkbox"]');
        expect(checkboxes.length).toBe(2);
        expect((checkboxes[0].element as HTMLInputElement).checked).toBe(true);
        expect((checkboxes[1].element as HTMLInputElement).checked).toBe(true);

        await checkboxTodos.setValue(false);
        expect((checkboxes[0].element as HTMLInputElement).checked).toBe(false);
    });

    it("deve inicializar com unidades pré-selecionadas", async () => {
        const wrapper = createWrapper({ unidadesPreSelecionadas: [1] });
        (wrapper.vm as any).abrir();
        await wrapper.vm.$nextTick();

        const checkboxes = wrapper.findAll('tbody input[type="checkbox"]');
        expect(checkboxes.length).toBe(2);
        expect((checkboxes[0].element as HTMLInputElement).checked).toBe(true); // U1
        expect((checkboxes[1].element as HTMLInputElement).checked).toBe(false); // U2
    });

    it("deve emitir 'confirmar' com os IDs selecionados", async () => {
        const wrapper = createWrapper({ unidadesPreSelecionadas: [2] });
        (wrapper.vm as any).abrir();
        await wrapper.vm.$nextTick();

        const btnConfirmar = wrapper.findAllComponents(BButton).find(b => b.text().includes("Confirmar"));
        expect(btnConfirmar?.exists()).toBe(true);
        await btnConfirmar?.trigger('click');

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
        (wrapper.vm as any).abrir();
        await wrapper.vm.$nextTick();

        const btnConfirmar = wrapper.findAllComponents(BButton).find(b => b.text().includes("Confirmar"));
        await btnConfirmar?.trigger('click');
        
        expect(wrapper.emitted('confirmar')).toBeFalsy();
        expect(wrapper.text()).toContain("A data limite é obrigatória");

        (wrapper.vm as any).dataLimite = '2024-12-31';
        await wrapper.vm.$nextTick();
        
        await btnConfirmar?.trigger('click');

        expect(wrapper.emitted('confirmar')).toBeTruthy();
        expect(wrapper.emitted('confirmar')![0][0]).toEqual({
            ids: [1],
            dataLimite: '2024-12-31'
        });
    });

    it("deve fechar o modal e limpar estado", () => {
        const wrapper = createWrapper();
        (wrapper.vm as any).abrir();
        (wrapper.vm as any).setProcessando(true);
        (wrapper.vm as any).setErro("Erro");

        (wrapper.vm as any).fechar();

        expect((wrapper.vm as any).mostrar).toBe(false);
        expect((wrapper.vm as any).processando).toBe(false);
        expect((wrapper.vm as any).erro).toBe(null);
    });
});

