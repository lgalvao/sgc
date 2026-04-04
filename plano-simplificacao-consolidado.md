# Plano Consolidado de Simplificação do SGC

Este documento consolida os aprendizados e as ações pendentes dos arquivos `plano-simplificacao.md`,
`simplification-plan.md` e `simplification-suggestions.md`, reconciliando o plano com o estado atual do código.

O objetivo é reduzir complexidade acidental sem alterar regras de negócio, contratos HTTP, DTOs externos,
segurança, transações, notificações ou textos relevantes de interface.

## Fontes de verdade

Antes de qualquer rodada de simplificação, considerar em ordem de precedência:

* `AGENTS.md`
* `etc/docs/regras-acesso.md`
* `etc/reqs`
* este plano consolidado

Se este plano divergir do código real, prevalece o código verificado e o plano deve ser atualizado na mesma rodada.

## Princípios e guardrails

* Simplificar primeiro a menor fronteira segura.
* Tornar dependências e fluxos explícitos.
* Reduzir superfície pública antes de criar abstração nova.
* Preservar DTOs como fronteira externa padrão.
* Não expor entidade JPA por conveniência.
* Não colapsar controller em repositório quando houver regra, segurança, transação ou montagem de resposta.
* Não criar facade nova sem regra transversal clara e comprovada.
* Se um método passar de 3 parâmetros, usar objeto de transporte.
* Remover código morto assim que a simplificação o tornar órfão.
* Validar em passos pequenos e registrar aprendizado no plano.

## Estado consolidado

### O que já foi confirmado

* As rodadas anteriores já reduziram parte da complexidade em scripts de QA.
* `SubprocessoConsultaService` já existe e centraliza parte relevante das leituras.
* `SubprocessoService` não é mais o principal hotspot do módulo `subprocesso`.
* `SubprocessoTransicaoService` continua sendo o maior concentrador de responsabilidades do fluxo de workflow.
* `LoadingButton.vue` segue como wrapper fino, mas ainda possui adoção ampla o suficiente para exigir auditoria antes de remoção.
* A suíte de backend voltou a ficar íntegra com `./gradlew --no-configuration-cache :backend:test` verde, após saneamento das invariáveis de domínio refletidas no `schema.sql`.
* O backend permanece íntegro após nova rodada de simplificação: `1412` testes executados, `1411` passando e `1` ignorado.

### Aprendizados consolidados da rodada mais recente

* A limpeza de simplificação ficou bloqueada por inconsistências reais entre modelo, schema e testes. Esse débito já foi reduzido e não deve mais competir com a Frente 1.
* `ADMIN` deve ser tratado como unidade virtual raiz, não como unidade participante de processo.
* Fluxos hierárquicos de subida não devem ter fallback para `ADMIN`; a invariável correta é que toda unidade participante possui superior.
* `Movimentacao` exige sempre `unidadeOrigem`, `unidadeDestino` e `usuario`.
* `Mapa` exige sempre vínculo com `Subprocesso`.
* `Processo` exige `dataLimite`.
* O conjunto `schema.sql`, `data.sql` e `seed.sql` passou a funcionar como superfície principal de alinhamento dos testes, reduzindo ruído estrutural antes da próxima simplificação.
* Testes unitários que constroem services manualmente precisam acompanhar a evolução das dependências explícitas; isso não deve ser compensado com afrouxamento do código de produção.
* Parte da rodada recente revelou bugs reais fora da frente principal, como nulidade defensiva ausente em `ImpactoMapaService` e configuração insegura do `TestSecurityConfig`; esses ajustes foram tratados como correções de robustez, não como mudança de escopo arquitetural.

### Premissas antigas que deixam de valer

* `SubprocessoService` não deve mais ser tratado como “superclasse” prioritária por tamanho.
* O objetivo não é criar um `SubprocessoConsultaService`; ele já existe.
* A próxima rodada de backend não deve partir automaticamente para uma `SubprocessoWorkflowFacade`.

### Hipóteses de `simplification-suggestions.md` absorvidas com ajuste

* Interfaces de implementação única continuam sem valor por padrão, mas isso não autoriza colapsar fronteiras úteis já existentes.
* Wrappers visuais finos continuam sob suspeita e devem passar por auditoria explícita.
* Stores e composables pass-through continuam sendo maus alvos e devem ser reduzidos quando não houver estado compartilhado real.
* Hotspots como `PdfFactory`, facades específicas e views extensas entram como candidatos de investigação, não como remoção automática.

