import {describe, expect, it} from "vitest";
import {
    getNotificacaoBadgeVariant,
    getNotificacaoStatusInfo,
    getProcessoBadgeVariant,
    getSubprocessoBadgeVariant,
    STATUS_NOTIFICACAO_INFO
} from "../statusHelpers";
import {SituacaoProcesso, SituacaoSubprocesso} from "@/types/tipos";

describe("statusHelpers", () => {
    describe("getProcessoBadgeVariant", () => {
        it("deve retornar 'secondary' se a situação for nula ou indefinida", () => {
            expect(getProcessoBadgeVariant(null)).toBe("secondary");
            expect(getProcessoBadgeVariant(undefined)).toBe("secondary");
        });

        it("deve retornar a variante correta para situações conhecidas", () => {
            expect(getProcessoBadgeVariant(SituacaoProcesso.FINALIZADO)).toBe("success");
            expect(getProcessoBadgeVariant(SituacaoProcesso.EM_ANDAMENTO)).toBe("primary");
            expect(getProcessoBadgeVariant(SituacaoProcesso.CRIADO)).toBe("secondary");
        });

        it("deve retornar 'dark' para situações desconhecidas", () => {
            expect(getProcessoBadgeVariant("QUALQUER_OUTRA")).toBe("dark");
        });
    });

    describe("getSubprocessoBadgeVariant", () => {
        it("deve retornar 'secondary' se a situação for nula ou indefinida", () => {
            expect(getSubprocessoBadgeVariant(null)).toBe("secondary");
            expect(getSubprocessoBadgeVariant(undefined)).toBe("secondary");
        });

        it("deve retornar 'success' para situações de homologação", () => {
            expect(getSubprocessoBadgeVariant("HOMOLOGADO")).toBe("success");
            expect(getSubprocessoBadgeVariant("HOMOLOGADA")).toBe("success");
        });

        it("deve retornar 'info' para situações de validação", () => {
            expect(getSubprocessoBadgeVariant("VALIDADO")).toBe("info");
            expect(getSubprocessoBadgeVariant("VALIDADA")).toBe("info");
        });

        it("deve retornar 'warning' para situações de disponibilização", () => {
            expect(getSubprocessoBadgeVariant("DISPONIBILIZADO")).toBe("warning");
            expect(getSubprocessoBadgeVariant("DISPONIBILIZADA")).toBe("warning");
        });

        it("deve retornar 'primary' para situações em andamento", () => {
            expect(getSubprocessoBadgeVariant("EM_ANDAMENTO")).toBe("primary");
        });

        it("deve retornar 'secondary' para NÃO_INICIADO", () => {
            expect(getSubprocessoBadgeVariant(SituacaoSubprocesso.NAO_INICIADO)).toBe("secondary");
        });

        it("deve retornar 'primary' como fallback para outras situações", () => {
            expect(getSubprocessoBadgeVariant("OUTRA_COISA")).toBe("primary");
        });
    });

    describe("getNotificacaoStatusInfo", () => {
        it("deve retornar as informações corretas para um status conhecido", () => {
            const info = getNotificacaoStatusInfo("PENDENTE");
            expect(info).toEqual(STATUS_NOTIFICACAO_INFO.PENDENTE);
        });

        it("deve retornar um fallback para status desconhecido", () => {
            const info = getNotificacaoStatusInfo("DESCONHECIDO");
            expect(info).toEqual({label: "DESCONHECIDO", variant: "secondary", prioridade: -1});
        });
    });

    describe("getNotificacaoBadgeVariant", () => {
        it("deve retornar a variante correta para status conhecido", () => {
            expect(getNotificacaoBadgeVariant("ENVIADO")).toBe("success");
            expect(getNotificacaoBadgeVariant("FALHA_DEFINITIVA")).toBe("danger");
        });

        it("deve retornar 'secondary' para status desconhecido", () => {
            expect(getNotificacaoBadgeVariant("DESCONHECIDO")).toBe("secondary");
        });
    });
});
