import os from "node:os";
import path from "node:path";
import {mkdtemp} from "node:fs/promises";
import {describe, expect, test} from "vitest";
import {
    executarSgc,
    escreverArquivo,
    escreverJson,
    existe
} from "./apoio.js";
import {VERSAO_CONFIGURACAO} from "../lib/configuracao.js";
import {principal as coletarFotografiaQualidade, type AdaptadorQualidade} from "../qualidade/coleta-execucao.js";

interface VerificacaoDiagnosticoJson {
    nome: string;
    status?: string;
    detalhe?: string;
}

describe("CLI raiz do toolkit", () => {
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
            "backend",
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

    test("audita assuntos no diretorio backend configurado", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-assuntos-configurado-"));
        const dir = path.join(base, "servidor", "java");
        await escreverJson(path.join(base, "configuracao-toolkit.json"), {
            versao: VERSAO_CONFIGURACAO,
            diretorios: {backendCodigo: "servidor/java"}
        });
        await escreverArquivo(
            path.join(dir, "diagnostico", "ServicoInvalido.java"),
            "class ServicoInvalido { void enviar() { String assunto = \"SGC: Assunto espalhado\"; } }\n"
        );

        const resultado = await executarSgc([
            "backend",
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

    test("grava fotografia de qualidade na base externa", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-qualidade-base-"));
        const adaptadores = ["testeExterno", "coberturaExterna"];
        const perfis = {externo: adaptadores};
        const adaptadoresFalsos: Record<string, AdaptadorQualidade> = {};
        for (const nome of adaptadores) {
            adaptadoresFalsos[nome] = async contexto => ({
                codigo: nome,
                nome,
                categoria: "qualidade",
                status: "sucesso",
                duracaoMs: 0,
                comando: "teste",
                diretorio: contexto.base,
                sumario: "",
                metricas: {},
                erros: [],
                artefatos: []
            });
        }

        const fotografia = await coletarFotografiaQualidade([
            "--perfil",
            "externo",
            "--base",
            base,
        ], {adaptadores: adaptadoresFalsos, perfis});

        const caminhoFotografia = path.join(
            base,
            "toolkit",
            "qualidade",
            "artefatos",
            "mais-recente",
            "fotografia.json"
        );
        expect(fotografia.resumo.statusGeral).toBe("verde");
        expect(fotografia.metadados.perfilExecucao).toBe("externo");
        expect(fotografia.verificacoes).toHaveLength(adaptadores.length);
        expect(await existe(caminhoFotografia)).toBe(true);
    });

    test("projeto diagnostico identifica corretamente a ausencia de arquivos essenciais e falha com codigo 1", async () => {
        const diretorioVazio = await mkdtemp(path.join(os.tmpdir(), "sgc-diagnostico-vazio-"));

        // Executamos o diagnostico passando o diretorio temporario vazio como base
        const resultado = await executarSgc(["projeto", "diagnostico", "--json", "--base", diretorioVazio]);

        // Como arquivos essenciais como gradlew e package.json estão ausentes, deve retornar código de erro 1
        expect(resultado.exitCode).toBe(1);

        const dados = JSON.parse(resultado.stdout);
        expect(dados.statusGeral).toBe("falha");
        expect(dados.totais.falha).toBeGreaterThan(0);

        // Verifica se um dos arquivos obrigatórios ausentes foi reportado como falha
        const falhaGradlew = dados.verificacoes.find((verificacao: VerificacaoDiagnosticoJson) => verificacao.nome === "gradlew");
        expect(falhaGradlew).toBeDefined();
        expect(falhaGradlew.status).toBe("falha");
        expect(falhaGradlew.detalhe).toContain("gradlew ausente");
    });

}, 30000);
