# Toolkit de Scripts do SGC

## Papel do módulo

`toolkit/` reúne a CLI de automação do repositório. Ela concentra comandos operacionais e de auditoria usados para
qualidade, preparação, diagnóstico do projeto e utilidades de backend/frontend.

### Execução e build

O toolkit executa a árvore-fonte diretamente. O build é uma verificação opcional do artefato compilado e não faz parte do
fluxo normal de desenvolvimento:

```bash
npm --prefix toolkit run build
```

Ponto de entrada principal:

```bash
npx tsx toolkit/sgc.ts
```

Quando um comando importar módulos TypeScript, o toolkit usa `tsx` automaticamente. Também é possível executar um
script diretamente:

```bash
npx tsx toolkit/sgc.ts --help
npx tsx toolkit/projeto/arvore-linhas.ts --help
```

## Visão arquitetural

O toolkit é um módulo Node.js em ESM, separado do backend/frontend, com dependências próprias e testes próprios.

```mermaid
graph TD
    CLI[sgc.ts] --> Backend[backend/]
    CLI --> Frontend[frontend/]
    CLI --> Codigo[codigo/]
    CLI --> Integracao[integracao/]
    CLI --> Qualidade[qualidade/]
    CLI --> Projeto[projeto/]
    Backend --> Lib[lib/]
    Frontend --> Lib
    Codigo --> Lib
    Integracao --> Lib
    Qualidade --> Lib
    Projeto --> Lib
```

## Estrutura do diretório

| Caminho       | Papel                                                             |
|---------------|-------------------------------------------------------------------|
| `sgc.ts`      | roteador principal da CLI                                         |
| `lib/`        | infraestrutura compartilhada, execução, caminhos, saída e utilidades |
| `backend/`    | comandos de cobertura, testes e higiene Java                      |
| `frontend/`   | comandos de cobertura, resíduos, validações e acessibilidade     |
| `codigo/`     | auditorias transversais de cheiros de código                       |
| `integracao/` | contratos OpenAPI e fronteira backend/frontend                    |
| `qualidade/`  | coleta e resumo de qualidade                                       |
| `projeto/`    | preparação, diagnóstico, limpeza e qualidade do repositório        |
| `test/`       | testes do toolkit                                                 |

## Comandos por domínio

### Backend

```bash
npx tsx toolkit/sgc.ts backend cobertura auditoria
npx tsx toolkit/sgc.ts backend testes analisar
npx tsx toolkit/sgc.ts backend testes priorizar
npx tsx toolkit/sgc.ts backend java corrigir-fqn --gravar
npx tsx toolkit/sgc.ts backend notificacoes auditar-assuntos
```

`backend java corrigir-fqn` apenas lista as substituições por padrão; use `--gravar` para alterar os arquivos Java.

### Frontend

```bash
npx tsx toolkit/sgc.ts frontend cobertura auditoria
npx tsx toolkit/sgc.ts frontend residuos auditar
npx tsx toolkit/sgc.ts frontend residuos validar
npx tsx toolkit/sgc.ts frontend identificadores-teste listar
npx tsx toolkit/sgc.ts frontend identificadores-teste listar-duplicados
npx tsx toolkit/sgc.ts frontend acessibilidade crawler
npx tsx toolkit/sgc.ts frontend acessibilidade processar
```

### Código transversal

```bash
npx tsx toolkit/sgc.ts codigo cheiros auditar
npx tsx toolkit/sgc.ts codigo semgrep auditar
npx tsx toolkit/sgc.ts codigo nomes coletar-simbolos
npx tsx toolkit/sgc.ts codigo nomes auditar-consistencia
```

As auditorias de cheiros são somente leitura por padrão e emitem a fotografia em JSON no stdout. Para atualizar os
artefatos `fotografia.json` e `resumo.md`, use a ação explícita `--gravar`:

```bash
npx tsx toolkit/sgc.ts codigo cheiros auditar --json
npx tsx toolkit/sgc.ts codigo cheiros auditar --gravar
```

