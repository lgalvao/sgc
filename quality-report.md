# Relatório de Qualidade de Código (Frontend + Backend)

## Escopo e evidências objetivas

Este diagnóstico foi baseado em validações e auditorias executadas no repositório:

- Baseline backend: `./gradlew --no-daemon --no-configuration-cache :backend:test` (1716 testes passando).
- Baseline frontend: `npm run lint`, `npm run typecheck`, `npm run test:unit` (133 arquivos de teste e 1283 testes passando).
- Cheiros gerais: `npm run smells:auditar`.
- Cruft frontend: `npm run frontend:cruft`.
- Cobertura/riscos backend: `node etc/scripts/sgc.js backend cobertura auditoria`.
- Cobertura/riscos frontend: `node etc/scripts/sgc.js frontend cobertura auditoria`.

## Achados principais

### 1) Complexidade e acoplamento excessivo no backend

- `backend/src/main/java/sgc/processo/service/ProcessoService.java` com **1408 linhas** (arquivo mais extenso do backend).
- Auditoria de cobertura aponta esse serviço como **P1 crítico** (impacto 656.5; 25 linhas + 33 branches sem teste).
- O serviço concentra múltiplas responsabilidades (consulta, workflow, validação, notificações, ações em bloco), elevando custo de manutenção e risco de regressão.

### 2) Concentração de lógica e parsing em views do frontend

- `frontend/src/views/FeedbacksAdminView.vue` aparece como hotspot de cobertura (**P2 alto**, 24 statements sem teste).
- `frontend/src/views/ProcessoCadastroView.vue` (404 linhas) e `frontend/src/views/FeedbacksAdminView.vue` (370 linhas) acumulam lógica de transformação e tratamento local.
- Em `FeedbacksAdminView.vue` havia repetição de mapeamentos de tipo de feedback (rótulo/variante/ícone) e parsing concentrado de metadados.

### 3) Sinais de robustez defensiva em excesso

Da auditoria de cheiros/cruft:

- Backend: 126 DTOs com `@Nullable`, 240 checks explícitos de null, 9 usos de `Objects.isNull/nonNull`.
- Frontend produção: 28 checks de null, 71 fallbacks defensivos, 58 blocos `catch`.
- Score global de cheiros: **1715 (crítico)**; score de cruft frontend: **475 (crítico)**.

### 4) Fragmentação de risco de testes/cobertura

- Cobertura global é boa, mas com hotspots relevantes:
  - Backend P1: `ProcessoService`, `ValidadorDadosOrganizacionais`, `ImpactoMapaService`.
  - Frontend P2/P3: `FeedbacksAdminView.vue`, `AtribuicaoTemporariaView.vue`, `ModalAcaoBloco.vue`.
- Risco localizado: mudanças em fluxos principais tendem a ser caras por baixa isolação por responsabilidade.

## Ações recomendadas (priorizadas)

### Prioridade alta (iniciadas nesta rodada)

1. **Reduzir repetição e ramificações no `ProcessoService`**  
   Consolidar verificações semelhantes de elegibilidade/permissão de ações em bloco em helpers únicos, mantendo contratos e regras de acesso intactos.

2. **Simplificar `FeedbacksAdminView`**  
   Centralizar configuração de tipos de feedback e modularizar transformação de metadados para reduzir complexidade ciclomática local e facilitar testes unitários focados.

### Prioridade alta (próximos passos)

3. **Fatiar `ProcessoService` por responsabilidade funcional**  
   Separar gradualmente workflow de notificações, montagem de DTO de detalhe e regras de ação em bloco em serviços coesos, preservando API pública.

4. **Extrair lógica de transformação das views para módulos de apoio**  
   Mover parsing/normalização para arquivos `src/views/*.ts` (ou helpers locais de módulo), reduzindo carga nas SFCs e melhorando testabilidade.

5. **Endurecer cobertura nos hotspots P1/P2**  
   Adicionar testes comportamentais para cenários de erro/edge-cases de `ProcessoService` e `FeedbacksAdminView`/`AtribuicaoTemporariaView`.

### Prioridade média

6. **Ratcheting contínuo dos budgets de qualidade**  
   Definir metas progressivas para reduzir checks nulos/fallbacks defensivos e impedir regressão de cruft.

7. **Remoção sistemática de código redundante/depreciado**  
   Revisão orientada por hotspots para eliminar caminhos mortos e APIs de suporte a fluxos antigos.

## Trabalho já iniciado nesta tarefa

Foram aplicadas simplificações concretas de maior impacto local sem alteração de contrato externo:

- **Frontend (`FeedbacksAdminView.vue`)**
  - Unificação da configuração de tipo de feedback (rótulo/variante/ícone) em fonte única.
  - Redução de duplicação na renderização de ícones.
  - Extração de passos de compactação/formatação de metadados para funções coesas.

- **Backend (`ProcessoService.java`)**
  - Consolidação de lógica repetida de elegibilidade/permissão de ações em bloco com helper dedicado.
  - Redução de duplicação entre métodos `podeAceitar*` e `podeHomologar*`.

## Critérios de sucesso para continuidade

- Manter todos os testes/lint/typecheck passando.
- Reduzir tamanho e responsabilidades por arquivo sem quebrar contratos HTTP/DTO.
- Priorizar cortes pequenos com validação imediata.
