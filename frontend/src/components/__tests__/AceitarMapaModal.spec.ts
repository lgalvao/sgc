import {getCommonMountOptions, setupComponentTest} from "@/test-utils/componentTestHelpers";
import {mount} from "@vue/test-utils";
import {describe, expect, it} from "vitest";
import AceitarMapaModal from "@/components/mapa/AceitarMapaModal.vue";

const ModalConfirmacaoStub = {
    template: `
        <div v-if="modelValue" :data-ok-title="okTitle" :data-titulo="titulo" data-testid="modal-stub">
            <slot />
            <button :data-testid="testCodigoCancelar" :disabled="loading" @click="$emit('update:modelValue', false)">Cancelar</button>
            <button :data-testid="testCodigoConfirmar" :disabled="loading" @click="$emit('confirmar')">{{ okTitle }}</button>
        </div>
    `,
    props: ["modelValue", "titulo", "okTitle", "testCodigoCancelar", "testCodigoConfirmar", "loading"],
    emits: ["update:modelValue", "confirmar"],
};

describe("AceitarMapaModal.vue", () => {
    const context = setupComponentTest();

    const createWrapper = (propsOverride = {}) => {
        const options = getCommonMountOptions({}, {ModalConfirmacao: ModalConfirmacaoStub});

        context.wrapper = mount(AceitarMapaModal, {
            ...options,
            props: {
                mostrarModal: true,
                ...propsOverride,
            },
            global: {
                ...options.global,
                components: {
                    ModalConfirmacao: ModalConfirmacaoStub,
                    ...options.global.components
                }
            },
        });
        return context.wrapper;
    };

    it("não deve renderizar o modal quando mostrarModal for falso", () => {
        const wrapper = createWrapper({mostrarModal: false});
        expect(wrapper.find('[data-testid="modal-stub"]').exists()).toBe(false);
    });

    it("deve renderizar o modal com o perfil padrão (não ADMIN)", () => {
        const wrapper = createWrapper({perfil: "CHEFE"});

        const corpoModal = wrapper.find('[data-testid="body-aceite-mapa"]');
        expect(corpoModal.exists()).toBe(true);
        expect(wrapper.find('[data-testid="modal-stub"]').attributes("data-titulo")).toBe("Aceitar mapa");
        expect(wrapper.find('[data-testid="modal-stub"]').attributes("data-ok-title")).toBe("Aceitar");
        expect(corpoModal.text()).toContain("Confirma o aceite da validação do mapa de competências?");
    });

    it("deve renderizar o modal com o perfil ADMIN", () => {
        const wrapper = createWrapper({perfil: "ADMIN"});

        const corpoModal = wrapper.find('[data-testid="body-aceite-mapa"]');
        expect(corpoModal.exists()).toBe(true);
        expect(wrapper.find('[data-testid="modal-stub"]').attributes("data-titulo")).toBe("Homologar mapa");
        expect(wrapper.find('[data-testid="modal-stub"]').attributes("data-ok-title")).toBe("Homologar");
        expect(corpoModal.text()).toContain("Confirma a homologação do mapa de competências?");
    });

    it("deve emitir o evento fecharModal ao clicar no botão de cancelar", async () => {
        const wrapper = createWrapper();

        await wrapper
            .find('[data-testid="btn-aceite-mapa-cancelar"]')
            .trigger("click");
        expect(wrapper.emitted("fecharModal")).toBeTruthy();
    });

    it("deve renderizar o campo opcional de observação", () => {
        const wrapper = createWrapper();

        expect(wrapper.find('[data-testid="inp-aceite-mapa-observacao"]').exists()).toBe(true);
    });

    it("deve emitir o evento confirmarAceitacao com a observação", async () => {
        const wrapper = createWrapper();

        await wrapper
            .find('[data-testid="inp-aceite-mapa-observacao"]')
            .setValue("Observação teste");

        await wrapper
            .find('[data-testid="btn-aceite-mapa-confirmar"]')
            .trigger("click");

        expect(wrapper.emitted("confirmarAceitacao")).toBeTruthy();
        expect(wrapper.emitted("confirmarAceitacao")?.[0]).toEqual(["Observação teste"]);
    });

    it("deve emitir o evento confirmarAceitacao com uma observação vazia", async () => {
        const wrapper = createWrapper();

        await wrapper
            .find('[data-testid="btn-aceite-mapa-confirmar"]')
            .trigger("click");

        expect(wrapper.emitted("confirmarAceitacao")).toBeTruthy();
        expect(wrapper.emitted("confirmarAceitacao")?.[0]).toEqual([""]);
    });

    it("deve desabilitar botões quando loading for true", () => {
        const wrapper = createWrapper({loading: true});

        const btnCancelar = wrapper.find('[data-testid="btn-aceite-mapa-cancelar"]');
        const btnConfirmar = wrapper.find('[data-testid="btn-aceite-mapa-confirmar"]');

        expect(btnCancelar.attributes("disabled")).toBeDefined();
        expect(btnConfirmar.attributes("disabled")).toBeDefined();
    });
});
