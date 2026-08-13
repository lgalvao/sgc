type EscopoComando = "nucleo" | "adaptavel" | "perfil-sgc";
type EfeitoComando = "auditoria" | "geracao" | "mutacao" | "orquestracao";

type DefinicaoComando = Readonly<{
    caminho: readonly [string, ...string[]];
    descricao: string;
    escopo: EscopoComando;
    efeito: EfeitoComando;
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
        arquivo: "backend/cobertura-auditoria.ts"
    },
    {
        caminho: ["backend", "cobertura", "ramificacoes"],
        descricao: "Lista classes com lacunas de ramificacoes no backend.",
        escopo: "adaptavel",
        efeito: "auditoria",
        arquivo: "backend/cobertura-ramificacoes.ts"
    },
    {
        caminho: ["backend", "arquitetura", "auditar"],
        descricao: "Audita god objects (Services, Facades, Controllers) por linhas, metodos e dependencias.",
        escopo: "adaptavel",
        efeito: "auditoria",
        arquivo: "backend/arquitetura-auditar.ts"
    },
    {
        caminho: ["backend", "coesao", "auditar"],
        descricao: "Audita Services com responsabilidades misturadas (consulta, mutacao, workflow, notificacao).",
        escopo: "perfil-sgc",
        efeito: "auditoria",
        arquivo: "backend/coesao-auditar.ts"
    },
    {
        caminho: ["backend", "contratos", "auditar"],
        descricao: "Audita vazamentos de model.* em DTOs expostos por controllers.",
        escopo: "adaptavel",
        efeito: "auditoria",
        arquivo: "backend/contratos-auditar.ts"
    },
    {
        caminho: ["backend", "testes", "analisar"],
        descricao: "Detecta classes sem testes e gera Markdown/JSON.",
        escopo: "adaptavel",
        efeito: "geracao",
        arquivo: "backend/testes-analisar.ts"
    },
    {
        caminho: ["backend", "testes", "priorizar"],
        descricao: "Prioriza backlog de testes do backend.",
        escopo: "adaptavel",
        efeito: "geracao",
        arquivo: "backend/testes-priorizar.ts"
    },
    {
        caminho: ["backend", "java", "corrigir-fqn"],
        descricao: "Substitui FQNs por imports em arquivos Java.",
        escopo: "adaptavel",
        efeito: "mutacao",
        arquivo: "backend/java-corrigir-fqn.ts"
    },
    {
        caminho: ["backend", "notificacoes", "auditar-assuntos"],
        descricao: "Audita literais de assunto de notificacao fora de AssuntosNotificacao.",
        escopo: "perfil-sgc",
        efeito: "auditoria",
        arquivo: "backend/notificacoes-assuntos-auditar.ts"
    },
    {
        caminho: ["frontend", "cobertura", "auditoria"],
        descricao: "Auditoria unificada de cobertura e risco (Frontend).",
        escopo: "adaptavel",
        efeito: "auditoria",
        arquivo: "frontend/cobertura-auditoria.ts"
    },
    {
        caminho: ["frontend", "cobertura", "ramificacoes"],
        descricao: "Lista arquivos com lacunas de ramificacoes no frontend.",
        escopo: "adaptavel",
        efeito: "auditoria",
        arquivo: "frontend/cobertura-ramificacoes.ts"
    },
    {
        caminho: ["frontend", "cobertura", "ramificacoes-erros"],
        descricao: "Cruza lacunas de ramificacoes com sinais de tratamento de erro suspeito no frontend.",
        escopo: "perfil-sgc",
        efeito: "auditoria",
        arquivo: "frontend/cobertura-ramificacoes-erros.ts"
    },
    {
        caminho: ["frontend", "residuos", "auditar"],
        descricao: "Audita residuos estruturais do frontend.",
        escopo: "adaptavel",
        efeito: "auditoria",
        arquivo: "frontend/residuos-auditar.ts"
    },
    {
        caminho: ["frontend", "residuos", "validar"],
        descricao: "Valida orcamentos e excecoes dos residuos do frontend.",
        escopo: "adaptavel",
        efeito: "auditoria",
        arquivo: "frontend/residuos-validar.ts"
    },
    {
        caminho: ["frontend", "arquitetura", "auditar"],
        descricao: "Audita vazamentos arquiteturais e estrategia de cache exposta no frontend.",
        escopo: "adaptavel",
        efeito: "auditoria",
        arquivo: "frontend/arquitetura-auditar.ts"
    },
    {
        caminho: ["frontend", "arquitetura", "validar"],
        descricao: "Valida regras arquiteturais do frontend (gate duro).",
        escopo: "adaptavel",
        efeito: "auditoria",
        arquivo: "frontend/arquitetura-validar.ts"
    },
    {
        caminho: ["frontend", "views", "templates-validar"],
        descricao: "Valida previsibilidade estrutural de templates das views.",
        escopo: "perfil-sgc",
        efeito: "auditoria",
        arquivo: "frontend/views-templates-validar.ts"
    },
    {
        caminho: ["frontend", "modais", "validar"],
        descricao: "Valida o uso padronizado de ModalPadrao e proibe BModal cru fora do componente-base.",
        escopo: "perfil-sgc",
        efeito: "auditoria",
        arquivo: "frontend/modais-validar.ts"
    },
    {
        caminho: ["frontend", "identificadores-teste", "listar"],
        descricao: "Lista identificadores de teste do frontend.",
        escopo: "adaptavel",
        efeito: "auditoria",
        arquivo: "frontend/identificadores-teste-listar.ts"
    },
    {
        caminho: ["frontend", "identificadores-teste", "listar-duplicados"],
        descricao: "Lista identificadores de teste duplicados.",
        escopo: "adaptavel",
        efeito: "auditoria",
        arquivo: "frontend/identificadores-teste-listar-duplicados.ts"
    },
    {
        caminho: ["codigo", "cheiros", "auditar"],
        descricao: "Gera fotografia de sinais de complexidade acidental e codigo defensivo.",
        escopo: "adaptavel",
        efeito: "auditoria",
        arquivo: "codigo/cheiros-auditar.ts"
    },
    {
        caminho: ["codigo", "semgrep", "auditar"],
        descricao: "Executa regras locais de Semgrep para backend, frontend e integração.",
        escopo: "adaptavel",
        efeito: "auditoria",
        arquivo: "codigo/semgrep-auditar.ts"
    },
    {
        caminho: ["codigo", "nomes", "coletar-simbolos"],
        descricao: "Gera inventario de pacotes, arquivos, tipos e membros.",
        escopo: "adaptavel",
        efeito: "geracao",
        arquivo: "codigo/nomes-simbolos-coletar.ts"
    },
    {
        caminho: ["codigo", "nomes", "auditar-consistencia"],
        descricao: "Audita padroes e divergencias de nomenclatura.",
        escopo: "adaptavel",
        efeito: "auditoria",
        arquivo: "codigo/nomes-consistencia-auditar.ts"
    },
    {
        caminho: ["codigo", "nomes", "auditar-idioma"],
        descricao: "Detecta nomes em inglês e campos com 'id' que deveriam usar 'codigo'.",
        escopo: "perfil-sgc",
        efeito: "auditoria",
        arquivo: "codigo/idioma-consistencia-auditar.ts"
    },
    {
        caminho: ["integracao", "contratos", "exportar-openapi"],
        descricao: "Exporta o OpenAPI atual da aplicação para arquivo local.",
        escopo: "adaptavel",
        efeito: "geracao",
        arquivo: "integracao/contratos-exportar-openapi.ts"
    },
    {
        caminho: ["integracao", "contratos", "diff"],
        descricao: "Compara duas versões do OpenAPI e resume mudanças de contrato.",
        escopo: "adaptavel",
        efeito: "auditoria",
        arquivo: "integracao/contratos-diff.ts"
    },
    {
        caminho: ["integracao", "contratos", "fixar-baseline"],
        descricao: "Promove o OpenAPI mais recente como baseline de comparação.",
        escopo: "adaptavel",
        efeito: "mutacao",
        arquivo: "integracao/contratos-fixar-baseline.ts"
    },
    {
        caminho: ["requisitos", "cdus", "inventariar"],
        descricao: "Inventaria formatos e convenções implícitas dos `specs/cdu-*.md`.",
        escopo: "perfil-sgc",
        efeito: "geracao",
        arquivo: "requisitos/cdus-inventariar.ts"
    },
    {
        caminho: ["requisitos", "cdus", "auditar"],
        descricao: "Audita a estrutura canônica mínima dos `specs/cdu-*.md`.",
        escopo: "perfil-sgc",
        efeito: "auditoria",
        arquivo: "requisitos/cdus-auditar.ts"
    },
    {
        caminho: ["requisitos", "cdus", "auditar-estilo"],
        descricao: "Audita convenções tipográficas de aspas simples, aspas duplas e crases nos `specs/cdu-*.md`.",
        escopo: "perfil-sgc",
        efeito: "auditoria",
        arquivo: "requisitos/cdus-auditar-estilo.ts"
    },
    {
        caminho: ["requisitos", "cdus", "inventariar-vocabulario"],
        descricao: "Inventaria perfis, situações, tipos de processo e elementos de UI recorrentes nos `specs/cdu-*.md`.",
        escopo: "perfil-sgc",
        efeito: "geracao",
        arquivo: "requisitos/cdus-inventariar-vocabulario.ts"
    },
    {
        caminho: ["requisitos", "cdus", "auditar-vocabulario"],
        descricao: "Audita variações de vocabulário controlado nos `specs/cdu-*.md`.",
        escopo: "perfil-sgc",
        efeito: "auditoria",
        arquivo: "requisitos/cdus-auditar-vocabulario.ts"
    },
    {
        caminho: ["requisitos", "cdus", "inventariar-mensagens"],
        descricao: "Inventaria descrições, assuntos, mensagens e toasts recorrentes nos `specs/cdu-*.md`.",
        escopo: "perfil-sgc",
        efeito: "geracao",
        arquivo: "requisitos/cdus-inventariar-mensagens.ts"
    },
    {
        caminho: ["requisitos", "cdus", "auditar-mensagens"],
        descricao: "Audita problemas mecânicos em descrições, assuntos, mensagens e toasts dos `specs/cdu-*.md`.",
        escopo: "perfil-sgc",
        efeito: "auditoria",
        arquivo: "requisitos/cdus-auditar-mensagens.ts"
    },
    {
        caminho: ["requisitos", "cdus", "auditar-mensagens-codigo"],
        descricao: "Compara descrições, mensagens e toasts dos `specs/cdu-*.md` com mensagens canônicas extraídas do código.",
        escopo: "perfil-sgc",
        efeito: "auditoria",
        arquivo: "requisitos/cdus-auditar-mensagens-codigo.ts"
    },
    {
        caminho: ["requisitos", "cdus", "inventariar-densidade"],
        descricao: "Inventaria densidade documental dos `specs/cdu-*.md` por palavras, passos e profundidade de listas.",
        escopo: "perfil-sgc",
        efeito: "geracao",
        arquivo: "requisitos/cdus-inventariar-densidade.ts"
    },
    {
        caminho: ["requisitos", "cdus", "inventariar-duplicacoes"],
        descricao: "Inventaria blocos textuais duplicados nos `specs/cdu-*.md`.",
        escopo: "perfil-sgc",
        efeito: "geracao",
        arquivo: "requisitos/cdus-inventariar-duplicacoes.ts"
    },
    {
        caminho: ["projeto", "arvore-linhas"],
        descricao: "Gera arvore agregada de linhas do repositório.",
        escopo: "nucleo",
        efeito: "geracao",
        arquivo: "projeto/arvore-linhas.ts"
    },
    {
        caminho: ["projeto", "versao-sincronizar"],
        descricao: "Sincroniza a versao entre gradle.properties e frontend/package.json.",
        escopo: "adaptavel",
        efeito: "mutacao",
        arquivo: "projeto/versao-sincronizar.ts"
    }
] as const satisfies readonly DefinicaoComandoArquivo[];

