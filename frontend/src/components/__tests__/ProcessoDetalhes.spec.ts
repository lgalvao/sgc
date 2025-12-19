import {mount} from "@vue/test-utils";
import {BBadge} from "bootstrap-vue-next";
import {describe, expect, it} from "vitest";
import ProcessoDetalhes from "../ProcessoDetalhes.vue";
import { setupComponentTest, getCommonMountOptions } from "@/test-utils/componentTestHelpers";

describe("ProcessoDetalhes.vue", () => {
    const context = setupComponentTest();

    const mountOptions = getCommonMountOptions();
    mountOptions.global.components = {
        BBadge,
    };

    it("renders correctly with props", () => {
        context.wrapper = mount(ProcessoDetalhes, {
            ...mountOptions,
            props: {
                descricao: "Processo de Teste",
                tipo: "REVISAO",
                situacao: "EM_ANDAMENTO",
            },
        });

        expect(context.wrapper.find('[data-testid="processo-info"]').text()).toBe(
            "Processo de Teste",
        );
        expect(context.wrapper.text()).toContain("Tipo: Revisão");
        expect(context.wrapper.text()).toContain("Situação: Em andamento");
        expect(context.wrapper.findComponent(BBadge).exists()).toBe(true);
    });
});
