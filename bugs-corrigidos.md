# Relatório Integral de Correções - Auditoria REQ x E2E

## Objetivo
Consolidar, em um único documento, as correções aplicadas durante a auditoria de aderência entre requisitos (`etc/reqs`) e testes E2E (`e2e/cdu-xx.spec.ts`), incluindo bugs reais de sistema corrigidos.

---

## Correções já concluídas (rodadas anteriores)

### 1) CDU-30 - Endurecimento do E2E e remoção de mascaramento
- **Arquivo:** `e2e/cdu-30.spec.ts`
- **Problema:** branches defensivos (`if/catch`) escondiam falhas reais.
- **Correção:** fluxo reescrito com asserts obrigatórios para listagem, adição, duplicidade, remoção e bloqueio de auto-remoção.
- **Validação:** `npx playwright test e2e/cdu-30.spec.ts` (passou).

### 2) Bug real no feedback (toasts descartados)
- **Arquivo:** `frontend/src/stores/feedback.ts`
- **Problema:** mensagens novas eram descartadas quando já havia toast visível.
- **Correção:** implementação de fila de toasts com processamento sequencial.
- **Validação:** `npm run test:unit --prefix frontend -- src/stores/__tests__/feedback.spec.ts` (passou).

### 3) CDU-28 - Obrigatoriedade de data de início
- **Arquivos:** `e2e/cdu-28.spec.ts`, `frontend/src/views/CadAtribuicao.vue`, `frontend/src/views/__tests__/CadAtribuicao.spec.ts`
- **Problema:** fluxo permitia submissão sem `dataInicio`, divergindo do requisito.
- **Correção:** `dataInicio` tornou-se obrigatório no sistema e no E2E.
- **Validação:**  
  - `npx playwright test e2e/cdu-28.spec.ts` (passou)  
  - `npm run test:unit --prefix frontend -- src/views/__tests__/CadAtribuicao.spec.ts` (passou)

### 4) CDU-34 - Fluxo de lembrete com efeito de negócio
- **Arquivos:** `e2e/cdu-34.spec.ts`, `frontend/src/views/Subprocesso.vue`, `backend/src/main/java/sgc/processo/service/ProcessoFacade.java`, `backend/src/main/java/sgc/subprocesso/service/SubprocessoFacade.java`, `backend/src/main/java/sgc/subprocesso/service/workflow/SubprocessoAdminWorkflowService.java`, `backend/src/main/java/sgc/seguranca/acesso/SubprocessoAccessPolicy.java`, testes backend relacionados
- **Problemas:**
  - E2E sem validação de confirmação/modelo de mensagem.
  - Sistema não registrava movimentação interna no envio de lembrete.
  - Permissão de envio de lembrete não refletida corretamente no contexto de subprocesso.
- **Correções:**
  - Modal de confirmação para envio de lembrete.
  - Registro de movimentação `"Lembrete de prazo enviado"`.
  - Ajuste de permissão para `ENVIAR_LEMBRETE_PROCESSO`.
- **Validação:**  
  - `./gradlew :backend:test --tests 'sgc.processo.service.ProcessoFacadeTest' --tests 'sgc.integracao.CDU34IntegrationTest'` (passou)  
  - `npx playwright test e2e/cdu-34.spec.ts` (passou)

### 5) Regressão arquitetural corrigida
- **Problema:** `ProcessoFacade` passou a acessar `MovimentacaoRepo` diretamente (violação de regra arquitetural).
- **Correção:** delegação para `SubprocessoFacade`/`SubprocessoAdminWorkflowService`.
- **Validação:** `./gradlew :backend:test` com `ArchConsistencyTest` verde.

---

## Correções aplicadas nesta rodada (concluídas)

### 6) CDU-22/23/27 - Endurecimento de testes e remoção de branches defensivos
- **Arquivos E2E:** `e2e/cdu-22.spec.ts`, `e2e/cdu-23.spec.ts`, `e2e/cdu-27.spec.ts`
- **Ações aplicadas:**
  - Remoção de padrões `isVisible().catch(() => false)` em passos críticos.
  - Cenários de cancelamento com verificação explícita de permanência na tela.
  - CDU-27 com fluxo completo de alteração de data (abrir modal, preencher data, confirmar, validar feedback).

### 7) Correção funcional em ação em bloco (CDU-22/23)
- **Arquivos:** `frontend/src/stores/processos/workflow.ts`, `frontend/src/services/processoService.ts`, `frontend/src/composables/useProcessoView.ts`
- **Problemas encontrados durante execução E2E:**
  - Frontend chamava endpoints antigos de subprocesso para ação em bloco (payload incompatível, 400).
  - Endpoint correto (`/processos/{codigo}/acao-em-bloco`) exigia enum em maiúsculo (`ACEITAR`, `HOMOLOGAR`, `DISPONIBILIZAR`).
  - Mensagens de sucesso genéricas divergiam dos requisitos de CDU-22/23.
- **Correções:**
  - Store passou a usar endpoint unificado de processo para ações em bloco.
  - Payload convertido para enum em maiúsculo no service.
  - Mensagens de sucesso específicas:
    - `"Cadastros aceitos em bloco"`
    - `"Cadastros homologados em bloco"`
  - Fluxo do aceite em bloco redireciona para `/painel` (conforme requisito do CDU-22).

### 8) Atualização de testes unitários impactados
- **Arquivos:**  
  - `frontend/src/views/__tests__/ProcessoView.spec.ts`  
  - `frontend/src/stores/__tests__/processos.spec.ts`  
  - `frontend/src/services/__tests__/processoService.spec.ts`
