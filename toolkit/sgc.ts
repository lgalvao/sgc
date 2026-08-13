import {pathToFileURL} from "node:url";
import {Command} from "commander";
import pc from "picocolors";
import {CATALOGO_COMANDOS, CATALOGO_COMANDOS_COMPLETO} from "./lib/catalogo-comandos.js";
import {executarNode} from "./lib/execucao.js";
import logger from "./lib/logger.js";

function criarGrupoComando(pai: Command, nome: string, descricao: string): Command {
    return pai.command(nome).description(descricao);
}

function criarComandoArquivo(pai: Command, nome: string, descricao: string, relativo: string): void {
    pai
        .command(nome)
        .description(descricao)
        .helpOption(false)
        .allowUnknownOption(true)
        .allowExcessArguments(true)
        .action(async (...valores: unknown[]) => {
            const comando = valores.at(-1) as Command;
            const argumentos = comando.args ?? [];
            await executarNode(relativo, argumentos);
        });
}

function obterComandoPai(programa: Command, caminho: readonly string[]): Command {
    let atual = programa;
    for (const segmento of caminho) {
        const proximo = atual.commands.find(comando => comando.name() === segmento);
        if (!proximo) {
            throw new Error(`Grupo de comando não encontrado no catálogo: ${[...caminho].join(" ")}`);
        }
        atual = proximo;
    }
    return atual;
}

function obterDescricaoComando(caminho: readonly string[]): string {
    const definicao = CATALOGO_COMANDOS_COMPLETO.find(item => item.caminho.join(" ") === caminho.join(" "));
    if (!definicao) {
        throw new Error(`Comando sem classificação no catálogo: ${caminho.join(" ")}`);
    }
    return definicao.descricao;
}

function registrarComandosCatalogados(programa: Command): void {
    for (const definicao of CATALOGO_COMANDOS) {
        const nome = definicao.caminho.at(-1);
        if (!nome) {
            throw new Error("Comando sem nome no catálogo.");
        }

        const pai = obterComandoPai(programa, definicao.caminho.slice(0, -1));
        if (pai.commands.some(comando => comando.name() === nome)) {
            throw new Error(`Comando duplicado no catálogo: ${definicao.caminho.join(" ")}`);
        }
        criarComandoArquivo(pai, nome, definicao.descricao, definicao.arquivo);
    }
}

const program = new Command();
program
    .name("sgc")
    .description("Toolkit do SGC para backend, frontend, qualidade e automacoes de projeto.")
    .showHelpAfterError()
    .showSuggestionAfterError();

const backend = program.command("backend").description("Ferramentas do backend.");
criarGrupoComando(backend, "cobertura", "Cobertura e diagnosticos do backend.");
criarGrupoComando(backend, "arquitetura", "Auditorias de arquitetura do backend.");
criarGrupoComando(backend, "coesao", "Auditorias de coesao do backend.");
criarGrupoComando(backend, "contratos", "Auditorias de contratos HTTP e DTOs publicos do backend.");
criarGrupoComando(backend, "testes", "Ferramentas de testes do backend.");
criarGrupoComando(backend, "java", "Utilitarios Java do backend.");
criarGrupoComando(backend, "notificacoes", "Auditorias de notificacoes e assuntos do backend.");

const frontend = program.command("frontend").description("Ferramentas do frontend.");
criarGrupoComando(frontend, "cobertura", "Cobertura e diagnosticos do frontend.");
criarGrupoComando(frontend, "residuos", "Auditorias de residuos estruturais e orcamentos do frontend.");
criarGrupoComando(frontend, "arquitetura", "Auditorias de arquitetura e vazamento de contratos no frontend.");
criarGrupoComando(frontend, "views", "Auditorias especificas de views.");
criarGrupoComando(frontend, "modais", "Auditorias especificas de modais.");
criarGrupoComando(frontend, "identificadores-teste", "Ferramentas para identificadores de teste.");

const codigo = program.command("codigo").description("Ferramentas de manutencao e higiene do código.");
criarGrupoComando(codigo, "cheiros", "Auditorias de cheiros de codigo.");
criarGrupoComando(codigo, "semgrep", "Auditorias estruturais com Semgrep OSS.");
criarGrupoComando(codigo, "nomes", "Inventario e auditoria de nomenclatura do projeto.");

const integracao = program.command("integracao").description("Ferramentas de qualidade na fronteira backend/frontend.");
criarGrupoComando(integracao, "contratos", "Auditorias e artefatos de contrato HTTP.");

const requisitos = program.command("requisitos").description("Ferramentas de inventario e auditoria de requisitos.");
criarGrupoComando(requisitos, "cdus", "Inventario e auditoria read-only dos casos de uso.");

const qualidade = program.command("qualidade").description("Ferramentas de coleta e analise da qualidade.");
const tarefasQualidade = qualidade
    .command("tarefas")
    .description("Executa tarefas de qualidade configuradas para o projeto.");
