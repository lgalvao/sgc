import {describe, expect, it, vi} from "vitest";
import {mount, VueWrapper} from "@vue/test-utils";
import TabelaMovimentacoes from "../TabelaMovimentacoes.vue";
import type {Movimentacao, Unidade} from "@/types/tipos";
import {BTable as _BTable} from "bootstrap-vue-next";
import {setupComponentTest} from "@/test-utils/componentTestHelpers";

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
    setupComponentTest();
    it("deve renderizar a tabela com movimentações (verificar BTable)", () => {
        const wrapper = mount(TabelaMovimentacoes, {
            props: {movimentacoes: mockMovimentacoes},
            global: {stubs: {BTable: true}}
        });

        const bTable = wrapper.findComponent(_BTable as any) as unknown as VueWrapper<any>;
        expect(bTable.exists()).toBe(true);
        expect(bTable.props("items")).toEqual(mockMovimentacoes);
    });

    it("deve renderizar mensagem quando não houver movimentações", () => {
        const wrapper = mount(TabelaMovimentacoes, {
            props: {movimentacoes: []},
            global: {
                // When stubbing BTable, we need to manually render the slot content
                // because mount won't render slots inside a stub automatically unless configured
                stubs: {
                    BTable: {
                        props: ['items', 'showEmpty'],
                        template: `
                            <div>
                                <slot name="empty" v-if="showEmpty === '' || showEmpty === true && (!items || items.length === 0)"></slot>
                                <slot v-else></slot>
                            </div>
                        `
                    }
                }
            }
        });

        expect(wrapper.text()).toContain("Nenhuma movimentação");
        expect(wrapper.text()).toContain("O histórico de movimentações deste processo aparecerá aqui.");
        expect(wrapper.find('[data-testid="empty-state-movimentacoes"]').exists()).toBe(true);
    });

    it("deve passar a função rowAttr correta", () => {
        const wrapper = mount(TabelaMovimentacoes, {
            props: {movimentacoes: mockMovimentacoes},
            global: {stubs: {BTable: true}}
        });

        const bTable = wrapper.findComponent(_BTable as any) as unknown as VueWrapper<any>;
        const rowAttrFn = bTable.props("tbodyTrAttr") || bTable.vm.$attrs["tbody-tr-attr"];

        expect(typeof rowAttrFn).toBe("function");

        expect(rowAttrFn(mockMovimentacoes[0], "row")).toEqual({'data-testid': 'row-movimentacao-1'});
        expect(rowAttrFn(null, "row")).toEqual({});
    });

    it("deve renderizar conteúdo dos slots corretamente (formatação de data e sigla)", () => {
        // Usar mount sem stub do BTable para renderizar slots pode ser pesado.
        // Alternativa: Stub funcional que renderiza os slots.
        const wrapper = mount(TabelaMovimentacoes, {
            props: {movimentacoes: mockMovimentacoes},
            global: {
                stubs: {
                    BTable: {
                        props: ['items', 'fields'],
                        template: `
                          <table>
                          <tbody>
                          <tr v-for="item in items" :key="item.codigo">
                            <td>
                              <slot name="cell(dataHora)" :item="item" :value="item.dataHora"></slot>
                            </td>
                            <td>
                              <slot name="cell(unidadeOrigem)" :item="item" :value="item.unidadeOrigem"></slot>
                            </td>
                            <td>
                              <slot name="cell(unidadeDestino)" :item="item" :value="item.unidadeDestino"></slot>
                            </td>
                          </tr>
                          </tbody>
                          </table>
                        `
                    }
                }
            }
        });

        expect(wrapper.text()).toContain("Formatted 2024-01-01T10:00:00Z");
        expect(wrapper.text()).toContain("ORG");
        expect(wrapper.text()).toContain("DST");
    });

    it("deve renderizar hífen quando unidade for nula nos slots", () => {
        const movSemUnidade: Movimentacao = {
            ...mockMovimentacoes[0],
            codigo: 2,
            unidadeOrigem: null as any,
            unidadeDestino: null as any
        };

        const wrapper = mount(TabelaMovimentacoes, {
            props: {movimentacoes: [movSemUnidade]},
            global: {
                stubs: {
                    BTable: {
                        props: ['items'],
                        template: `
                          <table>
                          <tbody>
                          <tr v-for="item in items" :key="item.codigo">
                            <td>
                              <slot name="cell(unidadeOrigem)" :item="item" :value="item.unidadeOrigem"></slot>
                            </td>
                            <td>
                              <slot name="cell(unidadeDestino)" :item="item" :value="item.unidadeDestino"></slot>
                            </td>
                          </tr>
                          </tbody>
                          </table>
                        `
                    }
                }
            }
        });

        // Verifica se renderizou dois hifens (um para origem, um para destino)
        const cells = wrapper.findAll('td');
        expect(cells[0].text()).toBe("-");
        expect(cells[1].text()).toBe("-");
    });

    it("deve renderizar badge de situação do subprocesso", () => {
        const wrapper = mount(TabelaMovimentacoes, {
            props: {movimentacoes: mockMovimentacoes},
            global: {
                stubs: {
                    BTable: {
                        props: ['items'],
                        template: `
                          <table>
                          <tbody>
                          <tr v-for="item in items" :key="item.codigo">
                            <td>
                              <slot name="cell(situacao)" :item="item"></slot>
                            </td>
                          </tr>
                          </tbody>
                          </table>
                        `
                    }
                }
            }
        });

        const badge = wrapper.find('[data-testid="badge-situacao"]');
        expect(badge.exists()).toBe(true);
        expect(badge.text()).toContain("NAO INICIADO");
    });
});
