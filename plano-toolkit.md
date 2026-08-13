# Plano de refatoração e modernização do toolkit

## 1. Objetivo

Transformar `toolkit/` em uma ferramenta de auditoria e automação:

- executável diretamente a partir do código-fonte com `tsx`;
- escrita integralmente em TypeScript, sem manter duas implementações da mesma regra;
- modular o suficiente para ser reutilizada em outros projetos Java/Spring Boot/Vue;
- configurável por projeto, sem copiar regras do SGC para cada novo repositório;
- previsível para uso humano, CI, scripts npm e automações externas;
- conservadora sobre o código auditado: auditores devem diagnosticar por padrão e só gravar quando a ação for explícita.

O objetivo não é transformar cada regra específica do SGC em uma abstração artificial. A separação correta é entre um
núcleo horizontal, perfis/adaptadores de projeto e regras que pertencem exclusivamente ao SGC. Reuso externo é uma
ampliação do toolkit, não uma substituição do seu consumidor original: as funcionalidades específicas do SGC devem
continuar disponíveis e cobertas pelo perfil SGC.

### 1.1 Escopo

Este plano trata exclusivamente de `toolkit/`. Backend, frontend, requisitos e E2E do SGC aparecem somente como:

- entradas que o toolkit analisa;
- consumidores de comandos do toolkit;
- fixtures ou smoke tests necessários para provar um contrato do toolkit;
- origem das políticas que formarão o perfil SGC.

Refatorar o produto SGC, suas telas, APIs, regras de negócio ou testes não faz parte deste plano. Se uma auditoria do
toolkit encontrar um problema no SGC, o resultado esperado aqui é melhorar a precisão do diagnóstico, não corrigir o
sistema auditado.

## 2. Diretrizes permanentes

### 2.1 Linguagem e nomenclatura

- Código, comentários, mensagens e documentação em português brasileiro.
- Identificadores novos devem usar `codigo` em vez de `id` para chaves e referências.
- Diretórios e arquivos novos devem seguir a nomenclatura portuguesa já adotada pelo toolkit.
- Nomes externos inevitáveis — `OpenAPI`, `Vue`, `Gradle`, `tsx`, `JSON`, `Java` — permanecem como nomes técnicos.
- Não fazer uma renomeação massiva de uma vez. Quando um identificador público mudar, atualizar o roteador, testes e
  documentação na mesma rodada; não criar aliases apenas para preservar uma interface antiga sem cliente real.
- Não remover uma regra apenas por ela ser específica do SGC. Remoção exige evidência de obsolescência e ausência de
  consumidor; caso contrário, a regra deve permanecer no perfil SGC.

### 2.2 Fonte única e runtime

- TypeScript deve ser a fonte única da implementação.
- O alvo do fluxo normal é `npx tsx toolkit/sgc.ts` — ou o script npm equivalente — diretamente na árvore-fonte; o
  roteador já foi migrado para TypeScript nesta rodada.
- `toolkit/dist/` é somente artefato opcional para verificar compilação, distribuição ou smoke test de pacote; não é o
  fluxo de desenvolvimento.
- Não criar novos arquivos `.js` como cópias, wrappers permanentes ou shims para uma implementação `.ts`.
- O fallback transitório de `garantirArquivo` que tenta `.ts` quando o despachador pede `.js` deve desaparecer quando o
  roteador e todos os comandos tiverem sido convertidos para caminhos TypeScript consistentes.
- Node padrão do projeto: `26.7.0`, registrado em `.nvmrc` e nos `engines`. TypeScript 6 permanece a linha adotada;
  TS7 fica explicitamente fora do escopo até que o ecossistema usado pelo projeto seja compatível.
- ESM continua sendo o formato do toolkit.

### 2.3 Fronteiras de módulo

Todo comando reutilizável deve:

1. exportar uma função pública, normalmente `principal(argumentos)` ou uma função de domínio nomeada;
2. não iniciar auditorias, rede, Gradle, npm, Playwright ou escrita de arquivos durante o `import`;
3. detectar execução direta somente na borda `ehEntradaPrincipal`;
4. receber base, entrada, saída e opções por argumento ou configuração, não por caminho global escondido;
5. retornar um resultado estruturado quando houver consumidor programático;
6. reservar `process.exitCode` para a borda CLI;
7. manter mensagens humanas e saída JSON estável e mutuamente compreensíveis.

O roteador deve apenas registrar comandos, transportar argumentos e delegar. Regra de auditoria e acesso ao sistema de
arquivos pertencem ao comando ou a uma biblioteca de domínio, não ao roteador.

### 2.4 Auditorias e efeitos colaterais

- O estado final desejado é: auditorias são read-only por padrão.
- O comportamento atual ainda não cumpre integralmente essa regra: vários comandos gravam fotografias ou relatórios por
  padrão e alguns oferecem `--sem-gravar`. Como o SGC tem um único consumidor sob nosso controle, a migração pode inverter
  o default diretamente, atualizando o roteador, testes e documentação no mesmo recorte.
- Gravação de fotografia, baseline, relatório ou correção deve exigir uma opção explícita e um nome de ação claro.
- Não normalizar, renumerar ou reescrever documentos apenas durante a leitura.
- Saída JSON deve ir para stdout sem texto decorativo; logs operacionais e diagnóstico de falha devem ir para stderr.
- Falhas devem ter código de saída diferente de zero, mas não podem destruir o relatório estruturado que o CI precisa ler.
- Não mascarar contrato incorreto com fallback silencioso. Compatibilidade histórica só deve ser mantida quando existir
  um segundo consumidor identificado e testado.

### 2.5 Configuração

`configuracao-toolkit.json` continua sendo a configuração externa simples e editável. A configuração deve evoluir para
um contrato versionado, com validação e defaults explícitos. Os caminhos atuais são:

- `backend`;
- `frontend`;
- `backendCodigo`;
- `backendTestes`;
- `frontendCodigo`;
- `testesIntegracao`;
- `artefatosQualidade`;
- `coberturaBackend`;
- `coberturaFrontend`;
- `orcamentoResiduosFrontend`;
- `excecoesResiduosFrontend`;
- `regrasSemgrep`;
- `contratosOpenapi`.

