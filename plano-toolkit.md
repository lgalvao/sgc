# Plano de modernização do toolkit

## Objetivos

O trabalho abrange exclusivamente `toolkit/`. O SGC não consome nem depende do toolkit: seu workspace é alvo de
auditoria, fonte de políticas locais e corpus real de validação.

1. remover código temporário, obsoleto, redundante ou sem finalidade permanente;
2. separar capacidades horizontais, integrações de stack e políticas específicas do SGC;
3. simplificar e uniformizar diretórios, símbolos, comandos, opções e resultados;
4. manter a implementação em TypeScript e português brasileiro;
5. preservar as capacidades específicas do SGC que continuam úteis;
6. distribuir um pacote utilizável pelo autor, por agentes e por outros projetos sem depender do workspace do SGC.

Não existe requisito de compatibilidade retroativa. A continuidade exigida é funcional, não histórica.

## Diretrizes vigentes

### Utilidade e escopo

- Uso humano ou por agentes é consumo legítimo; ausência de imports não torna um comando inútil.
- Todo comando mantido precisa ter finalidade permanente explicável em uma frase.
- Testes não justificam uma funcionalidade cuja finalidade desapareceu.
- Utilitários ocasionais podem permanecer quando resolvem um problema recorrente e são identificados como tal.
- O histórico pertence ao Git. Este plano descreve apenas decisões vigentes e trabalho ainda necessário.
- O SGC é corpus de validação do toolkit, não parte da implementação nem consumidor dela.

### Fronteiras

| Camada | Critério | Exemplos |
|---|---|---|
| Núcleo | algoritmo independente de projeto e stack | CLI, configuração, parser CDU, agregação de cobertura |
| Adaptador | integração parametrizável com linguagem, framework ou ferramenta | Gradle, npm, Vue, OpenAPI, Semgrep, JaCoCo |
| Perfil SGC | convenção, caminho ou política usada para auditar o SGC | notificações, views, modais, coesão e defaults SGC |

Uma política SGC bem identificada é um resultado correto. Ela só deve ser promovida a núcleo ou adaptador quando houver
contrato horizontal claro e teste externo que não carregue defaults do SGC.

### Implementação e execução

- TypeScript é a única fonte de implementação.
- `binarios/ferramentas.cjs` é a única exceção: lançador mínimo necessário ao `bin` do npm, sem regra de domínio.
- Código, símbolos, mensagens e documentação próprios usam português brasileiro.
- Vocabulário de bibliotecas e formatos externos permanece apenas nas bordas de integração.
- `toolkit/ferramentas.ts` executa diretamente com `tsx`; `dist/` existe somente para validar o build.
- Node 26.7 ou superior é o ambiente mínimo.
- Não criar aliases, wrappers de transição ou implementações paralelas.
- Não dividir arquivos, introduzir cache, concorrência ou abstrações sem responsabilidade independente ou evidência real.

### Segurança e contratos

- Auditorias e inventários são somente leitura por padrão.
- Persistência exige `--gravar`; remoção exige `--confirmar`, salvo quando persistir é a finalidade explícita do comando.
- JSON limpo vai para stdout; diagnóstico operacional vai para stderr.
- Opções desconhecidas, valores ausentes e argumentos excedentes falham.
- Ajuda, parser e catálogo seguem a gramática `<dominio> <recurso> <acao>` e devem permanecer sincronizados.
- Formatos próprios persistidos ou consumidos por outro comando têm tipo, versão e validação de entrada.
- Funções de domínio retornam resultados ou lançam erros; somente a borda CLI altera `process.exitCode`.

### Reuso

- `configuracao-toolkit.json` é o contrato de configuração por projeto; `--base` identifica a raiz auditada.
- Motores horizontais recebem caminhos, políticas e executores explicitamente.
- Defaults locais ficam na borda ou em módulos identificados como perfil SGC.
- O pacote é `ferramentas-projeto` e o binário é `ferramentas`.
- A instalação isolada não depende do `node_modules`, da configuração nem do layout do SGC.
- APIs programáticas são deliberadas e validadas por fixture ou consumidor isolado; não precisam de imports prévios no
  repositório para serem legítimas.

## Situação atual

A modernização estrutural está concluída:

- CLI, catálogo e README registram finalidade, camada, decisão e efeitos dos 38 comandos públicos;
- não há comando classificado como temporário, redundante ou pendente de decisão;
- a implementação é TypeScript e executa diretamente com `tsx` em Node 26;
- o pacote e o binário têm identidade neutra e o tarball é testado em consumidor isolado;
- casos de uso foram consolidados em `requisitos cdus inventariar` e `requisitos cdus auditar`;
- CDU, cobertura Java/web e análise de testes Java possuem contratos reutilizáveis e testes com projeto externo;
- Semgrep, OpenAPI, JaCoCo, V8 e sincronização de versão recebem entradas explícitas em seus motores;
- defaults e políticas do SGC estão nas bordas ou em módulos identificados como perfil SGC;
- acessibilidade Playwright/Axe pertence a `e2e/`, fora do toolkit;
- um reality check anterior corrigiu classificações contraditórias, ambiguidades sistemáticas e destaques sem violação;
- testes, pacote isolado, typechecks, lint, Knip, build e `git diff --check` passam.

Essa situação não comprova, por si só, a correção semântica uniforme de todos os auditores. A revisão final encontrou
auditores com bons testes comportamentais e outros ainda cobertos principalmente por execução, schema, persistência ou
um único exemplo positivo. Portanto, a modernização não deve ser declarada encerrada enquanto o recorte abaixo estiver
aberto.

## Reality check de encerramento

### Contexto da execução

Os 38 comandos públicos foram exercitados contra o workspace real do SGC. Operações destrutivas ficaram em simulação;
OpenAPI diff e promoção de baseline usaram arquivos temporários; a exportação tentou o endpoint padrão sem iniciar o
servidor. A primeira passagem executou comandos independentes em paralelo, inclusive `qualidade coletar` e `qualidade
tarefas executar rapido`. Como ambas acionam Gradle/npm e compartilham artefatos, a fotografia contaminada por essa
concorrência foi descartada e `qualidade coletar --perfil rapido` foi repetido isoladamente.

Na repetição isolada, testes do servidor (1.588), cobertura, cobertura do cliente, lint, typecheck, resíduos e arquitetura
passaram. A fotografia continuou vermelha apenas por duplicações globais de `data-testid`. O ambiente foi restaurado com
`npm install` depois que a rodada de tarefas removeu dependências necessárias à CLI. Nenhuma alteração versionada ficou
na worktree.

### Achados obrigatórios

#### 1. Execução de tarefas pode invalidar o próprio toolkit

**Evidência:** durante `qualidade tarefas executar rapido`, a tarefa `frontend:install` executou instalação npm. Ao final,
`commander` deixou de ser resolvido por `toolkit/ferramentas.ts`; comandos subsequentes falharam com
`ERR_MODULE_NOT_FOUND` até executar `npm install` na raiz. A execução também usou Node 26.4 em subprocessos enquanto a
CLI principal usava Node 26.7, produzindo avisos de engine.

**Cautela:** a descoberta ocorreu numa rodada concorrente. Antes de corrigir, reproduzir isoladamente e identificar se a
causa é a tarefa Gradle, o uso de `npm --prefix`, a topologia de workspaces ou a herança de PATH/Node. O teste de regressão
deve provar que `ferramentas --help` continua executável depois da tarefa e que todos os subprocessos usam o Node mínimo.

**Recomendação:** impedir que uma tarefa de qualidade reinstale ou remova dependências compartilhadas, ou executar a
instalação em escopo que preserve o workspace. Falha ou engine incompatível deve aparecer como erro da tarefa, não como
quebra tardia da próxima chamada.

#### 2. Unicidade global de `data-testid` produz gate enganoso

**Evidência:** `cliente identificadores-teste listar-duplicados` encontrou três valores em 273 identificadores e fez a
fotografia geral ficar vermelha, embora as outras sete verificações tenham passado. Os casos incluem seletores iguais em
telas/componentes distintos e cards repetidos por `v-for`; reutilização não implica colisão no mesmo DOM nem teste
ambíguo.

**Recomendação:** decidir explicitamente o contrato. Preferência: manter a listagem global como inventário e só falhar por
duplicidade comprovadamente ambígua no mesmo escopo renderizado. Se essa análise estática não for confiável, retirar o
comando do gate de qualidade ou exigir política/exceção configurada. A fotografia não pode declarar vermelho por uma
heurística sem violação demonstrável.

#### 3. Priorização de testes aceita entrada incompatível silenciosamente

**Evidência:** o JSON válido de `servidor testes analisar`, salvo com extensão `.out`, foi tratado como Markdown e gerou
P1/P2/P3 vazios sem erro. O mesmo conteúdo com extensão `.json` produziu 6 itens P1, 8 P2 e 4 P3.

