import {beforeEach, describe, expect, it, vi} from "vitest";
import {usePerfilStore} from "@/stores/perfil";
import {initPinia} from "@/test-utils/helpers";
import {Perfil} from "@/types/tipos";
import {usePerfil} from "../usePerfil";

vi.mock("@/stores/perfil");

describe("usePerfil", () => {
    const permissoesAdmin = {
        mostrarCriarProcesso: true,
        mostrarArvoreCompletaUnidades: true,
        mostrarCtaPainelVazio: true,
        mostrarDiagnosticoOrganizacional: true,
        mostrarMenuConfiguracoes: true,
        mostrarMenuAdministradores: true,
        mostrarCriarAtribuicaoTemporaria: true,
    };

    beforeEach(() => {
        initPinia();
        vi.clearAllMocks();
    });

    it("deve retornar o perfil e a unidade selecionada corretamente", () => {
        vi.mocked(usePerfilStore).mockReturnValue({
            perfilSelecionado: Perfil.CHEFE,
            unidadeSelecionada: 123,
            unidadeSelecionadaSigla: "TESTE",
            permissoesSessao: null,
        } as any);

        const {perfilSelecionado, unidadeSelecionada} = usePerfil();

        expect(perfilSelecionado.value).toBe(Perfil.CHEFE);
        expect(unidadeSelecionada.value).toBe("TESTE");
    });

    it("deve retornar nulo se sigla não estiver disponível", () => {
        vi.mocked(usePerfilStore).mockReturnValue({
            unidadeSelecionadaSigla: null,
            unidadeSelecionada: 123,
            permissoesSessao: null,
        } as any);

        const {unidadeSelecionada} = usePerfil();

        expect(unidadeSelecionada.value).toBeNull();
    });

    it("deve usar unidadeSelecionadaSigla se disponível", () => {
        vi.mocked(usePerfilStore).mockReturnValue({
            unidadeSelecionadaSigla: "SIGLA_EXISTENTE",
            unidadeSelecionada: 123,
            permissoesSessao: null,
        } as any);

        const {unidadeSelecionada} = usePerfil();

        expect(unidadeSelecionada.value).toBe("SIGLA_EXISTENTE");
    });

    it("deve identificar corretamente o perfil admin para exibição", () => {
        const mockStore = {
            perfilSelecionado: Perfil.ADMIN,
            unidadeSelecionada: 1,
            permissoesSessao: permissoesAdmin,
        };
        vi.mocked(usePerfilStore).mockReturnValue(mockStore as any);

        const {isAdmin} = usePerfil();

        expect(isAdmin.value).toBe(true);
    });

    it("deve expor permissões de sessão vindas do backend", () => {
        vi.mocked(usePerfilStore).mockReturnValue({
            perfilSelecionado: Perfil.ADMIN,
            permissoesSessao: permissoesAdmin,
        } as any);
        const {
            mostrarCriarProcesso,
            mostrarArvoreCompletaUnidades,
            mostrarCtaPainelVazio,
            mostrarDiagnosticoOrganizacional,
            mostrarMenuConfiguracoes,
            mostrarMenuAdministradores,
            mostrarCriarAtribuicaoTemporaria
        } = usePerfil();

        expect(mostrarCriarProcesso.value).toBe(true);
        expect(mostrarArvoreCompletaUnidades.value).toBe(true);
        expect(mostrarCtaPainelVazio.value).toBe(true);
        expect(mostrarDiagnosticoOrganizacional.value).toBe(true);
        expect(mostrarMenuConfiguracoes.value).toBe(true);
        expect(mostrarMenuAdministradores.value).toBe(true);
        expect(mostrarCriarAtribuicaoTemporaria.value).toBe(true);
    });

    it("deve retornar nulo se unidadeSelecionada e sigla forem nulas", () => {
        vi.mocked(usePerfilStore).mockReturnValue({
            unidadeSelecionadaSigla: null,
            unidadeSelecionada: null,
            permissoesSessao: null,
        } as any);

        const {unidadeSelecionada} = usePerfil();

        expect(unidadeSelecionada.value).toBeNull();
    });
});
