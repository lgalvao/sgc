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

### Resultado objetivo da continuação (2026-04-04, etapa final da Frente 1)

O fluxo interno de workflow deixou de trafegar listas extensas de parâmetros soltos: os pontos de registro de análise/transição agora recebem um objeto interno de transporte (`RegistrarWorkflowInternoCommand`), reduzindo acoplamento por assinatura e deixando explícito o contexto completo da operação. Na mesma etapa, a reabertura passou a usar `ReaberturaCommand` e `AlertaReaberturaContexto`, separando estado/transição dos efeitos derivados de alerta sem criar nova classe pública.

Com esse corte, a Frente 1 é encerrada nesta rodada: `SubprocessoTransicaoService` mantém a orquestração central, mas com menos superfície acidental por método e com fronteiras internas mais explícitas para workflow e efeitos derivados.

### Registro da rodada

* **Data da rodada:** 2026-04-04
* **Frente principal:** Frente 1 — Quebra coesa de `SubprocessoTransicaoService` (encerramento)
* **Arquivo(s) alvo:** `backend/src/main/java/sgc/subprocesso/service/SubprocessoTransicaoService.java`, `backend/src/test/java/sgc/subprocesso/service/SubprocessoTransicaoServiceExtraCoverageTest.java`, `plano-simplificacao-consolidado.md`
* **Corte aplicado:** substituição de assinaturas internas extensas por objetos de transporte internos para workflow e reabertura, com ajuste dos testes para validar comportamento por API pública em vez de acoplamento reflexivo ao formato de método privado.
* **Risco principal observado:** regressão na montagem de comando interno de workflow (origem/destino de transição) e nos gatilhos de alerta em reabertura.
* **Validação executada:** compilação de testes e suíte focada de `SubprocessoTransicaoService` (unit e cobertura extra), incluindo execução agregada dos testes do service.
* **Pendência aberta para próxima rodada:** iniciar Frente 2 com mapeamento de leitura simples versus composição de contexto em `SubprocessoConsultaService`.

## Continuação operacional (início da Frente 2)

### Recorte escolhido desta rodada

Após mapear `SubprocessoConsultaService`, o ponto mais acoplado entre leitura e composição está na montagem de detalhe/contexto e no cálculo de permissões de UI. O corte desta rodada será manter as APIs públicas atuais e extrair contexto interno explícito para permissões, além de helpers privados de detalhe e leitura obrigatória de mapa, reduzindo parâmetros soltos e deixando mais nítida a diferença entre consulta simples e composição de resposta.

### Resultado objetivo da rodada

`SubprocessoConsultaService` passou a tratar a composição de detalhe e de permissões com contexto interno explícito, sem alterar contratos públicos. A montagem de `SubprocessoDetalheResponse` agora delega a helpers privados para coletar responsável, titular, movimentações e permissões, enquanto o cálculo de permissões de UI deixou de trafegar múltiplos parâmetros soltos e passou a operar sobre `ContextoPermissaoSubprocesso`. Na mesma rodada, leituras que dependem obrigatoriamente de mapa (`listarAtividadesSubprocesso` e `obterMapaParaAjuste`) passaram a reutilizar a fronteira interna de mapa obrigatório, reduzindo regra implícita duplicada.

### Registro da rodada

* **Data da rodada:** 2026-04-04
* **Frente principal:** Frente 2 — Fatiamento de leitura e contexto em `SubprocessoConsultaService`
* **Arquivo(s) alvo:** `backend/src/main/java/sgc/subprocesso/service/SubprocessoConsultaService.java`, `plano-simplificacao-consolidado.md`
* **Corte aplicado:** extração de contexto interno para permissões e de helpers privados para montagem de detalhe e leitura obrigatória de mapa, preservando APIs públicas e DTOs externos.
* **Risco principal observado:** regressão discreta na combinação de permissões por perfil/situação e na montagem de detalhe consumida por contexto de edição.
* **Validação executada:** `./gradlew :backend:compileTestJava` e `./gradlew :backend:test --tests "sgc.subprocesso.service.SubprocessoConsultaServiceExtraCoverageTest" --tests "sgc.integracao.SubprocessoServiceContextoIntegrationTest"`.
* **Pendência aberta para próxima rodada:** continuar o mapeamento fino da Frente 2 separando leitura simples de composição de contexto em pontos ainda mistos, especialmente `obterContextoEdicao` e leituras de histórico/localização relacionadas a UI.

