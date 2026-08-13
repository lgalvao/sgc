# Toolkit de scripts do SGC

CLI TypeScript para auditoria, qualidade e manutenção de projetos Java/Spring Boot/Vue. O SGC é o perfil padrão, mas as
capacidades horizontais podem ser usadas em outros projetos por configuração e composição.

## Execução

Requisitos: Node 26.7 ou superior e dependências do workspace `toolkit` instaladas.

```bash
npm --prefix toolkit install
npx tsx toolkit/sgc.ts --help
```

O toolkit executa a fonte TypeScript diretamente com `tsx`. `dist/` é gerado apenas pelo gate de build e não participa
do fluxo normal.

A ajuda da CLI é a fonte canônica para comandos, opções e descrições:

```bash
npx tsx toolkit/sgc.ts backend --help
npx tsx toolkit/sgc.ts frontend --help
npx tsx toolkit/sgc.ts requisitos --help
npx tsx toolkit/sgc.ts qualidade --help
npx tsx toolkit/sgc.ts projeto --help
```

A CLI rejeita opções desconhecidas, valores ausentes e argumentos posicionais excedentes. Opções com valor também podem
usar a forma `--opcao=valor`; ela é normalizada antes de chegar ao script executado.

Os entrypoints diretos dos comandos catalogados reutilizam o mesmo contrato. Por exemplo, `npx tsx
toolkit/codigo/cheiros-auditar.ts --opcao-inexistente` falha antes de iniciar a auditoria.

## Organização

| Caminho | Responsabilidade |
|---|---|
| `sgc.ts` | entrada e roteamento da CLI |
| `lib/` | configuração, execução, saída, catálogo e domínios compartilhados |
| `backend/` | cobertura, arquitetura, contratos, testes e utilidades Java |
| `frontend/` | cobertura, arquitetura, resíduos e validações Vue |
| `codigo/` | auditorias transversais de código, nomes e Semgrep |
| `integracao/` | contratos OpenAPI |
| `requisitos/` | análise e auditoria de documentos CDU |
| `qualidade/` | coleta, fotografia, resumo e execução de tarefas |
| `projeto/` | ambiente, dependências, artefatos, versão e árvore de linhas |
| `test/` | testes e fixtures próprios do toolkit |

O desenho desejado separa:

- núcleo horizontal, sem regras do SGC;
- adaptadores de Java/Gradle, Vue/npm, OpenAPI e ferramentas externas;
- perfil SGC, com seus defaults e políticas específicas.

## Comandos representativos

### Backend e frontend

```bash
npx tsx toolkit/sgc.ts backend cobertura auditoria --json
npx tsx toolkit/sgc.ts backend testes analisar --json
npx tsx toolkit/sgc.ts backend testes analisar --gravar --saida analise-testes.md
npx tsx toolkit/sgc.ts backend testes priorizar --entrada analise-testes.json --gravar
npx tsx toolkit/sgc.ts backend java corrigir-fqn
npx tsx toolkit/sgc.ts frontend cobertura auditoria --json
npx tsx toolkit/sgc.ts frontend residuos validar
npx tsx toolkit/sgc.ts frontend identificadores-teste listar-duplicados
```

`backend java corrigir-fqn` simula por padrão; use `--gravar` para modificar fontes.

`backend testes analisar` e `backend testes priorizar` também são somente leitura por padrão. Use `--json` para alimentar
agentes e scripts pelo stdout; use `--gravar` para persistir relatórios.

O JSON persistido por `backend testes analisar` usa `versao: 1`, campos em português/camelCase e categorias com os grupos
`comTeste` e `semTeste`. `backend testes priorizar --json` emite outro contrato versionado, com `versao: 1` e a chave
`prioridades`; ao receber um relatório JSON incompatível, o comando falha antes de produzir uma priorização.

### Código e integração

```bash
npx tsx toolkit/sgc.ts codigo cheiros auditar --json
npx tsx toolkit/sgc.ts codigo semgrep auditar
npx tsx toolkit/sgc.ts codigo nomes auditar-consistencia
npx tsx toolkit/sgc.ts integracao contratos diff
```

Cheiros e Semgrep são complementares: o primeiro aplica heurísticas internas, enquanto o segundo executa regras
estruturais configuráveis.

