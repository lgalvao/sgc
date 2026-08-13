# Plano de modernização do toolkit

## Objetivos

O trabalho abrange exclusivamente `toolkit/`. O restante do SGC participa apenas como consumidor, fixture e fonte de
políticas locais.

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
| Perfil SGC | convenção, caminho ou política de negócio local | notificações, views, modais, coesão e defaults SGC |

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
- A CLI possui catálogo, preflight comum, opções em português e metadados separados de finalidade e efeitos.
- O pacote e o binário têm identidade neutra e o tarball é testado fora do workspace.
- Casos de uso foram reduzidos a `requisitos cdus inventariar` e `requisitos cdus auditar`; corpus, vocabulário, tipos,
  situações e fontes de mensagens são configuráveis, e há regressão com um segundo projeto.
- Cobertura Java/web e casos de uso possuem APIs horizontais publicadas.
- Motores de Semgrep, OpenAPI e sincronização de versão recebem entradas explícitas; defaults do SGC ficam nas bordas.
- O analisador de testes Java aceita política externa validada; as heurísticas de domínio SGC estão em política própria.
- Fotografias e relatórios próprios prioritários usam contratos versionados e campos em português/camelCase.
- A coleta de qualidade separa o motor de composição dos perfis SGC; a execução de tarefas permanece um comando distinto.
- Acessibilidade Playwright/Axe pertence a `e2e/`, fora do toolkit.

Ainda falta uma decisão final e limitada sobre a superfície remanescente. Em especial, auditores extensos de arquitetura
Vue e regras de views, modais, notificações, coesão e erros contêm políticas locais. Eles devem ser classificados como
perfil SGC, e não parametrizados automaticamente. `cliente arquitetura auditar` só continuará como adaptável se uma
fixture externa provar que suas regras gerais funcionam sem conhecimento do SGC.

## Escopo restante obrigatório

### Recorte 1 — fechar o inventário funcional

Revisar uma única vez todos os comandos públicos e registrar no catálogo e no README:

- finalidade permanente em uma frase;
- classificação como núcleo, adaptador ou perfil SGC;
- efeitos de arquivo, remoção, rede e subprocessos;
- decisão explícita de manter, fundir ou remover.

Resolver nessa revisão as dúvidas remanescentes, sem abrir novas famílias de comandos:

- classificar corretamente arquitetura Vue, views, modais, notificações, coesão e ramificações de erro;
- confirmar se `projeto diagnostico`, o corretor FQN e os inventários ainda têm finalidade ocasional clara;
- verificar se cheiros/Semgrep e cobertura/ramificações continuam complementares na saída atual;
- remover módulos e exports órfãos revelados pelas decisões.

Critério de saída: todo comando do catálogo tem finalidade e camada inequívocas; não resta decisão funcional marcada como
“avaliar”, “revisar” ou “confirmar”.

### Recorte 2 — fechar somente as fronteiras aprovadas

Corrigir apenas violações encontradas no recorte 1:

- mover literals e caminhos SGC ainda presentes em motores declarados horizontais para política ou borda;
- reclassificar como perfil SGC o que não justificar parametrização;
- separar CLI, persistência ou formatação de um motor apenas quando isso impedir reuso ou teste isolado;
- garantir que os dois comandos CDU e o analisador Java continuem funcionando com configuração externa;
- atualizar contratos públicos deliberados sem aliases de compatibilidade.

Critério de saída: nenhuma capacidade classificada como núcleo ou adaptador depende de vocabulário, caminho ou regra de
negócio SGC; capacidades locais continuam executáveis pelo perfil SGC.

### Recorte 3 — auditoria final e encerramento

- confrontar ajuda, catálogo, README, exports e arquivos empacotados;
- executar a instalação isolada e todas as validações do toolkit;
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
- [ ] políticas SGC mantidas estão identificadas e continuam funcionando;
- [ ] CDU e análise de testes Java funcionam com fixture externa sem editar o toolkit;
- [ ] ajuda, parser, catálogo e README concordam sobre a superfície pública;
- [ ] o tarball instalado isoladamente executa o binário e as APIs públicas suportadas;
- [ ] testes, typechecks, lint, Knip, build e `git diff --check` passam;
- [ ] não restam itens obrigatórios nos três recortes acima.

“Não existir qualquer melhoria possível” não é critério de término. Cobertura total, tamanho máximo de arquivo,
generalização de toda política e padronização de formatos externos também não são requisitos.

## Regra contra rabbit holes

Um novo achado só bloqueia a conclusão se violar pelo menos um destes pontos:

1. correção ou segurança de uma funcionalidade mantida;
2. funcionamento atual do perfil SGC;
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
