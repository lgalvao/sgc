import {beforeEach, describe, expect, it, vi} from "vitest";
import router from "../router";

// Mock the views to avoid loading real components
vi.mock("../views/LoginView.vue", () => ({
    default: {template: "<div>Login</div>"},
}));
vi.mock("../views/PainelView.vue", () => ({
    default: {template: "<div>Painel</div>"},
}));
vi.mock("../views/CadProcesso.vue", () => ({
    default: {template: "<div>CadProcesso</div>"},
}));
vi.mock("../views/ProcessoView.vue", () => ({
    default: {template: "<div>Processo</div>"},
}));
vi.mock("../views/SubprocessoView.vue", () => ({
    default: {template: "<div>Subprocesso</div>"},
}));
vi.mock("../views/CadMapa.vue", () => ({
    default: {template: "<div>CadMapa</div>"},
}));
vi.mock("../views/VisMapa.vue", () => ({
    default: {template: "<div>VisMapa</div>"},
}));
vi.mock("../views/CadAtividades.vue", () => ({
    default: {template: "<div>CadAtividades</div>"},
}));
vi.mock("../views/VisAtividades.vue", () => ({
    default: {template: "<div>VisAtividades</div>"},
}));
vi.mock("../views/DiagnosticoEquipe.vue", () => ({
    default: {template: "<div>DiagnosticoEquipe</div>"},
}));
vi.mock("../views/OcupacoesCriticas.vue", () => ({
    default: {template: "<div>OcupacoesCriticas</div>"},
}));
vi.mock("../views/AutoavaliacaoDiagnostico.vue", () => ({
    default: {template: "<div>AutoavaliacaoDiagnostico</div>"},
}));
vi.mock("../views/OcupacoesCriticasDiagnostico.vue", () => ({
    default: {template: "<div>OcupacoesCriticasDiagnostico</div>"},
}));
vi.mock("../views/UnidadeView.vue", () => ({
    default: {template: "<div>Unidade</div>"},
}));
vi.mock("../views/CadAtribuicao.vue", () => ({
    default: {template: "<div>CadAtribuicao</div>"},
}));
vi.mock("../views/HistoricoView.vue", () => ({
    default: {template: "<div>Historico</div>"},
}));
vi.mock("../views/RelatoriosView.vue", () => ({
    default: {template: "<div>Relatorios</div>"},
}));
vi.mock("../views/ConfiguracoesView.vue", () => ({
    default: {template: "<div>Configuracoes</div>"},
}));

