# Plano de Refatoração do Frontend SGC

**Versão:** 3.0 (Atualizado após conclusão das Fases 1 e 3)  
**Data:** 07 de dezembro de 2025  
**Objetivo:** Remover lógica de negócio, validações complexas, filtragens e ordenações desnecessárias do frontend, transferindo essas responsabilidades para o backend.

---

## Status Geral

### ✅ Fases Concluídas

#### Fase 1: Validações Críticas (COMPLETA)
- ✅ REF-001: CadAtividades - Validações movidas para backend
- ✅ REF-002: CadMapa - Bean Validation implementado
- ✅ REF-003: CadProcesso - Validações de data e elegibilidade no backend
- **Resultado**: 6 novos testes, 552 testes passando, ~70 linhas removidas do frontend

#### Fase 3: Otimização de DTOs (COMPLETA)
- ✅ Labels em português para `TipoProcesso` e `SituacaoProcesso`
- ✅ Criado `FormatadorData` para formatação centralizada
- ✅ Campos formatados em `ProcessoDto` e `ProcessoDetalheDto`
- ✅ Frontend atualizado para usar campos formatados (com fallbacks)
- **Resultado**: 8 novos testes, backend fornece dados pré-formatados

---

## Fases Pendentes

### Fase 2: Módulo Diagnóstico (PRÓXIMA PRIORIDADE)

**Objetivo:** Criar backend completo para funcionalidades de diagnóstico  
**Estimativa:** 3-4 semanas  
**Prioridade:** ALTA - Funcionalidade crítica sem backend

#### Contexto

Atualmente, `DiagnosticoEquipe.vue` e `OcupacoesCriticas.vue` contêm lógica de negócio que deveria estar no backend:
- Validação de avaliações pendentes (frontend, linhas 225-230)
- Inicialização com valores padrão (frontend, linhas 176-186)
- Cálculo de gaps e criticidades (não implementado)
- TODO na linha 243 indica falta de backend

**CRÍTICO:** Não existe módulo `sgc.diagnostico` no backend.

#### Tarefas

##### REF-004: Criar Módulo Diagnóstico Backend

**Estrutura a Criar:**

```
backend/src/main/java/sgc/diagnostico/
├── model/
│   ├── Diagnostico.java
│   ├── AvaliacaoCompetencia.java
│   ├── OcupacaoCritica.java
│   └── SituacaoDiagnostico.java (enum)
├── dto/
│   ├── DiagnosticoDto.java
│   ├── AvaliacaoRequest.java
│   ├── AvaliacaoDto.java
│   ├── OcupacaoCriticaRequest.java
│   └── OcupacaoCriticaDto.java
├── DiagnosticoRepo.java
├── AvaliacaoCompetenciaRepo.java
├── OcupacaoCriticaRepo.java
├── DiagnosticoService.java
└── DiagnosticoController.java
```

**Endpoints Necessários:**

```java
POST   /api/diagnosticos/{subprocessoId}/avaliacoes        // Salvar avaliação
GET    /api/diagnosticos/{subprocessoId}/avaliacoes        // Buscar avaliações
POST   /api/diagnosticos/{subprocessoId}/finalizar         // Finalizar diagnóstico
GET    /api/diagnosticos/{subprocessoId}                   // Buscar diagnóstico
POST   /api/diagnosticos/{subprocessoId}/ocupacoes         // Salvar ocupações críticas
GET    /api/diagnosticos/{subprocessoId}/ocupacoes         // Buscar ocupações críticas
```

**Validações Backend:**

- Importância e domínio entre 1-5
- Todas competências avaliadas antes de finalizar
- Cálculo automático de gaps (importância - domínio)
- Cálculo de criticidade baseado em gap e importância

**Arquivos Frontend a Simplificar:**

- `frontend/src/views/DiagnosticoEquipe.vue` (~100 linhas a remover)
- `frontend/src/views/OcupacoesCriticas.vue` (~80 linhas a remover)
- Criar `frontend/src/services/diagnosticoService.ts`

**Critérios de Sucesso:**

- [ ] Módulo `sgc.diagnostico` criado e completo
- [ ] 6+ endpoints REST funcionais
- [ ] Cobertura de testes >80% no módulo
- [ ] Frontend integrado e funcional
- [ ] Dados persistem no banco
- [ ] README.md do módulo criado
- [ ] Testes E2E passam

**Comandos de Verificação:**

```bash
./gradlew :backend:test --tests "sgc.diagnostico.*"
cd frontend && npm run typecheck && npm run lint
npm test  # E2E
```

---

### Fase 4: Filtragens e Ordenações

**Objetivo:** Backend fornece dados filtrados e ordenados  
**Estimativa:** 1-2 semanas  
**Prioridade:** MÉDIA

#### Tarefas

##### REF-005: Árvore de Unidades com Filtros

**Problema Atual:**
- `ArvoreUnidades.vue` (linhas 89-103): Filtragem de elegibilidade no frontend
- `ArvoreUnidades.vue` (linhas 105-120): Cálculo de `isElegivel` no frontend

**Solução:**

Endpoint melhorado:
```java
GET /api/unidades/arvore?tipoProcesso={tipo}&ocultarRaiz={boolean}
```

