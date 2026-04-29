#!/usr/bin/env node
import {Command} from "commander";
import pc from "picocolors";
import {executarNode} from "./lib/execucao.js";
import logger from "./lib/logger.js";
import {executarDoctor} from "./projeto/doctor.js";
import {executarLimpeza} from "./projeto/limpar.js";
import {executarPerfilQualidade} from "./projeto/qualidade.js";
import {executarSetup} from "./projeto/setup.js";
import {executarSnapshotQa} from "./qa/snapshot-coletar.js";
import {executarResumoQa} from "./qa/resumo.js";

function criarComandoScript(pai, nome, descricao, relativo) {
    pai
        .command(nome)
        .description(descricao)
        .allowUnknownOption(true)
        .allowExcessArguments(true)
        .action(async (...valores) => {
            const comando = valores.at(-1);
            const args = comando.args ?? [];
            await executarNode(relativo, args);
        });
}

const program = new Command();
program
    .name("sgc")
    .description("Toolkit do SGC para backend, frontend, QA e automacoes de projeto.")
    .showHelpAfterError()
    .showSuggestionAfterError();

const backend = program.command("backend").description("Ferramentas do backend.");
const backendCobertura = backend.command("cobertura").description("Cobertura e diagnosticos do backend.");
criarComandoScript(backendCobertura, "auditoria", "Auditoria unificada de cobertura e risco (Backend).", "etc/scripts/backend/cobertura-auditoria.js");

const backendTestes = backend.command("testes").description("Ferramentas de testes do backend.");
criarComandoScript(backendTestes, "analisar", "Detecta classes sem testes e gera Markdown/JSON.", "etc/scripts/backend/testes-analisar.js");
criarComandoScript(backendTestes, "priorizar", "Prioriza backlog de testes do backend.", "etc/scripts/backend/testes-priorizar.js");
criarComandoScript(backendTestes, "gerar-stub", "Gera CoverageTest inicial para uma classe.", "etc/scripts/backend/testes-gerar-stub.js");

const backendJava = backend.command("java").description("Utilitarios Java do backend.");
criarComandoScript(backendJava, "corrigir-fqn", "Substitui FQNs por imports em arquivos Java.", "etc/scripts/backend/java-corrigir-fqn.js");
criarComandoScript(backendJava, "auditar-null", "Audita verificacoes de null no backend.", "etc/scripts/backend/java-auditar-null.js");
criarComandoScript(backendJava, "instalar-certificados", "Importa certificados locais no cacerts.", "etc/scripts/backend/java-instalar-certificados.js");

const frontend = program.command("frontend").description("Ferramentas do frontend.");
const frontendCobertura = frontend.command("cobertura").description("Cobertura e diagnosticos do frontend.");
criarComandoScript(frontendCobertura, "auditoria", "Auditoria unificada de cobertura e risco (Frontend).", "etc/scripts/frontend/cobertura-auditoria.js");

const frontendMensagens = frontend.command("mensagens").description("Analise de mensagens e strings do frontend.");
criarComandoScript(frontendMensagens, "extrair", "Extrai mensagens do projeto.", "etc/scripts/frontend/mensagens-extrair.js");
criarComandoScript(frontendMensagens, "analisar", "Analisa o JSON de mensagens extraidas.", "etc/scripts/frontend/mensagens-analisar.js");

const frontendValidacoes = frontend.command("validacoes").description("Auditorias de validacao do frontend.");
criarComandoScript(frontendValidacoes, "auditar", "Compara validacoes de frontend e backend.", "etc/scripts/frontend/validacoes-auditar.js");

const frontendViews = frontend.command("views").description("Auditorias especificas de views.");
criarComandoScript(frontendViews, "auditar-validacoes", "Audita links e validacoes nas views.", "etc/scripts/frontend/views-auditar-validacoes.js");

const frontendTestIds = frontend.command("test-ids").description("Ferramentas para atributos data-test.");
criarComandoScript(frontendTestIds, "listar", "Lista data-test do frontend.", "etc/scripts/frontend/test-ids-listar.js");
criarComandoScript(frontendTestIds, "listar-duplicados", "Lista data-test duplicados.", "etc/scripts/frontend/test-ids-duplicados.js");

const frontendTelas = frontend.command("telas").description("Ferramentas de captura e apoio visual.");
criarComandoScript(frontendTelas, "capturar", "Captura telas para documentacao ou apoio visual.", "etc/scripts/frontend/telas-capturar.js");

