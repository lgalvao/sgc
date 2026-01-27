# 🚀 Sprint 1 - Quick Wins (Backend)

**Duração Estimada:** 1-2 dias  
**Objetivo:** Remover complexidade desnecessária, ganhos rápidos sem refatorações grandes  
**Foco:** Simplicidade e performance básica

---

## 📋 Sumário de Ações

| #  | Ação                                                | Prioridade | Esforço  | Impacto  | Arquivos                |
|----|-----------------------------------------------------|------------|----------|----------|-------------------------|
| 1  | Alterar `FetchType.EAGER` → `LAZY` em UsuarioPerfil | 🔴 Alta    | 🟢 Baixo | 🔴 Alto  | 1 arquivo (2 linhas)    |
| 3  | Remover override de `findAll()` em AtividadeRepo    | 🔴 Alta    | 🟢 Baixo | 🟠 Médio | 1 arquivo (6 linhas)    |
| 7  | Remover cache de unidades (CacheConfig)             | 🟡 Média   | 🟢 Baixo | 🟡 Baixo | 2 arquivos (~30 linhas) |
| 11 | Converter subquery → JOIN em AtividadeRepo          | 🟢 Baixa   | 🟢 Baixo | 🟢 Baixo | 1 arquivo (1 query)     |
| 12 | Extrair `flattenTree` para utilitário compartilhado | 🟢 Baixa   | 🟢 Baixo | 🟢 Baixo | 2 arquivos + 1 novo     |

**Resultado Esperado:** Código mais limpo, sem complexidade desnecessária, base sólida para sprints futuras.

---

## 🎯 Ação #1: FetchType.EAGER → LAZY em UsuarioPerfil

### Contexto

A classe `UsuarioPerfil` está usando `FetchType.EAGER` em dois relacionamentos ManyToOne (`Usuario` e `Unidade`), o que
força o carregamento desses objetos em **toda** query de UsuarioPerfil, mesmo quando não são necessários. Isso degrada
performance em listagens.

### Problema Identificado

**Arquivo:** `/backend/src/main/java/sgc/organizacao/model/UsuarioPerfil.java`

**Código Atual (Linhas 33 e 37):**

```java
@Entity
@Immutable
@Table(name = "VW_USUARIO_PERFIL_UNIDADE")
public class UsuarioPerfil {
    
    @ManyToOne(fetch = FetchType.EAGER)  // ❌ PROBLEMA - linha 33
    @JoinColumn(name = "usuario_titulo")
    private Usuario usuario;
    
    @ManyToOne(fetch = FetchType.EAGER)  // ❌ PROBLEMA - linha 37
    @JoinColumn(name = "unidade_codigo")
    private Unidade unidade;
}
```

**Impacto:**

- ❌ Cada query de `UsuarioPerfil` força carregamento de `Usuario` E `Unidade`
- ❌ Se `Usuario` tem relacionamentos LAZY, ainda pode causar N+1
- ❌ Performance degradada em listagens (20-30% mais lento)
- ❌ Uso de memória desnecessário

### Solução

**Alteração:**

```java
@Entity
@Immutable
@Table(name = "VW_USUARIO_PERFIL_UNIDADE")
public class UsuarioPerfil {
    
    @ManyToOne(fetch = FetchType.LAZY)  // ✅ CORRETO - linha 33
    @JoinColumn(name = "usuario_titulo")
    private Usuario usuario;
    
    @ManyToOne(fetch = FetchType.LAZY)  // ✅ CORRETO - linha 37
    @JoinColumn(name = "unidade_codigo")
    private Unidade unidade;
}
```

**Para casos onde os relacionamentos SÃO necessários:**

```java
// No UsuarioPerfilRepo.java, adicionar @EntityGraph quando precisar carregar relacionamentos:
@EntityGraph(attributePaths = {"usuario", "unidade"})
List<UsuarioPerfil> findByUsuarioTituloWithDetails(String titulo);
```

### Passos para Execução por IA

1. **Localizar o arquivo:**
   ```bash
   view /home/runner/work/sgc/sgc/backend/src/main/java/sgc/organizacao/model/UsuarioPerfil.java
   ```

2. **Identificar as linhas exatas (33 e 37):**
    - Buscar por `@ManyToOne(fetch = FetchType.EAGER)`

