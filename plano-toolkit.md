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
- O despachador recebe exclusivamente caminhos TypeScript registrados e sempre executa a fonte `.ts` pelo `tsx`; um
  `sgc.js` compilado pode ser usado apenas no smoke opcional, sem fallback para uma segunda implementação em `dist`.
- Node padrão do projeto: `26.7.0`, registrado em `.nvmrc` e nos `engines`. TypeScript 6 permanece a linha adotada;
  TS7 fica explicitamente fora do escopo até que o ecossistema usado pelo projeto seja compatível. O alvo TypeScript
  foi elevado para ES2025, coerente com o runtime mínimo e com a ausência deliberada de compatibilidade legada.
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
- O comportamento atual ainda não cumpre integralmente essa regra: cobertura e diff OpenAPI ainda têm contratos de
  geração que precisam ser uniformizados. As famílias já migradas usam `--gravar` como ação positiva; como o SGC tem um
  único consumidor sob nosso controle, a migração pode inverter o default diretamente, atualizando testes e documentação
  no mesmo recorte.
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
- `regrasSemgrep`;
- `contratosOpenapi`.

`orcamentoResiduosFrontend` e `excecoesResiduosFrontend` são nomes opcionais de override para políticas de resíduos;
não possuem default SGC nem arquivo empacotado. Na ausência de override, o analisador usa uma política neutra explícita.
Se um override for declarado, arquivo ausente ou JSON inválido interrompe a execução em vez de ser tratado como política
vazia.

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
- `tsx` é runtime, não apenas ferramenta de desenvolvimento, enquanto a execução de fonte for o caminho oficial. Ele já
  está em `dependencies` do pacote do toolkit.
- O binário declarado em `toolkit/package.json` usa um launcher mínimo que chama o CLI do `tsx` para executar `sgc.ts`;
  a distribuição externa usa fonte + `tsx`, com `version`, `files` e separação da raiz do consumidor validados por smoke.
- Não atualizar dependências major sem uma matriz mínima de validação. A linha de TypeScript fica em TS6 por decisão
  explícita do projeto.

## 3. Situação atual — 13 de agosto de 2026

### 3.1 O que já foi feito

- A política do projeto declara Node `26.7.0` em `.nvmrc`, `engines` e no diagnóstico do toolkit; esta rodada foi
  validada diretamente com o runtime `v26.7.0` instalado no ambiente, sem `EBADENGINE`.
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
- O pacote `sgc-scripts@0.1.0` agora declara `files`, `exports`, o binário e os assets de política necessários ao modelo
  fonte + `tsx`; `npm pack` e a instalação do tarball em consumidor isolado passaram.
- `lib/caminhos.ts` separa o diretório físico de instalação (`DIRETORIO_TOOLKIT`) da raiz padrão (`process.cwd()`),
  enquanto `--base` continua sendo a forma explícita de auditar outra raiz.
- `fs-extra` deixou de ser dependência de runtime: o código distribuído usa APIs nativas do Node, e a biblioteca ficou
  somente em `devDependencies` para os testes, com `@types/fs-extra` acompanhando o consumo TypeScript.
- A política Semgrep padrão e o executável Semgrep agora são resolvidos de forma portável: a primeira vem da instalação
  física do pacote e o segundo vem do `PATH`, com override de regra pela configuração do projeto.
- Dois comandos de projeto já foram convertidos:
  - `projeto/arvore-linhas.ts`;
  - `projeto/versao-sincronizar.ts`.
- `projeto/versao-sincronizar.ts` agora calcula pendências sem gravar por padrão, aceita `--base`, aplica alterações
  somente com `--gravar`, resolve o manifesto pelo diretório `frontend` configurado e evita reescrita quando a versão já
  está sincronizada; o teste externo confirma o layout `cliente/package.json`.
- `garantirArquivo` resolve somente entradas `.ts` na árvore-fonte física do toolkit; o despachador não mantém fallback
  para `.js` compilado nem aliases de comandos antigos.
- Exports de `toolkit/package.json` já expõem a árvore TypeScript do toolkit; todos os testes agora são TypeScript
  estrito, incluindo a CLI grande e o smoke de pacote.
- O corretor de FQN possui teste de escrita, conteúdo esperado sem duplicação e idempotência; agora resolve
  `backendCodigo` e `backendTestes` quando a base possui configuração, preservando a descoberta convencional para uma
  base backend isolada.
- A auditoria de efeitos corrigiu um vazamento de gravação em `codigo nomes auditar-consistencia`: a geração automática do
  inventário auxiliar agora acompanha `--gravar` e não grava nem polui o JSON final na execução padrão.
- A configuração externa agora exige schema versão `1`, valida estrutura, nomes de diretório e caminhos não vazios na
  borda, antes de qualquer auditoria; o tipo TypeScript dos diretórios agora usa a mesma união de nomes conhecidos do
  schema, em vez de `Record<string, string>` permissivo.
- A configuração externa também aceita a seção opcional `execucoes`, com perfis de qualidade, escopos de auditoria de
  dependências e escopos de instalação; cada categoria pode ser substituída separadamente, enquanto categorias ausentes
  continuam usando os defaults do perfil SGC. Opções explícitas da API/CLI têm precedência sobre o arquivo.
- Os defaults obsoletos de orçamento e exceções de resíduos frontend foram removidos: os dois nomes continuam aceitos
  somente como overrides opcionais da configuração. Sem override, a política neutra fica explícita no resultado; com
  override, arquivo ausente ou inválido gera erro visível.
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
  `--diretorio` significa a raiz do backend quando informado e deriva seus subdiretórios Java convencionais.
- A auditoria de identificadores de teste frontend agora usa `--base` como raiz do projeto, resolve `frontendCodigo` e
  usa `--diretorio` para um diretório de busca explícito; o coletor de qualidade foi ajustado para esse contrato.
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
  hotspots, fotografia e exceções; os pesos e classificações do perfil SGC foram preservados, a política de orçamento
  passou a ser neutra quando não há override e o carregamento JSON passou a rejeitar arquivos configurados inválidos.
- `codigo/cheiros-auditar.ts` agora calcula e exibe a auditoria sem gravar por padrão; a persistência da fotografia e do
  resumo exige `--gravar` na CLI ou `gravar: true` na API, e a leitura de fotografia anterior continua disponível para
  calcular deltas sem mutação.
- `frontend/arquitetura-auditar.ts` adotou a mesma fronteira read-only, com `--gravar` como ação positiva; o coletor de
  qualidade do SGC informa explicitamente essa opção para preservar seu artefato intermediário.
- `backend/arquitetura-auditar.ts` adotou a mesma fronteira read-only, com `--gravar` como ação positiva; o fixture de
  serviço cobre ausência de artefato no modo padrão e persistência explícita.
- `backend/coesao-auditar.ts` adotou a mesma fronteira read-only, com `--gravar` como ação positiva; o teste de caminhos
  configurados cobre ausência de artefato e gravação explícita.
- `backend/contratos-auditar.ts` adotou a mesma fronteira read-only, com `--gravar` como ação positiva; o fixture de
  vazamento de modelo mantém a regra SGC e cobre ausência e persistência do relatório.
- `frontend/residuos-auditar.ts` e `frontend/residuos-validar.ts` adotaram a mesma fronteira read-only, com `--gravar`
  como ação positiva; o coletor consolidado informa a opção para manter a fotografia mais recente do perfil SGC.
- `frontend/arquitetura-auditar.ts`, `frontend/residuos-auditar.ts`, `frontend/residuos-validar.ts` e
  `frontend/cobertura-auditoria.ts` agora resolvem entradas e saídas relativas contra a base efetiva e exibem caminhos
  relativos à mesma base, sem depender do `cwd` do processo.
- `codigo/semgrep-auditar.ts` adotou a mesma fronteira read-only, com `--gravar` como ação positiva; o smoke do pacote
  cobre execução sem gravação e persistência explícita junto com a resolução da política empacotada. O motor não exibe
  mais `SGC` fixo no relatório ou na ajuda; a política padrão continua sendo a do perfil SGC e pode ser substituída.