const CATALOGO_COMANDOS_ESPECIAIS = [
    {
        caminho: ["qualidade", "coletar"],
        descricao: "Coleta uma fotografia de qualidade do projeto.",
        escopo: "adaptavel",
        efeito: "orquestracao"
    },
    {
        caminho: ["qualidade", "resumo"],
        descricao: "Resume a fotografia de qualidade mais recente.",
        escopo: "nucleo",
        efeito: "auditoria"
    },
    {
        caminho: ["projeto", "dependencias", "auditar"],
        descricao: "Audita uso, atualizacao e vulnerabilidades de dependencias npm e Gradle.",
        escopo: "adaptavel",
        efeito: "orquestracao"
    },
    {
        caminho: ["projeto", "ambiente", "verificar"],
        descricao: "Verifica pre-requisitos, comandos e arquivos essenciais do projeto auditado.",
        escopo: "adaptavel",
        efeito: "auditoria"
    },
    {
        caminho: ["projeto", "artefatos", "limpar"],
        descricao: "Lista ou remove artefatos gerados pelo toolkit e pelas ferramentas de qualidade.",
        escopo: "adaptavel",
        efeito: "mutacao"
    },
    {
        caminho: ["projeto", "qualidade"],
        descricao: "Executa os perfis consolidados de qualidade do projeto.",
        escopo: "adaptavel",
        efeito: "orquestracao"
    },
    {
        caminho: ["projeto", "preparar"],
        descricao: "Prepara o ambiente do projeto com etapas opcionais.",
        escopo: "adaptavel",
        efeito: "orquestracao"
    }
] as const satisfies readonly DefinicaoComando[];

const CATALOGO_COMANDOS_COMPLETO = [...CATALOGO_COMANDOS, ...CATALOGO_COMANDOS_ESPECIAIS] as const;

export {
    CATALOGO_COMANDOS,
    CATALOGO_COMANDOS_COMPLETO
};
