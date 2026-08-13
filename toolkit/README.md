# Toolkit de ferramentas de projeto

CLI TypeScript para auditoria, qualidade e manutenção de projetos Java/Spring Boot/Vue. O SGC é o perfil padrão, mas as
capacidades horizontais podem ser usadas em outros projetos por configuração e composição.

## Execução

Requisitos: Node 26.7 ou superior e dependências do workspace `toolkit` instaladas.

```bash
npm --prefix toolkit install
npx tsx toolkit/ferramentas.ts --help
```

O toolkit executa a fonte TypeScript diretamente com `tsx`. `dist/` é gerado apenas pelo gate de build e não participa
do fluxo normal.

A ajuda da CLI é a fonte canônica para comandos, opções e descrições:

```bash
npx tsx toolkit/ferramentas.ts servidor --help
npx tsx toolkit/ferramentas.ts cliente --help
npx tsx toolkit/ferramentas.ts requisitos --help
npx tsx toolkit/ferramentas.ts qualidade --help
npx tsx toolkit/ferramentas.ts projeto --help
```

A CLI rejeita opções desconhecidas, valores ausentes e argumentos posicionais excedentes. Opções com valor também podem
usar a forma `--opcao=valor`; ela é normalizada antes de chegar ao script executado.

Os entrypoints diretos dos comandos catalogados reutilizam o mesmo contrato. Por exemplo, `npx tsx
toolkit/codigo/cheiros-auditar.ts --opcao-inexistente` falha antes de iniciar a auditoria.

## Organização

| Caminho | Responsabilidade |
|---|---|
| `ferramentas.ts` | entrada e roteamento da CLI |
| `biblioteca/` | configuração, execução, saída, catálogo e domínios compartilhados |
| `servidor/` | cobertura, arquitetura, contratos, testes e utilidades Java |
| `cliente/` | cobertura, arquitetura, resíduos e validações Vue |
| `codigo/` | auditorias transversais de código, nomes e Semgrep |
| `integracao/` | contratos OpenAPI |
| `requisitos/` | análise e auditoria de documentos CDU |
| `qualidade/` | coleta, fotografia, resumo e execução de tarefas |
| `projeto/` | ambiente, dependências, artefatos, versão e árvore de linhas |
| `testes/` | testes e fixtures próprios do toolkit |

O desenho desejado separa:

- núcleo horizontal, sem regras do SGC;
- adaptadores de Java/Gradle, Vue/npm, OpenAPI e ferramentas externas;
- perfil SGC, com seus defaults e políticas específicas.

## Comandos representativos

### Servidor e cliente

```bash
npx tsx toolkit/ferramentas.ts servidor cobertura auditoria --json
npx tsx toolkit/ferramentas.ts servidor testes analisar --json
npx tsx toolkit/ferramentas.ts servidor testes analisar --gravar --saida analise-testes.md
npx tsx toolkit/ferramentas.ts servidor testes priorizar --entrada analise-testes.json --gravar
npx tsx toolkit/ferramentas.ts servidor java corrigir-fqn
npx tsx toolkit/ferramentas.ts cliente cobertura auditoria --json
npx tsx toolkit/ferramentas.ts cliente residuos validar
npx tsx toolkit/ferramentas.ts cliente identificadores-teste listar-duplicados
```

`servidor java corrigir-fqn` simula por padrão; use `--gravar` para modificar fontes.

`servidor testes analisar` e `servidor testes priorizar` também são somente leitura por padrão. Use `--json` para alimentar
agentes e scripts pelo stdout; use `--gravar` para persistir relatórios.

Os relatórios próprios persistidos de `servidor arquitetura auditar` e `servidor coesao auditar` carregam `versao: 2` e
usam `pontosCriticos`. O JSON persistido pelo Semgrep continua sendo o formato externo da ferramenta e, por isso, não
recebe um envelope do toolkit.