- `integracao/contratos-diff.ts` adotou a mesma fronteira read-only, com `--gravar` como ação positiva; exportação e
  fixação de baseline continuam separadas como ações explicitamente geradoras/promotoras.
- `backend/cobertura-auditoria.ts` e `frontend/cobertura-auditoria.ts` adotaram a mesma fronteira read-only, com
  `--gravar` como ação positiva; o modo JSON não cria relatórios e os títulos deixaram de carregar `SGC` fixo.
- `frontend/arquitetura-lib.ts` foi convertido para TypeScript com tipos para análise AST, imports por camada, sinais,
  métricas, hotspots, famílias, exceções documentadas e fotografia; os hubs e heurísticas arquiteturais continuam
  explícitos no perfil SGC.
- `backend/testes-priorizar.ts` foi convertido para TypeScript; a leitura do relatório JSON passou a tratar a entrada
  como `unknown` na borda, filtrando itens inválidos sem alterar a priorização P1/P2/P3, as exclusões do perfil SGC ou
  o formato Markdown gerado.
- `backend/testes-analisar.ts` foi convertido para TypeScript; índices de testes, categorias, estatísticas, cobertura
  JaCoCo e relatórios Markdown/JSON agora têm contratos explícitos, mantendo a resolução configurável de `backendCodigo`
  e `backendTestes` e as classificações SGC de DTOs, models e outros.
- `backend/testes-analisar.ts` e `backend/testes-priorizar.ts` agora expõem opções em português (`--diretorio`,
  `--entrada`, `--saida`, `--saida-json`, `--arquivo-jacoco`), sem alterar as chaves estruturadas do relatório.
- `backend/testes-analisar.ts` agora usa `lib/saida.ts` também para o resumo humano; a emissão não bypassa mais a
  fronteira comum com `console.log`, sem alterar o Markdown, o JSON ou os textos produzidos.
- `backend/testes-analisar.ts` agora resolve `--diretorio` relativo a `--base`, preservando caminhos absolutos e
  evitando que a pasta de execução do processo determine a raiz de um projeto externo.
- `projeto/dependencias-auditar.ts` agora usa `escreverLinha` para separar e nomear os escopos, eliminando o último
  `process.stdout.write` de produção identificado na auditoria de saída humana.
- Cobertura, Semgrep e identificadores de teste frontend também adotaram `--saida`/`--diretorio`; a CLI não mantém
  `--output`/`--dir` como aliases sem consumidor identificado.
- `backend/contratos-auditar.ts` foi convertido para TypeScript com tipos para imports, retornos de controllers, campos
  expostos, índice Java, modelos e achados; a política SGC de detectar `model.*` em DTOs permanece e agora tem fixture
  externo cobrindo JSON, `backendCodigo` configurado e gravação explícita com `--gravar`.
- `backend/java-corrigir-fqn.ts` foi convertido para TypeScript com contratos para análise de imports, decisões de
  substituição, preservação de linhas e opções; a mutação agora exige `--gravar`, com simulação padrão e idempotência.
- `backend/coesao-auditar.ts` foi convertido para TypeScript com tipos para categorias de responsabilidade, severidade,
  hotspots, resumo e relatório; o vocabulário SGC de consulta, mutação, workflow, notificação e permissão permanece uma
  política explícita do auditor.
- `backend/arquitetura-auditar.ts` foi convertido para TypeScript com tipos para limiares, tipos de alvo, severidade,
  hotspots e relatório; um fixture externo valida a classificação de um service com múltiplos sinais, a configuração de
  `backendCodigo` e o modo read-only padrão com gravação explícita.
- `backend/notificacoes-assuntos-auditar.ts` foi convertido para TypeScript; regras de achado, itens de arquivo, resumo
  e relatório estão tipados, mantendo as exceções específicas de `AssuntosNotificacao`/`E2eController` e o exit code de
  violação do perfil SGC.
- `requisitos/cdus-auditar.ts` foi convertido para TypeScript; achados, relatórios e severidades agora são tipados a
  partir do contrato do parser CDU, mantendo a auditoria read-only e as regras canônicas do perfil SGC.
- `requisitos/cdus-auditar-estilo.ts` foi convertido para TypeScript; regras de estilo, perfis, linhas e achados agora
  têm tipos explícitos, preservando a auditoria read-only de aspas, placeholders legados e títulos de UI.
- `requisitos/cdus-inventariar-densidade.ts` foi convertido para TypeScript com contratos explícitos de documento,
  resumo e inventário, preservando as métricas de palavras, passos, placeholders, elementos de UI e profundidade de listas.
- `requisitos/cdus-inventariar-duplicacoes.ts` foi convertido para TypeScript; tipos de duplicação, itens registrados e
  resultado ordenado agora são explícitos, preservando a normalização de blocos, mensagens, assuntos e toasts.
- `requisitos/cdus-inventariar.ts` foi convertido para TypeScript com mapas de contagem, registros de numeração e
  inventário final tipados, preservando as frequências de formatos, situações, UI e placeholders.
- `requisitos/cdus-inventariar-vocabulario.ts` foi convertido para TypeScript com mapas, inventário e vocabulário
  canônico tipados, preservando as fontes SGC de perfis, situações, tipos de processo e elementos de UI.
- `requisitos/cdus-auditar-vocabulario.ts` foi convertido para TypeScript com regras, linhas, achados e relatórios
  tipados, mantendo as sugestões por similaridade e o vocabulário canônico específico do SGC.
- `requisitos/cdus-inventariar-mensagens.ts` foi convertido para TypeScript com o contrato do inventário e das quatro
  categorias de mensagens tipados, preservando o JSON e a saída textual existentes.
- `requisitos/cdus-auditar-mensagens.ts` foi convertido para TypeScript com regras, severidade, linhas, achados,
  relatório e resumo tipados, mantendo os avisos mecânicos e os placeholders legados identificados.
- `requisitos/cdus-auditar-mensagens-codigo.ts` foi convertido para TypeScript com contratos locais para ocorrências,
  referências, sugestões e resumo, reutilizando os tipos públicos de mensagens canônicas e categorias, preservando a
  comparação por categoria e similaridade.
- `codigo/nomes-caminhos.ts` foi convertido para TypeScript com caminhos de artefatos de nomenclatura tipados, mantendo
  a resolução relativa à configuração e à base auditada.
- `codigo/nomes-simbolos-coletar.ts` foi convertido para TypeScript com contratos para linguagens, tipos, membros,
  arquivos, pacotes, estatísticas, inventário e opções; o parser regex e a coleta recursiva continuam preservando o
  formato JSON, a parametrização da base auditada e a gravação somente com `--gravar`.
- `codigo/nomes-consistencia-auditar.ts` foi convertido para TypeScript usando o contrato compartilhado do inventário
  de símbolos; a classificação de formatos, parâmetros, pacotes Java e a fronteira read-only com `--gravar` foram
  preservadas e uniformizadas.
- `codigo/idioma-consistencia-auditar.ts` foi convertido para TypeScript usando o mesmo contrato de inventário; as
  detecções de nomes ingleses e de `id`/`*Id`, seus indicadores e a fronteira read-only com `--gravar` permanecem
  preservados.
- `codigo/cheiros-auditar.ts` foi convertido para TypeScript com tipos para padrões, filtros, contagens, pontuação,
  deltas, hotspots, fotografia e opções; os pesos e filtros parametrizados por `backendCodigo`/`frontendCodigo` foram
  preservados.
- `codigo/semgrep-auditar.ts` foi convertido para TypeScript com contratos para achados, posições, resultados,
  execução e relatórios; a entrada JSON externa é normalizada como `unknown`, mantendo regras, alvos configuráveis,
  modo automático e o schema de saída do Semgrep.
- `codigo/semgrep-auditar.ts` agora normaliza caminhos de achados relativos ou absolutos antes de gerar stdout e
  resumo Markdown, evitando que relatórios de bases externas exibam caminhos calculados contra o diretório do toolkit.
