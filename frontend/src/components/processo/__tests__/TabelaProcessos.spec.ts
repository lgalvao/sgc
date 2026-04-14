import {mount} from "@vue/test-utils";
import {describe, expect, it} from "vitest";
import TabelaProcessos from "@/components/processo/TabelaProcessos.vue";
import {type ProcessoResumo, SituacaoProcesso, TipoProcesso} from "@/types/tipos";

const BTableStub = {
    name: "BTable",
    props: ["fields", "items", "sortBy", "tbodyTrAttrs", "tbodyTrClass"],
    template: `
      <div data-testid="btable-stub">
        <slot name="cell(dataFinalizacao)" :item="items[0]" />
        <slot name="cell(situacao)" :item="items[0]" />
      </div>
    `,
};

describe("TabelaProcessos.vue", () => {
    const processos: ProcessoResumo[] = [{
        codigo: 1,
        descricao: "Processo finalizado",
        tipo: TipoProcesso.MAPEAMENTO,
        situacao: SituacaoProcesso.FINALIZADO,
        dataLimite: "2024-01-10T00:00:00",
        dataCriacao: "2024-01-01T08:00:00",
        dataFinalizacao: "2024-01-02T15:45:00",
        unidadeCodigo: 1,
        unidadeNome: "Secretaria 1",
        unidadesParticipantes: "SECRETARIA_1",
    }];

    it("deve ocultar a coluna de situação quando configurado", () => {
        const wrapper = mount(TabelaProcessos, {
            props: {
                processos,
                criterioOrdenacao: "dataFinalizacao",
                direcaoOrdenacaoAsc: false,
                showDataFinalizacao: true,
                showSituacao: false,
            },
            global: {
                stubs: {
                    BTable: BTableStub,
                    BBadge: true,
                    BButton: true,
                    EmptyState: true,
                },
            },
        });

        expect((wrapper.vm as any).fields.map((field: { key: string }) => field.key)).toEqual([
            "descricao",
            "tipo",
            "unidadesParticipantes",
            "dataFinalizacao",
        ]);
    });

    it("deve renderizar finalização apenas com a data", () => {
        const wrapper = mount(TabelaProcessos, {
            props: {
                processos,
                criterioOrdenacao: "dataFinalizacao",
                direcaoOrdenacaoAsc: false,
                showDataFinalizacao: true,
            },
            global: {
                stubs: {
                    BTable: BTableStub,
                    BBadge: true,
                    BButton: true,
                    EmptyState: true,
                },
            },
        });

        expect(wrapper.text()).toContain("02/01/2024");
        expect(wrapper.text()).not.toContain("15:45");
    });
});
