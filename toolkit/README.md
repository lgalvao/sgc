# Toolkit de ferramentas de projeto

CLI TypeScript para auditoria, qualidade e manutenção de projetos Java, Spring Boot e Vue. Inclui capacidades
horizontais, adaptadores de ferramentas e políticas do perfil SGC.

## Execução

Requisitos: Node 26.7 ou superior e dependências instaladas.

```bash
npm --prefix toolkit install
npm --prefix toolkit run ferramentas -- --help
```

No monorepo SGC:

```bash
npm run sgc -- <dominio> <recurso> <acao> [opcoes]
```

Após a instalação do pacote:

```bash
npm exec -- ferramentas --help
```

O toolkit executa `ferramentas.ts` diretamente com `tsx`. `dist/` é gerado pelo build.

## Organização

| Caminho | Responsabilidade |
|---|---|
| `ferramentas.ts` | Entrada e roteamento da CLI |
| `biblioteca/` | Configuração, execução, saída, catálogo e domínios compartilhados |
| `servidor/` | Cobertura, arquitetura, contratos, testes e utilidades Java |
| `cliente/` | Cobertura, arquitetura, resíduos e validações Vue |
| `codigo/` | Auditorias transversais de código, nomes e Semgrep |
| `integracao/` | Contratos OpenAPI |
| `requisitos/` | Análise e auditoria de documentos CDU |
| `qualidade/` | Coleta, fotografia, resumo e execução de tarefas |
| `projeto/` | Ambiente, dependências, artefatos, versão e árvore de linhas |
| `testes/` | Testes e fixtures do toolkit |

Camadas:

- núcleo: algoritmos independentes de projeto e stack;
- adaptador: integração parametrizável com linguagem, framework ou ferramenta;
- perfil SGC: caminhos, defaults e políticas específicas do SGC.

O SGC é uma base auditada pelo toolkit. O SGC não importa nem executa o toolkit como dependência.

## Comandos

| Comando | Camada | Tipo | Função |
|---|---|---|---|
| `servidor cobertura auditoria` | perfil SGC | auditoria | Cobertura e risco do servidor |
| `servidor cobertura ramificacoes` | perfil SGC | auditoria | Lacunas de ramificações do servidor |
| `servidor arquitetura auditar` | adaptável | auditoria | Concentração de responsabilidades em Java |
| `servidor coesao auditar` | perfil SGC | auditoria | Mistura de responsabilidades nos Services |
| `servidor contratos auditar` | adaptável | auditoria | Vazamento de modelos em DTOs expostos |
| `servidor testes analisar` | adaptável | análise | Evidências de testes e cobertura Java |
| `servidor testes priorizar` | adaptável | análise | Prioridades para o backlog de testes |
| `servidor java corrigir-fqn` | adaptável | utilitário | Conversão de FQNs Java em imports |
| `servidor notificacoes auditar-assuntos` | perfil SGC | auditoria | Assuntos de notificações do SGC |
| `cliente cobertura auditoria` | adaptável | auditoria | Cobertura e risco do cliente |
| `cliente cobertura ramificacoes` | adaptável | auditoria | Lacunas de ramificações do cliente |
| `cliente cobertura ramificacoes-erros` | perfil SGC | auditoria | Lacunas e tratamento de erros do SGC |
| `cliente residuos auditar` | adaptável | inventário | Resíduos estruturais do cliente |
| `cliente residuos validar` | adaptável | gate | Orçamento e exceções de resíduos |
| `cliente arquitetura auditar` | perfil SGC | auditoria | Arquitetura Vue segundo a política do SGC |
| `cliente arquitetura validar` | perfil SGC | gate | Regras arquiteturais do cliente |
| `cliente views templates-validar` | perfil SGC | gate | Previsibilidade estrutural de templates |
| `cliente modais validar` | perfil SGC | gate | Padrão de modais |
| `cliente identificadores-teste listar` | adaptável | inventário | Identificadores de testes |
| `cliente identificadores-teste listar-duplicados` | adaptável | inventário | Identificadores de testes repetidos |
| `codigo cheiros auditar` | adaptável | tendência | Complexidade e código defensivo |
| `codigo semgrep auditar` | adaptável | auditoria | Regras Semgrep configuradas |
| `codigo nomes coletar-simbolos` | adaptável | inventário | Pacotes, arquivos, tipos e membros |
| `codigo nomes auditar-consistencia` | adaptável | auditoria | Padrões de nomenclatura |
| `codigo nomes auditar-idioma` | perfil SGC | auditoria | Idioma e uso de `codigo` |
| `integracao contratos exportar-openapi` | adaptável | geração | Exportação do contrato OpenAPI |
| `integracao contratos diff` | adaptável | auditoria | Diferenças entre contratos OpenAPI |
| `integracao contratos fixar-baseline` | adaptável | geração | Promoção de contrato a baseline |
| `requisitos cdus inventariar` | adaptável | inventário | Formatos, vocabulário, mensagens, densidade e duplicações CDU |
| `requisitos cdus auditar` | adaptável | auditoria | Estrutura, estilo, vocabulário e referências CDU |
| `projeto arvore-linhas` | núcleo | inventário | Linhas do repositório |
| `projeto versao-sincronizar` | adaptável | transformação | Versões em arquivos configurados |
| `qualidade coletar` | perfil SGC | orquestração | Fotografia de qualidade do SGC |
| `qualidade tarefas executar` | adaptável | orquestração | Tarefas de qualidade configuradas |
| `qualidade resumo` | núcleo | análise | Resumo de fotografia de qualidade |
| `projeto dependencias auditar` | adaptável | auditoria | Uso, atualização e vulnerabilidades de dependências |
| `projeto ambiente verificar` | perfil SGC | diagnóstico | Pré-requisitos do workspace SGC |
| `projeto artefatos limpar` | adaptável | manutenção | Prévia e remoção de artefatos elegíveis |