describe("Router", () => {
    beforeEach(async () => {
        // Reset router to initial state if possible, or just push to root
        await router.push("/");
    });

    it("deve definir o título do documento após a navegação", async () => {
        await router.push("/login");
        expect(document.title).toBe("Login - SGC");

        await router.push("/painel");
        expect(document.title).toBe("Painel - SGC");
    });

    it("deve redirecionar da raiz para /login", async () => {
        await router.push("/");
        expect(router.currentRoute.value.path).toBe("/login");
    });

    it("deve resolver a rota de Login", async () => {
        await router.push("/login");
        const route = router.currentRoute.value;
        expect(route.name).toBe("Login");
        expect(route.meta.title).toBe("Login");
    });

    it("deve resolver a rota de Painel com props", async () => {
        await router.push("/painel");
        const route = router.currentRoute.value;
        expect(route.name).toBe("Painel");
        expect(route.meta.breadcrumb).toBe("Painel");
    });

    it("deve resolver a rota de Subprocesso com props dinâmicos", async () => {
        await router.push("/processo/123/DTI");
        const route = router.currentRoute.value;
        expect(route.name).toBe("Subprocesso");
        expect(route.params.codProcesso).toBe("123");
        expect(route.params.siglaUnidade).toBe("DTI");

        // Test props function
        const matched = route.matched.find((m) => m.name === "Subprocesso");
        expect(matched).toBeDefined();
        if (matched && typeof matched.props.default === "function") {
            const props = matched.props.default(route);
            expect(props).toEqual({
                codProcesso: 123,
                siglaUnidade: "DTI",
            });
        }

        // Test breadcrumb function
        const breadcrumb = route.meta.breadcrumb;
        if (typeof breadcrumb === "function") {
            expect(breadcrumb(route)).toBe("DTI");
        }
    });

    it("deve resolver a rota de SubprocessoMapa com props dinâmicos", async () => {
        await router.push("/processo/123/DTI/mapa");
        const route = router.currentRoute.value;
        expect(route.name).toBe("SubprocessoMapa");

        const matched = route.matched.find((m) => m.name === "SubprocessoMapa");
        expect(matched).toBeDefined();
        if (matched && typeof matched.props.default === "function") {
            const props = matched.props.default(route);
            expect(props).toEqual({
                codProcesso: 123,
                sigla: "DTI",
            });
        }
    });

    it("deve resolver a rota de SubprocessoVisMapa com props dinâmicos", async () => {
        await router.push("/processo/123/DTI/vis-mapa");
        const route = router.currentRoute.value;
        expect(route.name).toBe("SubprocessoVisMapa");

        const matched = route.matched.find((m) => m.name === "SubprocessoVisMapa");
        if (matched && typeof matched.props.default === "function") {
            const props = matched.props.default(route);
            expect(props).toEqual({
                codProcesso: 123,
                sigla: "DTI",
            });
        }
    });

    it("deve resolver a rota de SubprocessoCadastro com props dinâmicos", async () => {
        await router.push("/processo/123/DTI/cadastro");
        const route = router.currentRoute.value;
        expect(route.name).toBe("SubprocessoCadastro");

        const matched = route.matched.find((m) => m.name === "SubprocessoCadastro");
        if (matched && typeof matched.props.default === "function") {
            const props = matched.props.default(route);
            expect(props).toEqual({
                codProcesso: 123,
                sigla: "DTI",
            });
        }
    });

    it("deve resolver a rota de SubprocessoVisCadastro com props dinâmicos", async () => {
        await router.push("/processo/123/DTI/vis-cadastro");
        const route = router.currentRoute.value;
        expect(route.name).toBe("SubprocessoVisCadastro");

        const matched = route.matched.find(
            (m) => m.name === "SubprocessoVisCadastro",
        );
        if (matched && typeof matched.props.default === "function") {
            const props = matched.props.default(route);
            expect(props).toEqual({
                codProcesso: 123,
                sigla: "DTI",
            });
        }
    });

    it("deve resolver a rota de AutoavaliacaoDiagnostico com props dinâmicos", async () => {
        await router.push("/diagnostico/123/autoavaliacao");
        const route = router.currentRoute.value;
        expect(route.name).toBe("AutoavaliacaoDiagnostico");

        const matched = route.matched.find((m) => m.name === "AutoavaliacaoDiagnostico");
        // props: true means params are passed as props
        expect(route.params.codSubprocesso).toBe("123");
    });

    it("deve resolver a rota de OcupacoesCriticasDiagnostico com props dinâmicos", async () => {
        await router.push("/diagnostico/123/ocupacoes");
        const route = router.currentRoute.value;
        expect(route.name).toBe("OcupacoesCriticasDiagnostico");

        const matched = route.matched.find((m) => m.name === "OcupacoesCriticasDiagnostico");
        expect(route.params.codSubprocesso).toBe("123");
        // OcupacoesCriticasDiagnostico route does not use siglaUnidade in path
    });

    it("deve resolver a rota de Unidade com breadcrumb dinâmico", async () => {
        await router.push("/unidade/DTI");
        const route = router.currentRoute.value;
        expect(route.name).toBe("Unidade");

        const breadcrumb = route.meta.breadcrumb;
        if (typeof breadcrumb === "function") {
            expect(breadcrumb(route)).toBe("DTI");
        }
    });

    it("deve resolver a rota de Mapa de Unidade com props via query", async () => {
        await router.push("/unidade/DTI/mapa?idProcesso=999");
        const route = router.currentRoute.value;
        expect(route.name).toBe("Mapa");

        const matched = route.matched.find((m) => m.name === "Mapa");
        if (matched && typeof matched.props.default === "function") {
            const props = matched.props.default(route);
            expect(props).toEqual({
                sigla: "DTI",
                idProcesso: 999,
            });
        }
    });

    it("deve resolver a rota de AtribuicaoTemporariaForm", async () => {
        await router.push("/unidade/DTI/atribuicao");
        const route = router.currentRoute.value;
        expect(route.name).toBe("AtribuicaoTemporariaForm");

        const matched = route.matched.find(
            (m) => m.name === "AtribuicaoTemporariaForm",
        );
        if (matched && typeof matched.props.default === "function") {
            const props = matched.props.default(route);
            expect(props).toEqual({
                sigla: "DTI",
            });
        }
    });

    it("deve usar o nome da rota como título se meta.title não estiver definido", async () => {
        // Simulando uma rota sem meta.title, se houvesse.
        // Mas todas as rotas têm meta.title.
        // Vamos adicionar uma rota dinâmica para testar isso se possível,
        // ou apenas confiar que a lógica está coberta se houver algum branch.
        // O código é: const titleBase = typeof meta.title === 'string' ? meta.title : (to.name as string) || 'SGC'

        router.addRoute({
            path: "/teste-sem-titulo",
            name: "TesteSemTitulo",
            component: {template: "<div>Teste</div>"},
        });

        await router.push("/teste-sem-titulo");
        expect(document.title).toBe("TesteSemTitulo - SGC");
    });
});