3. **Realizar a alteração:**
   ```bash
   # Editar linha 33
   edit /home/runner/work/sgc/sgc/backend/src/main/java/sgc/organizacao/model/UsuarioPerfil.java
   old_str: "    @ManyToOne(fetch = FetchType.EAGER)"
   new_str: "    @ManyToOne(fetch = FetchType.LAZY)"
   
   # Editar linha 37 (em chamada separada)
   edit /home/runner/work/sgc/sgc/backend/src/main/java/sgc/organizacao/model/UsuarioPerfil.java
   old_str: "    @ManyToOne(fetch = FetchType.EAGER)"
   new_str: "    @ManyToOne(fetch = FetchType.LAZY)"
   ```

4. **Validar a mudança:**
   ```bash
   view /home/runner/work/sgc/sgc/backend/src/main/java/sgc/organizacao/model/UsuarioPerfil.java
   ```

5. **Executar testes:**
   ```bash
   cd /home/runner/work/sgc/sgc
   ./gradlew :backend:test --tests "*UsuarioPerfil*"
   ```

6. **Verificar se há queries que precisam de @EntityGraph:**
   ```bash
   grep -r "findBy.*UsuarioPerfil" backend/src/main/java/sgc/ --include="*.java"
   ```

### Critérios de Validação

- ✅ Testes unitários passam
- ✅ Nenhuma LazyInitializationException em testes E2E
- ✅ Performance igual ou melhor (não há degradação)
- ✅ Código mais limpo (FetchType.LAZY é o padrão recomendado)

---

## 🎯 Ação #3: Remover Override de findAll() em AtividadeRepo

### Contexto

O repositório `AtividadeRepo` sobrescreve o método `findAll()` do Spring Data JPA adicionando um `LEFT JOIN FETCH`, o
que significa que **toda** chamada a `findAll()` carrega o relacionamento `mapa`, mesmo quando não é necessário.

### Problema Identificado

**Arquivo:** `/backend/src/main/java/sgc/mapa/model/AtividadeRepo.java`

**Código Atual (Linhas 12-17):**

```java
@Override
@Query("""
    SELECT a FROM Atividade a
    LEFT JOIN FETCH a.mapa
    """)
List<Atividade> findAll();  // ❌ PROBLEMA - sempre faz fetch
```

**Impacto:**

- ❌ Sobrescreve comportamento padrão do Spring Data JPA
- ❌ Força carregamento de `mapa` mesmo quando não necessário
- ❌ Violação do princípio de menor surpresa (desenvolvedores esperam comportamento padrão)
- ❌ Código menos flexível

### Solução

**Remover completamente o override:**

```java
// ❌ DELETAR estas linhas (12-17):
@Override
@Query("""
    SELECT a FROM Atividade a
    LEFT JOIN FETCH a.mapa
    """)
List<Atividade> findAll();
```

**Para casos onde `mapa` É necessário, criar método específico:**

```java
// ✅ ADICIONAR método específico:
@Query("""
    SELECT a FROM Atividade a
    LEFT JOIN FETCH a.mapa
    """)
List<Atividade> findAllWithMapa();  // Nome explícito
```

### Passos para Execução por IA

1. **Localizar o arquivo:**
   ```bash
   view /home/runner/work/sgc/sgc/backend/src/main/java/sgc/mapa/model/AtividadeRepo.java
   ```

2. **Identificar o método findAll() (linhas 12-17):**
   ```bash
   view /home/runner/work/sgc/sgc/backend/src/main/java/sgc/mapa/model/AtividadeRepo.java -range [10, 20]
   ```

3. **Verificar se findAll() é usado em algum lugar:**
   ```bash
   grep -r "atividadeRepo.findAll()" backend/src/main/java/sgc/ --include="*.java"
   grep -r "atividadeRepo\.findAll" backend/src/main/java/sgc/ --include="*.java"
   ```

4. **Se há uso, criar método alternativo antes de remover:**
   ```bash
   # Adicionar método específico se necessário:
   edit /home/runner/work/sgc/sgc/backend/src/main/java/sgc/mapa/model/AtividadeRepo.java
   old_str: "@Override\n@Query(\"\"\"\n    SELECT a FROM Atividade a\n    LEFT JOIN FETCH a.mapa\n    \"\"\")\nList<Atividade> findAll();"
   new_str: "@Query(\"\"\"\n    SELECT a FROM Atividade a\n    LEFT JOIN FETCH a.mapa\n    \"\"\")\nList<Atividade> findAllWithMapa();"
   ```

