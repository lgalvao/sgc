#!/usr/bin/env node
import {Command} from "commander";
import pc from "picocolors";
import {executarNode} from "./lib/execucao.js";
import logger from "./lib/logger.js";

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
criarComandoScript(backendCobertura, "branches", "Lista classes com lacunas de branches no backend.", "etc/scripts/backend/cobertura-branches.js");
criarComandoScript(backendCobertura, "jornada", "Executa a jornada consolidada de cobertura do backend.", "etc/scripts/backend/cobertura-jornada.js");
criarComandoScript(backendCobertura, "cruzada", "Auditoria de cobertura cruzada e independente (Backend).", "etc/scripts/backend/cobertura-cruzada.js");

const backendArquitetura = backend.command("arquitetura").description("Auditorias de arquitetura do backend.");
criarComandoScript(backendArquitetura, "auditar", "Audita god objects (Services, Facades, Controllers) por linhas, metodos e dependencias.", "etc/scripts/backend/arquitetura-auditar.js");

const backendCoesao = backend.command("coesao").description("Auditorias de coesao do backend.");
criarComandoScript(backendCoesao, "auditar", "Audita Services com responsabilidades misturadas (consulta, mutacao, workflow, notificacao).", "etc/scripts/backend/coesao-auditar.js");

const backendContratos = backend.command("contratos").description("Auditorias de contratos HTTP e DTOs publicos do backend.");
criarComandoScript(backendContratos, "auditar", "Audita vazamentos de model.* em DTOs expostos por controllers.", "etc/scripts/backend/contratos-auditar.js");

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
criarComandoScript(frontendCobertura, "branches", "Lista arquivos com lacunas de branches no frontend.", "etc/scripts/frontend/cobertura-branches.js");
criarComandoScript(frontendCobertura, "branches-erros", "Cruza lacunas de branches com sinais de tratamento de erro suspeito no frontend.", "etc/scripts/frontend/cobertura-branches-erros.js");

const frontendMensagens = frontend.command("mensagens").description("Analise de mensagens e strings do frontend.");
criarComandoScript(frontendMensagens, "extrair", "Extrai mensagens do projeto.", "etc/scripts/frontend/mensagens-extrair.js");
criarComandoScript(frontendMensagens, "analisar", "Analisa o JSON de mensagens extraidas.", "etc/scripts/frontend/mensagens-analisar.js");

const frontendValidacoes = frontend.command("validacoes").description("Auditorias de validacao do frontend.");
criarComandoScript(frontendValidacoes, "auditar", "Compara validacoes de frontend e backend.", "etc/scripts/frontend/validacoes-auditar.js");

const frontendCruft = frontend.command("cruft").description("Auditorias de cruft e budgets do frontend.");
criarComandoScript(frontendCruft, "auditar", "Audita cruft estrutural do frontend.", "etc/scripts/frontend/cruft-auditar.js");
criarComandoScript(frontendCruft, "validar", "Valida budgets e waivers do cruft do frontend.", "etc/scripts/frontend/cruft-validar.js");

const frontendArquitetura = frontend.command("arquitetura").description("Auditorias de arquitetura e vazamento de contratos no frontend.");
criarComandoScript(frontendArquitetura, "auditar", "Audita vazamentos arquiteturais e estrategia de cache exposta no frontend.", "etc/scripts/frontend/arquitetura-auditar.js");
criarComandoScript(frontendArquitetura, "validar", "Valida regras arquiteturais do frontend (gate duro).", "etc/scripts/frontend/arquitetura-validar.js");

const frontendViews = frontend.command("views").description("Auditorias especificas de views.");
criarComandoScript(frontendViews, "validacoes-auditar", "Audita links e validacoes nas views.", "etc/scripts/frontend/views-validacoes-auditar.js");
criarComandoScript(frontendViews, "auditar-validacoes", "Alias legado para 'validacoes-auditar'.", "etc/scripts/legado/frontend/views-auditar-validacoes.js");

