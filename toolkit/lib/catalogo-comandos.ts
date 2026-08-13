import type {EsquemaArgumentos} from "./cli-opcoes.js";

type EscopoComando = "nucleo" | "adaptavel" | "perfil-sgc";
type EfeitoComando = "auditoria" | "geracao" | "mutacao" | "orquestracao";

function esquema(
    opcoesComValor: readonly string[] = [],
    opcoesBooleanas: readonly string[] = [],
    minimoPosicionais = 0,
    maximoPosicionais = minimoPosicionais
): EsquemaArgumentos {
    return {opcoesComValor, opcoesBooleanas, minimoPosicionais, maximoPosicionais};
}

type DefinicaoComando = Readonly<{
    caminho: readonly [string, ...string[]];
    descricao: string;
    escopo: EscopoComando;
    efeito: EfeitoComando;
    argumentos: EsquemaArgumentos;
}>;

type DefinicaoComandoArquivo = DefinicaoComando & Readonly<{
    arquivo: string;
}>;

const CATALOGO_COMANDOS = [
    {
        caminho: ["backend", "cobertura", "auditoria"],
        descricao: "Auditoria unificada de cobertura e risco (Backend).",
        escopo: "adaptavel",
        efeito: "auditoria",
        argumentos: esquema(["--base", "--arquivo", "--saida", "--minimo"], ["--json", "--gravar"]),
        arquivo: "backend/cobertura-auditoria.ts"
    },
    {
        caminho: ["backend", "cobertura", "ramificacoes"],
        descricao: "Lista classes com lacunas de ramificacoes no backend.",
        escopo: "adaptavel",
        efeito: "auditoria",
        argumentos: esquema(["--base", "--arquivo", "--limite", "--filtro"], ["--json"]),
        arquivo: "backend/cobertura-ramificacoes.ts"
    },
    {
        caminho: ["backend", "arquitetura", "auditar"],
        descricao: "Audita god objects (Services, Facades, Controllers) por linhas, metodos e dependencias.",
        escopo: "adaptavel",
        efeito: "auditoria",
        argumentos: esquema(["--base"], ["--json", "--gravar"]),
        arquivo: "backend/arquitetura-auditar.ts"
    },
    {
        caminho: ["backend", "coesao", "auditar"],
        descricao: "Audita Services com responsabilidades misturadas (consulta, mutacao, workflow, notificacao).",
        escopo: "perfil-sgc",
        efeito: "auditoria",
        argumentos: esquema(["--base"], ["--json", "--gravar"]),
        arquivo: "backend/coesao-auditar.ts"
    },
    {
        caminho: ["backend", "contratos", "auditar"],
        descricao: "Audita vazamentos de model.* em DTOs expostos por controllers.",
        escopo: "adaptavel",
        efeito: "auditoria",
        argumentos: esquema(["--base"], ["--json", "--gravar"]),
        arquivo: "backend/contratos-auditar.ts"
    },
    {
        caminho: ["backend", "testes", "analisar"],
        descricao: "Detecta classes sem testes e gera Markdown/JSON.",
        escopo: "adaptavel",
        efeito: "geracao",
        argumentos: esquema(["--base", "--diretorio", "--saida", "--saida-json", "--arquivo-jacoco"], ["--json", "--gravar"]),
        arquivo: "backend/testes-analisar.ts"
    },
    {
        caminho: ["backend", "testes", "priorizar"],
        descricao: "Prioriza backlog de testes do backend.",
        escopo: "adaptavel",
        efeito: "geracao",
        argumentos: esquema(["--entrada", "--saida"], ["--json", "--gravar"]),
        arquivo: "backend/testes-priorizar.ts"
    },
    {
        caminho: ["backend", "java", "corrigir-fqn"],
        descricao: "Substitui FQNs por imports em arquivos Java.",
        escopo: "adaptavel",
        efeito: "mutacao",
        argumentos: esquema(["--base"], ["--gravar"]),
        arquivo: "backend/java-corrigir-fqn.ts"
    },
    {
        caminho: ["backend", "notificacoes", "auditar-assuntos"],
        descricao: "Audita literais de assunto de notificacao fora de AssuntosNotificacao.",
        escopo: "perfil-sgc",
        efeito: "auditoria",
        argumentos: esquema(["--base"], ["--json"]),
        arquivo: "backend/notificacoes-assuntos-auditar.ts"
    },
    {
        caminho: ["frontend", "cobertura", "auditoria"],
        descricao: "Auditoria unificada de cobertura e risco (Frontend).",
        escopo: "adaptavel",
        efeito: "auditoria",
        argumentos: esquema(["--base", "--arquivo", "--saida", "--minimo"], ["--json", "--gravar"]),
        arquivo: "frontend/cobertura-auditoria.ts"
    },
    {
        caminho: ["frontend", "cobertura", "ramificacoes"],
        descricao: "Lista arquivos com lacunas de ramificacoes no frontend.",
        escopo: "adaptavel",
        efeito: "auditoria",
        argumentos: esquema(["--base", "--arquivo", "--limite"], ["--json"]),
        arquivo: "frontend/cobertura-ramificacoes.ts"
    },
    {
        caminho: ["frontend", "cobertura", "ramificacoes-erros"],
        descricao: "Cruza lacunas de ramificacoes com sinais de tratamento de erro suspeito no frontend.",
        escopo: "perfil-sgc",
        efeito: "auditoria",
        argumentos: esquema(["--base", "--arquivo", "--limite"], ["--json"]),
        arquivo: "frontend/cobertura-ramificacoes-erros.ts"
    },
    {
        caminho: ["frontend", "residuos", "auditar"],
        descricao: "Audita residuos estruturais do frontend.",
        escopo: "adaptavel",
        efeito: "auditoria",
        argumentos: esquema(["--base", "--orcamento", "--saida"], ["--json", "--gravar"]),
        arquivo: "frontend/residuos-auditar.ts"
    },
    {
        caminho: ["frontend", "residuos", "validar"],
        descricao: "Valida orcamentos e excecoes dos residuos do frontend.",
        escopo: "adaptavel",
        efeito: "auditoria",
        argumentos: esquema(["--base", "--orcamento", "--excecoes", "--saida"], ["--json", "--json-resumido", "--gravar"]),
        arquivo: "frontend/residuos-validar.ts"
    },
    {
        caminho: ["frontend", "arquitetura", "auditar"],
        descricao: "Audita vazamentos arquiteturais e estrategia de cache exposta no frontend.",
        escopo: "adaptavel",
        efeito: "auditoria",
        argumentos: esquema(["--base", "--saida"], ["--json", "--gravar"]),
        arquivo: "frontend/arquitetura-auditar.ts"
    },
    {
        caminho: ["frontend", "arquitetura", "validar"],
        descricao: "Valida regras arquiteturais do frontend (gate duro).",
        escopo: "adaptavel",
        efeito: "auditoria",
        argumentos: esquema(["--base"], ["--json"]),
        arquivo: "frontend/arquitetura-validar.ts"
    },
    {
        caminho: ["frontend", "views", "templates-validar"],
        descricao: "Valida previsibilidade estrutural de templates das views.",
        escopo: "perfil-sgc",
        efeito: "auditoria",
        argumentos: esquema(["--base"], ["--json"]),
        arquivo: "frontend/views-templates-validar.ts"
    },
    {
        caminho: ["frontend", "modais", "validar"],
        descricao: "Valida o uso padronizado de ModalPadrao e proibe BModal cru fora do componente-base.",
        escopo: "perfil-sgc",
        efeito: "auditoria",
        argumentos: esquema(["--base"], ["--json"]),
        arquivo: "frontend/modais-validar.ts"
    },
    {
        caminho: ["frontend", "identificadores-teste", "listar"],
        descricao: "Lista identificadores de teste do frontend.",
        escopo: "adaptavel",
        efeito: "auditoria",
        argumentos: esquema(["--base", "--diretorio"], ["--json"]),
        arquivo: "frontend/identificadores-teste-listar.ts"
    },
    {
        caminho: ["frontend", "identificadores-teste", "listar-duplicados"],
        descricao: "Lista identificadores de teste duplicados.",
        escopo: "adaptavel",
        efeito: "auditoria",
        argumentos: esquema(["--base", "--diretorio"], ["--json"]),
        arquivo: "frontend/identificadores-teste-listar-duplicados.ts"
    },
    {
        caminho: ["codigo", "cheiros", "auditar"],
        descricao: "Gera fotografia de sinais de complexidade acidental e codigo defensivo.",
        escopo: "adaptavel",
        efeito: "auditoria",
        argumentos: esquema(["--base"], ["--json", "--gravar"]),
        arquivo: "codigo/cheiros-auditar.ts"
    },
    {
        caminho: ["codigo", "semgrep", "auditar"],
        descricao: "Executa regras locais de Semgrep para backend, frontend e integração.",
        escopo: "adaptavel",
        efeito: "auditoria",
        argumentos: esquema(["--base", "--regra", "--diretorio"], ["--auto", "--json", "--gravar"]),
        arquivo: "codigo/semgrep-auditar.ts"
    },
    {
        caminho: ["codigo", "nomes", "coletar-simbolos"],
        descricao: "Gera inventario de pacotes, arquivos, tipos e membros.",
        escopo: "adaptavel",
        efeito: "geracao",
        argumentos: esquema(["--base", "--saida"], ["--json", "--gravar"]),
        arquivo: "codigo/nomes-simbolos-coletar.ts"
    },
    {
        caminho: ["codigo", "nomes", "auditar-consistencia"],
        descricao: "Audita padroes e divergencias de nomenclatura.",
        escopo: "adaptavel",
        efeito: "auditoria",
        argumentos: esquema(["--base", "--inventario", "--saida"], ["--json", "--gravar"]),
        arquivo: "codigo/nomes-consistencia-auditar.ts"
    },
    {
        caminho: ["codigo", "nomes", "auditar-idioma"],
        descricao: "Detecta nomes em inglês e campos com 'id' que deveriam usar 'codigo'.",
        escopo: "perfil-sgc",
        efeito: "auditoria",
        argumentos: esquema(["--base", "--inventario", "--saida"], ["--json", "--gravar"]),
        arquivo: "codigo/idioma-consistencia-auditar.ts"
    },
    {
        caminho: ["integracao", "contratos", "exportar-openapi"],
        descricao: "Exporta o OpenAPI atual da aplicação para arquivo local.",
        escopo: "adaptavel",
        efeito: "geracao",
        argumentos: esquema(["--base", "--url", "--saida"], ["--json"]),
        arquivo: "integracao/contratos-exportar-openapi.ts"
    },
    {
        caminho: ["integracao", "contratos", "diff"],
        descricao: "Compara duas versões do OpenAPI e resume mudanças de contrato.",
        escopo: "adaptavel",
        efeito: "auditoria",
        argumentos: esquema(["--base", "--anterior", "--atual"], ["--json", "--gravar"]),
        arquivo: "integracao/contratos-diff.ts"
    },
    {
        caminho: ["integracao", "contratos", "fixar-baseline"],
        descricao: "Promove o OpenAPI mais recente como baseline de comparação.",
        escopo: "adaptavel",
        efeito: "mutacao",
        argumentos: esquema(["--base", "--origem", "--destino"], ["--json"]),
        arquivo: "integracao/contratos-fixar-baseline.ts"
    },
    {
        caminho: ["requisitos", "cdus", "inventariar"],
        descricao: "Consolida formatos, vocabulário, mensagens, densidade e duplicações do corpus CDU.",
        escopo: "adaptavel",
        efeito: "geracao",
        argumentos: esquema(["--base", "--secoes"], ["--json"]),
        arquivo: "requisitos/cdus-inventariar.ts"
    },
    {
        caminho: ["requisitos", "cdus", "auditar"],
        descricao: "Audita estrutura, estilo, vocabulário, mensagens e referências do corpus CDU.",
        escopo: "adaptavel",
        efeito: "auditoria",
        argumentos: esquema(["--base", "--secoes"], ["--json"]),
        arquivo: "requisitos/cdus-auditar.ts"
    },
    {
        caminho: ["projeto", "arvore-linhas"],
        descricao: "Gera arvore agregada de linhas do repositório.",
        escopo: "nucleo",
        efeito: "geracao",
        argumentos: esquema(["--base", "--profundidade", "--minimo-linhas"], ["--excluir-testes"]),
        arquivo: "projeto/arvore-linhas.ts"
    },
    {
        caminho: ["projeto", "versao-sincronizar"],
        descricao: "Sincroniza a versao entre gradle.properties e frontend/package.json.",
        escopo: "adaptavel",
        efeito: "mutacao",
        argumentos: esquema(["--base"], ["--gravar"], 1),
        arquivo: "projeto/versao-sincronizar.ts"
    }
] as const satisfies readonly DefinicaoComandoArquivo[];

