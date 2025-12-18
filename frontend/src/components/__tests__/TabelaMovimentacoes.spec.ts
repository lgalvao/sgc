import {describe, expect, it, vi} from "vitest";
import {mount, VueWrapper} from "@vue/test-utils";
import TabelaMovimentacoes from "../TabelaMovimentacoes.vue";
import type {Movimentacao, Unidade} from "@/types/tipos";
import {BTable} from "bootstrap-vue-next";

// Mock do utils para formatDateTimeBR
vi.mock("@/utils", () => ({
    formatDateTimeBR: (date: string) => `Formatted ${date}`,
}));

const mockUnidadeOrigem: Unidade = {
    codigo: 1,
    nome: "Origem A",
    sigla: "ORG"
};

const mockUnidadeDestino: Unidade = {
    codigo: 2,
    nome: "Destino B",
    sigla: "DST"
};

const mockMovimentacoes: Movimentacao[] = [
    {
        codigo: 1,
        dataHora: "2024-01-01T10:00:00Z",
        unidadeOrigem: mockUnidadeOrigem,
        unidadeDestino: mockUnidadeDestino,
        descricao: "Movimento 1",
        usuario: {
            codigo: 10,
            nome: "User",
            tituloEleitoral: "123",
            email: "email@test.com",
            ramal: "1234",
            unidade: mockUnidadeOrigem
        },
        subprocesso: {
            codigo: 99,
            unidade: mockUnidadeOrigem,
            situacao: "NAO_INICIADO" as any,
            dataLimite: "",
            dataFimEtapa1: "",
            dataLimiteEtapa2: "",
            atividades: [],
            codUnidade: 1
        }
    },
];

describe("TabelaMovimentacoes.vue", () => {
    it("deve renderizar a tabela com movimentações (verificar BTable)", () => {
        const wrapper = mount(TabelaMovimentacoes, {
            props: {movimentacoes: mockMovimentacoes},
            global: {stubs: {BTable: true}}
        });

        const bTable = wrapper.findComponent(BTable) as unknown as VueWrapper<any>;
        expect(bTable.exists()).toBe(true);
        expect(bTable.props("items")).toEqual(mockMovimentacoes);
    });

    it("deve renderizar mensagem quando não houver movimentações", () => {
        const wrapper = mount(TabelaMovimentacoes, {
            props: {movimentacoes: []},
            global: {stubs: {BTable: true}}
        });

        // Verificação ajustada para o texto diretamente, já que a classe .alert-info foi removida/alterada
        expect(wrapper.text()).toContain("Nenhuma movimentação registrada");
        expect(wrapper.findComponent(BTable).exists()).toBe(false);
    });

    it("deve passar a função rowAttr correta", () => {
        const wrapper = mount(TabelaMovimentacoes, {
            props: {movimentacoes: mockMovimentacoes},
            global: {stubs: {BTable: true}}
        });

        const bTable = wrapper.findComponent(BTable) as unknown as VueWrapper<any>;
        const rowAttrFn = bTable.props("tbodyTrAttr") || bTable.vm.$attrs["tbody-tr-attr"];

        expect(typeof rowAttrFn).toBe("function");

        expect(rowAttrFn(mockMovimentacoes[0], "row")).toEqual({'data-testid': 'row-movimentacao-1'});
        expect(rowAttrFn(null, "row")).toEqual({});
    });
});
