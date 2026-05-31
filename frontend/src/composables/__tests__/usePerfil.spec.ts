import {beforeEach, describe, expect, it, vi} from "vitest";
import {usePerfilStore} from "@/stores/perfil";
import {initPinia} from "@/test-utils/helpers";
import {Perfil} from "@/types/tipos";
import {usePerfil} from "../usePerfil";

vi.mock("@/stores/perfil");

type PerfilStore = ReturnType<typeof usePerfilStore>;

const permissoesAdmin = {
    mostrarCriarProcesso: true,
    mostrarArvoreCompletaUnidades: true,
    mostrarCtaPainelVazio: true,
    mostrarDiagnosticoOrganizacional: true,
    mostrarMenuConfiguracoes: true,
    mostrarMenuAdministradores: true,
    mostrarCriarAtribuicaoTemporaria: true,
};

function criarStoreMock(parcial: Partial<PerfilStore>): PerfilStore {
    return {
        perfilSelecionado: null,
        unidadeSelecionada: null,
        unidadeSelecionadaSigla: null,
        permissoesSessao: null,
        ...parcial,
    } as PerfilStore;
}

describe("usePerfil", () => {
    beforeEach(() => {
        initPinia();
        vi.clearAllMocks();
    });

    it("deve retornar o perfil e a unidade selecionada corretamente", () => {
        vi.mocked(usePerfilStore).mockReturnValue(criarStoreMock({
            perfilSelecionado: Perfil.CHEFE,
            unidadeSelecionada: 123,
            unidadeSelecionadaSigla: "TESTE",
        }));

        const {perfilSelecionado, unidadeSelecionada} = usePerfil();

        expect(perfilSelecionado.value).toBe(Perfil.CHEFE);
        expect(unidadeSelecionada.value).toBe("TESTE");
    });

    it("deve retornar nulo se sigla não estiver disponível", () => {
        vi.mocked(usePerfilStore).mockReturnValue(criarStoreMock({
            unidadeSelecionada: 123,
        }));

        const {unidadeSelecionada} = usePerfil();

        expect(unidadeSelecionada.value).toBeNull();
    });

    it("deve usar unidadeSelecionadaSigla se disponível", () => {
        vi.mocked(usePerfilStore).mockReturnValue(criarStoreMock({
            unidadeSelecionadaSigla: "SIGLA_EXISTENTE",
            unidadeSelecionada: 123,
        }));

        const {unidadeSelecionada} = usePerfil();

        expect(unidadeSelecionada.value).toBe("SIGLA_EXISTENTE");
    });

    it("deve identificar corretamente o perfil admin para exibição", () => {
        vi.mocked(usePerfilStore).mockReturnValue(criarStoreMock({
            perfilSelecionado: Perfil.ADMIN,
            unidadeSelecionada: 1,
            permissoesSessao: permissoesAdmin,
        }));

        const {ehAdmin} = usePerfil();

        expect(ehAdmin.value).toBe(true);
    });

    it("deve permitir relatorios para admin e gestor", () => {
        vi.mocked(usePerfilStore).mockReturnValue(criarStoreMock({
            perfilSelecionado: Perfil.GESTOR,
        }));

        expect(usePerfil().podeVerRelatorios.value).toBe(true);

        vi.mocked(usePerfilStore).mockReturnValue(criarStoreMock({
            perfilSelecionado: Perfil.CHEFE,
        }));

        expect(usePerfil().podeVerRelatorios.value).toBe(false);
    });

    it("deve expor permissões de sessão vindas do backend", () => {
        vi.mocked(usePerfilStore).mockReturnValue(criarStoreMock({
            perfilSelecionado: Perfil.ADMIN,
            permissoesSessao: permissoesAdmin,
        }));
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
        vi.mocked(usePerfilStore).mockReturnValue(criarStoreMock({
            unidadeSelecionada: null,
            unidadeSelecionadaSigla: null,
        }));

        const {unidadeSelecionada} = usePerfil();

        expect(unidadeSelecionada.value).toBeNull();
    });
});