## Saída e efeitos

- auditorias e inventários são somente leitura por padrão;
- `--gravar` persiste fotografias e relatórios;
- `--confirmar` confirma remoções;
- `--json` emite o resultado completo em stdout;
- `--json-resumido` emite uma seleção limitada em stdout nos comandos que o oferecem;
- mensagens operacionais e erros ficam fora do JSON;
- caminhos relativos são resolvidos contra `--base` quando a opção existe;
- opções desconhecidas, valores ausentes e argumentos excedentes causam erro;
- `--help` e `-h` exibem a ajuda do comando.

Resumos JSON disponíveis:

| Comando | Conteúdo do resumo |
|---|---|
| `codigo nomes coletar-simbolos` | Totais, linguagens, pacotes principais e 20 arquivos com mais membros |
| `codigo nomes auditar-consistencia` | Indicadores, contagens de formatos e 20 achados por categoria |
| `servidor testes analisar` | Estatísticas, categorias e 20 principais pendências |
| `cliente arquitetura validar` | Módulos, regras e até 20 violações de cada fonte |
| `cliente residuos validar` | Status, resumo, violações e pontos críticos |

Contratos próprios versionados:

- `servidor arquitetura auditar`: `versao: 3`;
- `servidor coesao auditar`: `versao: 2`;
- `servidor testes analisar`: `versao: 2`;
- `servidor testes priorizar`: `versao: 1`;
- `cliente arquitetura auditar`: `versaoSchema: "5.0.0"`;
- `cliente residuos auditar`: `versaoSchema: "3.0.0"`;
- `servidor cobertura auditoria` e `cliente cobertura auditoria`: `versaoSchema: "1.0.0"`;
- `cliente cobertura ramificacoes`, `cliente cobertura ramificacoes-erros` e `servidor cobertura ramificacoes`:
  `versaoSchema: "1.0.0"`;
- `codigo cheiros auditar`: `versao: 4`;
- `codigo nomes coletar-simbolos`: `versao: 1`;
- `codigo nomes auditar-idioma`: `versao: 2`;
- `qualidade resumo`: `versaoSchema: "3.0.0"`.

## Servidor, cliente e código

```bash
ferramentas servidor cobertura auditoria --json
ferramentas servidor testes analisar --json-resumido
ferramentas servidor testes priorizar --entrada analise-testes.json --gravar
ferramentas servidor java corrigir-fqn
ferramentas cliente cobertura auditoria --json
ferramentas cliente residuos validar --json-resumido
ferramentas cliente identificadores-teste listar-duplicados
ferramentas codigo cheiros auditar --json
ferramentas codigo semgrep auditar
ferramentas codigo nomes coletar-simbolos --json-resumido
ferramentas codigo nomes auditar-consistencia --json-resumido
```

Políticas Java são recebidas por `--politica`. A política contém as listas de anotações, nomes, prefixos, sufixos e
caminhos estruturais da classificação.

O motor Semgrep recebe regra, alvos e comando explicitamente. A configuração do perfil compõe esses valores.

As APIs de cobertura recebem relatório, diretório base, padrões de exclusão e caminhos explicitamente. Os formatos JaCoCo
e V8 são preservados na fronteira dos adaptadores.

## OpenAPI

```bash
ferramentas integracao contratos exportar-openapi
ferramentas integracao contratos diff
ferramentas integracao contratos fixar-baseline
```

Os motores OpenAPI recebem URL, base e arquivos explicitamente. O contrato exportado preserva o vocabulário da
especificação; os resultados do toolkit usam campos em português e camelCase.

## Casos de uso CDU

```bash
ferramentas requisitos cdus inventariar
ferramentas requisitos cdus inventariar --secoes vocabulario,mensagens
ferramentas requisitos cdus auditar --json
ferramentas requisitos cdus auditar --secoes estrutura,estilo,vocabulario,mensagens
```

`inventariar` produz inventários de formatos, vocabulário, mensagens, densidade e duplicações. `auditar` produz
resultados de estrutura, estilo, vocabulário, mensagens e referências.

O corpus é configurado em `requisitos.cdus`. O parser e os motores CDU recebem a raiz do projeto, seções, corpus,
vocabulário, estilo e fontes de código pela configuração.

## Qualidade e projeto

