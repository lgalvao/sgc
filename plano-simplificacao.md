# Plano de Simplificação do SGC

Documento operacional com foco no **que ainda falta fazer** e em lições úteis para as próximas rodadas.

## Premissas (mantidas)

* simplificar sem alterar regra de negócio, contrato HTTP, DTO externo, transações e regras de acesso;
* evitar abstrações genéricas e camadas de compatibilidade;
* priorizar redução de leitura acidental, branches e acoplamento local.

## Situação atual (abril/2026)

### Já estabilizado nas últimas rodadas

* `SubprocessoConsultaService` ficou mais previsível no fluxo de permissões e no caminho de detalhe:
  * cálculo de permissões separado da montagem do DTO;
  * menos código morto e menos camada intermediária sem ganho real.
* `ProcessoCadastroView.vue` teve redução de duplicação no fluxo de carregar/salvar/iniciar/remover:
  * hidratação de edição centralizada;
  * fechamento de modal e criação pré-início menos repetitivos;
  * remoção de branch morta.
* Compatibilidade residual removida em `ModalAcaoBloco.vue` (campo não usado).
* `SubprocessoTransicaoService` — fetch de `Subprocesso` movido para métodos públicos:
  * `executarAceite`, `executarHomologacao` e `executarDevolucao` passam a receber `Subprocesso` em vez de `Long`;
  * operações em bloco (`aceitarCadastroEmBloco`, `homologarCadastroEmBloco`) eliminam fetch extra e derivam `isRevisao` diretamente do SP já carregado;
  * método privado `isRevisao(Long)` removido.
* `CadastroView.vue` — estado inicial consolidado:
  * `sincronizarEstadoInicialContexto` passou a incluir `atividadesSnapshotInicial`, `disponibilizacaoSemMudancas` e `unidade`, eliminando duplicação em `carregarContextoInicial` e `handleImportAtividades`;
  * dois watchers com lógica idêntica mesclados em um único `watch([...])`.

## O que ainda precisa ser feito

### 1) Frente 1 — `SubprocessoTransicaoService` (continua pendente, mas avançou)

**Objetivo:** reduzir mistura de workflow, persistência, notificação, alerta e e-mail no mesmo fluxo.

**Próximos cortes recomendados (incrementais):**

* `normalizarTexto()` ainda é chamado ad-hoc em vários pontos antes de repassar ao workflow; considerar centralizar na entrada pública (ou no builder do command);
* `registrarWorkflowComDestino()` é um adaptador sem lógica — avaliar se vale inlinar em `registrarAnalise()`;
* duplicação residual de "buscar + validar situação" em `executarAceiteValidacao` e `executarHomologacaoValidacao`.

**Restrições:**

* não criar dispatcher/strategy/facade interna para "esconder" complexidade;
* não alterar regra funcional de transição;
* manter no máximo 3 parâmetros por método público/privado novo (usar command quando necessário).

### 2) Frente 3 — views grandes ainda pendentes

**Alvos prioritários:**

* `MapaView.vue`
* `AtribuicaoTemporariaView.vue`

**Foco da próxima rodada:**

* reduzir lógica incidental no `<script setup>` (watchers e sincronizações repetidas);
* remover inferências locais de acesso quando o backend/contexto já entrega a decisão;
* centralizar tratamento de erro repetido sem criar camada genérica demais.

### 3) Frente 5 — hotspot fora de subprocesso

* `E2eController` continua grande; ainda falta recorte por grupos coesos de endpoint com medição simples (tamanho + acoplamento + frequência de alteração).

## Lições aprendidas relevantes

* simplificação efetiva aqui funciona melhor com **cortes pequenos e reversíveis**, não com extrações arquiteturais amplas;
* nem toda extração reduz complexidade: se o tipo/helper só encapsula passagem de dados sem clareza líquida, tende a piorar;
* no frontend, ganhos reais vieram de remover duplicação de fluxo e branches mortos, não de criar novos composables genéricos;
* evitar "compatibilidade por precaução": campos/aliases sem uso real acumulam dívida e confundem manutenção;
* mover o fetch do agregado para o método público (em vez de passar ID e buscar internamente) elimina fetches duplicados nas operações em bloco e torna o fluxo de dados explícito.

## Sequência recomendada (atualizada)

1. continuar em `SubprocessoTransicaoService` com cortes nos pontos residuais acima;
2. escolher **uma** view grande por rodada (próxima: `MapaView.vue`);
3. reavaliar `E2eController` só após avanço concreto nas duas frentes acima.

## Validação mínima por rodada

### Backend

* `./gradlew :backend:compileTestJava`
* `./gradlew :backend:test`

### Frontend

* `npm run typecheck`
* `npm run lint`
* `npm run test:unit`

### QA dashboard

Quando a rodada afetar testes/lint/typecheck/cobertura/E2E:

* `npm run qa:dashboard`

Fontes de verdade:

* `etc/qa-dashboard/latest/ultimo-snapshot.json`
* `etc/qa-dashboard/latest/ultimo-resumo.md`
