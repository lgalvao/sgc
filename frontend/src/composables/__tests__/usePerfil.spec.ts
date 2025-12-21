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
            obterUsuarioPorId: (id: number) => ({ id, nome: "Usuário Teste"}),
        } as any);

        const {servidorLogado} = usePerfil();

        expect(servidorLogado.value).toEqual({
            id: 1,
            nome: "Usuário Teste",
            perfil: Perfil.GESTOR,
            unidade: 456,
        });
    });

    it("deve retornar null se servidorLogado não encontrar usuário", () => {
        vi.mocked(usePerfilStore).mockReturnValue({
            usuarioCodigo: 999,
        } as any);

        vi.mocked(useUsuariosStore).mockReturnValue({
            obterUsuarioPorId: () => null,
        } as any);

        const {servidorLogado} = usePerfil();

        expect(servidorLogado.value).toBeNull();
    });

    it("deve retornar unidadeSelecionada como ID se sigla não encontrada e unidades vazias", () => {
        vi.mocked(usePerfilStore).mockReturnValue({
            unidadeSelecionadaSigla: null,
            unidadeSelecionada: 123,
        } as any);

        vi.mocked(useUnidadesStore).mockReturnValue({
            unidades: [],
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

    it("deve achatar hierarquia de unidades corretamente", () => {
        vi.mocked(usePerfilStore).mockReturnValue({
            unidadeSelecionada: 3,
            unidadeSelecionadaSigla: null,
        } as any);

        vi.mocked(useUnidadesStore).mockReturnValue({
            unidades: [
                {
                    codigo: 1,
                    sigla: "PAI",
                    filhas: [
                        {
                            codigo: 2,
                            sigla: "FILHO",
                            filhas: [
                                { codigo: 3, sigla: "NETO" }
                            ]
                        }
                    ]
                }
            ],
        } as any);

        const {unidadeSelecionada} = usePerfil();
        // A lógica do computed 'unidadeSelecionada' usa flattenUnidades internamente.
        // Se encontrar o ID 3 (NETO), deve retornar "NETO".
        expect(unidadeSelecionada.value).toBe("NETO");
    });
});