const frontendTestIds = frontend.command("test-ids").description("Ferramentas para atributos data-test.");
criarComandoScript(frontendTestIds, "listar", "Lista data-test do frontend.", "etc/scripts/frontend/test-ids-listar.js");
criarComandoScript(frontendTestIds, "listar-duplicados", "Lista data-test duplicados.", "etc/scripts/frontend/test-ids-listar-duplicados.js");
criarComandoScript(frontendTestIds, "duplicados", "Alias legado para 'listar-duplicados'.", "etc/scripts/legado/frontend/test-ids-duplicados.js");

const frontendTelas = frontend.command("telas").description("Ferramentas de captura e apoio visual.");
criarComandoScript(frontendTelas, "capturar", "Captura telas para documentacao ou apoio visual.", "etc/scripts/frontend/telas-capturar.js");

const frontendA11y = frontend.command("a11y").description("Auditorias de acessibilidade do frontend.");
criarComandoScript(frontendA11y, "auditar", "Executa auditoria estatica de acessibilidade no frontend.", "etc/scripts/frontend/a11y-auditar.js");
criarComandoScript(frontendA11y, "crawler", "Executa o crawler Axe-core em todas as rotas principais.", "e2e/a11y/crawler.spec.ts");
criarComandoScript(frontendA11y, "processar", "Processa os resultados do crawler em um relatório Markdown.", "etc/scripts/frontend/a11y-processar-resultados.js");

const codigo = program.command("codigo").description("Ferramentas de manutencao e higiene do código.");
const codigoSmells = codigo.command("smells").description("Auditorias de cheiros de codigo.");
criarComandoScript(codigoSmells, "auditar", "Gera snapshot de sinais de complexidade acidental e codigo defensivo.", "etc/scripts/codigo/smells-auditar.js");
const codigoSemgrep = codigo.command("semgrep").description("Auditorias estruturais com Semgrep OSS.");
criarComandoScript(codigoSemgrep, "auditar", "Executa regras locais de Semgrep para backend, frontend e integração.", "etc/scripts/codigo/semgrep-auditar.js");
const codigoNomes = codigo.command("nomes").description("Inventario e auditoria de nomenclatura do projeto.");
criarComandoScript(codigoNomes, "coletar-simbolos", "Gera inventario de pacotes, arquivos, tipos e membros.", "etc/scripts/codigo/nomes-simbolos-coletar.js");
criarComandoScript(codigoNomes, "auditar-consistencia", "Audita padroes e divergencias de nomenclatura.", "etc/scripts/codigo/nomes-consistencia-auditar.js");
criarComandoScript(codigoNomes, "auditar-idioma", "Detecta nomes em inglês e campos com 'id' que deveriam usar 'codigo'.", "etc/scripts/codigo/idioma-consistencia-auditar.js");

const e2e = program.command("e2e").description("Ferramentas auxiliares de testes end-to-end.");
criarComandoScript(e2e, "limpar", "Aplica limpeza automatizada em especificacoes E2E.", "etc/scripts/e2e/limpar.js");

const integracao = program.command("integracao").description("Ferramentas de qualidade na fronteira backend/frontend.");
const integracaoContratos = integracao.command("contratos").description("Auditorias e artefatos de contrato HTTP.");
criarComandoScript(integracaoContratos, "exportar-openapi", "Exporta o OpenAPI atual da aplicação para arquivo local.", "etc/scripts/integracao/contratos-exportar-openapi.js");
criarComandoScript(integracaoContratos, "gerar-tipos", "Gera tipos TypeScript a partir do OpenAPI da aplicação.", "etc/scripts/integracao/contratos-gerar-tipos.js");
criarComandoScript(integracaoContratos, "diff", "Compara duas versões do OpenAPI e resume mudanças de contrato.", "etc/scripts/integracao/contratos-diff.js");
criarComandoScript(integracaoContratos, "fixar-baseline", "Promove o OpenAPI mais recente como baseline de comparação.", "etc/scripts/integracao/contratos-fixar-baseline.js");