- **Ajustes:** asserts atualizados para novo contrato (mensagem, redirecionamento e uso de endpoint unificado).
- **Validação unitária:** `87 passed`.

### 9) CDU-24 - Disponibilização em bloco alinhada ao requisito
- **Arquivos:** `e2e/cdu-24.spec.ts`, `frontend/src/composables/useProcessoView.ts`
- **Problemas:**
  - E2E com branches defensivos no botão/modal.
  - Mensagem de sucesso diferente do texto do requisito.
  - Fluxo de disponibilização em bloco não redirecionava ao painel.
- **Correções:**
  - E2E endurecido com asserts obrigatórios para botão, modal, campo de data limite e confirmação.
  - Mensagem ajustada para `"Mapas de competências disponibilizados em bloco"`.
  - Redirecionamento para `/painel` após disponibilização em bloco.
- **Validação:** `npx playwright test e2e/cdu-24.spec.ts --reporter=list` (6/6).

### 10) CDU-32 - Reabrir cadastro + bug real de template de e-mail
- **Arquivos:** `e2e/cdu-32.spec.ts`, `backend/src/main/resources/templates/email/cadastro-reaberto.html`, `backend/src/main/resources/templates/email/revisao-cadastro-reaberta.html`
- **Problemas:**
  - E2E com branches defensivos e preparação incompleta (sem homologação prévia).
  - Bug real no sistema: template Thymeleaf `cadastro-reaberto` ausente (erro em runtime ao enviar e-mail da transição).
- **Correções:**
  - Preparação do E2E corrigida para homologar cadastro antes da reabertura.
  - Cenário de confirmação validando sucesso, situação e movimentação.
  - Criação dos templates de e-mail faltantes para reabertura de cadastro e revisão.
- **Validação:** `npx playwright test e2e/cdu-32.spec.ts --reporter=list` (8/8) sem erro de template.

### 11) CDU-33 - Reabrir revisão de cadastro (endurecimento completo)
- **Arquivo:** `e2e/cdu-33.spec.ts`
- **Problemas:**
  - Setup usava tipo/unidade incompatíveis para revisão.
  - Fluxo com `if/catch` defensivo mascarava ausência de pré-condições.
- **Correções:**
  - Preparação reescrita para fluxo real: criar revisão (`ASSESSORIA_12`) -> disponibilizar -> homologar.
  - Cenários obrigatórios com assert explícito: situação homologada, botão/modal, cancelamento e confirmação da reabertura com justificativa.
  - Validação de efeito de negócio: situação volta para "Revisão de cadastro em andamento" e movimentação registrada.
- **Validação:** `npx playwright test e2e/cdu-33.spec.ts --reporter=list` (**7/7**).

### 12) CDU-07 - Cobertura ampliada para múltiplos perfis
- **Arquivo:** `e2e/cdu-07.spec.ts`
- **Problema:** cobertura restrita a CHEFE, sem validar comportamento de ADMIN e GESTOR.
- **Correções:**
  - Cenário ampliado para ADMIN/GESTOR/CHEFE no mesmo fluxo.
  - Unidade ajustada para `SECAO_211` com hierarquia consistente (`COORD_21`).
  - Navegação por perfil validada explicitamente (subprocesso para ADMIN/CHEFE e visibilidade da unidade no detalhe para GESTOR).
- **Validação:** `npx playwright test e2e/cdu-07.spec.ts --reporter=list` (**1/1**).

---

## Estado atual da execução

- **Verde nesta rodada:**
- **Verde nesta rodada:**
  - `CDU-22`: preparação + cancelamento + confirmação do aceite em bloco.
  - `CDU-23`: preparação + cancelamento + confirmação da homologação em bloco.
  - `CDU-27`: preparação + navegação + alteração de data limite.
  - `CDU-24`: confirmação de disponibilização em bloco com data limite e redirecionamento.
  - `CDU-32`: reabertura de cadastro com preparo correto e validação completa do fluxo.
  - `CDU-33`: reabertura de revisão com preparo completo e confirmação sem branch defensivo.
  - `CDU-07`: detalhamento validado em fluxo multi-perfil.
  - Unit tests frontend relacionados (`87 passed`).

- **Ajuste final concluído no CDU-23:**
  - Assert de URL pós-homologação passou a validar padrão robusto de rota (`/processo/{codigo}`), removendo dependência de variável local suscetível a inconsistência em execução serial.

---

## Comandos de validação usados

```bash
./gradlew :backend:test
./gradlew :backend:test --tests 'sgc.processo.service.ProcessoFacadeTest' --tests 'sgc.integracao.CDU34IntegrationTest'

npx playwright test e2e/cdu-28.spec.ts --reporter=list
npx playwright test e2e/cdu-30.spec.ts --reporter=list
npx playwright test e2e/cdu-34.spec.ts --reporter=list
npx playwright test e2e/cdu-22.spec.ts e2e/cdu-23.spec.ts e2e/cdu-27.spec.ts --reporter=list
npx playwright test e2e/cdu-24.spec.ts --reporter=list
npx playwright test e2e/cdu-32.spec.ts --reporter=list
npx playwright test e2e/cdu-33.spec.ts --reporter=list
npx playwright test e2e/cdu-07.spec.ts --reporter=list

npm run test:unit --prefix frontend -- src/stores/__tests__/feedback.spec.ts
npm run test:unit --prefix frontend -- src/views/__tests__/CadAtribuicao.spec.ts
npm run test:unit --prefix frontend -- src/services/__tests__/processoService.spec.ts src/stores/__tests__/processos.spec.ts src/views/__tests__/ProcessoView.spec.ts
```
