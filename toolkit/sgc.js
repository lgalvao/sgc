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
criarComandoScript(backendCobertura, "auditoria", "Auditoria unificada de cobertura e risco (Backend).", "toolkit/backend/cobertura-auditoria.js");
criarComandoScript(backendCobertura, "branches", "Lista classes com lacunas de branches no backend.", "toolkit/backend/cobertura-branches.js");
criarComandoScript(backendCobertura, "jornada", "Executa a jornada consolidada de cobertura do backend.", "toolkit/backend/cobertura-jornada.js");
criarComandoScript(backendCobertura, "cruzada", "Auditoria de cobertura cruzada e independente (Backend).", "toolkit/backend/cobertura-cruzada.js");

const backendArquitetura = backend.command("arquitetura").description("Auditorias de arquitetura do backend.");
criarComandoScript(backendArquitetura, "auditar", "Audita god objects (Services, Facades, Controllers) por linhas, metodos e dependencias.", "toolkit/backend/arquitetura-auditar.js");

const backendCoesao = backend.command("coesao").description("Auditorias de coesao do backend.");
criarComandoScript(backendCoesao, "auditar", "Audita Services com responsabilidades misturadas (consulta, mutacao, workflow, notificacao).", "toolkit/backend/coesao-auditar.js");

const backendContratos = backend.command("contratos").description("Auditorias de contratos HTTP e DTOs publicos do backend.");
criarComandoScript(backendContratos, "auditar", "Audita vazamentos de model.* em DTOs expostos por controllers.", "toolkit/backend/contratos-auditar.js");

const backendTestes = backend.command("testes").description("Ferramentas de testes do backend.");
criarComandoScript(backendTestes, "analisar", "Detecta classes sem testes e gera Markdown/JSON.", "toolkit/backend/testes-analisar.js");
criarComandoScript(backendTestes, "priorizar", "Prioriza backlog de testes do backend.", "toolkit/backend/testes-priorizar.js");
criarComandoScript(backendTestes, "gerar-stub", "Gera CoverageTest inicial para uma classe.", "toolkit/backend/testes-gerar-stub.js");

const backendJava = backend.command("java").description("Utilitarios Java do backend.");
criarComandoScript(backendJava, "corrigir-fqn", "Substitui FQNs por imports em arquivos Java.", "toolkit/backend/java-corrigir-fqn.js");
criarComandoScript(backendJava, "auditar-null", "Audita verificacoes de null no backend.", "toolkit/backend/java-auditar-null.js");
criarComandoScript(backendJava, "instalar-certificados", "Importa certificados locais no cacerts.", "toolkit/backend/java-instalar-certificados.js");
const backendNotificacoes = backend.command("notificacoes").description("Auditorias de notificacoes e assuntos do backend.");
criarComandoScript(backendNotificacoes, "auditar-assuntos", "Audita literais de assunto de notificacao fora de AssuntosNotificacao.", "toolkit/backend/notificacoes-assuntos-auditar.js");

const frontend = program.command("frontend").description("Ferramentas do frontend.");
const frontendCobertura = frontend.command("cobertura").description("Cobertura e diagnosticos do frontend.");
criarComandoScript(frontendCobertura, "auditoria", "Auditoria unificada de cobertura e risco (Frontend).", "toolkit/frontend/cobertura-auditoria.js");
criarComandoScript(frontendCobertura, "branches", "Lista arquivos com lacunas de branches no frontend.", "toolkit/frontend/cobertura-branches.js");
criarComandoScript(frontendCobertura, "branches-erros", "Cruza lacunas de branches com sinais de tratamento de erro suspeito no frontend.", "toolkit/frontend/cobertura-branches-erros.js");

const frontendMensagens = frontend.command("mensagens").description("Analise de mensagens e strings do frontend.");
criarComandoScript(frontendMensagens, "extrair", "Extrai mensagens do projeto.", "toolkit/frontend/mensagens-extrair.js");
criarComandoScript(frontendMensagens, "analisar", "Analisa o JSON de mensagens extraidas.", "toolkit/frontend/mensagens-analisar.js");

const frontendValidacoes = frontend.command("validacoes").description("Auditorias de validacao do frontend.");
criarComandoScript(frontendValidacoes, "auditar", "Compara validacoes de frontend e backend.", "toolkit/frontend/validacoes-auditar.js");

const frontendCruft = frontend.command("cruft").description("Auditorias de cruft e budgets do frontend.");
criarComandoScript(frontendCruft, "auditar", "Audita cruft estrutural do frontend.", "toolkit/frontend/cruft-auditar.js");
criarComandoScript(frontendCruft, "validar", "Valida budgets e waivers do cruft do frontend.", "toolkit/frontend/cruft-validar.js");

