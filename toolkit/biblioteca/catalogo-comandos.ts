import type {EsquemaArgumentos} from "./cli-opcoes.js";

type EscopoComando = "nucleo" | "adaptavel" | "perfil-sgc";
type FinalidadeComando = "auditar" | "inventariar" | "gerar" | "transformar" | "orquestrar";
type DecisaoComando = "manter" | "manter-ocasional" | "manter-tendencia";
type PersistenciaComando = "nenhuma" | "opcional" | "intrinseca";

interface EfeitosComando {
    persistencia: PersistenciaComando;
    remocao: boolean;
    subprocessos: boolean;
    rede: boolean;
}

function criarEfeitosComando(
    persistencia: PersistenciaComando,
    ajustes: Partial<Omit<EfeitosComando, "persistencia">> = {}
): EfeitosComando {
    return {
        persistencia,
        remocao: false,
        subprocessos: false,
        rede: false,
        ...ajustes
    };
}

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
    decisao: DecisaoComando;
    finalidade: FinalidadeComando;
    efeitos: EfeitosComando;
    argumentos: EsquemaArgumentos;
}>;

type DefinicaoComandoArquivo = DefinicaoComando & Readonly<{
    arquivo: string;
}>;

type DefinicaoComandoCatalogada = DefinicaoComando | DefinicaoComandoArquivo;

