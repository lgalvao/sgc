import { mount, type VueWrapper } from "@vue/test-utils";
import { BTable } from "bootstrap-vue-next";
import { describe, expect, it } from "vitest";
import {
  type ProcessoResumo,
  SituacaoProcesso,
  TipoProcesso,
} from "@/types/tipos";
import TabelaProcessos from "../TabelaProcessos.vue";

// Mock de dados de processo
const mockProcessos: ProcessoResumo[] = [
  {
    codigo: 1,
    descricao: "Processo Alpha",
    tipo: TipoProcesso.MAPEAMENTO,
    unidadeCodigo: 1,
    unidadeNome: "UNID1, UNID2",
    situacao: SituacaoProcesso.EM_ANDAMENTO,
    dataLimite: new Date().toISOString(),
    dataCriacao: new Date().toISOString(),
    dataFinalizacao: null,
  },
  {
    codigo: 2,
    descricao: "Processo Beta",
    tipo: TipoProcesso.REVISAO,
    unidadeCodigo: 3,
    unidadeNome: "UNID3",
    situacao: SituacaoProcesso.FINALIZADO,
    dataLimite: new Date().toISOString(),
    dataCriacao: new Date().toISOString(),
    dataFinalizacao: new Date("2024-08-26").toISOString(),
    dataFinalizacaoFormatada: "26/08/2024",
  },
];

describe("TabelaProcessos.vue", () => {
  it("deve renderizar a tabela e os cabeçalhos corretamente", async () => {
    const wrapper = mount(TabelaProcessos, {
      props: {
        processos: [],
        criterioOrdenacao: "descricao",
        direcaoOrdenacaoAsc: true,
      },
    });

    const table = wrapper.findComponent(BTable);
    expect(table.exists()).toBe(true);

    await wrapper.vm.$nextTick();

    const headers = table.findAll("th");
    expect(headers[0].text()).toContain("Descrição");
    expect(headers[1].text()).toContain("Tipo");
    expect(headers[2].text()).toContain("Situação");
  });

  it("deve exibir os processos passados via prop", async () => {
    const wrapper = mount(TabelaProcessos, {
      props: {
        processos: mockProcessos,
        criterioOrdenacao: "descricao",
        direcaoOrdenacaoAsc: true,
      },
    });

    await wrapper.vm.$nextTick();

    const rows = wrapper.findAll("tbody tr");
    expect(rows.length).toBe(mockProcessos.length);

    const cells = rows[0].findAll("td");
    expect(cells[0].text()).toBe("Processo Alpha");
    expect(cells[1].text()).toBe("MAPEAMENTO");
    expect(cells[2].text()).toBe("EM_ANDAMENTO");

    const cells2 = rows[1].findAll("td");
    expect(cells2[0].text()).toBe("Processo Beta");
    expect(cells2[1].text()).toBe("REVISAO");
    expect(cells2[2].text()).toBe("FINALIZADO");
  });

  it("deve emitir o evento ordenar ao receber o evento sort-changed", async () => {
    const wrapper = mount(TabelaProcessos, {
      props: {
        processos: [],
        criterioOrdenacao: "descricao",
        direcaoOrdenacaoAsc: true,
      },
      global: {
        stubs: {
          BTable: {
            template: "<table><slot></slot></table>",
            emits: ["sort-changed"],
          },
        },
      },
    });

    await (
      wrapper.findComponent(BTable) as unknown as VueWrapper<any>
    ).vm.$emit("sort-changed", { sortBy: "tipo" });

    expect(wrapper.emitted("ordenar")).toBeTruthy();
    expect(wrapper.emitted("ordenar")![0]).toEqual(["tipo"]);
  });

  it("deve emitir o evento selecionarProcesso ao clicar em uma linha", async () => {
    const wrapper = mount(TabelaProcessos, {
      props: {
        processos: mockProcessos,
        criterioOrdenacao: "descricao",
        direcaoOrdenacaoAsc: true,
      },
    });

    await wrapper.vm.$nextTick();

    const rows = wrapper.findAll("tbody tr");
    await rows[0].trigger("click");

    expect(wrapper.emitted("selecionarProcesso")).toBeTruthy();
    expect(wrapper.emitted("selecionarProcesso")![0]).toEqual([
      mockProcessos[0],
    ]);
  });

  it("deve exibir a coluna Finalizado em quando showDataFinalizacao é true", async () => {
    const wrapper = mount(TabelaProcessos, {
      props: {
        processos: mockProcessos,
        criterioOrdenacao: "descricao",
        direcaoOrdenacaoAsc: true,
        showDataFinalizacao: true,
      },
    });

    await wrapper.vm.$nextTick();

    const headers = wrapper.findAll("th");
    expect(headers.some((h) => h.text() === "Finalizado em")).toBe(true);

    const rows = wrapper.findAll("tbody tr");
    expect(rows[1].text()).toContain("26/08/2024");
  });
});