O JSON persistido por `servidor testes analisar` usa `versao: 2`, campos em português/camelCase e categorias com os grupos
`comTeste` e `semTeste`. `servidor testes priorizar --json` emite outro contrato versionado, com `versao: 1` e a chave
`prioridades`; ao receber um relatório JSON incompatível, o comando falha antes de produzir uma priorização.

### Código e integração

```bash
npx tsx toolkit/ferramentas.ts codigo cheiros auditar --json
npx tsx toolkit/ferramentas.ts codigo semgrep auditar
npx tsx toolkit/ferramentas.ts codigo nomes auditar-consistencia
npx tsx toolkit/ferramentas.ts integracao contratos diff
```

Cheiros e Semgrep são complementares: o primeiro aplica heurísticas internas, enquanto o segundo executa regras
estruturais configuráveis.

`codigo nomes coletar-simbolos` produz um inventário `versao: 1`. As auditorias `codigo nomes auditar-consistencia` e
`codigo nomes auditar-idioma` reutilizam esse inventário e emitem resultados versionados (`auditar-idioma` em `versao: 2`); um inventário existente
com formato ou versão incompatível é rejeitado, enquanto a ausência do arquivo ainda permite uma coleta somente em
memória quando o comando não usa `--gravar`.

`codigo cheiros auditar` produz uma fotografia `versao: 3`; ela é reutilizada para calcular deltas na execução seguinte.
As chaves próprias da fotografia usam camelCase em português, e uma fotografia anterior incompatível é rejeitada em vez
de ser tratada como se não existisse.

`cliente arquitetura auditar` produz uma fotografia `versaoSchema: "4.0.0"`; seus resultados próprios usam
`pontosCriticos`, `pontuacao` e `pontuacaoTotal`. O formato anterior não é carregado nem traduzido.

`cliente arquitetura validar` é um gate do perfil SGC: além das regras locais do Dependency Cruiser, verifica a política
SGC que impede cálculos locais de ações baseados em estado de domínio. Ele não é uma auditoria horizontal sem adaptação.

`servidor cobertura auditoria` e `cliente cobertura auditoria` emitem resultados `versaoSchema: "1.0.0"` com
`pontosCriticos` e `pontuacaoImpacto`; os campos de cobertura dentro de `totais` continuam seguindo os formatos JaCoCo e
V8 lidos na fronteira.

O domínio JaCoCo não presume exclusões do SGC. Os comandos de cobertura do servidor passam explicitamente os padrões do
perfil SGC; consumidores externos podem usar `aplicarExclusoes` com seus próprios `padroesExclusao`.

Os três comandos de ramificações também emitem `versaoSchema: "1.0.0"` e `geradoEm`; seus campos de JaCoCo/V8 permanecem
nos nomes da fonte externa.

`qualidade resumo` lê fotografias `versaoSchema: "3.0.0"` produzidas pelo coletor, projeta apenas verificações e pontos
críticos atuais e rejeita fotografias incompatíveis. O coletor e os adaptadores usam `pontosCriticos` e `pontuacao` nos
contratos próprios; campos antigos não são aceitos.

OpenAPI mantém exportação, comparação e promoção de baseline. O toolkit não gera tipos TypeScript a partir do contrato.
O documento exportado preserva o vocabulário oficial da especificação; os resultados operacionais do toolkit usam campos
em português/camelCase (`quantidadeRotas`, `saidaPadrao`, `saidaErro`) e caminhos relativos informados por opção são
resolvidos a partir de `--base`.

### Casos de uso CDU

```bash
npx tsx toolkit/ferramentas.ts requisitos cdus inventariar
npx tsx toolkit/ferramentas.ts requisitos cdus inventariar --secoes vocabulario,mensagens
npx tsx toolkit/ferramentas.ts requisitos cdus auditar --json
npx tsx toolkit/ferramentas.ts requisitos cdus auditar --secoes estrutura,estilo,vocabulario,mensagens
npx tsx toolkit/ferramentas.ts requisitos cdus auditar --secoes mensagens-codigo
```

