import {createTestingPinia} from "@pinia/testing";
import {mount, RouterLinkStub} from "@vue/test-utils";
import {BBreadcrumbItem, BButton} from "bootstrap-vue-next";
import {beforeEach, describe, expect, it, vi} from "vitest";
import {useRoute} from "vue-router";
import {usePerfilStore} from "@/stores/perfil";
import {Perfil} from "@/types/tipos";
import BarraNavegacao from "../BarraNavegacao.vue";

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
const createMockRoute = (path: string, matched: any[]) => ({
    path,
    matched,
    params: {id: "123"},
    name: "",
    fullPath: path,
    query: {},
    hash: "",
    meta: {},
    redirectedFrom: undefined,
});

const mockMatchedDefault = [
    {name: "Painel", meta: {}},
    {name: "Processo", meta: {breadcrumb: "Processos"}},
    {name: "Subprocesso", meta: {breadcrumb: "Detalhes"}},
];

const getMountOptions = (pinia: any) => ({
    global: {
        plugins: [pinia],
        stubs: {
            RouterLink: RouterLinkStub,
            BButton,
        },
    },
});

describe("BarraNavegacao.vue", () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    describe("Visibilidade dos Elementos", () => {
        it("não deve exibir o botão de voltar e os breadcrumbs na página de login", () => {
            vi.mocked(useRoute).mockReturnValue(createMockRoute("/login", []));
            const wrapper = mount(
                BarraNavegacao,
                getMountOptions(createTestingPinia({createSpy: vi.fn})),
            );
            expect(wrapper.find("button").exists()).toBe(false);
            expect(wrapper.find('[data-testid="nav-breadcrumbs"]').exists()).toBe(false);
        });

        it("não deve exibir o botão de voltar e os breadcrumbs na página do painel", () => {
            vi.mocked(useRoute).mockReturnValue(createMockRoute("/painel", []));
            const wrapper = mount(
                BarraNavegacao,
                getMountOptions(createTestingPinia({createSpy: vi.fn})),
            );
            expect(wrapper.find("button").exists()).toBe(false);
            expect(wrapper.find('[data-testid="nav-breadcrumbs"]').exists()).toBe(false);
        });

        it("deve exibir o botão de voltar e os breadcrumbs em outras páginas", () => {
            vi.mocked(useRoute).mockReturnValue(
                createMockRoute("/processo/123", mockMatchedDefault),
            );
            const wrapper = mount(
                BarraNavegacao,
                getMountOptions(createTestingPinia({createSpy: vi.fn})),
            );
            expect(wrapper.find("button").exists()).toBe(true);
            expect(wrapper.find('[data-testid="nav-breadcrumbs"]').exists()).toBe(true);
        });
    });

    describe("Renderização dos Breadcrumbs", () => {
        it("deve renderizar os breadcrumbs corretamente a partir da rota", () => {
            vi.mocked(useRoute).mockReturnValue(
                createMockRoute("/processo/123", mockMatchedDefault),
            );
            const pinia = createTestingPinia({createSpy: vi.fn});
            const perfilStore = usePerfilStore(pinia);
            perfilStore.perfilSelecionado = Perfil.ADMIN;
            const wrapper = mount(BarraNavegacao, getMountOptions(pinia));

            const items = wrapper.findAllComponents(BBreadcrumbItem);
            expect(items).toHaveLength(3);
            expect(
                items[0].find('[data-testid="btn-nav-home"]').exists(),
            ).toBe(true);
            expect(items[1].text()).toBe("Processos");
            expect(items[2].text()).toBe("Detalhes");
        });

        it("o último breadcrumb não deve ser um link", () => {
            vi.mocked(useRoute).mockReturnValue(
                createMockRoute("/processo/123", mockMatchedDefault),
            );
            const wrapper = mount(
                BarraNavegacao,
                getMountOptions(createTestingPinia({createSpy: vi.fn})),
            );
            const items = wrapper.findAllComponents(BBreadcrumbItem);
            expect(items[2].findComponent(RouterLinkStub).exists()).toBe(false);
            expect(items[2].find("span").exists()).toBe(true);
        });
    });

    describe("Lógica de Perfil", () => {
        it("deve omitir o breadcrumb de Processo para o perfil CHEFE", () => {
            vi.mocked(useRoute).mockReturnValue(
                createMockRoute("/processo/123", mockMatchedDefault),
            );
            const pinia = createTestingPinia({createSpy: vi.fn});
            const perfilStore = usePerfilStore(pinia);
            perfilStore.perfilSelecionado = Perfil.CHEFE;

            const wrapper = mount(BarraNavegacao, getMountOptions(pinia));

            const items = wrapper.findAllComponents(BBreadcrumbItem);
            expect(items).toHaveLength(2);
            expect(
                items[0].find('[data-testid="btn-nav-home"]').exists(),
            ).toBe(true);
            expect(items[1].text()).toBe("Detalhes");
        });

        it("deve omitir o breadcrumb de Processo para o perfil SERVIDOR", () => {
            vi.mocked(useRoute).mockReturnValue(
                createMockRoute("/processo/123", mockMatchedDefault),
            );
            const pinia = createTestingPinia({createSpy: vi.fn});
            const perfilStore = usePerfilStore(pinia);
            perfilStore.perfilSelecionado = Perfil.SERVIDOR;

            const wrapper = mount(BarraNavegacao, getMountOptions(pinia));

            const items = wrapper.findAllComponents(BBreadcrumbItem);
            expect(items).toHaveLength(2);
            expect(
                items[0].find('[data-testid="btn-nav-home"]').exists(),
            ).toBe(true);
            expect(items[1].text()).toBe("Detalhes");
        });
    });

    describe("Funcionalidade", () => {
        it("deve chamar router.back() ao clicar no botão de voltar", async () => {
            vi.mocked(useRoute).mockReturnValue(
                createMockRoute("/processo/123", mockMatchedDefault),
            );
            const wrapper = mount(
                BarraNavegacao,
                getMountOptions(createTestingPinia({createSpy: vi.fn})),
            );
            await wrapper.findComponent(BButton).trigger("click");
            expect(mockRouter.back).toHaveBeenCalledTimes(1);
        });
    });
});
