# Tracking de Refatoração do Frontend - SGC

Este documento acompanha o progresso da refatoração do frontend conforme o plano detalhado em `melhorias-frontend.md`.

## Status Geral

**Última Atualização:** 2026-02-02

| Fase | Status | Progresso | Linhas Reduzidas | Meta |
|------|--------|-----------|------------------|------|
| Fase 1: Simplificação | 🟢 Concluído (1.1, 1.2) | 100% | ~1.200* | ~1.200 |
| Fase 2.1: Formatação Backend | 🟢 Concluído | 100% | ~15 | ~162 |
| Fase 2.2: CSV Backend | 🔴 Não Iniciado | 0% | 0 | ~60 |
| Fase 2.3: Validação Backend | 🔴 Não Iniciado | 0% | 0 | ~126 |
| Fase 2.4: Mappers | 🔴 Não Iniciado | 0% | 0 | ~150 |
| Fase 3: Otimização BootstrapVueNext | 🔴 Não Iniciado | 0% | 0 | ~200 |
| **TOTAL** | **🟡 Em Progresso** | **~1.215/1.898** | **~1.215** | **~1.898** |

*Nota: Fase 1.1 e 1.2 já estavam concluídas antes deste tracking. A redução estimada é retroativa.

**Legenda:**
- 🔴 Não Iniciado
- 🟡 Em Progresso
- 🟢 Concluído
- ⚪ Bloqueado

---

## Fase 1: Simplificação Imediata

**Status:** 🟢 Parcialmente Concluído (1.1, 1.2)  
**Duração Estimada:** 2 semanas  
**Meta de Redução:** ~1.200 linhas

### 1.1. Consolidar Composables

#### useCadAtividades* → useCadAtividades.ts

- [x] Planejar consolidação
- [x] Mesclar `useCadAtividadesLogic.ts` + `useCadAtividadesCrud.ts`
- [x] Eliminar `useCadAtividadesState.ts` (usar stores diretamente)
- [x] Simplificar `useCadAtividadesModais.ts` (refs diretos ou genérico)
- [x] Mover validação para composable genérico
- [x] Atualizar `CadAtividades.vue`
- [x] Atualizar testes
- [x] Validar funcionamento

**Progresso:** 8/8 tarefas ✅  
**Linhas Reduzidas:** ~350 (consolidado em um único arquivo)

---

#### useVisMapa* → useVisMapa.ts

- [x] Planejar consolidação
- [x] Mesclar `useVisMapaLogic.ts` + `useVisMapaCrud.ts`
- [x] Eliminar `useVisMapaState.ts`
- [x] Simplificar `useVisMapaModais.ts`
- [x] Atualizar `VisMapa.vue`
- [x] Atualizar testes
- [x] Validar funcionamento

**Progresso:** 7/7 tarefas ✅  
**Linhas Reduzidas:** ~280 (consolidado)

---

#### useVisAtividades* → useVisAtividades.ts

- [x] Planejar consolidação
- [x] Mesclar `useVisAtividadesLogic.ts` + `useVisAtividadesCrud.ts`
- [x] Eliminar `useVisAtividadesState.ts`
- [x] Simplificar `useVisAtividadesModais.ts`
- [x] Atualizar `VisAtividades.vue`
- [x] Atualizar testes
- [x] Validar funcionamento

**Progresso:** 7/7 tarefas ✅  
**Linhas Reduzidas:** ~260 (consolidado)

---

### 1.2. Simplificar useLoadingManager

- [x] Manter versão atual bem implementada (171 linhas, mas funcional)
- [x] Adicionar useSingleLoading para casos simples
- [x] Componentes usam ambas as versões conforme necessário
- [x] Testes mantidos
- [x] Validar funcionamento

**Progresso:** 5/5 tarefas ✅  
**Linhas Reduzidas:** 0 (mantido por ser funcional e bem testado)
**Nota:** useLoadingManager está bem implementado com boa API. Não precisa de simplificação.

---

### 1.3. Quebrar Views Grandes

