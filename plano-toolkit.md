# Plano de modernização do toolkit

## Objetivos

O trabalho abrange exclusivamente `toolkit/`. O SGC não consome nem depende do toolkit: seu workspace é alvo de
auditoria, fonte de políticas locais e massa de regressão para os comandos que operam sobre ele.

1. remover código temporário, obsoleto, redundante ou sem finalidade permanente;
2. separar capacidades horizontais de integrações de stack e políticas específicas do SGC;
3. simplificar e uniformizar diretórios, símbolos, comandos, opções e resultados;
4. manter toda a implementação em TypeScript e português brasileiro;
5. preservar as capacidades específicas do SGC que continuam úteis;
6. distribuir um pacote utilizável por outros projetos e por agentes sem depender do workspace do SGC.

Não existe requisito de compatibilidade retroativa. Nomes, caminhos, exports e formatos podem ser corrigidos diretamente.
A continuidade exigida é funcional, não histórica.

## Diretrizes

### Fronteiras

Cada capacidade mantida deve ter uma classificação explícita:

| Camada | Critério | Exemplos |
|---|---|---|
| Núcleo | algoritmo independente de projeto e stack | CLI, configuração, parser CDU, agregação de cobertura |
| Adaptador | integração parametrizável com linguagem, framework ou ferramenta | Gradle, npm, Vue, OpenAPI, Semgrep, JaCoCo |
| Perfil SGC | convenção, caminho ou política para auditar o SGC | notificações, views, modais, coesão e defaults SGC |

Não é necessário generalizar toda funcionalidade. Uma política SGC bem identificada é um resultado correto. Só promover
uma regra para núcleo ou adaptador quando o contrato horizontal estiver claro e puder ser testado sem carregar defaults
do SGC.

### Utilidade

- A ausência de imports não demonstra ausência de consumidores: autor e agentes usam a CLI e os relatórios diretamente.
- Manter um comando exige finalidade permanente explicável em uma frase, não uma referência no código.
- Testes não justificam código cuja finalidade desapareceu.
- Utilitários ocasionais podem permanecer quando resolvem um problema recorrente e estão identificados como tal.
- O histórico pertence ao Git; README e plano descrevem somente a interface e as decisões vigentes.

### Implementação e execução

- TypeScript é a única fonte; não manter JavaScript paralelo, wrappers ou aliases de transição.
- Código, símbolos, mensagens e documentação próprios usam português brasileiro.
- Vocabulário externo de bibliotecas e formatos permanece apenas nas bordas de leitura e integração.
- `toolkit/ferramentas.ts` roda diretamente com `tsx`; `dist/` serve apenas para verificar o build.
- Node 26.7 ou superior é o ambiente mínimo.
- Arquivos grandes só são divididos quando houver responsabilidades ou contratos realmente independentes.
- Não introduzir cache, concorrência ou novas abstrações sem medição ou caso de uso concreto.

### Segurança e contratos

- Auditorias e inventários são somente leitura por padrão.
- Persistência exige `--gravar`; remoção exige `--confirmar`, exceto quando gerar ou promover é a própria finalidade do
  comando.
- JSON limpo vai para stdout; diagnóstico operacional vai para stderr.
- Opções desconhecidas, valores ausentes e argumentos excedentes devem falhar.
- Ajuda, parser e catálogo descrevem a mesma gramática `<dominio> <recurso> <acao>`.
- Formatos persistidos ou consumidos por outro comando têm tipo, versão e validação de entrada.
- Funções de domínio retornam resultados ou lançam erros; somente a borda CLI altera `process.exitCode`.

### Reuso

- `configuracao-toolkit.json` é o contrato de configuração por projeto e `--base` identifica a raiz auditada.
- Motores horizontais recebem caminhos, políticas e executores explicitamente.
- Defaults locais ficam na borda ou em módulos nomeados como perfil SGC.
- O pacote publicado é `ferramentas-projeto`, o binário é `ferramentas` e a instalação isolada não depende do
  `node_modules` nem do layout do SGC.