- O despachador agora resolve exclusivamente os caminhos TypeScript registrados pela árvore-fonte física; até o smoke
  do `sgc.js` compilado continua delegando a execução dos comandos ao `tsx` sobre a fonte `.ts`.
- Foi criado `tsconfig.estrito.json`, cobrindo toda a implementação TypeScript com `strict` e `noImplicitOverride`; o
  gate passou e tornou-se o `typecheck` oficial.
- `projeto/diagnostico.ts` foi convertido para TypeScript, deixou de depender de `fs-extra` e aceita catálogos
  configuráveis de recursos e comandos registrados; o catálogo padrão resolve backend, frontend e integração pela
  configuração da base, enquanto recursos exclusivos do SGC só entram quando a base contém `toolkit/sgc.ts`. A
  política mínima local do Node é a major 26 (`26.7.0`).
- `qualidade/coleta-execucao.ts` foi convertido para TypeScript com contratos de contexto, adaptadores, execução de
  subprocessos, JUnit, métricas heterogêneas e fotografia; hotspots vindos de JSON são filtrados como `unknown`, e os
  perfis Gradle/npm/Playwright continuam declarados como orquestração específica do SGC. A montagem do comando
  Playwright agora é uma função pura e resolve `diretorios.testesIntegracao`, compartilhando a mesma convenção do
  crawler de acessibilidade. A função de coleta aceita catálogos de perfis e adaptadores externos por composição, sem
  mutar os defaults globais.
- `qualidade/coleta.ts` foi convertido para TypeScript; a validação de perfis/opções e o wrapper que delega ao coletor
  agora compartilham a fronteira tipada do runtime.
- `qualidade/resumo.ts` foi convertido para TypeScript; o carregador de fotografias passou a aceitar um tipo genérico e
  o comando `qualidade resumo` agora resolve a fotografia mais recente pela opção `--base` e relativiza o caminho de
  saída contra a base auditada, não contra o diretório do processo.
- `projeto/limpar.ts` foi convertido para TypeScript, substituiu `fs-extra` por APIs nativas do Node e aceita uma
  política de padrões de limpeza injetável; os padrões padrão agora derivam backend, frontend e artefatos de qualidade
  da configuração da base, removendo nomes legados que já não são produzidos.
- `projeto/preparar.ts` foi convertido para TypeScript; a base efetiva chega ao diagnóstico e aos comandos externos,
  os escopos de instalação de dependências podem ser fornecidos por projeto e a opção obsoleta `showTimer` do Listr2
  foi removida.
- `projeto/qualidade.ts` foi convertido para TypeScript; o catálogo Gradle do SGC continua como default, mas a base,
  o catálogo de perfis e o executor de comandos podem ser fornecidos por projeto externo ou teste. O catálogo também
  pode ser declarado em `execucoes.qualidade`.
- `projeto/dependencias-auditar.ts` foi convertido para TypeScript; os escopos de auditoria, comandos e argumentos
  agora podem ser definidos por projeto, enquanto raiz, frontend e toolkit continuam no catálogo padrão do SGC quando
  não há configuração.
- `projeto/preparar.ts` foi convertido para TypeScript; os escopos de instalação podem ser definidos em
  `execucoes.instalacao`, preservando raiz, frontend e toolkit como defaults do SGC.
- `backend/cobertura-ramificacoes.ts` e `backend/cobertura-auditoria.ts` foram convertidos para TypeScript com
  resultados, hotspots, métricas e geração de relatório tipados; a leitura JaCoCo continua delegada ao domínio comum.
- `frontend/cobertura-ramificacoes.ts`, `frontend/cobertura-ramificacoes-erros.ts` e
  `frontend/cobertura-auditoria.ts` foram convertidos para TypeScript com tipos para métricas V8, hotspots, linhas
  suspeitas e relatórios; os filtros de arquivos e heurísticas de erro do perfil SGC foram preservados.
- A família de auditorias de cobertura agora usa `--minimo` em backend e frontend, nomes internos em português e
  ajuda específica encaminhada pelo roteador; os campos JSON `hotspots` e `scoreImpacto` foram preservados como
  contrato de integração do agregador de qualidade.
- `frontend/residuos-auditar.ts` e `frontend/residuos-validar.ts` foram convertidos para TypeScript; fotografia,
  violações, avisos, exceções e opções de persistência agora têm contratos explícitos, mantendo os budgets do SGC.
- `frontend/acessibilidade-crawler.ts` e `frontend/acessibilidade-processar-resultados.ts` foram convertidos para
  TypeScript; o processador valida o JSON externo antes de gerar Markdown e o crawler aceita base, spec, configuração e
  executor Playwright parametrizados, mantendo os defaults SGC.
- `frontend/identificadores-teste-listar.ts` e `frontend/identificadores-teste-listar-duplicados.ts` foram convertidos
  para TypeScript; os resultados de coleta/duplicação são tipados e o agrupamento usa `Map`, preservando as regras e a
  saída dos identificadores de teste do perfil SGC.
- `frontend/modais-validar.ts` e `frontend/views-templates-validar.ts` foram convertidos para TypeScript; os contratos
  de violações, resumos e opções agora são explícitos, mantendo as exceções e os componentes SGC aprovados.
- `frontend/arquitetura-validar.ts` foi convertido para TypeScript com contratos do dependency-cruiser e da auditoria
  de ações; a opção runtime `cwd`, ausente na declaração da dependência, ficou confinada a um cast na borda.
- `frontend/arquitetura-auditar.ts` foi convertido para TypeScript; a fotografia, opções de persistência e resumo de
  hotspots agora usam o contrato público do núcleo AST, mantendo a análise específica do perfil SGC.
- `integracao/contratos-openapi-caminhos.ts` foi convertido para TypeScript com o contrato explícito dos caminhos
  atual, de referência e de relatório; ele permanece independente do gerador de tipos removido.
- `integracao/contratos-exportar-openapi.ts`, `integracao/contratos-diff.ts` e
  `integracao/contratos-fixar-baseline.ts` foram convertidos para TypeScript; opções, resultados de contrato e a
  validação da resposta JSON agora têm tipos explícitos, preservando os três fluxos de integração do perfil SGC.
- A configuração já aceita alguns caminhos diferentes do layout do SGC; auditores de cobertura, arquitetura, coesão,
  contratos, resíduos e coleta possuem parametrização parcial por `--base`, `--arquivo`, `--saida` ou configuração.
  Arquitetura, resíduos, OpenAPI e coleta já resolvem seus defaults após a base; outros comandos ainda têm defaults
  globais ou caminhos `backend/src`/`frontend/src` fixos. Isso ainda não equivale a portabilidade.
- O gerador de tipos OpenAPI foi removido. O toolkit mantém somente exportação, comparação e fixação de fotografias de
  contrato; o Springdoc permanece no backend porque o ciclo E2E usa Swagger/OpenAPI para aguardar a aplicação.
- O histórico recente relevante está publicado na `main`; esta rodada fecha a fronteira de distribuição fonte + `tsx`
  e separa a raiz do consumidor da instalação física do toolkit.

### 3.2 Evidência de validação atual

Nas validações desta rodada, em 13 de agosto de 2026, executadas diretamente sob Node `26.7.0`:

- `npm --prefix toolkit run test`: 121 testes aprovados em 7 arquivos; o smoke de pacote é separado para não tornar a
  suíte unitária dependente de rede ou instalação;
- `npm --prefix toolkit run test:coverage`: aprovado com baseline informativa de 45,72% de statements (604/1.321),
  34,02% de branches (313/920), 52,27% de funções (138/264) e 45,92% de linhas (580/1.263); o script exclui
  `test/**` para não contar o apoio de testes como implementação e ainda não aplica threshold, porque a prioridade é
  transformar os contratos críticos em cenários explícitos;
- `npm --prefix toolkit run test:pacote`: 1 teste aprovado, com `npm pack`, instalação isolada, auditoria no consumidor
  e verificação da política Semgrep empacotada;
