import {beforeEach, describe, expect, it, vi} from "vitest";
import {flushPromises, mount} from "@vue/test-utils";
import {createTestingPinia} from "@pinia/testing";
import {createMemoryHistory, createRouter} from "vue-router";
import FeedbacksAdminView from "../FeedbacksAdminView.vue";
import {listarFeedbacksAdmin} from "@/services/feedbackAdminService";

vi.mock("@/services/feedbackAdminService", () => ({
    listarFeedbacksAdmin: vi.fn(),
    obterUrlScreenshot: vi.fn().mockReturnValue("http://localhost:8080/api/feedback/abc/screenshot"),
}));

const router = createRouter({
    history: createMemoryHistory(),
    routes: [{path: "/", component: {}}]
});

describe("FeedbacksAdminView", () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    function montarComponente() {
        return mount(FeedbacksAdminView, {
            global: {
                plugins: [createTestingPinia({createSpy: vi.fn}), router],
                stubs: {
                    LayoutPadrao: {template: "<div><slot/></div>"},
                    PageHeader: {template: "<div><slot name='actions'/></div>", props: ["title"]},
                    EmptyState: {template: "<div class='empty-state-stub'></div>", props: ["title", "description", "icon"]},
                    BButton: {template: "<button @click=\"$emit('click')\"><slot/></button>", props: ["disabled", "variant", "size"]},
                    BAlert: {template: "<div><slot/></div>", props: ["modelValue", "variant"]},
                    BSpinner: true,
                    BBadge: {template: "<span><slot/></span>", props: ["variant"]},
                    BTable: {
                        template: `
                          <table data-testid="tbl-feedbacks">
                            <tr v-for="item in items" :key="item.codigo">
                              <slot name="cell(tipo)" :item="item" />
                              <slot name="cell(usuarioNome)" :item="item" />
                              <slot name="cell(nota)" :item="item" />
                              <slot name="cell(enviadoEm)" :item="item" />
                              <slot name="cell(acoes)" :item="item" />
                            </tr>
                          </table>`,
                        props: ["fields", "items"]
                    },
                    BModal: {template: "<div v-if='modelValue' data-testid='modal-detalhes-feedback'><slot/></div>", props: ["modelValue", "title"]},
                }
            }
        });
    }

    it("renderiza a lista de feedbacks", async () => {
        vi.mocked(listarFeedbacksAdmin).mockResolvedValue([
            {
                codigo: "abc",
                tipo: "BUG",
                nota: "Existe um erro na tela de painel",
                metadataJson: null,
                caminhoScreenshot: null,
                screenshotDisponivel: false,
                usuarioCodigo: "123",
                usuarioNome: "João",
                enviadoEm: "2026-05-04T10:00:00Z",
                rota: "/painel",
                status: "NOVO"
            }
        ] as any);

        const wrapper = montarComponente();
        await flushPromises();

        expect(wrapper.find("[data-testid='tbl-feedbacks']").exists()).toBe(true);
        expect(wrapper.text()).toContain("Bug");
        expect(wrapper.text()).toContain("João");
        expect(wrapper.text()).toContain("Existe um erro na tela de painel");
    });

    it("limpa tags HTML e entidades da descrição na listagem", async () => {
        vi.mocked(listarFeedbacksAdmin).mockResolvedValue([
            {
                codigo: "html",
                tipo: "QUESTAO",
                nota: "<p>Dúvida sobre <strong>acesso</strong>&nbsp;ao sistema.</p>",
                usuarioNome: "Maria",
                enviadoEm: "2026-05-04T11:00:00Z",
                rota: "/home"
            }
        ] as any);

        const wrapper = montarComponente();
        await flushPromises();

        expect(wrapper.text()).toContain("Dúvida sobre acesso ao sistema.");
        expect(wrapper.text()).not.toContain("<p>");
        expect(wrapper.text()).not.toContain("&nbsp;");
    });

    it("abre modal de detalhes e exibe metadados formatados e captura", async () => {
        vi.mocked(listarFeedbacksAdmin).mockResolvedValue([
            {
                codigo: "abc",
                tipo: "SUGESTAO",
                nota: "Melhorar <strong>contraste</strong>",
                metadataJson: "{\"userAgent\":\"Mozilla/5.0\",\"rotaCaminho\":\"/painel\"}",
                caminhoScreenshot: "c:/tmp/a.webp",
                screenshotDisponivel: true,
                usuarioCodigo: "123",
                usuarioNome: "João",
                enviadoEm: "2026-05-04T10:00:00Z",
                rota: "/painel",
                status: "NOVO"
            }
        ] as any);

        const wrapper = montarComponente();
        await flushPromises();
        await wrapper.find("[data-testid='btn-feedback-detalhes-abc']").trigger("click");

        const modal = wrapper.find("[data-testid='modal-detalhes-feedback']");
        expect(modal.exists()).toBe(true);
        expect(modal.html()).toContain("Melhorar <strong>contraste</strong>");
        expect(modal.text()).not.toContain("NOVO"); // Status removido
        
        // Verifica se metadados estão na tabela
        expect(modal.text()).toContain("userAgent");
        expect(modal.text()).toContain("Mozilla/5.0");
        
        // Verifica se a captura é exibida
        const img = modal.find("img");
        expect(img.exists()).toBe(true);
        expect(img.attributes("src")).toBe("http://localhost:8080/api/feedback/abc/screenshot");
    });

    it("exibe erro ao falhar no carregamento", async () => {
        vi.mocked(listarFeedbacksAdmin).mockRejectedValue(new Error("Erro ao listar"));

        const wrapper = montarComponente();
        await flushPromises();

        expect(wrapper.text()).toContain("Erro ao listar");
    });
});