OpenAPI mantém exportação, comparação e promoção de baseline. O toolkit não gera tipos TypeScript a partir do contrato.

### Casos de uso CDU

```bash
npx tsx toolkit/sgc.ts requisitos cdus inventariar
npx tsx toolkit/sgc.ts requisitos cdus inventariar --secoes vocabulario,mensagens
npx tsx toolkit/sgc.ts requisitos cdus auditar --json
npx tsx toolkit/sgc.ts requisitos cdus auditar --secoes estrutura,estilo,vocabulario,mensagens
npx tsx toolkit/sgc.ts requisitos cdus auditar --secoes mensagens-codigo
```

`inventariar` consolida formatos, vocabulário, mensagens, densidade e duplicações. `auditar` consolida estrutura, estilo,
vocabulário, mensagens e, quando as fontes canônicas estão disponíveis, a comparação com o código. Use `--secoes` para
executar apenas partes da análise; `todos` seleciona todas as seções. Os inventários continuam sendo diagnósticos
ocasionais, não gates automáticos.

O formato CDU é uma capacidade horizontal em evolução. O perfil atual do SGC usa `specs/cdu/cdu-*.md` e fornece seu
vocabulário, situações, mensagens e extratores de código. Esses elementos serão parametrizados para outros projetos.

### Qualidade e projeto

```bash
npx tsx toolkit/sgc.ts qualidade coletar --perfil rapido
npx tsx toolkit/sgc.ts qualidade tarefas executar rapido
npx tsx toolkit/sgc.ts qualidade resumo
npx tsx toolkit/sgc.ts projeto ambiente verificar
npx tsx toolkit/sgc.ts projeto dependencias auditar
npx tsx toolkit/sgc.ts projeto artefatos limpar
npx tsx toolkit/sgc.ts projeto versao-sincronizar 1.2.3
```

`qualidade coletar` executa adaptadores e produz uma fotografia consolidada. `qualidade tarefas executar` apenas executa
o perfil configurado em `execucoes.qualidade`.

`projeto dependencias auditar` reúne uso e declarações pelo Knip, versões npm desatualizadas, vulnerabilidades npm e
atualizações Gradle. Achados são diferenciados de falhas de execução.

`projeto artefatos limpar` mostra uma prévia e só remove com `--confirmar`. `projeto versao-sincronizar` simula e só
altera arquivos com `--gravar`.

## Contrato de efeitos colaterais

- Auditorias e inventários são somente leitura por padrão.
- Persistência de fotografias e relatórios exige `--gravar`.
- Remoção exige `--confirmar`.
- Ações explicitamente geradoras ou promotoras, como exportar OpenAPI e fixar baseline, gravam como parte do seu contrato.
- `--json` escreve dados em stdout; mensagens operacionais e erros devem permanecer fora do JSON.
- Caminhos relativos são resolvidos contra `--base` quando a opção estiver disponível.
- `--help` e `-h` mostram a ajuda sem exigir argumentos posicionais obrigatórios.

O catálogo interno separa a `finalidade` (`auditar`, `inventariar`, `gerar`, `transformar` ou `orquestrar`) dos efeitos
observáveis: persistência `nenhuma`, `opcional` ou `intrínseca`, remoção, subprocessos e rede. Persistência descreve o que
o toolkit grava diretamente; subprocessos podem ter efeitos próprios definidos pela ferramenta externa.

A acessibilidade Playwright/Axe do SGC não pertence ao toolkit. Ela é executada diretamente pelo workspace `e2e/`.

## Configuração por projeto

Crie `configuracao-toolkit.json` na raiz auditada. O schema atual exige `versao: 1` e rejeita chaves desconhecidas ou
caminhos vazios.

```json
{
  "versao": 1,
  "diretorios": {
    "backend": "servidor",
    "frontend": "cliente",
    "backendCodigo": "servidor/src/main/java",
    "backendTestes": "servidor/src/test/java",
    "frontendCodigo": "cliente/src",
    "testesIntegracao": "e2e",
    "coberturaBackend": "servidor/build/reports/jacoco/test/jacocoTestReport.xml",
    "coberturaFrontend": "cliente/coverage/coverage-final.json",
    "artefatosQualidade": ".qualidade"
  },
  "requisitos": {
    "cdus": {
      "padraoArquivos": "documentacao/casos-de-uso/cdu-*.md"
    }
  }
}
```