const CATALOGO_COMANDOS = [
    {
        caminho: ["servidor", "cobertura", "auditoria"],
        descricao: "Auditoria unificada de cobertura e risco do servidor segundo as exclusoes do perfil SGC.",
        escopo: "perfil-sgc",
        decisao: "manter",
        finalidade: "auditar",
        efeitos: criarEfeitosComando("opcional"),
        argumentos: esquema(["--base", "--arquivo", "--saida", "--minimo"], ["--json", "--gravar"]),
        arquivo: "servidor/cobertura-auditoria.ts"
    },
    {
        caminho: ["servidor", "cobertura", "ramificacoes"],
        descricao: "Lista classes com lacunas de ramificacoes no servidor segundo as exclusoes do perfil SGC.",
        escopo: "perfil-sgc",
        decisao: "manter",
        finalidade: "auditar",
        efeitos: criarEfeitosComando("nenhuma"),
        argumentos: esquema(["--base", "--arquivo", "--limite", "--filtro"], ["--json"]),
        arquivo: "servidor/cobertura-ramificacoes.ts"
    },
    {
        caminho: ["servidor", "arquitetura", "auditar"],
        descricao: "Audita concentração de responsabilidades em Services, Facades e Controllers por linhas, métodos e dependências.",
        escopo: "adaptavel",
        decisao: "manter",
        finalidade: "auditar",
        efeitos: criarEfeitosComando("nenhuma"),
        argumentos: esquema(["--base"], ["--json", "--gravar"]),
        arquivo: "servidor/arquitetura-auditar.ts"
    },
    {
        caminho: ["servidor", "coesao", "auditar"],
        descricao: "Audita Services com responsabilidades misturadas (consulta, mutacao, workflow, notificacao).",
        escopo: "perfil-sgc",
        decisao: "manter",
        finalidade: "auditar",
        efeitos: criarEfeitosComando("nenhuma"),
        argumentos: esquema(["--base"], ["--json", "--gravar"]),
        arquivo: "servidor/coesao-auditar.ts"
    },
    {
        caminho: ["servidor", "contratos", "auditar"],
        descricao: "Audita vazamentos de model.* em DTOs expostos por controllers.",
        escopo: "adaptavel",
        decisao: "manter",
        finalidade: "auditar",
        efeitos: criarEfeitosComando("nenhuma"),
        argumentos: esquema(["--base"], ["--json", "--gravar"]),
        arquivo: "servidor/contratos-auditar.ts"
    },
    {
        caminho: ["servidor", "testes", "analisar"],
        descricao: "Analisa evidências de testes e cobertura das classes do servidor e gera Markdown/JSON.",
        escopo: "adaptavel",
        decisao: "manter",
        finalidade: "inventariar",
        efeitos: criarEfeitosComando("opcional"),
        argumentos: esquema(["--base", "--diretorio", "--saida", "--saida-json", "--arquivo-jacoco", "--politica"], ["--json", "--gravar"]),
        arquivo: "servidor/testes-analisar.ts"
    },
    {
        caminho: ["servidor", "testes", "priorizar"],
        descricao: "Prioriza backlog de testes do servidor.",
        escopo: "adaptavel",
        decisao: "manter",
        finalidade: "gerar",
        efeitos: criarEfeitosComando("opcional"),
        argumentos: esquema(["--entrada", "--saida"], ["--json", "--gravar"]),
        arquivo: "servidor/testes-priorizar.ts"
    },
    {
        caminho: ["servidor", "java", "corrigir-fqn"],
        descricao: "Substitui FQNs por imports em arquivos Java.",
        escopo: "adaptavel",
        decisao: "manter-ocasional",
        finalidade: "transformar",
        efeitos: criarEfeitosComando("opcional"),
        argumentos: esquema(["--base"], ["--gravar"]),
        arquivo: "servidor/java-corrigir-fqn.ts"
    },
    {
        caminho: ["servidor", "notificacoes", "auditar-assuntos"],
        descricao: "Audita literais de assunto de notificacao fora de AssuntosNotificacao.",
        escopo: "perfil-sgc",
        decisao: "manter",
        finalidade: "auditar",
        efeitos: criarEfeitosComando("nenhuma"),
        argumentos: esquema(["--base"], ["--json"]),
        arquivo: "servidor/notificacoes-assuntos-auditar.ts"
    },
    {
        caminho: ["cliente", "cobertura", "auditoria"],
        descricao: "Auditoria unificada de cobertura e risco (Cliente).",
        escopo: "adaptavel",
        decisao: "manter",
        finalidade: "auditar",
        efeitos: criarEfeitosComando("opcional"),
        argumentos: esquema(["--base", "--arquivo", "--saida", "--minimo"], ["--json", "--gravar"]),
        arquivo: "cliente/cobertura-auditoria.ts"
    },
    {
        caminho: ["cliente", "cobertura", "ramificacoes"],
        descricao: "Lista arquivos com lacunas de ramificacoes no cliente.",
        escopo: "adaptavel",
        decisao: "manter",
        finalidade: "auditar",
        efeitos: criarEfeitosComando("nenhuma"),
        argumentos: esquema(["--base", "--arquivo", "--limite"], ["--json"]),
        arquivo: "cliente/cobertura-ramificacoes.ts"
    },
    {
        caminho: ["cliente", "cobertura", "ramificacoes-erros"],
        descricao: "Cruza lacunas de ramificacoes com sinais de tratamento de erro suspeito no cliente.",
        escopo: "perfil-sgc",
        decisao: "manter",
        finalidade: "auditar",
        efeitos: criarEfeitosComando("nenhuma"),
        argumentos: esquema(["--base", "--arquivo", "--limite"], ["--json"]),
        arquivo: "cliente/cobertura-ramificacoes-erros.ts"
    },
    {
        caminho: ["cliente", "residuos", "auditar"],
        descricao: "Audita residuos estruturais do cliente.",
        escopo: "adaptavel",
        decisao: "manter",
        finalidade: "auditar",
        efeitos: criarEfeitosComando("opcional"),
        argumentos: esquema(["--base", "--orcamento", "--saida"], ["--json", "--gravar"]),
        arquivo: "cliente/residuos-auditar.ts"
    },
    {
        caminho: ["cliente", "residuos", "validar"],
        descricao: "Valida orcamentos e excecoes dos residuos do cliente.",
        escopo: "adaptavel",
        decisao: "manter",
        finalidade: "auditar",
        efeitos: criarEfeitosComando("opcional"),
        argumentos: esquema(["--base", "--orcamento", "--excecoes", "--saida"], ["--json", "--json-resumido", "--gravar"]),
        arquivo: "cliente/residuos-validar.ts"
    },
    {
        caminho: ["cliente", "arquitetura", "auditar"],
        descricao: "Audita vazamentos arquiteturais e estratégia de cache do cliente segundo a política do SGC.",
        escopo: "perfil-sgc",
        decisao: "manter",
        finalidade: "auditar",
        efeitos: criarEfeitosComando("opcional"),
        argumentos: esquema(["--base", "--saida"], ["--json", "--gravar"]),
        arquivo: "cliente/arquitetura-auditar.ts"
    },
    {
        caminho: ["cliente", "arquitetura", "validar"],
        descricao: "Valida regras arquiteturais do cliente (gate duro).",
        escopo: "perfil-sgc",
        decisao: "manter",
        finalidade: "auditar",
        efeitos: criarEfeitosComando("nenhuma"),
        argumentos: esquema(["--base"], ["--json"]),
        arquivo: "cliente/arquitetura-validar.ts"
    },
    {
        caminho: ["cliente", "views", "templates-validar"],
        descricao: "Valida previsibilidade estrutural de templates das views.",
        escopo: "perfil-sgc",
        decisao: "manter",
        finalidade: "auditar",
        efeitos: criarEfeitosComando("nenhuma"),
        argumentos: esquema(["--base"], ["--json"]),
        arquivo: "cliente/views-templates-validar.ts"
    },
    {
        caminho: ["cliente", "modais", "validar"],
        descricao: "Valida o uso padronizado de ModalPadrao e proibe BModal cru fora do componente-base.",
        escopo: "perfil-sgc",
        decisao: "manter",
        finalidade: "auditar",
        efeitos: criarEfeitosComando("nenhuma"),
        argumentos: esquema(["--base"], ["--json"]),
        arquivo: "cliente/modais-validar.ts"
    },
    {
        caminho: ["cliente", "identificadores-teste", "listar"],
        descricao: "Lista identificadores de teste do cliente.",
        escopo: "adaptavel",
        decisao: "manter-ocasional",
        finalidade: "auditar",
        efeitos: criarEfeitosComando("nenhuma"),
        argumentos: esquema(["--base", "--diretorio"], ["--json"]),
        arquivo: "cliente/identificadores-teste-listar.ts"
    },
    {
        caminho: ["cliente", "identificadores-teste", "listar-duplicados"],
        descricao: "Inventaria identificadores de teste repetidos, sem transformar repeticao textual em gate.",
        escopo: "adaptavel",
        decisao: "manter-ocasional",
        finalidade: "inventariar",
        efeitos: criarEfeitosComando("nenhuma"),
        argumentos: esquema(["--base", "--diretorio"], ["--json"]),
        arquivo: "cliente/identificadores-teste-listar-duplicados.ts"
    },
    {
        caminho: ["codigo", "cheiros", "auditar"],
        descricao: "Gera fotografia de tendências de complexidade e código defensivo para comparação entre execuções.",
        escopo: "adaptavel",
        decisao: "manter-tendencia",
        finalidade: "auditar",
        efeitos: criarEfeitosComando("opcional"),
        argumentos: esquema(["--base"], ["--json", "--gravar"]),
        arquivo: "codigo/cheiros-auditar.ts"
    },
    {
        caminho: ["codigo", "semgrep", "auditar"],
        descricao: "Executa regras locais de Semgrep para servidor, cliente e integração.",
        escopo: "adaptavel",
        decisao: "manter",
        finalidade: "auditar",
        efeitos: criarEfeitosComando("opcional", {subprocessos: true}),
        argumentos: esquema(["--base", "--regra", "--diretorio"], ["--auto", "--json", "--gravar"]),
        arquivo: "codigo/semgrep-auditar.ts"
    },
    {
        caminho: ["codigo", "nomes", "coletar-simbolos"],
        descricao: "Gera inventario de pacotes, arquivos, tipos e membros.",
        escopo: "adaptavel",
        decisao: "manter-ocasional",
        finalidade: "inventariar",
        efeitos: criarEfeitosComando("opcional"),
        argumentos: esquema(["--base", "--saida"], ["--json", "--gravar"]),
        arquivo: "codigo/nomes-simbolos-coletar.ts"
    },
    {
        caminho: ["codigo", "nomes", "auditar-consistencia"],
        descricao: "Audita padroes e divergencias de nomenclatura.",
        escopo: "adaptavel",
        decisao: "manter",
        finalidade: "auditar",
        efeitos: criarEfeitosComando("opcional"),
        argumentos: esquema(["--base", "--inventario", "--saida"], ["--json", "--gravar"]),
        arquivo: "codigo/nomes-consistencia-auditar.ts"
    },
    {
        caminho: ["codigo", "nomes", "auditar-idioma"],
        descricao: "Detecta nomes em inglês e campos com 'id' que deveriam usar 'codigo'.",
        escopo: "perfil-sgc",
        decisao: "manter",
        finalidade: "auditar",
        efeitos: criarEfeitosComando("opcional"),
        argumentos: esquema(["--base", "--inventario", "--saida"], ["--json", "--gravar"]),
        arquivo: "codigo/idioma-consistencia-auditar.ts"
    },
    {
        caminho: ["integracao", "contratos", "exportar-openapi"],
        descricao: "Exporta o OpenAPI atual da aplicação para arquivo local.",
        escopo: "adaptavel",
        decisao: "manter-ocasional",
        finalidade: "gerar",
        efeitos: criarEfeitosComando("intrinseca", {rede: true}),
        argumentos: esquema(["--base", "--url", "--saida"], ["--json"]),
        arquivo: "integracao/contratos-exportar-openapi.ts"
    },
    {
        caminho: ["integracao", "contratos", "diff"],
        descricao: "Compara duas versões do OpenAPI e resume mudanças de contrato.",
        escopo: "adaptavel",
        decisao: "manter-ocasional",
        finalidade: "auditar",
        efeitos: criarEfeitosComando("opcional", {subprocessos: true}),
        argumentos: esquema(["--base", "--anterior", "--atual"], ["--json", "--gravar"]),
        arquivo: "integracao/contratos-diff.ts"
    },
    {
        caminho: ["integracao", "contratos", "fixar-baseline"],
        descricao: "Promove o OpenAPI mais recente como baseline de comparação.",
        escopo: "adaptavel",
        decisao: "manter-ocasional",
        finalidade: "transformar",
        efeitos: criarEfeitosComando("intrinseca"),
        argumentos: esquema(["--base", "--origem", "--destino"], ["--json"]),
        arquivo: "integracao/contratos-fixar-baseline.ts"
    },
    {
        caminho: ["requisitos", "cdus", "inventariar"],
        descricao: "Consolida formatos, vocabulário, mensagens, densidade e duplicações do corpus CDU.",
        escopo: "adaptavel",
        decisao: "manter-ocasional",
        finalidade: "inventariar",
        efeitos: criarEfeitosComando("nenhuma"),
        argumentos: esquema(["--base", "--secoes"], ["--json"]),
        arquivo: "requisitos/cdus-inventariar.ts"
    },
    {
        caminho: ["requisitos", "cdus", "auditar"],
        descricao: "Audita estrutura, estilo, vocabulário, mensagens e referências do corpus CDU.",
        escopo: "adaptavel",
        decisao: "manter",
        finalidade: "auditar",
        efeitos: criarEfeitosComando("nenhuma"),
        argumentos: esquema(["--base", "--secoes"], ["--json"]),
        arquivo: "requisitos/cdus-auditar.ts"
    },
    {
        caminho: ["projeto", "arvore-linhas"],
        descricao: "Gera arvore agregada de linhas do repositório.",
        escopo: "nucleo",
        decisao: "manter-ocasional",
        finalidade: "inventariar",
        efeitos: criarEfeitosComando("nenhuma", {subprocessos: true}),
        argumentos: esquema(["--base", "--profundidade", "--minimo-linhas"], ["--excluir-testes"]),
        arquivo: "projeto/arvore-linhas.ts"
    },
    {
        caminho: ["projeto", "versao-sincronizar"],
        descricao: "Sincroniza a versao entre gradle.properties e o package.json do cliente configurado (frontend/package.json por padrão).",
        escopo: "adaptavel",
        decisao: "manter",
        finalidade: "transformar",
        efeitos: criarEfeitosComando("opcional"),
        argumentos: esquema(["--base"], ["--gravar"], 1),
        arquivo: "projeto/versao-sincronizar.ts"
    }
] as const satisfies readonly DefinicaoComandoArquivo[];

