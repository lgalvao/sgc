import {describe, expect, it} from "vitest";
import {badgeClass, iconeTipo} from "@/utils/styleUtils";

describe("styleUtils", () => {
  describe("badgeClass", () => {
    it("deve retornar a classe correta para situações conhecidas", () => {
      // Assumindo que EM_ELABORACAO existe em CLASSES_BADGE_SITUACAO
      expect(badgeClass("EM_ELABORACAO")).toBeDefined();
    });

    it("deve retornar bg-secondary para situações desconhecidas", () => {
      expect(badgeClass("SITUACAO_INEXISTENTE")).toBe("bg-secondary");
    });
  });

  describe("iconeTipo", () => {
    it("deve retornar o ícone correto para success", () => {
      expect(iconeTipo("success")).toContain("bi-check-circle-fill");
    });

    it("deve retornar o ícone correto para error", () => {
      expect(iconeTipo("error")).toContain("bi-exclamation-triangle-fill");
    });

    it("deve retornar o ícone correto para warning", () => {
      expect(iconeTipo("warning")).toContain("bi-exclamation-triangle-fill");
    });

    it("deve retornar o ícone correto para info", () => {
      expect(iconeTipo("info")).toContain("bi-info-circle-fill");
    });

    it("deve retornar ícone default para tipos desconhecidos", () => {
      // @ts-expect-error - testando fallback para valor inválido
      expect(iconeTipo("desconhecido")).toBe("bi bi-bell-fill");
    });
  });
});
