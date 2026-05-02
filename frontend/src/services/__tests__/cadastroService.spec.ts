import {beforeEach, describe, expect, it, vi} from "vitest";
import apiClient from "@/axios-setup";
import * as cadastroService from "../cadastroService";

vi.mock("@/axios-setup", () => ({
    default: {
        post: vi.fn(),
    },
}));

describe("cadastroService", () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    const postMock = vi.mocked(apiClient.post);

    it("chama os endpoints de cadastro", async () => {
        postMock.mockResolvedValue({data: undefined} as never);

        await cadastroService.disponibilizarCadastro(1);
        await cadastroService.iniciarRevisaoCadastro(1);
        await cadastroService.cancelarInicioRevisaoCadastro(1);
        await cadastroService.disponibilizarRevisaoCadastro(1);
        await cadastroService.devolverCadastro(1, {observacoes: "Obs"});
        await cadastroService.aceitarCadastro(1, {observacoes: "Obs"});
        await cadastroService.homologarCadastro(1, {observacoes: "Obs"});
        await cadastroService.devolverRevisaoCadastro(1, {observacoes: "Obs"});
        await cadastroService.aceitarRevisaoCadastro(1, {observacoes: "Obs"});
        await cadastroService.homologarRevisaoCadastro(1, {observacoes: "Obs"});

        expect(apiClient.post).toHaveBeenCalledWith("/subprocessos/1/cadastro/disponibilizar");
        expect(apiClient.post).toHaveBeenCalledWith("/subprocessos/1/iniciar-revisao-cadastro");
        expect(apiClient.post).toHaveBeenCalledWith("/subprocessos/1/cancelar-inicio-revisao-cadastro");
        expect(apiClient.post).toHaveBeenCalledWith("/subprocessos/1/disponibilizar-revisao");
        expect(apiClient.post).toHaveBeenCalledWith("/subprocessos/1/devolver-cadastro", {justificativa: "Obs"});
        expect(apiClient.post).toHaveBeenCalledWith("/subprocessos/1/aceitar-cadastro", {texto: "Obs"});
        expect(apiClient.post).toHaveBeenCalledWith("/subprocessos/1/homologar-cadastro", {texto: "Obs"});
        expect(apiClient.post).toHaveBeenCalledWith("/subprocessos/1/devolver-revisao-cadastro", {justificativa: "Obs"});
        expect(apiClient.post).toHaveBeenCalledWith("/subprocessos/1/aceitar-revisao-cadastro", {texto: "Obs"});
        expect(apiClient.post).toHaveBeenCalledWith("/subprocessos/1/homologar-revisao-cadastro", {texto: "Obs"});
    });
});
