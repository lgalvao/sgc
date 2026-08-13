#!/usr/bin/env node
import {pathToFileURL} from "node:url";
import {Command} from "commander";
import pc from "picocolors";
import {executarNode} from "./lib/execucao.js";
import logger from "./lib/logger.js";

function criarComandoScript(pai: Command, nome: string, descricao: string, relativo: string): void {
    pai
        .command(nome)
        .description(descricao)
        .allowUnknownOption(true)
        .allowExcessArguments(true)
        .action(async (...valores: unknown[]) => {
            const comando = valores.at(-1) as Command;
            const argumentos = comando.args ?? [];
            await executarNode(relativo, argumentos);
        });
}

const program = new Command();
program
    .name("sgc")
    .description("Toolkit do SGC para backend, frontend, qualidade e automacoes de projeto.")
    .showHelpAfterError()
    .showSuggestionAfterError();

const backend = program.command("backend").description("Ferramentas do backend.");
const backendCobertura = backend.command("cobertura").description("Cobertura e diagnosticos do backend.");
criarComandoScript(backendCobertura, "auditoria", "Auditoria unificada de cobertura e risco (Backend).", "toolkit/backend/cobertura-auditoria.js");
criarComandoScript(backendCobertura, "ramificacoes", "Lista classes com lacunas de ramificacoes no backend.", "toolkit/backend/cobertura-ramificacoes.js");

const backendArquitetura = backend.command("arquitetura").description("Auditorias de arquitetura do backend.");
criarComandoScript(backendArquitetura, "auditar", "Audita god objects (Services, Facades, Controllers) por linhas, metodos e dependencias.", "toolkit/backend/arquitetura-auditar.js");

const backendCoesao = backend.command("coesao").description("Auditorias de coesao do backend.");
criarComandoScript(backendCoesao, "auditar", "Audita Services com responsabilidades misturadas (consulta, mutacao, workflow, notificacao).", "toolkit/backend/coesao-auditar.js");

const backendContratos = backend.command("contratos").description("Auditorias de contratos HTTP e DTOs publicos do backend.");
criarComandoScript(backendContratos, "auditar", "Audita vazamentos de model.* em DTOs expostos por controllers.", "toolkit/backend/contratos-auditar.js");

const backendTestes = backend.command("testes").description("Ferramentas de testes do backend.");
criarComandoScript(backendTestes, "analisar", "Detecta classes sem testes e gera Markdown/JSON.", "toolkit/backend/testes-analisar.js");
criarComandoScript(backendTestes, "priorizar", "Prioriza backlog de testes do backend.", "toolkit/backend/testes-priorizar.js");

const backendJava = backend.command("java").description("Utilitarios Java do backend.");
criarComandoScript(backendJava, "corrigir-fqn", "Substitui FQNs por imports em arquivos Java.", "toolkit/backend/java-corrigir-fqn.js");
const backendNotificacoes = backend.command("notificacoes").description("Auditorias de notificacoes e assuntos do backend.");
criarComandoScript(backendNotificacoes, "auditar-assuntos", "Audita literais de assunto de notificacao fora de AssuntosNotificacao.", "toolkit/backend/notificacoes-assuntos-auditar.js");

const frontend = program.command("frontend").description("Ferramentas do frontend.");
const frontendCobertura = frontend.command("cobertura").description("Cobertura e diagnosticos do frontend.");
criarComandoScript(frontendCobertura, "auditoria", "Auditoria unificada de cobertura e risco (Frontend).", "toolkit/frontend/cobertura-auditoria.js");
criarComandoScript(frontendCobertura, "ramificacoes", "Lista arquivos com lacunas de ramificacoes no frontend.", "toolkit/frontend/cobertura-ramificacoes.js");
criarComandoScript(frontendCobertura, "ramificacoes-erros", "Cruza lacunas de ramificacoes com sinais de tratamento de erro suspeito no frontend.", "toolkit/frontend/cobertura-ramificacoes-erros.js");

const frontendResiduos = frontend.command("residuos").description("Auditorias de residuos estruturais e orcamentos do frontend.");
criarComandoScript(frontendResiduos, "auditar", "Audita residuos estruturais do frontend.", "toolkit/frontend/residuos-auditar.js");
criarComandoScript(frontendResiduos, "validar", "Valida orcamentos e excecoes dos residuos do frontend.", "toolkit/frontend/residuos-validar.js");

const frontendArquitetura = frontend.command("arquitetura").description("Auditorias de arquitetura e vazamento de contratos no frontend.");
criarComandoScript(frontendArquitetura, "auditar", "Audita vazamentos arquiteturais e estrategia de cache exposta no frontend.", "toolkit/frontend/arquitetura-auditar.js");
criarComandoScript(frontendArquitetura, "validar", "Valida regras arquiteturais do frontend (gate duro).", "toolkit/frontend/arquitetura-validar.js");

const frontendViews = frontend.command("views").description("Auditorias especificas de views.");
criarComandoScript(frontendViews, "templates-validar", "Valida previsibilidade estrutural de templates das views.", "toolkit/frontend/views-templates-validar.js");

const frontendModais = frontend.command("modais").description("Auditorias especificas de modais.");
criarComandoScript(frontendModais, "validar", "Valida o uso padronizado de ModalPadrao e proibe BModal cru fora do componente-base.", "toolkit/frontend/modais-validar.js");

