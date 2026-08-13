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
import {VERSAO_CONFIGURACAO} from "../biblioteca/configuracao.js";

describe("Resíduos do cliente", () => {
    test("trata politicas de residuos como overrides opcionais e explicita arquivos invalidos", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-residuos-politicas-opcionais-"));
        const caminhoOrcamento = path.join(base, "politicas", "orcamento.json");
        const caminhoExcecoes = path.join(base, "politicas", "excecoes.json");
        await escreverJson(path.join(base, "configuracao-toolkit.json"), {
            versao: VERSAO_CONFIGURACAO,
            diretorios: {
                orcamentoResiduosCliente: "politicas/orcamento.json",
                excecoesResiduosCliente: "politicas/excecoes.json"
            }
        });
        await escreverJson(caminhoOrcamento, {
            versaoSchema: "1.0.0",
            camadas: {},
            metricas: {maximosProducao: {}}
        });
        await escreverJson(caminhoExcecoes, {versaoSchema: "1.0.0", excecoes: []});

        const resultado = await executarSgc([
            "cliente", "residuos", "validar", "--json", "--base", base
        ]);
        expect(resultado.exitCode).toBe(0);
        const conteudo = JSON.parse(resultado.stdout);
        expect(conteudo.orcamento).toBe(path.relative(base, caminhoOrcamento));
        expect(conteudo.excecoes).toBe(path.relative(base, caminhoExcecoes));
        const gravacao = await executarSgc([
            "cliente", "residuos", "validar", "--json", "--gravar", "--base", base
        ]);
        expect(gravacao.exitCode).toBe(0);
        expect(await existe(path.join(base, "toolkit", "qualidade", "artefatos", "cliente-residuos", "mais-recente", "fotografia.json"))).toBe(true);

        await escreverArquivo(caminhoOrcamento, "{");
        const falha = await executarSgc([
            "cliente", "residuos", "validar", "--json", "--base", base
        ]);
        expect(falha.exitCode).toBe(1);
        expect(`${falha.stdout}\n${falha.stderr}`).toContain("Nao foi possivel ler a politica de residuos");

        await escreverJson(caminhoOrcamento, {
            versaoSchema: "99.0.0",
            camadas: {},
            metricas: {maximosProducao: {}}
        });
        const versaoInvalida = await executarSgc([
            "cliente", "residuos", "validar", "--json", "--base", base
        ]);
        expect(versaoInvalida.exitCode).toBe(1);
        expect(`${versaoInvalida.stdout}\n${versaoInvalida.stderr}`).toContain("versaoSchema 1.0.0");

        await escreverJson(caminhoOrcamento, {
            versaoSchema: "1.0.0",
            camadas: {},
            metricas: {maximosProducao: {}}
        });
        await escreverJson(caminhoExcecoes, {
            versaoSchema: "1.0.0",
            excecoes: [{arquivo: "", maxLinhas: -1}]
        });
        const excecaoInvalida = await executarSgc([
            "cliente", "residuos", "validar", "--json", "--base", base
        ]);
        expect(excecaoInvalida.exitCode).toBe(1);
        expect(`${excecaoInvalida.stdout}\n${excecaoInvalida.stderr}`).toContain("excecoes com arquivo ou maxLinhas invalidos");
    });

    test("audita residuos do cliente em um recorte controlado", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-residuos-auditar-"));
        const diretorioCliente = path.join(base, "frontend", "src");
        const orcamento = path.join(base, "orcamento.json");

        await escreverJson(orcamento, {
            versaoSchema: "1.0.0",
            camadas: {
                service: {meta: 4, limite: 8},
                component: {meta: 6, limite: 10},
                outro: {meta: 6, limite: 10}
            },
            metricas: {
                maximosProducao: {
                    anyExplicito: 1,
                    checksNull: 1,
                    fallbacksDefensivos: 1,
                    catchBlocks: 1,
                    castsDuplos: 0,
                    storageDireto: 1,
                    exportacoesSuspeitas: 1,
                    arquivosAcimaMetaPorCamada: {
                        service: 1,
                        component: 1,
                        outro: 0
                    }
                }
            }
        });

        await escreverArquivo(
            path.join(diretorioCliente, "services", "exemploService.ts"),
            [
                "export function exemploService(valor: any) {",
                "  if (valor === null) {",
                "    return valor || [];",
                "  }",
                "  return valor;",
                "}",
            ].join("\n")
        );
        await escreverArquivo(
            path.join(diretorioCliente, "components", "ExemploCard.vue"),
            [
                "<script setup lang=\"ts\">",
                "const salvar = () => localStorage.setItem('chave', 'valor');",
                "</script>",
                "<template><button @click=\"salvar\">Salvar</button></template>"
            ].join("\n")
        );

        const resultado = await executarSgc([
            "cliente",
            "residuos",
            "auditar",
            "--json",
            "--base",
            base,
            "--orcamento",
            "orcamento.json"
        ]);

        expect(resultado.exitCode).toBe(0);
        const conteudo = JSON.parse(resultado.stdout);
        expect(conteudo.versaoSchema).toBe("2.0.0");
        expect(conteudo.resumo.pontuacaoTotal).toBeTypeOf("number");
        expect(conteudo.pontosCriticos).toBeInstanceOf(Array);
        expect(conteudo.hotspots).toBeUndefined();
        expect(conteudo.contagens.producao.anyExplicito).toBe(1);
        expect(conteudo.contagens.producao.checksNull).toBe(1);
        expect(conteudo.contagens.producao.fallbacksDefensivos).toBe(1);
        expect(conteudo.contagens.producao.storageDireto).toBe(1);
        expect(conteudo.contagens.producao.exportacoesSuspeitas).toBe(1);
        expect(conteudo.contagens.producao.arquivosAcimaMeta.service).toBe(1);

        const gravacao = await executarSgc([
            "cliente",
            "residuos",
            "auditar",
            "--json",
            "--gravar",
            "--base",
            base,
            "--orcamento",
            "orcamento.json",
            "--saida",
            "artefatos/residuos"
        ]);
        expect(gravacao.exitCode).toBe(0);
        expect(await existe(path.join(base, "artefatos", "residuos", "fotografia.json"))).toBe(true);
    });

    test("calcula a saida padrao de residuos a partir da base externa", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-residuos-saida-base-"));
        await escreverArquivo(
            path.join(base, "frontend", "src", "exemplo.ts"),
            "export function carregarExemplo(codigo: string) { return codigo; }\n"
        );

        const resultado = await executarSgc([
            "cliente",
            "residuos",
            "auditar",
            "--json",
            "--gravar",
            "--base",
            base
        ]);

        expect(resultado.exitCode).toBe(0);
        const fotografia = JSON.parse(resultado.stdout);
        expect(fotografia.base).toBe(base);
        expect(await existe(path.join(
            base,
            "toolkit",
            "qualidade",
            "artefatos",
            "cliente-residuos",
            "mais-recente",
            "fotografia.json"
        ))).toBe(true);
    });

    test("classifica residuos usando codigoCliente configurado", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-residuos-codigo-configurado-"));
        await escreverJson(path.join(base, "configuracao-toolkit.json"), {
            versao: VERSAO_CONFIGURACAO,
            diretorios: {codigoCliente: "cliente/codigo"},
        });
        await escreverArquivo(
            path.join(base, "cliente", "codigo", "services", "exemploService.ts"),
            "export function carregarExemplo(codigo: string) { return codigo; }\n"
        );

        const resultado = await executarSgc([
            "cliente",
            "residuos",
            "auditar",
            "--json",
            "--base",
            base,
        ]);

        expect(resultado.exitCode).toBe(0);
        const fotografia = JSON.parse(resultado.stdout);
        expect(fotografia.resumo.arquivosProducao).toBe(1);
        expect(fotografia.arquivos[0].arquivo).toBe("cliente/codigo/services/exemploService.ts");
        expect(fotografia.arquivos[0].camada).toBe("service");
    });

    test("valida residuos do cliente com excecao de tamanho", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-residuos-validar-"));
        const diretorioCliente = path.join(base, "frontend", "src");
        const orcamento = path.join(base, "orcamento.json");
        const excecoes = path.join(base, "excecoes.json");

        await escreverJson(orcamento, {
            versaoSchema: "1.0.0",
            camadas: {
                service: {meta: 3, limite: 6},
                outro: {meta: 6, limite: 10}
            },
            metricas: {
                maximosProducao: {
                    anyExplicito: 0,
                    checksNull: 0,
                    fallbacksDefensivos: 0,
                    catchBlocks: 0,
                    castsDuplos: 0,
                    storageDireto: 0,
                    exportacoesSuspeitas: 2,
                    arquivosAcimaMetaPorCamada: {
                        service: 1,
                        outro: 0
                    }
                }
            }
        });
        await escreverJson(excecoes, {
            versaoSchema: "1.0.0",
            excecoes: [
                {
                    arquivo: "frontend/src/services/exemploService.ts",
                    camada: "service",
                    maxLinhas: 6,
                    responsavel: "teste",
                    justificativa: "Congelamento de baseline",
                    criterioRemocao: "Reduzir o arquivo."
                }
            ]
        });
        await escreverArquivo(
            path.join(diretorioCliente, "services", "exemploService.ts"),
            [
                "export function exemploService() {",
                "  return 1;",
                "}",
                "export function outro() {",
                "  return 2;",
                "}"
            ].join("\n")
        );

        const resultado = await executarSgc([
            "cliente",
            "residuos",
            "validar",
            "--json",
            "--base",
            base,
            "--orcamento",
            "orcamento.json",
            "--excecoes",
            "excecoes.json"
        ]);

        expect(resultado.exitCode).toBe(0);
        const conteudo = JSON.parse(resultado.stdout);
        expect(conteudo.status).toBe("ok");
        expect(conteudo.violacoes).toEqual([]);
        expect(await existe(path.join(base, "toolkit", "qualidade", "artefatos", "cliente-residuos"))).toBe(false);
        const gravacao = await executarSgc([
            "cliente",
            "residuos",
            "validar",
            "--json",
            "--gravar",
            "--base",
            base,
            "--orcamento",
            "orcamento.json",
            "--excecoes",
            "excecoes.json"
        ]);
        expect(gravacao.exitCode).toBe(0);
        expect(await existe(path.join(base, "toolkit", "qualidade", "artefatos", "cliente-residuos", "mais-recente", "fotografia.json"))).toBe(true);
    });
});