- `npm --prefix toolkit run build`: aprovado;
- `npm --prefix toolkit run typecheck`: aprovado;
- `npm --prefix toolkit run typecheck:testes`: aprovado sobre os oito arquivos de teste TypeScript e o apoio comum
  `test/apoio.ts`;
- `npm --prefix toolkit run lint`: aprovado;
- `npm --prefix toolkit run deps:audit`: aprovado;
- `npx knip --reporter compact` dentro de `toolkit/`: aprovado sem exports não consumidos;
- `git diff --check`: aprovado;
- importação dos comandos sem execução acidental: coberta pelos testes;
- execução fonte pelo script npm e smoke do artefato compilado: aprovados nas rodadas de migração;
- binário do workspace e do pacote: aprovados. `npm exec --workspace toolkit sgc -- --help` usa `toolkit/bin/sgc.cjs`,
  chama `tsx` e executa `sgc.ts`; o smoke de pacote repete o contrato sem o hoisting do workspace.

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
de caminhos dos relatórios V8 frontend; esta chega a 96 com o launcher `tsx` do binário npm. Nenhuma dessas mudanças
reintroduz o wrapper obsoleto; as rodadas posteriores de limpeza, preparação, qualidade, dependências e acessibilidade
elevam a cobertura para 101 cenários; uma rodada chega a 102 com o teste comportamental do auditor de contratos backend;
esta rodada adiciona a resolução portável do Semgrep e o smoke de pacote isolado; a suíte unitária chega a 104 cenários;
a parametrização de execuções externas chega a 105; uma rodada explicita as políticas de resíduos e chega a 106; outra
uniformiza a família de nomenclatura e chega a 107; uma rodada uniformiza os relatórios de cobertura e chega a 108; uma
rodada protege a sincronização de versão e chega a 109; uma rodada centraliza o catálogo da CLI e chega a 110; uma
rodada adiciona o fixture externo Java/Vue e chega a 111; esta rodada padroniza as opções da árvore de linhas e chega a
112. Nesta rodada, os 14 cenários de projeto foram extraídos para `test/projeto.test.ts`; `test/sgc.test.ts` continua
com 86 cenários da CLI, saída, integração e requisitos, enquanto os contratos de projeto passam a ter um ponto de
execução focada próprio. Na rodada seguinte, os 3 cenários de configuração foram extraídos para
`test/configuracao.test.ts`, deixando 83 cenários no teste principal e tornando explícita a fronteira entre configuração
parametrizada e roteamento da CLI.
Na rodada posterior, os 2 cenários de integração e OpenAPI foram extraídos para `test/integracao.test.ts`, deixando 81
cenários no teste principal e isolando o contrato de distribuição de artefatos de integração.
Na rodada seguinte, os 4 cenários de resumo e coleta foram extraídos para `test/qualidade.test.ts`, deixando 77 cenários
no teste principal e mantendo a validação dos perfis sem depender dos testes de auditoria de backend ou frontend.
Nesta rodada, o projeto externo ganhou um cenário de sincronização de versão usando `diretorios.frontend`, chegando a
113 cenários regulares e 78 cenários restantes no teste principal.
Nesta rodada, o crawler de acessibilidade passou a derivar seus defaults de `diretorios.testesIntegracao`, com regressão
para uma base externa configurada; a suíte chega a 114 cenários regulares e o teste principal permanece com 78.
Na rodada seguinte, a coleta consolidada passou a montar o comando Playwright pela mesma configuração, com teste focado
na função pura de opções; a suíte chega a 115 cenários regulares e `test/qualidade.test.ts` passa a ter 5 cenários.
Na rodada seguinte, o diagnóstico passou a separar recursos estruturais configuráveis do perfil SGC, com regressão para
uma base sem o toolkit instalado; a suíte chega a 116 cenários regulares e `test/projeto.test.ts` passa a ter 16.
Na rodada seguinte, o corretor FQN passou a usar `backendCodigo` e `backendTestes` quando configurados, cobrindo fonte e
testes Java externos; a suíte chega a 117 cenários regulares e `test/sgc.test.ts` passa a ter 79. Na rodada seguinte,
os relatórios backend passaram a usar os nomes portugueses `analise-testes.md/json` e `priorizacao-testes.md`; a limpeza
passou a derivar backend, frontend e artefatos da configuração e a suíte chega a 118 cenários regulares, com
`test/projeto.test.ts` em 17. Na rodada seguinte, `backend testes analisar` passou a resolver `--diretorio` relativo a
`--base`, com regressão em uma base externa; a suíte chega a 119 cenários regulares e `test/sgc.test.ts` passa a ter 80.
Na rodada seguinte, o Semgrep passou a normalizar caminhos de achados relativos e absolutos em relação à base auditada;
a suíte chega a 120 cenários regulares. Na rodada seguinte, `qualidade resumo` passou a exibir o caminho da fotografia
relativo à base auditada, inclusive em consumidores externos. Na rodada seguinte, a família de auditorias frontend passou
a alinhar entradas, saídas e mensagens de artefatos à base efetiva; a contagem permanece em 120 cenários. Na rodada
seguinte, o alvo de compilação TypeScript passou de ES2023 para ES2025, coerente com Node 26 e sem compatibilidade legada;
os 120 cenários e a baseline de cobertura permanecem verdes. Na rodada seguinte, os `tsconfig` passaram a incluir somente
TypeScript; o launcher `bin/sgc.cjs` continua como fronteira npm fora do grafo de compilação.
Na rodada seguinte, o despachador deixou de procurar `.js` em `dist`: inclusive no smoke do `sgc.js` compilado, a
execução é delegada ao `tsx` sobre a fonte `.ts`; `dist` permanece apenas um artefato de verificação.
Na rodada seguinte, a coleta de qualidade passou a aceitar perfis e adaptadores externos por composição, preservando os
catálogos SGC como defaults; a suíte chega a 121 cenários regulares, com 81 no teste principal e 6 em `qualidade.test.ts`.
Na rodada seguinte, o contrato TypeScript dos diretórios configuráveis passou a refletir os nomes aceitos pelo schema,
mantendo a rejeição de chaves desconhecidas e eliminando índices textuais permissivos nos resolvers.

### 3.3 Tamanho e composição atual

Inventário dos arquivos rastreados do toolkit, excluindo `dist`, cobertura e artefatos ignorados:

- 73 arquivos TypeScript de implementação;
- 0 arquivos JavaScript de implementação; o único CJS é o launcher mínimo do binário;
- 0 arquivos JavaScript de teste e 8 arquivos TypeScript de teste (`test/sgc.test.ts`, `test/projeto.test.ts`,
  `test/configuracao.test.ts`, `test/integracao.test.ts`, `test/qualidade.test.ts`, `test/cdus.test.ts`,
  `test/externo.test.ts` e `test/pacote.test.ts`);
- 7 arquivos de teste TypeScript concentram 121 cenários regulares, mais 1 smoke de distribuição isolada;
- `test/apoio.ts` centraliza a raiz do toolkit, o launcher `tsx`, o contrato de execução e `executarSgc`, evitando
  cópias divergentes nos testes de projeto, integração, qualidade e CLI;
- maior módulo atual: `frontend/arquitetura-lib.ts`, com aproximadamente 1.200 linhas;
- outros hotspots: `codigo/nomes-simbolos-coletar.ts`, `frontend/residuos-lib.ts` e
  `qualidade/coleta-execucao.ts`.

O núcleo TypeScript de implementação foi concluído: 100% dos arquivos de implementação rastreados são TypeScript.

### 3.4 Achados da auditoria crítica