**Recomendação:** detectar o formato pelo conteúdo ou exigir formato explícito; em ambos os casos, entrada não reconhecida
deve falhar. Adicionar regressões para JSON com extensão não convencional, Markdown válido e conteúdo inválido que hoje
resulta silenciosamente vazio.

#### 4. JSON completo é impraticável como resposta padrão para agentes

**Evidência:** na base real, as respostas chegaram aproximadamente a 2,1 MB em símbolos, 858 KB no gate arquitetural,
424 KB na validação de resíduos, 392 KB na auditoria de resíduos, 366 KB na análise de testes, 264 KB na consistência de
nomes, 184 KB na cobertura do servidor e 104 KB na auditoria CDU.

**Recomendação:** definir um contrato uniforme de resumo estruturado, preferencialmente `--json-resumido`, preservando
`--json` como saída completa quando necessária. O resumo deve conter totais, classificação, principais achados limitados,
motivos e indicação de truncamento; não deve despejar módulos, símbolos, XMLs ou todos os arquivos. Priorizar primeiro os
comandos acima de 250 KB e a saída de `qualidade resumo`.

#### 5. Auditoria de dependências ainda contém ruído evitável

**Evidência:** a auditoria funcionou e separou achados de falhas, mas apresentou TypeScript 7 para toolkit, frontend e
e2e apesar da decisão explícita de permanecer em TypeScript 6 enquanto o ecossistema for incompatível. O Knip também
apontou `tsx` como binário não declarado na raiz e no frontend. Não foram encontradas vulnerabilidades npm.

**Recomendação:** configurar uma exclusão documentada e temporária para a major 7, com condição clara de remoção, sem
ocultar atualizações da série 6. Verificar se os usos de `tsx` devem virar dependências declaradas ou se os scripts podem
usar a dependência do escopo correto. O comando deve continuar retornando código distinto quando houver atualizações,
sem tratar achado esperado como falha de execução.

#### 6. Árvore de linhas tem saída padrão excessiva

**Evidência:** `projeto arvore-linhas` imprimiu a árvore integral de cerca de 246 mil linhas do repositório. As opções
`--profundidade` e `--minimo-linhas` resolvem o problema, mas exigem conhecimento prévio e não há JSON resumido.

**Recomendação:** adotar defaults humanos úteis de profundidade/mínimo ou apresentar primeiro um resumo com orientação
para expandir. Não transformar o comando em auditor nem adicionar persistência sem caso de uso.

### Resultados úteis que não exigem correção imediata

- OpenAPI diff e promoção de baseline funcionaram em arquivos temporários. A exportação falhou porque o servidor não
  estava ativo e apresentou orientação adequada; isso é pré-condição esperada, não defeito.
- `servidor arquitetura auditar` encontrou 3 críticos e 7 alertas com arquivos e motivos coerentes.
- CDU encontrou 0 erros, 35 avisos e 64 referências imprecisas; a saída completa é útil, mas precisa do resumo recomendado.
- Contratos, notificações, views, modais e Semgrep não encontraram violações.
- O corretor FQN encontrou 114 arquivos alteráveis em simulação, confirmando seu valor como utilitário ocasional; nenhuma
  alteração deve ser aplicada como parte deste recorte.
- `projeto ambiente verificar` passou nas 20 verificações; sincronização de versão confirmou que `1.3.8` já está alinhada.

### Ordem de execução recomendada

1. reproduzir isoladamente e corrigir a invalidação de dependências/Node em `qualidade tarefas executar`;
2. corrigir o contrato e a severidade de identificadores duplicados;
3. corrigir a detecção e validação de formato em `servidor testes priorizar`;
4. introduzir resumos JSON consistentes, começando pelos maiores resultados;
5. reduzir ruído conhecido da auditoria de dependências;
6. ajustar a saída padrão da árvore de linhas;
7. construir a matriz semântica usando cada problema confirmado acima como regressão inicial;
8. repetir a amostra final sequencialmente, sem concorrência entre comandos que compartilhem npm, Gradle ou artefatos.

## Recorte final — comprovação semântica

### Objetivo

Comprovar que os auditores mantidos respondem corretamente às perguntas que anunciam, sem reabrir arquitetura, catálogo
ou generalização já encerrados.

### Passos obrigatórios

