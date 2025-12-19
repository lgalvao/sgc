import {mount} from "@vue/test-utils";
import {BBreadcrumbItem, BButton} from "bootstrap-vue-next";
import {beforeEach, describe, expect, it, vi} from "vitest";
import {useRoute} from "vue-router";
import {Perfil} from "@/types/tipos";
import BarraNavegacao from "../BarraNavegacao.vue";
import {getCommonMountOptions, setupComponentTest} from "@/test-utils/componentTestHelpers";

// Mock vue-router
const mockRouter = {
    back: vi.fn(),
};
vi.mock("vue-router", () => ({
    useRoute: vi.fn(),
    useRouter: () => mockRouter,
    createRouter: vi.fn(() => ({
        beforeEach: vi.fn(),
        afterEach: vi.fn(),
    })),
    createWebHistory: vi.fn(),
    createMemoryHistory: vi.fn(),
}));

// Helper to create mock routes
const createMockRoute = (path: string, matched: any[], routeName = "", params: Record<string, string> = {}) => ({
    path,
    matched,
    params,
    name: routeName,
    fullPath: path,
    query: {},
    hash: "",
    meta: {},
    redirectedFrom: undefined,
});

const mockMatchedProcesso = [
    {name: "Painel", meta: {}},
    {name: "Processo", meta: {}},
];

const mockMatchedSubprocesso = [
    {name: "Painel", meta: {}},
    {name: "Subprocesso", meta: {}},
];

const mockMatchedUnidade = [
    {name: "Painel", meta: {}},
    {name: "Unidade", meta: {breadcrumb: "Minha unidade"}},
];

