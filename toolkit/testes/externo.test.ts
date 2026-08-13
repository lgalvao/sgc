import os from "node:os";
import path from "node:path";
import {access, mkdtemp, mkdir, writeFile} from "node:fs/promises";
import {execa} from "execa";
import {describe, expect, test} from "vitest";

const DIRETORIO_RAIZ = path.resolve(import.meta.dirname, "..", "..");
const CAMINHO_SGC = path.join(DIRETORIO_RAIZ, "toolkit", "sgc.ts");
const CAMINHO_TSX = path.join(
    DIRETORIO_RAIZ,
    "node_modules",
    ".bin",
    process.platform === "win32" ? "tsx.cmd" : "tsx"
);

interface ResultadoExecucao {
    exitCode?: number;
    stdout: string;
    stderr: string;
}

async function escreverArquivo(caminho: string, conteudo: string): Promise<void> {
    await mkdir(path.dirname(caminho), {recursive: true});
    await writeFile(caminho, conteudo, "utf8");
}

async function executarSgc(diretorioBase: string, argumentos: string[]): Promise<ResultadoExecucao> {
    const resultado = await execa(CAMINHO_TSX, [CAMINHO_SGC, ...argumentos], {
        cwd: diretorioBase,
        reject: false,
    });
    return {
        exitCode: resultado.exitCode,
        stdout: String(resultado.stdout),
        stderr: String(resultado.stderr),
    };
}