const CATALOGO_COMANDOS_ESPECIAIS = [
    {
        caminho: ["qualidade", "coletar"],
        descricao: "Coleta uma fotografia de qualidade usando os adaptadores e perfis do SGC.",
        escopo: "perfil-sgc",
        decisao: "manter",
        finalidade: "orquestrar",
        efeitos: criarEfeitosComando("intrinseca", {subprocessos: true}),
        argumentos: esquema(["--perfil", "--base"]),
        arquivo: "qualidade/coleta.ts"
    },
    {
        caminho: ["qualidade", "tarefas", "executar"],
        descricao: "Executa tarefas de qualidade configuradas para o projeto.",
        escopo: "adaptavel",
        decisao: "manter",
        finalidade: "orquestrar",
        efeitos: criarEfeitosComando("nenhuma", {subprocessos: true}),
        argumentos: esquema(["--base"], [], 0, 1)
    },
    {
        caminho: ["qualidade", "resumo"],
        descricao: "Resume a fotografia de qualidade mais recente.",
        escopo: "nucleo",
        decisao: "manter",
        finalidade: "auditar",
        efeitos: criarEfeitosComando("nenhuma"),
        argumentos: esquema(["--arquivo", "--base", "--limite-pontos-criticos"], ["--json"])
    },
    {
        caminho: ["projeto", "dependencias", "auditar"],
        descricao: "Audita uso, atualizacao e vulnerabilidades de dependencias npm e Gradle.",
        escopo: "adaptavel",
        decisao: "manter",
        finalidade: "orquestrar",
        efeitos: criarEfeitosComando("nenhuma", {subprocessos: true, rede: true}),
        argumentos: esquema(["--base"])
    },
    {
        caminho: ["projeto", "ambiente", "verificar"],
        descricao: "Verifica pré-requisitos, comandos e arquivos essenciais do workspace do SGC.",
        escopo: "perfil-sgc",
        decisao: "manter",
        finalidade: "auditar",
        efeitos: criarEfeitosComando("nenhuma", {subprocessos: true}),
        argumentos: esquema(["--base"], ["--json"])
    },
    {
        caminho: ["projeto", "artefatos", "limpar"],
        descricao: "Lista ou remove artefatos gerados pelo toolkit e pelas ferramentas de qualidade.",
        escopo: "adaptavel",
        decisao: "manter",
        finalidade: "transformar",
        efeitos: criarEfeitosComando("nenhuma", {remocao: true}),
        argumentos: esquema(["--base"], ["--json", "--confirmar"])
    }
] as const satisfies readonly (DefinicaoComando | DefinicaoComandoArquivo)[];

const CATALOGO_COMANDOS_COMPLETO: readonly DefinicaoComandoCatalogada[] = [
    ...CATALOGO_COMANDOS,
    ...CATALOGO_COMANDOS_ESPECIAIS
];

function obterDefinicaoComandoArquivo(arquivo: string): DefinicaoComandoArquivo | undefined {
    return CATALOGO_COMANDOS_COMPLETO.find(
        (item): item is DefinicaoComandoArquivo => "arquivo" in item && item.arquivo === arquivo
    );
}

export {
    CATALOGO_COMANDOS,
    CATALOGO_COMANDOS_COMPLETO,
    obterDefinicaoComandoArquivo
};
