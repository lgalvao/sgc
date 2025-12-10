import { describe, it, expect, vi } from "vitest";
import { mount } from "@vue/test-utils";
import TabelaMovimentacoes from "../TabelaMovimentacoes.vue";
import type { Movimentacao } from "@/types/tipos";
import { BTable } from "bootstrap-vue-next";

// Mock do utils para formatDateTimeBR
vi.mock("@/utils", () => ({
  formatDateTimeBR: (date: string) => `Formatted ${date}`,
}));

const mockMovimentacoes: Movimentacao[] = [
  {
    codigo: 1,
    dataHora: "2024-01-01T10:00:00Z",
    unidadeOrigem: "Origem A",
    unidadeDestino: "Destino B",
    descricao: "Movimento 1",
    usuarioNome: "User",
    usuarioTitulo: "123"
  },
];

describe("TabelaMovimentacoes.vue", () => {
  it("deve renderizar a tabela com movimentações (verificar BTable)", () => {
    const wrapper = mount(TabelaMovimentacoes, {
      props: { movimentacoes: mockMovimentacoes },
      global: { stubs: { BTable: true } }
    });

    const bTable = wrapper.findComponent(BTable);
    expect(bTable.exists()).toBe(true);
    expect(bTable.props("items")).toEqual(mockMovimentacoes);
  });

  it("deve renderizar mensagem quando não houver movimentações", () => {
    const wrapper = mount(TabelaMovimentacoes, {
      props: { movimentacoes: [] },
      global: { stubs: { BTable: true } }
    });

    expect(wrapper.find(".alert-info").exists()).toBe(true);
    expect(wrapper.text()).toContain("Nenhuma movimentação registrada");
    expect(wrapper.findComponent(BTable).exists()).toBe(false);
  });

  it("deve passar a função rowAttr correta", () => {
    const wrapper = mount(TabelaMovimentacoes, {
        props: { movimentacoes: mockMovimentacoes },
        global: { stubs: { BTable: true } }
    });

    const bTable = wrapper.findComponent(BTable);
    const rowAttrFn = bTable.props("tbodyTrAttr") || bTable.vm.$attrs["tbody-tr-attr"];
    
    expect(typeof rowAttrFn).toBe("function");
    
    expect(rowAttrFn(mockMovimentacoes[0], "row")).toEqual({ 'data-testid': 'row-movimentacao-1' });
    expect(rowAttrFn(null, "row")).toEqual({});
  });
});

