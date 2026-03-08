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
});