Novos comandos devem preferir esses pontos de configuração ou opções explícitas. Caminhos fixos só são aceitáveis dentro
de um adaptador de perfil, nunca no núcleo horizontal.

Defaults dependentes da base não podem ser calculados no carregamento do módulo. Eles devem ser resolvidos depois de
interpretar `--base` e carregar a configuração daquele projeto. Isso vale especialmente para arquitetura e resíduos do
frontend e para os caminhos OpenAPI.

### 2.6 Compatibilidade e dependências

- O toolkit tem um único consumidor atual: este repositório, sob controle direto do projeto. Não há obrigação de manter
  aliases, formatos de configuração ou opções legadas; mudanças podem ser breaking quando todos os consumidores internos,
  testes e documentação forem atualizados no mesmo recorte.
- Uma mudança de pacote não é suficiente: executar testes unitários, typecheck, lint, Knip e os smoke tests do toolkit.
- `tsx` é runtime, não apenas ferramenta de desenvolvimento, enquanto a execução de fonte for o caminho oficial. A
  posição dele em `dependencies`/`devDependencies` deve ser corrigida quando o pacote do toolkit for tornado instalável
  fora do workspace.
- O binário declarado em `toolkit/package.json` usa um launcher mínimo que chama o CLI do `tsx` para executar `sgc.ts`;
  `tsx` agora é dependência de runtime do toolkit. A distribuição externa completa ainda depende de versão, `files` e
  separação da raiz do consumidor.
- Não atualizar dependências major sem uma matriz mínima de validação. A linha de TypeScript fica em TS6 por decisão
  explícita do projeto.

## 3. Situação atual — 12 de agosto de 2026

### 3.1 O que já foi feito

- Node atualizado para `26.7.0` com `.nvmrc` e `engines` no projeto raiz e no toolkit.
- Execução de fonte padronizada em `tsx`; scripts npm, documentação, `release-it` e ADR relevante já não usam `node`
  puro para comandos fonte.
- Build TypeScript criado e mantido como verificação opcional em `toolkit/dist/`.
- A configuração do Knip agora lista os entrypoints CLI reais do toolkit, inclui JavaScript e TypeScript como projetos e
  permite que o grafo encontre exports órfãos; a execução passou sem achados depois da remoção de oito exports internos
  não consumidos.
- O núcleo compartilhado foi convertido para TypeScript:
  - `lib/caminhos.ts`;
  - `lib/cli-ajuda.ts`;
  - `lib/cli-opcoes.ts`;
  - `lib/configuracao.ts`;
  - `lib/execucao.ts`;
  - `lib/logger.ts`;
  - `lib/qualidade.ts`;
  - `lib/saida.ts`.
- O roteador `sgc.ts` agora é a entrada fonte única; scripts raiz, scripts do frontend, ajuda, ADR, testes e coleta de
  qualidade foram atualizados para executá-lo por `tsx`.
- O `bin` do toolkit agora usa `bin/sgc.cjs` apenas como launcher para o CLI do `tsx`; o smoke do `npm exec` cobre o
  contrato sem transformar o launcher em uma segunda implementação.
- Dois comandos de projeto já foram convertidos:
  - `projeto/arvore-linhas.ts`;
  - `projeto/versao-sincronizar.ts`.
- `garantirArquivo` já encontra `.ts` na fonte e `.js` no build, permitindo migração incremental sem duplicar módulos.
- Exports de `toolkit/package.json` já distinguem módulos migrados e módulos ainda JavaScript.
- O corretor de FQN possui teste de escrita, conteúdo esperado sem duplicação e idempotência; a implementação não foi
  alterada porque a suspeita de duplicação não se confirmou.
- A auditoria de efeitos corrigiu um vazamento de `--sem-gravar` em `codigo nomes auditar-consistencia`: a geração
  automática do inventário auxiliar agora não grava nem polui o JSON final quando executada internamente.
- A configuração externa agora exige schema versão `1`, valida estrutura, nomes de diretório e caminhos não vazios na
  borda, antes de qualquer auditoria.
- Os defaults de saída de `frontend arquitetura auditar` e `frontend residuos auditar/validar` agora são resolvidos
  depois da base efetiva; uma base externa não volta a gravar esses artefatos na raiz do SGC.
- Os caminhos de exportação, diff e baseline OpenAPI agora são resolvidos depois da base efetiva, com `--base` nos três
  comandos e teste de projeto externo configurado.
- O coletor de qualidade agora aceita `--base`, grava a fotografia na base auditada, executa Gradle/npm e auditores
  internos nesse projeto e usa o `tsx` resolvido pelo runtime do toolkit.
- O Semgrep agora deriva seus alvos padrão de `backendCodigo` e `frontendCodigo` da configuração da base, em vez de
  assumir os diretórios do SGC.
- A auditoria de cheiros agora aplica os filtros de backend/frontend aos diretórios `backendCodigo` e `frontendCodigo`
  configurados, mantendo o diagnóstico em projetos com layout diferente.
- A auditoria de assuntos de notificação agora usa `backendCodigo` configurado, preservando as exceções específicas do
  perfil SGC sem fixar `backend/src/main/java/sgc` no código.
- A análise de testes backend agora resolve separadamente `backendCodigo` e `backendTestes` pela configuração da base;
  a opção histórica `--dir` continua significando a raiz do backend e deriva seus subdiretórios Java convencionais.
- A auditoria de identificadores de teste frontend agora usa `--base` como raiz do projeto, resolve `frontendCodigo` e
  reserva `--dir` para um diretório de busca explícito; o coletor de qualidade foi ajustado para esse contrato.
- Os validadores estruturais de views e modais frontend agora resolvem `frontendCodigo` pela base, preservando as
  políticas SGC de `LayoutPadrao`, cabeçalhos e `ModalPadrao` sem fixar `frontend/src`.
- O gate de arquitetura frontend e sua auditoria de ações agora resolvem o pacote Vue por `frontend` configurado,
  permitindo que o projeto auditado mova o pacote sem alterar o toolkit.
