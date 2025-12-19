import { describe } from "vitest";
import { setupServiceTest, testPostEndpoint, testErrorHandling } from "@/test-utils/serviceTestHelpers";
import * as AlertaService from "../alertaService";

describe("AlertaService", () => {
    setupServiceTest();

    describe("marcarComoLido", () => {
        testPostEndpoint(
            () => AlertaService.marcarComoLido(123),
            "/alertas/123/marcar-como-lido"
        );

        testErrorHandling(() => AlertaService.marcarComoLido(123), 'post');
    });
});
