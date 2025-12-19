import { describe } from "vitest";
import { setupServiceTest, testErrorHandling, testPostEndpoint } from "../../test-utils/serviceTestHelpers";
import * as cadastroService from "@/services/cadastroService";

describe("cadastroService", () => {
    setupServiceTest();

    describe("disponibilizarCadastro", () => {
        testPostEndpoint(
            () => cadastroService.disponibilizarCadastro(1),
            "/subprocessos/1/cadastro/disponibilizar"
        );
        testErrorHandling(() => cadastroService.disponibilizarCadastro(1), 'post');
    });

    describe("disponibilizarRevisaoCadastro", () => {
        testPostEndpoint(
            () => cadastroService.disponibilizarRevisaoCadastro(1),
            "/subprocessos/1/disponibilizar-revisao"
        );
        testErrorHandling(() => cadastroService.disponibilizarRevisaoCadastro(1), 'post');
    });

    describe("devolverCadastro", () => {
        const payload = { observacoes: "teste" };
        testPostEndpoint(
            () => cadastroService.devolverCadastro(1, payload),
            "/subprocessos/1/devolver-cadastro",
            payload
        );
        testErrorHandling(() => cadastroService.devolverCadastro(1, payload), 'post');
    });

    describe("aceitarCadastro", () => {
        const payload = { observacoes: "teste" };
        testPostEndpoint(
            () => cadastroService.aceitarCadastro(1, payload),
            "/subprocessos/1/aceitar-cadastro",
            payload
        );
        testErrorHandling(() => cadastroService.aceitarCadastro(1, payload), 'post');
    });

    describe("homologarCadastro", () => {
        const payload = { observacoes: "teste" };
        testPostEndpoint(
            () => cadastroService.homologarCadastro(1, payload),
            "/subprocessos/1/homologar-cadastro",
            payload
        );
        testErrorHandling(() => cadastroService.homologarCadastro(1, payload), 'post');
    });

    describe("devolverRevisaoCadastro", () => {
        const payload = { observacoes: "teste" };
        testPostEndpoint(
            () => cadastroService.devolverRevisaoCadastro(1, payload),
            "/subprocessos/1/devolver-revisao-cadastro",
            payload
        );
        testErrorHandling(() => cadastroService.devolverRevisaoCadastro(1, payload), 'post');
    });

    describe("aceitarRevisaoCadastro", () => {
        const payload = { observacoes: "teste" };
        testPostEndpoint(
            () => cadastroService.aceitarRevisaoCadastro(1, payload),
            "/subprocessos/1/aceitar-revisao-cadastro",
            payload
        );
        testErrorHandling(() => cadastroService.aceitarRevisaoCadastro(1, payload), 'post');
    });

    describe("homologarRevisaoCadastro", () => {
        const payload = { observacoes: "teste" };
        testPostEndpoint(
            () => cadastroService.homologarRevisaoCadastro(1, payload),
            "/subprocessos/1/homologar-revisao-cadastro",
            payload
        );
        testErrorHandling(() => cadastroService.homologarRevisaoCadastro(1, payload), 'post');
    });
});