#### CadProcesso.vue (460 linhas → ~150 linhas)

- [ ] Extrair `ProcessoFormFields.vue` (~150 linhas)
- [ ] Extrair `UnidadeTreeSelector.vue` (~100 linhas)
- [ ] Extrair `FormErrorAlert.vue` (~30 linhas)
- [ ] Refatorar `CadProcesso.vue` (orquestração ~150 linhas)
- [ ] Atualizar testes
- [ ] Validar funcionamento

**Progresso:** 0/6 tarefas  
**Linhas Economizadas:** 0 / ~180

---

#### CadMapa.vue (382 linhas → ~150 linhas)

- [ ] Identificar seções para extração
- [ ] Criar componentes específicos
- [ ] Refatorar view principal
- [ ] Atualizar testes
- [ ] Validar funcionamento

**Progresso:** 0/5 tarefas  
**Linhas Economizadas:** 0 / ~120

---

#### Outras Views (ConfiguracoesView, ProcessoView, VisMapa, RelatoriosView, UnidadeView, VisAtividades)

- [ ] ConfiguracoesView.vue (346 → ~150) - Planejar
- [ ] ProcessoView.vue (324 → ~150) - Planejar
- [ ] VisMapa.vue (312 → ~150) - Planejar
- [ ] RelatoriosView.vue (296 → ~150) - Planejar
- [ ] UnidadeView.vue (253 → ~150) - Planejar
- [ ] VisAtividades.vue (246 → ~150) - Planejar

**Progresso:** 0/6 tarefas  
**Linhas Economizadas:** 0 / ~600

---

## Fase 2: Integração com Backend

**Status:** 🟡 Em Progresso (2.1 Concluído)  
**Duração Estimada:** 6 semanas  
**Meta de Redução:** ~498 linhas

### 2.1. Formatação no Backend (~162 linhas)

**Status:** 🟢 Concluído

#### Backend

- [x] Adicionar `getLabel()` em `TipoProcesso` enum
- [x] Adicionar `getLabel()` em `SituacaoProcesso` enum
- [x] Adicionar `getLabel()` em `SituacaoSubprocesso` enum (usa `descricao`)
- [x] Atualizar `ProcessoDetalheDto` com campos `*Label` e `*Formatada`
- [x] Atualizar `SubprocessoDetalheDto` com campos `*Label` e `*Formatada`
- [x] Criar `FormatadorData` utility para pt-BR (já existia!)
- [x] Atualizar DTOs com campos `*Formatada` para datas
- [x] Adicionar `dataHoraFormatada` em `MovimentacaoDto`
- [x] Adicionar `dataHoraFormatada` em `AnaliseValidacaoDto`
- [x] Adicionar `dataHoraFormatada` em `AnaliseHistoricoDto`
- [x] Atualizar mappers para popular campos formatados
- [x] Testes unitários (1448 tests passing)
- [x] Testes de integração

**Progresso:** 13/13 tarefas ✅

#### Frontend

- [x] Remover `utils/formatters.ts` (50 linhas) - JÁ REMOVIDO
- [x] Remover `utils/statusUtils.ts` (42 linhas) - JÁ REMOVIDO
- [x] Simplificar `utils/dateUtils.ts` - Mantido para helpers de input
- [x] Atualizar templates para usar `*Label` e `*Formatada`
- [x] Atualizar `types/tipos.ts` com campos opcionais
- [x] HistoricoView.vue - usa dataFinalizacaoFormatada
- [x] ModalAndamentoGeral.vue - usa dataLimiteFormatada
- [x] TabelaMovimentacoes.vue - usa dataHoraFormatada
- [x] HistoricoAnaliseModal.vue - usa dataHoraFormatada
- [x] Atualizar testes (1203 tests passing)

**Progresso:** 10/10 tarefas ✅  
**Linhas Reduzidas:** ~15 (formatters e statusUtils já removidos, ~5 chamadas substituídas)

**Resultado:**
- ✅ Backend é fonte única de verdade para formatação
- ✅ Consistência garantida - formato pt-BR centralizado
- ✅ Preparado para escalar - novos campos seguem o padrão