const comunicacao = program.command("comunicacao").description("Ferramentas de auditoria de comunicacao, templates e notificacoes.");
criarComandoScript(comunicacao, "cobertura-notificacoes", "Audita a cobertura de testes de notificacoes e modelos de email.", "etc/scripts/auditar-cobertura-notificacoes.js");
criarComandoScript(comunicacao, "strings", "Audita consistência de mensagens de comunicação entre backend e frontend.", "etc/scripts/auditar-strings-comunicacao.js");
criarComandoScript(comunicacao, "templates-email", "Audita consistência de variáveis nos templates de email HTML do backend.", "etc/scripts/auditar-templates-email.js");

const qa = program.command("qa").description("Ferramentas de qualidade e dashboard.");
const qaSnapshot = qa.command("snapshot").description("Coleta e consolidacao de snapshots.");
qaSnapshot
    .command("coletar")
    .description("Coleta snapshot de QA.")
    .allowUnknownOption(true)
    .option("--perfil <perfil>", "Perfil de execucao (rapido, completo, backend, frontend).", "rapido")
    .action(async (opcoes, comando) => {
        const {executarSnapshotQa} = await import("./qa/snapshot-coletar.js");
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
        const {executarResumoQa} = await import("./qa/resumo.js");
        await executarResumoQa(opcoes);
    });
const qaDashboard = qa.command("dashboard").description("Ferramentas operacionais do dashboard.");
criarComandoScript(qaDashboard, "servir", "Serve o dashboard de QA localmente.", "etc/scripts/qa/dashboard-servir.js");

const projeto = program.command("projeto").description("Ferramentas transversais do repositório.");
projeto
    .command("dependencias")
    .description("Ferramentas para auditar uso e declaracao de dependencias.")
    .command("auditar")
    .description("Executa o knip na raiz, no frontend e no toolkit.")
    .action(async () => {
        const {executarAuditoriaDependencias} = await import("./projeto/dependencias-auditar.js");
        await executarAuditoriaDependencias();
    });

projeto
    .command("doctor")
    .description("Valida comandos e arquivos essenciais do ambiente.")
    .option("--json", "Emite saida estruturada em JSON.")
    .option("--base <diretorio>", "Sobrescreve o diretório base para diagnostico.")
    .action(async (opcoes) => {
        const {executarDoctor} = await import("./projeto/doctor.js");
        await executarDoctor(opcoes);
    });

projeto
    .command("limpar")
    .description("Lista ou remove artefatos transientes de QA e do toolkit.")
    .option("--json", "Emite saida estruturada em JSON.")
    .option("--confirmar", "Remove de fato os artefatos elegiveis.")
    .option("--base <diretorio>", "Sobrescreve o diretório base para limpeza.")
    .action(async (opcoes) => {
        const {executarLimpeza} = await import("./projeto/limpar.js");
        await executarLimpeza(opcoes);
    });

projeto
    .command("qualidade [perfil]")
    .description("Executa os perfis consolidados de qualidade do projeto.")
    .action(async (perfil = "rapido") => {
        const {executarPerfilQualidade} = await import("./projeto/qualidade.js");
        await executarPerfilQualidade(perfil);
    });

projeto
    .command("setup")
    .description("Prepara o ambiente do projeto com etapas opcionais de bootstrap.")
    .option("--instalar-dependencias", "Executa npm install na raiz, no frontend e no toolkit.")
    .option("--instalar-playwright", "Instala o Chromium do Playwright.")
    .option("--importar-certificados", "Executa a importacao de certificados Java locais.")
    .action(async (opcoes) => {
        const {executarSetup} = await import("./projeto/setup.js");
        await executarSetup(opcoes);
    });

criarComandoScript(projeto, "arvore-linhas", "Gera arvore agregada de linhas do repositório.", "etc/scripts/projeto/arvore-linhas.js");
criarComandoScript(projeto, "versao-sincronizar", "Sincroniza a versao entre gradle.properties e frontend/package.json.", "etc/scripts/projeto/versao-sincronizar.js");
criarComandoScript(projeto, "sincronizar-versao", "Alias legado para 'versao-sincronizar'.", "etc/scripts/legado/projeto/sincronizar-versao.js");

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