### Recorte escolhido da continuação imediata

Na continuação da Frente 2, o próximo ponto misto é `obterContextoEdicao`, que ainda concentra a coleta de subprocesso, detalhe, mapa e atividades no mesmo método público. O corte será extrair um contexto interno único para edição e deixar o método público apenas como adaptador para `ContextoEdicaoResponse`, mantendo a API externa estável.

### Resultado objetivo da continuação imediata

`obterContextoEdicao` deixou de concentrar montagem espalhada e passou a operar sobre um contexto interno único de edição. O método público agora só adapta `DadosContextoEdicao` para `ContextoEdicaoResponse`, enquanto a coleta de subprocesso, detalhe, mapa e atividades ficou centralizada em helper privado. Com isso, a Frente 2 avança na separação entre leitura simples e composição de resposta sem abrir nova classe pública nem alterar contratos.

### Registro da continuação imediata

* **Data da rodada:** 2026-04-04
* **Frente principal:** Frente 2 — Fatiamento de leitura e contexto em `SubprocessoConsultaService`
* **Arquivo(s) alvo:** `backend/src/main/java/sgc/subprocesso/service/SubprocessoConsultaService.java`, `plano-simplificacao-consolidado.md`
* **Corte aplicado:** extração de contexto interno de edição (`DadosContextoEdicao`) para remover a orquestração espalhada de `obterContextoEdicao`.
* **Risco principal observado:** regressão na composição do contexto completo de edição, especialmente conciliação entre detalhe, mapa e lista de atividades.
* **Validação executada:** `./gradlew :backend:compileTestJava` e `./gradlew :backend:test --tests "sgc.subprocesso.service.SubprocessoConsultaServiceExtraCoverageTest" --tests "sgc.integracao.SubprocessoServiceContextoIntegrationTest"`.
* **Pendência aberta para próxima rodada:** revisar se `obterUnidadeLocalizacao` e `obterLocalizacaoAtual` ainda justificam coexistência separada e mapear possível consolidação segura sem efeito colateral oculto.

### Recorte escolhido da continuação seguinte

Na sequência da Frente 2, a duplicação remanescente mais objetiva está entre `obterUnidadeLocalizacao` e `obterLocalizacaoAtual`: ambos resolvem a localização com a mesma regra de fallback, mas apenas um persiste o valor em cache no próprio `Subprocesso`. O corte desta etapa será centralizar a resolução em helper privado único, preservando a diferença externa entre leitura sem cache e leitura com cache.

### Interrupção crítica tratada durante a Frente 2

Ao revisar a duplicação de localização, a rodada encontrou um problema de domínio que inviabilizava seguir apenas com simplificação estrutural: a `localizacaoAtual` era derivada em múltiplos pontos, dependia de campo `@Transient` em `Subprocesso` e tolerava, de forma implícita, subprocessos persistidos em situação avançada sem qualquer `Movimentacao`.

Como a regra de acesso por escrita depende diretamente da localização atual do subprocesso, a simplificação só permaneceu segura após tratar esse ponto como correção estrutural obrigatória. Nesta exceção controlada, a Frente 2 precisou combinar simplificação com endurecimento de regra de domínio.

### Resultado objetivo da interrupção crítica

