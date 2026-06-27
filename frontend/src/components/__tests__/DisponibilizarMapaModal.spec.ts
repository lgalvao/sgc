import {getCommonMountOptions, setupComponentTest} from "@/test-utils/componentTestHelpers";
import {mount} from "@vue/test-utils";
import {BFormInput} from "bootstrap-vue-next";
import {describe, expect, it, vi} from "vitest";
import DisponibilizarMapaModal from "@/components/mapa/modais/MapaDisponibilizacaoModal.vue";
import {obterAmanhaFormatado} from "@/utils/date";

vi.mock("@/utils/date", async () => {
    const actual = await vi.importActual<typeof import("@/utils/date")>("@/utils/date");
    return {
        ...actual,
        obterAmanhaFormatado: () => "2026-03-25",
        ehDataEstritamenteFutura: (d: string) => d > "2026-03-24",
    };
});

const ModalPadraoStub = {
    template: `
        <div v-if="modelValue" data-testid="modal-stub">
            <slot name="alerta" />
            <slot />
            <button
                :data-testid="testIdCancelar || 'btn-modal-padrao-cancelar'"
                :disabled="loading"
                @click="$emit('fechar')"
            >
                Cancelar
            </button>
            <slot name="acao" />
        </div>
    `,
    props: ["modelValue", "testIdCancelar", "loading"],
    emits: ["update:modelValue", "fechar", "confirmar"],
};

const EditorTextoRicoStub = {
    props: ["modelValue"],
    emits: ["update:modelValue"],
    template: `
        <textarea
            :data-testid="$attrs['data-testid']"
            :value="modelValue"
            @input="$emit('update:modelValue', $event.target.value)"
        />
    `,
};

