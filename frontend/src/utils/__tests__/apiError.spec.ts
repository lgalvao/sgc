import { describe, expect, it, vi } from "vitest";
import { normalizeError, notifyError, shouldNotifyGlobally, existsOrFalse, getOrNull, type NormalizedError } from "../apiError";
import { useFeedbackStore } from "@/stores/feedback";
import { createPinia, setActivePinia } from "pinia";

// Mock axios error
const createAxiosError = (status?: number, data?: any, noResponse = false) => {
  const error: any = new Error("Axios Error");
  error.isAxiosError = true;
  if (!noResponse) {
    error.response = {
      status,
      data
    };
  }
  return error;
};

describe("apiError.ts", () => {
  describe("normalizeError", () => {
    it("deve normalizar erro de rede (sem resposta)", () => {
      const error = createAxiosError(undefined, undefined, true);
      const normalized = normalizeError(error);
      expect(normalized.kind).toBe("network");
      expect(normalized.message).toBe("Não foi possível conectar ao servidor. Verifique sua conexão.");
    });

    it("deve normalizar erro 400 como validation", () => {
      const error = createAxiosError(400, { message: "Bad Request" });
      const normalized = normalizeError(error);
      expect(normalized.kind).toBe("validation");
      expect(normalized.status).toBe(400);
      expect(normalized.message).toBe("Bad Request");
    });

    it("deve normalizar erro 401 como unauthorized", () => {
      const error = createAxiosError(401);
      const normalized = normalizeError(error);
      expect(normalized.kind).toBe("unauthorized");
    });

    it("deve normalizar erro 403 como forbidden", () => {
        const error = createAxiosError(403);
        const normalized = normalizeError(error);
        expect(normalized.kind).toBe("forbidden");
    });

    it("deve normalizar erro 404 como notFound", () => {
      const error = createAxiosError(404);
      const normalized = normalizeError(error);
      expect(normalized.kind).toBe("notFound");
    });

    it("deve normalizar erro 409 como conflict", () => {
        const error = createAxiosError(409);
        const normalized = normalizeError(error);
        expect(normalized.kind).toBe("conflict");
    });

    it("deve normalizar erro 500 como unexpected", () => {
      const error = createAxiosError(500);
      const normalized = normalizeError(error);
      expect(normalized.kind).toBe("unexpected");
    });

    it("deve usar mensagem padrão se payload não tiver message", () => {
      const error = createAxiosError(400, {});
      const normalized = normalizeError(error);
      expect(normalized.message).toBe("Erro desconhecido.");
    });

    it("deve normalizar erro genérico (Error)", () => {
        const error = new Error("Generic Error");
        const normalized = normalizeError(error);
        expect(normalized.kind).toBe("unexpected");
        expect(normalized.message).toBe("Generic Error");
    });

    it("deve normalizar erro desconhecido (string/obj)", () => {
        const error = "Unknown string error";
        const normalized = normalizeError(error);
        expect(normalized.kind).toBe("unexpected");
        expect(normalized.message).toBe("Erro desconhecido.");
    });

    it("deve lidar com payload nulo em axios error", () => {
        const error = createAxiosError(400, null);
        const normalized = normalizeError(error);
        expect(normalized.message).toBe("Erro desconhecido.");
    });

    it("deve retornar unexpected para status desconhecido", () => {
        const error = createAxiosError(418); // I'm a teapot
        const normalized = normalizeError(error);
        expect(normalized.kind).toBe("unexpected");
    });
  });

  describe("notifyError", () => {
    beforeEach(() => {
        setActivePinia(createPinia());
    });

    it("deve chamar feedbackStore.show com mensagem correta", () => {
        const feedbackStore = useFeedbackStore();
        const spy = vi.spyOn(feedbackStore, 'show');

        const normalized: NormalizedError = {
            kind: 'validation',
            message: 'Erro de validação'
        };

        notifyError(normalized);
        expect(spy).toHaveBeenCalledWith('Erro de Validação', 'Erro de validação', 'danger');
    });

    it("deve mapear corretamente títulos para cada kind", () => {
        const feedbackStore = useFeedbackStore();
        const spy = vi.spyOn(feedbackStore, 'show');

        const kinds = ['validation', 'notFound', 'conflict', 'unauthorized', 'forbidden', 'network', 'unexpected'] as const;
        const titles = ['Erro de Validação', 'Não Encontrado', 'Conflito', 'Não Autorizado', 'Acesso Negado', 'Erro de Rede', 'Erro Inesperado'];

        kinds.forEach((kind, index) => {
            notifyError({ kind, message: 'msg' });
            expect(spy).toHaveBeenLastCalledWith(titles[index], 'msg', 'danger');
        });
    });
  });

  describe("shouldNotifyGlobally", () => {
      it("deve retornar true para unauthorized, forbidden, network, unexpected", () => {
          expect(shouldNotifyGlobally({ kind: 'unauthorized', message: '' })).toBe(true);
          expect(shouldNotifyGlobally({ kind: 'forbidden', message: '' })).toBe(true);
          expect(shouldNotifyGlobally({ kind: 'network', message: '' })).toBe(true);
          expect(shouldNotifyGlobally({ kind: 'unexpected', message: '' })).toBe(true);
      });

      it("deve retornar false para validation, notFound, conflict", () => {
          expect(shouldNotifyGlobally({ kind: 'validation', message: '' })).toBe(false);
          expect(shouldNotifyGlobally({ kind: 'notFound', message: '' })).toBe(false);
          expect(shouldNotifyGlobally({ kind: 'conflict', message: '' })).toBe(false);
      });
  });

  describe("existsOrFalse", () => {
      it("deve retornar true se apiCall resolver", async () => {
          const apiCall = vi.fn().mockResolvedValue('ok');
          expect(await existsOrFalse(apiCall)).toBe(true);
      });

      it("deve retornar false se apiCall rejeitar com 404", async () => {
          const apiCall = vi.fn().mockRejectedValue(createAxiosError(404));
          expect(await existsOrFalse(apiCall)).toBe(false);
      });

      it("deve lançar erro se apiCall rejeitar com outro erro", async () => {
          const error = createAxiosError(500);
          const apiCall = vi.fn().mockRejectedValue(error);
          await expect(existsOrFalse(apiCall)).rejects.toThrow();
      });
  });

  describe("getOrNull", () => {
      it("deve retornar valor se apiCall resolver", async () => {
          const apiCall = vi.fn().mockResolvedValue('value');
          expect(await getOrNull(apiCall)).toBe('value');
      });

      it("deve retornar null se apiCall rejeitar com 404", async () => {
          const apiCall = vi.fn().mockRejectedValue(createAxiosError(404));
          expect(await getOrNull(apiCall)).toBeNull();
      });

      it("deve lançar erro se apiCall rejeitar com outro erro", async () => {
          const error = createAxiosError(500);
          const apiCall = vi.fn().mockRejectedValue(error);
          await expect(getOrNull(apiCall)).rejects.toThrow();
      });
  });
});