describe("reuso do toolkit em projeto externo", () => {
    test("usa layout Java/Vue configurado sem depender dos caminhos do SGC", async () => {
        const diretorioBase = await mkdtemp(path.join(os.tmpdir(), "sgc-projeto-externo-"));

        await escreverArquivo(
            path.join(diretorioBase, "servidor", "src", "main", "java", "exemplo", "controle", "ExemploController.java"),
            [
                "package exemplo.controle;",
                "",
                "public class ExemploController {",
                "    public String consultar() {",
                "        return \"ok\";",
                "    }",
                "}"
            ].join("\n")
        );
        await escreverArquivo(
            path.join(diretorioBase, "cliente", "src", "views", "ExemploView.vue"),
            [
                "<template>",
                "  <main data-testid=\"exemplo\">Exemplo</main>",
                "</template>",
                "",
                "<script setup lang=\"ts\">",
                "const titulo = \"Exemplo\";",
                "</script>"
            ].join("\n")
        );
        await escreverArquivo(
            path.join(diretorioBase, "configuracao-toolkit.json"),
            JSON.stringify({
                versao: 2,
                diretorios: {
                    codigoServidor: "servidor/src/main/java",
                    codigoCliente: "cliente/src",
                    artefatosQualidade: "artefatos/qualidade"
                }
            })
        );

        const auditoriaServidor = await executarSgc(diretorioBase, [
            "servidor",
            "arquitetura",
            "auditar",
            "--json",
            "--base",
            diretorioBase
        ]);
        expect(auditoriaServidor.exitCode).toBe(0);
        expect(JSON.parse(auditoriaServidor.stdout)).toMatchObject({
            resumo: {totalAnalisados: 1},
            todos: [{caminhoRelativo: "servidor/src/main/java/exemplo/controle/ExemploController.java"}]
        });

        const auditoriaCliente = await executarSgc(diretorioBase, [
            "cliente",
            "arquitetura",
            "auditar",
            "--json",
            "--base",
            diretorioBase
        ]);
        expect(auditoriaCliente.exitCode).toBe(0);
        expect(JSON.parse(auditoriaCliente.stdout)).toMatchObject({
            base: diretorioBase,
            resumo: {arquivosProducao: 1}
        });

        const identificadores = await executarSgc(diretorioBase, [
            "cliente",
            "identificadores-teste",
            "listar",
            "--json",
            "--base",
            diretorioBase
        ]);
        expect(identificadores.exitCode).toBe(0);
        expect(JSON.parse(identificadores.stdout)).toMatchObject({
            diretorioBusca: path.join(diretorioBase, "cliente", "src"),
            identificadores: [{arquivo: "cliente/src/views/ExemploView.vue", valor: "exemplo"}]
        });

        const residuos = await executarSgc(diretorioBase, [
            "cliente",
            "residuos",
            "auditar",
            "--json",
            "--base",
            diretorioBase
        ]);
        expect(residuos.exitCode).toBe(0);
        expect(JSON.parse(residuos.stdout)).toMatchObject({
            base: diretorioBase,
            resumo: {arquivosCliente: 1}
        });

        await expect(access(path.join(diretorioBase, "backend", "src"))).rejects.toThrow("ENOENT");
        await expect(access(path.join(diretorioBase, "frontend", "src"))).rejects.toThrow("ENOENT");
        await expect(access(path.join(diretorioBase, "toolkit"))).rejects.toThrow("ENOENT");
    }, 60000);

    test("executa os agregadores CDU com corpus e vocabulário próprios", async () => {
        const diretorioBase = await mkdtemp(path.join(os.tmpdir(), "sgc-projeto-cdu-horizontal-"));

        await escreverArquivo(
            path.join(diretorioBase, "documentacao", "casos", "cdu-01.md"),
            [
                "# CDU-01 - Registrar solicitação",
                "",
                "## Atores",
                "",
                "- OPERADOR",
                "",
                "## Pré-condições",
                "",
                "- Processo do tipo 'Solicitação' na situação 'Liberado'.",
                "",
                "## Fluxo principal",
                "",
                "1. O operador acessa o `Painel`."
            ].join("\n")
        );
        await escreverArquivo(
            path.join(diretorioBase, "documentacao", "situacoes.md"),
            [
                "## Situações",
                "",
                "- **Liberado**: Solicitação pronta para tratamento."
            ].join("\n")
        );
        await escreverArquivo(
            path.join(diretorioBase, "configuracao-toolkit.json"),
            JSON.stringify({
                versao: 2,
                requisitos: {
                    cdus: {
                        padraoArquivos: "documentacao/casos/cdu-*.md",
                        fontesMensagensCodigo: [],
                        vocabulario: {
                            perfisCanonicos: ["OPERADOR"],
                            tiposProcessoCanonicos: ["Solicitação"],
                            arquivoSituacoesCanonicas: "documentacao/situacoes.md"
                        },
                        estilo: {perfisEmCrases: ["OPERADOR"]}
                    }
                }
            })
        );

        const inventario = await executarSgc(diretorioBase, [
            "requisitos",
            "cdus",
            "inventariar",
            "--secoes",
            "vocabulario",
            "--json",
            "--base",
            diretorioBase
        ]);
        expect(inventario.exitCode).toBe(0);
        expect(JSON.parse(inventario.stdout)).toMatchObject({
            secoes: {
                vocabulario: {
                    canonicos: {
                        perfis: ["OPERADOR"],
                        situacoes: ["Liberado"],
                        tiposProcesso: ["Solicitação"]
                    },
                    perfis: {OPERADOR: 1},
                    tiposProcesso: {Solicitação: 1}
                }
            }
        });

        const auditoria = await executarSgc(diretorioBase, [
            "requisitos",
            "cdus",
            "auditar",
            "--secoes",
            "estrutura,vocabulario",
            "--json",
            "--base",
            diretorioBase
        ]);
        expect(auditoria.exitCode).toBe(0);
        expect(JSON.parse(auditoria.stdout)).toMatchObject({
            totalArquivos: 1,
            resumo: {erros: 0, avisos: 0}
        });
    }, 60000);

    test("aplica política de mensagens própria sem herdar convenções do SGC", async () => {
        const diretorioBase = await mkdtemp(path.join(os.tmpdir(), "sgc-projeto-mensagens-horizontal-"));

        await escreverArquivo(
            path.join(diretorioBase, "documentacao", "casos", "cdu-01.md"),
            [
                "# CDU-01 - Registrar solicitação",
                "",
                "## Atores",
                "",
                "- OPERADOR",
                "",
                "## Pré-condições",
                "",
                "- Sistema disponível.",
                "",
                "## Fluxo principal",
                "",
                "1. O sistema registra:",
                "   - `Descrição`: \"Solicitação criada\"",
                "",
                "   Assunto: APP: Solicitação criada",
                "",
                "2. O sistema mostra a mensagem \"Solicitação criada\".",
                "3. O sistema mostra *toast* \"Solicitação criada\"."
            ].join("\n")
        );
        await escreverArquivo(
            path.join(diretorioBase, "codigo", "mensagens.java"),
            "public static final String HIST_SOLICITACAO_CRIADA = \"Solicitação criada\";\n"
        );
        await escreverArquivo(
            path.join(diretorioBase, "codigo", "assuntos.java"),
            "String assunto() { return \"APP: Solicitação criada\"; }\n"
        );
        await escreverArquivo(
            path.join(diretorioBase, "codigo", "textos.ts"),
            "export const TEXTOS = { SOLICITACAO_CRIADA: \"Solicitação criada\" };\n"
        );
        await escreverArquivo(
            path.join(diretorioBase, "configuracao-toolkit.json"),
            JSON.stringify({
                versao: 2,
                requisitos: {
                    cdus: {
                        padraoArquivos: "documentacao/casos/cdu-*.md",
                        fontesMensagensCodigo: [
                            {caminho: "codigo/mensagens.java", tipo: "mensagensJava"},
                            {caminho: "codigo/assuntos.java", tipo: "assuntosJava"},
                            {caminho: "codigo/textos.ts", tipo: "textosTypescript"}
                        ],
                        politicaMensagensCodigo: {
                            prefixosUiExcluidos: [],
                            chavesMensagem: ["SOLICITACAO_CRIADA"],
                            categoriasChavesMensagem: ["toast", "mensagem"],
                            regrasJava: [{prefixo: "HIST_", categoria: "descricao", grupo: "historico_aplicacao"}],
                            assuntosJava: {
                                prefixo: "APP: ",
                                grupo: "assunto_aplicacao",
                                marcadorSubprocesso: ":SIGLA_APLICACAO:",
                                marcadorFormatado: ":VALOR:",
                                sufixoSuperior: ""
                            },
                            typescript: {
                                prefixoSucesso: "CONCLUIDO_",
                                grupoSucesso: "sucesso_aplicacao",
                                grupoNotificacao: "notificacao_aplicacao",
                                prefixoMovimentacao: "FLUXO_",
                                grupoMovimentacao: "movimentacao_aplicacao",
                                grupoResultado: "resultado_aplicacao"
                            }
                        }
                    }
                }
            })
        );

        const auditoria = await executarSgc(diretorioBase, [
            "requisitos",
            "cdus",
            "auditar",
            "--secoes",
            "mensagens-codigo",
            "--json",
            "--base",
            diretorioBase
        ]);

        expect(auditoria.exitCode).toBe(0);
        const relatorio = JSON.parse(auditoria.stdout).secoes.mensagensCodigo.relatorio as Array<{
            tipo: string;
            valor: string;
            referenciasExatas: Array<{grupo: string}>;
        }>;
        expect(relatorio.find(item => item.tipo === "descricoes" && item.valor === "Solicitação criada")?.referenciasExatas)
            .toEqual(expect.arrayContaining([expect.objectContaining({grupo: "historico_aplicacao"})]));
        expect(relatorio.find(item => item.tipo === "assuntos" && item.valor === "APP: Solicitação criada")?.referenciasExatas)
            .toEqual(expect.arrayContaining([expect.objectContaining({grupo: "assunto_aplicacao"})]));
        expect(relatorio.find(item => item.tipo === "mensagens" && item.valor === "Solicitação criada")?.referenciasExatas)
            .toEqual(expect.arrayContaining([expect.objectContaining({grupo: "resultado_aplicacao"})]));
    }, 60000);
});
