import {getCommonMountOptions, setupComponentTest} from "@/test-utils/componentTestHelpers";
import {flushPromises, mount} from "@vue/test-utils";
import {BButton, BCard, BFormCheckbox, BFormTextarea, BModal} from "bootstrap-vue-next";
import {describe, expect, it} from "vitest";
import CriarCompetenciaModal from "@/components/CriarCompetenciaModal.vue";

const BModalStub = {
    template: `
        <div v-if="modelValue" data-testid="modal-stub">
            <slot />
            <slot name="footer" />
        </div>
    `,
    props: ["modelValue"],
    emits: ["update:modelValue", "hide"],
};

describe("CriarCompetenciaModal.vue", () => {
    const context = setupComponentTest();

    const atividades = [
        {codigo: 1, descricao: "Atividade 1", conhecimentos: []},
        {
            codigo: 2,
            descricao: "Atividade 2",
            conhecimentos: [{codigo: 1, descricao: "Conhecimento 1"}],
        },
    ];

    const createWrapper = (propsOverride = {}) => {
        const options = getCommonMountOptions({}, { BModal: BModalStub });

        context.wrapper = mount(CriarCompetenciaModal, {
            ...options,
            props: {
                mostrar: true,
                atividades: [],
                ...propsOverride,
            },
            global: {
                ...options.global,
                components: {
                    BFormTextarea,
                    BButton,
                    BCard,
                    BFormCheckbox,
                    BModal,
                    ...(options.global.components || {})
                }
            },
        });
        return context.wrapper;
    };

    it("não deve renderizar o modal quando mostrar for falso", () => {
        const wrapper = createWrapper({ mostrar: false, atividades: [] });
        expect(wrapper.find('[data-testid="inp-criar-competencia-descricao"]').exists()).toBe(false);
    });

    it("deve renderizar o modal no modo de criação", () => {
        const wrapper = createWrapper({ mostrar: true, atividades });

        expect(wrapper.findComponent(BFormTextarea).props().modelValue).toBe("");
        expect(
            wrapper
                .find('[data-testid="btn-criar-competencia-salvar"]')
                .attributes("disabled"),
        ).toBeDefined();
    });

    it("deve renderizar o modal no modo de edição", async () => {
        const competenciaParaEditar = {
            codigo: 1,
            descricao: "Competência existente",
            atividadesAssociadas: [1],
        };

        const wrapper = createWrapper({ mostrar: true, atividades, competenciaParaEditar });

        await wrapper.vm.$nextTick();

        expect(wrapper.findComponent(BFormTextarea).props().modelValue).toBe("Competência existente");
    });

    it("deve habilitar o botão de salvar quando a descrição e pelo menos uma atividade forem selecionadas", async () => {
        const wrapper = createWrapper({ mostrar: true, atividades });

        await wrapper.findComponent(BFormTextarea).setValue("Nova competência");
        await wrapper.find('input[type="checkbox"]').trigger("click");

        expect(
            wrapper
                .find('[data-testid="btn-criar-competencia-salvar"]')
                .attributes("disabled"),
        ).toBeFalsy();
    });

    it("deve emitir o evento fechar ao clicar no botão de cancelar", async () => {
        const wrapper = createWrapper({ mostrar: true, atividades });

        await wrapper.find('[data-testid="btn-criar-competencia-cancelar"]').trigger("click");
        expect(wrapper.emitted("fechar")).toBeTruthy();
    });

    it("deve emitir o evento salvar com os dados corretos", async () => {
        const wrapper = createWrapper({ mostrar: true, atividades });

        const descricao = "Competência de teste";
        await wrapper.findComponent(BFormTextarea).setValue(descricao);
        // Using setValue(true) to simulate checking the checkbox
        await wrapper.find('input[type="checkbox"]').setValue(true);
        await flushPromises();
        await wrapper.find('[data-testid="btn-criar-competencia-salvar"]').trigger("click");

        expect(wrapper.emitted("salvar")).toBeTruthy();
        expect(wrapper.emitted("salvar")?.[0]).toEqual([
            {
                descricao,
                atividadesSelecionadas: [atividades[0].codigo],
            },
        ]);
    });
});
