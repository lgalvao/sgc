import {describe, expect, it} from "vitest";
import {mount} from "@vue/test-utils";
import VisAtividadeItem from "../VisAtividadeItem.vue";

describe("VisAtividadeItem.vue", () => {
    const mockAtividade = {
        codigo: 1,
        descricao: "Atividade de Teste",
        conhecimentos: [
            { codigo: 10, descricao: "Conhecimento 1" },
            { codigo: 11, descricao: "Conhecimento 2" }
        ]
    };

    it("deve renderizar a atividade e seus conhecimentos", () => {
        const wrapper = mount(VisAtividadeItem, {
            props: {
                atividade: mockAtividade
            },
            global: {
                stubs: {
                    BCard: { template: '<div><slot /></div>' },
                    BCardBody: { template: '<div><slot /></div>' }
                }
            }
        });

        expect(wrapper.find('[data-testid="txt-atividade-descricao"]').text()).toBe("Atividade de Teste");
        const conhecimentos = wrapper.findAll('[data-testid="txt-conhecimento-descricao"]');
        expect(conhecimentos).toHaveLength(2);
        expect(conhecimentos[0].text()).toBe("Conhecimento 1");
        expect(conhecimentos[1].text()).toBe("Conhecimento 2");
    });

    it("deve renderizar corretamente mesmo sem conhecimentos", () => {
        const wrapper = mount(VisAtividadeItem, {
            props: {
                atividade: { ...mockAtividade, conhecimentos: [] }
            },
            global: {
                stubs: {
                    BCard: { template: '<div><slot /></div>' },
                    BCardBody: { template: '<div><slot /></div>' }
                }
            }
        });

        expect(wrapper.find('[data-testid="txt-atividade-descricao"]').text()).toBe("Atividade de Teste");
        expect(wrapper.findAll('[data-testid="txt-conhecimento-descricao"]')).toHaveLength(0);
    });
});
