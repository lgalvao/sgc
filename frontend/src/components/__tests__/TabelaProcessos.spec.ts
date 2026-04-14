import {mount} from "@vue/test-utils";
import {describe, expect, it} from "vitest";
import {type ProcessoResumo, SituacaoProcesso, TipoProcesso,} from "@/types/tipos";
import TabelaProcessos from "../processo/TabelaProcessos.vue";
import EmptyState from "../comum/EmptyState.vue";
import {getCommonMountOptions, setupComponentTest} from "@/test-utils/componentTestHelpers";

const BTableSortStub = {
    name: "BTable",
    template: "<table><slot /></table>",
    emits: ["update:sort-by"],
};

const mockProcessos: ProcessoResumo[] = [
    {
        codigo: 1,
        descricao: "Processo alpha",
        tipo: TipoProcesso.MAPEAMENTO,
        unidadeCodigo: 1,
        unidadeNome: "UNID1, UNID2",
        unidadesParticipantes: "UNID1, UNID2",
        situacao: SituacaoProcesso.EM_ANDAMENTO,
        dataLimite: "2023-10-15T10:00:00Z",
        dataCriacao: new Date().toISOString(),
        dataFinalizacao: undefined,
    },
    {
        codigo: 2,
        descricao: "Processo beta",
        tipo: TipoProcesso.REVISAO,
        unidadeCodigo: 3,
        unidadeNome: "UNID3",
        unidadesParticipantes: "UNID3",
        situacao: SituacaoProcesso.FINALIZADO,
        dataLimite: "2023-11-20T10:00:00Z",
        dataCriacao: new Date().toISOString(),
        dataFinalizacao: "2024-08-26T21:00:00Z",
    },
];

