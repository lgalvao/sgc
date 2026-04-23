import {describe, expect, it} from "vitest";
import {setupServiceTest, testErrorHandling, testPostEndpoint} from "@/test-utils/serviceTestHelpers";
import * as cadastroService from "@/services/cadastroService";

describe("cadastroService", () => {
    const {mockApi} = setupServiceTest();

    describe("disponibilizarCadastro", () => {
        testPostEndpoint(
            () => cadastroService.disponibilizarCadastro(1),
            "/subprocessos/1/cadastro/disponibilizar",
            {observacoes: ""}
        );
        testErrorHandling(() => cadastroService.disponibilizarCadastro(1), 'post');
    });

    describe("disponibilizarRevisaoCadastro", () => {
        testPostEndpoint(
            () => cadastroService.disponibilizarRevisaoCadastro(1),
            "/subprocessos/1/disponibilizar-revisao",
            {observacoes: ""}
        );
        testErrorHandling(() => cadastroService.disponibilizarRevisaoCadastro(1), 'post');
    });

    describe("issue #1553 - observações na disponibilização", () => {
        it("deve enviar observações em disponibilizarCadastro", async () => {
            mockApi.post.mockResolvedValue({data: {}});

            await cadastroService.disponibilizarCadastro(1, {observacoes: "observação de teste"});

            expect(mockApi.post).toHaveBeenCalledWith("/subprocessos/1/cadastro/disponibilizar", {
                observacoes: "observação de teste"
            });
        });

        it("deve enviar observações em disponibilizarRevisaoCadastro", async () => {
            mockApi.post.mockResolvedValue({data: {}});

            await cadastroService.disponibilizarRevisaoCadastro(1, {observacoes: "observação de revisão"});

            expect(mockApi.post).toHaveBeenCalledWith("/subprocessos/1/disponibilizar-revisao", {
                observacoes: "observação de revisão"
            });
        });
    });

    describe("devolverCadastro", () => {
        const input = {observacoes: "teste"};
        testPostEndpoint(
            () => cadastroService.devolverCadastro(1, input),
            "/subprocessos/1/devolver-cadastro",
            {justificativa: "teste"}
        );
        testErrorHandling(() => cadastroService.devolverCadastro(1, input), 'post');
    });

    describe("aceitarCadastro", () => {
        const input = {observacoes: "teste"};
        testPostEndpoint(
            () => cadastroService.aceitarCadastro(1, input),
            "/subprocessos/1/aceitar-cadastro",
            {texto: "teste"}
        );
        testErrorHandling(() => cadastroService.aceitarCadastro(1, input), 'post');
    });

    describe("homologarCadastro", () => {
        const input = {observacoes: "teste"};
        testPostEndpoint(
            () => cadastroService.homologarCadastro(1, input),
            "/subprocessos/1/homologar-cadastro",
            {texto: "teste"}
        );
        testErrorHandling(() => cadastroService.homologarCadastro(1, input), 'post');
    });

    describe("devolverRevisaoCadastro", () => {
        const input = {observacoes: "teste"};
        testPostEndpoint(
            () => cadastroService.devolverRevisaoCadastro(1, input),
            "/subprocessos/1/devolver-revisao-cadastro",
            {justificativa: "teste"}
        );
        testErrorHandling(() => cadastroService.devolverRevisaoCadastro(1, input), 'post');
    });

    describe("aceitarRevisaoCadastro", () => {
        const input = {observacoes: "teste"};
        testPostEndpoint(
            () => cadastroService.aceitarRevisaoCadastro(1, input),
            "/subprocessos/1/aceitar-revisao-cadastro",
            {texto: "teste"}
        );
        testErrorHandling(() => cadastroService.aceitarRevisaoCadastro(1, input), 'post');
    });

    describe("homologarRevisaoCadastro", () => {
        const input = {observacoes: "teste"};
        testPostEndpoint(
            () => cadastroService.homologarRevisaoCadastro(1, input),
            "/subprocessos/1/homologar-revisao-cadastro",
            {texto: "teste"}
        );
        testErrorHandling(() => cadastroService.homologarRevisaoCadastro(1, input), 'post');
    });
});
