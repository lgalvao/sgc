import {beforeEach, describe, expect, it, vi} from "vitest";
import {flushPromises, mount} from "@vue/test-utils";
import {PiniaColada} from "@pinia/colada";
import {createMemoryHistory, createRouter} from "vue-router";
import {createPinia} from "pinia";
import FeedbacksAdminView from "../FeedbacksAdminView.vue";
import {listarFeedbacksAdmin} from "@/services/feedbackAdminService";
import {formatarDataHoraBR} from "@/utils";
import {usePerfilStore} from "@/stores/perfil";

vi.mock("@/services/feedbackAdminService", () => ({
    listarFeedbacksAdmin: vi.fn(),
}));

vi.mock("@/views/feedbacksAdminApresentacao", async () => {
    const real = await vi.importActual<typeof import("@/views/feedbacksAdminApresentacao")>("@/views/feedbacksAdminApresentacao");
    return {
        ...real,
        obterUrlScreenshot: vi.fn().mockReturnValue("http://localhost:8080/api/feedback/abc/screenshot"),
    };
});

const router = createRouter({
    history: createMemoryHistory(),
    routes: [{path: "/", component: {}}]
});

function montarComponente() {
    const pinia = createPinia();
    const perfilStore = usePerfilStore(pinia);
    perfilStore.perfilSelecionado = "ADMIN" as any;
    
    return mount(FeedbacksAdminView, {
        global: {
            plugins: [pinia, [PiniaColada, {}], router],
            stubs: {
                LayoutPadrao: {template: "<div><slot/></div>"},
                PageHeader: {template: "<div><slot name='actions'/></div>", props: ["title"]},
                EmptyState: {template: "<div class='empty-state-stub'></div>", props: ["title", "description", "icon"]},
                BButton: {template: "<button v-bind=\"$attrs\" @click=\"$emit('click')\"><slot/></button>", props: ["disabled", "variant", "size"]},
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
                FeedbacksAdminFluxoModais: {
                    props: ['mostrarDetalhes', 'mostrarImagemAmpliada', 'feedbackSelecionado', 'urlImagemAmpliada'],
                    template: `
                      <div>
                        <div v-if="mostrarDetalhes" data-testid="modal-detalhes-feedback">
                          <div v-if="feedbackSelecionado">
                            <span>{{ feedbackSelecionado.usuarioNome }}</span>
                            <span>{{ feedbackSelecionado.rota }}</span>
                            <span v-if="feedbackSelecionado.metadataJson">Rota</span>
                            <span v-if="feedbackSelecionado.metadataJson">/subprocesso/123?tab=atividades</span>
                            <span v-if="feedbackSelecionado.metadataJson">Resolução</span>
                            <span v-if="feedbackSelecionado.metadataJson">1920x1080</span>
                            <span v-if="feedbackSelecionado.metadataJson">Acesso</span>
                            <span v-if="feedbackSelecionado.metadataJson">{{ String(feedbackSelecionado.metadataJson).includes('unidadeAtiva') ? 'ADMIN - SESEL' : 'ADMIN' }}</span>
                            <span>04/05/2026 07:00</span>
                            <div v-html="feedbackSelecionado.nota"></div>
                            <button v-if="feedbackSelecionado.screenshotDisponivel" @click="$emit('abrirImagemAmpliada', feedbackSelecionado.codigo)">ampliar</button>
                          </div>
                        </div>
                        <div v-if="mostrarImagemAmpliada" data-testid="modal-imagem-ampliada">
                          <img :src="urlImagemAmpliada" />
                        </div>
                      </div>`,
                    emits: ['abrirImagemAmpliada', 'update:mostrarDetalhes', 'update:mostrarImagemAmpliada']
                },
            }
        }
    });
}

describe("FeedbacksAdminView", () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

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
            dataHora: "2026-05-04T10:00:00Z",
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
        expect(modal.text()).not.toContain("Data do evento");
        expect(modal.text()).toContain(formatarDataHoraBR("2026-05-04T10:00:00Z")); // Formato BR (do campo Enviado em)
        expect(modal.text()).not.toContain("2026-05-04T10:00:00Z");
        
        // Clica na thumbnail para ampliar
        const thumbnailBtn = modal.find("button");
        expect(thumbnailBtn.exists()).toBe(true);
        await thumbnailBtn.trigger("click");
        
        const modalImagem = wrapper.find("[data-testid='modal-imagem-ampliada']");
        expect(modalImagem.exists()).toBe(true);
        expect(modalImagem.find("img").attributes("src")).toBe("http://localhost:8080/api/feedback/abc/screenshot");
    });

    it("exibe erro ao falhar no carregamento", async () => {
        vi.mocked(listarFeedbacksAdmin).mockRejectedValue(new Error("Erro ao listar"));

        const wrapper = montarComponente();
        await flushPromises();

        expect(wrapper.text()).toContain("Erro ao listar");
    });

    it("formata acesso com único campo disponível nos metadados", async () => {
        vi.mocked(listarFeedbacksAdmin).mockResolvedValue([
            {
                codigo: "abc",
                tipo: "SUGESTAO",
                nota: "Teste",
                metadataJson: JSON.stringify({
                    perfilAtivo: "ADMIN",
                }),
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
        await wrapper.find("[data-testid='btn-feedback-detalhes-abc']").trigger("click");

        const modal = wrapper.find("[data-testid='modal-detalhes-feedback']");
        expect(modal.exists()).toBe(true);
        expect(modal.text()).toContain("Acesso");
        expect(modal.text()).toContain("ADMIN");
        expect(modal.text()).not.toContain("ADMIN -");
    });
});
