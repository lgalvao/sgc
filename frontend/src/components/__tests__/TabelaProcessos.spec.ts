import {mount} from "@vue/test-utils";
import {BTable as _BTable} from "bootstrap-vue-next";
import {describe, expect, it} from "vitest";
import {type ProcessoResumo, SituacaoProcesso, TipoProcesso,} from "@/types/tipos";
import TabelaProcessos from "../processo/TabelaProcessos.vue";
import EmptyState from "../comum/EmptyState.vue";
import {getCommonMountOptions, setupComponentTest} from "@/test-utils/componentTestHelpers";

// Mock de dados de processo
const mockProcessos: ProcessoResumo[] = [
    {
        codigo: 1,
        descricao: "Processo Alpha",
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
        descricao: "Processo Beta",
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
        // Do not stub BTable so we can check headers rendered by it
        context.wrapper = mount(TabelaProcessos, {
            ...getCommonMountOptions(),
            props: {
                processos: [],
                criterioOrdenacao: "descricao",
                direcaoOrdenacaoAsc: true,
            },
        });

        const table = context.wrapper.findComponent(_BTable as any) as any;
        expect(table.exists()).toBe(true);

        await context.wrapper.vm.$nextTick();

        const headers = table.findAll("th");
        expect(headers.length).toBeGreaterThan(0);
        expect(headers[0].text()).toContain("Descrição");
        expect(headers[1].text()).toContain("Tipo");
        expect(headers[2].text()).toContain("Unidades participantes");
        expect(headers[3].text()).toContain("Situação");
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

        const rows = context.wrapper.findAll("tbody tr");
        expect(rows.length).toBe(mockProcessos.length);

        const cells = rows[0].findAll("td");
        expect(cells[0].text()).toBe("Processo Alpha");
        expect(cells[1].text()).toBe("Mapeamento");
        expect(cells[2].text()).toBe("UNID1, UNID2");
        expect(cells[3].text()).toBe("Em andamento");

        const cells2 = rows[1].findAll("td");
        expect(cells2[0].text()).toBe("Processo Beta");
        expect(cells2[1].text()).toBe("Revisão");
        expect(cells2[2].text()).toBe("UNID3");
        expect(cells2[3].text()).toBe("Finalizado");
    });

    it("deve emitir o evento ordenar ao receber o evento sort-changed", async () => {
        // Here we stub BTable to easily trigger sort-changed
        context.wrapper = mount(TabelaProcessos, {
            ...getCommonMountOptions({}, {
                BTable: {
                    template: "<table><slot></slot></table>",
                    emits: ["update:sort-by"],
                }
            }),
            props: {
                processos: [],
                criterioOrdenacao: "descricao",
                direcaoOrdenacaoAsc: true,
            },
        });

        await (
            context.wrapper.findComponent(_BTable as any) as any
        ).vm.$emit("update:sort-by", [{key: "tipo", order: "asc"}]);

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
                    props: ['items', 'tbodyTrAttr'],
                    template: `
                        <table>
                            <tbody>
                                <tr v-for="item in items"
                                    :key="item.codigo"
                                    v-bind="tbodyTrAttr ? tbodyTrAttr(item, 'row') : {}">
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
        await rows[0].trigger("keydown", { key: "Enter" });

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
                situacao: "OUTRA_SITUACAO" as any,
                tipo: "OUTRO_TIPO" as any,
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
        const rows = context.wrapper.findAll("tbody tr");

        const cells3 = rows[0].findAll("td");
        expect(cells3[1].text()).toBe("Diagnóstico");
        expect(cells3[3].text()).toContain("Criado");

        const cells4 = rows[1].findAll("td");
        expect(cells4[1].text()).toBe("OUTRO_TIPO");
        expect(cells4[3].text()).toContain("OUTRA_SITUACAO");
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
                components: { EmptyState }
            },
            props: {
                processos: [],
                criterioOrdenacao: "descricao",
                direcaoOrdenacaoAsc: true,
            },
        });

        expect(context.wrapper.text()).toContain("Nenhum processo encontrado");
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

    describe("Modo Compacto", () => {
        it("deve exibir o rótulo reduzido 'Unidades' quando compacto é true", async () => {
            // Need real BTable to check headers
            context.wrapper = mount(TabelaProcessos, {
                ...getCommonMountOptions(),
                props: {
                    processos: [],
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
                    processos: [],
                    criterioOrdenacao: "descricao",
                    direcaoOrdenacaoAsc: true,
                    compacto: true,
                },
            });

            const table = context.wrapper.findComponent(_BTable as any) as any;
            const fields = (table.props("fields") as any[]);
            const desc = fields.find(f => f.key === "descricao");
            expect(desc.sortable).toBe(true);
        });

        it("deve emitir evento de ordenação ao clicar no cabeçalho em modo compacto", async () => {
            context.wrapper = mount(TabelaProcessos, {
                ...getCommonMountOptions({}, {
                    BTable: {
                        template: "<table><slot></slot></table>",
                        emits: ["update:sort-by"],
                    }
                }),
                props: {
                    processos: [],
                    criterioOrdenacao: "descricao",
                    direcaoOrdenacaoAsc: true,
                    compacto: true,
                },
            });

            await (
                context.wrapper.findComponent(_BTable as any) as any
            ).vm.$emit("update:sort-by", [{key: "tipo", order: "asc"}]);

            expect(context.wrapper.emitted("ordenar")).toBeTruthy();
            expect(context.wrapper.emitted("ordenar")![0]).toEqual(["tipo"]);
        });
    });
});
