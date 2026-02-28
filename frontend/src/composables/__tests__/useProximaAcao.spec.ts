import {describe, expect, it} from "vitest";
import {useProximaAcao} from "../useProximaAcao";

describe("useProximaAcao", () => {
    it("prioriza ação de finalização quando disponível", () => {
        const {obterProximaAcao} = useProximaAcao();
        const texto = obterProximaAcao({
            perfil: "ADMIN" as any,
            podeFinalizar: true,
            situacao: "EM_ANDAMENTO",
        });
        expect(texto).toContain("finalizar o processo");
    });

    it("retorna ação de acompanhamento quando sem permissões específicas", () => {
        const {obterProximaAcao} = useProximaAcao();
        const texto = obterProximaAcao({
            perfil: "GESTOR" as any,
            situacao: "MAPEAMENTO",
        });
        expect(texto).toContain("MAPEAMENTO");
    });
});