| Severidade | Achado | Evidência e impacto |
|---|---|---|
| Resolvido nesta rodada | Binário npm quebrado | `bin.sgc` aponta para `bin/sgc.cjs`, que chama o CLI do `tsx`; o workspace e o tarball instalado em consumidor isolado executam `sgc --help` e uma auditoria real. |
| Resolvido nesta rodada | Pacote não distribuível | `sgc-scripts@0.1.0` declara `version`, `files`, `exports`, `bin` e dependências de runtime; `npm pack` e `npm run test:pacote` passaram. A política de publicação ainda não foi escolhida. |
| Resolvido nesta rodada | Raiz acoplada à posição física | `lib/caminhos.ts` usa `process.cwd()` como raiz padrão e mantém `DIRETORIO_TOOLKIT` para recursos do próprio pacote; o smoke isolado confirmou que o consumidor é a base auditada. |
| Resolvido nesta rodada | Semgrep acoplado ao ambiente local | A política padrão vinha da raiz do consumidor e o executável era fixado em `~/.local/bin/semgrep`; ambos agora usam a instalação do toolkit e o `PATH`, com testes de override e consumidor isolado. |
| Resolvido | Configuração permissiva do Knip | A configuração anterior tratava praticamente todos os arquivos como entrypoints. A nova lista os comandos reais, inclui JS/TS e, nesta rodada, encontrou e removeu oito exports internos não consumidos. |
| Resolvido parcialmente | Base externa é parcialmente ignorada | Arquitetura, resíduos, OpenAPI, coleta, Semgrep, cheiros, assuntos de notificação, sincronização de versão, crawler de acessibilidade e diagnóstico agora respeitam a base/configuração; outros comandos ainda precisam da mesma correção. |
| Resolvido nesta rodada | Auditores gravam por padrão | `codigo cheiros auditar`, `frontend arquitetura auditar`, `backend arquitetura auditar`, `backend coesao auditar`, `backend contratos auditar`, `frontend residuos auditar/validar`, `codigo semgrep auditar`, toda a família `codigo nomes`, `integracao contratos diff` e as duas auditorias unificadas de cobertura agora só persistem com `--gravar`. Geração de relatórios, coleta e mutações continuam classificadas separadamente. |
| Resolvido | Cobertura insuficiente de mutação | O corretor `backend/java-corrigir-fqn.ts` agora tem fixture de escrita, verificação de conteúdo sem duplicação e segunda execução idempotente. |
| Resolvido nesta rodada | Corretor FQN ignorava raízes Java configuradas | `backend/java-corrigir-fqn` agora usa `diretorios.backendCodigo` e `diretorios.backendTestes` quando a base possui configuração; a heurística anterior continua para uma base backend isolada sem configuração. |
| Resolvido nesta rodada | Análise de testes ignorava a base para diretório explícito | `backend testes analisar --diretorio servidor --base <base>` agora procura `servidor` dentro da base informada; caminhos absolutos continuam inalterados. |
| Resolvido nesta rodada | Relatório Semgrep calculava caminhos contra a raiz errada | Achados devolvidos com caminho relativo ou absoluto agora são normalizados contra a base auditada antes da exibição em stdout e Markdown. |
| Resolvido nesta rodada | Resumo de qualidade calculava caminho contra o `cwd` | `qualidade resumo` agora informa a fotografia relativa à base efetiva, preservando a mesma referência em JSON e saída humana para bases externas. |
| Resolvido nesta rodada | Auditorias frontend misturavam `cwd` e base auditada | Arquitetura, resíduos e cobertura frontend agora resolvem caminhos relativos contra `--base` e exibem artefatos relativos à mesma raiz. |
| Resolvido nesta rodada | Configuração TypeScript ainda incluía JavaScript legado | Os `tsconfig` de checagem e build agora incluem somente `.ts`; o único `.cjs` restante é o launcher deliberadamente externo ao compilador. |
| Resolvido nesta rodada | Despachador mantinha fallback para implementação compilada | `lib/execucao.ts` agora resolve sempre a fonte `.ts` via `DIRETORIO_TOOLKIT` e `tsx`; o build não cria nem exige uma segunda árvore de comandos `.js`. |
| Resolvido nesta rodada | Coleta de qualidade dependia de catálogos globais mutáveis | `qualidade/coleta-execucao.ts` agora recebe perfis e adaptadores por opção, valida adaptadores ausentes antes de criar artefatos e mantém os catálogos SGC como defaults. |
| Resolvido nesta rodada | Contrato TypeScript de diretórios era permissivo | `lib/configuracao.ts` agora restringe chaves aos nomes suportados pelo schema e os resolvers aceitam somente essas chaves; a configuração externa continua rejeitando nomes desconhecidos em runtime. |
| Resolvido | Efeito colateral oculto de gravação | `codigo nomes auditar-consistencia` gerava o inventário auxiliar com gravação habilitada e contaminava `--json`; a opção `--gravar` agora é propagada e a coleta interna é silenciosa. |
| Resolvido | Configuração sem validação | `configuracao-toolkit.json` agora exige a versão `1` e valida estrutura, chaves conhecidas e caminhos textuais antes da combinação com defaults. |
| Resolvido nesta rodada | Políticas de resíduos apontando para legado ausente | Os defaults de orçamento e exceções frontend foram removidos; overrides continuam aceitos, a ausência usa política neutra explícita e arquivo configurado ausente ou inválido falha visivelmente. |
| Média | Opções e efeitos divergentes | Os comandos principais já usam opções em português; ainda há defaults de nomes de artefatos e alguns contratos de geração que precisam ser uniformizados, além de mutações sem prévia uniforme. |
| Resolvido nesta rodada | Testes não representam pacote externo | A suíte interna continua separada do smoke de distribuição, e `npm run test:pacote` empacota, instala em diretório isolado e executa o binário sem dependências hoisted do monorepo. |
| Resolvido nesta rodada | Cobertura funcional não medida | `npm run test:coverage` agora gera a baseline informativa do próprio toolkit com `@vitest/coverage-v8`; threshold fica para depois da divisão dos testes por domínio e da análise dos contratos críticos. |
| Resolvido nesta rodada | Testes de projeto misturados ao teste da CLI | Os 17 cenários de versão, árvore, diagnóstico, limpeza, preparação, qualidade e dependências agora estão em `test/projeto.test.ts`; o teste principal concentra 81 cenários e a suíte permite execução focada por domínio. |
| Resolvido nesta rodada | Nomenclatura e limpeza de relatórios divergentes | O pipeline backend agora usa `analise-testes.md/json` -> `priorizacao-testes.md`; `projeto/limpar` deriva diretórios configurados e remove apenas padrões ainda produzidos, descartando caminhos legados sem referências. |
| Resolvido nesta rodada | Configuração misturada à validação da CLI | Os 3 cenários de carregamento, validação e execução parametrizada agora estão em `test/configuracao.test.ts`; o teste principal caiu para 83 cenários e a configuração pode ser validada sem importar o roteador. |
| Resolvido nesta rodada | Integração OpenAPI misturada à validação da CLI | Os 2 cenários de importação e artefatos OpenAPI agora estão em `test/integracao.test.ts`; o teste principal caiu para 81 cenários e a persistência de diff continua coberta com `--gravar`. |
| Resolvido nesta rodada | Qualidade misturada à validação da CLI | Os 4 cenários iniciais de resumo e coleta foram extraídos para `test/qualidade.test.ts`; o teste principal caiu para 77 cenários e o arquivo agora cobre também a montagem configurada do Playwright. |
| Resolvido nesta rodada | Apoio de execução duplicado entre testes | `test/apoio.ts` passou a ser a fonte única da raiz do toolkit, do `tsx` e do contrato `ResultadoExecucao`; os quatro testes que executam a CLI não mantêm cópias locais desse mecanismo. |
| Resolvido nesta rodada | Apoio de testes contaminava a cobertura | `test:coverage` agora exclui `test/**`; a baseline permanece focada na implementação e não conta `test/apoio.ts` como código produtivo. |
| Resolvido nesta rodada | Sincronização de versão fixava o frontend do SGC | `projeto/versao-sincronizar` agora resolve `diretorios.frontend` pela configuração da base; o fallback continua sendo `frontend` e o teste externo cobre `cliente/package.json`. |
| Resolvido nesta rodada | Crawler fixava a raiz de integração do SGC | `frontend/acessibilidade-crawler` agora deriva os defaults de `diretorios.testesIntegracao`; opções explícitas continuam substituindo a convenção e o teste externo cobre a base configurada. |
| Resolvido nesta rodada | Coleta Playwright divergia do crawler | `qualidade/coleta-execucao` agora centraliza a montagem de descrição e argumentos Playwright em função pura que resolve `diretorios.testesIntegracao`; o contrato tem teste de base externa. |
| Resolvido nesta rodada | Diagnóstico exigia artefatos do próprio toolkit em qualquer base | `projeto/diagnostico` agora monta recursos estruturais com `backend`, `frontend` e `testesIntegracao` configurados; recursos de `toolkit`, portas e `.env.e2e` continuam no perfil SGC e não contaminam bases externas. |
| Resolvido nesta rodada | Resumo de análise bypassava a saída comum | `backend/testes-analisar.ts` agora usa `escreverLinha` em todas as linhas humanas; stdout continua igual e não há mistura com o JSON gravado. |
| Resolvido nesta rodada | Auditoria de dependências bypassava a saída comum | `projeto/dependencias-auditar.ts` agora usa `escreverLinha` para a separação dos escopos; o fluxo mantém títulos, quebras e códigos de falha. |
| Resolvido nesta rodada | Roteador monolítico e inventário duplicado | Os 42 comandos que apenas despacham scripts agora vêm de `lib/catalogo-comandos.ts`, com teste de unicidade, descrição, rota e arquivo existente. A documentação passou a tratar `sgc --help` como catálogo canônico e mantém apenas exemplos; comandos com ações/opções próprias continuam explícitos em `sgc.ts`. |
| Resolvido nesta rodada | Ajuda de folhas catalogadas era genérica | O roteador desativa a ajuda automática do Commander somente nas folhas que despacham arquivos e encaminha `--help` ao script TypeScript; grupos continuam usando a ajuda do Commander e as opções específicas ficam visíveis. |
| Resolvido nesta rodada | Opção de meta de cobertura em inglês | `backend cobertura auditoria` e `frontend cobertura auditoria` agora usam `--minimo`; os símbolos internos e os relatórios Markdown da família foram normalizados sem alterar os campos JSON de integração. |
| Baixa | APIs nativas e dependências se sobrepõem | `fs-extra` já foi removido do runtime e ficou restrito aos testes; a auditoria de dependências restantes deve continuar por uso real, sem remoção antecipada. |