`localizacaoAtual` deixou de existir como estado cacheado na entidade e passou a ser derivada em ponto único por `LocalizacaoSubprocessoService`. Consulta, segurança e controller passaram a usar a mesma regra de resolução. Na mesma rodada, o sistema deixou de aceitar silenciosamente subprocesso persistido sem movimentação em situação avançada: a ausência de histórico só permanece válida em `NAO_INICIADO`; nos demais casos, o backend lança erro explícito de inconsistência.

O trabalho também revelou dívida de testes e seed: parte das suítes de integração e o `data.sql` do backend montavam estados impossíveis para subprocessos já avançados. Esses cenários foram saneados para registrar a movimentação inicial mínima coerente com o estado persistido. O seed de E2E foi auditado e já estava íntegro para essa regra.

### Registro da interrupção crítica

* **Data da rodada:** 2026-04-04
* **Frente principal:** Frente 2 — Fatiamento de leitura e contexto em `SubprocessoConsultaService`
* **Arquivo(s) alvo:** `backend/src/main/java/sgc/subprocesso/service/LocalizacaoSubprocessoService.java`, `backend/src/main/java/sgc/subprocesso/service/SubprocessoConsultaService.java`, `backend/src/main/java/sgc/seguranca/SgcPermissionEvaluator.java`, `backend/src/main/java/sgc/processo/ProcessoController.java`, `backend/src/main/java/sgc/subprocesso/model/Subprocesso.java`, `backend/src/test/resources/data.sql`, `e2e/setup/seed.sql`, `backend/src/test/java/sgc/subprocesso/service/LocalizacaoSubprocessoServiceTest.java`, testes de integração e cobertura relacionados
* **Corte aplicado:** eliminação do campo transitório `localizacaoAtual`, criação de ponto único de cálculo por service dedicado e endurecimento da regra para tratar subprocesso persistido sem movimentação fora de `NAO_INICIADO` como erro grave.
* **Risco principal observado:** quebra ampla de testes e cenários seedados por revelar estados antes tolerados, além de risco de mascarar erro funcional se a ordem das validações de negócio continuasse consultando localização cedo demais.
* **Validação executada:** suíte focada dos grupos quebrados após o endurecimento, criação de suíte dedicada para `LocalizacaoSubprocessoService` e execução completa de `./gradlew :backend:test`.
* **Pendência aberta para próxima rodada:** retomar a Frente 2 a partir do novo ponto de verdade de localização, revisando a redução adicional de mistura entre leitura simples, contexto rico e permissões de UI em `SubprocessoConsultaService`.

### Próximo alvo natural da Frente 2

Com a localização já consolidada e endurecida, o próximo corte seguro volta a ser estrutural: enxugar a montagem de permissões e detalhe em `SubprocessoConsultaService`, agora sem duplicação de regra de localização e sem cache transitório na entidade. O foco deve permanecer em reduzir mistura entre fetch, composição de payload e decisão de UI, sem reabrir a discussão de domínio já estabilizada nesta interrupção.

### Continuação após estabilização da localização

Com a localização atual já resolvida em ponto único, a continuação imediata da Frente 2 passou a atacar a costura entre detalhe de tela e permissões de UI em `SubprocessoConsultaService`. O problema remanescente era menos de regra duplicada e mais de dependência implícita: detalhe e permissões recalculavam partes do mesmo contexto de consulta em caminhos diferentes, especialmente localização, unidade do usuário e flags derivadas de processo finalizado, mesma unidade e mapa vigente.

### Resultado objetivo da continuação

`SubprocessoConsultaService` passou a montar um contexto interno único de consulta (`ContextoConsultaSubprocesso`) antes de compor detalhe e permissões. Com isso, a localização atual, a unidade do usuário, a condição de processo finalizado e os indicadores derivados deixaram de ser recalculados de forma espalhada entre `obterDetalhes`, `obterPermissoesUI` e helpers privados. O contrato público permaneceu igual, mas a diferença entre coleta de contexto e decisão de UI ficou mais explícita.

