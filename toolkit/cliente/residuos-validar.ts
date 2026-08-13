import path from "node:path";
import pc from "picocolors";
import {DIRETORIO_RAIZ} from "../biblioteca/caminhos.js";
import {
    analisarResiduosCliente,
    gravarFotografiaAuditoria,
    resolverDiretorioSaidaResiduos
} from "./residuos-lib.js";
import {
    carregarExcecoes,
    resolverCaminhoExcecoesResiduos,
    resolverCaminhoOrcamentoResiduos,
    type ExcecaoResiduo,
} from "./residuos-politicas.js";
import type {FotografiaResiduos} from "./residuos-lib.js";
import {escreverLinha, imprimirCabecalho, imprimirJson} from "../biblioteca/saida.js";
import {exibirAjudaComando} from "../biblioteca/cli-ajuda.js";
import {lerOpcao} from "../biblioteca/cli-opcoes.js";
import {ehEntradaPrincipal, validarArgumentosEntradaDireta} from "../biblioteca/execucao.js";

interface ViolacaoResiduoValidacao {
    tipo: string;
    mensagem: string;
    [chave: string]: unknown;
}

interface ResultadoValidacaoResiduos {
    status: "ok" | "falha";
    geradoEm: string;
    resumo: {
        pontuacaoTotal: number;
        faixa: FotografiaResiduos["resumo"]["faixa"];
        violacoes: number;
        avisos: number;
    };
    orcamento: string;
    excecoes: string;
    fotografia: FotografiaResiduos;
    violacoes: ViolacaoResiduoValidacao[];
    avisos: ViolacaoResiduoValidacao[];
}

interface ResumoValidacaoResiduos extends Omit<ResultadoValidacaoResiduos, "fotografia"> {
    pontosCriticos: FotografiaResiduos["pontosCriticos"];
}

interface OpcoesValidacaoClienteResiduos {
    base?: string;
    orcamento?: string;
    excecoes?: string;
    saida?: string;
    gravar?: boolean;
}

function criarViolacao(tipo: string, mensagem: string, detalhes: Record<string, unknown> = {}): ViolacaoResiduoValidacao {
    return {
        tipo,
        mensagem,
        ...detalhes,
    };
}

function indexarExcecoes(excecoes: ExcecaoResiduo[]): Map<string, ExcecaoResiduo> {
    return new Map(excecoes.map((excecao) => [excecao.arquivo, excecao]));
}

function resumirResultado(resultado: ResultadoValidacaoResiduos): ResumoValidacaoResiduos {
    return {
        status: resultado.status,
        geradoEm: resultado.geradoEm,
        resumo: resultado.resumo,
        orcamento: resultado.orcamento,
        excecoes: resultado.excecoes,
        violacoes: resultado.violacoes,
        avisos: resultado.avisos,
        pontosCriticos: resultado.fotografia.pontosCriticos,
    };
}

