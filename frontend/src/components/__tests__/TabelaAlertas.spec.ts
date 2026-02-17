import {describe, expect, it} from "vitest";
import {mount, VueWrapper} from "@vue/test-utils";
import TabelaAlertas from "../processo/TabelaAlertas.vue";
import EmptyState from "../comum/EmptyState.vue";
import type {Alerta} from "@/types/tipos";
import {BTable as _BTable} from "bootstrap-vue-next";
import {setupComponentTest} from "@/test-utils/componentTestHelpers";
import {checkA11y} from "@/test-utils/a11yTestHelpers";

const mockAlertas: Alerta[] = [
    {
        codigo: 1,
        mensagem: "Alerta 1",
        processo: "Proc 1",
        origem: "Origem 1",
        dataHoraLeitura: null, // Não lido
        dataHora: "2024-01-01T00:00:00Z",
        codProcesso: 101,
        unidadeOrigem: "DTI",
        unidadeDestino: "DEST",
        descricao: "Desc"
    },
    {
        codigo: 2,
        mensagem: "Alerta 2",
        processo: "Proc 2",
        origem: "Origem 2",
        dataHoraLeitura: "2024-01-02T10:00:00Z", // Lido
        dataHora: "2024-01-02T00:00:00Z",
        codProcesso: 102,
        unidadeOrigem: "DTI",
        unidadeDestino: "DEST",
        descricao: "Desc"
    },
];

describe("TabelaAlertas.vue", () => {
    setupComponentTest();

    it("deve passar os alertas para o BTable", () => {
        const wrapper = mount(TabelaAlertas, {
            props: {alertas: mockAlertas},
            global: {
                stubs: {BTable: true},
                components: { EmptyState }
            }
        });

        const bTable = wrapper.findComponent(_BTable as any) as unknown as VueWrapper<any>;
        expect(bTable.exists()).toBe(true);
        expect(bTable.props("items")).toEqual(mockAlertas);
    });

    it("deve renderizar o texto 'Não lido' para alertas não lidos (acessibilidade)", () => {
        const wrapper = mount(TabelaAlertas, {
            props: {alertas: mockAlertas},
             // Não stubbando BTable permite renderização real dos slots
             global: {
                 // stubs: { BTable: false }
             }
        });

        // Verifica se o texto visualmente oculto está presente e associado ao alerta não lido
        const unreadAlertRow = wrapper.findAll('tr')[1]; // Primeira linha de dados (após header)
        expect(unreadAlertRow.text()).toContain('Não lido:');
        expect(unreadAlertRow.find('.visually-hidden').text()).toBe('Não lido:');

        // Verifica se o alerta lido não tem o texto
        const readAlertRow = wrapper.findAll('tr')[2]; // Segunda linha de dados
        expect(readAlertRow.text()).not.toContain('Não lido:');
    });

    it("deve passar a função rowClass correta", () => {
        const wrapper = mount(TabelaAlertas, {
            props: {alertas: mockAlertas},
            global: {stubs: {BTable: true}}
        });

        const bTable = wrapper.findComponent(_BTable as any) as unknown as VueWrapper<any>;
        const rowClassFn = bTable.props("tbodyTrClass");

        expect(rowClassFn(mockAlertas[0], "row")).toBe("fw-bold"); // Não lido
        expect(rowClassFn(mockAlertas[1], "row")).toBe(""); // Lido
    });

    it("deve passar a função rowAttr correta", () => {
        const wrapper = mount(TabelaAlertas, {
            props: {alertas: mockAlertas},
            global: {stubs: {BTable: true}}
        });

        const bTable = wrapper.findComponent(_BTable as any) as unknown as VueWrapper<any>;
        const fn = bTable.props("tbodyTrAttr") || bTable.vm.$attrs["tbody-tr-attr"];

        expect(typeof fn).toBe("function");
        expect(fn(mockAlertas[0], "row")).toEqual({'data-testid': 'row-alerta-1'});
        expect(fn(null, "row")).toEqual({});
    });

    it("deve emitir 'ordenar' quando BTable emitir sort-changed", async () => {
        const wrapper = mount(TabelaAlertas, {
            props: {alertas: mockAlertas},
            global: {stubs: {BTable: true}}
        });

        const bTable = wrapper.findComponent(_BTable as any) as unknown as VueWrapper<any>;

        await bTable.vm.$emit("sort-changed", {sortBy: "dataHora"});
        expect(wrapper.emitted("ordenar")?.[0]).toEqual(["data"]);

        await bTable.vm.$emit("sort-changed", {sortBy: "processo"});
        expect(wrapper.emitted("ordenar")?.[1]).toEqual(["processo"]);
    });

    it("deve ser acessível", async () => {
        const wrapper = mount(TabelaAlertas, {
            props: {alertas: mockAlertas},
        });
        await checkA11y(wrapper.element as HTMLElement);
    });
});