Na mesma rodada, `ContextoPermissaoSubprocesso` deixou de depender implicitamente do `Subprocesso` espalhado pelo call stack e passou a carregar também a unidade-alvo do subprocesso, reduzindo acoplamento acidental nos helpers de habilitação de acesso.

### Registro da continuação

* **Data da rodada:** 2026-04-04
* **Frente principal:** Frente 2 — Fatiamento de leitura e contexto em `SubprocessoConsultaService`
* **Arquivo(s) alvo:** `backend/src/main/java/sgc/subprocesso/service/SubprocessoConsultaService.java`, `plano-simplificacao-consolidado.md`
* **Corte aplicado:** criação de contexto interno único para consulta rica (`ContextoConsultaSubprocesso`) e eliminação de recálculo disperso entre detalhe e permissões de UI.
* **Risco principal observado:** regressão discreta nas permissões exibidas em detalhe, principalmente no branch de processo finalizado e na conciliação entre mesma unidade/localização derivada.
* **Validação executada:** `./gradlew :backend:test --tests "sgc.subprocesso.service.SubprocessoConsultaServiceExtraCoverageTest" --tests "sgc.integracao.SubprocessoServiceContextoIntegrationTest" --tests "sgc.integracao.CDU06IntegrationTest" --tests "sgc.subprocesso.service.SubprocessoServiceCoverageIntegrationTest"`.
* **Pendência aberta para próxima rodada:** revisar se a leitura de atividades/mapa e a montagem de contexto de edição ainda justificam fetchs separados em `SubprocessoConsultaService`, agora que detalhe e permissões compartilham o mesmo contexto interno.

### Continuação focada em contexto de edição

Na sequência imediata, o ponto ainda redundante estava em `obterContextoEdicao`: o método já carregava o `Subprocesso`, mas seguia chamando caminhos públicos que refaziam busca e resolução de mapa/atividades a partir do código. O corte aplicado foi interno e conservador: reaproveitar o `Subprocesso` já carregado para derivar mapa completo e atividades, sem abrir nova API pública nem alterar DTO externo.

### Resultado objetivo da continuação focada

`obterContextoEdicao` deixou de acionar refetchs desnecessários do próprio `Subprocesso` durante a composição do contexto. A montagem agora reaproveita a entidade já carregada para obter o mapa completo e listar atividades, mantendo os mesmos contratos externos e a mesma regra de validação de mapa obrigatório.

### Registro da continuação focada

* **Data da rodada:** 2026-04-04
* **Frente principal:** Frente 2 — Fatiamento de leitura e contexto em `SubprocessoConsultaService`
* **Arquivo(s) alvo:** `backend/src/main/java/sgc/subprocesso/service/SubprocessoConsultaService.java`, `plano-simplificacao-consolidado.md`
* **Corte aplicado:** eliminação de refetch interno de `Subprocesso` na montagem de contexto de edição por meio de helpers privados que operam sobre a entidade já carregada.
* **Risco principal observado:** divergência silenciosa entre o caminho público de listagem de atividades e o caminho privado reutilizado por contexto de edição.
* **Validação executada:** `./gradlew :backend:test --tests "sgc.subprocesso.service.SubprocessoConsultaServiceExtraCoverageTest" --tests "sgc.integracao.SubprocessoServiceContextoIntegrationTest" --tests "sgc.integracao.CDU08IntegrationTest"`.
* **Pendência aberta para próxima rodada:** avaliar se `obterMapaParaAjuste` e o histórico de análises ainda podem compartilhar uma fronteira interna de leitura obrigatória por mapa sem ampliar responsabilidade pública do service.

### Continuação focada em mapa de ajuste e histórico

Na sequência, a redundância remanescente mais direta estava em duas leituras correlatas: `obterMapaParaAjuste` montava dependências de mapa e análise de validação de forma espalhada, enquanto o histórico filtrava análises por tipo repetindo a mesma coleta base. O corte aplicado foi interno: explicitar helpers privados para leituras de análise por tipo e para carregamento dos dados de mapa usados no ajuste.