async function executarValidacaoClienteResiduos(
    opcoes: OpcoesValidacaoClienteResiduos = {}
): Promise<ResultadoValidacaoResiduos> {
    const base = path.resolve(opcoes.base ?? DIRETORIO_RAIZ);
    const resolverCaminho = (caminho: string): string => path.resolve(base, caminho);
    const caminhoOrcamentoConfigurado = opcoes.orcamento ?? resolverCaminhoOrcamentoResiduos(base);
    const caminhoExcecoesConfigurado = opcoes.excecoes ?? resolverCaminhoExcecoesResiduos(base);
    const caminhoOrcamento = caminhoOrcamentoConfigurado ? resolverCaminho(caminhoOrcamentoConfigurado) : undefined;
    const caminhoExcecoes = caminhoExcecoesConfigurado ? resolverCaminho(caminhoExcecoesConfigurado) : undefined;
    const caminhoSaida = opcoes.saida ? resolverCaminho(opcoes.saida) : resolverDiretorioSaidaResiduos(base);
    const fotografia = await analisarResiduosCliente({
        base,
        caminhoOrcamento,
    });
    const excecoes = await carregarExcecoes(caminhoExcecoes);
    const excecoesPorArquivo = indexarExcecoes(excecoes.excecoes);
    const violacoes: ViolacaoResiduoValidacao[] = [];
    const avisos: ViolacaoResiduoValidacao[] = [];

    const maximos = fotografia.orcamento.metricas?.maximosProducao ?? {};
    for (const [chave, maximo] of Object.entries(maximos)) {
        if (chave === "arquivosAcimaMetaPorCamada" || typeof maximo !== "number") {
            continue;
        }
        const valorAtual = fotografia.contagens.producao[chave as keyof typeof fotografia.contagens.producao];
        if (typeof valorAtual !== "number" || typeof maximo !== "number") {
            continue;
        }
        if (valorAtual > maximo) {
            violacoes.push(criarViolacao(
                "metrica_global",
                `Metrica ${chave} acima do orcamento: ${valorAtual} > ${maximo}`,
                {chave, valorAtual, maximo}
            ));
        }
    }

    const maximosCamadaValor = fotografia.orcamento.metricas?.maximosProducao?.arquivosAcimaMetaPorCamada;
    const maximosCamada = maximosCamadaValor && typeof maximosCamadaValor === "object" ? maximosCamadaValor : {};
    for (const [camada, maximo] of Object.entries(maximosCamada)) {
        const valorAtual = fotografia.contagens.producao.arquivosAcimaMeta[camada] ?? 0;
        if (valorAtual > maximo) {
            violacoes.push(criarViolacao(
                "quantidade_acima_meta",
                `Camada ${camada} excedeu o numero permitido de arquivos acima da meta: ${valorAtual} > ${maximo}`,
                {camada, valorAtual, maximo}
            ));
        }
    }

    for (const arquivo of fotografia.arquivos.filter((item) => item.categoriaArquivo === "producao")) {
        const excecao = excecoesPorArquivo.get(arquivo.arquivo);
        if (arquivo.linhas > arquivo.limites.meta) {
            if (!excecao) {
                violacoes.push(criarViolacao(
                    "arquivo_sem_excecao",
                    `Arquivo acima da meta sem excecao: ${arquivo.arquivo} (${arquivo.linhas} linhas)`,
                    {
                        arquivo: arquivo.arquivo,
                        camada: arquivo.camada,
                        linhas: arquivo.linhas,
                        meta: arquivo.limites.meta
                    }
                ));
            } else if (arquivo.linhas > excecao.maxLinhas) {
                violacoes.push(criarViolacao(
                    "arquivo_cresceu",
                    `Arquivo excedeu a excecao de tamanho: ${arquivo.arquivo} (${arquivo.linhas} > ${excecao.maxLinhas})`,
                    {
                        arquivo: arquivo.arquivo,
                        camada: arquivo.camada,
                        linhas: arquivo.linhas,
                        maxLinhas: excecao.maxLinhas
                    }
                ));
            }
        } else if (excecao) {
            avisos.push(criarViolacao(
                "excecao_obsoleta",
                `Excecao pode ser removida: ${arquivo.arquivo} ja voltou a meta (${arquivo.linhas} <= ${arquivo.limites.meta})`,
                {arquivo: arquivo.arquivo, camada: arquivo.camada, linhas: arquivo.linhas}
            ));
        }
    }

    if (opcoes.gravar) {
        await gravarFotografiaAuditoria(fotografia, caminhoSaida);
    }

    const resultado: ResultadoValidacaoResiduos = {
        status: violacoes.length === 0 ? "ok" : "falha",
        geradoEm: new Date().toISOString(),
        resumo: {
            pontuacaoTotal: fotografia.resumo.pontuacaoTotal,
            faixa: fotografia.resumo.faixa,
            violacoes: violacoes.length,
            avisos: avisos.length,
        },
        orcamento: caminhoOrcamento ? path.relative(base, caminhoOrcamento).replaceAll("\\", "/") : "padrao-do-toolkit",
        excecoes: caminhoExcecoes ? path.relative(base, caminhoExcecoes).replaceAll("\\", "/") : "padrao-do-toolkit",
        fotografia,
        violacoes,
        avisos,
    };

    return resultado;
}