`frontend arquitetura auditar` segue a mesma regra: a fotografia sai no stdout sem gravação; use `--gravar` para
persistir os artefatos. O coletor consolidado de qualidade já informa essa opção quando precisa manter o artefato
intermediário do perfil SGC.

`backend arquitetura auditar` também é somente leitura por padrão. Use `--gravar` para atualizar os relatórios
Markdown e JSON no diretório de artefatos configurado.

`backend coesao auditar` segue o mesmo contrato e só grava os relatórios quando recebe `--gravar`.

`backend contratos auditar` também é somente leitura por padrão; `--gravar` persiste o relatório Markdown sem alterar
o código Java.

`frontend residuos auditar` e `frontend residuos validar` seguem o mesmo contrato. A fotografia, o resumo e a fotografia
mais recente da validação só são atualizados com `--gravar`; sem essa opção, os resultados ficam no stdout.

`codigo semgrep auditar` também só grava `resultado.json` e `resumo.md` com `--gravar`; a política padrão continua
vindo do pacote e políticas locais podem ser informadas por `--regra` ou pela configuração.

`codigo nomes coletar-simbolos`, `codigo nomes auditar-consistencia` e `codigo nomes auditar-idioma` também são
somente leitura por padrão. Use `--gravar` para persistir o inventário ou os relatórios de nomenclatura; quando uma
auditoria precisar criar o inventário auxiliar, a mesma opção é propagada explicitamente para essa coleta interna.

`integracao contratos diff` compara os documentos OpenAPI sem gravar por padrão. Use `--gravar` para persistir o resumo
Markdown; `exportar-openapi` e `fixar-baseline` continuam sendo ações de geração/promoção explícitas.

`backend cobertura auditoria` e `frontend cobertura auditoria` também só persistem o relatório Markdown com `--gravar`.
O modo `--json` continua adequado para integração sem criar arquivos.

### Requisitos

```bash
npx tsx toolkit/sgc.ts requisitos cdus inventariar
npx tsx toolkit/sgc.ts requisitos cdus auditar
npx tsx toolkit/sgc.ts requisitos cdus auditar-estilo
npx tsx toolkit/sgc.ts requisitos cdus inventariar-vocabulario
npx tsx toolkit/sgc.ts requisitos cdus auditar-vocabulario
npx tsx toolkit/sgc.ts requisitos cdus inventariar-mensagens
npx tsx toolkit/sgc.ts requisitos cdus auditar-mensagens
npx tsx toolkit/sgc.ts requisitos cdus auditar-mensagens-codigo
npx tsx toolkit/sgc.ts requisitos cdus inventariar-densidade
npx tsx toolkit/sgc.ts requisitos cdus inventariar-duplicacoes
```

### Qualidade

```bash
npx tsx toolkit/sgc.ts qualidade coletar --perfil rapido
npx tsx toolkit/sgc.ts qualidade resumo
npx tsx toolkit/sgc.ts qualidade resumo --limite-pontos-criticos 10
```

### Projeto

```bash
npx tsx toolkit/sgc.ts projeto diagnostico
npx tsx toolkit/sgc.ts projeto dependencias auditar
npx tsx toolkit/sgc.ts projeto limpar --confirmar
npx tsx toolkit/sgc.ts projeto qualidade rapido
npx tsx toolkit/sgc.ts projeto preparar --instalar-dependencias
npx tsx toolkit/sgc.ts projeto arvore-linhas
npx tsx toolkit/sgc.ts projeto versao-sincronizar 1.2.3 --gravar
```

`projeto versao-sincronizar` apenas simula por padrão. Use `--base` para outra raiz e `--gravar` para atualizar
`gradle.properties` e `frontend/package.json`.

## Casos de uso típicos

