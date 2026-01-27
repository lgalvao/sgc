# 🔧 Sprint 4 - Otimizações Opcionais

**Duração Estimada:** Conforme necessário  
**Objetivo:** Refinamentos e otimizações incrementais  
**Foco:** Implementar apenas se houver necessidade demonstrada com dados reais

---

## ⚠️ ATENÇÃO: Sprint Opcional

Esta sprint contém otimizações **não críticas** que devem ser implementadas **apenas se houver necessidade demonstrada
**. Não implemente por "achismo" - sempre meça antes e depois.

**Princípio YAGNI:** You Aren't Gonna Need It (Você não vai precisar disso)

---

## 📋 Sumário de Ações

| #  | Ação                                       | Prioridade | Esforço  | Impacto  | Quando Implementar                        |
|----|--------------------------------------------|------------|----------|----------|-------------------------------------------|
| 9  | Implementar cache HTTP parcial (frontend)  | 🟡 Média   | 🟡 Médio | 🟡 Baixo | **SE** UX apresentar latência perceptível |
| 13 | Adicionar @EntityGraph onde apropriado     | 🟢 Baixa   | 🟡 Médio | 🟢 Baixo | **SE** surgir problema N+1 medido         |
| 14 | Decompor `processos.ts` store (345 linhas) | 🟢 Baixa   | 🔴 Alto  | 🟢 Baixo | **SE** manutenção se tornar difícil       |

**Resultado Esperado:** Otimizações pontuais apenas quando justificadas por métricas reais.

---

## 🎯 Ação #9: Implementar Cache HTTP Parcial (Frontend)

### ⚠️ Critérios de Implementação

**IMPLEMENTE APENAS SE:**

- ✅ UX apresenta latência perceptível (medida > 500ms)
- ✅ Dados são acessados múltiplas vezes na mesma sessão
- ✅ Dados têm baixa taxa de mudança (< 1x por sessão)
- ✅ Validação com usuários reais confirma necessidade

**NÃO IMPLEMENTE SE:**

- ❌ Performance atual é aceitável (< 500ms)
- ❌ Dados mudam frequentemente
- ❌ Apenas por "achismo" de que seria melhor

### Contexto

