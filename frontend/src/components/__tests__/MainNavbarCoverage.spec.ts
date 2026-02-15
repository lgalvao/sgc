import {describe, expect, it, vi} from "vitest";
import {mount} from "@vue/test-utils";
import MainNavbar from "../layout/MainNavbar.vue";
import {usePerfil} from "@/composables/usePerfil";
import {ref} from "vue";
import {createTestingPinia} from "@pinia/testing";

// Mock usePerfil
vi.mock("@/composables/usePerfil");

// Mock vue-router
const mocks = vi.hoisted(() => ({
    push: vi.fn()
}));

vi.mock("vue-router", () => ({
    useRoute: vi.fn(),
    useRouter: () => ({
        push: mocks.push,
    }),
    createRouter: vi.fn(() => ({
        beforeEach: vi.fn(),
        afterEach: vi.fn(),
        push: mocks.push,
        currentRoute: { value: { path: "/" } }
    })),
    createWebHistory: vi.fn(),
    createMemoryHistory: vi.fn(),
}));

describe("MainNavbar.vue Coverage", () => {
    // Setup default mock for usePerfil
    vi.mocked(usePerfil).mockReturnValue({
        perfilSelecionado: ref("GESTOR"),
        unidadeSelecionada: ref("Unidade Teste"),
        usuarioNome: ref("User")
    } as any);

    it("deve atualizar isMobile ao redimensionar a janela", async () => {
        // Mount component
        const wrapper = mount(MainNavbar, {
            global: {
                plugins: [createTestingPinia({ stubActions: false })],
                stubs: {
                    BNavbar: { template: '<div><slot /></div>' },
                    BNavbarBrand: { template: '<div></div>' },
                    BNavbarToggle: { template: '<div></div>' },
                    BCollapse: { template: '<div><slot /></div>' },
                    BNavbarNav: { template: '<div><slot /></div>' },
                    BNavItem: { template: '<div><slot /></div>' }
                },
                directives: {
                    'b-tooltip': {}
                }
            }
        });

        // Initial state (assuming default window.innerWidth is 1024 or similar in JSDOM)
        // JSDOM usually defaults to 1024x768
        expect((wrapper.vm as any).isMobile).toBe(false);

        // Resize to mobile
        window.innerWidth = 500;
        window.dispatchEvent(new Event('resize'));

        expect((wrapper.vm as any).isMobile).toBe(true);

        // Resize back to desktop
        window.innerWidth = 1200;
        window.dispatchEvent(new Event('resize'));

        expect((wrapper.vm as any).isMobile).toBe(false);
    });

    it("deve remover event listener ao desmontar", async () => {
        const removeEventListenerSpy = vi.spyOn(window, 'removeEventListener');

        const wrapper = mount(MainNavbar, {
            global: {
                plugins: [createTestingPinia({ stubActions: false })],
                stubs: {
                    BNavbar: { template: '<div><slot /></div>' },
                    BNavbarBrand: { template: '<div></div>' },
                    BNavbarToggle: { template: '<div></div>' },
                    BCollapse: { template: '<div><slot /></div>' },
                    BNavbarNav: { template: '<div><slot /></div>' },
                    BNavItem: { template: '<div><slot /></div>' }
                },
                directives: {
                    'b-tooltip': {}
                }
            }
        });

        wrapper.unmount();

        expect(removeEventListenerSpy).toHaveBeenCalledWith('resize', expect.any(Function));
    });
});