1. Resolver os achados obrigatórios do reality check na ordem recomendada acima.
2. Criar uma matriz `auditor -> regras relevantes -> evidências existentes -> lacunas`.
3. Priorizar regras que produzem gate, severidade, pontuação, violação ou recomendação acionável.
4. Completar somente as evidências necessárias para cada tipo de regra:
   - achado positivo mínimo, com arquivo e motivo corretos;
   - negativo próximo para heurísticas sujeitas a falso positivo;
   - limite inferior/superior quando houver threshold ou mudança de classificação;
   - invariantes entre resumo, severidade, lista destacada e motivos;
   - mais de um arquivo quando contagem, duplicação ou escala alterar o resultado;
   - saída humana quando ela for o principal produto do comando.
5. Não exigir mecanicamente todos os casos acima de toda regra. A matriz deve registrar `não aplicável` com justificativa
   curta quando a natureza da regra não exigir uma dimensão.
6. Executar uma amostra curta e representativa no SGC, cobrindo ao menos:
   - um auditor de servidor;
   - um auditor de cliente;
   - uma capacidade horizontal configurável;
   - CDU;
   - um orquestrador ou agregador.
7. Confrontar os principais resultados com os arquivos apontados. Todo falso positivo, falso negativo ou texto enganoso
   confirmado gera primeiro uma regressão mínima e depois a correção.

### Limites do recorte

- Não buscar cobertura total nem um percentual arbitrário.
- Não testar detalhes internos, fórmulas copiadas da implementação ou snapshots extensos.
- Não generalizar políticas SGC sem necessidade de um segundo projeto.
- Não reabrir comandos já decididos sem nova evidência de inutilidade ou incorreção.
- Inventários, transformadores e orquestradores não são auditores: recebem testes adequados ao seu contrato, não uma
  matriz artificial de falsos positivos e severidade.
- Melhorias sem relação com um critério de término vão para backlog e não ampliam este recorte.

### Critério de saída do recorte

O recorte termina quando a matriz não tiver lacuna obrigatória, as regressões descobertas estiverem corrigidas e a amostra
real não apresentar classificação contraditória, ambiguidade sistemática ou destaque sem motivo correspondente.

## Critérios de término

- [x] todos os comandos públicos têm finalidade atual, camada e efeitos documentados;
- [x] não há comando decidido como redundante, temporário ou obsoleto ainda presente;
- [x] não há JavaScript legado, alias de compatibilidade ou implementação paralela;
- [x] nomes e contratos próprios seguem TypeScript e português brasileiro;
- [x] motores classificados como núcleo ou adaptador não importam política ou caminho SGC;
- [x] políticas para auditar o SGC estão identificadas e continuam funcionando contra seu workspace;
- [x] CDU e análise de testes Java funcionam com projeto externo sem editar o toolkit;
- [x] ajuda, parser, catálogo e README concordam sobre a superfície pública;
- [x] o tarball instalado isoladamente executa o binário e as APIs públicas suportadas;
- [ ] tarefas de qualidade preservam a instalação do toolkit e usam a versão mínima de Node nos subprocessos;
- [ ] gates e status gerais não falham por inventários ou heurísticas sem violação demonstrável;
- [ ] entradas estruturadas incompatíveis falham em vez de produzir resultado vazio;
- [ ] comandos de grande volume oferecem resumo JSON acionável para agentes;
- [ ] auditoria de dependências respeita a decisão temporária sobre TypeScript 7 sem ocultar atualizações da série 6;
- [ ] a matriz semântica está completa e não contém lacuna obrigatória;
- [ ] a amostra final contra o SGC foi confrontada com o código e não revelou resultado enganoso sem regressão;
- [x] testes, typechecks, lint, Knip, build e `git diff --check` passam.

“Não existir qualquer melhoria possível” não é critério de término. Cobertura total, tamanho máximo de arquivo,
generalização de toda política e padronização de formatos externos também não são requisitos.

## Regra contra rabbit holes

Um novo achado só bloqueia a conclusão se violar correção ou segurança, funcionamento contra o SGC, reuso de capacidade
declarada horizontal, consistência pública ou um critério de término acima. Caso contrário, fica fora do recorte.

Cada correção deve ser pequena, semanticamente coesa, validada e seguida de commit/push. Um achado maior substitui uma
etapa futura ou vai para backlog; não cria uma cadeia ilimitada de sub-recortes.

## Validação

Ao encerrar uma rodada com código:

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

Para alteração exclusivamente documental, validar referências afetadas, consistência com catálogo/README e executar
`git diff --check`.

## Backlog após a conclusão

O backlog não bloqueia o encerramento e deve permanecer pequeno. Só registrar item com evidência concreta e condição
clara para retomada. Ideias sem caso de uso ficam fora do plano.
