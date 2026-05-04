import {beforeEach, describe, expect, it, vi} from "vitest";
import apiClient from "@/axios-setup";
import * as service from "../notificacaoService";

vi.mock("@/axios-setup");

describe("notificacaoService", () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    it("listarNotificacoesAdmin deve chamar endpoint administrativo", async () => {
        const dados = [{codigo: 1, destinatario: "teste@teste.com", situacao: "PENDENTE"}];
        vi.mocked(apiClient.get).mockResolvedValue({data: dados});

        const resultado = await service.listarNotificacoesAdmin(20);

        expect(apiClient.get).toHaveBeenCalledWith("/admin/notificacoes/listar", {
            params: {limite: 20}
        });
        expect(resultado).toEqual(dados);
    });

    it("reenviarNotificacao deve chamar endpoint de reenvio por código", async () => {
        const resposta = {codigo: 123, reenfileiradas: 1};
        vi.mocked(apiClient.post).mockResolvedValue({data: resposta});

        const resultado = await service.reenviarNotificacao(123);

        expect(apiClient.post).toHaveBeenCalledWith("/admin/notificacoes/123/reenviar");
        expect(resultado).toEqual(resposta);
    });
});