Diretórios reconhecidos:

- `backend`, `frontend`, `backendCodigo`, `backendTestes`, `frontendCodigo` e `testesIntegracao`;
- `artefatosQualidade`, `coberturaBackend`, `coberturaFrontend` e `contratosOpenapi`;
- `regrasSemgrep`, `orcamentoResiduosFrontend` e `excecoesResiduosFrontend`.

O corpus CDU usa `specs/cdu/cdu-*.md` por padrão. Para outro layout, configure
`requisitos.cdus.padraoArquivos` com um glob relativo à raiz auditada. O parser, as auditorias estruturais, a densidade e
as duplicações continuam horizontais; políticas de vocabulário, estilo e comparação com mensagens do código ainda são
específicas do perfil configurado.

A seção `mensagens-codigo` usa sete fontes SGC por padrão. Um projeto pode substituí-las por fontes próprias, declarando
o caminho e o adaptador de cada arquivo; uma lista vazia desativa essa comparação:

```json
{
  "versao": 1,
  "requisitos": {
    "cdus": {
      "fontesMensagensCodigo": [
        {"caminho": "servidor/src/main/java/app/Mensagens.java", "tipo": "mensagensJava"},
        {"caminho": "cliente/src/constants/textos.ts", "tipo": "textosTypescript"}
      ]
    }
  }
}
```

Os tipos aceitos são `mensagensJava`, `assuntosJava`, `notificacoesTypescript` e `textosTypescript`. Eles definem o
adaptador de leitura; prefixos, grupos e convenções de mensagens continuam sendo políticas que precisam ser extraídas do
perfil quando houver necessidade de generalização adicional.

Os valores controlados do CDU também podem ser substituídos em `requisitos.cdus.vocabulario` (`perfisCanonicos`,
`tiposProcessoCanonicos` e `arquivoSituacoesCanonicas`) e `requisitos.cdus.estilo.perfisEmCrases`. O default preserva os
perfis, tipos, situações e convenções tipográficas atuais do SGC.

Execuções de dependências e qualidade também podem ser substituídas:

```json
{
  "versao": 1,
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

Opções explícitas da API ou CLI têm precedência sobre o arquivo. Categorias não configuradas usam os defaults do perfil
SGC. A configuração é considerada confiável e não funciona como sandbox.

A política Semgrep padrão vem do pacote; um override em `diretorios.regrasSemgrep` é resolvido contra a raiz auditada.
Orçamentos e exceções de resíduos são opcionais; quando configurados, os arquivos precisam existir e conter JSON válido.

## Uso como pacote

O pacote usa o modelo fonte + `tsx`. O smoke de distribuição instala o tarball em um consumidor temporário para impedir
dependências acidentais do workspace.

```bash
npm --prefix toolkit pack
npm install --save-dev ./sgc-scripts-0.1.0.tgz
npx sgc --help
```

As APIs programáticas públicas atuais são deliberadamente pequenas:

```ts
import {extrairCoberturaJacoco} from "sgc-scripts/cobertura-java";
import {extrairCoberturaFrontend} from "sgc-scripts/cobertura-web";

const backend = await extrairCoberturaJacoco("relatorios/jacoco.xml", {diretorioBase});
const frontend = await extrairCoberturaFrontend("cliente/coverage/coverage-final.json", {diretorioBase});
```

Módulos internos não fazem parte da API pública. Novos subpaths devem ser publicados quando representarem uma fronteira
estável e útil para composição por scripts, humanos ou agentes; o smoke usa um projeto temporário instalado para validar o
contrato, mesmo quando não existe consumidor prévio no código.

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

Os testes ficam em `toolkit/test/` e são organizados por domínio. `test/pacote.test.ts` é executado separadamente porque
instala o pacote em ambiente isolado. O build valida a compilação, mas a execução cotidiana continua usando TypeScript
diretamente.

## Limites

O toolkit complementa Gradle, npm, Playwright e ferramentas de análise; não substitui seus comandos nativos. Políticas
específicas do SGC devem continuar funcionando, mas precisam permanecer identificadas como perfil local quando não forem
universais.