### Hipóteses de `simplification-suggestions.md` rejeitadas neste plano

* Controllers não devem ser encorajados a acessar repositórios diretamente fora de leituras triviais sem regra, segurança contextual ou montagem de resposta.
* O plano não autoriza retorno direto de entidades JPA como política geral para GET.
* `SubprocessoTransicaoService`, `SubprocessoValidacaoService` e `SubprocessoService` não devem ser fundidos mecanicamente.
* Facades existentes não devem ser removidas por decreto; primeiro é preciso comprovar que são pass-through reais e que a remoção não espalha regra, permissão ou composição.

## Diagnóstico consolidado por área

### 1. Scripts de QA

Diagnóstico:

* Havia concentração de complexidade em `etc/scripts/qa/snapshot-coletar-execucao.mjs`.
* Parte dessa complexidade era justificável pela consolidação de múltiplas fontes.
* Parte era acidental, especialmente repetição de parse, agregação e cálculo de percentuais.

Situação:

* A simplificação dessa frente já foi iniciada.
* Os ganhos medidos anteriormente continuam válidos como evidência histórica.
* Esta frente deixa de ser prioridade arquitetural principal e passa a entrar em manutenção incremental.

### 2. Backend `subprocesso`

Diagnóstico:

* O principal hotspot atual é `backend/src/main/java/sgc/subprocesso/service/SubprocessoTransicaoService.java`.
* O problema dominante não é apenas tamanho de arquivo, e sim mistura de responsabilidades:
  * transição de estado;
  * criação de análise;
  * persistência de movimentação;
  * notificação;
  * envio de e-mail;
  * criação de alertas;
  * regras específicas de fluxos de cadastro, revisão e validação.
* `SubprocessoConsultaService` ainda acumula leitura com composição de contexto e permissões, então não está “puro”.
* `SubprocessoService` ainda merece limpeza pontual, mas não deve liderar a fila de refatoração.

Direção:

* Priorizar extrações coesas dentro de `SubprocessoTransicaoService`.
* Fatiar `SubprocessoConsultaService` por responsabilidade real, sem quebrar contratos.
* Tratar `SubprocessoService` como frente secundária de limpeza interna.
* Evitar abrir novas frentes estruturais no backend antes de concluir pelo menos um corte pequeno em `SubprocessoTransicaoService`.

### 3. Backend `e2e`

Diagnóstico:

* `backend/src/main/java/sgc/e2e/E2eController.java` segue como hotspot relevante por tamanho.
* Ainda não há, neste plano, evidência suficiente para prescrever o corte ideal.

Direção:

* Medir métodos e agrupamentos internos antes de escolher extração.
* Evitar refatoração ampla baseada só em volume de linhas.

### 4. Frontend

Diagnóstico:

* Existem views grandes com mistura de responsabilidades.
* O plano anterior corretamente mira lógica demais em `<script setup>`, normalização de erro e duplicação de parsing.
* `LoadingButton.vue` é fino, mas o número de usos, stories e testes indica que ele precisa de auditoria formal antes de qualquer remoção.

Direção:

* Priorizar views com fetch, transformação, decisão e sincronização local misturados no mesmo arquivo.
* Reduzir estado global desnecessário e efeito colateral implícito.
* Auditar wrappers visuais finos com critério explícito de manter, ajustar ou remover.

## Frentes priorizadas

## Frente 1 — Quebra coesa de `SubprocessoTransicaoService`

Objetivo:

* Reduzir acoplamento interno e tornar o workflow mais navegável sem alterar contrato externo.

Escopo inicial:

* Mapear blocos por responsabilidade:
  * transição e movimentação;
  * análise de workflow;
  * aceite, devolução e homologação;
  * reabertura;
  * alteração de data limite e notificações derivadas.
* Extrair primeiro helpers privados ou componentes internos coesos.
* Só promover nova classe quando houver fronteira semântica estável e mais de um uso relevante.

Estado da frente:

* O primeiro corte técnico já ocorreu: foi removida duplicação no algoritmo de devolução dentro de `SubprocessoTransicaoService`.
* A rodada seguinte eliminou ruído que mascarava a simplificação principal:
  * fallback hierárquico indevido para `ADMIN`;
  * aceitação indevida de `RAIZ` como participante de subprocesso;
  * cenários de teste que violavam invariáveis já exigidas pelo schema.
* A rodada atual avançou a simplificação interna do service sem abrir nova classe:
  * extração da transição recorrente para unidade superior;
  * centralização dos fluxos explícitos com `ADMIN` em helpers privados;
  * consolidação da montagem repetida de `RegistrarWorkflowCommand` em helper com destino explícito.
* O núcleo de workflow ficou mais explícito, mas `SubprocessoTransicaoService` ainda concentra persistência, notificação, e-mail e alertas no mesmo ponto.
* A próxima etapa deve ser precedida por uma limpeza geral do workspace para reduzir ruído acumulado antes de novos cortes estruturais.

Próximo corte recomendado:

* Fazer uma limpeza geral e reavaliar o diff acumulado antes de continuar a Frente 1.
* Retomar em seguida a separação entre:
  * persistência de workflow;
  * notificação/e-mail;
  * criação de alertas.
* Só considerar extração de colaborador interno depois dessa limpeza e de nova medição do arquivo.

Restrições:

* Não criar facade adicional apenas para “esconder” o service atual.
* Não misturar simplificação estrutural com mudança de regra.
* Preservar testes e assinaturas públicas relevantes.

Critério de pronto:

* menos branches por método crítico;
* menos dependências diretas por classe ou responsabilidade mais clara por agrupamento;
* sem regressão em testes do backend.

## Frente 2 — Fatiamento de leitura e contexto em `SubprocessoConsultaService`

Objetivo:

* Separar leitura pura de composição de contexto, permissões e detalhe de tela.

Escopo inicial:

* Identificar métodos de consulta simples versus métodos de montagem de resposta rica.
* Separar, quando viável, leitura de entidade/mapa/status da composição de contexto de edição e permissões.
* Preservar DTOs externos e evitar retorno de entidade por conveniência.

Critério de pronto:

* leitura básica mais previsível;
* menos mistura entre fetch, permissão e montagem de payload;
* sem impacto nos controllers e contratos HTTP.

## Frente 3 — Limpeza seletiva de `SubprocessoService`

Objetivo:

* Remover duplicação interna e helpers largos sem tratá-lo como principal gargalo arquitetural.

Escopo inicial:

* Revisar trechos de criação, ajuste de mapa e importação de atividades.
* Consolidar validações e obtenção de contexto repetidas.
* Remover APIs internas redundantes liberadas por simplificações anteriores.

Critério de pronto:

* menos navegação para entender operações comuns;
* menor duplicação interna;
* sem criação de novas camadas desnecessárias.

## Frente 4 — Auditoria de wrappers visuais e views grandes

Objetivo:

* Reduzir abstrações finas sem contrato real e simplificar views com responsabilidades mistas.

Escopo inicial:

* Classificar `LoadingButton.vue` como manter, ajustar ou remover.
* Aplicar a mesma triagem a outros wrappers visuais pequenos.
* Identificar views grandes onde a simplificação reduz estado implícito, parsing repetido e tratamento de erro duplicado.

Perguntas de triagem para wrappers:

* Padroniza algo recorrente?
* Reduz duplicação material?
* Adiciona acessibilidade ou comportamento?
* Evita divergência visual entre telas?

Critério de pronto:

* cada wrapper auditado com decisão explícita;
* views alvo com menos lógica incidental em `<script setup>`;
* `normalizeError` usado de forma consistente nas camadas corretas.

## Frente 5 — Hotspots fora de `subprocesso`

Objetivo:

* Atacar arquivos extensos fora do fluxo principal apenas com diagnóstico suficiente.

Escopo inicial:

* Reavaliar `E2eController` com medição de métodos e responsabilidades.
* Auditar `PdfFactory` e facades citadas em `simplification-suggestions.md` para separar pass-through real de fronteira útil.
* Continuar manutenção incremental dos scripts de QA quando houver ganho claro e local.

Critério de pronto:

* hotspot priorizado por evidência, não por impressão;
* cortes pequenos com validação direcionada.

## Estratégia de execução

Para cada rodada:

