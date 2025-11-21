import {mount} from "@vue/test-utils";
import {BFormInput} from "bootstrap-vue-next";
import {describe, expect, it} from "vitest";
import DisponibilizarMapaModal from "../DisponibilizarMapaModal.vue";

describe("DisponibilizarMapaModal", () => {
  const globalComponents = {
    global: {
      components: {
        BFormInput,
      },
    },
  };

    it("não deve renderizar o modal quando mostrar for falso", () => {
    const wrapper = mount(DisponibilizarMapaModal, {
      props: {
        mostrar: false,
      },
      ...globalComponents,
    });
        expect(wrapper.find('[data-testid="input-data-limite"]').exists()).toBe(
            false,
        );
  });

    it("deve renderizar o modal com os campos iniciais", () => {
    const wrapper = mount(DisponibilizarMapaModal, {
      props: {
        mostrar: true,
      },
      ...globalComponents,
    });

    const dataInput = wrapper.find('[data-testid="input-data-limite"]');
    expect(dataInput.exists()).toBe(true);
        expect(wrapper.findComponent(BFormInput).props().modelValue).toBe("");

        const disponibilizarButton = wrapper.find(
            '[data-testid="btn-disponibilizar"]',
        );
        expect(disponibilizarButton.attributes("disabled")).toBeDefined();
  });

    it("deve habilitar o botão de disponibilizar quando a data for selecionada", async () => {
    const wrapper = mount(DisponibilizarMapaModal, {
      props: {
        mostrar: true,
      },
      ...globalComponents,
    });

    const inputWrapper = wrapper.findComponent(BFormInput);
        const nativeInput = inputWrapper.find("input");
        await nativeInput.setValue("2024-12-31");
        const disponibilizarButton = wrapper.find(
            '[data-testid="btn-disponibilizar"]',
        );
        expect(disponibilizarButton.attributes("disabled")).toBeUndefined();
  });

    it("deve emitir o evento fechar ao clicar no botão de cancelar", async () => {
    const wrapper = mount(DisponibilizarMapaModal, {
      props: {
        mostrar: true,
      },
      ...globalComponents,
    });

        await wrapper.find('[data-testid="btn-modal-cancelar"]').trigger("click");
        expect(wrapper.emitted("fechar")).toBeTruthy();
  });

    it("deve emitir o evento disponibilizar com a data selecionada", async () => {
    const wrapper = mount(DisponibilizarMapaModal, {
      props: {
        mostrar: true,
      },
      ...globalComponents,
    });

        const dataLimite = "2024-12-31";
    const inputWrapper = wrapper.findComponent(BFormInput);
        const nativeInput = inputWrapper.find("input");
    await nativeInput.setValue(dataLimite);
        await wrapper.find('[data-testid="btn-disponibilizar"]').trigger("click");

        expect(wrapper.emitted("disponibilizar")).toBeTruthy();
        expect(wrapper.emitted("disponibilizar")?.[0]).toEqual([dataLimite]);
  });
});
