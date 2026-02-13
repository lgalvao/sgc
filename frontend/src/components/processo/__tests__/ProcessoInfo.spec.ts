import {describe, expect, it} from "vitest";
import {mount} from "@vue/test-utils";
import ProcessoInfo from "../ProcessoInfo.vue";

describe("ProcessoInfo.vue", () => {
    it("deve renderizar informações básicas", () => {
        const wrapper = mount(ProcessoInfo, {
            props: {
                tipoLabel: "Cadastro",
                situacaoLabel: "Em Andamento",
                dataLimite: "2023-12-31"
            }
        });

        expect(wrapper.text()).toContain("Tipo: Cadastro");
        expect(wrapper.text()).toContain("Situação: Em Andamento");
        expect(wrapper.text()).toContain("Data Limite:");
        expect(wrapper.text()).toContain("/12/2023");
    });

    it("deve renderizar número de unidades quando solicitado", () => {
        const wrapper = mount(ProcessoInfo, {
            props: {
                numUnidades: 5,
                showUnidades: true
            }
        });

        expect(wrapper.text()).toContain("Unidades participantes: 5");
    });

    it("não deve renderizar campos ocultos", () => {
        const wrapper = mount(ProcessoInfo, {
            props: {
                tipoLabel: "Tipo",
                showTipo: false
            }
        });

        expect(wrapper.text()).not.toContain("Tipo:");
    });

    it("formatarData deve lidar com data nula", () => {
        const wrapper = mount(ProcessoInfo);
        expect((wrapper.vm as any).formatarData('')).toBe('');
        expect((wrapper.vm as any).formatarData(null)).toBe('');
    });
});
