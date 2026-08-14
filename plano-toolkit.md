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

A modernização estrutural e a ergonomia dos maiores resultados estão concluídas; resta apenas a validação final do pacote
e a confirmação de que catálogo, README e plano refletem o contrato atual:

- CLI e catálogo registram finalidade, camada, decisão e efeitos; README documenta a superfície e os contratos atuais dos
  38 comandos públicos;
- não há comando classificado como temporário, redundante ou pendente de decisão;
- a implementação é TypeScript e executa diretamente com `tsx` em Node 26;
- o pacote e o binário têm identidade neutra e o tarball é testado em consumidor isolado;
- casos de uso foram consolidados em `requisitos cdus inventariar` e `requisitos cdus auditar`;
- CDU, cobertura Java/web e análise de testes Java possuem contratos reutilizáveis e testes com projeto externo;
- Semgrep, OpenAPI, JaCoCo, V8 e sincronização de versão recebem entradas explícitas em seus motores;
- defaults e políticas do SGC estão nas bordas ou em módulos identificados como perfil SGC;
- acessibilidade Playwright/Axe pertence a `e2e/`, fora do toolkit;
- os gates de identificadores não transformam repetição textual global em falha sem escopo demonstrável;
- a priorização de testes rejeita entrada estruturada incompatível em vez de produzir backlog vazio;
- os produtores de maior volume identificados no reality check oferecem `--json-resumido`: símbolos, análise de testes do
  servidor, gate arquitetural, consistência de nomenclatura, qualidade, resíduos, cobertura e CDU;
- a árvore de linhas usa profundidade 3 e mínimo de 500 linhas por padrão, com expansão explícita pelas opções atuais;
- testes, pacote isolado, typechecks, lint, Knip, build e `git diff --check` passam.

Essa situação não comprova, por si só, a correção semântica uniforme de todos os auditores. A revisão final encontrou
auditores com bons testes comportamentais e outros ainda cobertos principalmente por execução, schema, persistência ou
um único exemplo positivo. Portanto, a modernização não deve ser declarada encerrada enquanto o recorte abaixo estiver
aberto.

## Reality check de encerramento

Os 38 comandos públicos foram exercitados contra o workspace real do SGC; operações destrutivas ficaram em simulação e
os fluxos OpenAPI usaram arquivos temporários quando necessário. Os achados abaixo são evidência para decisões do toolkit,
não histórico operacional nem autorização para alterar o SGC.

### Achados obrigatórios

#### 1. Execução de tarefas pode invalidar o próprio toolkit

**Evidência:** durante `qualidade tarefas executar rapido`, a tarefa `frontend:install` executou instalação npm. Ao final,
`commander` deixou de ser resolvido por `toolkit/ferramentas.ts`; comandos subsequentes falharam com
`ERR_MODULE_NOT_FOUND` até executar `npm install` na raiz. A execução também usou Node 26.4 em subprocessos enquanto a
CLI principal usava Node 26.7, produzindo avisos de engine.

**Fronteira:** a serialização das tarefas pertence ao toolkit, mas a tarefa que reinstala dependências e a versão de Node
herdada pertencem ao perfil externo do projeto. O toolkit só deve receber uma proteção adicional se ela puder ser testada
em fixture isolada sem codificar o layout do SGC.

**Decisão implementada:** a execução permanece serial, os subprocessos recebem o diretório do `process.execPath` no início
do `PATH`, e a instalação do toolkit é verificada antes e depois de cada tarefa. Se uma tarefa externa remover dependência
do toolkit, a execução para antes da próxima tarefa com erro explícito. A proteção é testada com tarefas injetadas e não
ensina ao núcleo como o SGC instala npm/Gradle.

#### 2. Unicidade global de `data-testid` produz gate enganoso

**Evidência:** `cliente identificadores-teste listar-duplicados` encontrou três valores em 273 identificadores. Os casos
incluem seletores iguais em telas/componentes distintos e cards repetidos por `v-for`; reutilização não implica colisão no
mesmo DOM nem teste ambíguo.

**Decisão implementada:** a listagem global permanece inventário, com código de saída zero; a fotografia não falha por
repetição textual sem uma regra de escopo renderizado. Uma análise futura de ambiguidade precisa ser uma política
separada e testável.

#### 3. Priorização de testes aceita entrada incompatível silenciosamente

