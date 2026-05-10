import {getCommonMountOptions, setupComponentTest} from "@/test-utils/componentTestHelpers";
import {flushPromises, mount} from "@vue/test-utils";
import {BCard, BFormCheckbox, BFormTextarea} from "bootstrap-vue-next";
import {describe, expect, it} from "vitest";
import CriarCompetenciaModal from "@/components/mapa/modais/CompetenciaEdicaoModal.vue";

const ModalPadraoStub = {
    template: `
        <div v-if="modelValue" data-testid="modal-stub">
            <slot />
            <button :data-testid="testIdCancelar" @click="$emit('fechar')">Cancelar</button>
            <button :data-testid="testIdConfirmar" :disabled="loading" @click="$emit('confirmar')">{{ textoAcao }}</button>
        </div>
    `,
    props: ["modelValue", "testIdCancelar", "testIdConfirmar", "textoAcao", "loading"],
    emits: ["update:modelValue", "fechar", "confirmar", "shown"],
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
        const options = getCommonMountOptions({}, {
            ModalPadrao: ModalPadraoStub,
            BTooltip: {
                props: ['placement'],
                template: '<span><slot /></span>',
            },
        });

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
                    ModalPadrao: ModalPadraoStub,
                    BFormTextarea,
                    BCard,
                    BFormCheckbox,
                    ...options.global.components
                }
            },
        });
        return context.wrapper;
    };

    it("não deve renderizar o modal quando mostrar for falso", () => {
        const wrapper = createWrapper({mostrar: false, atividades: []});
        expect(wrapper.find('[data-testid="inp-criar-competencia-descricao"]').exists()).toBe(false);
    });

    it("deve renderizar o modal no modo de criação", () => {
        const wrapper = createWrapper({mostrar: true, atividades});

        expect(wrapper.findComponent(BFormTextarea).props().modelValue).toBe("");
        expect(wrapper.find('[data-testid="btn-criar-competencia-salvar"]').text()).toBe("Criar");
    });

    it("deve renderizar o modal no modo de edição", async () => {
        const competenciaParaEditar = {
            codigo: 1,
            descricao: "Competência existente",
            atividades: [{codigo: 1, descricao: "Atividade 1"}],
        };

        const wrapper = createWrapper({mostrar: true, atividades, competenciaParaEditar});

        await wrapper.vm.$nextTick();

        expect(wrapper.findComponent(BFormTextarea).props().modelValue).toBe("Competência existente");
        expect(wrapper.find('[data-testid="btn-criar-competencia-salvar"]').text()).toBe("Salvar");
    });

    it("deve emitir o evento salvar quando a descrição e pelo menos uma atividade forem preenchidas", async () => {
        const wrapper = createWrapper({mostrar: true, atividades});

        await wrapper.findComponent(BFormTextarea).setValue("Nova competência");
        await wrapper.find('input[type="checkbox"]').setValue(true);
        await flushPromises();
        await wrapper.find('[data-testid="btn-criar-competencia-salvar"]').trigger("click");

        expect(wrapper.emitted("salvar")).toBeDefined();
    });

    it("deve permitir salvar edicao sem atividades associadas", async () => {
        const competenciaParaEditar = {
            codigo: 1,
            descricao: "Competência existente",
            atividades: [{codigo: 1, descricao: "Atividade 1"}],
        };

        const wrapper = createWrapper({mostrar: true, atividades, competenciaParaEditar});

        await wrapper.vm.$nextTick();
        await wrapper.find('input[type="checkbox"]').setValue(false);
        await flushPromises();

        expect(
            wrapper
                .find('[data-testid="btn-criar-competencia-salvar"]')
                .attributes("disabled"),
        ).toBeUndefined();
    });

    it("deve exigir campos obrigatorios e exibir erros inline ao salvar", async () => {
        const wrapper = createWrapper({mostrar: true, atividades});

        await wrapper.find('[data-testid="btn-criar-competencia-salvar"]').trigger('click');
        await wrapper.vm.$nextTick();

        expect(wrapper.text()).toContain('A descrição é obrigatória.');
        expect(wrapper.text()).toContain('Selecione ao menos uma atividade.');
        expect(wrapper.emitted('salvar')).toBeUndefined();
    });

    it("deve emitir o evento fechar ao clicar no botão de cancelar", async () => {
        const wrapper = createWrapper({mostrar: true, atividades});

        await wrapper.find('[data-testid="btn-criar-competencia-cancelar"]').trigger("click");
        expect(wrapper.emitted("fechar")).toBeDefined();
    });

    it("deve emitir o evento salvar com os dados corretos", async () => {
        const wrapper = createWrapper({mostrar: true, atividades});

        const descricao = "Competência de teste";
        await wrapper.findComponent(BFormTextarea).setValue(descricao);
        // Using setValue(true) to simulate checking the checkbox
        await wrapper.find('input[type="checkbox"]').setValue(true);
        await flushPromises();
        await wrapper.find('[data-testid="btn-criar-competencia-salvar"]').trigger("click");

        expect(wrapper.emitted("salvar")).toBeDefined();
        expect(wrapper.emitted("salvar")?.[0]).toEqual([
            {
                descricao,
                atividadesSelecionadas: [atividades[0].codigo],
            },
        ]);
    });

    it("deve bloquear novo salvamento enquanto estiver carregando", async () => {
        const wrapper = createWrapper({mostrar: true, atividades, loading: true});

        await wrapper.findComponent(BFormTextarea).setValue("Competência de teste");
        await wrapper.find('input[type="checkbox"]').setValue(true);
        await flushPromises();
        await wrapper.find('[data-testid="btn-criar-competencia-salvar"]').trigger("click");

        expect(wrapper.emitted("salvar")).toBeUndefined();
    });

});