1. Mapear o acoplamento real do alvo.
2. Classificar o problema:
   * duplicação interna;
   * superfície larga demais;
   * efeito colateral escondido;
   * estado global desnecessário;
   * wrapper fino;
   * código morto.
3. Escolher o menor corte seguro.
4. Validar imediatamente após a mudança.
5. Registrar aprendizado novo neste plano.

### Sequência recomendada da próxima rodada

1. Fazer limpeza geral do workspace e revisar alterações acumuladas.
2. Medir novamente `SubprocessoTransicaoService` após os cortes já feitos.
3. Escolher o próximo corte entre efeitos derivados de notificação/e-mail/alerta.
4. Validar suíte focada e, ao fim da rodada, validar o backend completo.

## Validação mínima por frente

### Backend

* `./gradlew :backend:compileTestJava`
* `./gradlew :backend:test`
* Se houver falha suspeita de cache: `./gradlew --no-configuration-cache :backend:compileTestJava`

### Frontend

* `npm run typecheck`
* `npm run lint`
* `npm run test:unit`

### QA dashboard

* Quando a rodada afetar testes, lint, typecheck, cobertura ou E2E, atualizar snapshot com:
  * `npm run qa:dashboard`
  * ou `node etc/scripts/sgc.js qa snapshot coletar --perfil rapido`
* Usar `etc/qa-dashboard/latest/ultimo-snapshot.json` e `etc/qa-dashboard/latest/ultimo-resumo.md` como fonte de verdade.

## Critérios de sucesso

O plano terá sido bem executado se:

* houver menos pontos de navegação para seguir um fluxo simples;
* não houver perda de regras de segurança, transação, notificação ou permissão;
* o backend tiver menos concentração de workflow em um único service;
* o frontend reduzir estado implícito e wrappers sem contrato claro;
* os componentes e services remanescentes tiverem justificativa explícita de existência.

## Aprendizados consolidados

1. Duplicação antes de fusão.
2. Reuso intermediário é aceitável, desde que o acoplamento fique registrado.
3. Assinaturas públicas só devem mudar quando a API redundante realmente deixar de fazer sentido.
4. Testes precisam seguir o collaborator real após a simplificação.
5. Singleton só quando houver compartilhamento real.
6. A view deve sincronizar explicitamente o que consome.
7. Round-trips seguem sendo critério de design.
8. Erro estruturado e erro genérico não devem ser tratados do mesmo jeito.
9. Falhas de cache de build não devem ser confundidas com regressão de código.
10. Stories e README também precisam acompanhar simplificações estruturais.
11. Em `SubprocessoTransicaoService`, o primeiro corte seguro foi extrair algoritmo duplicado de devolução antes de mover fluxos inteiros para novas classes.

## Fonte de verdade operacional

Este arquivo passa a ser a referência consolidada para novas rodadas.

Os arquivos históricos `plano-simplificacao.md`, `simplification-plan.md` e `simplification-suggestions.md` devem ser
tratados como insumos de contexto.


## Continuação operacional (rodada atual)

### Checklist objetivo da Frente 1

1. Medir o estado atual de `SubprocessoTransicaoService` com foco em:
   * quantidade de métodos com mais de uma responsabilidade visível;
   * pontos de chamada para persistência, notificação, e-mail e alerta;
   * duplicações de montagem de comandos e eventos de workflow.
2. Registrar o recorte escolhido da rodada em até um parágrafo neste plano antes de alterar código.
3. Aplicar apenas um corte estrutural por vez, mantendo validação curta entre cortes.
4. Atualizar este arquivo com resultado objetivo (o que saiu, o que ficou e por quê).

### Recorte escolhido desta rodada

Após nova medição local, o ponto mais repetitivo entre transição e efeitos derivados está na etapa de homologação de cadastro/revisão e na alteração de data limite, onde a regra principal fica misturada com envio de e-mail e criação de alerta. O corte desta rodada será manter o fluxo de estado no método principal e extrair a execução de efeitos derivados para helpers privados nomeados por contexto, sem criar nova classe e sem alterar contratos públicos.

### Definição de pronto da rodada atual

A rodada atual só deve ser encerrada quando todos os itens abaixo estiverem verdadeiros:

* existe registro explícito do corte realizado em `SubprocessoTransicaoService`;
* a separação entre transição de estado e efeitos derivados ficou mais legível no diff;
* nenhum contrato público foi alterado sem justificativa registrada;
* a suíte mínima de backend executou sem regressão atribuível ao corte.