**Evidência:** o JSON válido de `servidor testes analisar`, salvo com extensão `.out`, foi tratado como Markdown e gerou
P1/P2/P3 vazios sem erro. O mesmo conteúdo com extensão `.json` produziu 6 itens P1, 8 P2 e 4 P3.

**Decisão implementada:** o conteúdo estruturado é detectado mesmo com extensão não convencional; Markdown válido continua
aceito; conteúdo que aparenta ser estruturado mas é inválido falha. As três situações têm regressões.

#### 4. JSON completo é impraticável como resposta padrão para agentes

**Evidência:** na base real, as respostas chegaram aproximadamente a 2,1 MB em símbolos, 858 KB no gate arquitetural,
424 KB na validação de resíduos, 392 KB na auditoria de resíduos, 366 KB na análise de testes, 264 KB na consistência de
nomes, 184 KB na cobertura do servidor e 104 KB na auditoria CDU.

**Decisão implementada:** os comandos de símbolos, testes do servidor, gate arquitetural, nomenclatura, qualidade, resíduos,
cobertura e CDU oferecem `--json-resumido` com `versaoResumo: 1`, `truncado`, limite explícito, totais e amostras
acionáveis. `--json` continua completo; listas de módulos, arquivos, linhas de cobertura, métricas detalhadas e documentos
integrais ficam fora do resumo.

#### 5. Auditoria de dependências precisa separar política de projeto e ruído de ferramenta

**Evidência:** a auditoria funciona e separa achados de falhas. `npm outdated` apresenta TypeScript 7 para toolkit,
frontend e e2e apesar da decisão de permanecer na série 6; o Gradle já filtra as configurações relevantes do build e
produz um conjunto limitado de dependências do projeto. O Knip aponta `tsx` como binário não declarado na raiz e no
frontend. Não foram encontradas vulnerabilidades npm.

**Decisão implementada:** `execucoes.dependencias[].ignorarAtualizacoes` filtra pares pacote/major do JSON de
`npm outdated` e recalcula o status. O perfil SGC ignora apenas `typescript` major 7; Pinia e demais atualizações continuam
visíveis. Declarações ausentes de `tsx` em root/frontend são propriedade dos manifestos do SGC e não entram neste recorte.

#### 6. Árvore de linhas tem saída padrão excessiva

**Evidência:** `projeto arvore-linhas` imprimiu a árvore integral de cerca de 246 mil linhas do repositório. As opções
`--profundidade` e `--minimo-linhas` resolvem o problema, mas exigem conhecimento prévio e não há JSON resumido.

**Decisão implementada:** os defaults são profundidade 3 e mínimo de 500 linhas. Os limites podem ser substituídos pelas
opções existentes, inclusive com zero.

### Resultados úteis que não exigem correção imediata

- OpenAPI diff e promoção de baseline funcionaram em arquivos temporários. A exportação falhou porque o servidor não
  estava ativo e apresentou orientação adequada; isso é pré-condição esperada, não defeito.
- `servidor arquitetura auditar` encontrou 3 críticos e 7 alertas com arquivos e motivos coerentes.
- CDU encontrou 0 erros, 35 avisos e 64 referências imprecisas; o resumo agora mantém esses totais e até 20 achados ou itens
  sem referência exata.
- Contratos, notificações, views, modais e Semgrep não encontraram violações.
- O corretor FQN encontrou 114 arquivos alteráveis em simulação, confirmando seu valor como utilitário ocasional; nenhuma
  alteração deve ser aplicada como parte deste recorte.
- `projeto ambiente verificar` passou nas 20 verificações; sincronização de versão confirmou que `1.3.8` já está alinhada.
- A fotografia anterior do SGC permaneceu vermelha por um resultado obsoleto de identificadores; uma nova coleta rápida,
  executada após a correção, passou em 8/8 verificações. O resumo compacto mostrou os oito status e os pontos críticos sem
  reproduzir o despejo de métricas detalhadas.
- As novas respostas resumidas no SGC ficaram entre aproximadamente 3,8 KB e 25,4 KB: resíduos 11,4 KB, cobertura 4,9–5,7
  KB, ramificações 3,9–4,2 KB, inventário CDU 17,1 KB e auditoria CDU 25,4 KB.

### Ordem de execução recomendada

