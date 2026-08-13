type DefinicaoComandoArquivo = Readonly<{
    caminho: readonly [string, ...string[]];
    descricao: string;
    arquivo: string;
}>;

const CATALOGO_COMANDOS = [
    {
        caminho: ["backend", "cobertura", "auditoria"],
        descricao: "Auditoria unificada de cobertura e risco (Backend).",
        arquivo: "backend/cobertura-auditoria.ts"
    },
    {
        caminho: ["backend", "cobertura", "ramificacoes"],
        descricao: "Lista classes com lacunas de ramificacoes no backend.",
        arquivo: "backend/cobertura-ramificacoes.ts"
    },
    {
        caminho: ["backend", "arquitetura", "auditar"],
        descricao: "Audita god objects (Services, Facades, Controllers) por linhas, metodos e dependencias.",
        arquivo: "backend/arquitetura-auditar.ts"
    },
    {
        caminho: ["backend", "coesao", "auditar"],
        descricao: "Audita Services com responsabilidades misturadas (consulta, mutacao, workflow, notificacao).",
        arquivo: "backend/coesao-auditar.ts"
    },
    {
        caminho: ["backend", "contratos", "auditar"],
        descricao: "Audita vazamentos de model.* em DTOs expostos por controllers.",
        arquivo: "backend/contratos-auditar.ts"
    },
    {
        caminho: ["backend", "testes", "analisar"],
        descricao: "Detecta classes sem testes e gera Markdown/JSON.",
        arquivo: "backend/testes-analisar.ts"
    },
    {
        caminho: ["backend", "testes", "priorizar"],
        descricao: "Prioriza backlog de testes do backend.",
        arquivo: "backend/testes-priorizar.ts"
    },
    {
        caminho: ["backend", "java", "corrigir-fqn"],
        descricao: "Substitui FQNs por imports em arquivos Java.",
        arquivo: "backend/java-corrigir-fqn.ts"
    },
    {
        caminho: ["backend", "notificacoes", "auditar-assuntos"],
        descricao: "Audita literais de assunto de notificacao fora de AssuntosNotificacao.",
        arquivo: "backend/notificacoes-assuntos-auditar.ts"
    },
    {
        caminho: ["frontend", "cobertura", "auditoria"],
        descricao: "Auditoria unificada de cobertura e risco (Frontend).",
        arquivo: "frontend/cobertura-auditoria.ts"
    },
    {
        caminho: ["frontend", "cobertura", "ramificacoes"],
        descricao: "Lista arquivos com lacunas de ramificacoes no frontend.",
        arquivo: "frontend/cobertura-ramificacoes.ts"
    },
    {
        caminho: ["frontend", "cobertura", "ramificacoes-erros"],
        descricao: "Cruza lacunas de ramificacoes com sinais de tratamento de erro suspeito no frontend.",
        arquivo: "frontend/cobertura-ramificacoes-erros.ts"
    },
    {
        caminho: ["frontend", "residuos", "auditar"],
        descricao: "Audita residuos estruturais do frontend.",
        arquivo: "frontend/residuos-auditar.ts"
    },
    {
        caminho: ["frontend", "residuos", "validar"],
        descricao: "Valida orcamentos e excecoes dos residuos do frontend.",
        arquivo: "frontend/residuos-validar.ts"
    },
    {
        caminho: ["frontend", "arquitetura", "auditar"],
        descricao: "Audita vazamentos arquiteturais e estrategia de cache exposta no frontend.",
        arquivo: "frontend/arquitetura-auditar.ts"
    },
    {
        caminho: ["frontend", "arquitetura", "validar"],
        descricao: "Valida regras arquiteturais do frontend (gate duro).",
        arquivo: "frontend/arquitetura-validar.ts"
    },
    {
        caminho: ["frontend", "views", "templates-validar"],
        descricao: "Valida previsibilidade estrutural de templates das views.",
        arquivo: "frontend/views-templates-validar.ts"
    },
    {
        caminho: ["frontend", "modais", "validar"],
        descricao: "Valida o uso padronizado de ModalPadrao e proibe BModal cru fora do componente-base.",
        arquivo: "frontend/modais-validar.ts"
    },
    {
        caminho: ["frontend", "identificadores-teste", "listar"],
        descricao: "Lista identificadores de teste do frontend.",
        arquivo: "frontend/identificadores-teste-listar.ts"
    },
    {
        caminho: ["frontend", "identificadores-teste", "listar-duplicados"],
        descricao: "Lista identificadores de teste duplicados.",
        arquivo: "frontend/identificadores-teste-listar-duplicados.ts"
    },
    {
        caminho: ["frontend", "acessibilidade", "crawler"],
        descricao: "Executa o crawler Axe-core em todas as rotas principais.",
        arquivo: "frontend/acessibilidade-crawler.ts"
    },
    {
        caminho: ["frontend", "acessibilidade", "processar"],
        descricao: "Processa os resultados do crawler em um relatorio Markdown.",
        arquivo: "frontend/acessibilidade-processar-resultados.ts"
    },
    {
        caminho: ["codigo", "cheiros", "auditar"],
        descricao: "Gera fotografia de sinais de complexidade acidental e codigo defensivo.",
        arquivo: "codigo/cheiros-auditar.ts"
    },
    {
        caminho: ["codigo", "semgrep", "auditar"],
        descricao: "Executa regras locais de Semgrep para backend, frontend e integração.",
        arquivo: "codigo/semgrep-auditar.ts"
    },
    {
        caminho: ["codigo", "nomes", "coletar-simbolos"],
        descricao: "Gera inventario de pacotes, arquivos, tipos e membros.",
        arquivo: "codigo/nomes-simbolos-coletar.ts"
    },
    {
        caminho: ["codigo", "nomes", "auditar-consistencia"],
        descricao: "Audita padroes e divergencias de nomenclatura.",
        arquivo: "codigo/nomes-consistencia-auditar.ts"
    },
    {
        caminho: ["codigo", "nomes", "auditar-idioma"],
        descricao: "Detecta nomes em inglês e campos com 'id' que deveriam usar 'codigo'.",
        arquivo: "codigo/idioma-consistencia-auditar.ts"
    },
    {
        caminho: ["integracao", "contratos", "exportar-openapi"],
        descricao: "Exporta o OpenAPI atual da aplicação para arquivo local.",
        arquivo: "integracao/contratos-exportar-openapi.ts"
    },
    {
        caminho: ["integracao", "contratos", "diff"],
        descricao: "Compara duas versões do OpenAPI e resume mudanças de contrato.",
        arquivo: "integracao/contratos-diff.ts"
    },
    {
        caminho: ["integracao", "contratos", "fixar-baseline"],
        descricao: "Promove o OpenAPI mais recente como baseline de comparação.",
        arquivo: "integracao/contratos-fixar-baseline.ts"
    },
    {
        caminho: ["requisitos", "cdus", "inventariar"],
        descricao: "Inventaria formatos e convenções implícitas dos `specs/cdu-*.md`.",
        arquivo: "requisitos/cdus-inventariar.ts"
    },
    {
        caminho: ["requisitos", "cdus", "auditar"],
        descricao: "Audita a estrutura canônica mínima dos `specs/cdu-*.md`.",
        arquivo: "requisitos/cdus-auditar.ts"
    },
    {
        caminho: ["requisitos", "cdus", "auditar-estilo"],
        descricao: "Audita convenções tipográficas de aspas simples, aspas duplas e crases nos `specs/cdu-*.md`.",
        arquivo: "requisitos/cdus-auditar-estilo.ts"
    },
    {
        caminho: ["requisitos", "cdus", "inventariar-vocabulario"],
        descricao: "Inventaria perfis, situações, tipos de processo e elementos de UI recorrentes nos `specs/cdu-*.md`.",
        arquivo: "requisitos/cdus-inventariar-vocabulario.ts"
    },
    {
        caminho: ["requisitos", "cdus", "auditar-vocabulario"],
        descricao: "Audita variações de vocabulário controlado nos `specs/cdu-*.md`.",
        arquivo: "requisitos/cdus-auditar-vocabulario.ts"
    },
    {
        caminho: ["requisitos", "cdus", "inventariar-mensagens"],
        descricao: "Inventaria descrições, assuntos, mensagens e toasts recorrentes nos `specs/cdu-*.md`.",
        arquivo: "requisitos/cdus-inventariar-mensagens.ts"
    },
    {
        caminho: ["requisitos", "cdus", "auditar-mensagens"],
        descricao: "Audita problemas mecânicos em descrições, assuntos, mensagens e toasts dos `specs/cdu-*.md`.",
        arquivo: "requisitos/cdus-auditar-mensagens.ts"
    },
    {
        caminho: ["requisitos", "cdus", "auditar-mensagens-codigo"],
        descricao: "Compara descrições, mensagens e toasts dos `specs/cdu-*.md` com mensagens canônicas extraídas do código.",
        arquivo: "requisitos/cdus-auditar-mensagens-codigo.ts"
    },
    {
        caminho: ["requisitos", "cdus", "inventariar-densidade"],
        descricao: "Inventaria densidade documental dos `specs/cdu-*.md` por palavras, passos e profundidade de listas.",
        arquivo: "requisitos/cdus-inventariar-densidade.ts"
    },
    {
        caminho: ["requisitos", "cdus", "inventariar-duplicacoes"],
        descricao: "Inventaria blocos textuais duplicados nos `specs/cdu-*.md`.",
        arquivo: "requisitos/cdus-inventariar-duplicacoes.ts"
    },
    {
        caminho: ["projeto", "arvore-linhas"],
        descricao: "Gera arvore agregada de linhas do repositório.",
        arquivo: "projeto/arvore-linhas.ts"
    },
    {
        caminho: ["projeto", "versao-sincronizar"],
        descricao: "Sincroniza a versao entre gradle.properties e frontend/package.json.",
        arquivo: "projeto/versao-sincronizar.ts"
    }
] as const satisfies readonly DefinicaoComandoArquivo[];

export {
    CATALOGO_COMANDOS,
    type DefinicaoComandoArquivo
};