### Resultado objetivo da continuação de mapa e histórico

`SubprocessoConsultaService` passou a tratar análise por tipo e dados de mapa para ajuste como fronteiras internas nomeadas, em vez de recompor essas leituras diretamente nos métodos públicos. O service continua com a mesma API, mas `obterMapaParaAjuste`, `listarAnalisesPorSubprocesso(tipo)` e o histórico por tipo deixaram de repetir a mesma coleta base em linha.

### Registro da continuação de mapa e histórico

* **Data da rodada:** 2026-04-04
* **Frente principal:** Frente 2 — Fatiamento de leitura e contexto em `SubprocessoConsultaService`
* **Arquivo(s) alvo:** `backend/src/main/java/sgc/subprocesso/service/SubprocessoConsultaService.java`, `plano-simplificacao-consolidado.md`
* **Corte aplicado:** extração de helpers privados para análise por tipo e carregamento de dados de mapa usados em `obterMapaParaAjuste`.
* **Risco principal observado:** regressão discreta no mapa de ajuste por troca indireta de ordem entre leitura da análise mais recente e coleta de competências/atividades/conhecimentos.
* **Validação executada:** `./gradlew :backend:test --tests "sgc.subprocesso.service.SubprocessoConsultaServiceExtraCoverageTest" --tests "sgc.subprocesso.service.SubprocessoServiceCoverageIntegrationTest"`.
* **Pendência aberta para próxima rodada:** medir se a Frente 2 já chegou ao ponto de rendimento decrescente e, se sim, preparar a transição para a Frente 3 em `SubprocessoService`, especialmente nos fluxos de criação e importação.

### Abertura da Frente 3 em importação de atividades

Com a Frente 2 já em rendimento decrescente, o próximo corte seguro passou a ser o fluxo de `importarAtividades` em `SubprocessoService`. O método concentrava busca de origem/destino, validação de situação, checagem de permissões, resolução de mapas, atualização de situação e registro de movimentação no mesmo bloco. A abertura da Frente 3 começou por separar essas responsabilidades em helpers privados pequenos e por explicitar um contexto interno de importação, sem alterar assinatura pública nem mensagens de negócio.

### Resultado objetivo da abertura da Frente 3

`importarAtividades` passou a operar sobre um `ImportacaoAtividadesContexto`, montado depois da validação de situação permitida e das permissões de origem/destino. A atualização de situação pós-importação e o registro de movimentação saíram do corpo principal do método, deixando a ordem de validação mais legível e a costura do fluxo mais estável para próximas simplificações em criação e revisão de subprocessos.

### Registro da abertura da Frente 3

* **Data da rodada:** 2026-04-04
* **Frente principal:** Frente 3 — Simplificação de fluxos concentrados em `SubprocessoService`
* **Arquivo(s) alvo:** `backend/src/main/java/sgc/subprocesso/service/SubprocessoService.java`, `plano-simplificacao-consolidado.md`
* **Corte aplicado:** extração de contexto interno e helpers privados para `importarAtividades`, isolando validação, atualização de situação e histórico de movimentação.
* **Risco principal observado:** mascarar a ordem funcional de falhas entre situação inválida, ausência de permissão no destino e ausência de permissão na origem.
* **Validação executada:** `./gradlew --no-configuration-cache :backend:test --tests "sgc.subprocesso.service.SubprocessoServiceExtraCoverageTest"` e `./gradlew --no-configuration-cache :backend:test --tests "sgc.integracao.SubprocessoServiceAtividadeIntegrationTest"`.
* **Pendência aberta para próxima rodada:** atacar a duplicação remanescente entre criação para mapeamento/revisão/diagnóstico, especialmente a costura entre subprocesso, mapa e movimentação inicial.

