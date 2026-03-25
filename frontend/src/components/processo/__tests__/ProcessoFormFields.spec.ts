import {describe, expect, it} from "vitest";
import {mount} from "@vue/test-utils";
import {nextTick} from "vue";
import ProcessoFormFields from "../ProcessoFormFields.vue";
import {obterAmanhaFormatado} from "@/utils/dateUtils";

const modelValueBase = {
    descricao: "",
    tipo: null,
    unidadesSelecionadas: [],
    dataLimite: "",
};

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

describe("ProcessoFormFields.vue", () => {

    it("não deve focar automaticamente nos campos quando há erro no mount", async () => {
        const wrapper = criarWrapper({descricao: "Erro"});
        await nextTick();
        const inputDescricao = wrapper.find('[data-testid="inp-processo-descricao"]');
        expect(inputDescricao.element).not.toBe(document.activeElement);
    });

    it("foca no campo correto quando focarPrimeiroErro é chamado manualmente", async () => {
        const wrapper = criarWrapper({dataLimite: "Erro"});
        await nextTick();
        
        // Chama manualmente o método exposto
        (wrapper.vm as any).focarPrimeiroErro();
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

    it("campo de data deve ter atributos min e max corretos", async () => {
        const wrapper = criarWrapper();
        const input = wrapper.find('[data-testid="inp-processo-data-limite"]').element as HTMLInputElement;

        expect(input.getAttribute('min')).toBe(obterAmanhaFormatado());
        expect(input.getAttribute('max')).toBe('2099-12-31');
    });
});