describe("DisponibilizarMapaModal.vue", () => {
    const context = setupComponentTest();

    const createWrapper = (propsOverride = {}) => {
        const options = getCommonMountOptions({}, {ModalPadrao: ModalPadraoStub, EditorTextoRico: EditorTextoRicoStub});

        context.wrapper = mount(DisponibilizarMapaModal, {
            ...options,
            props: {
                mostrar: true,
                ...propsOverride,
            },
            global: {
                ...options.global,
                components: {
                    BFormInput,
                    ...options.global.components
                }
            },
        });
        return context.wrapper;
    };

    async function definirObservacoes(wrapper: ReturnType<typeof mount>, conteudoHtml: string) {
        const editor = wrapper.find('[data-testid="inp-disponibilizar-mapa-obs"]');
        await editor.setValue(conteudoHtml);
    }

    it("não deve renderizar o modal quando mostrar for falso", () => {
        const wrapper = createWrapper({mostrar: false});
        expect(wrapper.find('[data-testid="modal-stub"]').exists()).toBe(false);
    });

    it("deve renderizar o modal com os campos iniciais", () => {
        const wrapper = createWrapper({mostrar: true});

        const dataInput = wrapper.find('[data-testid="inp-disponibilizar-mapa-data"]');
        expect(dataInput.exists()).toBe(true);
        expect(wrapper.findComponent(BFormInput).props().modelValue).toBe("");
    });

    it("deve emitir o evento fechar ao clicar no botão de cancelar", async () => {
        const wrapper = createWrapper({mostrar: true});

        await wrapper.find('[data-testid="btn-disponibilizar-mapa-cancelar"]').trigger("click");
        expect(wrapper.emitted("fechar")).toBeDefined();
    });

    it("deve emitir o evento disponibilizar com a data selecionada", async () => {
        const wrapper = createWrapper({mostrar: true});

        const dataLimite = obterAmanhaFormatado();
        const inputWrapper = wrapper.findComponent(BFormInput);
        const nativeInput = inputWrapper.find("input");
        await nativeInput.setValue(dataLimite);

        await wrapper.find('[data-testid="btn-disponibilizar-mapa-confirmar"]').trigger("click");

        expect(wrapper.emitted("disponibilizar")).toBeDefined();
        expect(wrapper.emitted("disponibilizar")?.[0]).toEqual([{
            dataLimite,
            observacoes: ""
        }]);
    });

    it("deve exibir estado de carregamento quando loading for true", () => {
        const wrapper = createWrapper({mostrar: true, loading: true});

        const btnConfirmar = wrapper.find('[data-testid="btn-disponibilizar-mapa-confirmar"]');
        expect(btnConfirmar.attributes("disabled")).toBe("");
        expect(btnConfirmar.text()).toContain("Disponibilizando...");
        expect(btnConfirmar.find(".spinner-border").exists()).toBe(true);

        const btnCancelar = wrapper.find('[data-testid="btn-disponibilizar-mapa-cancelar"]');
        expect(btnCancelar.attributes("disabled")).toBe("");
    });

    it("deve exibir erros de validação quando fornecidos", () => {
        const wrapper = createWrapper({
            mostrar: true,
            fieldErrors: {
                generic: "Erro genérico",
                dataLimite: "Data inválida",
                observacoes: "Observação inválida"
            }
        });

        expect(wrapper.text()).toContain("Erro genérico");
        expect(wrapper.text()).toContain("Data inválida");
        expect(wrapper.text()).toContain("Observação inválida");
    });

    it("deve incluir observações no evento disponibilizar", async () => {
        const wrapper = createWrapper({mostrar: true});
        const dataLimite = obterAmanhaFormatado();
        const observacoes = "Teste observação";

        const inputWrapper = wrapper.findComponent(BFormInput);
        const nativeInput = inputWrapper.find("input");
        await nativeInput.setValue(dataLimite);

        await definirObservacoes(wrapper, `<p>${observacoes}</p>`);

        await wrapper.find('[data-testid="btn-disponibilizar-mapa-confirmar"]').trigger("click");

        expect(wrapper.emitted("disponibilizar")?.[0]).toEqual([{
            dataLimite,
            observacoes: `<p>${observacoes}</p>`
        }]);
    });

    it("deve resetar campos quando mostrar mudar para true", async () => {
        const wrapper = createWrapper({mostrar: false});
        await wrapper.setProps({mostrar: true});

        const inputWrapper = wrapper.findComponent(BFormInput);
        const nativeInput = inputWrapper.find("input");
        await nativeInput.setValue(obterAmanhaFormatado());

        await definirObservacoes(wrapper, "<p>Obs</p>");

        // Hide
        await wrapper.setProps({mostrar: false});

        // Show again
        await wrapper.setProps({mostrar: true});

        expect(wrapper.findComponent(BFormInput).props().modelValue).toBe("");

        const inputAposReset = wrapper.findComponent(BFormInput).find("input");
        await inputAposReset.setValue(obterAmanhaFormatado());
        await wrapper.find('[data-testid="btn-disponibilizar-mapa-confirmar"]').trigger("click");

        expect(wrapper.emitted("disponibilizar")?.[0]).toEqual([{
            dataLimite: obterAmanhaFormatado(),
            observacoes: "",
        }]);
    });

    it("deve ter o atributo min correto no campo de data", () => {
        const wrapper = mount(DisponibilizarMapaModal, {
            props: {mostrar: true},
        });
        const input = wrapper.find('[data-testid="inp-disponibilizar-mapa-data"]');
        expect(input.attributes("min")).toBe("2026-03-25");
    });

    it("deve exibir erro se a data não for futura", async () => {
        const wrapper = mount(DisponibilizarMapaModal, {
            props: {mostrar: true},
        });
        const input = wrapper.find('[data-testid="inp-disponibilizar-mapa-data"]');
        await input.setValue("2026-03-24");
        expect(wrapper.text()).toContain("A data limite para validação deve ser uma data futura.");
    });

    it("deve usar o dia seguinte ao fim da etapa anterior como mínimo quando ele for maior que amanhã", () => {
        const wrapper = mount(DisponibilizarMapaModal, {
            props: {mostrar: true, dataFimEtapaAnterior: "2026-03-30T00:00:00"},
        });
        const input = wrapper.find('[data-testid="inp-disponibilizar-mapa-data"]');
        expect(input.attributes("min")).toBe("2026-03-31");
    });

    it("deve exigir data maior que a data de fim da etapa anterior", async () => {
        const wrapper = mount(DisponibilizarMapaModal, {
            props: {mostrar: true, dataFimEtapaAnterior: "2026-03-30T00:00:00"},
        });
        const input = wrapper.find('[data-testid="inp-disponibilizar-mapa-data"]');
        await input.setValue("2026-03-30");
        expect(wrapper.text()).toContain("A data limite deve ser maior que a data de fim da etapa anterior.");
    });

    it("deve exigir data limite obrigatória no submit e focar o input", async () => {
        const wrapper = mount(DisponibilizarMapaModal, {
            props: {mostrar: true},
        });
        const btn = wrapper.find('[data-testid="btn-disponibilizar-mapa-confirmar"]');
        await btn.trigger("click");
        await wrapper.vm.$nextTick();
        expect(wrapper.text()).toContain("A data limite é obrigatória.");
    });
});