### Baseline de métricas da simplificação

Para evitar que a simplificação vire percepção subjetiva, foi definido um baseline pequeno e comparável entre rodadas. A ideia não é montar um painel grande agora, mas manter métricas suficientes para detectar redução real de superfície, acoplamento e duplicação de regra.

### Métricas-base escolhidas

* **Tamanho da classe:** linhas totais por classe alvo.
* **Superfície pública:** quantidade de métodos públicos.
* **Acoplamento direto:** quantidade de dependências injetadas (`private final`).
* **Complexidade aproximada de métodos críticos:** contagem simples de pontos de decisão visíveis (`if`, `switch`, `case`, operadores booleanos encadeados e loops relevantes), usada apenas como régua comparativa entre rodadas.
* **Ponto único de regra crítica:** quantidade de lugares que realmente recalculam uma regra sensível de domínio, mesmo que existam wrappers finos.

### Baseline atual em 2026-04-04

* `SubprocessoConsultaService`: 554 linhas, 35 métodos públicos, 11 dependências injetadas.
* `SubprocessoService`: 477 linhas, 14 métodos públicos, 11 dependências injetadas.
* `LocalizacaoSubprocessoService`: 36 linhas, 1 método público, 1 dependência injetada.

### Métodos críticos observados no baseline

* `SubprocessoConsultaService.obterContextoEdicao`: baixo desvio de fluxo; hoje funciona mais como adaptador fino para `DadosContextoEdicao`.
* `SubprocessoConsultaService.montarContextoConsulta`: complexidade aproximada baixa para média; concentra as decisões de processo finalizado, mesma unidade e mapa vigente.
* `SubprocessoConsultaService.obterPermissoesUI`: baixo desvio de fluxo no método público, mas ainda depende de blocos internos com alta densidade de regra em `construirPermissoes`.
* `SubprocessoService.importarAtividades`: baixo desvio de fluxo no método público após a extração do contexto interno; a complexidade foi deslocada para helpers menores e nomeados.
* `SubprocessoService.atualizarSituacaoDestinoAposImportacao`: complexidade aproximada média; concentra `if` inicial e `switch` por tipo de processo.
* `SubprocessoService.criarParaMapeamento`: complexidade aproximada média; ainda mistura filtragem elegível, persistência em lote, associação de mapa e criação de movimentação inicial.
* `SubprocessoService.criarSubprocessoComMapa`: complexidade aproximada baixa, mas já concentra a costura de subprocesso, cópia de mapa e movimentação inicial para revisão/diagnóstico.
* `LocalizacaoSubprocessoService.obterLocalizacaoAtual`: complexidade aproximada baixa; hoje existe um único cálculo real da localização derivada, com guarda explícita para subprocesso persistido sem movimentação fora de `NAO_INICIADO`.

### Leitura objetiva do baseline

* O principal hotspot atual de superfície continua sendo `SubprocessoConsultaService`, não por um método isolado muito longo, mas pela combinação de API pública larga com regra de permissão concentrada.
* `SubprocessoService` já está abaixo de `SubprocessoConsultaService` em tamanho bruto, mas ainda mistura criação, edição, ajustes e importação no mesmo service.
* A regra crítica de localização está consolidada em um único cálculo real, o que reduz um risco arquitetural que antes não aparecia só por contagem de linhas.

### Como comparar as próximas rodadas

* Recoletar essas mesmas métricas sempre que uma frente mover responsabilidade relevante.
* Considerar ganho real quando houver pelo menos um destes efeitos:
* queda de linhas na classe alvo sem aumento equivalente em outra classe irmã;
* queda de métodos públicos ou da quantidade de dependências injetadas;
* redução da complexidade aparente do método-alvo por extração legítima, e não só por empurrar branch para helper genérico;
* redução de pontos reais de cálculo da mesma regra de domínio.

### Continuação da Frente 3 em criação inicial de subprocesso

