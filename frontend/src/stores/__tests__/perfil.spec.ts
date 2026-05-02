import {beforeEach, describe, expect, it, vi} from "vitest";
import {createPinia, setActivePinia} from "pinia";
import {usePerfilStore} from "../perfil";
import * as usuarioService from "../../services/usuarioService";
import {Perfil} from "@/types/tipos";
import {ref} from "vue";

vi.mock("../../services/usuarioService", () => ({
    login: vi.fn(),
    entrar: vi.fn(),
    logout: vi.fn(),
}));

vi.mock("@/composables/useLocalStorage", () => ({
    useLocalStorage: vi.fn((key, initial) => ref(initial)),
}));

vi.mock("@/composables/useSessionStorage", () => ({
    useSessionStorage: vi.fn((key, initial) => ref(initial)),
}));

describe("usePerfilStore", () => {
    beforeEach(() => {
        setActivePinia(createPinia());
        vi.clearAllMocks();
    });

    it("deve definir usuarioCodigo", () => {
        const store = usePerfilStore();
        store.definirUsuarioCodigo("123456");
        expect(store.usuarioCodigo).toBe("123456");
    });

    it("deve definir perfil e unidade", () => {
        const store = usePerfilStore();
        store.definirPerfilUnidade({
            perfil: Perfil.GESTOR,
            unidadeCodigo: 1,
            unidadeSigla: "U1",
            permissoes: { acoes: [] } as any,
            nome: "Usuario Teste"
        });
        expect(store.perfilSelecionado).toBe(Perfil.GESTOR);
        expect(store.unidadeSelecionada).toBe(1);
        expect(store.unidadeSelecionadaSigla).toBe("U1");
        expect(store.usuarioNome).toBe("Usuario Teste");
    });

    it("deve realizar login com sucesso e definir perfil se único", async () => {
        const store = usePerfilStore();
        const mockFluxo = {
            autenticado: true,
            requerSelecaoPerfil: false,
            perfisUnidades: [{ perfil: Perfil.GESTOR, unidade: { codigo: 1, sigla: "U1" } }],
            sessao: { perfil: Perfil.GESTOR, unidadeCodigo: 1, tituloEleitoral: "123", nome: "Teste", permissoes: { acoes: [] } }
        };
        vi.mocked(usuarioService.login).mockResolvedValue(mockFluxo as any);

        const res = await store.iniciarLogin("123", "senha");
        expect(res).toEqual(mockFluxo);
        expect(store.perfilSelecionado).toBe(Perfil.GESTOR);
    });

    it("deve tratar erro 404 no login", async () => {
        const store = usePerfilStore();
        vi.mocked(usuarioService.login).mockRejectedValue({ response: { status: 404 } });

        const res = await store.iniciarLogin("123", "senha");
        expect(res.autenticado).toBe(false);
    });

    it("deve relançar erro genérico no login", async () => {
        const store = usePerfilStore();
        vi.mocked(usuarioService.login).mockRejectedValue(new Error("Erro grave"));

        await expect(store.iniciarLogin("123", "senha")).rejects.toThrow("Erro grave");
    });

    it("deve concluir login com perfil", async () => {
        const store = usePerfilStore();
        const mockSessao = { perfil: Perfil.GESTOR, unidadeCodigo: 1, tituloEleitoral: "123", nome: "Teste", permissoes: { acoes: [] } };
        vi.mocked(usuarioService.entrar).mockResolvedValue(mockSessao as any);

        await store.concluirLoginComPerfil({ perfil: Perfil.GESTOR, unidade: { codigo: 1, sigla: "U1" } } as any);
        expect(store.perfilSelecionado).toBe(Perfil.GESTOR);
    });

    it("deve realizar logout", async () => {
        const store = usePerfilStore();
        store.usuarioCodigo = "123" as any;
        
        await store.logout();
        
        expect(store.usuarioCodigo).toBeNull();
        expect(usuarioService.logout).toHaveBeenCalled();
    });

    it("unidadeAtual deve retornar valor correto", () => {
        const store = usePerfilStore();
        store.perfilSelecionado = Perfil.GESTOR as any;
        store.unidadeSelecionada = 50 as any;
        
        expect(store.unidadeAtual).toBe(50);
        
        store.unidadeSelecionada = null as any;
        store.perfisUnidades = [{ perfil: Perfil.GESTOR, unidade: { codigo: 60 } }] as any;
        expect(store.unidadeAtual).toBe(60);
    });
});
