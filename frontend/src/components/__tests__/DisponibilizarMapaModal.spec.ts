import {getCommonMountOptions, setupComponentTest} from "@/test-utils/componentTestHelpers";
import {mount} from "@vue/test-utils";
import {BFormInput, BModal} from "bootstrap-vue-next";
import {describe, expect, it} from "vitest";
import DisponibilizarMapaModal from "@/components/DisponibilizarMapaModal.vue";

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

describe("DisponibilizarMapaModal.vue", () => {
    const context = setupComponentTest();

    const createWrapper = (propsOverride = {}) => {
        const options = getCommonMountOptions({}, { BModal: BModalStub });

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
                    BModal,
                    ...(options.global.components || {})
                }
            },
        });
        return context.wrapper;
    };

    it("não deve renderizar o modal quando mostrar for falso", () => {
        const wrapper = createWrapper({ mostrar: false });
        expect(wrapper.find('[data-testid="modal-stub"]').exists()).toBe(false);
    });

    it("deve renderizar o modal com os campos iniciais", () => {
        const wrapper = createWrapper({ mostrar: true });

        const dataInput = wrapper.find('[data-testid="inp-disponibilizar-mapa-data"]');
        expect(dataInput.exists()).toBe(true);
        expect(wrapper.findComponent(BFormInput).props().modelValue).toBe("");

        const disponibilizarButton = wrapper.find(
            '[data-testid="btn-disponibilizar-mapa-confirmar"]'
        );
        expect(disponibilizarButton.attributes("disabled")).toBeDefined();
    });

    it("deve habilitar o botão de disponibilizar quando a data for selecionada", async () => {
        const wrapper = createWrapper({ mostrar: true });

        const inputWrapper = wrapper.findComponent(BFormInput);
        const nativeInput = inputWrapper.find("input");
        await nativeInput.setValue("2024-12-31");

        const disponibilizarButton = wrapper.find(
            '[data-testid="btn-disponibilizar-mapa-confirmar"]'
        );
        expect(disponibilizarButton.attributes("disabled")).toBeUndefined();
    });

    it("deve emitir o evento fechar ao clicar no botão de cancelar", async () => {
        const wrapper = createWrapper({ mostrar: true });

        await wrapper.find('[data-testid="btn-disponibilizar-mapa-cancelar"]').trigger("click");
        expect(wrapper.emitted("fechar")).toBeTruthy();
    });

    it("deve emitir o evento disponibilizar com a data selecionada", async () => {
        const wrapper = createWrapper({ mostrar: true });

        const dataLimite = "2024-12-31";
        const inputWrapper = wrapper.findComponent(BFormInput);
        const nativeInput = inputWrapper.find("input");
        await nativeInput.setValue(dataLimite);

        await wrapper.find('[data-testid="btn-disponibilizar-mapa-confirmar"]').trigger("click");

        expect(wrapper.emitted("disponibilizar")).toBeTruthy();
        expect(wrapper.emitted("disponibilizar")?.[0]).toEqual([{
            dataLimite,
            observacoes: ""
        }]);
    });
});