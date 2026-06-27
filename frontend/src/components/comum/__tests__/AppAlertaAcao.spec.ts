import {describe, expect, it} from "vitest";
import {mount} from "@vue/test-utils";
import AppAlertaAcao from "../AppAlertaAcao.vue";

describe("AppAlertaAcao.vue", () => {
    it("deve renderizar feedback de sucesso", () => {
        const wrapper = mount(AppAlertaAcao, {
            props: {
                feedback: {
                    mensagem: "Operacao concluida",
                    variante: "success",
                },
            },
        });

        expect(wrapper.find('[data-testid="app-alerta-acao"]').exists()).toBe(true);
        expect(wrapper.text()).toContain("Operacao concluida");
    });

    it("nao deve renderizar sem feedback", () => {
        const wrapper = mount(AppAlertaAcao);
        expect(wrapper.find('[data-testid="app-alerta-acao"]').exists()).toBe(false);
    });

    it("deve emitir dismissed quando o alerta for dispensado", () => {
        const wrapper = mount(AppAlertaAcao, {
            props: {
                feedback: {
                    mensagem: "Erro",
                    variante: "danger",
                },
            },
        });

        wrapper.findComponent({name: "AppAlert"}).vm.$emit("dismissed");
        expect(wrapper.emitted().dismissed).toBeDefined();
    });
});