describe("BarraNavegacao.vue", () => {
    setupComponentTest();

    beforeEach(() => {
        vi.clearAllMocks();
    });

    describe("Visibilidade dos Elementos", () => {
        it("não deve exibir o botão de voltar e os breadcrumbs na página de login", () => {
            vi.mocked(useRoute).mockReturnValue(createMockRoute("/login", []));
            const wrapper = mount(
                BarraNavegacao,
                getCommonMountOptions({}, {BButton}),
            );
            expect(wrapper.find("button").exists()).toBe(false);
            expect(wrapper.find('[data-testid="nav-breadcrumbs"]').exists()).toBe(false);
        });

        it("não deve exibir o botão de voltar e os breadcrumbs na página do painel", () => {
            vi.mocked(useRoute).mockReturnValue(createMockRoute("/painel", []));
            const wrapper = mount(
                BarraNavegacao,
                getCommonMountOptions({}, {BButton}),
            );
            expect(wrapper.find("button").exists()).toBe(false);
            expect(wrapper.find('[data-testid="nav-breadcrumbs"]').exists()).toBe(false);
        });

        it("deve exibir o botão de voltar e os breadcrumbs em outras páginas", () => {
            vi.mocked(useRoute).mockReturnValue(
                createMockRoute("/processo/123", mockMatchedProcesso, "Processo", {codProcesso: "123"}),
            );
            const wrapper = mount(
                BarraNavegacao,
                getCommonMountOptions({}, {BButton}),
            );
            expect(wrapper.find("button").exists()).toBe(true);
            expect(wrapper.find('[data-testid="nav-breadcrumbs"]').exists()).toBe(true);
        });
    });

    describe("Renderização dos Breadcrumbs", () => {
        it("deve renderizar breadcrumbs para rota de Processo", () => {
            vi.mocked(useRoute).mockReturnValue(
                createMockRoute("/processo/123", mockMatchedProcesso, "Processo", {codProcesso: "123"}),
            );
            const wrapper = mount(BarraNavegacao, getCommonMountOptions({
                perfil: {perfilSelecionado: Perfil.ADMIN}
            }, {BButton}));

            const items = wrapper.findAllComponents(BBreadcrumbItem);
            expect(items).toHaveLength(2); // Home + "Detalhes do processo"
            expect(items[0].find('[data-testid="btn-nav-home"]').exists()).toBe(true);
            expect(items[1].text()).toBe("Detalhes do processo");
        });

        it("deve renderizar breadcrumbs para rota de Subprocesso", () => {
            vi.mocked(useRoute).mockReturnValue(
                createMockRoute(
                    "/processo/123/ASSESSORIA_12",
                    mockMatchedSubprocesso,
                    "Subprocesso",
                    {codProcesso: "123", siglaUnidade: "ASSESSORIA_12"},
                ),
            );
            const wrapper = mount(BarraNavegacao, getCommonMountOptions({
                perfil: {perfilSelecionado: Perfil.ADMIN}
            }, {BButton}));

            const items = wrapper.findAllComponents(BBreadcrumbItem);
            expect(items).toHaveLength(3); // Home + "Detalhes do processo" + sigla
            expect(items[0].find('[data-testid="btn-nav-home"]').exists()).toBe(true);
            expect(items[1].text()).toBe("Detalhes do processo");
            expect(items[2].text()).toBe("ASSESSORIA_12");
        });

        it("deve renderizar breadcrumbs para rotas de unidade", () => {
            vi.mocked(useRoute).mockReturnValue(
                createMockRoute("/unidade/1", mockMatchedUnidade, "Unidade", {codUnidade: "1"}),
            );
            const wrapper = mount(BarraNavegacao, getCommonMountOptions({
                perfil: {perfilSelecionado: Perfil.ADMIN}
            }, {BButton}));

            const items = wrapper.findAllComponents(BBreadcrumbItem);
            expect(items).toHaveLength(3); // Home + sigla/codigo + "Minha unidade"
            expect(items[0].find('[data-testid="btn-nav-home"]').exists()).toBe(true);
            // Sem unidade carregada no store, mostra "Unidade X"
            expect(items[1].text()).toBe("Unidade 1");
            expect(items[2].text()).toBe("Minha unidade");
        });
    });

    describe("Lógica de Perfil", () => {
        it("deve omitir o breadcrumb 'Detalhes do processo' para o perfil CHEFE", () => {
            vi.mocked(useRoute).mockReturnValue(
                createMockRoute("/processo/123", mockMatchedProcesso, "Processo", {codProcesso: "123"}),
            );

            const wrapper = mount(BarraNavegacao, getCommonMountOptions({
                perfil: {perfilSelecionado: Perfil.CHEFE}
            }, {BButton}));

            const items = wrapper.findAllComponents(BBreadcrumbItem);
            expect(items).toHaveLength(1); // Apenas Home
            expect(items[0].find('[data-testid="btn-nav-home"]').exists()).toBe(true);
        });

        it("deve omitir o breadcrumb 'Detalhes do processo' para o perfil SERVIDOR", () => {
            vi.mocked(useRoute).mockReturnValue(
                createMockRoute("/processo/123", mockMatchedProcesso, "Processo", {codProcesso: "123"}),
            );

            const wrapper = mount(BarraNavegacao, getCommonMountOptions({
                perfil: {perfilSelecionado: Perfil.SERVIDOR}
            }, {BButton}));

            const items = wrapper.findAllComponents(BBreadcrumbItem);
            expect(items).toHaveLength(1); // Apenas Home
            expect(items[0].find('[data-testid="btn-nav-home"]').exists()).toBe(true);
        });

        it("deve mostrar sigla da unidade para CHEFE em Subprocesso", () => {
            vi.mocked(useRoute).mockReturnValue(
                createMockRoute(
                    "/processo/123/ASSESSORIA_12",
                    mockMatchedSubprocesso,
                    "Subprocesso",
                    {codProcesso: "123", siglaUnidade: "ASSESSORIA_12"},
                ),
            );

            const wrapper = mount(BarraNavegacao, getCommonMountOptions({
                perfil: {perfilSelecionado: Perfil.CHEFE}
            }, {BButton}));

            const items = wrapper.findAllComponents(BBreadcrumbItem);
            expect(items).toHaveLength(2); // Home + sigla (sem "Detalhes do processo")
            expect(items[0].find('[data-testid="btn-nav-home"]').exists()).toBe(true);
            expect(items[1].text()).toBe("ASSESSORIA_12");
        });
    });

    describe("Funcionalidade", () => {
        it("deve chamar router.back() ao clicar no botão de voltar", async () => {
            vi.mocked(useRoute).mockReturnValue(
                createMockRoute("/processo/123", mockMatchedProcesso, "Processo", {codProcesso: "123"}),
            );
            const wrapper = mount(
                BarraNavegacao,
                getCommonMountOptions({}, {BButton}),
            );
            await wrapper.findComponent(BButton).trigger("click");
            expect(mockRouter.back).toHaveBeenCalledTimes(1);
        });
    });
});