### Registro padrão por rodada (preencher ao final)

* **Data da rodada:**
* **Frente principal:**
* **Arquivo(s) alvo:**
* **Corte aplicado:**
* **Risco principal observado:**
* **Validação executada:**
* **Pendência aberta para próxima rodada:**

### Fila sugerida após a rodada atual

1. Consolidar um ponto único para efeitos de notificação/e-mail/alerta disparados por transição.
2. Reavaliar se ainda há ganho em extrair colaborador interno focado apenas em persistência de workflow.
3. Iniciar mapeamento fino de `SubprocessoConsultaService` separando leitura simples e composição de contexto.
4. Rodar triagem de wrappers visuais começando por `LoadingButton.vue`, com decisão explícita de manter, ajustar ou remover.

### Resultado objetivo da rodada atual

Saiu a mistura direta de regra principal com efeitos derivados em dois pontos de `SubprocessoTransicaoService`: homologação de cadastro/revisão e alteração de data limite agora delegam para helpers privados focados em alerta e e-mail. Ficou no service a mesma orquestração de transição, persistência e validação de estado, sem nova classe nesta etapa para manter o corte pequeno e seguro. Esse recorte melhora leitura do fluxo principal e prepara a próxima rodada para eventual consolidação de efeitos derivados em fronteira interna única.

Na continuação desta rodada, o ponto central `registrarTransicao` também foi fatiado em duas etapas explícitas: persistência da transição e disparo de notificação. Com isso, o fluxo principal passou a evidenciar melhor a separação entre mudança de estado e efeito derivado sem alterar assinatura pública nem semântica do método.

Ainda nesta sequência, o fluxo de alteração de data limite recebeu um corte interno adicional para reduzir regra implícita duplicada por `contains("MAPA")`: a escolha do campo de data e o cálculo da etapa de alerta passaram a ter helpers dedicados, deixando a regra principal mais direta.

Na continuidade da Frente 1 em 2026-04-04, o fluxo de reabertura teve os efeitos derivados de alerta isolados em ponto único (`enviarAlertasReabertura`) com helpers por destinatário (unidade e cadeia hierárquica). O método principal de reabertura preserva estado e transição, enquanto os alertas ficam explícitos e centralizados sem criação de nova classe.

### Registro da rodada

* **Data da rodada:** 2026-04-03
* **Frente principal:** Frente 1 — Quebra coesa de `SubprocessoTransicaoService`
* **Arquivo(s) alvo:** `backend/src/main/java/sgc/subprocesso/service/SubprocessoTransicaoService.java`, `plano-simplificacao-consolidado.md`
* **Corte aplicado:** extração de helpers privados para efeitos derivados (alertas/e-mail), fatiamento de `registrarTransicao` e isolamento da regra de etapa/data na alteração de data limite.
* **Risco principal observado:** possível alteração acidental na ordem de disparo dos efeitos derivados após persistência.
* **Validação executada:** compilação de testes do backend e suíte focada de testes de `SubprocessoTransicaoService`, reexecutadas após os cortes incrementais no fluxo de transição e data limite.
* **Pendência aberta para próxima rodada:** avaliar consolidação adicional dos efeitos derivados de transição em ponto único sem ampliar superfície pública, especialmente alertas de reabertura e trilha de e-mail.

### Registro da rodada

* **Data da rodada:** 2026-04-04
* **Frente principal:** Frente 1 — Quebra coesa de `SubprocessoTransicaoService`
* **Arquivo(s) alvo:** `backend/src/main/java/sgc/subprocesso/service/SubprocessoTransicaoService.java`, `plano-simplificacao-consolidado.md`
* **Corte aplicado:** centralização dos efeitos derivados de reabertura em helper único, com extração dos disparos de alerta por destinatário para reduzir ramificações no laço hierárquico.
* **Risco principal observado:** alteração acidental de ordem ou público-alvo dos alertas de reabertura.
* **Validação executada:** compilação de testes do backend e suíte focada de `SubprocessoTransicaoService`.
* **Pendência aberta para próxima rodada:** unificar, em fronteira interna única, a trilha de efeitos derivados entre homologação, reabertura e alteração de data limite para reduzir dispersão de regras de notificação.
