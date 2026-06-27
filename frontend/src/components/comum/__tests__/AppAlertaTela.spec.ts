import {describe, expect, it} from "vitest";
import {mount} from "@vue/test-utils";
import AppAlertaTela from "../AppAlertaTela.vue";

describe("AppAlertaTela.vue", () => {
    it("deve renderizar a mensagem com test id configuravel", () => {
        const wrapper = mount(AppAlertaTela, {
            props: {
                mensagem: "Erro persistente de tela",
                dataTestid: "alerta-tela",
            },
        });

        expect(wrapper.find('[data-testid="alerta-tela"]').exists()).toBe(true);
        expect(wrapper.text()).toContain("Erro persistente de tela");
    });

    it("deve emitir dismissed quando o alerta for dispensado", () => {
        const wrapper = mount(AppAlertaTela, {
            props: {
                mensagem: "Erro",
            },
        });

        wrapper.findComponent({name: "AppAlert"}).vm.$emit("dismissed");
        expect(wrapper.emitted().dismissed).toBeDefined();
    });
});