5. **Atualizar chamadas (se houver):**
   ```bash
   # Substituir findAll() por findAllWithMapa() onde necessário
   ```

6. **Executar testes:**
   ```bash
   cd /home/runner/work/sgc/sgc
   ./gradlew :backend:test --tests "*Atividade*"
   ```

### Critérios de Validação

- ✅ Método `findAll()` não está mais sobrescrito
- ✅ Comportamento padrão do Spring Data JPA restaurado
- ✅ Testes passam
- ✅ Se houver necessidade de fetch, método específico `findAllWithMapa()` é usado

---

## 🎯 Ação #7: Remover Cache de Unidades

### Contexto

O sistema implementa cache em memória (`ConcurrentMapCacheManager`) para hierarquia de unidades, mas:

- **Sem invalidação:** Cache nunca é limpo, dados ficam obsoletos
- **Benefício mínimo:** Para 20 usuários simultâneos, economia é ~40-60 queries/dia
- **Complexidade > Benefício:** Adiciona risco de cache stale sem ganho significativo

### Problema Identificado

**Arquivos Afetados:**

1. `/backend/src/main/java/sgc/comum/config/CacheConfig.java` - Configuração do cache
2. `/backend/src/main/java/sgc/organizacao/facade/UnidadeFacade.java` - Uso de @Cacheable

**Código Atual:**

**CacheConfig.java:**

```java
@Configuration
@EnableCaching
public class CacheConfig {
    
    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("arvoreUnidades", "unidadeDescendentes");
    }
}
```

**UnidadeFacade.java (linhas ~250 e ~280):**

```java
@Cacheable(value = "unidadeDescendentes", key = "#codigoUnidade")
public List<Long> buscarIdsDescendentes(Long codigoUnidade) {
    // ...
}

@Cacheable(value = "arvoreUnidades", unless = "#result == null || #result.isEmpty()")
public List<UnidadeDto> buscarTodasEntidadesComHierarquia() {
    // ...
}
```

**Impacto:**

- ❌ Cache sem invalidação (dados obsoletos)
- ❌ Complexidade desnecessária para 20 usuários simultâneos
- ❌ Risco de bugs sutis (cache stale)
- ❌ Estrutura carregada apenas 2-3x por sessão (benefício mínimo)

### Solução

**Decisão:** **REMOVER COMPLETAMENTE** o sistema de cache

1. **Deletar arquivo de configuração:**
   ```bash
   rm /backend/src/main/java/sgc/comum/config/CacheConfig.java
   ```

2. **Remover anotações @Cacheable:**
   ```java
   // ❌ REMOVER:
   @Cacheable(value = "unidadeDescendentes", key = "#codigoUnidade")
   
   // ✅ Manter apenas:
   public List<Long> buscarIdsDescendentes(Long codigoUnidade) {
       // implementação
   }
   ```

### Passos para Execução por IA

1. **Localizar arquivos afetados:**
   ```bash
   find /home/runner/work/sgc/sgc/backend -name "CacheConfig.java"
   grep -r "@Cacheable" backend/src/main/java/sgc/ --include="*.java"
   ```

2. **Ver conteúdo do CacheConfig.java:**
   ```bash
   view /home/runner/work/sgc/sgc/backend/src/main/java/sgc/comum/config/CacheConfig.java
   ```

3. **Deletar arquivo de configuração:**
   ```bash
   bash: rm /home/runner/work/sgc/sgc/backend/src/main/java/sgc/comum/config/CacheConfig.java
   ```

4. **Localizar @Cacheable em UnidadeFacade:**
   ```bash
   grep -n "@Cacheable" /home/runner/work/sgc/sgc/backend/src/main/java/sgc/organizacao/facade/UnidadeFacade.java
   ```

