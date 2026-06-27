import {describe, expect, it} from "vitest";
import {mount} from "@vue/test-utils";
import AppAlertaFormulario from "../AppAlertaFormulario.vue";

describe("AppAlertaFormulario.vue", () => {
    it("deve renderizar a mensagem com test id configuravel", () => {
        const wrapper = mount(AppAlertaFormulario, {
            props: {
                mensagem: "Erro global do formulario",
                dataTestid: "alert-formulario",
            },
        });

        expect(wrapper.find('[data-testid="alert-formulario"]').exists()).toBe(true);
        expect(wrapper.text()).toContain("Erro global do formulario");
    });

    it("deve emitir dismissed quando o alerta for dispensado", () => {
        const wrapper = mount(AppAlertaFormulario, {
            props: {
                mensagem: "Erro",
            },
        });

        wrapper.findComponent({name: "AppAlert"}).vm.$emit("dismissed");
        expect(wrapper.emitted().dismissed).toBeDefined();
    });
});
