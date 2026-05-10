import {mount} from "@vue/test-utils";
import {describe, expect, it} from "vitest";
import {formatarDataBR} from "@/utils";
import HistoricoAnaliseModal from "../processo/HistoricoAnaliseModal.vue";
import {getCommonMountOptions, setupComponentTest} from "@/test-utils/componentTestHelpers";

const mockAnalises: any[] = [
    {
        dataHora: "2024-01-01T12:00:00Z",
        unidadeSigla: "TEST",
        unidadeNome: "Unidade teste",
        acao: "ACEITE_MAPEAMENTO",
        acaoDescricao: "Aceite",
        analistaUsuarioTitulo: "123456",
        usuarioNome: "Usuário com nome razoavelmente longo para truncamento visual",
        observacoes: "Tudo certo.",
        motivo: "",
        tipo: "CADASTRO"
    },
    {
        dataHora: "2024-01-02T14:30:00Z",
        unidadeSigla: "TEST2",
        unidadeNome: "Unidade teste 2",
        acao: "DEVOLUCAO_MAPEAMENTO",
        acaoDescricao: "Devolução",
        analistaUsuarioTitulo: "654321",
        usuarioNome: "Usuário Dois",
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

        expect(context.wrapper.find('[data-testid="alert-historico-vazio"]').text()).toContain(
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
        const expectedDate = formatarDataBR(new Date(mockAnalises[0].dataHora));
        expect(rows[0].text()).toContain(expectedDate);
        expect(rows[0].text()).toContain("TEST");
        expect(rows[0].text()).toContain("Aceite");
        expect(rows[0].text()).toContain("Usuário com nome razoavelmente longo para truncamento visual");
        expect(rows[0].text()).toContain("Tudo certo.");
        expect(rows[1].text()).toContain("Devolução");
        expect(context.wrapper.find('[data-testid="cell-usuario-0"]').attributes("title"))
            .toContain("Usuário com nome razoavelmente longo");
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
        expect(context.wrapper.emitted("fechar")).toBeDefined();
    });
});