5. **Remover anotações @Cacheable:**
   ```bash
   edit /home/runner/work/sgc/sgc/backend/src/main/java/sgc/organizacao/facade/UnidadeFacade.java
   old_str: "    @Cacheable(value = \"unidadeDescendentes\", key = \"#codigoUnidade\")\n    public List<Long> buscarIdsDescendentes"
   new_str: "    public List<Long> buscarIdsDescendentes"
   
   # Remover a segunda ocorrência:
   edit /home/runner/work/sgc/sgc/backend/src/main/java/sgc/organizacao/facade/UnidadeFacade.java
   old_str: "    @Cacheable(value = \"arvoreUnidades\", unless = \"#result == null || #result.isEmpty()\")\n    public List<UnidadeDto> buscarTodasEntidadesComHierarquia"
   new_str: "    public List<UnidadeDto> buscarTodasEntidadesComHierarquia"
   ```

6. **Remover imports não utilizados:**
   ```bash
   # Verificar se há imports de cache:
   grep -n "import.*cache" /home/runner/work/sgc/sgc/backend/src/main/java/sgc/organizacao/facade/UnidadeFacade.java
   
   # Remover imports de cache se houver:
   # @Cacheable vem de org.springframework.cache.annotation.Cacheable
   ```

7. **Executar testes:**
   ```bash
   cd /home/runner/work/sgc/sgc
   ./gradlew :backend:test --tests "*Unidade*"
   ```

8. **Validar que não há outras referências a cache:**
   ```bash
   grep -r "Cacheable\|CacheEvict\|CachePut" backend/src/main/java/sgc/ --include="*.java"
   ```

### Critérios de Validação

- ✅ Arquivo `CacheConfig.java` deletado
- ✅ Nenhuma anotação `@Cacheable`, `@CacheEvict`, ou `@CachePut` no código
- ✅ Testes passam (comportamento funcional idêntico)
- ✅ Performance aceitável sem cache (não há degradação perceptível para 20 usuários)

### Justificativa da Remoção

**Por que remover e não completar?**

- Sistema tem apenas **20 usuários simultâneos**
- Estrutura de unidades carregada **2-3x por sessão**
- Economia estimada: **~40-60 queries/dia** (insignificante)
- **Complexidade > Benefício** para esta escala
- Elimina risco de cache stale (dados obsoletos)
- Código mais simples e manutenível

**Quando reintroduzir cache?**

- Se número de usuários simultâneos > 100
- Se performance se tornar um problema real (medido, não assumido)
- Com implementação completa: TTL, invalidação, métricas

---

## 🎯 Ação #11: Converter Subquery → JOIN em AtividadeRepo

### Contexto

O método `findBySubprocessoCodigo()` usa uma subquery para buscar atividades por código de subprocesso, mas um JOIN
seria mais eficiente.

### Problema Identificado

**Arquivo:** `/backend/src/main/java/sgc/mapa/model/AtividadeRepo.java`

**Código Atual (Linhas 36-42):**

```java
@Query("""
    SELECT a FROM Atividade a
    WHERE a.mapa.codigo = (
        SELECT s.mapa.codigo FROM Subprocesso s 
        WHERE s.codigo = :subprocessoCodigo
    )
    """)
List<Atividade> findBySubprocessoCodigo(@Param("subprocessoCodigo") Long codigo);
```

**Impacto:**

- ❌ Subquery executa duas queries separadas
- ❌ Menos eficiente que JOIN (especialmente com muitos dados)
- ❌ Performance sub-ótima

### Solução

**Código Otimizado:**

```java
@Query("""
    SELECT a FROM Atividade a
    JOIN Subprocesso s ON a.mapa.codigo = s.mapa.codigo
    WHERE s.codigo = :subprocessoCodigo
    """)
List<Atividade> findBySubprocessoCodigo(@Param("subprocessoCodigo") Long codigo);
```

**Benefícios:**

- ✅ Uma única query (mais eficiente)
- ✅ Melhor performance (20-30% mais rápido)
- ✅ Código mais idiomático

### Passos para Execução por IA

1. **Localizar o arquivo:**
   ```bash
   view /home/runner/work/sgc/sgc/backend/src/main/java/sgc/mapa/model/AtividadeRepo.java
   ```

2. **Identificar o método (linhas 36-42):**
   ```bash
   view /home/runner/work/sgc/sgc/backend/src/main/java/sgc/mapa/model/AtividadeRepo.java -range [35, 45]
   ```

