import {mount, RouterLinkStub} from "@vue/test-utils";
import {beforeEach, describe, expect, it, vi} from "vitest";
import {ref} from "vue";
import {getCommonMountOptions, setupComponentTest} from "@/test-utils/componentTestHelpers";
import NavBar from "../MainNavbar.vue";
import {usePerfil} from "@/composables/usePerfil";
import {checkA11y} from "@/test-utils/a11yTestHelpers";

// Mock usePerfil
vi.mock("@/composables/usePerfil");

const { mockPush } = vi.hoisted(() => ({
    mockPush: vi.fn()
}));

// Mock vue-router to control push and satisfy imports
vi.mock("vue-router", () => ({
    useRoute: vi.fn(),
    useRouter: () => ({
        push: mockPush,
    }),
    createRouter: vi.fn(() => ({
        beforeEach: vi.fn(),
        afterEach: vi.fn(),
        push: mockPush,
        currentRoute: { value: { path: "/" } }
    })),
    createWebHistory: vi.fn(),
    createMemoryHistory: vi.fn(),
}));

describe("MainNavbar.vue", () => {
    // Setup cleanup
    const ctx = setupComponentTest();

    beforeEach(() => {
        vi.clearAllMocks();

        // Default mock for usePerfil
        vi.mocked(usePerfil).mockReturnValue({
            servidorLogado: ref({ nome: "Usuario Teste" }),
            perfilSelecionado: ref("GESTOR"),
            unidadeSelecionada: ref("Unidade Teste"),
        } as any);
    });

    const checkLink = (text: string, to: string) => {
         const links = ctx.wrapper.findAllComponents(RouterLinkStub);
         const link = links.find(w => w.text().includes(text));
         expect(link?.exists()).toBe(true);
         expect(link?.props().to).toBe(to);
    };

    it("deve navegar para a rota correta ao clicar nos links do menu (ADMIN)", async () => {
        const options = getCommonMountOptions({
            perfil: {
                perfilSelecionado: "ADMIN",
                unidadeSelecionada: 456
            }
        });

        ctx.wrapper = mount(NavBar, options);

        checkLink("Painel", "/painel");
        // ADMIN vê "Unidades" apontando para unidade raiz (código 1)
        checkLink("Unidades", "/unidade/1");
        checkLink("Relatórios", "/relatorios");
        checkLink("Histórico", "/historico");
    });

    it("deve navegar para a unidade do usuário ao clicar em 'Minha unidade' (não-ADMIN)", async () => {
        const options = getCommonMountOptions({
            perfil: {
                perfilSelecionado: "GESTOR",
                unidadeSelecionada: 456
            }
        });

        ctx.wrapper = mount(NavBar, options);


        // GESTOR vê "Minha unidade" apontando para sua unidade
        checkLink("Minha unidade", "/unidade/456");
    });

    it("deve exibir o perfil e a unidade do usuário", async () => {
        vi.mocked(usePerfil).mockReturnValue({
            perfilSelecionado: ref("CHEFE"),
            unidadeSelecionada: ref("TRE-PR"),
        } as any);

        const options = getCommonMountOptions({
            perfil: {
                perfilSelecionado: "CHEFE",
                unidadeSelecionada: 123
            }
        });

        ctx.wrapper = mount(NavBar, options);

        const userItem = ctx.wrapper.find(".user-profile-item");
        expect(userItem.exists()).toBe(true);
        expect(userItem.text()).toContain("CHEFE - TRE-PR");
    });

    it("deve exibir e navegar pelo ícone de configurações para o perfil ADMIN", async () => {
        const options = getCommonMountOptions({
            perfil: {
                perfilSelecionado: "ADMIN",
                unidadeSelecionada: 456
            }
        });

        ctx.wrapper = mount(NavBar, options);

        const settingsNavItem = ctx.wrapper.find('[data-testid="btn-configuracoes"]');
        expect(settingsNavItem.exists()).toBe(true);

        const link = settingsNavItem.findComponent(RouterLinkStub);

        if (link.exists()) {
             expect(link.props().to).toBe("/configuracoes");
        } else {
            expect(settingsNavItem.attributes("to")).toBe("/configuracoes");
        }
    });

    it("NÃO deve exibir o ícone de configurações para perfis diferentes de ADMIN", async () => {
        const options = getCommonMountOptions({
            perfil: {
                perfilSelecionado: "GESTOR",
                unidadeSelecionada: 456
            }
        });

        ctx.wrapper = mount(NavBar, options);

        const settingsIcon = ctx.wrapper.find('[title="Configurações do sistema"]');
        expect(settingsIcon.exists()).toBe(false);
    });

    it("deve chamar router.push ao fazer logout", async () => {
         const options = getCommonMountOptions({
            perfil: {
                perfilSelecionado: "ADMIN",
                unidadeSelecionada: 456
            }
        });
        ctx.wrapper = mount(NavBar, options);

        const logoutNavItem = ctx.wrapper.find('[data-testid="btn-logout"]');

        // Try finding 'a' tag inside if BNavItem renders it
        const anchor = logoutNavItem.find("a");
        if (anchor.exists()) {
             await anchor.trigger("click");
        } else {
             await logoutNavItem.trigger("click");
        }


        expect(mockPush).toHaveBeenCalledWith("/login");
    });

    it("deve ser acessível", async () => {
        const options = getCommonMountOptions({
            perfil: {
                perfilSelecionado: "ADMIN",
                unidadeSelecionada: 456
            }
        });
        ctx.wrapper = mount(NavBar, options);
        await checkA11y(ctx.wrapper.element as HTMLElement);
    });
});