async function principal(argumentosInformados: string[] = process.argv.slice(2)): Promise<void> {
    const argumentos = validarArgumentosEntradaDireta(import.meta.url, argumentosInformados);
    const emitirJson = argumentos.includes("--json");
    const emitirJsonResumido = argumentos.includes("--json-resumido");
    const exibirAjuda = argumentos.includes("--help") || argumentos.includes("-h");

    if (exibirAjuda) {
        exibirAjudaComando({
            comandoToolkit: "cliente residuos validar",
            descricao: "Valida orcamentos e excecoes dos residuos do cliente para impedir regressao estrutural.",
            opcoes: [
                "--json               Emite o resultado em JSON.",
                "--json-resumido      Emite somente status, resumo, violacoes e pontos criticos.",
                "--gravar             Atualiza a fotografia mais recente.",
                "--base <diretorio>   Sobrescreve o diretorio base da validacao.",
                "--orcamento <arquivo> Usa um arquivo de orcamento alternativo.",
                "--excecoes <arquivo>  Usa um arquivo de excecoes alternativo.",
                "--saida <diretorio>  Sobrescreve o diretorio de saida da fotografia."
            ],
            exemplos: [
                "ferramentas cliente residuos validar",
                "ferramentas cliente residuos validar --json",
                "ferramentas cliente residuos validar --base /tmp/sgc --orcamento /tmp/orcamento.json --excecoes /tmp/excecoes.json"
            ]
        });
        return;
    }

    const base = lerOpcao(argumentos, "--base", undefined);
    const baseResolvida = path.resolve(base ?? DIRETORIO_RAIZ);
    const orcamentoInformado = lerOpcao(argumentos, "--orcamento", resolverCaminhoOrcamentoResiduos(baseResolvida));
    const excecoesInformadas = lerOpcao(argumentos, "--excecoes", resolverCaminhoExcecoesResiduos(baseResolvida));
    const saida = lerOpcao(argumentos, "--saida", resolverDiretorioSaidaResiduos(baseResolvida));
    const resultado = await executarValidacaoClienteResiduos({
        base: baseResolvida,
        orcamento: orcamentoInformado,
        excecoes: excecoesInformadas,
        saida,
        gravar: argumentos.includes("--gravar"),
    });

    if (emitirJson) {
        imprimirJson(resultado);
        if (resultado.status !== "ok") process.exitCode = 1;
        return;
    }

    if (emitirJsonResumido) {
        imprimirJson(resumirResultado(resultado));
        if (resultado.status !== "ok") process.exitCode = 1;
        return;
    }

    imprimirCabecalho("VALIDACAO DE RESIDUOS DO CLIENTE");
    escreverLinha(`Status: ${resultado.status === "ok" ? pc.green("ok") : pc.red("falha")}`);
    escreverLinha(`Pontuacao total: ${resultado.resumo.pontuacaoTotal} (${resultado.resumo.faixa})`);
    escreverLinha(`Violacoes: ${resultado.resumo.violacoes}`);
    escreverLinha(`Avisos: ${resultado.resumo.avisos}`);
    escreverLinha("");

    if (resultado.violacoes.length > 0) {
        escreverLinha(pc.bold("Violacoes:"));
        resultado.violacoes.forEach((violacao, indice) => {
            escreverLinha(`${indice + 1}. ${violacao.mensagem}`);
        });
    } else {
        escreverLinha(pc.green("Nenhuma violacao de orcamento encontrada."));
    }

    if (resultado.avisos.length > 0) {
        escreverLinha("");
        escreverLinha(pc.bold("Avisos:"));
        resultado.avisos.forEach((aviso, indice) => {
            escreverLinha(`${indice + 1}. ${aviso.mensagem}`);
        });
    }

    escreverLinha("");
    escreverLinha(`Orcamento: ${resultado.orcamento}`);
    escreverLinha(`Excecoes: ${resultado.excecoes}`);
    escreverLinha(`Fotografia mais recente: ${path.relative(baseResolvida, path.resolve(baseResolvida, saida ?? resolverDiretorioSaidaResiduos(baseResolvida))).replaceAll("\\", "/")}`);

    if (resultado.status !== "ok") process.exitCode = 1;
}

if (ehEntradaPrincipal(import.meta.url)) {
    principal().catch((erro) => {
        const mensagem = erro instanceof Error ? erro.message : String(erro);
        escreverLinha(pc.red(`Erro na validacao de residuos: ${mensagem}`));
        process.exitCode = 1;
    });
}

export {
    executarValidacaoClienteResiduos,
    principal,
};
