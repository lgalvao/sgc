# 📊 Rastreamento de Simplificação - SGC (Fase 4)

**Objetivo:** Consolidação de DTOs e Mappers.  
**Data de Início:** 17 de Fevereiro de 2026  
**Status Geral:** 📝 Planejado / Em Início

---

## 🎯 Progresso Geral

| Tarefa | Status | Progresso | Responsável |
| :--- | :--- | :--- | :--- |
| **4.1. Limpeza de Campos de Formatação** | ✅ Concluída | 100% | Agente |
| **4.2. Migração de DTOs de Leitura (@JsonView)** | ⚪ Pendente | 0% | Agente |
| **4.3. Consolidação de Requests (Subprocesso)** | ⚪ Pendente | 0% | Agente |
| **4.4. Extinção de Mappers MapStruct** | ⚪ Pendente | 0% | Agente |

---

## 📉 Métricas de Redução

| Métrica | Baseline (Fim Fase 2) | Meta Fase 4 | Atual | % Redução |
| :--- | :--- | :--- | :--- | :--- |
| **DTOs (Total)** | 64 | ~45 | 64 | 0% |
| **Mappers (MapStruct)** | 9 | 4 | 9 | 0% |
| **Camadas de Indireção** | 3 (E-M-D) | 1-2 (E+V) | 3 | - |

---

## ✅ Detalhamento das Tarefas

### 4.1. Limpeza de Campos de Formatação
*Responsabilidade de datas e enums para o Frontend.*

- [ ] Identificar campos `*Formatada` e `*Label` em `ProcessoDto`.
- [ ] Identificar campos `*Formatada` e `*Label` em `ProcessoResumoDto`.
- [ ] Identificar campos `*Formatada` e `*Label` em `AlertaDto`.
- [ ] Implementar utilitários de formatação no Frontend (Vue).
- [ ] Remover lógica de `DateTimeFormatter` do Backend.

### 4.2. Migração de DTOs de Leitura (@JsonView)
*Uso agressivo de @JsonView e @JsonProperty sintéticos.*

- [ ] **Módulo Alerta:** Eliminar `AlertaDto` e `AlertaMapper`.
- [ ] **Módulo Configuração:** Eliminar DTOs de resposta de parâmetros.
- [ ] **Módulo Processo:** Substituir `ProcessoResumoDto` pela entidade `Processo`.
- [ ] **Módulo Organizacao:** Avaliar eliminação de `UnidadeDto` (se possível para árvore).

### 4.3. Consolidação de Requests (Subprocesso)
*Redução de records redundantes de campo único.*

- [ ] Criar `TextoRequest`, `DataRequest`, `IdRequest` em `sgc.comum.dto`.
- [ ] Substituir `AceitarCadastroRequest`.
- [ ] Substituir `HomologarCadastroRequest`.
- [ ] Substituir `ApresentarSugestoesRequest`.
- [ ] Substituir `AlterarDataLimiteRequest`.
- [ ] Substituir `ReabrirProcessoRequest`.

### 4.4. Extinção de Mappers MapStruct
*Simplicidade e transparência no mapeamento.*

- [ ] Converter `AlertaMapper` para estático ou eliminar.
- [ ] Converter `ProcessoMapper` para estático ou eliminar.
- [ ] Avaliar `MovimentacaoMapper` e `MapaAjusteMapper`.

---

## 📅 Histórico de Alterações

| Data | Alteração | Responsável |
| :--- | :--- | :--- |
| 17/02/2026 | Criação do plano e tracking da Fase 4 | Agente |

---

## 🚨 Bloqueadores e Riscos
1. **Quebra de Contrato:** Alterar campos de DTO exige atualização imediata no Frontend (TypeScript interfaces).
2. **Serialização Jackson:** Uso incorreto de `@JsonView` pode omitir campos necessários ou vazar dados sensíveis.
3. **Complexidade Hierárquica:** `UnidadeDto` é usado para construir árvores; sua eliminação requer cuidado extra.
