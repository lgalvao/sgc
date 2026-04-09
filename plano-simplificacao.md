# Plano de Simplificação do SGC

Documento operacional com foco exclusivo no que ainda falta fazer. Este arquivo deve refletir o código atual; histórico de rodadas concluídas, medições antigas e cortes já aplicados não devem permanecer aqui.

Premissa operacional:

* o sistema é simples, com baixa concorrência e volume moderado de dados;
* simplificação aqui significa reduzir leitura, ramificações e superfície do código;
* evitar abstrações genéricas, infraestrutura interna “elegante” e preparo especulativo para escala que o sistema não precisa.

## Objetivo

Reduzir complexidade acidental sem alterar:

* regras de negócio;
* contratos HTTP;
* DTOs externos;
* regras de acesso;
* transações;
* textos relevantes de interface.

## Fontes de verdade

Ordem de precedência:

* `AGENTS.md`
* `etc/docs/regras-acesso.md`
* `etc/reqs`
* este plano

## Achados confirmados no código atual

### Backend

* `backend/src/main/java/sgc/subprocesso/service/SubprocessoTransicaoService.java` continua sendo o principal hotspot:
  * arquivo ainda muito grande;
  * muitas dependências diretas;
  * workflow, persistência, notificação, e-mail e alertas ainda convivem no mesmo service.
* `backend/src/main/java/sgc/subprocesso/service/SubprocessoConsultaService.java` ainda mistura:
  * leitura de entidade;
  * montagem de detalhe/contexto;
  * cálculo de permissões para UI.
* `backend/src/main/java/sgc/subprocesso/service/SubprocessoService.java` segue grande, mas não há evidência de que deva liderar a fila antes dos dois pontos acima.
* `backend/src/main/java/sgc/e2e/E2eController.java` continua extenso e com muitos endpoints/fixtures, mas ainda precisa de medição melhor antes de um corte estrutural.

### Frontend

* Existem views grandes com responsabilidades misturadas, especialmente:
  * `frontend/src/views/CadastroView.vue`
  * `frontend/src/views/ProcessoCadastroView.vue`
  * `frontend/src/views/MapaView.vue`
  * `frontend/src/views/AtribuicaoTemporariaView.vue`
* `frontend/src/components/comum/LoadingButton.vue` segue sendo um wrapper fino.
* `LoadingButton.vue` é amplamente usado e já possui stories e testes, então qualquer remoção ou fusão exige auditoria explícita; não é um alvo de remoção automática.

## Prioridades pendentes

## Frente 1 — `SubprocessoTransicaoService`

Objetivo:

* reduzir mistura de responsabilidades no workflow sem mudar contrato externo.

Próximo corte:

* priorizar cortes que removam código ou o substituam por helpers locais mais curtos;
* manter juntos apenas os trechos que realmente pertencem ao mesmo fluxo de negócio;
* evitar dispatcher, hierarquia interna de comandos, strategies ou camadas privadas genéricas para alertas/notificações.

Restrições:

* não criar facade nova só para esconder complexidade;
* não misturar refatoração estrutural com mudança de regra;
* se surgir método com mais de 3 parâmetros, usar objeto de transporte.
* não aceitar simplificação que aumente o arquivo de forma líquida sem remover complexidade visível no fluxo principal.

Critério de pronto:

* fluxo principal mais legível;
* menos branches e menos dependências cruzadas por responsabilidade;
* diff pequeno e defensável, preferencialmente com redução líquida de código ou aumento mínimo justificado;
* sem regressão em testes focados do backend.

## Frente 2 — `SubprocessoConsultaService`

Objetivo:

* separar leitura básica da composição de detalhe/contexto/permissões.

Próximo corte:

* isolar melhor a montagem de permissões de UI sem criar camada nova;
* reduzir a mistura entre fetch de dados, contexto rico e payload de resposta;
* preferir extrair helpers privados curtos ou remover pass-throughs restantes antes de introduzir novos tipos internos;
* manter DTOs externos e contratos HTTP intactos.

Critério de pronto:

* leitura simples mais previsível;
* menos lógica incidental em torno de detalhe/permissões;
* menos motivos para esse service crescer em direções diferentes.

## Frente 3 — Views grandes do frontend

Objetivo:

* reduzir lógica incidental em `<script setup>` e tornar sincronização de dados mais explícita.

Próximo corte:

* priorizar views que acumulam carregamento, transformação, decisão de fluxo e tratamento de erro no mesmo arquivo;
* reduzir dependência de estado implícito quando a tela já tem o dado necessário no contexto local;
* manter `normalizeError` e exibição de erro nas camadas corretas.

Critério de pronto:

* menos branches e watchers acidentais por view;
* menos parsing ou sincronização repetidos;
* leitura mais curta do fluxo principal da tela.

## Frente 4 — Auditoria de wrappers visuais

Objetivo:

* revisar wrappers finos com critério explícito, sem remoção automática.

Escopo inicial:

* começar por `frontend/src/components/comum/LoadingButton.vue`;
* decidir entre manter, ajustar ou remover com base em:
  * padronização real;
  * redução de duplicação;
  * acessibilidade/comportamento adicional;
  * custo de manutenção dos usos, stories e testes.

Critério de pronto:

* decisão explícita por wrapper auditado;
* nenhuma remoção baseada apenas em tamanho pequeno do componente.

## Frente 5 — Hotspots fora de `subprocesso`

Objetivo:

* atacar arquivos grandes fora da frente principal apenas quando houver diagnóstico suficiente.

Escopo inicial:

* medir `backend/src/main/java/sgc/e2e/E2eController.java` por grupos de endpoints e helpers;
* só escolher extração depois de confirmar coesão real.

Critério de pronto:

* hotspot priorizado por evidência;
* corte pequeno e validável.

## Sequência recomendada

1. Continuar pela Frente 1.
2. Retomar a Frente 2 assim que houver um corte pequeno e seguro.
3. Escolher uma view grande do frontend como primeiro alvo real da Frente 3.
4. Tratar `LoadingButton.vue` como item de baixa prioridade até surgir evidência concreta de custo real.
5. Reavaliar `E2eController` só depois das frentes acima ou quando ele bloquear alguma rodada.

## Validação mínima por rodada

### Backend

* `./gradlew :backend:compileTestJava`
* `./gradlew :backend:test`

### Frontend

* `npm run typecheck`
* `npm run lint`
* `npm run test:unit`

### QA dashboard

Quando a rodada afetar testes, lint, typecheck, cobertura ou E2E:

* `npm run qa:dashboard`

Fonte de verdade:

* `etc/qa-dashboard/latest/ultimo-snapshot.json`
* `etc/qa-dashboard/latest/ultimo-resumo.md`
