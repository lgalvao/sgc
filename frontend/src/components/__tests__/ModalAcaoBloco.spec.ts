import {mount} from "@vue/test-utils";
import {BFormCheckbox} from "bootstrap-vue-next";
import {describe, expect, it} from "vitest";
import type {UnidadeSelecao} from "../ModalAcaoBloco.vue";
import ModalAcaoBloco from "../ModalAcaoBloco.vue";
import {getCommonMountOptions, setupComponentTest} from "@/test-utils/componentTestHelpers";

describe("ModalAcaoBloco", () => {
    const context = setupComponentTest();

    const unidades: UnidadeSelecao[] = [
        {
            sigla: "U1",
            nome: "Unidade 1",
            situacao: "Pendente",
            selecionada: false,
        },
        {sigla: "U2", nome: "Unidade 2", situacao: "Pendente", selecionada: true},
    ];

    const mountOptions = getCommonMountOptions();
    mountOptions.global.components = {
        BFormCheckbox,
    };

    it("não deve renderizar o modal quando mostrar for falso", () => {
        context.wrapper = mount(ModalAcaoBloco, {
            ...mountOptions,
            props: {mostrar: false, tipo: "aceitar", unidades},
        });
        expect(context.wrapper.find(".table").exists()).toBe(false);
    });

    it('deve renderizar o título e o botão corretos para o tipo "aceitar"', () => {
        context.wrapper = mount(ModalAcaoBloco, {
            ...mountOptions,
            props: {mostrar: true, tipo: "aceitar", unidades},
        });
        expect(context.wrapper.find(".btn-primary").text()).toContain("Aceitar");
    });

    it('deve renderizar o título e o botão corretos para o tipo "homologar"', () => {
        context.wrapper = mount(ModalAcaoBloco, {
            ...mountOptions,
            props: {mostrar: true, tipo: "homologar", unidades},
        });
        expect(context.wrapper.find(".btn-success").text()).toContain("Homologar");
    });

    it("deve renderizar a lista de unidades", () => {
        context.wrapper = mount(ModalAcaoBloco, {
            ...mountOptions,
            props: {mostrar: true, tipo: "aceitar", unidades},
        });
        const rows = context.wrapper.findAll("tbody tr");
        expect(rows.length).toBe(unidades.length);
        expect(rows[0].text()).toContain("Unidade 1");
        expect(rows[1].findComponent(BFormCheckbox).props().modelValue).toBe(true);
    });

    it('deve emitir "fechar" ao clicar no botão de cancelar', async () => {
        context.wrapper = mount(ModalAcaoBloco, {
            ...mountOptions,
            props: {mostrar: true, tipo: "aceitar", unidades},
        });
        await context.wrapper.find(".btn-secondary").trigger("click");
        expect(context.wrapper.emitted("fechar")).toBeTruthy();
    });

    it('deve emitir "confirmar" com as unidades selecionadas', async () => {
        context.wrapper = mount(ModalAcaoBloco, {
            ...mountOptions,
            props: {mostrar: true, tipo: "aceitar", unidades},
        });
        await context.wrapper.find(".btn-primary").trigger("click");
        expect(context.wrapper.emitted("confirmar")).toBeTruthy();
        const emittedUnidades = context.wrapper.emitted(
            "confirmar",
        )?.[0][0] as UnidadeSelecao[];
        expect(emittedUnidades.length).toBe(unidades.length);
        expect(emittedUnidades[1].selecionada).toBe(true);
    });
});
