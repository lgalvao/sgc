import {beforeEach, describe, expect, it, vi} from "vitest";
import {usePerfilStore} from "@/stores/perfil";
import {useUnidadesStore} from "@/stores/unidades";
import {useUsuariosStore} from "@/stores/usuarios";
import {initPinia} from "@/test-utils/helpers";
import {Perfil} from "@/types/tipos";
import {usePerfil} from "../usePerfil";

vi.mock("@/stores/perfil");
vi.mock("@/stores/usuarios");
vi.mock("@/stores/unidades");

describe("usePerfil", () => {
    beforeEach(() => {
        initPinia();
        vi.clearAllMocks();
    });

    it("deve retornar o perfil e a unidade selecionada corretamente", () => {
        vi.mocked(usePerfilStore).mockReturnValue({
            perfilSelecionado: Perfil.CHEFE,
            unidadeSelecionada: 123,
        } as any);

        vi.mocked(useUnidadesStore).mockReturnValue({
            unidades: [{codigo: 123, sigla: "TESTE"}],
        } as any);

        const {perfilSelecionado, unidadeSelecionada} = usePerfil();

        expect(perfilSelecionado.value).toBe(Perfil.CHEFE);
        expect(unidadeSelecionada.value).toBe("TESTE");
    });

    it("deve retornar o servidor logado com os dados corretos", () => {
        vi.mocked(usePerfilStore).mockReturnValue({
            usuarioCodigo: 1,
            perfilSelecionado: Perfil.GESTOR,
            unidadeSelecionada: 456,
        } as any);

        vi.mocked(useUsuariosStore).mockReturnValue({
            obterUsuarioPorId: (id: number) => ({id, nome: "Usuário Teste"}),
        } as any);

        const {servidorLogado} = usePerfil();

        expect(servidorLogado.value).toEqual({
            id: 1,
            nome: "Usuário Teste",
            perfil: Perfil.GESTOR,
            unidade: 456,
        });
    });
});
