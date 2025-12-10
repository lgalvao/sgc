import {beforeEach, describe, expect, it, vi} from "vitest";
import router from "../index";
import {createPinia, setActivePinia} from "pinia";
import {usePerfilStore} from "@/stores/perfil";

// Mock das rotas para evitar problemas de importação nos testes unitários do index
vi.mock("../main.routes", () => ({default: []}));
vi.mock("../processo.routes", () => ({default: []}));
vi.mock("../unidade.routes", () => ({default: []}));
vi.mock("../diagnostico.routes", () => ({default: []}));

describe("router/index.ts", () => {
    beforeEach(() => {
        setActivePinia(createPinia());
        // Resetar o router é complicado pois é uma instância global,
        // mas adicionar rotas de teste ajuda a isolar.
    });

    it("deve criar uma instância do router", () => {
        expect(router).toBeDefined();
        expect(router.options.routes).toBeDefined();
    });

    it("deve redirecionar para /login se não autenticado e rota requer auth", async () => {
        const perfilStore = usePerfilStore();
        perfilStore.servidorId = null; // Não autenticado

        router.addRoute({
            path: "/rota-protegida",
            name: "RotaProtegida",
            component: {template: "<div>Protegida</div>"}
        });

        router.addRoute({
            path: "/login",
            name: "Login",
            component: {template: "<div>Login</div>"}
        });

        try {
            await router.push("/rota-protegida");
        } catch {
            // Ignorar erros de navegação cancelada
        }

        // O router deve ter redirecionado para /login
        // Nota: Como o router é global, ele pode manter estado.
        // O teste ideal seria recriar o router, mas isso exige refatorar o src/router/index.ts para exportar uma função de criação.
        expect(router.currentRoute.value.path).toBe("/login");
    });

    it("deve permitir navegação se autenticado", async () => {
        const perfilStore = usePerfilStore();
        perfilStore.servidorId = 123; // Autenticado

        router.addRoute({
            path: "/rota-permitida",
            name: "RotaPermitida",
            component: {template: "<div>Permitida</div>"}
        });

        await router.push("/rota-permitida");
        expect(router.currentRoute.value.path).toBe("/rota-permitida");
    });

    it("deve atualizar o título da página após navegação", async () => {
        const perfilStore = usePerfilStore();
        perfilStore.servidorId = 123; // Autenticado para evitar redirecionamento

        router.addRoute({
            path: "/titulo",
            name: "TituloTeste",
            component: {template: "<div>Titulo</div>"},
            meta: {title: "Meu Título"}
        });

        await router.push("/titulo");
        // Esperar a navegação completar
        await router.isReady();

        expect(document.title).toBe("Meu Título - SGC");
    });

    it("deve usar o nome da rota como título se meta.title não existir", async () => {
        const perfilStore = usePerfilStore();
        perfilStore.servidorId = 123; // Autenticado

        router.addRoute({
            path: "/nome-titulo",
            name: "NomeComoTitulo",
            component: {template: "<div>Nome</div>"}
        });

        await router.push("/nome-titulo");
        await router.isReady();

        expect(document.title).toBe("NomeComoTitulo - SGC");
    });
});