import {beforeEach, describe, expect, it, vi} from "vitest";
import {usePerfilStore} from "@/stores/perfil";
import {initPinia} from "@/test-utils/helpers";
import {Perfil} from "@/types/tipos";
import {usePerfil} from "../usePerfil";

vi.mock("@/stores/perfil");

describe("usePerfil", () => {
    beforeEach(() => {
        initPinia();
        vi.clearAllMocks();
    });

    it("deve retornar o perfil e a unidade selecionada corretamente", () => {
        vi.mocked(usePerfilStore).mockReturnValue({
            perfilSelecionado: Perfil.CHEFE,
            unidadeSelecionada: 123,
            unidadeSelecionadaSigla: "TESTE",
        } as any);

        const {perfilSelecionado, unidadeSelecionada} = usePerfil();

        expect(perfilSelecionado.value).toBe(Perfil.CHEFE);
        expect(unidadeSelecionada.value).toBe("TESTE");
    });

    it("deve retornar unidadeSelecionada como ID se sigla não disponível", () => {
        vi.mocked(usePerfilStore).mockReturnValue({
            unidadeSelecionadaSigla: null,
            unidadeSelecionada: 123,
        } as any);

        const {unidadeSelecionada} = usePerfil();

        expect(unidadeSelecionada.value).toBe(123);
    });

    it("deve usar unidadeSelecionadaSigla se disponível", () => {
        vi.mocked(usePerfilStore).mockReturnValue({
            unidadeSelecionadaSigla: "SIGLA_EXISTENTE",
            unidadeSelecionada: 123,
        } as any);

        const {unidadeSelecionada} = usePerfil();

        expect(unidadeSelecionada.value).toBe("SIGLA_EXISTENTE");
    });

    it("deve identificar corretamente os perfis", () => {
        const mockStore = {
            perfilSelecionado: Perfil.ADMIN,
            unidadeSelecionada: 1
        };
        vi.mocked(usePerfilStore).mockReturnValue(mockStore as any);

        const {isAdmin, isGestor, isChefe, isServidor} = usePerfil();

        expect(isAdmin.value).toBe(true);
        expect(isGestor.value).toBe(false);
        expect(isChefe.value).toBe(false);
        expect(isServidor.value).toBe(false);
    });

    it("deve identificar perfil servidor e gestor", () => {
        vi.mocked(usePerfilStore).mockReturnValue({perfilSelecionado: Perfil.SERVIDOR} as any);
        const {isServidor, isGestor} = usePerfil();
        expect(isServidor.value).toBe(true);
        expect(isGestor.value).toBe(false);

        vi.mocked(usePerfilStore).mockReturnValue({perfilSelecionado: Perfil.GESTOR} as any);
        const {isGestor: isGestor2} = usePerfil();
        expect(isGestor2.value).toBe(true);
    });

    it("deve calcular permissões baseadas no perfil ADMIN", () => {
        vi.mocked(usePerfilStore).mockReturnValue({perfilSelecionado: Perfil.ADMIN} as any);
        const {podeCriarProcesso, podeAcessarTodasUnidades, podeVisualizarTabelaCtaVazio, podeAcessoGeralAdminGestor} = usePerfil();

        expect(podeCriarProcesso.value).toBe(true);
        expect(podeAcessarTodasUnidades.value).toBe(true);
        expect(podeVisualizarTabelaCtaVazio.value).toBe(true);
        expect(podeAcessoGeralAdminGestor.value).toBe(true);
    });

    it("deve calcular podeAcessoGeralAdminGestor para GESTOR", () => {
        vi.mocked(usePerfilStore).mockReturnValue({perfilSelecionado: Perfil.GESTOR} as any);
        const {podeAcessoGeralAdminGestor} = usePerfil();
        expect(podeAcessoGeralAdminGestor.value).toBe(true);
    });

    it("deve retornar nulo se unidadeSelecionada e sigla forem nulas", () => {
        vi.mocked(usePerfilStore).mockReturnValue({
            unidadeSelecionadaSigla: null,
            unidadeSelecionada: null,
        } as any);

        const {unidadeSelecionada} = usePerfil();

        expect(unidadeSelecionada.value).toBeNull();
    });
});