Backend deve:
- Calcular `isElegivel` baseado no tipo de processo
- Retornar árvore sem SEDOC se `ocultarRaiz=true`
- Filtrar unidades não elegíveis se solicitado

**Arquivos:**
- `backend/src/main/java/sgc/unidade/UnidadeController.java`
- `backend/src/main/java/sgc/unidade/UnidadeService.java`
- `frontend/src/components/ArvoreUnidades.vue` (~30 linhas a remover)

---

##### REF-006: Ordenação de Listas

**Problema Atual:**
- Múltiplos componentes ordenam dados localmente
- Lógica de ordenação duplicada

**Solução:**

Adicionar query params de ordenação:
```java
GET /api/processos?sortBy={campo}&sortOrder={asc|desc}
GET /api/atividades?sortBy={campo}&sortOrder={asc|desc}
```

**Arquivos:**
- Controllers principais (Processo, Atividade, etc.)
- Componentes Vue que fazem ordenação local

---

### Fase 5: Refatoração de Stores

**Objetivo:** Simplificar stores removendo lógica complexa  
**Estimativa:** 1 semana  
**Prioridade:** BAIXA (manutenibilidade)

#### Tarefas

##### REF-007: Simplificar Stores

**Problema Atual:**
- Stores contêm computeds complexos
- Lógica de transformação de dados nas stores
- Mapeamentos que deveriam estar no backend

**Solução:**

1. Revisar cada store (`frontend/src/stores/*.ts`)
2. Remover computeds complexos - mover para backend
3. Padronizar estrutura de todas as stores
4. Usar dados do backend diretamente

**Arquivos:**
- `frontend/src/stores/processos.ts`
- `frontend/src/stores/subprocessos.ts`
- `frontend/src/stores/atividades.ts`
- Outros stores conforme necessário

---

## Melhorias Opcionais (Fase 3 - Continuação)

Estas melhorias podem ser feitas a qualquer momento para completar 100% da Fase 3:

### OPT-001: Remover Função `situacaoLabel` Completamente

**Estimativa:** 1-2 horas  
**Benefício:** ~40 linhas removidas

**Passos:**
1. Atualizar `SubprocessoCards.vue` para receber `situacaoLabel` como prop
2. Atualizar `SubprocessoView.vue` para passar label ao invés de enum
3. Remover função `situacaoLabel` de `utils/index.ts`
4. Remover imports da função em todos os componentes

### OPT-002: Usar Campos Formatados de Datas

**Estimativa:** 2-3 horas  
**Benefício:** ~20 linhas removidas

**Passos:**
1. Identificar usos de `formatDateBR` no frontend
2. Substituir por campos `*Formatada` onde disponíveis
3. Manter `formatDateBR` apenas para casos sem campo formatado

---

## Ordem de Execução Recomendada

```
1. Fase 2 (Módulo Diagnóstico) - 3-4 semanas
   └─ Funcionalidade crítica, maior impacto

2. Fase 4 (Filtragens e Ordenações) - 1-2 semanas
   └─ Melhora performance e simplifica frontend

3. Fase 5 (Stores) - 1 semana
   └─ Polimento final, manutenibilidade

4. Melhorias Opcionais - Conforme tempo disponível
   └─ Pequenas otimizações incrementais
```

---

## Comandos Essenciais

```bash
# Verificação completa
./gradlew :backend:test
cd frontend && npm run typecheck && npm run lint
npm test  # E2E quando necessário

# Commits
git add -A
git commit -m "refactor(módulo): descrição

- Detalhe 1
- Detalhe 2

Refs: Fase X do plano de refatoração"
```

---

## Regras Fundamentais

1. **SEMPRE** leia `AGENTS.md` antes de começar
2. **SEMPRE** execute testes após modificações
3. **NUNCA** modifique código não relacionado à tarefa
4. **SEMPRE** use Português Brasileiro
5. **SEMPRE** commits pequenos e focados
6. **SEMPRE** verifique que builds passam

---

## Referências Rápidas

### Estrutura Backend
```
backend/src/main/java/sgc/
├── comum/           # Exceções, DTOs base, utilitários
├── processo/        # Processos
├── subprocesso/     # Workflows
├── mapa/            # Mapas de competências
├── atividade/       # CRUD de atividades
├── diagnostico/     # [CRIAR NA FASE 2]
├── unidade/         # Estrutura organizacional
├── notificacao/     # Notificações
└── alerta/          # Alertas UI
```

### Estrutura Frontend
```
frontend/src/
├── components/      # Componentes reutilizáveis
├── views/          # Páginas
├── stores/         # Estado global (Pinia)
├── services/       # Comunicação com API
├── router/         # Rotas
├── utils/          # Funções utilitárias
└── types/          # Definições TypeScript
```

---

**Documento atualizado:** 07 de dezembro de 2025  
**Versão:** 3.0  
**Status:** Fases 1 e 3 concluídas, Fase 2 é a próxima prioridade

**Changelog:**
- **v3.0 (07/12/2025 20:22):** Plano enxuto focado em fases pendentes
  - Resumidas Fases 1 e 3 (concluídas)
  - Detalhada Fase 2 (próxima prioridade)
  - Simplificadas Fases 4 e 5
  - Adicionadas melhorias opcionais
  - Removido conteúdo detalhado de tarefas já concluídas