- A auditoria de resíduos frontend agora deriva o prefixo, as camadas e a coleta de arquivos de `frontendCodigo`,
  mantendo budgets, fotografia e classificação de produção/teste em layouts externos.
- O núcleo AST de arquitetura frontend agora deriva o mesmo contexto de `frontendCodigo` para coleta, camadas,
  imports, aliases `@/`, composables e famílias; os hubs e heurísticas específicos do SGC continuam explícitos.
- A leitura de cobertura V8 frontend agora normaliza caminhos em relação à base auditada, sem procurar o literal
  `frontend/src`; relatórios de layouts externos mantêm seus caminhos relativos.
- `lib/dominios/cobertura-java.ts` e `lib/dominios/cobertura-web.ts` foram convertidos para TypeScript com tipos
  explícitos para XML JaCoCo, dados V8, métricas, arquivos e opções; `@types/xml2js` documenta a dependência de
  parsing. Os dois domínios puros de cobertura agora têm entrada tipada e não fazem I/O durante a importação.
- `backend/lib/testes-analisar-regras.ts` foi convertido para TypeScript com tipos internos para categorias, perfis,
  correspondências, evidências e itens de relatório; as regras SGC e as chaves JSON permaneceram inalteradas.
- `requisitos/cdus-mensagens-lib.ts` foi convertido para TypeScript; os extratores Markdown e o mapa de contagens são
  agora tipados e continuam compartilhados pelos auditores e inventários de mensagens.
- `frontend/identificadores-teste-lib.ts` foi convertido para TypeScript com tipos para identificadores coletados e
  resultado da busca; o contrato de ausência de opções foi alinhado ao `lerOpcao` (`undefined`).
- `requisitos/cdus-lib.ts` foi convertido para TypeScript com tipos para opções, índices de seções, contagens e análise
  estrutural; o parser Markdown CDU continua preservando seus campos e diagnósticos atuais.
- `requisitos/cdus-vocabulario-lib.ts` foi convertido para TypeScript; `sugerirCanonico` aceita qualquer `Iterable` de
  candidatos, enquanto perfis, tipos de processo e o caminho de situações continuam declarados como perfil SGC.
- `requisitos/cdus-mensagens-codigo-lib.ts` foi convertido para TypeScript com tipos para mensagens extraídas,
  categorias, grupos, índice e sugestões; seus caminhos, prefixes e chaves canônicas continuam explicitamente no perfil
  SGC. A similaridade textual é um candidato separado para reutilização futura, não uma abstração inventada nesta etapa.
- `frontend/acoes-backend-lib.ts` foi convertido para TypeScript com tipos para ocorrências, violações, exceções e
  resultado da auditoria; o núcleo usa APIs `node:fs` diretamente e valida o JSON de exceções como `unknown`, enquanto
  as heurísticas de domínio continuam explicitamente específicas do SGC.
- `frontend/residuos-lib.ts` foi convertido para TypeScript com tipos para orçamento, camadas, contagens, arquivos,
  hotspots, fotografia e exceções; os budgets, pesos e classificações atuais foram preservados como política do perfil
  SGC, e o carregamento JSON passou a filtrar entradas de exceção inválidas.
- `frontend/arquitetura-lib.ts` foi convertido para TypeScript com tipos para análise AST, imports por camada, sinais,
  métricas, hotspots, famílias, exceções documentadas e fotografia; os hubs e heurísticas arquiteturais continuam
  explícitos no perfil SGC.
- A configuração já aceita alguns caminhos diferentes do layout do SGC; auditores de cobertura, arquitetura, coesão,
  contratos, resíduos e coleta possuem parametrização parcial por `--base`, `--arquivo`, `--saida` ou configuração.
  Arquitetura, resíduos, OpenAPI e coleta já resolvem seus defaults após a base; outros comandos ainda têm defaults
  globais ou caminhos `backend/src`/`frontend/src` fixos. Isso ainda não equivale a portabilidade.
- O gerador de tipos OpenAPI foi removido. O toolkit mantém somente exportação, comparação e fixação de fotografias de
  contrato; o Springdoc permanece no backend porque o ciclo E2E usa Swagger/OpenAPI para aguardar a aplicação.
- O histórico recente relevante está publicado na `main`, culminando, antes desta rodada, em `39036604d Parametriza
  artefatos OpenAPI pela base`.

### 3.2 Evidência de validação atual

Nas validações desta rodada, executadas diretamente sob Node `26.5.1` (Node 26 disponível no ambiente):

- `npm --prefix toolkit run test`: 94 testes aprovados em 2 arquivos;
- `npm --prefix toolkit run build`: aprovado;
- `npm --prefix toolkit run typecheck`: aprovado;
- `npm --prefix toolkit run lint`: aprovado;
- `npm --prefix toolkit run deps:audit`: aprovado;
- `npx knip --reporter compact` dentro de `toolkit/`: aprovado sem exports não consumidos;
- `git diff --check`: aprovado;
- importação dos comandos sem execução acidental: coberta pelos testes;
- execução fonte pelo script npm e smoke do artefato compilado: aprovados nas rodadas de migração;
- binário do workspace: aprovado. `npm exec --workspace toolkit sgc -- --help` usa `toolkit/bin/sgc.cjs`, chama `tsx` e
  executa `sgc.ts`; o mesmo contrato agora tem teste de integração no toolkit.

