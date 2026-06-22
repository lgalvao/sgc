import {describe, expect, it} from "vitest";
import {mount} from "@vue/test-utils";
import ModalObservacaoAcao from "../ModalObservacaoAcao.vue";

describe("ModalObservacaoAcao.vue", () => {
    function montar(props = {}) {
        return mount(ModalObservacaoAcao, {
            props: {
                modelValue: true,
                titulo: "Titulo",
                observacao: "",
                label: "Justificativa",
                testIdConfirmar: "btn-confirmar-teste",
                inputDataTestid: "textarea-teste",
                ...props,
            },
            global: {
                stubs: {
                    ModalPadrao: {
                        template: "<div><slot /><button :data-testid=\"testIdConfirmar\" @click=\"$emit('confirmar')\">Confirmar</button></div>",
                        props: ["modelValue", "titulo", "loading", "variantAcao", "textoAcao", "textoAcaoCarregando", "testIdConfirmar", "testIdCancelar"],
                        emits: ["confirmar", "fechar", "update:modelValue"],
                    },
                    AppAlert: {
                        props: ["mensagem"],
                        template: "<div class=\"app-alert\">{{ mensagem }}</div>",
                    },
                    BFormGroup: {
                        template: "<div><slot name=\"label\" /><slot /></div>",
                    },
                    BFormTextarea: {
                        props: ["modelValue"],
                        emits: ["update:modelValue"],
                        template: "<textarea :data-testid=\"$attrs['data-testid']\" :value=\"modelValue\" @input=\"$emit('update:modelValue', $event.target.value)\" />",
                    },
                    BFormText: {
                        template: "<small><slot /></small>",
                    },
                },
            },
        });
    }

    it("encaminha a observacao e emite atualizacao de model", async () => {
        const wrapper = montar();

        await wrapper.get("[data-testid='textarea-teste']").setValue("Nova justificativa");

        expect(wrapper.emitted("update:observacao")?.[0]).toEqual(["Nova justificativa"]);
    });

    it("renderiza erro e feedback de observacao quando informados", () => {
        const wrapper = montar({
            erro: "Falha ao salvar",
            feedbackObservacao: "A justificativa e obrigatoria",
        });

        expect(wrapper.text()).toContain("Falha ao salvar");
        expect(wrapper.text()).toContain("A justificativa e obrigatoria");
    });
});
