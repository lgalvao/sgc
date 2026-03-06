vi.mock("../package.json", () => ({default: {version: "1.0.0-test"}}));

import {beforeEach, describe, expect, it, vi} from "vitest";
import {mount} from "@vue/test-utils";
import App from "../App.vue";
import {createTestingPinia} from "@pinia/testing";
import {useRoute} from "vue-router";

vi.mock("@/components/layout/BarraNavegacao.vue", () => ({default: {template: '<div data-testid="barra-navegacao"></div>'}}));
vi.mock("@/components/layout/MainNavbar.vue", () => ({default: {template: '<div data-testid="main-navbar"></div>'}}));
vi.mock("bootstrap-vue-next", async () => {
    const actual = await vi.importActual("bootstrap-vue-next");
    return {
        ...actual,
        BOrchestrator: {template: '<div></div>'}
    };
});

vi.mock("vue-router", () => ({
    useRoute: vi.fn(),
    RouterView: {template: '<div data-testid="router-view"></div>'}
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
                stubs: {
                    BOrchestrator: true,
                    BAlert: true,
                    'router-view': true
                }
            },
        });

        expect(wrapper.find('[data-testid="main-navbar"]').exists()).toBe(false);
        expect(wrapper.find('[data-testid="barra-navegacao"]').exists()).toBe(false);
        expect(wrapper.find('footer').exists()).toBe(false);
        expect(wrapper.findComponent({name: 'RouterView'}).exists() || wrapper.find('router-view-stub').exists()).toBe(true);
    });

    it("deve renderizar layout completo na rota /processo", () => {
        (useRoute as any).mockReturnValue({path: "/processo", fullPath: "/processo"});

        const wrapper = mount(App, {
            global: {
                plugins: [createTestingPinia({createSpy: vi.fn})],
                stubs: {BOrchestrator: true, BAlert: true, 'router-view': true}
            },
        });

        expect(wrapper.find('[data-testid="main-navbar"]').exists()).toBe(true);
        expect(wrapper.find('[data-testid="barra-navegacao"]').exists()).toBe(true);
        expect(wrapper.find('footer').exists()).toBe(true);
        expect(wrapper.text()).toContain("Versão");
    });

    it("deve renderizar o link de 'pular para o conteúdo' com os atributos corretos", () => {
        (useRoute as any).mockReturnValue({path: "/painel", fullPath: "/painel"});
        const wrapper = mount(App, {
            global: {
                plugins: [createTestingPinia({createSpy: vi.fn})],
                stubs: {BOrchestrator: true, BAlert: true, 'router-view': true}
            },
        });

        const skipLink = wrapper.find('a[href="#main-content"]');
        expect(skipLink.exists()).toBe(true);
        expect(skipLink.classes()).toContain('visually-hidden-focusable');
        expect(wrapper.find('main#main-content').exists()).toBe(true);
    });

    it("não deve renderizar BarraNavegacao na rota /painel", () => {
        (useRoute as any).mockReturnValue({path: "/painel", fullPath: "/painel"});

        const wrapper = mount(App, {
            global: {
                plugins: [createTestingPinia({createSpy: vi.fn})],
                stubs: {BOrchestrator: true, BAlert: true, 'router-view': true}
            },
        });

        expect(wrapper.find('[data-testid="main-navbar"]').exists()).toBe(true);
        expect(wrapper.find('[data-testid="barra-navegacao"]').exists()).toBe(false);
    });
});