A contagem anterior caiu de 76 para 75 quando o teste do wrapper experimental `sgc-ts.js` foi removido junto com o
wrapper. Uma rodada voltou a 76 com escrita e idempotência do corretor FQN; outra chegou a 77 com o contrato
`--sem-gravar` da auditoria de nomenclatura; outra chegou a 79 com validação de configuração; outra chegou a 81 com
defaults de arquitetura e resíduos dependentes da base externa; outra chegou a 82 com os caminhos OpenAPI dependentes
da base externa; outra chegou a 83 com o coletor de qualidade dependente da base externa; esta chega a 84 com os alvos
configuráveis do Semgrep; outra chegou a 85 com filtros de cheiros dependentes da configuração; outra chegou a 86 com a
auditoria de assuntos dependente de `backendCodigo`; outra chegou a 87 com as raízes separadas da análise de testes backend;
esta chega a 88 com a separação entre raiz e diretório de busca dos identificadores de teste frontend; agora chega a 89
com os validadores estruturais dependentes de `frontendCodigo`; agora chega a 90 com o pacote Vue resolvido por
`frontend` configurado no gate arquitetural; outra chegou a 91 com a coleta de resíduos dependente de `frontendCodigo`;
outra chegou a 92 com o núcleo AST de arquitetura dependente de `frontendCodigo`; outra chegou a 93 com a normalização
de caminhos dos relatórios V8 frontend; esta chega a 94 com o launcher `tsx` do binário npm. Nenhuma dessas mudanças
reintroduz o wrapper obsoleto.

### 3.3 Tamanho e composição atual

Inventário dos arquivos rastreados do toolkit, excluindo `dist`, cobertura e artefatos ignorados:

- 10 arquivos TypeScript de implementação;
- 62 arquivos JavaScript de implementação ainda pendentes;
- 2 arquivos JavaScript de teste (`test/sgc.test.js` e `test/cdus.test.js`);
- 2 arquivos de teste concentrando 94 cenários;
- maior módulo atual: `frontend/arquitetura-lib.js`, com aproximadamente 1.000 linhas;
- outros hotspots: `codigo/nomes-simbolos-coletar.js`, `backend/testes-analisar.js`,
  `frontend/residuos-lib.js`, `backend/contratos-auditar.js` e `qualidade/coleta-execucao.js`.

O núcleo TypeScript está adiantado, mas a migração do toolkit como um todo ainda está no início: aproximadamente 14%
dos arquivos de implementação rastreados são TypeScript.

### 3.4 Achados da auditoria crítica

| Severidade | Achado | Evidência e impacto |
|---|---|---|
| Resolvido nesta rodada | Binário npm quebrado | `bin.sgc` agora aponta para `bin/sgc.cjs`, que chama o CLI do `tsx`; `npm exec --workspace toolkit sgc -- --help` passou. A distribuição externa completa continua pendente. |
| Bloqueador | Pacote não pode ser empacotado | `npm pack --workspace toolkit --dry-run` falha porque `toolkit/package.json` não possui `version`. `private: true`, nome específico e ausência de `files` também mostram que a distribuição externa ainda não foi desenhada. |
| Alta | Raiz acoplada à posição física | `lib/caminhos.ts` deriva a raiz como pai de `toolkit/`. Ao instalar o pacote em `node_modules`, a raiz calculada deixa de ser o projeto consumidor. |
| Resolvido | Configuração permissiva do Knip | A configuração anterior tratava praticamente todos os arquivos como entrypoints. A nova lista os comandos reais, inclui JS/TS e, nesta rodada, encontrou e removeu oito exports internos não consumidos. |
| Resolvido parcialmente | Base externa é parcialmente ignorada | Arquitetura, resíduos, OpenAPI, coleta, Semgrep, cheiros e assuntos de notificação agora respeitam a base/configuração; outros comandos ainda precisam da mesma correção. |
| Alta | Auditores gravam por padrão | Arquitetura, resíduos, cobertura, nomenclatura, Semgrep e diff OpenAPI têm escrita automática ou defaults distintos. A diretriz read-only ainda é meta, não realidade. |
| Resolvido | Cobertura insuficiente de mutação | O corretor `backend/java-corrigir-fqn.js` agora tem fixture de escrita, verificação de conteúdo sem duplicação e segunda execução idempotente. |
| Resolvido | Efeito colateral oculto de `--sem-gravar` | `codigo nomes auditar-consistencia` gerava o inventário auxiliar com gravação habilitada e contaminava `--json`; a opção agora é propagada e a coleta interna é silenciosa. |
| Resolvido | Configuração sem validação | `configuracao-toolkit.json` agora exige a versão `1` e valida estrutura, chaves conhecidas e caminhos textuais antes da combinação com defaults. |
| Média | Opções e efeitos divergentes | Há `--input`/`--output` e `--entrada`/`--saida`, `--dry-run` e `--sem-gravar`, além de comandos mutáveis sem modo de prévia uniforme. |
| Média | Testes não representam pacote externo | Os 94 testes rodam dentro do monorepo e encontram dependências hoisted. Não existe instalação em diretório isolado nem teste de raiz do consumidor. |
| Média | Cobertura funcional não medida | `@vitest/coverage-v8` está instalado, mas não há script, threshold ou relatório de cobertura do próprio toolkit. Quantidade de testes não mede contratos não exercitados. |
| Média | Roteador monolítico e inventário duplicado | `sgc.ts` registra todos os comandos e a documentação repete a lista manualmente; é fácil haver deriva de nomes, extensões e ajuda. |
| Baixa | APIs nativas e dependências se sobrepõem | Parte do núcleo já substituiu `fs-extra` por Node nativo; a migração deve reavaliar dependências por uso real, sem remoção antecipada. |

### 3.5 Interpretação correta do estado

- O toolkit funciona como ferramenta interna executada pelo script npm ou por `npx tsx` dentro deste workspace.
- O toolkit ainda **não** funciona como pacote CLI externo completo, apesar de declarar `bin` e `exports`: não possui
  `version`, continua `private` e ainda não tem teste de `npm pack` em instalação isolada.
- `exports` aponta alguns subpaths diretamente para `.ts`; esse contrato serve ao workspace com loader, mas não é um
  contrato consumível por Node puro e precisa acompanhar a decisão fonte versus pacote compilado.
- Parametrização parcial não significa generalização concluída.
- Build aprovado prova que a árvore pode ser emitida; não prova que o pacote emitido contém assets, configuração e
  resolução de raiz adequados para distribuição.
- Knip aprovado e 94 testes verdes são gates úteis, mas ainda insuficientes para afirmar ausência de código morto fora
  do grafo declarado, segurança de todos os comandos mutáveis ou portabilidade. O grafo do Knip agora é uma evidência
  útil de exports não consumidos; não substitui testes de pacote externo.

