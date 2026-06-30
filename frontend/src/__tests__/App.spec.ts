vi.mock("../package.json", () => ({default: {version: "1.0.0-test"}}));

import {beforeEach, describe, expect, it, vi} from "vitest";
import {nextTick} from "vue";
import {mount} from "@vue/test-utils";
import App from "../App.vue";
import {createTestingPinia} from "@pinia/testing";
import {useRoute} from "vue-router";
import {usePerfilStore} from "@/stores/perfil";
import {useConfiguracoes} from "@/composables/useConfiguracoes";

vi.mock("@/components/layout/BarraNavegacao.vue", () => ({default: {template: '<div data-testid="barra-navegacao"></div>'}}));
vi.mock("@/components/layout/MainNavbar.vue", () => ({default: {template: '<div data-testid="main-navbar"></div>'}}));
vi.mock("@/composables/useConfiguracoes", () => ({
    useConfiguracoes: vi.fn(() => ({
        carregarConfiguracoes: vi.fn(),
    })),
}));
vi.mock("@/composables/useCacheSync", () => ({
    useCacheSync: vi.fn(() => vi.fn()),
}));
vi.mock("bootstrap-vue-next", async () => {
    const actual = await vi.importActual("bootstrap-vue-next");
    return {
        ...actual,
        BOrchestrator: {template: '<div></div>'}
    };
});

const routerViewStub = {
    name: "RouterView",
    render(this: {
        $slots: {
            default?: (props: {
                Component: { name: string; template: string };
                route: { meta: Record<string, unknown>; fullPath: string };
            }) => unknown;
        };
    }) {
        return this.$slots.default?.({
            Component: {name: "RouteComponentStub", template: '<div data-testid="route-component"></div>'},
            route: {meta: {}, fullPath: "/mock"}
        });
    }
};

vi.mock("vue-router", () => ({
    useRoute: vi.fn(),
}));

describe("App.vue", () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    it("deve renderizar corretamente na rota /login", () => {
        (useRoute as any).mockReturnValue({path: "/login", fullPath: "/login"});

        const wrapper = mount(App, {
            global: {
                plugins: [createTestingPinia({createSpy: vi.fn})],
                components: {
                    "router-view": routerViewStub,
                },
                stubs: {
                    BOrchestrator: true,
                    BAlert: true,
                }
            },
        });

        expect(wrapper.find('[data-testid="main-navbar"]').exists()).toBe(false);
        expect(wrapper.find('[data-testid="barra-navegacao"]').exists()).toBe(false);
        expect(wrapper.find('[data-testid="app-footer"]').exists()).toBe(false);
        expect(wrapper.findComponent({name: 'RouterView'}).exists() || wrapper.find('router-view-stub').exists()).toBe(true);
    });

    it("deve renderizar layout completo na rota /processo", () => {
        (useRoute as any).mockReturnValue({path: "/processo", fullPath: "/processo"});

        const wrapper = mount(App, {
            global: {
                plugins: [createTestingPinia({createSpy: vi.fn})],
                components: {
                    "router-view": routerViewStub,
                },
                stubs: {BOrchestrator: true, BAlert: true}
            },
        });

        expect(wrapper.find('[data-testid="main-navbar"]').exists()).toBe(true);
        expect(wrapper.find('[data-testid="barra-navegacao"]').exists()).toBe(true);
        expect(wrapper.find('[data-testid="app-footer"]').exists()).toBe(true);
        expect(wrapper.find('[data-testid="app-version"]').exists()).toBe(true);
    });

    it("deve renderizar o link de 'pular para o conteúdo' com os atributos corretos", () => {
        (useRoute as any).mockReturnValue({path: "/painel", fullPath: "/painel"});
        const wrapper = mount(App, {
            global: {
                plugins: [createTestingPinia({createSpy: vi.fn})],
                components: {
                    "router-view": routerViewStub,
                },
                stubs: {BOrchestrator: true, BAlert: true}
            },
        });

        const skipLink = wrapper.find('[data-testid="skip-link"]');
        expect(skipLink.exists()).toBe(true);
        expect(skipLink.classes()).toContain('visually-hidden-focusable');
        expect(wrapper.find('[data-testid="main-content"]').exists()).toBe(true);
    });

    it("não deve renderizar BarraNavegacao na rota /painel", () => {
        (useRoute as any).mockReturnValue({path: "/painel", fullPath: "/painel"});

        const wrapper = mount(App, {
            global: {
                plugins: [createTestingPinia({createSpy: vi.fn})],
                components: {
                    "router-view": routerViewStub,
                },
                stubs: {BOrchestrator: true, BAlert: true}
            },
        });

        expect(wrapper.find('[data-testid="main-navbar"]').exists()).toBe(true);
        expect(wrapper.find('[data-testid="barra-navegacao"]').exists()).toBe(false);
    });

    it("deve carregar configurações e iniciar sincronização de cache quando o usuário loga e tem permissão", async () => {
        const mockCarregarConfiguracoes = vi.fn();
        vi.mocked(useConfiguracoes).mockReturnValue({
            carregarConfiguracoes: mockCarregarConfiguracoes,
        } as any);

        const pinia = createTestingPinia({stubActions: false});
        const store = usePerfilStore(pinia);

        (useRoute as any).mockReturnValue({path: "/painel", fullPath: "/painel"});

        const wrapper = mount(App, {
            global: {
                plugins: [pinia],
                components: {
                    "router-view": routerViewStub,
                },
                stubs: {BOrchestrator: true, BAlert: true}
            },
        });

        store.usuarioCodigo = "USER_123";
        store.permissoesSessao = {mostrarMenuConfiguracoes: true} as any;

        await nextTick();

        expect(mockCarregarConfiguracoes).toHaveBeenCalled();
        wrapper.unmount();
    });

    it("deve renderizar KeepAlive quando a rota tem meta.keepAlive como true", () => {
        (useRoute as any).mockReturnValue({path: "/keep", fullPath: "/keep"});

        const routerViewStubKeepAlive = {
            name: "RouterView",
            render(this: any) {
                return this.$slots.default?.({
                    Component: {name: "RouteComponentStub", template: '<div data-testid="route-component"></div>'},
                    route: {meta: {keepAlive: true}, fullPath: "/keep"}
                });
            }
        };

        const wrapper = mount(App, {
            global: {
                plugins: [createTestingPinia()],
                components: {
                    "router-view": routerViewStubKeepAlive,
                },
                stubs: {BOrchestrator: true, BAlert: true, KeepAlive: true}
            },
        });

        expect(wrapper.find('[data-testid="route-component"]').exists()).toBe(true);
        wrapper.unmount();
    });

    it("não deve renderizar navbar ou footer na rota /erro", () => {
        (useRoute as any).mockReturnValue({path: "/erro", fullPath: "/erro"});

        const wrapper = mount(App, {
            global: {
                plugins: [createTestingPinia()],
                components: {
                    "router-view": routerViewStub,
                },
                stubs: {BOrchestrator: true, BAlert: true}
            },
        });

        expect(wrapper.find('[data-testid="main-navbar"]').exists()).toBe(false);
        expect(wrapper.find('[data-testid="app-footer"]').exists()).toBe(false);
        wrapper.unmount();
    });
});