const frontendIdentificadoresTeste = frontend.command("identificadores-teste").description("Ferramentas para identificadores de teste.");
criarComandoScript(frontendIdentificadoresTeste, "listar", "Lista identificadores de teste do frontend.", "toolkit/frontend/identificadores-teste-listar.js");
criarComandoScript(frontendIdentificadoresTeste, "listar-duplicados", "Lista identificadores de teste duplicados.", "toolkit/frontend/identificadores-teste-listar-duplicados.js");

const frontendAcessibilidade = frontend.command("acessibilidade").description("Auditorias de acessibilidade do frontend.");
criarComandoScript(frontendAcessibilidade, "crawler", "Executa o crawler Axe-core em todas as rotas principais.", "toolkit/frontend/acessibilidade-crawler.js");
criarComandoScript(frontendAcessibilidade, "processar", "Processa os resultados do crawler em um relatorio Markdown.", "toolkit/frontend/acessibilidade-processar-resultados.js");

const codigo = program.command("codigo").description("Ferramentas de manutencao e higiene do código.");
const codigoCheiros = codigo.command("cheiros").description("Auditorias de cheiros de codigo.");
criarComandoScript(codigoCheiros, "auditar", "Gera fotografia de sinais de complexidade acidental e codigo defensivo.", "toolkit/codigo/cheiros-auditar.js");
const codigoSemgrep = codigo.command("semgrep").description("Auditorias estruturais com Semgrep OSS.");
criarComandoScript(codigoSemgrep, "auditar", "Executa regras locais de Semgrep para backend, frontend e integração.", "toolkit/codigo/semgrep-auditar.js");
const codigoNomes = codigo.command("nomes").description("Inventario e auditoria de nomenclatura do projeto.");
criarComandoScript(codigoNomes, "coletar-simbolos", "Gera inventario de pacotes, arquivos, tipos e membros.", "toolkit/codigo/nomes-simbolos-coletar.js");
criarComandoScript(codigoNomes, "auditar-consistencia", "Audita padroes e divergencias de nomenclatura.", "toolkit/codigo/nomes-consistencia-auditar.js");
criarComandoScript(codigoNomes, "auditar-idioma", "Detecta nomes em inglês e campos com 'id' que deveriam usar 'codigo'.", "toolkit/codigo/idioma-consistencia-auditar.js");

const integracao = program.command("integracao").description("Ferramentas de qualidade na fronteira backend/frontend.");
const integracaoContratos = integracao.command("contratos").description("Auditorias e artefatos de contrato HTTP.");
criarComandoScript(integracaoContratos, "exportar-openapi", "Exporta o OpenAPI atual da aplicação para arquivo local.", "toolkit/integracao/contratos-exportar-openapi.js");
criarComandoScript(integracaoContratos, "diff", "Compara duas versões do OpenAPI e resume mudanças de contrato.", "toolkit/integracao/contratos-diff.js");
criarComandoScript(integracaoContratos, "fixar-baseline", "Promove o OpenAPI mais recente como baseline de comparação.", "toolkit/integracao/contratos-fixar-baseline.js");

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

const qualidade = program.command("qualidade").description("Ferramentas de coleta e analise da qualidade.");
qualidade
    .command("coletar")
    .description("Coleta uma fotografia de qualidade do projeto.")
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
    .description("Resume a fotografia de qualidade mais recente.")
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
    .description("Executa o knip na raiz, no frontend e no toolkit.")
    .action(async () => {
        const {executarAuditoriaDependencias} = await import("./projeto/dependencias-auditar.js");
        await executarAuditoriaDependencias();
    });

projeto
    .command("diagnostico")
    .description("Valida comandos e arquivos essenciais do ambiente.")
    .option("--json", "Emite saida estruturada em JSON.")
    .option("--base <diretorio>", "Sobrescreve o diretório base para diagnostico.")
    .action(async (opcoes) => {
        const {executarDiagnostico} = await import("./projeto/diagnostico.js");
        const resultado = await executarDiagnostico(opcoes);
        if (resultado.statusGeral === "falha") {
            process.exitCode = 1;
        }
    });

projeto
    .command("limpar")
    .description("Lista ou remove artefatos transientes de qualidade e do toolkit.")
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
    .command("preparar")
    .description("Prepara o ambiente do projeto com etapas opcionais.")
    .option("--instalar-dependencias", "Executa npm install na raiz, no frontend e no toolkit.")
    .option("--instalar-playwright", "Instala o Chromium do Playwright.")
    .action(async (opcoes) => {
        const {executarPreparacao} = await import("./projeto/preparar.js");
        await executarPreparacao(opcoes);
    });

criarComandoScript(projeto, "arvore-linhas", "Gera arvore agregada de linhas do repositório.", "toolkit/projeto/arvore-linhas.js");
criarComandoScript(projeto, "versao-sincronizar", "Sincroniza a versao entre gradle.properties e frontend/package.json.", "toolkit/projeto/versao-sincronizar.js");

program.addHelpText(
    "after",
    `\nExemplos:\n  ${pc.dim("npx tsx toolkit/sgc.ts backend cobertura auditoria")}\n  ${pc.dim("npx tsx toolkit/sgc.ts frontend cobertura auditoria")}\n  ${pc.dim("npx tsx toolkit/sgc.ts qualidade coletar --perfil rapido")}\n  ${pc.dim("npx tsx toolkit/sgc.ts qualidade resumo")}\n  ${pc.dim("npx tsx toolkit/sgc.ts projeto diagnostico --json")}\n  ${pc.dim("npx tsx toolkit/sgc.ts codigo cheiros auditar --json")}`
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
    criarComandoScript,
    executar,
    principal,
    program
};