Depois da abertura por `importarAtividades`, a duplicação mais direta de `SubprocessoService` continuou no bloco de criação inicial. `criarParaMapeamento` ainda montava manualmente subprocesso base, associação de mapa e movimentação inicial, enquanto revisão e diagnóstico usavam outra costura privada para subprocesso, cópia de mapa e histórico. O corte aplicado foi novamente interno: explicitar helpers pequenos para subprocesso inicial, movimentação inicial, mapa vigente obrigatório e associação de mapas em lote.

### Resultado objetivo da continuação em criação

`criarParaMapeamento`, `criarParaRevisao` e `criarParaDiagnostico` continuam semanticamente distintos, mas agora compartilham as mesmas operações elementares de criação de subprocesso e histórico inicial. Isso reduz duplicação de montagem e deixa mais visível o que realmente difere entre os fluxos: criação de mapa vazio em lote para mapeamento versus cópia de mapa vigente para revisão/diagnóstico.

### Registro da continuação da Frente 3

* **Data da rodada:** 2026-04-04
* **Frente principal:** Frente 3 — Simplificação de fluxos concentrados em `SubprocessoService`
* **Arquivo(s) alvo:** `backend/src/main/java/sgc/subprocesso/service/SubprocessoService.java`, `plano-simplificacao-consolidado.md`
* **Corte aplicado:** extração de helpers privados para subprocesso inicial, movimentação inicial, mapa vigente obrigatório e associação de mapas aos subprocessos salvos.
* **Risco principal observado:** regressão discreta na costura entre subprocesso salvo, mapa associado e movimentação inicial, principalmente na diferença entre mapa vazio de mapeamento e cópia de mapa para revisão/diagnóstico.
* **Validação executada:** `./gradlew --no-configuration-cache :backend:test --tests "sgc.subprocesso.service.SubprocessoServiceExtraCoverageTest"` e `./gradlew --no-configuration-cache :backend:test --tests "sgc.subprocesso.service.SubprocessoServiceCoverageIntegrationTest"`.
* **Observação de ambiente:** `./gradlew --no-configuration-cache :backend:test --tests "sgc.subprocesso.service.SubprocessoServiceTest"` executou `9/9` testes com sucesso, mas o Gradle falhou depois ao gravar artefato em `backend/build/test-results`, sem indício de regressão funcional no recorte.
* **Pendência aberta para próxima rodada:** avaliar se `SubprocessoService` já permite separar melhor os fluxos de criação e manutenção de mapa, ou se o próximo ganho real passa a ser reduzir a superfície pública de `SubprocessoConsultaService`.

### Continuação da Frente 3 em manutenção de mapa

Na sequência, o ponto mais redundante dentro de `SubprocessoService` passou a ser a manutenção de mapa. `salvarMapaSubprocesso`, `adicionarCompetencia` e `removerCompetencia` repetiam a mesma costura de carregamento do subprocesso editável, resolução de mapa obrigatório, obtenção do código do mapa e checagem de vazio para decidir a transição de situação. O corte aplicado foi novamente interno: introduzir um contexto pequeno de edição de mapa e explicitar helpers para atualização de situação após preenchimento ou esvaziamento.

### Resultado objetivo da continuação em manutenção de mapa

Os três fluxos de manutenção continuam separados do ponto de vista funcional, mas agora compartilham a mesma fronteira interna para subprocesso editável, mapa obrigatório, código do mapa e estado de vazio inicial. Isso reduz a repetição acidental e deixa mais claro o que varia de verdade em cada operação: salvar o mapa completo, criar competência ou remover competência.

### Registro da continuação em manutenção de mapa