const CATALOGO_COMANDOS_ESPECIAIS = [
    {
        caminho: ["qualidade", "coletar"],
        descricao: "Coleta uma fotografia de qualidade do projeto.",
        escopo: "adaptavel",
        efeito: "orquestracao",
        argumentos: esquema(["--perfil", "--base"])
    },
    {
        caminho: ["qualidade", "tarefas", "executar"],
        descricao: "Executa tarefas de qualidade configuradas para o projeto.",
        escopo: "adaptavel",
        efeito: "orquestracao",
        argumentos: esquema(["--base"], [], 0, 1)
    },
    {
        caminho: ["qualidade", "resumo"],
        descricao: "Resume a fotografia de qualidade mais recente.",
        escopo: "nucleo",
        efeito: "auditoria",
        argumentos: esquema(["--arquivo", "--base", "--limite-pontos-criticos"], ["--json"])
    },
    {
        caminho: ["projeto", "dependencias", "auditar"],
        descricao: "Audita uso, atualizacao e vulnerabilidades de dependencias npm e Gradle.",
        escopo: "adaptavel",
        efeito: "orquestracao",
        argumentos: esquema(["--base"])
    },
    {
        caminho: ["projeto", "ambiente", "verificar"],
        descricao: "Verifica pre-requisitos, comandos e arquivos essenciais do projeto auditado.",
        escopo: "adaptavel",
        efeito: "auditoria",
        argumentos: esquema(["--base"], ["--json"])
    },
    {
        caminho: ["projeto", "artefatos", "limpar"],
        descricao: "Lista ou remove artefatos gerados pelo toolkit e pelas ferramentas de qualidade.",
        escopo: "adaptavel",
        efeito: "mutacao",
        argumentos: esquema(["--base"], ["--json", "--confirmar"])
    }
] as const satisfies readonly DefinicaoComando[];

const CATALOGO_COMANDOS_COMPLETO = [...CATALOGO_COMANDOS, ...CATALOGO_COMANDOS_ESPECIAIS] as const;

export {
    CATALOGO_COMANDOS,
    CATALOGO_COMANDOS_COMPLETO
};