### 3.6 Inventário de efeitos colaterais

O inventário abaixo foi levantado por inspeção dos comandos registrados e das bibliotecas chamadas por eles. Ele descreve
o comportamento atual; não transforma automaticamente todo comando que gera relatório em auditoria read-only.

| Classe atual | Comandos ou famílias | Efeito observado e controle existente |
|---|---|---|
| Auditoria read-only | `requisitos cdus *`, `backend cobertura ramificacoes`, `frontend cobertura ramificacoes`, `frontend arquitetura validar`, `frontend modais validar`, `frontend views templates-validar`, `frontend identificadores-teste *`, `projeto diagnostico` | Leem código/relatórios e escrevem somente stdout/JSON; não criam artefatos próprios. Dependências externas podem fazer leitura adicional. |
| Auditoria com geração por padrão | `backend arquitetura/coesao/contratos auditar`, `frontend arquitetura auditar`, `frontend residuos auditar/validar`, `codigo cheiros auditar`, `codigo nomes coletar-simbolos/auditar-consistencia/auditar-idioma`, `codigo semgrep auditar` | Gravem fotografias ou relatórios em caminhos padrão. A maioria oferece `--sem-gravar`, mas a ausência da opção ainda permite mutação de artefatos. |
| Geração de relatório indicado | `backend cobertura auditoria`, `frontend cobertura auditoria`, `backend testes analisar/priorizar`, `frontend acessibilidade processar` | Criam arquivos Markdown/JSON definidos por `--output`, `--output-json` ou defaults. São geradores explícitos, não auditores read-only. |
| Artefato de contrato | `integracao contratos exportar-openapi`, `integracao contratos diff` | Exportação grava OpenAPI; diff grava resumo Markdown por padrão. `--sem-gravar` existe no diff, mas não é o default. |
| Mutação de fonte ou baseline | `backend java corrigir-fqn`, `projeto versao-sincronizar`, `integracao contratos fixar-baseline` | Alteram código/configuração ou promovem arquivo. FQN usa `--dry-run` opt-in; versão não possui prévia; baseline copia diretamente para o destino. |
| Limpeza confirmada | `projeto limpar` | Lista em prévia por padrão e remove somente com `--confirmar`; é o modelo de confirmação explícita a preservar. |
| Orquestração externa | `qualidade coletar`, `projeto qualidade`, `projeto dependencias auditar`, `projeto preparar`, `frontend acessibilidade crawler` | Executam Gradle, npm, Playwright, Semgrep ou Git e podem criar artefatos fora da biblioteca do toolkit. Precisam de perfil/ação explícita e limites de raiz. |

Achado corrigido nesta rodada: quando `codigo nomes auditar-consistencia --sem-gravar` precisava gerar um inventário
auxiliar ausente, ele chamava a coleta com gravação habilitada. A propagação de `semGravar` e a opção interna
`silencioso` preservam o contrato de leitura e mantêm o stdout JSON válido.

## 4. Classificação para reuso externo

### 4.1 Horizontal com pouca adaptação

Estes componentes podem formar o pacote reutilizável:

- resolução de raiz, configuração, parsing de opções e saída;
- execução de comandos, logging e detecção de entrada principal;
- fotografia/resumo de qualidade quando o schema for formalizado;
- contagem de linhas e operações sobre um diretório Git;
- leitura de JaCoCo e de cobertura V8, desde que caminhos e schema de entrada sejam parâmetros;
- comparação de fotografias JSON e geração de relatórios determinísticos;
- exportação/diff/baseline de OpenAPI, desde que URL e diretórios de artefatos sejam configuráveis;
- primitivas para detectar resíduos, dependências, convenções e violações estruturais.

### 4.2 Horizontal com adaptador de projeto

Estas capacidades são úteis em projetos Spring Boot/Vue, mas hoje carregam decisões do SGC:

- auditoria de arquitetura Vue: nomes de stores/composables/services, views e estratégias de cache;
- auditoria de resíduos e modais BootstrapVueNext;
- auditorias de cobertura e priorização de testes Java/Gradle;
- coleta de qualidade que coordena Gradle, npm, Playwright e auditores;
- Semgrep, regras de nomenclatura e contratos OpenAPI;
- diagnóstico de ambiente e preparação do projeto.

Elas devem receber um perfil/adaptador com:

- diretórios e globs;
- tarefas de build/teste;
- convenções de camadas;
- nomes de componentes permitidos;
- schema de fotografias;
- comandos opcionais disponíveis;
- políticas de exceção.

### 4.3 Específico do SGC

Não promover diretamente ao núcleo horizontal:

- `backend/src/main/java/sgc` e o pacote Java `sgc`;
- `AssuntosNotificacao`, `Mensagens.java` e a lista de constantes de mensagens do SGC;
- regras de nomenclatura `codigo` e auditoria de inglês próprias do SGC;
- estrutura `specs/cdu-*.md`, vocabulário e convenções de CDU;
- arquivos concretos de constantes e stores do frontend do SGC;
- `ModalPadrao`, `LayoutPadrao`, `PageHeader` e exceções específicas de views do SGC;
- tarefas Gradle e perfis E2E fixos do SGC;
- portas padrão, `swagger-ui.html`, `/api-docs` e diretórios de artefatos sem configuração;
- arquivos de política e exceção mantidos em `toolkit/qualidade/`.

Essas regras continuam valiosas no perfil SGC. O trabalho correto é mover a regra para uma política/adaptador explícito,
não apagá-la nem espalhá-la em condicionais genéricas.

### 4.4 Contrato de preservação do perfil SGC

A extração do núcleo horizontal deve preservar o comportamento observável do toolkit no SGC:

- comandos, opções, códigos de saída e formatos consumidos por scripts existentes;
- políticas de nomenclatura, CDU, acesso e arquitetura que sejam intencionais;
- integração com Gradle, npm, Playwright, OpenAPI e diretórios de artefatos do projeto;
- diagnósticos e exceções já documentados pelo perfil;
- capacidade de executar o catálogo específico do SGC a partir do mesmo entrypoint, ainda que organizado em namespace
  ou adaptador explícito.

