import {describe, expect, it} from "vitest";
import {mount} from "@vue/test-utils";
import CadAtividadeForm from "../CadAtividadeForm.vue";

describe("CadAtividadeForm.vue", () => {
    function criarWrapper(props: Record<string, unknown> = {}, modelValue = "") {
        return mount(CadAtividadeForm, {
            props: {
                modelValue,
                ...props,
            },
            global: {
                stubs: {
                    LoadingButton: {
                        template: '<button :disabled="disabled" @click="$emit(\'click\')"><slot /></button>',
                        props: ["disabled", "loading", "type", "variant", "icon", "size"],
                    }
                }
            }
        });
    }

    it("mostra validação inline e não submete com valor vazio", async () => {
        const wrapper = criarWrapper({}, "   ");
        await wrapper.find("form").trigger("submit.prevent");
        expect(wrapper.text()).toContain("Informe a atividade.");
        expect(wrapper.emitted("submit")).toBeFalsy();
    });

    it("mantém o botão habilitado com campo vazio e valida no submit", async () => {
        const wrapper = criarWrapper({}, "");
        const botao = wrapper.find('[aria-label="Adicionar atividade"]');

        expect(botao.attributes("disabled")).toBeUndefined();

        await wrapper.find("form").trigger("submit.prevent");

        expect(wrapper.text()).toContain("Informe a atividade.");
        expect(wrapper.emitted("submit")).toBeFalsy();
    });

    it("exibe erro recebido via prop", () => {
        const wrapper = criarWrapper({erro: "Atividade já cadastrada."}, "Atividade X");
        expect(wrapper.text()).toContain("Atividade já cadastrada.");
    });

    it("emite submit quando texto é válido", async () => {
        const wrapper = criarWrapper({}, "Nova atividade");
        await wrapper.find("form").trigger("submit.prevent");
        expect(wrapper.emitted("submit")).toBeTruthy();
    });

    it("remove o erro local após correção depois da primeira tentativa", async () => {
        const wrapper = criarWrapper({}, "");

        await wrapper.find("form").trigger("submit.prevent");
        expect(wrapper.text()).toContain("Informe a atividade.");

        await wrapper.setProps({modelValue: "Nova atividade"});
        expect(wrapper.text()).not.toContain("Informe a atividade.");
    });
});
