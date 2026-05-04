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
        metadataJson: JSON.stringify({
            userAgent: "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/147.0.0.0 Safari/537.36",
            rotaNome: "Painel",
            rotaCaminho: "/subprocesso/123",
            rotaQuery: JSON.stringify({tab: "atividades"}),
            dataEvento: "2026-05-04T10:00:00Z",
            larguraTela: 1920,
            alturaTela: 1080,
            fusoHorario: -180,
            perfilAtivo: "ADMIN",
            unidadeAtiva: "SESEL"
        }),
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
        
        expect(modal.text()).not.toContain("Mozilla/5.0");
        expect(modal.text()).not.toContain("rotaNome");
        expect(modal.text()).toContain("Rota");
        expect(modal.text()).toContain("/subprocesso/123?tab=atividades");
        expect(modal.text()).toContain("Resolução");
        expect(modal.text()).toContain("1920x1080");
        expect(modal.text()).not.toContain("fusoHorario");
        expect(modal.text()).toContain("Acesso");
        expect(modal.text()).toContain("ADMIN - SESEL");
        expect(modal.text()).toContain("04/05/2026 07:00"); // Formato BR
        expect(modal.text()).not.toContain("2026-05-04T10:00:00Z");
        
        // Clica na thumbnail para ampliar
        const thumbnailBtn = modal.find("button");
        expect(thumbnailBtn.exists()).toBe(true);
        await thumbnailBtn.trigger("click");
        
        expect(wrapper.vm.mostrarImagemAmpliada).toBe(true);
        expect(wrapper.vm.urlImagemAmpliada).toBe("http://localhost:8080/api/feedback/abc/screenshot");
    });

    it("exibe erro ao falhar no carregamento", async () => {
        vi.mocked(listarFeedbacksAdmin).mockRejectedValue(new Error("Erro ao listar"));

        const wrapper = montarComponente();
        await flushPromises();

        expect(wrapper.text()).toContain("Erro ao listar");
    });
});