describe("TabelaProcessos.vue", () => {
    const context = setupComponentTest();

    it("deve renderizar a tabela e os cabeçalhos corretamente", async () => {
        context.wrapper = mount(TabelaProcessos, {
            ...getCommonMountOptions(),
            props: {
                processos: mockProcessos,
                criterioOrdenacao: "descricao",
                direcaoOrdenacaoAsc: true,
            },
        });

        const table = context.wrapper.findComponent({name: "BTable"});
        expect(table.exists()).toBe(true);

        await context.wrapper.vm.$nextTick();

        expect((context.wrapper.vm as any).fields).toEqual([
            {key: "descricao", label: "Descrição", sortable: true},
            {key: "tipo", label: "Tipo", sortable: true},
            {key: "unidadesParticipantes", label: "Unidades participantes", sortable: false},
            {key: "situacao", label: "Situação", sortable: true},
        ]);
    });

    it("deve exibir os processos passados via prop", async () => {
        context.wrapper = mount(TabelaProcessos, {
            ...getCommonMountOptions(),
            props: {
                processos: mockProcessos,
                criterioOrdenacao: "descricao",
                direcaoOrdenacaoAsc: true,
            },
        });

        await context.wrapper.vm.$nextTick();

        expect(context.wrapper.text()).toContain("Processo alpha");
        expect(context.wrapper.text()).toContain("Mapeamento");
        expect(context.wrapper.text()).toContain("UNID1, UNID2");
        expect(context.wrapper.text()).toContain("Em andamento");
        expect(context.wrapper.text()).toContain("Processo beta");
        expect(context.wrapper.text()).toContain("Revisão");
        expect(context.wrapper.text()).toContain("UNID3");
        expect(context.wrapper.text()).toContain("Finalizado");
    });

    it("deve emitir o evento ordenar ao receber o evento sort-changed", async () => {
        context.wrapper = mount(TabelaProcessos, {
            ...getCommonMountOptions({}, {
                BTable: {
                    ...BTableSortStub,
                }
            }),
            props: {
                processos: mockProcessos,
                criterioOrdenacao: "descricao",
                direcaoOrdenacaoAsc: true,
            },
        });

        await context.wrapper.findComponent({name: "BTable"}).vm.$emit("update:sort-by", [{key: "tipo", order: "asc"}]);

        expect(context.wrapper.emitted("ordenar")).toBeTruthy();
        expect(context.wrapper.emitted("ordenar")![0]).toEqual(["tipo"]);
    });

    it("deve emitir o evento selecionarProcesso ao clicar em uma linha", async () => {
        context.wrapper = mount(TabelaProcessos, {
            ...getCommonMountOptions(),
            props: {
                processos: mockProcessos,
                criterioOrdenacao: "descricao",
                direcaoOrdenacaoAsc: true,
            },
        });

        await context.wrapper.vm.$nextTick();

        const rows = context.wrapper.findAll("tbody tr");
        await rows[0].trigger("click");

        expect(context.wrapper.emitted("selecionarProcesso")).toBeTruthy();
        expect(context.wrapper.emitted("selecionarProcesso")![0]).toEqual([
            mockProcessos[0],
        ]);
    });

    it("deve emitir o evento selecionarProcesso ao pressionar Enter em uma linha", async () => {
        context.wrapper = mount(TabelaProcessos, {
            ...getCommonMountOptions({}, {
                BTable: {
                    props: ['items', 'tbodyTrAttrs'],
                    template: `
                        <table>
                            <tbody>
                                <tr v-for="item in items"
                                    :key="item.codigo"
                                    v-bind="tbodyTrAttrs ? tbodyTrAttrs(item, 'row') : {}">
                                    <td>{{ item.descricao }}</td>
                                </tr>
                            </tbody>
                        </table>
                    `
                }
            }),
            props: {
                processos: mockProcessos,
                criterioOrdenacao: "descricao",
                direcaoOrdenacaoAsc: true,
            },
        });

        await context.wrapper.vm.$nextTick();

        const rows = context.wrapper.findAll("tbody tr");
        await rows[0].trigger("keydown", {key: "Enter"});

        expect(context.wrapper.emitted("selecionarProcesso")).toBeTruthy();
        expect(context.wrapper.emitted("selecionarProcesso")![0]).toEqual([
            mockProcessos[0],
        ]);
    });

    it("deve exibir a coluna Finalizado em quando showDataFinalizacao é true", async () => {
        context.wrapper = mount(TabelaProcessos, {
            ...getCommonMountOptions(),
            props: {
                processos: mockProcessos,
                criterioOrdenacao: "descricao",
                direcaoOrdenacaoAsc: true,
                showDataFinalizacao: true,
            },
        });

        await context.wrapper.vm.$nextTick();

        const headers = context.wrapper.findAll("th");
        expect(headers.some((h) => h.text() === "Finalizado em")).toBe(true);

        const rows = context.wrapper.findAll("tbody tr");
        expect(rows[1].text()).toContain("26/08/2024");
    });

    it("deve formatar corretamente situações e tipos variados e desconhecidos", async () => {
        const processosVariados: ProcessoResumo[] = [
            {
                ...mockProcessos[0],
                codigo: 3,
                situacao: SituacaoProcesso.CRIADO,
                tipo: TipoProcesso.DIAGNOSTICO,
            },
            {
                ...mockProcessos[0],
                codigo: 4,
                situacao: "OUTRA_SITUACAO" as unknown as SituacaoProcesso,
                tipo: "OUTRO_TIPO" as unknown as TipoProcesso,
            }
        ];

        context.wrapper = mount(TabelaProcessos, {
            ...getCommonMountOptions(),
            props: {
                processos: processosVariados,
                criterioOrdenacao: "descricao",
                direcaoOrdenacaoAsc: true,
            },
        });

        await context.wrapper.vm.$nextTick();
        expect(context.wrapper.text()).toContain("Diagnóstico");
        expect(context.wrapper.text()).toContain("Criado");
        expect(context.wrapper.text()).toContain("OUTRO_TIPO");
        expect(context.wrapper.text()).toContain("OUTRA_SITUACAO");
    });

    it("deve aplicar atributos nas linhas", async () => {
        context.wrapper = mount(TabelaProcessos, {
            ...getCommonMountOptions(),
            props: {
                processos: mockProcessos,
                criterioOrdenacao: "descricao",
                direcaoOrdenacaoAsc: true,
            },
        });
        await context.wrapper.vm.$nextTick();
        const row = context.wrapper.find(`.row-processo-${mockProcessos[0].codigo}`);
        expect(row.exists()).toBe(true);
    });

    it("deve exibir mensagem de vazio quando não houver processos", async () => {
        context.wrapper = mount(TabelaProcessos, {
            ...getCommonMountOptions(),
            global: {
                components: {EmptyState}
            },
            props: {
                processos: [],
                criterioOrdenacao: "descricao",
                direcaoOrdenacaoAsc: true,
            },
        });

        expect(context.wrapper.find('[data-testid="empty-state-processos"]').exists()).toBe(true);
        expect(context.wrapper.find('[data-testid="tbl-processos"]').exists()).toBe(false);
        expect(context.wrapper.text()).toContain("Nenhum processo");
    });

    it("deve exibir CTA no estado vazio e emitir evento ao clicar", async () => {
        context.wrapper = mount(TabelaProcessos, {
            ...getCommonMountOptions(),
            props: {
                processos: [],
                criterioOrdenacao: "descricao",
                direcaoOrdenacaoAsc: true,
                mostrarCtaVazio: true,
            },
        });

        await context.wrapper.vm.$nextTick();
        const botaoCta = context.wrapper.find('[data-testid="btn-empty-state-criar-processo"]');
        expect(botaoCta.exists()).toBe(true);
        await botaoCta.trigger("click");
        expect(context.wrapper.emitted("ctaVazio")).toBeTruthy();
    });

    describe("Modo compacto", () => {
        it("deve exibir o rótulo reduzido 'Unidades' quando compacto é true", async () => {
            context.wrapper = mount(TabelaProcessos, {
                ...getCommonMountOptions(),
                props: {
                    processos: mockProcessos,
                    criterioOrdenacao: "descricao",
                    direcaoOrdenacaoAsc: true,
                    compacto: true,
                },
            });

            await context.wrapper.vm.$nextTick();
            const headers = context.wrapper.findAll("th");
            expect(headers.some(h => h.text() === "Unidades")).toBe(true);
            expect(headers.some(h => h.text() === "Unidades participantes")).toBe(false);
        });

        it("deve manter sortable: true mesmo em modo compacto", async () => {
            context.wrapper = mount(TabelaProcessos, {
                ...getCommonMountOptions(),
                props: {
                    processos: mockProcessos,
                    criterioOrdenacao: "descricao",
                    direcaoOrdenacaoAsc: true,
                    compacto: true,
                },
            });

            const table = context.wrapper.findComponent({name: "BTable"});
            const fields = table.props("fields") as Array<{key: string; sortable?: boolean}>;
            const desc = fields.find(f => f.key === "descricao");
            expect(desc?.sortable).toBe(true);
        });

        it("deve emitir evento de ordenação ao clicar no cabeçalho em modo compacto", async () => {
            context.wrapper = mount(TabelaProcessos, {
                ...getCommonMountOptions({}, {
                BTable: {
                        ...BTableSortStub,
                    }
                }),
                props: {
                    processos: mockProcessos,
                    criterioOrdenacao: "descricao",
                    direcaoOrdenacaoAsc: true,
                    compacto: true,
                },
            });

            await context.wrapper.findComponent({name: "BTable"}).vm.$emit("update:sort-by", [{key: "tipo", order: "asc"}]);

            expect(context.wrapper.emitted("ordenar")).toBeTruthy();
            expect(context.wrapper.emitted("ordenar")![0]).toEqual(["tipo"]);
        });
    });
});
