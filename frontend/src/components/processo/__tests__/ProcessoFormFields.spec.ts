import {describe, expect, it} from "vitest";
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
});
