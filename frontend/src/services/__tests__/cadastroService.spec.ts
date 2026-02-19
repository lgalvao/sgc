import {describe} from "vitest";
import {setupServiceTest, testErrorHandling, testPostEndpoint} from "@/test-utils/serviceTestHelpers";
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
        const input = { observacoes: "teste" };
        testPostEndpoint(
            () => cadastroService.devolverCadastro(1, input),
            "/subprocessos/1/devolver-cadastro",
            { justificativa: "teste" }
        );
        testErrorHandling(() => cadastroService.devolverCadastro(1, input), 'post');
    });

    describe("aceitarCadastro", () => {
        const input = { observacoes: "teste" };
        testPostEndpoint(
            () => cadastroService.aceitarCadastro(1, input),
            "/subprocessos/1/aceitar-cadastro",
            { texto: "teste" }
        );
        testErrorHandling(() => cadastroService.aceitarCadastro(1, input), 'post');
    });

    describe("homologarCadastro", () => {
        const input = { observacoes: "teste" };
        testPostEndpoint(
            () => cadastroService.homologarCadastro(1, input),
            "/subprocessos/1/homologar-cadastro",
            { texto: "teste" }
        );
        testErrorHandling(() => cadastroService.homologarCadastro(1, input), 'post');
    });

    describe("devolverRevisaoCadastro", () => {
        const input = { observacoes: "teste" };
        testPostEndpoint(
            () => cadastroService.devolverRevisaoCadastro(1, input),
            "/subprocessos/1/devolver-revisao-cadastro",
            { justificativa: "teste" }
        );
        testErrorHandling(() => cadastroService.devolverRevisaoCadastro(1, input), 'post');
    });

    describe("aceitarRevisaoCadastro", () => {
        const input = { observacoes: "teste" };
        testPostEndpoint(
            () => cadastroService.aceitarRevisaoCadastro(1, input),
            "/subprocessos/1/aceitar-revisao-cadastro",
            { texto: "teste" }
        );
        testErrorHandling(() => cadastroService.aceitarRevisaoCadastro(1, input), 'post');
    });

    describe("homologarRevisaoCadastro", () => {
        const input = { observacoes: "teste" };
        testPostEndpoint(
            () => cadastroService.homologarRevisaoCadastro(1, input),
            "/subprocessos/1/homologar-revisao-cadastro",
            { texto: "teste" }
        );
        testErrorHandling(() => cadastroService.homologarRevisaoCadastro(1, input), 'post');
    });
});
