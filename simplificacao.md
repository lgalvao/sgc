# Plano de Simplificação SGC - Tarefas Pendentes

## Contexto do Sistema

- **Usuários totais:** ~500 pessoas
- **Usuários simultâneos:** Máximo de 10 pessoas  
- **Demanda de performance:** Leve - não justifica complexidade excessiva

**Princípios Norteadores:**
- ✅ **Simplicidade** sobre otimização prematura
- ✅ **Consistência** sobre diversidade de padrões
- ✅ **Clareza** sobre abstrações complexas

---

## 📊 Estado Atual da Implementação

### Fase 1: Remoção de Código Morto - ✅ 95% CONCLUÍDA

**Backend:**
- ✅ Removidos 3 eventos Spring mortos (~240 linhas)
- ✅ Removido teste duplicado (~263 linhas)
- ✅ Removidos 4 Repository Services (~765 linhas)
- ✅ Removido cache de atribuições (~25 linhas)
- ✅ Centralizada validação duplicada getMensagemErroUnidadesSemMapa (~20 linhas)

**Frontend:**
- ✅ Padrão unificado de erro (lastError) implementado em todas as stores
- ✅ Store processos.ts refatorada com sub-stores especializadas
- ✅ diagnosticoService convertido para objeto com funções
- ⏸️ useStoreLoading composable (não crítico - useErrorHandler já existe)

**Impacto:** ~1.313 linhas removidas no backend

---

### Fase 2: Simplificação de Arquitetura - ⚠️ 70% CONCLUÍDA

**Backend:**
- ✅ ProcessoDetalheBuilder refatorado (4→2 loops consolidados)
- ✅ MapaManutencaoService modularizado em 5 services especializados
- ❌ **PENDENTE:** Extrair métodos privados de SubprocessoFacade

**Frontend:**
- ✅ Store processos.ts modularizada
- ✅ diagnosticoService como funções
- ✅ Padrão consistente de imports

**Próximas Ações:**

#### 2.1 Extrair Métodos Privados de SubprocessoFacade

**Problema:** SubprocessoFacade tem 16 métodos privados complexos misturando responsabilidades.

**Solução:** Criar 4-5 services especializados:

1. **SubprocessoMapaService** (~200 linhas)
   - `salvarAjustesMapaInterno()`
   - `validarSituacaoParaAjuste()`
   - `obterMapaParaAjusteInterno()`

2. **SubprocessoAtividadeService** (~150 linhas)
   - `atualizarDescricoesAtividades()`
   - `importarAtividadesInterno()`
   - `listarAtividadesSubprocessoInterno()`

3. **SubprocessoCompetenciaService** (~100 linhas)
   - `atualizarCompetenciasEAssociacoes()`

4. **SubprocessoContextoService** (~150 linhas)
   - `obterDetalhesInterno()` (2 versões)
   - `obterCadastroInterno()`
   - `obterSugestoesInterno()`
   - `obterContextoEdicaoInterno()`

5. **SubprocessoPermissoesService** (~100 linhas)
   - `obterPermissoesInterno()`
   - `calcularPermissoesInterno()`

**Impacto:** ~700 linhas movidas, melhor separação de responsabilidades

---

### Fase 3: Correção de Performance - ❌ 30% CONCLUÍDA

**Backend:**

#### 3.1 ❌ CRÍTICO: Corrigir N+1 Query em ProcessoDetalheBuilder

**Problema:** 
```java
// ProcessoDetalheBuilder.java linha 56-74
private boolean isCurrentUserChefeOuCoordenador(...) {
    // Faz N+1 queries - findByUsuarioTitulo sem @EntityGraph
    UsuarioPerfil perfil = usuarioPerfilRepo.findByUsuarioTitulo(usuario.getTitulo());
    ...
}
```

**Solução:**
1. Adicionar método em `UsuarioPerfilRepo`:
```java
@EntityGraph(attributePaths = {"unidade", "atribuicoesTemporarias"})
Optional<UsuarioPerfil> findByUsuarioTituloComAtribuicoes(String titulo);
```

2. Atualizar ProcessoDetalheBuilder para usar novo método

**Impacto:** Redução de 50-70% em queries N+1

---

#### 3.2 ❌ IMPORTANTE: Configurar TaskExecutor para 10 Usuários

**Problema:** TaskExecutor usa padrão Spring (threads ilimitadas)

**Solução:** Adicionar em `application.properties`:
```properties
# Configuração para 10 usuários simultâneos
spring.task.execution.pool.core-size=5
spring.task.execution.pool.max-size=10
spring.task.execution.pool.queue-capacity=25
spring.task.execution.thread-name-prefix=sgc-async-
```

**Impacto:** Uso eficiente de recursos para carga real

---

**Frontend:**

#### 3.3 ⏸️ OPCIONAL: Otimizar Lookups em Stores

**Problema:** Stores usam `.find()` linear em arrays (O(n))

**Exemplos:**
- `perfil.ts` linha 39: `perfisUnidades.find(p => p.perfil === value)`
- `unidades.ts` linhas 37-44: buscas repetidas

**Solução:** Usar `Map` para lookups O(1)
```typescript
// Antes
const perfil = perfisUnidades.find(p => p.perfil === value)

// Depois  
const perfilMap = new Map(perfisUnidades.map(p => [p.perfil, p]))
const perfil = perfilMap.get(value)
```

**Impacto:** Melhoria marginal (~5-10%) - não crítico para 10 usuários

---

### Fase 4: Documentação - ✅ 85% CONCLUÍDA

- ✅ 36 packages com package-info.java
- ✅ Eventos vs chamadas diretas documentado
- ✅ Composables documentados
- ✅ Stores com guia de convenções

**Nenhuma ação pendente crítica**

---

## 🎯 Plano de Execução Priorizado

### P0 - CRÍTICO (Executar Agora)

1. **Corrigir N+1 query em ProcessoDetalheBuilder** (~30 min)
   - Adicionar método com @EntityGraph em UsuarioPerfilRepo
   - Atualizar ProcessoDetalheBuilder
   - Validar com testes

2. **Configurar TaskExecutor** (~10 min)
   - Adicionar propriedades em application.properties
   - Validar configuração

### P1 - IMPORTANTE (Próximas Horas)

3. **Extrair services de SubprocessoFacade** (~2-3 horas)
   - Criar SubprocessoMapaService
   - Criar SubprocessoAtividadeService
   - Criar SubprocessoCompetenciaService
   - Criar SubprocessoContextoService
   - Criar SubprocessoPermissoesService
   - Atualizar SubprocessoFacade para usar novos services
   - Atualizar testes

### P2 - OPCIONAL (Se Houver Tempo)

4. **Otimizar lookups em stores** (~30 min)
   - Converter .find() para Map em perfil.ts
   - Converter .find() para Map em unidades.ts

5. **Criar useStoreLoading composable** (~20 min)
   - Extrair padrão comum de loading
   - Documentar uso

---

## 📊 Métricas de Impacto Esperado

### Após Completar Tarefas Pendentes

| Categoria | Meta Final | Impacto |
|-----------|------------|---------|
| **Linhas Removidas Backend** | ~1.500 | Fase 2: +700 linhas movidas |
| **N+1 Queries Reduzidos** | 70-90% | Fase 3.1: @EntityGraph |
| **Separação Responsabilidades** | Alta | SubprocessoFacade modularizado |
| **Configuração Performance** | Otimizada | TaskExecutor para 10 usuários |

---

## 📝 Registro de Execução

**Última atualização:** 2026-01-30

**Status Geral:** 75% completo

**Próxima ação:** Iniciar P0 (N+1 query fix)
