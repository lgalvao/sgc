import {describe, expect, it} from "vitest";
import {mount} from "@vue/test-utils";
import ProcessoInfo from "../ProcessoInfo.vue";
import {SituacaoProcesso, TipoProcesso} from "@/types/tipos";

describe("ProcessoInfo.vue", () => {
    it("deve renderizar informações básicas", () => {
        const wrapper = mount(ProcessoInfo, {
            props: {
                tipo: TipoProcesso.MAPEAMENTO,
                situacao: SituacaoProcesso.EM_ANDAMENTO,
                dataLimite: "2023-12-31T12:00:00"
            }
        });

        expect(wrapper.text()).toContain("Tipo: Mapeamento");
        expect(wrapper.text()).toContain("Situação: Em andamento");
        expect(wrapper.text()).toContain("Data Limite:");
        expect(wrapper.text()).toContain("31/12/2023");
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
                tipo: TipoProcesso.MAPEAMENTO,
                showTipo: false
            }
        });

        expect(wrapper.text()).not.toContain("Tipo:");
    });
});