Compatibilidade não significa congelar defeitos. Correções de segurança, falsos positivos, efeitos colaterais indevidos
e contratos inconsistentes devem ser feitas com teste que registre a mudança intencional. Antes de mover uma regra para
o núcleo, comparar resultados sobre fixtures representativas do SGC; depois da extração, manter esse teste como regressão
do perfil.

## 5. Lacunas e riscos conhecidos

### Prioridade alta

1. **Efeitos colaterais**: inverter gradualmente defaults de auditorias geradoras para read-only e exigir ação explícita
   para persistir artefatos, usando o inventário da seção 3.6 e atualizando o único consumidor interno na mesma rodada.
2. **Entry point e pacote**: o roteador agora é `sgc.ts`, o launcher do pacote executa `tsx` e os smoke de workspace
   passam; ainda faltam o desenho completo de distribuição e o teste isolado de pacote.
3. **Raiz do consumidor**: separar diretório de instalação do toolkit, diretório de trabalho e raiz do projeto auditado.
   `process.cwd()`, `--base` e configuração explícita devem ter precedência documentada.
4. **Modelo de distribuição**: decidir cedo se o reuso será por pacote compilado, pacote-fonte com runtime `tsx` ou cópia
   vendorizada. A escolha define `version`, `private`, nome, `files`, `bin`, `exports`, assets e dependências de runtime.
5. **TypeScript sem rigor uniforme**: `tsconfig.nucleo.json` cobre o núcleo, mas o `tsconfig.json` geral mantém
   `checkJs: false` e não impõe `strict` aos próximos comandos TS. Criar uma configuração estrita por etapas, sem tentar
   tipar os 62 módulos JavaScript de uma vez.
6. **Dependência de runtime**: `tsx` já foi movido para `dependencies` e o launcher de pacote foi criado; ainda falta
   decidir se o pacote final continuará fonte+tsx ou será compilado para distribuição.
7. **Contrato `.js`/`.ts` temporário**: o fallback do despachador é útil durante a transição, mas aumenta a superfície e
   pode esconder caminhos inválidos. Medir os consumidores e removê-lo ao fim da conversão.
8. **Hard-coding de perfil**: várias regras continuam presas ao layout e ao vocabulário SGC. Antes de declarar o toolkit
   reutilizável, separar engine, política e adaptador.

### Prioridade média

9. **Testes concentrados**: dois arquivos de teste grandes em JavaScript dificultam localizar contratos e impedem que os
   tipos dos testes ajudem na migração. Dividir por domínio e converter para TypeScript depois de estabilizar os comandos.
10. **Schema de resultados**: fotografias, auditorias, cobertura, diagnósticos e relatórios usam objetos sem schema
   versionado. Formalizar tipos e versões de saída antes de extrair o pacote externo.
11. **Opções inconsistentes**: há mistura de `--input`, `--output`, `--dir`, `--arquivo`, `--saida` e defaults locais.
   Definir opções canônicas em português e remover formas antigas diretamente, atualizando o catálogo e os testes.
12. **Documentação derivada**: o catálogo já foi atualizado para `sgc.ts`, mas ainda precisa ser centralizado para não
    derivar ajuda, comandos e exports em fontes duplicadas. O inventário de comandos não deve divergir do roteador.
13. **Orquestração pesada**: `qualidade/coleta-execucao.js` mistura subprocessos, Gradle, npm, Playwright, parsing de
    relatórios e schema da fotografia. Separar executor, adaptadores de ferramenta e agregador.

### Prioridade baixa

14. **Artefatos e limpeza**: revisar políticas, arquivos ignorados e nomes de `mais-recente`/`execucoes` para evitar que
    saídas locais sejam confundidas com recursos do pacote.
15. **Performance**: medir antes de otimizar. A coleta e os auditores só devem ser otimizados por gargalo observado, com
    comparação antes/depois e sem sacrificar a legibilidade do relatório.

## 6. Próximos passos ordenados

Cada item abaixo deve ser uma rodada pequena, validada e publicada antes do próximo. A ordem privilegia redução de risco,
reuso externo e preservação de contratos.

### Fase 0 — estabilizar os contratos existentes

1. **[concluído]** Criar teste que execute o modo de escrita de `backend/java-corrigir-fqn.js` sobre fixture
   temporária, confirme o conteúdo esperado sem linhas duplicadas e repita a execução para verificar idempotência; não
   houve alteração da implementação porque não existia falha reproduzível.
2. **[concluído nesta rodada]** Inventariar todos os comandos que escrevem, removem ou promovem arquivos e classificá-los como:
   - auditoria read-only;
   - geração explícita de artefato;
   - manutenção mutável;
   - orquestração externa.
3. Não converter um comando mutável antes de haver teste de efeito, prévia e idempotência quando aplicável.
4. **[concluído]** Corrigir `toolkit/knip.json`: incluir `js` e `ts`, declarar somente entrypoints reais e
   permitir que o grafo encontre módulos/exports não usados; oito exports internos órfãos foram removidos.
5. **[concluído nesta rodada]** Adicionar testes focados para configuração inválida e combinação de defaults. Arquitetura,
   resíduos, OpenAPI, coleta, Semgrep, cheiros, assuntos de notificação e análise de testes backend agora calculam seus
   caminhos após a resolução de `--base`; outros defaults import-time continuam pendentes.
6. Registrar uma baseline de cobertura do próprio toolkit, inicialmente informativa; definir thresholds apenas depois de
   identificar quais contratos críticos ainda não têm teste.

Situação: itens 1, 2, 4 e 5 concluídos; itens 3 e 6 continuam pendentes.

Critério de aceite: comandos mutáveis conhecidos têm testes de efeito, Knip consegue revelar código órfão real e a
configuração externa possui testes de precedência e erro.

### Fase A — fechar a fronteira TypeScript do runtime

