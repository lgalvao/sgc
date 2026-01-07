# Plano de Melhoria de Testabilidade - SGC Backend

**Data:** 2026-01-07  
**Baseado em:** Análise do BACKLOG_TESTABILIDADE.md e cobertura atual

---

## Métricas Atuais

| Métrica | Valor Anterior | Valor Atual | Meta | Status |
|---------|----------------|-------------|------|--------|
| Cobertura de Linhas | 99.50% | **99.50%** | 100% | ⏺️ Estável |
| Cobertura de Branches | 88.52% | **89.05%** | 95% | ⬆️ Melhorado |
| Total de Testes | 1081 | **1093** | - | ✅ +12 testes |
| Linhas Perdidas | ~88 | **~88** | 0 | ⏺️ Estável |

---

## 📊 Análise de Cobertura Detalhada

### Componentes com 100% de Cobertura de Linhas ✅

Os seguintes módulos atingiram cobertura perfeita de linhas:
- `sgc.processo.service` (100%)
- `sgc.comum.util` (100%)
- `sgc.comum.model` (100%)
- `sgc.processo.dto.mappers` (100%)
- `sgc.organizacao.mapper` (100%)
- `sgc.subprocesso.service.decomposed` (100%)
- Diversos outros módulos menores

### Top 5 - Arquivos com Mais Branches Perdidos (Prioridade Alta)

| Arquivo | Branches Perdidos | Linhas Perdidas | Prioridade | Status Recente |
|---------|-------------------|-----------------|------------|----------------|
| `SubprocessoPermissoesService` | **4** (era 13) | 0 | 🔴 Alta | ✅ Melhorado significativamente |
| `ProcessoService` | 14 | 26 | 🔴 Alta | ⚠️ Regressão ou medição mais precisa |
| `PainelService` | 29 | 89 | 🔴 Alta | ⚠️ Requer atenção imediata |
| `SubprocessoMapaWorkflowService` | 9 | 0 | 🟡 Média | Pendente |
| `UsuarioService` | 8 | 0 | 🟡 Média | Pendente |

**Nota:** A contagem de branches/linhas perdidas para `PainelService` e `ProcessoService` parece ter aumentado ou sido recalibrada no último relatório. A investigação revelou que `PainelService` possui lógica complexa de visibilidade recursiva que precisa de casos de teste específicos.

---

## 📋 Linhas Não Cobertas Identificadas

### Serviços de Domínio

| Arquivo | Linhas | Contexto | Categoria |
|---------|--------|----------|-----------|
| `PainelService` | Várias | Lógica de visibilidade recursiva e links | Lógica de Negócio |
| `SubprocessoMapaService` | 167 | Validação específica ou branch raro | Validação |
| `SubprocessoCadastroWorkflowService` | 203 | Condição de borda em workflow | Workflow |
| `ValidadorDadosOrganizacionais` | 118 | Validação de dados inválidos | Validação |
| `AnalisadorCompetenciasService` | 145 | Branch complexo de análise | Lógica de Negócio |
| `AtividadeFacade` | 39 | Método não coberto | Serviço |

### Listeners e Eventos

| Arquivo | Linhas | Contexto | Categoria |
|---------|--------|----------|-----------|
| `EventoProcessoListener` | 123, 124 | Exceção no loop externo (difícil de simular) | Exception Handling |

### Controllers

| Arquivo | Linhas | Contexto | Categoria |
|---------|--------|----------|-----------|
| `SubprocessoCadastroController` | 326 | Tratamento de erro ou validação | Error Handling |

---

## 🎯 Estratégias de Teste Aplicadas

### 1. Cobertura de Branches Críticos ✅
- **SubprocessoPermissoesService**: Reduzido de 13 para 4 branches perdidos.
  - Testes adicionados para validação de ações (`validar`).
  - Cobertura completa da matriz de permissões (`calcularPermissoes`).
  - Verificação de edge cases (unidade nula, mapa vazio).

- **ProcessoService**:
  - Testes para validação de segurança (`checarAcesso`) com hierarquia.
  - Testes para inicialização de workflows (Mapeamento, Revisão, Diagnóstico).
  - Testes para finalização de processos com validações.
  - Testes para listagens com filtros de segurança.

- **PainelService**:
  - Testes unitários para `listarProcessos` com diferentes perfis (ADMIN, GESTOR, CHEFE).
  - Testes para `listarAlertas` com filtros de usuário e unidade.
  - Verificação de cálculo de links de destino.
  - Tratamento de exceções em formatação de unidades.

---

## 🚀 Plano de Execução para Atingir Metas

### Fase 1: Cobertura de Branches (Prioridade Máxima)
**Meta:** Atingir 95% de branch coverage

1. **PainelService** (Prioridade Imediata)
   - Focar na lógica de `selecionarIdsVisiveis` e `encontrarMaiorIdVisivel`.
   - Simular hierarquias de unidade complexas para validar a recursividade.
   - Cobrir os catch blocks de exceções ignoradas.

2. **ProcessoService** (Refinamento)
   - Analisar os 14 branches perdidos restantes.
   - Provavelmente relacionados a streams e Optionals encadeados em `checarAcesso` e `buscarCodigosDescendentes`.

3. **SubprocessoMapaWorkflowService** (Próximo)
   - Atacar os 9 branches perdidos.

### Fase 2: Linhas Restantes (Prioridade Alta)
**Meta:** Atingir 100% de line coverage

1. **Código Defensivo Alcançável**
   - `PainelService`: blocos try-catch vazios.
   - `SubprocessoCadastroController` linha 326
   - `ValidadorDadosOrganizacionais` linha 118

2. **Exception Handling**
   - `EventoProcessoListener` linhas 123-124 (requer mock específico)

### Fase 3: Limpeza e Documentação
1. Remover código morto (se identificado)
2. Documentar casos de teste complexos
3. Atualizar este documento com resultados finais

---

## ✅ Progresso e Conquistas

### Melhorias Implementadas
- ✅ +12 testes adicionados recentemente (Total +67 desde o início)
- ✅ Cobertura de branches global melhorada para ~89%
- ✅ `SubprocessoPermissoesService` deixou de ser o maior ofensor.
- ✅ `PainelService` agora tem uma suíte de testes unitários dedicada.
- ✅ `ProcessoService` tem cobertura robusta de segurança e workflow.

### Próximos Marcos
- [ ] Branch coverage ≥ 90%
- [ ] Branch coverage ≥ 92%
- [ ] Branch coverage ≥ 95% (Meta Final)
- [ ] Line coverage ≥ 99.8%
- [ ] Line coverage = 100% (Meta Final)

---

**Última Atualização:** 2026-01-07  
**Status:** 🟢 Em Progresso - Foco em PainelService e ProcessoService
