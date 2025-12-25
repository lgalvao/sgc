import {mount} from "@vue/test-utils";
import {BButton} from "bootstrap-vue-next";
import {describe, expect, it} from "vitest";
import ProcessoAcoes from "../ProcessoAcoes.vue";
import {getCommonMountOptions, setupComponentTest} from "@/test-utils/componentTestHelpers";

describe("ProcessoAcoes.vue", () => {
    const context = setupComponentTest();

    const mountOptions = getCommonMountOptions();
    mountOptions.global.components = { BButton };

    it('deve renderizar botão "Aceitar em bloco" para GESTOR', async () => {
        context.wrapper = mount(ProcessoAcoes, {
            ...mountOptions,
            props: {
                mostrarBotoesBloco: true,
                perfil: "GESTOR",
                situacaoProcesso: "EM_ANDAMENTO",
            },
        });

        const btn = context.wrapper.find('[data-testid="btn-acao-bloco-aceitar"]');
        expect(btn.exists()).toBe(true);
        expect(btn.text()).toContain("Aceitar em bloco");

        await btn.trigger("click");
        expect(context.wrapper.emitted("aceitarBloco")).toBeTruthy();
    });

    it("não deve renderizar botões de bloco se mostrarBotoesBloco for falso", () => {
        context.wrapper = mount(ProcessoAcoes, {
            ...mountOptions,
            props: {
                mostrarBotoesBloco: false,
                perfil: "GESTOR",
                situacaoProcesso: "EM_ANDAMENTO",
            },
        });

        expect(context.wrapper.find('[data-testid="btn-acao-bloco-aceitar"]').exists()).toBe(
            false,
        );
    });

    it('deve renderizar botão "Homologar em bloco" para ADMIN', async () => {
        context.wrapper = mount(ProcessoAcoes, {
            ...mountOptions,
            props: {
                mostrarBotoesBloco: true,
                perfil: "ADMIN",
                situacaoProcesso: "EM_ANDAMENTO",
            },
        });

        const btn = context.wrapper.find('[data-testid="btn-acao-bloco-homologar"]');
        expect(btn.exists()).toBe(true);
        expect(btn.text()).toContain("Homologar em bloco");

        await btn.trigger("click");
        expect(context.wrapper.emitted("homologarBloco")).toBeTruthy();
    });

    it('deve renderizar botão "Finalizar processo" para ADMIN em processo EM_ANDAMENTO', async () => {
        context.wrapper = mount(ProcessoAcoes, {
            ...mountOptions,
            props: {
                mostrarBotoesBloco: true,
                perfil: "ADMIN",
                situacaoProcesso: "EM_ANDAMENTO",
            },
        });

        const btn = context.wrapper.find('[data-testid="btn-processo-finalizar"]');
        expect(btn.exists()).toBe(true);
        expect(btn.text()).toContain("Finalizar processo");

        await btn.trigger("click");
        expect(context.wrapper.emitted("finalizar")).toBeTruthy();
    });

    it('não deve renderizar botão "Finalizar processo" se não for ADMIN', () => {
        context.wrapper = mount(ProcessoAcoes, {
            ...mountOptions,
            props: {
                mostrarBotoesBloco: true,
                perfil: "GESTOR",
                situacaoProcesso: "EM_ANDAMENTO",
            },
        });

        expect(
            context.wrapper.find('[data-testid="btn-finalizar-processo"]').exists(),
        ).toBe(false);
    });

    it('não deve renderizar botão "Finalizar processo" se situação não for EM_ANDAMENTO', () => {
        context.wrapper = mount(ProcessoAcoes, {
            ...mountOptions,
            props: {
                mostrarBotoesBloco: true,
                perfil: "ADMIN",
                situacaoProcesso: "FINALIZADO",
            },
        });

        expect(
            context.wrapper.find('[data-testid="btn-finalizar-processo"]').exists(),
        ).toBe(false);
    });
});