1. **[concluído nesta rodada]** Migrar `toolkit/sgc.js` para `toolkit/sgc.ts`.
2. Tipar integralmente o registro de comandos do Commander, opções e resultado de `principal`.
3. **[concluído nesta rodada]** Atualizar scripts raiz, script do toolkit, README, ADRs, testes e referências internas
   para o novo entrypoint.
4. Separar explicitamente três caminhos: diretório de instalação do toolkit, diretório atual e raiz do projeto auditado.
5. **[concluído nesta rodada]** Corrigir o `bin` do pacote com launcher que chama `tsx` e testar a execução por `npm exec`,
   `npx tsx` e `npm --prefix toolkit run sgc`.
6. Decidir e implementar o modelo de instalação externo: manter fonte+`tsx` como dependência ou adotar distribuição compilada.
7. Criar teste de pacote isolado com `npm pack`, instalação em diretório temporário e projeto consumidor fixture.
8. Remover o fallback `.js` → `.ts` do despachador somente depois de todos os comandos registrados terem extensões e
   imports consistentes.

Critério de aceite: nenhum comando fonte depender de `node` puro, nenhum binário apontar para um caminho quebrado e o
roteador fonte/compilado possuir testes de smoke equivalentes.

### Fase B — converter bibliotecas puras e contratos de dados

1. **[concluído nesta rodada]** Migrar `lib/dominios/cobertura-java.ts` e `lib/dominios/cobertura-web.ts`, mantendo os
   contratos de métricas, os caminhos relativos à base auditada e os fixtures existentes.
2. **[parcial nesta rodada]** Migrar `backend/lib/testes-analisar-regras.ts`, `requisitos/cdus-mensagens-lib.ts`,
   `frontend/identificadores-teste-lib.ts`, `requisitos/cdus-lib.ts`, `requisitos/cdus-vocabulario-lib.ts`,
   `requisitos/cdus-mensagens-codigo-lib.ts`, `frontend/acoes-backend-lib.ts`, `frontend/residuos-lib.ts` e
   `frontend/arquitetura-lib.ts`; ainda faltam fotografia de qualidade, bibliotecas de diagnóstico e comandos maiores.
3. **[parcial nesta rodada]** Introduzir tipos para JaCoCo, V8, regras da análise de testes backend, mensagens CDU,
   violações de ações, resíduos, arquitetura AST, fotografias e exceções; ainda faltam achados de auditoria e
   diagnósticos.
4. Substituir `any` implícito por `unknown` na entrada JSON e validar apenas o que o consumidor realmente exige.
5. Criar `tsconfig.toolkit-estrito.json` ou equivalente com `strict`, aplicando-o aos módulos já convertidos e aos
   próximos lotes.
6. Eliminar defaults de caminho calculados durante import; expor funções que resolvam caminhos a partir da base e da
   configuração efetivas.

Critério de aceite: as bibliotecas convertidas não fazem I/O durante import, têm tipos públicos documentados e mantêm os
mesmos fixtures e resultados JSON.

### Fase C — converter comandos por reuso, não por diretório

Lotes sugeridos:

1. **Projeto**: diagnóstico, limpeza, preparação e perfil de qualidade; separar o que é genérico do perfil SGC sem
   alterar o backend/frontend auditado.
2. **Backend**: cobertura, análise/priorização de testes, contratos e FQN; parametrizar raiz Java, tarefas Gradle e
   categorias.
3. **Frontend**: cobertura V8, resíduos, acessibilidade e identificadores de teste; parametrizar raiz Vue, globs e
   convenções de componentes.
4. **Integração**: exportação, diff e baseline OpenAPI; manter o módulo independente do gerador de tipos removido.
5. **Requisitos**: converter o motor de Markdown e depois isolar o perfil CDU do SGC.
6. **Código transversal**: converter cheiros, Semgrep e inventários de nomes/idioma por último, pois concentram mais
   políticas locais e maior volume de parsing.

O corretor FQN só entra nesta fase depois do bloqueador de segurança da Fase 0 estar corrigido e publicado.

Para cada comando convertido:

- definir tipos das opções e do resultado;
- preservar `principal(argumentos)` e importação sem efeito colateral;
- trocar mensagens e exemplos para `npx tsx`;
- atualizar o registro do roteador e o teste de importação;
- mover caminhos específicos para configuração/política, quando a regra tiver potencial horizontal;
- remover o `.js` somente quando todos os consumidores forem atualizados.

### Fase D — separar núcleo horizontal e perfil SGC

1. Definir uma configuração de projeto versionada, mantendo JSON como formato oficial de entrada e validando-a na borda.
2. Criar uma camada de adaptadores para:
   - layout de backend Java/Spring/Gradle;
   - layout de frontend Vue;
   - contratos OpenAPI;
   - coleta de qualidade;
   - políticas de nomenclatura, CDU, modais e arquitetura.
3. Fazer o núcleo receber adaptadores por composição, sem `if (projeto === "sgc")` espalhado.
4. Criar um projeto fixture externo mínimo com backend/frontend fictícios e executar os comandos horizontais contra ele.
5. Documentar claramente quais comandos são `núcleo`, `perfil-sgc` ou `opcionais`.
6. Mover políticas do SGC para um diretório de perfil explícito somente quando o motor correspondente estiver estável;
   não reorganizar todos os arquivos antecipadamente.
7. Criar testes de caracterização para cada funcionalidade específica antes de separar seu motor horizontal; preservar
   o resultado no perfil SGC ou registrar explicitamente a correção de comportamento.

Critério de aceite: um segundo projeto consegue configurar raiz, globs, tarefas e políticas sem editar o código do
núcleo; as regras CDU e `AssuntosNotificacao` não aparecem nesse projeto fictício e continuam funcionando quando o perfil
SGC está ativo.

### Fase E — padronizar CLI e resultados

1. Inventariar todas as opções, defaults, mensagens e códigos de saída.
2. Definir opções canônicas em português (`--entrada`, `--saida`, `--diretorio`, `--arquivo`, `--base`) e remover as
   formas antigas que não tenham valor semântico, atualizando todos os usos internos.
