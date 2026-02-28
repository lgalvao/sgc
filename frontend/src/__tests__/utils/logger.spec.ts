import {describe, expect, it, vi} from "vitest";
import logger, {getLogLevel} from "@/utils/logger";

// Unmock the logger to test the real implementation
vi.unmock("@/utils/logger");

describe("utils/logger.ts", () => {
    it("deve exportar uma instância do logger", () => {
        expect(logger).toBeDefined();
        expect(typeof logger.info).toBe("function");
        expect(typeof logger.error).toBe("function");
        expect(typeof logger.warn).toBe("function");
        expect(typeof logger.success).toBe("function");
    });

    describe("getLogLevel", () => {
        it("deve retornar 1 (ERROR) para ambiente de teste", () => {
            expect(getLogLevel("test")).toBe(1);
        });

        it("deve retornar 3 (WARN) para ambiente de produção", () => {
            expect(getLogLevel("production")).toBe(3);
        });

        it("deve retornar 4 (INFO) para ambiente de desenvolvimento", () => {
            expect(getLogLevel("development")).toBe(4);
        });

        it("deve retornar 4 (INFO) para outros ambientes", () => {
            expect(getLogLevel("staging")).toBe(4);
            expect(getLogLevel("")).toBe(4);
        });

        it("deve usar import.meta.env.MODE como padrão", () => {
            // Como estamos rodando em test, deve ser 1
            expect(getLogLevel()).toBe(1);
        });
    });
});