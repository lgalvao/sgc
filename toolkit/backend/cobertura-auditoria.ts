import fs from "node:fs/promises";
import path from "node:path";
import pc from "picocolors";
import {resolverNaRaiz} from "../lib/caminhos.js";
import {lerNumero, lerOpcao} from "../lib/cli-opcoes.js";
import {ehEntradaPrincipal, validarArgumentosEntradaDireta} from "../lib/execucao.js";
import {extrairCoberturaJacoco, type ClasseCobertura, type ResultadoCoberturaJacoco} from "../lib/dominios/cobertura-java.js";
import {escreverLinha, imprimirCabecalho, imprimirJson} from "../lib/saida.js";
import {exibirAjudaComando} from "../lib/cli-ajuda.js";

const CAMINHO_PADRAO_SAIDA = "backend-cobertura-auditoria.md";
const VERSAO_SCHEMA_RESULTADO = "1.0.0" as const;

interface PontoCriticoRelatorio {
    nome: string;
    complexidade: number;
    linhasPerdidas: number;
    linhasPerdidasLista: number[];
    ramificacoesPerdidas: number;
    ramificacoesPerdidasLista: string[];
    pontuacaoImpacto: number;
    coberturaLinhas: number;
}

interface ResultadoAuditoriaCobertura {
    status: "ok";
    versaoSchema: typeof VERSAO_SCHEMA_RESULTADO;
    geradoEm: string;
    totais: ResultadoCoberturaJacoco;
    pontosCriticos: PontoCriticoRelatorio[];
}

function calcularPontuacaoImpacto(classe: ClasseCobertura): number {
    // Classes complexas com lacunas são os verdadeiros pontos críticos.
    // Se não há lacunas (100% de cobertura), a pontuação deve ser zero.

    if (classe.linhasPerdidas === 0 && classe.ramificacoesPerdidas === 0) {
        return 0;
    }

    const pesoLinhas = 1.0;
    const pesoRamificacoes = 1.5;
    const fatorComplexidade = 1 + (classe.complexidade / 50); // Multiplicador baseado na complexidade

    const pontuacaoLacunas = (classe.linhasPerdidas * pesoLinhas) +
        (classe.ramificacoesPerdidas * pesoRamificacoes);

    return pontuacaoLacunas * fatorComplexidade;
}

function obterPrioridade(pontuacao: number): string {
    if (pontuacao > 50) return pc.red("P1 (Crítico)");
    if (pontuacao > 20) return pc.yellow("P2 (Alto)");
    return pc.cyan("P3 (Médio)");
}

async function gerarRelatorioMarkdown(dados: ResultadoAuditoriaCobertura, caminho: string): Promise<string> {
    const {totais, pontosCriticos} = dados;
    let markdown = "# Auditoria de Cobertura Backend\n\n";

    markdown += `## Resumo Geral\n`;
    markdown += `- **Cobertura Global (Instruções):** ${totais.instrucoes.percentual}%\n`;
    markdown += `- **Cobertura de Linhas:** ${totais.linhas.percentual}%\n`;
    markdown += `- **Cobertura de Ramificações:** ${totais.ramificacoes.percentual}%\n`;
    markdown += `- **Complexidade Total:** ${totais.complexidade.cobertos + totais.complexidade.perdidos}\n\n`;

    markdown += `## Top 10 Pontos Críticos de Qualidade (Maior Risco)\n`;
    markdown += `Prioridade baseada em complexidade ciclomática cruzada com lacunas de teste.\n\n`;
    markdown += `| Posição | Classe | Pontuação | Complexidade | Linhas S/ Cobertura | Ramificações S/ Cobertura | Prioridade |\n`;
    markdown += `|---------|--------|-----------|--------------|---------------------|-----------------------|------------|\n`;

    pontosCriticos.slice(0, 10).forEach((ponto, indice) => {
        const prioridade = ponto.pontuacaoImpacto > 50 ? "P1" : (ponto.pontuacaoImpacto > 20 ? "P2" : "P3");
        markdown += `| ${indice + 1} | \`${ponto.nome}\` | ${ponto.pontuacaoImpacto.toFixed(1)} | ${ponto.complexidade} | ${ponto.linhasPerdidas} | ${ponto.ramificacoesPerdidas} | ${prioridade} |\n`;
    });

    markdown += `\n## Detalhamento das Lacunas dos Principais Pontos Críticos\n\n`;
    pontosCriticos.slice(0, 10).forEach((ponto) => {
        if (ponto.linhasPerdidas > 0 || ponto.ramificacoesPerdidas > 0) {
            markdown += `### \`${ponto.nome}\` (Risco: ${ponto.pontuacaoImpacto.toFixed(1)})\n`;
            if (ponto.linhasPerdidas > 0) {
                markdown += `- **Linhas 100% descobertas:** ${ponto.linhasPerdidasLista.join(", ")}\n`;
            }
            if (ponto.ramificacoesPerdidas > 0) {
                markdown += `- **Ramificações sem cobertura total/parcial (linha(perdidos/total)):** ${ponto.ramificacoesPerdidasLista.join(", ")}\n`;
            }
            markdown += `\n`;
        }
    });

    markdown += `\n\n_Gerado automaticamente pelo toolkit em ${new Date().toLocaleString("pt-BR")}._\n`;

    await fs.mkdir(path.dirname(caminho), {recursive: true});
    await fs.writeFile(caminho, markdown, "utf8");
    return caminho;
}