### 3.5 Interpretação correta do estado

- O toolkit funciona como ferramenta interna executada pelo script npm ou por `npx tsx` dentro deste workspace.
- O toolkit também funciona como pacote CLI externo fonte + `tsx`: o tarball foi instalado em consumidor isolado e o
  binário executou uma auditoria contra a raiz do consumidor.
- `exports` aponta alguns subpaths diretamente para `.ts`; esse contrato serve ao workspace e ao pacote fonte com `tsx`,
  mas não é um contrato consumível por Node puro sem um loader TypeScript.
- Parametrização parcial não significa generalização concluída.
- Build aprovado prova que a árvore pode ser emitida; não prova que o pacote emitido contém assets, configuração e
  resolução de raiz adequados para distribuição.
- Knip aprovado, 121 testes unitários verdes e o smoke de pacote aprovado são gates úteis, mas ainda insuficientes para afirmar ausência de código morto fora
  do grafo declarado, segurança de todos os comandos mutáveis ou portabilidade. O grafo do Knip agora é uma evidência
  útil de exports não consumidos; não substitui testes de pacote externo.

### 3.6 Inventário de efeitos colaterais

O inventário abaixo foi levantado por inspeção dos comandos registrados e das bibliotecas chamadas por eles. Ele descreve
o comportamento atual; não transforma automaticamente todo comando que gera relatório em auditoria read-only.

| Classe atual | Comandos ou famílias | Efeito observado e controle existente |
|---|---|---|
| Auditoria read-only | `requisitos cdus *`, `backend cobertura auditoria/ramificacoes`, `frontend cobertura auditoria/ramificacoes`, `frontend arquitetura validar`, `frontend modais validar`, `frontend views templates-validar`, `frontend identificadores-teste *`, `codigo nomes coletar-simbolos/auditar-consistencia/auditar-idioma`, `projeto diagnostico` | Leem código/relatórios e escrevem somente stdout/JSON por padrão; não criam artefatos próprios. Dependências externas podem fazer leitura adicional. |
| Auditoria read-only com persistência explícita | `backend cobertura auditoria`, `frontend cobertura auditoria` | Calculam a análise e exibem stdout/JSON por padrão; só criam o relatório Markdown indicado por `--saida` quando recebem `--gravar`. |
| Geração de relatório indicado | `backend testes analisar/priorizar`, `frontend acessibilidade processar` | Criam arquivos Markdown/JSON definidos por `--saida`, `--saida-json` ou defaults. São geradores explícitos, não auditores read-only. |
| Artefato de contrato | `integracao contratos exportar-openapi`, `integracao contratos diff` | Exportação grava OpenAPI; diff apenas grava resumo Markdown com `--gravar`; `fixar-baseline` promove uma referência somente quando chamado. |
| Mutação de fonte ou baseline | `backend java corrigir-fqn`, `projeto versao-sincronizar`, `integracao contratos fixar-baseline` | Alteram código/configuração ou promovem arquivo. FQN e versão simulam por padrão e exigem `--gravar`; baseline copia diretamente para o destino por ser uma ação de promoção nomeada. |
| Limpeza confirmada | `projeto limpar` | Lista em prévia por padrão e remove somente com `--confirmar`; é o modelo de confirmação explícita a preservar. |
| Orquestração externa | `qualidade coletar`, `projeto qualidade`, `projeto dependencias auditar`, `projeto preparar`, `frontend acessibilidade crawler` | Executam Gradle, npm, Playwright, Semgrep ou Git e podem criar artefatos fora da biblioteca do toolkit. Precisam de perfil/ação explícita e limites de raiz. |

Achado corrigido nesta rodada: quando `codigo nomes auditar-consistencia` precisa gerar um inventário auxiliar ausente,
ele só chama a coleta com gravação habilitada se a auditoria recebeu `--gravar`. A propagação de `gravar` e a opção
interna `silencioso` preservam o contrato de leitura e mantêm o stdout JSON válido.

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
2. **[resolvido nesta rodada] Entry point e pacote**: o roteador é `sgc.ts`, o launcher executa `tsx`, e workspace,
   tarball e instalação isolada passaram nos smoke tests.
3. **[resolvido nesta rodada] Raiz do consumidor**: o diretório físico do toolkit, o `process.cwd()` e `--base` estão
   separados e documentados; o teste do pacote prova a precedência da raiz do consumidor.
4. **[decidido e validado nesta rodada] Modelo de distribuição**: o reuso será por pacote-fonte com runtime `tsx`;
   `version`, `files`, `bin`, `exports`, assets e dependências de runtime refletem esse modelo. A política de publicação
   continua uma decisão operacional futura.
5. **[resolvido nesta rodada] TypeScript sem rigor uniforme**: `tsconfig.estrito.json` cobre os 73 módulos de
   implementação TypeScript com `strict` e `noImplicitOverride`; o gate estrito passou e tornou-se o `typecheck` oficial.
6. **[resolvido nesta rodada] Dependência de runtime**: `tsx` está em `dependencies`, o launcher de pacote foi criado e
   a instalação isolada confirma que o pacote fonte+tsx não depende do hoisting do workspace.
7. **[resolvido nesta rodada] Contrato `.js`/`.ts` temporário**: todos os comandos registrados usam caminhos `.ts` e o
   fallback do despachador foi removido.
8. **Hard-coding de perfil**: várias regras continuam presas ao layout e ao vocabulário SGC. Antes de declarar o toolkit
   reutilizável, separar engine, política e adaptador.

### Prioridade média

9. **Testes ainda parcialmente concentrados**: `test/sgc.test.ts` ainda concentra 81 cenários, embora os 17 cenários
   de projeto, os 3 de configuração, os 2 de integração e os 4 de qualidade já tenham sido extraídos para arquivos
   próprios. Dividir os cenários restantes por domínio continua recomendado para localizar contratos, reduzir o custo de
   execução focada e permitir fixtures mais independentes.