- APIs programáticas são deliberadas e validadas por fixture ou consumidor isolado; não precisam ter imports prévios no
  repositório para serem legítimas.

## Situação atual

- A implementação do toolkit é TypeScript e executa diretamente com `tsx` em Node 26.
- A coleta de qualidade resolve `eslint`, Playwright e demais ferramentas locais pelos binários instalados; não há
  `npx` no código ou na documentação do toolkit.
- A CLI possui catálogo, preflight comum, opções em português e metadados separados de finalidade, decisão e efeitos.
- O pacote e o binário têm identidade neutra e o tarball é testado fora do workspace.
- Casos de uso foram reduzidos a `requisitos cdus inventariar` e `requisitos cdus auditar`; corpus, vocabulário, tipos,
  situações e fontes de mensagens são configuráveis, e há regressão com um segundo projeto.
- Cobertura Java/web e casos de uso possuem APIs horizontais publicadas.
- O domínio JaCoCo é horizontal; os entrypoints Java de cobertura foram identificados como perfil SGC porque aplicam
  exclusões locais sem receber uma política pela CLI. O uso externo continua disponível pela API com padrões explícitos.
- Motores de Semgrep, OpenAPI e sincronização de versão recebem entradas explícitas; defaults do SGC ficam nas bordas.
- O analisador de testes Java aceita política externa validada; as heurísticas de domínio SGC estão em política própria.
- Fotografias e relatórios próprios prioritários usam contratos versionados e campos em português/camelCase.
- A coleta de qualidade separa o motor de composição dos perfis SGC; a execução de tarefas permanece um comando distinto.
- Acessibilidade Playwright/Axe pertence a `e2e/`, fora do toolkit.

Uma execução representativa contra o workspace real mostrou que rapidez, contratos e testes não bastam. Os comandos
avaliados terminaram em poucos segundos, mas revelaram problemas de utilidade que passam a orientar o fechamento:

- `projeto ambiente verificar` conhece `e2e`, portas e arquivos do SGC; no estado atual é perfil SGC, não adaptador;
- `servidor arquitetura auditar` agora separa `pontosCriticos` de `alertas`; a saída humana usa `achados` sem chamar todo
  achado de ponto crítico;
- `servidor testes analisar` agora compara o pacote Java declarado no código, e não apenas o caminho relativo de cada
  raiz; correspondências no pacote esperado deixaram de ser classificadas como ambíguas;
- `cliente residuos auditar` agora separa `sinaisAtivos` de `violacoes`, e `resumo.classificacao` é `"inventario"`;
  `pontuacaoTotal` serve somente para ordenar itens, enquanto o gate continua em `cliente residuos validar`;
- `codigo cheiros auditar` agora é explicitamente uma fotografia de tendência, sem faixa de severidade; `any` em testes
  foi retirado do conjunto padrão porque dominava o ranking sem uma política contextual;
- `cliente arquitetura auditar` agora identifica sua saída como `classificacao: "politica-sgc"`; a pontuação serve para
  ordenar sinais e não para classificar a severidade global do cliente;
- os dois comandos CDU agora mostram amostras limitadas com arquivo, linha, regra, mensagem e sugestão quando aplicável;
  o inventário ordena frequências e exibe exemplos das duplicações, mantendo o JSON completo para agentes;
- `servidor contratos auditar` foi rápido, específico e teve resultado inequívoco, servindo como referência de comando
  bem delimitado;
- exemplos baseados em `npx tsx` acrescentam ruído npm. No workspace, a entrada estável deve ser um script npm; no pacote
  instalado, deve ser o binário `ferramentas`.

Auditores extensos de arquitetura Vue e regras de views, modais, notificações, coesão e erros contêm políticas locais.
Eles devem ser classificados como perfil SGC, e não parametrizados automaticamente. A fixture externa continua sendo
exigida apenas para capacidades que permanecerem declaradas como adaptáveis.

## Escopo restante obrigatório

