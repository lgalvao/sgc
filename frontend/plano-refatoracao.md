# Plano de Refatoração Frontend e Integração Backend

Este documento descreve a análise do estado atual do frontend e backend, identificando lógicas duplicadas, mocks e pontos de melhoria na integração, além de traçar um plano para adoção completa do BootstrapVueNext.

## 1. Análise de Lógica Duplicada e Mockada

### 1.1. Cálculo de Impacto no Mapa (Crítico)
**Problema:** O frontend implementa uma lógica complexa de "Revisão" e cálculo de impactos no cliente.
- **Arquivos Afetados:** `frontend/src/stores/revisao.ts`, `frontend/src/components/ImpactoMapaModal.vue`.
- **Lógica:** O store `revisao.ts` armazena mudanças em `sessionStorage` e calcula manualmente quais competências são impactadas por alterações em atividades.
- **Solução Backend:** O backend já possui `SubprocessoMapaControle.verificarImpactos` (retornando `ImpactoMapaDto` via `GET /api/subprocessos/{id}/impactos-mapa`).
- **Ação:** Remover a lógica de cálculo do frontend. O frontend deve apenas enviar as ações (adicionar/remover atividade) para o backend e, ao solicitar visualização de impacto, consultar o endpoint.

### 1.2. Regras de Visualização e Permissões
**Problema:** Regras de negócio sobre visibilidade de botões estão hardcoded no frontend.
- **Arquivos Afetados:** `CadMapa.vue`.
- **Lógica:** `podeVerImpacto` verifica `Perfil.ADMIN` e status do subprocesso localmente.
- **Solução Backend:** O DTO `SubprocessoDetalhe` retorna `permissoes`. É necessário adicionar `podeVisualizarImpacto` neste DTO.
- **Ação:** Adicionar flag ao backend e consumir no frontend.

### 1.3. Hierarquia e Busca de Unidades
**Problema:** O frontend carrega árvores de unidades e realiza buscas recursivas pesadas no cliente em `unidades.ts` e `CadMapa.vue`.
- **Ação:** Substituir a recursão no frontend por chamadas diretas à API.

## 2. Adoção do BootstrapVueNext

O projeto já utiliza BootstrapVueNext, mas a migração está incompleta em componentes como `SubprocessoModal.vue`, `PainelView.vue`, e `SubprocessoCards.vue`.

## 3. Plano de Execução Detalhado

### Passo 1: Backend - Permissões de Impacto
- [x] 1.  Adicionar campo `podeVisualizarImpacto` (boolean) ao `SubprocessoPermissoesDto`.
- [x] 2.  Implementar lógica no `SubprocessoPermissoesService`: `(isAdmin) && (situacao == ATIVIDADES_HOMOLOGADAS || situacao == MAPA_AJUSTADO)`.
- [x] 3.  Verificar testes backend.

### Passo 2: Frontend - Tipagem e Serviço de Impacto
- [x] 1.  Criar interfaces TypeScript em `frontend/src/types/impacto.ts` espelhando `ImpactoMapaDto`, `AtividadeImpactadaDto`, `CompetenciaImpactadaDto`.
- [x] 2.  Adicionar método `verificarImpactos(idSubprocesso: number): Promise<ImpactoMapa>` em `frontend/src/services/mapaService.ts`.

### Passo 3: Frontend - Refatoração do Modal de Impacto
- [x] 1.  Alterar `ImpactoMapaModal.vue`:
    -   Remover dependência de `revisaoStore.mudancasParaImpacto`.
    -   Adicionar chamada a `mapaService.verificarImpactos` ao abrir o modal.
    -   Atualizar template para consumir a estrutura do `ImpactoMapa`.
- [x] 2.  Testar o componente refatorado.

### Passo 4: Frontend - Integração de Permissões e Limpeza
- [x] 1.  Em `CadMapa.vue`, substituir a computed `podeVerImpacto` pela propriedade vinda do backend `subprocesso.permissoes.podeVisualizarImpacto`.
- [x] 2.  Remover lógica de cálculo de impacto de `frontend/src/stores/revisao.ts`.
- [ ] 3.  Executar `npm run typecheck` e garantir integridade.