const codigo = program.command("codigo").description("Ferramentas de manutencao e higiene do código.");
const codigoSmells = codigo.command("smells").description("Auditorias de cheiros de codigo.");
criarComandoScript(codigoSmells, "auditar", "Gera snapshot de sinais de complexidade acidental e codigo defensivo.", "etc/scripts/codigo/smells-auditar.js");

const e2e = program.command("e2e").description("Ferramentas auxiliares de testes end-to-end.");
criarComandoScript(e2e, "limpar", "Aplica limpeza automatizada em especificacoes E2E.", "etc/scripts/e2e/limpar.js");

const qa = program.command("qa").description("Ferramentas de qualidade e dashboard.");
const qaSnapshot = qa.command("snapshot").description("Coleta e consolidacao de snapshots.");
qaSnapshot
    .command("coletar")
    .description("Coleta snapshot de QA.")
    .allowUnknownOption(true)
    .option("--perfil <perfil>", "Perfil de execucao (rapido, completo, backend, frontend).", "rapido")
    .action(async (opcoes, comando) => {
        const argsExtras = comando.args ?? [];
        const args = ["--perfil", opcoes.perfil, ...argsExtras];
        await executarSnapshotQa(args);
    });
qa
    .command("resumo")
    .description("Resume o snapshot mais recente do QA Dashboard.")
    .option("--arquivo <caminho>", "Usa um snapshot especifico em vez do latest.")
    .option("--json", "Emite saida estruturada em JSON.")
    .option("--max-hotspots <n>", "Limita a quantidade de hotspots exibidos.", Number.parseInt)
    .action(async (opcoes) => {
        await executarResumoQa(opcoes);
    });
const qaDashboard = qa.command("dashboard").description("Ferramentas operacionais do dashboard.");
criarComandoScript(qaDashboard, "servir", "Serve o dashboard de QA localmente.", "etc/scripts/qa/dashboard-servir.js");

const projeto = program.command("projeto").description("Ferramentas transversais do repositório.");
projeto
    .command("doctor")
    .description("Valida comandos e arquivos essenciais do ambiente.")
    .option("--json", "Emite saida estruturada em JSON.")
    .option("--base <diretorio>", "Sobrescreve o diretório base para diagnostico.")
    .action(async (opcoes) => {
        await executarDoctor(opcoes);
    });

projeto
    .command("limpar")
    .description("Lista ou remove artefatos transientes de QA e do toolkit.")
    .option("--json", "Emite saida estruturada em JSON.")
    .option("--confirmar", "Remove de fato os artefatos elegiveis.")
    .option("--base <diretorio>", "Sobrescreve o diretório base para limpeza.")
    .action(async (opcoes) => {
        await executarLimpeza(opcoes);
    });

projeto
    .command("qualidade [perfil]")
    .description("Executa os perfis consolidados de qualidade do projeto.")
    .action(async (perfil = "rapido") => {
        await executarPerfilQualidade(perfil);
    });

projeto
    .command("setup")
    .description("Prepara o ambiente do projeto com etapas opcionais de bootstrap.")
    .option("--instalar-dependencias", "Executa npm install na raiz, no frontend e no toolkit.")
    .option("--instalar-playwright", "Instala o Chromium do Playwright.")
    .option("--importar-certificados", "Executa a importacao de certificados Java locais.")
    .action(async (opcoes) => {
        await executarSetup(opcoes);
    });

criarComandoScript(projeto, "arvore-linhas", "Gera arvore agregada de linhas do repositório.", "etc/scripts/projeto/arvore-linhas.js");

program.addHelpText(
    "after",
    `\nExemplos:\n  ${pc.dim("node etc/scripts/sgc.js backend cobertura auditoria")}\n  ${pc.dim("node etc/scripts/sgc.js frontend cobertura auditoria")}\n  ${pc.dim("node etc/scripts/sgc.js qa snapshot coletar --perfil rapido")}\n  ${pc.dim("node etc/scripts/sgc.js qa resumo")}\n  ${pc.dim("node etc/scripts/sgc.js projeto doctor --json")}\n  ${pc.dim("node etc/scripts/sgc.js codigo smells auditar --json")}`
);

try {
    await program.parseAsync(process.argv);
} catch (error) {
    logger.error(pc.red(`Erro: ${error.message}`));
    process.exit(1);
}