3. Definir um envelope comum de resultado: versão do schema, status, resumo, violações, métricas, artefatos e avisos.
4. Separar stdout estruturado, stdout humano e stderr operacional.
5. Definir quando um comando retorna falha por violação encontrada versus erro de execução.
6. Adicionar `--json`/`--sem-gravar` de forma consistente, sem inventar opções para comandos que não precisam delas.
7. Substituir gradualmente `--sem-gravar` por execução read-only padrão e uma opção positiva de persistência; remover
   `--sem-gravar` quando cada comando tiver sido migrado e seus testes atualizados.
8. Separar no catálogo da CLI comandos de auditoria, geração, manutenção e orquestração para tornar efeitos explícitos.

### Fase F — testes, documentação e distribuição

1. Converter `test/sgc.test.js` e `test/cdus.test.js` para TypeScript após a estabilização das interfaces.
2. Dividir testes por domínio: runtime, configuração, saída, projeto, backend, frontend, integração e requisitos.
3. Manter testes comportamentais sobre a API pública; não testar métodos privados por reflexão ou acoplamento à
   implementação.
4. Adicionar smoke test de instalação em diretório externo, incluindo `npx tsx` e o binário do pacote.
5. Adicionar matriz de validação para Node `26.7+`, TypeScript 6 e as versões de Vitest/tsx usadas no workspace.
6. Criar fixtures próprias do toolkit para Java/Spring, Vue e Markdown; não usar a suíte do produto SGC como validação
   rotineira da modernização do toolkit.
7. Executar smoke tests sobre um recorte do SGC apenas quando necessário para provar que uma funcionalidade específica
   do perfil continua funcionando após a mudança do toolkit.
8. Atualizar `toolkit/README.md` e exemplos a partir de uma fonte única de comandos.
9. Fechar o modelo de distribuição e retirar arquivos JS, aliases e fallbacks de transição.

## 7. Validação obrigatória por rodada

### Rodada focada

```bash
node --version
cd toolkit
npm run typecheck:nucleo
npx vitest run test/sgc.test.js test/cdus.test.js --reporter=dot --no-color
npm run build
cd ..
git diff --check
```

O comando focado deve apontar para os testes do módulo alterado assim que a suíte for dividida. Enquanto os dois arquivos
monolíticos existirem, a rodada acima é o mínimo seguro.

### Rodada completa do toolkit

```bash
cd toolkit
npm run test
npm run typecheck
npm run lint
npm run deps:audit
npm run build
cd ..
```

### Verificações de integração de execução

```bash
npx tsx toolkit/sgc.ts --help
npx tsx toolkit/sgc.ts projeto arvore-linhas --help
npm --prefix toolkit run sgc -- --help
npm exec --workspace toolkit sgc -- --help
```

O último comando está atualmente reprovado e é critério da Fase A; não deve ser tratado como gate verde antes da correção
do `bin`.

### Verificações adicionais por classe de mudança

- **Configuração/caminhos**: executar contra projeto fixture fora do repositório e verificar que nenhum artefato foi lido
  ou gravado na raiz do SGC.
- **Auditoria**: comparar JSON e código de saída com fixture estável; confirmar ausência de escrita sem opção explícita.
- **Comando mutável**: testar prévia, execução, idempotência quando aplicável e preservação de conteúdo não relacionado.
- **Empacotamento**: usar `npm pack`, instalar o tarball em diretório temporário e executar o binário sem dependências
  hoisted do monorepo.
- **Build**: executar ao menos um subcomando compilado que importe biblioteca e um que despache outro script; `--help` da
  raiz isoladamente não prova o pacote.

Depois de qualquer renomeação, procurar referências antigas fora de `dist` e `node_modules`:

```bash
rg -n 'node toolkit|sgc-ts|node --import=tsx|arquivo\.js removido' \
  --glob '!node_modules/**' --glob '!toolkit/dist/**'
```

Cada recorte deve terminar com um commit pequeno e push para `main`, conforme o fluxo adotado nesta modernização.

## 8. Decisões que não devem ser reabertas sem evidência

- Não reintroduzir o gerador de tipos OpenAPI enquanto ele exigir uma versão antiga de TypeScript e não houver consumidor
  real dos tipos gerados.
- Não tornar `dist` o caminho principal apenas porque o build existe.
- Não manter uma implementação JavaScript paralela para facilitar uma migração parcial.
- Não generalizar regras do SGC com dezenas de flags antes de existir um segundo consumidor real.
- O projeto fixture externo é prova de portabilidade técnica, não evidência suficiente para criar abstrações de negócio.
- Não transformar auditores read-only em formatadores ou corretores automáticos silenciosos.
- Não trocar TS6 por TS7 antes da compatibilidade do conjunto Node/Vitest/Vue/tsx/ESLint estar comprovada.
- Não otimizar coleta e auditorias sem medição de um cenário monitorado e comparação antes/depois.

## 9. Definição de concluído

A modernização do toolkit estará concluída quando:

- todo o código de implementação e testes estiver em TypeScript;
- o entrypoint, o binário, o `npx tsx`, os scripts npm e o build tiverem o mesmo contrato;
- o binário passar em instalação isolada, sem depender do hoisting do workspace;
- a raiz do projeto auditado for explícita e independente do local de instalação do toolkit;
- não houver fallback de extensão ou wrapper de transição sem justificativa;
- o núcleo horizontal estiver separado do perfil SGC;
- um segundo projeto fixture executar os comandos horizontais apenas por configuração;
- o perfil SGC preservar seu catálogo específico, com testes de caracterização e regressão;
- schemas de saída e códigos de retorno estiverem documentados e testados;
- os auditores mantiverem comportamento read-only por padrão;
- comandos mutáveis tiverem testes de efeito e prévia segura;
- Knip analisar um grafo de entrypoints reais e a cobertura do toolkit possuir baseline conhecida;
- as suítes e fixtures próprias do toolkit passarem sob Node 26.7+;
- o pacote puder ser instalado ou copiado para outro projeto sem depender de caminhos ou `node_modules` implícitos do SGC.