### Recorte 1 — inventário funcional (concluído)

O catálogo e o README agora registram finalidade, camada, efeitos e decisão para as 38 folhas da CLI. A revisão confirmou:

- o corretor FQN é útil como utilitário ocasional e funciona em raiz Java externa configurada;
- inventários de símbolos, identificadores, árvore de linhas, CDU e OpenAPI têm valor sob demanda e não devem ser gates
  permanentes;
- Semgrep executa regras estruturais configuráveis, enquanto cheiros mede tendências heurísticas; cobertura unificada e
  ramificações respondem a perguntas diferentes e permanecem complementares;
- não há módulos ou exports órfãos segundo o Knip, e todos os arquivos catalogados existem;
- os entrypoints Java de cobertura são perfil SGC por aplicarem exclusões locais; o domínio JaCoCo permanece horizontal;
- a execução documentada usa scripts npm no workspace, o binário `ferramentas` no pacote e não usa `npx tsx`.

Critério de saída atendido: todo comando tem finalidade e camada inequívocas, e nenhuma decisão funcional ficou marcada como
“avaliar”, “revisar” ou “confirmar”.

### Recorte 2 — fechar somente as fronteiras aprovadas

Corrigir apenas violações encontradas no recorte 1:

- mover literals e caminhos SGC ainda presentes em motores declarados horizontais para política ou borda;
- reclassificar como perfil SGC o que não justificar parametrização;
- separar CLI, persistência ou formatação de um motor apenas quando isso impedir reuso ou teste isolado;
- garantir que os dois comandos CDU e o analisador Java continuem funcionando com configuração externa, incluindo a
  correspondência por pacote já corrigida;
- substituir testes que apenas reproduzem a implementação por testes semânticos das decisões aprovadas;
- atualizar contratos públicos deliberados sem aliases de compatibilidade.

Critério de saída: nenhum motor classificado como núcleo ou adaptador depende de vocabulário, caminho ou regra de negócio
SGC; entrypoints adaptáveis podem oferecer uma conveniência SGC somente quando aceitam configuração explícita para outro
projeto. Capacidades locais continuam executáveis contra o workspace do SGC.

### Recorte 3 — auditoria final e encerramento

- confrontar ajuda, catálogo, README, exports e arquivos empacotados;
- executar a instalação isolada e todas as validações do toolkit;
- repetir a amostra contra o SGC e confrontar manualmente os principais achados com o código apontado;
- remover artefatos, fixtures e documentação que tenham perdido a finalidade;
- registrar melhorias opcionais em uma seção curta de backlog, sem tratá-las como bloqueadoras;
- encerrar com worktree limpo, commit e push.

Critério de saída: todos os critérios de término abaixo estão comprovados e não há pendência obrigatória aberta.

## Critérios de término

A modernização termina quando todos estes itens forem verdadeiros:

- [ ] todos os comandos públicos têm finalidade atual, camada e efeitos documentados;
- [ ] não há comando decidido como redundante, temporário ou obsoleto ainda presente;
- [ ] não há JavaScript legado, alias de compatibilidade ou segundo caminho de execução;
- [ ] nomes e contratos próprios do toolkit seguem TypeScript e português brasileiro;
- [ ] motores classificados como núcleo/adaptador não importam política ou caminho SGC;
- [ ] políticas para auditar o SGC estão identificadas e continuam funcionando contra seu workspace;
- [ ] CDU e análise de testes Java funcionam com fixture externa sem editar o toolkit;
- [ ] uma execução representativa contra o SGC não produz severidade contraditória, ambiguidade sistemática ou destaque
  sem violação correspondente;
- [ ] cada auditor mantido possui testes que demonstram acerto semântico, e não apenas execução, schema ou snapshot;
- [ ] ajuda, parser, catálogo e README concordam sobre a superfície pública;
- [ ] o tarball instalado isoladamente executa o binário e as APIs públicas suportadas;
- [ ] testes, typechecks, lint, Knip, build e `git diff --check` passam;
- [ ] não restam itens obrigatórios nos três recortes acima.

