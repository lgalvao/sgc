import path from "node:path";
import pc from "picocolors";
import {DIRETORIO_RAIZ} from "../lib/caminhos.js";
import {
    analisarResiduosFrontend,
    gravarFotografiaAuditoria,
    resolverCaminhoOrcamentoResiduos,
    resolverDiretorioSaidaResiduos
} from "./residuos-lib.js";
import type {FotografiaResiduos} from "./residuos-lib.js";
import {escreverLinha, imprimirCabecalho, imprimirJson} from "../lib/saida.js";
import {exibirAjudaComando} from "../lib/cli-ajuda.js";
import {lerOpcao} from "../lib/cli-opcoes.js";
import {ehEntradaPrincipal, validarArgumentosEntradaDireta} from "../lib/execucao.js";

interface OpcoesAuditoriaFrontendResiduos {
    base?: string;
    orcamento?: string;
    saida?: string;
    gravar?: boolean;
}

async function executarAuditoriaFrontendResiduos(opcoes: OpcoesAuditoriaFrontendResiduos = {}): Promise<FotografiaResiduos> {
    const base = path.resolve(opcoes.base ?? DIRETORIO_RAIZ);
    const fotografia = await analisarResiduosFrontend({
        base,
        caminhoOrcamento: opcoes.orcamento ? path.resolve(base, opcoes.orcamento) : undefined,
    });

    if (opcoes.gravar) {
        const diretorioSaida = opcoes.saida ? path.resolve(fotografia.base, opcoes.saida) : undefined;
        await gravarFotografiaAuditoria(fotografia, diretorioSaida);
    }

    return fotografia;
}

async function principal(argumentosInformados: string[] = process.argv.slice(2)): Promise<void> {
    const argumentos = validarArgumentosEntradaDireta(import.meta.url, argumentosInformados);
    const emitirJson = argumentos.includes("--json");
    const exibirAjuda = argumentos.includes("--help") || argumentos.includes("-h");

    if (exibirAjuda) {
        exibirAjudaComando({
            comandoSgc: "frontend residuos auditar",
            scriptDireto: "frontend/residuos-auditar.ts",
            descricao: "Audita sinais de residuos estruturais e defensividade acidental no frontend.",
            opcoes: [
                "--json               Emite a fotografia em JSON.",
                "--gravar             Atualiza fotografia e resumo em disco.",
                "--base <diretorio>   Sobrescreve o diretorio base da auditoria.",
                "--orcamento <arquivo> Usa um arquivo de orcamento alternativo.",
                "--saida <diretorio>  Sobrescreve o diretorio de saida da fotografia."
            ],
            exemplos: [
                "npx tsx toolkit/sgc.ts frontend residuos auditar",
                "npx tsx toolkit/sgc.ts frontend residuos auditar --json",
                "npx tsx toolkit/sgc.ts frontend residuos auditar --gravar --base /tmp/sgc"
            ]
        });
        return;
    }

    const base = lerOpcao(argumentos, "--base", undefined);
    const baseResolvida = path.resolve(base ?? DIRETORIO_RAIZ);
    const saida = lerOpcao(argumentos, "--saida", resolverDiretorioSaidaResiduos(baseResolvida));
    const fotografia = await executarAuditoriaFrontendResiduos({
        base: baseResolvida,
        orcamento: lerOpcao(argumentos, "--orcamento", resolverCaminhoOrcamentoResiduos(baseResolvida)),
        saida,
        gravar: argumentos.includes("--gravar"),
    });

    if (emitirJson) {
        imprimirJson(fotografia);
        return;
    }

    imprimirCabecalho("AUDITORIA DE RESIDUOS DO FRONTEND");
    escreverLinha(`Score total: ${pc.bold(String(fotografia.resumo.scoreTotal))} (${fotografia.resumo.faixa})`);
    escreverLinha(`Arquivos de producao: ${fotografia.resumo.arquivosProducao}`);
    escreverLinha(`Arquivos de teste/story: ${fotografia.resumo.arquivosTeste}`);
    escreverLinha("");
    escreverLinha("Sinais em producao:");
    escreverLinha(`- any explicito: ${fotografia.contagens.producao.anyExplicito}`);
    escreverLinha(`- checks de null: ${fotografia.contagens.producao.checksNull}`);
    escreverLinha(`- fallbacks defensivos: ${fotografia.contagens.producao.fallbacksDefensivos}`);
    escreverLinha(`- blocos catch: ${fotografia.contagens.producao.catchBlocks}`);
    escreverLinha(`- casts duplos: ${fotografia.contagens.producao.castsDuplos}`);
    escreverLinha(`- storage direto: ${fotografia.contagens.producao.storageDireto}`);
    escreverLinha(`- exportacoes suspeitas: ${fotografia.contagens.producao.exportacoesSuspeitas}`);
    escreverLinha("");
    escreverLinha(pc.bold("Top 5 hotspots:"));
    fotografia.hotspots.slice(0, 5).forEach((hotspot, indice) => {
        escreverLinha(`${indice + 1}. ${hotspot.arquivo} [${hotspot.camada}]`);
        escreverLinha(`   Linhas: ${hotspot.linhas} | Score: ${hotspot.score}`);
    });

    if (argumentos.includes("--gravar")) {
        const diretorio = path.resolve(fotografia.base, saida ?? resolverDiretorioSaidaResiduos(fotografia.base));
        escreverLinha("");
        escreverLinha(`${pc.green("✓")} Fotografia salva em ${path.relative(fotografia.base, diretorio).replaceAll("\\", "/")}`);
    }
}

if (ehEntradaPrincipal(import.meta.url)) {
    principal().catch((erro) => {
        const mensagem = erro instanceof Error ? erro.message : String(erro);
        escreverLinha(pc.red(`Erro na auditoria de residuos: ${mensagem}`));
        process.exitCode = 1;
    });
}

export {
    executarAuditoriaFrontendResiduos,
    principal,
};
