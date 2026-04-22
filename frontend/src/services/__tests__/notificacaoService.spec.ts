import {beforeEach, describe, expect, it, vi} from "vitest";
import apiClient from "@/axios-setup";
import * as service from "../notificacaoService";

vi.mock("@/axios-setup");

describe("notificacaoService", () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    it("listarResumoSubprocessosAtivos deve chamar endpoint administrativo", async () => {
        const dados = [{subprocessoCodigo: 1, statusGeral: "PENDENTE"}];
        vi.mocked(apiClient.get).mockResolvedValue({data: dados});

        const resultado = await service.listarResumoSubprocessosAtivos();

        expect(apiClient.get).toHaveBeenCalledWith("/admin/notificacoes/subprocessos-ativos");
        expect(resultado).toEqual(dados);
    });

    it("reenviarFalhasDefinitivas deve chamar endpoint de reenvio do subprocesso", async () => {
        const resposta = {subprocessoCodigo: 1, reenfileiradas: 2};
        vi.mocked(apiClient.post).mockResolvedValue({data: resposta});

        const resultado = await service.reenviarFalhasDefinitivas(1);

        expect(apiClient.post).toHaveBeenCalledWith("/admin/notificacoes/subprocessos/1/reenviar");
        expect(resultado).toEqual(resposta);
    });
});