- gerar fotografia consolidada de qualidade para revisão técnica;
- auditar residuos e duplicidade no frontend;
- apoiar evolução da suíte de testes backend;
- validar divergência entre Bean Validation e validação de UI;
- preparar ambiente local de desenvolvimento;
- produzir artefatos de qualidade para inspeção manual.

Os artefatos gerados ficam em `toolkit/qualidade/artefatos/` e são organizados por execução e fotografia mais recente.

### Contrato OpenAPI

O Springdoc continua no backend porque o ciclo E2E usa a documentação OpenAPI/Swagger para aguardar a aplicação nos
ambientes `e2e` e `hom`. No toolkit, a integração mantém exportação, comparação e fixação de referência do contrato.
O gerador de tipos TypeScript foi removido enquanto não houver consumidor de tipos gerados no frontend e enquanto a
ferramenta de geração exigir uma versão anterior do TypeScript.

## Configuração e migração para TypeScript

Os diretórios variáveis do projeto podem ser sobrescritos em `configuracao-toolkit.json` na raiz. Os valores padrão
cobrem o layout atual do SGC, enquanto a configuração permite reutilizar o toolkit em outros projetos sem editar os
auditores.

O arquivo exige a versão `1` e aceita uma seção `diretorios` com nomes conhecidos pelo toolkit:

```json
{
  "versao": 1,
  "diretorios": {
    "backendCodigo": "backend/src/main/java",
    "backendTestes": "backend/src/test/java",
    "frontendCodigo": "frontend/src",
    "artefatosQualidade": "toolkit/qualidade/artefatos"
  }
}
```

Chaves desconhecidas, versões não suportadas e caminhos vazios falham na borda de configuração, antes de um auditor
iniciar.

Além dos diretórios, um projeto pode declarar as execuções que substituem os catálogos padrão do perfil SGC. A seção
`execucoes` aceita escopos de auditoria de dependências, escopos de instalação e perfis de qualidade. Cada comando é
executado na base do projeto ou no segmento informado; `argumentos` deve ser sempre uma lista de textos:

```json
{
  "versao": 1,
  "execucoes": {
    "dependencias": [
      {
        "titulo": "Auditar cliente",
        "segmento": "cliente",
        "comando": "npm",
        "argumentos": ["run", "auditar-dependencias"]
      }
    ],
    "qualidade": {
      "rapido": {
        "descricao": "Verificações rápidas do projeto",
        "tarefas": [
          {
            "titulo": "Verificar projeto",
            "comando": "npm",
            "argumentos": ["run", "qualidade"]
          }
        ]
      }
    },
    "instalacao": [
      {"titulo": "Instalar cliente", "segmento": "cliente"}
    ]
  }
}
```

As três listas são independentes: uma configuração que declara somente um perfil de qualidade continua usando os
defaults SGC para dependências e instalação. Opções explícitas da API ou da CLI têm precedência sobre a configuração;
quando nenhuma delas existe, os defaults SGC preservam o comportamento atual. Os comandos são configuração confiável
do projeto e não formam uma camada de segurança ou de sandbox.

Os orçamentos e exceções de resíduos frontend não têm política padrão empacotada. Sem `diretorios.orcamentoResiduosFrontend`
ou `diretorios.excecoesResiduosFrontend`, o toolkit usa uma política neutra identificada como `padrao-do-toolkit`. Ao
declarar um desses caminhos, o arquivo passa a ser obrigatório e precisa conter JSON válido; arquivo ausente ou inválido
interrompe a validação.

A regra Semgrep padrão é a política do perfil SGC fornecida pelo próprio pacote. Em outro projeto, informe
`diretorios.regrasSemgrep` para usar uma política local; o toolkit resolve o override relativo à raiz auditada.

Nos comandos de inspeção de frontend, `--base` representa a raiz do projeto e resolve `frontendCodigo`; use `--diretorio`
quando a intenção for apontar diretamente para outro diretório de código.

O toolkit executa a árvore-fonte com `tsx`; toda a implementação está em TypeScript estrito. Os testes permanecem em
JavaScript por enquanto, mas não participam da implementação distribuída nem do gate estrito.