```bash
ferramentas qualidade coletar --perfil rapido
ferramentas qualidade tarefas executar rapido
ferramentas qualidade resumo
ferramentas projeto ambiente verificar
ferramentas projeto dependencias auditar
ferramentas projeto artefatos limpar
ferramentas projeto versao-sincronizar 1.2.3
```

`qualidade coletar` usa os perfis de qualidade configurados. `qualidade tarefas executar` executa as tarefas de
`execucoes.qualidade`. `qualidade resumo` lê fotografias `versaoSchema: "3.0.0"`.

`projeto dependencias auditar` reúne uso e declarações pelo Knip, versões npm, vulnerabilidades npm e atualizações Gradle.
Achados e falhas de execução são separados. Atualizações da major 7 do TypeScript são ignoradas pelo perfil SGC enquanto
os projetos permanecem na série 6; outras atualizações continuam visíveis.

`projeto artefatos limpar` lista artefatos e remove com `--confirmar`. `projeto versao-sincronizar` mostra alterações e
grava com `--gravar`.

`projeto arvore-linhas` usa profundidade 3 e mínimo de 500 linhas por padrão. Use `--profundidade 0` e
`--minimo-linhas 0` para expandir todos os níveis e arquivos.

## Configuração por projeto

Crie `configuracao-toolkit.json` na raiz auditada. O schema atual usa `versao: 2`.

```json
{
  "versao": 2,
  "diretorios": {
    "servidor": "servidor",
    "cliente": "cliente",
    "codigoServidor": "servidor/src/main/java",
    "testesServidor": "servidor/src/test/java",
    "codigoCliente": "cliente/src",
    "testesIntegracao": "e2e",
    "coberturaServidor": "servidor/build/reports/jacoco/test/jacocoTestReport.xml",
    "coberturaCliente": "cliente/coverage/coverage-final.json",
    "artefatosQualidade": ".qualidade"
  },
  "requisitos": {
    "cdus": {
      "padraoArquivos": "documentacao/casos-de-uso/cdu-*.md"
    }
  }
}
```

Diretórios configuráveis: `servidor`, `cliente`, `codigoServidor`, `testesServidor`, `codigoCliente`,
`testesIntegracao`, `artefatosQualidade`, `coberturaServidor`, `coberturaCliente`, `contratosOpenapi`, `regrasSemgrep`,
`orcamentoResiduosCliente` e `excecoesResiduosCliente`.

O padrão CDU é `specs/cdu/cdu-*.md`. O glob é relativo à raiz auditada.

Fontes de mensagens CDU usam `mensagensJava`, `assuntosJava`, `notificacoesTypescript` e `textosTypescript`. Prefixos,
grupos, marcadores, vocabulário, situações e estilo são configuráveis em `requisitos.cdus`.

Execuções de dependências e qualidade são configuráveis:

```json
{
  "versao": 2,
  "execucoes": {
    "dependencias": [
      {
        "titulo": "Auditar cliente",
        "segmento": "cliente",
        "comando": "npm",
        "argumentos": ["audit"],
        "codigoNaoZeroIndicaAchados": true
      }
    ],
    "qualidade": {
      "rapido": {
        "descricao": "Verificações rápidas",
        "tarefas": [
          {
            "titulo": "Verificar cliente",
            "comando": "npm",
            "argumentos": ["--prefix", "cliente", "run", "check"]
          }
        ]
      }
    }
  }
}
```

`execucoes.dependencias[].ignorarAtualizacoes` aceita pares `{pacote, major}` para remover essas atualizações do JSON de
`npm outdated` e recalcular o status do escopo.

## Uso como pacote

O pacote publica a fonte TypeScript, o lançador npm e os subpaths públicos:

```bash
npm --prefix toolkit pack
npm install --save-dev ./ferramentas-projeto-0.1.0.tgz
npm exec -- ferramentas --help
```

APIs públicas:

```ts
import {extrairCoberturaJacoco} from "ferramentas-projeto/cobertura-java";
import {extrairCoberturaCliente} from "ferramentas-projeto/cobertura-web";
import {auditarCasosDeUso, inventariarCasosDeUso} from "ferramentas-projeto/casos-de-uso";

const coberturaServidor = await extrairCoberturaJacoco("relatorios/jacoco.xml", {diretorioBase});
const coberturaCliente = await extrairCoberturaCliente("cliente/coverage/coverage-final.json", {diretorioBase});
const inventario = await inventariarCasosDeUso({base: diretorioBase, secoes: ["formatos"]});
const auditoria = await auditarCasosDeUso({base: diretorioBase, secoes: ["estrutura"]});
```

## Desenvolvimento e validação

```bash
npm --prefix toolkit run test -- --maxWorkers=1
npm --prefix toolkit run test:coverage -- --maxWorkers=1
npm --prefix toolkit run test:pacote
npm --prefix toolkit run typecheck
npm --prefix toolkit run typecheck:testes
npm --prefix toolkit run lint
npm --prefix toolkit run deps:audit
npm --prefix toolkit run build
```

Os testes ficam em `toolkit/testes/`. `testes/pacote.test.ts` usa um ambiente de pacote instalado separadamente.

A acessibilidade Playwright/Axe do SGC é executada diretamente pelo workspace `e2e/`.