const frontendArquitetura = frontend.command("arquitetura").description("Auditorias de arquitetura e vazamento de contratos no frontend.");
criarComandoScript(frontendArquitetura, "auditar", "Audita vazamentos arquiteturais e estrategia de cache exposta no frontend.", "toolkit/frontend/arquitetura-auditar.js");
criarComandoScript(frontendArquitetura, "validar", "Valida regras arquiteturais do frontend (gate duro).", "toolkit/frontend/arquitetura-validar.js");

const frontendViews = frontend.command("views").description("Auditorias especificas de views.");
criarComandoScript(frontendViews, "validacoes-auditar", "Audita links e validacoes nas views.", "toolkit/frontend/views-validacoes-auditar.js");
criarComandoScript(frontendViews, "templates-validar", "Valida previsibilidade estrutural de templates das views.", "toolkit/frontend/views-templates-validar.js");
criarComandoScript(frontendViews, "auditar-validacoes", "Alias legado para 'validacoes-auditar'.", "toolkit/legado/frontend/views-auditar-validacoes.js");

const frontendModais = frontend.command("modais").description("Auditorias especificas de modais.");
criarComandoScript(frontendModais, "validar", "Valida o uso padronizado de ModalPadrao e proibe BModal cru fora do componente-base.", "toolkit/frontend/modais-validar.js");

const frontendTestIds = frontend.command("test-ids").description("Ferramentas para atributos data-test.");
criarComandoScript(frontendTestIds, "listar", "Lista data-test do frontend.", "toolkit/frontend/test-ids-listar.js");
criarComandoScript(frontendTestIds, "listar-duplicados", "Lista data-test duplicados.", "toolkit/frontend/test-ids-listar-duplicados.js");
criarComandoScript(frontendTestIds, "duplicados", "Alias legado para 'listar-duplicados'.", "toolkit/legado/frontend/test-ids-duplicados.js");

const frontendTelas = frontend.command("telas").description("Ferramentas de captura e apoio visual.");
criarComandoScript(frontendTelas, "capturar", "Captura telas para documentacao ou apoio visual.", "toolkit/frontend/telas-capturar.js");

const frontendA11y = frontend.command("a11y").description("Auditorias de acessibilidade do frontend.");
criarComandoScript(frontendA11y, "auditar", "Executa auditoria estatica de acessibilidade no frontend.", "toolkit/frontend/a11y-auditar.js");
criarComandoScript(frontendA11y, "crawler", "Executa o crawler Axe-core em todas as rotas principais.", "e2e/a11y/crawler.spec.ts");
criarComandoScript(frontendA11y, "processar", "Processa os resultados do crawler em um relatório Markdown.", "toolkit/frontend/a11y-processar-resultados.js");

const codigo = program.command("codigo").description("Ferramentas de manutencao e higiene do código.");
const codigoSmells = codigo.command("smells").description("Auditorias de cheiros de codigo.");
criarComandoScript(codigoSmells, "auditar", "Gera snapshot de sinais de complexidade acidental e codigo defensivo.", "toolkit/codigo/smells-auditar.js");
const codigoSemgrep = codigo.command("semgrep").description("Auditorias estruturais com Semgrep OSS.");
criarComandoScript(codigoSemgrep, "auditar", "Executa regras locais de Semgrep para backend, frontend e integração.", "toolkit/codigo/semgrep-auditar.js");
const codigoNomes = codigo.command("nomes").description("Inventario e auditoria de nomenclatura do projeto.");
criarComandoScript(codigoNomes, "coletar-simbolos", "Gera inventario de pacotes, arquivos, tipos e membros.", "toolkit/codigo/nomes-simbolos-coletar.js");
criarComandoScript(codigoNomes, "auditar-consistencia", "Audita padroes e divergencias de nomenclatura.", "toolkit/codigo/nomes-consistencia-auditar.js");
criarComandoScript(codigoNomes, "auditar-idioma", "Detecta nomes em inglês e campos com 'id' que deveriam usar 'codigo'.", "toolkit/codigo/idioma-consistencia-auditar.js");

const e2e = program.command("e2e").description("Ferramentas auxiliares de testes end-to-end.");
criarComandoScript(e2e, "limpar", "Aplica limpeza automatizada em especificacoes E2E.", "toolkit/e2e/limpar.js");

const integracao = program.command("integracao").description("Ferramentas de qualidade na fronteira backend/frontend.");
const integracaoContratos = integracao.command("contratos").description("Auditorias e artefatos de contrato HTTP.");
criarComandoScript(integracaoContratos, "exportar-openapi", "Exporta o OpenAPI atual da aplicação para arquivo local.", "toolkit/integracao/contratos-exportar-openapi.js");
criarComandoScript(integracaoContratos, "gerar-tipos", "Gera tipos TypeScript a partir do OpenAPI da aplicação.", "toolkit/integracao/contratos-gerar-tipos.js");
criarComandoScript(integracaoContratos, "diff", "Compara duas versões do OpenAPI e resume mudanças de contrato.", "toolkit/integracao/contratos-diff.js");
criarComandoScript(integracaoContratos, "fixar-baseline", "Promove o OpenAPI mais recente como baseline de comparação.", "toolkit/integracao/contratos-fixar-baseline.js");

