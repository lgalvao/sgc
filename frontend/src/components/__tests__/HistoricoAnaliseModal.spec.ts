import {mount} from "@vue/test-utils";
import {describe, expect, it} from "vitest";
import {formatDateBR} from "@/utils";
import HistoricoAnaliseModal from "../processo/HistoricoAnaliseModal.vue";
import {getCommonMountOptions, setupComponentTest} from "@/test-utils/componentTestHelpers";

const mockAnalises: any[] = [
    {
        dataHora: "2024-01-01T12:00:00Z",
        unidadeSigla: "TEST",
        unidadeNome: "Unidade Teste",
        acao: "ACEITE_MAPEAMENTO",
        analistaUsuarioTitulo: "123456",
        observacoes: "Tudo certo.",
        motivo: "",
        tipo: "CADASTRO"
    },
    {
        dataHora: "2024-01-02T14:30:00Z",
        unidadeSigla: "TEST2",
        unidadeNome: "Unidade Teste 2",
        acao: "DEVOLUCAO_MAPEAMENTO",
        analistaUsuarioTitulo: "654321",
        observacoes: "Faltou informação.",
        motivo: "Incompleto",
        tipo: "CADASTRO"
    },
];

describe("HistoricoAnaliseModal", () => {
    const context = setupComponentTest();

    it("não deve renderizar o modal quando mostrar for falso", () => {
        context.wrapper = mount(HistoricoAnaliseModal, {
            ...getCommonMountOptions(),
            props: {
                mostrar: false,
                historico: [],
            },
        });
        expect(context.wrapper.find('[data-testid="modal-historico-body"]').exists()).toBe(
            false,
        );
    });

    it('deve renderizar a mensagem de "nenhuma análise" quando não houver análises', () => {
        context.wrapper = mount(HistoricoAnaliseModal, {
            ...getCommonMountOptions(),
            props: {
                mostrar: true,
                historico: [],
            },
        });

        expect(context.wrapper.find(".alert-info").text()).toContain(
            "Nenhuma análise registrada",
        );
    });

    it("deve renderizar a tabela com as análises", async () => {
        context.wrapper = mount(HistoricoAnaliseModal, {
            ...getCommonMountOptions(),
            props: {
                mostrar: true,
                historico: mockAnalises,
            },
        });

        const rows = context.wrapper.findAll("tbody tr");
        expect(rows.length).toBe(mockAnalises.length);
        const expectedDate = formatDateBR(new Date(mockAnalises[0].dataHora));
        expect(rows[0].text()).toContain(expectedDate);
        expect(rows[0].text()).toContain("TEST");
        expect(rows[0].text()).toContain("ACEITE_MAPEAMENTO");
        expect(rows[0].text()).toContain("123456");
        expect(rows[0].text()).toContain("Tudo certo.");
    });

    it("deve emitir o evento fechar ao clicar no botão de fechar", async () => {
        context.wrapper = mount(HistoricoAnaliseModal, {
            ...getCommonMountOptions(),
            props: {
                mostrar: true,
                historico: [],
            },
        });

        await context.wrapper.find('[data-testid="btn-modal-fechar"]').trigger("click");
        expect(context.wrapper.emitted("fechar")).toBeTruthy();
    });
});