`inventariar` consolida formatos, vocabulário, mensagens, densidade e duplicações. `auditar` consolida estrutura, estilo,
vocabulário, mensagens e, quando as fontes canônicas estão disponíveis, a comparação com o código. Use `--secoes` para
executar apenas partes da análise; `todos` seleciona todas as seções. Os inventários continuam sendo diagnósticos
ocasionais, não gates automáticos.

O formato CDU é uma capacidade horizontal em evolução. O perfil atual do SGC usa `specs/cdu/cdu-*.md` e fornece seu
vocabulário, situações, mensagens e extratores de código. O parser e os motores de inventário/auditoria são independentes
da borda da CLI; políticas e fontes de código continuam sendo configuradas pelo projeto. Não há aliases de compatibilidade
para interfaces anteriores do toolkit.

### Qualidade e projeto

```bash
npx tsx toolkit/ferramentas.ts qualidade coletar --perfil rapido
npx tsx toolkit/ferramentas.ts qualidade tarefas executar rapido
npx tsx toolkit/ferramentas.ts qualidade resumo
npx tsx toolkit/ferramentas.ts projeto ambiente verificar
npx tsx toolkit/ferramentas.ts projeto dependencias auditar
npx tsx toolkit/ferramentas.ts projeto artefatos limpar
npx tsx toolkit/ferramentas.ts projeto versao-sincronizar 1.2.3
```

`qualidade coletar` executa os adaptadores e perfis do SGC e produz uma fotografia consolidada. Seu motor interno de
coleta não depende dos adaptadores SGC e pode ser composto programaticamente; a entrada CLI mantém somente os perfis SGC.
`qualidade tarefas executar` é a orquestração adaptável para outros projetos: apenas executa o perfil configurado em
`execucoes.qualidade`.

`projeto dependencias auditar` reúne uso e declarações pelo Knip, versões npm desatualizadas, vulnerabilidades npm e
atualizações Gradle. Achados são diferenciados de falhas de execução. A verificação Gradle usa `dependencyUpdates`,
`--no-parallel` e `-Drevision=release`; o plugin pode ainda exibir dependências declaradas sem versão explícita e plugins
ou plataformas do build, algo que precisa ser filtrado no próprio build consumidor, não pelo toolkit.

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

Crie `configuracao-toolkit.json` na raiz auditada. O schema atual exige `versao: 2` e rejeita chaves desconhecidas ou
caminhos vazios.

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

Diretórios reconhecidos:

- `servidor`, `cliente`, `codigoServidor`, `testesServidor`, `codigoCliente` e `testesIntegracao`;
- `artefatosQualidade`, `coberturaServidor`, `coberturaCliente` e `contratosOpenapi`;
- `regrasSemgrep`, `orcamentoResiduosCliente` e `excecoesResiduosCliente`.

O corpus CDU usa `specs/cdu/cdu-*.md` por padrão. Para outro layout, configure
`requisitos.cdus.padraoArquivos` com um glob relativo à raiz auditada. O parser, as auditorias estruturais, a densidade e
as duplicações continuam horizontais; políticas de vocabulário, estilo e comparação com mensagens do código ainda são
específicas do perfil configurado.

A seção `mensagens-codigo` usa sete fontes SGC por padrão. Um projeto pode substituí-las por fontes próprias, declarando
o caminho e o adaptador de cada arquivo; uma lista vazia desativa essa comparação:

```json
{
  "versao": 2,
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
adaptador de leitura. Prefixos, grupos, marcadores e convenções de mensagens podem ser substituídos em
`requisitos.cdus.politicaMensagensCodigo`; o default mantém as políticas do SGC.

O override é parcial e substitui listas inteiras quando informado. Por exemplo, um projeto pode trocar a convenção de
assuntos Java e as regras de classificação sem alterar o toolkit:

```json
{
  "versao": 2,
  "requisitos": {
    "cdus": {
      "politicaMensagensCodigo": {
        "regrasJava": [
          {"prefixo": "HIST_", "categoria": "descricao", "grupo": "historico_aplicacao"}
        ],
        "assuntosJava": {
          "prefixo": "APP: ",
          "grupo": "assunto_aplicacao",
          "marcadorSubprocesso": ":SIGLA_APLICACAO:",
          "marcadorFormatado": ":VALOR:",
          "sufixoSuperior": ""
        },
        "chavesMensagem": ["SOLICITACAO_CRIADA"],
        "categoriasChavesMensagem": ["toast", "mensagem"]
      }
    }
  }
}
```

`palavrasVazias`, `prefixosUiExcluidos` e os grupos de TypeScript também fazem parte da política. A comparação continua
somente leitura e a prova de um projeto externo com convenções próprias faz parte da suíte do toolkit.

Os valores controlados do CDU também podem ser substituídos em `requisitos.cdus.vocabulario` (`perfisCanonicos`,
`tiposProcessoCanonicos` e `arquivoSituacoesCanonicas`) e `requisitos.cdus.estilo.perfisEmCrases`. O default preserva os
perfis, tipos, situações e convenções tipográficas atuais do SGC.

O inventário de vocabulário identifica tipos de processo pelo contexto textual (`tipo`/`tipos`) e pela lista configurada;
ele não mantém uma lista fixa de tipos do SGC. Assim, um projeto externo pode executar os agregadores CDU com corpus,
perfis, tipos e situações próprios.

Execuções de dependências e qualidade também podem ser substituídas:

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

Opções explícitas da API ou CLI têm precedência sobre o arquivo. Categorias não configuradas usam os defaults do perfil
SGC. A configuração é considerada confiável e não funciona como sandbox.

A política Semgrep padrão vem do pacote; um override em `diretorios.regrasSemgrep` é resolvido contra a raiz auditada.
Orçamentos e exceções de resíduos são opcionais; quando configurados, os arquivos precisam existir, usar
`versaoSchema: "1.0.0"` e respeitar a estrutura da política. O carregamento e a validação dessas políticas ficam
separados do motor de análise.

## Uso como pacote

O pacote usa o modelo fonte + `tsx`. O smoke de distribuição instala o tarball em um consumidor temporário para impedir
dependências acidentais do workspace.

```bash
npm --prefix toolkit pack
npm install --save-dev ./ferramentas-projeto-0.1.0.tgz
npx ferramentas --help
```

As APIs programáticas públicas atuais são deliberadamente pequenas:

```ts
import {extrairCoberturaJacoco} from "ferramentas-projeto/cobertura-java";
import {extrairCoberturaCliente} from "ferramentas-projeto/cobertura-web";
import {auditarCasosDeUso, inventariarCasosDeUso} from "ferramentas-projeto/casos-de-uso";

const coberturaServidor = await extrairCoberturaJacoco("relatorios/jacoco.xml", {diretorioBase});
const coberturaCliente = await extrairCoberturaCliente("cliente/coverage/coverage-final.json", {diretorioBase});
const inventario = await inventariarCasosDeUso({base: diretorioBase, secoes: ["formatos"]});
const auditoria = await auditarCasosDeUso({base: diretorioBase, secoes: ["estrutura"]});
```

Módulos internos não fazem parte da API pública. O subpath `casos-de-uso` é a fachada horizontal dos dois motores CDU; ele
recebe a raiz do projeto e as seções desejadas, sem depender da CLI ou do workspace de desenvolvimento. O smoke usa um
projeto temporário instalado para validar esse contrato.

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

Os testes ficam em `toolkit/testes/` e são organizados por domínio. `testes/pacote.test.ts` é executado separadamente porque
instala o pacote em ambiente isolado. O build valida a compilação, mas a execução cotidiana continua usando TypeScript
diretamente.

## Limites

O toolkit complementa Gradle, npm, Playwright e ferramentas de análise; não substitui seus comandos nativos. Políticas
específicas do SGC devem continuar funcionando, mas precisam permanecer identificadas como perfil local quando não forem
universais.
