import {describe, expect, it} from "vitest";
import {mount} from "@vue/test-utils";
import SubprocessoMovimentacoes from "../SubprocessoMovimentacoes.vue";

describe("SubprocessoMovimentacoes", () => {
    it("renderiza EmptyState quando não há movimentações", () => {
        const wrapper = mount(SubprocessoMovimentacoes, {
            props: {movimentacoes: []},
            global: {
                stubs: {
                    BTable: {
                        template: "<div><slot name='empty' /></div>"
                    },
                    EmptyState: {
                        template: "<div class='empty-stub'>{{ title }}</div>",
                        props: ["title"]
                    }
                }
            }
        });

        expect(wrapper.find(".empty-stub").exists()).toBe(true);
        // Verificamos apenas parte do texto para evitar fragilidade com constantes
        expect(wrapper.text().toLowerCase()).toContain("movimenta");
    });

    it("renderiza linhas da tabela com dados formatados", () => {
        const movimentacoes = [
            {
                codigo: 1,
                dataHora: "2026-05-24T10:00:00Z",
                unidadeOrigemSigla: "ORG",
                unidadeDestinoSigla: "DST",
                descricao: "Teste"
            }
        ];

        const wrapper = mount(SubprocessoMovimentacoes, {
            props: {movimentacoes: movimentacoes as any},
            global: {
                stubs: {
                    BTable: {
                        template: `
                    <div class="tbl-stub">
                      <div v-for="item in items" :key="item.codigo">
                        <slot name="cell(dataHora)" :item="item" />
                        <slot name="cell(unidadeOrigem)" :item="item" />
                        <slot name="cell(unidadeDestino)" :item="item" />
                      </div>
                    </div>`,
                        props: ["items"]
                    }
                }
            }
        });

        const content = wrapper.find(".tbl-stub").text();
        expect(content).toContain("24/05/2026 07:00");
        expect(content).toContain("ORG");
        expect(content).toContain("DST");
    });

    it("exibe '-' quando siglas são nulas", () => {
        const wrapper = mount(SubprocessoMovimentacoes, {
            props: {movimentacoes: [{codigo: 1, dataHora: "2026-01-01T10:00:00Z"}] as any},
            global: {
                stubs: {
                    BTable: {
                        template: `<div class="tbl-stub"><slot name="cell(unidadeOrigem)" :item="items[0]" /></div>`,
                        props: ["items"]
                    }
                }
            }
        });
        expect(wrapper.find(".tbl-stub").text()).toBe("-");
    });
});