“Não existir qualquer melhoria possível” não é critério de término. Cobertura total, tamanho máximo de arquivo,
generalização de toda política e padronização de formatos externos também não são requisitos.

## Regra contra rabbit holes

Um novo achado só bloqueia a conclusão se violar pelo menos um destes pontos:

1. correção ou segurança de uma funcionalidade mantida;
2. funcionamento dos comandos que auditam o workspace do SGC;
3. reuso de uma capacidade já declarada horizontal;
4. consistência pública de CLI, configuração, pacote ou contrato persistido;
5. um critério de término listado acima.

Caso contrário, o achado vai para backlog e não amplia o recorte ativo. Em particular:

- não parametrizar uma heurística apenas porque ela contém nomes locais; é válido reclassificá-la como perfil SGC;
- não criar API pública, camada ou arquivo apenas por possibilidade futura;
- não dividir código por contagem de linhas;
- não reescrever um relatório sem consumidor automático ou incompatibilidade concreta;
- não perseguir thresholds arbitrários de cobertura;
- não reabrir uma decisão encerrada sem nova evidência de uso, falha ou necessidade externa.

Cada recorte deve caber em uma decisão funcional coesa, terminar validado e receber commit/push. Se surgir trabalho maior,
ele substitui um recorte futuro ou vai para backlog; não vira uma cadeia ilimitada de sub-recortes.

## Testes de utilidade e correção semântica

Todo auditor mantido deve ter uma especificação comportamental curta que diga o que constitui achado, não achado e
severidade. Os testes devem partir dessa especificação, sem calcular o resultado esperado repetindo a fórmula interna.

Para cada regra relevante, exigir:

- exemplo positivo mínimo, no qual o problema existe e é localizado com motivo compreensível;
- exemplo negativo próximo, no qual uma construção legítima não é marcada;
- caso limítrofe para thresholds ou classificações;
- teste de invariantes do relatório, como “ponto crítico que declara severidade é crítico”, “inventário declara quando a
  pontuação não é severidade”, “violação destacada contém ao menos um motivo” e “teste no pacote correspondente não é
  ambíguo”;
- fixture com vários arquivos para detectar efeitos proporcionais ao tamanho, duplicações e falsos positivos sistêmicos;
- teste da saída humana, verificando que ela informa arquivo, motivo e próxima decisão sem exigir leitura do JSON completo;
- regressão focada derivada de cada erro descoberto no reality check.

Snapshots e validações de schema continuam úteis, mas apenas como testes de contrato. Não substituem assertions sobre o
significado do resultado. Cobertura de linhas também não comprova qualidade da heurística.

Além das fixtures sintéticas, o encerramento exige uma amostra de caracterização contra o SGC real. Os principais achados
devem ser confrontados com os arquivos apontados; resultados incorretos geram uma regressão mínima antes da correção. O
corpus real não deve ser copiado integralmente para os testes nem congelado em snapshots gigantes.

## Validação

Executar em série ao encerrar cada recorte com código:

```bash
npm --prefix toolkit run test -- --maxWorkers=1
npm --prefix toolkit run test:coverage -- --maxWorkers=1
npm --prefix toolkit run test:pacote
npm --prefix toolkit run typecheck
npm --prefix toolkit run typecheck:testes
npm --prefix toolkit run lint
npm --prefix toolkit run deps:audit
npm --prefix toolkit run build
git diff --check
```

Para alteração exclusivamente documental, bastam validação dos links/referências afetados, consistência com o catálogo e
`git diff --check`. Testes focados adicionais são exigidos somente quando o risco do recorte justificar.

## Backlog após a conclusão

O backlog não bloqueia o encerramento e deve permanecer pequeno. Só adicionar um item com evidência concreta e condição
clara para retomada, por exemplo: “generalizar o auditor de arquitetura Vue quando um segundo projeto precisar das mesmas
heurísticas”. Ideias sem caso de uso permanecem fora do plano.
