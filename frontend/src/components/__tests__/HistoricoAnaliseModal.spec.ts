import {mount} from "@vue/test-utils";
import {createPinia, setActivePinia} from "pinia";
import {beforeEach, describe, expect, it, vi} from "vitest";
import {formatDateBR} from "@/utils";
import HistoricoAnaliseModal from "../HistoricoAnaliseModal.vue";

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

vi.mock("@/stores/analises", () => ({
    useAnalisesStore: vi.fn(() => ({
        obterAnalisesPorSubprocesso: (codSubprocesso: number) => {
            return codSubprocesso === 1 ? mockAnalises : [];
        },
        buscarAnalisesCadastro: vi.fn().mockResolvedValue(undefined), // Adicionado o mock para buscarAnalisesCadastro
        isLoading: false,
        clearError: vi.fn(),
    })),
}));

describe("HistoricoAnaliseModal", () => {
    beforeEach(() => {
        setActivePinia(createPinia());
    });

    it("não deve renderizar o modal quando mostrar for falso", () => {
        const wrapper = mount(HistoricoAnaliseModal, {
            props: {
                mostrar: false,
                codSubprocesso: 1,
            },
        });
        expect(wrapper.find('[data-testid="modal-historico-body"]').exists()).toBe(
            false,
        );
    });

    it('deve renderizar a mensagem de "nenhuma análise" quando não houver análises', () => {
        const wrapper = mount(HistoricoAnaliseModal, {
            props: {
                mostrar: true,
                codSubprocesso: 2,
            },
        });

        expect(wrapper.find(".alert-info").text()).toContain(
            "Nenhuma análise registrada",
        );
    });

    it("deve renderizar a tabela com as análises", async () => {
        const wrapper = mount(HistoricoAnaliseModal, {
            props: {
                mostrar: true,
                codSubprocesso: 1,
            },
        });

        await wrapper.vm.$nextTick(); // Aguarda a atualização do DOM

        const rows = wrapper.findAll("tbody tr");
        expect(rows.length).toBe(mockAnalises.length);
        const expectedDate = formatDateBR(new Date(mockAnalises[0].dataHora));
        expect(rows[0].text()).toContain(expectedDate);
        expect(rows[0].text()).toContain("TEST");
        expect(rows[0].text()).toContain("APROVADO");
        expect(rows[0].text()).toContain("Tudo certo.");
    });

    it("deve emitir o evento fechar ao clicar no botão de fechar", async () => {
        const wrapper = mount(HistoricoAnaliseModal, {
            props: {
                mostrar: true,
                codSubprocesso: 1,
            },
        });

        await wrapper.find('[data-testid="btn-modal-fechar"]').trigger("click");
        expect(wrapper.emitted("fechar")).toBeTruthy();
    });
});