1. executar a validação final completa do toolkit e conferir a sincronização catálogo/README/plano;
2. repetir a amostra final sequencialmente, sem concorrência entre comandos que compartilhem npm, Gradle ou artefatos;
3. marcar o recorte encerrado se todos os critérios abaixo permanecerem satisfeitos.

## Recorte final — comprovação semântica

### Objetivo

Comprovar que os auditores mantidos respondem corretamente às perguntas que anunciam, sem reabrir arquitetura, catálogo
ou generalização já encerrados.

### Matriz semântica atual

| Área | Contrato verificado | Evidência comportamental | Lacuna obrigatória |
|---|---|---|---|
| Servidor: cobertura e auditorias | Mede cobertura, risco, arquitetura, coesão e contratos com caminhos/políticas explícitos | `servidor-auditorias.test.ts`, `cobertura-cli.test.ts` | Nenhuma identificada |
| Servidor: testes e notificações | Classifica evidências, prioriza backlog e detecta assuntos fora da fonte canônica | `servidor-testes.test.ts`, `servidor-notificacoes.test.ts` | Nenhuma identificada |
| Cliente: arquitetura e gates | Sinaliza violações reais, permite composição correta e mantém políticas SGC na borda | `cliente-arquitetura.test.ts`, `cliente-arquitetura-gates.test.ts` | Nenhuma identificada |
| Cliente: resíduos, templates, modais e identificadores | Distingue inventário de gate e valida padrões estruturais configuráveis | `cliente-residuos.test.ts`, `cliente-validadores.test.ts`, `cliente-identificadores.test.ts` | Nenhuma identificada |
| Cliente: cobertura | Lista lacunas, cruza sinais de tratamento de erro e ignora fontes removidas | `cobertura-cli.test.ts` | Nenhuma identificada |
| Código e nomenclatura | Executa Semgrep/heurísticas e valida inventário, idioma, contratos e resumos | `codigo-auditorias.test.ts`, `consistencia.test.ts` | Nenhuma identificada |
| OpenAPI e CDU | Usa caminhos/configuração explícitos e produz contratos horizontais em projeto externo | `integracao.test.ts`, `cdus.test.ts`, `externo.test.ts` | Nenhuma identificada |
| Projeto e qualidade | Mantém efeitos, status, filtros de dependência, árvore limitada e composição externa | `projeto.test.ts`, `qualidade.test.ts`, `qualidade-externa.test.ts` | Nenhuma identificada |

### Passos obrigatórios

1. Priorizar regras que produzem gate, severidade, pontuação, violação ou recomendação acionável.
2. Completar somente as evidências necessárias para cada tipo de regra:
   - achado positivo mínimo, com arquivo e motivo corretos;
   - negativo próximo para heurísticas sujeitas a falso positivo;
   - limite inferior/superior quando houver threshold ou mudança de classificação;
   - invariantes entre resumo, severidade, lista destacada e motivos;
   - mais de um arquivo quando contagem, duplicação ou escala alterar o resultado;
   - saída humana quando ela for o principal produto do comando.
3. Não exigir mecanicamente todos os casos acima de toda regra. A matriz deve registrar `não aplicável` com justificativa
   curta quando a natureza da regra não exigir uma dimensão.
4. Executar uma amostra curta e representativa no SGC, cobrindo ao menos:
   - um auditor de servidor;
   - um auditor de cliente;
   - uma capacidade horizontal configurável;
   - CDU;
   - um orquestrador ou agregador.
5. Confrontar os principais resultados com os arquivos apontados. Todo falso positivo, falso negativo ou texto enganoso
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
- [x] README do toolkit contém somente referência do estado atual, sem histórico ou plano de execução;
- [x] o tarball instalado isoladamente executa o binário e as APIs públicas suportadas;
- [x] tarefas de qualidade preservam a instalação do toolkit e usam a versão mínima de Node nos subprocessos;
- [x] gates e status gerais não falham por inventários ou heurísticas sem violação demonstrável;
- [x] entradas estruturadas incompatíveis falham em vez de produzir resultado vazio;
- [x] comandos de grande volume oferecem resumo JSON acionável para agentes;
- [x] auditoria de dependências respeita a decisão sobre TypeScript 7 sem ocultar atualizações da série 6;
- [x] a matriz semântica está completa e não contém lacuna obrigatória;
- [x] a amostra final contra o SGC foi confrontada com o código e não revelou resultado enganoso sem regressão;
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
