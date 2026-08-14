import os from "node:os";
import path from "node:path";
import {mkdir, mkdtemp, writeFile} from "node:fs/promises";
import {describe, expect, test} from "vitest";
import {execa} from "execa";
import {pathToFileURL} from "node:url";
import {auditarCdus} from "../requisitos/cdus-auditoria-motor.js";
import {auditarMensagensCodigo} from "../requisitos/cdus-auditar-mensagens-codigo.js";
import {inventariarCdus} from "../requisitos/cdus-inventario-motor.js";
import {carregarMensagensCanonicas} from "../requisitos/cdus-mensagens-codigo-lib.js";

const DIRETORIO_RAIZ = path.resolve(import.meta.dirname, "..", "..");
const CAMINHO_FERRAMENTAS = path.join(DIRETORIO_RAIZ, "toolkit", "ferramentas.ts");
const CAMINHO_TSX = path.join(DIRETORIO_RAIZ, "node_modules", ".bin", process.platform === "win32" ? "tsx.cmd" : "tsx");
const CAMINHOS_COMANDOS_CDU = [
    "cdus-inventariar.ts",
    "cdus-auditar.ts"
].map(nome => path.join(DIRETORIO_RAIZ, "toolkit", "requisitos", nome));

async function escreverArquivo(caminho: string, conteudo: string): Promise<void> {
    await mkdir(path.dirname(caminho), {recursive: true});
    await writeFile(caminho, conteudo, "utf8");
}

interface ResultadoExecucao {
    exitCode?: number;
    stdout: string;
    stderr: string;
}

interface ItemAuditoriaCdu {
    arquivo: string;
    achados: Array<{regra: string}>;
}

interface ResultadoAuditoriaCdu {
    secoes: {
        estrutura?: {
            resumo: {
                totalArquivos: number;
                arquivosComErro: number;
                erros: number;
                avisos: number;
            };
            relatorio: ItemAuditoriaCdu[];
        };
        estilo?: {resumo: {totalArquivos: number; arquivosComAviso: number; avisos: number}; relatorio: ItemAuditoriaCdu[]};
        vocabulario?: {resumo: {totalArquivos: number; arquivosComAviso: number; avisos: number}; relatorio: ItemAuditoriaCdu[]};
        mensagens?: {resumo: {totalArquivos: number; arquivosComAviso: number; avisos: number}; relatorio: ItemAuditoriaCdu[]};
        mensagensCodigo?: ResultadoMensagensCodigo;
    };
}

interface ResultadoInventarioFormatos {
    totalArquivos: number;
    formatosAtor: Record<string, number>;
    formatosPreCondicoes: Record<string, number>;
    formatosFluxoPrincipal: Record<string, number>;
    situacoesMaisFrequentes: Record<string, number>;
    elementosUiMaisFrequentes: Record<string, number>;
}

interface ResultadoInventarioCdus {
    secoes: {
        formatos?: ResultadoInventarioFormatos;
        vocabulario?: ResultadoInventarioVocabulario;
        mensagens?: ResultadoInventarioMensagens;
        densidade?: ResultadoDensidade;
        duplicacoes?: ResultadoDuplicacoes;
    };
}

interface ResultadoInventarioMensagens {
    descricoes: Record<string, number>;
    assuntos: Record<string, number>;
    mensagens: Record<string, number>;
    toasts: Record<string, number>;
}

interface ResultadoInventarioVocabulario {
    perfis: Record<string, number>;
    situacoes: Record<string, number>;
    tiposProcesso: Record<string, number>;
    elementosUi: Record<string, number>;
    canonicos: {
        perfis: string[];
        situacoes: string[];
        tiposProcesso: string[];
    };
}

interface ItemMensagemCodigo {
    tipo: string;
    valor: string;
    referenciasExatas: Array<{texto: string}>;
    sugestoes: Array<{texto: string}>;
}

interface ResultadoMensagensCodigo {
    resumo: {itensComReferenciaExata: number};
    relatorio: ItemMensagemCodigo[];
}

interface ResultadoDensidade {
    totalArquivos: number;
    resumo: {mediaPalavras: number};
    documentos: Array<{passos: number}>;
}

interface ResultadoDuplicacoes {
    duplicacoes: unknown[];
}

function lerJson<T>(resultado: ResultadoExecucao): T {
    return JSON.parse(resultado.stdout) as T;
}

async function executarSgc(args: string[]): Promise<ResultadoExecucao> {
    const resultado = await execa(CAMINHO_TSX, [CAMINHO_FERRAMENTAS, ...args], {
        cwd: DIRETORIO_RAIZ,
        reject: false,
    });
    return {
        exitCode: resultado.exitCode,
        stdout: String(resultado.stdout),
        stderr: String(resultado.stderr),
    };
}

