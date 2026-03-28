#!/usr/bin/env node
import {Command} from "commander";
import pc from "picocolors";
import {executarNode} from "./lib/execucao.js";
import {executarDoctor} from "./projeto/doctor.js";
import {executarLimpeza} from "./projeto/limpar.js";
import {PERFIS, executarPerfilQualidade} from "./projeto/qualidade.js";
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
criarComandoScript(backendCobertura, "analisar", "Analise tabular da cobertura do backend.", "backend/etc/scripts/cobertura-analisar.cjs");
criarComandoScript(backendCobertura, "priorizar", "Ranking resumido de prioridades de cobertura.", "backend/etc/scripts/cobertura-priorizar.cjs");
criarComandoScript(backendCobertura, "complexidade", "Ranking de complexidade pelo CSV do JaCoCo.", "backend/etc/scripts/cobertura-complexidade.cjs");
criarComandoScript(backendCobertura, "lacunas", "Gera JSON estruturado com lacunas de cobertura.", "backend/etc/scripts/cobertura-lacunas.cjs");
criarComandoScript(backendCobertura, "plano", "Gera plano detalhado para 100% de cobertura.", "backend/etc/scripts/cobertura-plano.cjs");
criarComandoScript(backendCobertura, "verificar", "Consulta cobertura global e por classe.", "backend/etc/scripts/cobertura-verificar.cjs");
criarComandoScript(backendCobertura, "jornada", "Executa a jornada completa de cobertura.", "backend/etc/scripts/cobertura-jornada.cjs");

const backendTestes = backend.command("testes").description("Ferramentas de testes do backend.");
criarComandoScript(backendTestes, "analisar", "Detecta classes sem testes e gera Markdown/JSON.", "backend/etc/scripts/testes-analisar.cjs");
criarComandoScript(backendTestes, "priorizar", "Prioriza backlog de testes do backend.", "backend/etc/scripts/testes-priorizar.cjs");
criarComandoScript(backendTestes, "gerar-stub", "Gera CoverageTest inicial para uma classe.", "backend/etc/scripts/testes-gerar-stub.cjs");

const backendJava = backend.command("java").description("Utilitarios Java do backend.");
criarComandoScript(backendJava, "corrigir-fqn", "Substitui FQNs por imports em arquivos Java.", "backend/etc/scripts/java-corrigir-fqn.cjs");
criarComandoScript(backendJava, "auditar-null", "Audita verificacoes de null no backend.", "backend/etc/scripts/java-auditar-null.cjs");
criarComandoScript(backendJava, "instalar-certificados", "Importa certificados locais no cacerts.", "backend/etc/scripts/java-instalar-certificados.cjs");

const frontend = program.command("frontend").description("Ferramentas do frontend.");
const frontendCobertura = frontend.command("cobertura").description("Cobertura e diagnosticos do frontend.");
criarComandoScript(frontendCobertura, "verificar", "Lista arquivos abaixo do limiar de cobertura.", "frontend/etc/scripts/verificar-cobertura.cjs");
criarComandoScript(frontendCobertura, "impacto", "Prioriza arquivos por impacto potencial de cobertura.", "frontend/etc/scripts/analisar-impacto-cobertura.cjs");
criarComandoScript(frontendCobertura, "linhas-sem-cobertura", "Mostra linhas sem cobertura no frontend.", "frontend/etc/scripts/mostrar-linhas-sem-cobertura.cjs");

const frontendMensagens = frontend.command("mensagens").description("Analise de mensagens e strings do frontend.");
criarComandoScript(frontendMensagens, "extrair", "Extrai mensagens do projeto.", "frontend/etc/scripts/extrair-mensagens.cjs");
criarComandoScript(frontendMensagens, "analisar", "Analisa o JSON de mensagens extraidas.", "frontend/etc/scripts/analisar-mensagens.cjs");

const frontendValidacoes = frontend.command("validacoes").description("Auditorias de validacao do frontend.");
criarComandoScript(frontendValidacoes, "auditar", "Compara validacoes de frontend e backend.", "frontend/etc/scripts/audit-frontend-validations.cjs");

const frontendViews = frontend.command("views").description("Auditorias especificas de views.");
criarComandoScript(frontendViews, "auditar-validacoes", "Audita links e validacoes nas views.", "frontend/etc/scripts/audit-view-validations.cjs");

const frontendTestIds = frontend.command("test-ids").description("Ferramentas para atributos data-test.");
criarComandoScript(frontendTestIds, "listar", "Lista data-test do frontend.", "frontend/etc/scripts/listar-test-ids.cjs");
criarComandoScript(frontendTestIds, "listar-duplicados", "Lista data-test duplicados.", "frontend/etc/scripts/listar-test-ids-duplicados.cjs");

const frontendTelas = frontend.command("telas").description("Ferramentas de captura e apoio visual.");
criarComandoScript(frontendTelas, "capturar", "Captura telas para documentacao ou apoio visual.", "frontend/etc/scripts/capturar-telas.cjs");

const qa = program.command("qa").description("Ferramentas de qualidade e dashboard.");
const qaSnapshot = qa.command("snapshot").description("Coleta e consolidacao de snapshots.");
criarComandoScript(qaSnapshot, "coletar", "Coleta snapshot de QA.", "etc/qa-dashboard/scripts/coletar-snapshot.mjs");
qa
    .command("resumo")
    .description("Resume o snapshot mais recente do QA Dashboard.")
    .option("--arquivo <caminho>", "Usa um snapshot especifico em vez do latest.")
    .option("--json", "Emite saida estruturada em JSON.")
    .option("--max-hotspots <n>", "Limita a quantidade de hotspots exibidos.", Number.parseInt)
    .action(async (opcoes) => {
        await executarResumoQa(opcoes);
    });

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

program.addHelpText(
    "after",
    `\nExemplos:\n  ${pc.dim("node etc/scripts/sgc.js backend cobertura verificar --min=95")}\n  ${pc.dim("node etc/scripts/sgc.js frontend mensagens analisar")}\n  ${pc.dim("node etc/scripts/sgc.js qa snapshot coletar --perfil rapido")}\n  ${pc.dim("node etc/scripts/sgc.js qa resumo")}\n  ${pc.dim("node etc/scripts/sgc.js projeto doctor --json")}\n  ${pc.dim(`node etc/scripts/sgc.js projeto qualidade rapido  # perfis: ${Object.keys(PERFIS).join(", ")}`)}`
);

try {
    await program.parseAsync(process.argv);
} catch (error) {
    console.error(pc.red(`Erro: ${error.message}`));
    process.exit(1);
}