Os comandos de requisitos/CDUs e de contratos OpenAPI são módulos importáveis: só executam quando chamados diretamente
pela CLI. Isso permite reutilizar suas funções `principal(argumentos)` em outras automações sem iniciar auditorias ou
integrações durante o carregamento.
`npm run typecheck` executa `tsconfig.estrito.json` com `strict` e `noImplicitOverride` sobre todos os módulos de
implementação TypeScript.

Os comandos de projeto seguem a mesma fronteira. A árvore de linhas aceita `--base <diretorio>` para analisar outro
repositório Git, e a sincronização de versão aceita um diretório base nas funções reutilizáveis sem alterar o projeto
atual por padrão.

Na camada de qualidade, `coleta.ts` e `resumo.ts` têm fronteira reutilizável e não executam trabalho durante o `import`.
Já `coleta-execucao.ts` permanece específico do SGC por coordenar Gradle, npm, Playwright e os auditores locais; ele não
deve ser promovido a abstração horizontal antes de existir um contrato de adaptadores para outro projeto.

Os comandos `codigo nomes` também resolvem `simbolos.json`, `consistencia.json` e `idioma.json` relativos ao `--base`
informado. Assim, a auditoria de outro projeto não lê nem grava silenciosamente no diretório de artefatos do SGC.

Os comandos `backend cobertura auditoria` e `backend cobertura ramificacoes` aceitam `--base` e `--arquivo` para
reutilizar relatórios JaCoCo de outro projeto Spring/Gradle. A antiga auditoria cruzada manual foi removida da CLI por
duplicar a leitura do XML sem contrato JSON ou testes próprios; o histórico Git preserva sua implementação.

## Dependências e execução

`toolkit/package.json` define dependências próprias, separadas do restante do repositório.

Instalação:

```bash
npm --prefix toolkit install
```

Execução dos testes do toolkit:

```bash
npm --prefix toolkit run test
```

Para medir a cobertura do próprio toolkit sem impor threshold prematuro:

```bash
npm --prefix toolkit run test:coverage
```

O smoke de distribuição instala o tarball em um consumidor temporário, sem usar dependências hoisted do monorepo:

```bash
npm --prefix toolkit run test:pacote
```

O modelo de distribuição é fonte + `tsx`. Para consumir uma versão empacotada em outro projeto, instale o tarball e
execute o binário pelo `npx`:

```bash
npm install --save-dev ./sgc-scripts-0.1.0.tgz
npx sgc --help
```

Por padrão, a raiz auditada é o diretório de trabalho do processo. Use `--base` ou `configuracao-toolkit.json` quando
o projeto auditado estiver em outro caminho; o local de instalação do toolkit não é usado como raiz do consumidor.

Lint do toolkit:

```bash
npm --prefix toolkit run lint
```

Auditoria de dependências:

```bash
npm --prefix toolkit run deps:audit
npx tsx toolkit/sgc.ts projeto dependencias auditar
```

## Organização dos testes

O diretório `test/` contém:

- `sgc.test.js`: testes da CLI principal
- `cdus.test.js`: testes das regras CDU específicas do perfil SGC
- `pacote.test.js`: smoke de empacotamento e instalação em consumidor isolado
- `fixtures/`: dados auxiliares para simular cenários de execução

Esses testes garantem que a CLI continue roteando comandos, produzindo saídas e respeitando contratos básicos de
operação.

## Relação com o restante do repositório

O toolkit não substitui os comandos nativos de Gradle, npm ou Playwright; ele os complementa com:

- automação padronizada;
- relatórios agregados;
- auditorias específicas do SGC;
- comandos de produtividade difíceis de expressar apenas com scripts simples.

## Referências

- [README raiz](../../README.md)
- [Backend do SGC](../../backend/README.md)
- [Frontend do SGC](../../frontend/README.md)
