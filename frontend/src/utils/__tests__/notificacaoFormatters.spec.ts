import {describe, expect, it} from "vitest";
import {
    formatarAssunto,
    formatarDataOuHifen,
    formatarDestinatario,
    formatarQuando,
    formatarTipoNotificacao,
    resumirContexto
} from "../notificacaoFormatters";
import type {Notificacao} from "@/services/notificacaoService";

describe("notificacaoFormatters", () => {
    describe("formatarDataOuHifen", () => {
        it("deve retornar '-' para valores nulos ou vazios", () => {
            expect(formatarDataOuHifen(null)).toBe("-");
            expect(formatarDataOuHifen(undefined)).toBe("-");
            expect(formatarDataOuHifen("")).toBe("-");
        });

        it("deve formatar data corretamente", () => {
            // Assumindo que formatarDataHoraBR funciona corretamente (é testada em outro lugar)
            // Aqui testamos a lógica de fallback do formatter específico
            expect(formatarDataOuHifen("2023-01-01T10:00:00")).not.toBe("-");
        });
    });

    describe("resumirContexto", () => {
        it("deve retornar o resumo correto", () => {
            const item = {
                processoDescricao: "Processo A",
                usuarioDestinoTitulo: "Chefe"
            } as Notificacao;
            expect(resumirContexto(item)).toBe("Processo A • Título Chefe");
        });

        it("deve lidar com campos ausentes", () => {
            const item = {
                processoDescricao: "Processo A"
            } as Notificacao;
            expect(resumirContexto(item)).toBe("Processo A");
        });

        it("deve retornar fallback se nada existir", () => {
            const item = {} as Notificacao;
            expect(resumirContexto(item)).toBe("Sem contexto adicional");
        });
    });

    describe("formatarTipoNotificacao", () => {
        it("deve retornar '-' para tipo ausente", () => {
            expect(formatarTipoNotificacao()).toBe("-");
        });

        it("deve retornar o label se existir nas constantes", () => {
            expect(formatarTipoNotificacao("PROCESSO_INICIADO")).toBe("Início do processo");
        });

        it("deve retornar o próprio tipo se não houver label", () => {
            expect(formatarTipoNotificacao("OUTRO")).toBe("OUTRO");
        });
    });

    describe("formatarDestinatario", () => {
        it("deve retornar o destinatário se houver título de usuário", () => {
            const item = {destinatario: "usuario@teste.com", usuarioDestinoTitulo: "Chefe"};
            expect(formatarDestinatario(item)).toBe("usuario@teste.com");
        });

        it("deve retornar a sigla da unidade se houver", () => {
            const item = {destinatario: "setor@teste.com", unidadeSigla: "DTI"};
            expect(formatarDestinatario(item)).toBe("DTI");
        });

        it("deve extrair o usuário do email institucional se for o caso", () => {
            const item = {destinatario: "fulano.detal@tre-pe.jus.br"};
            expect(formatarDestinatario(item)).toBe("FULANO.DETAL");
        });

        it("deve retornar o destinatário original se nenhum critério for atendido", () => {
            const item = {destinatario: "externo@gmail.com"};
            expect(formatarDestinatario(item)).toBe("externo@gmail.com");
        });
    });

    describe("formatarAssunto", () => {
        it("deve remover o prefixo 'SGC:'", () => {
            expect(formatarAssunto("SGC: Novo Processo")).toBe("Novo Processo");
            expect(formatarAssunto("sgc: Novo Processo")).toBe("Novo Processo");
        });

        it("deve retornar '-' se vazio", () => {
            expect(formatarAssunto()).toBe("-");
            expect(formatarAssunto("")).toBe("-");
        });
    });

    describe("formatarQuando", () => {
        const dataCriacao = "2023-01-01T08:00:00";
        const dataEnvio = "2023-01-01T09:00:00";
        const proximaTentativa = "2023-01-01T10:00:00";

        it("deve usar dataHoraEnvio se estiver ENVIADO", () => {
            const item = {situacao: "ENVIADO", dataHoraEnvio: dataEnvio} as Notificacao;
            expect(formatarQuando(item)).not.toBe("-");
        });

        it("deve usar proximaTentativaEm se disponível e não enviado", () => {
            const item = {situacao: "PENDENTE", proximaTentativaEm: proximaTentativa} as Notificacao;
            expect(formatarQuando(item)).not.toBe("-");
        });

        it("deve usar dataHoraCriacao como fallback", () => {
            const item = {situacao: "PENDENTE", dataHoraCriacao: dataCriacao} as Notificacao;
            expect(formatarQuando(item)).not.toBe("-");
        });
    });
});
