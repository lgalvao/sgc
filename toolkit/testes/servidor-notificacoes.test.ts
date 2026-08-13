import os from "node:os";
import path from "node:path";
import {mkdtemp} from "node:fs/promises";
import {describe, expect, test} from "vitest";
import {
    executarSgc,
    escreverArquivo,
    escreverJson
} from "./apoio.js";
import {VERSAO_CONFIGURACAO} from "../biblioteca/configuracao.js";

describe("Auditoria de notificações do servidor", () => {
    test("audita assuntos literais fora de AssuntosNotificacao", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-assuntos-auditar-"));
        const dir = path.join(base, "backend", "src", "main", "java", "sgc");

        await escreverArquivo(
            path.join(dir, "alerta", "AssuntosNotificacao.java"),
            [
                "package sgc.alerta;",
                "public final class AssuntosNotificacao {",
                "  public static String ok() {",
                "    return \"SGC: Assunto centralizado\";",
                "  }",
                "}"
            ].join("\n")
        );

        await escreverArquivo(
            path.join(dir, "diagnostico", "ServicoInvalido.java"),
            [
                "package sgc.diagnostico;",
                "class ServicoInvalido {",
                "  void enviar() {",
                "    String assunto = \"SGC: Assunto espalhado\";",
                "  }",
                "}"
            ].join("\n")
        );

        const resultado = await executarSgc([
            "servidor",
            "notificacoes",
            "auditar-assuntos",
            "--json",
            "--base",
            base
        ]);

        expect(resultado.exitCode).toBe(1);
        const corpo = JSON.parse(resultado.stdout);
        expect(corpo.resumo.arquivosComViolacao).toBe(1);
        expect(corpo.relatorio[0].arquivo).toBe("backend/src/main/java/sgc/diagnostico/ServicoInvalido.java");
        expect(corpo.relatorio[0].achados.some((item: {regra: string}) => item.regra === "literal_sgc")).toBe(true);
    });

    test("audita assuntos no diretorio do servidor configurado", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-assuntos-configurado-"));
        const dir = path.join(base, "servidor", "java");
        await escreverJson(path.join(base, "configuracao-toolkit.json"), {
            versao: VERSAO_CONFIGURACAO,
            diretorios: {codigoServidor: "servidor/java"}
        });
        await escreverArquivo(
            path.join(dir, "diagnostico", "ServicoInvalido.java"),
            "class ServicoInvalido { void enviar() { String assunto = \"SGC: Assunto espalhado\"; } }\n"
        );

        const resultado = await executarSgc([
            "servidor",
            "notificacoes",
            "auditar-assuntos",
            "--json",
            "--base",
            base
        ]);

        expect(resultado.exitCode).toBe(1);
        const corpo = JSON.parse(resultado.stdout);
        expect(corpo.resumo.arquivosComViolacao).toBe(1);
        expect(corpo.relatorio[0].arquivo).toBe("servidor/java/diagnostico/ServicoInvalido.java");
    });
});
