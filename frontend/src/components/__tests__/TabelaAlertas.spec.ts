import {describe, expect, it} from "vitest";
import {mount} from "@vue/test-utils";
import TabelaAlertas from "../TabelaAlertas.vue";
import type {Alerta} from "@/types/tipos";
import {BTable} from "bootstrap-vue-next";

const mockAlertas: Alerta[] = [
    {
        codigo: 1,
        mensagem: "Alerta 1",
        processo: "Proc 1",
        origem: "Origem 1",
        dataHoraFormatada: "01/01/2024",
        dataHoraLeitura: null,
        dataHoraCriacao: "2024-01-01T00:00:00Z",
        servidorTitulo: "123",
        codigoProcesso: 101,
        siglaUnidade: "DTI"
    },
    {
        codigo: 2,
        mensagem: "Alerta 2",
        processo: "Proc 2",
        origem: "Origem 2",
        dataHoraFormatada: "02/01/2024",
        dataHoraLeitura: "2024-01-02T10:00:00Z",
        dataHoraCriacao: "2024-01-02T00:00:00Z",
        servidorTitulo: "123",
        codigoProcesso: 102,
        siglaUnidade: "DTI"
    },
];

describe("TabelaAlertas.vue", () => {
    it("deve passar os alertas para o BTable", () => {
        const wrapper = mount(TabelaAlertas, {
            props: {alertas: mockAlertas},
            global: {
                stubs: {BTable: true} // Stub do BTable para focar nas props
            }
        });

        const bTable = wrapper.findComponent(BTable);
        expect(bTable.exists()).toBe(true);
        expect(bTable.props("items")).toEqual(mockAlertas);
    });

    it("deve passar a função rowClass correta", () => {
        const wrapper = mount(TabelaAlertas, {
            props: {alertas: mockAlertas},
            global: {stubs: {BTable: true}}
        });

        const bTable = wrapper.findComponent(BTable);
        const rowClassFn = bTable.props("tbodyTrClass");

        // Testar a função passada
        expect(rowClassFn(mockAlertas[0], "row")).toBe("fw-bold"); // Não lido
        expect(rowClassFn(mockAlertas[1], "row")).toBe(""); // Lido
    });

    it("deve passar a função rowAttr correta", () => {
        const wrapper = mount(TabelaAlertas, {
            props: {alertas: mockAlertas},
            global: {stubs: {BTable: true}}
        });

        const bTable = wrapper.findComponent(BTable);
        // BTable do bootstrap-vue-next pode tratar isso como prop ou attr
        // No stub, vamos checar props primeiro, depois attrs
        const rowAttrFn = bTable.props("tbodyTrAttr") || bTable.attributes("tbody-tr-attr");

        // Se for atributo, pode ser que o Vue Test Utils não retorne o valor da prop passada como função se ela não for definida como prop no stub.
        // Mas vamos tentar acessar via vm.$attrs se necessário.

        // Se for undefined no props, tentamos attributes. Se attributes retornar string "[object Object]" ou similar, é problema.
        // O melhor é acessar props().tbodyTrAttr se o stub tiver props definidas, mas 'BTable: true' não define props.

        // Vamos tentar acessar bTable.vm.$attrs se disponível
        const fn = bTable.props("tbodyTrAttr") || bTable.vm.$attrs["tbody-tr-attr"];

        expect(typeof fn).toBe("function");

        // Testar a função passada
        expect(fn(mockAlertas[0], "row")).toEqual({'data-testid': 'row-alerta-1'});
        expect(fn(null, "row")).toEqual({});
    });

    it("deve emitir 'selecionar-alerta' quando BTable emitir row-clicked", async () => {
        const wrapper = mount(TabelaAlertas, {
            props: {alertas: mockAlertas},
            global: {stubs: {BTable: true}}
        });

        const bTable = wrapper.findComponent(BTable);
        await bTable.vm.$emit("row-clicked", mockAlertas[0]);

        expect(wrapper.emitted("selecionar-alerta")).toBeTruthy();
        expect(wrapper.emitted("selecionar-alerta")?.[0]).toEqual([mockAlertas[0]]);
    });

    it("deve emitir 'ordenar' quando BTable emitir sort-changed", async () => {
        const wrapper = mount(TabelaAlertas, {
            props: {alertas: mockAlertas},
            global: {stubs: {BTable: true}}
        });

        const bTable = wrapper.findComponent(BTable);

        await bTable.vm.$emit("sort-changed", {sortBy: "dataHoraFormatada"});
        expect(wrapper.emitted("ordenar")?.[0]).toEqual(["data"]);

        await bTable.vm.$emit("sort-changed", {sortBy: "processo"});
        expect(wrapper.emitted("ordenar")?.[1]).toEqual(["processo"]);
    });
});