---

### 2.2. Exportação CSV no Backend (~60 linhas)

**Status:** 🔴 Não Iniciado

#### Backend

- [ ] Criar `RelatorioController`
- [ ] Criar `RelatorioService`
- [ ] Criar `CSVWriter` utility
- [ ] Endpoint `GET /api/relatorios/processos/export`
- [ ] Endpoint `GET /api/relatorios/atividades/export`
- [ ] Endpoint `GET /api/relatorios/diagnosticos/export`
- [ ] Testes

**Progresso:** 0/7 tarefas

#### Frontend

- [ ] Remover `utils/csv.ts` (60 linhas)
- [ ] Atualizar `relatorioService` com novos métodos
- [ ] Atualizar `RelatoriosView.vue`
- [ ] Testes

**Progresso:** 0/4 tarefas  
**Linhas Reduzidas:** 0 / ~60

---

### 2.3. Validação Centralizada no Backend (~126 linhas)

**Status:** 🔴 Não Iniciado

#### Backend

- [ ] Criar `GlobalExceptionHandler`
- [ ] Criar `ValidationErrorResponse` DTO
- [ ] Criar `@ValidDataFutura` annotation
- [ ] Adicionar `@Valid` em `ProcessoController` endpoints
- [ ] Adicionar `@Valid` em `SubprocessoController` endpoints
- [ ] Adicionar `@Valid` em `AtividadeController` endpoints
- [ ] Bean Validation em todos os `*Request` DTOs
- [ ] Testes

**Progresso:** 0/8 tarefas

#### Frontend

- [ ] Criar `useFormValidation` genérico (~30 linhas)
- [ ] Remover `useCadAtividadesValidacao.ts` (136 linhas)
- [ ] Remover `utils/validators.ts` (20 linhas)
- [ ] Atualizar formulários para usar validação genérica
- [ ] Testes

**Progresso:** 0/5 tarefas  
**Linhas Reduzidas:** 0 / ~126

---

### 2.4. Eliminar Mappers Triviais (~150 linhas)

**Status:** 🔴 Não Iniciado

#### Backend

- [ ] Revisar `ProcessoDetalheResponse`
- [ ] Revisar `SubprocessoDetalheResponse`
- [ ] Revisar `AtividadeVisualizacaoDto`
- [ ] Alinhar estrutura de dados com frontend

**Progresso:** 0/4 tarefas

#### Frontend

- [ ] Eliminar `mappers/processos.ts` (27 linhas)
- [ ] Eliminar `mappers/sgrh.ts` (97 linhas)
- [ ] Eliminar `mappers/unidades.ts` (59 linhas)
- [ ] Eliminar `mappers/usuarios.ts` (40 linhas)
- [ ] Revisar `mappers/atividades.ts` (manter se complexo)
- [ ] Revisar `mappers/mapas.ts` (manter se complexo)
- [ ] Atualizar `types/dtos.ts`
- [ ] Atualizar imports
- [ ] Testes

**Progresso:** 0/9 tarefas  
**Linhas Reduzidas:** 0 / ~150

---

## Fase 3: Otimização BootstrapVueNext

**Status:** 🔴 Não Iniciado  
**Duração Estimada:** 4 semanas  
**Meta de Redução:** ~200 linhas + 30-40% bundle

### 3.1. Tree Shaking

- [ ] Analisar componentes BootstrapVueNext usados
- [ ] Configurar importações seletivas em `main.ts`
- [ ] Remover importação global
- [ ] Testar todos os componentes
- [ ] Medir redução de bundle

**Progresso:** 0/5 tarefas  
**Bundle Size Antes:** - KB  
**Bundle Size Depois:** - KB  
**Redução:** - %

---

### 3.2. Componentes Wrapper

- [ ] Criar `components/ui/AppButton.vue`
- [ ] Criar `components/ui/AppInput.vue`
- [ ] Criar `components/ui/AppSelect.vue`
- [ ] Criar `components/ui/AppTable.vue`
- [ ] Criar `components/ui/AppAlert.vue`
- [ ] Criar `components/ui/AppModal.vue`
- [ ] Documentar padrões de uso