async function principal(argumentosInformados: string[] = process.argv.slice(2)): Promise<void> {
    const argumentos = validarArgumentosEntradaDireta(import.meta.url, argumentosInformados);
    const emitirJson = argumentos.includes("--json");
    const exibirAjuda = argumentos.includes("--help") || argumentos.includes("-h");

    if (exibirAjuda) {
        exibirAjudaComando({
            comandoSgc: "backend cobertura auditoria",
            scriptDireto: "backend/cobertura-auditoria.ts",
            descricao: "Auditoria unificada de cobertura e risco (Backend).",
            opcoes: [
                "--json              Saída em formato JSON para integração com outras ferramentas.",
                "--saida <arquivo>    Caminho do arquivo Markdown a ser gerado.",
                "--arquivo <xml>     Usa um relatório JaCoCo específico.",
                "--base <diretorio>   Resolve o relatório relativo a outra base.",
                "--gravar             Persiste o relatório Markdown.",
                "--minimo <percentual> Falha se a cobertura global for menor que a meta."
            ]
        });
        return;
    }

    const diretorioBase = path.resolve(lerOpcao(argumentos, "--base", resolverNaRaiz()) ?? resolverNaRaiz());
    const arquivo = lerOpcao(argumentos, "--arquivo", "") ?? "";
    const caminhoSaida = lerOpcao(argumentos, "--saida", CAMINHO_PADRAO_SAIDA) ?? CAMINHO_PADRAO_SAIDA;
    const gravar = argumentos.includes("--gravar");
    const metaMinima = lerNumero(argumentos, "--minimo", 0, {inteiro: false, minimo: 0, maximo: 100}) ?? 0;

    if (!emitirJson) {
        imprimirCabecalho("AUDITORIA DE COBERTURA BACKEND");
    }

    try {
        const coleta = await extrairCoberturaJacoco(arquivo || undefined, {
            diretorioBase,
            incluirSemLacunas: true,
            aplicarExclusoes: true
        });

        const pontosCriticos = coleta.classes
            .map(c => ({
                ...c,
                pontuacaoImpacto: calcularPontuacaoImpacto(c)
            }))
            .filter(c => c.pontuacaoImpacto > 0)
            .toSorted((a, b) => b.pontuacaoImpacto - a.pontuacaoImpacto)
            .slice(0, 20);

        const resultado: ResultadoAuditoriaCobertura = {
            status: "ok",
            geradoEm: new Date().toISOString(),
            totais: coleta,
            versaoSchema: VERSAO_SCHEMA_RESULTADO,
            pontosCriticos: pontosCriticos.map(ponto => ({
                nome: ponto.nome,
                complexidade: ponto.complexidade,
                linhasPerdidas: ponto.linhasPerdidas,
                linhasPerdidasLista: ponto.linhasPerdidasLista,
                ramificacoesPerdidas: ponto.ramificacoesPerdidas,
                ramificacoesPerdidasLista: ponto.ramificacoesPerdidasLista,
                pontuacaoImpacto: ponto.pontuacaoImpacto,
                coberturaLinhas: ponto.linhasPercentual
            }))
        };

        const caminhoRelatorio = path.isAbsolute(caminhoSaida)
            ? caminhoSaida
            : path.resolve(diretorioBase, caminhoSaida);

        if (gravar) {
            await gerarRelatorioMarkdown(resultado, caminhoRelatorio);
        }

        if (emitirJson) {
            imprimirJson(resultado);
            if (metaMinima > 0 && coleta.instrucoes.percentual < metaMinima) {
                process.exitCode = 1;
            }
            return;
        }

        escreverLinha(`${pc.bold("Resumo do Projeto:")}`);
        escreverLinha(`  Instruções: ${coleta.instrucoes.percentual}%`);
        escreverLinha(`  Linhas:      ${coleta.linhas.percentual}%`);
        escreverLinha(`  Ramificações: ${coleta.ramificacoes.percentual}%`);
        escreverLinha("");

        escreverLinha(pc.bold(pc.underline("TOP 5 PENDÊNCIAS PRIORITÁRIAS:")));
        pontosCriticos.slice(0, 5).forEach((ponto, indice) => {
            escreverLinha(`${indice + 1}. ${pc.bold(ponto.nome)}`);
            escreverLinha(`   Impacto: ${pc.bold(ponto.pontuacaoImpacto.toFixed(1))} | Prioridade: ${obterPrioridade(ponto.pontuacaoImpacto)}`);
            escreverLinha(`   Lacunas: ${ponto.linhasPerdidas} linhas e ${ponto.ramificacoesPerdidas} ramificações sem teste.`);
            if (ponto.linhasPerdidas > 0) {
                escreverLinha(`     ↳ Linhas sem cobertura: ${pc.dim(ponto.linhasPerdidasLista.join(", "))}`);
            }
            if (ponto.ramificacoesPerdidas > 0) {
                escreverLinha(`     ↳ Ramificações sem cobertura (linha(perdidos/total)): ${pc.dim(ponto.ramificacoesPerdidasLista.join(", "))}`);
            }
        });

        if (gravar) {
            escreverLinha(`\n${pc.green("✓")} Relatório detalhado gerado em: ${pc.dim(caminhoRelatorio)}`);
        }

        if (metaMinima > 0 && coleta.instrucoes.percentual < metaMinima) {
            escreverLinha(pc.red(`\nFALHA: Cobertura global (${coleta.instrucoes.percentual}%) abaixo da meta (${metaMinima}%).`));
            process.exitCode = 1;
        }

    } catch (erro) {
        const mensagem = erro instanceof Error ? erro.message : String(erro);
        if (emitirJson) {
            imprimirJson({status: "erro", mensagem});
        } else {
            escreverLinha(pc.red(`Erro na auditoria: ${mensagem}`));
        }
        process.exitCode = 1;
    }
}

if (ehEntradaPrincipal(import.meta.url)) {
    await principal();
}

export {
    calcularPontuacaoImpacto,
    principal
};