10. **Schema de resultados**: fotografias, auditorias, cobertura, diagnósticos e relatórios usam objetos sem schema
   versionado. Formalizar tipos e versões de saída antes de extrair o pacote externo.
11. **Opções heterogêneas, mas sem aliases ingleses próprios ativos**: a implementação usa `--entrada`, `--saida`,
   `--diretorio`, `--arquivo`, `--base` e opções de domínio em português; a busca não encontrou `--input`, `--output`,
   `--dir` ou `--directory` como contratos do toolkit. Ainda falta um contrato comum para parsing, mensagens de ajuda,
   valores padrão e validação; flags de Node, Semgrep e Playwright permanecem somente como encaminhamento externo.
12. **Documentação derivada**: o catálogo já foi atualizado para `sgc.ts`, mas ainda precisa ser centralizado para não
    derivar ajuda, comandos e exports em fontes duplicadas. O inventário de comandos não deve divergir do roteador.
13. **Orquestração pesada**: `qualidade/coleta-execucao.ts` mistura subprocessos, Gradle, npm, Playwright, parsing de
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

1. **[concluído]** Criar teste que execute o modo de escrita de `backend/java-corrigir-fqn.ts` sobre fixture
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
6. **[concluído nesta rodada]** Registrar uma baseline de cobertura do próprio toolkit com `npm run test:coverage`,
   inicialmente informativa; definir thresholds apenas depois de identificar quais contratos críticos ainda não têm teste.

Situação: itens 1, 2, 4, 5 e 6 concluídos; item 3 continua pendente.

Critério de aceite: comandos mutáveis conhecidos têm testes de efeito, Knip consegue revelar código órfão real e a
configuração externa possui testes de precedência e erro.

### Fase A — fechar a fronteira TypeScript do runtime

1. **[concluído nesta rodada]** Migrar `toolkit/sgc.js` para `toolkit/sgc.ts`.
2. **[concluído nesta rodada]** Tipar o registro de comandos do Commander, opções e resultado de `principal`.
3. **[concluído nesta rodada]** Atualizar scripts raiz, script do toolkit, README, ADRs, testes e referências internas
   para o novo entrypoint.
4. **[concluído nesta rodada]** Separar explicitamente três caminhos: diretório físico de instalação do toolkit, diretório
   atual (`process.cwd()`) e raiz explícita do projeto auditado (`--base`); o smoke do pacote cobre o consumidor isolado.
5. **[concluído nesta rodada]** Corrigir o `bin` do pacote com launcher que chama `tsx` e testar a execução por `npm exec`,
   `npx tsx` e `npm --prefix toolkit run sgc`.
6. **[concluído nesta rodada]** Decidir e implementar o modelo de instalação externo: pacote fonte + `tsx` como
   dependência de runtime, com `version`, `files`, `exports` e launcher declarados.
7. **[concluído nesta rodada]** Criar `test:pacote`, que executa `npm pack`, instala em diretório temporário, cria um
   projeto consumidor fixture e audita a raiz do consumidor pelo binário instalado.
8. **[concluído nesta rodada]** Remover o fallback `.ts` → `.js` do despachador depois de todos os comandos registrados
   passarem a usar caminhos TypeScript explícitos.

Critério de aceite: nenhum comando fonte depender de `node` puro, nenhum binário apontar para um caminho quebrado e o
launcher compilado, quando exercitado no smoke opcional, continuar despachando a mesma fonte TypeScript pelo `tsx`.

### Fase B — converter bibliotecas puras e contratos de dados

1. **[concluído nesta rodada]** Migrar `lib/dominios/cobertura-java.ts` e `lib/dominios/cobertura-web.ts`, mantendo os
   contratos de métricas, os caminhos relativos à base auditada e os fixtures existentes.
2. **[concluído nesta rodada]** Migrar `backend/lib/testes-analisar-regras.ts`, `backend/cobertura-ramificacoes.ts`,
   `backend/cobertura-auditoria.ts`, `frontend/cobertura-ramificacoes.ts`, `frontend/cobertura-ramificacoes-erros.ts`,
   `frontend/cobertura-auditoria.ts`, `requisitos/cdus-mensagens-lib.ts`,
   `frontend/identificadores-teste-lib.ts`, `requisitos/cdus-lib.ts`, `requisitos/cdus-vocabulario-lib.ts`,
   `requisitos/cdus-mensagens-codigo-lib.ts`, `frontend/acoes-backend-lib.ts`, `frontend/residuos-lib.ts`,
   `frontend/arquitetura-lib.ts`, `projeto/diagnostico.ts`, `projeto/limpar.ts`, `projeto/preparar.ts`,
   `projeto/qualidade.ts`, `projeto/dependencias-auditar.ts`, `qualidade/coleta-execucao.ts`, `qualidade/coleta.ts`,
   `qualidade/resumo.ts` e os três comandos de contratos OpenAPI; os achados e comandos transversais restantes foram
   convertidos nos lotes posteriores da Fase C.
3. **[concluído nesta rodada]** Introduzir tipos para JaCoCo, V8, regras da análise de testes backend, mensagens CDU,
   cobertura frontend, violações de ações, resíduos, arquitetura AST, execução e resumo de qualidade, diagnóstico,
   fotografias, exceções e contratos OpenAPI; os contratos de entrada JSON não confiáveis são tratados como `unknown`.
4. Substituir `any` implícito por `unknown` na entrada JSON e validar apenas o que o consumidor realmente exige.
5. **[concluído nesta rodada]** Criar `tsconfig.estrito.json` com `strict` e `noImplicitOverride`, aplicá-lo a toda a
   implementação TypeScript, zerar os diagnósticos e incorporá-lo ao `typecheck` principal.
6. **[parcial nesta rodada]** Eliminar defaults de caminho calculados durante import; `lib/execucao.ts` agora resolve
   o executável `tsx` somente ao executar um comando, removendo a sondagem `existsSync` do carregamento. Ainda faltam
   revisar constantes de política e caminhos específicos de perfil, que não devem ser confundidas com I/O acidental.

Critério de aceite: as bibliotecas convertidas não fazem I/O durante import, têm tipos públicos documentados e mantêm os
mesmos fixtures e resultados JSON.

### Fase C — converter comandos por reuso, não por diretório

Lotes sugeridos:

1. **[concluído nesta rodada]** Projeto: diagnóstico, limpeza, preparação, perfil de qualidade e auditoria de
   dependências convertidos. O catálogo padrão continua sendo o perfil SGC, com base e execução parametrizáveis para
   reuso externo; `configuracao-toolkit.json` pode substituir separadamente qualidade, dependências e instalação.
2. **Backend**: cobertura, análise, priorização de testes, contratos e FQN já convertidos; o FQN já respeita as raízes
   configuradas, mas ainda falta parametrizar tarefas Gradle e categorias das demais famílias.
3. **[parcial nesta rodada]** Frontend: cobertura V8, resíduos, acessibilidade e identificadores de teste já
   convertidos; o crawler e a coleta Playwright respeitam `testesIntegracao`, mas faltam parametrizar completamente raiz
   Vue, globs e convenções de componentes.
4. **[concluído nesta rodada]** Integração: exportação, diff e baseline OpenAPI; o módulo permanece independente do
   gerador de tipos removido.
5. **[concluído nesta rodada]** Requisitos: o motor Markdown, as bibliotecas de mensagens e os dez comandos CDU foram
   convertidos para TypeScript, preservando as regras específicas do SGC; o isolamento do perfil CDU continua na Fase D.
6. **[concluído nesta rodada]** Código transversal: cheiros, Semgrep e inventários de nomes/idioma foram convertidos
   para TypeScript, preservando as políticas locais e os contratos do SGC; a parametrização horizontal continua na
   Fase D.

O corretor FQN foi incluído depois da cobertura de mutação e idempotência da Fase 0 estar corrigida e publicada.