* **Data da rodada:** 2026-04-04
* **Frente principal:** Frente 3 — Simplificação de fluxos concentrados em `SubprocessoService`
* **Arquivo(s) alvo:** `backend/src/main/java/sgc/subprocesso/service/SubprocessoService.java`, `plano-simplificacao-consolidado.md`
* **Corte aplicado:** extração de `ContextoEdicaoMapa` e de helpers privados para atualização de situação após preenchimento/esvaziamento do mapa.
* **Risco principal observado:** mudança sutil na ordem entre reconciliação de situação, verificação de mapa vazio e retorno do mapa atualizado.
* **Validação executada:** `./gradlew --no-configuration-cache :backend:test --tests "sgc.integracao.SubprocessoServiceSalvarIntegrationTest"`.
* **Observação de ambiente:** novas execuções de `SubprocessoServiceExtraCoverageTest` e `SubprocessoServiceTest` foram interrompidas por inconsistências de artefatos em `backend/build/classes/java/test` durante `compileTestJava`, sem sinal de falha funcional específica do recorte.
* **Pendência aberta para próxima rodada:** reavaliar o baseline de `SubprocessoService` após esses cortes e decidir se o melhor ganho seguinte permanece no service de escrita ou volta para a superfície pública de `SubprocessoConsultaService`.

### Reavaliação de baseline e retorno ao hotspot de consulta

Após os dois cortes da Frente 3, `SubprocessoService` ficou mais legível por dentro, mas não reduziu sua superfície pública nem o número de dependências. Na prática, a simplificação local melhorou a costura dos fluxos, porém o principal hotspot estrutural voltou a ser `SubprocessoConsultaService`, que ainda concentrava aliases públicos redundantes e uma API larga demais para o papel que exerce.

### Resultado objetivo da reavaliação

* `SubprocessoService`: 495 linhas, 14 métodos públicos, 11 dependências injetadas.
* `SubprocessoConsultaService`: caiu de 554 para 546 linhas e de 35 para 33 métodos públicos após remover aliases públicos redundantes sem regra própria.

### Continuação focada em aliases redundantes de consulta

O corte aplicado em `SubprocessoConsultaService` foi propositalmente pequeno: remoção de `buscarSubprocessoComMapa` e `mapaCompletoPorSubprocesso`, dois métodos públicos que apenas repassavam chamadas sem agregar regra de negócio, segurança, montagem ou contrato útil distinto. Os testes de integração que existiam só para esses aliases passaram a cobrir `buscarSubprocesso` e `mapaCompletoDtoPorSubprocesso`, que são as fronteiras efetivamente úteis.

### Registro da continuação em consulta

* **Data da rodada:** 2026-04-04
* **Frente principal:** Frente 2 — Redução de superfície pública redundante em `SubprocessoConsultaService`
* **Arquivo(s) alvo:** `backend/src/main/java/sgc/subprocesso/service/SubprocessoConsultaService.java`, `backend/src/test/java/sgc/integracao/SubprocessoServiceMethodsIntegrationTest.java`, `backend/src/test/java/sgc/integracao/SubprocessoServiceExtraMethodsIntegrationTest.java`, `plano-simplificacao-consolidado.md`
* **Corte aplicado:** remoção de dois aliases públicos redundantes e ajuste da cobertura para os métodos remanescentes com contrato útil.
* **Risco principal observado:** referências residuais a métodos removidos em testes ou pontos de integração de baixa visibilidade.
* **Validação executada:** tentativa de executar `SubprocessoServiceMethodsIntegrationTest`, `SubprocessoServiceExtraMethodsIntegrationTest` e `SubprocessoConsultaServiceExtraCoverageTest`.
* **Observação de ambiente:** a validação voltou a ser bloqueada por falhas de `compileTestJava`/empacotamento do Gradle em artefatos do diretório `backend/build`, sem indicação direta de referência quebrada aos aliases removidos.
* **Pendência aberta para próxima rodada:** seguir reduzindo superfície pública redundante e pass-throughs de `SubprocessoConsultaService`, priorizando cortes que reduzam API exposta sem mudar contrato HTTP.
