import {describe, expect, it, vi} from "vitest";
import {mount} from "@vue/test-utils";
import MainNavbar from "../layout/MainNavbar.vue";
import {usePerfil} from "@/composables/usePerfil";
import {ref} from "vue";
import {createTestingPinia} from "@pinia/testing";

vi.mock("@/composables/usePerfil");

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
        currentRoute: {value: {path: "/"}}
    })),
    createWebHistory: vi.fn(),
    createMemoryHistory: vi.fn(),
}));

describe("MainNavbar.vue Coverage", () => {
    // Setup default mock for usePerfil
    vi.mocked(usePerfil).mockReturnValue({
        perfilSelecionado: ref("GESTOR"),
        unidadeSelecionada: ref("Unidade teste"),
        usuarioNome: ref("User"),
        podeAcessarTodasUnidades: ref(false)
    } as any);

    it("deve atualizar isMobile ao redimensionar a janela", async () => {
        // Mount component
        const wrapper = mount(MainNavbar, {
            global: {
                plugins: [createTestingPinia({stubActions: false})],
                stubs: {
                    BNavbar: {template: '<div><slot /></div>'},
                    BNavbarBrand: {template: '<div></div>'},
                    BNavbarToggle: {template: '<div></div>'},
                    BCollapse: {template: '<div><slot /></div>'},
                    BNavbarNav: {template: '<div><slot /></div>'},
                    BNavItem: {template: '<div><slot /></div>'}
                },
                directives: {
                    'b-tooltip': {}
                }
            }
        });

        expect((wrapper.vm as any).isMobile).toBe(false);

        // Resize to mobile
        globalThis.innerWidth = 500;
        globalThis.dispatchEvent(new Event('resize'));

        expect((wrapper.vm as any).isMobile).toBe(true);

        // Resize back to desktop
        globalThis.innerWidth = 1200;
        globalThis.dispatchEvent(new Event('resize'));

        expect((wrapper.vm as any).isMobile).toBe(false);
    });

    it("deve remover event listener ao desmontar", async () => {
        const removeEventListenerSpy = vi.spyOn(globalThis, 'removeEventListener');

        const wrapper = mount(MainNavbar, {
            global: {
                plugins: [createTestingPinia({stubActions: false})],
                stubs: {
                    BNavbar: {template: '<div><slot /></div>'},
                    BNavbarBrand: {template: '<div></div>'},
                    BNavbarToggle: {template: '<div></div>'},
                    BCollapse: {template: '<div><slot /></div>'},
                    BNavbarNav: {template: '<div><slot /></div>'},
                    BNavItem: {template: '<div><slot /></div>'}
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
