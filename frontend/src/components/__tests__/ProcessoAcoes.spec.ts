import {mount} from "@vue/test-utils";
import {BButton} from "bootstrap-vue-next";
import {describe, expect, it} from "vitest";
import ProcessoAcoes from "../processo/ProcessoAcoes.vue";
import {getCommonMountOptions, setupComponentTest} from "@/test-utils/componentTestHelpers";

describe("ProcessoAcoes.vue", () => {
    const context = setupComponentTest();

    const mountOptions = getCommonMountOptions();
    mountOptions.global.components = {BButton};

    it('deve renderizar botão "Aceitar em bloco" quando podeAceitarBloco é true', async () => {
        context.wrapper = mount(ProcessoAcoes, {
            ...mountOptions,
            props: {
                podeAceitarBloco: true,
                podeHomologarBloco: false,
                podeFinalizar: false,
            },
        });

        const btn = context.wrapper.find('[data-testid="btn-acao-bloco-aceitar"]');
        expect(btn.exists()).toBe(true);
        expect(btn.text()).toContain("Aceitar em bloco");

        await btn.trigger("click");
        expect(context.wrapper.emitted("aceitarBloco")).toBeTruthy();
    });

    it("não deve renderizar botão de aceitar quando podeAceitarBloco é false", () => {
        context.wrapper = mount(ProcessoAcoes, {
            ...mountOptions,
            props: {
                podeAceitarBloco: false,
                podeHomologarBloco: false,
                podeFinalizar: false,
            },
        });

        expect(context.wrapper.find('[data-testid="btn-acao-bloco-aceitar"]').exists()).toBe(
            false,
        );
    });

    it('deve renderizar botão "Homologar em bloco" quando podeHomologarBloco é true', async () => {
        context.wrapper = mount(ProcessoAcoes, {
            ...mountOptions,
            props: {
                podeAceitarBloco: false,
                podeHomologarBloco: true,
                podeFinalizar: false,
            },
        });

        const btn = context.wrapper.find('[data-testid="btn-acao-bloco-homologar"]');
        expect(btn.exists()).toBe(true);
        expect(btn.text()).toContain("Homologar em bloco");

        await btn.trigger("click");
        expect(context.wrapper.emitted("homologarBloco")).toBeTruthy();
    });

    it('deve renderizar botão "Finalizar processo" quando podeFinalizar é true', async () => {
        context.wrapper = mount(ProcessoAcoes, {
            ...mountOptions,
            props: {
                podeAceitarBloco: false,
                podeHomologarBloco: false,
                podeFinalizar: true,
            },
        });

        const btn = context.wrapper.find('[data-testid="btn-processo-finalizar"]');
        expect(btn.exists()).toBe(true);
        expect(btn.text()).toContain("Finalizar processo");

        await btn.trigger("click");
        expect(context.wrapper.emitted("finalizar")).toBeTruthy();
    });

    it('não deve renderizar botão "Finalizar processo" quando podeFinalizar é false', () => {
        context.wrapper = mount(ProcessoAcoes, {
            ...mountOptions,
            props: {
                podeAceitarBloco: false,
                podeHomologarBloco: false,
                podeFinalizar: false,
            },
        });

        expect(
            context.wrapper.find('[data-testid="btn-processo-finalizar"]').exists(),
        ).toBe(false);
    });

    it("não deve renderizar botão de homologar quando podeHomologarBloco é false", () => {
        context.wrapper = mount(ProcessoAcoes, {
            ...mountOptions,
            props: {
                podeAceitarBloco: false,
                podeHomologarBloco: false,
                podeFinalizar: false,
            },
        });

        expect(
            context.wrapper.find('[data-testid="btn-acao-bloco-homologar"]').exists(),
        ).toBe(false);
    });
});