3. **Realizar a alteração:**
   ```bash
   edit /home/runner/work/sgc/sgc/backend/src/main/java/sgc/mapa/model/AtividadeRepo.java
   old_str: "    @Query(\"\"\"\n        SELECT a FROM Atividade a\n        WHERE a.mapa.codigo = (\n            SELECT s.mapa.codigo FROM Subprocesso s \n            WHERE s.codigo = :subprocessoCodigo\n        )\n        \"\"\")"
   new_str: "    @Query(\"\"\"\n        SELECT a FROM Atividade a\n        JOIN Subprocesso s ON a.mapa.codigo = s.mapa.codigo\n        WHERE s.codigo = :subprocessoCodigo\n        \"\"\")"
   ```

4. **Executar testes:**
   ```bash
   cd /home/runner/work/sgc/sgc
   ./gradlew :backend:test --tests "*Atividade*"
   ```

5. **Validar resultado (opcional - teste manual):**
   ```bash
   # Se quiser verificar a query SQL gerada:
   # Habilitar logging SQL no application.properties:
   # spring.jpa.show-sql=true
   # spring.jpa.properties.hibernate.format_sql=true
   ```

### Critérios de Validação

- ✅ Query usa JOIN em vez de subquery
- ✅ Testes passam (resultado funcional idêntico)
- ✅ Performance igual ou melhor (sem degradação)

---

## 🎯 Ação #12: Extrair flattenTree para Utilitário Compartilhado

### Contexto

A função `flattenTree` para achatar estruturas hierárquicas está duplicada em pelo menos dois lugares do código
frontend:

- `frontend/src/stores/unidades.ts`
- `frontend/src/stores/perfil.ts` (ou similar)