const comunicacao = program.command("comunicacao").description("Ferramentas de auditoria de comunicacao, templates e notificacoes.");
criarComandoScript(comunicacao, "cobertura-notificacoes", "Audita a cobertura de testes de notificacoes e modelos de email.", "toolkit/auditar-cobertura-notificacoes.js");
criarComandoScript(comunicacao, "strings", "Audita consistência de mensagens de comunicação entre backend e frontend.", "toolkit/auditar-strings-comunicacao.js");
criarComandoScript(comunicacao, "templates-email", "Audita consistência de variáveis nos templates de email HTML do backend.", "toolkit/auditar-templates-email.js");

const requisitos = program.command("requisitos").description("Ferramentas de inventario e auditoria de requisitos.");
const requisitosCdus = requisitos.command("cdus").description("Inventario e auditoria read-only dos casos de uso.");
criarComandoScript(requisitosCdus, "inventariar", "Inventaria formatos e convenções implícitas dos `specs/cdu-*.md`.", "toolkit/requisitos/cdus-inventariar.js");
criarComandoScript(requisitosCdus, "auditar", "Audita a estrutura canônica mínima dos `specs/cdu-*.md`.", "toolkit/requisitos/cdus-auditar.js");
criarComandoScript(requisitosCdus, "auditar-estilo", "Audita convenções tipográficas de aspas simples, aspas duplas e crases nos `specs/cdu-*.md`.", "toolkit/requisitos/cdus-auditar-estilo.js");
criarComandoScript(requisitosCdus, "inventariar-vocabulario", "Inventaria perfis, situações, tipos de processo e elementos de UI recorrentes nos `specs/cdu-*.md`.", "toolkit/requisitos/cdus-inventariar-vocabulario.js");
criarComandoScript(requisitosCdus, "auditar-vocabulario", "Audita variações de vocabulário controlado nos `specs/cdu-*.md`.", "toolkit/requisitos/cdus-auditar-vocabulario.js");
criarComandoScript(requisitosCdus, "inventariar-mensagens", "Inventaria descrições, assuntos, mensagens e toasts recorrentes nos `specs/cdu-*.md`.", "toolkit/requisitos/cdus-inventariar-mensagens.js");
criarComandoScript(requisitosCdus, "auditar-mensagens", "Audita problemas mecânicos em descrições, assuntos, mensagens e toasts dos `specs/cdu-*.md`.", "toolkit/requisitos/cdus-auditar-mensagens.js");
criarComandoScript(requisitosCdus, "auditar-mensagens-codigo", "Compara descrições, mensagens e toasts dos `specs/cdu-*.md` com mensagens canônicas extraídas do código.", "toolkit/requisitos/cdus-auditar-mensagens-codigo.js");
criarComandoScript(requisitosCdus, "inventariar-densidade", "Inventaria densidade documental dos `specs/cdu-*.md` por palavras, passos e profundidade de listas.", "toolkit/requisitos/cdus-inventariar-densidade.js");
criarComandoScript(requisitosCdus, "inventariar-duplicacoes", "Inventaria blocos textuais duplicados nos `specs/cdu-*.md`.", "toolkit/requisitos/cdus-inventariar-duplicacoes.js");

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
criarComandoScript(qaDashboard, "servir", "Serve o dashboard de QA localmente.", "toolkit/qa/dashboard-servir.js");

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

criarComandoScript(projeto, "arvore-linhas", "Gera arvore agregada de linhas do repositório.", "toolkit/projeto/arvore-linhas.js");
criarComandoScript(projeto, "versao-sincronizar", "Sincroniza a versao entre gradle.properties e frontend/package.json.", "toolkit/projeto/versao-sincronizar.js");
criarComandoScript(projeto, "sincronizar-versao", "Alias legado para 'versao-sincronizar'.", "toolkit/legado/projeto/sincronizar-versao.js");

program.addHelpText(
    "after",
    `\nExemplos:\n  ${pc.dim("node toolkit/sgc.js backend cobertura auditoria")}\n  ${pc.dim("node toolkit/sgc.js frontend cobertura auditoria")}\n  ${pc.dim("node toolkit/sgc.js qa snapshot coletar --perfil rapido")}\n  ${pc.dim("node toolkit/sgc.js qa resumo")}\n  ${pc.dim("node toolkit/sgc.js projeto doctor --json")}\n  ${pc.dim("node toolkit/sgc.js codigo smells auditar --json")}`
);

try {
    await program.parseAsync(process.argv);
} catch (error) {
    logger.error(pc.red(`Erro: ${error.message}`));
    process.exit(1);
}
