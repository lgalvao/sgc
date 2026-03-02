import {describe, expect, it, vi} from "vitest";
import {mount} from "@vue/test-utils";
import {nextTick} from "vue";
import ProcessoFormFields from "../ProcessoFormFields.vue";

const modelValueBase = {
    descricao: "",
    tipo: null,
    unidadesSelecionadas: [],
    dataLimite: "",
};

describe("ProcessoFormFields.vue", () => {
    function criarWrapper(fieldErrors: Record<string, string> = {}) {
        return mount(ProcessoFormFields, {
            props: {
                modelValue: modelValueBase,
                fieldErrors,
                unidades: [],
                isLoadingUnidades: false,
            },
            attachTo: document.body,
            global: {
                stubs: {
                    ArvoreUnidades: {template: "<div>Arvore</div>"}
                }
            }
        });
    }

    it("foca no campo descrição quando há erro de descrição", async () => {
        const wrapper = criarWrapper({descricao: "Descrição obrigatória"});
        await nextTick();
        const inputDescricao = wrapper.find('[data-testid="inp-processo-descricao"]');
        expect(inputDescricao.element).toBe(document.activeElement);
    });

    it("foca no container de unidades quando só há erro de unidades", async () => {
        const wrapper = criarWrapper({unidades: "Selecione ao menos uma unidade"});
        await nextTick();
        const container = wrapper.find('[data-testid="container-processo-unidades"]');
        expect(container.element).toBe(document.activeElement);
    });

    it("foca no campo data limite quando há erro de data limite", async () => {
        const wrapper = criarWrapper({dataLimite: "Data limite obrigatória"});
        await nextTick();
        const inputDataLimite = wrapper.find('[data-testid="inp-processo-data-limite"]');
        expect(inputDataLimite.element).toBe(document.activeElement);
    });

    it("renderiza o ícone de calendário", () => {
        const wrapper = criarWrapper();
        const icone = wrapper.find('.bi-calendar-event');
        expect(icone.exists()).toBe(true);
        expect(icone.element.parentElement?.classList.contains('input-group-text')).toBe(true);
    });

    it("tenta abrir o seletor de data ao clicar no ícone", async () => {
        const wrapper = criarWrapper();
        const input = wrapper.find('[data-testid="inp-processo-data-limite"]').element as HTMLInputElement;

        // Mock showPicker
        const showPickerSpy = vi.fn();
        input.showPicker = showPickerSpy;

        const iconeContainer = wrapper.find('.cursor-pointer');
        await iconeContainer.trigger('click');

        expect(showPickerSpy).toHaveBeenCalled();
    });

    it("campo de data deve ter atributos min e max para validação de ano", async () => {
        const wrapper = criarWrapper();
        const input = wrapper.find('[data-testid="inp-processo-data-limite"]').element as HTMLInputElement;

        expect(input.getAttribute('min')).toBe('2000-01-01');
        expect(input.getAttribute('max')).toBe('2099-12-31');
    });
});