qualidade
    .command("coletar")
    .description(obterDescricaoComando(["qualidade", "coletar"]))
    .allowUnknownOption(true)
    .option("--perfil <perfil>", "Perfil de execucao (rapido, completo, backend, frontend).", "rapido")
    .option("--base <diretorio>", "Sobrescreve o diretorio base do projeto auditado.")
    .action(async (opcoes, comando) => {
        const {executarColetaQualidade} = await import("./qualidade/coleta.js");
        const argsExtras = comando.args ?? [];
        const args = ["--perfil", opcoes.perfil];
        if (opcoes.base) {
            args.push("--base", opcoes.base);
        }
        args.push(...argsExtras);
        await executarColetaQualidade(args);
    });
qualidade
    .command("resumo")
    .description(obterDescricaoComando(["qualidade", "resumo"]))
    .option("--arquivo <caminho>", "Usa uma fotografia especifica em vez da mais recente.")
    .option("--base <diretorio>", "Resolve a fotografia mais recente a partir da base do projeto.")
    .option("--json", "Emite saida estruturada em JSON.")
    .option("--limite-pontos-criticos <n>", "Limita a quantidade de pontos criticos exibidos.", Number.parseInt)
    .action(async (opcoes) => {
        const {executarResumoQualidade} = await import("./qualidade/resumo.js");
        const resultado = await executarResumoQualidade(opcoes) as {resumo?: {statusGeral?: string}};
        if (resultado.resumo?.statusGeral === "vermelho") {
            process.exitCode = 1;
        }
    });

const projeto = program.command("projeto").description("Ferramentas transversais do repositório.");
projeto
    .command("dependencias")
    .description("Ferramentas para auditar uso e declaracao de dependencias.")
    .command("auditar")
    .description(obterDescricaoComando(["projeto", "dependencias", "auditar"]))
    .option("--base <diretorio>", "Sobrescreve o diretório base para auditoria.")
    .action(async (opcoes) => {
        const {executarAuditoriaDependencias} = await import("./projeto/dependencias-auditar.js");
        const resultado = await executarAuditoriaDependencias(opcoes);
        if (resultado.statusGeral !== "ok") {
            process.exitCode = 1;
        }
    });

const ambiente = projeto
    .command("ambiente")
    .description("Ferramentas para verificar o ambiente do projeto auditado.");
ambiente
    .command("verificar")
    .description(obterDescricaoComando(["projeto", "ambiente", "verificar"]))
    .option("--json", "Emite saida estruturada em JSON.")
    .option("--base <diretorio>", "Sobrescreve o diretório base para verificação.")
    .action(async (opcoes) => {
        const {executarVerificacaoAmbiente} = await import("./projeto/ambiente-verificar.js");
        const resultado = await executarVerificacaoAmbiente(opcoes);
        if (resultado.statusGeral === "falha") {
            process.exitCode = 1;
        }
    });

const artefatos = projeto
    .command("artefatos")
    .description("Ferramentas para inspecionar e limpar artefatos gerados.");
artefatos
    .command("limpar")
    .description(obterDescricaoComando(["projeto", "artefatos", "limpar"]))
    .option("--json", "Emite saida estruturada em JSON.")
    .option("--confirmar", "Remove de fato os artefatos elegiveis.")
    .option("--base <diretorio>", "Sobrescreve o diretório base para limpeza.")
    .action(async (opcoes) => {
        const {limparArtefatos} = await import("./projeto/artefatos-limpar.js");
        await limparArtefatos(opcoes);
    });

tarefasQualidade
    .command("executar [perfil]")
    .description(obterDescricaoComando(["qualidade", "tarefas", "executar"]))
    .option("--base <diretorio>", "Sobrescreve o diretorio base do projeto.")
    .action(async (perfil = "rapido", opcoes) => {
        const {executarTarefasQualidade} = await import("./qualidade/tarefas-executar.js");
        await executarTarefasQualidade(perfil, opcoes);
    });

registrarComandosCatalogados(program);

program.addHelpText(
    "after",
    `\nExemplos:\n  ${pc.dim("npx tsx toolkit/sgc.ts backend cobertura auditoria")}\n  ${pc.dim("npx tsx toolkit/sgc.ts frontend cobertura auditoria")}\n  ${pc.dim("npx tsx toolkit/sgc.ts qualidade coletar --perfil rapido")}\n  ${pc.dim("npx tsx toolkit/sgc.ts qualidade resumo")}\n  ${pc.dim("npx tsx toolkit/sgc.ts projeto ambiente verificar --json")}\n  ${pc.dim("npx tsx toolkit/sgc.ts codigo cheiros auditar --json")}`
);

async function executar(argumentos = process.argv) {
    await program.parseAsync(argumentos);
}

async function principal(argumentos = process.argv) {
    try {
        await executar(argumentos);
        return 0;
    } catch (erro) {
        const mensagem = erro instanceof Error ? erro.message : String(erro);
        logger.error(pc.red(`Erro: ${mensagem}`));
        process.exitCode = 1;
        return 1;
    }
}

if (process.argv[1] && import.meta.url === pathToFileURL(process.argv[1]).href) {
    await principal();
}

export {
    criarComandoArquivo,
    executar,
    principal,
    program
};