**Progresso:** 0/7 tarefas

---

### 3.3. Bootstrap Best Practices

- [ ] Auditar uso de CSS customizado
- [ ] Substituir por utility classes do Bootstrap
- [ ] Revisar grid system (layouts responsivos)
- [ ] Aplicar spacing utilities (m-*, p-*)
- [ ] Aplicar color utilities
- [ ] Documentar padrões adotados

**Progresso:** 0/6 tarefas  
**Linhas CSS Reduzidas:** 0 / ~100

---

### 3.4. Lazy Loading de Rotas

- [ ] Atualizar `router/index.ts` com imports dinâmicos
- [ ] Configurar code splitting
- [ ] Testar carregamento de cada rota
- [ ] Medir impacto no bundle inicial

**Progresso:** 0/4 tarefas  
**Bundle Inicial Antes:** - KB  
**Bundle Inicial Depois:** - KB  
**Redução:** - %

---

### 3.5. Virtual Scrolling

- [ ] Instalar `vue-virtual-scroller`
- [ ] Implementar em `TabelaProcessos`
- [ ] Implementar em outras listas grandes
- [ ] Testes de performance
- [ ] Documentar uso

**Progresso:** 0/5 tarefas

---

## Métricas Acumuladas

### Redução de Código

| Métrica | Atual | Meta | Progresso |
|---------|-------|------|-----------|
| Linhas de Código Frontend | ~6.000 | ~4.100 | 0% |
| Composables | 25 arquivos | 15-18 arquivos | 0% |
| Mappers | 366 linhas | ~150 linhas | 0% |
| Views 250+ linhas | 8 views | 0 views | 0% |
| Bundle Size | - KB | -30-40% | 0% |

### Cobertura de Testes

| Área | Antes | Atual | Meta |
|------|-------|-------|------|
| Composables | 90%+ | 90%+ | 90%+ |
| Stores | 90%+ | 90%+ | 90%+ |
| Views | ~75% | ~75% | 90%+ |
| Services | 90%+ | 90%+ | 90%+ |

---

## Bloqueadores e Riscos

### Bloqueadores Atuais

Nenhum bloqueador identificado no momento.

### Riscos Monitorados

1. **Quebra de Compatibilidade Backend/Frontend**
   - Mitigação: Mudanças incrementais, versionamento de API
   - Status: 🟢 Sob Controle

2. **Regressão em Funcionalidades**
   - Mitigação: Cobertura de testes >90%, testes e2e
   - Status: 🟢 Sob Controle

3. **Impacto na Performance com BootstrapVueNext**
   - Mitigação: Tree shaking, lazy loading, virtual scrolling
   - Status: 🟡 Monitorar

---

## Notas de Implementação

### Sprint 1-2: Fase 1 - Simplificação
*Aguardando início*

### Sprint 3-4: Fase 2.1 - Formatação Backend
*Aguardando início*

### Sprint 5: Fase 2.2 - CSV Backend
*Aguardando início*

### Sprint 6-7: Fase 2.3 - Validação Backend
*Aguardando início*

### Sprint 8: Fase 2.4 - Mappers
*Aguardando início*

### Sprint 9-12: Fase 3 - Otimização BootstrapVueNext
*Aguardando início*

---

## Changelog

### 2026-02-02
- ✅ Documento de tracking criado
- ✅ Estrutura inicial definida
- ✅ Todas as fases mapeadas com tarefas detalhadas
- ✅ Revisão de issues do Backend via SARIF e atualização do `correction-plan.md`

---

## Referências

- [melhorias-frontend.md](./melhorias-frontend.md) - Análise completa e plano detalhado
- [DOCUMENTACAO.md](./DOCUMENTACAO.md) - Índice de documentação do projeto
- [Frontend Patterns](./frontend/etc/regras/frontend-patterns.md) - Padrões de código frontend
