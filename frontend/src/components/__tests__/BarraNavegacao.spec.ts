import {mount} from "@vue/test-utils";
import {computed} from "vue";
import {beforeEach, describe, expect, it, vi} from "vitest";
import BarraNavegacao from "../layout/BarraNavegacao.vue";

const mockRouter = {
    back: vi.fn(),
};

const mockRoute = {
    path: "/processo/123",
    matched: [],
    params: {},
    name: "Processo",
    fullPath: "/processo/123",
    query: {},
    hash: "",
    meta: {},
    redirectedFrom: undefined,
};

const breadcrumbsMock = computed(() => [
    {label: "Painel", to: {name: "Painel"}, isHome: true},
    {label: "Detalhes do processo", to: undefined},
]);

vi.mock("vue-router", () => ({
    useRoute: () => mockRoute,
    useRouter: () => mockRouter,
}));

vi.mock("@/composables/useBreadcrumbs", () => ({
    useBreadcrumbs: () => ({breadcrumbs: breadcrumbsMock}),
}));

const BButtonStub = {
    template: '<button><slot /></button>',
};

const BBreadcrumbStub = {
    template: '<nav data-testid="nav-breadcrumbs"><slot /></nav>',
};

const BBreadcrumbItemStub = {
    template: '<li><slot /></li>',
    props: ["active", "to"],
};

describe("BarraNavegacao.vue", () => {
    beforeEach(() => {
        vi.clearAllMocks();
        mockRoute.path = "/processo/123";
        mockRoute.name = "Processo";
    });

    function montarComponente() {
        return mount(BarraNavegacao, {
            global: {
                stubs: {
                    BButton: BButtonStub,
                    BBreadcrumb: BBreadcrumbStub,
                    BBreadcrumbItem: BBreadcrumbItemStub,
                },
                directives: {
                    "b-tooltip": {},
                },
            },
        });
    }

    it("não deve exibir o botão de voltar e os breadcrumbs na página de login", () => {
        mockRoute.path = "/login";

        const wrapper = montarComponente();

        expect(wrapper.find("button").exists()).toBe(false);
        expect(wrapper.find('[data-testid="nav-breadcrumbs"]').exists()).toBe(false);
    });

    it("não deve exibir o botão de voltar e os breadcrumbs na página do painel", () => {
        mockRoute.path = "/painel";
        mockRoute.name = "Painel";

        const wrapper = montarComponente();

        expect(wrapper.find("button").exists()).toBe(false);
        expect(wrapper.find('[data-testid="nav-breadcrumbs"]').exists()).toBe(false);
    });

    it("deve exibir o botão de voltar e os breadcrumbs em outras páginas", () => {
        const wrapper = montarComponente();

        expect(wrapper.find("button").exists()).toBe(true);
        expect(wrapper.find('[data-testid="nav-breadcrumbs"]').exists()).toBe(true);
    });

    it("deve renderizar os breadcrumbs recebidos do composable", () => {
        const wrapper = montarComponente();

        const items = wrapper.findAllComponents(BBreadcrumbItemStub);
        expect(items).toHaveLength(2);
        expect(items[0].find('[data-testid="btn-nav-home"]').exists()).toBe(true);
        expect(items[1].text()).toBe("Detalhes do processo");
    });

    it("deve chamar void router.back() ao clicar no botão de voltar", async () => {
        const wrapper = montarComponente();

        await wrapper.find("button").trigger("click");

        expect(mockRouter.back).toHaveBeenCalledTimes(1);
    });
});
