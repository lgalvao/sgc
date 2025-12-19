import { mount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";
import { formatDateBR } from "@/utils";
import HistoricoAnaliseModal from "../HistoricoAnaliseModal.vue";
import { setupComponentTest, getCommonMountOptions } from "@/test-utils/componentTestHelpers";

const mockAnalises = [
    {
        dataHora: "2024-01-01T12:00:00Z",
        unidadeSigla: "TEST",
        resultado: "APROVADO",
        observacoes: "Tudo certo.",
    },
    {
        dataHora: "2024-01-02T14:30:00Z",
        unidade: "TEST2",
        resultado: "REPROVADO",
        observacoes: "Faltou informação.",
    },
];

describe("HistoricoAnaliseModal", () => {
    const context = setupComponentTest();

    it("não deve renderizar o modal quando mostrar for falso", () => {
        context.wrapper = mount(HistoricoAnaliseModal, {
            ...getCommonMountOptions(),
            props: {
                mostrar: false,
                codSubprocesso: 1,
            },
        });
        expect(context.wrapper.find('[data-testid="modal-historico-body"]').exists()).toBe(
            false,
        );
    });

    it('deve renderizar a mensagem de "nenhuma análise" quando não houver análises', () => {
        const mapAnalises = new Map();

        context.wrapper = mount(HistoricoAnaliseModal, {
            ...getCommonMountOptions({
                analises: {
                    analisesPorSubprocesso: mapAnalises,
                }
            }),
            props: {
                mostrar: true,
                codSubprocesso: 2,
            },
        });

        expect(context.wrapper.find(".alert-info").text()).toContain(
            "Nenhuma análise registrada",
        );
    });

    it("deve renderizar a tabela com as análises", async () => {
        const mapAnalises = new Map();
        mapAnalises.set(1, mockAnalises);

        context.wrapper = mount(HistoricoAnaliseModal, {
            ...getCommonMountOptions({
                analises: {
                    analisesPorSubprocesso: mapAnalises,
                }
            }),
            props: {
                mostrar: true,
                codSubprocesso: 1,
            },
        });

        await context.wrapper.vm.$nextTick(); // Aguarda a atualização do DOM

        const rows = context.wrapper.findAll("tbody tr");
        expect(rows.length).toBe(mockAnalises.length);
        const expectedDate = formatDateBR(new Date(mockAnalises[0].dataHora));
        expect(rows[0].text()).toContain(expectedDate);
        expect(rows[0].text()).toContain("TEST");
        expect(rows[0].text()).toContain("APROVADO");
        expect(rows[0].text()).toContain("Tudo certo.");
    });

    it("deve emitir o evento fechar ao clicar no botão de fechar", async () => {
        context.wrapper = mount(HistoricoAnaliseModal, {
            ...getCommonMountOptions(),
            props: {
                mostrar: true,
                codSubprocesso: 1,
            },
        });

        await context.wrapper.find('[data-testid="btn-modal-fechar"]').trigger("click");
        expect(context.wrapper.emitted("fechar")).toBeTruthy();
    });
});
