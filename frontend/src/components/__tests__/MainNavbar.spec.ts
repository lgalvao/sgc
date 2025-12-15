import {mount} from "@vue/test-utils";
import {beforeEach, describe, expect, it, type MockInstance, vi,} from "vitest";
import {ref} from "vue";
import {createMemoryHistory, createRouter} from "vue-router";
import {usePerfil} from "@/composables/usePerfil";
import {usePerfilStore} from "@/stores/perfil";
import {initPinia} from "@/test-utils/helpers";
import NavBar from "../MainNavbar.vue";

// Mocks
vi.mock("@/composables/usePerfil");
vi.mock("@/stores/perfil");

const routes = [
    {path: "/", component: {template: "<div></div>"}},
    {path: "/painel", component: {template: "<div></div>"}},
    {path: "/login", component: {template: "<div></div>"}},
    {path: "/teste", component: {template: "<div></div>"}},
    {path: "/configuracoes", component: {template: "<div></div>"}},
    {path: "/unidade/456", component: {template: "<div></div>"}},
    {path: "/relatorios", component: {template: "<div></div>"}},
    {path: "/historico", component: {template: "<div></div>"}},
];

describe("MainNavbar.vue", () => {
    let router: ReturnType<typeof createRouter>;
    let pushSpy: MockInstance;

    beforeEach(() => {
        vi.restoreAllMocks();
        initPinia();
        router = createRouter({history: createMemoryHistory(), routes});
        pushSpy = vi.spyOn(router, "push");

        vi.mocked(usePerfil).mockReturnValue({
            servidorLogado: ref({nome: "Usuario Teste"}),
            perfilSelecionado: ref("GESTOR"),
            unidadeSelecionada: ref("Unidade Teste"),
        } as any);

        vi.mocked(usePerfilStore).mockReturnValue({
            perfilSelecionado: "ADMIN",
            unidadeSelecionada: 456,
        } as any);
    });

    it("deve navegar para a rota correta ao clicar nos links do menu", async () => {
        const wrapper = mount(NavBar, {global: {plugins: [router]}});
        await router.isReady();

        // Painel
        const painelLink = wrapper
            .findAll("a")
            .find((a) => a.text().includes("Painel"));
        expect(painelLink?.exists()).toBe(true);
        await painelLink?.trigger("click");
        expect(pushSpy).toHaveBeenCalledWith("/painel");

        // Minha unidade
        const unidadeLink = wrapper
            .findAll("a")
            .find((a) => a.text().includes("Minha unidade"));
        await unidadeLink?.trigger("click");
        expect(pushSpy).toHaveBeenCalledWith("/unidade/456");

        // Relatorios
        const relatoriosLink = wrapper
            .findAll("a")
            .find((a) => a.text().includes("Relatórios"));
        await relatoriosLink?.trigger("click");
        expect(pushSpy).toHaveBeenCalledWith("/relatorios");

        // Historico
        const historicoLink = wrapper
            .findAll("a")
            .find((a) => a.text().includes("Histórico"));
        await historicoLink?.trigger("click");
        expect(pushSpy).toHaveBeenCalledWith("/historico");
    });

    it("deve exibir o perfil e a unidade do usuário", async () => {
        vi.mocked(usePerfil).mockReturnValue({
            perfilSelecionado: ref("CHEFE"),
            unidadeSelecionada: ref("TRE-PR"),
        } as any);

        const wrapper = mount(NavBar, {global: {plugins: [router]}});
        await router.isReady();

        const userInfo = wrapper.find("span.nav-link");
        expect(userInfo.text()).toContain("CHEFE - TRE-PR");
    });

    it("deve exibir e navegar pelo ícone de configurações para o perfil ADMIN", async () => {
        vi.mocked(usePerfilStore).mockReturnValue({
            perfilSelecionado: "ADMIN",
            unidadeSelecionada: 456,
        } as any);

        const wrapper = mount(NavBar, {global: {plugins: [router]}});
        await router.isReady();


        // Or just find by testid on the BNavItem which might fallthrough to LI or A depending on impl.
        // The testid is on BNavItem.
        // If BNavItem renders LI, the click listener might be on LI or A.
        // The component code has @click.prevent on BNavItem.

        const settingsNavItem = wrapper.find('[data-testid="btn-configuracoes"]');
        expect(settingsNavItem.exists()).toBe(true);

        // Need to trigger click on the element that listens.
        // If BNavItem is a stub or real component? It is NOT stubbed here explicitly, so it's the real component from library.
        // Real BNavItem usually has the click listener on the anchor tag.
        const anchor = settingsNavItem.find("a");
        if (anchor.exists()) {
            await anchor.trigger("click");
        } else {
            await settingsNavItem.trigger("click");
        }

        expect(pushSpy).toHaveBeenCalledWith("/configuracoes");
    });

    it("NÃO deve exibir o ícone de configurações para perfis diferentes de ADMIN", async () => {
        vi.mocked(usePerfilStore).mockReturnValue({
            perfilSelecionado: "GESTOR",
            unidadeSelecionada: 456,
        } as any);

        const wrapper = mount(NavBar, {global: {plugins: [router]}});
        await router.isReady();

        const settingsIcon = wrapper.find('[title="Configurações do sistema"]');
        expect(settingsIcon.exists()).toBe(false);
    });
});