Com a eliminação da cascata de reloads (Sprint 2, Ação #5), a maioria das requisições duplicadas será eliminada. Cache
HTTP adicional só se justifica se houver navegação muito frequente entre as mesmas páginas.

### Análise de Necessidade

**Cenário Real - Navegação de Usuário:**

```
1. Usuário acessa lista de processos
   → GET /api/processos (200ms)

2. Usuário clica no Processo #123
   → GET /api/processos/123/detalhes (150ms)

3. Usuário navega para Subprocesso #456
   → GET /api/subprocessos/456/detalhes (180ms)

4. Usuário volta para Processo #123 (breadcrumb)
   → GET /api/processos/123/detalhes  ❓ Cache útil aqui?
```

**Com Ação #5 (Sprint 2) implementada:**

- Dados completos retornados na primeira requisição
- Necessidade de cache reduzida em 60-70%

**Benefício Potencial:**

- Para 20 usuários simultâneos: ~100-200 requests/dia economizados
- Redução de latência: ~150ms por hit de cache
- **Benefício marginal** - complexidade adicionada pode não valer a pena

### Solução (SE implementar)

**Implementação Simples - Map-based Cache:**

**Arquivo:** `/frontend/src/utils/httpCache.ts` (NOVO)

```typescript
/**
 * Cache HTTP simples baseado em Map.
 * 
 * ⚠️ ATENÇÃO: Implementar apenas se houver necessidade demonstrada.
 * Medir performance antes e depois.
 */

interface CachedData<T> {
  data: T;
  timestamp: number;
  ttl: number; // Time To Live em milissegundos
}

/**
 * Cache HTTP com TTL (Time To Live).
 * 
 * @example
 * ```typescript
 * // Uso em service
 * export async function obterProcesso(codigo: number) {
 *   const cacheKey = `processo:${codigo}`;
 *   
 *   // Tentar buscar do cache
 *   const cached = httpCache.get<ProcessoDetalhado>(cacheKey);
 *   if (cached) {
 *     return cached;
 *   }
 *   
 *   // Se não houver, buscar da API
 *   const data = await apiClient.get<ProcessoDetalhado>(`/api/processos/${codigo}`);
 *   
 *   // Armazenar no cache (5 minutos)
 *   httpCache.set(cacheKey, data, 5 * 60 * 1000);
 *   
 *   return data;
 * }
 * ```

*/
class HttpCache {
private cache = new Map<string, CachedData<any>>();
private defaultTTL = 5 * 60 * 1000; // 5 minutos

/**

* Busca item do cache.
* Retorna null se não encontrado ou expirado.
  */
  get<T>(key: string): T | null {
  const cached = this.cache.get(key);
  if (!cached) {
  return null;
  }

    const now = Date.now();
    if (now - cached.timestamp > cached.ttl) {
      this.cache.delete(key);
      return null;
    }

    return cached.data as T;

}

/**

* Armazena item no cache.
*
* @param key - Chave única do cache
* @param data - Dados a serem armazenados
* @param ttl - Tempo de vida em ms (opcional, padrão 5 min)
  */
  set<T>(key: string, data: T, ttl?: number): void {
  this.cache.set(key, {
  data,
  timestamp: Date.now(),
  ttl: ttl ?? this.defaultTTL
  });
  }

/**

* Invalida entradas do cache que correspondem ao padrão.
*
* @example
* ```typescript
* // Invalidar todos os processos
* httpCache.invalidate('processo:');
*
* // Invalidar processo específico
* httpCache.invalidate('processo:123');
*
* // Usar regex
* httpCache.invalidate(/processo:\d+/);
* ```

*/
invalidate(pattern: string | RegExp): void {
const keys = Array.from(this.cache.keys());
const toDelete = typeof pattern === 'string'
? keys.filter(k => k.includes(pattern))
: keys.filter(k => pattern.test(k));

    toDelete.forEach(k => this.cache.delete(k));

}

/**

* Limpa todo o cache.
  */
  clear(): void {
  this.cache.clear();
  }

/**

* Retorna estatísticas do cache (para debug).
  */
  getStats(): { size: number; keys: string[] } {
  return {
  size: this.cache.size,
  keys: Array.from(this.cache.keys())
  };
  }
  }

export const httpCache = new HttpCache();

```

**Uso em Service:**

```typescript
// processoService.ts - COM cache
import { httpCache } from '@/utils/httpCache';

export async function obterDetalhesProcesso(codProcesso: number) {
  const cacheKey = `processo:detalhes:${codProcesso}`;
  
  // ✅ Tentar cache primeiro
  const cached = httpCache.get<ProcessoDetalhado>(cacheKey);
  if (cached) {
    logger.debug('Cache hit:', cacheKey);
    return cached;
  }
  
  // ❌ Cache miss - buscar da API
  logger.debug('Cache miss:', cacheKey);
  const url = `/api/processos/${codProcesso}/detalhes`;
  const data = await apiClient.get<ProcessoDetalhado>(url);
  
  // Armazenar no cache (5 minutos)
  httpCache.set(cacheKey, data, 5 * 60 * 1000);
  
  return data;
}

// Invalidar cache quando criar/atualizar
export async function atualizarProcesso(
  codProcesso: number,
  request: AtualizarProcessoRequest
) {
  const data = await apiClient.put(`/api/processos/${codProcesso}`, request);
  
  // ✅ Invalidar cache após atualização
  httpCache.invalidate(`processo:${codProcesso}`);
  
  return data;
}
```

### Passos para Execução por IA

**ANTES DE IMPLEMENTAR:**

1. **Medir baseline de performance:**
   ```bash
   # Executar testes E2E com medição de performance
   npm run test:e2e
   
   # Analisar métricas de navegação
   # Procurar por latências > 500ms
   ```

2. **Validar necessidade:**
    - Há navegação frequente entre mesmas páginas?
    - Latência é perceptível (> 500ms)?
    - Usuários reclamam de lentidão?

**SE necessidade confirmada:**

1. **Criar httpCache.ts:**
   ```bash
   create /home/runner/work/sgc/sgc/frontend/src/utils/httpCache.ts
   ```

2. **Adicionar export em utils/index.ts:**
   ```typescript
   export { httpCache } from './httpCache';
   ```

3. **Implementar em services seletivamente:**
    - Começar com processoService (mais usado)
    - Medir impacto
    - Expandir se benefício for significativo

4. **Adicionar invalidação:**
    - Em todos os métodos de criação/atualização/exclusão
    - Testar que cache é invalidado corretamente

5. **Medir performance DEPOIS:**
   ```bash
   npm run test:e2e
   # Comparar com baseline
   # Ganho deve ser > 20% para justificar complexidade
   ```

### Critérios de Validação

- ✅ Cache implementado apenas em endpoints frequentes
- ✅ TTL configurado apropriadamente
- ✅ Invalidação funciona corretamente
- ✅ Performance melhorada em > 20% (medida)
- ✅ Testes E2E passam
- ✅ Nenhuma regressão funcional
- ✅ Cache pode ser facilmente desabilitado (feature flag)

### Decisão de Implementação

**✅ IMPLEMENTE SE:**

- Performance medida mostra latência > 500ms
- Benefício medido > 20%
- Usuários relatam lentidão

**❌ NÃO IMPLEMENTE SE:**

- Performance atual é aceitável
- Benefício < 20%
- Complexidade > Benefício

---

## 🎯 Ação #13: Adicionar @EntityGraph Onde Apropriado

### ⚠️ Critérios de Implementação

**IMPLEMENTE APENAS SE:**

- ✅ Problema N+1 **comprovado** em logs
- ✅ Performance degradada **medida** (> 500ms)
- ✅ Solução alternativa (JOIN FETCH) não é viável
- ✅ Testes demonstram melhoria > 30%

**NÃO IMPLEMENTE SE:**

- ❌ Nenhum problema N+1 identificado
- ❌ Performance atual é aceitável
- ❌ JOIN FETCH resolve o problema

### Contexto

`@EntityGraph` é uma alternativa ao `JOIN FETCH` que permite definir quais relacionamentos carregar sem escrever queries
JPQL customizadas. É útil quando:

- Mesma entidade precisa ser carregada de formas diferentes
- Queries derivadas do Spring Data são preferíveis

### Análise de Necessidade

**Problemas N+1 Potenciais (verificar em logs):**

```sql
-- Exemplo de N+1 (verificar se acontece):
SELECT * FROM processo WHERE situacao = 'EM_ANDAMENTO';
-- Se retornar 10 processos, e cada um tiver participantes lazy:
SELECT * FROM participante WHERE processo_id = 1;
SELECT * FROM participante WHERE processo_id = 2;
...
SELECT * FROM participante WHERE processo_id = 10;
-- Total: 11 queries (1 + 10)
```

**Como Identificar:**

1. Habilitar log SQL:
   ```properties
   # application.properties
   spring.jpa.show-sql=true
   logging.level.org.hibernate.SQL=DEBUG
   logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
   ```

2. Executar fluxo suspeito
3. Contar queries
4. Se N+1 confirmado, considerar @EntityGraph

### Solução (SE implementar)

**Exemplo - ProcessoRepo com @EntityGraph:**

```java
public interface ProcessoRepo extends JpaRepository<Processo, Long> {
    
    // Método base (sem fetch)
    List<Processo> findBySituacao(SituacaoProcesso situacao);
    
    // Com participantes (usando @EntityGraph)
    @EntityGraph(attributePaths = {"participantes"})
    List<Processo> findBySituacaoWithParticipantes(SituacaoProcesso situacao);
    
    // Com múltiplos relacionamentos
    @EntityGraph(attributePaths = {"participantes", "subprocessos"})
    List<Processo> findBySituacaoWithAll(SituacaoProcesso situacao);
    
    // Named Entity Graph (alternativa)
    @EntityGraph(value = "Processo.completo")
    List<Processo> findBySituacao(SituacaoProcesso situacao);
}
```

**Definindo Named Entity Graph na Entidade:**

```java
@Entity
@Table(name = "PROCESSO")
@NamedEntityGraph(
    name = "Processo.completo",
    attributeNodes = {
        @NamedAttributeNode("participantes"),
        @NamedAttributeNode("subprocessos")
    }
)
public class Processo {
    // ...
}
```

### Passos para Execução por IA

**ANTES DE IMPLEMENTAR:**

1. **Habilitar logs SQL:**
   ```bash
   edit backend/src/main/resources/application.properties
   # Adicionar:
   # spring.jpa.show-sql=true
   # logging.level.org.hibernate.SQL=DEBUG
   ```

2. **Executar testes e analisar logs:**
   ```bash
   ./gradlew :backend:test --tests "*Processo*" > test_output.txt
   grep -c "SELECT" test_output.txt
   # Contar queries - se N+1 confirmado, prosseguir
   ```

**SE N+1 confirmado:**

1. **Adicionar @EntityGraph seletivamente:**
   ```bash
   edit backend/src/main/java/sgc/processo/model/ProcessoRepo.java
   # Adicionar método com @EntityGraph
   ```

2. **Atualizar Services para usar novo método:**
   ```bash
   edit backend/src/main/java/sgc/processo/service/ProcessoService.java
   # Substituir findBySituacao() por findBySituacaoWithParticipantes()
   ```

3. **Medir performance DEPOIS:**
   ```bash
   ./gradlew :backend:test --tests "*Processo*" > test_output_after.txt
   # Comparar número de queries
   # Deve reduzir de N+1 para 1
   ```

### Critérios de Validação

- ✅ Problema N+1 identificado e confirmado
- ✅ @EntityGraph resolve o problema
- ✅ Redução de queries medida (N+1 → 1)
- ✅ Performance melhorada > 30%
- ✅ Testes passam
- ✅ Nenhuma regressão

### Decisão de Implementação

**✅ IMPLEMENTE SE:**

- N+1 confirmado em logs
- Performance degradada medida
- Melhoria > 30% demonstrada

**❌ NÃO IMPLEMENTE SE:**

- Nenhum N+1 identificado
- Performance atual aceitável
- Complexidade > Benefício

---

## 🎯 Ação #14: Decompor processos.ts Store (345 linhas)

### ⚠️ Critérios de Implementação

**IMPLEMENTE APENAS SE:**

- ✅ Manutenção do store se tornou **difícil**
- ✅ Múltiplos desenvolvedores trabalhando no mesmo arquivo causam **conflitos**
- ✅ Testes se tornaram **complexos** demais
- ✅ Store claramente violando **SRP**

**NÃO IMPLEMENTE SE:**

- ❌ Store funciona bem (sem reclamações)
- ❌ Apenas um desenvolvedor trabalhando
- ❌ Testes são simples e claros
- ❌ "Apenas porque tem 345 linhas" (tamanho sozinho não é problema)

### Contexto

`processos.ts` tem **345 linhas** e **6 responsabilidades**, mas isso só é um problema se causar dificuldades práticas
de manutenção. Para um sistema com 20 usuários e um ou dois desenvolvedores, pode ser perfeitamente aceitável.

### Análise de Necessidade

**Responsabilidades Atuais:**

1. Lista de processos (filtros, paginação)
2. Detalhes de processo (cache local)
3. Ações de workflow (iniciar, finalizar)
4. Gerenciamento de participantes
5. Contexto completo (processo + subprocessos + unidades)
6. Helpers (flatten, mapeamento)

**Perguntas para Decidir:**

- Há conflitos frequentes em Git?
- Testes são difíceis de escrever/manter?
- Desenvolvedores se perdem no código?
- Store muda frequentemente (> 1x por semana)?

**SE TODAS AS RESPOSTAS FOREM NÃO:** Não implementar.

### Solução (SE implementar)

**Estrutura Proposta:**

```
frontend/src/stores/
├── processos/
│   ├── index.ts                    (Re-export)
│   ├── processosCoreStore.ts       (~150 linhas - lista, filtros, paginação)
│   ├── processosWorkflowStore.ts   (~100 linhas - workflow, ações)
│   └── processosContextStore.ts    (~100 linhas - contexto completo)
```

**Decomposição:**

#### processosCoreStore.ts (~150 linhas)

```typescript
/**
 * Store principal para listagem e CRUD de processos.
 */
export const useProcessosCoreStore = defineStore('processos-core', () => {
  const processos = ref<Processo[]>([]);
  const filtro = ref<FiltroProcesso>({});
  const paginacao = ref<Paginacao>({ page: 1, perPage: 20 });
  
  async function buscarProcessos(filtro: FiltroProcesso) {
    // Lógica de busca
  }
  
  async function criarProcesso(request: CriarProcessoRequest) {
    // Lógica de criação
  }
  
  return { processos, filtro, paginacao, buscarProcessos, criarProcesso };
});
```

#### processosWorkflowStore.ts (~100 linhas)

```typescript
/**
 * Store para ações de workflow de processos.
 */
export const useProcessosWorkflowStore = defineStore('processos-workflow', () => {
  async function iniciarProcesso(codigo: number) {
    // Lógica de iniciar
  }
  
  async function finalizarProcesso(codigo: number) {
    // Lógica de finalizar
  }
  
  return { iniciarProcesso, finalizarProcesso };
});
```

#### processosContextStore.ts (~100 linhas)

```typescript
/**
 * Store para contexto completo de processos.
 */
export const useProcessosContextStore = defineStore('processos-context', () => {
  const contextoCompleto = ref<ProcessoContexto | null>(null);
  
  async function carregarContextoCompleto(codigo: number) {
    // Carrega processo + subprocessos + unidades
  }
  
  return { contextoCompleto, carregarContextoCompleto };
});
```

#### index.ts (Re-export para compatibilidade)

```typescript
/**
 * Re-exporta stores de processos para manter compatibilidade.
 */
export * from './processosCoreStore';
export * from './processosWorkflowStore';
export * from './processosContextStore';

// Alias para manter compatibilidade com código existente
export { useProcessosCoreStore as useProcessosStore };
```

### Passos para Execução por IA

**ANTES DE IMPLEMENTAR:**

1. **Validar necessidade com equipe:**
    - Há problemas práticos de manutenção?
    - Há conflitos em Git?
    - Vale a pena o esforço?

**SE necessidade confirmada:**

1. **Criar pasta processos:**
   ```bash
   mkdir -p /home/runner/work/sgc/sgc/frontend/src/stores/processos
   ```

2. **Mover e dividir processos.ts:**
   ```bash
   # Criar os 3 stores separados
   # Copiar métodos relevantes para cada um
   ```

3. **Criar index.ts com re-exports:**
   ```bash
   create /home/runner/work/sgc/sgc/frontend/src/stores/processos/index.ts
   ```

4. **Atualizar imports em componentes:**
   ```bash
   # Buscar usos de useProcessosStore
   grep -r "useProcessosStore" frontend/src/ --include="*.vue" --include="*.ts"
   
   # Atualizar imports conforme necessário
   ```

5. **Executar testes:**
   ```bash
   npm run typecheck
   npm run test:unit
   npm run test:e2e
   ```

### Critérios de Validação

- ✅ 3 stores especializados criados
- ✅ Cada store < 200 linhas
- ✅ Re-exports mantêm compatibilidade
- ✅ Testes passam (100%)
- ✅ Nenhuma regressão
- ✅ Manutenção mais fácil (validar com equipe)

### Decisão de Implementação

**✅ IMPLEMENTE SE:**

- Problemas práticos de manutenção confirmados
- Conflitos frequentes em Git
- Equipe concorda que vale a pena

**❌ NÃO IMPLEMENTE SE:**

- Store funciona bem como está
- Nenhum problema prático
- Esforço > Benefício

---

## 📊 Checklist de Validação da Sprint 4

**IMPORTANTE:** Validar necessidade ANTES de implementar cada ação.

### Antes de Implementar QUALQUER Ação

- [ ] ✅ Problema identificado e **medido**
- [ ] ✅ Benefício estimado > 20%
- [ ] ✅ Equipe concorda que vale a pena
- [ ] ✅ Alternativa mais simples não existe

### Após Implementar

- [ ] ✅ Benefício **medido** (não assumido)
- [ ] ✅ Performance melhorada conforme esperado
- [ ] ✅ Testes passam (100%)
- [ ] ✅ Nenhuma regressão
- [ ] ✅ Complexidade adicionada justificada

---

## 📈 Métricas de Sucesso

**Esta sprint é OPCIONAL** - métricas só fazem sentido SE implementada.

**SE Ação #9 implementada:**

- ✅ Redução de requisições HTTP: 10-20%
- ✅ Latência reduzida: 20-30%
- ✅ Cache hit rate: > 40%

**SE Ação #13 implementada:**

- ✅ Queries N+1 eliminadas: 100%
- ✅ Performance de listagens: +30-50%

**SE Ação #14 implementada:**

- ✅ Manutenibilidade melhorada (subjetivo, validar com equipe)
- ✅ Conflitos Git reduzidos
- ✅ Stores < 200 linhas cada

---

## 🚀 Conclusão da Sprint 4

**Lembre-se:**

- Esta sprint é **OPCIONAL**
- Implemente apenas com **necessidade demonstrada**
- **Meça antes e depois**
- **Complexidade > Benefício?** Não implemente!

**Princípios:**

- **YAGNI:** You Aren't Gonna Need It
- **KISS:** Keep It Simple, Stupid
- **Measure, Don't Assume**

---

## 📚 Documentação Relacionada

- [optimization-report.md](./optimization-report.md) - Relatório completo
- [refactoring-tracker.md](./refactoring-tracker.md) - Tracking de progresso
- [backend-sprint-1.md](./backend-sprint-1.md) - Sprint 1
- [frontend-sprint-2.md](./frontend-sprint-2.md) - Sprint 2
- [backend-sprint-3.md](./backend-sprint-3.md) - Sprint 3

---

**Versão:** 1.0  
**Data de Criação:** 26 de Janeiro de 2026  
**Status:** 🔵 Planejada (Opcional)
