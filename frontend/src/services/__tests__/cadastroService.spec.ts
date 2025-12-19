import { describe } from "vitest";
import { setupServiceTest, testPostEndpoint } from "../../test-utils/serviceTestHelpers";
import * as cadastroService from "@/services/cadastroService";

describe("cadastroService", () => {
    setupServiceTest();

    describe("disponibilizarCadastro", () => {
        testPostEndpoint(
            () => cadastroService.disponibilizarCadastro(1),
            "/subprocessos/1/cadastro/disponibilizar"
        );
    });

    describe("disponibilizarRevisaoCadastro", () => {
        testPostEndpoint(
            () => cadastroService.disponibilizarRevisaoCadastro(1),
            "/subprocessos/1/disponibilizar-revisao"
        );
    });

    describe("devolverCadastro", () => {
        const payload = { observacoes: "teste" };
        testPostEndpoint(
            () => cadastroService.devolverCadastro(1, payload),
            "/subprocessos/1/devolver-cadastro",
            payload
        );
    });

    describe("aceitarCadastro", () => {
        const payload = { observacoes: "teste" };
        testPostEndpoint(
            () => cadastroService.aceitarCadastro(1, payload),
            "/subprocessos/1/aceitar-cadastro",
            payload
        );
    });

    describe("homologarCadastro", () => {
        const payload = { observacoes: "teste" };
        testPostEndpoint(
            () => cadastroService.homologarCadastro(1, payload),
            "/subprocessos/1/homologar-cadastro",
            payload
        );
    });

    describe("devolverRevisaoCadastro", () => {
        const payload = { observacoes: "teste" };
        testPostEndpoint(
            () => cadastroService.devolverRevisaoCadastro(1, payload),
            "/subprocessos/1/devolver-revisao-cadastro",
            payload
        );
    });

    describe("aceitarRevisaoCadastro", () => {
        const payload = { observacoes: "teste" };
        testPostEndpoint(
            () => cadastroService.aceitarRevisaoCadastro(1, payload),
            "/subprocessos/1/aceitar-revisao-cadastro",
            payload
        );
    });

    describe("homologarRevisaoCadastro", () => {
        const payload = { observacoes: "teste" };
        testPostEndpoint(
            () => cadastroService.homologarRevisaoCadastro(1, payload),
            "/subprocessos/1/homologar-revisao-cadastro",
            payload
        );
    });
});