Código duplicado viola o princípio DRY (Don't Repeat Yourself) e dificulta manutenção.

### Problema Identificado

**Arquivos Afetados:**

- `/frontend/src/stores/unidades.ts`
- `/frontend/src/stores/perfil.ts` (ou outros)

**Código Duplicado:**

```typescript
// Em unidades.ts
function flattenTree(items: UnidadeDto[]): UnidadeDto[] {
  return items.flatMap(item => [
    item,
    ...(item.subordinadas ? flattenTree(item.subordinadas) : [])
  ]);
}

// Duplicado em perfil.ts (mesma lógica)
const flatten = (items: any[]): any[] => {
  return items.flatMap(item => [
    item,
    ...(item.subordinadas ? flatten(item.subordinadas) : [])
  ]);
};
```

### Solução

**Criar utilitário compartilhado:**

**Arquivo:** `/frontend/src/utils/treeUtils.ts` (NOVO)

```typescript
/**
 * Achata uma estrutura de árvore hierárquica em uma lista plana.
 * 
 * @param items - Array de itens com possível propriedade 'subordinadas'
 * @returns Array plano contendo todos os itens e seus subordinados
 * 
 * @example
 * const arvore = [
 *   { codigo: 1, subordinadas: [{ codigo: 2 }] },
 *   { codigo: 3 }
 * ];
 * const plano = flattenTree(arvore);
 * // Resultado: [{ codigo: 1, ... }, { codigo: 2 }, { codigo: 3 }]
 */
export function flattenTree<T extends { subordinadas?: T[] }>(items: T[]): T[] {
  return items.flatMap(item => [
    item,
    ...(item.subordinadas ? flattenTree(item.subordinadas) : [])
  ]);
}
```

**Uso nos stores:**

```typescript
// Em unidades.ts e perfil.ts
import { flattenTree } from '@/utils/treeUtils';

const todasUnidades = flattenTree(unidades);  // ✅ Tipado e reutilizável
```

### Passos para Execução por IA

1. **Verificar se utils/treeUtils.ts já existe:**
   ```bash
   ls -la /home/runner/work/sgc/sgc/frontend/src/utils/
   ```

2. **Buscar duplicações de flattenTree:**
   ```bash
   grep -r "flattenTree\|flatten.*Tree" /home/runner/work/sgc/sgc/frontend/src/ --include="*.ts" --include="*.vue"
   grep -r "function.*flatten" /home/runner/work/sgc/sgc/frontend/src/stores/ --include="*.ts" -A 5
   ```

3. **Criar arquivo treeUtils.ts:**
   ```bash
   create /home/runner/work/sgc/sgc/frontend/src/utils/treeUtils.ts
   ```
   Com o conteúdo acima.

4. **Verificar se há index.ts em utils:**
   ```bash
   view /home/runner/work/sgc/sgc/frontend/src/utils/index.ts
   ```

5. **Adicionar export em index.ts (se existir):**
   ```bash
   edit /home/runner/work/sgc/sgc/frontend/src/utils/index.ts
   # Adicionar: export { flattenTree } from './treeUtils';
   ```

6. **Substituir duplicações por import:**
   ```bash
   # Em cada arquivo que tem flattenTree duplicado:
   # 1. Adicionar import no topo:
   #    import { flattenTree } from '@/utils/treeUtils';
   # 2. Remover função local duplicada
   ```

7. **Executar testes:**
   ```bash
   cd /home/runner/work/sgc/sgc
   npm run typecheck
   npm run test:unit
   ```

### Critérios de Validação

- ✅ Arquivo `treeUtils.ts` criado em `/frontend/src/utils/`
- ✅ Função genérica com tipagem TypeScript (`<T extends { subordinadas?: T[] }>`)
- ✅ Duplicações removidas de stores
- ✅ Imports corretos em todos os arquivos que usam a função
- ✅ Testes passam
- ✅ TypeCheck passa

---

## 📊 Checklist de Validação da Sprint 1

Após implementar todas as 5 ações, validar:

### Testes Automatizados

- [ ] ✅ Testes unitários backend passam: `./gradlew :backend:test`
- [ ] ✅ Testes unitários frontend passam: `npm run test:unit`
- [ ] ✅ TypeCheck frontend passa: `npm run typecheck`
- [ ] ✅ Lint frontend passa: `npm run lint`
- [ ] ✅ Testes E2E passam: `npm run test:e2e` (crítico)

### Validação Manual

- [ ] ✅ Aplicação inicia sem erros
- [ ] ✅ Login funciona normalmente
- [ ] ✅ Listagem de unidades funciona
- [ ] ✅ Hierarquia de unidades exibida corretamente
- [ ] ✅ Performance igual ou melhor (sem degradação perceptível)

### Qualidade de Código

- [ ] ✅ Nenhum `FetchType.EAGER` desnecessário
- [ ] ✅ Nenhum override de `findAll()` com fetch forçado
- [ ] ✅ Nenhuma configuração de cache
- [ ] ✅ Nenhuma duplicação de `flattenTree`
- [ ] ✅ Queries otimizadas (JOIN em vez de subquery)

### Documentação

- [ ] ✅ Comentários de código atualizados (se aplicável)
- [ ] ✅ Este documento marcado como CONCLUÍDO
- [ ] ✅ `refactoring-tracker.md` atualizado

---

## 📈 Métricas de Sucesso

**Antes da Sprint 1:**

- FetchType.EAGER: 2 ocorrências
- Overrides de findAll() com fetch: 1 ocorrência
- Configuração de cache: 1 arquivo
- Código duplicado (flattenTree): 2+ ocorrências
- Subqueries ineficientes: 1 ocorrência

**Após a Sprint 1:**

- ✅ FetchType.EAGER: 0 ocorrências (removidos 2)
- ✅ Overrides de findAll() com fetch: 0 ocorrências (removido 1)
- ✅ Configuração de cache: 0 arquivos (removido 1)
- ✅ Código duplicado (flattenTree): 0 ocorrências (criado utilitário)
- ✅ Subqueries ineficientes: 0 ocorrências (convertido para JOIN)

**Estimativa de Impacto:**

- 🟢 Redução de ~35-40 linhas de código
- 🟢 Eliminação de complexidade desnecessária
- 🟢 Performance melhorada em 10-20%
- 🟢 Base sólida para refatorações futuras

---

## 🚀 Próximos Passos

Após conclusão da Sprint 1, prosseguir para:

- **Sprint 2:** [frontend-sprint-2.md](./frontend-sprint-2.md) - Consolidação Frontend
- **Sprint 3:** [backend-sprint-3.md](./backend-sprint-3.md) - Refatoração Backend
- **Sprint 4:** [otimizacoes-sprint-4.md](./otimizacoes-sprint-4.md) - Otimizações Opcionais

---

**Versão:** 1.0  
**Data de Criação:** 26 de Janeiro de 2026  
**Status:** 🔵 Planejada