async function criarIntroSituacoes(dirSpecs: string): Promise<void> {
    await escreverArquivo(
        path.join(path.dirname(dirSpecs), "intro_3_situacoes.md"),
        [
            "## Situações",
            "",
            "- **Criado**: Processo cadastrado.",
            "- **Em andamento**: Processo em execução.",
            "- **Finalizado**: Processo encerrado.",
            "- **Diagnóstico**: Tipo de processo de diagnóstico.",
            "- **Autoavaliação concluída**: Avaliação individual concluída."
        ].join("\n")
    );
}

describe("Ferramentas de requisitos dos CDUs", () => {
    test("executa os motores CDU diretamente, sem carregar a borda da CLI", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-cdus-motores-"));
        await escreverArquivo(
            path.join(base, "specs", "cdu", "cdu-01.md"),
            [
                "# CDU-01 - Exemplo",
                "",
                "## Atores",
                "",
                "- ADMIN",
                "",
                "## Pré-condições",
                "",
                "- Sistema disponível para o tipo 'Solicitação' na situação 'Liberado'.",
                "",
                "## Fluxo principal",
                "",
                "1. O usuário acessa o `Painel`.",
                "2. O sistema registra a mensagem \"Pedido criado\", a `Descrição`: \"Pedido criado\" e mostra *toast* \"Pedido criado\"."
            ].join("\n")
        );

        const inventario = await inventariarCdus(base);
        expect(inventario.versao).toBe(1);
        expect(inventario.secoes.formatos?.totalArquivos).toBe(1);
        expect(inventario.secoes.densidade?.documentos[0].passos).toBe(2);
        expect(inventario.secoes.mensagens?.mensagens["Pedido criado"]).toBe(1);

        const auditoria = await auditarCdus(base);
        expect(auditoria.secoes.estrutura?.resumo.erros).toBe(0);
        expect(auditoria.secoes.estilo).toBeDefined();
        expect(auditoria.secoes.vocabulario).toBeDefined();
        expect(auditoria.secoes.mensagens).toBeDefined();
    });

    test("saidas humanas dos agregadores CDU mostram amostras acionaveis", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-cdus-saida-humana-"));
        const dirSpecs = path.join(base, "specs", "cdu");
        const conteudo = [
            "# CDU-01 - Exemplo",
            "",
            "## Atores",
            "",
            "- 'ADMIN'",
            "",
            "## Pré-condições",
            "",
            "- Sistema disponível.",
            "",
            "## Fluxo principal",
            "",
            "1. O sistema mostra o `Painel`.",
            "1. O sistema registra a mensagem \"Exemplo repetido\".",
            "",
            "```text",
            "Assunto: SGC: Exemplo repetido em vários documentos",
            "Detalhe: mensagem documental compartilhada entre os casos de uso.",
            "```"
        ].join("\n");
        await escreverArquivo(path.join(dirSpecs, "cdu-01.md"), conteudo);
        await escreverArquivo(path.join(dirSpecs, "cdu-02.md"), conteudo.replace("CDU-01", "CDU-02"));

        const auditoria = await executarSgc([
            "requisitos", "cdus", "auditar", "--secoes", "estrutura,estilo", "--base", base
        ]);
        expect(auditoria.exitCode).toBe(0);
        expect(auditoria.stdout).toContain("Amostra de achados de estrutura");
        expect(auditoria.stdout).toContain("numeracao_repetida");
        expect(auditoria.stdout).toContain("cdu-01.md");
        expect(auditoria.stdout).toContain("use --json para consultar todos os itens");

        const inventario = await executarSgc([
            "requisitos", "cdus", "inventariar", "--secoes", "duplicacoes", "--base", base
        ]);
        expect(inventario.exitCode).toBe(0);
        expect(inventario.stdout).toContain("Itens duplicados:");
        expect(inventario.stdout).toContain("Amostra:");
        expect(inventario.stdout).toContain("cdu-01.md");
    });

    test("limita casos Java ao método de assuntos configurado", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-cdus-assuntos-java-"));
        await escreverArquivo(
            path.join(base, "codigo", "assuntos.java"),
            [
                "public static String inicio(String tipo) {",
                "    return switch (tipo) {",
                "        case NAO_ASSUNTO -> \"mapeamento de competências\";",
                "        default -> tipo;",
                "    };",
                "}",
                "public static String subprocesso(TipoTransicao tipo, String sigla, boolean paraSuperior) {",
                "    String base = switch (tipo) {",
                "        case ASSUNTO_REAL -> \"Assunto de %s\";",
                "        default -> tipo.getDescMovimentacao();",
                "    };",
                "    return incluirSigla ? \"SGC: %s - %s\".formatted(base, sigla) : \"SGC: %s\".formatted(base);",
                "    }"
            ].join("\n")
        );
        await escreverArquivo(
            path.join(base, "configuracao-toolkit.json"),
            JSON.stringify({
                versao: 2,
                requisitos: {
                    cdus: {
                        fontesMensagensCodigo: [{caminho: "codigo/assuntos.java", tipo: "assuntosJava"}]
                    }
                }
            })
        );
        const caminhoCdu = path.join(base, "specs", "cdu", "cdu-01.md");
        await escreverArquivo(caminhoCdu, "Assunto: SGC: Assunto de unidade\n");

        const resultado = carregarMensagensCanonicas(base);
        expect(resultado.itens.some(item => item.chave === "NAO_ASSUNTO")).toBe(false);
        expect(resultado.itens.some(item => item.chave === "ASSUNTO_REAL")).toBe(true);
        expect(resultado.itens.some(item => item.chave === "ASSUNTO_REAL_SUPERIOR")).toBe(true);

        const auditoria = await auditarMensagensCodigo(base, [caminhoCdu]);
        expect(auditoria.resumo.totalItens).toBe(1);
        expect(auditoria.relatorio[0].sugestoes.length).toBeGreaterThan(0);
    });

    test("pode importar todos os comandos sem executar auditorias", async () => {
        const resultados = await Promise.all(CAMINHOS_COMANDOS_CDU.map(async caminho => {
            const urlModulo = pathToFileURL(caminho).href;
            return execa(process.execPath, [
                "--import=tsx",
                "--input-type=module",
                "-e",
                `process.argv.push("--help"); await import(${JSON.stringify(urlModulo)}); process.stdout.write("importacao-ok\\n");`
            ], {
                cwd: DIRETORIO_RAIZ,
                reject: false
            });
        }));

        for (const resultado of resultados) {
            expect(resultado.exitCode).toBe(0);
            expect(resultado.stdout).toBe("importacao-ok");
        }
    });

    test("inventaria formatos implícitos dos CDUs", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-cdus-inventario-"));
        const dirSpecs = path.join(base, "specs", "cdu");

        await escreverArquivo(
            path.join(dirSpecs, "cdu-01.md"),
            [
                "# CDU-01 - Exemplo",
                "",
                "## Atores",
                "",
                "- ADMIN",
                "",
                "## Pré-condições",
                "",
                "- Usuário autenticado",
                "",
                "## Fluxo principal",
                "",
                "1. O usuário acessa o `Painel`.",
                "2. O sistema muda a situação para 'Em andamento'."
            ].join("\n")
        );
        await criarIntroSituacoes(dirSpecs);

        const resultado = await executarSgc([
            "requisitos",
            "cdus",
            "inventariar",
            "--json",
            "--base",
            base
        ]);

        expect(resultado.exitCode).toBe(0);
        const conteudo = lerJson<ResultadoInventarioCdus>(resultado);
        const formatos = conteudo.secoes.formatos!;
        expect(Object.keys(conteudo.secoes)).toEqual(["formatos", "vocabulario", "mensagens", "densidade", "duplicacoes"]);
        expect(formatos.totalArquivos).toBe(1);
        expect(formatos.formatosAtor["## Atores"]).toBe(1);
        expect(formatos.formatosPreCondicoes["## Pré-condições"]).toBe(1);
        expect(formatos.formatosFluxoPrincipal["## Fluxo principal"]).toBe(1);
        expect(formatos.situacoesMaisFrequentes["'Em andamento'"]).toBe(1);
        expect(formatos.elementosUiMaisFrequentes["`Painel`"]).toBe(1);
    });

    test("usa o padrão de arquivos CDU configurado pelo projeto", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-cdus-configuracao-corpus-"));
        const diretorioCorpus = path.join(base, "documentacao", "casos-de-uso");

        await escreverArquivo(
            path.join(diretorioCorpus, "cdu-01.md"),
            [
                "# CDU-01 - Corpus configurado",
                "",
                "## Atores",
                "",
                "- ADMIN",
                "",
                "## Pré-condições",
                "",
                "- Usuário autenticado",
                "",
                "## Fluxo principal",
                "",
                "1. O usuário acessa o painel."
            ].join("\n")
        );
        await escreverArquivo(
            path.join(base, "configuracao-toolkit.json"),
            JSON.stringify({
                versao: 2,
                requisitos: {
                    cdus: {
                        padraoArquivos: "documentacao/casos-de-uso/cdu-*.md"
                    }
                }
            })
        );

        const resultado = await executarSgc([
            "requisitos",
            "cdus",
            "inventariar",
            "--json",
            "--secoes",
            "formatos",
            "--base",
            base
        ]);

        expect(resultado.exitCode).toBe(0);
        const conteudo = lerJson<ResultadoInventarioCdus>(resultado);
        expect(conteudo.secoes.formatos?.totalArquivos).toBe(1);
    });

    test("rejeita seção CDU desconhecida", async () => {
        const resultado = await executarSgc([
            "requisitos",
            "cdus",
            "inventariar",
            "--secoes",
            "nao-existe"
        ]);

        expect(resultado.exitCode).toBe(1);
        expect(resultado.stderr).toContain("Seção de inventário CDU desconhecida");
    });

    test("audita a estrutura canônica mínima dos CDUs", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-cdus-auditoria-"));
        const dirSpecs = path.join(base, "specs", "cdu");

        await escreverArquivo(
            path.join(dirSpecs, "cdu-01.md"),
            [
                "# CDU-01 - Exemplo válido",
                "",
                "## Atores",
                "",
                "- CHEFE",
                "",
                "## Pré-condições",
                "",
                "- Existe processo em andamento",
                "",
                "## Fluxo principal",
                "",
                "1. O usuário acessa o `Painel`.",
                "2. O sistema mostra a tela `Detalhes`."
            ].join("\n")
        );
        await criarIntroSituacoes(dirSpecs);

        await escreverArquivo(
            path.join(dirSpecs, "cdu-02.md"),
            [
                "# CDU-99 - Exemplo inválido",
                "",
                "Ator: ADMIN",
                "",
                "## Pré-condição",
                "",
                "## Fluxo principal",
                "",
                "1. Primeiro passo.",
                "1. Passo repetido."
            ].join("\n")
        );

        const resultado = await executarSgc([
            "requisitos",
            "cdus",
            "auditar",
            "--json",
            "--base",
            base
        ]);

        expect(resultado.exitCode).toBe(0);
        const conteudo = lerJson<ResultadoAuditoriaCdu>(resultado);
        const estrutura = conteudo.secoes.estrutura!;
        expect(estrutura.resumo.totalArquivos).toBe(2);
        expect(estrutura.resumo.arquivosComErro).toBe(1);

        const invalido = estrutura.relatorio.find(item => item.arquivo === "specs/cdu/cdu-02.md")!;
        expect(invalido.achados.some(achado => achado.regra === "titulo_numero")).toBe(true);
        expect(invalido.achados.some(achado => achado.regra === "atores_canonicos")).toBe(true);
        expect(invalido.achados.some(achado => achado.regra === "pre_condicoes")).toBe(true);
        expect(invalido.achados.some(achado => achado.regra === "numeracao_repetida")).toBe(true);
    });

    test("audita convenções tipográficas dos CDUs", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-cdus-estilo-"));
        const dirSpecs = path.join(base, "specs", "cdu");

        await escreverArquivo(
            path.join(dirSpecs, "cdu-01.md"),
            [
                "# CDU-01 - Exemplo",
                "",
                "## Atores",
                "",
                "- ADMIN",
                "",
                "## Pré-condições",
                "",
                "- Usuário autenticado com perfil 'ADMIN'",
                "",
                "## Fluxo principal",
                "",
                "1. O sistema mostra um modal com título \"Adicionar administrador\" e usa [SIGLA_UNIDADE]."
            ].join("\n")
        );
        await criarIntroSituacoes(dirSpecs);

        const resultado = await executarSgc([
            "requisitos",
            "cdus",
            "auditar",
            "--secoes",
            "estilo",
            "--json",
            "--base",
            base
        ]);

        expect(resultado.exitCode).toBe(0);
        const conteudo = lerJson<ResultadoAuditoriaCdu>(resultado);
        const estilo = conteudo.secoes.estilo!;
        expect(estilo.resumo.totalArquivos).toBe(1);
        expect(estilo.resumo.arquivosComAviso).toBe(1);

        const arquivo = estilo.relatorio.find(item => item.arquivo === "specs/cdu/cdu-01.md")!;
        expect(arquivo.achados.some(achado => achado.regra === "perfil_em_aspas_simples")).toBe(true);
        expect(arquivo.achados.some(achado => achado.regra === "ui_em_aspas_duplas")).toBe(true);
        expect(arquivo.achados.some(achado => achado.regra === "placeholder_legado")).toBe(true);
    });

    test("inventaria vocabulário controlado dos CDUs", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-cdus-vocabulario-"));
        const dirSpecs = path.join(base, "specs", "cdu");

        await escreverArquivo(
            path.join(dirSpecs, "cdu-01.md"),
            [
                "# CDU-01 - Exemplo",
                "",
                "## Atores",
                "",
                "- ADMIN",
                "- GESTOR",
                "",
                "## Pré-condições",
                "",
                "- Processo do tipo 'Diagnóstico' na situação 'Em andamento'.",
                "",
                "## Fluxo principal",
                "",
                "1. O usuário abre o `Painel`."
            ].join("\n")
        );
        await criarIntroSituacoes(dirSpecs);

        const resultado = await executarSgc([
            "requisitos",
            "cdus",
            "inventariar",
            "--secoes",
            "vocabulario",
            "--json",
            "--base",
            base
        ]);

        expect(resultado.exitCode).toBe(0);
        const conteudo = lerJson<ResultadoInventarioCdus>(resultado);
        const vocabulario = conteudo.secoes.vocabulario!;
        expect(vocabulario.perfis.ADMIN).toBe(1);
        expect(vocabulario.perfis.GESTOR).toBe(1);
        expect(vocabulario.situacoes["Em andamento"]).toBe(1);
        expect(vocabulario.tiposProcesso["Diagnóstico"]).toBe(1);
        expect(vocabulario.elementosUi.Painel).toBe(1);
    });

    test("usa política de vocabulário e estilo CDU configurada pelo projeto", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-cdus-politica-configurada-"));
        const diretorioCorpus = path.join(base, "documentacao", "casos");

        await escreverArquivo(
            path.join(diretorioCorpus, "cdu-01.md"),
            [
                "# CDU-01 - Exemplo externo",
                "",
                "## Atores",
                "",
                "- OPERADOR",
                "",
                "## Pré-condições",
                "",
                "- Usuário com perfil 'OPERADOR'.",
                "- Solicitação na situação 'Aberta'.",
                "",
                "## Fluxo principal",
                "",
                "1. O usuário abre o `Painel`."
            ].join("\n")
        );
        await escreverArquivo(
            path.join(base, "documentacao", "situacoes.md"),
            "- **Aberta**: Solicitação disponível.\n"
        );
        await escreverArquivo(
            path.join(base, "configuracao-toolkit.json"),
            JSON.stringify({
                versao: 2,
                requisitos: {
                    cdus: {
                        padraoArquivos: "documentacao/casos/cdu-*.md",
                        vocabulario: {
                            perfisCanonicos: ["OPERADOR"],
                            tiposProcessoCanonicos: ["Solicitação"],
                            arquivoSituacoesCanonicas: "documentacao/situacoes.md"
                        },
                        estilo: {
                            perfisEmCrases: ["OPERADOR"]
                        }
                    }
                }
            })
        );

        const inventario = await executarSgc([
            "requisitos",
            "cdus",
            "inventariar",
            "--secoes",
            "vocabulario",
            "--json",
            "--base",
            base
        ]);

        expect(inventario.exitCode).toBe(0);
        const vocabulario = lerJson<ResultadoInventarioCdus>(inventario).secoes.vocabulario!;
        expect(vocabulario.canonicos.perfis).toEqual(["OPERADOR"]);
        expect(vocabulario.canonicos.situacoes).toEqual(["Aberta"]);
        expect(vocabulario.canonicos.tiposProcesso).toEqual(["Solicitação"]);

        const estilo = await executarSgc([
            "requisitos",
            "cdus",
            "auditar",
            "--secoes",
            "estilo",
            "--json",
            "--base",
            base
        ]);

        expect(estilo.exitCode).toBe(0);
        const resultadoEstilo = lerJson<ResultadoAuditoriaCdu>(estilo).secoes.estilo!;
        expect(resultadoEstilo.relatorio[0].achados.some(achado => achado.regra === "perfil_em_aspas_simples")).toBe(true);
    });

    test("audita vocabulário controlado dos CDUs", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-cdus-vocabulario-auditar-"));
        const dirSpecs = path.join(base, "specs", "cdu");

        await escreverArquivo(
            path.join(dirSpecs, "cdu-01.md"),
            [
                "# CDU-01 - Exemplo",
                "",
                "## Atores",
                "",
                "- Admim",
                "",
                "## Pré-condições",
                "",
                "- Processo do tipo 'Diagnostico' na situação 'Auto avaliação concluída'.",
                "",
                "## Fluxo principal",
                "",
                "1. O usuário abre o `Painel`."
            ].join("\n")
        );
        await criarIntroSituacoes(dirSpecs);

        const resultado = await executarSgc([
            "requisitos",
            "cdus",
            "auditar",
            "--secoes",
            "vocabulario",
            "--json",
            "--base",
            base
        ]);

        expect(resultado.exitCode).toBe(0);
        const conteudo = lerJson<ResultadoAuditoriaCdu>(resultado);
        const vocabulario = conteudo.secoes.vocabulario!;
        expect(vocabulario.resumo.arquivosComAviso).toBe(1);
        const arquivo = vocabulario.relatorio.find(item => item.arquivo === "specs/cdu/cdu-01.md")!;
        expect(arquivo.achados.some(achado => achado.regra === "perfil_fora_vocabulario")).toBe(true);
        expect(arquivo.achados.some(achado => achado.regra === "tipo_processo_variacao")).toBe(true);
        expect(arquivo.achados.some(achado => achado.regra === "situacao_variacao")).toBe(true);
    });

    test("inventaria mensagens recorrentes dos CDUs", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-cdus-mensagens-"));
        const dirSpecs = path.join(base, "specs", "cdu");
        await criarIntroSituacoes(dirSpecs);

        await escreverArquivo(
            path.join(dirSpecs, "cdu-01.md"),
            [
                "# CDU-01 - Exemplo",
                "",
                "## Atores",
                "",
                "- ADMIN",
                "",
                "## Pré-condições",
                "",
                "- Usuário autenticado",
                "",
                "## Fluxo principal",
                "",
                "1. O sistema registra:",
                "   - `Descrição`: \"Cadastro aceito\"",
                "   - `Processo`: :DESCRICAO_PROCESSO:",
                "",
                "   ```text",
                "   Assunto: SGC: Cadastro aceito",
                "   ```",
                "",
                "2. O sistema mostra a mensagem \"Aceite registrado\".",
                "3. O sistema mostra *toast* \"Aceite registrado\"."
            ].join("\n")
        );

        const resultado = await executarSgc([
            "requisitos",
            "cdus",
            "inventariar",
            "--secoes",
            "mensagens",
            "--json",
            "--base",
            base
        ]);

        expect(resultado.exitCode).toBe(0);
        const conteudo = lerJson<ResultadoInventarioCdus>(resultado);
        const mensagens = conteudo.secoes.mensagens!;
        expect(mensagens.descricoes["Cadastro aceito"]).toBe(1);
        expect(mensagens.assuntos["SGC: Cadastro aceito"]).toBe(1);
        expect(mensagens.mensagens["Aceite registrado"]).toBe(1);
        expect(mensagens.toasts["Aceite registrado"]).toBe(1);
    });

    test("audita problemas mecânicos de mensagens dos CDUs", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-cdus-mensagens-auditar-"));
        const dirSpecs = path.join(base, "specs", "cdu");
        await criarIntroSituacoes(dirSpecs);

        await escreverArquivo(
            path.join(dirSpecs, "cdu-01.md"),
            [
                "# CDU-01 - Exemplo",
                "",
                "## Atores",
                "",
                "- ADMIN",
                "",
                "## Pré-condições",
                "",
                "- Usuário autenticado",
                "",
                "## Fluxo principal",
                "",
                "1. O sistema registra:",
                "   - `Descrição`: \"Mapa  homologado\"",
                "",
                "   ```text",
                "   Assunto: SGC: Reabertura - :SIGLA_UNIDADE:]",
                "   ```"
            ].join("\n")
        );

        const resultado = await executarSgc([
            "requisitos",
            "cdus",
            "auditar",
            "--secoes",
            "mensagens",
            "--json",
            "--base",
            base
        ]);

        expect(resultado.exitCode).toBe(0);
        const conteudo = lerJson<ResultadoAuditoriaCdu>(resultado);
        const mensagens = conteudo.secoes.mensagens!;
        expect(mensagens.resumo.arquivosComAviso).toBe(1);
        const arquivo = mensagens.relatorio.find(item => item.arquivo === "specs/cdu/cdu-01.md")!;
        expect(arquivo.achados.some(achado => achado.regra === "descricao_espacamento")).toBe(true);
        expect(arquivo.achados.some(achado => achado.regra === "assunto_fechamento_suspeito")).toBe(true);
    });

    test("compara mensagens dos CDUs com mensagens canônicas do código", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-cdus-mensagens-codigo-"));
        const dirSpecs = path.join(base, "specs", "cdu");
        await criarIntroSituacoes(dirSpecs);

        await escreverArquivo(
            path.join(dirSpecs, "cdu-01.md"),
            [
                "# CDU-01 - Exemplo",
                "",
                "## Atores",
                "",
                "- ADMIN",
                "",
                "## Pré-condições",
                "",
                "- Usuário autenticado",
                "",
                "## Fluxo principal",
                "",
                "1. O sistema registra:",
                "   - `Descrição`: \"Início do processo\"",
                "",
                "   ```text",
                "   Assunto: SGC: Cadastro de atividades e conhecimentos disponibilizado - :SIGLA_UNIDADE_SUBPROCESSO:",
                "   ```",
                "2. O sistema mostra a mensagem \"Homologação efetivada\".",
                "3. O sistema mostra *toast* \"Diagnóstico homologado\"."
            ].join("\n")
        );

        await escreverArquivo(
            path.join(base, "backend", "src", "main", "java", "sgc", "comum", "Mensagens.java"),
            [
                "package sgc.comum;",
                "public final class Mensagens {",
                "    public static final String HIST_PROCESSO_INICIADO = \"Processo iniciado\";",
                "    public static final String HIST_DIAGNOSTICO_HOMOLOGADO = \"Homologação de diagnóstico\";",
                "    public static final String ALERTA_DIAGNOSTICO_HOMOLOGADO = \"Diagnóstico da unidade %s homologado\";",
                "}"
            ].join("\n")
        );

        await escreverArquivo(
            path.join(base, "backend", "src", "main", "java", "sgc", "alerta", "AssuntosNotificacao.java"),
            [
                "package sgc.alerta;",
                "public final class AssuntosNotificacao {",
                "    public static String subprocesso() {",
                "        return \"SGC: Cadastro de atividades e conhecimentos disponibilizado - %s\".formatted(\"ABC\");",
                "    }",
                "}"
            ].join("\n")
        );

        await escreverArquivo(
            path.join(base, "frontend", "src", "constants", "notificacoes.ts"),
            "export const TIPOS_NOTIFICACAO_LABELS = { DIAGNOSTICO_HOMOLOGADO: \"Diagnóstico homologado\" };\n"
        );
        await escreverArquivo(
            path.join(base, "frontend", "src", "constants", "textos-subprocesso.ts"),
            "export const TEXTOS_SUCESSO_SUBPROCESSO = { HOMOLOGACAO_EFETIVADA: \"Homologação efetivada\" };\n"
        );
        await escreverArquivo(
            path.join(base, "frontend", "src", "constants", "textos-mapa.ts"),
            "export const TEXTOS_SUCESSO_MAPA = { SUCESSO_HOMOLOGACAO: \"Mapa homologado\" };\n"
        );
        await escreverArquivo(
            path.join(base, "frontend", "src", "constants", "textos-diagnostico.ts"),
            "export const TEXTOS_DIAGNOSTICO = { SUCESSO_DIAGNOSTICO_HOMOLOGADO: 'Diagnóstico homologado' };\n"
        );
        await escreverArquivo(
            path.join(base, "frontend", "src", "constants", "textos-processo.ts"),
            "export const TEXTOS_SUCESSO_PROCESSO = { PROCESSO_INICIADO: \"Processo iniciado\" };\n"
        );

        const resultado = await executarSgc([
            "requisitos",
            "cdus",
            "auditar",
            "--secoes",
            "mensagens-codigo",
            "--json",
            "--base",
            base
        ]);

        expect(resultado.exitCode).toBe(0);
        const agregado = lerJson<ResultadoAuditoriaCdu>(resultado);
        const conteudo = agregado.secoes.mensagensCodigo!;
        expect(conteudo.resumo.itensComReferenciaExata).toBeGreaterThan(0);
        const descricao = conteudo.relatorio.find(item => item.tipo === "descricoes" && item.valor === "Início do processo")!;
        expect(descricao.referenciasExatas).toHaveLength(0);
        expect(descricao.sugestoes.some(item => item.texto === "Processo iniciado")).toBe(true);
        const mensagem = conteudo.relatorio.find(item => item.tipo === "mensagens" && item.valor === "Homologação efetivada")!;
        expect(mensagem.referenciasExatas.some(item => item.texto === "Homologação efetivada")).toBe(true);
        const assunto = conteudo.relatorio.find(item => item.tipo === "assuntos" && item.valor === "SGC: Cadastro de atividades e conhecimentos disponibilizado - :SIGLA_UNIDADE_SUBPROCESSO:")!;
        expect(assunto.referenciasExatas.some(item => item.texto === "SGC: Cadastro de atividades e conhecimentos disponibilizado - :VALOR:")).toBe(true);
        const toast = conteudo.relatorio.find(item => item.tipo === "toasts" && item.valor === "Diagnóstico homologado")!;
        expect(toast.referenciasExatas.some(item => item.texto === "Diagnóstico homologado")).toBe(true);
    });

    test("usa fontes canônicas de mensagens configuradas pelo projeto", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-cdus-fontes-mensagens-configuradas-"));
        const dirSpecs = path.join(base, "specs", "cdu");

        await escreverArquivo(
            path.join(dirSpecs, "cdu-01.md"),
            [
                "# CDU-01 - Exemplo",
                "",
                "## Atores",
                "",
                "- ADMIN",
                "",
                "## Pré-condições",
                "",
                "- Usuário autenticado",
                "",
                "## Fluxo principal",
                "",
                "1. O sistema registra:",
                "   - `Descrição`: \"Pedido criado\""
            ].join("\n")
        );
        await escreverArquivo(
            path.join(base, "codigo", "mensagens.java"),
            "public static final String HIST_PEDIDO_CRIADO = \"Pedido criado\";\n"
        );
        await escreverArquivo(
            path.join(base, "configuracao-toolkit.json"),
            JSON.stringify({
                versao: 2,
                requisitos: {
                    cdus: {
                        fontesMensagensCodigo: [
                            {caminho: "codigo/mensagens.java", tipo: "mensagensJava"}
                        ]
                    }
                }
            })
        );

        const resultado = await executarSgc([
            "requisitos",
            "cdus",
            "auditar",
            "--secoes",
            "mensagens-codigo",
            "--json",
            "--base",
            base
        ]);

        expect(resultado.exitCode).toBe(0);
        const agregado = lerJson<ResultadoAuditoriaCdu>(resultado);
        const descricao = agregado.secoes.mensagensCodigo?.relatorio.find(item => item.valor === "Pedido criado");
        expect(descricao?.referenciasExatas.some(item => item.texto === "Pedido criado")).toBe(true);
    });

    test("inventaria densidade documental dos CDUs", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-cdus-densidade-"));
        const dirSpecs = path.join(base, "specs", "cdu");
        await criarIntroSituacoes(dirSpecs);

        await escreverArquivo(
            path.join(dirSpecs, "cdu-01.md"),
            [
                "# CDU-01 - Exemplo",
                "",
                "## Atores",
                "",
                "- ADMIN",
                "",
                "## Pré-condições",
                "",
                "- Usuário autenticado",
                "",
                "## Fluxo principal",
                "",
                "1. O usuário abre o `Painel`.",
                "2. O sistema mostra a tela `Detalhes`.",
                "   - item",
                "      - subitem"
            ].join("\n")
        );

        const resultado = await executarSgc([
            "requisitos",
            "cdus",
            "inventariar",
            "--secoes",
            "densidade",
            "--json",
            "--base",
            base
        ]);

        expect(resultado.exitCode).toBe(0);
        const agregado = lerJson<ResultadoInventarioCdus>(resultado);
        const conteudo = agregado.secoes.densidade!;
        expect(conteudo.totalArquivos).toBe(1);
        expect(conteudo.resumo.mediaPalavras).toBeGreaterThan(0);
        expect(conteudo.documentos[0].passos).toBe(2);
    });

    test("inventaria duplicações textuais dos CDUs", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-cdus-duplicacoes-"));
        const dirSpecs = path.join(base, "specs", "cdu");
        await criarIntroSituacoes(dirSpecs);

        const conteudo = [
            "# CDU-01 - Exemplo",
            "",
            "## Atores",
            "",
            "- ADMIN",
            "",
            "## Pré-condições",
            "",
            "- Usuário autenticado",
            "",
            "## Fluxo principal",
            "",
            "1. O sistema envia notificação.",
            "",
            "```text",
            "Assunto: SGC: Exemplo",
            "",
            "Prezada unidade, acompanhe o processo pelo sistema.",
            "```"
        ].join("\n");

        await escreverArquivo(path.join(dirSpecs, "cdu-01.md"), conteudo);
        await escreverArquivo(path.join(dirSpecs, "cdu-02.md"), conteudo.replace("CDU-01", "CDU-02"));

        const resultado = await executarSgc([
            "requisitos",
            "cdus",
            "inventariar",
            "--secoes",
            "duplicacoes",
            "--json",
            "--base",
            base
        ]);

        expect(resultado.exitCode).toBe(0);
        const agregado = lerJson<ResultadoInventarioCdus>(resultado);
        const corpo = agregado.secoes.duplicacoes!;
        expect(corpo.duplicacoes.length).toBeGreaterThan(0);
    });
});