Para cada comando convertido:

- definir tipos das opções e do resultado;
- preservar `principal(argumentos)` e importação sem efeito colateral;
- trocar mensagens e exemplos para `npx tsx`;
- atualizar o registro do roteador e o teste de importação;
- mover caminhos específicos para configuração/política, quando a regra tiver potencial horizontal;
- remover o `.js` somente quando todos os consumidores forem atualizados.

### Fase D — separar núcleo horizontal e perfil SGC

1. **[concluído nesta rodada]** Definir uma configuração de projeto versionada, mantendo JSON como formato oficial de
   entrada, validando-o na borda e incluindo execuções substituíveis por categoria. O catálogo SGC continua sendo o
   fallback quando a categoria não é declarada.
2. Criar uma camada de adaptadores para:
   - layout de backend Java/Spring/Gradle;
   - layout de frontend Vue;
   - contratos OpenAPI;
   - coleta de qualidade;
   - políticas de nomenclatura, CDU, modais e arquitetura.
3. **[parcial nesta rodada]** Fazer o núcleo receber adaptadores por composição, sem `if (projeto === "sgc")` espalhado. A
   coleta de qualidade já aceita catálogos externos; os adaptadores de Gradle, npm, Playwright e políticas ainda precisam
   ser separados do orquestrador SGC.
4. **[concluído nesta rodada]** Criar `test/externo.test.ts` com um projeto fictício mínimo de Java/Vue, layout
   `servidor/src/main/java` e `cliente/src` configurado por JSON, e executar contra ele as auditorias de arquitetura
   backend/frontend, identificadores de teste e resíduos. O fixture confirma o recorte horizontal dessas famílias sem
   criar os diretórios `backend`, `frontend` ou `toolkit` do SGC; tarefas, políticas e adaptadores completos continuam
   pendentes, embora a coleta de qualidade já tenha uma fronteira de composição testada.
5. Documentar claramente quais comandos são `núcleo`, `perfil-sgc` ou `opcionais`.
6. Mover políticas do SGC para um diretório de perfil explícito somente quando o motor correspondente estiver estável;
   não reorganizar todos os arquivos antecipadamente.
7. Criar testes de caracterização para cada funcionalidade específica antes de separar seu motor horizontal; preservar
   o resultado no perfil SGC ou registrar explicitamente a correção de comportamento.

Critério de aceite: um segundo projeto consegue configurar raiz, globs, tarefas e políticas sem editar o código do
núcleo; as regras CDU e `AssuntosNotificacao` não aparecem nesse projeto fictício e continuam funcionando quando o perfil
SGC está ativo.

### Fase E — padronizar CLI e resultados

1. **[parcial nesta rodada]** Inventariar opções, defaults, mensagens e códigos de saída: as opções próprias foram
   catalogadas e não há aliases ingleses `--input`, `--output`, `--dir` ou `--directory`; ainda falta consolidar o
   inventário de defaults, mensagens e códigos de saída por família.
2. **[parcial nesta rodada]** Definir opções canônicas em português (`--entrada`, `--saida`, `--diretorio`, `--arquivo`,
   `--base`) e remover as formas antigas que não tenham valor semântico, atualizando todos os usos internos. Os
   primeiros recortes foram aplicados em `projeto arvore-linhas`, com `--profundidade`, `--minimo-linhas` e
   `--excluir-testes`, nas auditorias de cobertura, com `--minimo`, e na substituição documentada de `--dir` por
   `--diretorio`; permanece a oportunidade de compartilhar parsing e validação sem esconder semântica específica.
3. Definir um envelope comum de resultado: versão do schema, status, resumo, violações, métricas, artefatos e avisos.
4. Separar stdout estruturado, stdout humano e stderr operacional.
5. Definir quando um comando retorna falha por violação encontrada versus erro de execução.
6. Adicionar `--json`/`--gravar` de forma consistente, sem inventar opções para comandos que não precisam delas.
7. Aplicar execução read-only padrão e opção positiva de persistência às famílias restantes; não reintroduzir
   `--sem-gravar` como compatibilidade sem um consumidor identificado.
8. Separar no catálogo da CLI comandos de auditoria, geração, manutenção e orquestração para tornar efeitos explícitos.

### Fase F — testes, documentação e distribuição

1. **[concluído nesta rodada]** Converter todos os testes para TypeScript estrito. O teste principal da CLI, os testes
   CDU, o fixture externo e o smoke de pacote passam em `tsconfig.testes.json`; `@types/fs-extra` formaliza a única
   dependência de tipos necessária ao teste principal.
2. **[parcial nesta rodada]** Dividir testes por domínio: os cenários de projeto, configuração, integração e qualidade
   já estão em `test/projeto.test.ts`, `test/configuracao.test.ts`, `test/integracao.test.ts` e `test/qualidade.test.ts`;
   ainda falta separar runtime, saída, backend, frontend e requisitos que permanecem no `test/sgc.test.ts`.
3. Manter testes comportamentais sobre a API pública; não testar métodos privados por reflexão ou acoplamento à
   implementação.
4. **[concluído nesta rodada]** Adicionar smoke test de instalação em diretório externo, incluindo a execução do binário
   do pacote; o modelo fonte + `tsx` é exercitado pelo launcher e não depende de `node_modules` hoisted.
5. Adicionar matriz de validação para Node `26.7+`, TypeScript 6 e as versões de Vitest/tsx usadas no workspace.
6. **[parcial nesta rodada]** Criar fixtures próprias do toolkit para Java/Spring, Vue e Markdown; o fixture externo
   Java/Vue já cobre o layout e os comandos estruturais, mas ainda faltam fixtures independentes para cobertura,
   contratos, OpenAPI e Markdown. Não usar a suíte do produto SGC como validação rotineira da modernização do toolkit.
7. Executar smoke tests sobre um recorte do SGC apenas quando necessário para provar que uma funcionalidade específica
   do perfil continua funcionando após a mudança do toolkit.
8. **[parcial nesta rodada]** Atualizar `toolkit/README.md` e exemplos a partir de uma fonte única de comandos. Os 42
   despachadores de arquivos já usam `lib/catalogo-comandos.ts`, a ajuda específica das folhas é encaminhada ao módulo
   e a ajuda da CLI é o inventário canônico; os comandos com ações e opções próprias ainda têm sua estrutura declarada
   em `sgc.ts` por exigirem lógica de registro específica.
9. O modelo de distribuição fonte + `tsx` está fechado; os testes TypeScript não entram no artefato distribuído. A
   implementação não possui arquivos JS, aliases ou fallbacks de transição; somente o launcher CJS mínimo permanece
   como adaptação do campo `bin` do npm.

## 7. Validação obrigatória por rodada

### Rodada focada

```bash
node --version
cd toolkit
npm run typecheck
npx vitest run test/sgc.test.ts test/projeto.test.ts test/configuracao.test.ts test/integracao.test.ts test/qualidade.test.ts test/cdus.test.ts test/externo.test.ts --reporter=dot --no-color
npm run build
cd ..
git diff --check
```

O comando focado deve apontar para os testes do módulo alterado assim que a suíte for dividida. Enquanto
`test/sgc.test.ts` continuar monolítico, a rodada acima é o mínimo seguro.

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

O último comando está aprovado; o launcher usa `tsx` e o mesmo contrato também é validado pela instalação isolada do
tarball em `test:pacote`.

### Verificações adicionais por classe de mudança

- **Configuração/caminhos**: executar contra projeto fixture fora do repositório e verificar que nenhum artefato foi lido
  ou gravado na raiz do SGC.
- **Auditoria**: comparar JSON e código de saída com fixture estável; confirmar ausência de escrita sem opção explícita.
- **Comando mutável**: testar prévia, execução, idempotência quando aplicável e preservação de conteúdo não relacionado.
- **Empacotamento**: usar `npm pack`, instalar o tarball em diretório temporário e executar o binário sem dependências
  hoisted do monorepo (`npm --prefix toolkit run test:pacote`).
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
